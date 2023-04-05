package hr.fer.srs.commands;

import hr.fer.srs.ShellExecutor;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.nio.file.Path;
import java.util.List;

public class InitDatabaseCommand implements ShellCommand {

    public static final String COMMAND_NAME = "init";
    private static final List<String> COMMAND_DESCRIPTION = List.of("Initializes the database.");

    public static final String COMMAND_USAGE = "init [database_name] [password]";


    @Override
    public ShellStatus executeCommand(Environment env, String arguments) throws ShellIOException {

        UtilitySharedCommand.checkArguments(env, arguments);

        String[] argumentsArray = arguments.trim().split("[\\s]+");

        if (argumentsArray.length != 2) {
            UtilitySharedCommand.twoArguments(env, COMMAND_NAME, argumentsArray.length);
            env.writeln( "Wrong input, usage: " + COMMAND_USAGE );
            return ShellStatus.CONTINUE;
        }

        Path path = Path.of( ShellExecutor.DATABASES_PATH + argumentsArray[0] );

        if (UtilitySharedCommand.checkIfExists(path) == true) {
            env.writeln( "Database already exists with this name" );
            return ShellStatus.CONTINUE;
        }

        try {
            String name = env.initDatabase(argumentsArray[0], argumentsArray[1]);

            if(name == null) {
                env.writeln( "Database could not be created" );
                return ShellStatus.CONTINUE;
            }

            env.writeln( "Database " + name + " initialized." );

        } catch (Exception e) {
            env.writeln( "Error while creating key for database" );
            return ShellStatus.CONTINUE;
        }

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
