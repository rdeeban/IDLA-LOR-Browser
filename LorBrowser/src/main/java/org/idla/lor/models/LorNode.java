package org.idla.lor.models;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * LorNode Model
 */
public class LorNode {

    String key;

    /**
     * default constructor
     */
    public LorNode() {

    }

    /**
     * constructs node with key
     * @param key - set
     */
    public LorNode(String key) {
        this.key = key;
    }

    /**
     *
     * @return key
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
