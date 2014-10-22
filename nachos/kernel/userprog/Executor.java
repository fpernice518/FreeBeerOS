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

        passArguments(space);

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

    // private void passArgs(AddrSpace space)
    // {
    // int index = CPU.readRegister(MIPS.StackReg) - 1;
    // ArrayList<Integer> ptrs = new ArrayList<Integer>();
    //
    // //push strings onto stack
    // for(int i = 0; i < args.length; ++i)
    // {
    // int j = 0;    
    // while(j < args[i].length)
    // {
    // space.pushToMemory(index, args[i][j]);
    // System.out.println("Data Addr= " + index + " Data = " +
    // (char)args[i][j]);
    // --index;
    // ++j;
    // }
    // space.pushToMemory(index, (byte) 0); //push null character
    // System.out.println("Data Addr= " + index + " Data = " + 0);
    // --index;
    // }
    //
    // //index to new aligned spot in memory
    // while((index % 4) != 0)
    // --index;
    //
    //
    // int masterPtr = index;
    // //push child pointers to stack
    // for(int i = 0; i < ptrs.size(); ++i)
    // {
    // byte[] b = ByteBuffer.allocate(4).putInt(ptrs.get(i)).array();
    //
    // System.out.println("Ptr Addr= " + index + " Ptr Value = " + ptrs.get(i));
    // for(int j = 0; j < b.length; ++j)
    // {
    // space.pushToMemory(index, b[j]);
    // System.out.println("Byte = " + j + " Value = " + Integer.toHexString(b[j]
    // & 0xFF));
    // --index;
    // }
    // }
    //
    // System.out.println("Master Ptr Value = " + masterPtr);
    //
    // CPU.writeRegister(4, args.length);
    // CPU.writeRegister(5, masterPtr);
    // CPU.writeRegister(MIPS.StackReg, index);
    //
    // try
    // {
    // Executor.write("Debug Me", Machine.mainMemory);
    // } catch (IOException e)
    // {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

    // private void newPassArgs(AddrSpace space)
    // {
    // int numChars = 0;
    // int numPointers = argsList.size();
    // int sp = CPU.readRegister(MIPS.StackReg);
    // int spCopy;
    // int[] ptrs = new int[numPointers];
    //
    // for(int i = 0; i < argsList.size(); ++i)
    // numChars += argsList.get(i).length;
    // numChars += argsList.size();
    //
    // System.out.println("^^^^^^^^^" + sp);
    // sp -= numChars;
    // spCopy = sp;
    //
    // for(int i = argsList.size() - 1; i >= 0; --i)
    // {
    // space.pushToMemory(sp, (byte) 0);
    // ++sp;
    //
    // for(int j = argsList.get(i).length -1; j >= 0 ; --j)
    // {
    // space.pushToMemory(sp, argsList.get(i)[j]);
    // ++sp;
    // }
    //
    // ptrs[i] = sp-1;
    // }
    //
    // sp = spCopy;
    // sp -= (numPointers * 4);
    //
    //
    // while(sp % 4 != 0) --sp;
    // spCopy = sp;
    //
    // int masterPtr = sp;
    //
    // for(int i = ptrs.length - 1; i >= 0; --i)
    // {
    // byte[] b =
    // ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ptrs[i]).array();
    //
    // for(int j = 0; j < b.length; ++j)
    // {
    // space.pushToMemory(sp, b[j]);
    // ++sp;
    // }
    // }
    //
    // CPU.writeRegister(5, masterPtr);
    // CPU.writeRegister(MIPS.StackReg, spCopy);
    //
    // int k = 0xDEADBEEF;
    // System.out.println(k);
    //
    // // linearArray = new byte[numChars];
    // //
    // // int k = 0;
    // // for(int i = argsList.size()-1; i >= 0 ; --i)
    // // {
    // // for(int j = argsList.get(i).length - 1; j >= 0; --j)
    // // {
    // // linearArray[k] = argsList.get(i)[j];
    // // ++k;
    // // }
    // // linearArray[k] = 0;
    // // ++k;
    // // }
    //
    // }

    /*
     * Dumps the contents of main memory to a file
     */
    private static void write(String filename, byte[] mainmemory)
            throws IOException
    {
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(filename));

        int j = 0;

        for (int i = mainmemory.length - 1; i >= 0; i--)
        {

            outputWriter.write(Integer.toHexString(mainmemory[i] & 0xFF)
                    + "    ");
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
