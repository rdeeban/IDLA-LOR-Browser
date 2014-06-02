package org.idla.lor.services;

import org.idla.lor.models.Status;
import com.google.gson.Gson;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * JsonService converts JAVA POJO's into JSON for client-side manipulation
 */
public class JsonService implements ResponseService {

    Object response;
    Status status;

    /**
     * default constructor
     */
    public JsonService() {

    }

    /**
     * constructs with parameters Object and Status
     * @param responseObject - any JAVA Object with field variables
     * @param status - status object
     * @see Status
     */
    public JsonService(Object responseObject, Status status) {
        this.response = responseObject;
        this.status = status;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * returns JSON formatted response string
     * @return JSON formatted response string
     */
    public String getResponse() {
        return new Gson().toJson(this);
    }
}
