package org.ws.gui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;



public class Instance {
	private String name;
    public  int numMachines;  
    public  int numJobs;
    public  MachineGUI[] machines;
    public int DueDate;
    public String SolFile; 
    public File loadedFile;
    public String tardiness;
    public LinkedList<Perturbation> PerturbationList;
    
    public Instance(String fileName, int duedate, String solution, LinkedList<Perturbation> PertList, int nmachines) throws IOException {
       name = fileName;
       DueDate = duedate;
       SolFile = solution;
       //SolFile = solution.getName();
       loadedFile = new File(solution);
       PerturbationList = PertList;
       machines = new MachineGUI[nmachines];
    }
    
    public Instance(String fileName, int duedate, String solution, int nmachines) throws IOException {
        name = fileName;
        DueDate = duedate;
        SolFile = solution;
        //SolFile = solution.getName();
        loadedFile = new File(solution);
        machines = new MachineGUI[nmachines];
     }
    
    public Instance(String fileName, int duedate, File solution) throws IOException {
        name = fileName;
        DueDate = duedate;
        SolFile = solution.getName();
        loadedFile = solution;
     }
    
	public String getName() {
    	return name;
    }
    
	public LinkedList<Perturbation> getPerturbationList(){
		return PerturbationList;
	}
	
    public MachineGUI[] getMachines() {
    	return Arrays.copyOf(machines, machines.length);
    }
    
    public List<MachineGUI> getMachineList() {
    	return Arrays.asList(getMachines());
    }
}
