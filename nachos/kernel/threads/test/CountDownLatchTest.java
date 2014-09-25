package nachos.kernel.threads.test;

import nachos.Debug;
import nachos.machine.NachosThread;
import nachos.kernel.Nachos;
import nachos.kernel.threads.CountDownLatch;
import nachos.kernel.threads.SpinLock;

class testThread implements Runnable
{
    private CountDownLatch cdl;
    private SpinLock sl;
    private String name;
    
    testThread(CountDownLatch cdl, String name)
    {
        this.cdl = cdl;
        this.name = name;
    }
   
    @Override
    public void run()
    {
        
        cdl.countDown();
        Debug.println('+', "Count Downed");
        Nachos.scheduler.finishThread();
    }

}
class WaitDecThread implements Runnable
{
    private CountDownLatch cdl;
    private SpinLock sl;
    private String name;
    
    WaitDecThread(CountDownLatch cdl, String name)
    {
        this.cdl = cdl;
        this.name = name;
    }
   
    @Override
    public void run()
    {
        
        cdl.countDown();
        Debug.println('+', "Count Downed and waiting");
        cdl.await();
        
        Nachos.scheduler.finishThread();
    }

}

class awaitThread implements Runnable
{
    private CountDownLatch cdl;
 
    
    awaitThread(CountDownLatch cdl, String name)
    {
        this.cdl = cdl;

    }
   
    @Override
    public void run()
    {
        cdl.await();
        
        Nachos.scheduler.finishThread();
    }

}
public class CountDownLatchTest
{

    private int which;
    private CountDownLatch cdl;
    private SpinLock spinLock;
    public CountDownLatchTest(int w)
    {

        which = w;
        cdl = new CountDownLatch(5);
        NachosThread t;
        int x = 0;
        for (int i = 0; i < 1; i++)
        {
            System.out.println("Await Thread " + i);
            Nachos.scheduler.readyToRun(new NachosThread("await thread " + i,
                    new awaitThread(cdl,"await test "+w)));
        }
        for (int i = 0; i < 3; i++)
        {
            System.out.println("WaitDec Thread " + i);
            Nachos.scheduler.readyToRun(new NachosThread("await thread " + i,
                    new WaitDecThread(cdl,"WaitDec test "+w)));
        }
        for (int i = 0; i < 2; i++)
        {
            System.out.println("Thread " + i);
            Nachos.scheduler.readyToRun(new NachosThread("Test thread " + i,
                    new testThread(cdl,"test "+w)));

            x = i;
        }

        Debug.println('+', "all threads done "+x);

    }



    public static void start()
    {

        Debug.println('+', "Entering ThreadTest");
        CountDownLatchTest cdlt =new CountDownLatchTest(1);

    }

}
