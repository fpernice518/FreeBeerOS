package nachos.kernel.threads;

import java.util.Queue;

import nachos.machine.NachosThread;
import nachos.util.FIFOQueue;

public class LotteryQueue<T> extends java.util.LinkedList<T> implements
        Queue_I<T>
{
    final int maxTickets = 42;
    final int minTickets = 1;
    int currentTicketsInUse;

    // private Queue<NachosThread> readyList = new Queue<NachosThread>();
    LotteryQueue()
    {
        currentTicketsInUse = 0;
    }

    @Override
    public boolean offer(T thread)
    {
        currentTicketsInUse++;
        // TODO Auto-generated method stub
        this.add(thread);
        return true;
    }

    @Override
    public boolean isEmpty()
    {
        // TODO Auto-generated method stub
        return (this.size() == 0);
    }

    @Override
    public T poll()
    {
        // TODO Auto-generated method stub
        if (!isEmpty())
        {
            return this.pollFirst();
        } else
        {
            return null;
        }
    }

    @Override
    public T peek()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
