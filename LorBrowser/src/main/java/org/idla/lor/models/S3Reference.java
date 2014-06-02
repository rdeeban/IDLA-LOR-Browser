package org.idla.lor.models;

import java.io.File;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * S3 File Reference Model
 */
public class S3Reference {

    private String name;
    private File value;
    private boolean isOverwrite;

    /**
     * default constructor
     */
    public S3Reference() {

    }

    /**
     * constructor with parameters
     * @param name - name of S3 key
     * @param value - file data of S3 key
     * @param isOverwrite - 1 for an overwrite, 0 for new
     */
    public S3Reference(String name, File value, boolean isOverwrite) {
        this.name = name;
        this.value = value;
        this.isOverwrite = isOverwrite;
    }

    /**
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name - set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return value
     */
    public File getValue() {
        return value;
    }

    /**
     *
     * @param value - set
     */
    public void setValue(File value) {
        this.value = value;
    }

    /**
     *
     * @return - is an overwrite
     */
    public boolean isOverwrite() {
        return isOverwrite;
    }

    /**
     *
     * @param overwrite - set
     */
    public void setOverwrite(boolean overwrite) {
        isOverwrite = overwrite;
    }
}
