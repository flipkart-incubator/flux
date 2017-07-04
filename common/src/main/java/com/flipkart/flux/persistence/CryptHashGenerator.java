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
