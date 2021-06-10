package org.ws.logic;

import java.util.ArrayList;

public class Job implements Cloneable{
	
	private final int ID;
	public ArrayList <Operation> operations;
	public Operation ToTest = new Operation(20,20,"lol");//review
	public double[][] QValues;
	public int j_end_time;
	public boolean finished = false;
	public int jfulltime = 0;
	public int time_remaining;
	public int temp_endtime;
	public int aux_end = 0;	
	
	//public int limitOpFix = 0;
	public int opStart = 0; // operation para empezar el re-schedule
	
	
	public Job(int i){
		ID = i;
		
	}

	public int GetID(){
		return ID;
	}
	
	@Override
	public Job clone() throws CloneNotSupportedException {
		 	Job jj = (Job) super.clone();
		 	jj.ToTest = (Operation) ToTest.clone();
		 	jj.operations = new ArrayList<Operation>();
		 	for (int a=0; a < this.operations.size(); a++)
		 		jj.operations.set(a,(Operation) operations.get(a).clone());
		 	return jj;
    }

	
	public void PrintRoute(){
		for (int m=0; m < operations.size(); m++)
			System.out.print((operations.get(m).Ma+1)+ "	");
		System.out.println();
	}

	public void PrintQV(){
		for (int m=0; m < operations.size(); m++)
			operations.get(m).PrintQV();
		System.out.println();
	}
	
	
	public void Start(Machine[] machines){
		//System.out.println("Machine to activate: " + operations.get(0).Ma+ " Job "+operations.get(0).GetJob()+" op "+operations.get(0).GetID());
		boolean insert = true;
		int index = (opStart != -1)?opStart:0;
		if (opStart < operations.size()) {
			for (int i = 0; i < machines[operations.get(index).Ma].Queue.size(); i++) {
				if(machines[operations.get(index).Ma].Queue.get(i).equals(operations.get(index))) {
					insert = false;
				}
			}
			if(insert)
				machines[operations.get(index).Ma].Queue.add(operations.get(index));
		}
		
	}
	
	public void SendNext(int index, Machine[] machs){
		//chequear que haya una proxima!!!
		machs[operations.get(index+1).Ma].Queue.add(operations.get(index+1));
		//System.out.println("End time of the job "+ ID +" so far " + j_end_time);
	}

	public void startReChedule(Machine[] machines) {
		//System.out.println("Mando a la op "+opStart);
		machines[operations.get(opStart).Ma].Queue.add(operations.get(opStart));
		
	}
	
	
	
}
