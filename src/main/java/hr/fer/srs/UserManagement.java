package hr.fer.srs;

import hr.fer.srs.commands.*;
import hr.fer.srs.commands.managment.AddUserCommand;
import hr.fer.srs.commands.managment.ChangePasswordCommand;
import hr.fer.srs.commands.managment.DeleteUserCommand;
import hr.fer.srs.commands.managment.ForcePasswordChangeCommand;
import hr.fer.srs.commands.user.LoginCommand;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

public class UserManagement {

    private static final SortedMap<String, ShellCommand> commands;

    public static final String MANAGEMENT_DATABASE_NAME = "userManagement.txt";

    public static final String FORCE_PASS_SEPARATOR = "&##&";

    public static final String FORCE_PASS_YES = "true";

    public static final String FORCE_PASS_NO = "false";


    static {
        commands = new TreeMap<>();
        commands.put( "exit", new ExitCommand() );
        commands.put( "add", new AddUserCommand() );
        commands.put( "del", new DeleteUserCommand() );
        commands.put( "passwd", new ChangePasswordCommand() );
        commands.put("forcepass", new ForcePasswordChangeCommand());
    }

    public static void main(String[] args) {

        try (Scanner scener = new Scanner( System.in )) {

            ShellExecutor myShell = new ShellExecutor( scener );
            myShell.writeln( "SRS lab 2-1.0.0v" );

            myShell.writeln( "Welcome to user management database." );

            Path path = Path.of( ShellExecutor.DATABASES_PATH + MANAGEMENT_DATABASE_NAME );

            if (UtilitySharedCommand.checkIfExists(path) == false) {
                //Initialise database if it does not exist
                File file = new File( path.toString() );
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    myShell.writeln( "Could not initialise management tool." );
                    return;
                }
            }

            while (true) {

                try {
                    if (myShell.status.equals( ShellStatus.TERMINATE )) {
                        break;
                    }

                    myShell.write( myShell.getPromptSymbol().toString() + " " );

                    //Razdvoji komandu i argumente
                    String[] input = myShell.readLine().trim().split( "[\\s]+", 2 );

                    String commandName = input[0].toLowerCase();
                    String arguments;

                    if (input.length == 2) {
                        arguments = input[1].toLowerCase();
                    } else {
                        arguments = "";
                    }

                    //UserManagment commands
                    ShellCommand command = UserManagement.commands.get( commandName );

                    if (command == null) {
                        myShell.writeln( "Unknown command \"" + commandName + "\"" );
                        continue;
                    }

                    myShell.status = command.executeCommand( myShell, arguments );

                } catch (ShellIOException e) {
                    myShell.writeln( e.getMessage() );
                    //Nastavi izvoditi
                } catch (RuntimeException e) {
                    myShell.writeln( e.getMessage() );
                    //Nije jasna greska
                    return;
                }

            }

        } catch (ShellIOException e) {
            //Scanner error
            e.printStackTrace();
            System.err.println( "Greska" );
        }
    }

}
