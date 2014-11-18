package nachos.kernel.threads;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class BufferCache extends LinkedHashMap<Integer, Buffer>
{
    private static final long serialVersionUID = 1L;
    Lock cacheLock;
    public static final int MAX_SIZE = 10;
    Semaphore diskSemaphore;

    public BufferCache(Semaphore diskSemaphore)
    {
        super(MAX_SIZE, 0.75f, true);
        cacheLock = new Lock("Cache Lock");
        this.diskSemaphore = diskSemaphore;
    }

    Buffer getBuffer(int sectorNumber)
    {
        cacheLock.acquire();
        boolean contained = this.containsKey(sectorNumber);
        Buffer buffer;

        if (contained == true)
        {
            buffer = this.get(sectorNumber);
            buffer.reserve();
            cacheLock.release();
            return buffer;
        } else
        {
            buffer = replaceBuffer();
            buffer.setSector(sectorNumber);
            this.put(sectorNumber, buffer);
            cacheLock.release();
            return buffer;
        }
    }

    private Buffer replaceBuffer()
    {
        try
        {
        Buffer lru = this.entrySet().iterator().next().getValue();
        lru.reserve();

        if (lru.isValid())
        {
            cacheLock.release();
            lru.writeBack();
            lru.invalidate();
            cacheLock.acquire();
            this.remove(lru);
        }

        return lru;
        }
        catch(NoSuchElementException e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Buffer> eldest)
    {
        Buffer buff = eldest.getValue();
        buff.reserve();
        return size() > MAX_SIZE;
    }
}
