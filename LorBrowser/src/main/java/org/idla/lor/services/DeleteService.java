package org.idla.lor.services;

import java.io.File;
import java.io.IOException;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * DeleteService deletes leftover files from server after uploads and downloads
 */
public class DeleteService {

    /**
     * default constructor
     */
    public DeleteService () {

    }

    /**
     * deletes file
     * @param file - file to delete
     * @throws IOException - if file cannot be found or read
     */
    public void delete(File file) throws IOException {
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                file.delete();
            } else {
                String files[] = file.list();
                for (String temp : files) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }
                if (file.list().length == 0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
    }

}
