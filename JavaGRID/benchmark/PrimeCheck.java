/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Serial implementation of the prime counting function, using a trivial trial by division algorithm.  Used as serial benchmark.
 */
public class PrimeCheck {

    /**
     * see project documentation
     *
     * @param number the number to be checked if prime
     * @return return 0 for not prime, 1 for prime
     */
    int isPrime(int number) {
        if (number <= 1) return 0; // zero and one are not prime
        int i;
        for (i=2; i*i<=number; i++) {
            if (number % i == 0) return 0;
        }
        return 1;
    }

	public static void main(String[] args){

		//task duration measurement
		PrimeCheck pc = new PrimeCheck();
		int count = 0;
		long startTime;
		long endTime;
		double taskDuration;
		int millions = 0;

		long taskTimeStart;
		long taskTimeEnd;
		double taskTimeDuration;

		startTime =  System.nanoTime();
		taskTimeStart = System.nanoTime();

		//iterate all numbers up to n
		for (int i = 0; i <= 100000000; i++){

			if(pc.isPrime(i) == 1){
				count++;
			}

			//every millionth number checked, show task duration
			if(i % 1000000 == 0){
				millions++;
				taskTimeEnd = System.nanoTime();
				taskTimeDuration = taskTimeEnd - taskTimeStart;
				taskTimeStart = System.nanoTime();

				//round
			    BigDecimal bd = new BigDecimal(taskTimeDuration / 1000000000.0);
			    bd = bd.setScale(2, RoundingMode.HALF_UP);
				taskDuration = bd.doubleValue();

				System.out.println(millions + " took " + taskDuration + " using nanoseconds.");
			}
		}

		endTime =  System.nanoTime();
		System.out.println("Number of primes: " + count);
		taskDuration = (endTime - startTime);

		//round
	    BigDecimal bd = new BigDecimal(taskDuration / 1000000000.0);
	    bd = bd.setScale(2, RoundingMode.HALF_UP);
		taskDuration = bd.doubleValue();

		//display task duration
		System.out.println("It took: " + taskDuration + " measuring using nanoseconds." );
	}

}

