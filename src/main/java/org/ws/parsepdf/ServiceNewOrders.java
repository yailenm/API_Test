//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ws.parsepdf;

import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.FileUtils;
import org.ws.gui.Test;
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

@Path("/newOrders")
public class ServiceNewOrders {
    private static final String UPLOAD_FOLDER = System.getProperty("java.io.tmpdir") + "/uploaded";
    private static final String UPLOAD_FOLDER_CONST = System.getProperty("java.io.tmpdir") + "/uploaded";
    @Context
    private UriInfo context;

    public ServiceNewOrders() {
    }

    @POST
    @Consumes({"multipart/form-data"})
    @Produces({"application/json"})
    public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file2") InputStream uploadedInputStream2, @FormDataParam("passwd") String passwd,
                               @FormDataParam("iter") int iter,@FormDataParam("order") int startTime) {

        if(uploadedInputStream != null && uploadedInputStream2 != null && passwd != null && iter > 0 && startTime > 0) {
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
            File solution = new File(String.format("%s/solution.tmp", UPLOAD_FOLDER_CONST));

            try {
                Test t = new Test(solution);
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

            return Response.status(200).entity("OK").build();
        } else {
            return Response.status(400).entity("Invalid form data").build();
        }
    }

    private void createFolderIfNotExists(String dirName) throws SecurityException {
        File theDir = new File(dirName);
        if(!theDir.exists()) {
            theDir.mkdir();
        }

    }
}
