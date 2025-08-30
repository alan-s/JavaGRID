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

import javagrid.utils.javaGridFile;

/**
 * Local interface, implemented by the user in {@link javagrid.master.job.Job} class.  The Job class includes the
 * Embarrassingly Parallel problem logic.  Refer to to accompanying report for details.
 */
public interface JobInt {


	/**
	 * Initial method called prior to any other for job setup instructions
	 *
	 * @param workingdir the working directory, on the worker, where this job will be run
	 * @param jobId the job id set by the user
	 * @param inputFile the location of the javagrid file, if this job requires it
	 * @return return true on successful completion of initialisation method
	 */
	public boolean initialise(String workingdir, String jobId, javaGridFile inputFile);


	/**
	 * Method called prior to computation but after initialisation, for generating any runtime values
	 * @return return true on successful completion of generation method
	 */
	public boolean runtimeGeneration();

	/**
	 * The main point of entry for computation in the simple mode of operation, which is based on transformation of a single
	 * value passed to this method. i.e. f(x) {@literal ->} y.  Returns a value of type Object.
	 *
	 * @param input the input value to be computed on
	 * @return return the result object back to the worker if it should be saved.  Return null, for it to be discarded.
	 */
	public Object computation(Object input);

	/**
	 * The main point of entry for computation in the advanced mode of operation, which is based on instructions for an entire
	 * task range.  Using this method requires, that you handle reading from the input source, and the storage of the output.
	 * Use this method, over the simple mode above, to for example carry out some intermediate steps of computation not
	 * supported otherwise.
	 *
	 * @param fileName the output filename used for the current task
	 * @param startIndex the start index
	 * @param endIndex the end index
	 * @return return true on successful completion of the task, false otherwise
	 */
	public boolean computation(String fileName, int startIndex, int endIndex);

}