package nachos.util;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class FixedBuffer<E> implements Iterable<E>
{
    LinkedHashSet<E> buffer;
    int maxSize;
    
    public FixedBuffer(int maxSize)
    {
        this.maxSize = maxSize;
        buffer = new LinkedHashSet<E>();
    }
    
    public void add(E element)
    {
        Iterator<E> iterator = buffer.iterator();
        while(buffer.size() >= maxSize)
            buffer.remove(iterator.next());
        buffer.add(element);
    }
    
    public E remove(int index)
    {
        int i = 0;
        E element = null;
        Iterator<E> iterator = buffer.iterator();
        
        //return null if index is out of bounds
        if(index >= buffer.size() || index < 0)
            return null;
        
        //iterate to the 'index'th location
        while(i <= index)
        {
            element = iterator.next();
            ++i;
        }
        
        buffer.remove(element);
        return element;
    }
    
    
    public E moveToFront(int index)
    {
        E element = remove(index);
        add(element);
        return element;
    }
    
    
    public E get(int index)
    {
        
        int i = 0;
        E element = null;
        Iterator<E> iterator = buffer.iterator();
        
        //return null if index is out of bounds
        if(index >= buffer.size() || index < 0)
            return null;
        
        //iterate to the 'index'th location
        while(i <= index)
        {
            element = iterator.next();
            ++i;
        }
        
        return element;
    }
    
    public int getMaxSize()
    {
        return maxSize;
    }

    @Override
    public Iterator<E> iterator()
    {
        return buffer.iterator();
    }
}
