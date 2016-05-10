package FactorizationAutoScaler;


import java.awt.List;
import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

import FactorizationDB.FactorizationDB_API;
import FactorizationDB.FactorizationElement;


public class AutoScaler {

	private static int period;
	private static AmazonEC2 ec2;
	private static ArrayList<String> instance_ids = new ArrayList<String>();
	private static FactorizationDB_API db_api = new FactorizationDB_API();

	private static void init() throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [default]
		 * credential profile by reading from the credentials file located at
		 * (~/.aws/credentials).
		 */
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (~/.aws/credentials), and is in valid format.",
							e);
		}

		ec2 = new AmazonEC2Client(credentials);
		Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
		ec2.setRegion(euWest1);
	}


	public static void main(String[] args) {

		//period = Integer.parseInt(args[0]);
		period = 60000;
		System.out.println("Don't forget to add the credentials file");
		try {
			init();
			while(true){

				if(!areAnyActive()){
					System.out.println("Period loop ---> There are no instances running, creating one");
					createInstance();
				} else {
					System.out.println("Period loop ---> There are instances running");
				}
	
				FactorizationElement element;
				for(String id: instance_ids){
					System.out.println("Instance id: " + id);
					for(Integer thread: db_api.get_Threads(id)){
						element = db_api.get_Element(id, thread);
						System.out.println("Thread: " + element.getProcessID()
								+ " time_on_cpu: " + element.getTimeOnCpu());
					}
				}
				
				
				Thread.sleep(period);
				//removeInstance(instance_ids.get(0));
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}

	public static void createInstance(){

		System.out.println("Creating a new ec2 instance");
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

		runInstancesRequest.withImageId("ami-9d67e7ee")
		.withInstanceType("t2.micro")
		.withMinCount(1)
		.withMaxCount(1)
		.withKeyName("precious-thing")
		.withSecurityGroups("yesofficer");
		
		RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
		
		instance_ids.add(runInstancesResult.getReservation().getInstances().get(0).getInstanceId());
		for(String i: instance_ids){
			System.out.println("Running instance: " + i);
		}
		
		db_api.createTable(runInstancesResult.getReservation().getInstances().get(0).getInstanceId());
	}

	public static boolean areAnyActive() {
		if(instance_ids.isEmpty()){
			return false;
		} else {
			return true;
		}	
	}

	public static void removeInstance(String id){
		ArrayList<String> singleIdList = new ArrayList<String>();
		singleIdList.add(id);
		TerminateInstancesRequest termInstanceRequest = new TerminateInstancesRequest();
		termInstanceRequest.setInstanceIds(singleIdList);
		
		TerminateInstancesResult termInstancesResult = ec2.terminateInstances(termInstanceRequest);
		
		System.out.println("Terminating instance with id: " + id);
		System.out.println(termInstancesResult.getTerminatingInstances().get(0).getInstanceId());
		
		db_api.deleteTable(id);
	}

}
