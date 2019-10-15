package org.ws.parsepdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.ws.parsepdf.parsers.CommonText;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by charlie on 1/08/18.
 */
@Path("/pdfs")
public class FinderPDFService {

    private ResourceBundle properties;

    public FinderPDFService(){
        String propertyFilePath = "org.ws.parsepdf.config";
        properties = ResourceBundle.getBundle(propertyFilePath);

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"application/json"})
    @Path("/find")
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

        int count = 0;

        for (String pdf : pdfs) {
            try {
                document = PDDocument.load(new File(dir.getPath()+"/"+pdf));

                CommonText commonText = new CommonText(document);
                String[] pdfText = commonText.getLines().toArray( new String[commonText.getLines().size()]);

                boolean flag_stop = false;
                Iterator<Client> iterator = Arrays.asList(clients).iterator();
                String passport = pdfText[4];
                while (iterator.hasNext() && !flag_stop) {
                    Client elem = iterator.next();

                    if(passport.equals(elem.getPassport())){
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
                count++;

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("File: " +pdf);
            }

        }

        ResponseFinder responseFinder = new ResponseFinder(200,"Files processed: "+count+" Found: "+appointments.size());
        responseFinder.setData(appointments.toArray());

        return Response.status(200).entity(responseFinder.getJson()).build();



    }

    @POST
    @Produces({"application/json"})
    @Path("/list")
    public Response list() {

        PDDocument document = null;

        File dir = new File(properties.getString("FOLDER_TARGET"));

        File[] pdfs = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Pattern.matches(".*pdf$",name);
            }
        });

        Arrays.sort(pdfs);

        ArrayList<String> results = new ArrayList<>();

        int count = 0;

        for (File pdf : pdfs) {
            try {
                document = PDDocument.load(pdf);

                CommonText commonText = new CommonText(document);
                String pdfText = commonText.getLines().get(1);
                results.add("File:" + pdf.getName() + " Fecha: " + pdfText);

                document.close();
                count++;

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("File: " +pdf.getName());
            }

        }

        ResponseFinder responseFinder = new ResponseFinder(200,"Files processed: "+count);
        responseFinder.setData(results.toArray());

        return Response.status(200).entity(responseFinder.getJson()).build();

    }

    @POST
    @Produces({"application/json"})
    @Path("/groupby/{param}")
    public Response groupby(@PathParam("param") String field) {

        PDDocument document = null;

        File dir = new File(properties.getString("FOLDER_TARGET"));

        File[] pdfs = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Pattern.matches(".*pdf$",name);
            }
        });

        Arrays.sort(pdfs);
        HashMap<String,Integer> map = new HashMap<>();
        Set<String> tmpS = new HashSet<>();

        ArrayList<String> results = new ArrayList<>();

        int count = 0;


        String sfield = null;

        for (File pdf : pdfs) {
            try {
                document = PDDocument.load(pdf);

                CommonText commonText = new CommonText(document);
                if (field.equals("date")){

                    if (commonText.getLines().size() > 4){
                        sfield = commonText.getLines().get(4);
                        boolean ispass = Pattern.matches("[A-Z]{1}[0-9]+",sfield);
                        if (!ispass){
                            sfield = commonText.getLines().get(3);
                        }
                    }

                    if(!tmpS.contains(sfield)){
                        tmpS.add(sfield);
                    }else {
                        document.close();
                        continue;
                    }

                    sfield = commonText.getLines().get(1);
                    boolean isdate = Pattern.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}",sfield);
                    if (!isdate){
                        sfield = commonText.getLines().get(0);
                    }
                }

                if (field.equals("passport")){

                    if (commonText.getLines().size() > 4){
                        sfield = commonText.getLines().get(4);
                        boolean ispass = Pattern.matches("[A-Z]{1}[0-9]+",sfield);
                        if (!ispass){
                            sfield = commonText.getLines().get(3);
                        }
                    }else{
                        sfield = commonText.getText();
                    }


                    System.out.println(sfield +"---"+pdf);
                }

                if (!map.containsKey(sfield)) {
                    map.put(sfield,0);
                }

                map.put(sfield,map.get(sfield) + 1);


                document.close();
                count++;

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("File: " +pdf.getName());
            }

        }

        if (field.equals("date")){

            SimpleDateFormat dateFormat =new SimpleDateFormat("dd-MM-yyyy");
            ArrayList<Date> dates = new ArrayList<>();

            for (String sdate : map.keySet()){
                try {
                    dates.add(dateFormat.parse(sdate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Collections.sort(dates);

            for (Date date : dates){
                String key =  dateFormat.format(date);
                results.add(field +": " + key + " count: " + map.get(key));
            }

        }

        if (field.equals("passport")){

            for (String key : map.keySet()){
                results.add(field +": " + key + " count: " + map.get(key));
            }

        }




        ResponseFinder responseFinder = new ResponseFinder(200,"Files processed: "+count);
        responseFinder.setData(results.toArray());

        return Response.status(200).entity(responseFinder.getJson()).build();

    }

}
