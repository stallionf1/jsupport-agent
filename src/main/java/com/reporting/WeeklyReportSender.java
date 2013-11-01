package com.reporting;

import com.slaagent.services.MailService;
import com.slaagent.services.PropertiesService;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

public class WeeklyReportSender {
    
    private static Properties properties = PropertiesService.getInstance().getProperties();
    private static MailService ms = MailService.getInstance();
    private LinkedList<String> recipients;

    public WeeklyReportSender() {
        recipients = new LinkedList<String>();
        String recipientsEmails = properties.getProperty("recipients.emails").toString();
        StringTokenizer st1 = new StringTokenizer(recipientsEmails, ",");
        while (st1.hasMoreElements()) {
            recipients.add(st1.nextElement().toString());
        }
    }
       
    
    public void sendReport(){
        Calendar sDateCalendar = new GregorianCalendar();
        int weekNumber = sDateCalendar.get(Calendar.WEEK_OF_YEAR);
        
        String body = "<html><head><title>support-weekly-report</title></head><body><p>Dear all, <br/> <br/> This report shows the weekly differneces between viewing Knowledge Base articles, Hosters' documentations and opened tickets in Zendesk.<br/>"
                + " To view this report, please click <a href='https://docs.google.com/a/hivext.net/"
                + "spreadsheet/ccc?key=0AlVetYyDSe95dE9adExTdUlvSi1DaEh5ZVRXSENnYlE#gid=1'>here</a></p><br/><br/><br/></body></html>";    
        
        for(int i = 0;i<recipients.size();i++){
            System.out.println(recipients.get(i));
            ms.SendHTMLEmail(recipients.get(i), "Support-Weekly-Report. Week: "+weekNumber+" / KB and hoster-docs productivity", body);
        }
        
    }
    
    public static void main(String[] args) {
        
        new WeeklyReportSender().sendReport();
    }
    
}