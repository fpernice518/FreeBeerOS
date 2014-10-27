package nachos.kernel.userprog;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;

import nachos.Debug;
import nachos.Options;
import nachos.kernel.Nachos;
import nachos.kernel.filesys.OpenFile;
import nachos.kernel.threads.Exchanger;
import nachos.machine.CPU;
import nachos.machine.MIPS;
import nachos.machine.Machine;
import nachos.machine.NachosThread;

class exchangeItem
{
    exchangeItem(String name)
    {
    }

}

public class Executor implements Runnable
{
    private String execName;
    private int child;
    private AddrSpace space;
    private int spaceId;
    private ArrayList<byte[]> argsList;

    public Executor(String filename, ArrayList<byte[]> args, int num, int parent)
    {
        String name = "ProgTest" + num + "(" + filename + ")";

        Debug.println('+', "starting ProgTest: " + name);

        execName = filename;
        this.argsList = args;

        space = new AddrSpace(num);
        spaceId = space.getSpaceId();
        UserThread t = new UserThread(name, this, space);
        Nachos.scheduler.readyToRun(t);

    }

    public int getspaceId()
    {
        return spaceId;
    }

    public void setChild(int x)
    {
        this.child = x;
    }

    public int getChild()
    {
        return child;
    }

    /**
     * Entry point for the thread created to run the user program. The specified
     * executable file is used to initialize the address space for the current
     * thread. Once this has been done, CPU.run() is called to transfer control
     * to user mode.
     */
    public void run()
    {
        OpenFile executable;

        if ((executable = Nachos.fileSystem.open(execName)) == null)
        {
            Debug.println('+', "Unable to open executable file: " + execName);
            Nachos.scheduler.finishThread();
            return;
        }

        AddrSpace space = ((UserThread) NachosThread.currentThread()).space;
        if (space.exec(executable) == -1)
        {
            Debug.println('+', "Unable to read executable file: " + execName);
            Nachos.scheduler.finishThread();
            return;
        }

        space.initRegisters(); // set the initial register values

        passArgs(space);

        space.restoreState(); // load page table register

        CPU.runUserCode(); // jump to the user program

        Debug.ASSERT(false);
        
        
    }

    private void passArgs(AddrSpace space)
    {
        int numPtrs = argsList.size();
        int sp = CPU.readRegister(MIPS.StackReg);
        int ptrs[] = new int[numPtrs];
        int masterPtr, stackTop;
        
        //calculate space
        sp -= sp % 4;
        
        int i = 0;
        for(byte[] bytes : argsList)
        {
            sp -= 4*wordAllign(bytes);
            ptrs[i] = sp;
            ++i;
        }
        
        sp -= 4;
        space.pushToMemory(sp, (int) 0);
        sp -= numPtrs * 4;
        
        masterPtr = sp;
        stackTop = masterPtr - 4;
        
        //add data
        for(int ptr : ptrs)
        {
            space.pushToMemory(sp, ptr);
            sp += 4;
        }
        
        sp += 4;
        for(int j = 0; j < argsList.size(); ++j)
        {
            sp = ptrs[j];
            for(int k = 0; k < argsList.get(j).length; ++k)
            {
                space.pushToMemory(sp, argsList.get(j)[k]);
                ++sp;
            }
            space.pushToMemory(sp, (byte) 0);
        }
        
        CPU.writeRegister(5, masterPtr);
        CPU.writeRegister(MIPS.StackReg, stackTop);
    }
    
    /**
     * Returns the number of words needed to
     * place an alligned string into memory
     * 
     * @param bytes The string
     * @return number of words
     */
    private int wordAllign(byte[] bytes)
    {
        int i = (bytes.length + 1) / 4; //+1 for null char
        if((bytes.length + 1)%4 != 0) ++i;
        return i;
    }

    /*
     * Dumps the contents of main memory to a file for debugging 
     * purposes
     */
    private static void write(String filename, byte[] mainmemory)
    {
        try
        {
            BufferedWriter outputWriter = null;
            outputWriter = new BufferedWriter(new FileWriter(filename));
    
            int j = 0;
    
            
            for (int i = 0; i < mainmemory.length; ++i)
            {
    
                outputWriter.write(String.format("0x%2s", Integer.toHexString(mainmemory[i] & 0xFF)).replace(' ', '0') + "    ");
                ++j;
                if (j == 4)
                {
                    outputWriter.newLine();
                    j = 0;
                }
    
            }
            outputWriter.flush();
            outputWriter.close();
            }
        catch(Exception e)
        {
            System.out.println("Could not write to file " + filename);
        }
    }
}
