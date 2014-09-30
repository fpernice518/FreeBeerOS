package nachos.kernel.threads;

import java.util.HashSet;
import java.util.Set;

import nachos.machine.Machine;
import nachos.machine.Timer;
import nachos.machine.InterruptHandler;


public class TimerService implements InterruptHandler
{

    private Set<InterruptHandler> observers; //Set used to ensure no duplicates
    private Timer nachosTimer;
    private static TimerService timerService = null;
    
    private TimerService()
    {
        observers = new HashSet<InterruptHandler>();
        nachosTimer = Machine.getTimer(0); 
        nachosTimer.setHandler(this);
        nachosTimer.start();
    }

    public void subscribe(InterruptHandler handler)
    {
        observers.add(handler);
    }

    public void unsubscribe(InterruptHandler handler)
    {
        observers.remove(handler);
    }
    
    @Override
    public void handleInterrupt()
    {
        for (InterruptHandler observer : observers)
        {
            observer.handleInterrupt();
        }
    }
    
    public static TimerService getTimerService()
    {
        if(timerService == null)
            timerService = new TimerService();
        
        return timerService;
    }
    
    
}
