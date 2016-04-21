package FactorizationAutoScaler;

public class AutoScaler {
	
	private static int period;
	

	public static void main(String[] args) {
		
		period = Integer.parseInt(args[0]);
		
		while(true){
			try {
				
				Thread.sleep(period);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
