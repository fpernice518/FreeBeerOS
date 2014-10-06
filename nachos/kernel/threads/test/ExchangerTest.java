package nachos.kernel.threads.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.threads.Exchanger;
import nachos.kernel.threads.Exchanger.TimeoutException;
import nachos.machine.NachosThread;

/**
 * Test/demonstration of the Exchanger class.
 * 
 * @author E. Stark
 * @version 20141001
 */

public class ExchangerTest {
    
    private static final int NUM_EXCHANGERS = 1;
    private static final int THREADS_PER_EXCHANGER = 3;
    private static final int EXCHANGES_PER_THREAD = 3;
    private static final int WASTED_TIME = 10;
    private static final double TIMEOUT_PROB = 0.7;
    private static final int MAX_TIMEOUT = 10000;
    
    /**
     * Entry point for the test:
     * NUM_EXCHANGERS Exchanger objects are created.
     * For each exchanger, THREADS_PER_EXCHANGER threads are created.
     * Each thread attempts EXCHANGES_PER_THREAD exchanges.
     * Before each exchange attempt, a thread wastes time by making
     * a random number of calls (from 0 to WASTED_TIME-1) calls to yieldThread().
     * On each exchange attempt, a thread will specify a timeout with
     * probability TIMEOUT_PROB.
     * If a timeout is specified, it will be a random number of ticks in
     * the range 0 to MAX_TIMEOUT-1.
     * The objects exchanged by the threads are lists that accumulate
     * the names of the threads who have handled them.
     * This test can produce a lot of different scenarios, but it would
     * be better if it were easier to check that the results make sense.
     */
    public static void start() {
	Debug.println('+', "Entering ExchangerTest");
	final Random random = new Random();
	for(int i = 0; i < NUM_EXCHANGERS; i++) {
	    final Exchanger<List<String>> exchanger = new Exchanger<List<String>>();
	    for(int j = 0; j < THREADS_PER_EXCHANGER; j++) {
		NachosThread t =
			new NachosThread
			("Exchanger " + i + " thread " + j,
		         new Runnable() {
			    @Override
			    public void run() {
				String name = NachosThread.currentThread().name;
				List<String> myObject = new ArrayList<String>();
				Debug.println('+', name + " starting");
				for(int i = 0; i < EXCHANGES_PER_THREAD; i++) {
				    // Add my name to the list
				    myObject.add(name);
				    
				    // Waste some time randomly
				    int t = random.nextInt(WASTED_TIME);
				    for(int j = 0; j < t; j++)
					Nachos.scheduler.yieldThread();

				    // Now try an exchange
				    try {
					boolean doTimeout = random.nextDouble() < TIMEOUT_PROB;
					int timeout = doTimeout ? random.nextInt(MAX_TIMEOUT) : 0;
					Debug.println('+', name + " offering exchange:"
							+ (doTimeout ? " timeout=" + timeout : " (no timeout)"));
					myObject = exchanger.exchange(myObject, timeout);
					Debug.println('+', name + " got " + myObject);
				    } catch(TimeoutException x) {
					Debug.println('+', name + " timed out");
				    }
				}
				Debug.println('+', name + " finishing");
				Nachos.scheduler.finishThread();
			    }
			});
		Nachos.scheduler.readyToRun(t);
	    }
	}
    }
}
