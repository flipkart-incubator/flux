package com.flipkart.flux.shard;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by amitkumar.o on 19/06/17.
 */
public class test_reading_from_string {
    public static void main(String args[]){
        String a = "{ \"masterSlavePairList\" : [ { \"master\":{ \"shardId\":1, \"ip\": \"127.0.0.1\", \"shardKeys\": [\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"a\",\"b\",\"c\",\"d\",\"e\",\"f\"] },\"slave\":{ \"shardId\":2, \"ip\":\"127.0.0.1\", \"shardKeys\": [\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"a\",\"b\",\"c\",\"d\",\"e\",\"f\"] }}  ]  }" ;
        System.out.println();
        ObjectMapper objectMapper = new ObjectMapper();
        MasterSlavePairList yo = null;
        try {
            yo = objectMapper.readValue(a, MasterSlavePairList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        yo.getMasterSlavePairList().forEach( masterSlavePair -> {
            System.out.println(masterSlavePair.toString());
        });
    }
}
