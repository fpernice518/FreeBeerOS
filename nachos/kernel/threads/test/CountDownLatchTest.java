package nachos.kernel.threads.test;

import java.util.Random;

import nachos.Debug;
import nachos.machine.NachosThread;
import nachos.kernel.Nachos;
import nachos.kernel.threads.CountDownLatch;
import nachos.kernel.threads.SpinLock;

//only counts down
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
        Random rand = new Random();
        int randomInt = (int)rand.nextInt();
        for(int i = 0; i < randomInt; ++i);
        cdl.countDown();
        Debug.println('1', "Counted Down");
        Nachos.scheduler.finishThread();
    }

}
//counts down and then awaits
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
        Random rand = new Random();
        int randomInt = (int)rand.nextInt();
        for(int i = 0; i < randomInt; ++i);
        
        cdl.countDown();
        Debug.println('1', "Counted Down and waiting");
        cdl.await();
        
        Nachos.scheduler.finishThread();
    }

}
//merely awaits, never counts down
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
        Random rand = new Random();
        int randomInt = (int)rand.nextInt();
        for(int i = 0; i < randomInt; ++i);
        
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
        for (int i = 0; i < 2; i++)
        {
            System.out.println("Await Thread " + i);
            Nachos.scheduler.readyToRun(new NachosThread("await thread " + i,
                    new awaitThread(cdl,"await test "+w)));
        }
        for (int i = 0; i < 3; i++)
        {
            System.out.println("WaitDec Thread " + i);
            Nachos.scheduler.readyToRun(new NachosThread("WaitDec thread " + i,
                    new WaitDecThread(cdl,"WaitDec test "+w)));
        }
        for (int i = 0; i < 4; i++)
        {
            System.out.println("Thread " + i);
            Nachos.scheduler.readyToRun(new NachosThread("Test thread " + i,
                    new testThread(cdl,"test "+w)));

            x = i;
        }

        Debug.println('1', "all threads done "+x);

    }



    public static void start()
    {

        Debug.println('1', "Entering CountdownLatch Test");
        CountDownLatchTest cdlt =new CountDownLatchTest(1);

    }

}
