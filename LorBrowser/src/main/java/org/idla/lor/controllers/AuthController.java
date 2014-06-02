package org.idla.lor.controllers;

import org.apache.log4j.Logger;
import org.idla.lor.models.AccessToken;
import org.idla.lor.models.Progress;
import org.idla.lor.models.Status;
import org.idla.lor.services.CryptoService;
import org.idla.lor.services.JsonService;
import org.idla.lor.services.S3Service;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * AuthController handles authentication requests
 */
@Controller
@RequestMapping(value = "/auth")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class);

    private String key;
    private String access;
    private String secret;
    private String bucket;

    /**
     * Logs in user with AES key
     * @param code - client login AES key
     * @return JSON to client
     * @throws GeneralSecurityException - if key cannot be authenticated
     */
    @RequestMapping (value = "/login/{code}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String login(@PathVariable String code, HttpServletRequest req) throws GeneralSecurityException {
        logger.info(req.getMethod()+ " " +req.getRequestURL());
        // initialize accessToken to hold successful login response information
        AccessToken accessToken = new AccessToken();
        CryptoService cryptoService = new CryptoService(key);
        Status status = new Status();
        S3Service s3Service = new S3Service(bucket, access, secret);
        // decrypted path
        String decrypted = cryptoService.decrypt(code);
        // check if bucket exists
        if (s3Service.checkBucket(bucket) && (decrypted.equals("admin") || s3Service.isValidKey(decrypted))) {
            if (decrypted.equals("admin")) {
                decrypted = "";
            }
            accessToken.setKey(code);
            accessToken.setResult(bucket + ";" + decrypted);
            // set status message for client
            status.setStatus("successful");
            status.setReason("logged in with key " + code);
            ProgressController.progressMap.put(code,new Progress());
        }
        else {
            accessToken.setKey("");
            accessToken.setResult("");
            // set status message for client
            status.setStatus("fail");
            status.setReason("failed to log in with key " + code);
        }
        // returns JSON feed to client with accesstoken attached
        JsonService jsonService = new JsonService();
        jsonService.setResponse(accessToken);
        jsonService.setStatus(status);
        logger.info(jsonService.getResponse());
        return jsonService.getResponse();
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
}
