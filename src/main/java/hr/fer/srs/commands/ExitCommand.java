package hr.fer.srs.commands;

import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.util.List;

public class ExitCommand implements ShellCommand {
	
    private static final String COMMAND_NAME = "exit";
    
    private static final List<String> COMMAND_DESCRIPTION = List.of("exit, terminate shell");

	public static final String COMMAND_USAGE = "exit";

	@Override
	public ShellStatus executeCommand(Environment env, String arguments) throws ShellIOException{
		
        UtilitySharedCommand.checkArguments(env, arguments);
        
		String[] argumentsArray = arguments.trim().split("[\\s]+");
        
		//tekst iza komande, bijeline dopustamo
        if (arguments.isBlank() == false) {
        	UtilitySharedCommand.noArguments(env, COMMAND_NAME, argumentsArray.length);
			env.writeln("Wrong input, usage: " + COMMAND_USAGE);
            return ShellStatus.CONTINUE;
        }
        
        return ShellStatus.TERMINATE;
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
