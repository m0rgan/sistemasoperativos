/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Hardware;

import java.util.Scanner;

/**
 *
 * @author htrefftz
 */
public class StandardInput {
    public static int readInteger() {
        System.out.println("Please enter a number: ");
        Scanner s = new Scanner(System.in);
        return s.nextInt();
    }
}
