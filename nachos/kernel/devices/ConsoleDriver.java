// ConsoleDriver.java
//
// Copyright (c) 2003 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and 
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.devices;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import nachos.Debug;
import nachos.machine.CPU;
import nachos.machine.Console;
import nachos.machine.InterruptHandler;
import nachos.machine.Machine;
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

    /**
     * Semaphore used to indicate that output is ready to accept a new
     * character.
     */
    private Semaphore outputDone = new Semaphore("Console output done", 1);
    
    /** Output variables and constants **/
    private Semaphore            outputBufferSpaceAvail = new Semaphore("Output Buffer Semaphore", 10);
    private Lock                 outputBufferLock = new Lock("Output Buffer Lock");
    private boolean              outputBusy = false;
    private boolean              outputStalled = false;
    private int                  waitingOutThreads = 0;
    private final int            OUTBUFFMAX = 10;
    private static final int     OUTBUFFMIN = 2;
    private Queue<Character> outputBuffer = new LinkedList<>();
    
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
        Debug.ASSERT(console.isInputAvail());
        char ch = console.getChar();
        inputLock.release();
        return ch;
    }
    
    /**
     * This looks like the old putchar() because we want to echo
     * instantaneously and not wait for the user to input 10
     * characters.
     * @param ch character to be echoed
     */
    private void echo(char ch)
    {
        outputLock.acquire();
        ensureOutputHandler();
        outputDone.P();
        Debug.ASSERT(!console.isOutputBusy());
        console.putChar(ch);
        outputLock.release();
        
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
        
        
        
//        if(outputRunning == false)
//        {
//            outputLock.acquire();                 
//            ensureOutputHandler();                
//            outputDone.P();                       
//            Debug.ASSERT(!console.isOutputBusy());
//            //buffer.add(ch);
//            console.putChar(ch);
//            outputRunning = true;
//            outputLock.release();                 
//        }
//        else
//        {
//            outputBufferSpaceAvail.P();
//            outputBufferLock.acquire();
//            ++testint;
//            buffer.add(ch);
//            outputBufferLock.release();    
//        }
        
//        outputLock.acquire();
//        ensureOutputHandler();
//        outputDone.P();
//        Debug.ASSERT(!console.isOutputBusy());                
//        console.putChar(ch);
//        outputLock.release(); 


//        outputLock.acquire();
//        if(buffer.size() < 10)
//        {
//            buffer.add(ch);
//        }
//        else
//        {       
//            ensureOutputHandler();
//            
//            for(int i = 0; i < buffer.size(); ++i)
//            {               
//                outputDone.P();
//                Debug.ASSERT(!console.isOutputBusy());                
//                console.putChar(buffer.get(i).charValue());
//            }
//
//            buffer.clear();
//            buffer.add(ch);
//        }
//        outputLock.release();
    }

    /* 
        private void startOutput () {
    // If output stalled or already busy or nothing to do
        if(stalled || busy || outQ.length() == 0)
        return;                    // then just return.
        busy = true;                   // Mark device busy
    char c = outQ.remove();        // Dequeue first character
        XMIT(c);                       // and transmit it.
        if(outQ.length() <= OUTQLOWAT) 
        {
        while(waitingThreads > 0) {  // If output queue getting short
        waitingThreads--;
        spaceAvail.V();        // wakeup any blocked threads.
        }
    }
    }
     */
    private void startOutput()
    {
        if(outputStalled || outputBusy || outputBuffer.size() == 0)
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
            charAvail.V();
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
            //outputDone.V();
        }

    }
}
