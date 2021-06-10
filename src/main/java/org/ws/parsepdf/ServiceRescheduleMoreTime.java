//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ws.parsepdf;

import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.FileUtils;
import org.ws.gui.Test;
import org.ws.logic.Operation;
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
import java.util.ArrayList;

@Path("/rescheduleTimeExtension")
public class ServiceRescheduleMoreTime {
    private static final String UPLOAD_FOLDER = System.getProperty("java.io.tmpdir") + "/uploaded";
   // private static final String UPLOAD_FOLDER_CONST = System.getProperty("java.io.tmpdir") + "/uploaded";
    private QLearning ql;
    @Context
    private UriInfo context;

    public ServiceRescheduleMoreTime() {
    }

    @POST
   @Consumes({"multipart/form-data"})
   // @Consumes({"application/json"})
    @Produces({"application/json"})
    //Parametros: nombre del recurso, timeActual, newTime
    public Response uploadFile(@FormDataParam("resource") String resource, @FormDataParam("currentTime") int currentTime, @FormDataParam("timeOperation") int timeOperation,
                               @FormDataParam("reschedule") boolean reschedule, @FormDataParam("password") String passwd, @FormDataParam("iter") int iter) {
      // System.out.println( p.getValue());
        if(resource != null && currentTime > -1 && "12345".equals(passwd) && iter > 0) {
            readSolutionFile(iter,reschedule);//read solution file, constraints and time recordings
            int job, op;
            int machine = ServiceRescheduleLessResources.getMachine(resource);
            int[] job_op = searchJobOP(machine, currentTime);//buscar que operation is
            job = job_op[0];
            op = job_op[1];

            //asignar a timeReSchedule el tiempo a empezar la resecuenciacion
            ql.Machines[machine].timeReSchedule = ql.Jobs[job].operations.get(op).initial_time + timeOperation;
           // System.out.println("Job "+job+" op "+op+" Time ReSchedule "+ ql.Machines[machine].timeReSchedule);
            Operation operation = ql.Jobs[job].operations.get(op);
            startTimes(operation,timeOperation);

            //New times of the operation
            operation.end_time = operation.initial_time + timeOperation; //new operation's time initial
            operation.proc_time = timeOperation;
            try {
                ql.ExecuteReSchedule();
            } catch (FileNotFoundException | CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return Response.status(200).entity("OK").build();
        }else{
            return Response.status(400).entity("Invalid form data").build();
        }
       // return Response.status(200).entity("OK").build();

    }

    private int[] searchJobOP(int machine, int currentTime) {
        int [] job_op= new int[2];
        for (int i = 0; i < ql.Jobs.length; i++){
            for (int j = 0; j < ql.Jobs[i].operations.size(); j++){
                if (ql.Jobs[i].operations.get(j).Ma == machine) { //si se ejecuto en la machine
                    if (ql.Jobs[i].operations.get(j).initial_time <= currentTime && ql.Jobs[i].operations.get(j).end_time >= currentTime){
                        job_op[0] = i;
                        job_op[1] = j;
                        break;
                    }
                }
            }
        }
        return job_op;
    }

    //leer el archivo solution para saber dónde es la interrupcion
    public void readSolutionFile(int iter, boolean reschedule) {
        File solution = reschedule ? new File(String.format("%s/solution_copy.tmp", UPLOAD_FOLDER))
                : new File(String.format("%s/solution.tmp", UPLOAD_FOLDER)); //Read before solution

        File[] targetFile = new File[2]; //files constraints and times recording
        targetFile[0] = new File(String.format("%s/targetFile.txt", UPLOAD_FOLDER));
        targetFile[1] = new File(String.format("%s/targetFile2.txt", UPLOAD_FOLDER));
        try {
            if (!reschedule) {
                File solutionCopy = new File(String.format("%s/solution_copy.tmp", UPLOAD_FOLDER)); //crear copia de la solution para reschedule
                FileUtils.copyFile(solution,solutionCopy); //copy solution to solution_copy
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        double LR = 0.1;
        double DF = 0.8;
        double epsi = 0.2;

        try {
            ql = new QLearning(targetFile, LR, DF, iter, epsi);
            ql.ReadData(targetFile);
            new Test(solution,ql);
           // ql.PrintJob();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startTimes(Operation operationFix, int newTime) {
        ArrayList <Operation> opNoModify = new ArrayList<>();
        for (int i = 0; i < ql.Jobs.length; i++) {
            for (int j = 0; j < ql.Jobs[i].operations.size(); j++) {
                if (ql.Jobs[i].operations.get(j).initial_time < operationFix.end_time) {
                    opNoModify.add(ql.Jobs[i].operations.get(j));
                }else {
                    //si no es la op fija y si tiene back to back
                    if (ql.Jobs[i].operations.get(j).back2back_before != -1 && !(i == operationFix.GetJob() && j == operationFix.GetID())) {
                        //si la operacion empieza despues de q acabe la op fija y su back to back empieza antes que acabe la fija
                        if (ql.Jobs[i].operations.get(j).initial_time >= operationFix.end_time && ql.Jobs[i].operations.get(j-1).initial_time < operationFix.end_time) {
                            //quito la op del array
                            ql.Jobs[i].operations.get(j).initial_time += (newTime + operationFix.proc_time);
                            ql.Jobs[i].operations.get(j).end_time += (newTime + operationFix.proc_time);
                            //opNoModify.
                            //opNoModify.remove(opNoModify.size()-1);
                        }
                    }
                }

                //agregarla si es la operation fija para q no se modifique
                if (i == operationFix.GetJob() && j == operationFix.GetID()) {
                    opNoModify.add(ql.Jobs[i].operations.get(j));
                }

                //si la fija tiene una operation que tiene q ir back to back con ella
                if (ql.Jobs[i].operations.get(j).back2back_before != -1 && (i == operationFix.GetJob() && operationFix.GetID() == ql.Jobs[i].operations.get(j).back2back_before)) {
                    ql.Jobs[i].operations.get(j).initial_time = operationFix.initial_time + newTime;
                    ql.Jobs[i].operations.get(j).end_time = ql.Jobs[i].operations.get(j).initial_time + ql.Jobs[i].operations.get(j).proc_time;
                    opNoModify.add(ql.Jobs[i].operations.get(j));
                    //	System.out.println("get back ");
                }
            }
        }

        //start times
        for (Operation operation : opNoModify) {
            //System.out.println(" job "+opNoModify.get(i).GetJob()+" op "+opNoModify.get(i).GetID()+" name "+ opNoModify.get(i).name+" Ma "+opNoModify.get(i).Ma);
            //time of machines
            if (operation.end_time > ql.Machines[operation.Ma].timeReSchedule)
                ql.Machines[operation.Ma].timeReSchedule = operation.end_time;

            //time of zones
            String job_operation_machine = "" + operation.GetJob() + operation.GetID() + operation.Ma;
            for (int j = 0; j < ql.zone.length; j++) {
                //si ocupa la zona y el tiempo actual de la zona es menor q el final de la operation
                if (ql.zone[j].job_operation_occupied.get(job_operation_machine).equals(true) && ql.zone[j].timeReScheduleZone < operation.end_time) {
                    ql.zone[j].timeReScheduleZone = operation.end_time;
                }
            }

            //operation para empezar re-schedule
            ql.Jobs[operation.GetJob()].opStart = operation.GetID() + 1;
            ql.Jobs[operation.GetJob()].temp_endtime = operation.end_time;
        }
    }

}
