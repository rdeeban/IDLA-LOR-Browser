package org.idla.lor.controllers;

import org.apache.log4j.Logger;
import org.idla.lor.models.LorNode;
import org.idla.lor.models.Progress;
import org.idla.lor.models.S3Reference;
import org.idla.lor.models.Status;
import org.idla.lor.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * S3Controller handles amazon s3 key create/read requests
 */
@Controller
@RequestMapping(value = "/s3")
public class S3Controller {

    private static final Logger logger = Logger.getLogger(S3Controller.class);

    private String access;
    private String secret;

    /**
     * Initializes Lor Tree with Root Node
     * @param bucket - Amazon S3 bucket
     * @return JSON to client
     */
    @RequestMapping(value = "/{bucket}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String init(@PathVariable String bucket, HttpServletRequest req) {
        logger.info(req.getMethod()+ " " +req.getRequestURL());
        Status status = new Status();
        status.setStatus("success");
        status.setReason("Bucket " + bucket + " with no prefix found");
        // return json feed to client
        JsonService jsonService = new JsonService("", status);
        logger.info(jsonService.getResponse());
        return jsonService.getResponse();
    }

    /**
     * Initializes Lor Tree with Root Node
     * @param bucket - Amazon S3 bucket
     * @param prefix - Amazon S3 prefix
     * @return JSON to client
     */
    @RequestMapping(value = "/{bucket}/{prefix}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String init(@PathVariable String bucket, @PathVariable String prefix, HttpServletRequest req) {
        logger.info(req.getMethod()+ " " +req.getRequestURL());
        prefix = prefix.replace("&","/");
        // initialize tree using bucket and prefix
        LorNode lorNode = new LorNode();
        lorNode.setKey(prefix);
        Status status = new Status();
        status.setStatus("success");
        status.setReason("Bucket " + bucket + " with prefix " + prefix + " found");
        // return json feed to client
        JsonService jsonService = new JsonService(prefix, status);
        logger.info(jsonService.getResponse());
        return jsonService.getResponse();
    }

    /**
     * Appends children to target node in Lor Tree
     * @param bucket - Amazon S3 bucket
     * @return JSON to client
     */
    @RequestMapping(value = "/children/{bucket}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String append(@PathVariable String bucket, HttpServletRequest req) {
        logger.info(req.getMethod()+ " " +req.getRequestURL());
        ArrayList<LorNode> children = new ArrayList<LorNode>();
        S3Service s3Service = new S3Service(bucket, access, secret);
        for (String child : s3Service.getAllKeys("")) {
            // iterate through amazon keys and create children of specified prefix
            LorNode newChild = new LorNode();
            newChild.setKey(child);
            children.add(newChild);
        }
        // set up status information for the client
        Status status = new Status();
        status.setStatus("success");
        status.setReason("Children returned from bucket " + bucket + " with no prefix");
        // return json feed to client
        JsonService jsonService = new JsonService(children, status);
        logger.info(jsonService.getResponse());
        return jsonService.getResponse();
    }

    /**
     * Appends children to target node in Lor Tree
     * @param bucket - Amazon S3 bucket
     * @param prefix - Amazon S3 prefix of target node
     * @return JSON to client
     */
    @RequestMapping(value = "/children/{bucket}/{prefix}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String append(@PathVariable String bucket, @PathVariable String prefix, HttpServletRequest req) {
        logger.info(req.getMethod()+ " " +req.getRequestURL());
        prefix = prefix.replace("&","/");
        ArrayList<LorNode> children = new ArrayList<LorNode>();
        S3Service s3Service = new S3Service(bucket, access, secret);
        for (String child : s3Service.getAllKeys(prefix)) {
            // iterate through amazon keys and create children of specified prefix
            LorNode newChild = new LorNode();
            newChild.setKey(child);
            children.add(newChild);
        }
        // set up status information for the client
        Status status = new Status();
        status.setStatus("success");
        status.setReason("Children returned from bucket " + bucket + " with prefix " + prefix);
        // return json feed to client
        JsonService jsonService = new JsonService(children, status);
        logger.info(jsonService.getResponse());
        return jsonService.getResponse();
    }

    /**
     * Receives .zip package from client, unpackages .zip, and uploads zip entries to Amazon S3 maintaining original file structure
     * @param bucket - Amazon S3 Bucket
     * @param prefix - Amazon S3 prefix of target node
     * @param checkOverwrites - 1 to check for overwrites and warn user if any, 0 to overwrite without user approval
     * @param file - .zip file to unpackage
     * @return status of upload action
     * @throws IOException - if files cannot be found or read
     */
    @RequestMapping(value = "/upload/{code}/{bucket}/{prefix}/{checkOverwrites}/{metadata}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String upload(@PathVariable String metadata, @PathVariable String code, @PathVariable String bucket, @PathVariable String prefix, @PathVariable int checkOverwrites, @RequestParam("file") MultipartFile file, HttpServletRequest req) throws IOException {
        logger.info(req.getMethod()+ " " +req.getRequestURL());
        prefix = prefix.replace("&","/");
        if (ProgressController.progressMap == null) {
            ProgressController.progressMap = new HashMap<String, Progress>();
        }
        if (ProgressController.progressMap.get(code) == null) {
            ProgressController.progressMap.put(code, new Progress());
        }
        ProgressController.progressMap.get(code).upload = 0;
        ArrayList<S3Reference> uploads = new ArrayList<S3Reference>();
        // prepare s3 client for AWS transactions
        S3Service s3Service = new S3Service(bucket, access, secret);
        // prepare zip client for ZIP actions
        ZipService zipService = new ZipService();
        zipService.unzipFile(file, uploads, prefix, s3Service, code);
        JsonService jsonService = null;
        ProgressController.progressMap.get(code).upload = 0;
        if (checkOverwrites == 0) {
            // if client does not wish to check for overwrites
            for (S3Reference entry : uploads) {
                s3Service.uploadFile(entry.getName(), entry.getValue(), code, metadata);
                try {
                    ProgressService.progressMap.get(code).upload ++;
                } catch (Exception e) {System.out.println(entry.getName()+ " error");};
            }
            Status status = new Status();
            status.setStatus("success");
            status.setReason("Uploaded to " + prefix);
            jsonService = new JsonService(uploads, status);
        }
        else if (checkOverwrites == 1) {
            // if there are overwrites keep track of number
            int countOverwrites = 0;
            for (S3Reference entry : uploads) {
                if(entry.isOverwrite()) {
                    countOverwrites ++;
                }
            }
            // if there are no overwrites proceed with upload
            if (countOverwrites == 0) {
                for (S3Reference entry : uploads) {
                    s3Service.uploadFile(entry.getName(), entry.getValue(), code, metadata);
                    try {
                        ProgressService.progressMap.get(code).upload ++;
                    } catch (Exception e) {System.out.println(entry.getName() + " error");}
                }
                Status status = new Status();
                status.setStatus("success");
                status.setReason("Uploaded to " + prefix + " since there were no overwrites");
                jsonService = new JsonService(uploads, status);
            }
            else {
                // client if there are any overwrites
                Status status = new Status();
                status.setStatus("failed");
                status.setReason("Total number of overwrites: " + countOverwrites);
                jsonService = new JsonService(uploads, status);
            }
        }
        DeleteService deleteService = new DeleteService();
        deleteService.delete(new File("upload.zip"));
        deleteService.delete(new File("upload"));
        ProgressController.progressMap.get(code).upload = 0;
        logger.info(jsonService.getResponse());
        return jsonService.getResponse();
    }

    /**
     * Receives all keys under S3 prefix, packages them into a .zip, and sends it to client
     * @param bucket - Amazon S3 bucket
     * @param prefix - Amazon S3 prefix
     * @param resp - JSON response
     * @throws IOException - if files cannot be found or read
     */
    @RequestMapping(value = "/download/{code}/{bucket}/{prefix:.+}", method = RequestMethod.GET)
    public void download(@PathVariable String code, @PathVariable String bucket, @PathVariable String prefix, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info(req.getMethod() + " " + req.getRequestURL());
        if (ProgressController.progressMap == null) {
            ProgressController.progressMap = new HashMap<String, Progress>();
        }
        if (ProgressController.progressMap.get(code) == null) {
            ProgressController.progressMap.put(code, new Progress());
        }
        ProgressController.progressMap.get(code).download = 0;
        prefix = prefix.replace("&","/");
        String target = prefix.split("/")[prefix.split("/").length-1];
        // prepare s3 client for AWS transactions
        S3Service s3Service = new S3Service(bucket, access, secret);
        s3Service.downloadDirectory(prefix, code);
        ProgressController.progressMap.get(code).download = 0;
        // prepare Zip client for ZIP actions
        ZipService zipService = new ZipService();
        zipService.zipFile("content/","download/"+target+".zip", true, code);
        // prepare response feed to client
        resp.setContentType("application/zip");
        resp.setHeader("Content-Disposition", "attachment;filename=" + target + ".zip");
        File file = new File("download/"+target+".zip");
        FileInputStream fileIn = new FileInputStream(file);
        ServletOutputStream out = resp.getOutputStream();
        byte[] outputByte = new byte[4096];
        // send data to client
        while (fileIn.read(outputByte, 0, 4096) != -1) {
            out.write(outputByte, 0, 4096);
        }
        fileIn.close();
        out.flush();
        out.close();
        // delete old files on server
        DeleteService deleteService = new DeleteService();
        deleteService.delete(new File("download/"));
        deleteService.delete(new File("content/"));
        ProgressController.progressMap.get(code).download = 0;
        logger.info("download done");
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

}
