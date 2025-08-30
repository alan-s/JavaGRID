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

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.healthmarketscience.rmiio.RemoteInputStream;

import javagrid.common.JobParameters;
import javagrid.enumeration.Notification;

/**
 * Interface implemented by {@link javagrid.worker.WorkerMainApp} that interacts with the TaskSpace and Master
 */
public interface WorkerInt extends Remote {
	
	/**
	 * Method to return the id of the Worker.  Used by the TaskSpace to detect activity
	 * 
	 * @return a string which is the id of the Worker
	 * @throws RemoteException RMI calls must throw RemoteException RMI calls must throw RemoteException
	 */
	public String getUUID() throws RemoteException;

	/**
	 * Authentication call against the TaskSpace
	 *
	 * @param token the provided security token
	 * @param clientType the type of authenticating node {TaskSpace, Worker}
	 * @param hostname the hostname of the connecting node
	 * @return true is authenticated, false otherwise
	 * @throws RemoteException RMI calls must throw RemoteException RMI calls must throw RemoteException
	 */
	public boolean authenticate(String token, String clientType, String hostname) throws RemoteException;

	/**
	 * Notification method for the Worker
	 *
	 * @param type the notification type.  See {@link javagrid.enumeration.Notification}
	 * @param value any additional flag
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void notification(Notification type, String value) throws RemoteException;

	/**
	 * Method to pass the Worker the job parameter
	 *
	 * @param jobParam the job parameter class to pass. See {@link javagrid.common.JobParameters}
	 * @throws RemoteException RMI calls must throw RemoteException RMI calls must throw RemoteException
	 */
	public void jobParameters(JobParameters jobParam) throws RemoteException;

	/**
	 * The entry point for execution.  Called by the Master after a Worker has registered
	 * 
	 * @param job the job implemented by the user and to be run by the Worker.  See {@link javagrid.master.job.Job}
	 * @throws RemoteException RMI calls must throw RemoteException RMI calls must throw RemoteException
	 */
	public void executeJob(JobInt job) throws RemoteException;

	/**
	 * File transfer call for the Worker to transfer a file
	 * 
	 * @param taskFileName the taskFileName which is to be transfered
	 * @param gzipCompressedStream if gzip compressed streams should be used
	 * @return a RemoteInputStream which is output on the receiving side
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public RemoteInputStream sendFile(String taskFileName, boolean gzipCompressedStream) throws RemoteException;
}
