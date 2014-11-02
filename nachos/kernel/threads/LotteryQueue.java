package nachos.kernel.threads;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import nachos.util.Queue;

public class LotteryQueue implements Queue<KernelThread>
{
    Set<KernelThread> runningThreads;  //set automatically ensures no duplicates
    Random rand;
    KernelThread t = null;
    
    LotteryQueue()
    {
        runningThreads = new HashSet<>();
        rand = new Random(System.currentTimeMillis());
    }

    @Override      
    public boolean offer(KernelThread thread)
    {
        return runningThreads.add(thread);
    }

    @Override
    public KernelThread peek()
    {
        return t;
    }

    @Override
    public KernelThread poll()
    {
        t = getNextThread();
        runningThreads.remove(t);
        return t;
    }

    @Override
    public boolean isEmpty()
    {
        return runningThreads.isEmpty();
    }
    
    
    private KernelThread getNextThread()
    {
        int count;
        int oldCount;
        int selectedTicket;
        KernelThread selectedThread = null;
        
        if(runningThreads.size() <= 0)
            return selectedThread;
        
        //tally up all tickets currently in play
        count = 0;
        for(KernelThread thread : runningThreads)
        {
            count += thread.getNumTickets();
        }
        
        //select a ticket
        System.out.println("Ticket Count: " + count);
        selectedTicket = rand.nextInt(count);
        System.out.println("Ticket Selected: " + selectedTicket);
        
        //find the corresponding thread
        count = 0;
        oldCount = 0;
        for(KernelThread thread : runningThreads)
        {
            oldCount = count;
            count += thread.getNumTickets();
            
            if(selectedTicket >= oldCount && selectedTicket < count)
            {
                selectedThread = thread;
                break;
            }        
        }
        System.out.println("Bonus ticket of Selected: "+selectedThread.getBonusTickets());
        selectedThread.clearBonusTickets();
        System.out.println("Bonus ticket of Selected Cleared: "+selectedThread.getBonusTickets());
        return selectedThread;
    }

}
