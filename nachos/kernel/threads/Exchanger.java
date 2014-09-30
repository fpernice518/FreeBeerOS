package nachos.kernel.threads;

import nachos.kernel.Nachos;
import nachos.machine.CPU;
import nachos.machine.InterruptHandler;
import nachos.machine.Machine;
import nachos.machine.Timer;

/**
 * This class is patterned after the Exchanger class in the java.util.concurrent
 * package.
 *
 * An <EM>exchanger</EM> is an object that provides the ability for a thread to
 * "mate" with a partner thread, resulting in the exchange of objects between
 * the two threads. That is, each thread presents some object upon entry to the
 * <CODE>exchange</CODE> method, matches with a partner thread (if there is no
 * partner currently available the thread blocks until one arrives) and receives
 * its partner's object on return.
 */
public class Exchanger<V>
{
    private Condition cond;
    private Lock handshake;
    private Lock singleLock;
    private boolean recieved_first_thread;
    private V firstObject;
    private V secondObject;
    private boolean error;

    /**
     * Creates a new Exchanger.
     */
    public Exchanger()
    {

        handshake = new Lock("Handshake Lock");
        cond = new Condition("Handshake Condition", handshake);
        // this lock is used for who ever gets first
        // singleLock = new Lock("Single Lock");

    }

    /**
     * Waits for another thread to arrive at this exchange point and then
     * transfers the given object to it, receiving the other thread's object in
     * return.
     *
     * @param x
     *            The object to exchange
     * @return The object provided by the other thread.
     */
    public V exchange(V x)
    {

        handshake.acquire();

        if (!recieved_first_thread)
        {

            firstObject = x;
            recieved_first_thread = true;
            // wait for next thread
            cond.await();
            handshake.release();
            // recieved_first_thread = false;
            // after await on first thread

            return secondObject;

        } else
        {
            // we recieved the first thread now we get work done

            secondObject = x;
            // wakeup other thread; lets do this damn thing
            cond.signal();

            handshake.release();
            recieved_first_thread = false;
            return firstObject;
        }

    }

    public static class TimeoutException extends Exception
    {
        public TimeoutException()
        {

        }
    }

    /**
     * Waits for another thread to arrive at this exchange point and then
     * transfers the given object to it, receiving the other thread's object in
     * return.
     *
     * @param x
     *            The object to exchange
     * @int timeout If positive, then the value is the maximum number of "ticks"
     *      of time to wait before timing out and throwing TimeoutException. If
     *      zero, a thread will wait as long as necessary for another thread to
     *      arrive.
     * @return The object provided by the other thread.
     * @throws TimeoutException
     *             if a timeout was specified and the caller thread has been
     *             waiting that amount of time for another thread to arrive.
     */
    public V exchange(V x, int timeout) throws TimeoutException
    {
        // int currentTick = 0;

        if (timeout < 0)
        {
            return null;
        } else if (timeout == 0)
        {
            return exchange(x);
        }
        CPU cpu = Machine.getCPU(0);
         Timer timer = cpu.timer;
         
         
//        Timer timer = cpu.timer;
        V temp = null;
        timer.setHandler(new TimerInterruptHandler(timeout));
        timer.start();
        temp = exchange(x);
        timer.stop();
        if (error)
        {

            error = false;
            throw new TimeoutException();

        }

        return temp;

        // V temp = exchange(x);

        // return temp;

    }

    /**
     * Interrupt handler called by the general-purpose timer device each time
     * one "tick" of time goes by. This handler should do what is necessary to
     * wake up a waiting thread whose timeout has expired. Note that there is
     * just one timer device, but you will want to be able to use multiple
     * Exchanger objects at once, so it will be necessary to
     */
    private class TimerInterruptHandler implements InterruptHandler
    {

        private int timeout;
        private int timer;
        

        public TimerInterruptHandler(int timeout)
        {
            TimerService.getTimerService().subscribe(this);
            timer = 0;
            this.timeout = timeout;

        }

        @Override
        public void handleInterrupt()
        {
            if (timer >= timeout)
            {
                error = true;
                
                cond.broadcast();
//                handshake.release();
               
            }

            timer++;
        }

    }

}