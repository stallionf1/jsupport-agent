package com.jira;

import com.slaagent.services.DatabaseService;
import com.slaagent.services.FileService;
import com.slaagent.services.MailService;
import com.slaagent.services.PropertiesService;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

public class JiraNotifier {

    String DATE_DAILY_PATTERN = "dd-MM-yyyy", DATE_HOURLY_PATTERN = "dd-MM-yyyy HH:mm";
    LinkedList<String> supportTeamEmails, supportManagementEmails;

    public JiraNotifier() {
        Properties properties = PropertiesService.getInstance().getProperties();
        supportTeamEmails = new LinkedList<String>();
        supportManagementEmails = new LinkedList<String>();
        String teamEmails = properties.getProperty("sla.notification.regular").toString();
        String managementEmails = properties.getProperty("sla.notification.management").toString();
        StringTokenizer st1 = new StringTokenizer(teamEmails, ",");
        StringTokenizer st2 = new StringTokenizer(managementEmails, ",");
        while (st1.hasMoreElements()) {
            supportTeamEmails.add(st1.nextElement().toString());
        }
        while (st2.hasMoreElements()) {
            supportManagementEmails.add(st2.nextElement().toString());
        }
    }

    public void sendJiraQueryResult() {
        DatabaseService ds = DatabaseService.getInstance();
        Properties p = PropertiesService.getInstance().getProperties();
        String query = p.getProperty("agent.sql.custom-query");
        try {
            Statement statement;
            statement = ds.getConnection().createStatement();
            System.out.println(ds.getConnection());
            ResultSet rs = statement.executeQuery(query);

            StringBuffer sb = new StringBuffer();
            int count = 0;
            String htmlText = "<table border = \"0\">";
            String plainText = "";
            while (rs.next()) {
                String id = rs.getString("id");
                String pkey = rs.getString("pkey");
                String summary = rs.getString("summary");
                String issuestatus = rs.getString("issuestatus");
                String issuetype = rs.getString("issuetype"), htmlLine = "";
                
                htmlLine = "<tr><td>"+id+"</td><td>"+pkey+"</td><td width=\"18%\">"+summary+"</td><td>"+issuestatus+"</td><td>"+issuetype+"</td></tr>";
                htmlText += htmlLine;
                plainText  = plainText + id + " " + pkey + " " + summary + " " +  issuestatus + " " + issuetype + "\n";
            }
            //creating log file
            Date date = new Date();
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            
            //sending email
            MailService ms = MailService.getInstance();
            SimpleDateFormat format = new SimpleDateFormat(DATE_HOURLY_PATTERN);
            String subSubject = format.format(date).toString();
            String subject = subSubject + " Jira Report";
            
            FileService fs = new FileService();
            String fileName = "jira-jelastic.txt";
            fs.createFile(fileName, new StringBuffer(plainText));
            
            for (String mailToAddress : supportTeamEmails) {
                //ms.SendHTMLEmail(mailToAddress, subject, htmlText);
                //ms.sendHTMLAndAttachment(mailToAddress, subject, htmlText, p.getProperty("logs.files.path") +"jira-jelastic.txt", fileName);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            ds.closeConnection();
        }
    }
}
