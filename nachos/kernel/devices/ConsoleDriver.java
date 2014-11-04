// ConsoleDriver.java
//
// Copyright (c) 2003 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and 
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.devices;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import nachos.Debug;
import nachos.machine.CPU;
import nachos.machine.Console;
import nachos.machine.InterruptHandler;
import nachos.kernel.threads.Lock;
import nachos.kernel.threads.Semaphore;

/**
 * This class provides for the initialization of the NACHOS console, and gives
 * NACHOS user programs a capability of outputting to the console. This driver
 * does not perform any input or output buffering, so a thread performing output
 * must block waiting for each individual character to be printed, and there are
 * no input-editing (backspace, delete, and the like) performed on input typed
 * at the keyboard.
 * 
 * Students will rewrite this into a full-fledged interrupt-driven driver that
 * provides efficient, thread-safe operation, along with echoing and
 * input-editing features.
 * 
 * @author Eugene W. Stark
 */
public class ConsoleDriver
{

    /** Raw console device. */
    private Console console;

    /** Lock used to ensure at most one thread trying to input at a time. */
    private Lock inputLock;

    /** Lock used to ensure at most one thread trying to output at a time. */
    private Lock outputLock;

    /** Semaphore used to indicate that an input character is available. */
    private Semaphore charAvail = new Semaphore("Console char avail", 0);

    
    /** Output variables and constants **/
    private static final int     OUTBUFFMAX = 10;
    private static final int     OUTBUFFMIN = 2;
    private Semaphore            outputBufferSpaceAvail = new Semaphore("Output Buffer Semaphore", OUTBUFFMAX);
    private Lock                 outputBufferLock = new Lock("Output Buffer Lock");
    private boolean              outputBusy = false;
    private boolean              outputStalled = false;
    private int                  waitingOutThreads = 0;
    private Queue<Character>     outputBuffer = new LinkedList<>();
    
    /** Echo variables and constants **/
    private static final int     ECHOBUFFMAX = 10000;
    private static final int     ECHOBUFFMIN = 1;
    private static int           echoBuffIndex = 0;
    private Semaphore            echoBufferSpaceAvail = new Semaphore("Echo Buffer Semaphore", ECHOBUFFMAX);
    private Queue<Character>     echoBuffer = new LinkedList<>();
    private Stack<Character>     ctrlRBuffer = new Stack<>();

    /** Interrupt handler used for console keyboard interrupts. */
    private InterruptHandler inputHandler;

    /** Interrupt handler used for console output interrupts. */
    private InterruptHandler outputHandler;

    /**
     * Initialize the driver and the underlying physical device.
     * 
     * @param console
     *            The console device to be managed.
     */
    public ConsoleDriver(Console console)
    {
        inputLock = new Lock("console driver input lock");
        outputLock = new Lock("console driver output lock");
        this.console = console;
        // Delay setting the interrupt handlers until first use.
    }

    /**
     * Create and set the keyboard interrupt handler, if one has not already
     * been set.
     */
    private void ensureInputHandler()
    {
        if (inputHandler == null)
        {
            inputHandler = new InputHandler();
            console.setInputHandler(inputHandler);
        }
    }

    /**
     * Create and set the output interrupt handler, if one has not already been
     * set.
     */
    private void ensureOutputHandler()
    {
        if (outputHandler == null)
        {
            outputHandler = new OutputHandler();
            console.setOutputHandler(outputHandler);
        }
    }

    /**
     * Wait for a character to be available from the console and then return the
     * character.
     */
    public char getChar()
    {
        inputLock.acquire();
        ensureInputHandler();
        charAvail.P();
//        Debug.ASSERT(console.isInputAvail());
        char ch = console.getChar();//TODO, swap with the buffer
        inputLock.release();
        return ch;
    }

    /**
     * Print a single character on the console. If the console is already busy
     * outputting a character, then wait for it to finish before attempting to
     * output the new character. A lock is employed to ensure that at most one
     * thread at a time will attempt to print.
     *
     * @param ch
     *            The character to be printed.
     */
    public void putChar(char ch)
    {   
        outputBufferLock.acquire();
        ensureOutputHandler();
        int oldLevel = CPU.setLevel(CPU.IntOff);
        while(outputBuffer.size() >= OUTBUFFMAX)
        {
            ++waitingOutThreads;
            outputBufferLock.release();
            outputBufferSpaceAvail.P();
            outputBufferLock.acquire();
        }
        
        outputBuffer.add(ch);
        startOutput();
        CPU.setLevel(oldLevel);
        outputBufferLock.release();
    }
    
    public void echo(char ch)
    {   
        ensureOutputHandler();
        int oldLevel = CPU.setLevel(CPU.IntOff);
//        while(outputBuffer.size() >= OUTBUFFMAX)
//        {
//            ++waitingOutThreads;
//            outputBufferLock.release();
//            outputBufferSpaceAvail.P();
//            outputBufferLock.acquire();
//        }
        
        outputBuffer.add(ch);
        startOutput();
        echoBufferSpaceAvail.V();
        CPU.setLevel(oldLevel);
    }
    
    private void startOutput()
    {
        if(outputStalled || outputBusy || (outputBuffer.size() == 0))
            return;
        
        outputBusy = true;
        char ch = outputBuffer.remove();
        console.putChar(ch);
        if(outputBuffer.size() <= OUTBUFFMIN)
        {
            while(waitingOutThreads > 0)
            {
                --waitingOutThreads;
                outputBufferSpaceAvail.V();
            }
                
        }
    }

    /**
     * Stop the console device. This removes the interrupt handlers, which
     * otherwise prevent the Nachos simulation from terminating automatically.
     */
    public void stop()
    {
        inputLock.acquire();
        console.setInputHandler(null);
        inputLock.release();
        outputLock.acquire();
        console.setOutputHandler(null);
        outputLock.release();
    }

    /**
     * Interrupt handler for the input (keyboard) half of the console.
     */
    private class InputHandler implements InterruptHandler
    {

        @Override
        public void handleInterrupt()
        {
            int inc = 0;
            boolean clearBuff = false;
            boolean ctrlU = false;
            boolean ctrlR = false;
            char ch = console.getChar();
            switch(ch)
            {
                case '\n':
                    echoBuffer.add('\r');
                    echoBuffer.add(ch);
                    clearBuff = true;
                    inc = 2;
                    break;
                case '\r':
                    echoBuffer.add(ch);
                    echoBuffer.add('\n');
                    clearBuff = true;
                    inc = 2;
                    break;
                case '\b':
                    echoBuffer.add('\b');
                    echoBuffer.add(' ');
                    echoBuffer.add('\b');
                    if(!ctrlRBuffer.isEmpty())
                        ctrlRBuffer.pop();
                    inc = 3;
                    break;
                case (char)21:
                    ctrlU = true;
                    break;
                case (char)18:
                    ctrlR = true;
                    break;
                    
                default:
                {
                    if((ch >= 32 && ch <= 126))
                    {
                        ctrlRBuffer.add(ch);
                        echoBuffer.add(ch);
                        inc = 1;
                    }
                }
            }
            
            Iterator<Character> buffIterator = echoBuffer.iterator();
            
            int i = 0;
            while(i < echoBuffIndex)
            {
                buffIterator.next();
                ++i;
            }
            
            if(!ctrlU && !ctrlR)
            {
                while(buffIterator.hasNext())
                {
                    echoBufferSpaceAvail.P();
                    echo(buffIterator.next());
                }
            }
            else if(ctrlU)
            {
                int lineSize = echoBuffer.size();
                
                echoBufferSpaceAvail.P();
                echo('\r');
                for(int j = 0; j < lineSize; ++j)
                {
                    echoBufferSpaceAvail.P();
                    echo(' ');
                }
                echoBufferSpaceAvail.P();
                echo('\r');
                ctrlRBuffer.clear();
            }
            else if(ctrlR)
            {
                echoBufferSpaceAvail.P();
                echo('\r');
                buffIterator = ctrlRBuffer.iterator();
                int lineSize =echoBuffer.size();
                
                for(int j = 0; j < lineSize; ++j);
                {
                    echoBufferSpaceAvail.P();
                    echo(' ');
                }
                
                echoBufferSpaceAvail.P();
                echo('\r');
                
                while(buffIterator.hasNext())
                {
                    char c = buffIterator.next();
                    echoBufferSpaceAvail.P();
                    echo(c);
                }
            }
            
            echoBuffIndex += inc;
            
            if(clearBuff)
            {
                echoBuffer.clear();
                ctrlRBuffer.clear();
                echoBuffIndex = 0;
//                charAvail.V();    
            }
        }

    }

    /**
     * Interrupt handler for the output (screen) half of the console.
     */
    private class OutputHandler implements InterruptHandler
    {
        @Override
        public void handleInterrupt()
        {
            outputBusy = false;
            startOutput();
        }

    }
}
