package FactorizationLoadBalancer;


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


public class LoadBalancer {

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
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}

}
