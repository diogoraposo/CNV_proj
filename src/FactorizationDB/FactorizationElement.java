package FactorizationDB;

public class FactorizationElement {

	private int processID;
	private int numFuncCalls;
	private int dynNumBB;
	private int dynNumInst;
	private long timeOnCpu;
	private long endTime;
	
	public FactorizationElement(String processID, String numFuncCalls, String dynNumBB, String dynNumInst, String timeOnCpu, String endTime) {
		this.processID = Integer.valueOf(processID);
		this.numFuncCalls = Integer.valueOf(numFuncCalls);
		this.dynNumBB = Integer.valueOf(dynNumBB);
		this.dynNumInst = Integer.valueOf(dynNumInst);
		this.timeOnCpu = Long.valueOf(timeOnCpu);
		this.endTime = Long.valueOf(endTime);
	}
	
	public FactorizationElement(int processID, int numFuncCalls, int dynNumBB, int dynNumInst, long timeOnCpu, long endTime) {
		this.processID = processID;
		this.numFuncCalls = numFuncCalls;
		this.dynNumBB = dynNumBB;
		this.dynNumInst = dynNumInst;
		this.timeOnCpu = timeOnCpu;
		this.endTime = endTime;
	}


	public int getProcessID(){
		return processID;
	}
	
	public int getNumFuncCalls() {
		return numFuncCalls;
	}


	public int getDynNumBB() {
		return dynNumBB;
	}


	public int getDynNumInst() {
		return dynNumInst;
	}


	public long getTimeOnCpu() {
		return timeOnCpu;
	}
	
	public long getEndTime(){
		return endTime;
	}
	
	@Override
	public String toString(){
		return "processID: " + processID + " numFuncCalls: " + numFuncCalls + " dynNumBB: " + dynNumBB + " dynNumInst: " + dynNumInst + " timeOnCpu: " + timeOnCpu + " endtime: " + endTime;
	}
	
}
