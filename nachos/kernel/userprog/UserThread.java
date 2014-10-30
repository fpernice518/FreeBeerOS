// UserThread.java
//	A UserThread is a NachosThread extended with the capability of
//	executing user code.
//
// Copyright (c) 1992-1993 The Regents of the University of California.
// Copyright (c) 1998 Rice University.
// Copyright (c) 2003 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.userprog;

import java.util.ArrayList;
import java.util.Iterator;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.threads.Ticket;
import nachos.machine.InterruptHandler;
import nachos.machine.MIPS;
import nachos.machine.NachosThread;
import nachos.machine.CPU;
import nachos.util.TimerService;

/**
 * A UserThread is a NachosThread extended with the capability of executing user
 * code. It is kept separate from AddrSpace to provide for the possibility of
 * having multiple UserThreads running in a single AddrSpace.
 * 
 * @author Thomas Anderson (UC Berkeley), original C++ version
 * @author Peter Druschel (Rice University), Java translation
 * @author Eugene W. Stark (Stony Brook University)
 */
public class UserThread extends NachosThread
{

    /** The context in which this thread will execute. */
    public final AddrSpace space;
    private UserThreadInterruptHandler handler;
    private ArrayList<Ticket> tickets = null;

    // A thread running a user program actually has *two* sets of
    // CPU registers -- one for its state while executing user code,
    // and one for its state while executing kernel code.
    // The kernel registers are managed by the super class.
    // The user registers are managed here.

    /** User-level CPU register state. */
    private int userRegisters[] = new int[MIPS.NumTotalRegs];

    /**
     * Initialize a new user thread.
     *
     * @param name
     *            An arbitrary name, useful for debugging.
     * @param runObj
     *            Execution of the thread will begin with the run() method of
     *            this object.
     * @param addrSpace
     *            The context to be installed when this thread is executing in
     *            user mode.
     */
    public UserThread(String name, Runnable runObj, AddrSpace addrSpace)
    {
        super(name, runObj);
        space = addrSpace;
        handler = new UserThreadInterruptHandler();
        TimerService.getInstance().subscribe(handler);
    }

    public void addTicket(Ticket x)
    {
        /*
         * ensures we don't create a new list of tickets unless
         * we actually use them (ie, if we are using round-robin
         * we will not instantiate the new list)
         */
        if(tickets == null)
            tickets = new ArrayList<Ticket>();
        
        this.tickets.add(x);
    }

    public boolean findTicket(int x)
    {
        if (tickets.size() != 0)
        {
            for (Iterator<Ticket> iterator = tickets.iterator(); iterator.hasNext();)
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

    /**
     * Save the CPU state of a user program on a context switch.
     */
    @Override
    public void saveState()
    {
        // Save state associated with the address space.
        space.saveState();

        // Save user-level CPU registers.
        for (int i = 0; i < MIPS.NumTotalRegs; i++)
            userRegisters[i] = CPU.readRegister(i);

        // Save kernel-level CPU state.
        super.saveState();
    }

    public int getTickCount()
    {
        return handler.getTickCount();
    }

    public void resetTickCount()
    {
        handler.resetTickCount();
    }

    /**
     * Restore the CPU state of a user program on a context switch.
     */
    @Override
    public void restoreState()
    {
        // Restore the kernel-level CPU state.
        super.restoreState();

        // Restore the user-level CPU registers.
        for (int i = 0; i < MIPS.NumTotalRegs; i++)
            CPU.writeRegister(i, userRegisters[i]);

        // Restore state associated with the address space.
        space.restoreState();
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
