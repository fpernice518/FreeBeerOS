package nachos.kernel.threads;

import nachos.Debug;
import nachos.kernel.Nachos;

/**
 * A CountDownLatch is an object that provides a thread with the ability to
 * block until a specified number of other activities have completed. A
 * CountDownLatch object is created with a specified initial count. A thread
 * calling the await() method blocks within the method call if the count is
 * greater than zero. Calls to the countDown() method decrement the count. When
 * the count reaches zero, any threads blocked within await() are awakened and
 * allowed to proceed. A CountDownLatch can only be used once; there is no
 * facility for resetting the count and starting over.
 */
public class CountDownLatch
{
    private Semaphore semaphore;
    private Semaphore on_off_switch;
    private int count;
    private boolean done = false;

    /**
     * Initialize a CountDownLatch with a specified initial count value.
     *
     * @param count
     *            The initial count.
     */
    public CountDownLatch(int count)
    {
        semaphore = new Semaphore("Countdown Latch", 0);
        on_off_switch = new Semaphore("Variable Latch", 1);
        this.count = count;
    }

    /**
     * Decrement the count. If the new count is less than or equal to zero, any
     * threads blocked within calls to await() are awakened and allowed to
     * proceed.
     */
    public void countDown()
    {
       
        on_off_switch.P();
        if (count > 0)
        {
            --count;
            Debug.println('+', "Current Count : " + count);
            if(count == 0){
            semaphore.V();
            }

        }
        on_off_switch.V();


    }

    /**
     * When this method is called, if the count is greater than zero the caller
     * will block within the method call until the count has been decremented to
     * zero or less. If the count is already less than or equal to zero at the
     * time await() is called, it returns immediately.
     */
    public void await()
    {

 

        semaphore.P();
            Debug.println('+', count + " Flood Gates Released");
        semaphore.V();

    }
}
