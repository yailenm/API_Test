package org.ws.gui;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.ws.logic.QLearning;


/**
 * This class visualizes the Gantt chart of a given schedule(solution file) without executing an algorithm
 * It receives as parameters: text to show in the window, due date, and the path to the solution file
 * 
 * @author Yailen
 */

public class Test {

	public static PairQL<Instance,Schedule> loadSchedule(Instance instance, QLearning ql) throws IOException {
        //BufferedReader file = new BufferedReader(new FileReader(instance.SolFile));
        
        BufferedReader file = new BufferedReader(new FileReader(instance.loadedFile));

        ArrayList<OperationAllocation> allocs = new ArrayList<OperationAllocation>();

        //This is the makespan
        file.readLine();
        String line;
        ////this is the tardiness
        //instance.tardiness = file.readLine();
        
        //this is the number of machines, replacing the tardiness
        instance.numMachines = Integer.valueOf(file.readLine());
        instance.machines = new MachineGUI[instance.numMachines];
        //System.out.println("num machine "+instance.numMachines);
        MachineGUI M;
        
        while((line = file.readLine()) != null) {
            String[] numbers = line.split("\t");
            int jobId = Integer.valueOf(numbers[0]);
            int opId = Integer.valueOf(numbers[1]);
            int machineId = Integer.valueOf(numbers[3]);
            String opName = String.valueOf(numbers[2]);
            int start = Integer.valueOf(numbers[4]);
            int end = Integer.valueOf(numbers[5]);
            int backToBackBefore = Integer.valueOf(numbers[6]);
            int operation_precedent = Integer.valueOf(numbers[7]);
            int slack = Integer.valueOf(numbers[8]);
            if (ql != null) {
				ql.Jobs[jobId].operations.get(opId).initial_time = start;
				ql.Jobs[jobId].operations.get(opId).end_time = end;
                ql.Jobs[jobId].operations.get(opId).Ma = machineId;
                ql.Jobs[jobId].operations.get(opId).M = ql.Machines[machineId];
                for (int i = 0; i < ql.Jobs[jobId].operations.get(opId).machines.length; i++) { //search position of the machine selected into array machines
                    if (ql.Jobs[jobId].operations.get(opId).machines[i] == machineId+1) {
                        ql.Jobs[jobId].operations.get(opId).index_Ma = i;
                        ql.Jobs[jobId].operations.get(opId).proc_time = ql.Jobs[jobId].operations.get(opId).times[i];
                    }
                }
			}
            //Create Operation and Machine, and add it with start and end time
            JobGUI J = new JobGUI(jobId);
            OperationGUI O = new OperationGUI(opId, jobId, start, end, opName,backToBackBefore,operation_precedent);
            O.Mjob = J;
            O.setSlack(slack);
            if (instance.machines[machineId] == null){
            	M = new MachineGUI(machineId);
            	instance.machines[machineId] = M;
            }
            else 
            	M = instance.machines[machineId];
    
            allocs.add(new OperationAllocation(
                    O,
                    M,
                    start,
                    end,
                    false));
        }
        
        for (int m=0; m<instance.machines.length; m++)
        	if (instance.machines[m] == null)
        		instance.machines[m] = new MachineGUI(m);
        
        //System.out.println("ctdad de maquinas" + instance.machines.length);
        file.close();
        
        return new PairQL<Instance,Schedule>(instance,new Schedule(allocs));
    }

	
	public Test(File file) throws IOException {
		//Instance instance = new Instance("Schedule", 50, "Solutions/Mine/Solution-Constraints.dzn.txt");
		Instance instance = new Instance("Schedule", 50, file);
		
		//Instance instance = new Instance("Schedule", 50, file.getName(), 3);
		PairQL<Instance,Schedule> result = loadSchedule(instance,null);
	//	 ScheduleFrame sf = new ScheduleFrame(result.getFirst(), result.getSecond(), "Test",null,false);
	//	 sf.setVisible(true);
	}
	
	public Test(File file,QLearning ql) throws IOException {
		//Instance instance = new Instance("Schedule", 50, "Solutions/Mine/Solution-Constraints.dzn.txt");
		Instance instance = new Instance("Schedule", 50, file);
        //System.out.println("File Test "+file.getName());
		//Instance instance = new Instance("Schedule", 50, file.getName(), 3);
		PairQL<Instance,Schedule> result = loadSchedule(instance,ql);
	//	 ScheduleFrame sf = new ScheduleFrame(result.getFirst(), result.getSecond(), "Test",ql,false);
	//	 sf.setVisible(true);
	}
   
//	public static void main(String[] args) throws IOException {
//		Instance instance = new Instance("Schedule", 50, "Solutions/Mine/Solution-Constraints.dzn.txt", 3);
//		Pair<Instance,Schedule> result = loadSchedule(instance);
//		 ScheduleFrame sf = new ScheduleFrame(result.getFirst(), result.getSecond(), "Test");
//		 sf.setVisible(true);
//
//	}

	
//	public static void main(String[] args) throws FileNotFoundException {
//		 boolean releaseDates = false;
//		 String path_file = "Data-FJSSP/Brandimarte_Data/Text/Mk01.fjs";
//		 String path_sol = "Solutions/Test.txt";
//		 ScheduleReader scheduleReader=new ScheduleReader();
//		 File problemFile=new File(path_file);
//		 FJSPProblem fjspProblem = FJSReader.readFJSPProblemFromFile(problemFile, releaseDates);
//		 File solutionFile=new File(path_sol);
//		 Schedule solution = scheduleReader.readSolutionForProblem(problemFile, solutionFile,releaseDates);
//		 ScheduleFrame scheduleFrame=new ScheduleFrame(fjspProblem, solution);
//		 
//	}

	
}


//CHANGE THE TEST CLASS!!!!!!!!