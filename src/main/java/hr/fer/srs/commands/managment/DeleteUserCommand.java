package hr.fer.srs.commands.managment;

import hr.fer.srs.UserManagement;
import hr.fer.srs.commands.UtilitySharedCommand;
import hr.fer.srs.status.OperationStatus;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.util.List;

public class DeleteUserCommand implements ShellCommand {
    private static final String COMMAND_NAME = "del";
    private static final List<String> COMMAND_DESCRIPTION = List.of("Delete user from management database.");

    public static final String COMMAND_USAGE = "del [username]";


    @Override
    public ShellStatus executeCommand(Environment env, String arguments) throws ShellIOException {

        UtilitySharedCommand.checkArguments(env, arguments);

        String[] argumentsArray = arguments.trim().split("[\\s]+");

        if (argumentsArray.length != 1) {
            UtilitySharedCommand.singleArgument(env, COMMAND_NAME, argumentsArray.length );
            env.writeln( "Wrong input, usage: " + COMMAND_USAGE );
            return ShellStatus.CONTINUE;
        }

        String database_name;
        String userName;

        database_name = UserManagement.MANAGEMENT_DATABASE_NAME;
        userName = argumentsArray[0];

        //Operation for putting new user to database
        OperationStatus operationStatus = UtilitySharedCommand.deleteUser(env, database_name, userName);

        if(operationStatus.equals( OperationStatus.FAILURE )) {
            return ShellStatus.CONTINUE;
        }

        env.writeln( "Successful removing user from database." );

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


