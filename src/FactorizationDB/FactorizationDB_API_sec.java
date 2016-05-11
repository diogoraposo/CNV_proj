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


import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
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
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;

public class FactorizationDB_API_sec implements Runnable{

	private static AmazonDynamoDBClient _db;
	private static ServerSocket local;
	private static String instance_id;

	private static Socket rcvlocal;

	public FactorizationDB_API_sec(Socket loc, String instance) {
		rcvlocal = loc;
		instance_id = instance;
	}

	@Override
	public void run() {
		initialize();
		DataInputStream rcv;
		try {
			String read;
			rcv = new DataInputStream(rcvlocal.getInputStream());
			while((read = rcv.readLine()) != null){
				String[] parts = read.split(",");
				System.out.println("Read: " + read);
				System.out.println("For instance: " + instance_id);
				System.out.println("For thread: " + parts[0]);
				System.out.println("num_func_calls: " + parts[1]);
				System.out.println("dyn_num_bb: " + parts[2]);
				System.out.println("dyn_num_instr: " + parts[3]);
				System.out.println("num_threads: " + parts[4]);
				System.out.println("time_on_cpu: " + parts[5]);
				System.out.println("---------------------------------------------\n");

				if(Long.parseLong(parts[5]) > 10000000){
					break;
				}
				read.substring(parts[0].length() 
						+ parts[1].length() 
						+ parts[2].length() 
						+ parts[3].length() 
						+ parts[4].length() 
						+ parts[5].length());
				System.out.println("Going to put elements");
				putMetric(instance_id, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Long.parseLong(parts[5]));
				System.out.println("Done putting elements");
				//				for(FactorizationElement f: getAllProcessInstrumentationData(instance_id)){
				//					System.out.println("key" + f.getProcessID());
				//					if(!read.contains(Integer.toString(f.getProcessID()))){
				//						System.out.println("DELETING THREAD " + f.getProcessID());
				//						deleteThread(f.getProcessID());
				//					}
				//				}	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public static void main(String[] args){
		try {
			URL url = new URL("http://169.254.169.254/latest/meta-data/instance-id");
			URLConnection conn = url.openConnection();
			Scanner s = new Scanner(conn.getInputStream());
			if (s.hasNext()) {
				instance_id = s.next();
				System.out.println(instance_id);

			}

			local = new ServerSocket(11000, 20, InetAddress.getByName("127.0.0.2"));
			System.out.println("Starting local process");
			while(true){
				rcvlocal = local.accept();
				System.out.println("Starting new thread");
				(new Thread(new FactorizationDB_API_sec(rcvlocal, instance_id))).start();
				System.out.println("Passing in front of created thread");
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
		System.out.println(credentials.getAWSAccessKeyId());
		_db = new AmazonDynamoDBClient(credentials);
		_db.setRegion(Region.getRegion(Regions.EU_WEST_1));
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

	public static void putMetric(String instance_id, int processID, int numFuncCalls, int dynNumBB, int dynNumInst, long timeOnCpu){
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();

		item.put("processID", new AttributeValue("" + processID));		
		item.put("numFuncCalls", new AttributeValue("" + numFuncCalls));
		item.put("dynNumBB", new AttributeValue("" + dynNumBB));
		item.put("dynNumInst", new AttributeValue("" + dynNumInst));
		item.put("timeOnCpu", new AttributeValue("" + timeOnCpu));

		System.out.println("Putting: " + processID + "," + instance_id);		

		PutItemRequest pir = new PutItemRequest(instance_id, item);
		PutItemResult result = _db.putItem(pir);

		System.out.println("Result: " + result);
	}

	public void insertAllElements(String instance_id, int thread_num, FactorizationElement element){
		FactorizationDB_API_sec.putMetric(instance_id, thread_num, element.getNumFuncCalls(), element.getDynNumBB(), element.getDynNumInst(), element.getTimeOnCpu());
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

	public void deleteThread(int thread){
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();

		item.put("processID", new AttributeValue("" + thread));

		System.out.println("Starting to remove");
		DeleteItemRequest pir = new DeleteItemRequest(instance_id, item);
		DeleteItemResult result = _db.deleteItem(pir);

		System.out.println("Result: " + result);
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
