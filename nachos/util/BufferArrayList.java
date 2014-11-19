package nachos.util;

import java.util.ArrayList;

import nachos.kernel.filesys.CacheSector;

public class BufferArrayList extends ArrayList<CacheSector>
{
    private final int MAX_SIZE = 10;
    
    public void stuffIntoBuff(CacheSector sector)
    {
//        cacheLock.acquire();
        if (this.size() < 10)
        {
            this.add(0, sector);
        } else
        {
            ensureRemove();
            this.add(0, sector);
        }

//        cacheLock.release();

    }
    public boolean containsSector(int i){
        for (int j = 0; j < this.size(); j++)
        {
            if(this.get(j).getSectorNumber() == i){
                return true;
            }
            
        }
        return false;
    }
    public CacheSector getBySector(int x){
        for (int j = 0; j < this.size(); j++)
        {
            if(this.get(j).getSectorNumber() == x){
                return this.get(j);
            }
        }
        return null;
    }

    /**
     * must use with cache lock
     */
    private void ensureRemove()
    {
        CacheSector cs = this.get(this.size() - 1);
        if (cs.isValid())
        {
            this.remove(cs);
        } else
        {
            cs.reserve();
            this.remove(cs);
        }
    }

    

}
