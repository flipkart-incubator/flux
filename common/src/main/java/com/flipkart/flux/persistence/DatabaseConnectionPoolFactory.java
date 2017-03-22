package com.flipkart.flux.persistence;

import jdk.nashorn.internal.objects.annotations.Getter;
import org.skife.jdbi.v2.DBI;

import java.sql.Connection;

/**
 * Obtain connections for DBI handles
 * @author amitkumar.o
 */

public class DatabaseConnectionPoolFactory {
    private DBI dbi;

    public DatabaseConnectionPoolFactory(DBI dbi){
        dbi = dbi;
    }

    public Connection getDatabaseConnection() {
        return dbi.open().getConnection();
    }
}
