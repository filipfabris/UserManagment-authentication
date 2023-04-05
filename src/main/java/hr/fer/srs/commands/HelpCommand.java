package hr.fer.srs.commands;

import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.util.List;

public class HelpCommand implements ShellCommand{

	private static final String COMMAND_NAME = "help";
    
    private static final List<String> COMMAND_DESCRIPTION = List.of("commands descriptions");
	public static final String COMMAND_USAGE = "help //help for all commands,\t help [command_name] //help for one command";

	@Override
	public ShellStatus executeCommand(Environment env, String arguments) throws ShellIOException {

		
        UtilitySharedCommand.checkArguments(env, arguments);
        
		String[] argumentsArray = arguments.trim().split("[\\s]+");
		
		if(argumentsArray.length > 1) {
			UtilitySharedCommand.oneOrNone(env, arguments, argumentsArray.length);
			env.writeln("Wrong input, usage:\n " + COMMAND_USAGE);
            return ShellStatus.CONTINUE;
		}
		
		ShellCommand command;
		String output;
		StringBuilder sb = new StringBuilder();;
				
		//help za jednu commandu
		if(argumentsArray.length == 1 && argumentsArray[0].length() != 0) {
			command = env.commands().get(argumentsArray[0]);
			
			if(command == null) {
				UtilitySharedCommand.commandNotExists(env, argumentsArray[0]);
				return ShellStatus.CONTINUE;
			}
			
			command.getCommandDescription().stream().forEach(s -> sb.append(s));
			
			output = String.format("Command name: %s\nCommand description: %s\nCommand usage: %s\n", command.getCommandName(), sb.toString(), command.getCommandUsage());
			env.write(output);
            return ShellStatus.CONTINUE;
		}
		
		//help all
		sb.setLength(0);
		StringBuilder helper = new StringBuilder();
		for(String key: env.commands().keySet()) {
			helper.setLength(0);
			command = env.commands().get(key);
			
			command.getCommandDescription().stream().forEach(s -> helper.append(s));
			output = String.format("Command name: %s\nCommand description: %s\nCommand usage: %s\n"
					,command.getCommandName(),helper.toString(), command.getCommandUsage());
			sb.append(output);
		}
		env.write(sb.toString());

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
