package com.slaagent.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseService {

    private static DatabaseService ds = null;
    private Connection connection;
    private static Properties properties = PropertiesService.getInstance().getProperties();

    public DatabaseService() {
        createConnection();
    }   
    
    public static DatabaseService getInstance() {
        if (ds == null) {
            ds = new DatabaseService();
        }
        return ds;
    }

    public void createConnection() {
        try {
            String url = properties.getProperty("database.url").toString();
            String username = properties.getProperty("database.username").toString();
            String password = properties.getProperty("database.password").toString();
            String driver = properties.getProperty("database.driver").toString();

            System.out.println("url: " + url + "\nusername: " 
                                + username + "\npassword: " + 
                                    password + "\ndriver: " + driver);

            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);

        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}