/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.master.job;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;

import javagrid.interfaces.JobInt;
import javagrid.utils.javaGridFile;
import javagrid.worker.WorkerMainApp;

/**
 * This class is where your user defined logic is inserted.  Depending on the mode of execution, insert either function or task based into the relevant methods.
 */
public class Job implements JobInt, Serializable{

	//Serialisable version id necessary for RMI
	private static final long serialVersionUID = -4095264426509906877L;
	String workingDir;
	String jobID;
	javaGridFile inputFile;
	javaGridFile outputFile;

		/* (non-Javadoc)
	 * @see javagrid.interfaces.JobInt#initialise(java.lang.String, java.lang.String, javagrid.utils.DiskArraySQL)
	 */
	public boolean initialise(String workingdir, String jobid, javaGridFile inputFile){

		///////////////
		/// framework - DO NOT REMOVE
		this.workingDir = workingdir;
		this.jobID = jobid;
		this.inputFile = inputFile;
		/// framework - DO NOT REMOVE

		return true;
	}


	//generates all input values but left clear as we generate on the fly in this example
	/* (non-Javadoc)
	 * @see javagrid.interfaces.JobInt#runtimeGeneration()
	 */
	public boolean runtimeGeneration(){

		return true;

	}


	//simple mode of operation. Not used in this example
	 /* (non-Javadoc)
     * @see javagrid.interfaces.JobInt#computation(java.lang.Object)
     */
    public Object computation(Object input){
    	
    	return null;

	}

    //For every task, iterate the BBP series to get the value of pi, save final summation to a file and send back to TaskSpace
	/* (non-Javadoc)
     * @see javagrid.interfaces.JobInt#computation(java.lang.String, int, int)
     */
    public boolean computation(String fileName, int startIndex, int endIndex){

    	outputFile = new javaGridFile(true, workingDir, jobID, fileName);

    	BigDecimal pi = BigDecimal.ZERO;
		MathContext mc = new MathContext(300); // set precision
		BigDecimal sixteen = new BigDecimal(16);
		BigDecimal eight = new BigDecimal(8);
		BigDecimal one = BigDecimal.ONE;

		for (int i = startIndex; i <= endIndex; i++){
			
			if(i >= 1000000){
				WorkerMainApp.terminateJob(false);
				System.out.println("terminate");
				outputFile.saveValue(String.valueOf(pi));
		    	outputFile.commit();
		    	outputFile.closeConnection();
				return true;
			}

			BigDecimal a = one.divide(sixteen.pow(i, mc), mc);
			BigDecimal b = new BigDecimal(4).divide(eight.multiply(new BigDecimal(i)).add(new BigDecimal(1)), mc);
			BigDecimal c = new BigDecimal(2).divide(eight.multiply(new BigDecimal(i)).add(new BigDecimal(4)), mc);
			BigDecimal d = one.divide(eight.multiply(new BigDecimal(i)).add(new BigDecimal(5)), mc);
			BigDecimal e = one.divide(eight.multiply(new BigDecimal(i)).add(new BigDecimal(6)), mc);

			BigDecimal term = a.multiply(b.subtract(c).subtract(d).subtract(e), mc);
			pi = pi.add(term, mc);

		}

		outputFile.saveValue(String.valueOf(pi));
    	outputFile.commit();
    	outputFile.closeConnection();
    	return true;
    }

}


