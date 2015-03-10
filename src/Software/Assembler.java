/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Software;

import Hardware.Processor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This code compiles assembly code into executable code
 * 
 * Changes:
 *   2015 02 28: Changed addi and subbi to receive labels as immediate
 *     operands. If operand is a label, its address is used.
 *   2015 03 01: Changed word directive to receive labels.
 *     If a label is found, its address is used.
 *   2015 03 04: Added support for main1 and main2, directives
 *     so define the starting point of a thread
 * @author htrefftz
 */
public class Assembler {
    
    /**
     * Table to store the label addresses,  created during the first pass
     * The String is the key, the name of the label
     * The Integer is the value, the address of the label
     * Labels must finish with a colon.
     * A label can be in a line by itself or with an instruction or a directive
     */
    TreeMap<String, Integer> labels;
    
    /**
     * List of directives
     * Directives include:
     * .data
     * .text
     * .word
     */
    TreeMap<String, Integer> directives;
    
    public static final int DATA = 0;
    public static final int SPACE = 1;
    public static final int TEXT = 2;
    public static final int WORD = 3;
    public static final int MAIN1 = 4;
    public static final int MAIN2 = 5;
    public static final int MAIN3 = 6;
    public static final int MAIN4 = 7;
    public static final int MAIN5 = 8;
    public static final String DATA_STRING = ".data";
    public static final String SPACE_STRING = ".space";
    public static final String TEXT_STRING = ".text";
    public static final String WORD_STRING = ".word";
    public static final String MAIN1_STRING = ".main1";
    public static final String MAIN2_STRING = ".main2";
    public static final String MAIN3_STRING = ".main3";
    public static final String MAIN4_STRING = ".main4";
    public static final String MAIN5_STRING = ".main5";
    
    /**
     * List of instructions
     * Instructions include:
     * MOV
     * MOVI
     * LDW
     * BEQ
     * ...
     * The String is the instruction name
     * The Integer is the numerical operation code
     */
    TreeMap<String, Integer> instructions;
    
    ArrayList<TokenStructure []> intermediateCode;
    /**
     * Executable file
     * Currently an array of integers.
     * Should be a Byte array, in order to address single bytes.
     * But in the current implementation, everything is a word 
     */
    ArrayList<Integer> executable;
    /**
     * Name of the file with the source code
     */
    String fileName;
    /**
     * Name of the output (executable) file
     */
    String executableFileName;
    /**
     * Counter to determine the address
     */
    int address;
    /**
     * Starting address of the data segment
     * In this version, we assume that the data segment is continuous
     */
    int dataSegmentStart;
    /**
     * Length of the data segment
     */
    int dataSegmentLength;
    /**
     * Starting address of the text segment
     * In this version, we assume that the text segment is continuous
     */
    int textSegmentStart;
    /**
     * Length of the text segment
     */
    int textSegmentLength;
    /**
     * Start of thread 1 (main1)
     */
    int main1Start = 0;
    /**
     * Start of thread 2 (main2)
     */
    int main2Start = 0;
    /**
     * Start of thread 3 (main3)
     */
    int main3Start = 0;
    /**
     * Start of thread 4 (main4)
     */
    int main4Start = 0;
    /**
     * Start of thread 5 (main5)
     */
    int main5Start = 0;
    
    boolean DEBUG = true;
    
    /**
     * Constructor
     * @param fileName name of the input file
     * @param executableFileName name of the executable file
     */
    public Assembler(String fileName, String executableFileName) {
        labels = new TreeMap<>();
        
        directives = new TreeMap<>();
        directives.put(DATA_STRING, DATA);
        directives.put(SPACE_STRING, SPACE);
        directives.put(TEXT_STRING, TEXT);
        directives.put(WORD_STRING, WORD);
        directives.put(MAIN1_STRING, MAIN1);
        directives.put(MAIN2_STRING, MAIN2);
        directives.put(MAIN3_STRING, MAIN3);
        directives.put(MAIN4_STRING, MAIN4);
        directives.put(MAIN5_STRING, MAIN5);
        
        
        instructions = new TreeMap<>();
        instructions.put(Processor.MOV_STRING, Processor.MOV);
        instructions.put(Processor.MOVN_STRING, Processor.MOVN);
        instructions.put(Processor.ADD_STRING, Processor.ADD);
        instructions.put(Processor.SUB_STRING, Processor.SUB);
        instructions.put(Processor.CMP_STRING, Processor.CMP);
        
        instructions.put(Processor.MOVI_STRING, Processor.MOVI);
        instructions.put(Processor.MVNI_STRING, Processor.MVNI);
        instructions.put(Processor.ADDI_STRING, Processor.ADDI);
        instructions.put(Processor.SUBI_STRING, Processor.SUBI);
        instructions.put(Processor.CMPI_STRING, Processor.CMPI);
        
        instructions.put(Processor.LDW_STRING, Processor.LDW);
        instructions.put(Processor.POP_STRING, Processor.POP);
        instructions.put(Processor.STW_STRING, Processor.STW);
        instructions.put(Processor.PSH_STRING, Processor.PSH);
        instructions.put(Processor.TSL_STRING, Processor.TSL);

        
        instructions.put(Processor.BEQ_STRING, Processor.BEQ);
        instructions.put(Processor.BNE_STRING, Processor.BNE);
        instructions.put(Processor.BLT_STRING, Processor.BLT);
        instructions.put(Processor.BGE_STRING, Processor.BGE);
        instructions.put(Processor.BLE_STRING, Processor.BLE);
        instructions.put(Processor.BGT_STRING, Processor.BGT);
        instructions.put(Processor.BR_STRING, Processor.BR);
        instructions.put(Processor.BSR_STRING, Processor.BSR);
        instructions.put(Processor.RET_STRING, Processor.RET);
        instructions.put(Processor.TRAP_STRING, Processor.TRAP);

        executable = new ArrayList<>();
        intermediateCode = new ArrayList<>();
        this.fileName = fileName;
        this.executableFileName = executableFileName;
        address = 0;
    }

    /**
     * Read the file finding the address of each label
     * Each label and its address is stored in the TreeMap labels
     * @throws java.io.FileNotFoundException
     */
    private void firstPass() throws FileNotFoundException {
        TokenStructure [] tokens;
        try (Scanner s = new Scanner(new File(fileName))) {
            while(s.hasNext()) {
                // Process one line
                String line = s.nextLine();
                if(DEBUG) 
                    System.out.println(line);
                tokens = scanLine(line);
                intermediateCode.add(tokens);
                if(DEBUG) {
                    //System.out.println("Number of tokens: " + tokens.size());
                    System.out.println(
                            tokens[0].tokenType + " " +
                            tokens[1].tokenType + " " +
                            tokens[2].tokenType
                     );
                }
                // label: add to label map
                if(tokens[0].tokenType == TokenStructure.label) {
                    labels.put(tokens[0].tokenString, address);
                }
                // Start of data segment
                if(tokens[1].tokenType == TokenStructure.directive && 
                        tokens[1].tokenString.equals(DATA_STRING)) {
                    dataSegmentStart = address;
                }
                // Start of text segment
                if(tokens[1].tokenType == TokenStructure.directive && 
                        tokens[1].tokenString.equals(TEXT_STRING)) {
                    textSegmentStart = address;
                    // In the current version, the data segment comes first,
                    // then the text segment
                    dataSegmentLength = textSegmentStart - dataSegmentStart;
                }
                // main1 and main2 directives
                if(tokens[1].tokenType == TokenStructure.directive && 
                        tokens[1].tokenString.equals(MAIN1_STRING)) {
                    main1Start = address;
                }
                if(tokens[1].tokenType == TokenStructure.directive && 
                        tokens[1].tokenString.equals(MAIN2_STRING)) {
                    main2Start = address;
                }
                if(tokens[1].tokenType == TokenStructure.directive && 
                        tokens[1].tokenString.equals(MAIN3_STRING)) {
                    main3Start = address;
                }
                if(tokens[1].tokenType == TokenStructure.directive && 
                        tokens[1].tokenString.equals(MAIN4_STRING)) {
                    main4Start = address;
                }
                if(tokens[1].tokenType == TokenStructure.directive && 
                        tokens[1].tokenString.equals(MAIN5_STRING)) {
                    main5Start = address;
                }
                // Word directive
                if(tokens[1].tokenType == TokenStructure.directive && 
                        tokens[1].tokenString.equals(WORD_STRING)) {
                    address += 4;
                }
                // Space directive
                if(tokens[1].tokenType == TokenStructure.directive && 
                        tokens[1].tokenString.equals(SPACE_STRING)) {
                    address += Integer.parseInt(tokens[2].tokenString);
                }
                // An instruction
                if(tokens[1].tokenType == TokenStructure.instruction) {
                    address += 4;
                }
            }
            // In the current version, the text segment comes at the end
            textSegmentLength = address - textSegmentStart;
        }
        if(DEBUG) {
            System.out.println(labels);
        }
        
    }
    
    /**
     * Takes a line and returns an ArrayList of tokens
     * Tokens are organized in 3 columns: 
     * - A label (optional)
     * - An instruction code or a directive
     *   directives start with a period (.data, .text, .word, etc)
     * - A set of parameters for an instruction or a directive
     *   
     * @param line 
     */
    private TokenStructure [] scanLine(String line) {
        int tokenType;
        String tokenString;
        ArrayList <TokenStructure> tokensArrayList = new ArrayList();
        TokenStructure [] tokensArray = new TokenStructure[3];
        // Initialize the array
        tokensArray[0] = new TokenStructure(TokenStructure.none, "");
        tokensArray[1] = new TokenStructure(TokenStructure.none, "");
        tokensArray[2] = new TokenStructure(TokenStructure.none, "");
        
        StringTokenizer st = new StringTokenizer(line);
        // First, separate the tokens in the line
        // and determine the type of each token
        while(st.hasMoreTokens()) {
            String word = st.nextToken().toLowerCase();
            //System.out.println(word);
            
            if(word.startsWith(".")) {
                // directive
                tokenType = TokenStructure.directive;
                tokenString = word;
            } else if (word.endsWith(":")) {
                tokenType = TokenStructure.label;
                tokenString = word;
            } else if (instructions.containsKey(word)) {
                tokenType = TokenStructure.instruction;
                tokenString = word;
            } else {
                tokenType = TokenStructure.other;
                tokenString = word;
            }
            tokensArrayList.add(new TokenStructure(tokenType, tokenString));
        }
        // Now arrange the tokens in 3 columns:
        // [label] (optional)
        // [
        //   (directive | instruction) 
        //   [parameter] (optional)
        // ]
        int size = tokensArrayList.size();
        TokenStructure ts;
        int tokenIndex = 0;
        if(tokenIndex < size) {
            ts = tokensArrayList.get(tokenIndex);
            // the line starts with a label
            if(ts.tokenType == TokenStructure.label) {
                tokensArray[0] = ts;
                tokenIndex++;
                if(tokenIndex < size) {
                    // a directive or an instruction
                    ts = tokensArrayList.get(tokenIndex);
                    if (ts.tokenType == TokenStructure.directive
                            || ts.tokenType == TokenStructure.instruction) {
                        tokensArray[1] = ts;
                        tokenIndex++;
                        if (tokenIndex < size) {
                            // parameters of an instruction
                            ts = tokensArrayList.get(tokenIndex);
                            if (ts.tokenType == TokenStructure.other) {
                                tokensArray[2] = ts;
                                tokenIndex++;
                            }
                        }
                    }
                }
            } else if(ts.tokenType == TokenStructure.directive ||
                    ts.tokenType == TokenStructure.instruction) {
                // if the line does not start with a label, 
                // it should start with an instruction or a directive
                tokensArray[1] = ts;
                tokenIndex++;
                if (tokenIndex < size) {
                    // this should be the parameters for an instruction
                    ts = tokensArrayList.get(tokenIndex);
                    if (ts.tokenType == TokenStructure.other) {
                        tokensArray[2] = ts;
                        tokenIndex++;
                    }
                }
            } 
        }        
        return tokensArray;
    }
    
    
    
    /**
     * The addresses of the labels are defined.
     * Now generate the binary code.
     */
    private void secondPass() {
        int a, b, c;
        address = 0;
        for(TokenStructure [] tokens: intermediateCode) {
            if(DEBUG) {
                System.out.println(tokens[0] + " " + 
                        tokens[1] + " " +
                        tokens[2]);
            }
            // label
            if (tokens[0].tokenType == TokenStructure.label) {
                /* nothing */
            }
            // Start of data segment
            if (tokens[1].tokenType == TokenStructure.directive
                    && tokens[1].tokenString.equals(DATA_STRING)) {
                /* nothing */
            }
            // Start of text segment
            if (tokens[1].tokenType == TokenStructure.directive
                    && tokens[1].tokenString.equals(TEXT_STRING)) {
                /* nothing */
            }
            // Word directive
            // Generate the code for the constant and advance the
            // address counter
            if (tokens[1].tokenType == TokenStructure.directive
                    && tokens[1].tokenString.equals(WORD_STRING)) {
                if(tokens[2].tokenString.equals("")) {
                    a = 0;
                } else { 
                    //a = Integer.parseInt(tokens[2].tokenString);
                    if(!isNumeric(tokens[2].tokenString.trim())) {
                        a = labels.get(tokens[2].tokenString+":");
                    } else {
                        a = Integer.parseInt(tokens[2].tokenString);
                    }

                }
                if(DEBUG) {
                    System.out.println("Word: " + a);
                }
                executable.add(a);
                address += 4;
            }
            // Space directive
            // Currently, the parameter of a space directive must
            // be multiple of 4
            // Generate 0s and advance the address counter
            if (tokens[1].tokenType == TokenStructure.directive
                    && tokens[1].tokenString.equals(SPACE_STRING)) {
                a = Integer.parseInt(tokens[2].tokenString);
                for(int i = 0; i < a / 4; i++) {
                    executable.add(0);
                }
                address += a;
            }
            // An instruction
            if (tokens[1].tokenType == TokenStructure.instruction) {
                // executable.add(new Integer(-1));
                a = encodeInstruction(tokens, address);
                executable.add(a);
                address += 4;
            }
        }
    }
    
    /**
     * Encode the current instruction
     * @param tokens array of TokenStructures, it contains the elements of 
     * the instruction being decoded
     * @param address address of the current address. It is needed for the
     * Branch instructions, since the displacement is relative
     * (current address - target address)
     * @return 
     */
    private int encodeInstruction(TokenStructure [] tokens, int address) {
        int instruction = -1;
        String opCodeString = tokens[1].tokenString;
        int opCode;
        int a;
        int b;
        int c;
        int imm;
        int disp;
        switch (opCodeString) {
            case Processor.MOV_STRING:
                {
                    opCode = Processor.MOV;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    c = Integer.parseInt(operands[1].substring(1));
                    instruction = putInstructionF0(opCode, a, 0, c);
                    break;
                }
            case Processor.MOVN_STRING:
                {
                    opCode = Processor.MOVN;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    c = Integer.parseInt(operands[1].substring(1));
                    instruction = putInstructionF0(opCode, a, 0, c);
                    break;
                }
            case Processor.ADD_STRING:
                {
                    opCode = Processor.ADD;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    b = Integer.parseInt(operands[1].substring(1));
                    c = Integer.parseInt(operands[2].substring(1));
                    instruction = putInstructionF0(opCode, a, b, c);
                    break;
                }
            case Processor.SUB_STRING:
                {
                    opCode = Processor.SUB;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    b = Integer.parseInt(operands[1].substring(1));
                    c = Integer.parseInt(operands[2].substring(1));
                    instruction = putInstructionF0(opCode, a, b, c);
                    break;
                }
            case Processor.CMP_STRING:
                {
                    opCode = Processor.CMP;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    b = Integer.parseInt(operands[0].substring(1));
                    c = Integer.parseInt(operands[1].substring(1));
                    instruction = putInstructionF0(opCode, 0, b, c);
                    break;
                }
            case Processor.MOVI_STRING:
                {
                    opCode = Processor.MOVI;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    imm = Integer.parseInt(operands[1]);
                    instruction = putInstructionF1(opCode, a, 0, imm);
                    break;
                }
            case Processor.MVNI_STRING:
                {
                    opCode = Processor.MVNI;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    imm = Integer.parseInt(operands[1]);
                    instruction = putInstructionF1(opCode, a, 0, imm);
                    break;
                }
            case Processor.ADDI_STRING:
                {
                    opCode = Processor.ADDI;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    b = Integer.parseInt(operands[1].substring(1));
                    //imm = Integer.parseInt(operands[2]);
                    if(!isNumeric(operands[2].trim())) {
                        imm = labels.get(operands[2]+":");
                    } else {
                        imm = Integer.parseInt(operands[2]);
                    }
                    instruction = putInstructionF1(opCode, a, b, imm);
                    break;
                }
            case Processor.SUBI_STRING:
                {
                    opCode = Processor.SUBI;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    b = Integer.parseInt(operands[1].substring(1));
                    //imm = Integer.parseInt(operands[2]);
                    if(!isNumeric(operands[2].trim())) {
                        imm = labels.get(operands[2]+":");
                    } else {
                        imm = Integer.parseInt(operands[2]);
                    }
                    instruction = putInstructionF1(opCode, a, b, imm);
                    break;
                }
            case Processor.CMPI_STRING:
                {
                    opCode = Processor.CMPI;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    b = Integer.parseInt(operands[0].substring(1));
                    imm = Integer.parseInt(operands[1]);
                    instruction = putInstructionF1(opCode, 0, b, imm);
                    break;
                }
            case Processor.LDW_STRING:
                {
                    opCode = Processor.LDW;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    //imm = Integer.parseInt(operands[1]);
                    if(!isNumeric(operands[1].trim())) {
                        disp = labels.get(operands[1]+":");
                    } else {
                        disp = Integer.parseInt(operands[1]);
                    }
                    b = Integer.parseInt(operands[2].substring(1));
                    // Format of F1 instructions is basically the same
                    // as for F2 instruction
                    instruction = putInstructionF1(opCode, a, b, disp);
                    break;
                }
            case Processor.POP_STRING:
                {
                    opCode = Processor.POP;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    b = Integer.parseInt(operands[1].substring(1));
                    disp = Integer.parseInt(operands[2]);
                    // Format of F1 instructions is basically the same
                    // as for F2 instruction
                    instruction = putInstructionF1(opCode, a, b, disp);
                    break;
                }
            case Processor.STW_STRING:
                {
                    opCode = Processor.STW;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    //imm = Integer.parseInt(operands[0]);
                    if(!isNumeric(operands[0].trim())) {
                        disp = labels.get(operands[0]+":");
                    } else {
                        disp = Integer.parseInt(operands[0]);
                    }
                    b = Integer.parseInt(operands[1].substring(1));
                    a = Integer.parseInt(operands[2].substring(1));
                    // Format of F1 instructions is basically the same
                    // as for F2 instruction
                    instruction = putInstructionF1(opCode, a, b, disp);
                    break;
                }
            case Processor.PSH_STRING:
                {
                    opCode = Processor.PSH;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    a = Integer.parseInt(operands[0].substring(1));
                    b = Integer.parseInt(operands[1].substring(1));
                    disp = Integer.parseInt(operands[2]);
                    // Format of F1 instructions is basically the same
                    // as for F2 instruction
                    instruction = putInstructionF1(opCode, a, b, disp);
                    break;
                }
            case Processor.TSL_STRING:
                {
                    opCode = Processor.TSL;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    //imm = Integer.parseInt(operands[0]);
                    if(!isNumeric(operands[0].trim())) {
                        disp = labels.get(operands[0]+":");
                    } else {
                        disp = Integer.parseInt(operands[0]);
                    }
                    b = Integer.parseInt(operands[1].substring(1));
                    a = Integer.parseInt(operands[2].substring(1));
                    // Format of F1 instructions is basically the same
                    // as for F2 instruction
                    instruction = putInstructionF1(opCode, a, b, disp);
                    break;
                }
            
            case Processor.BEQ_STRING:
                {
                    opCode = Processor.BEQ;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    disp = (labels.get(operands[0]+":") - address) / 4;
                    instruction = putInstructionF3(opCode, disp);
                    break;
                }
            case Processor.BNE_STRING:
                {
                    opCode = Processor.BNE;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    disp = (labels.get(operands[0]+":") - address) / 4;
                    instruction = putInstructionF3(opCode, disp);
                    break;
                }
            case Processor.BLT_STRING:
                {
                    opCode = Processor.BLT;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    disp = (labels.get(operands[0]+":") - address) / 4;
                    instruction = putInstructionF3(opCode, disp);
                    break;
                }
            case Processor.BLE_STRING:
                {
                    opCode = Processor.BLE;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    disp = (labels.get(operands[0]+":") - address) / 4;
                    instruction = putInstructionF3(opCode, disp);
                    break;
                }
            case Processor.BGT_STRING:
                {
                    opCode = Processor.BGT;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    disp = (labels.get(operands[0]+":") - address) / 4;
                    instruction = putInstructionF3(opCode, disp);
                    break;
                }
            case Processor.BGE_STRING:
                {
                    opCode = Processor.BGE;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    disp = (labels.get(operands[0]+":") - address) / 4;
                    instruction = putInstructionF3(opCode, disp);
                    break;
                }
            case Processor.BR_STRING:
                {
                    opCode = Processor.BR;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    disp = (labels.get(operands[0]+":") - address) / 4;
                    instruction = putInstructionF3(opCode, disp);
                    break;
                }
            case Processor.BSR_STRING:
                {
                    opCode = Processor.BSR;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    disp = (labels.get(operands[0]+":") - address) / 4;
                    instruction = putInstructionF3(opCode, disp);
                    break;
                }
            case Processor.RET_STRING:
                {
                    opCode = Processor.RET;
                    disp = 0;
                    instruction = putInstructionF3(opCode, disp);
                    break;
                }
            case Processor.TRAP_STRING:
                {
                    opCode = Processor.TRAP;
                    String [] operands = returnOperands(tokens[2].tokenString);
                    disp = Integer.parseInt(operands[0]);
                    instruction = putInstructionF3(opCode, disp);
                    break;
                }
        }
        //if(DEBUG) {
            System.out.println("put: " + opCodeString);
        //}
        return instruction;
    }
  
    /**
     * This small routine is used to determine if a displacement in a 
     * load or store instruction is numeric or a label
     * @param s String to check
     * @return true if the string is a number (minus sign optional)
     */
    private boolean isNumeric(String s) {
        return s.matches("-?\\d+");
    }
    

    
    /**
     * Parses an input string and returns an array with up to 3 parameters
     * Parameters are separated by blank space, comma or parenthesis.
     * @param s String to be tokenized
     * @return 
     */
    public String [] returnOperands(String s) {
        StringTokenizer st = new StringTokenizer(s,",() ");
        String [] operands = new String[3];
        int i = 0;
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if(DEBUG) 
                System.out.println(token);
            operands[i] = token;
            //registros[i] = Integer.parseInt(token.substring(1));
            i++;
        }
        return operands;
    }
    
    
    
    /**
     * Encodes F0 type of instructions
     * F0 instructions are formed as follows:
     * - 00
     * - opCode         (4 bits)
     * - a register     (4 bits)
     * - b register     (4 bits)
     * - not used       (14 bits)
     * - c register     (4 bits)
     * (based on Compiler Construction by N. Wirth, p. 47)
     * @return the encoded instruction as an integer
     */
    private int putInstructionF0(int opCode, int a, int b, int c) {
        int instruction;
        instruction = opCode;
        instruction = (instruction << 4) + a;
        instruction = (instruction << 4) + b;
        instruction = (instruction << 18) + (c % Processor.b8);
        if(DEBUG) {
            System.out.println("Instrucción 00: " + instruction);
        }
        return instruction;
    }
    
    /**
     * Encodes F1 or F2 type of instructions
     * F1 instructions are formed as follows:
     * - 01 (or 10)
     * - opCode         (4 bits)
     * - a register     (4 bits)
     * - b register     (4 bits)
     * - im (or disp)   (18 bits)
     * 
     * F2 instructions have the same structure, but instead of an
     * immediate operator (imm), a displacemente (disp) operator is
     * received, 
     * 
     * (based on Compiler Construction by N. Wirth, p. 47)
     * @return the encoded instruction as an integer
     */
    private int putInstructionF1(int opCode, int a, int b, int im) {
        int instruction;
        instruction = opCode;
        instruction = (instruction << 4) + a;
        instruction = (instruction << 4) + b;
        //instruction = (instruction << 18) + (im % Processor.b18);
        instruction = instruction << 18;
        int imInt = im;
        // Make sure the first 14 bits of imInt are 0
        imInt = (imInt << 14) >>> 14;
        instruction = instruction | imInt;
        if(DEBUG) {
            System.out.println("Instrucción 01 o 10: " + instruction);
        }
        return instruction;
    }
    
     /**
     * Encodes F3 type of instructions
     * F3 instructions are formed as follows:
     * - 11
     * - opCode         (4 bits)
     * - disp           (26 bits)
     * (based on Compiler Construction by N. Wirth, p. 47)
     * @return the encoded instruction as an integer
     */
    private int putInstructionF3(int opCode, int disp) {
        int instruction;
        int opCodeInt = opCode << 26;
        // Make sure the first 6 bits of dispInt are 0
        int dispInt = (disp << 6) >>> 6;
        //instruction = (instruction << 26) + (disp % Processor.b26);
        instruction = opCodeInt | dispInt;
        if(DEBUG) {
            System.out.println("Instrucción 11: " + instruction);
            System.out.printf("%32s%n", Integer.toBinaryString(instruction));
        }
        return instruction;
    }
    
   /**
     * Write the executable file
     */
    private void writeExecutable() throws IOException {
        // Write the size of the data segment
        
        // Write the size of the text segment
        
        // Write the data segment
        // (this could be crated by the loader in memory, no actual need
        // to put it in the executable file unless constans are defined
        // with directive .word)
        
        // Write the text segment
        if(DEBUG) {
            System.out.println("Executable file:");
        }
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(executableFileName))) {
            writer.write(dataSegmentStart + " ");
            writer.write(dataSegmentLength + " ");
            writer.write(textSegmentStart + " ");
            writer.write(textSegmentLength + " ");
            writer.write(main1Start + " ");
            writer.write(main2Start + " ");
            writer.write(main3Start + " ");
            writer.write(main4Start + " ");
            writer.write(main5Start + " ");
            for (Integer word : executable) {
                writer.write(word + " ");
                //writer.write(" ");
                //writer.newLine();
                if(DEBUG) {
                    System.out.println(word);                    
                }
            }
        }
    }
    /**
     * Invoke the first and second pass of the assembler
     */
    public void assemble() {
        //ArrayList<TokenStructure> structure = new ArrayList<>();
        try {
            firstPass();
            secondPass();
            writeExecutable();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        //return structure;
    }
    
    /**
     * This class simply holds a token structure, in order to pass tokens
     * around.
     * A token structure is comprised of:
     * - A tokenType
     * - A tokenString
     */
    public class TokenStructure {
        public static final int label = 0;
        public static final int directive = 1;
        public static final int instruction = 2;
        public static final int other = 3;
        public static final int none = 4;
        
        protected final int tokenType;
        protected final String tokenString;

        public TokenStructure(int tokenType, String tokenString) {
            this.tokenType = tokenType;
            this.tokenString = tokenString;
        }
        
        @Override
        public String toString() {
            String s = tokenType + ": " + tokenString;
            return s;
        }
    }
    
    // public Scanner lineInput = new Scanner(System.int);
    
    public static void main(String [] args) {
        String fileIn = "file.asm";
        String fileOut = "file.out";
        if(args.length == 2) {
            fileIn = args[0];
            fileOut = args[1];
        } 
        Assembler asm = new Assembler(fileIn, fileOut);
        asm.assemble();
    }
}


