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
package com.flipkart.flux.MigrationUtil;

import com.flipkart.polyguice.config.YamlConfiguration;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import org.apache.commons.configuration.Configuration;

import java.sql.DriverManager;
import java.util.Properties;

/**
 * <code>MigrationsRunner</code> performs mysql migration using liquibase.
 * To perform migration run this class with "migrate" parameter.
 * @author shyam.akirala
 */
public class MigrationsRunner {

    public static void main(String[] args) {

        if(args != null && args[0].equals("migrate")) {

            try {
                YamlConfiguration yamlConfiguration = new YamlConfiguration("runtime/src/main/resources/packaged/configuration.yml");
                Configuration configuration = yamlConfiguration.subset("Hibernate");
                Properties properties = new Properties();
                properties.put("user", configuration.getProperty("hibernate.connection.username"));
                properties.put("password", configuration.getProperty("hibernate.connection.password"));
                String url = (String) configuration.getProperty("hibernate.connection.url");
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                java.sql.Connection connection = DriverManager.getConnection(url, properties);
                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                Liquibase liquibase = new Liquibase("persistence-mysql/src/main/resources/migrations.xml", new FileSystemResourceAccessor(), database);
                liquibase.update(new Contexts());
            } catch (Exception e) {
                System.err.println("Unable to perform database migration.");
                e.printStackTrace();
            }
        }
    }
}