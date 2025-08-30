/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.interfaces;

import javagrid.enumeration.ResultDestination;

/**
 * Local interface, implemented by the user in {@link javagrid.master.job} class.  Instructs the TaskSpace to amalgamate the results
 * accordingly.
 */
public interface AmalgamateInt {

	/**
	 * An initial method called prior to actual amalgamation.  Use for any setup work
	 *
	 * @param outputType signals to the method if the results are held in the DataSpace or on disk
	 * @param workingDirPath the working directory, on the TaskSpace, where files will be stored
	 * @param filePath the locaon and filename of the amalgamated file
	 */
	public void amalgamateInitialise(ResultDestination outputType, String workingDirPath, String filePath);


	/**
	 * Method called, when amalgamation is from the DataSpace
	 *
	 * @return return true if DataSpace amalgamation was successful
	 */
	public boolean amalgamateDataSpace();


	/**
	 * Method call when amalgamation is from file on disk
	 *
	 * @return return true if file amalgamation was successful
	 */
	public boolean amalgamateFile();
}
