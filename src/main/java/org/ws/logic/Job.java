package org.ws.logic;

import java.util.ArrayList;

public class Job implements Cloneable{
	
	private final int ID;
	private int number;
	public ArrayList <Operation> operations;
	public Operation ToTest = new Operation(20,20,"lol");//review
	public double[][] QValues;
	public int j_end_time;
	public boolean finished = false;
	public int time_remaining;
	public int temp_endtime,endTimeRechedule;
	public int aux_end = 0;	
	

	public int opStart = 0; // operation para empezar el re-schedule
	
	
	public Job(int i, int number){
		ID = i;
		this.number = number;
	}

	public int GetID(){
		return ID;
	}

	public int getNumber() {
		return number;
	}
	public void setNumber(int number){
		this.number = number;
	}

	@Override
	public Job clone() throws CloneNotSupportedException {
		 	Job jj = (Job) super.clone();
		 	jj.ToTest = (Operation) ToTest.clone();
		 	jj.operations = new ArrayList<>();
		 	for (int a=0; a < this.operations.size(); a++)
		 		jj.operations.set(a,(Operation) operations.get(a).clone());
		 	return jj;
    }

	
	public void PrintRoute(){
		for (Operation operation : operations)
			System.out.print((operation.Ma + 1) + "	");
		System.out.println();
	}

	public void PrintQV(){
		for (Operation operation : operations)
			operation.PrintQV();
		System.out.println();
	}
	
	
	public void Start(Machine[] machines){
		//System.out.println("Machine to activate: " + operations.get(0).Ma+ " Job "+operations.get(0).GetJob()+" op "+operations.get(0).GetID());
		boolean insert = true;
		//int index = (opStart != -1)?opStart:0;
		int index = opStart;
		if (opStart < operations.size()) {
			for (int i = 0; i < machines[operations.get(index).Ma].Queue.size(); i++) {
				if(machines[operations.get(index).Ma].Queue.get(i).equals(operations.get(index))) {
					insert = false;
				}
			}
			if(insert) {
				machines[operations.get(index).Ma].Queue.add(operations.get(index));
				//System.out.println("Job add "+ID);
			}
		}
		
	}
	
	public void SendNext(int index, Machine[] machs){
		//chequear que haya una proxima!!!
		machs[operations.get(index+1).Ma].Queue.add(operations.get(index+1));
		//System.out.println("End time of the job "+ ID +" so far " + j_end_time);
	}

	public void startReChedule(Machine[] machines) {
		//System.out.println("Mando a la op "+opStart+" job "+ID);
		machines[operations.get(opStart).Ma].Queue.add(operations.get(opStart));
		
	}
	
	
	
}
