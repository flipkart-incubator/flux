/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.persistence;


import java.security.MessageDigest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generates a SHA-256 Hex Code of a given String and returns first two characters as shard String
 * @author amitkumar.o
 */
public class CryptHashGenerator {

    private static final String cryptHashAlgorithmPrefix = "SHA-256";
    private static final Logger logger = LogManager.getLogger(CryptHashGenerator.class);


    public static String getUniformCryptHash(String stateMachineId) {
        try {
            MessageDigest md = MessageDigest.getInstance(cryptHashAlgorithmPrefix);
            md.update(stateMachineId.getBytes());
            String cryptHash = javax.xml.bind.DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
            return cryptHash.substring(0,2);
        } catch (Exception ex) {
            logger.error("Unable to generate Hash for the given stateMachine Id {} {}", stateMachineId, ex.getStackTrace());
            throw new RuntimeException("Exception in generating SHA-256 for the given key : " + stateMachineId);
        }
    }
}
