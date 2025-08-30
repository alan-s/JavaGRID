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
 * Object representation of a Master node, inheriting from {@link javagrid.common.Node} superclass
 */
public class MasterNode extends Node {

	private StringProperty jobStatus;

	/**
	 * @param uuidParam the globally unique identifier for this master node
	 * @param hostnameParam the hostname of the device this instance is running on
	 * @param ipParam the IP address of the device
	 * @param portParam the RMI port for which this node exposes its remote objects
	 * @param statusParam current status of this master node
	 * @param jobStatusParam the status of the job submitted or yet to be submitted by the master node
	 */
	public MasterNode(String uuidParam, String hostnameParam, String ipParam, int portParam, String statusParam,
			String jobStatusParam) {

		super(uuidParam, hostnameParam, ipParam, portParam, statusParam);

		this.jobStatus = new SimpleStringProperty(jobStatusParam);

	}


	public StringProperty jobStatusProperty(){
		return jobStatus;
	}
	public void setJobStatus(String submitted) {
		this.jobStatus.set(submitted);
	}
	public String getJobStatus(){
		return this.jobStatus.get();
	}

}
