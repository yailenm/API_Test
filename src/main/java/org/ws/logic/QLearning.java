package org.ws.logic;

import javax.ws.rs.core.Response;
import java.io.*;
import java.time.Instant;
import java.util.*;




public class QLearning {

	private final double LearningRate;
	private final double DiscountFactor;
	private final int iterations;
	private final double epsilon;
	private final String filename;
	private int njobs, nmachines, navg_op, njobsNew;
	public Job[] Jobs;
	public Zone[] zone;//new
	int countZones;//new
	public Machine[] Machines;
	private int BestSol;
	public int max_num_operations;
	public LinkedList<Operation> OrderedList;
	public File instance;
	String file_saved;
	public LinkedList<Operation> FullOperationList;
	public LinkedList<Operation> HabOperationList;

	String UPLOAD_FOLDER = System.getProperty("java.io.tmpdir") + "/uploaded";
	private final Hashtable<String, Integer> actions = new Hashtable<>();
	public int[] jobsInExecute = new int[2];

	public QLearning(File[] file, double LearningRate, double DiscountFactor, int cycles, double epsilon){
		this.LearningRate = LearningRate;
		this.DiscountFactor = DiscountFactor;
		this.epsilon = epsilon;
		this.iterations = cycles;
		this.filename = file[0].getName();
		this.max_num_operations = 0;
		OrderedList = new LinkedList<>();
		FullOperationList = new LinkedList<>();
		HabOperationList = new LinkedList<>();
	}



	@SuppressWarnings("resource")
	public void ReadData(File[] file) throws IOException, NullPointerException, ArrayIndexOutOfBoundsException {
		//System.out.println("Name "+file[0].getName().toString()+" "+file[1].getName());


		String s;
		FileReader f, f1;
		BufferedReader a1, a;

		//verify if file[1] is timeRecordings
		//File instance2 = file[1];
		String s1 = new String();
		f1 = new FileReader(file[1]);
		a1 = new BufferedReader(f1);
		s = a1.readLine();
		if (s.contains("actionDurationTable")) {
			f = new FileReader(file[0]);
			instance = file[0];
			//System.out.println("op a "+file[0].getName());
			//System.out.println("a1 "+file[1].getName());
		} else {
			f = new FileReader(file[1]);
			f1 = new FileReader(file[0]);
			a1 = new BufferedReader(f1);
			a1.readLine();
			//System.out.println("a "+file[1].getName());
			//System.out.println("a1 "+file[0].getName());
			//a1.readLine();
			instance = file[1];
		}
		a = new BufferedReader(f);

		//Read First Line
		a.readLine();
		a.readLine();
		a.readLine();
		a.readLine();
		//System.out.println(a.readLine());
		// operations
		a.readLine();
		//System.out.println(s);
		//	int countOperations = s.split(",").length;
		//	System.out.println(" size "+countOperations);

		// Robot left, robot right, operator
		s = a.readLine();
		//System.out.println(s);
		String[] cadArray1 = s.split(",");
		nmachines = cadArray1.length;
		Machines = new Machine[nmachines];
		//System.out.println(nmachines);
		// products
		s = a.readLine();
		//System.out.println(s);
		cadArray1 = s.split("=");
		String[] cadArray2 = cadArray1[1].split("[..]+");
		String[] cadArray = cadArray2[1].split(";");
		njobs = njobsNew = Integer.parseInt(cadArray[0]);
		Jobs = new Job[njobs];
	 /*	if (njobs >= 2) {
			lastJob = 2;
		}else
			lastJob = 1;*/
		//System.out.println(" size "+cadArray2.length+" "+cadArray1[1].toString());

		// zones
		s = a.readLine();
		//System.out.println(s);
		cadArray1 = s.split("=");
		cadArray2 = cadArray1[1].split("[..]+");
		cadArray = cadArray2[1].split(";");
		countZones = Integer.parseInt(cadArray[0]);
		zone = new Zone[countZones];

		a.readLine();
		a.readLine();
		/*a.readLine();*/
		//System.out.println(a.readLine());
		//System.out.println(a.readLine());

		//a.readLine();


		//System.out.println(" product "+ njobs+" zones "+countZones+" operations "+countOperations);

		//int tmp_zone = 0;

		//count zones
		for (int i = 0; i < countZones; i++) {
			a.readLine();
			//System.out.println(+" 3");
			zone[i] = new Zone(i + 1);
			//count products
			for (int j = 0; j < njobs; j++) {
				if (i == 0) {
					Jobs[j] = new Job(j);
					Jobs[j].operations = new ArrayList<>();
					//	System.out.println(a1.readLine());// read product
					s = a1.readLine();// read product
					//System.out.println(s);
				}
				//Read product name
				//System.out.println(a.readLine()+" 6");
				a.readLine(); //product name

				//System.out.println(" zone "+ i);

				//System.out.println(j);
				//System.out.println(" product "+zone[i].product.length +" zones "+zone.length+" operations "+zone[i].product[j].operations.length);

				//count operation
				s = a.readLine();//First operation

				int k = 0; //number of operations
				char ch1 = ']';
				while (!s.equals("") && s.charAt(0) != ch1) {
					boolean zone_occupied; // value Hashtable
					//System.out.println(s+" read");
					cadArray1 = s.split("%");
					cadArray = cadArray1[0].split(",");


					for (int l = 0; l < nmachines; l++) {
						String job_operation_machine = "" + j + k + l;
						//System.out.println("ppp "+job_operation_machine);
						zone_occupied = !cadArray[l].equals("0");
						zone[i].job_operation_occupied.put(job_operation_machine, zone_occupied);//fill in Hashtable
						//System.out.println(zone[i].job_operation_occupied.get(job_operation_machine));
						// System.out.println("job "+ j+" operation "+k+" machine "+l+" zone occupied "+ zone_occupied+" boolean ");
					}

					//read times
					if (i == 0) {
						s1 = a1.readLine();// read operation
						//System.out.println(s1);
						cadArray1 = s1.split("[ \t]+");
						cadArray = cadArray1[0].split(",");
						ArrayList<Integer> arrayTimes = new ArrayList<>();// fill in with times > 0


						for (String value : cadArray) {
							//System.out.println(" cadArray "+ cadArray[k2]);
							if (!value.equals("0")) {
								//System.out.println(" izq "+ cadArray[k2]);
								arrayTimes.add(Integer.parseInt(value));
							}
						}

						Jobs[j].operations.add(new Operation(k, j, cadArray1[2]));
						actions.put(cadArray1[2], k);
						//	System.out.println(" name "+ cadArray1[2]);
						Jobs[j].operations.get(k).machines = new int[arrayTimes.size()];
						Jobs[j].operations.get(k).times = new int[arrayTimes.size()];
						Jobs[j].operations.get(k).QV = new double[arrayTimes.size()];

						int k2 = 0;
						for (int l = 0; l < cadArray.length; l++) {
							if (!cadArray[l].equals("0")) {
								//System.out.println(" down "+ cadArray[l]+" position "+ l);
								Jobs[j].operations.get(k).machines[k2] = l + 1;
								Jobs[j].operations.get(k).times[k2] = Integer.parseInt(cadArray[l]);
								//Initialize the QV
								Jobs[j].operations.get(k).QV[k2] = 0;
								k2++;
							}
						}
					}
					k++;
					s = a.readLine();
					//System.out.println(s+" 23");
				}
				//System.out.println("numero op "+k+" job "+j+ " zone "+i);
				max_num_operations = Math.max(max_num_operations, k);
				//a1.readLine();// read white space
				if (i == 0)
					a1.readLine();
				//System.out.println(a1.readLine()+" 5");
			}
			//System.out.println(zone[i].job_operation_occupied.size());
			//a.readLine();
			//a.readLine();
			//System.out.println(a.readLine()+" 4");
			//System.out.println(a.readLine());
		}

		for (int j = 0; j < nmachines; j++)
			Machines[j] = new Machine(j);
		//max_num_operations = countOperations;
		//System.out.println(" [max_num_operations "+ max_num_operations+" njobs "+ njobs);
		for (int m = 0; m < nmachines; m++)
			Machines[m].QValues = new double[njobs][max_num_operations];
		a.readLine(); // reading line finishActionBefore
		//System.out.println(a.readLine()+" fa"); // reading line finishActionBefore

		a.readLine();//white line
		a.readLine();// reading line finishActionBefore
		s = a.readLine();
		//System.out.println(s.toString()+" 2");
		char ch1 = '|';
		while (s.charAt(0) != ch1) {
			cadArray = s.split(",");
			cadArray1 = cadArray[1].split(" ");
			cadArray[1] = cadArray1[1];
			//System.out.println(cadArray[0] + " " + cadArray[1] + " size " + cadArray.length);
			for (Job job : Jobs) {
				job.operations.get(actions.get(cadArray[1])).operation_precedent = actions.get(cadArray[0]);
			}
			s = a.readLine();
			//System.out.println(s);
		}
		a.readLine();//reading white space
		a.readLine(); // reading line back2back
		//System.out.println(a.readLine());//reading white space
		//System.out.println(a.readLine()); // reading line back2back
		s = a.readLine();
		//System.out.println(s);
		while (s.charAt(0) != ch1) {
			cadArray = s.split(",");
			cadArray1 = cadArray[1].split(" ");
			cadArray[1] = cadArray1[1];
			//System.out.println(cadArray[0] + " " + cadArray[1] + " size " + cadArray.length);
			for (Job job : Jobs) {
				job.operations.get(actions.get(cadArray[1])).back2back_before = actions.get(cadArray[0]);
			}
			s = a.readLine();
			//System.out.println(s);
		}
//	 printValues();
		a.close();
		a1.close();
		f1.close();
		//System.out.println(" size zone 2 " +zone[3].job_operation_occupied.size());
		//System.out.println(" size job " +Jobs.length);
		//System.out.println(" size operations job 1 " +Jobs[0].operations.size());
	}

	public static double roundToDecimals(double d, int c) {
		int temp = (int) ((d * Math.pow(10, c)));
		return (((double) temp) / Math.pow(10, c));
	}

	public void SaveToFile(int cmax) {
		PrintWriter pw;
		//String UPLOAD_FOLDER = System.getProperty("java.io.tmpdir") + "/uploaded";
		//String PathSol = "Solutions/Mine/Test.txt";

		//String PathSol = FileName;
		//File SolutionFile=new File(PathSol);
		File SolutionFile = new File(String.format("%s/solution.tmp", UPLOAD_FOLDER));
		try {
			pw = new PrintWriter(SolutionFile);
			//pw.println(instance.getName());
			pw.println(cmax);
			//pw.println(0.0);//tardiness
			pw.println(nmachines);//number of machines
			pw.flush();

			for (int m = 0; m < njobs; m++)
				//for (int n=0; n < Jobs[m].operations.size(); n++){
				for (int n = Jobs[m].opStart; n < Jobs[m].operations.size(); n++) {
					pw.print(m + "\t"); //Job ID
					pw.print(n + "\t"); //Oper ID
					pw.write(Jobs[m].operations.get(n).name + "\t"); //Oper name
					pw.write(Jobs[m].operations.get(n).Ma + "\t"); //index of the machine that executed it
					pw.write(Jobs[m].operations.get(n).initial_time + "\t");
					pw.write(Jobs[m].operations.get(n).end_time + "\t");
					pw.write(Jobs[m].operations.get(n).back2back_before + "\t");//operation back to back before
					pw.write(Jobs[m].operations.get(n).operation_precedent + "\t");//operation precedent
					pw.write(0 + "\n");
					pw.flush();
				}
			pw.close();
			SaveToTxt();
		} catch (FileNotFoundException e) {
			System.err.println("Problem writing to file " + SolutionFile.getAbsolutePath());
			e.printStackTrace();
		}

	}

	public void SaveToTxt() {
		PrintWriter pw;
		//String PathSol = "Solutions/Mine/Test.txt";
		//String PathSol = "Schedule.txt";
		//String UPLOAD_FOLDER = System.getProperty("java.io.tmpdir") + "/uploaded";


		Calendar cal = Calendar.getInstance();
		cal.setTime(Date.from(Instant.now()));
        
      /*  String result = String.format(
                "file-%1$tY-%1$tm-%1$td-%1$tk-%1$tS-%1$tp.txt", cal);*/

		String result2 = String.format(
				"%1$tY-%1$tm-%1$td.txt", cal);


		// String PathTest = System.getProperty("user.home")+"/Desktop/"+"Schedule__" + result2;
		//File SolutionFile=new File(PathTest);
		File SolutionFile = new File(String.format("%s/schedule.txt", UPLOAD_FOLDER));


//		File SolutionFile=new File(PathSol);

		try {
			pw = new PrintWriter(SolutionFile);
			//pw.println(instance.getName());

			pw.print("ACTIONS = {");
			//System.out.println(actions.size());
			for (int i = 0; i < actions.size(); i++) {
				pw.write(Jobs[0].operations.get(i).name);
				if (i < actions.size() - 1)
					pw.write(", ");
				pw.flush();
			}

			pw.println("};");
			pw.println("RESOURCES = {robotL, robotR, operator};");
			pw.print("PRODUCTS = [");
			for (int i = 1; i <= njobs; i++) {
				pw.write(i + " ");
			}
			pw.println("];");
			pw.flush();

			pw.print("actionStartTime = [");
			StringBuilder end_time = new StringBuilder();
			StringBuilder duration = new StringBuilder();
			StringBuilder resource = new StringBuilder();
			for (int m = 0; m < njobs; m++) {
				for (int n = 0; n < Jobs[m].operations.size(); n++) {
					pw.print(Jobs[m].operations.get(n).initial_time); //initial time
					end_time.append(Jobs[m].operations.get(n).end_time);
					duration.append(Jobs[m].operations.get(n).proc_time);
					resource.append(Jobs[m].operations.get(n).Ma + 1);
					if (n < Jobs[m].operations.size() - 1) {
						pw.write(", ");
						resource.append(", ");
						duration.append(", ");
						end_time.append(", ");
					}

				}
				if (m < Jobs.length - 1) {
					pw.print(", ");
					resource.append(", ");
					duration.append(", ");
					end_time.append(", ");
				}
				pw.flush();
			}
			pw.println("];");
			pw.println("actionEndTime = [" + end_time + "];");
			pw.println("actionDuration = [" + duration + "];");
			pw.println("selectedResource = [" + resource + "];");
			pw.flush();

			pw.print("resourceStartTimes = [");
			for (int i = 0; i < nmachines; i++) {
				//System.out.println(Machines[i].Op_executed.size());
				pw.print(Machines[i].initial_time_machine);
				if (i < nmachines - 1)
					pw.write(", ");
				pw.flush();
			}
			pw.println("];");

			String resourceDurations = "";
			int cycleTime = 0;
			pw.print("resourceEndTimes = [");
			for (int i = 0; i < nmachines; i++) {
				pw.print(Machines[i].end_time_machine);
				resourceDurations += (Machines[i].end_time_machine - Machines[i].initial_time_machine);
				cycleTime = Math.max(cycleTime, (Machines[i].end_time_machine - Machines[i].initial_time_machine));
				if (i < nmachines - 1) {
					pw.write(", ");
					resourceDurations += ", ";
				}
				pw.flush();
			}
			pw.println("];");

			pw.println("resourceDurations = [" + resourceDurations + "];");
			pw.println("obj = " + cycleTime + ";");
			pw.println("softConstr = ;");
			pw.println("cycleTime = " + cycleTime + ";");
			pw.println("maxDuration = ;");
			pw.close();
			//FileUtils.copyFileToDirectory(SolutionFile, targetFile);
			Response.status(200).entity(SolutionFile).build();
		} catch (FileNotFoundException e) {
			System.err.println("Problem writing to file " + SolutionFile.getAbsolutePath());
			e.printStackTrace();
		}

	}

	public void PrintJob() {
		for (int g = 0; g < Jobs.length; g++) {
			System.out.println("Job " + g);
			for (int i = 0; i < Jobs[g].operations.size(); i++) {
				System.out.println("op " + i + " Ma " + Jobs[g].operations.get(i).Ma + " ");
			}
		}

	}

	public void PrintRoutes() {
		for (int j = 0; j < njobs; j++)
			Jobs[j].PrintRoute();
	}


	public void SearchRoutesVersion2(int njobs2) {
		int j_index;
		//System.out.println("njobs " + njobs + " Jobs.length " + Jobs.length);
		//Digo que las operaciones iniciales de cada trabajo estan habilitadas
		for (int j = Jobs.length - njobs2; j < njobs; j++) {
			//System.out.println("lolooooo");
			if (Jobs[j].opStart < Jobs[j].operations.size())
				HabOperationList.add(Jobs[j].operations.get(Jobs[j].opStart));
		}
		FullOperationList.clear();
		//Mientras queden operaciones sin maquinas asignadas sigo llenando FullOperationsList 
		//seleccionando una operacion random de las habilitadas
		while (!HabOperationList.isEmpty()) {
			//Selecciono una operacion y pongo en habilitadas la proxima de ese Job si es que tiene mas
			double rnd = Math.random();
			int index = Math.abs((int) Math.round(rnd * (HabOperationList.size() - 1)));
			Operation Oper = HabOperationList.get(index);
			FullOperationList.add(Oper);
			HabOperationList.remove(index);


			j_index = Oper.GetJob(); //indice del trabajo de la operacion

			int o_index = Oper.GetID(); //indice de la operacion
			for (int i = o_index + 1; i < Jobs[j_index].operations.size(); i++) {//por si hay mas de dos op seguidas en un mismo job
				//if (Jobs[j_index].operations.length-1 != o_index){
				if (Jobs[j_index].operations.get(i).back2back_before == o_index) {
					Oper = Jobs[j_index].operations.get(i);
					FullOperationList.add(Oper);
					// HabOperationList.remove(i);
					o_index = Oper.GetID();
				} else {
					if (i == Jobs[j_index].operations.size() - 1) {
						//si no era la ultima del trabajo
						if (Jobs[j_index].operations.size() - 1 != o_index)
							HabOperationList.add(Jobs[j_index].operations.get(o_index + 1));
					}

				}
				//}
			}

		}

//		//Print FullOperList
//		for (int h=0; h < FullOperationList.size(); h++){
//			Operation Ope = FullOperationList.get(h);
//			System.out.println("Oper " + Ope.GetID()+" Job " + Ope.GetJob());
//		}

		//System.out.println("size HabOperationList "+HabOperationList.size()+" size FullOperationList "+FullOperationList.size());
		//Ahora para cada operacion en FullOper... seleccionar una maquina
		for (Operation Ope : FullOperationList) {
			//System.out.println("aux_end "+Jobs[Ope.GetJob()].aux_end);
			int m = Ope.MachineSelection(epsilon, Machines, Jobs[Ope.GetJob()].aux_end, Jobs[Ope.GetJob()].operations);
			Ope.Ma = Ope.machines[m] - 1;
			//System.out.println("Ma "+Ope.Ma);
			Ope.M = Machines[Ope.machines[m] - 1];
			Ope.index_Ma = m;
			Machines[Ope.machines[m] - 1].work += Ope.proc_time;
			Machines[Ope.machines[m] - 1].Op_assigned.add(Ope);
			//JobEndTimes[j_index]=Ope.end_time;
			Jobs[Ope.GetJob()].aux_end = Ope.end_time;
			//System.out.println("job "+Ope.GetJob()+" op "+Ope.GetID()+" maq "+Ope.M.GetID()+" Ma "+Ope.Ma+" end time " +Ope.end_time);

		}

//		for (int jo=0; jo < njobs; jo++)
//			System.out.print(Jobs[jo].aux_end+"	");

//		System.out.println();
	}

	public void Initialize() {

		for (int j = 0; j < njobs; j++) {
			Jobs[j].Start(Machines);
		}
	}

	public boolean AllJobsFinished(Job[] Jobs) {
		for (int b = 0; b < njobs; b++)
			if (!Jobs[b].finished)
				return false;
		return true;
	}


	public void ProcessNonDelay(double alpha, double gamma, int iter) throws CloneNotSupportedException {
		boolean finished = false;
		Operation op_selected;
		Operation last_op;
		LinkedList<Machine> Working = new LinkedList<>();

		while (!finished) {
			//	counter++;
			for (int m = 0; m < nmachines; m++) {
				//System.out.println("machine "+m);
				if (!Machines[m].Queue.isEmpty()) {
					//	System.out.println("machine with queue "+m);
					Working.add(Machines[m]);
					//System.out.println("Agrego machine "+m);
					//op_selected = Machines[m].ActionSelection(Jobs, epsilon, LearningRate, pwQV, zone);
					op_selected = Machines[m].ActionSelection(Jobs, epsilon, LearningRate, zone, jobsInExecute);

					if (op_selected != null) {
						Machines[m].Queue.remove(op_selected);
						//System.out.println("eliminado "+Machines[m].Queue.remove(op_selected));
						Machines[m].Op_executed.add(op_selected);
						op_selected.end_time = op_selected.initial_time + op_selected.proc_time;
						//System.out.println("Select job "+op_selected.GetJob()+" op "+op_selected.GetID()+" maq "+op_selected.M.GetID()+" Ma "+op_selected.Ma+" initial time "+op_selected.initial_time +" end time "+op_selected.end_time );
						//a su trabajo actualizarle el end_time y mandar la otra
						Jobs[op_selected.GetJob()].j_end_time = op_selected.end_time;
						Jobs[op_selected.GetJob()].time_remaining = Jobs[op_selected.GetJob()].time_remaining - op_selected.proc_time;
						Machines[m].initial_time_machine = Machines[m].Op_executed.getFirst().initial_time;
						Machines[m].end_time_machine = Machines[m].Op_executed.getLast().end_time;
					} else {
						Working.remove(Machines[m]);
						//System.out.println(Working.remove(Machines[m])+" machine "+Machines[m].GetID());
					}

				}
			}

			for (Machine machine : Working) {
				last_op = machine.Op_executed.getLast();
				//last_op = Machines[n].Op_executed.getLast();
				//si la op no es la ultima del trabajo
				if (last_op.GetID() < Jobs[last_op.GetJob()].operations.size() - 1) {
					Jobs[last_op.GetJob()].SendNext(last_op.GetID(), Machines);
					//System.out.println(" enviar op "+ (last_op.GetID()+1)+" job "+last_op.GetJob());
				} else {//si es la ultima del trabajo
					Jobs[last_op.GetJob()].finished = true;//se pone como finished
					//System.out.println("Termino "+last_op.GetJob()+ " machine "+last_op.Ma);
					//jobsInExecute trabajos ejecutandose ahora xq no pueden ser m�s de dos al mismo tiempo
					// if (last_op.GetJob() == jobsInExecute[0]-1) {//colocar en 0 porque el trabajo ya termino
					if (last_op.GetJob() == jobsInExecute[0]) {//colocar en 0 porque el trabajo ya termino
						jobsInExecute[0] = -1;
						for (Job job : Jobs) {
							if (!job.finished && job.GetID() != jobsInExecute[1]) {
								job.Start(Machines);
								System.out.println("posicion 0 envie "+job.GetID());
							}
						}
					} else {
						jobsInExecute[1] = -1;
						for (Job job : Jobs) {
							if (!job.finished && job.GetID() != jobsInExecute[0]) {
								job.Start(Machines);
								System.out.println("posicion 1 envie "+job.GetID());
							}
						}

					}
						/* if(lastJob != njobs){
							 Jobs[lastJob].Start(Machines);
							 lastJob++;
						 }*/
				}
			}

			Working.clear();
			//////////////

			if (AllJobsFinished(Jobs))
				finished = true;

			//System.out.println("...."+Machines[0].Op_executed.size());
		}


	}


	public double GetMaxNextQV(Operation op) {
		double max, ch2;
		//buscar el maximo entre las operaciones que se quedan en esta cola y las de la cola de la proxima op de ese job
		double ch1 = Machines[op.Ma].MaxQVQueue();
		//System.out.println("op length "+Jobs[op.GetJob()].operations.size()+" job "+Jobs[op.GetJob()].GetID()+" op "+Jobs[op.GetJob()].operations.get(op.GetID() + 1).GetID());
		//chequear que no sea la ultima operacion de ese Job
		if (op.GetID() < Jobs[op.GetJob()].operations.size() - 1) {
			ch2 = (Jobs[op.GetJob()].operations.get(op.GetID() + 1).M == null)?0:Jobs[op.GetJob()].operations.get(op.GetID() + 1).M.MaxQVQueue();
		}else
			ch2 = 0;

		max = Math.max(ch1, ch2);

		return max;
	}


	public double GetMinNextQV(Operation op) {
		double min, ch2;
		//buscar el maximo entre las operaciones que se quedan en esta cola y las de la cola de la proxima op de ese job
		double ch1 = Machines[op.Ma].MinQVQueue();
		//chequear que no sea la ultima operacion de ese Job
		if (op.GetID() < Jobs[op.GetJob()].operations.size() - 1)
			ch2 = Jobs[op.GetJob()].operations.get(op.GetID() + 1).M.MinQVQueue();
		else
			ch2 = 0;

		min = Math.min(ch1, ch2);

		return min;
	}


	public void UpdateQV(Operation op, double temp, double alpha, double gamma, int R01) {
		double R = (double) 1 / op.proc_time;
//		double WR = Jobs[op.GetJob()].time_remaining;
		//double aux = Machines[op.Ma].QValues[op.GetJob()][op.GetID()];
		//Machines[op.Ma].QValues[op.GetJob()][op.GetID()] = aux + alpha * (R + gamma * temp);
		//Machines[op.Ma].QValues[op.GetJob()][op.GetID()] += alpha *(R01 - Machines[op.Ma].QValues[op.GetJob()][op.GetID()]);
		Machines[op.Ma].QValues[op.GetJob()][op.GetID()] += alpha * (R - Machines[op.Ma].QValues[op.GetJob()][op.GetID()]);
		//Machines[op.Ma].QValues[op.GetJob()][op.GetID()] += alpha * (R + gamma * temp - Machines[op.Ma].QValues[op.GetJob()][op.GetID()]);
	}

	public void UpdateQVGlobal(Operation op, double alpha, double gamma, double temp, int cmax) {
		//	double R = (double) 1/cmax;
		double aux = Machines[op.Ma].QValues[op.GetJob()][op.GetID()];


//		Machines[op.Ma].QValues[op.GetJob()][op.GetID()] = aux + alpha * (R + gamma * temp - aux);

		//Machines[op.Ma].QValues[op.GetJob()][op.GetID()] = aux + alpha * (R - aux);

		//cuando es 0 o 1
		Machines[op.Ma].QValues[op.GetJob()][op.GetID()] = aux + alpha * (cmax + gamma * temp - aux);

		//Machines[op.Ma].QValues[op.GetJob()][op.GetID()] += alpha * (1/op.proc_time + gamma * temp - Machines[op.Ma].QValues[op.GetJob()][op.GetID()]);
	}


	public double MostWorkRemaining(Operation op) {
		return 0;
	}


	public int CalculateCmax(Job[] UJobs) {
		int cmax = UJobs[0].j_end_time;

		for (int j = 1; j < njobs; j++)
			if (UJobs[j].j_end_time > cmax)
				cmax = UJobs[j].j_end_time;

		return cmax;
	}


	public void PrintInitialQueues() {
		for (int m = 0; m < nmachines; m++)
			if (!Machines[m].Queue.isEmpty())
				Machines[m].PrintQueue();
		//System.out.println(Machines[m].Queue.getFirst().GetID());
	}

	public void PrintValues() {
		System.out.println("My Learning Rate is: " + LearningRate);
		System.out.println("My Discount Factor is: " + DiscountFactor);
		System.out.println("My Epsilon is: " + epsilon);
		System.out.println("Number of iterations: " + iterations);
		System.out.println("The instance is: " + filename);
		System.out.println("Number of jobs: " + njobs);
		System.out.println("Number of machines: " + nmachines);
		System.out.println("Average number of machines per operation: " + navg_op);
	}


	public void RestartTimesForOnce() {
		//restart machines' times
		for (int x = 0; x < nmachines; x++) {
			Machines[x].time = 0;
			Machines[x].TempOrderedList.clear();
			//Machines[x].Op_assigned.clear();
			Machines[x].minInitialM = 0;
		}

		//restart jobs' times and each job restarts its operations' times
		for (int j = 0; j < njobs; j++) {
			Jobs[j].aux_end = 0;
			Jobs[j].j_end_time = 0;
			Jobs[j].finished = false;
			for (int o = 0; o < Jobs[j].operations.size(); o++) {
				Jobs[j].operations.get(o).initial_time = 0;
				Jobs[j].operations.get(o).end_time = 0;
				Jobs[j].operations.get(o).temp_end = 0;
				//	Jobs[j].operations[o].proc_time = 0;
			}

		}
		for (Zone value : zone) {
			value.time = 0;
		}
		//FullOperationList.clear();
		jobsInExecute = new int[2];
		//System.out.println("jobsInExecute[0] "+jobsInExecute[0]+" jobsInExecute[1] "+jobsInExecute[1]);
	}


	public void RestartTimes() {
		//restart machines' times
		for (int x = 0; x < nmachines; x++) {
			Machines[x].time = 0;
			Machines[x].TempOrderedList.clear();
			Machines[x].Op_assigned.clear();
			Machines[x].minInitialM = 0;
		}

		//restart jobs' times and each job restarts its operations' times
		for (int j = 0; j < njobs; j++) {
			Jobs[j].aux_end = 0;
			Jobs[j].j_end_time = 0;
			Jobs[j].finished = false;
			for (int o = 0; o < Jobs[j].operations.size(); o++) {
				Jobs[j].operations.get(o).initial_time = 0;
				Jobs[j].operations.get(o).end_time = 0;
				Jobs[j].operations.get(o).temp_end = 0;
				Jobs[j].operations.get(o).proc_time = 0;
			}

		}
		FullOperationList.clear();
	}


	public void PrintOrders() {
		for (int m = 0; m < nmachines; m++)
			Machines[m].PrintOrder();
	}


	public void Locate_Op_OrderedList(Operation op) {
		boolean located = false;
		int i = 0;
		while (!located) {
			// if the List is still empty or this value is higher than the highest so far, then add it
			if ((OrderedList.isEmpty()) || (OrderedList.getLast().end_time > op.end_time)) {
//			if (OrderedList.isEmpty()) {  
				OrderedList.add(op);
				located = true;
			} else {
				if (op.end_time < OrderedList.get(i).end_time)
					i++;
				else {
					OrderedList.add(i, op);
					located = true;
				}
			}
		}
	}

	public void PrintQValues() {
		for (int q = 0; q < njobs; q++)
			Jobs[q].PrintQV();
	}

	public void PrintQValuesMachines() {
		for (int q = 0; q < nmachines; q++)
			Machines[q].PrintQValues();
	}

	public int[] CheckAvailability(Machine M, int time_slot, int min_posible_start) { //chequear en las operaciones asignadas los tiempos ocupados
		int[] resultado = new int[2];
		boolean located = false;
		//si esta vacia es 0 todo
		//if ((M.Op_executed_Optim.size()==0)||(M.Op_executed_Optim.getFirst().initial_time >= time_slot)){
		if ((M.Op_executed_Optim.isEmpty()) ||
				((M.Op_executed_Optim.getFirst().initial_time >= time_slot) && (M.Op_executed_Optim.getFirst().initial_time >= min_posible_start + time_slot))) {
			if (M.timeReSchedule != -1) {
				resultado[1] = M.timeReSchedule;
			}
			located = true;
		} else {
			for (int i = 0; i < M.Op_executed_Optim.size() - 1; i++) {
				int aux = M.Op_executed_Optim.get(i + 1).initial_time - M.Op_executed_Optim.get(i).end_time;
				//si la dif entre en inicial de una y el final de la otra es de tama�o del slot necesario entonces devuelvo pos y tiempo inicial
				//if (aux >= time_slot) {
				if ((aux >= time_slot) && (M.Op_executed_Optim.get(i).end_time >= min_posible_start)) {
					//  if ((aux >= time_slot) && (min_posible_start+time_slot<= M.Op_executed_Optim.get(i+1).initial_time) && located == false){
					//if ((aux >= time_slot) && (M.Op_executed_Optim.get(i+1).initial_time >= min_posible_start+time_slot)){
					//it fits here
					resultado[0] = i + 1;
					resultado[1] = M.Op_executed_Optim.get(i).end_time;
					located = true;
					break;
				}
			}
		}

		if (!located) {
			resultado[0] = M.Op_executed_Optim.size();
			resultado[1] = M.Op_executed_Optim.getLast().end_time;
		}
		return resultado;
	}


	public void ClearTimesOpt() {
		for (int j = 0; j < njobs; j++)
			Jobs[j].temp_endtime = 0;

		OrderedList.clear();

		for (int m = 0; m < nmachines; m++)
			Machines[m].Op_executed_Optim.clear();

		for (Zone value : zone) value.time = 0;
	}


	public void GetBackwardForward() {
		//System.out.println("backward forward ");
		//Finding slots...
		for (Operation operation : OrderedList) {
			//int min_initial_j = 0;
			int min_initial_j = Jobs[operation.GetJob()].temp_endtime;

			//CheckAvailability on the first machine, and this gives a possible initial time on the machine
			int[] data = CheckAvailability(Machines[operation.machines[0] - 1], operation.times[0], min_initial_j);
			int index = data[0];
			int min_initial_m = data[1];
			int initial = Math.max(min_initial_j, min_initial_m);
			int min_initial = initial;
			int min_end = initial + operation.times[0];
			operation.temp_index = 0;
			operation.index_Ma = 0;
			//System.out.println("job "+OrderedList.get(t).GetJob()+" op "+OrderedList.get(t).GetID()+" name "+OrderedList.get(t).name);

			for (int p = 1; p < operation.machines.length; p++) {
				data = CheckAvailability(Machines[operation.machines[p] - 1], operation.times[p], min_initial_j);
				min_initial_m = data[1];
				initial = Math.max(min_initial_j, min_initial_m);
				if (initial + operation.times[p] < min_end) {
					index = data[0];
					min_initial = initial;
					min_end = initial + operation.times[p];
					operation.temp_index = p;
					operation.index_Ma = p;
					//	finalArray = ""+OrderedList.get(t).GetJob()+ OrderedList.get(t).GetID()+(OrderedList.get(t).machines[p]-1);
				}
			}
			//Jobs[OrderedList.get(t).GetJob()].temp_endtime += OrderedList.get(t).times[OrderedList.get(t).temp_index];
			Jobs[operation.GetJob()].temp_endtime = min_end;
			operation.initial_time = min_initial;
			operation.end_time = min_end;
			operation.M = Machines[operation.machines[operation.temp_index] - 1];
			operation.Ma = operation.machines[operation.temp_index] - 1;
			//System.out.println("min_initial "+min_initial+" min_end "+min_end+" index_Ma "+ OrderedList.get(t).index_Ma+" maq "+Machines[OrderedList.get(t).Ma].GetID()+" Ma "+OrderedList.get(t).Ma+" index "+index);
			//insertar con posicion donde va...
			if (index == 0)
				Machines[operation.machines[operation.temp_index] - 1].Op_executed_Optim.addFirst(operation);
			else {
				//Machines[OrderedList.get(t).temp_index].Op_executed_Optim.add(index, OrderedList.get(t));
				if (index == Machines[operation.machines[operation.temp_index] - 1].Op_executed_Optim.size())
					Machines[operation.machines[operation.temp_index] - 1].Op_executed_Optim.addLast(operation);
				else
					Machines[operation.machines[operation.temp_index] - 1].Op_executed_Optim.add(index, operation);
			}

			//update time of zones
		/*	for (int i = 0; i < zone.length; i++)
				if (zone[i].job_operation_occupied.get(finalArray).equals(true))			
					zone[i].time = min_end;			*/

		}

		//Print this step
//		for (int i=0; i<OrderedList.size(); i++){
//			System.out.print("J"+OrderedList.get(i).GetJob()+"O"+OrderedList.get(i).GetID()+" Prefers M" + OrderedList.get(i).machines[OrderedList.get(i).temp_index]);
//			System.out.println(" From "+OrderedList.get(i).initial_time + " To " + OrderedList.get(i).end_time);
//		}

	}

	public void Order() {
		for (int x = 0; x < njobs; x++)
			for (int y = 0; y < Jobs[x].operations.size(); y++)
				Locate_Op_OrderedList(Jobs[x].operations.get(y));

//		for (int i=0; i<OrderedList.size(); i++)
//			System.out.println("J"+OrderedList.get(i).GetJob()+"O"+OrderedList.get(i).GetID());
	}


	public void ExecuteModeOptimization() {
		//order the operations by end-time (the highest go first)
		Order();
		GetBackwardForward();
		ClearTimesOpt();
		Order();
		GetBackwardForward2(false);
		/*for (int j=0; j < njobs; j++)
			Jobs[j].j_end_time = Jobs[j].operations.get(Jobs[j].operations.size()-1).end_time;*/
		//ComputeMakespan
	}


	private void GetBackwardForward2(boolean reschedule) {
		// TODO Auto-generated method stub
		//Finding slots...
		//	System.out.println("________________");
		//System.out.println("backward forward 2 ");
		for (Operation operation : OrderedList) {
			//	System.out.println("n= "+t+" temp_endtime "+ Jobs[OrderedList.get(t).GetJob()].temp_endtime);
			int min_initial_j = Jobs[operation.GetJob()].temp_endtime;
			//int min_initial_j = 0;
			int min_end, min_initial, index;
			if (reschedule && operation.GetID() == Jobs[operation.GetJob()].opStart) {
				min_initial_j = Jobs[operation.GetJob()].operations.get(Jobs[operation.GetJob()].opStart).end_time;
			}
			//ArrayList<Integer> arrayZone = new ArrayList<Integer>();
			String array;
			String finalArray;
			//System.out.println("job "+OrderedList.get(t).GetJob()+" op "+OrderedList.get(t).GetID()+" name "+OrderedList.get(t).name);
			if (operation.back2back_before > -1) {
				int beforeMachine = Jobs[operation.GetJob()].operations.get(operation.back2back_before).Ma;
				int indexTime = 0;
				array = "" + operation.GetJob() + operation.GetID() + beforeMachine;
				finalArray = "" + operation.GetJob() + operation.GetID() + beforeMachine;
				//search zones
				for (Zone value : zone) {
					//System.out.println(zone[i].id_zone);
					if (value.job_operation_occupied.get(array).equals(true)) {
						if (min_initial_j < value.time)
							min_initial_j = value.time;
					}
				}

				for (int i = 0; i < operation.times.length; i++) {
					if (operation.machines[i] - 1 == beforeMachine) {
						indexTime = i;
					}
				}
				//CheckAvailability on the first machine, and this gives a possible initial time on the machine
				int[] data = CheckAvailability(Machines[beforeMachine], operation.times[indexTime], min_initial_j);
				index = data[0];
				int min_initial_m = data[1];
				int initial = Math.max(min_initial_j, min_initial_m);
				min_initial = initial;
				operation.index_Ma = indexTime;
				min_end = initial + operation.times[operation.index_Ma];
				operation.temp_index = operation.index_Ma;

				//System.out.println(" back: min_initial_j "+ min_initial_j+" min initial machine "+min_initial_m);
			} else {
				array = "" + operation.GetJob() + operation.GetID() + (operation.machines[0] - 1);
				finalArray = "" + operation.GetJob() + operation.GetID() + (operation.machines[0] - 1);
				//search zones
				for (Zone item : zone) {
					if (item.job_operation_occupied.get(array).equals(true)) {
						if (min_initial_j < item.time)
							min_initial_j = item.time;
					}
				}
				//	min_initial_j = Jobs[OrderedList.get(t).GetJob()].temp_endtime;
				//CheckAvailability on the first machine, and this gives a possible initial time on the machine
				int[] data = CheckAvailability(Machines[operation.machines[0] - 1], operation.times[0], min_initial_j);
				index = data[0];
				int min_initial_m = data[1];
				int initial = Math.max(min_initial_j, min_initial_m);
				min_initial = initial;
				min_end = initial + operation.times[0];
				operation.temp_index = 0;
				operation.index_Ma = 0;
				//	System.out.println(" first machine: min_initial_j "+ min_initial_j+" min initial machine "+min_initial_m+" machine "+(OrderedList.get(t).machines[OrderedList.get(t).temp_index]-1));
				for (int p = 1; p < operation.machines.length; p++) {
					array = "" + operation.GetJob() + operation.GetID() + (operation.machines[p] - 1);
					min_initial_j = Jobs[operation.GetJob()].temp_endtime;
					//min_initial_j = 0;
					if (reschedule) {
						min_initial_j = Jobs[operation.GetJob()].operations.get(Jobs[operation.GetJob()].opStart).end_time;
						//min_initial_j = Jobs[OrderedList.get(t).GetJob()].temp_endtime;
					}
					//search zone
					for (Zone value : zone) {
						if (value.job_operation_occupied.get(array).equals(true)) {
							if (min_initial_j < value.time)
								min_initial_j = value.time;
						}
					}

					data = CheckAvailability(Machines[operation.machines[p] - 1], operation.times[p], min_initial_j);
					min_initial_m = data[1];
					initial = Math.max(min_initial_j, min_initial_m);
					if (initial + operation.times[p] < min_end) {
						index = data[0];
						min_initial = initial;
						min_end = initial + operation.times[p];
						operation.temp_index = p;
						operation.index_Ma = p;
						finalArray = "" + operation.GetJob() + operation.GetID() + (operation.machines[p] - 1);
					}
					//System.out.println(" others machine : min_initial_j "+ min_initial_j+" min initial machine "+min_initial_m+" machine "+(OrderedList.get(t).machines[OrderedList.get(t).temp_index]-1));
				}
				//System.out.println("min_initial "+min_initial+" min_end "+min_end+" index_Ma "+ OrderedList.get(t).index_Ma+" maq "+Machines[OrderedList.get(t).Ma].GetID()+" Ma "+OrderedList.get(t).Ma+" index "+index);
			}

			//Jobs[OrderedList.get(t).GetJob()].temp_endtime += OrderedList.get(t).times[OrderedList.get(t).temp_index];
			Jobs[operation.GetJob()].temp_endtime = min_end;
			operation.initial_time = min_initial;
			operation.end_time = min_end;
			operation.M = Machines[operation.machines[operation.temp_index] - 1];
			operation.Ma = operation.machines[operation.temp_index] - 1;
			operation.proc_time = operation.times[operation.index_Ma];
			//	System.out.println("Job "+ OrderedList.get(t).GetJob()+" op "+ OrderedList.get(t).GetID()+" min_initial "+min_initial+" min_end "+min_end+" index_Ma "+ OrderedList.get(t).index_Ma+" maq "+Machines[OrderedList.get(t).Ma].GetID()+" Ma "+OrderedList.get(t).Ma+" time "+OrderedList.get(t).times[OrderedList.get(t).index_Ma]);
			//	System.out.println("min_initial "+OrderedList.get(t).initial_time+" min_end "+OrderedList.get(t).end_time+" index_Ma "+ OrderedList.get(t).index_Ma+" maq "+Machines[OrderedList.get(t).Ma].GetID()+" Ma "+OrderedList.get(t).Ma+" index "+index);
			//insertar con posicion donde va...
			if (index == 0)
				Machines[operation.machines[operation.temp_index] - 1].Op_executed_Optim.addFirst(operation);
			else {
				//Machines[OrderedList.get(t).temp_index].Op_executed_Optim.add(index, OrderedList.get(t));
				if (index == Machines[operation.machines[operation.temp_index] - 1].Op_executed_Optim.size())
					Machines[operation.machines[operation.temp_index] - 1].Op_executed_Optim.addLast(operation);
				else {
					//	System.out.println("size "+Machines[OrderedList.get(t).machines[OrderedList.get(t).temp_index]-1].Op_executed_Optim.size()+" index "+index);
					Machines[operation.machines[operation.temp_index] - 1].Op_executed_Optim.add(index, operation);
				}

			}

			//update time of zones
			for (Zone value : zone)
				if (value.job_operation_occupied.get(finalArray).equals(true))
					value.time = min_end;
		}

		//Print this step
//				for (int i=0; i<OrderedList.size(); i++){
//					System.out.print("J"+OrderedList.get(i).GetJob()+"O"+OrderedList.get(i).GetID()+" Prefers M" + OrderedList.get(i).machines[OrderedList.get(i).temp_index]);
//					System.out.println(" From "+OrderedList.get(i).initial_time + " To " + OrderedList.get(i).end_time);
//				}
	}

	public void UpdateQValuesProcedure(double alpha, double gamma, int R) {

		//ACTUALIZAR LOS Q-VALUES DE LA 1era ITERACION CON EL CMAX OBTENIDO
		for (int a = 0; a < njobs; a++) {
			for (int b = 0; b < Jobs[a].operations.size(); b++)
				Jobs[a].operations.get(b).UpdateQVGlobal(alpha, R);
			//System.out.println();
		}
		//ACTUALIZAR LOS Q-VALUES DE LA 2da ITERACION CON EL SPT 
		for (int c = 0; c < njobs; c++)
			for (int d = 0; d < Jobs[c].operations.size(); d++) {
				double temp = GetMaxNextQV(Jobs[c].operations.get(d));
				UpdateQV(Jobs[c].operations.get(d), temp, alpha, gamma, R);
			}

	}

	public int Execute(double alpha, double gamma) throws FileNotFoundException, CloneNotSupportedException {
		Date date = new Date();
		long initial = date.getTime();
		int R;

		Calendar cal = Calendar.getInstance();
		cal.setTime(Date.from(Instant.now()));

		String dateNow = String.format(
				"%1$tY-%1$tm-%1$td.txt", cal);


		/////// check date ////////
		Calendar calendario = Calendar.getInstance();

		calendario.set(2021, 03, 28);

		Date theDate = calendario.getTime();

		// Date expiration_date = new Date(2020, 10, 29);

		// System.err.println(theDate);
		//System.err.println(cal.getTime());
        
      /*  if (cal.getTime().after(theDate)) {
        	JOptionPane.showMessageDialog(null, "Trial expired", "ERROR", JOptionPane.ERROR_MESSAGE);
        	System.exit(0);
        	//System.out.println("Trial expired");
        } else 
        	System.out.println("Valid");*/

		/////////////////////////////

		file_saved = "Solution__" + dateNow;

		//SearchRoutesVersion2();
		int temp = 0;
		for (int n = 0; n < this.iterations; n++) {

			//PrintRoutes();
			//improvement = true;	
			//System.out.println("_______");
			if (n == 0 || n == Math.abs(iterations / 10 * temp)) {
				RestartTimesForOnce();
				SearchRoutesVersion2(njobs);
				//System.out.println(Math.abs(iterations/5*temp));
				temp++;
			}
			//ExecuteModeOptimization();//quitar si no se hace backwardforward
			RestartTimesForOnce();

			//PrintRoutes();		
			//
			Initialize(); //Send job to the first machine
			//ProcessWithDelay();
			//pwQV.println("Iteration " + n);
			jobsInExecute[0] = -1;
			jobsInExecute[1] = -1;
			ProcessNonDelay(alpha, gamma, n);
			int cmax = CalculateCmax(Jobs);
			//System.out.println(" n="+n+" Cmax="+Cmax);			

			//Variante 2 ModeOptimization
			if (n == 0) {
				BestSol = cmax;
				SaveToFile(BestSol);
			}
			if (cmax < BestSol) {
				//	System.out.println("encontre mejor sol");
				BestSol = cmax;
				SaveToFile(BestSol);
				R = 1;
				//UpdateQValuesProcedure(alpha, gamma, R);
			} else {
				if (cmax == BestSol)
					R = 1;
				else
					R = 0;
			}
			UpdateQValuesProcedure(alpha, gamma, R);
			//	 ClearTimesOpt();		//Backward Forward
			//RestartTimesForOnce();
		}


		System.out.println("The makespan is: " + BestSol);


		Date date1 = new Date();
		long fin = date1.getTime();
		System.out.println((fin - initial) + " Milliseconds...");
		System.out.println((fin - initial) / 1000 + "." + (fin - initial) % 1000 + " Seconds...");
		System.out.println("----------------");
		return BestSol;
	}

	//Re-schedule

	public void ExecuteReSchedule() throws FileNotFoundException, CloneNotSupportedException {
		System.out.println("reschedule Jobs.length "+Jobs.length);
		Date date = new Date();
		long initial = date.getTime();
		int R;
		Calendar cal = Calendar.getInstance();
		cal.setTime(Date.from(Instant.now()));
		//file_saved = "Solutions/Mine/Solution-" + filename + ".txt";
		//SearchRoutesVersion2();
		String dateNow = String.format(
				"%1$tY-%1$tm-%1$td.txt", cal);

		file_saved = "Solution_ReSchedule__" + dateNow;
		
		/*for (int i = 0; i < Jobs.length; i++) {
			for (int j = 0; j < Jobs[i].operations.size(); j++) {
				System.out.println("job "+i+" op "+Jobs[i].operations.get(j).GetID()+" start " +Jobs[i].operations.get(j).initial_time+" end "+Jobs[i].operations.get(j).end_time);

			}
		}*/
		int temp = 0;
		for (int n = 0; n < this.iterations; n++) {
			//for (int n = 0; n < 1; n++){
			//System.out.println("ooooo");	
			/*RestartTimesForOnceReSchedule();
			ExecuteModeOptimizationReSchedule();*/

			if (n == 0 || n == Math.abs(iterations / 10 * temp)) {
				RestartTimesForOnceReSchedule();
				SearchRoutesVersion2(njobsNew);
				//System.out.println("ExecuteReSchedule "+ njobsNew);
				temp++;
			}

			RestartTimesForOnceReSchedule();
			initializeReSchedule(); //Send job to the first machine

			//Trabajos q se estan ejecutando al mismo tiempo
			jobsInExecute[0] = -1;
			jobsInExecute[1] = -1;
			for (int i = 0; i < Jobs.length; i++) {
				if (Jobs[i].opStart < Jobs[i].operations.size()
						&& jobsInExecute[0] == -1 && jobsInExecute[1] != i) {
					jobsInExecute[0] = i;
					//System.out.println("jobsInExecute[0] "+i);
				}else {
					if (Jobs[i].opStart < Jobs[i].operations.size()
							&& jobsInExecute[1] == -1 && jobsInExecute[0] != i){
						jobsInExecute[1] = i;
						//System.out.println("jobsInExecute[1] "+i);
					}
				}

			}
			//System.out.println("jobsInExecute[0] "+jobsInExecute[0]+" jobsInExecute[1] "+jobsInExecute[1]);

			ProcessNonDelay(LearningRate, DiscountFactor, n);
			int cmax = CalculateCmax(Jobs);
			//Cmax = CalculateCmax(Jobs);
			//System.out.println(cmax);
			//pwQV.println("makespan-iteration " + n + ": " + Cmax);
			//pwQV.println();
			//pwQV.flush();

			//Variante 2 ModeOptimization
			if (n == 0) {
				BestSol = cmax;
				SaveToFile(BestSol);
			}
			if (cmax < BestSol) {
				//System.out.println("encontre mejor sol");
				BestSol = cmax;
				SaveToFile(BestSol);
				R = 1;
				//UpdateQValuesProcedure(alpha, gamma, R);
			} else {
				if (cmax == BestSol)
					R = 1;
				else
					R = 0;
			}
			UpdateQValuesProcedure(LearningRate, DiscountFactor, R);
			ClearTimesOptReSchedule();

			// System.out.println("Time cycle "+Cmax);
			//RestartTimesForOnce();
		}

		//borrar timeReSchedule en machine, timeReScheduleZone in zone
		System.out.println("The makespan is: " + BestSol);
				
		/*Instance instance;
		try {
			//System.out.println(file_saved.toString());
			instance = new Instance("Schedule", 55, file_saved,nmachines);
			Pair<Instance,Schedule> result = Test.loadSchedule(instance,this);
			List<Schedule> s = new ArrayList<Schedule>(); 
			ArrayList<OperationAllocation> newAllocs = new ArrayList<OperationAllocation>();
			for (int i=0; i < result.getSecond().getAllocations().size(); i++) {
				OperationGUI operation = result.getSecond().getAllocations().get(i).getOperation();
				MachineGUI machine = result.getSecond().getAllocations().get(i).getMachine();
				int startTime = result.getSecond().getAllocations().get(i).getStartTime();
				int endTime = result.getSecond().getAllocations().get(i).getEndTime();
				boolean border = result.getSecond().getAllocations().get(i).getBorder();
				
				newAllocs.add(new OperationAllocation(operation, machine, startTime, endTime,border));
				
			}
			
			Schedule otro = new Schedule (newAllocs);	

			s.add(otro);
			new File(file_saved);
			
			//System.out.println("The fixBton size is: "+fixBoton.size());
			ScheduleFrame sf = new ScheduleFrame(result.getFirst(), result.getSecond()," Optimized using Q-Learning",this,true);
			//sf.saveSchedule(filename);
			sf.setVisible(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

		// end new add

		Date date1 = new Date();
		long fin = date1.getTime();
		System.out.println((fin - initial) + " Milliseconds...");
		System.out.println((fin - initial) / 1000 + "." + (fin - initial) % 1000 + " Seconds...");
		System.out.println("----------------");

	}

	//initialization of all attributes of the operations with time bigger than the fix one  
	public void RestartTimesForOnceReSchedule() {
		//restart machines' times
		//System.out.println("machine "+Machines.length+" m "+Machines[0].GetID());
		for (int x = 0; x < nmachines; x++) {
			Machines[x].time = Machines[x].timeReSchedule;
			//System.out.println("machine "+x+" time "+Machines[x].time);
			Machines[x].TempOrderedList.clear();//look
			Machines[x].Op_assigned.clear();
			Machines[x].minInitialM = Machines[x].timeReSchedule;
		}

		//restart jobs' times and each job restarts its operations' times
		for (int j = 0; j < njobs; j++) {
			//	Jobs[j].aux_end = 0;
			//Jobs[j].j_end_time =0;
			if (Jobs[j].opStart != Jobs[j].operations.size())
				Jobs[j].finished = false;
			/*for (int o=0; o<Jobs[j].operations.size(); o++){
				Jobs[j].operations.get(o).initial_time = 0;
				Jobs[j].operations.get(o).end_time = 0;
				Jobs[j].operations.get(o).temp_end = 0;
			//	Jobs[j].operations[o].proc_time = 0;
			}*/

		}
		for (Zone value : zone) {
			value.time = value.timeReScheduleZone;
		}
		//FullOperationList.clear();
	}

	public void initializeReSchedule() {

		for (int j = 0; j < njobs; j++) {
			//System.out.println("job "+j+" opStart "+Jobs[j].opStart);
			if (Jobs[j].opStart < Jobs[j].operations.size()) {
				//System.out.println("job "+j);
				if (Jobs[j].opStart == -1) Jobs[j].opStart = 0;
				Jobs[j].startReChedule(Machines);
			}
		}
	}

	public void ExecuteModeOptimizationReSchedule() {
		//order the operations by end-time (the highest go first)
		OrderReSchedule();
		GetBackwardForward();
		ClearTimesOptReSchedule();
		OrderReSchedule();
		GetBackwardForward2(true);
		/*for (int j=0; j < njobs; j++)
			Jobs[j].j_end_time = Jobs[j].operations.get(Jobs[j].operations.size()-1).end_time;*/
		//ComputeMakespan
	}

	public void OrderReSchedule() {
		for (int x = 0; x < njobs; x++)
			for (int y = Jobs[x].opStart; y < Jobs[x].operations.size(); y++)
				Locate_Op_OrderedList(Jobs[x].operations.get(y));
	}

	public void ClearTimesOptReSchedule() {
		for (int j = 0; j < njobs; j++) {
			if (Jobs[j].opStart != 0) {
				Jobs[j].temp_endtime = Jobs[j].operations.get(Jobs[j].opStart - 1).end_time;
				//System.out.println("time end job "+j+" time "+Jobs[j].temp_endtime);
			} else
				Jobs[j].temp_endtime = 0;
		}

		OrderedList.clear();

		for (int m = 0; m < nmachines; m++)
			Machines[m].Op_executed_Optim.clear();

		for (Zone value : zone) value.time = value.timeReScheduleZone;

		for (Machine machine : Machines) {
			machine.time = machine.timeReSchedule;
			//System.out.println("machine "+m+" time "+Machines[m].timeReSchedule);
		}
	}

	//Methods for new products

	public void ReadDataNewProducts(File[] file) throws IOException {
		System.out.println("Name " + file[0].getName() + " " + file[1].getName());


		String s;
		FileReader f;

		//verify if file[1] is timeRecordings
		//File instance2 = file[1];
		String s1;
		FileReader f1 = new FileReader(file[1]);
		BufferedReader a1 = new BufferedReader(f1);
		s = a1.readLine();
		if (s.contains("actionDurationTable")) {
			f = new FileReader(file[0]);
			instance = file[0];
		} else {
			f = new FileReader(file[1]);
			f1 = new FileReader(file[0]);
			a1 = new BufferedReader(f1);
			a1.readLine();
			instance = file[1];
		}
		BufferedReader a = new BufferedReader(f);
		//Read First Line
		a.readLine();
		a.readLine();
		a.readLine();
		a.readLine();

		// operations
		a.readLine();
		//	int countOperations = s.split(",").length;
		//	System.out.println(" size "+countOperations);

		// Robot left, robot right, operator
		a.readLine();
		//nmachines = 3;
		//Machines = new Machine[nmachines];

		// products
		s = a.readLine();
		String[] cadArray1 = s.split("=");
		String[] cadArray2 = cadArray1[1].split("[..]+");
		String[] cadArray = cadArray2[1].split(";");
		njobs = Integer.parseInt(cadArray[0]);

		//redimensional array Jobs
		Job[] cloneJobs = Jobs.clone();
		Jobs = new Job[Jobs.length + njobs];
		System.out.println(" size " + Jobs.length);
		//Jobs = cloneJobs.clone();
		for (int i = 0; i < Jobs.length - njobs; i++) {
			Jobs[i] = cloneJobs[i];
			System.out.println(i + "  " + Jobs[i].GetID());
		}


		// zones
		s = a.readLine();
		cadArray1 = s.split("=");
		cadArray2 = cadArray1[1].split("[..]+");
		cadArray = cadArray2[1].split(";");
		countZones = Integer.parseInt(cadArray[0]);
		//	zone = new Zone[countZones];

		a.readLine();
		a.readLine();
		//a.readLine();
		//a.readLine();

		//System.out.println(" product "+ njobs+" zones "+countZones+" operations "+countOperations);

		//int tmp_zone = 0;

		//count zones
		for (int i = 0; i < countZones; i++) {
			//zone[i] = new Zone(i+1);
			//count products
			a.readLine();
			for (int j = Jobs.length - njobs; j < Jobs.length; j++) {
				if (i == 0) {
					Jobs[j] = new Job(j);
					Jobs[j].operations = new ArrayList<>();
					Jobs[j].opStart = 0;
					//a1.readLine();// read [ or white space
					a1.readLine();// read product
				}

				a.readLine(); //product name
				//System.out.println(" zone "+ i);

				//System.out.println(j);
				//System.out.println(" product "+zone[i].product.length +" zones "+zone.length+" operations "+zone[i].product[j].operations.length);

				//count operation
				s = a.readLine();//First operation
				int k = 0; //number of operations
				char ch1 = ']';
				while (!s.equals("") && s.charAt(0) != ch1) {
					boolean zone_occupied = false; // value Hashtable

					cadArray1 = s.split("%");
					cadArray = cadArray1[0].split(",");


					for (int l = 0; l < nmachines; l++) {
						//int job_operation_machine[] = new int[3]; // key Hashtable
						//job_operation_machine[0] = j; // job
						//job_operation_machine[1] = k; // operation
						//job_operation_machine[2] = l; //machine
						String job_operation_machine = "" + j + k + l;
						//System.out.println("ppp "+job_operation_machine);
						zone_occupied = !cadArray[l].equals("0");
						zone[i].job_operation_occupied.put(job_operation_machine, zone_occupied);//fill in Hashtable
						//System.out.println(zone[i].job_operation_occupied.get(job_operation_machine));
						// System.out.println("job "+ j+" operation "+k+" machine "+l+" zone occupied "+ zone_occupied+" boolean ");
					}

					//read times
					if (i == 0) {
						s1 = a1.readLine();// read operation
						cadArray1 = s1.split("[ \t]+");
						cadArray = cadArray1[0].split(",");
						ArrayList<Integer> arrayTimes = new ArrayList<Integer>();// fill in with times > 0


						for (String value : cadArray) {
							//System.out.println(" cadArray "+ cadArray[k2]);
							if (!value.equals("0")) {
								//System.out.println(" izq "+ cadArray[k2]);
								arrayTimes.add(Integer.parseInt(value));
							}
						}

						Jobs[j].operations.add(new Operation(k, j, cadArray1[2]));
						actions.put(cadArray1[2], k);
						//	System.out.println(" name "+ cadArray1[2]);
						Jobs[j].operations.get(k).machines = new int[arrayTimes.size()];
						Jobs[j].operations.get(k).times = new int[arrayTimes.size()];
						Jobs[j].operations.get(k).QV = new double[arrayTimes.size()];

						int k2 = 0;
						for (int l = 0; l < cadArray.length; l++) {
							if (!cadArray[l].equals("0")) {
								//System.out.println(" down "+ cadArray[l]+" position "+ l);
								Jobs[j].operations.get(k).machines[k2] = l + 1;
								Jobs[j].operations.get(k).times[k2] = Integer.parseInt(cadArray[l]);
								//Initialize the QV
								Jobs[j].operations.get(k).QV[k2] = 0;
								k2++;
							}
						}
					}
					k++;
					s = a.readLine();
				}
				max_num_operations = Math.max(max_num_operations, k);
				if (i == 0)
					a1.readLine();
			}
			//System.out.println(zone[i].job_operation_occupied.size());
			//a.readLine();

		}
	 	
	 /*	for (int j=0; j < nmachines; j++)
	 		Machines[j] = new Machine(j);
	 	//max_num_operations = countOperations;
	 	System.out.println(" [max_num_operations "+ max_num_operations+" njobs "+ njobs);
	 	for (int m=0; m < nmachines; m++)
	 		Machines[m].QValues = new double[njobs][max_num_operations];
	 	*/
		a.readLine(); // reading line finishActionBefore
		a.readLine();
		a.readLine();
		s = a.readLine();
		char ch1 = '|';
		while (s.charAt(0) != ch1) {
			cadArray = s.split(",");
			cadArray1 = cadArray[1].split(" ");
			cadArray[1] = cadArray1[1];
			//System.out.println(cadArray[0] + " " + cadArray[1] + " size " + cadArray.length);
			for (int i = Jobs.length - njobs; i < Jobs.length; i++) {
				Jobs[i].operations.get(actions.get(cadArray[1])).operation_precedent = actions.get(cadArray[0]);
			}
			s = a.readLine();
		}

		a.readLine();//reading white space
		a.readLine(); // reading line back2back
		s = a.readLine();
		while (s.charAt(0) != ch1) {
			cadArray = s.split(",");
			cadArray1 = cadArray[1].split(" ");
			cadArray[1] = cadArray1[1];
			//System.out.println(cadArray[0] + " " + cadArray[1] + " size " + cadArray.length);
			for (int i = Jobs.length - njobs; i < Jobs.length; i++) {
				Jobs[i].operations.get(actions.get(cadArray[1])).back2back_before = actions.get(cadArray[0]);
			}
			s = a.readLine();
		}
//	 printValues();
		a.close();
		a1.close();
		njobsNew = njobs;
		SearchRoutesVersion2(njobs);
		njobs = Jobs.length;
		for (int m = 0; m < nmachines; m++)
			Machines[m].QValues = new double[njobs][max_num_operations];
		f1.close();
	}

	public void fusionFiles() {

		File[] targetFile = new File[2]; //read files of old products
		targetFile[0] = new File(String.format("%s/targetFile_copy.txt", UPLOAD_FOLDER));
		targetFile[1] = new File(String.format("%s/targetFile2_copy.txt", UPLOAD_FOLDER));
		File[] reschedule = new File[2]; //read files of new products
		reschedule[0] = new File(String.format("%s/constraints_reschedule.txt", UPLOAD_FOLDER));
		reschedule[1] = new File(String.format("%s/timeRecordings_reschedule.txt", UPLOAD_FOLDER));

		PrintWriter pwConstraint, pwTimeRecording;

		File solutionFile = new File(String.format("%s/targetFileTest.txt", UPLOAD_FOLDER));
		File solutionFile2 = new File(String.format("%s/targetFileTest2.txt", UPLOAD_FOLDER));
		try {
			pwConstraint = new PrintWriter(solutionFile);
			pwTimeRecording = new PrintWriter(solutionFile2);

			String s,s1;
			FileReader f, f1;
			BufferedReader a1 = null, a = null;
			BufferedReader a_reschedule = null, a1_reschedule = null;

			try {
				//Primeros dos files
				f = new FileReader(targetFile[0]);
				a = new BufferedReader(f);
				f1 = new FileReader(targetFile[1]);
				a1 = new BufferedReader(f1);
				//Nuevos dos files
				f = new FileReader(reschedule[0]);
				a_reschedule = new BufferedReader(f);
				f1 = new FileReader(reschedule[1]);
				a1_reschedule = new BufferedReader(f1);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			//write lines from %%R1 to resources={....}
			for (int i=0; i < 6 ; i++) {
				s = a.readLine();
				//System.out.println(s);
				pwConstraint.println(s);
				a_reschedule.readLine();
				pwConstraint.flush();
			}
			a1_reschedule.readLine();
			pwTimeRecording.println(a1.readLine());//first line of time recording
			s = a.readLine();//cantidad prod primer old file
			String []cadArray1 = s.split("=");
			String []cadArray2 = cadArray1[1].split("[..]+");
			String []cadArray = cadArray2[1].split(";");
			int cantJobOldFile = Integer.parseInt(cadArray[0]);
			s = a_reschedule.readLine();//cantidad prod primer new file
			//int cantJobNewFile = Integer.parseInt(cadArray[0]);
			int prod = 0;
			for (int i = 0; i < njobs; i++){
				if (Jobs[i].opStart < Jobs[i].operations.size())
					prod++;
			}

			pwConstraint.println("PRODUCTS = 1.."+prod+";");
			//System.out.println("PRODUCTS = 1.."+prod+";");
			a.readLine(); a_reschedule.readLine();
			pwConstraint.println("ZONES = 1.."+zone.length+";");
			//System.out.println("ZONES = 1.."+zone.length+";");
			a_reschedule.readLine();
			s = a.readLine(); //System.out.println(s);
			pwConstraint.println(s);//espacio en blanco
			a_reschedule.readLine();
			s = a.readLine(); //System.out.println(s);
			pwConstraint.println(s);//zone Occupation
			pwConstraint.flush();
			for (int i = 0; i < zone.length; i++) {
				a.readLine();a_reschedule.readLine();//%%zone
				pwConstraint.println("%%zone"+zone[i].id_zone);
				//System.out.println("%%zone"+zone[i].id_zone);
				int p = 1;
				for (int j = 0; j < njobs; j++) {
					s = (j < cantJobOldFile) ? a.readLine() : a_reschedule.readLine();//read % y product que toca file constraint
					if ((j < cantJobOldFile) && i == 0) {
						a1.readLine();
					} else {
						a1_reschedule.readLine();
					}//read % y product que toca file time recording
					cadArray1 = s.split("=");
					if (Jobs[j].opStart < Jobs[j].operations.size()){
						pwConstraint.println("% P"+(p)+" = "+cadArray1[1]);
						pwTimeRecording.println("% P"+(p)+" = "+cadArray1[1]);
						p++;
					}

					//System.out.println("% P"+(j+1)+" = "+cadArray1[1]);
					s = (j < cantJobOldFile) ? a.readLine() : a_reschedule.readLine();//First operation
					s1 = (j < cantJobOldFile && i == 0) ? a1.readLine() : a1_reschedule.readLine();//First operation

					char ch1 = ']';
					while (!s.equals("") && s.charAt(0) != ch1) {
						if (Jobs[j].opStart < Jobs[j].operations.size()) {
							pwConstraint.println(s);
							//System.out.println(s);
							if (i == 0)
								pwTimeRecording.println(s1);
						}
						//System.out.println(s);
						s = (j < cantJobOldFile) ? a.readLine() : a_reschedule.readLine();//operations
						s1 = (j < cantJobOldFile && i == 0) ? a1.readLine() : a1_reschedule.readLine();//time of operations

					}
					if (Jobs[j].opStart < Jobs[j].operations.size()) {
						//System.out.println(s);
						pwConstraint.println(s);//espacio en blanco
						if (i == 0) {
							if (j == njobs - 1)
								pwTimeRecording.println(s1);
							else
								pwTimeRecording.println("");
							pwTimeRecording.flush();
						}
					}
					//System.out.println(s);
					pwConstraint.flush();

				}
			}
			pwConstraint.println(a.readLine());
			pwConstraint.println(a.readLine());
			pwConstraint.flush();
			s = a.readLine();
			//int i = 0;
			while (s != null){
				//System.out.println(s);
				pwConstraint.println(s);
				s = a.readLine();
				//System.out.println("uuu "+s);
				pwConstraint.flush();
			}
			a.close();
			a1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
}
