package nachos.kernel.filesys;

import java.util.Comparator;

import nachos.kernel.threads.Semaphore;
import nachos.machine.Machine;

public class ReadWriteRequest implements Comparable<ReadWriteRequest>
{
    private int sectorNumber;
    private byte[] data;
    char requestType;
    Semaphore sem;

    public ReadWriteRequest(int sectorNumber, byte[] data)
    {
        this.data = data;
        this.sectorNumber = sectorNumber;
        sem = new Semaphore("Sector " + sectorNumber + " Semaphore", 0);
    }

    public int getSectorNumber()
    {
        return sectorNumber;
    }

    public byte[] getData()
    {
        return data;
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
