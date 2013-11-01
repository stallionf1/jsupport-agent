package com.slaagent.services;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

public class SlaService {

    LinkedList<String> supportTeamEmails, supportManagementEmails;
    String DATE_DAILY_PATTERN = "dd-MM-yyyy", DATE_HOURLY_PATTERN = "dd-MM-yyyy HH:mm";
    
    public SlaService() {
        Properties properties = PropertiesService.getInstance().getProperties();
        supportTeamEmails  = new LinkedList<String>();
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
    
    public void getAllUsolvedTopicsWithoutJAnswer() {
            DatabaseService ds = DatabaseService.getInstance();
        try {

            Statement statement;
            statement = ds.getConnection().createStatement();
            System.out.println(ds.getConnection());
            ResultSet rs = statement.executeQuery("SELECT * FROM community.ipbtopics WHERE "
                    + "ipbtopics.state NOT LIKE 'closed' AND ipbtopics.last_poster_id "
                    + "NOT IN (select member_id from community.ipbmembers where member_group_id "
                    + "IN (4,5,6,7,9))");

            StringBuffer sb = new StringBuffer();
            int count = 0;
            while (rs.next()) {

                String line = "";

                String topic_id = rs.getString("tid");
                String topic_status = rs.getString("state");
                String topic_name = rs.getString("title");
                String topic_starter_name = rs.getString("starter_name");

                if (count == 0) {
                    sb.append(topic_id + "\t" + topic_status + "\t" + topic_name + "\t" + topic_starter_name);
                } else {
                    sb.append("\n" + line + topic_id + "\t" + topic_status + "\t" + topic_name + "\t" + topic_starter_name);
                }
                count++;
            }

            //creating log file
            Date date = new Date();
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            String fileName = f.format(date) + ".log";
            FileService fs = new FileService();
            fs.createFile(fileName, sb);

            // Sending by email
            // setting suject
            SimpleDateFormat format = new SimpleDateFormat(DATE_DAILY_PATTERN);
            String subSubject = format.format(date).toString();
            String subject = ""+subSubject+" Community \\ SLA Notification \\ All Unanswered Topics";
            MailService ms = MailService.getInstance();
            System.out.println("ms = "+ms);
            for (String mailToAddress : supportTeamEmails) {
                System.out.println(subject);
                ms.sendPlainEmail(mailToAddress, subject, sb.toString());
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        finally {
            ds.closeConnection();
        }
    }

    public void getAllOpenedTopicsCurrentSlaStatus() {
            DatabaseService ds = DatabaseService.getInstance();
        try {
            Statement statement;
            statement = ds.getConnection().createStatement();
            System.out.println(ds.getConnection());
            ResultSet rs = statement.executeQuery("SELECT * FROM community.ipbtopics WHERE "
                    + "ipbtopics.state NOT LIKE 'closed' AND ipbtopics.last_poster_id "
                    + "NOT IN (select member_id from community.ipbmembers where member_group_id "
                    + "IN (4,5,6,7,9))");

            StringBuffer sb = new StringBuffer();
            int count = 0;

            while (rs.next()) {
                String topic_id = rs.getString("tid");
                String link = "http://community.jelastic.com/index.php/topic/"+topic_id + "-"+rs.getString("title_seo");
                String topic_status = rs.getString("state");
                String topic_name = rs.getString("title");
                String topic_starter_name = rs.getString("starter_name"), line = "";
                Long l = rs.getLong("last_post");
                int hours = getTimeDiffWithCurrentTime(l);

               if (count == 0) {
                    sb.append("id: " + topic_id + "\n"+"link: " + link + "\n" + "status: " + topic_status + "\nname: " + topic_name + "\nstarter: " + topic_starter_name
                            + "\nhours since last post: " + hours + "\nSLA | Yellow: " + isYellow(hours) + "\nSLA | RED: " + isRed(hours));
                    sb.append("\n---------------------------");
                } else {
                    sb.append("\nid: " + topic_id + "\n" +"link: " + link + "\n" + "status: " + topic_status + "\nname: " + topic_name + "\nstarter: " + topic_starter_name
                            + "\nhours since last post: " + hours + "\nSLA | Yellow: " + isYellow(hours) + "\nSLA | RED: " + isRed(hours));
                    sb.append("\n---------------------------");
                }
                count++;
            }

            //creating log file
            Date date = new Date();
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            String fileName = f.format(date) + ".sla.log";
            FileService fs = new FileService();
            fs.createFile(fileName, sb);

            //sending email
            MailService ms = MailService.getInstance();
            SimpleDateFormat format = new SimpleDateFormat(DATE_HOURLY_PATTERN);
            String subSubject = format.format(date).toString();
            String subject = subSubject + " Community \\ Hourly SLA Notification";
            for (String mailToAddress : supportTeamEmails) {
                ms.sendPlainEmail(mailToAddress, subject, sb.toString());
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            ds.closeConnection();
        }
    }

    public void getOpenedTopicsLastCommentPartner() {
            DatabaseService ds = DatabaseService.getInstance();
        try {
            Statement statement;
            statement = ds.getConnection().createStatement();
            System.out.println(ds.getConnection());
            ResultSet rs = statement.executeQuery("SELECT * FROM community.ipbtopics WHERE "
                    + "ipbtopics.state NOT LIKE 'closed' AND ipbtopics.last_poster_id "
                    + "IN (select member_id from community.ipbmembers where member_group_id "
                    + "= 9)");

            StringBuffer sb = new StringBuffer();
            int count = 0;

            while (rs.next()) {
                String topic_id = rs.getString("tid");
                String link = "http://community.jelastic.com/index.php/topic/"+topic_id + "-"+rs.getString("title_seo");
                String topic_status = rs.getString("state");
                String topic_name = rs.getString("title");
                String last_poster_name = rs.getString("last_poster_name");
                String topic_starter_name = rs.getString("starter_name"), line = "";
                Long l = rs.getLong("last_post");
                int hours = getTimeDiffWithCurrentTime(l);

               if (count == 0) {
                    sb.append("id: " + topic_id + "\n"+"link: " + link + "\n" + "status: " + topic_status + "\nname: " + topic_name + "\nstarter: " + topic_starter_name
                            + "\nlast poster name: " + last_poster_name + "\nhours since last post: " + hours + "\nSLA | Yellow: " + isYellow(hours) + "\nSLA | RED: " + isRed(hours));
                    sb.append("\n---------------------------");
                } else {
                    sb.append("\nid: " + topic_id + "\n" +"link: " + link + "\n" + "status: " + topic_status + "\nname: " + topic_name + "\nstarter: " + topic_starter_name
                            + "\nlast poster name: " + last_poster_name +" \nhours since last post: " + hours + "\nSLA | Yellow: " + isYellow(hours) + "\nSLA | RED: " + isRed(hours));
                    sb.append("\n---------------------------");
                }
                count++;
            }

            //creating log file
            Date date = new Date();
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            String fileName = f.format(date) + ".last-comment-partner.sla.log";
            FileService fs = new FileService();
            fs.createFile(fileName, sb);

            //sending email
            MailService ms = MailService.getInstance();
            SimpleDateFormat format = new SimpleDateFormat(DATE_HOURLY_PATTERN);
            String subSubject = format.format(date).toString();
            String subject = subSubject + " Community \\ Partners activity \\ Notification";
            for (String mailToAddress : supportTeamEmails) {
                ms.sendPlainEmail(mailToAddress, subject, sb.toString());
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            ds.closeConnection();
        }
    }
    
    public void getTopicsInRed() {
            DatabaseService ds = DatabaseService.getInstance();
        try {
            Statement statement;
            statement = ds.getConnection().createStatement();
            System.out.println(ds.getConnection());
            ResultSet rs = statement.executeQuery("SELECT * FROM community.ipbtopics WHERE "
                    + "ipbtopics.state NOT LIKE 'closed' AND ipbtopics.last_poster_id "
                    + "NOT IN (select member_id from community.ipbmembers where member_group_id "
                    + "IN (4,5,6,7,9))");

            StringBuffer sb = new StringBuffer();
            int count = 0;

            while (rs.next()) {
                String topic_id = rs.getString("tid");
                String topic_status = rs.getString("state");
                String topic_name = rs.getString("title");
                String topic_starter_name = rs.getString("starter_name"), line = "";
                Long l = rs.getLong("last_post");
                int hours = getTimeDiffWithCurrentTime(l);

                System.out.println("----------------------");
                if (isRed(hours)) {
                    System.out.println("RED exists");
                    if (count == 0) {
                        sb.append("id: " + topic_id + "\n" + "status: " + topic_status + "\nname: " + topic_name + "\nstarter: " + topic_starter_name
                                + "\nhours since last post: " + hours + "\nSLA | Yellow: " + isYellow(hours) + "\nSLA | RED: " + isRed(hours));
                        sb.append("\n---------------------------");
                    } else {
                        sb.append("\nid: " + topic_id + "\n" + "status: " + topic_status + "\nname: " + topic_name + "\nstarter: " + topic_starter_name
                                + "\nhours since last post: " + hours + "\nSLA | Yellow: " + isYellow(hours) + "\nSLA | RED: " + isRed(hours));
                        sb.append("\n---------------------------");
                    }
                    count++;
                }
            }

            //creating log file
            Date date = new Date();
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            String fileName = f.format(date) + ".sla.RED.log";
            FileService fs = new FileService();
            fs.createFile(fileName, sb);

            //sending by email id RED ones exist
            if (count != 0) {
                MailService ms = MailService.getInstance();
                for (String mailToAddress : supportTeamEmails) {
                    ms.sendPlainEmail(mailToAddress, "Community \\ Topics in [RED]", sb.toString());
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            ds.closeConnection();
        }
    }
    
    public void getTopicsInYellow() {
        DatabaseService ds = DatabaseService.getInstance();
        try {
            Statement statement;
            statement = ds.getConnection().createStatement();
            System.out.println(ds.getConnection());
            ResultSet rs = statement.executeQuery("SELECT * FROM community.ipbtopics WHERE "
                    + "ipbtopics.state NOT LIKE 'closed' AND ipbtopics.last_poster_id "
                    + "NOT IN (select member_id from community.ipbmembers where member_group_id "
                    + "IN (4,5,6,7,9))");

            StringBuffer sb = new StringBuffer();
            int count = 0;

            while (rs.next()) {
                String topic_id = rs.getString("tid");
                String topic_status = rs.getString("state");
                String topic_name = rs.getString("title");
                String topic_starter_name = rs.getString("starter_name"), line = "";
                Long l = rs.getLong("last_post");
                int hours = getTimeDiffWithCurrentTime(l);

                System.out.println("----------------------");
                if (isYellow(hours)) {
                    System.out.println("Yellow exists");
                    if (count == 0) {
                        sb.append("id: " + topic_id + "\n" + "status: " + topic_status + "\nname: " + topic_name + "\nstarter: " + topic_starter_name
                                + "\nhours since last post: " + hours + "\nSLA | Yellow: " + isYellow(hours) + "\nSLA | RED: " + isRed(hours));
                        sb.append("\n---------------------------");
                    } else {
                        sb.append("\nid: " + topic_id + "\n" + "status: " + topic_status + "\nname: " + topic_name + "\nstarter: " + topic_starter_name
                                + "\nhours since last post: " + hours + "\nSLA | Yellow: " + isYellow(hours) + "\nSLA | RED: " + isRed(hours));
                        sb.append("\n---------------------------");
                    }
                    count++;
                }
            }

            //creating log file
            Date date = new Date();
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            String fileName = f.format(date) + ".sla.Yellow.log";
            FileService fs = new FileService();
            fs.createFile(fileName, sb);

            //sending by email id Yellow ones exist
            if (count != 0) {
                MailService ms = MailService.getInstance();
                for (String mailToAddress : supportTeamEmails) {
                    ms.sendPlainEmail(mailToAddress, "Community \\ Topics in [Yellow]", sb.toString());
                }                
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            ds.closeConnection();
        }
    }

    public boolean isYellow(int hours) {
        Properties properties = PropertiesService.getInstance().getProperties();
        int yellow_h = Integer.parseInt(properties.getProperty("sla.yellow.period"));
        boolean isRed = isRed(hours);

        if ((hours >= yellow_h) && (!isRed)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRed(int hours) {
        Properties properties = PropertiesService.getInstance().getProperties();
        int red_h = Integer.parseInt(properties.getProperty("sla.red.period"));
        if (hours >= red_h) {
            return true;
        } else {
            return false;
        }
    }

    public static int getTimeDiffWithCurrentTime(Long last_post_time) {

        Date dateOne;
        String diff = "";
        Timestamp last_post = new Timestamp(last_post_time);
        Date current_date = new Date();
        dateOne = new Date();
        dateOne.setTime((long) last_post.getTime() * 1000);

        long timeDiff = Math.abs(dateOne.getTime() - current_date.getTime());
        diff = String.format("%d", TimeUnit.MILLISECONDS.toHours(timeDiff));
        int res = Integer.parseInt(diff);

        return res;
    }
}
