package hr.fer.srs.commands.user;

import hr.fer.srs.UserManagement;
import hr.fer.srs.commands.UtilitySharedCommand;
import hr.fer.srs.status.OperationStatus;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.status.login.LoginStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.util.List;

public class LoginCommand implements ShellCommand {
    private static final String COMMAND_NAME = "login";
    private static final List<String> COMMAND_DESCRIPTION = List.of("Login for user simulation");

    public static final String COMMAND_USAGE = "login [username]";


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


        //Check if user exists in database
        if(env.containsKey( database_name, userName ) == null) {
            env.writeln( "User does not exist." );
            return ShellStatus.CONTINUE;
        }

        env.writeln( "Enter password for user: " );
        env.write( env.getPromptSymbol().toString() + " " );
        String pass1 = env.readPasswordLine();
//        String pass1 = env.readLine();
        userPassword = pass1;


        LoginStatus loginStatus = env.userLogin( database_name, userName, userPassword );


        if(loginStatus.equals( LoginStatus.SUCCESS )) {
            env.writeln( "Successful login." );
            return ShellStatus.CONTINUE;
        }

        if (loginStatus.equals( LoginStatus.WRONG_USERNAME )) {
            env.writeln( "Unknown username." );
            return ShellStatus.CONTINUE;
        }

        if (loginStatus.equals( LoginStatus.WRONG_PASSWORD )) {
            env.writeln( "Wrong password." );
            return ShellStatus.CONTINUE;
        }

        env.writeln( "You must change password." );
        //Force password change
        env.writeln( "Enter new password: " );
        env.write( env.getPromptSymbol().toString() + " " );
        pass1 = env.readPasswordLine();
//        pass1 = env.readLine();

        if(UtilitySharedCommand.checkPasswordComplexity( pass1 ) == false) {
            env.writeln( "Password is not complex enough." );
            env.writeln( "Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character and must have at least 8 characters" );
            return ShellStatus.CONTINUE;
        }

        env.writeln( "Enter new password again: " );
        env.write( env.getPromptSymbol().toString() + " " );
        String pass2 = env.readPasswordLine();
//        String pass2 = env.readLine();

        if(pass1.equals( pass2 ) == false) {
            env.writeln( "Passwords do not match." );
            return ShellStatus.CONTINUE;
        }

        if(pass1.equals( userPassword ) == true) {
            env.writeln( "New password must be different from old password, please login again and change password" );
            return ShellStatus.CONTINUE;
        }

        userPassword = pass1;

        OperationStatus op = UtilitySharedCommand.deleteUser(env, database_name, userName);

        if(op.equals( OperationStatus.SUCCESS ) == false) {
            env.writeln( "Error while deleting user." );
            return ShellStatus.CONTINUE;
        }

        userName = userName + UserManagement.FORCE_PASS_SEPARATOR + UserManagement.FORCE_PASS_NO;

        OperationStatus operationStatus = env.addUserAuthorization( database_name, userName, userPassword );

        if(operationStatus.equals( OperationStatus.FAILURE )) {
            return ShellStatus.CONTINUE;
        }

//        env.addFlag( database_name, userName, UserManagement.FORCE_PASS_SEPARATOR, UserManagement.FORCE_PASS_NO);
        env.writeln( "Successful user login and changing password." );

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

