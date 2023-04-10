package hr.fer.srs.commands.managment;

import hr.fer.srs.ShellExecutor;
import hr.fer.srs.UserManagement;
import hr.fer.srs.commands.UtilitySharedCommand;
import hr.fer.srs.cryptography.Crypto;
import hr.fer.srs.status.OperationStatus;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class AddUserCommand implements ShellCommand {
    private static final String COMMAND_NAME = "add";
    private static final List<String> COMMAND_DESCRIPTION = List.of("Add user to management database.");

    public static final String COMMAND_USAGE = "add [username]";


    @Override
    public ShellStatus executeCommand(Environment env, String arguments) throws ShellIOException {

        UtilitySharedCommand.checkArguments(env, arguments);

        String[] argumentsArray = arguments.trim().split("[\\s]+");

        if (argumentsArray.length != 1) {
                UtilitySharedCommand.singleArgument( env, COMMAND_NAME, argumentsArray.length );
                env.writeln( "Wrong input, usage: " + COMMAND_USAGE );
                return ShellStatus.CONTINUE;
            }

        String database_name;
        String userName;
        String userPassword;

        database_name = UserManagement.MANAGEMENT_DATABASE_NAME;
        userName = argumentsArray[0];

        //Check if user already exists in database
        if(env.containsKey( database_name, userName ) != null) {
            env.writeln( "User already exists." );
            return ShellStatus.CONTINUE;
        }

        env.writeln( "Enter password for user: " );
        env.write( env.getPromptSymbol().toString() + " " );
        String pass1 = env.readPasswordLine();
//        String pass1 = env.readLine();

        if(UtilitySharedCommand.checkPasswordComplexity( pass1 ) == false) {
            env.writeln( "Password is not complex enough." );
            env.writeln( "Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character and must have at least 8 characters" );
            return ShellStatus.CONTINUE;
        }

        env.writeln( "Enter password again: " );
        env.write( env.getPromptSymbol().toString() + " " );
        String pass2 = env.readPasswordLine();
//        String pass2 = env.readLine();



        if(pass1.equals( pass2 ) == false) {
            env.writeln( "Passwords do not match." );
            return ShellStatus.CONTINUE;
        }

        userPassword = pass1;

        userName = userName + UserManagement.FORCE_PASS_SEPARATOR + UserManagement.FORCE_PASS_NO;
        String userValue = userPassword;

        //Operation for putting new user to database
        OperationStatus operationStatus = env.addUserAuthorization( database_name, userName, userValue );

        if(operationStatus.equals( OperationStatus.FAILURE )) {
            return ShellStatus.CONTINUE;
        }

//        env.addFlag( database_name, userName, UserManagement.FORCE_PASS_SEPARATOR, UserManagement.FORCE_PASS_NO);
        env.writeln( "Successful adding user to database." );

        return ShellStatus.CONTINUE;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public List<String> getCommandDescription() {
        return COMMAND_DESCRIPTION;
    }

    @Override
    public String getCommandUsage() {
        return COMMAND_USAGE;
    }
}

