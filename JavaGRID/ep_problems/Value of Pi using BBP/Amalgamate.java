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
import java.math.BigDecimal;
import java.math.MathContext;

import javagrid.enumeration.ResultDestination;
import javagrid.interfaces.AmalgamateInt;
import javagrid.utils.javaGridFile;
import javagrid.utils.DiskIO;


public class Amalgamate implements AmalgamateInt, Serializable{

	private static final long serialVersionUID = -2038633801052541192L;
	String workingDir;
	File workingDirFile;
	File amalgamationFile;
	File[] files;
	DiskIO dio;

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
	public boolean amalgamateDataSpace() {

		return true;

	}

	//Iterates through each received file from the workers, reading the computed value of Pi they have returned, and generates a new value accordingly
	public boolean amalgamateFile() {

 		///////////////
 		/// framework - DO NOT REMOVE
		BigDecimal pi = BigDecimal.ZERO; 		
 		MathContext mc = new MathContext(300);
	    for(File result : files){
	    	String temp = "";
	    	if(result.getName() != null && result.getName().contains("."))
	    	temp = result.getName().substring(0, result.getName().lastIndexOf('.'));

	    	javaGridFile das = new javaGridFile(false, workingDir, "", temp);
	    	System.out.println("Amalgamating file: " + temp);
 		///////////////
 		/// framework - DO NOT REMOVE


	    	if(das.readValue(0) != null){
		    	pi = pi.add(new BigDecimal(das.readValue(0)), mc);
	    	}

		///////////////
		/// framework - DO NOT REMOVE
	    	das.closeConnection();
	    }
	    dio.appendItem(String.valueOf(pi));
	    dio.closeReadersAndWriters();
		return true;
	}
}
