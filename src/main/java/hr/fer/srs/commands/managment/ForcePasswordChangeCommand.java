package hr.fer.srs.commands.managment;

import hr.fer.srs.UserManagement;
import hr.fer.srs.commands.UtilitySharedCommand;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.util.List;

public class ForcePasswordChangeCommand implements ShellCommand {
    private static final String COMMAND_NAME = "forcepass";
    private static final List<String> COMMAND_DESCRIPTION = List.of("Change user password in management database.");

    public static final String COMMAND_USAGE = "forcepass [username]";


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

        database_name = UserManagement.MANAGEMENT_DATABASE_NAME;
        userName = argumentsArray[0];

        //Check if user exists in database
        if(env.containsKey( database_name, userName ) == null) {
            env.writeln( "User does not exist." );
            return ShellStatus.CONTINUE;
        }

        env.forcePasswordChange( database_name, userName );


        env.writeln( "Successful, on next login user will need to change password." );

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

