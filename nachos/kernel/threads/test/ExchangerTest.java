package nachos.kernel.threads.test;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.threads.Exchanger;
import nachos.kernel.threads.Exchanger.TimeoutException;
import nachos.kernel.threads.KernelThread;
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
        for (int i = 0; i < 30; i++)
        {
            Nachos.scheduler.yieldThread();
        }

        System.out.println("Pre Name of thread: " + name + "\tName of object: "
                + p.getName());
        try
        {
            p = (Person) exchanger.exchange(p, 3);
        } catch (TimeoutException e)
        {
            System.out.println("Shit ended");
            Nachos.scheduler.finishThread();
        }
        System.out.println("post Name of thread: " + name
                + "\tName of object: " + p.getName());
        Nachos.scheduler.finishThread();

        // Person p = new Person(name);
        // for(int i = 0 ; i<30; i++){
        // Nachos.scheduler.yieldThread();
        // }
        // System.out.println("Pre Name of thread: "+name+"\tName of object: "+p.getName());
        // p = (Person) exchanger.exchange(p);
        // System.out.println("post Name of thread: "+name+"\tName of object: "+p.getName());
        // Nachos.scheduler.finishThread();
    }

}

class SecondThread extends Observer implements Runnable 
{
    private Exchanger<Person> exchanger;

    private String name;

    SecondThread(Exchanger<Person> exchanger, String name)
    {
        this.exchanger = exchanger;
        this.name = name;
    }

    @Override
    public void run()
    {
        Person p = new Person(name);
        for (int i = 0; i < 30; i++)
        {
            Nachos.scheduler.yieldThread();
        }

        System.out.println("Pre Name of thread: " + name + "\tName of object: "
                + p.getName());
        try
        {
            p = (Person) exchanger.exchange(p, 1);
        } catch (TimeoutException e)
        {
            System.out.println("Shit ended");
            Nachos.scheduler.finishThread();
        }
        System.out.println("post Name of thread: " + name
                + "\tName of object: " + p.getName());
        Nachos.scheduler.finishThread();

        // Person p = new Person(name);
        // for(int i = 0 ; i<30; i++){
        // Nachos.scheduler.yieldThread();
        // }
        // System.out.println("Pre Name of thread: "+name+"\tName of object: "+p.getName());
        // p = (Person) exchanger.exchange(p);
        // System.out.println("post Name of thread: "+name+"\tName of object: "+p.getName());
        // Nachos.scheduler.finishThread();
    }

    @Override
    public void update()
    {
        
        
    }

}


public class ExchangerTest
{
    private Exchanger<Person> exchanger;

    public ExchangerTest(int w)
    {
        
        if(w == 1){
            exchanger = new Exchanger<Person>();
            
            int x = 0;
            for (int i = 0; i < 5; i++)
            {
                System.out.println("Exchanger Thread " + i);
                Nachos.scheduler.readyToRun(new KernelThread("thread " + i,
                        new FirstThread(exchanger, "number: " + i)));
            }
            System.out.println();
           

            Debug.println('1', "all threads done " + (x + 1));

            
        }
        
        if(w == 2){
            exchanger = new Exchanger<Person>();
        
        int x = 0;
        for (int i = 0; i < 5; i++)
        {
            System.out.println("Exchanger Thread " + i);
            Nachos.scheduler.readyToRun(new KernelThread("thread " + i,
                    new SecondThread(exchanger, "number: " + i)));
        }
        System.out.println();
       

        Debug.println('1', "all threads done " + (x + 1));
        }
       

    }

    public static void start()
    {

        Debug.println('1', "Entering ThreadTest");
        Debug.println('1', "Entering Exchange()");
        ExchangerTest cdlt = new ExchangerTest(1);
        Debug.println('1', "Entering Exchange(Timeout)");
        cdlt = new ExchangerTest(2);

    }

}
