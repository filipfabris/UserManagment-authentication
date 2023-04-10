package hr.fer.srs;

import hr.fer.srs.cryptography.Crypto;

import java.io.Console;
import java.util.Scanner;

import static hr.fer.srs.cryptography.Crypto.checkAuthorization;
import static hr.fer.srs.cryptography.Crypto.prepareLineAutentificator;

public class Test {

    public static void main(String[] args) {

        String username = "ff";
        String password = "abc123";

        String output = prepareLineAutentificator(username, password);

        System.out.println(output);


        String line = output.split( Crypto.SEPARATOR )[1];

        System.out.println(line);

        line = line.trim();
        System.out.println(checkAuthorization(username, password, line));




        String proba = "DQ5znwZzGw0mXSUeA40oxsz9mCscJKS6TQPOA1Di0aU=####/wsk8o/+WXwSfTZotcMoHWUH7BfoIpNaESsZ2wkVvcX+jwsq94UlMhUeP5FQo2Hd\n";
        String encrypted = proba.split( Crypto.SEPARATOR )[1];


        System.out.println(checkAuthorization( "ff&##&false", "abc123", encrypted ));



//        ePqzBfbGRrOFqkMlz9gdxh2xQiP1E62UtykBAj/91COJkSGpZqARGSKHnV8yCYP5
//        ePqzBfbGRrOFqkMlz9gdxh2xQiP1E62UtykBAj/91COJkSGpZqARGSKHnV8yCYP5
//
//        DQ5znwZzGw0mXSUeA40oxsz9mCscJKS6TQPOA1Di0aU=####ePqzBfbGRrOFqkMlz9gdxh2xQiP1E62UtykBAj/91COJkSGpZqARGSKHnV8yCYP5
//        d5AX6J7qDtsFoHdapOwO+wTxozy0k+67QEv0yIVrxWY=####ePqzBfbGRrOFqkMlz9gdxh2xQiP1E62UtykBAj/91COJkSGpZqARGSKHnV8yCYP5




    }
}
