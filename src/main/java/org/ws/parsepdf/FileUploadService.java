//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ws.parsepdf;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.FileUtils;
import org.ws.logic.QLearning;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("/upload")
public class FileUploadService {
    private static final String UPLOAD_FOLDER = System.getProperty("java.io.tmpdir") + "/uploaded";
   // private static final String UPLOAD_FOLDER_CONST = System.getProperty("java.io.tmpdir") + "/uploaded";
    @Context
    private UriInfo context;

    public FileUploadService() {
    }

    @POST
    @Consumes({"multipart/form-data"})
    @Produces({"application/json"})
    //add boolean zone
   // public Response uploadFile(@FormDataParam("file") FormDataBodyPart p, @FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file2") InputStream uploadedInputStream2, @FormDataParam("passwd") String passwd, @FormDataParam("iter") int iter) {
    public Response uploadFile(@FormDataParam("constraints") InputStream p, @FormDataParam("timeRecordings") InputStream p1, @FormDataParam("password") String passwd, @FormDataParam("iter") int iter) {
        if(p != null && p1 != null && passwd != null && iter > 0) {
            try {
                this.createFolderIfNotExists(UPLOAD_FOLDER);
                //this.createFolderIfNotExists(UPLOAD_FOLDER_CONST);
            } catch (SecurityException var) {
                return Response.status(500).entity("Can not create destination folder on server").build();
            }
            double LR = 0.1;
            double DF = 0.8;
            double epsi = 0.2;
            File[] targetFile = new File[2];
            targetFile[0] = new File(String.format("%s/targetFile.txt", UPLOAD_FOLDER));
            targetFile[1] = new File(String.format("%s/targetFile2.txt", UPLOAD_FOLDER));
            try {
                //Files.write(Paths.get(targetFile[0].getPath()), p.getValue().getBytes());
               // Files.write(Paths.get(targetFile[1].getPath()), p1.getValue().getBytes());
                 FileUtils.copyInputStreamToFile(p, targetFile[0]);
                FileUtils.copyInputStreamToFile(p1, targetFile[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            QLearning ql = null;
            try {
                ql = new QLearning(targetFile, LR, DF, iter, epsi);
                ql.ReadData(targetFile);
            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                return Response.status(400).entity("One of more files are not correct").build();
                //e.printStackTrace();
            }
            int bestSol = 0;
            try {
                bestSol = ql.Execute(LR, DF);
            } catch (FileNotFoundException | CloneNotSupportedException e1) {
                e1.printStackTrace();
            }
            File solution = new File(String.format("%s/schedule.txt", UPLOAD_FOLDER));
            return Response.status(200).entity(solution).build();
        }else{
            return Response.status(400).entity("You must sent all the requirement parameters").build();
        }

       /* if(uploadedInputStream != null && uploadedInputStream2 != null && passwd != null && iter > 0) {
            try {
                this.createFolderIfNotExists(UPLOAD_FOLDER);
                this.createFolderIfNotExists(UPLOAD_FOLDER_CONST);
            } catch (SecurityException var) {
                return Response.status(500).entity("Can not create destination folder on server").build();
            }
            double LR = 0.1;
            double DF = 0.8;
            double epsi = 0.2;
            File [] targetFile = new File[2];
            targetFile[0] = new File(String.format("%s/targetFile.tmp", UPLOAD_FOLDER));
            targetFile[1] = new File(String.format("%s/targetFile2.tmp", UPLOAD_FOLDER_CONST));
            try {
                FileUtils.copyInputStreamToFile(uploadedInputStream, targetFile[0]);
                FileUtils.copyInputStreamToFile(uploadedInputStream2, targetFile[1]);
                QLearning ql = new QLearning(targetFile, LR, DF, iter, epsi);
                ql.ReadData(targetFile);
                try {
                    //QL2.ExecuteEverybody(LR, DF);
                    ql.Execute(LR, DF);
                } catch (FileNotFoundException | CloneNotSupportedException e1) {
                    e1.printStackTrace();
                } // TODO Auto-generated catch block

                // FileUtils.copyInputStreamToFile(uploadedInputStream, targetFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(Date.from(Instant.now()));
            String result2 = String.format(
                    "%1$tY-%1$tm-%1$td.txt", cal);
            String PathTest = System.getProperty("user.home")+"/Desktop/"+"Schedule__" + result2;
            File f = new File(PathTest);
            return  Response.status(200).entity(f).build();
           // return Response.status(200).entity("OK").build();
        } else {*/

      //  }
    }

    private void createFolderIfNotExists(String dirName) throws SecurityException {
        File theDir = new File(dirName);
        if(!theDir.exists()) {
            theDir.mkdir();
        }

    }
}
