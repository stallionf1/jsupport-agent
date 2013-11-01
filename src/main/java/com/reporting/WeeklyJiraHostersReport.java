package com.reporting;

import java.sql.SQLException;
import java.sql.Statement;
import com.slaagent.services.DatabaseService;
import com.slaagent.services.FileService;
import com.slaagent.services.MailService;
import com.slaagent.services.PropertiesService;
import java.io.File;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeeklyJiraHostersReport {

    private static Properties properties = PropertiesService.getInstance().getProperties();
    private static MailService ms = MailService.getInstance();
    private LinkedList<String> recipients;

    public WeeklyJiraHostersReport() {
        recipients = new LinkedList<String>();
        String emails = properties.getProperty("recipients.emails").toString();
        StringTokenizer st1 = new StringTokenizer(emails, ",");
        while (st1.hasMoreElements()) {
            recipients.add(st1.nextElement().toString());
        }
    }
    
    public void sendFridaysReport() {
        String pathToReports = properties.getProperty("reports.location");
        String messageBody = "Hello Dmitry, <br/>This is a weekly emails which contains: <b>JIRA ticket reports by hoster</b> <br/ >Please see .html files in the attachment.";
        Calendar sDateCalendar = new GregorianCalendar();
        int weekNumber = sDateCalendar.get(Calendar.WEEK_OF_YEAR);

        try {
            LinkedList<File> files = new LinkedList<File>();
            DatabaseService ds = DatabaseService.getInstance();
            Statement statement;
            statement = ds.getConnection().createStatement();
            System.out.println(ds.getConnection());
            ResultSet rs = statement.executeQuery("SELECT filter_url, email_to, name FROM reportinfo INNER JOIN hosters ON reportinfo.hoster_id=hosters.id");

            while (rs.next()) {
                String filterUrl = rs.getString(1);
                System.out.println("filetr url: "+filterUrl);
                String emailTo = rs.getString(2);
                String hosterName = rs.getString(3);
                FileService fs = new FileService();
                String fileName = hosterName + "-week-" + weekNumber + ".html";
                String gzippedFileName = fileName + ".zip";
                fs.downloadFileByURL(filterUrl, pathToReports, fileName);
                
                //file compressing
                fs.compressFile(pathToReports+fileName, pathToReports+gzippedFileName);
                files.add(new File(pathToReports+gzippedFileName));
            }

             for(String email : recipients){
                 ms.sendHTMLAndAttachment(email, "JIRA tickets report: by hoster [Week " + weekNumber + "]", messageBody, files);
             }
             
        } catch (SQLException ex) {
            Logger.getLogger(WeeklyJiraHostersReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        new WeeklyJiraHostersReport().sendFridaysReport();
    }
}