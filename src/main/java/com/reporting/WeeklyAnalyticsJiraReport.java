
package com.reporting;

/**
 *
 * @author al
 */
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

public class WeeklyAnalyticsJiraReport {

    private static Properties properties = PropertiesService.getInstance().getProperties();
    private static MailService ms = MailService.getInstance();
    private LinkedList<String> recipients;

    public WeeklyAnalyticsJiraReport() {
        recipients = new LinkedList<String>();
        String emails = properties.getProperty("recepients.maks.jira.resport").toString();
        StringTokenizer st1 = new StringTokenizer(emails, ",");
        while (st1.hasMoreElements()) {
            recipients.add(st1.nextElement().toString());
        }
    }
    
    public void sendFridaysReport() {
        String pathToReports = properties.getProperty("reports.location");
        String messageBody = "Hi all. <br/ ><br/ >Attached are reports regarding JIRA tickets opened on behalf of our customers. <br/ >"
                + "These include both: open and closed tickets grouped by the type of users reported the issue (trial/paid) <br /><br/ > Thanks,<br />Maksym Polevchuk";
        Calendar sDateCalendar = new GregorianCalendar();
        int weekNumber = sDateCalendar.get(Calendar.WEEK_OF_YEAR);

        try {
            LinkedList<File> files = new LinkedList<File>();
            DatabaseService ds = DatabaseService.getInstance();
            Statement statement;
            statement = ds.getConnection().createStatement();
            System.out.println(ds.getConnection());
            ResultSet rs = statement.executeQuery("SELECT * FROM analytics_weekly_report");

            while (rs.next()) {
                String reportName = rs.getString(2);
                System.out.println("report name: "+reportName);
                String reportUrl = rs.getString(3);
                System.out.println("report url: " + reportUrl);
                //String hosterName = rs.getString(3);
                FileService fs = new FileService();
                String fileName = reportName + "-week-" + weekNumber + ".html";
                fs.downloadFileByURL(reportUrl, pathToReports, fileName);
                
                files.add(new File(pathToReports+fileName));
            }

             for(String email : recipients){
                 ms.sendHTMLAndAttachment(email, "JIRA tickets opened on behalf of end-sers (customers feedback) [Week " + weekNumber + "]", messageBody, files);
             }
             
        } catch (SQLException ex) {
            Logger.getLogger(WeeklyJiraHostersReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        new WeeklyAnalyticsJiraReport().sendFridaysReport();
    }
}
