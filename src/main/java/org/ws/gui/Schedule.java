package org.ws.gui;

import java.util.ArrayList;
import java.util.HashMap;

//import GUI.ScheduleJComponent.Boton;

/**
 * A Schedule is a solution to a HFFSPInstance, assigning operations to machines over time. A Schedule object
 * is immutable.
 * @author bert
 *
 */
public class Schedule {
    
    //---------- Constants ------------------------------------------------------------------------
    
    /**
     * The empty schedule.
     */
    public static Schedule EMPTY = new Schedule(new ArrayList<OperationAllocation>());
    
    //---------- Member variables -----------------------------------------------------------------
    
    /** The list of operation allocations. */
	private final HashMap<OperationGUI,OperationAllocation> allocations;
	
	/** The makespan of this schedule. */
	private int makespan;
	
	/** Keeps the first available time slot for each machine. */
	private final HashMap<MachineGUI,Integer> firstAvailableTime;

	/** 
	 * Keeps the last allocated job for each machine. This is necessary to determine sequence
	 * dependent setup times.
	 */
    private final HashMap<MachineGUI,JobGUI> lastAllocatedJobs;

    /** Keeps the completion times of each job. */
    private final HashMap<JobGUI, Integer> jobCompletionTimes;
		
	//---------- Construction ---------------------------------------------------------------------
	
	/**
	 * Creates a schedule with the specified operation allocations.
	 * @param allocations the operation allocations making up this schedule.
	 */
	public Schedule(ArrayList<OperationAllocation> allocations) { 
		this.allocations = new HashMap<>();
		this.firstAvailableTime = new HashMap<>();
		this.lastAllocatedJobs = new HashMap<>();
		this.jobCompletionTimes = new HashMap<>();
		
		int makespan = -1;
		
		// Go through the operations making up this schedule and calculate some values for later reference
		for (OperationAllocation alloc : allocations) {
		    // Add allocation
		    this.allocations.put(alloc.getOperation(), alloc);
		    JobGUI curJob = alloc.getOperation().getJob();
		    MachineGUI curMachine = alloc.getMachine();
		    int endTime = alloc.getEndTime();
		    
		    // Adjust makespan, if necessary
		    if(endTime > makespan) {
				makespan = endTime;
			}
		    
		    // Adjust first available time slots and last allocated jobs per machine,
		    // if necessary
		    if(!firstAvailableTime.containsKey(curMachine) || 
		            endTime > firstAvailableTime.get(curMachine)) {
		        firstAvailableTime.put(curMachine, endTime);
		        lastAllocatedJobs.put(curMachine, curJob);
		    }
		    
		    // Adjust job completion times, if necessary
		    if(!jobCompletionTimes.containsKey(curJob) ||
		            endTime > jobCompletionTimes.get(curJob)) {
		        jobCompletionTimes.put(curJob, endTime);
		    }
		}
		
//		System.out.println(" FAT: " + firstAvailableTime);
//		System.out.println(" JCT: " + jobCompletionTimes);
//		System.out.println(" LAJ: " + lastAllocatedJobs);
		
		this.makespan = makespan;
	}
	
	//---------- Accessors ------------------------------------------------------------------------

	/**
	 * Returns the makespan of this schedule.
	 * @return the makespan of this schedule.
	 */
	public int getMakespan() {
		return makespan;
	}

	/**
	 * Returns the list of operation allocations. This method creates a defensive copy of the allocation list, so
	 * limit the number of calls when possible.
	 * @return the list of operation allocations.
	 */
	public ArrayList<OperationAllocation> getAllocations() {
	    return new ArrayList<>(allocations.values());
	}
	
	/**
	 * Returns a new schedule with the specified allocation added. The current schedule remains unchanged.
	 * @param alloc an allocation to be added to the current schedule.
	 * @return a new schedule with the specified allocation added.
	 */
	public Schedule addAllocation(OperationAllocation alloc) {
	    ArrayList<OperationAllocation> newAlloc = getAllocations();
	    newAlloc.add(alloc);
	    return new Schedule(newAlloc);
	}
	
	// Queries for operations
	
	/**
	 * Returns the time at which the setup of the specified operation begins. 
	 * @param op an operation in this schedule.
	 * @return the time at which the setup of the specified operation begins.
	 */
	public int getSetupTime(OperationGUI op) {
        return findAlloc(op).getSetupTime();
    }
    
	/**
     * Returns the time at which the processing of the specified operation begins. 
     * @param op an operation in this schedule.
     * @return the time at which the processing of the specified operation begins.
     */
    public int getStartTime(OperationGUI op) {
	    return findAlloc(op).getStartTime();
	}
	
    /**
     * Returns the time at which the processing of the specified operation ends. 
     * @param op an operation in this schedule.
     * @return the time at which the processing of the specified operation ends.
     */
    public int getEndTime(OperationGUI op) {
	    return findAlloc(op).getEndTime();
	}
	
    /**
     * Returns the machine which the specified operation is allocated on.
     * @param op an operation in this schedule.
     * @return the machine which the specified operation is allocated on.
     */
	public MachineGUI getAllocatedMachine(OperationGUI op) {
	    return findAlloc(op).getMachine();
	}
	
	/**
	 * Find the allocation data of the specified operation.
	 * @param op an operation in this schedule.
	 * @return the allocation data of the specified operation.
	 */
	private OperationAllocation findAlloc(OperationGUI op) {
	    if(!allocations.containsKey(op)) {
	        throw new IllegalArgumentException("Operation " + op + " is not allocated in this schedule");
	    }
	    return allocations.get(op);
	}
	
	/**
	 * Predicate that determines whether the specified operation is allocated.
	 * @param op the operation to be checked.
	 * @return <code>true</code> if the specified operation is allocated, else false.
	 */
	public boolean isAllocated(OperationGUI op) {
	    return allocations.containsKey(op);
	}
	
	/**
	 * Predicate that determines whether the specified job is allocated. A job is allocated if all
	 * operations in visited stages are allocated.
	 * @param job the job to be checked.
	 * @return <code>true</code> if the job is allocated, else <code>false</code>
	 */
//	public boolean isAllocated(Job job) {
//	    for (int stage = 0; stage < job.getNumStages(); stage++) {
//            if(job.isStageVisited(stage) && !isAllocated(job.getOperation(stage))) {
//                return false;
//            }
//        }
//	    return true;
//	}
	
	/**
	 * Returns the latest completion time of allocated operations belonging to the specified Job.
	 * @param job the job to be checked
	 * @return the latest completion time of allocated operations belonging to the specified Job.
	 */
	public int getJobCompletionTime(JobGUI job) {
	    return jobCompletionTimes.get(job);
	}
	
	/**
	 * Returns the first time slot when the specified machine is available. If other operations are
	 * allocated to this machine, 
	 * @param machine the machine to be checked.
	 * @return the first time slot when the specified machine is available.
	 */
//	public int getFirstAvailableTime(Machine machine) {
//	    if(!firstAvailableTime.containsKey(machine)) {
//	        return machine.getReleaseTime();
//	    } else {
//	        return firstAvailableTime.get(machine);
//	    }
//	}
	
	/**
	 * Returns the last allocated job on the specified machine.
	 * @param machine the machine to be checked.
	 * @return the last allocated job on the specified machine.
	 */
	public JobGUI getLastAllocatedJob(MachineGUI machine) {
	    return lastAllocatedJobs.get(machine);
	}

    //---------- Overridden methods ---------------------------------------------------------------
	
	@Override
    public String toString() {
        return "{ Schedule:\n  makespan = " + getMakespan() + 
                ";\n  allocations = " + allocations.toString() + "; }";
    }

	public void setStartTime(OperationGUI op, int startTime) {
	     findAlloc(op).setStartTime(startTime);
	}
	
	public void setEndTime(OperationGUI op, int endTime) {
	     findAlloc(op).setEndTime(endTime);
	}
	
	public void setMakespan(int makespan) {
		 this.makespan = makespan;
	}
}
