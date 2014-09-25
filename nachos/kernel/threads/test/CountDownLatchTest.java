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
        
//        cdl.countDown();
          cdl.await();
//        cdl.await(name);
        Debug.println('+', "Count Downed");
        Nachos.scheduler.finishThread();
    }

}

class awaitThread implements Runnable
{
    private CountDownLatch cdl;
    private SpinLock sl;
    private String name;
    
    awaitThread(CountDownLatch cdl, String name)
    {
        this.cdl = cdl;
        this.name = name;
    }
   
    @Override
    public void run()
    {
//        cdl.await();
        cdl.countDown();
        
//        cdl.await(name);
        Debug.println('+', "Count Downed");
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
        cdl = new CountDownLatch(2);
        NachosThread t;
        int x = 0;
        spinLock = new SpinLock("name" + " spin lock");
        for (int i = 0; i < 1; i++)
        {
            // t = new NachosThread("Test thread " + w, this);
            System.out.println("Thread " + i);
            Nachos.scheduler.readyToRun(new NachosThread("await thread " + i,
                    new testThread(cdl,"test "+w)));
            //
//            Debug.println('+', i+"");
            x = i;
        }
        for (int i = 0; i < 10; i++)
        {
            // t = new NachosThread("Test thread " + w, this);
            System.out.println("Thread " + i);
            Nachos.scheduler.readyToRun(new NachosThread("Test thread " + i,
                    new testThread(cdl,"test "+w)));
            //
//            Debug.println('+', i+"");
            x = i;
        }
//        cdl.await();
        Debug.println('+', "all threads done "+x);

    }

//    public void run()
//    {
//        Debug.println('+', "thread");
//        cdl.countDown();
//        //
//        // // cdl.await();
//        //
//        Nachos.scheduler.finishThread();
//
//    }

    public static void start()
    {

        Debug.println('+', "Entering ThreadTest");
        CountDownLatchTest cdlt =new CountDownLatchTest(1);
//        CountDownLatchTest.start();
//        new CountDownLatchTest(2);
    }

}
