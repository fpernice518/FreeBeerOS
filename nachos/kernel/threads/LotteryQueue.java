package nachos.kernel.threads;

public class LotteryQueue<T> extends java.util.LinkedList<T> implements Queue_I<T>
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
        return this.add(thread);
    }

    @Override
    public boolean isEmpty()
    {
        return (this.size() == 0);
    }

    @Override
    public T poll()
    {
        if (!isEmpty())
        {
            return this.pollFirst();
        } 
        
        else
        {
            return null;
        }
    }

    @Override
    public T peek()
    {
        return null;
    }

}
