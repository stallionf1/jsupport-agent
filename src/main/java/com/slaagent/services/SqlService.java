package com.slaagent.services;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqlService {

    public void executeCustomQuery() {
        try {

            Properties properties = PropertiesService.getInstance().getProperties();
            DatabaseService ds = new DatabaseService();
            Statement statement;
            statement = ds.getConnection().createStatement();
            System.out.println(properties.getProperty("agent.sql.custom-query"));
            ResultSet rs = statement.executeQuery(properties.getProperty("agent.sql.custom-query"));
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount(), count = 1;
            StringBuffer sb = new StringBuffer();

            while (rs.next()) {
                String s = "";
                for (int i = 1; i < numColumns + 1; i++) {
                    if (i == 1) {
                        s = rs.getString(i);
                    } else {
                        s = " " + rs.getString(i);
                    }
                }
                if(count == 1){
                    sb.append(s);
                } else {
                    sb.append("\n"+s);
                }
                count ++;
            }

            Date date = new Date();
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            String fileName = f.format(date) + ".custom-sql-query.log";
            FileService fs = new FileService();
            fs.createFile(fileName, sb);

        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(SqlService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
