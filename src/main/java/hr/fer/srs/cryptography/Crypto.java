package hr.fer.srs.cryptography;

import hr.fer.srs.strategy.Environment;
import hr.fer.srs.strategy.ShellIOException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.List;


public class Crypto {

    private final static int GCM_IV_LENGTH = 12;

    public static final int SALT_LENGTH = 16;
    private final static int AUTH_TAG = 128;

    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final String SECRET_KEY_SPEC_ALGORITHM = "AES";

    private static final int AES_KEY_LENGTH = 256;

    private static final int ITERATION_COUNT = 65536;

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    public static final String SEPARATOR = "####";

    private final static SecureRandom secureRandom = new SecureRandom();

    public static byte[] generateRandom(int length) {
        byte[] iv = new byte[length];
        secureRandom.nextBytes( iv );
        return iv;
    }

    private static byte[] encrypt(String plaintext, SecretKey secretKey) throws Exception {
        byte[] iv = generateRandom( GCM_IV_LENGTH );
        final Cipher cipher = Cipher.getInstance( CIPHER_ALGORITHM );
        GCMParameterSpec parameterSpec = new GCMParameterSpec( AUTH_TAG, iv );
        cipher.init( Cipher.ENCRYPT_MODE, secretKey, parameterSpec );

        byte[] cipherText = cipher.doFinal( plaintext.getBytes( StandardCharsets.UTF_8 ) ); //perform the encryption or decryption operation

        ByteBuffer byteBuffer = ByteBuffer.allocate( iv.length + cipherText.length );
        byteBuffer.put( iv );
        byteBuffer.put( cipherText );
        return byteBuffer.array();
    }

    private static String decrypt(byte[] cipherMessage, SecretKey secretKey) throws Exception {
        final Cipher cipher = Cipher.getInstance( CIPHER_ALGORITHM );
        //use first 12 bytes for iv
        AlgorithmParameterSpec gcmIv = new GCMParameterSpec( 128, cipherMessage, 0, GCM_IV_LENGTH );
        cipher.init( Cipher.DECRYPT_MODE, secretKey, gcmIv );

        //use everything from 12 bytes on as ciphertext
        byte[] plainText = cipher.doFinal( cipherMessage, GCM_IV_LENGTH, cipherMessage.length - GCM_IV_LENGTH );

        return new String( plainText, StandardCharsets.UTF_8 );
    }

    private static SecretKey getKeyFromPassword(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance( SECRET_KEY_FACTORY_ALGORITHM );
        KeySpec spec = new PBEKeySpec( password.toCharArray(), salt, ITERATION_COUNT, AES_KEY_LENGTH );
        SecretKey originalKey = new SecretKeySpec( factory.generateSecret( spec ).getEncoded(), SECRET_KEY_SPEC_ALGORITHM );
        return originalKey;
    }

    public static String lineDecryption(String masterPasswordRaw, String line) throws Exception {

        byte[] lineEncription = Base64.getDecoder().decode(line);

        //salt is first 16 bytes
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy( lineEncription, 0, salt, 0, SALT_LENGTH );

        //iv and ciphertext is everything after salt
        byte[] IVAndCiphertext = new byte[lineEncription.length - SALT_LENGTH];
        System.arraycopy( lineEncription, SALT_LENGTH, IVAndCiphertext, 0, lineEncription.length - SALT_LENGTH );

        //decrypt
        return decrypt( IVAndCiphertext, Crypto.getKeyFromPassword( masterPasswordRaw, salt ) );
    }

    private static byte[] executeEncription(String masterPasswordRaw, String inputText) throws Exception {
        byte[] salt = generateRandom( SALT_LENGTH );
        SecretKey secretKey = getKeyFromPassword( masterPasswordRaw, salt );
        byte[] encrypt = Crypto.encrypt( inputText, secretKey );

        ByteBuffer byteBuffer = ByteBuffer.allocate( salt.length + encrypt.length );
        byteBuffer.put( salt );
        byteBuffer.put( encrypt );

        byte[] data = byteBuffer.array();
        return data;
    }

    public static String prepareLineForDatabase(String password, String key, String value){
        byte[] initTextBytes;
        MessageDigest digest;
        String inputForEncryption = key + "=" + value;
        try {
            initTextBytes = Crypto.executeEncription( password, inputForEncryption );
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        byte[] EncodedHashKey = digest.digest(key.getBytes(StandardCharsets.UTF_8));
        //SHA-256, used for faster searching user key in database for password

        StringBuilder sb = new StringBuilder();
        sb.append( Base64.getEncoder().encodeToString( EncodedHashKey ) );
        sb.append( Crypto.SEPARATOR );
        sb.append( Base64.getEncoder().encodeToString( initTextBytes ) );
        sb.append( System.lineSeparator() );

        return sb.toString();
    }

    public static String prepareLineAutentificator( String key, String value){
        MessageDigest digest;
        String inputForEncryption = key + "=" + value;
        byte[] salt = generateRandom( SALT_LENGTH );
        byte[] encrypt;

        try {
            PBEKeySpec spec = new PBEKeySpec( inputForEncryption.toCharArray(), salt, ITERATION_COUNT, AES_KEY_LENGTH);
            encrypt  = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM).generateSecret(spec).getEncoded();
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
        
        ByteBuffer byteBuffer = ByteBuffer.allocate( salt.length + encrypt.length );
        byteBuffer.put( salt );
        byteBuffer.put( encrypt );
        byte[] data = byteBuffer.array();
        byte[] EncodedHashKey = digest.digest(key.getBytes(StandardCharsets.UTF_8));


        StringBuilder sb = new StringBuilder();
        sb.append( Base64.getEncoder().encodeToString( EncodedHashKey ) );
        sb.append( Crypto.SEPARATOR );
        sb.append( Base64.getEncoder().encodeToString( data ) );
        sb.append( System.lineSeparator() );

        return sb.toString();
    }

    public static boolean checkAuthorization(String key, String value, String line){
        line = line.trim();

        //Given line
        byte[] lineEncription = Base64.getDecoder().decode(line);
        //salt is first 16 bytes
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy( lineEncription, 0, salt, 0, SALT_LENGTH );


        //Calculated with given salt
        byte[] encrypt;
        String inputForEncryption = key + "=" + value;
        try {
            PBEKeySpec spec = new PBEKeySpec( inputForEncryption.toCharArray(), salt, ITERATION_COUNT, AES_KEY_LENGTH);
            encrypt  = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return false;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate( salt.length + encrypt.length );
        byteBuffer.put( salt );
        byteBuffer.put( encrypt );
        byte[] data = byteBuffer.array();
        String calculated = Base64.getEncoder().encodeToString( data );

        if (calculated.equals(line)){
            return true;
        }

        return false;
    }

    public static boolean checkPassword(Environment env, String databaseName, String password) throws ShellIOException {
        try {
            List<String> databaseContent = env.readDatabaseAsList( databaseName);
            String line = databaseContent.get(0);
            String[] encryptedPassword = line.split(Crypto.SEPARATOR);
            String decriptedvalue = Crypto.lineDecryption(password, encryptedPassword[1]);
        } catch (Exception e) {
            env.writeln( "Wrong password for database: " + databaseName );
            return false;
        }
        return true;
    }


    public static boolean checkSHA(String inputFromDB, String calculate){
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            return false;
        }
        calculate = Base64.getEncoder().encodeToString(digest.digest(calculate.getBytes(StandardCharsets.UTF_8)));

        if(calculate.equals(inputFromDB)){
            return true;
        }

        return false;
    }


}



