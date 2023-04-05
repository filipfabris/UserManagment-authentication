package hr.fer.srs.strategy;

import hr.fer.srs.status.ShellStatus;

import java.io.IOException;
import java.util.List;

public interface ShellCommand {
	
	ShellStatus executeCommand(Environment env, String arguments) throws ShellIOException;
	
	String getCommandName();
	
	List<String> getCommandDescription();

	String getCommandUsage();

}
