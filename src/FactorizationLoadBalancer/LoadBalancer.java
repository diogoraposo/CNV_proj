package FactorizationLoadBalancer;

import java.util.ArrayList;
import java.util.Collection;
import FactorizationDB.FactorizationElement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import java.util.Date;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import FactorizationDB.FactorizationDB_API;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadBalancer {

	private static AmazonEC2 ec2;
	private static ArrayList<String> instance_ids = new ArrayList<String>();
	private static FactorizationDB_API db_api = new FactorizationDB_API();


	private static final String USER_AGENT = "Mozilla/5.0";

	private static long avgcpu_upper = 8000000;
	private static int totalbb_upper = 100000;
	private static int totalinst_upper = 50000;

	private static void init() throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [default] credential
		 * profile by reading from the credentials file located at
		 * (~/.aws/credentials).
		 */
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);
		}

		ec2 = new AmazonEC2Client(credentials);
		Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
		ec2.setRegion(euWest1);
	}

	public static void main(String[] args) {

		System.out.println("Don't forget to add the credentials file");
		try {
			init();
			db_api.initialize();	
			HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), 8000), 0);
			System.out.println(InetAddress.getLocalHost().getHostName());
			server.createContext("/f", new MyHandler());
			server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
			server.start();

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	static class MyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String response = "This was the query:" + t.getRequestURI().getQuery() 
					+ "##";

			BigInteger query = new BigInteger(t.getRequestURI().getQuery().split("=")[1]);
			System.out.println("Recv query " + query);
			(new Thread(new requestInstance(query, t))).start();

		}
	}

	public static String statusInstance(String id){
                DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(id);
                DescribeInstancesResult describeInstanceResult = ec2.describeInstances(describeInstanceRequest);
                InstanceState state = describeInstanceResult.getReservations().get(0).getInstances().get(0).getState();
                System.out.println("Instace " + id + " is " + state.getCode());
                return Integer.toString(state.getCode());
        }

	static class requestInstance implements Runnable {
		private BigInteger calc;
		private ArrayList<BigInteger> answers;
		private HttpExchange exchange;

		public requestInstance(BigInteger i, HttpExchange t){
			calc = i;
			exchange = t;
		}

		@Override
		public void run() {

			instance_ids = db_api.listTables();
		//	for(String s : instance_ids){
		//		System.out.println("Repeating list " + s);
		//	}


			int avgcpu;
			int totaldynbb;
			int totalinst;	
			try{
				ArrayList<String> instance_tmps = (ArrayList<String>) instance_ids.clone();
				int lower = 0;
				double cpuu = 0;
				String instance = "";
				for(String id: instance_tmps){
					
					final AmazonCloudWatchClient client = client();
                                        final GetMetricStatisticsRequest request = request(id);
                                        final GetMetricStatisticsResult result = result(client, request);
					System.out.println("Result " + result.toString());
                                        for (final Datapoint dataPoint : result.getDatapoints()) {
                                                System.out.println(id + " instance's average CPU utilization : " + dataPoint.getAverage());
                                        	if(cpuu==0){
							cpuu=dataPoint.getAverage();
							instance=id;
							break;
						} else if(cpuu>dataPoint.getAverage()){
							cpuu=dataPoint.getAverage();
                                                        instance=id;
							break;
						}
					}				
	
					int temp;
					avgcpu = 0;
					totaldynbb = 0;
					totalinst = 0;
					System.out.println("Instance id: " + id);
					for(FactorizationElement element: db_api.getAllProcessInstrumentationData(id)){
						if((long)System.currentTimeMillis()-element.getEndTime() < 20000){
				//			System.out.println("Thread: " + element.getProcessID()
				//			+ " time_on_cpu: " + element.getTimeOnCpu()
				//			+ " bb: " + element.getDynNumBB()
				//			+ " inst: " + element.getDynNumInst());
							avgcpu += element.getTimeOnCpu();
							totaldynbb += element.getDynNumBB();
							totalinst += element.getDynNumInst();
						}
					}
					if(db_api.getAllProcessInstrumentationData(id).size()>0)
						avgcpu = avgcpu/(db_api.getAllProcessInstrumentationData(id).size());
				//	System.out.println("Avgcpu: " + avgcpu
				//			+ " totalbb: " + totaldynbb 
				//			+ " totalinst: " + totalinst);
				//	System.out.println("STATUS INSTANCE: " + statusInstance(id));
					if(statusInstance(id).equals("16")){
					if(avgcpu==0){
						instance = id;
						break;
					}
					temp = totaldynbb + totalinst;
					if(lower==0){
						lower = temp;
						instance = id;
					} else if(temp < lower){
						lower = temp;
						instance = id;
					}
					}

				}

				String response=sendGet(instance, calc);
				exchange.sendResponseHeaders(200, response.length());
				OutputStream os = exchange.getResponseBody();
				os.write(response.getBytes());
				os.close();

			} catch(Exception e){
				System.out.println("[" + Thread.currentThread().getId() + "] has crashed...");
				e.printStackTrace();
			}
		}

		public ArrayList<BigInteger> getResult(){
			return answers;
		}
	}

	private static GetMetricStatisticsRequest request(final String instanceId) {
                final long twentyFourHrs = 1000 * 60 * 60 * 24;
                final int oneHour = 60 * 60;
                return new GetMetricStatisticsRequest()
            .withStartTime(new Date(new Date().getTime()- twentyFourHrs))
            .withNamespace("AWS/EC2")
            .withPeriod(oneHour)
            .withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
            .withMetricName("CPUUtilization")
            .withStatistics("Average", "Maximum")
            .withEndTime(new Date());
         }

        private static AmazonCloudWatchClient client() {
                final AmazonCloudWatchClient client =new AmazonCloudWatchClient(new ProfileCredentialsProvider().getCredentials());
		client.setEndpoint("http://monitoring.eu-west-1.amazonaws.com");
		return client;
        }

        private static GetMetricStatisticsResult result(
            final AmazonCloudWatchClient client, final GetMetricStatisticsRequest request) {
            return client.getMetricStatistics(request);
        }

	private static String sendGet(String instanceID, BigInteger i) throws Exception {

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(instanceID);
		request.setInstanceIds((Collection<String>)temp);

		DescribeInstancesResult result = ec2.describeInstances(request);
		String publicip = ((ArrayList<Instance>)((ArrayList<Reservation>)result.getReservations()).get(0).getInstances()).get(0).getPublicIpAddress();

		System.out.println("Found public ip " + publicip);


		String response = "";

		String url = "http://" + publicip + ":8000/f.html?n=" + i.toString();

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			response += inputLine + "\n" ;
		}
		in.close();

		//print result
		System.out.println(response.toString());

		return response;
	}
}
