package nachos.kernel.threads;

public interface Scheduler_I <T>
{
    boolean offer(T thread);
    
    boolean isEmpty();
    
    T poll();
    
}
