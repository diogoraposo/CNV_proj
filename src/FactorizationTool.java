import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.Executors;

import BIT.highBIT.BasicBlock;
import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;

public class FactorizationTool {
	
	private static Vector num_func_calls = new Vector();
	private static Vector dyn_num_bb = new Vector();
	private static Vector dyn_num_instr = new Vector();
	private static Vector thread_cpu_time = new Vector();
	private static Vector time = new Vector();
	private static int num_threads;       
	
	public static void main(String argv[]){
		
		if (argv.length < 1 ) {
			printUsage();
		}
		
		String in_dir = System.getProperty("user.dir") + System.getProperty("file.separator") + argv[0];
		String tmp_out = argv[0].split("\\.")[0] + "Out" + ".class";
		String out_dir = in_dir;
		String log_file = System.getProperty("user.dir") + System.getProperty("file.separator") + argv[0] + ".log";
		if (in_dir.endsWith(".class")) {
			ClassInfo ci = new ClassInfo(in_dir);
			for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
				Routine routine = (Routine) e.nextElement();
				routine.addBefore("FactorizationTool", "dynMethodCount", new Integer(1));
				//routine.addAfter("FactorizationTool", "stopCountCpu", new Integer(0));
				if(routine.getMethodName().equals("end")){
					routine.addBefore("FactorizationTool", "logFile", log_file);
				}
				if(routine.getMethodName().equals("calcPrimeFactors")){
					routine.addBefore("FactorizationTool", "startCountCpu", new Integer(1));
				}
            
				for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
					BasicBlock bb = (BasicBlock) b.nextElement();
					bb.addBefore("FactorizationTool", "dynInstrCount", new Integer(bb.size()));
				}
			}

			ci.write(out_dir);
		}
		else {
			printUsage();
		}
		
	}
	
    public static synchronized void dynInstrCount(int incr) 
		{
    		long thread = Thread.currentThread().getId();
    		
    		int i = 0;
    		if(dyn_num_bb.isEmpty()){
    			ThreadStat temp = new ThreadStat();
				ThreadStat temp2 = new ThreadStat();
				temp.setStat(incr);
				temp.setThread_num(thread);
				temp2.setStat(1);
				temp2.setThread_num(thread);
				dyn_num_bb.add(temp);
				dyn_num_instr.add(temp2);
    		} else {
    			while( ((ThreadStat)dyn_num_bb.get(i)).getThread_num() != thread ){
					i++;
					if(i==dyn_num_bb.size()){
						break;
					}
				}

    			if(i==dyn_num_bb.size()){
    				ThreadStat temp = new ThreadStat();
    				ThreadStat temp2 = new ThreadStat();
    				temp.setStat(incr);
    				temp.setThread_num(thread);
    				temp2.setStat(1);
    				temp2.setThread_num(thread);
    				dyn_num_bb.add(temp);
    				dyn_num_instr.add(temp2);
    			} else {
    				((ThreadStat)dyn_num_bb.get(i)).setStat(((ThreadStat)dyn_num_bb.get(i)).getStat()+incr);
    				((ThreadStat)dyn_num_instr.get(i)).setStat(((ThreadStat)dyn_num_instr.get(i)).getStat()+1);
    			}
    		}
		}
    
    public static synchronized void startCountCpu(int expendable){
    	long thread = Thread.currentThread().getId();
    	ThreadStat temp = new ThreadStat();
    	temp.setLongStat((long)System.currentTimeMillis());
    	temp.setThread_num(thread);
    	time.add(temp);
    }
    
    public static synchronized void stopCountCpu(int expendable){
    	long thread = Thread.currentThread().getId();
    	int i = 0;
    	while( ((ThreadStat)num_func_calls.get(i)).getThread_num() != thread ){
			i++;
			if(i==num_func_calls.size()){
				break;
			}
		}
		((ThreadStat)time.get(i)).setLongStat(((long)System.currentTimeMillis()) - ((ThreadStat)time.get(i)).getLongStat());
    }

    public static synchronized void dynMethodCount(int incr) 
		{
			long thread = Thread.currentThread().getId();
			int i = 0;
			if(num_func_calls.isEmpty()){
				ThreadStat temp = new ThreadStat();
				temp.setStat(1);
				temp.setThread_num(thread);
				num_func_calls.add(temp);
			} else {
				while( ((ThreadStat)num_func_calls.get(i)).getThread_num() != thread ){
					i++;
					if(i==num_func_calls.size()){
						break;
					}
				}
				if(i==num_func_calls.size()){
					ThreadStat temp = new ThreadStat();
					temp.setStat(1);
					temp.setThread_num(thread);
					num_func_calls.add(temp);
				} else {
					((ThreadStat)num_func_calls.get(i)).setStat(((ThreadStat)num_func_calls.get(i)).getStat()+1);
				}
			}
		}
    
    public static synchronized void logFile(String log_file){
    	num_threads = Thread.activeCount();
    	stopCountCpu(1);
    	FileWriter fw;
		try {
			fw = new FileWriter(log_file);
			BufferedWriter bw = new BufferedWriter(fw);
			int i = 0;
			for(i=0 ; i<num_func_calls.size() ; i++){
				System.out.println("For thread: " + ((ThreadStat)num_func_calls.get(i)).getThread_num());
				System.out.println("num_func_calls: " + ((ThreadStat)num_func_calls.get(i)).getStat());
				System.out.println("dyn_num_bb: " + ((ThreadStat)dyn_num_bb.get(i)).getStat());
				System.out.println("dyn_num_instr: " + ((ThreadStat)dyn_num_instr.get(i)).getStat());
				System.out.println("num_threads: " + num_threads);
				System.out.println("time_on_cpu: " + ((ThreadStat)time.get(i)).getLongStat());
				System.out.println("---------------------------------------------\n");
				bw.write("For thread: " + ((ThreadStat)num_func_calls.get(i)).getThread_num() + "\n");
				bw.write("num_func_calls: " + ((ThreadStat)num_func_calls.get(i)).getStat() + "\n");
				bw.write("dyn_num_bb: " + ((ThreadStat)dyn_num_bb.get(i)).getStat() + "\n");
				bw.write("dyn_num_instr: " + ((ThreadStat)dyn_num_instr.get(i)).getStat() + "\n");
				bw.write("num_threads: " + num_threads + "\n");
				bw.write("time_on_cpu: " + ((ThreadStat)time.get(i)).getLongStat() + "\n");
				String rqstr = ((ThreadStat)num_func_calls.get(i)).getThread_num() + ","
						+ ((ThreadStat)num_func_calls.get(i)).getStat() + ","
						+ ((ThreadStat)dyn_num_bb.get(i)).getStat() + ","
						+ ((ThreadStat)dyn_num_instr.get(i)).getStat() + ","
						+ num_threads + ","
						+ ((ThreadStat)time.get(i)).getLongStat();
//				Thread[] threadArray = (Thread[])Thread.getAllStackTraces().keySet().toArray(new Thread[Thread.getAllStackTraces().keySet().size()]);
//				for(int j = 0; j< Thread.getAllStackTraces().keySet().size();j++){
//					System.out.println("Thread list " + ((Thread)Thread.getAllStackTraces().keySet().toArray()[j]).getId());
//					rqstr += "," + ((Thread)Thread.getAllStackTraces().keySet().toArray()[j]).getId();
//				}
				rqstr += "\n";
				System.out.println("Response: " + rqstr);
				bw.write("---------------------------------------------\n");
				Socket request = new Socket(InetAddress.getByName("127.0.0.2"), 11000);
				DataOutputStream out = new DataOutputStream(request.getOutputStream());
				out.write(rqstr.getBytes());
								
			}
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    

	public static void printUsage(){
		System.out.println("Please provide 1 argument:");
		System.out.println(">File to be instrumented (in_dir)");
	}
	
}
