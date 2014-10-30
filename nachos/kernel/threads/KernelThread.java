package nachos.kernel.threads;

import java.util.ArrayList;
import java.util.Iterator;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.userprog.UserThread;
import nachos.machine.CPU;
import nachos.machine.InterruptHandler;
import nachos.machine.NachosThread;
import nachos.util.TimerService;

public class KernelThread extends NachosThread
{
    private UserThreadInterruptHandler handler;
    private ArrayList<Ticket> tickets = null;

    public KernelThread(String name, Runnable runObj)
    {
        super(name, runObj);

        handler = new UserThreadInterruptHandler();
        TimerService.getInstance().subscribe(handler);
    }

    public void addTicket(Ticket x)
    {
        /*
         * ensures we don't create a new list of tickets unless we actually use
         * them (ie, if we are using round-robin we will not instantiate the new
         * list)
         */
        if (tickets == null)
            tickets = new ArrayList<Ticket>();

        this.tickets.add(x);
    }

    public boolean findTicket(int x)
    {
        if (tickets.size() != 0)
        {
            for (Iterator<Ticket> iterator = tickets.iterator(); iterator
                    .hasNext();)
            {
                Ticket ticket = (Ticket) iterator.next();
                if (ticket.getTicketNumber() == x)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public int getTickCount()
    {
        return handler.getTickCount();
    }

    public void resetTickCount()
    {
        handler.resetTickCount();
    }

    protected void finalize() throws Throwable
    {
        TimerService.getInstance().unsubscribe(handler);
        super.finalize();
    }
}

class UserThreadInterruptHandler implements InterruptHandler
{
    private int tickCount = 0;
    private static final int quantum = 1000;

    @Override
    public void handleInterrupt()
    {
        tickCount += TimerService.getInstance().getResolution();

        if (tickCount >= quantum)
        {
            CPU.setOnInterruptReturn(new UTRunnable());
            resetTickCount();
        }
    }

    public void resetTickCount()
    {
        tickCount = 0;
    }

    public int getTickCount()
    {
        return tickCount;
    }

}

class UTRunnable implements Runnable
{
    @Override
    public void run()
    {
        if (NachosThread.currentThread() != null)
        {
            Debug.println('t', "Yielding current thread on interrupt return");
            Nachos.scheduler.yieldThread();
            ((UserThread) NachosThread.currentThread()).resetTickCount();
        } else
        {
            Debug.println('i',
                    "No current thread on interrupt return, skipping yield");
        }
    }
}