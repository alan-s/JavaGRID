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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

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

		for (int i = 0; i < 100000; i++) {
			inputFile.saveValue(String.valueOf(generateRandomWord(3,6)));
		}
		return true;

	}


	//for every input plaintexts passed as a parameter, an MD5 hash is computed result returned
    /* (non-Javadoc)
     * @see javagrid.interfaces.JobInt#computation(java.lang.Object)
     */
	int count = 0;
    public Object computation(Object input){
    	
    	
		if(count >= 100000){
			WorkerMainApp.terminateJob(false);
			return null;
		}
		count++;
    	return computeHash((String)input);

	}

    //advanced mode of operation. Not used in this example
    /* (non-Javadoc)
     * @see javagrid.interfaces.JobInt#computation(java.lang.String, int, int)
     */
    public boolean computation(String fileName, int startIndex, int endIndex){

    	return true;
    }
    
    
    //generate a random alphanumeric word given a minimum and maximum length
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();
    public static String generateRandomWord(int minLength, int maxLength) {
        if (minLength < 1 || maxLength < minLength) {
            throw new IllegalArgumentException("Invalid length range");
        }

        int length = RANDOM.nextInt(maxLength - minLength + 1) + minLength;
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }

        return sb.toString();
    }
    
    //computes the MD5 hash of the passed plaintext
    private String computeHash(String plainText){

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Problem generating hash");
            return null;
        }
    }

}


