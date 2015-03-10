/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Hardware;

import Software.ProgramStatusWord;
import Software.ProcessControlBlock;

/**
 * Emulator for a DLX processor
 * Loosely based on the processor defined in Compiler Construction
 * (by Niklaus Wirth): p47
 * @author htrefftz
 */
public class Processor {
    /*
    * Registers 0 to 15
    * r14: return address used by BSR instruction
    * r15: PC (program counter)
    */
    public int [] r = new int[16];
    /**
     * Relocation register or Base Register. 
     * Points at the start (in bytes) of the data segment. 
     * Used for absolute-address instructions, such as LDW and STW
     */
    protected int br;
    /*
    * Instruction register, contains the instruction being decodified
    */
    protected int ir;
    /**
     * Address of next instruction
     */
    protected int nxt;
    /**
     * zero flag in the status register
     */
    protected boolean z;
    /**
     * negative flag in the status register
     */
    protected boolean n; 
    /**
     * Process Control Block of the program that is running.
     * Data is saved into this data structure when the process
     * is moved from running to another status
     */
    protected ProcessControlBlock pcb;
    /**
     * flag indicating that the program has ended
     */
    public boolean finished;
    /**
     * flag indicating that the program is issuing an interruption
     */
    public boolean interruptFlag;
    /**
     * Number of interruption to execute
     * 1: open file
     * 2: close file
     * 3: read block from file
     * 4: write block to file
     * 5: formatted output to Standard Output
     */
    public int interruptNumber;
    
    public static final int OPEN_FILE = 1;
    public static final int CLOSE_FILE = 2;
    public static final int READ_BLOCK = 3;
    public static final int WRITE_BLOCK = 4;
    public static final int FORMATTED_OUTPUT = 5;
    /**
     * Instruction constants
     * Notes:
     *  r[15] is the program counter
     *  r[14] is the return address
     */
    // Type 00 instruction
    public static final int MOV = 0;
    public static final int MOVN = 1;
    public static final int ADD = 2;
    public static final int SUB = 3;
    public static final int CMP = 7;
    
    public static final String MOV_STRING = "mov";
    public static final String MOVN_STRING = "movn";
    public static final String ADD_STRING = "add";
    public static final String SUB_STRING = "sub";
    public static final String CMP_STRING = "cmp";
    // type 01 instruction
    public static final int MOVI = 16;
    public static final int MVNI = 17;
    public static final int ADDI = 18;
    public static final int SUBI = 19;
    public static final int CMPI = 23;
    
    public static final String MOVI_STRING = "movi";
    public static final String MVNI_STRING = "mvni";
    public static final String ADDI_STRING = "addi";
    public static final String SUBI_STRING = "subi";
    public static final String CMPI_STRING = "cmpi";
    
    // type 10 instructions
    public static final int LDW = 32;
    public static final int POP = 34;
    public static final int STW = 36;
    public static final int PSH = 38;
    public static final int TSL = 39;
    
    public static final String LDW_STRING = "ldw";
    public static final String POP_STRING = "pop";
    public static final String STW_STRING = "stw";
    public static final String PSH_STRING = "psh";
    public static final String TSL_STRING = "tsl";
    
    // type 11 instructions
    public static final int BEQ = 48;
    public static final int BNE = 49;
    public static final int BLT = 50;
    public static final int BGE = 51;
    public static final int BLE = 52;
    public static final int BGT = 53;
    public static final int BR = 56;
    public static final int BSR = 57;
    public static final int RET = 58;
    public static final int TRAP = 59;
    
    public static final String BEQ_STRING = "beq";
    public static final String BNE_STRING = "bne";
    public static final String BLT_STRING = "blt";
    public static final String BGE_STRING = "bge";
    public static final String BLE_STRING = "ble";
    public static final String BGT_STRING = "bgt";
    public static final String BR_STRING = "br";
    public static final String BSR_STRING = "bsr";
    public static final String RET_STRING = "ret";
    public static final String TRAP_STRING = "trap";
    
    // Shift constants
    public static final int b26 = 0x4000000;
    public static final int b25 = 0x2000000;
    public static final int b22 = 0x400000;
    public static final int b18 = 0x40000;
    public static final int b17 = 0x20000;
    public static final int b8  = 0x100;
    
    private final boolean DEBUG = false;
    
    /**
     * This method simulates the execution of ONE instruction
     * Prerequisite: All the following structures have to contain the
     * appropriate values:
     *  . Registers
     *  . PSW
     *  . Relocation Register
     */
    public void executeInstruction() {
        // next instruction to be executed
        nxt = r[15] + 4;
        // instruction to be decodified
        ir = Memory.mem[r[15] / 4];
        if (DEBUG) {
            System.out.println("IR: " + ir);
        }
        // determine the op code
        // there are 4 types of instructions: 00, 01, 10 and 11
        // NOTE the logical shift (>>>) instead of the arithmetic shift
        // (>>), because the first bit in 1 should not be interpreted
        // as a minus sign.
        int opCode = ir >>> 26;
        // Determine values for a, b and c
        // (usually register numbers)
        // int a = (ir / b22) % 0x10;
        // int b = (ir / b18) % 0x10;
        // We have extracted the opCode (first 6 bits).
        // Now make it 0, in order to avoid
        ir = ir << 6;
        ir = ir >>> 6;
        int a = (ir >>> 22) % 0x10;
        int b = (ir >>> 18) % 0x10;
        int c;
        //
        if (opCode < 16) { // instruction type 00 
            c = r[ir % 0x10];
        } else if (opCode < 48) { // instruction type 01 or 10
            c = ir % b18;
            if (c >= b17) {     // sign extension
                c = c - b18;
            }
        } else { // instruction type 11
            c = ir % b26;
            if (c >= b25) {
                c = c - b26;
            }
        }

        switch (opCode) {
            case MOV:
            case MOVI:
                r[a] = c >> b;
                break;
            case MOVN:
            case MVNI:
                r[a] = -(c >> b);
                break;
            case ADD:
            case ADDI:
                r[a] = r[b] + c;
                break;
            case SUB:
            case SUBI:
                r[a] = r[b] - c;
                break;
            case CMP:
            case CMPI:
                z = (r[b] == c);
                n = (r[b] < c);
                break;
            case LDW:
                r[a] = Memory.mem[(r[b] + c + br) / 4];
                if (DEBUG) {
                    System.out.println("ldw: " + r[a]);
                }
                break;
            case POP:
                // b is the stack pointer, c is the size of the stack entry
                if (a != 0) {
                    r[a] = Memory.mem[r[b] / 4];
                }
                r[b] += c;
                break;
            case STW:
                Memory.mem[(r[b] + c + br) / 4] = r[a];
                if (DEBUG) {
                    System.out.println("stw: " + Memory.mem[(r[b] + c + br) / 4]);
                    System.out.println("Store address: " + (r[b] + c + br) / 4);
                    Memory.dump(br, br + 6);
                }
                break;
            case PSH:
                // b is the stack pointer, c is the size of the stack entry
                r[b] -= c;
                Memory.mem[r[b] / 4] = r[a];
                break;
            // Atomically read the content of the memory variable (the lock)
            // into the register and then store a non-zero value into the lock
            case TSL:
                r[a] = Memory.mem[(r[b] + c + br) / 4];
                Memory.mem[(r[b] + c + br) / 4] = 1;
                if (DEBUG) {
                    System.out.println("tsl: " + Memory.mem[(r[b] + c + br) / 4]);
                    System.out.println("Store address: " + (r[b] + c + br) / 4);
                }
                break;
            case BEQ:
                if (z) {
                    nxt = r[15] + c * 4;
                }
                break;
            case BNE:
                if (!z) {
                    nxt = r[15] + c * 4;
                }
                break;
            case BLT:
                if (n) {
                    nxt = r[15] + c * 4;
                }
                break;
            case BGE:
                if (!n) {
                    nxt = r[15] + c * 4;
                }
                break;
            case BLE:
                if (z || n) {
                    nxt = r[15] + c * 4;
                }
                break;
            case BGT:
                if (!z && !n) {
                    nxt = r[15] + c * 4;
                }
                break;
            case BR:
                nxt = r[15] + c * 4;
                break;
            case BSR:
                nxt = r[15] + c * 4;
                r[14] = r[15] + 4;
                break;
            case RET:
                //nxt = r[c % 0x10];
                nxt = r[14];            // 
                if (nxt == 0) {
                    finished = true;
                }
                break;
            case TRAP:
                // Code for the trap goes here
                interruptFlag = true;
                interruptNumber = c;
                if(DEBUG) {
                    System.out.println("Trap: " + interruptNumber);
                }
                break;
        }
        r[15] = nxt;
    }
    
    /**
     * This method loads the context of the program that will be run
     * i.e., when it is moved to Running state
     * Registers, PSW, Relocation Register
     * @param savedPcb PCB that will be loaded into memory
     */
    public void loadContext(ProcessControlBlock savedPcb) {
        z = savedPcb.psw.z;
        n = savedPcb.psw.n;
        // int mode = savedPcb.psw.mode; // currently not used
        // int status = savedPcb.psw.status; // currently not used
        System.arraycopy(savedPcb.registers, 0, r, 0, 16);
        br = savedPcb.relocationRegister;
    }
    
    /**
     * This method saves the context of the program that will not
     * be running, i.e. it will be moved to the ready or to the blocked
     * queues.
     * Registers, PSW, Relocation Register
     * In this version, the information is stored in main memory
     * @param pcbSaveArea Process Control to store the data
     */
    public void saveContext(ProcessControlBlock pcbSaveArea) {
        ProgramStatusWord psw = new ProgramStatusWord(z, n, 
                ProgramStatusWord.RUNNING, ProgramStatusWord.USER);
        pcbSaveArea.psw.z = z;
        pcbSaveArea.psw.n = n;
        pcbSaveArea.psw.mode = ProgramStatusWord.USER;
        // Currently running, when stored in a different queue,
        // this is modified
        pcbSaveArea.psw.status = ProgramStatusWord.RUNNING;
        System.arraycopy(r, 0, pcbSaveArea.registers, 0, 16);
        pcbSaveArea.relocationRegister = br;
    }


}

