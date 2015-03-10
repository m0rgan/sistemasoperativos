/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Software;

import Hardware.Memory;

/**
 *
 * @author htrefftz
 */
public class ProcessControlBlock {
    /**
     * Program status word:
     */
    public ProgramStatusWord psw;
    /**
     * Registers 0 to 15
     */
    public int [] registers;
    /**
     * Where the program is loaded in memory
     */
    public int relocationRegister;


    /**
     * Create a new PCB knowing where the program is loaded into memory,
     * as well as the size of the data and text segments
     * @param programOrigin Where the program will be loaded into memory,
     *   in words
     * @param textSegmentStart Start of the text segment, in bytes
     * @param dataSegmentStart Start of the data segment, in bytes
     * @param start position in the executable file where the thread starts
     *   executing
     */
    public ProcessControlBlock(int programOrigin, int textSegmentStart, 
            int dataSegmentStart, int start) {
        registers = new int[16];
        // Register 0 is always 0.
        registers[0] = 0;
        // Stack Pointer and Frame Pointer initialization
        registers[13] = programOrigin * 4 + Memory.TOTAL_PROGRAM_SIZE * 4;
        registers[12] = registers[13];
        // return address
        // if it is 0, it means that the main program has ended.
        registers[14] = 0;
        // Initialize the program counter
        // registers[15] = textSegmentStart + programOrigin * 4;
        registers[15] = start + programOrigin * 4;
        // The relocation register will point at the data segment start
        relocationRegister = dataSegmentStart + programOrigin * 4;
        // Comparison flags
        boolean n = false;
        boolean z = false;
        psw = new ProgramStatusWord(z, n, 
            ProgramStatusWord.READY, ProgramStatusWord.USER);
    }
    

    
}
