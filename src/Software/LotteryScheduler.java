/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Software;

import Hardware.Processor;
import Hardware.Memory;
import Hardware.StandardOutput;
import java.util.HashMap;

import java.util.LinkedList;
import java.util.Map;
/**
 *
 * @author SEBAS
 */
public class LotteryScheduler {
    private static final boolean DEBUG = false;
    /**
     * Processes that are ready to be chosen to run
     */
    private final LinkedList<ProcessControlBlock> readyQueue;
    /**
     * Processes that are ready to be chosen to run
     */
    private final LinkedList<ProcessControlBlock> blockedQueue;
    /**
     * Process Control Block of the program that is currently running
     */
    private ProcessControlBlock runningProcess;
    /**
     * Processes that are ready to be chosen to run
     */
    private final LinkedList<ProcessControlBlock> finishedQueue;
    /**
     * Reference to the processor running the programs
     */
    Processor processor;
    /**
     * Number of instructions to execute before switching context
     */
    private final int quantum = 20;
    /**
     * Global time variable
     */
    public int time; 
    public double turnaroundTime = 0;
    public double contextSwitchTime = 0;
    
    public int ticket;
    
    Map<Integer, ProcessControlBlock> map;
    
    /**
     * Constructor
     * @param processor that will run the programs
     */
    public LotteryScheduler(Processor processor) {
        readyQueue = new LinkedList<>();
        finishedQueue = new LinkedList<>();
        blockedQueue = new LinkedList<>();
        this.processor = processor;
        map = new HashMap<>();
    }
    
    /**
     * Take the pcb and store it into the ready queue
     * @param pcb Process Control Block to be moved
     */   
    public void addToBlockedQueue(ProcessControlBlock pcb) {
        pcb.psw.status = ProgramStatusWord.BLOCKED;
        blockedQueue.add(pcb);
    }
    
    /**
     * Take the pcb and store it into the ready queue
     * @param pcb Process Control Block to be moved
     */
    public void addToReadyQueue(ProcessControlBlock pcb) {
        if (pcb.psw.status == ProgramStatusWord.BLOCKED) {
            blockedQueue.remove();
        }
        pcb.psw.status = ProgramStatusWord.READY;
        readyQueue.add(pcb);
    }
    
    /**
     * Take the pcb and store it into the finished queue
     * @param pcb Process Control Block to be moved
     */
    public void addToFinishedQueue(ProcessControlBlock pcb) {
        pcb.psw.status = ProgramStatusWord.FINISHED;
        finishedQueue.add(pcb);
        
    }
    
    /**
     * Take the pcb and move it to the Running data structure
     * @param pcb Process Control Block to be moved
     */
    public void moveToRunning(ProcessControlBlock pcb) {
        pcb.psw.status = ProgramStatusWord.RUNNING;
        runningProcess = pcb;        
    }
    
    /**
     * Execute the trap for the running process
     * @param trapNumber 
     */
    public void executeTrap(int trapNumber) {
        // Retrieve the information from register 11
        ProcessControlBlock pcb = runningProcess;
        int r11 = pcb.registers[11];
        int rr = pcb.relocationRegister;
        int fileDescriptor = Memory.mem[(rr + r11)/4];      // not used
        int bufferPointer = Memory.mem[(rr + r11 + 4)/4];   
        int size = Memory.mem[(rr + r11 + 8)/4];            // not used
        if(DEBUG) {
            System.out.println("Execute trap:");
            System.out.println("R11: " + r11);
            System.out.println(rr);
            System.out.println(fileDescriptor);
            System.out.println(bufferPointer);
            System.out.println(size);
        }
        if(trapNumber == 3) {
            // Call the device driver to perform the I/O operation
            //int readValue = StandardInput.readInteger();
            int readValue = (int)Math.floor(Math.random()*100 + 1);
            // Put the data read by the driver into the program bufferPointer space
            Memory.mem[(rr + bufferPointer)/4] = readValue;
        } else if (trapNumber == 5) {
            int intValue = Memory.mem[(rr + bufferPointer)/4];
            StandardOutput.writeInteger(intValue);
        }
        // return
        // *** BEWARE: do not use r1 in your program if you are going to read
        // from keyboard
        pcb.registers[1] = 0;
        // Insert your code here
    }
    
    /**
     * Run the scheduler.
     * Programs have  been loaded into memory, PCBs have been created 
     * and added to the Wait list.
     */
    public void run() {
        time = 0;
        while(!readyQueue.isEmpty()) {
            // Move a process from ready to run
            
            ProcessControlBlock pcb = chooseProcess();
            moveToRunning(pcb);
            // Load the context of the process
            processor.loadContext(pcb);
            int i = 0;
            // Execute numInstructions instructions
            // or until the program finishes
            while(!processor.finished 
                    && i < quantum
                    && !processor.interruptFlag) {
                processor.executeInstruction();
                time++;
                i++;
            }
            if(DEBUG) {
                System.out.println("Context Switch");
            }
            processor.saveContext(pcb);
            time += 10;
            contextSwitchTime += 10;
            // If program finished, move it to finished queue
            if(processor.finished) {
                addToFinishedQueue(pcb);
                // Prepare flag for any new process to be run
                processor.finished = false;
                turnaroundTime += i;
                map.remove(ticket);
            } else if (i >= quantum) {
                // Move the program to the ready queue
                turnaroundTime += i;                
                addToReadyQueue(pcb);
                updateMap(pcb);
            } else if(processor.interruptFlag) {
                addToBlockedQueue(pcb);
                //updateMap(pcb);
                executeTrap(processor.interruptNumber);                
                time += 100;
                turnaroundTime += i + 100;
                processor.interruptFlag = false;
                addToReadyQueue(pcb);
                updateMap(pcb);
            }
            runningProcess = null;
            // Check here
        }
        System.out.println("---------- Lottery ----------");
        double totalTurnaroundTime = (turnaroundTime + contextSwitchTime) / finishedQueue.size();
        System.out.println("Turnaround Time: " + (turnaroundTime + contextSwitchTime) + "/" + finishedQueue.size() + " = " + totalTurnaroundTime);
        System.out.println("Number of Processes: " + finishedQueue.size());
        double throughput = finishedQueue.size() / turnaroundTime;
        System.out.println("Throughput: " + finishedQueue.size() + "/" + (turnaroundTime + contextSwitchTime) + " = " + throughput);
    }
    
    public void deliverTickets() {
        for (int i = 0; i < readyQueue.size(); i++) {
            map.put(i + 1, readyQueue.get(i));
        }
    }
    
    public ProcessControlBlock chooseProcess() {        
        ProcessControlBlock p = null;
        while(p == null) {
            ticket = (int)Math.floor(Math.random()*5 + 1);
            for (Integer key : map.keySet()) {
                if (key == ticket) {
                    p = map.get(key);
                    readyQueue.remove(p);
                    break;
                }
            }
        }
        return p;
    }
    
    public void updateMap(ProcessControlBlock pcb) {
        for (Integer key : map.keySet()) {
                if (key == ticket) {
                    map.put(key, pcb);
                    break;
                }
        }
    }
}
