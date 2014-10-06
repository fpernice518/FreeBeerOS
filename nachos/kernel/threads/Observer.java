package nachos.kernel.threads;

import nachos.util.TimerService;

public abstract class Observer {
    protected TimerService subject;
    public abstract void update();
 }
