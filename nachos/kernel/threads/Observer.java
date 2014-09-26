package nachos.kernel.threads;

public abstract class Observer {
    protected TimerService subject;
    public abstract void update();
 }
