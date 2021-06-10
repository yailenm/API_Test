package org.ws.gui;

public class MachineGUI {

	private final int id;
	
	public int criticality;
	
	public MachineGUI(int id, int stage, int releaseTime) {
		this.id = id;
	}
	
	public MachineGUI(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
}
