//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ws.parsepdf;

import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ws.gui.Test;
import org.ws.logic.Operation;
import org.ws.logic.QLearning;
import org.ws.logic.Zone;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

@Path("/newOrders")
public class ServiceNewOrders {
    private static final String UPLOAD_FOLDER = System.getProperty("java.io.tmpdir") + "/uploaded";
    private QLearning ql;
    @Context
    private UriInfo context;

    public ServiceNewOrders() {
    }

    @POST
    @Consumes({"multipart/form-data"})
    @Produces({"application/json"})
    public Response uploadFile(@FormDataParam("constraints") InputStream constraints, @FormDataParam("timeRecordings") InputStream timeRecordings,
                               @FormDataParam("iter") int iter,@FormDataParam("currentTime") int startTime,@FormDataParam("reschedule") boolean reschedule) {

        if(constraints != null && timeRecordings != null && iter > 0 && startTime > -1) {
            File [] targetFile = new File[2];
            File copy = new File(String.format("%s/copy.txt", UPLOAD_FOLDER));

            //Copy new files on server
            targetFile[0] = new File(String.format("%s/constraints_reschedule.txt", UPLOAD_FOLDER));
            targetFile[1] = new File(String.format("%s/timeRecordings_reschedule.txt", UPLOAD_FOLDER));

                boolean read = readSolutionFile(iter,reschedule);//read solution file, constraints and time recordings
                if (read) {
                    try {
                        FileUtils.copyInputStreamToFile(constraints, copy);
                        File s = new File(String.format("%s/copy.txt", UPLOAD_FOLDER)); //Read copy
                        BufferedReader file = new BufferedReader(new FileReader(s));
                        //Check input files are in correct order
                         char c = file.readLine().charAt(0);
                        if (c =='%') {
                            //System.out.println("Entre 1 "+c);
                            s = new File(String.format("%s/copy.txt", UPLOAD_FOLDER)); //Read copy
                            FileUtils.copyFile(s,targetFile[0]);
                            FileUtils.copyInputStreamToFile(timeRecordings, targetFile[1]);
                        }else {
                            //System.out.println("Entre 2 "+c);
                            s = new File(String.format("%s/copy.txt", UPLOAD_FOLDER)); //Read copy
                            FileUtils.copyFile(s,targetFile[1]);
                            FileUtils.copyInputStreamToFile(timeRecordings, targetFile[0]);
                            //FileUtils.copyInputStreamToFile(constraints, targetFile[1]);
                        }
                        startTimes(startTime);
                        ql.ReadDataNewProducts(targetFile);
                    } catch (IOException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
                        e.printStackTrace();
                        return Response.status(400).entity("One or more files are not correct").build();
                    }
                    try {
                        ql.ExecuteReSchedule();
                        ql.fusionFiles();
                    } catch (FileNotFoundException | CloneNotSupportedException e1) {
                        e1.printStackTrace();
                    } // TODO Auto-generated catch block
                    File solution1 = new File(String.format("%s/schedule.txt", UPLOAD_FOLDER));
                    return Response.status(200).entity(solution1).build();
                }else
                    return Response.status(400).entity("There are no previous schedules").build();//no se ha corrido newOrders before.
                // FileUtils.copyInputStreamToFile(uploadedInputStream, targetFile);
        } else {
            return Response.status(400).entity("Missing parameters").build();
        }
    }

    //leer el archivo solution para saber dónde es la interrupcion
    public boolean readSolutionFile(int iter, boolean reschedule){
        File solution = null;
        File[] targetFile = new File[2]; //files constraints and times recording
        try {
            //if it's reschedule take copy files else take original files
            solution = reschedule ? new File(String.format("%s/solution_copy.tmp", UPLOAD_FOLDER))
                    : new File(String.format("%s/solution.tmp", UPLOAD_FOLDER)); //Read before solution
            targetFile[0] = reschedule ? new File(String.format("%s/targetFile_copy.txt", UPLOAD_FOLDER))
                    :new File(String.format("%s/targetFile.txt", UPLOAD_FOLDER));
            targetFile[1] = reschedule ? new File(String.format("%s/targetFile2_copy.txt", UPLOAD_FOLDER))
                    :new File(String.format("%s/targetFile2.txt", UPLOAD_FOLDER));

            //Si existen los ficheros y no es reschedule, se crea copia a los ficheros del server
            if (!reschedule && solution.exists() && targetFile[0].exists() && targetFile[1].exists()) {
                File solutionCopy = new File(String.format("%s/solution_copy.tmp", UPLOAD_FOLDER)); //crear copia de la solution para reschedule
                FileUtils.copyFile(solution,solutionCopy); //copy solution to solution_copy
                solutionCopy = new File(String.format("%s/targetFile_copy.txt", UPLOAD_FOLDER)); //crear copia de constraint original para reschedule
                FileUtils.copyFile(targetFile[0],solutionCopy); //copy solution to solution_copy
                solutionCopy = new File(String.format("%s/targetFile2_copy.txt", UPLOAD_FOLDER)); //crear copia de time recording original para reschedule
                FileUtils.copyFile(targetFile[1],solutionCopy); //copy solution to solution_copy
            }

        } catch (FileNotFoundException e) {
            Response.status(500).entity("There aren't solution files on server").build();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double LR = 0.1;
        double DF = 0.8;
        double epsi = 0.2;

        try {
            if (solution.exists() && targetFile[0].exists() && targetFile[1].exists()){
                ql = new QLearning(targetFile, LR, DF, iter, epsi);
                ql.ReadData(targetFile);
                new Test(solution,ql);
            }
            // ql.PrintJob();
        } catch (FileNotFoundException e) {
            Response.status(500).entity("No existen ficheros soluciones en el server").build();
            e.printStackTrace();
        } catch (IOException e) {
            Response.status(500).entity("Ficheros incorrectos").build();
            e.printStackTrace();
        }
        return solution.exists();
    }

    private void startTimes(int currentTime) {
        // System.out.println("start Times");
        ArrayList<Operation> opNoModify = new ArrayList<>();
        for (int i = 0; i < ql.Jobs.length; i++) {
            for (int j = 0; j < ql.Jobs[i].operations.size(); j++) {
                //System.out.println("job "+ql.Jobs[i].GetID()+" op "+ql.Jobs[i].operations.get(j).GetID());
                ql.Machines[ql.Jobs[i].operations.get(j).Ma].timeReSchedule = currentTime;
                //Si empieza antes que tiempo de llegada de los nuevos products
                if (ql.Jobs[i].operations.get(j).initial_time <= currentTime) {
                    opNoModify.add(ql.Jobs[i].operations.get(j));
                }else if (ql.Jobs[i].operations.get(j).back2back_before != -1){ //if has back to back
                    //si la op back2back_before starts before current time, add the current op
                    if (ql.Jobs[i].operations.get(ql.Jobs[i].operations.get(j).back2back_before).initial_time <= currentTime)
                        opNoModify.add(ql.Jobs[i].operations.get(j));
                }

            }
        }
        //inicializar la zona con el time actual
        for (int j = 0; j < ql.zone.length; j++) {
           ql.zone[j].timeReScheduleZone = currentTime;
        }

        //start times
        for (Operation operation : opNoModify) {
            //System.out.println(" job "+operation.GetJob()+" op "+operation.GetID()+" name "+ operation.name+" Ma "+operation.Ma+" end time "+operation.end_time);
            //time of machines
            ql.Machines[operation.Ma].timeReSchedule = Math.max(operation.end_time, ql.Machines[operation.Ma].timeReSchedule);
            //System.out.println("time Reschedule "+ql.Machines[operation.Ma].timeReSchedule);

            //time of zones
            String job_operation_machine = "" + operation.GetJob() + operation.GetID() + operation.Ma;
            for (int j = 0; j < ql.zone.length; j++) {
                //si ocupa la zona el time de la zona es el max entre el end time de la op y el current time
                if (ql.zone[j].job_operation_occupied.get(job_operation_machine).equals(true))
                    ql.zone[j].timeReScheduleZone = Math.max(operation.end_time, ql.zone[j].timeReScheduleZone);
            }

            //operation para empezar re-schedule
            ql.Jobs[operation.GetJob()].opStart = operation.GetID() + 1;
            //System.out.println(" job "+operation.GetJob()+" opStart "+ql.Jobs[operation.GetJob()].opStart);
            //ql.Jobs[operation.GetJob()].temp_endtime = operation.end_time;
            ql.Jobs[operation.GetJob()].temp_endtime = currentTime;
            ql.Jobs[operation.GetJob()].endTimeRechedule = currentTime;
            ql.Jobs[operation.GetJob()].finished = ql.Jobs[operation.GetJob()].opStart >= ql.Jobs[operation.GetJob()].operations.size();
           // System.out.println(" job "+operation.GetJob()+" finished "+ql.Jobs[operation.GetJob()].finished);
        }

        for (int i = 0; i < ql.Machines.length ; i++){
            System.out.println("machine "+i+" time  "+ql.Machines[i].timeReSchedule);
        }
        for (int i = 0; i < ql.zone.length ; i++){
            System.out.println("zone "+i+" time  "+ql.zone[i].timeReScheduleZone);
        }
    }
}
