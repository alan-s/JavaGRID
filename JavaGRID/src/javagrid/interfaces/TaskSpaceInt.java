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

import javagrid.common.DataResult;
import javagrid.common.JobParameters;
import javagrid.common.NodeSpecification;
import javagrid.common.Pulse;
import javagrid.common.Task;
import javagrid.enumeration.Notification;

/**
 * Remote interface implemented by {@link javagrid.taskspace.TaskSpaceMainApp} which Master and Workers interact with
 */
public interface TaskSpaceInt extends Remote{

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
	 * Called by Master and Workers, to register themselves with the TaskSpace
	 *
	 * @param uuid the id of the registering worker
	 * @param hostname the calling worker's hostname
	 * @param clientType the node type registering
	 * @param port the port the worker is exposing its RMI registry
	 * @throws RemoteException RMI calls must throw RemoteException RMI calls must throw RemoteException
	 */
	public void registerNode(String uuid, String hostname, String clientType, int port) throws RemoteException;

	/**
	 * Method to pass the TaskSpace the job parameter
	 *
	 * @param jobParam the job parameter class to pass. See {@link javagrid.common.JobParameters}
	 * @throws RemoteException RMI calls must throw RemoteException RMI calls must throw RemoteException
	 */
	public void jobParameters(JobParameters jobParam) throws RemoteException;

	/**
	 * Upon connection from a node, receive the necessary details about the node
	 *
	 * @param nodeSpec the node specification. See {@link javagrid.common.NodeSpecification}
	 * @throws RemoteException RMI calls must throw RemoteException RMI calls must throw RemoteException
	 */
	public void receiveNodeSpecs(NodeSpecification nodeSpec) throws RemoteException;

	/**
	 * Nodes sending pulse 'packet' to the TaskSpace
	 *
	 * @param pulseParam pulse to send.  See {@link javagrid.common.Pulse}
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void pulse(Pulse pulseParam) throws RemoteException;

	/**
	 * Notification method for the TaskSpace
	 *
	 * @param type the notification type.  See {@link javagrid.enumeration.Notification}
	 * @param value any additional flag
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void notification(Notification type, String value) throws RemoteException;

	/**
	 * Add a task to the queue
	 *
	 * @param theTask the task to add.  See {@link javagrid.common.Task}
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void addTask(Task theTask) throws RemoteException;

	/**
	 * Take the first task from the queue
	 *
	 * @param uuid the Worker requesting a task
	 * @return a task to the requesting worker
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public Task takeTask(String uuid) throws RemoteException;

	/**
	 * Add a single result item to the ResultSpace
	 *
	 * @param dataResult the result to return.  See {@link javagrid.common.DataResult}
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void addResult(DataResult dataResult) throws RemoteException;

	/**
	 * Take a result item from the ResultSpace
	 * @param key the id of the result to take
	 * @return DataResult to return.  See {@link javagrid.common.DataResult}
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public DataResult takeResult(int key) throws RemoteException;

	/**
	 * Add a single data item to the DataSpace
	 *
	 * @param dataResult the data item to add.  See {@link javagrid.common.DataResult}
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void addData(DataResult dataResult) throws RemoteException;

	/**
	 * Take a data item from DataSpace
	 *
	 * @param key the id of the data item
	 * @return the item.  See {@link javagrid.common.DataResult}
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public DataResult takeData(int key) throws RemoteException;

	/**
	 * Clear the entire DataSpace of inputs
	 *
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void clearDataSpace() throws RemoteException;

	/**
	 * Called by a Worker which has met the termination condition defined in {@link javagrid.master.job.Job}
	 *
	 * @param uuid the id of the Worker which raised the termination request
	 * @param terminateNow whether the job should complete immediately, or Workers should terminate at end of current task
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void raiseTermination(String uuid, boolean terminateNow) throws RemoteException;

	/**
	 * Notification to the TaskSpace, by the Worker, that they have completed a task
	 *
	 * @param theTask the completed task returned to the TaskSpace
	 * @param status the status of completed task
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void taskComplete(Task theTask, int status) throws RemoteException;


	/**
	 * File transfer call for the TaskSpace to transfer a file
	 * 
	 * @param fileType the file to be sent
	 * @param gzipCompressedStream if gzip compressed streams should be used
	 * @return a RemoteInputStream which is output on the receiving side
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public RemoteInputStream sendFile(String fileType, boolean gzipCompressedStream) throws RemoteException;

	/**
	 * Call to the TaskSpace to amalgamate the results once all computations are finished
	 * 
	 * @param amalgamate the Amalgamation class which contains the user defined logic.  See {@link javagrid.master.job.Amalgamate}
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void amalgamateResults(AmalgamateInt amalgamate) throws RemoteException;


}