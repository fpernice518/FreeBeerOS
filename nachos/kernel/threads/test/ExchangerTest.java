package nachos.kernel.threads.test;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.threads.CountDownLatch;
import nachos.kernel.threads.Exchanger;
import nachos.kernel.threads.SpinLock;
import nachos.machine.NachosThread;
class Person{
    private String name;
    Person(String name){
        this.name = name;
    }
    public String getName()
    {
        return name;
    }
    
}

    class FirstThread implements Runnable
    {
        private Exchanger exchanger;
      
        private String name;
        
       FirstThread(Exchanger exchanger, String name)
        {
            this.exchanger = exchanger;
            this.name = name;
        }
       
        @Override
        public void run()
        {
            Person p = new Person(name);
            for(int i = 0 ; i<30; i++){
                Nachos.scheduler.yieldThread();
            }
            System.out.println("Pre Name of thread: "+name+"\tName of object: "+p.getName());
            p = (Person) exchanger.exchange(p);
            System.out.println("post Name of thread: "+name+"\tName of object: "+p.getName());
            Nachos.scheduler.finishThread();
        }

    }
  

    public class ExchangerTest {
    

        private int which;
        private Exchanger exchanger;
        private SpinLock spinLock;
        public ExchangerTest(int w)
        {

            which = w;
            exchanger = new Exchanger();
            NachosThread t;
            
            int x = 0;
            for (int i = 0; i < 30; i++)
            {
                System.out.println("Exchanger Thread " + i);
                Nachos.scheduler.readyToRun(new NachosThread("thread " + i,
                        new FirstThread(exchanger,"number: "+i)));
            }
            System.out.println();
//            for (int i = 0; i < 2; i++)
//            {
//                System.out.println("Exchanger Thread " + i);
//                Nachos.scheduler.readyToRun(new NachosThread("bobthread " + i,
//                        new FirstThread(exchanger,"bobThread: "+i)));
//            }


            Debug.println('+', "all threads done "+(x+1));

        }



        public static void start()
        {

            Debug.println('+', "Entering ThreadTest");
            ExchangerTest cdlt =new ExchangerTest(1);

        }
    
}
