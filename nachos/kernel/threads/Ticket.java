package nachos.kernel.threads;

public class Ticket
{
    private int ticketNumber;
    private boolean inUse;

    Ticket(int ticketNumber){
        this.ticketNumber = ticketNumber;
    }
    
    Ticket(int ticketNumber, boolean inuse){
        this.ticketNumber = ticketNumber;
        this.inUse = inuse;
    }
    
    public int getTicketNumber()
    {
        return ticketNumber;
    }

    public void setTicketNumber(int ticketNumber)
    {
        this.ticketNumber = ticketNumber;
    }

    public boolean isInUse()
    {
        return inUse;
    }

    public void setInUse(boolean inUse)
    {
        this.inUse = inUse;
    }
    
   
}
