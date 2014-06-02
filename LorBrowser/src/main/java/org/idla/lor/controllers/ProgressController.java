package org.idla.lor.controllers;

import org.apache.log4j.Logger;
import org.idla.lor.models.Progress;
import org.idla.lor.models.Status;
import org.idla.lor.services.JsonService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Controller
@RequestMapping(value = "/progress")
public class ProgressController {

    private static final Logger logger = Logger.getLogger(ProgressController.class);

    public static HashMap<String, Progress> progressMap = new HashMap<String, Progress>();

    @RequestMapping(value = "/{unique}", method = RequestMethod.GET)
    @ResponseBody
    public String progress (@PathVariable String unique, HttpServletRequest req) {
        //logger.info(req.getMethod() + " " + req.getRequestURL());
        Status status = new Status();
        status.setStatus("success");
        status.setReason("progress returned");
        JsonService jsonService = new JsonService(progressMap.get(unique), status);
        //logger.info(jsonService.getResponse());
        return jsonService.getResponse();
    }
}
