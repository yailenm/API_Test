package org.ws.logic;

import java.util.ArrayList;


public class Operation implements Cloneable{
	
	private final int ID;
	private final int Job;
	public final String name;//new
	public int operation_precedent = -1;
	public int back2back_before = -1;
	public int initial_time;
	public int end_time;
	//public int[] zone_occupied = new int[3];//new
	
	//set of agents that can execute it...
	//machines tiene los indices originales de las maquinas en el fichero(empezando en 1)
	public int[] machines;
	
	//time it takes, corresponding to the machines
	public int[] times;
	
	// 1/p_times
	public double[] oamk;
	
	public double[] probabilities;
	
	//The machine that will execute it, the index in the array(not the real one)
	public Machine M;
	public int Ma;
	public int index_Ma;
	//The time it will take on the machine
	public int proc_time;
	
	//indice temporal para el Mode Optimization Procedure, es el indice de la maquina en machines
	public int temp_index;
	public int testing = 0;
	public int temp_initial, temp_end;
	
	public double[] QV;
	
	public Operation(int i, int j, String name){
		ID = i;
		Job = j;
		this.name = name;
	}
	
	public Operation(Operation o){
		ID = o.GetID();
		Job = o.GetJob();
		this.name = o.name;
	}

	public int GetID(){
		return ID;
	}
	
	public int GetJob(){
		return Job;
	}
	
	@Override
	 public Object clone() throws CloneNotSupportedException {
	        return super.clone();
	    }

	
		
	public int MachineSelection(double epsilon, Machine[] machs, int j_end_time, ArrayList<Operation> op){
		int min = 0;		
		int mindex = 0;
		//if es backtoback escoger directo
		if (this.back2back_before != -1) {
			//System.out.println("back2back_before");
			//System.out.println("job "+Job+" "+name+" back2back "+op[back2back_before].name);
			int m = op.get(back2back_before).Ma+1;
			//System.out.println("machine before"+m);
			for (int i = 0; i < machines.length; i++) {
				//System.out.println("machine "+machines[i]);
				if(machines[i] == m) {
					mindex = i;
					//System.out.println("find out "+machines[mindex]);
				}
			}
			//System.out.println("machine escogida "+machines[mindex]);
			//end_time+=times[mindex];
			end_time = op.get(back2back_before).end_time + times[mindex];
			temp_end= op.get(back2back_before).end_time + times[mindex];
			machs[machines[mindex]-1].minInitialM = end_time;
		}else {
		
		//Version que selecciona la maquina que primero la terminara (shortest end time, not SPT)
			double rnd = Math.random();
			
			if (rnd < 0.2){ //one at random
				//System.out.println("random");
				double rnd2 = Math.random();
				int rounded = Math.abs((int) Math.round(rnd2 * (machines.length-1)));
				//System.out.println("rounded "+machs[machines[rounded]-1].GetID());
				//Check if machine is active
				while (!machs[machines[rounded]-1].active) {
					rnd2 = Math.random();
					rounded = Math.abs((int) Math.round(rnd2 * (machines.length - 1)));

				}
				//System.out.println("rounded final "+machs[machines[rounded]-1].GetID());
				mindex = rounded;
					//end_time += times[mindex];
				//System.out.println("machs "+machs[machines[mindex]-1].GetID());
				int minGlobal =0;
				if(machs[machines[mindex]-1].Op_assigned.size() == 0)
					minGlobal = machs[machines[mindex]-1].time;
				else
					 minGlobal = (j_end_time > machs[machines[mindex]-1].Op_assigned.getLast().end_time) ? j_end_time : machs[machines[mindex]-1].Op_assigned.getLast().end_time;
				// minGlobal = (machs[machines[mindex]-1].Op_assigned.size()==0 ||j_end_time > machs[machines[mindex]-1].Op_assigned.getLast().end_time) ? j_end_time : machs[machines[mindex]-1].Op_assigned.getLast().end_time;
					end_time = minGlobal + times[mindex];
					temp_end= minGlobal + times[mindex];
					machs[machines[mindex]-1].minInitialM = end_time;
				
				//System.out.println("job "+Job+" "+name+" back2back ");
				//for (int i = 0; i < machines.length; i++) {
					//System.out.println("random machine "+machines[i]);
				//}
				
			}
			else{ //mejor opcion
				//System.out.println("mejor opcion");
		 		int i = 0;
				min = machs[machines[0]-1].GetLowestEndTime(this, j_end_time, times[0]);
				//Check if machine is active
				while (!machs[machines[i]-1].active) {
					i++;
					min = machs[machines[i] - 1].GetLowestEndTime(this, j_end_time, times[i]);
					mindex = i;
				}
				//System.out.println("Min final "+machs[machines[i]-1].GetID());
				for (int x=i+1; x < machines.length; x++){
					if(machs[machines[x]-1].active) {
						//System.out.println("entre machine "+machs[machines[x]-1].GetID());
						int aux = machs[machines[x] - 1].GetLowestEndTime(this, j_end_time, times[x]);
						if (aux < min) {
							min = aux;
							mindex = x;
						}
					}
				}	
				end_time = min;
				//System.out.println("mejor job "+Job+" "+name+" back2back ");
				
			}
		}
		proc_time = times[mindex];
		
		//machs[machines[mindex]-1].Op_assigned.add(this);
		//System.out.println("machine escogida "+machines[mindex]);
		return mindex;				
	}
	
	public void PrintQV(){
		for (int m=0; m < QV.length; m++)
			System.out.print("M"+ (machines[m]-1)+" "+ QV[m]+ "	");
	}
	
	
	/*public void UpdateQV(int index, double alpha){ 	// R es 1/proc_time
//		double r = (double) 1/proc_time;
//		QV[index] = QV[index] + alpha * (r - QV[index]);
		//double ac = (double) 1/M.work;
//		double ac = (double) 1/(M.work+proc_time);
		double r = (double) 1/proc_time + (double) 1/M.work;
		QV[index] = QV[index] + alpha * (r - QV[index]);
	}*/

	public void UpdateQVGlobal(double alpha, int cmax){ 	// R es 1/proc_time o 1/cmax
		//double r = (double) 1/cmax;
		
		//QV[Ma] = QV[Ma] + alpha * (r - QV[Ma]);
		
		// cuando el reward es 0 o 1
		QV[index_Ma] = QV[index_Ma] + alpha * (cmax - QV[index_Ma]) ;
		//System.out.print("QValue Op-"+GetID()+"_Ma-"+(M.GetID()+1)+" ="+QV[index_Ma]+"		");
	}
	
	
	
}
