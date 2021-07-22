package org.ws.logic;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

public class Machine implements Cloneable{

	private final int ID;
	public LinkedList<Operation> Queue;
	public LinkedList<Operation> Op_executed;
	public LinkedList<Operation> Op_executed_Optim;
	public int time = 0;
	public double[][] QValues;
	public LinkedList<Operation> TempOrderedList;
	public LinkedList<Operation> Op_assigned;
	int minInitialM = 0;
	public int initial_time_machine = Integer.MAX_VALUE, initial_time_final = 0;
	public int end_time_machine = 0;
	public Operation firstOp;
	
	public int timeReSchedule = -1;

	public boolean active = true;
	
	//amount of work so far
	public int work=0;
	
	public Machine(int p_id) {
		ID = p_id;
		Queue = new LinkedList<>();
		Op_executed = new LinkedList<>();
		Op_executed_Optim = new LinkedList<>();
		Op_assigned = new LinkedList<>();
		TempOrderedList = new LinkedList<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	 protected Machine clone() throws CloneNotSupportedException {
		Machine MM = (Machine) super.clone();
	    MM.Queue = new LinkedList<>();
	    MM.Queue = (LinkedList<Operation>) Queue.clone();
		return MM;
	 }

	
	public int GetID(){
		return ID;
	}
	
	public void PrintQueue(){
		for (Operation operation : Queue)
			System.out.print("Job:" + operation.GetJob() + "-Op:" + operation.GetID() + " 	");
		System.out.println();
	}
	
	
	public void Locate_Op_OrderedList(Operation op){
		boolean located = false;
		int i=0;
		while (!located){
			// if the List is still empty or this value is higher than the highest so far, then add it
			if ((TempOrderedList.isEmpty()) || (TempOrderedList.getLast().temp_end > op.temp_end)){  
				TempOrderedList.add(op);
				located = true;
			}
			else{
				if (op.temp_end < TempOrderedList.get(i).temp_end)
					i++;
				else{
					TempOrderedList.add(i, op);
					located = true;
				}
			}
		}
	}
	
	
	//Gets Lowest End Time for this machine
	
	public int GetLowestEndTime(Operation Oper, int j_end, int time_slot){
		int minInitialJ, minGlobal, endTime=0;
		boolean located = false;
		
		minInitialJ = j_end;
		TempOrderedList.clear();

		TempOrderedList.addAll(Op_assigned);
		
		minInitialM = (Op_assigned.size()>0)?Op_assigned.getLast().end_time:time;//new
		//System.out.println("Maquina " + ID + " minInitialM "+minInitialM);
		minGlobal = Math.max(minInitialJ, minInitialM);
		
		if ((TempOrderedList.isEmpty())||
				((TempOrderedList.getFirst().temp_initial >= time_slot) && (TempOrderedList.getFirst().temp_initial >= minGlobal + time_slot))){
			endTime = minGlobal + time_slot;
			Oper.temp_initial = minGlobal;
			Oper.temp_end = endTime;
			located = true;
		}
		else{
			for (int i=0; i < TempOrderedList.size()-1; i++){
				  int aux = TempOrderedList.get(i+1).temp_initial - TempOrderedList.get(i).temp_end;
				  //minInitialM = (Op_assigned.size()>0)?TempOrderedList.get(i).temp_end+Op_assigned.getLast().end_time:TempOrderedList.get(i).temp_end;
				  minInitialM = TempOrderedList.get(i).temp_end;
				 // System.out.println("minInitialM down "+minInitialM);
				  minGlobal = Math.max(minInitialJ, minInitialM);
				//si la dif entre en inicial de una y el final de la otra es de tama�o del slot necesario entonces devuelvo pos y tiempo inicial
					//if (aux >= time_slot) {
					if ((aux >= time_slot) && ((minGlobal+time_slot) <= TempOrderedList.get(i+1).temp_initial)){
						//it fits here
						endTime = minGlobal + time_slot;
						Oper.temp_initial = minGlobal;
						Oper.temp_end = endTime;
						located = true;
					}
				}
			}
		
		
		if (!located){
			minInitialM = TempOrderedList.getLast().temp_end;
			minGlobal = Math.max(minInitialJ, minInitialM);
			endTime = minGlobal + time_slot;
			Oper.temp_initial = minGlobal;
			Oper.temp_end = endTime;
			//System.out.println("anterior " + TempOrderedList.getLast().GetJob() + " op " + TempOrderedList.getLast().GetID() + " located "+minInitialM);
		}
		
		minInitialM = endTime;
		//System.out.println("minInitialM final "+minInitialM);
		return endTime;
		
	}
	
	public static double roundToDecimals(double d, int c) {
		int temp=(int)((d*Math.pow(10,c)));
		return (((double)temp)/Math.pow(10,c));
		}
	
	
	public void PrintQValues(){
		for (double[] qValue : QValues) {
			for (double v : qValue) System.out.print(v + "	");
			System.out.println();
		}
	}
	

	public void SaveQValues2File(PrintWriter pw){
		for (double[] qValue : QValues) {
			// pw.print(QValues[q][p]+"		");
			for (double v : qValue) pw.print(roundToDecimals(v, 2) + "		");
			pw.println();
		}
	}
	
	
	
	public double MaxQVQueue(){
		double max =0;
		if (!Queue.isEmpty()){
		max = QValues[Queue.get(0).GetJob()][Queue.get(0).GetID()];
		for(int x=1; x < Queue.size(); x++)
			 if (QValues[Queue.get(x).GetJob()][Queue.get(x).GetID()] > max){
				  	max = QValues[Queue.get(x).GetJob()][Queue.get(x).GetID()];
				  }
		}
		return max;
	}
	
	public double MinQVQueue(){
		double min =0;
		if (!Queue.isEmpty()){
		min = QValues[Queue.get(0).GetJob()][Queue.get(0).GetID()];
		for(int x=1; x < Queue.size(); x++)
			 if (QValues[Queue.get(x).GetJob()][Queue.get(x).GetID()] < min){
				  	min = QValues[Queue.get(x).GetJob()][Queue.get(x).GetID()];
				  }
		}
		return min;
	}
	
	public void PrintOrder(){
		for (Operation operation : Op_executed)
			System.out.print("Job:" + operation.GetJob() + "-Op:" + operation.GetID() + " 	");
		System.out.println();
	}
	

	public Operation ActionSelection(Job [] Jobs, double epsilon, double alpha,Zone[]zone, int[]jobsInExecute){	

		//Learning
		Operation op = null;
		boolean zone_occupied = false;
		ArrayList<Integer> arrayZone = new ArrayList<>();
		int timeZone = 0;
		int minTimePossible;
		boolean flag = true;
		if (Op_executed.size() != 0) {// back to back
			for (Operation operation : Queue) {
				int job = operation.GetJob();
				int back2back = operation.back2back_before;
				if (Op_executed.getLast().GetJob() == job && Op_executed.getLast().GetID() == back2back) {//if the laster job has back to back
					op = operation;
					//System.out.println(" hay backToback "+" job "+job+" op "+ op.GetID());
					String array = "" + op.GetJob() + op.GetID() + ID;

					//buscar la zona y ver precedencia
					for (int i = 0; i < zone.length; i++) {
						if (zone[i].job_operation_occupied.get(array).equals(true)) {
							zone_occupied = true;
							arrayZone.add(i);
							if (timeZone < zone[i].time) {
								timeZone = zone[i].time;
							}
							//System.out.println("ocupa la zona "+(i+1)+" time zone "+zone[i].time+" nuevo time "+timeZone);
						}
					}
					if (zone_occupied) {
						if (op.operation_precedent > -1) {
							int time_precedent = Jobs[op.GetJob()].operations.get(op.operation_precedent).end_time;
							minTimePossible = Math.max(timeZone, time_precedent);//max between time zone and time precedent
							op.initial_time = Math.max(minTimePossible, time);//max between minTimePossible and time of machine
							//System.out.println(" Tiene de precedent "+op.operation_precedent+" time final precedent "+time_precedent);
							/*if (minTimePossible > time) {//si el tiempo final del anterior(=time machine)es menor q minTimePOssible pegar a esta operacion
								op.initial_time = minTimePossible;


								//System.out.println("maximo entre la zona y el precedente es mayor que time del backtoback. "+time+" Se corre la op anterior "+" initial time "+Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).initial_time+" final "+Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).end_time );
							} else {
								//System.out.println("maximo entre el time de la zona y el del precedente no es mayor que time del backtoback. Se pone atras del backtoback "+time);
								op.initial_time = time;
							}*/
						} else {
							op.initial_time = Math.max(timeZone, time);
							//System.out.println("no tiene precedente la operacion time initial "+op.initial_time);
							/*if (op.initial_time > time) {//si el tiempo final del anterior(=time machine)es menor q minTimePOssible pegar a esta operacion
								//op.initial_time = minTimePossible;
								boolean zoneOccup = checkZonePrecedent(Op_executed.getLast(),zone);//check if precedent occupies any zone
								if (!zoneOccup) {
									Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).end_time = op.initial_time;
									Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).initial_time = op.initial_time - Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).proc_time;
								}
								//System.out.println("El tiempo inicial es mayor q el time final de la anterior. Se corre la op anterior "+" initial time "+Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).initial_time+" final "+Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).end_time );
							}*/
							//System.out.println("no tiene precedente la operacion. Se pone pegada al back to Back "+time);
							//time = op.initial_time + op.proc_time;
						}
						//int minTimePossible2 = (Jobs[op.GetJob()].j_end_time > time) ? Jobs[op.GetJob()].j_end_time : time;
						//op.initial_time = (minTimePossible > minTimePossible2) ? minTimePossible : minTimePossible2;
						time = op.initial_time + op.proc_time;
						for (Integer integer : arrayZone) {
							zone[integer].time = op.initial_time + op.proc_time;
							//System.out.println("Tiempo nuevo d la zone "+zone[arrayZone.get(j)].id_zone+" es de "+zone[arrayZone.get(j)].time);
						}
					} else {//No ocupa ninguna zona
						if (op.operation_precedent > -1) {
							int time_precedent = Jobs[op.GetJob()].operations.get(op.operation_precedent).end_time;
							op.initial_time = Math.max(time_precedent, time);
							/*boolean zoneOccup = checkZonePrecedent(Op_executed.getLast(),zone);//check if precedent occupies any zone
							if (!zoneOccup){
								//mover op anterior del mismo trabajo para q termine justo cuando empieza si backtoback
								Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).end_time = op.initial_time;
								Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).initial_time = op.initial_time - Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).proc_time;
								if (firstOp.GetID() == Op_executed.getLast().GetID() && firstOp.GetJob() == Op_executed.getLast().GetJob())
									initial_time_machine = Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).initial_time;
							}*/

						} else {
							op.initial_time = time;

							//System.out.println("Llego al final y no ocupa zona lo pongo en el time de la machine. Time initial op "+op.initial_time+" time maquina "+time);
						}
						time = op.initial_time + op.proc_time;
					}

					boolean zoneOccup = checkZonePrecedent(Op_executed.getLast(),zone);//check if precedent occupies any zone
					if (!zoneOccup){
						Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).end_time = op.initial_time;
						Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).initial_time = op.initial_time - Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).proc_time;
						if (firstOp.GetID() == Op_executed.getLast().GetID() && firstOp.GetJob() == Op_executed.getLast().GetJob())
							initial_time_machine = Jobs[op.GetJob()].operations.get(Op_executed.getLast().GetID()).initial_time;
					}
					flag = false;
					break;
				}
			}
		}
		//PW.println();
		//PW.flush();
		
		
		//Now print the QValues, or maybe somewhere closer to the corresponding action	
		if (flag) {
			boolean flash = true;
			while (flash) {
				double rnd = Math.random();
				if (rnd < epsilon){	//Select a random machine
					double rnd2 = Math.random();
					int index = Math.abs((int) Math.round(rnd2 * (Queue.size()-1)));
					op = Queue.get(index);
					//System.out.println("Escogi po random. Job "+op.GetJob()+" op "+op.GetID());
				}else { //Select the best action, SPT!! pero necesito saber las posibilidades para solo analizar esos Q_values!!!!!!
					
					double max = QValues[Queue.get(0).GetJob()][Queue.get(0).GetID()];
					int index = 0;
					for (int q=1; q < Queue.size(); q++) {
					  if (QValues[Queue.get(q).GetJob()][Queue.get(q).GetID()] > max){
					  	max = QValues[Queue.get(q).GetJob()][Queue.get(q).GetID()];
					  	index = q;
					  }
					}
					op = Queue.get(index);	
					//System.out.println("Escogi la mejor. Job "+op.GetJob()+" op "+op.GetID());
				}
				flash = false;//revisar
				//if ((jobsInExecute[0] == 0 || op.GetJob() == jobsInExecute[0]-1) && op.GetJob() != jobsInExecute[1]-1) {//colocar el trabajo en la posicion 0 si no hay ninguno
				if (jobsInExecute[0] == -1 && op.GetJob() != jobsInExecute[1]) {//colocar el trabajo en la posicion 0 si no hay ninguno
					jobsInExecute[0] = op.GetJob();
					//System.out.println(" coloque el trabajo en la posicion 0 "+ op.GetJob());
				}else if (jobsInExecute[1] == -1 && op.GetJob() != jobsInExecute[0]) {//colocar el trabajo en la posicion 1 si no hay ninguno
					jobsInExecute[1] = op.GetJob();
					//System.out.println(" coloque el trabajo en la posicion 1 "+op.GetJob());
				}else {
					if(op.GetJob() != jobsInExecute[1] && op.GetJob() != jobsInExecute[0]){
						flash = true;
						Queue.remove(op);//eliminar la operacion que no pertenece a ninguno de los trabajos q se estan ejecutando
						//System.out.println(" elimine la op de la queue del job "+op.GetJob());
						
						if (Queue.isEmpty()) {
							op = null;
							flash = false;
							//System.out.println(" esta vacia la cola");
						}
					}
				}
				
			}
			
			if (op!=null) {
				String array = ""+op.GetJob()+ op.GetID()+ID;
				
				//search by zone 
				for (int i = 0; i < zone.length; i++) {
					if (zone[i].job_operation_occupied.get(array).equals(true)) {
						zone_occupied = true;
						arrayZone.add(i);
						if (timeZone < zone[i].time) {
							timeZone = zone[i].time;
						}
						//System.out.println("Ocupa la zona "+(i+1)+" time zone "+zone[i].time);									
					}
				}			
					
				if (zone_occupied) {
					if (op.operation_precedent > -1) {//check precedent						
						int time_precedent = Jobs[op.GetJob()].operations.get(op.operation_precedent).end_time;
						minTimePossible = Math.max(timeZone, time_precedent);//max between time zone and time precedent
						//System.out.println("Tiene de precedente "+op.operation_precedent+" time precedent "+time_precedent+" max entre el precedent y la zona "+minTimePossible);
					}else{
						minTimePossible = timeZone;
						//System.out.println("No tiene de precedente. Se toma el min de la zona "+minTimePossible);
					}
					//int minTimePossible2 = (Jobs[op.GetJob()].j_end_time > time) ? Jobs[op.GetJob()].j_end_time : time;
					//System.out.println("Busco el max entre la maq "+time+" time minTimePossible "+minTimePossible);
					//op.initial_time = (minTimePossible > minTimePossible2) ? minTimePossible : minTimePossible2;
					op.initial_time = Math.max(minTimePossible, time);
					op.initial_time = Math.max(op.initial_time, Jobs[op.GetJob()].temp_endtime);
					//System.out.println("Tiempo initial final "+op.initial_time);
					time = op.initial_time + op.proc_time;
					for (Integer integer : arrayZone) {//update zones
						zone[integer].time = time;
						//System.out.println("Tiempo nuevo d la zone "+zone[arrayZone.get(j)].id_zone+" es de "+zone[arrayZone.get(j)].time);
					}				
				}else{
					if (op.operation_precedent > -1) {//check precedent						
						int time_precedent = Jobs[op.GetJob()].operations.get(op.operation_precedent).end_time;
						op.initial_time = Math.max(time_precedent, time);
						op.initial_time = Math.max(op.initial_time, Jobs[op.GetJob()].temp_endtime);
						//System.out.println("Tiempo initial final "+op.initial_time);
					}else {
						op.initial_time = time;
						op.initial_time = Math.max(op.initial_time, Jobs[op.GetJob()].temp_endtime);
					}
					time = op.initial_time + op.proc_time;
					//System.out.println("no ocupa zona lo pongo en el time de la machine. Time initial op "+op.initial_time+" time maquina "+time);
				}
			}
			//System.out.println(op.initial_time);
		}

		return op;
	}

	private boolean checkZonePrecedent(Operation op, Zone[] zone) {
		boolean zone_occupied = false;
		String array = "" + op.GetJob() + op.GetID() + ID;
		//buscar la zona
		for (int i = 0; i < zone.length; i++) {
			if (zone[i].job_operation_occupied.get(array).equals(true)) {
				zone_occupied = true;
				break;
			}
		}
		return zone_occupied;
	}


}
