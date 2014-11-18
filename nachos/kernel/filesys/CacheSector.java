package nachos.kernel.filesys;

import nachos.kernel.threads.Condition;
import nachos.kernel.threads.Lock;

public class CacheSector
{
    byte[] data;
    int sectorNumber;
    Condition condition;
    Lock conditionLock;
    boolean inUse = false;
    
    
    public CacheSector(int sectorNumber, byte[] data)
    {
        this.sectorNumber = sectorNumber;
        this.data = data;
        conditionLock = new Lock("Sector " + sectorNumber + " Condition Lock");
        condition = new Condition("Sector " + sectorNumber + " Condition", conditionLock);
                
    }
    
    public byte[] getData()
    {
        return data;
    }
    
    public int getSectorNumber()
    {
        return sectorNumber;
    }
    
    public void waitTillFree()
    {
        conditionLock.acquire();
        while(inUse == true)
            condition.await();
        conditionLock.release();
    }
    
    public void signal()
    {
        conditionLock.acquire();
        condition.signal();
        conditionLock.release();
    }
    
    public void broadcast()
    {
        conditionLock.acquire();
        condition.broadcast();
        conditionLock.release();
    }
    
    public void setUseage(boolean inUse) {this.inUse = inUse;}
    
    
    public Lock getConditionLock() {return conditionLock;}
    
    public Condition getCondition() {return condition;}
    
}
