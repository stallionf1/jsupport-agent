package com.reporting;

import com.slaagent.services.FileService;
import com.slaagent.services.MailService;
import com.slaagent.services.PropertiesService;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeeklySupportReport {

    private static Properties properties = PropertiesService.getInstance().getProperties();
    private static MailService ms = MailService.getInstance();
    private LinkedList<String> recipients;

    public WeeklySupportReport() {
        recipients = new LinkedList<String>();
        String recipientsEmails = properties.getProperty("recipients.emails").toString();
        StringTokenizer st1 = new StringTokenizer(recipientsEmails, ",");
        while (st1.hasMoreElements()) {
            recipients.add(st1.nextElement().toString());
        }
    }
    
    public void sendReport(){
        try {
            Calendar sDateCalendar = new GregorianCalendar();
            int weekNumber = sDateCalendar.get(Calendar.WEEK_OF_YEAR);
            
            FileService fs = new FileService();
            String message = fs.readFile(properties.getProperty("email.template.location")+"SupportWeeklyReport.html");
            System.out.println(message);
            
            for(int i = 0;i<recipients.size();i++){
                System.out.println(recipients.get(i));
                ms.SendHTMLEmail(recipients.get(i), "Support-Weekly-General-Report. Week: "+weekNumber, message);
            }
        } catch (IOException ex) {
            Logger.getLogger(WeeklySupportReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
        new WeeklySupportReport().sendReport();
    }
    
}
