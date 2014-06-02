package org.idla.lor.controllers;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * Handles the web context root / request and returns the public/ directory
 */
@Controller
public class IndexController {

    private static final Logger logger = Logger.getLogger(IndexController.class);

    /**
     * Redirects / to /public/
     * @return redirect command to /public/
     */
    @RequestMapping (value = "/", method = RequestMethod.GET)
    public String index(HttpServletRequest req) {
        logger.info(req.getMethod()+ " " +req.getRequestURL());
        return "redirect:/public/";
    }
}
