package com.slaagent.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesService {

    private static PropertiesService ps = null;
    private static Properties properties;

    public PropertiesService() {
        loadProperties();
    }
    
    public static PropertiesService getInstance(){
        if(ps == null){
            ps = new PropertiesService();
        } 
        return ps;
    }
    

    public void loadProperties() {
        try {            
            String os = System.getProperty("os.name");
            String delimiter = "/";
            if (os.toLowerCase().contains("windows")) {
                delimiter = "\\";
            }
            String pathToFile = System.getProperty("user.home") +delimiter+"sla-agent.properties";
            System.out.println("path to .properties = "+pathToFile);
            properties = new Properties();
            properties.load(new FileInputStream(pathToFile));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Properties getProperties() {
        System.out.println("------------");
        System.out.println(properties);
        System.out.println("------------");
        return properties;
    }
}
