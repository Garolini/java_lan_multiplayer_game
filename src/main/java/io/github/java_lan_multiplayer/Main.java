package io.github.java_lan_multiplayer;

import io.github.java_lan_multiplayer.client.ClientMain;
import io.github.java_lan_multiplayer.server.ServerMain;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String mode;
        String[] args2;

        if (args.length == 0) {
            System.out.println("Welcome to \u001B[95mJava Lan Multiplayer Game\u001B[0m!");
            System.out.println("Enter mode [client/server] \u001B[37m[arguments]\u001B[0m: ");
            Scanner scanner = new Scanner(System.in);
            String[] inputArgs = scanner.nextLine().trim().split("\\s+");

            if (inputArgs.length == 0 || (!inputArgs[0].equalsIgnoreCase("client") && !inputArgs[0].equalsIgnoreCase("server"))) {
                System.err.println("Invalid mode. Use 'client' or 'server'");
                System.exit(1);
            }

            mode = inputArgs[0].toLowerCase();
            args2 = Arrays.copyOfRange(inputArgs, 1, inputArgs.length);
        } else {
            mode = args[0].toLowerCase();
            if (!mode.equals("client") && !mode.equals("server")) {
                System.err.println("Invalid mode. Use 'client' or 'server'");
                System.exit(1);
            }
            args2 = Arrays.copyOfRange(args, 1, args.length);
        }

        switch (mode) {
            case "client" -> ClientMain.main(args2);
            case "server" -> ServerMain.main(args2);
        }
    }
}