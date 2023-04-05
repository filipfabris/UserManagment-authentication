package hr.fer.srs.commands;

import hr.fer.srs.ShellExecutor;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.nio.file.Path;
import java.util.List;

public class UseDatabaseCommand implements ShellCommand {

    private static final String COMMAND_NAME = "use";
    private static final List<String> COMMAND_DESCRIPTION = List.of("use specific database.");

    public static final String COMMAND_USAGE = "use [database_name]";


    @Override
    public ShellStatus executeCommand(Environment env, String arguments) throws ShellIOException {

        UtilitySharedCommand.checkArguments(env, arguments);

        String[] argumentsArray = arguments.trim().split("[\\s]+");

        if (argumentsArray.length != 1) {
            UtilitySharedCommand.singleArgument(env, COMMAND_NAME, argumentsArray.length);
            env.writeln( "Wrong input, usage: " + COMMAND_USAGE );
            return ShellStatus.CONTINUE;
        }

        String databaseName = argumentsArray[0];

        Path path = Path.of( ShellExecutor.DATABASES_PATH + databaseName );

        if (UtilitySharedCommand.checkIfExists(path) == false) {
            env.writeln( "Database with" + databaseName + "does not exist" );
            return ShellStatus.CONTINUE;
        }

        env.setMapInUse( databaseName );
        env.setDatabaseNameinUse( databaseName );

        env.writeln( "Database " + databaseName + " is now in use." );

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
