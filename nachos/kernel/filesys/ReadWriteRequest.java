package nachos.kernel.filesys;

import java.util.Comparator;

import nachos.kernel.threads.Semaphore;

public class ReadWriteRequest implements Comparable<ReadWriteRequest>
{
    private int sectorNumber,  index;
    private byte[]  data;
    char requestType;
    Semaphore sem;
   
    public ReadWriteRequest(int sectorNumber,byte[] data, int index, char requestType, Semaphore sem){
        this.data = data;
        this.index = index;
        this.sectorNumber = sectorNumber;
        this.requestType = requestType;
        this.sem = sem;
    }
    public ReadWriteRequest(int sectorNumber,byte[] data, int index){
        this.data = data;
        this.index = index;
        this.sectorNumber = sectorNumber;
    }
    public int getSectorNumber()
    {
        return sectorNumber;
    }

    public int getIndex()
    {
        return index;
    }

    public byte[] getData()
    {
        return data;
    }

    public char getRequestType()
    {
        return requestType;
    }
    public void p(){
        sem.P();
        
    }
    public void v(){
        sem.V();
    }
    
    public int getCylinderNumber()
    {
        return sectorNumber % 32;
    }

    @Override
    public int compareTo(ReadWriteRequest o)
    {
        return this.getCylinderNumber() - o.getCylinderNumber();
    }
}
