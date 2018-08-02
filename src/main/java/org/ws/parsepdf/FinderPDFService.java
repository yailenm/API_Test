package org.ws.parsepdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.simple.JSONObject;
import org.ws.parsepdf.parsers.CommonText;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Created by charlie on 1/08/18.
 */
@Path("/find")
public class FinderPDFService {

    private ResourceBundle properties;

    public FinderPDFService(){
        String propertyFilePath = "org.ws.parsepdf.config";
        properties = ResourceBundle.getBundle(propertyFilePath);

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"application/json"})
    public Response find(Client[] clients) {

        PDDocument document = null;

        File dir = new File(properties.getString("FOLDER_TARGET"));

        String[] pdfs = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Pattern.matches(".*pdf$",name);
            }
        });

        Appointment appointment = null;

        ArrayList<Appointment> appointments = new ArrayList<>();


        for (String pdf : pdfs) {
            try {
                document = PDDocument.load(new File(dir.getPath()+"/"+pdf));

                CommonText commonText = new CommonText(document);
                String pdfText = commonText.getText();

                boolean flag_stop = false;
                Iterator<Client> iterator = Arrays.asList(clients).iterator();

                while (iterator.hasNext() && !flag_stop) {
                    Client elem = iterator.next();

                    if(pdfText.indexOf(elem.getPassport()) > 0){
                        appointment = new Appointment();
                        appointment.setPassport(elem.getPassport());
                        appointment.setContent(pdfText);
                        appointment.setFile(pdf);
                        FileUtils.copyFile(new File(dir.getPath()+"/"+pdf), new File(properties.getString("FOLDER_OUTPUT")+"/"+elem.getPassport()+".pdf"));
                        flag_stop =  true;
                        appointments.add(appointment);

                    }
                }

                document.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        String result = "";

        ObjectMapper mapper = new ObjectMapper();
        try {
            result = mapper.writeValueAsString(appointments);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return Response.status(200).entity(result).build();



    }

}
