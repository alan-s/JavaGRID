/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.common;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Object representation of a Worker node, inheriting from {@link javagrid.common.Node} superclass
 */
public class WorkerNode extends Node {

	private StringProperty tasksExecuted;

	/**
	 * @param uuidParam the globally unique identifier for this master node
	 * @param hostnameParam the hostname of the device this instance is running on
	 * @param ipParam the IP address of the device
	 * @param portParam the RMI port for which this node exposes its remote objects
	 * @param statusParam current status of this master node
	 * @param tasksParam the number of tasks returned to the grid by this worker
	 */
	public WorkerNode(String uuidParam, String hostnameParam, String ipParam, int portParam, String statusParam, int tasksParam) {
		super(uuidParam, hostnameParam, ipParam, portParam, statusParam);
		this.tasksExecuted = new SimpleStringProperty(Integer.toString(tasksParam));

	}


	public StringProperty tasksExecutedProperty(){
		return tasksExecuted;
	}
	public void setTasksExecuted(int tasksExecuted) {
		this.tasksExecuted.set(String.valueOf(tasksExecuted));
	}
	public String getTasksExecuted(){
		return this.tasksExecuted.get();
	}

}
