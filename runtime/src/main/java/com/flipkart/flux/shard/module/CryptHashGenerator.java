package com.flipkart.flux.shard.module;

import java.security.MessageDigest;

/**
 * Created by amitkumar.o on 06/06/17.
 */
public class CryptHashGenerator {
    private static final String cryptHashAlgorithmPrefix = "SHA-256";
    public static Character getUniformCryptHash(String stateMachineId) {
        try {
            MessageDigest md = MessageDigest.getInstance(cryptHashAlgorithmPrefix);
            md.update(stateMachineId.getBytes());
            String cryptHash = javax.xml.bind.DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
            return cryptHash.charAt(0);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }
}
