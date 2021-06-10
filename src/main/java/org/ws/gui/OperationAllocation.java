package org.ws.gui;

/**
 * An OperationAllocation object represents the assignment of an operation to a machine in time. This class is
 * immutable.
 * @author bert
 *
 */
public class OperationAllocation {
    
    private final OperationGUI operation;
	
	private final MachineGUI machine;
	
	private final int setupTime;
	
	public int startTime;
	
	public int oldStartTime;
	
	public int endTime;
	
	public int oldEndTime;
	
	public boolean border;
	
	//public int back2back_before = -1;

	/**
	 * Creates an OperationAllocation object with the specified values.
	 * @param operation  an operation (part of a job)
	 * @param machine    the machine to which this operation is assigned
	 * @param setupTime  the start of the sequence dependent setup. If no setup is necessary, setupTime is
	 *                   equal to startTime.
	 * @param startTime  the start time of the operation.
	 * @param endTime    the end time of the operation.
	 */
	public OperationAllocation(OperationGUI operation, MachineGUI machine, int setupTime, int startTime, int endTime, boolean border) {
		assert setupTime <= startTime;
		assert startTime <= endTime;
		
		this.operation = operation;
		this.machine = machine;
		this.setupTime = setupTime;
		this.startTime = startTime;
		this.endTime = endTime;
		this.border = border;
	}
	
	/**
     * Creates an OperationAllocation object with the specified values, without a setup time.
     * @param operation  an operation (part of a job)
     * @param machine    the machine to which this operation is assigned
     * @param startTime  the start time of the operation.
     * @param endTime    the end time of the operation.
     */
    public OperationAllocation(OperationGUI operation, MachineGUI machine, int startTime, int endTime,boolean border) {
		this(operation, machine, startTime, startTime, endTime,border);
	}

    /**
     * Returns the allocated operation.
     * @return the allocated operation.
     */
	public OperationGUI getOperation() {
		return operation;
	}
	
	/**
	 * Returns the machine to which this operation is assigned.
	 * @return the machine to which this operation is assigned.
	 */
    public MachineGUI getMachine() {
        return machine;
    }

    /**
	 * Returns the start of the sequence-dependent setup.
	 * @return the start of the setup.
	 */
	public int getSetupTime() {
		return setupTime;
	}

	/**
	 * Returns the start time of the operation.
	 * @return the start time of the operation.
	 */
	public int getStartTime() {
		return startTime;
	}

	/**
	 * Returns the end time of the operation.
	 * @return the end time of the operation.
	 */
	public int getEndTime() {
		return endTime;
	}
	
	public void setEndTime(int newEndTime){
		endTime = newEndTime;
	}

    //---------- Hashcode, equals -----------------------------------------------------------------
	
	@Override
    public boolean equals(Object obj) {
	    if (obj == this) {
            return true;
        }
        if (! (obj instanceof OperationAllocation)) {
            return false;
        }
        OperationAllocation oa = (OperationAllocation)obj;
        
        return oa.operation.equals(operation)
            && oa.machine.equals(machine)
            && oa.setupTime == setupTime
            && oa.startTime == startTime
            && oa.endTime == endTime;
    }

    @Override
    public int hashCode() {
        int result = 17;
        
        result = 31 * result + operation.hashCode(); 
        result = 31 * result + machine.hashCode();
        result = 31 * result + setupTime;
        result = 31 * result + startTime;
        result = 31 * result + endTime;
        
        return result;
    }

    @Override
    public String toString() {
        
        return "{ OperationAllocation: " + operation +
               " on " + machine + 
               " at (" + setupTime + "," + startTime + "," + endTime + ") }";
    }

	public boolean getBorder() {
		// TODO Auto-generated method stub
		return border;
	}
	
	public void setBorder(){
		border = true;
	}

	public void setStartTime(int startTime) {
		 this.startTime = startTime;
	}
	
	
	
}
