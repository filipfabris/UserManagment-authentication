package hr.fer.srs.commands;

import hr.fer.srs.ShellExecutor;
import hr.fer.srs.cryptography.Crypto;
import hr.fer.srs.status.OperationStatus;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.nio.file.Path;
import java.util.List;

public class DestroyDatabaseCommand implements ShellCommand {

    private static final String COMMAND_NAME = "destroy";
    private static final List<String> COMMAND_DESCRIPTION = List.of("Destroy the database.");

    public static final String COMMAND_USAGE = "destroy [database_name] [password]";


    @Override
    public ShellStatus executeCommand(Environment env, String arguments) throws ShellIOException {

        UtilitySharedCommand.checkArguments(env, arguments);

        String[] argumentsArray = arguments.trim().split("[\\s]+");

        if (argumentsArray.length != 2) {
            if(argumentsArray.length == 1) {
                if(env.getMapInUse() == null) {
                    env.writeln( "No database is in use." );
                    return ShellStatus.CONTINUE;
                }
            }else {
                UtilitySharedCommand.twoArguments( env, COMMAND_NAME, argumentsArray.length );
                env.writeln( "Wrong input, usage: " + COMMAND_USAGE );
                return ShellStatus.CONTINUE;
            }
        }

        String database_name;
        String password;

        if(argumentsArray.length == 1) {
            database_name = env.getDatabaseNameinUse();
            password = argumentsArray[0];
        }else {
            database_name = argumentsArray[0];
            password = argumentsArray[1];
        }

        Path path = Path.of( ShellExecutor.DATABASES_PATH + database_name );

        if (UtilitySharedCommand.checkIfExists(path) == false) {
            env.writeln( "Database does not exists with " + database_name + " name" );
            return ShellStatus.CONTINUE;
        }

        //Checks if password for database is correct
        if(Crypto.checkPassword(env, database_name, password )==false)
            return ShellStatus.CONTINUE;

        //Destroy database execution
        OperationStatus operationStatus = env.destroyDatabase(database_name);

        if(operationStatus.equals( OperationStatus.FAILURE )) {
        	env.writeln( "Database could not be destroyed" );
            return ShellStatus.CONTINUE;
        }

        env.writeln( "Database " + database_name + " destroyed." );

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
