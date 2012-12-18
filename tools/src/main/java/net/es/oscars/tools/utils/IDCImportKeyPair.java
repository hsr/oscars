/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.oscars.tools.utils;

import java.security.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.security.spec.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.KeyStore.*;
import java.util.Collection;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 *
 * @author Xi Yang
 * 
 * Disclaimer: part of code is borrowed from ImportKey.java 1.1 
 * by Joachim Karrer and Jens Carlberg.
 * 
 */
public class IDCImportKeyPair {
    
    private static String keypass = "changeit";
    private static String alias = "imported_key";
    private static String keystorename = null;
    private static String keyfile = null;
    private static String certfile = null;
    private static boolean override = false;

    private static InputStream fullStream ( String fname ) throws IOException {
        FileInputStream fis = new FileInputStream(fname);
        DataInputStream dis = new DataInputStream(fis);
        byte[] bytes = new byte[dis.available()];
        dis.readFully(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return bais;
    }

    /**
     * 
     * @param args: -s key_store -k private_key -c signed_cert -a alias_in_store
     *       -p store_and_key_pass  -O (override existing key) -h (help)
     */

    public static void main ( String args[]) {
        try {
            parseArgs(args);

            // loading keystore 
            KeyStore ks = KeyStore.getInstance("JKS", "SUN");
            FileInputStream ksFileIn = new FileInputStream(keystorename);
            ks.load(ksFileIn, keypass.toCharArray());
            System.out.println("Using keystore-file : "+keystorename);
            ks.store(new FileOutputStream ( keystorename  ),
                    keypass.toCharArray());
            ks.load(new FileInputStream ( keystorename ),
                    keypass.toCharArray());

            // checking / overriding existing alias
            PasswordProtection pp = new PasswordProtection(keypass.toCharArray());
            Entry ke = ks.getEntry(alias, pp);
            if (ke != null) {
                if (override) {
                    ks.deleteEntry(alias);
                } else {
                    System.err.println("KeyEntry: "+keypass+" has already existed."
                         + "\n ==> With caution: use -O option to override.");
                    System.exit(-1);
                }
            }

            // loading Key
            InputStream fl = fullStream (keyfile);
            byte[] key = new byte[fl.available()];
            KeyFactory kf = KeyFactory.getInstance("RSA");
            fl.read ( key, 0, fl.available() );
            fl.close();
            PKCS8EncodedKeySpec keysp = new PKCS8EncodedKeySpec ( key );
            PrivateKey ff = kf.generatePrivate (keysp);

            // loading CertificateChain
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream certstream = fullStream (certfile);

            Collection c = cf.generateCertificates(certstream) ;
            Certificate[] certs = new Certificate[c.toArray().length];

            if (c.size() == 1) {
                certstream = fullStream (certfile);
                System.out.println("One certificate, no chain.");
                Certificate cert = cf.generateCertificate(certstream) ;
                certs[0] = cert;
            } else {
                System.out.println("Certificate chain length: "+c.size());
                certs = (Certificate[])c.toArray();
            }

            // storing keystore
            ks.setKeyEntry(alias, ff, 
                           keypass.toCharArray(),
                           certs );
            ks.store(new FileOutputStream ( keystorename ),
                     keypass.toCharArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        System.out.println ("SUCCESS: Key and certificate imported.");
    }

    public static void parseArgs(String args[])  throws IOException {
        OptionParser parser = new OptionParser("s:k:c:a:p:hO");
        OptionSet options = parser.parse( args );

        if (options.has( "h" )) {
            System.out.println("Args: -s key_store -k private_key -c signed_cert "
                + "-a alias_in_store -p store_and_key_pass -h (help)"
                + "-O (override existing key)");
            System.exit(0);
        }
        if (options.has("O") ){
            override = true;
        }
        if (options.has("s")) {
            keystorename = (String) options.valueOf("s");
        } else if (keystorename == null) {
            throw new IOException("No key_store argument!");
        }
        if (options.has("k")) {
            keyfile = (String) options.valueOf("k");
        } else {
            throw new IOException("No private_key_file argument!");
        }
        if (options.has("c")) {
            certfile = (String) options.valueOf("c");
        } else {
            throw new IOException("No signed_cert_file argument!");
        }
        if (options.has("a")) {
            alias = (String) options.valueOf("a");
        }
        if (options.has("p")) {
            keypass = (String) options.valueOf("p");
        }
    }
}
