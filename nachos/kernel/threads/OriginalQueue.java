package nachos.kernel.threads;

import nachos.machine.NachosThread;
import nachos.util.FIFOQueue;
import nachos.util.Queue;

public class OriginalQueue implements Queue_I<NachosThread>
{
    private final Queue<NachosThread> readyList;

    OriginalQueue()
    {
        readyList = new FIFOQueue<NachosThread>();
    }

    public boolean offer(NachosThread thread)
    {
        return readyList.offer(thread);
    }

    @Override
    public boolean isEmpty()
    {
        return readyList.isEmpty();
    }

    @Override
    public NachosThread poll()
    {
        return readyList.poll();
    }

    @Override
    public NachosThread peek()
    {
        return readyList.peek();
    }

}
