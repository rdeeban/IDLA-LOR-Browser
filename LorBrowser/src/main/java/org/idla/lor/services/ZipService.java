package org.idla.lor.services;

import org.idla.lor.controllers.ProgressController;
import org.idla.lor.models.Progress;
import org.idla.lor.models.S3Reference;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * ZipService performs zipping/unzipping on files
 */
public class ZipService {

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * default constructor
     */
    public ZipService() {

    }

    /**
     * zips fileToZip into zipFile.zip in/excluding containing folder
     * @param fileToZip - file to zip
     * @param zipFile - destination .zip file
     * @param excludeContainingFolder - in/exclude containing folder
     * @throws IOException if file does not exist or cannot be read
     */
    public void zipFile(String fileToZip, String zipFile, boolean excludeContainingFolder, String code) throws IOException {
        File f = new File(zipFile);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
        File srcFile = new File(fileToZip);
        if (excludeContainingFolder && srcFile.isDirectory()) {
            for (String fileName : srcFile.list()) {
                addToZip("", fileToZip + "/" + fileName, zipOut, code);
            }
        } else {
            addToZip("", fileToZip, zipOut, code);
            ProgressController.progressMap.get(code).download++;
            System.out.println(ProgressController.progressMap.get(code).download);
        }
        zipOut.flush();
        zipOut.close();
    }

    /**
     *
     * @param path
     * @param srcFile
     * @param zipOut
     * @throws IOException
     */
    private void addToZip(String path, String srcFile, ZipOutputStream zipOut, String code) throws IOException {
        File file = new File(srcFile);
        String filePath = "".equals(path) ? file.getName() : path + "/" + file.getName();
        if (file.isDirectory()) {
            for (String fileName : file.list()) {
                addToZip(filePath, srcFile + "/" + fileName, zipOut, code);
            }
        } else {
            zipOut.putNextEntry(new ZipEntry(filePath));
            FileInputStream in = new FileInputStream(srcFile);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int len;
            while ((len = in.read(buffer)) != -1) {
                zipOut.write(buffer, 0, len);
            }
            in.close();
        }
    }


    /**
     * unzips package
     * @param file
     * @param entries
     * @param destination
     * @throws IOException
     */
    public void unzipFile(MultipartFile file, ArrayList<S3Reference> entries, String destination, S3Service s3Service, String code) throws IOException {
        byte[] bytes = file.getBytes();
        BufferedOutputStream stream =
                new BufferedOutputStream(new FileOutputStream(new File("upload.zip")));
        stream.write(bytes);
        stream.close();

        String outputFolder = "upload";

        //create output directory is not exists
        File folder = new File(outputFolder);
        if(!folder.exists()){
            folder.mkdir();
        }

        //get the zip file content
        ZipInputStream zis =
                new ZipInputStream(new FileInputStream("upload.zip"));
        //get the zipped file list entry
        ZipEntry ze = zis.getNextEntry();

        byte[] buffer = new byte[1024];

        while(ze!=null){

            String fileName = ze.getName();
            File newFile = new File(outputFolder + File.separator + fileName);

            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();

            FileOutputStream fos = new FileOutputStream(newFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            String fullPath = destination + fileName;
            entries.add(new S3Reference(fullPath, newFile, s3Service.checkOverwrite(fullPath)));

            fos.close();
            ze = zis.getNextEntry();

            Progress p = ProgressController.progressMap.get(code);
            p.upload++;
            ProgressController.progressMap.put(code, p);
        }

        zis.closeEntry();
        zis.close();

    }
}
