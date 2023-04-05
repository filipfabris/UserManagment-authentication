package hr.fer.srs.strategy;

public interface DatabaseExecutor {

    boolean writeDatabase(String databaseName, String password, String input, Boolean append) throws ShellIOException;

    String readDatabase(String databaseName) throws ShellIOException;

}
