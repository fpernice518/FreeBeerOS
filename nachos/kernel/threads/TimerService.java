package nachos.kernel.threads;

import nachos.machine.NachosThread;
import java.util.ArrayList;
import java.util.List;


public class TimerService {
    
   private List<NachosThread> observers 
      = new ArrayList<NachosThread>();
   private int state;

   public int getState() {
      return state;
   }

   public void setState(int state) {
      this.state = state;
      notifyAllObservers();
   }

   public void attach(NachosThread observer){
      observers.add(observer);      
   }

   public void notifyAllObservers(){
      for (NachosThread observer : observers) {
//         observer.update();
      }
   }    
}
