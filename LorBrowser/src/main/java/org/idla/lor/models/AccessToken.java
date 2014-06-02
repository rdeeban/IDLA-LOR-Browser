package org.idla.lor.models;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * Access Token Model
 */
public class AccessToken {

    private String key;
    private String result;

    /**
     * default constructor
     */
    public AccessToken() {

    }

    /**
     *
     * @return result of access key
     */
    public String getResult() {
        return result;
    }

    /**
     *
     * @param result - set
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     *
     * @return access key
     */
    public String getKey() {
        return key;
    }

    /**
     *
     * @param key - set
     */
    public void setKey(String key) {
        this.key = key;
    }
}
