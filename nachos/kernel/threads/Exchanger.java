package nachos.kernel.threads;

import nachos.util.*;
import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.machine.InterruptHandler;

/**
 * This class is patterned after the Exchanger class
 * in the java.util.concurrent package.
 *
 * An <EM>exchanger</EM> is an object that provides the ability for
 * a thread to "mate" with a partner thread, resulting in the exchange
 * of objects between the two threads.
 * That is, each thread presents some object upon entry to the
 * <CODE>exchange</CODE> method, matches with a partner thread
 * (if there is no partner currently available the thread blocks until
 * one arrives) and receives its partner's object on return.
 * 
 * @author E. Stark
 * @version 20141001
 */
public class Exchanger<V> {
    
    /**
     * Class to package up information about a pending exchange offer.
     */
    private class Offer {
	/** The object offered for exchange. */
	public V object;
	
	/**
	 * Time left to wait.  If <= 0 and a timeout was specified,
	 * then a timeout has occurred.
	 */
	public int ticksToWait;
	
	public Offer(V object) {
	    this.object = object;
	}
	
	public V swap(V other) {
	    V result = object;
	    object = other;
	    return result;
	}
    }
    
    /** The currently unmatched exchange offer, if any, otherwise null. */
    private Offer pendingOffer;

    /**
     * Spinlock to prevent a pending offer from being manipulated concurrently
     * by two different CPUs.
     */
    public SpinLock offerSpinlock;

    /** Mutual exclusion lock on exchanger state. */
    private Lock lock;
    
    /** Condition used to wait for a match to arrive. */
    private Condition condition;

    /**
     * Creates a new Exchanger.
     */
    public Exchanger() {
	lock = new Lock("Lock on Exchanger " + this);
	condition = new Condition("Exchanger condition " + this, lock);
	offerSpinlock = new SpinLock("Offer spin lock for " + this);
    }

    /**
     * Waits for another thread to arrive at this exchange point
     * and then transfers the given object to it, receiving the other
     * thread's object in return.
     *
     * @param x  The object to exchange
     * @return  The object provided by the other thread.
     */
    public V exchange(V x) {
	try {
	    return exchange(x, 0);
	} catch(TimeoutException e) {
	    Debug.ASSERT(false, "TimeoutException thrown, but no timeout specified");
	    return null;
	}
    }

    public static class TimeoutException extends Exception { }

    /**
     * Waits for another thread to arrive at this exchange point
     * and then transfers the given object to it, receiving the other
     * thread's object in return.
     *
     * @param x  The object to exchange
     * @int timeout  If positive, then the value is the maximum number of
     * "ticks" of time to wait before timing out and throwing TimeoutException.
     * If zero, a thread will wait as long as necessary for another thread
     * to arrive.
     * @return  The object provided by the other thread.
     * @throws TimeoutException  if a timeout was specified and the caller
     * thread has been waiting that amount of time for another thread to arrive.
     */
    public V exchange(V x, int timeout) throws TimeoutException {
	InterruptHandler handler = null;
	try {
	    // Exclude other threads.
	    lock.acquire();
	    
	    // Prevent interrupt handlers on other CPUs from looking at offer.
	    offerSpinlock.acquire();
	    
	    // Now safe to look at offer.
	    if(pendingOffer == null) {   // No waiting thread
		Offer offer = new Offer(x);
		if(timeout > 0) {
		    offer.ticksToWait = timeout;
		    handler = new TimerInterruptHandler(offer);
		    Nachos.timerService.addHandler(handler);
		}
		pendingOffer = offer;
		offerSpinlock.release();  // Release spin lock before sleeping.
		
		condition.await();
		
		// Here we have been awakened, so it must be that either a
		// timeout has occurred or the exchange has succeeded.
		// In either case, the pendingOffer instance variable will have been
		// nulled out and we will be the only one left with a reference
		// to it (in local variable myOffer).  So it is safe to look
		// at the offer object without further locking.
		if(timeout > 0 && offer.ticksToWait <= 0)
		    // We asked for a timeout and time has expired.
		    throw new TimeoutException();
		else
		    // The exchange must have succeeded.
		    return offer.object;
	    } else {
		// Thread already waiting -- make the exchange.
		V result = pendingOffer.swap(x);
				
		// Null out pendingOffer field to prevent another thread from accepting it
		pendingOffer = null;
		
		// Wake up the offering thread.
		condition.signal();
		
		// At this point, nobody but original creator has a reference to the offer.
		offerSpinlock.release();
		return result;
	    }
	} finally {  // Always executed, even if exception thrown
	    if(handler != null)
	        Nachos.timerService.removeHandler(handler);
	    lock.release();
	}
    }

    /**
     * Interrupt handler called by the general-purpose timer device
     * each time one "tick" of time goes by.  This handler should do
     * what is necessary to wake up a waiting thread whose timeout
     * has expired.  Note that there is just one timer device, but
     * you will want to be able to use multiple Exchanger objects
     * at once, so it will be necessary to 
     */
    private class TimerInterruptHandler implements InterruptHandler {
	
	/**
	 * The offer with which this interrupt handler is associated.
	 * Before doing anything, we check to see that pendingOffer == offer,
	 * to avoid situations in which this interrupt handler gets invoked "late",
	 * when the original offer has already been accepted and a new offer
	 * has subsequently been created.
	 */
	private Offer offer;
	
	public TimerInterruptHandler(Offer offer) {
	    this.offer = offer;
	}
	
	@Override
	public void handleInterrupt() {
	    // Make sure no other CPU is manipulating offer while we are.
	    offerSpinlock.acquire();
	    
	    // Check that the offer is still active before doing anything.
	    if(pendingOffer == offer) {
		pendingOffer.ticksToWait -= Nachos.timerService.getResolution();
		if(pendingOffer.ticksToWait <= 0) {
		    // Null out the offer so that no thread can accept it.
		    // The offering thread still has a reference to the offer object
		    // in a local variable.
		    pendingOffer = null;
		    condition.signal();
		}
	    }
	    offerSpinlock.release();
	}
    }

}
