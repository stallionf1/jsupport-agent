package com.slaagent.services;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailService {

    private static MailService ms = new MailService();

    public MailService() {
    }

    public void sendPlainEmail(String mailToAddress, String subject, String messageText) {

        Properties props = PropertiesService.getInstance().getProperties();
        String host = props.getProperty("smtp.host"),
                username = props.getProperty("smtp.username"),
                password = props.getProperty("smtp.password"),
                addressFrom = props.getProperty("smtp.sentfrom.address"), result = "";
        int port = Integer.parseInt(props.getProperty("smtp.port"));

        Session session = Session.getInstance(props);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, addressFrom));
            message.setSubject(subject);
            message.setText(messageText);
            Transport transport = session.getTransport("smtp");
            try {
                transport.connect(host, port, username, password);
            } catch (Exception e) {
                e.printStackTrace();
                result = e.toString();
                System.out.println("connection refused");
            }

            transport.sendMessage(message, InternetAddress.parse(mailToAddress));

            result = "Ok";

        } catch (UnsupportedEncodingException ex) {
            result = ex.toString();
        } catch (MessagingException e) {
            result = e.toString();
            throw new RuntimeException(e);
        } finally {
            System.out.println("mailto: " + mailToAddress + "   result: " + result);
        }
    }

    public void SendHTMLEmail(String mailToAddress, String subject, String messageText) {

        Properties props = PropertiesService.getInstance().getProperties();
        String host = props.getProperty("smtp.host"),
                username = props.getProperty("smtp.username"),
                password = props.getProperty("smtp.password"),
                addressFrom = props.getProperty("smtp.sentfrom.address"),
                port = props.getProperty("smtp.port"),
                result = "";
        // Get system properties
        Properties properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);
        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(addressFrom));
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailToAddress));
            // Set Subject: header field
            message.setSubject(subject);
            // Send the actual HTML message, as big as you like
            message.setContent(messageText, "text/html");
            // Send message
            Transport.send(message);
            result = "OK";
        } catch (MessagingException mex) {
            mex.printStackTrace();
            result = "Failed!";
        } finally {
            System.out.println(mailToAddress + " : " + result);
        }
    }

    public void sendHTMLAndAttachment(String mailToAddress, String subject, String messageText, LinkedList<File> files) {
        Properties props = PropertiesService.getInstance().getProperties();
        String host = props.getProperty("smtp.host"),
                username = props.getProperty("smtp.username"),
                password = props.getProperty("smtp.password"),
                addressFrom = props.getProperty("smtp.sentfrom.address"), result = "";
        String port = props.getProperty("smtp.port");
        // Get system properties
        Properties properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);
        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(addressFrom));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailToAddress));

            // Set Subject: header field
            message.setSubject(subject);
            message.setContent(messageText, "text/html");

            // Create the message part 
            BodyPart messageBodyPart = new MimeBodyPart();

            // Fill the message
            //messageBodyPart.setText(messageText);
            messageBodyPart.setContent(messageText, "text/html");

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            

            for (File f : files ) {
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(f.getAbsolutePath());
                System.out.println("abs path: "+f.getAbsolutePath());
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(f.getName());
                System.out.println("name: "+f.getName());
                multipart.addBodyPart(messageBodyPart);
            }
            // Send the complete message parts

            message.setContent(multipart);

            // Send message
            Transport.send(message);
            result = "OK";
        } catch (MessagingException mex) {
            mex.printStackTrace();
            result = "Failed!";
        } finally {
            System.out.println(mailToAddress + " : " + result);
        }
    }

    public static MailService getInstance() {
        if (ms == null) {
            new MailService();
        }
        return ms;
    }
}