package org.idla.lor.models;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * Status Model
 */
public class Status {

    private String reason;
    private String status;

    /**
     * default constructor
     */
    public Status() {

    }

    /**
     *
     * @return reason
     */
    public String getReason() {
        return reason;
    }

    /**
     *
     * @param reason - set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     *
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     *
     * @param status - set
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
