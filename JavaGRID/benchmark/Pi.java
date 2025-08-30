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
 * A serial implementation of the BBP formula for computing the nth digit of Pi, as a benchmark.
 */
public class Pi {

	public static void main(String[] args){

		//measure task duration
		long startTime;
		long endTime;
		double taskDuration;
		int millions = 0;

		long taskTimeStart;
		long taskTimeEnd;
		double taskTimeDuration;

		startTime =  System.nanoTime();
		taskTimeStart = System.nanoTime();

		//refer to project documentation for formula details
		BigDecimal pi = new BigDecimal(0);
		for (int i = 0; i <= 100000000; i++){

			BigDecimal a = new BigDecimal(1.0/(Math.pow(16, i)));
			BigDecimal b = new BigDecimal(4.0/((8*i) + 1));
			BigDecimal c = new BigDecimal(2.0/((8*i) + 4));
			BigDecimal d = new BigDecimal(1.0/((8*i) + 5));
			BigDecimal e = new BigDecimal(1.0/((8*i) + 6));
		    pi = pi.add(a.multiply((b.subtract(c).subtract(d).subtract(e))));

		    //for every millionth output, print task duration
			if(i % 1000000 == 0){
				millions++;
				taskTimeEnd = System.nanoTime();
				taskTimeDuration = taskTimeEnd - taskTimeStart;
				taskTimeStart = System.nanoTime();

			    BigDecimal bd = new BigDecimal(taskTimeDuration / 1000000000.0);
			    bd = bd.setScale(2, RoundingMode.HALF_UP);
				taskDuration = bd.doubleValue();

				System.out.println(millions + " took " + taskDuration + " using nanoseconds.");
			}
		}

		endTime =  System.nanoTime();
		taskDuration = (endTime - startTime);

		//rounding
	    BigDecimal bd = new BigDecimal(taskDuration / 1000000000.0);
	    bd = bd.setScale(2, RoundingMode.HALF_UP);
		taskDuration = bd.doubleValue();

		//return the computed value of pi
		System.out.println("It took: " + taskDuration + " measuring using nanoseconds.");
		System.out.println("Value of Pi: " + pi);
	}

}

