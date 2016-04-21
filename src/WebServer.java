import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), 8000), 0);
        System.out.println(InetAddress.getLocalHost().getHostName());
        server.createContext("/f", new MyHandler());
        server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        	String response = "This was the query:" + t.getRequestURI().getQuery() 
                               + "##";
        	
        	
        	BigInteger query = new BigInteger(t.getRequestURI().getQuery().split("=")[1]);
        	System.out.println("Recv query " + query);
        	(new Thread(new factorialThread(query, t))).start();
        	
        	
            /*t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            */
        }
    }
    
    static class factorialThread implements Runnable {
    	private BigInteger calc;
    	private ArrayList<BigInteger> answers;
    	private HttpExchange exchange;
    	
    	public factorialThread(BigInteger i, HttpExchange t){
    		calc = i;
    		exchange = t;
    	}
    	
		@Override
		public void run() {
			NewIntFactorization ifact = new NewIntFactorization();
			ArrayList<BigInteger> answer = ifact.calcPrimeFactors(calc);
			answers = answer;
			
			String response = "";
			for(BigInteger bi : answers){
				response += bi.toString() + "\n";
			}
			
			try{
				float i = 0;
				exchange.sendResponseHeaders(200, response.length());
				OutputStream os = exchange.getResponseBody();
	            os.write(response.getBytes());
	            os.close();
	            
			} catch(IOException e){
				System.out.println("[" + Thread.currentThread().getId() + "] has crashed...");
				e.printStackTrace();
			}
		}
    	
		public ArrayList<BigInteger> getResult(){
			return answers;
		}
    }
    
    

}