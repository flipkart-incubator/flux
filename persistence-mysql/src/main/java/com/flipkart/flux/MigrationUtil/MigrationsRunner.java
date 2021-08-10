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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * <code>MigrationsRunner</code> performs mysql migration using liquibase.
 * To perform migration run this class with "migrate" parameter.
 *
 * @author shyam.akirala
 */
@Singleton
public class MigrationsRunner {

    @Inject
    private YamlConfiguration yamlConfiguration;

    public void migrate(String dbName) {
        try {
            Configuration configuration = yamlConfiguration.subset(dbName + ".Hibernate");
            Properties properties = new Properties();
            properties.put("user", configuration.getProperty("hibernate.connection.username"));
            properties.put("password", configuration.getProperty("hibernate.connection.password"));
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url = (String) configuration.getProperty("hibernate.connection.url");
            java.sql.Connection connection = DriverManager.getConnection(url, properties);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(dbName + "/migrations.xml").getFile());
            Liquibase liquibase = new Liquibase(file.getCanonicalPath(), new FileSystemResourceAccessor(), database);
            liquibase.update(new Contexts());
        } catch (Exception e) {
            System.err.println("Unable to perform database migration.");
            e.printStackTrace();
        }
    }
}