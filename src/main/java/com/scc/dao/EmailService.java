package com.scc.dao;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    public static void sendSecurityCode(String recipientEmail, String code) {
        final String senderEmail = "Kennethcanon99@gmail.com"; // Your system Gmail
        final String appPassword = "yyvn ooom hpzk eevs";     // Your 16-digit Gmail App Password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Account Security Verification Code");
            message.setText("Hello,\n\nYour auto-generated security code is: " + code +
                    "\n\nPlease use this code to complete your account activation.\n\nRegards,\nSystem Admin");

            Transport.send(message);
            System.out.println("Security code successfully sent to email: " + recipientEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}