import java.math.BigInteger;
import java.util.ArrayList;
import java.io.IOException;

public class NewIntFactorization {

	private BigInteger zero = new BigInteger("0");
	private BigInteger one = new BigInteger("1");
	private BigInteger divisor = new BigInteger("2");
	private ArrayList factors = new ArrayList();
	private boolean DEBUG = true;


	public ArrayList calcPrimeFactors(BigInteger num) {
		return start(num);
	}

	public void end(){
		return;
	}

	public ArrayList start(BigInteger num){
		if (num.compareTo(one)==0) {
			end();
			return factors;
		}

		while(num.remainder(divisor).compareTo(zero)!=0) {
			divisor = divisor.add(one);
		}

		factors.add(divisor);
		try{
			if(DEBUG)Thread.sleep(100);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return calcPrimeFactors(num.divide(divisor));
	}
}
