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
    private int numTickets = 1;
    private int bonusTickets= 0;

    public KernelThread(String name, Runnable runObj)
    {
        super(name, runObj);
        handler = new UserThreadInterruptHandler();
        TimerService.getInstance().subscribe(handler);
    }
    
    public KernelThread(String name, Runnable runObj, int numTickets)
    {
        this(name, runObj);
        this.numTickets = numTickets;
    }
    
    public void setnumTickets(int numTickets)
    {
        this.numTickets = numTickets;
    }

    public int getNumTickets()
    {
        return numTickets+bonusTickets;
    }

    public int getBonusTickets()
    {
        return bonusTickets;
    }

    protected void setBonusTickets(int bonusTickets)
    {
        this.bonusTickets = bonusTickets;
    }
    public void incBonusTickets()
    {
        this.bonusTickets = bonusTickets+1;
    }
    protected void clearBonusTickets()
    {
        this.bonusTickets = 0;
    }
    
    protected void resetTickCount()
    {
        handler.resetTickCount();
    }
    
    @Override
    public void destroy()
    {
        TimerService.getInstance().unsubscribe(handler);
        super.destroy();
    }
    
    private class UserThreadInterruptHandler implements InterruptHandler
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

        private void resetTickCount()
        {
            tickCount = 0;
        }
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
        } else
        {
            Debug.println('i', "No current thread on interrupt return, skipping yield");
        }
    }
}