//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ws.parsepdf;

import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.FileUtils;
import org.ws.gui.Test;
import org.ws.logic.Job;
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

@Path("/lessResources")
public class ServiceRescheduleLessResources {
    private static final String UPLOAD_FOLDER = System.getProperty("java.io.tmpdir") + "/uploaded";
    private QLearning ql;

    @Context
    private UriInfo context;

    public ServiceRescheduleLessResources() {
    }

    @POST
    @Consumes({"multipart/form-data"})
    @Produces({"application/json"})
    //Parametros: nombre del recurso a salir, time
    public Response uploadFile(@FormDataParam("resource") String resource, @FormDataParam("currentTime") int currentTime,
                               @FormDataParam("iter") int iter, @FormDataParam("reschedule") boolean reschedule) {

        if(resource != null && currentTime > -1 && iter > 0) {
            boolean solution = readSolutionFile(iter,reschedule);//read solution file, constraints and time recordings
            if (solution){
                int machine = getMachine(resource); //search id of machine
                for (int i = 0; i < ql.Machines.length; i++){
                    ql.Machines[i].timeReSchedule = currentTime;
                }
                ql.Machines[machine].active = false;
                startTimes(machine,currentTime);
                try {
                    ql.ExecuteReSchedule();
                } catch (FileNotFoundException | CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                File solution1 = new File(String.format("%s/schedule.txt", UPLOAD_FOLDER));
                return Response.status(200).entity(solution1).build();
            }else{
                return Response.status(400).entity("There are no previous schedules").build();
            }
        }else
            return Response.status(400).entity("Missing parameters").build();

    }

    static int getMachine(String resource) {
        int machine;
        switch (resource){
            case "robotL": machine = 0; break;
            case "robotR": machine = 1; break;
            default: machine = 2; break;
        }
        return machine;
    }

    //leer el archivo solution para saber dónde es la interrupcion
    public boolean readSolutionFile(int iter, boolean reschedule){
        File solution = null;
        File[] targetFile = new File[2]; //files constraints and times recording
        try {
            solution = reschedule ? new File(String.format("%s/solution_copy.tmp", UPLOAD_FOLDER))
                    : new File(String.format("%s/solution.tmp", UPLOAD_FOLDER)); //Read before solution

            targetFile[0] = new File(String.format("%s/targetFile.txt", UPLOAD_FOLDER));
            targetFile[1] = new File(String.format("%s/targetFile2.txt", UPLOAD_FOLDER));

            if (!reschedule && solution.exists()) {
                File solutionCopy = new File(String.format("%s/solution_copy.tmp", UPLOAD_FOLDER)); //crear copia de la solution para reschedule
                FileUtils.copyFile(solution,solutionCopy); //copy solution to solution_copy
            }

        } catch (FileNotFoundException e) {
            Response.status(500).entity("No existen ficheros soluciones en el server").build();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double LR = 0.1;
        double DF = 0.8;
        double epsi = 0.2;

        try {
            if (solution.exists()){
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

    private void startTimes(int machine, int currentTime) {
       System.out.println("start Times machine "+machine);
        //Poner alguna forma de saber q la op anterior no fue añadida....y asi no añadir la prox aunq este en otra machine y con buen time
        ArrayList<Operation> opNoModify = new ArrayList<>();
        for (int i = 0; i < ql.Jobs.length; i++) {
            for (int j = 0; j < ql.Jobs[i].operations.size(); j++) {
                System.out.println("job "+ql.Jobs[i].GetID()+" op "+ql.Jobs[i].operations.get(j).GetID()+" Ma "+ ql.Jobs[i].operations.get(j).Ma);
                if (ql.Jobs[i].operations.get(j).end_time <= currentTime ||
                        (ql.Jobs[i].operations.get(j).initial_time < currentTime && ql.Jobs[i].operations.get(j).Ma != machine)) {
                    opNoModify.add(ql.Jobs[i].operations.get(j));
                    System.out.println("add 1 job "+ql.Jobs[i].GetID()+" op "+ql.Jobs[i].operations.get(j).GetID());
                }else if (ql.Jobs[i].operations.get(j).back2back_before != -1){ //si has back to back
                    //si la op back2back_before starts before current time y isn't the broken machine
                    if (ql.Jobs[i].operations.get(ql.Jobs[i].operations.get(j).back2back_before).initial_time <= currentTime
                            && ql.Jobs[i].operations.get(j).Ma != machine) {
                        opNoModify.add(ql.Jobs[i].operations.get(j));
                        System.out.println("add 2 job "+ql.Jobs[i].GetID()+" op "+ql.Jobs[i].operations.get(j).GetID());
                    }
                    //si la op back2back_before ends before current time y it's the broken machine
                    if (ql.Jobs[i].operations.get(ql.Jobs[i].operations.get(j).back2back_before).end_time <= currentTime
                            && ql.Jobs[i].operations.get(j).Ma == machine) {
                         //quito la op del array
                        System.out.println("remove job "+opNoModify.get(opNoModify.size()-1).GetJob()+" op "+opNoModify.get(opNoModify.size()-1).GetID());
                       opNoModify.remove(opNoModify.size()-1);
                        break;
                    }
                }else break;

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
            //ql.Jobs[opNoModify.get(i).GetJob()].temp_endtime = opNoModify.get(i).end_time;
            ql.Jobs[operation.GetJob()].temp_endtime = currentTime;
            ql.Jobs[operation.GetJob()].finished = ql.Jobs[operation.GetJob()].opStart >= ql.Jobs[operation.GetJob()].operations.size();
        }
        /*for (Job j: ql.Jobs) {
            System.out.println("job "+j.GetID()+" opStart "+j.opStart+" time "+j.operations.get(j.opStart-1).end_time);
        }*/
        //System.out.println(" job 0 opStart "+ ql.Jobs[0].opStart);
        //System.out.println(" job 1 opStart "+ ql.Jobs[1].opStart);
    }

}
