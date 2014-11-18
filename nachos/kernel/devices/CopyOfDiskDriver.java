// DiskDriver.java
//  Class for synchronous access of the disk.  The physical disk 
//  is an asynchronous device (disk requests return immediately, and
//  an interrupt happens later on).  This is a layer on top of
//  the disk providing a synchronous interface (requests wait until
//  the request completes).
//
//  Uses a semaphore to synchronize the interrupt handlers with the
//  pending requests.  And, because the physical disk can only
//  handle one operation at a time, uses a lock to enforce mutual
//  exclusion.
//
// Copyright (c) 1992-1993 The Regents of the University of California.
// Copyright (c) 1998 Rice University.
// Copyright (c) 2003 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and 
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.devices;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import nachos.Debug;
import nachos.machine.Machine;
import nachos.machine.Disk;
import nachos.machine.InterruptHandler;
import nachos.util.FixedBuffer;
import nachos.kernel.filesys.CacheSector;
import nachos.kernel.filesys.ReadWriteRequest;
import nachos.kernel.filesys.ReadWriteRequestComparator;
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
public class CopyOfDiskDriver
{

    /** Raw disk device. */
    private Disk disk;

    ReadWriteRequest currentRequest;

    /** Only one read/write request can be sent to the disk at a time. */
    private Lock queueLock;
    private Lock diskLock;
    private Lock cacheLock;
    private Semaphore diskSemaphore;
    private ArrayList<ReadWriteRequest> queue;
    private FixedBuffer<CacheSector> cache;
    private static final int MAX_CACHE_SIZE = 10;


    private int lastSector = 0;
    private boolean goingUP = true;

    /**
     * Initialize the synchronous interface to the physical disk, in turn
     * initializing the physical disk.
     * 
     * @param unit
     *            The disk unit to be handled by this driver.
     */
    public CopyOfDiskDriver(int unit)
    {
        queueLock = new Lock("disk request queue lock");
        diskLock = new Lock("disk lock");
        cacheLock = new Lock("cache lock");
        diskSemaphore = new Semaphore("disk semaphore", 0);
        disk = Machine.getDisk(unit);
        disk.setHandler(new DiskIntHandler());
        queue = new ArrayList<ReadWriteRequest>();
        cache = new FixedBuffer<>(MAX_CACHE_SIZE);        
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
        Debug.ASSERT(0 <= sectorNumber && sectorNumber < getNumSectors());
        //check if we have it in the cache
        CacheSector sector = null;
//        
        try
        {
            cacheLock.acquire();
            sector = getCacheSector(sectorNumber);
            if(sector != null)
            {
//                hit
                
                cache.moveToFront(sector);
                sector.waitTillFree();
                
                cache.moveToFront(sector);
                return;
//                System.arraycopy(sector.getData(), 0, data, index, sector.getData().length);
            }
            else{
                // miss
                sector = new CacheSector(sectorNumber,data);
                cache.moveToFront(sector);
                sector.setUseage(false);
               
            }
        }finally
        {
            //always release the lock no matter what
            cacheLock.release();
            if(sector != null) return;
        }
//        
//        //otherwise we need to request it from the disk
//        
        int semSize = 1;
        if(queue.size() > 0)
            semSize = 0;
            
        Semaphore sem = new Semaphore("Request Semaphore", semSize);        
        ReadWriteRequest myRequest = new ReadWriteRequest(sectorNumber, data, index, 'r', sem);

        queueLock.acquire();
        queue.add(myRequest);
        queueLock.release();
        
        myRequest.p();
        serviceDisk(true, myRequest.getSectorNumber());
        System.out.println("Request Queue Size = " + queue.size());
        
        
        
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
        Debug.ASSERT(0 <= sectorNumber && sectorNumber < getNumSectors());

        int semSize = 1;
        if(queue.size() > 0)
            semSize = 0;
            
        Semaphore sem = new Semaphore("Lock", semSize);
        
        ReadWriteRequest myRequest = new ReadWriteRequest(sectorNumber, data,
                index, 'w', sem);

        queueLock.acquire();
        queue.add(myRequest);
        queueLock.release();
        
        myRequest.p();
//        CacheSector sector = getCacheSector(myRequest.getSectorNumber());
//        if(sector != null)
//            sector.waitTillFree();
        serviceDisk(false, myRequest.getSectorNumber());
    }
    
    private void serviceDisk(boolean read, int sectorNumber)
    {                       
        diskLock.acquire();
        ReadWriteRequest request = queue.remove(getNextFromQueue());
//        CacheSector sectorObject = getCacheSector(request.getSectorNumber());
//        if(sectorObject != null) sectorObject.setUseage(true);
        
        if(read)
        {
            disk.readRequest(request.getSectorNumber(), request.getData(), request.getIndex());
//            cacheCondition.get(sector).await();
        }
        else
        {
            
            disk.writeRequest(request.getSectorNumber(), request.getData(), request.getIndex());
            
//            cacheCondition.get(sector).await();
//            CacheSector oSector = new CacheSector(request.getSectorNumber(), request.getData());
//            cache.add(oSector);
        }
        
        diskSemaphore.P();
        diskLock.release();
        
        
//        if(sectorObject != null) 
//        {
//            sectorObject.setUseage(false);
//            sectorObject.signal();
//        }
        if(!queue.isEmpty())
            queue.get(getNextFromQueue()).v();        
    }
    
    /**
     * Gets the CacheSector object from cache whose sector number
     * matches SsectorNumber.
     * @param sectorNumber - the sector to return
     * @return the requested CacheSector object or null if it doesn't esist
     */
    CacheSector getCacheSector(int sectorNumber)
    {
        for(CacheSector sector :  cache)
        {
            if(sector.getSectorNumber() == sectorNumber)
                return sector;
        }
        return null;
    }
   
    private int getNextFromQueue()
    {

        while (true)
        {
            for (int i = 0; i < queue.size(); i++)
            {
                if (queue.get(i).getCylinderNumber() == lastSector)
                {
                    System.out.println("Next From Queue = " + i + " Cylinder = " + queue.get(i).getCylinderNumber());
                    return i;
                }
            }
            if (lastSector == 31)
            {
                goingUP = false;
            } else if (lastSector == 0)
            {
                goingUP = true;
            }
            if (goingUP)
            {
                lastSector++;
            } else
            {
                lastSector--;
            }
           
        }
    }
    
    /**
     * Debugging function which will spit out the provided byte array to a file.
     * @param bytes bytes to be printed
     */
    private void logBytes(byte[] bytes)
    {
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("log", true)))) {
            for(byte b : bytes)
            {
                out.print(Integer.toHexString(b));
                out.print(',');
            }
            out.println();
        }catch (IOException e) {
        }
    }

    /**
     * sectorNumber, data, index DiskDriver interrupt handler class.
     */
    private class DiskIntHandler implements InterruptHandler
    {
        /**
         * When the disk interrupts, just wake up the thread that issued the
         * request that just finished.
         */
        public void handleInterrupt()
        {
            diskSemaphore.V();
            Collections.sort(queue, new ReadWriteRequestComparator());
        }
    }

}
