package nachos.kernel.threads;

public interface Queue_I <T>
{
    boolean offer(T thread);
    
    boolean isEmpty();
    
    T poll();
    
}
