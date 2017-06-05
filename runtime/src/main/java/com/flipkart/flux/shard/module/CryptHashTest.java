package com.flipkart.flux.shard.module;

import java.security.MessageDigest;
import java.util.UUID;
import org.hibernate.Session
/**
 * Created by amitkumar.o on 31/05/17.
 */
public class CryptHashTest {

    public static void main(String args[]) {
        int cnt[] = new int[150] ;

        for (int i = 0; i <= 1000000; i++) {
            String test = "sfs-cor-" + i;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(test.getBytes());
                byte hash[] = md.digest();
                String code = javax.xml.bind.DatatypeConverter.printHexBinary(hash).toLowerCase() ;
                cnt[(int)(code.charAt(0))]++;
                //System.out.println(test + " " + javax.xml.bind.DatatypeConverter.printHexBinary(hash).toLowerCase());
            }
            catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }
        for(int i = 0 ; i < 150 ; i++)
            if( (i >=48 && i <= 57) || ( i >= 97 && i <= 102) )
            System.out.println( i + " " + cnt[i] );
    }
}
