//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ws.parsepdf;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.ws.parsepdf.ParserData;
import org.ws.parsepdf.ResponseParse;
import org.ws.parsepdf.parsers.Captcha;
import org.ws.parsepdf.parsers.Token;

@Path("/upload")
public class FileUploadService {
    private static final String UPLOAD_FOLDER = System.getProperty("java.io.tmpdir") + "/uploaded";
    @Context
    private UriInfo context;

    public FileUploadService() {
    }

    @POST
    @Consumes({"multipart/form-data"})
    @Produces({"application/json"})
    public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
        if(uploadedInputStream != null && fileDetail != null) {
            try {
                this.createFolderIfNotExists(UPLOAD_FOLDER);
            } catch (SecurityException var9) {
                return Response.status(500).entity("Can not create destination folder on server").build();
            }

            ResponseParse responseParse = new ResponseParse(200, "File parsed");
            PDDocument document = null;
            ParserData data = new ParserData();

            try {
                document = PDDocument.load(uploadedInputStream, MemoryUsageSetting.setupTempFileOnly());
                Captcha e = new Captcha();
                data.setCaptcha(e.getCaptchaBase64(document));
                Token token = new Token(document);
                data.setToken(token.getToken());
                document.close();
                uploadedInputStream.close();
            } catch (IOException var8) {
                var8.printStackTrace();
            }

            responseParse.setData(data);
            return Response.status(200).entity(responseParse.getJson()).build();
        } else {
            return Response.status(400).entity("Invalid form data").build();
        }
    }

    private void saveToFile(InputStream inStream, String target) throws IOException {
        FileOutputStream out = null;
        boolean read = false;
        byte[] bytes = new byte[1024];
        out = new FileOutputStream(new File(target));

        int read1;
        while((read1 = inStream.read(bytes)) != -1) {
            out.write(bytes, 0, read1);
        }

        out.flush();
        out.close();
    }

    private void createFolderIfNotExists(String dirName) throws SecurityException {
        File theDir = new File(dirName);
        if(!theDir.exists()) {
            theDir.mkdir();
        }

    }
}
