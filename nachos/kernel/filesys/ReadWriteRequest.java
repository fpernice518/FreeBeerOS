package nachos.kernel.filesys;

public class ReadWriteRequest
{
    private int sectorNumber,  index;
    private byte[]  data;
    char requestType;
   
    public ReadWriteRequest(int sectorNumber,byte[] data, int index, char requestType){
        this.data = data;
        this.index = index;
        this.sectorNumber = sectorNumber;
        this.requestType = requestType;
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
    
}
