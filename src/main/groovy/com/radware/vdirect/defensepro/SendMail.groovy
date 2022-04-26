package com.radware.vdirect.defensepro

import com.radware.vdirect.Password
import com.sun.org.apache.xpath.internal.operations.Bool

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SendMail {
    private static final Logger log = LoggerFactory.getLogger(DiffFunctions.class)

    public static sendMail (String recipient, String mailFrom, String user, String pass, String smtpHost,
                            String subject, String body, TLS){
        log.info('Reached SMTP Mail Action')

        //provide recipient's email ID
        String to = recipient

        //provide sender's email ID
        String from = mailFrom
        //provide Mailtrap's username
        final String username = user
        //provide Mailtrap's password
        final String password = pass

        //provide Mailtrap's host address
        String host = smtpHost
        //configure Mailtrap's SMTP server details
        Properties props = new Properties();
        //props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.enable", TLS)
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "25");

        Session session
        //create the Session object
        if(pass){
            props.put("mail.smtp.auth", "true");
            session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    }
            );
        }else{
            props.put("mail.smtp.auth", "false");
            session = Session.getInstance(props)
        }


        try {
            //create a MimeMessage object
            Message message = new MimeMessage(session);

            //set From email field
            message.setFrom(new InternetAddress(from));

            //set To email field
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            //set email subject field
            //message.setSubject("Here comes Jakarta Mail!");
            message.setSubject(subject);

            //set the content of the email message
            //message.setText("Just discovered that Jakarta Mail is fun and easy to use");
            //message.setText(body);
            message.setContent(body, "text/html");

            //send the email message
            Transport.send(message);

            //System.out.println("Email Message Sent Successfully");
            log.info("Email Message Sent Successfully")

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
