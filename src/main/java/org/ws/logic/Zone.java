package org.ws.logic;

import java.util.Hashtable;

public class Zone {	
	public int id_zone;	// start in 1 
	//int[] job,operation,machine
	public Hashtable<String, Boolean> job_operation_occupied = new Hashtable<>();
	public int time = 0;
	
	public int timeReScheduleZone = 0;
	//public int[] zone_occupied = new int[3];
	
	public Zone(int id_zone) {
		this.id_zone = id_zone;	
	}
		
}
