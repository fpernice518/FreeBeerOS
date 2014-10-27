package nachos.kernel.threads;

import java.util.Queue;

import nachos.machine.NachosThread;

public class LotteryQueue implements Queue_I
{
    final int maxTickets = 42;
    final int minTickets = 1;
    int currentTicketsInUse;
//    private Queue<NachosThread> readyList = new Queue<NachosThread>();
    LotteryQueue(){
        currentTicketsInUse = 1;
    }
    @Override
    public boolean offer(Object thread)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object poll()
    {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Object peek()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
