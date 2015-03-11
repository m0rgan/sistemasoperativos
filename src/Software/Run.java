/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Software;

import Hardware.Memory;
import Hardware.Processor;

/**
 *
 * @author htrefftz
 */
public class Run {

    private static final boolean DEBUG = true;
    
    
    public static void main(String [] args) {
        // Processor
        Processor processor = new Processor();
        // Scheduler
        RRScheduler scheduler = new RRScheduler(processor);
        //LotteryScheduler scheduler = new LotteryScheduler(processor);
        
        // Where the programs will be loaded into memory
        int program1Origin = 50;        // in words
        //int program2Origin = 200;       // in words
        int program2Origin = 50;        // same in order to share data segment
        int program3Origin = 50;
        int program4Origin = 50;
        int program5Origin = 50;
        
        // Load first program
        Loader loader = new Loader();   // create a loader
        loader.readFile("file.out");    // read the executable file
        // load the executable into memory and retrieve a blank PCB
        ProcessControlBlock pcb1 = loader.load(program1Origin, 1);
        // Add the program's PCB to the Ready queue
        scheduler.addToReadyQueue(pcb1);
        int dataStart1 = loader.dataSegmentStart / 4;
        int dataLength1 = loader.dataSegmentLength / 4;
        
        // load second program
        
        loader.readFile("file.out");
        ProcessControlBlock pcb2 = loader.load(program2Origin, 2);
        scheduler.addToReadyQueue(pcb2);
        int dataStart2 = loader.dataSegmentStart / 4;
        int dataLength2 = loader.dataSegmentLength / 4;
        
        loader.readFile("file.out");
        ProcessControlBlock pcb3 = loader.load(program3Origin, 3);
        scheduler.addToReadyQueue(pcb3);
        int dataStart3 = loader.dataSegmentStart / 4;
        int dataLength3 = loader.dataSegmentLength / 4;
        
        loader.readFile("file.out");
        ProcessControlBlock pcb4 = loader.load(program4Origin, 4);
        scheduler.addToReadyQueue(pcb4);
        int dataStart4 = loader.dataSegmentStart / 4;
        int dataLength4 = loader.dataSegmentLength / 4;
        
        loader.readFile("file.out");
        ProcessControlBlock pcb5 = loader.load(program5Origin, 5);
        scheduler.addToReadyQueue(pcb5);
        int dataStart5 = loader.dataSegmentStart / 4;
        int dataLength5 = loader.dataSegmentLength / 4;

        
        //scheduler.deliverTickets();
        // dump the memory
        /*
        System.out.println("Program1 before: ");
        Memory.dump(dataStart1 + program1Origin,
                dataStart1 + program1Origin + dataLength1);
        */
        
        // run
        scheduler.run();
        
        // dump the memory
        /*
        System.out.println("Program1 after: ");
        Memory.dump(dataStart1 + program1Origin,
                dataStart1 + program1Origin + dataLength1);
        */
        /*
        System.out.println("Program2 after: ");
        Memory.dump(dataStart2 + program2Origin,
                dataStart2 + program2Origin + dataLength2);
        */
        

    }
    
    
}
