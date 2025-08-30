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

import java.io.Serializable;

/**
 * This class is used to transmit a message containing the node specifications recorded in {@link javagrid.common.Node} to the
 * {@link javagrid.taskspace.TaskSpaceMainApp} server
 */
public class NodeSpecification implements Serializable {

	private static final long serialVersionUID = -2461746514272194995L;
	private String clientType;
	private String uuid;
	private String osArch;
	private String osName;
	private String osVersion;
	private String jreVendor;
	private String jreVersion;
	private String cpuModel;
	private String cpuVendor;
	private int mhz;
	private int totalCores;
	private long systemMemory;
	private long jvmMaxMemory;
	private long freeDisk;


	/**
	 * @param clientType flag indicator.  Either {Master, Worker} and used by the TaskSpace to identify source of node message
	 * @param uuid used to identify the source of this node specification message
	 * @param osArch self-explanatory
	 * @param osName self-explanatory
	 * @param osVersion self-explanatory
	 * @param jreVendor self-explanatory
	 * @param jreVersion self-explanatory
	 * @param cpuModel self-explanatory
	 * @param cpuVendor self-explanatory
	 * @param mhz self-explanatory
	 * @param totalCores self-explanatory
	 * @param systemMemory self-explanatory
	 * @param jvmMaxMemory the amount of memory available, as determined by host OS, to the JVM
	 * @param freeDisk self-explanatory
	 */
	public NodeSpecification(String clientType, String uuid, String osArch, String osName, String osVersion,
			String jreVendor, String jreVersion, String cpuModel, String cpuVendor, int mhz, int totalCores,
			long systemMemory, long jvmMaxMemory, long freeDisk) {

		this.clientType = clientType;
		this.uuid = uuid;
		this.osArch = osArch;
		this.osName = osName;
		this.osVersion = osVersion;
		this.jreVendor = jreVendor;
		this.jreVersion = jreVersion;
		this.cpuModel = cpuModel;
		this.cpuVendor = cpuVendor;
		this.mhz = mhz;
		this.totalCores = totalCores;
		this.systemMemory = systemMemory;
		this.jvmMaxMemory = jvmMaxMemory;
		this.freeDisk = freeDisk;
	}


	public String getClientType() {
		return clientType;
	}
	public String getUuid() {
		return uuid;
	}
	public String getOsArch() {
		return osArch;
	}
	public String getOsName() {
		return osName;
	}
	public String getOsVersion() {
		return osVersion;
	}
	public String getJreVendor() {
		return jreVendor;
	}
	public String getJreVersion() {
		return jreVersion;
	}
	public String getCpuModel() {
		return cpuModel;
	}
	public String getCpuVendor() {
		return cpuVendor;
	}
	public int getMhz() {
		return mhz;
	}
	public int getTotalCores() {
		return totalCores;
	}
	public long getSystemMemory() {
		return systemMemory;
	}
	public long getJvmMaxMemory() {
		return jvmMaxMemory;
	}
	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}
	public void setOsName(String osName) {
		this.osName = osName;
	}
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	public void setJreVendor(String jreVendor) {
		this.jreVendor = jreVendor;
	}
	public void setJreVersion(String jreVersion) {
		this.jreVersion = jreVersion;
	}
	public void setCpuModel(String cpuModel) {
		this.cpuModel = cpuModel;
	}
	public void setCpuVendor(String cpuVendor) {
		this.cpuVendor = cpuVendor;
	}
	public void setMhz(int mhz) {
		this.mhz = mhz;
	}
	public void setTotalCores(int totalCores) {
		this.totalCores = totalCores;
	}
	public void setSystemMemory(long systemMemory) {
		this.systemMemory = systemMemory;
	}
	public void setJvmMaxMemory(long jvmMaxMemory) {
		this.jvmMaxMemory = jvmMaxMemory;
	}
	public long getFreeDisk() {
		return freeDisk;
	}
	public void setFreeDisk(long freeDisk) {
		this.freeDisk = freeDisk;
	}

}
