/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Software;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import Hardware.Memory;

/**
 * Loads a file into memory
 * @author htrefftz
 */
public class Loader {
    /**
     * Where the Data Segment starts, in bytes
     */
    protected int dataSegmentStart;
    /**
     * Length of the Data Segment, in bytes
     */
    protected int dataSegmentLength;
    /**
     * Where the Text Segment starts, in bytes
     */
    protected int textSegmentStart;
    /**
     * Length of the Text Segment, in bytes
     */
    protected int textSegmentLength;
    /**
     * Start of Main1 (thread 1)
     */
    protected int main1Start;
    /**
     * Start of Main2 (thread 2)
     */
    protected int main2Start;
    /**
     * Start of Main3 (thread 3)
     */
    protected int main3Start;
    /**
     * Start of Main4 (thread 4)
     */
    protected int main4Start;
    /**
     * Start of Main5 (thread 5)
     */
    protected int main5Start;
    
    boolean DEBUG = false;
    
    ArrayList <Integer> executable;
    /**
     * Constructor
     * The ArrayList that will hold the program instructions is initialized.
     */
    public Loader() {
        executable = new ArrayList<>();
    }
    
    /**
     * Read the file into an ArrayList of integers
     * @param fileName name of the file to load
     */
    protected void readFile(String fileName) {
        int instruction;
        try (Scanner s = new Scanner(new File(fileName))) {
            dataSegmentStart = s.nextInt();
            dataSegmentLength = s.nextInt();
            textSegmentStart = s.nextInt();
            textSegmentLength = s.nextInt();
            main1Start = s.nextInt();
            main2Start = s.nextInt();
            main3Start = s.nextInt();
            main4Start = s.nextInt();
            main5Start = s.nextInt();
            
            while(s.hasNext()) {
                instruction = s.nextInt();
                executable.add(instruction);
                if(DEBUG) {
                    System.out.println(instruction);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found: " + fileName);
        }
    }
    
    /**
     * This method loads the executable file into memory
     * It starts loading it at Memory.ProgramOrigin
     * @param programOrigin where the program will be loaded into memory
     * @return A new pcb, with the default values to be put in the ready queue
     */
    public ProcessControlBlock load(int programOrigin, int threadNumber) {
        // First, check if the executable fits in memory
        // Todo: take into account a stack area
        int lengthInWords = (dataSegmentLength + textSegmentLength) / 4;
        if(programOrigin + lengthInWords > Memory.MEM_SIZE) {
            System.out.println("The program does not fit in memory");
            System.exit(1);
        }
        if(lengthInWords > Memory.TOTAL_PROGRAM_SIZE) {
            System.out.println("Program too big");
            System.exit(1);
        }
        if(DEBUG) {
            System.out.println("Memory size: " + Memory.mem.length);
            System.out.println("Program size: " + executable.size());
            System.out.println("In words: " + lengthInWords);
        }
        // Load the program into memory
        for(int memIndex = programOrigin, pgmIndex = 0; 
                pgmIndex < lengthInWords; 
                memIndex++, pgmIndex++) {
            Memory.mem[memIndex] = executable.get(pgmIndex);
            if(DEBUG) {
                System.out.println("memIndex, pgmIndex: " + memIndex + " " + pgmIndex);
            }
        }
        // Create a Process Control Block for the process
        int start = textSegmentStart;
        if (threadNumber == 1) {
            start = main1Start;
        } else if (threadNumber == 2) {
            start = main2Start;
        } else if (threadNumber == 3) {
            start = main3Start;
        } else if (threadNumber == 4) {
            start = main4Start;
        } else if (threadNumber == 5) {
            start = main5Start;
        }
        ProcessControlBlock pcb = new ProcessControlBlock(programOrigin, 
            textSegmentStart, dataSegmentStart, start);
        return pcb;
    }
     
    
    public static void main(String [] args) {
        Loader loader = new Loader();
        loader.readFile("file.out");
        loader.load(1000, 0);
    }
}
