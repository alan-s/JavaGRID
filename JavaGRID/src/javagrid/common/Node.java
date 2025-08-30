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
 * Superclass object representation of both {@link javagrid.master.MasterMainApp} and {@link javagrid.worker.WorkerMainApp} nodes.
 * Includes all the specific attributes of nodes necessary to be part of the grid, as well as additional device parameters such as hardware and software settings
 * Values obtained using Hyperic Sigar library
 */
public class Node {

	private StringProperty uuid;
	private StringProperty hostname;
	private StringProperty ip;
	private StringProperty port;
	private StringProperty ipPort;
	private StringProperty status;

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

	private long jvmTotalMemory;
	private long jvmUsed;
	private long jvmFree;

	private double userCPU;
	private double systemCPU;
	private double idleCPU;
	private double combinedCPU;
	private double jvmCPU;

	/**
	 * @param uuidParam the globally unique identifier for this master node
	 * @param hostnameParam the hostname of the device this instance is running on
	 * @param ipParam the IP address of the device
	 * @param portParam portParam the RMI port for which this node exposes its remote objects
	 * @param statusParam current status of this node
	 */
	public Node(String uuidParam, String hostnameParam, String ipParam, int portParam, String statusParam){

		this.uuid = new SimpleStringProperty(uuidParam);
		this.hostname = new SimpleStringProperty(hostnameParam);
		this.ip = new SimpleStringProperty(ipParam);
		this.port = new SimpleStringProperty(Integer.toString(portParam));
		this.ipPort = new SimpleStringProperty(ipParam + ":" + portParam);
		this.status = new SimpleStringProperty(statusParam);

	}


	public StringProperty uuidProperty() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid.set(uuid);
	}
	public String getUUID(){
		return this.uuid.get();
	}
	public StringProperty hostnameProperty() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname.set(hostname);
	}
	public String getHostname(){
		return this.hostname.get();
	}
	public StringProperty ipProperty() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip.set(ip);
	}
	public String getIP(){
		return this.ip.get();
	}
	public StringProperty portProperty(){
		return port;
	}
	public void setPort(int port) {
		this.port.set(String.valueOf(port));
	}
	public String getPort(){
		return this.port.get();
	}
	public StringProperty ipPortProperty() {
		return ipPort;
	}
	public void setIpPort(String ipPort) {
		this.ipPort.set(ipPort);
	}
	public String getIpPort(){
		return this.ipPort.get();
	}
	public StringProperty statusProperty(){
		return status;
	}
	public void setStatus(String status) {
		this.status.set(status);
	}
	public String getStatus(){
		return this.status.get();
	}
	public String getOsArch() {
		return osArch;
	}
	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}
	public String getOsName() {
		return osName;
	}
	public void setOsName(String osName) {
		this.osName = osName;
	}
	public String getOsVersion() {
		return osVersion;
	}
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	public String getJreVendor() {
		return jreVendor;
	}
	public void setJreVendor(String jreVendor) {
		this.jreVendor = jreVendor;
	}
	public String getJreVersion() {
		return jreVersion;
	}
	public void setJreVersion(String jreVersion) {
		this.jreVersion = jreVersion;
	}
	public String getCpuModel() {
		return cpuModel;
	}
	public void setCpuModel(String cpuModel) {
		this.cpuModel = cpuModel;
	}
	public String getCpuVendor() {
		return cpuVendor;
	}
	public void setCpuVendor(String cpuVendor) {
		this.cpuVendor = cpuVendor;
	}
	public int getMhz() {
		return mhz;
	}
	public void setMhz(int mhz) {
		this.mhz = mhz;
	}
	public int getTotalCores() {
		return totalCores;
	}
	public void setTotalCores(int totalCores) {
		this.totalCores = totalCores;
	}
	public long getSystemMemory() {
		return systemMemory;
	}
	public void setSystemMemory(long systemMemory) {
		this.systemMemory = systemMemory;
	}
	public long getJvmMaxMemory() {
		return jvmMaxMemory;
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
	public long getJvmTotalMemory() {
		return jvmTotalMemory;
	}
	public long getJvmUsed() {
		return jvmUsed;
	}
	public long getJvmFree() {
		return jvmFree;
	}
	public double getUserCPU() {
		return userCPU;
	}
	public double getSystemCPU() {
		return systemCPU;
	}
	public double getIdleCPU() {
		return idleCPU;
	}
	public double getCombinedCPU() {
		return combinedCPU;
	}
	public void setJvmTotalMemory(long jvmTotalMemory) {
		this.jvmTotalMemory = jvmTotalMemory;
	}
	public void setJvmUsed(long jvmUsed) {
		this.jvmUsed = jvmUsed;
	}
	public void setJvmFree(long jvmFree) {
		this.jvmFree = jvmFree;
	}
	public void setUserCPU(double userCPU) {
		this.userCPU = userCPU;
	}
	public void setSystemCPU(double systemCPU) {
		this.systemCPU = systemCPU;
	}
	public void setIdleCPU(double idleCPU) {
		this.idleCPU = idleCPU;
	}
	public void setCombinedCPU(double combinedCPU) {
		this.combinedCPU = combinedCPU;
	}
	public double getJvmCPU() {
		return jvmCPU;
	}
	public void setJvmCPU(double jvmCPU) {
		this.jvmCPU = jvmCPU;
	}

}
