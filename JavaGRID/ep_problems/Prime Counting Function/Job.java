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

    //For every integer within each task, calls isPrime function to check if the supplied number is prime.  Increments counter is prime.
	 /* (non-Javadoc)
     * @see javagrid.interfaces.JobInt#computation(java.lang.String, int, int)
     */
    public boolean computation(String fileName, int startIndex, int endIndex){

    	outputFile = new javaGridFile(true, workingDir, jobID, fileName);
    	
    	int count = 0;
		for (int i = startIndex; i <= endIndex; i++){

			if(i >= 10000000){
				WorkerMainApp.terminateJob(false);
				outputFile.saveValue(String.valueOf(count));
		    	outputFile.commit();
		    	outputFile.closeConnection();
				return true;
			}
			
			if(isPrime(i) == 1){
				count++;
			}
			continue;
		}
		outputFile.saveValue(String.valueOf(count));
    	outputFile.commit();
    	outputFile.closeConnection();
    	return true;
    }
    
    //returns 0 or 1 after determining if the passed number is prime.  Uses a trivial trial division method
    int isPrime(int number) {
        if (number <= 1) return 0; // zero and one are not prime
        int i;
        for (i=2; i*i<=number; i++) {
            if (number % i == 0) return 0;
        }
        return 1;
    }

}


