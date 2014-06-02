package org.idla.lor.services;

import org.idla.lor.models.Mail;
import javax.mail.MessagingException;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * MailService acts as the basic interface from which mailing classes implement
 */
public interface MailService {

    /**
     * sends e-mail
     * @param mail - Mail
     * @throws MessagingException - if e-mail fails
     * @see Mail
     */
    public void sendMail(Mail mail) throws MessagingException;

}
