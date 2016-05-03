package FactorizationDB;

public class FactorizationElement {

	private int numFuncCalls;
	private int dynNumBB;
	private int dynNumInst;
	private long timeOnCpu;
	
	
	public FactorizationElement(int numFuncCalls, int dynNumBB, int dynNumInst, long timeOnCpu) {
		this.numFuncCalls = numFuncCalls;
		this.dynNumBB = dynNumBB;
		this.dynNumInst = dynNumInst;
		this.timeOnCpu = timeOnCpu;
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
	
	
}
