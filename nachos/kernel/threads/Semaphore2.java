package nachos.kernel.threads;

import java.util.ArrayList;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.filesys.ReadWriteRequest;
import nachos.machine.CPU;
import nachos.machine.NachosThread;
import nachos.util.FIFOQueue;
import nachos.util.Queue;

/**
 * This class defines a "semaphore" whose value is a non-negative integer. The
 * semaphore has only two operations, P() and V().
 *
 * P() -- waits until value > 0, then decrement.
 *
 * V() -- increment, waking up a thread waiting in P() if necessary.
 * 
 * Note that the interface does *not* allow a thread to read the value of the
 * semaphore directly -- even if you did read the value, the only thing you
 * would know is what the value used to be. You don't know what the value is
 * now, because by the time you get the value into a register, a context switch
 * might have occurred, and some other thread might have called P or V, so the
 * true value might now be different.
 * 
 * @author Thomas Anderson (UC Berkeley), original C++ version
 * @author Peter Druschel (Rice University), Java translation
 * @author Eugene W. Stark (Stony Brook University)
 */
public class Semaphore2
{

    /** Printable name useful for debugging. */
    public final String name;

    /** The value of the semaphore, always >= 0. */
    private int value;

    /** Threads waiting in P() for the value to be > 0. */
    private final ArrayList<NachosThread> queue;

    private ArrayList<ReadWriteRequest> rwrArrayList;

    /**
     * Spin lock used to obtain exclusive access to semaphore state in a
     * multiprocessor setting.
     */
    private final SpinLock spinLock;

    /**
     * Initialize a semaphore, so that it can be used for synchronization.
     *
     * @param debugName
     *            An arbitrary name, useful for debugging.
     * @param initialValue
     *            The initial value of the semaphore.
     */
    public Semaphore2(String debugName, int initialValue)
    {
        name = debugName;
        value = initialValue;
        rwrArrayList = new ArrayList<ReadWriteRequest>();
        queue = new ArrayList<NachosThread>();

        spinLock = new SpinLock(name + " spin lock");
    }

    /**
     * Wait until semaphore value > 0, then decrement.
     */
    public void P()
    {
        /*
         * Checking the value and decrementing must be done atomically, so we
         * need to disable interrupts and obtain the scheduler spinLock before
         * checking the value.
         * 
         * Note that Scheduler.Sleep() assumes that interrupts are disabled and
         * the scheduler spinLock is held when it is called.
         */
        int oldLevel = CPU.setLevel(CPU.IntOff); // disable interrupts
        spinLock.acquire(); // exclude other CPUs

        while (value == 0)
        {
            // semaphore not available, so go to sleep
            queue.add(NachosThread.currentThread());
            Nachos.scheduler.sleepThread(spinLock);
            spinLock.acquire(); // restore exclusion
        }
        Debug.println('s', "Semaphore " + name + ": value " + value + " -> "
                + (value - 1));
        value--; // semaphore available,
        // consume its value
        spinLock.release(); // release exclusion
        CPU.setLevel(oldLevel); // restore interrupts
    }

    /**
     * Wait until semaphore value > 0, then decrement.
     */
    public void P(ReadWriteRequest rwr)
    {
        /*
         * Checking the value and decrementing must be done atomically, so we
         * need to disable interrupts and obtain the scheduler spinLock before
         * checking the value.
         * 
         * Note that Scheduler.Sleep() assumes that interrupts are disabled and
         * the scheduler spinLock is held when it is called.
         */
        int oldLevel = CPU.setLevel(CPU.IntOff); // disable interrupts
        spinLock.acquire(); // exclude other CPUs

        while (value == 0)
        {
            // semaphore not available, so go to sleep
            queue.add(NachosThread.currentThread());
            Nachos.scheduler.sleepThread(spinLock);
            rwrArrayList.add(rwr);
            spinLock.acquire(); // restore exclusion
        }
        Debug.println('s', "Semaphore " + name + ": value " + value + " -> "
                + (value - 1));
        value--; // semaphore available,
        // consume its value
        spinLock.release(); // release exclusion
        CPU.setLevel(oldLevel); // restore interrupts
    }

    /**
     * Increment semaphore value, waking up a waiter if necessary.
     */
    public void V()
    {
        KernelThread thread;
        /*
         * As with P(), this operation must be atomic, so we need to disable
         * interrupts.
         */
        int oldLevel = CPU.setLevel(CPU.IntOff);
        spinLock.acquire(); // exclude other CPUs
        if (queue.size() != 0)
        {
            thread = (KernelThread) queue.remove(0);
        } else
        {
            thread = null;
        }
        if (thread != null) // make thread ready, consuming the V immediately
            Nachos.scheduler.readyToRun(thread);

        Debug.println('s', "Semaphore " + name + ": value " + value + " -> "
                + (value + 1));
        value++;

        spinLock.release(); // release exclusion
        CPU.setLevel(oldLevel);
    }

    public void V(int value)
    {
        KernelThread thread;
        /*
         * As with P(), this operation must be atomic, so we need to disable
         * interrupts.
         */
        int oldLevel = CPU.setLevel(CPU.IntOff);
        spinLock.acquire(); // exclude other CPUs

        if (queue.size() != 0)
        {
            thread = (KernelThread) queue.remove(0);
        } else
        {
            thread = null;
        }
        if (thread != null) // make thread ready, consuming the V immediately
            Nachos.scheduler.readyToRun(thread);

        Debug.println('s', "Semaphore " + name + ": value " + value + " -> "
                + (value + 1));
        value++;

        spinLock.release(); // release exclusion
        CPU.setLevel(oldLevel);
    }

    // public int getNextClosestIndex(int myNumber)
    // {
    //
    // int distance;
    // int c;
    // int idx;
    // myNumber = myNumber % 32;
    // distance = Math.abs(queue.get(0).getSectorNumber() - myNumber);
    // c = 1;
    // idx = 0;
    //
    // for (; c < queue.size(); c++)
    // {
    //
    // int cdistance = Math.abs(queue.get(c).getSectorNumber() % 32
    // - myNumber);
    // if (cdistance < distance)
    // {
    // idx = c;
    // distance = cdistance;
    // }
    //
    // }
    // return idx;
    // }

}