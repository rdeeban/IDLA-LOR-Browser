package org.idla.lor.models;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * Mail Model
 */
public class Mail {

    private String recipient;
    private String subject;
    private String body;
    private String sender;
    private String password;

    /**
     * default constructor for mail
     */
    public Mail() {

    }

    /**
     * constructor with basic e-mail parameters
     * @param recipient - recipient of e-mail
     * @param subject - subject of e-mail
     * @param body - body of e-mail
     * @param sender - sender of e-mail
     * @param password - password of sender
     */
    public Mail(String recipient, String subject, String body, String sender, String password) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.sender = sender;
        this.password = password;
    }

    /**
     *
     * @return recipient
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     *
     * @param recipient - set
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     *
     * @return subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     *
     * @param subject - set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     *
     * @return body
     */
    public String getBody() {
        return body;
    }

    /**
     *
     * @param body - set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     *
     * @return sender
     */
    public String getSender() {
        return sender;
    }

    /**
     *
     * @param sender - set
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password - set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
