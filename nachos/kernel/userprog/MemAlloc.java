package nachos.kernel.userprog;

import nachos.Debug;
import nachos.kernel.threads.Lock;
import nachos.machine.Machine;
import nachos.machine.NachosThread;

public class MemAlloc
{
    private static MemAlloc my_mem_alloc = null;
    private static int[] process_id;
    private static int machine_page_size;

    private MemAlloc()
    {
        machine_page_size = Machine.NumPhysPages;
        process_id = new int[machine_page_size];

    }

    public static MemAlloc getMemAlloc()
    {
        if (my_mem_alloc == null)
        {
            my_mem_alloc = new MemAlloc();
        }
        return my_mem_alloc;
    }

    public static int allocatePage()
    {
        NachosThread process_to_allocate = NachosThread.currentThread();
        Lock lock = new Lock(process_to_allocate.name);
        lock.acquire();
        
        int returnPage = -1;

        for (int i = 0; i < machine_page_size; i++)
        {
            if (process_id[i] == 0)
            {
                process_id[i] = process_to_allocate.hashCode();
                returnPage = i;
                break;
            }
        }
        
        if(returnPage == -1)
        Debug.println('2', "Insuffcient Pages when trying to allocate");
        
        lock.release();
        return returnPage;

    }

    public static boolean deAllocatePage()
    { 
        NachosThread process_to_deallocate = NachosThread.currentThread();
        Lock lock = new Lock(process_to_deallocate.name);
        lock.acquire();
        
        boolean returnResult = false;

        for (int i = 0; i < machine_page_size; i++)
        {
            if (process_id[i] ==  process_to_deallocate.hashCode())
            {
                process_id[i] = 0;
                clearPage(i);
                returnResult = true;
                break;
            }
        }
        if(!returnResult)
            Debug.println('2', "Could not dealocate space");
        
        lock.release();
        
        return returnResult;

    }
    private static void clearPage(int pageNumber){
        Machine.mainMemory[pageNumber]= (byte)0;
        
    }
}
