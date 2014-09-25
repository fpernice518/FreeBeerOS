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
        on_off_switch = new Semaphore("Variable Latch", 0);
        this.count = count;
    }

    /**
     * Decrement the count. If the new count is less than or equal to zero, any
     * threads blocked within calls to await() are awakened and allowed to
     * proceed.
     */
    public void countDown()
    {
        // if(count<=0)
        // {
        // semaphore.V();
        on_off_switch.P();
        if (count > 0)
        {
            --count;
            semaphore.V();
        }
        on_off_switch.V();

        // semaphore.V();
        // }

    }

    /**
     * When this method is called, if the count is greater than zero the caller
     * will block within the method call until the count has been decremented to
     * zero or less. If the count is already less than or equal to zero at the
     * time await() is called, it returns immediately.
     */
    public void await()
    {

        // while(count > 0)
        // {
        // // what thread r we at?!?!
        // System.out.println("name  : " +name);
        //
        // }

        // semaphore.V();
        // System.out.println("\n\n\n\n\n\n\n\n\n"+count+"\n\n\n\n\n\n\n\n\n\n\n\n\n");
  

            // for(int i = 0; i < count_const; ++i)
        
            semaphore.P();
//            Debug.println('+', "Helo");
            semaphore.V();

//            Debug.println('+', "flood gate release Count ended at " + count);


        
    }
}
