package nachos.kernel.threads;

import nachos.machine.NachosThread;
import nachos.util.FIFOQueue;
import nachos.util.Queue;

public class OriginalQueue implements Queue_I<NachosThread>
{
    private final Queue<NachosThread> readyList;
  


    OriginalQueue(){
        readyList = new FIFOQueue<NachosThread>();
    }

    public boolean offer(NachosThread thread)
    {
        // TODO Auto-generated method stub
        return readyList.offer(thread);
    }

    @Override
    public boolean isEmpty()
    {
        // TODO Auto-generated method stub
        return readyList.isEmpty();
    }

    @Override
    public NachosThread poll()
    {
        // TODO Auto-generated method stub
        return readyList.poll();
    }

}
