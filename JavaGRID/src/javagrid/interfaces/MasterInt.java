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

import javagrid.enumeration.Notification;

/**
 * Remote interface implemented by {@link javagrid.master.MasterMainApp} and which interacts with TaskSpace and Workers
 */
public interface MasterInt extends Remote{

	/**
	 * Called by Workers, to register themselves with the Master and receive job + job parameters
	 *
	 * @param uuid the id of the registering worker
	 * @param hostname the calling worker's hostname
	 * @param port the port the worker is exposing its RMI registry
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void registerNode(String uuid, String hostname, int port) throws RemoteException;

	/**
	 * Authentication call against the Master
	 * @param token the provided security token
	 * @param clientType the type of authenticating node {TaskSpace, Worker}
	 * @param hostname the hostname of the connecting node
	 * @return true is authenticated, false otherwise
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public boolean authenticate(String token, String clientType, String hostname) throws RemoteException;

	/**
	 * Notify the Master method
	 *
	 * @param type the notification type.  See {@link javagrid.enumeration.Notification}
	 * @param value any additional flag
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void notification(Notification type, String value) throws RemoteException;

	/**
	 * Method to pass grid information to the Master as Workers connect
	 *
	 * @param recruited the number of Workers in the grid
	 * @param totalCPU the total CPU count in Mhz
	 * @param totalRam the total RAM available on the grid
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public void gridInfo(int recruited, int totalCPU, long totalRam) throws RemoteException;

	/**
	 * File transfer call, for the Master to transmit a file
	 *
	 * @param fileType the type of file to send back e.g. input set
	 * @param gzipCompressedStream if gzip compressed streams should be used from {javagrid.utils.rmiio}
	 * @return a RemoteInputStream which is output on the other end
	 * @throws RemoteException RMI calls must throw RemoteException
	 */
	public RemoteInputStream sendFile(String fileType, boolean gzipCompressedStream) throws RemoteException;

}
