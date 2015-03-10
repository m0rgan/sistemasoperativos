/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Software;

/**
 * This word stores various information about the execution of a program,
 * including:
 * n: negative flag, set by comparison instructions
 * z: zero flag, set by comparison instructions
 * status:
 * mode:
 * 
 * @author htrefftz
 */
public class ProgramStatusWord {
    /**
     * Status of the process
     */
    public static final int RUNNING = 0;
    public static final int READY = 1;
    public static final int BLOCKED = 2;
    public static final int FINISHED = 3;
    public static final int SUSPENDED = 4;
    /**
     * Mode of execution
     */
    public static final int USER = 0;
    public static final int KERNEL = 1;
    
    /**
     * z Zero is the result of a comparison
     */
    public boolean z;
    
    /**
     * n Negative is the result of a comparison
     */
    public boolean n;
    
    /**
     * Status of the program
     * . running
     * . ready
     * . blocked
     * . finished
     * . suspended
     */
    public int status;
    /**
     * Execution mode
     * . user
     * . kernel
     */
    public int mode;

    /**
     * Constructor
     * @param z     Zero flag
     * @param n     Negative flag
     * @param status    Running, Watiting, Blocked, ...
     * @param mode      User or Kernel
     */
    public ProgramStatusWord(boolean z, boolean n, int status, int mode) {
        this.z = z;
        this.n = n;
        this.status = status;
        this.mode = mode;
    }
    
}

