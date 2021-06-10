package org.ws.gui;


public class JobGUI {

	private final int id;
	private OperationGUI[] operations;
	private int[] processingTimes;
	private int[] slacks;
	
	public JobGUI(int id, int n_op, int[] processingTimes){
		this.id = id;
		this.processingTimes = processingTimes;
		this.operations = new OperationGUI[n_op];
		for (int i = 0; i < n_op; i++) {
				operations[i] = new OperationGUI(i, id, 0, 0,null,-1,-1);			
		}
	}
	
	public JobGUI(int id){
		this.id = id;
	}
	
	public OperationGUI getOperation(int stageNumber) {
		return operations[stageNumber];
	}
	
	public int getId() { 
		return id; 
	}
	
	public int getSlack(int index){
		return slacks[index];
	}
	
	public int processingTime(int machineID) {
		return processingTimes[machineID];
	}
	
	public JobGUI(int id, 
	        int[] stagesVisited, 
	        int[] eligibleMachines, 
	        int[] processingTimes, 
	        int[] predecessors,
	        int[] slacks) {
		this.id = id;
		
//		this.stagesVisited = stagesVisited;
//		this.eligibleMachines = eligibleMachines;
		this.processingTimes = processingTimes;
//		this.predecessors = predecessors;
		this.slacks = slacks;
		
		this.operations = new OperationGUI[stagesVisited.length];
		for (int i = 0; i < stagesVisited.length; i++) {
//			if (isStageVisited(i)) {
				operations[i] = new OperationGUI(i, id, 0, 0,null,-1,-1);
//			}  
		}
	}
}
