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

public class DeleteDatabaseCommand implements ShellCommand {
    private static final String COMMAND_NAME = "delete";
    private static final List<String> COMMAND_DESCRIPTION = List.of("Put data to database.");

    public static final String COMMAND_USAGE = "delete [database_name] [password] [key]";


    @Override
    public ShellStatus executeCommand(Environment env, String arguments) throws ShellIOException {

        UtilitySharedCommand.checkArguments(env, arguments);

        String[] argumentsArray = arguments.trim().split("[\\s]+");

        if (argumentsArray.length != 3) {
            if(argumentsArray.length == 2) {
                if(env.getMapInUse() == null) {
                    env.writeln( "No database is in use." );
                    return ShellStatus.CONTINUE;
                }
            }else {
                UtilitySharedCommand.threeArguments( env, COMMAND_NAME, argumentsArray.length );
                env.writeln( "Wrong input, usage: " + COMMAND_USAGE );
                return ShellStatus.CONTINUE;
            }
        }

        String database_name;
        String password;
        String key;

        if(argumentsArray.length == 2) {
            database_name = env.getDatabaseNameinUse();
            password = argumentsArray[0];
            key = argumentsArray[1];
        }else {
            database_name = argumentsArray[0];
            password = argumentsArray[1];
            key = argumentsArray[2];
        }

        Path path = Path.of( ShellExecutor.DATABASES_PATH + database_name );

        if (UtilitySharedCommand.checkIfExists(path) == false) {
            env.writeln( "Database does not exists with name: " + database_name );
            return ShellStatus.CONTINUE;
        }

        //Checks if password for database is correct
        if(Crypto.checkPassword(env, database_name, password )==false)
            return ShellStatus.CONTINUE;

        if(key.equalsIgnoreCase( "init" )){
            env.writeln( "Key init is reserved for database initialization." );
            return ShellStatus.CONTINUE;
        }

        //Deletion operation execution
        OperationStatus operationStatus = env.deleteEntry( database_name, password, key );

        if(operationStatus.equals( OperationStatus.KEY_NOT_FOUND )) {
            env.writeln( "Database " + database_name +" does contain key " + key + ".");
            return ShellStatus.CONTINUE;
        }

        if(operationStatus.equals( OperationStatus.FAILURE )) {
            env.writeln( "Error occured during deletion of key " + key + ".");
            return ShellStatus.CONTINUE;
        }

        env.writeln( "Successful deletion of key " + key + "." );

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
