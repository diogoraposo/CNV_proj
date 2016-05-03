package FactorizationDB;

import java.util.ArrayList;

public class FactorizationDB_API {
	
	public void createTable(String instance_id){
		
	}
	
	public void deleteTable(String instance_id){
		
	}
	
	public void insertAllElements(String instance_id, int thread_num){
		
	}
	
	//Function calls
	public int get_NumFuncCalls(String instance_id, int thread_num){
		return 0;
	}
	
	//Basic-blocks
	public int get_DynNumBB(String instance_id, int thread_num){
		return 0;
	}
	
	//Instructions
	public int get_DynNumInst(String instance_id, int thread_num){
		return 0;
	}
	
	//Time on cpu
	public long get_TimeOnCPU(String instance_id, int thread_num){
		return 0;
	}

	//All elements in one request
	public FactorizationElement get_Element(String instance_id, int thread_num){
		return new FactorizationElement(0,0,0,0);
	}
	
	//Get list of threads 
	public ArrayList<Integer> get_Threads(String instance_id){
		ArrayList<Integer> threads = new ArrayList<Integer>();
		return threads;
	}
}
