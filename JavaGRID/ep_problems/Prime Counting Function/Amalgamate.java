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

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import javagrid.enumeration.ResultDestination;
import javagrid.interfaces.AmalgamateInt;
import javagrid.utils.javaGridFile;
import javagrid.utils.DiskIO;

/**
 * Amalgamation class, which you as the user must implement logic within the specific method brackets, depending on preferred execution mode.
 * DO NOT alter framework specific code which is necessary for interaction.  Please read documentation and follow instructions.
 */
public class Amalgamate implements AmalgamateInt, Serializable{

	//Serialisable version id necessary for RMI
	private static final long serialVersionUID = -2038633801052541192L;
	String workingDir;
	File workingDirFile;
	File amalgamationFile;
	File[] files;
	DiskIO dio;

	/* (non-Javadoc)
	 * @see javagrid.interfaces.AmalgamateInt#amalgamateInitialise(javagrid.enumeration.ResultDestination, java.lang.String, java.lang.String)
	 */
	public void amalgamateInitialise(ResultDestination outputType, String workingDirPath, String filePath){

		///////////////
		/// framework - DO NOT REMOVE
		workingDir = workingDirPath;
		workingDirFile = new File(workingDirPath + "/");
		amalgamationFile = new File(filePath);
		dio = new DiskIO();
		dio.newFile(amalgamationFile);

		FilenameFilter fileNameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
               if(name.lastIndexOf('.') > 0 ){
            	   return name.toLowerCase().endsWith(".jgf");
               }
               return false;
            }
         };
         files = workingDirFile.listFiles(fileNameFilter);

         if(outputType == ResultDestination.RESULT_SPACE){
        	 amalgamateDataSpace();
         }else if(outputType == ResultDestination.FILE){
        	 amalgamateFile();
         }
 		///////////////
 		/// framework - DO NOT REMOVE
	}


	//Amalgamation if using DataSpace, which is not used in this example
	/* (non-Javadoc)
	 * @see javagrid.interfaces.AmalgamateInt#amalgamateDataSpace()
	 */
	public boolean amalgamateDataSpace() {

		return true;

	}

	//Iterates through each received file from the workers, reading the computed number of primes in each task range, summing to return a total number
	/* (non-Javadoc)
	 * @see javagrid.interfaces.AmalgamateInt#amalgamateFile()
	 */
	public boolean amalgamateFile() {

		int numOfPrimes = 0;
 		///////////////
 		/// framework - DO NOT REMOVE
	    for(File result : files){
	    	String temp = "";
	    	if(result.getName() != null && result.getName().contains("."))
	    	temp = result.getName().substring(0, result.getName().lastIndexOf('.'));

	    	javaGridFile das = new javaGridFile(false, workingDir, "", temp);
	    	System.out.println("Amalgamating file: " + temp);
 		///////////////
 		/// framework - DO NOT REMOVE


	    	if(das.readValue(0) != null){
		    	numOfPrimes = numOfPrimes + Integer.parseInt(das.readValue(0));
	    	}

		///////////////
		/// framework - DO NOT REMOVE
	    	das.closeConnection();
	    }
	    dio.appendItem(String.valueOf(numOfPrimes));
	    dio.closeReadersAndWriters();
		return true;
	}

}
