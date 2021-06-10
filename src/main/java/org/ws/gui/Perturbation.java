package org.ws.gui;

public class Perturbation {

	int IDmachine;
	int startTime;
	int duration;

	public Perturbation(int machine, int start, int dur){
		IDmachine = machine;
		startTime = start;
		duration = dur;
	}
	
	public int getMachine(){
		return IDmachine;
	}
	
	public int getstartTime(){
		return startTime;
	}
	
	public int getDuration(){
		return duration;
	}
	
}
