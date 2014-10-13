package nachos.kernel.userprog;

import nachos.Debug;
import nachos.kernel.threads.Lock;
import nachos.machine.Machine;
import nachos.machine.NachosThread;

public class MemAlloc
{
    private MemAlloc my_mem_alloc = null;
    private NachosThread[] process_id;
    private int machine_page_size;
    private Lock lock;

    private static class MemAllocWrapper
    {
        static MemAlloc INSTANCE = new MemAlloc();
    }

    private MemAlloc()
    {
        System.out.println("hello");
        machine_page_size = Machine.NumPhysPages;
        process_id = new NachosThread[machine_page_size];
        lock = new Lock("MemAllocateLock");

    }

    public static MemAlloc getInstance() {
        return MemAllocWrapper.INSTANCE;
    }


    public int allocatePage()
    {
        int returnPage = -1;
        try{
            lock.acquire();
        NachosThread process_to_allocate = NachosThread.currentThread();
        // Lock lock = new Lock(process_to_allocate.name);
        // lock.acquire();

        for (int i = 0; i < machine_page_size; i++)
        {
            if (process_id[i] == null)
            {
                
                process_id[i] = process_to_allocate;
                returnPage = i;
                break;
            }
        }

        if (returnPage == -1)
            Debug.println('2', "Insuffcient Pages when trying to allocate");
        }
        
        finally
        {
            lock.release();
        }
        
        return returnPage;  
    }
    
    public boolean deAllocatePages(NachosThread process_to_deallocate)
    {
        boolean returnResult = false;
        try{
            lock.acquire();
        // Lock lock = new Lock(process_to_deallocate.name);
        // lock.acquire();

        for (int i = 0; i < machine_page_size; i++)
        {
            if (process_id[i] == process_to_deallocate)
            {
                process_id[i] = null;
//                clearPage(i);
                System.out.println("This should be null at "+i+" : "+process_id[i] + " ******************");
                returnResult = true;
//                break;
            }
        }
        if (!returnResult)
            Debug.println('2', "Could not dealocate space");
        // i think we have a problem when allocating memory of the data&& text bc we do not have all of it.
        // lock.release();
        }finally{
            lock.release();
        }
        return returnResult;

    }

    // kills lower spot * page size
//    private void clearPage(int pageNumber)
//    {
//        Machine.mainMemory[pageNumber] = (byte) 0;
//
//    }
}
