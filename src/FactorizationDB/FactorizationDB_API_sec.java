package FactorizationDB;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;

public class FactorizationDB_API_sec {
	
	private static AmazonDynamoDBClient _db;
	private static ServerSocket local;
	private static String instance_id;
	
	public static void main(String[] args){
		//initialize();
		try {
			/*URL url = new URL("http://169.254.169.254/latest/meta-data/instance-id");
		    URLConnection conn = url.openConnection();
		    Scanner s = new Scanner(conn.getInputStream());
		    if (s.hasNext()) {
		      System.out.println(s.next());
		    }*/
			
			DataInputStream rcv;
			local = new ServerSocket(11000, 20, InetAddress.getByName("127.0.0.2"));
			while(true){
				System.out.println("Starting local process");
				Socket rcvlocal = local.accept();
				rcv = new DataInputStream(rcvlocal.getInputStream());
				String read = rcv.readLine();
				//System.out.println("Read: " + read);
				String[] parts = read.split(",");
				System.out.println("For thread: " + parts[0]);
				System.out.println("num_func_calls: " + parts[1]);
				System.out.println("dyn_num_bb: " + parts[2]);
				System.out.println("dyn_num_instr: " + parts[3]);
				System.out.println("num_threads: " + parts[4]);
				System.out.println("time_on_cpu: " + parts[5]);
//				for(int i = 6; i<parts.length;i++){
//					System.out.println("Active thread: " + parts[i]);
//				}
				System.out.println("---------------------------------------------\n");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void initialize(){
		AWSCredentials credentials = null;
        
        ProfileCredentialsProvider pcp = new ProfileCredentialsProvider();
        credentials = pcp.getCredentials();
        _db = new AmazonDynamoDBClient(credentials);
        _db.setRegion(Region.getRegion(Regions.EU_WEST_1));
	}
	
	public boolean doesTableExist(String table_id){
		boolean answer;
		try{
			TableDescription table = _db.describeTable(new DescribeTableRequest(table_id)).getTable();
			answer = TableStatus.ACTIVE.toString().equals(table.getTableStatus());
		}catch(ResourceNotFoundException rnfe){
			answer = false;
		}
		return answer;
	}
	
	public void createTable(String instance_id){
		if(this.doesTableExist(instance_id)){
			//do nothing
		}else{
			CreateTableRequest ctr = new CreateTableRequest();
			ctr.withTableName(instance_id);
			
			ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
			keySchema.add(new KeySchemaElement().withAttributeName("processID").withKeyType(KeyType.HASH));
			
			AttributeDefinition atribDef = new AttributeDefinition();
			atribDef.withAttributeName("processID").withAttributeType(ScalarAttributeType.S);
			
			ProvisionedThroughput pt = new ProvisionedThroughput();
			pt.withReadCapacityUnits(6L);
			pt.withWriteCapacityUnits(6L);
			
			ctr.withKeySchema(keySchema);
			ctr.withAttributeDefinitions(atribDef);	
			ctr.withProvisionedThroughput(pt);
			
			CreateTableResult result = _db.createTable(ctr);
			System.out.println(result);
		}
	}
	
	public void deleteTable(String instance_id){
		if(this.doesTableExist(instance_id)){
			DeleteTableRequest dtr = new DeleteTableRequest();
			dtr.withTableName(instance_id);
			System.out.println("Issuing Delete table request for: " + instance_id);
			DeleteTableResult result = _db.deleteTable(dtr);
			System.out.println(result);
		}
	}
	
	public void putMetric(String instance_id, int processID, int numFuncCalls, int dynNumBB, int dynNumInst, long timeOnCpu){
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		
		item.put("processID", new AttributeValue("" + processID));		
		item.put("numFuncCalls", new AttributeValue("" + numFuncCalls));
		item.put("dynNumBB", new AttributeValue("" + dynNumBB));
		item.put("dynNumInst", new AttributeValue("" + dynNumInst));
		item.put("timeOnCpu", new AttributeValue("" + timeOnCpu));
		
		PutItemRequest pir = new PutItemRequest(instance_id, item);
		PutItemResult result = _db.putItem(pir);
		
		System.out.println("Result: " + result);
	}
	
	public void insertAllElements(String instance_id, int thread_num, FactorizationElement element){
		this.putMetric(instance_id, thread_num, element.getNumFuncCalls(), element.getDynNumBB(), element.getDynNumInst(), element.getTimeOnCpu());
	}
	
	private FactorizationElement map2Factor(Map<String, AttributeValue> item){
		System.out.println(item);
		return new FactorizationElement(item.get("processID").getS(), item.get("numFuncCalls").getS(), item.get("dynNumBB").getS(), item.get("dynNumInst").getS(), item.get("timeOnCpu").getS());
	}
	
	private FactorizationElement getProcessInstrumentationData(String instance_id, int processID){
		FactorizationElement answer = null;
		Map<String, AttributeValue> itemKey = new HashMap<String, AttributeValue>();
		Map<String, AttributeValue> fetchedItem;
		itemKey.put("processID", new AttributeValue("" + processID));
		
		try{
			GetItemRequest gir = new GetItemRequest();
			gir.withTableName(instance_id);
			gir.withKey(itemKey);
			
			GetItemResult result = _db.getItem(gir);
			fetchedItem = result.getItem();
			
			answer = map2Factor(fetchedItem);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return answer;
	}
	
	private ArrayList<FactorizationElement> getAllProcessInstrumentationData(String instance_id){
		ArrayList<FactorizationElement> answer = new ArrayList<FactorizationElement>();
		
		ScanRequest sr = new ScanRequest(instance_id);
		for(Map<String, AttributeValue> item : _db.scan(sr).getItems()){
			answer.add(this.map2Factor(item));
		}
		
		return answer;
	}
	
	//All elements in one request
	public FactorizationElement get_Element(String instance_id, int thread_num){
		return this.getProcessInstrumentationData(instance_id, thread_num);
	}
	
	//All registered metrics from one instance
	public ArrayList<FactorizationElement> getAllElements(String instance_id){
		return this.getAllProcessInstrumentationData(instance_id);
	}
	
	//Function calls
	public int get_NumFuncCalls(String instance_id, int thread_num){
		return this.getProcessInstrumentationData(instance_id, thread_num).getNumFuncCalls();
	}
	
	//Basic-blocks
	public int get_DynNumBB(String instance_id, int thread_num){
		return this.getProcessInstrumentationData(instance_id, thread_num).getDynNumBB();
	}
	
	//Instructions
	public int get_DynNumInst(String instance_id, int thread_num){
		return this.getProcessInstrumentationData(instance_id, thread_num).getDynNumInst();
	}
	
	//Time on cpu
	public long get_TimeOnCPU(String instance_id, int thread_num){
		return this.getProcessInstrumentationData(instance_id, thread_num).getTimeOnCpu();
	}
	
	//Get list of threads 
	public ArrayList<Integer> get_Threads(String instance_id){
		ArrayList<Integer> threads = new ArrayList<Integer>();
		
		for(FactorizationElement fe : this.getAllElements(instance_id)){
			threads.add(fe.getProcessID());
		}
		
		return threads;
	}
	
	
}
