package nachos.kernel.threads;

import nachos.machine.Disk;
import nachos.machine.Machine;

public class Buffer
{
    Lock bufferLock;
    Condition bufferCondition;
    Disk disk;
    byte[] data;
    int sector;
    boolean reserved;
    boolean valid;
    boolean dirty;

    public Buffer(byte[] data, int sector, int diskNumber)
    {
        disk = Machine.getDisk(diskNumber);
        bufferLock = new Lock("Sector " + sector + " Lock");
        bufferCondition = new Condition("Sector " + sector + " Condition", bufferLock);
        this.data = data;
        this.sector = sector;
        
        //TODO Check these
        reserved = false;
        valid = false;
        dirty = false;
    }
    
    public byte[] getData()
    {
        // TODO assumes buffer is reserved by caller
        if (valid == false)
        {
            disk.readRequest(sector, data, 0);
            // TODO we need to wait here, need a lock or semaphore
            valid = true;
        }
        return data;
    }

    public void writeBack()
    {
        // TODO Assumes buffer is reserved by caller
        if (dirty = true)
        {
            // TODO need to write to disk here
            // TODO need to hang out here
            dirty = false;
        }
    }

    public void setDirty()
    {
        // TODO assumes reserved by caller
        dirty = true;
    }

    public boolean isValid()
    {
        return valid;
    }

    public void invalidate()
    {
        valid = false;
    }

    public int getSector()
    {
        return sector;
    }

    public void setSector(int sectorNumber)
    {
        // TODO assumes buffer is invalid and reserved by caller
        if (valid == false)
            sector = sectorNumber;

    }
    
    public void release()
    {
        bufferLock.acquire();
        reserved = false;
        bufferCondition.signal();
        bufferLock.release();
    }

    public void reserve()
    {
        // TODO assumes buffer is reserved by caller
        bufferLock.acquire();
        while (reserved == true)
            bufferCondition.await();
        reserved = true;
        bufferLock.release();
    }
}
