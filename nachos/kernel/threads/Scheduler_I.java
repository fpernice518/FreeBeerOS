package nachos.kernel.threads;

import nachos.machine.NachosThread;

public interface Scheduler_I
{
    boolean offer(NachosThread thread);
    
    boolean isEmpty();
    
    NachosThread poll();
    
}
