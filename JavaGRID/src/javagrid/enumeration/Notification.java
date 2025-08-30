/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.enumeration;

/**
 * Enumeration types for notifications send between the different nodes
 */
public enum Notification {
	NEW_TASK, TERMINATE_IMMEDIATE, TERMINATE_END, EXECUTE, PAUSE, RESUME, ABORT, SUCCESS, FAILURE, RESET, INPUT_READY, RESULT_READY, AMALGAMATE

}
