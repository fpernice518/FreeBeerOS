package nachos.kernel.userprog;

import java.util.ArrayList;

import nachos.Debug;
import nachos.kernel.threads.Exchanger;
import nachos.kernel.threads.Lock;
import nachos.machine.Machine;
import nachos.machine.NachosThread;

public class MemAlloc
{
    private MemAlloc my_mem_alloc = null;
    private NachosThread[] process_id;
    private int machine_page_size;
    private int spaceid;
    private Lock lock;
    private ArrayList<Exchanger> exchange;
    private ArrayList<Integer> parentsInWait;
    private ArrayList<Integer> childrenInProgress;
    

    private static class MemAllocWrapper
    {
        static MemAlloc INSTANCE = new MemAlloc();
    }

    public int getSpaceId(){
       
            return spaceid;
       
        
    }

    private MemAlloc()
    {
        // System.out.println("hello");
        machine_page_size = Machine.NumPhysPages;
        process_id = new NachosThread[machine_page_size];
        lock = new Lock("MemAllocateLock");
        parentsInWait = new ArrayList<Integer>();
        childrenInProgress = new ArrayList<Integer>();
        exchange = new ArrayList<Exchanger>();
        spaceid = 1;

    }

    public static MemAlloc getInstance()
    {
        return MemAllocWrapper.INSTANCE;
    }
  
  public int setRelation(int spaceidParent, int spaceidChild){
        parentsInWait.add(new Integer(spaceidParent));
        childrenInProgress.add(new Integer(spaceidChild));
       
        
        exchange.add(new Exchanger());
//        int i  = exchange.get(childrenInProgress.indexOf(new Integer(spaceidChild)).exchange());
        int i = (int) exchange.get(childrenInProgress.indexOf(new Integer(spaceidChild))).exchange(new Integer(1));
//        Exchanger x = exchange.get(childrenInProgress.indexOf(new Integer(spaceidChild)));
      
        
        
        return i;
//        System.out.println(spaceidParent);
//        System.out.println(spaceidChild);
        
    }
  public void checkIfChildThatAreWaitedOn(int child, int exit){
      
      if(childrenInProgress.contains(new Integer(child))){
          int location  = childrenInProgress.indexOf(new Integer(child));
          Exchanger x = exchange.get(location);
          x.exchange(new Integer(exit));
          childrenInProgress.remove(location);
          parentsInWait.remove(location);
          exchange.remove(location);
//          Exchanger
      }
  }
  
    public int allocatePage()
    {
        int returnPage = -1;
        try
        {
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
                    spaceid++;
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
        try
        {
            lock.acquire();
            // Lock lock = new Lock(process_to_deallocate.name);
            // lock.acquire();

            for (int i = 0; i < machine_page_size; i++)
            {
                if (process_id[i] == process_to_deallocate)
                {
                    process_id[i] = null;
                    // clearPage(i);
                    // System.out.println("This should be null at "+i+" : "+process_id[i]
                    // + " ******************");
                    returnResult = true;
                    // break;
                }
            }
            if (!returnResult)
                Debug.println('2', "Could not dealocate space");
            // i think we have a problem when allocating memory of the data&&
            // text bc we do not have all of it.
            // lock.release();
        } finally
        {
            lock.release();
        }
        return returnResult;

    }

    public boolean searchPrcoess(int hashcode)
    {
        boolean found = false;
        for (int i = 0; i < process_id.length; i++)
        {
            if (hashcode == process_id[i].hashCode())
            {
                return true;
            }

        }

        return found;
    }
    // kills lower spot * page size
    // private void clearPage(int pageNumber)
    // {
    // Machine.mainMemory[pageNumber] = (byte) 0;
    //
    // }
}
