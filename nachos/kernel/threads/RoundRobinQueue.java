package nachos.kernel.threads;

import nachos.machine.NachosThread;
import nachos.util.FIFOQueue;
import nachos.util.Queue;

public class RoundRobinQueue implements Queue<KernelThread>
{
    private final Queue<KernelThread> readyList;

    RoundRobinQueue()
    {
        readyList = new FIFOQueue<KernelThread>();
    }

    public boolean offer(KernelThread thread)
    {
        return readyList.offer(thread);
    }

    @Override
    public boolean isEmpty()
    {
        return readyList.isEmpty();
    }

    @Override
    public KernelThread poll()
    {
        return readyList.poll();
    }

    @Override
    public KernelThread peek()
    {
        return readyList.peek();
    }

}
