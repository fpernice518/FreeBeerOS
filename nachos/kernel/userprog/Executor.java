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
    private String parentname;

    exchangeItem(String name)
    {
        this.parentname = name;
    }

}

public class Executor implements Runnable
{

    /** The name of the program to execute. */
    private String execName;
    private byte[][] args;
    private ArrayList<byte[]> argList;
    // private Exchanger exchanger[];
    private int child;
    private AddrSpace space;

    // private Exchanger exchanger[];
    private int spaceId;
    private ArrayList<byte[]> argsList;

    public Executor(String filename, byte[][] args, int num, int parent)
    {
        String name = "ProgTest" + num + "(" + filename + ")";

        Debug.println('+', "starting ProgTest: " + name);

        execName = filename;
        this.args = args;
        // exchanger = new Exchanger();

        space = new AddrSpace(num);
        spaceId = space.getSpaceId();

        UserThread t = new UserThread(name, this, space);
        Nachos.scheduler.readyToRun(t);
    }

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

        // Debug.print('2', "we went here");
        space.initRegisters(); // set the initial register values

        // push first argument to stack
        // int argc = args.length;

        passArggs(space);
        try
        {
            this.write("debugMe", Machine.mainMemory);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        space.restoreState(); // load page table register

        CPU.runUserCode(); // jump to the user program

        Debug.ASSERT(false);
        
        
    }

    private void passArguments(AddrSpace space)
    {

        int ptr = 0;
        int byteBuff = 0;
        ArrayList<Integer> ptrs = new ArrayList<Integer>();
        boolean gotPtr = false;

        for (int i = 0; i < args.length; ++i)
        {
            int numWords = args[i].length / 4;
            int numBytesLeft = args[i].length % 4;

            for (int j = 0; j < numWords; ++j)
            {
                byteBuff = args[i][j];
                byteBuff = byteBuff << 8;

                byteBuff |= args[i][j + 1];
                byteBuff = byteBuff << 8;

                byteBuff |= args[i][j + 2];
                byteBuff = byteBuff << 8;

                byteBuff |= args[i][j + 3];

                if (gotPtr == false)
                {
                    ptr = space.pushToStack(byteBuff);
                    gotPtr = true;
                } else
                    space.pushToStack(byteBuff);
            }

            if (numBytesLeft > 0)
            {
                for (int j = 0; j < 3; ++j)
                {
                    if (numBytesLeft == 0)
                    {
                        byteBuff |= 0;
                        byteBuff = byteBuff << 8;
                    } else
                    {
                        byteBuff |= args[i][j];
                        byteBuff = byteBuff << 8;
                    }

                    --numBytesLeft;
                }

                if (gotPtr == false)
                {
                    ptr = space.pushToStack(byteBuff);
                    gotPtr = true;
                } else
                    space.pushToStack(byteBuff);
            }
            ptrs.add(ptr);
            gotPtr = false;
        }

        gotPtr = false;
        // Collections.reverse(ptrs);
        for (int i = 0; i < ptrs.size(); ++i)
        {
            if (false == gotPtr)
            {
                ptr = space.pushToStack(ptrs.get(i));
                gotPtr = true;
            } else
                space.pushToStack(ptrs.get(i));
        }
        space.pushToStack(0);
        CPU.writeRegister(5, ptr);
    }

    private void passArgs(AddrSpace space)
    {
        int numPtrs = argsList.size();
        int numChars = 0;
        int sp = CPU.readRegister(MIPS.StackReg);
        int[] ptrList = new int[numPtrs];

        // guarantee word alignment
        // if (sp % 4 != 0)
        // {
        // sp = sp - (sp%4);
        // }

        for (byte[] element : argsList)
        {
            numChars += element.length + 1; // +1 for null
            /*
             * check if needs padding // while(sp % 4!=0){ // ++numChars; //
             * --sp; // }
             * 
             * // for(int i = 0 ; i < element.length+1; i++) // { // --sp; // }
             */

        }
        ArrayList<byte[]> buffer = new ArrayList<>();
        ArrayList<Integer> pointLocation = new ArrayList<>();
        int count = 0;
        byte[] array;
        for (byte[] element : argsList)
        {
            if (element.length % 4 != 0)
            {
                // append until you cant then append nulls
                int amountThatDoesFit = element.length / 4;
                int index = 0;
                pointLocation.add(new Integer(count));
                for (int i = 0; i < amountThatDoesFit; i++)
                {
                    array = new byte[4];
                    for (int j = 0; j < 4; j++)
                    {
                       array[j]= element[index];
                       index++;
                        
                    }
                    count ++;
                    buffer.add(array);
                }
                
                // append the rest
                array = new byte[4];
                for(int i = 0 ; i<4; i++){
                    if(index<element.length){
                        array[i]= element[index];
                        index++;
                    }
                    else{
                        array[i]= (char)0;
                    }
                }
                count++;
                buffer.add(array);
            } else
            {
                int amountThatDoesFit = element.length / 4;
                int index = 0;
                pointLocation.add(new Integer(count));
                for (int i = 0; i < amountThatDoesFit; i++)
                {
                    array = new byte[4];
                    for (int j = 0; j < 4; j++)
                    {
                       array[j]= element[index];
                       index++;
                        
                    }
                    count++;
                    buffer.add(array);
                }
                array = new byte[4];
                for (int i = 0; i < 4; i++)
                {
                 array[i]= (char)0;   
                }
                count++;
                buffer.add(array);
                // append a row of nulls
            }

        }
        System.out.println();
        // if(sp%4 != 0){
        // sp= sp-numChars;
        // }

    }

    
    private void passArggs(AddrSpace space)
    {
        int numPtrs = argsList.size();
        int sp = CPU.readRegister(MIPS.StackReg);
        int ptrs[] = new int[numPtrs];
        int masterPtr, stackTop;
        
        while(sp % 4 != 0) 
            --sp;
        
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
     * Dumps the contents of main memory to a file
     */
    private static void write(String filename, byte[] mainmemory)
            throws IOException
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
}
