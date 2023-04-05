package hr.fer.srs;

import java.io.Console;
import java.util.Scanner;

public class Test {

    public static void main(String[] args) {
        Console console = System.console();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter password: ");


        String input = console.readLine();

        System.out.println("Password entered: " + input);


        String input2 = console.readPassword().toString();
        String input3 = new String(input2);
        System.out.println("Password entered: " + input3);
        System.out.println("Password entered: " + input2);

        System.out.println("pa preko scanner-a");
        String a = scanner.nextLine();
        System.out.println("Password entered: " + a);
    }
}
