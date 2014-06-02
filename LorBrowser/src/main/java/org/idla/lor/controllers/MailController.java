package org.idla.lor.controllers;

import org.apache.log4j.Logger;
import org.idla.lor.models.Mail;
import org.idla.lor.models.Status;
import org.idla.lor.services.CryptoService;
import org.idla.lor.services.JsonService;
import org.idla.lor.services.LorMailService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * MailController handles mail requests
 */
@Controller
@RequestMapping(value = "/mail")
public class MailController {

    private static final Logger logger = Logger.getLogger(MailController.class);

    private String senderAddress;
    private String senderPassword;
    private String subject;
    private String k;

    /**
     * Sends e-mail to recipient with LOR Browser Access Key
     * @param recipient - e-mail address of recipient
     * @param key - Amazon S3 Key
     * @param req - used to construct link in the body of the e-mail
     * @return JSON to client
     * @throws MessagingException - if error occurs in the sending of the e-mail
     * @throws GeneralSecurityException - if password for sender is incorrect
     */
    @RequestMapping(value = "/{recipient}/{key}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String sendMail(@PathVariable String recipient, @PathVariable String key, HttpServletRequest req) throws MessagingException, GeneralSecurityException {
        logger.info(req.getMethod()+ " " +req.getRequestURL());
        key = key.replace("&","/");
        // prepared AES encrypted key using path
        CryptoService cryptoService = new CryptoService(k);
        key = cryptoService.encrypt(key);
        // prepare mail for mailman
        Mail mail = new Mail();
        mail.setSender(senderAddress);
        mail.setPassword(senderPassword);
        mail.setBody("http://" + req.getServerName() + ":" + req.getServerPort() + "/public/?key=" + key);
        mail.setRecipient(recipient);
        mail.setSubject(subject);
        // prepare mailman to send mail containing encrypted key via email
        LorMailService lorMailService = new LorMailService();
        lorMailService.sendMail(mail);
        Status status = new Status();
        status.setStatus("success");
        status.setReason("Mail sent");
        // return json feed to client
        JsonService jsonService = new JsonService();
        jsonService.setResponse(mail);
        jsonService.setStatus(status);
        logger.info(jsonService.getResponse());
        return jsonService.getResponse();
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public void setSenderPassword(String senderPassword) {
        this.senderPassword = senderPassword;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setKey(String key) {
        this.k = key;
    }
}
