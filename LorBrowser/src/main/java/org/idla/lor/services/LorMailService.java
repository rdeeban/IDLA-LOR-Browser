package org.idla.lor.services;

import org.idla.lor.models.Mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * LorMailService sends mail using Google SMTP client
 */
public class LorMailService implements MailService {

    private Properties mailServerProperties;
    private Session getMailSession;
    private MimeMessage generateMailMessage;

    /**
     * default constructor
     */
    public LorMailService() {

    }

    /**
     * Sends e-mail using gmail smtp client
     * @param mail
     * @throws MessagingException - if e-mail fails
     */
    @Override
    public void sendMail(Mail mail) throws MessagingException {
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(mail.getRecipient()));
        generateMailMessage.setSubject(mail.getSubject());
        generateMailMessage.setContent(mail.getBody(), "text/html");
        Transport transport = getMailSession.getTransport("smtp");
        transport.connect("smtp.gmail.com", mail.getSender(), mail.getPassword());
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
    }
}
