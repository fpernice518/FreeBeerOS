package nachos.kernel.filesys;

import nachos.kernel.threads.Condition;
import nachos.kernel.threads.Lock;

public class CacheSector
{
    byte[] data;
    int sectorNumber;
    Condition condition;
    Lock conditionLock;
    boolean reserved;
    boolean valid;
    boolean dirty;
    
    
    public CacheSector(int sectorNumber, byte[] data)
    {
        this.sectorNumber = sectorNumber;
        this.data = data;
        conditionLock = new Lock("Sector " + sectorNumber + " Condition Lock");
        condition = new Condition("Sector " + sectorNumber + " Condition", conditionLock);
        
        reserved = false;
        valid = false;
        dirty = false;
                
    }
    
    public byte[] getData()
    {
        return data;
    }
    
    public int getSectorNumber()
    {
        return sectorNumber;
    }

    public void reserve()
    {
        conditionLock.acquire();
        while(reserved)
            condition.await();
        reserved = true;
        conditionLock.release();
        
    }

    public boolean isValid()
    {
        return valid;
    }

    public void setValid()
    {
        valid = true;        
    }
    
    public boolean isDirty()
    {
        return dirty;
    }
    
    public void setDirty()
    {
        dirty = true;
    }

    public void release()
    {
        conditionLock.acquire();
        reserved = false;
        conditionLock.release();
    }
    
}
