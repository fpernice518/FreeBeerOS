package nachos.kernel.threads;

public class Buffer
{
    Lock bufferLock;
    Condition bufferCondition;
    byte[] data;
    int sector;
    boolean reserved;
    boolean valid;
    boolean dirty;

    public byte[] getData()
    {
        // TODO assumes buffer is reserved by caller
        if (valid == false)
        {
            // TODO figure out how to get access to the disk here
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
