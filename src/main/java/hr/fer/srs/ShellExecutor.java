package hr.fer.srs;

import hr.fer.srs.commands.*;
import hr.fer.srs.cryptography.Crypto;
import hr.fer.srs.status.OperationStatus;
import hr.fer.srs.status.ShellStatus;
import hr.fer.srs.status.login.LoginStatus;
import hr.fer.srs.strategy.DatabaseExecutor;
import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellCommand;
import hr.fer.srs.strategy.ShellIOException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.exit;

public class ShellExecutor implements Environment, DatabaseExecutor {

    public static String DATABASES_PATH = "database/";

    private Scanner scener;
    ShellStatus status;

    private Character promptSymbol;

    private PrintStream ps = new PrintStream( System.out, true, Charset.forName( "UTF-8" ) );

    private static final SortedMap<String, ShellCommand> commands;

    private Map<String, String> databaseMap;

    private String databaseName;

    private Console console = System.console();

    static {
        commands = new TreeMap<>();
        commands.put( "init", new InitDatabaseCommand() );
        commands.put( "exit", new ExitCommand() );
        commands.put( "help", new HelpCommand() );
        commands.put( "destroy", new DestroyDatabaseCommand() );
        commands.put( "put", new PutDatabaseCommand() );
        commands.put( "delete", new DeleteDatabaseCommand() );
        commands.put( "get", new GetDatabaseCommand() );
        commands.put( "getall", new GetallDatabaseCommand() );
        commands.put( "list", new ListDatabaseCommand() );
        commands.put( "use", new UseDatabaseCommand() );
    }

    public ShellExecutor(Scanner scener) {
        this.scener = scener;
        this.promptSymbol = '>';
        this.status = ShellStatus.CONTINUE;
        this.databaseMap = null;
        this.databaseName = null;
    }


//    public static void main(String[] args) {
//
//        //init [password] -+
//        //destroy [database_name] [password] -+
//        //put [database_name] [password] [key] [value] -+
//        //delete [database_name] [password] [key] -+
//        //get [database_name] [password] [key] -+
//        //getall [database_name] [password] -+
//        //list -+
//        //exit -+
//        try (Scanner scener = new Scanner( System.in )) {
//
//            ShellExecutor myShell = new ShellExecutor( scener );
//            myShell.writeln( "SRS lab 1-1.0.0v" );
//
//            while (true) {
//
//                try {
//                    if (myShell.status.equals( ShellStatus.TERMINATE )) {
//                        break;
//                    }
//
//                    myShell.write( myShell.getPromptSymbol().toString() + " " );
//
//                    //Razdvoji komandu i argumente
//                    String[] input = myShell.readLine().trim().split( "[\\s]+", 2 );
//
//                    String commandName = input[0].toLowerCase();
//                    String arguments;
//
//                    if (input.length == 2) {
//                        arguments = input[1].toLowerCase();
//                    } else {
//                        arguments = "";
//                    }
//
//                    ShellCommand command = ShellExecutor.commands.get( commandName );
//
//                    if (command == null) {
//                        myShell.writeln( "Unknown command \"" + commandName + "\"" );
//                        continue;
//                    }
//
//                    myShell.status = command.executeCommand( myShell, arguments );
//
//                } catch (ShellIOException e) {
//                    myShell.writeln( e.getMessage() );
//                    //Nastavi izvoditi
//                } catch (RuntimeException e) {
//                    myShell.writeln( e.getMessage() );
//                    //Nije jasna greska
//                    return;
//                }
//
//            }
//
//        } catch (ShellIOException e) {
//            //Scanner error
//            System.err.println( "Greska" );
//        }
//    }

    @Override
    public String readLine() throws ShellIOException {
        try {
            StringBuilder sb = new StringBuilder();

            String line = this.scener.nextLine().trim();

            return sb.append( line ).toString();

        } catch (RuntimeException e) {
            throw new ShellIOException( e.getMessage() );
        }
    }

    @Override
    public String readPasswordLine() {
        String line = new String(console.readPassword()).trim();
        return line;
    }

    @Override
    public void write(String text) throws ShellIOException {
        try {
            if (text != null) {
                ps.print( text );
            } else {
                throw new NullPointerException( "text should not be null" );
            }
        } catch (RuntimeException e) {
            throw new ShellIOException( e.getMessage() );
        }

    }

    @Override
    public void writeln(String text) throws ShellIOException {
        try {
            if (text != null) {
                ps.println( text );
            } else {
                throw new NullPointerException( "text should not be null" );
            }
        } catch (RuntimeException e) {
            throw new ShellIOException( e.getMessage() );
        }

    }

    @Override
    public SortedMap<String, ShellCommand> commands() {
        return commands;
    }

    @Override
    public void setMapInUse(String databaseName) throws ShellIOException {
        List<String> input = null;
        this.databaseMap = new LinkedHashMap<>();
        try {
            input = this.readDatabaseAsList( databaseName );
        } catch (ShellIOException e) {
            this.writeln( "Could not read database" );
        }

        for (String line : input) {
            String[] keyValue = line.split( Crypto.SEPARATOR );
            databaseMap.put( keyValue[0], keyValue[1] );
        }
    }

    @Override
    public Map<String, String> getMapInUse() {
        return databaseMap;
    }

    @Override
    public void setDatabaseNameinUse(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String getDatabaseNameinUse() {
        return databaseName;
    }

    @Override
    public Character getPromptSymbol() {
        return promptSymbol;
    }

    @Override
    public String initDatabase(String name, String password) throws ShellIOException {
        try {
            Path path = Path.of( DATABASES_PATH + name );
            File file = new File( path.toString() );
            file.createNewFile();

            String hexLine = Crypto.prepareLineForDatabase( password, "init", name );

            if (hexLine == null) {
                this.writeln( "Failure while converting to hex" );
                return null;
            }

            try (BufferedWriter writer = Files.newBufferedWriter( path, StandardCharsets.UTF_8 )) {
                writer.write( hexLine );
            }

            return file.getName();

        } catch (IOException e) {
            this.writeln( "Error while writing database" );
        } catch (Exception e) {
            this.writeln( "Error while creating database" );
        }

        return null;
    }

    @Override
    public OperationStatus destroyDatabase(String name) throws ShellIOException {
        Path path = Path.of( DATABASES_PATH + name );

        File file = new File( path.toString() );

        if (file.delete()) {
            this.databaseMap = null;
            this.databaseName = null;
            return OperationStatus.SUCCESS;
        } else {
            return OperationStatus.FAILURE;
        }
    }

    @Override
    public OperationStatus putEntry(String databaseName, String password, String key, String value) throws ShellIOException {
        List<String> listDatabases = this.listDatabases();
        Map<String, String> passwordMap;


        if (listDatabases.contains( databaseName ) == false) {
            this.writeln( "Database with name" + databaseName + " does not exists" );
            return OperationStatus.FAILURE;
        }

        List<String> input = this.readDatabaseAsList( databaseName );
        passwordMap = new LinkedHashMap<>();

        for (String line : input) {
            String[] keyValue = line.split( Crypto.SEPARATOR );
            passwordMap.put( keyValue[0], keyValue[1] );
        }


        String hexKey = null;
        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
            byte[] EncodedHashKey = digest.digest( key.getBytes( StandardCharsets.UTF_8 ) );
            hexKey = Base64.getEncoder().encodeToString( EncodedHashKey );
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        String entryToAdd = Crypto.prepareLineForDatabase( password, key, value );
        if (entryToAdd == null) {
            this.writeln( "Failure while converting to hex" );
            return OperationStatus.FAILURE;
        }

        String hexLine = passwordMap.get( hexKey );
        String[] keyValue = entryToAdd.split( Crypto.SEPARATOR );
        if (hexLine != null) {
            passwordMap.put( keyValue[0], keyValue[1] ); //Overidas previous key

            //Write it to database
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : passwordMap.entrySet()) {
                String lineToBeAdded = entry.getKey() + Crypto.SEPARATOR + entry.getValue().trim();
                sb.append( lineToBeAdded ).append( System.lineSeparator() );
            }

            try {
                this.writeDatabase( databaseName, password, sb.toString(), false );
            } catch (ShellIOException e) {
                return OperationStatus.FAILURE;
            }
        } else {
            //Entry does not exists, just append database
            try {
                this.writeDatabase( databaseName, password, entryToAdd, true );
            } catch (ShellIOException e) {
                return OperationStatus.FAILURE;
            }
        }

        this.setMapInUse( databaseName ); //Update map in use
        return OperationStatus.SUCCESS;

    }






    @Override
    public OperationStatus addUserAuthorization(String databaseName, String key, String value) throws ShellIOException {
        List<String> listDatabases = this.listDatabases();
        Map<String, String> passwordMap;


        if (listDatabases.contains( databaseName ) == false) {
            this.writeln( "Database with name" + databaseName + " does not exists" );
            return OperationStatus.FAILURE;
        }

        List<String> input = this.readDatabaseAsList( databaseName );
        passwordMap = new LinkedHashMap<>();

        for (String line : input) {
            String[] keyValue = line.split( Crypto.SEPARATOR );
            passwordMap.put( keyValue[0], keyValue[1] );
        }


        String hexKey = null;
        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
            byte[] EncodedHashKey = digest.digest( key.getBytes( StandardCharsets.UTF_8 ) );
            hexKey = Base64.getEncoder().encodeToString( EncodedHashKey );
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        String entryToAdd = Crypto.prepareLineAutentificator(key, value);
        if (entryToAdd == null) {
            this.writeln( "Failure while converting to hex" );
            return OperationStatus.FAILURE;
        }

        String hexLine = passwordMap.get( hexKey );
        String[] keyValue = entryToAdd.split( Crypto.SEPARATOR );
        if (hexLine != null) {
            passwordMap.put( keyValue[0], keyValue[1] ); //Overidas previous key

            //Write it to database
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : passwordMap.entrySet()) {
                String lineToBeAdded = entry.getKey() + Crypto.SEPARATOR + entry.getValue().trim();
                sb.append( lineToBeAdded ).append( System.lineSeparator() );
            }

            try {
                this.writeDatabase( databaseName, null, sb.toString(), false );
            } catch (ShellIOException e) {
                return OperationStatus.FAILURE;
            }
        } else {
            //Entry does not exists, just append database
            try {
                this.writeDatabase( databaseName, null, entryToAdd, true );
            } catch (ShellIOException e) {
                return OperationStatus.FAILURE;
            }
        }

        this.setMapInUse( databaseName ); //Update map in use
        return OperationStatus.SUCCESS;

    }










    @Override
    public String getEntry(String databaseName, String password, String key) throws ShellIOException {
        List<String> listDatabases = this.listDatabases();
        Map<String, String> passwordMap;

        if (listDatabases.contains( databaseName ) == false) {
            this.writeln( "Database with name" + databaseName + " does not exists" );
            return null;
        }

        //Does not have map in use
        if (this.getMapInUse() == null) {
            List<String> input = this.readDatabaseAsList( databaseName );
            passwordMap = new LinkedHashMap<>();

            for (String line : input) {
                String[] keyValue = line.split( Crypto.SEPARATOR );
                passwordMap.put( keyValue[0], keyValue[1] );
            }

        } else {
            passwordMap = this.getMapInUse(); //Getting map in use
        }

        String hexKey = null;
        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
            byte[] EncodedHashKey = digest.digest( key.getBytes( StandardCharsets.UTF_8 ) );
            hexKey = Base64.getEncoder().encodeToString( EncodedHashKey );
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        String hexLine = passwordMap.get( hexKey );

        if (hexLine == null) {
            return null;
        }

        try {
            String decriptedvalue = Crypto.lineDecryption( password, hexLine );
            String[] values = decriptedvalue.split( "=" );

            if (Crypto.checkSHA( hexKey, values[0] ) == false) {
                this.writeln( "Lines have been changed" );
                exit( 0 );
            }

            return values[1];
        } catch (Exception e) {
            this.writeln( "Wrong password" );
            return null;
        }
    }

    @Override
    public LoginStatus userLogin(String databaseName, String username, String password) throws ShellIOException {
        List<String> listDatabases = this.listDatabases();
        Map<String, String> passwordMap;

        if (listDatabases.contains( databaseName ) == false) {
            this.writeln( "Database with name" + databaseName + " does not exists" );
            return null;
        }

        List<String> input = this.readDatabaseAsList( databaseName );
        passwordMap = new LinkedHashMap<>();

        for (String line : input) {
            String[] keyValue = line.split( Crypto.SEPARATOR );
            passwordMap.put( keyValue[0], keyValue[1] );
        }

        String hexKey = null;
        String hexKeyNoForceChange = null;
        String hexKeyYesForceChange = null;
        String username1 = username + UserManagement.FORCE_PASS_SEPARATOR + UserManagement.FORCE_PASS_NO;
        String username2 = username + UserManagement.FORCE_PASS_SEPARATOR + UserManagement.FORCE_PASS_YES;

        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
            byte[] EncodedHashKey1 = digest.digest( username1.getBytes( StandardCharsets.UTF_8 ) );
            byte[] EncodedHashKey2 = digest.digest( username2.getBytes( StandardCharsets.UTF_8 ) );
            hexKeyNoForceChange = Base64.getEncoder().encodeToString( EncodedHashKey1 );
            hexKeyYesForceChange = Base64.getEncoder().encodeToString( EncodedHashKey2 );
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        //Username has in format username=true or username=false, true false is for force password change
        String hexLine = passwordMap.get( hexKeyNoForceChange ); //Get without force change
        hexKey = hexKeyNoForceChange;
        username = username1;
        if (hexLine == null) {
            hexLine = passwordMap.get( hexKeyYesForceChange ); //Get without force change
            hexKey = hexKeyYesForceChange; //Set key to yes force change
            username = username2;
        }

        if (hexLine == null) {
            return LoginStatus.WRONG_USERNAME;
        }

//        String[] flagLines = hexLine.split( UserManagement.FORCE_PASS_SEPARATOR );

        try {
            boolean decriptedvalue1 = Crypto.checkAuthorization( username1, password, hexLine );
            boolean decriptedvalue2 = Crypto.checkAuthorization( username2, password, hexLine );


            if(decriptedvalue1 == false && decriptedvalue2 == false){
                return LoginStatus.WRONG_PASSWORD;
            }

            if (hexKey.equals( hexKeyYesForceChange )) {
                return LoginStatus.CHANGE_PASSWORD;
            }

            return LoginStatus.SUCCESS;

        } catch (Exception e) {
            return LoginStatus.WRONG_PASSWORD;
        }
    }

    @Override
    public String forcePasswordChange(String databaseName, String username) throws ShellIOException {
        List<String> listDatabases = this.listDatabases();
        Map<String, String> passwordMap;

        if (listDatabases.contains( databaseName ) == false) {
            this.writeln( "Database with name" + databaseName + " does not exists" );
            return null;
        }

        //Does not have map in use
        if (this.getMapInUse() == null) {
            List<String> input = this.readDatabaseAsList( databaseName );
            passwordMap = new LinkedHashMap<>();

            for (String line : input) {
                String[] keyValue = line.split( Crypto.SEPARATOR );
                passwordMap.put( keyValue[0], keyValue[1] );
            }

        } else {
            passwordMap = this.getMapInUse(); //Getting map in use
        }

        String hexKey = null;
        String hexKeyNoForceChange = null;
        String hexKeyYesForceChange = null;
        String username1 = username + UserManagement.FORCE_PASS_SEPARATOR + UserManagement.FORCE_PASS_NO;
        String username2 = username + UserManagement.FORCE_PASS_SEPARATOR + UserManagement.FORCE_PASS_YES;

        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
            byte[] EncodedHashKey1 = digest.digest( username1.getBytes( StandardCharsets.UTF_8 ) );
            byte[] EncodedHashKey2 = digest.digest( username2.getBytes( StandardCharsets.UTF_8 ) );
            hexKeyNoForceChange = Base64.getEncoder().encodeToString( EncodedHashKey1 );
            hexKeyYesForceChange = Base64.getEncoder().encodeToString( EncodedHashKey2 );
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        //Username has in format username=true or username=false, true false is for force password change
        String hexLine = passwordMap.get( hexKeyNoForceChange ); //Get without force change
        hexKey = hexKeyNoForceChange;
        if (hexLine == null) {
            hexLine = passwordMap.get( hexKeyYesForceChange ); //Get without force change
            hexKey = hexKeyYesForceChange; //Set key to yes force change
        }

        if (hexLine == null) {
            return null;
        }

        //hexkey conatins username which exists
        String encryptedSicret = passwordMap.get( hexKey );
        passwordMap.remove( hexKey );
        passwordMap.put( hexKeyYesForceChange, encryptedSicret );

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : passwordMap.entrySet()) {
            sb.append( entry.getKey() + Crypto.SEPARATOR + entry.getValue() + "\n" );
        }

        try {
            this.writeDatabase( databaseName, null, sb.toString(), false );
        } catch (ShellIOException e) {
            return null;
        }

        return encryptedSicret;
    }


    @Override
    public OperationStatus deleteEntry(String databaseName, String password, String key) throws ShellIOException {
        List<String> listDatabases = this.listDatabases();
        Map<String, String> passwordMap;

        if (listDatabases.contains( databaseName ) == false) {
            return OperationStatus.FAILURE;
        }

        //Does not have map in use
        List<String> input = this.readDatabaseAsList( databaseName );
        passwordMap = new LinkedHashMap<>();

        for (String line : input) {
            String[] keyValue = line.split( Crypto.SEPARATOR );
            passwordMap.put( keyValue[0], keyValue[1] );
        }


        String hexKey = null;
        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
            byte[] EncodedHashKey = digest.digest( key.getBytes( StandardCharsets.UTF_8 ) );
            hexKey = Base64.getEncoder().encodeToString( EncodedHashKey );
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        if (passwordMap.containsKey( hexKey ) == false) {
            return OperationStatus.KEY_NOT_FOUND;
        }

        passwordMap.remove( hexKey );

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : passwordMap.entrySet()) {
            sb.append( entry.getKey() + Crypto.SEPARATOR + entry.getValue() + "\n" );
        }

        try {
            this.writeDatabase( databaseName, password, sb.toString(), false );
        } catch (ShellIOException e) {
            return OperationStatus.FAILURE;
        }

        return OperationStatus.SUCCESS;
    }

    @Override
    public String containsKey(String databaseName, String key) throws ShellIOException {
        List<String> listDatabases = this.listDatabases();
        Map<String, String> passwordMap;

        if (listDatabases.contains( databaseName ) == false) {
            return null;
        }

        //Does not have map in use
        List<String> input = this.readDatabaseAsList( databaseName );
        passwordMap = new LinkedHashMap<>();

        for (String line : input) {
            String[] keyValue = line.split( Crypto.SEPARATOR );
            passwordMap.put( keyValue[0], keyValue[1] );
        }


        String hexKey = null;
        String hexKeyNoForceChange = null;
        String hexKeyYesForceChange = null;
        String username1 = key + UserManagement.FORCE_PASS_SEPARATOR + UserManagement.FORCE_PASS_NO;
        String username2 = key + UserManagement.FORCE_PASS_SEPARATOR + UserManagement.FORCE_PASS_YES;

        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
            byte[] EncodedHashKey1 = digest.digest( username1.getBytes( StandardCharsets.UTF_8 ) );
            byte[] EncodedHashKey2 = digest.digest( username2.getBytes( StandardCharsets.UTF_8 ) );
            hexKeyNoForceChange = Base64.getEncoder().encodeToString( EncodedHashKey1 );
            hexKeyYesForceChange = Base64.getEncoder().encodeToString( EncodedHashKey2 );
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        //Username has in format username=true or username=false, true false is for force password change
        String hexLine = passwordMap.get( hexKeyNoForceChange ); //Get without force change
        hexKey = hexKeyNoForceChange;
        if (hexLine == null) {
            hexLine = passwordMap.get( hexKeyYesForceChange ); //Get without force change
            hexKey = hexKeyYesForceChange; //Set key to yes force change
        }

        if (hexLine == null) {
            return null;
        }

        return passwordMap.get( hexKey );
    }

    @Override
    public String addFlag(String databaseName, String username, String flag, String force) throws ShellIOException {
        List<String> listDatabases = this.listDatabases();
        Map<String, String> passwordMap;

        //Does not have map in use
        if (this.getMapInUse() == null) {
            List<String> input = this.readDatabaseAsList( databaseName );
            passwordMap = new LinkedHashMap<>();

            for (String line : input) {
                String[] keyValue = line.split( Crypto.SEPARATOR );
                passwordMap.put( keyValue[0], keyValue[1] );
            }

        } else {
            passwordMap = this.getMapInUse(); //Getting map in use
        }

        String hexKey = null;
        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
            byte[] EncodedHashKey = digest.digest( username.getBytes( StandardCharsets.UTF_8 ) );
            hexKey = Base64.getEncoder().encodeToString( EncodedHashKey );
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        String hexLine = passwordMap.get( hexKey );
        hexLine = hexLine + flag + force;

        passwordMap.put( hexKey, hexLine );

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : passwordMap.entrySet()) {
            String lineToBeAdded = entry.getKey() + Crypto.SEPARATOR + entry.getValue().trim();
            sb.append( lineToBeAdded ).append( System.lineSeparator() );
        }

        try {
            this.writeDatabase( databaseName, null, sb.toString(), false );
        } catch (ShellIOException e) {
            return null;
        }
        this.setMapInUse( databaseName ); //Update map in use

        return hexLine;
    }

    @Override
    public String getAllEntries(String databaseName, String password) throws ShellIOException {
        List<String> listDatabases = this.listDatabases();

        if (listDatabases.contains( databaseName ) == false) {
            return null;
        }

        List<String> input = this.readDatabaseAsList( databaseName );
        StringBuilder sb = new StringBuilder();

        for (String line : input) {
            String[] keyValue = line.split( Crypto.SEPARATOR );
            String decriptedvalue = null;
            try {
                decriptedvalue = Crypto.lineDecryption( password, keyValue[1] );
            } catch (Exception e) {
                return null;
            }

            //Check hash if lines have been changed
            String[] values = decriptedvalue.split( "=" );
            if (Crypto.checkSHA( keyValue[0], values[0] ) == false) {
                this.writeln( "Lines have been changed" );
                exit( 0 );
            }

            sb.append( decriptedvalue + "\n" );
        }

        return sb.toString();
    }


    @Override
    public List<String> listDatabases() {
        List<String> files = Stream.of( new File( DATABASES_PATH ).listFiles() )
                .filter( file -> !file.isDirectory() )
                .map( File::getName )
                .collect( Collectors.toList() );

        return files;
    }

    @Override
    public boolean writeDatabase(String databaseName, String password, String input, Boolean append) throws ShellIOException {
        Path path = Path.of( DATABASES_PATH + databaseName );

        if (append == false) { //Clear file
            try {
                new PrintWriter( path.toFile() ).close();
            } catch (FileNotFoundException e) {
                return false;
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter( path, StandardCharsets.UTF_8, StandardOpenOption.APPEND );) {
            writer.write( input );
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    @Override
    public String readDatabase(String databaseName) throws ShellIOException {
        try {
            String text = new String( Files.readAllBytes( Paths.get( "database" + "/" + databaseName ) ), StandardCharsets.UTF_8 );
            return text;
        } catch (IOException e) {
            this.writeln( "Error while reading database" );
        }

        return null;
    }

    @Override
    public List<String> readDatabaseAsList(String databaseName) throws ShellIOException {
        try {
            List<String> text = Files.readAllLines( Paths.get( "database" + "/" + databaseName ) );
            return text;
        } catch (IOException e) {
            this.writeln( "Error while reading database" );
        }

        return null;
    }


}
