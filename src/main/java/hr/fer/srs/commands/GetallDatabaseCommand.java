package hr.fer.srs.commands;

import hr.fer.srs.ShellExecutor;
import hr.fer.srs.cryptography.Crypto;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.nio.file.Path;
import java.util.List;

public class GetallDatabaseCommand implements ShellCommand {
    private static final String COMMAND_NAME = "getall";
    private static final List<String> COMMAND_DESCRIPTION = List.of("Get all key value from database.");

    public static final String COMMAND_USAGE = "getall [database_name] [password]";


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
            env.writeln( "Database does not exists with name: " + password );
            return ShellStatus.CONTINUE;
        }

        //Checks if password for database is correct
        if(Crypto.checkPassword(env, database_name, password )==false)
            return ShellStatus.CONTINUE;

        //Get all values from database
        String output = env.getAllEntries( database_name, password );

        if(output == null) {
            env.writeln( "Wrong password for database: " + database_name );
            return ShellStatus.CONTINUE;
        }

        output = output.trim();
        env.writeln( "Database values:\n" + output );

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
