package nachos.kernel.threads.test;

import java.util.Random;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.threads.Exchanger;
import nachos.kernel.threads.Exchanger.TimeoutException;
import nachos.kernel.threads.Observer;
import nachos.kernel.threads.SpinLock;
import nachos.machine.NachosThread;

//Test object
class Person
{
    private String name;

    Person(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

}

class FirstThread implements Runnable
{
    private Exchanger<Person> exchanger;

    private String name;

    FirstThread(Exchanger<Person> exchanger, String name)
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
         
         Random rand = new Random();
         int randomInt = (int)rand.nextInt();
         for(int i = 0; i < randomInt; ++i);
         
         Debug.println('1', "Pre Name of thread: "+name+"\tName of object: "+p.getName());
         p = (Person) exchanger.exchange(p);
         Debug.println('1', "post Name of thread: "+name+"\tName of object: "+p.getName());
         Nachos.scheduler.finishThread();
    }

}



public class ExchangerTest
{
    private Exchanger<Person> exchanger;

    public ExchangerTest(int w)
    {
        exchanger = new Exchanger<Person>();

        int x = 0;
        for (int i = 0; i < 5; i++)
        {
            Debug.println('1', "Exchanger Thread " + i);
            Nachos.scheduler.readyToRun(new NachosThread("thread " + i,
                    new FirstThread(exchanger, "number: " + i)));
        }

        Debug.println('1', "all threads done " + (x + 1));

    }

    public static void start()
    {

        Debug.println('1', "Entering ExchangerTest");
        ExchangerTest cdlt = new ExchangerTest(1);

    }

}
