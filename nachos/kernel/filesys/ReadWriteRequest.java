package nachos.kernel.filesys;

import java.util.Comparator;

import nachos.kernel.threads.Semaphore;
import nachos.machine.Machine;

public class ReadWriteRequest implements Comparable<ReadWriteRequest>
{
    private int sectorNumber;
    private int index;
    private byte[] data;
    char requestType;
    Semaphore sem;
    boolean read;

    public ReadWriteRequest(int sectorNumber, byte[] data, int index, boolean read)
    {
        this.data = data;
        this.index = index;
        this.sectorNumber = sectorNumber;
        sem = new Semaphore("Sector " + sectorNumber + " Semaphore", 0);
        this.read = read;
    }
    
    public boolean isRead()
    {
        return read;
    }

    public int getSectorNumber()
    {
        return sectorNumber;
    }

    public byte[] getData()
    {
        return data;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    public void setIndex(int index)
    {
        this.index = index;
    }

    public char getRequestType()
    {
        return requestType;
    }

    public void p()
    {
        sem.P();

    }

    public void v()
    {
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
