package nachos.kernel.threads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import nachos.kernel.userprog.UserThread;
import nachos.util.Queue;

public class LotteryQueue implements Queue<KernelThread>
{
    int currentTicketsInUse;
    Set<KernelThread> runningThreads;  //set automatically ensures no duplicates
    
    LotteryQueue()
    {
        runningThreads = new HashSet<>();
    }

    @Override      
    public boolean offer(KernelThread thread)
    {
        return runningThreads.add(thread);
    }

    @Override
    public KernelThread peek()
    {
        return getNextThread();
    }

    @Override
    public KernelThread poll()
    {
        KernelThread t = getNextThread();
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
        Random rand = new Random(System.currentTimeMillis());
        
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
        return selectedThread;
    }

}
