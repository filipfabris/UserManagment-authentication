package hr.fer.srs.commands;

import hr.fer.srs.UserManagement;
import hr.fer.srs.status.OperationStatus;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellIOException;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UtilitySharedCommand {

	public static void checkArguments(Environment env, String arguments) {

		if (env == null) {
			throw new NullPointerException("Given environment cannot be null");
		}
		if (arguments == null) {
			throw new NullPointerException("Given string of arguments cannot be null");
		}
	}

	public static void noArguments(Environment env, String commandName, int length) throws ShellIOException {
		env.writeln(commandName + "  cannot take any arguments, recived: " + length);
	}

	public static void singleArgument(Environment env, String commandName, int length) throws ShellIOException {
		env.writeln(commandName + "  expects only one argument, recived: " + length);
	}

	public static void twoArguments(Environment env, String commandName, int length) throws ShellIOException {
		env.writeln(commandName + " expects two arguments, recived: " + length);
	}

	public static void threeArguments(Environment env, String commandName, int length) throws ShellIOException {
		env.writeln(commandName + " expects two arguments, recived: " + length);
	}

	public static void fourArguments(Environment env, String commandName, int length) throws ShellIOException {
		env.writeln(commandName + " expects two arguments, recived: " + length);
	}
	
	public static void oneOrtwoArguments(Environment env, String commandName, int length) throws ShellIOException {
		env.writeln(commandName + " expects one or two arguments, recived: " + length);
	}
	
	public static void oneOrNone(Environment env, String commandName, int length) throws ShellIOException {
		env.writeln(commandName + " expects one or none arguments, recived: " + length);
	}
	
	public static void commandNotExists(Environment env, String commandName) throws ShellIOException {
		env.writeln("command by name: " + commandName + " does not exists");
	}
	
	public static Path getPath(Environment env, String pathString) throws ShellIOException {
		Path path = null; //Bolje
//		 File file = null; //legacy
		try {
			path = Paths.get(pathString);
//            file = new File(pathString);
		} catch (InvalidPathException e) {
			env.writeln("Invalid path for given input: \"" + pathString + "\"");
		}
		
//		return file.toPath(); //Vraca path
//		return file.getPath(); //Vraca string
		return path;
	}

	public static boolean checkIfExists(Path path) throws ShellIOException {
//		File file = new File(path.toString()); //Legacy
//		if(file.exists()) {
//			return true;
//
//		}
		
		if (Files.exists(path)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean checkifDirectory(Path path) {
		if(Files.isDirectory(path)) {
			return true;
        }
		return false;
	}
	
	public static boolean checkifFile(Path path) {
		if(Files.isRegularFile(path)) {
			return true;
        }
		return false;
	}

	public static boolean checkPasswordComplexity(String password){
		if(password.length() < 8){
			return false;
		}
		boolean hasNumber = false;
		boolean hasSpecial = false;
		boolean hasUpper = false;
		boolean hasLower = false;

		for(int i = 0; i < password.length(); i++){
			if(Character.isDigit(password.charAt(i))){
				hasNumber = true;
			}
			if(!Character.isLetterOrDigit(password.charAt(i))){
				hasSpecial = true;
			}
			if(Character.isUpperCase(password.charAt(i))){
				hasUpper = true;
			}
			if(Character.isLowerCase(password.charAt(i))){
				hasLower = true;
			}
		}

		if(hasNumber && hasSpecial && hasUpper && hasLower){
			return true;
		}

		return false;
	}

	public static OperationStatus deleteUser(Environment env, String database_name, String username) throws ShellIOException {
		String username1 = username + UserManagement.FORCE_PASS_SEPARATOR + UserManagement.FORCE_PASS_NO;
		OperationStatus operationStatus1 = env.deleteEntry( database_name, null, username1);
		if(operationStatus1.equals( OperationStatus.FAILURE )) {
			return  OperationStatus.FAILURE;
		}

		String username2 = username + UserManagement.FORCE_PASS_SEPARATOR + UserManagement.FORCE_PASS_YES;
		OperationStatus operationStatus2 = env.deleteEntry( database_name, null, username2);
		if(operationStatus2.equals( OperationStatus.FAILURE )) {
			return  OperationStatus.FAILURE;
		}

		if(operationStatus1.equals( OperationStatus.SUCCESS ) || operationStatus2.equals( OperationStatus.SUCCESS )) {
			return OperationStatus.SUCCESS;
		}

		return OperationStatus.KEY_NOT_FOUND;

	}

}
