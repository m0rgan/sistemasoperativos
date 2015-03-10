/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Hardware;

/**
 *
 * @author htrefftz
 */
public class Memory {
    
    /**
     * Memory size (in words)
     */
    public static int MEM_SIZE = 4096;
            
    /**
     * Memory is simulated as an array of integers.
     * Each integer is 4 bytes.
     */
    public static     int [] mem = new int[MEM_SIZE];
    /**
     * Total memory allocated to data, text and stack, in words
     */
    public static final int TOTAL_PROGRAM_SIZE = 100;
    /**
     * Prints the content of memory, between two provided addresses (in bytes)
     * @param start start of dump area (in bytes)
     * @param end end of dump area (in bytes)
     */
    public static void dump(int start, int end) {
        int index;
        System.out.println("Memory dump:");
        for(int i = start; i < end; i++) {
            System.out.println(i*4 + " " + mem[i]);
        }
    }
    
}
