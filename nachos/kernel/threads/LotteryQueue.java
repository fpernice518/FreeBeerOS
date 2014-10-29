package nachos.kernel.threads;

import java.util.ArrayList;
import java.util.Iterator;

import nachos.kernel.userprog.UserThread;

public class LotteryQueue<T> extends java.util.LinkedList<T> implements
        Queue_I<T>
{
    // final int maxTickets = 42;
    // final int minTickets = 1;

    int currentTicketsInUse;
    ArrayList<Ticket> ticketsInUse;

    // private Queue<NachosThread> readyList = new Queue<NachosThread>();
    LotteryQueue()
    {
        ticketsInUse = new ArrayList<Ticket>();
        currentTicketsInUse = 0;
    }

    private Ticket getNextTicket()
    {
        Ticket foundATicket = null;
        if (ticketsInUse.size() == 0)
        {
            // its empty so make a new one
            return foundATicket;
        } else
        {
            for (Iterator iterator = ticketsInUse.iterator(); iterator
                    .hasNext();)
            {
                Ticket ticket = (Ticket) iterator.next();
                if (!ticket.isInUse())
                {
                    // found one that isnt in use
                    return ticket;
                }
            }
            // all of them are in use make a new one
            return foundATicket;

        }

    }

    @Override
    public boolean offer(T thread)
    {

        currentTicketsInUse++;
        Ticket ticket = getNextTicket();
        if (thread instanceof UserThread)
        {
            if (ticket == null)
            {
                ticket = new Ticket(currentTicketsInUse, true);
                ticketsInUse.add(ticket);
                UserThread usrt = ((UserThread) thread);
                usrt.addTicket(ticket);

                // we could not find another ticket so we need to create a new
                // one
                // and append it
            } else
            {
                // we found one now use the damn thing
                UserThread usrt = ((UserThread) thread);
                usrt.addTicket(ticket);

            }
            System.out.println(ticket.getTicketNumber());
        }
        return this.add(thread);
    }

    @Override
    public boolean isEmpty()
    {
        return (this.size() == 0);
    }

    @Override
    public T poll()
    {
        if (!isEmpty())
        {
            return this.pollFirst();
        }

        else
        {
            return null;
        }
    }

    @Override
    public T peek()
    {
        return null;
    }

}
