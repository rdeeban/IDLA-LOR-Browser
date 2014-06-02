package org.idla.lor.services;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.idla.lor.controllers.ProgressController;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * S3Service utilizes Amazon S3 API to perform create/read on Amazon S3
 */
public class S3Service {

    private AmazonS3 s3;
    private AWSCredentials credentials;
    private String bucketName;

    public S3Service() {}

    /**
     * constructs Amazon S3 client with bucket bucketName
     * @param bucketName - amazon s3 bucket
     */
    public S3Service(String bucketName, String accessKey, String secretKey) {
        credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3 = new AmazonS3Client(credentials);
        this.bucketName = bucketName;
    }

    /**
     * returns immediate children of prefix in s3
     * @param prefix - amazon s3 prefix
     * @return String ArrayList of amazon keys
     */
    public ArrayList<String> getAllKeys (String prefix) {
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(getSubDirs(prefix));
        keys.addAll(getFiles(prefix));
        return keys;
    }

    /**
     * returns immediate subdirectory in s3
     * @param prefix - amazon s3 prefix
     * @return subdirectories of key
     */
    public ArrayList<String> getSubDirs (String prefix) {
        String delimiter = "/";
        if(prefix.length() > 0) {
            if (!prefix.endsWith(delimiter)) {
                prefix += delimiter;
            }
        }
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix).withDelimiter(delimiter);
        ObjectListing objects = s3.listObjects(listObjectsRequest);
        return (ArrayList<String>) objects.getCommonPrefixes();
    }

    /**
     * returns immediate files under prefix in s3
     * @param prefix - amazon s3 prefix
     * @return immediate files under prefix
     */
    public ArrayList<String> getFiles (String prefix) {
        ArrayList<String> files = new ArrayList<String>();
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix).withDelimiter("/");
        ObjectListing objects = s3.listObjects(listObjectsRequest);
        ListIterator<S3ObjectSummary> iter = objects.getObjectSummaries().listIterator();
        while(iter.hasNext()) {
            S3ObjectSummary obj = iter.next();
            if(!obj.getKey().contains("_$folder$") && obj.getKey().length() != prefix.length()) {
                files.add(obj.getKey());
            }
        }
        return files;
    }

    /**
     * uploads file to target path in s3
     * @param path - target path
     * @param file - target file
     */
    public void uploadFile (String path, File file, String code, String email) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("code",code);
        map.put("email",email);
        objectMetadata.setUserMetadata(map);
        try {
            s3.putObject(new PutObjectRequest(bucketName, path, new FileInputStream(file.getAbsolutePath()), objectMetadata));
            s3.setObjectAcl(bucketName, path, CannedAccessControlList.PublicRead);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * downloads entire directory from s3 along with subdirectories and files
     * @param path - target path
     * @throws IOException if file does not exist or cannot be read
     */
    public void downloadDirectory (String path, String code) throws IOException {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(path).withMaxKeys(1000);
        ObjectListing objects = s3.listObjects(listObjectsRequest);
        do {
            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                downloadFile(objectSummary.getKey());
                ProgressController.progressMap.get(code).download++;
            }
            objects = s3.listNextBatchOfObjects(objects);
        } while (objects.isTruncated());
    }

    /**
     * downloads file from s3
     * @param path - target path
     * @throws IOException if file does not exist or cannot be read
     */
    public void downloadFile (String path) throws IOException {
        if (path.contains(".")) {
            String key = path;
            path = "content" + File.separator + path;
            path = path.replace("/",File.separator);
            File f = new File(path);
            f.getParentFile().mkdirs();
            if (f.isDirectory()) {
                f.createNewFile();
            }
            OutputStream outputStream = new FileOutputStream(new File(path));
            S3Object s3Object = s3.getObject(new GetObjectRequest(bucketName, key));
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = s3Object.getObjectContent().read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.close();
        }
    }

    /**
     * checks if key already exists in s3
     * @param path - target path
     * @return true or false
     * @throws AmazonClientException if client fails
     * @throws AmazonServiceException if service fails
     */
    public boolean isValidKey(String path) {
        boolean isValidFile = true;
        try {
            s3.getObjectMetadata(bucketName, path);
        } catch (AmazonS3Exception s3e) {
            isValidFile = false;
        }
        return isValidFile;
    }

    /**
     * checks if key already exists in s3
     * @param name - target path
     * @return true or false
     */
    public boolean checkOverwrite(String name) {
        return isValidKey(name);
    }

    /**
     * checks existence of bucket
     * @param name - name of bucket
     * @return true or false
     */
    public boolean checkBucket(String name) {
        return s3.doesBucketExist(name);
    }
}
