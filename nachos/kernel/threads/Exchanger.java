package nachos.kernel.threads;

import nachos.machine.InterruptHandler;

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

    /**
     * Creates a new Exchanger.
     */
    public Exchanger()
    {

        handshake = new Lock("Handshake Lock");
        cond = new Condition("Handshake Condition", handshake);
        // this lock is used for who ever gets first
//        singleLock = new Lock("Single Lock");

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
            recieved_first_thread = true;

            firstObject = x;
//            singleLock.release();
            cond.await();
            handshake.release();
            // after await on first thread

            return secondObject;

        } else
        {
            // we recieved the first thread now we get work done

            secondObject = x;

            cond.broadcast();
            handshake.release();

            return firstObject;
        }

    }

    public static class TimeoutException extends Exception
    {
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
        return null;
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
        @Override
        public void handleInterrupt()
        {
        }
    }

}