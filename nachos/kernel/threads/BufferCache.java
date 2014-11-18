package nachos.kernel.threads;

import java.util.LinkedHashMap;

public class BufferCache
{
   Lock cacheLock;
   LinkedHashMap<Integer, Buffer> cache;
   
   Buffer getBuffer(int sectorNumber)
   {
       cacheLock.acquire();
       boolean contained = cache.containsKey(sectorNumber);
       Buffer buffer;
       
       if(contained == true)
       {
           buffer = cache.get(sectorNumber);
           buffer.reserve();
           cacheLock.release();
           return buffer;           
       }
       else
       {
           buffer = replaceBuffer();
           buffer.setSector(sectorNumber);
           cache.put(sectorNumber, buffer);
           cacheLock.release();
           return buffer;
       }
   }

private Buffer replaceBuffer()
{
    Buffer lru = new Buffer(); //TODO fix this
    //TODO get LRU buffer
    
    lru.reserve();
    
    if(lru.isValid())
    {
        cacheLock.release();
        lru.writeBack();
        lru.invalidate();
        cacheLock.acquire();
        cache.remove(lru.getSector());  //TODO not sure about this yet
    }
    
    return lru;
}
}
