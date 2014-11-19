// DiskDriver.java
//	Class for synchronous access of the disk.  The physical disk 
//	is an asynchronous device (disk requests return immediately, and
//	an interrupt happens later on).  This is a layer on top of
//	the disk providing a synchronous interface (requests wait until
//	the request completes).
//
//	Uses a semaphore to synchronize the interrupt handlers with the
//	pending requests.  And, because the physical disk can only
//	handle one operation at a time, uses a lock to enforce mutual
//	exclusion.
//
// Copyright (c) 1992-1993 The Regents of the University of California.
// Copyright (c) 1998 Rice University.
// Copyright (c) 2003 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and 
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.devices;

import java.util.ArrayList;
import java.util.HashMap;

import nachos.Debug;
import nachos.machine.CPU;
import nachos.machine.MIPS;
import nachos.machine.Machine;
import nachos.machine.Disk;
import nachos.machine.InterruptHandler;
import nachos.util.BufferArrayList;
import nachos.util.FixedBuffer;
import nachos.kernel.filesys.CacheSector;
import nachos.kernel.filesys.ReadWriteRequest;
import nachos.kernel.threads.Semaphore;
import nachos.kernel.threads.Lock;

/**
 * This class defines a "synchronous" disk abstraction. As with other I/O
 * devices, the raw physical disk is an asynchronous device -- requests to read
 * or write portions of the disk return immediately, and an interrupt occurs
 * later to signal that the operation completed. (Also, the physical
 * characteristics of the disk device assume that only one operation can be
 * requested at a time).
 *
 * This driver provides the abstraction of "synchronous I/O": any request blocks
 * the calling thread until the requested operation has finished.
 * 
 * @author Thomas Anderson (UC Berkeley), original C++ version
 * @author Peter Druschel (Rice University), Java translation
 * @author Eugene W. Stark (Stony Brook University)
 */
public class CacheDriver
{

    /** Raw disk device. */
    private Disk disk;
    private DiskDriver diskDriver;

    /** To synchronize requesting thread with the interrupt handler. */
    private Semaphore semaphore;

    /** Only one read/write request can be sent to the disk at a time. */
    private Lock cacheLock;

    /** Buffer used to store request objects **/

    private ArrayList<ReadWriteRequest> requestQueue;
    private static final int MAX_SIZE = 10;

    /** Map used to store cache objects **/
    private BufferArrayList cache;

    /** Flag used to indicate if the disk is busy **/
    boolean isDiskBusy;

    /**
     * Initialize the synchronous interface to the physical disk, in turn
     * initializing the physical disk.
     * 
     * @param unit
     *            The disk unit to be handled by this driver.
     */
    public CacheDriver(int unit)
    {
        semaphore = new Semaphore("synch disk", 0);
        cacheLock = new Lock("synch disk lock");
        disk = Machine.getDisk(unit);
        diskDriver = new DiskDriver();
        cache = new BufferArrayList();
        requestQueue = new ArrayList<>();
    }

    /**
     * Get the total number of sectors on the disk.
     * 
     * @return the total number of sectors on the disk.
     */
    public int getNumSectors()
    {
        return disk.geometry.NumSectors;
    }

    /**
     * Get the sector size of the disk, in bytes.
     * 
     * @return the sector size of the disk, in bytes.
     */
    public int getSectorSize()
    {
        return disk.geometry.SectorSize;
    }

    /**
     * Read the contents of a disk sector into a buffer. Return only after the
     * data has been read.
     *
     * @param sectorNumber
     *            The disk sector to read.
     * @param data
     *            The buffer to hold the contents of the disk sector.
     * @param index
     *            Offset in the buffer at which to place the data.
     */
    public void readSector(int sectorNumber, byte[] data, int index)
    {
        CacheSector entry;

        Debug.ASSERT(0 <= sectorNumber && sectorNumber < getNumSectors());
        cacheLock.acquire(); // only one disk I/O at a time

        entry = cache.getBySector(sectorNumber);
        if (entry == null)
        {
            entry = new CacheSector(sectorNumber, data);
            cache.stuffIntoBuff(entry);
        }

        entry.reserve();
        cacheLock.release();

        if (!entry.isValid())
        {
            diskDriver.readRequest(entry, index);
            entry.setValid();
        }

        data = entry.getData();
        entry.release();
        // disk.readRequest(sectorNumber, data, index);
    }

    /**
     * Write the contents of a buffer into a disk sector. Return only after the
     * data has been written.
     *
     * @param sectorNumber
     *            The disk sector to be written.
     * @param data
     *            The new contents of the disk sector.
     * @param index
     *            Offset in the buffer from which to get the data.
     */
    public void writeSector(int sectorNumber, byte[] data, int index)
    {
        CacheSector entry;

        Debug.ASSERT(0 <= sectorNumber && sectorNumber < getNumSectors());

        cacheLock.acquire(); // only one disk I/O at a time
        entry = cache.getBySector(sectorNumber);
//        byte[] newByte = new byte[disk.geometry.SectorSize];
//        System.arraycopy(data, index, newByte, 0, disk.geometry.SectorSize);
        
        if (entry != null)
        {
            
            // we write through
            
            
            Debug.print('4', "Write Hit");

            entry.reserve();
            entry.setData(data);
            cache.sendToTheFront(entry);
            
            // cache.stuffIntoBuff(entry);
             cacheLock.release();
             diskDriver.writeRequest(entry, index);

        } else
        {
            Debug.print('4', "Write Miss");

            entry = new CacheSector(sectorNumber, data);
            cache.stuffIntoBuff(entry);
            entry.reserve();
            cacheLock.release();
            diskDriver.writeRequest(entry, index);

        }
        entry.release();
        // entry.reserve();

        // cacheLock.release();
        // disk.writeRequest(sectorNumber, data, index);
        // semaphore.P(); // wait for interrupt
    }

    class DiskDriver
    {
        Lock diskLock;
        Lock queueLock;
        Semaphore diskSemaphore;
        ReadWriteRequest currentRequest;

        DiskDriver()
        {
            diskLock = new Lock("Disk Lock");
            queueLock = new Lock("Queue Lock");
            diskSemaphore = new Semaphore("Disk Sempahore", 0);
            disk.setHandler(new DiskIntHandler());
            isDiskBusy = false;
        }

        public void writeRequest(CacheSector entry, int index)
        {
            queueLock.acquire();
//            int oldLevel = CPU.setLevel(CPU.IntOff);
            ReadWriteRequest diskRequest = new ReadWriteRequest(entry.getSectorNumber(), entry.getData(), index);
            requestQueue.add(diskRequest);
//            CPU.setLevel(oldLevel);
            queueLock.release();
            
            startDisk(diskRequest, false);
        }

        public void readRequest(CacheSector entry, int index)
        {
            queueLock.acquire();
//            int oldLevel = CPU.setLevel(CPU.IntOff);
            ReadWriteRequest diskRequest = new ReadWriteRequest(entry.getSectorNumber(), entry.getData(), index);
            requestQueue.add(diskRequest);
//            CPU.setLevel(oldLevel);
            queueLock.release();
            
            startDisk(diskRequest, true);
            
        }

        private void startDisk(ReadWriteRequest diskRequest, boolean read)
        {
//            diskLock.acquire();
            currentRequest = diskRequest;
            if(isDiskBusy == true)
                diskRequest.p();
            
            isDiskBusy = true;
            
            queueLock.acquire();
//            currentRequest = diskRequest
            queueLock.release();
            
            
            if(read)
                disk.readRequest(diskRequest.getSectorNumber(), diskRequest.getData(), diskRequest.getIndex());
            else
                disk.writeRequest(diskRequest.getSectorNumber(), diskRequest.getData(), diskRequest.getIndex());
            
            diskSemaphore.P();
//            diskLock.release();
        }

        /**
         * DiskDriver interrupt handler class.
         */
        private class DiskIntHandler implements InterruptHandler
        {
            /**
             * When the disk interrupts, just wake up the thread that issued the
             * request that just finished.
             */
            public void handleInterrupt()
            {
                isDiskBusy = false;
                diskSemaphore.V();
                currentRequest.v();
            }
        }

    }
}
