// AddrSpace.java
//	Class to manage address spaces (executing user programs).
//
//	In order to run a user program, you must:
//
//	1. link with the -N -T 0 option 
//	2. run coff2noff to convert the object file to Nachos format
//		(Nachos object code format is essentially just a simpler
//		version of the UNIX executable object code format)
//	3. load the NOFF file into the Nachos file system
//		(if you haven't implemented the file system yet, you
//		don't need to do this last step)
//
// Copyright (c) 1992-1993 The Regents of the University of California.
// Copyright (c) 1998 Rice University.
// Copyright (c) 2003 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.userprog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;

import nachos.Debug;
import nachos.machine.CPU;
import nachos.machine.MIPS;
import nachos.machine.Machine;
import nachos.machine.NachosThread;
import nachos.machine.TranslationEntry;
import nachos.noff.NoffHeader;
import nachos.kernel.filesys.OpenFile;

import java.util.Arrays;

/**
 * This class manages "address spaces", which are the contexts in which user
 * programs execute. For now, an address space contains a "segment descriptor",
 * which describes the the virtual-to-physical address mapping that is to be
 * used when the user program is executing. As you implement more of Nachos, it
 * will probably be necessary to add other fields to this class to keep track of
 * things like open files, network connections, etc., in use by a user program.
 *
 * NOTE: Most of what is in currently this class assumes that just one user
 * program at a time will be executing. You will have to rewrite this code so
 * that it is suitable for multiprogramming.
 * 
 * @author Thomas Anderson (UC Berkeley), original C++ version
 * @author Peter Druschel (Rice University), Java translation
 * @author Eugene W. Stark (Stony Brook University)
 */
public class AddrSpace
{

    /** Page table that describes a virtual-to-physical address mapping. */
    private TranslationEntry pageTable[];

    private int[] savedRegisters = new int[MIPS.NumTotalRegs];

    /** Default size of the user stack area -- increase this as necessary! */
    private static final int UserStackSize = 1024;
    private int argc;
    private int child;
    private int spaceId;

    /**
     * Create a new address space.
     */
    public AddrSpace()
    {
        spaceId = MemAlloc.getInstance().getSpaceId();
    }

    public int getSpaceId()
    {
        return spaceId;
    }

    public AddrSpace(int argc)
    {
        spaceId = MemAlloc.getInstance().getSpaceId();
        this.argc = argc;
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
     * Load the program from a file "executable", and set everything up so that
     * we can start executing user instructions.
     *
     * Assumes that the object code file is in NOFF format.
     *
     * First, set up the translation from program memory to physical memory. For
     * now, this is really simple (1:1), since we are only uniprogramming.
     *
     * @param executable
     *            The file containing the object code to load into memory
     * @return -1 if an error occurs while reading the object file, otherwise 0.
     */
    public int exec(OpenFile executable)
    {
        NoffHeader noffH;
        long size;
        long nonStackPages;

        if ((noffH = NoffHeader.readHeader(executable)) == null)
            return (-1);

        // how big is address space?
        size = roundToPage(noffH.code.size)
                + roundToPage(noffH.initData.size + noffH.uninitData.size)
                + UserStackSize; // we need to increase the size
        nonStackPages = roundToPage(noffH.code.size)
                + roundToPage(noffH.initData.size + noffH.uninitData.size);
        // to leave room for the stack
        int numPages = (int) (size / Machine.PageSize);

        nonStackPages = (int)(nonStackPages/Machine.PageSize);
        
        Debug.ASSERT((numPages <= Machine.NumPhysPages),// check we're not
                                                        // trying
                "AddrSpace constructor: Not enough memory!");
        // to run anything too big --
        // at least until we have
        // virtual memory

        Debug.println('a', "Initializing address space, numPages=" + numPages
                + ", size=" + size);

        // first, set up the translation
        pageTable = new TranslationEntry[numPages];

        for (int i = 0; i < numPages; i++)
        {
            pageTable[i] = new TranslationEntry();
            pageTable[i].virtualPage = i; // for now, virtual page# = phys
                                          // page#
//            pageTable[i].physicalPage = MemAlloc.getInstance().allocatePage();

            /**
             * This valid bit needs to change if i is inbetween code.size and
             * initData.size keep valid else invalid
             */
            pageTable[i].use = false;
            pageTable[i].dirty = false;
            pageTable[i].readOnly = false;
            if (i < nonStackPages)
            {
                pageTable[i].physicalPage = MemAlloc.getInstance().allocatePage();
                pageTable[i].valid = true;
            } else
            {
                pageTable[i].valid = false;
                break;
            }
//            pageTable[i].use = false;
//            pageTable[i].dirty = false;
//            pageTable[i].readOnly = false; // if code and data segments live
                                           // on
            // separate pages, we could set code
            // pages to be read-only

        }

        // Zero out the entire address space, to zero the uninitialized data
        // segment and the stack segment.
        for (int i = 0; i < numPages; i++)
        {
            int f = (pageTable[i].physicalPage + 1) * Machine.PageSize;

            for (int j = pageTable[i].physicalPage * Machine.PageSize; j < f; j++)
            {
                Machine.mainMemory[j] = (byte) 0;
            }
        }

        // then, copy in the code and data segments into memory
        int remainders = (int) (noffH.code.size % Machine.PageSize);
        if (noffH.code.size > 0)
        {
            Debug.println('a', "Initializing code segment, at "
                    + noffH.code.virtualAddr + ", size " + noffH.code.size);

            // seek once
            executable.seek(noffH.code.inFileAddr);// this is correct keep it

            // I dont think we are getting all of the program, i think we are
            // getting 99% of it
            for (int i = 0; i < numPages; i++)
                executable.read(Machine.mainMemory,
                        (pageTable[i].physicalPage * Machine.PageSize),
                        Machine.PageSize);

        }
        // usermanual press release review
        if (noffH.initData.size > 0)
        {
            Debug.println('a', "Initializing data segment, at "
                    + noffH.initData.virtualAddr + ", size "
                    + noffH.initData.size);

            executable.seek(noffH.initData.inFileAddr);
            for (int i = 0; i < numPages; i++)
                executable.read(Machine.mainMemory,
                        (pageTable[i].physicalPage * Machine.PageSize),
                        Machine.PageSize);
        }

        return (0);
    }

    public void getNewPageException(){
        
    }
    
    /**
     * Loads this AddrSpace into the kernel
     */
    public byte[] copyIntoKernel(int ptr, int length)
    {
        byte[] copy = new byte[length];

        int start = getPhysicalAddress(ptr);
        // TODO: fix for non-contiguous pages
        System.arraycopy(Machine.mainMemory, start, copy, 0, length);
        return copy;
    }

    public byte[] getCString(int ptr)
    {
        int start = getPhysicalAddress(ptr);
        int locationInMemory = start;
        int length = 0;
        while (Machine.mainMemory[locationInMemory] != 0)
        {
            locationInMemory++;
            length++;
        }
        byte[] copy = new byte[length];
        System.arraycopy(Machine.mainMemory, start, copy, 0, length);

        return copy;
    }

    public ArrayList<byte[]> getArgsList(int ptr, int wordSize)
    {
        ArrayList<byte[]> ba = new ArrayList<>();
        int ptrin;
        while (Machine.mainMemory[ptr] != 0)
        {
            ptrin = (int) Machine.mainMemory[ptr + 3] & 0xFF;
            ptrin = ptrin << 8;
            ptrin |= (int) Machine.mainMemory[ptr + 2] & 0xFF;
            ptrin = ptrin << 8;
            ptrin |= (int) Machine.mainMemory[ptr + 1] & 0xFF;
            ptrin = ptrin << 8;
            ptrin |= (int) Machine.mainMemory[ptr] & 0xFF;

            ba.add(getCString(ptrin));
            ptr += wordSize;
        }
        return ba;
    }

    /**
     * Initialize the user-level register set to values appropriate for starting
     * execution of a user program loaded in this address space.
     *
     * We write these directly into the "machine" registers, so that we can
     * immediately jump to user code.
     */
    public void initRegisters()
    {
        int i;

        for (i = 0; i < MIPS.NumTotalRegs; i++)
            CPU.writeRegister(i, 0);

        // Initial program counter -- must be location of "Start"
        CPU.writeRegister(MIPS.PCReg, 0);

        // Need to also tell MIPS where next instruction is, because
        // of branch delay possibility
        CPU.writeRegister(MIPS.NextPCReg, 4);

        // Set the stack register to the end of the segment.
        // NOTE: Nachos traditionally subtracted 16 bytes here,
        // but that turns out to be to accomodate compiler convention that
        // assumes space in the current frame to save four argument registers.
        // That code rightly belongs in start.s and has been moved there.
        int sp = pageTable.length * Machine.PageSize;
        CPU.writeRegister(MIPS.StackReg, sp);
        if (argc != 0)
            CPU.writeRegister(4, argc);

        Debug.println('a', "Initializing stack register to " + sp);
    }

    /**
     * On a context switch, save any machine state, specific to this address
     * space, that needs saving.
     *
     * For now, nothing!
     */
    public void saveState()
    {
        for (int i = 0; i < MIPS.NumTotalRegs; ++i)
            savedRegisters[i] = CPU.readRegister(i);
    }

    /**
     * On a context switch, restore any machine state specific to this address
     * space.
     *
     * For now, just tell the machine where to find the page table.
     */
    public void restoreState()
    {
        CPU.setPageTable(pageTable);
    }

    /**
     * Utility method for rounding up to a multiple of CPU.PageSize;
     */
    private long roundToPage(long size)
    {
        return (Machine.PageSize * ((size + (Machine.PageSize - 1)) / Machine.PageSize));
    }

    public int exit(int i)
    {
        MemAlloc.getInstance().deAllocatePages(NachosThread.currentThread());
        return i;
    }

    public void pushToMemory(int virtAddr, byte b)
    {
        int physAddr = getPhysicalAddress(virtAddr);
        Machine.mainMemory[physAddr] = b;
        // System.out.println();
    }

    public void pushToMemory(int virtAddr, byte b[])
    {
        // int physAddr = getPhysicalAddress(virtAddr);
        for (int i = 0; i < b.length; i++)
        {
            pushToMemory(virtAddr + i, b[i]);
        }

    }

    /**
     * Pushes a 1 word quantity to memory. Assumes that programer has allocated
     * proper space above the provided virtual address
     * 
     * @param virtAddr
     *            -address to begin pushing data
     * @param i
     *            word to push.
     */
    public void pushToMemory(int virtAddr, int i)
    {
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(i).array();

        for (byte b : bytes)
        {
            pushToMemory(virtAddr, b);
            // System.out.println();
            ++virtAddr;
        }
    }

    private int getPhysicalAddress(int virtAddr)
    {
        int pageNumber = virtAddr / Machine.PageSize;
        int pageOffset = virtAddr % Machine.PageSize;

        return pageTable[pageNumber].physicalPage * Machine.PageSize
                + pageOffset;
    }
}
