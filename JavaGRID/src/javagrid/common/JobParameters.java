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

import javagrid.enumeration.Algorithm;
import javagrid.enumeration.DataSource;
import javagrid.enumeration.Mode;
import javagrid.enumeration.ResultDestination;


/**
 * The job parameters class is used to hold the configuration of the job to be run on the grid.
 * It is set through the {@link javagrid.master.MasterMainApp}.  The parameters in this class are stored for later retrieval by
 * the {@link javagrid.config.JobXML} class.
 *
 */
public class JobParameters implements Serializable {


	private static final long serialVersionUID = 2752905700457264218L;
	private  boolean jobPaused;
	private String jobId;
	private Mode jobMode;
	private DataSource inputSource;
	private int continuation;
	private ResultDestination outputDestination;
	private int pulseInterval;
	private boolean compressFiles;
	private boolean compressSteams;
	private Algorithm algorithm;
	private int granularity;
	private int taksTimeout;


	/**
	 * @param jobPaused set if the current job is paused
	 * @param jobId the unique job identifier used throughout the system including, at times, for folder and file names
	 * @param jobMode enumerated from {@link javagrid.enumeration.Mode} and used as basis of system operation.  Consult the framework guide for more details
	 * @param inputSource enumerated from {@link javagrid.enumeration.DataSource} determining where the system will receive input from
	 * @param continuation value to offset the job from a given start index
	 * @param outputDestination enumerated from {@link javagrid.enumeration.ResultDestination} determining where outputs are saved to
	 * @param pulseInterval the integer value, in seconds, used for background agents generating pulses and detecting heartbeat of nodes.  See {@link javagrid.utils.Heartbeat}.
	 * @param compressFiles (reserved/deprecated) whether gzip compression is used for file transmission
	 * @param compressStreams sets file transfer compression, using gzip, in RMMIO file transfer library
	 * @param algorithm (reserved/deprecated) the algorithm used to alter the granularity used for task creation
	 * @param granularity the task size, obtained by adding this value to the previous tasks end index
	 * @param taskTimeout the integer value, in seconds, used to flag a worker node as failed should a result object not be returned in time
	 */
	public JobParameters(boolean jobPaused, String jobId, Mode jobMode, DataSource inputSource, int continuation,
			ResultDestination outputDestination, int pulseInterval, boolean compressFiles, boolean compressStreams, Algorithm algorithm,
			int granularity, int taskTimeout) {

		this.jobPaused = jobPaused;
		this.jobId = jobId;
		this.jobMode = jobMode;
		this.inputSource = inputSource;
		this.continuation = continuation;
		this.outputDestination = outputDestination;
		this.pulseInterval = pulseInterval;
		this.compressFiles = compressFiles;
		this.compressSteams = compressStreams;
		this.algorithm = algorithm;
		this.granularity = granularity;
		this.taksTimeout = taskTimeout;

	}


	public boolean getJobPaused() {
		return jobPaused;
	}
	public void setJobPaused(boolean jobPaused) {
		this.jobPaused = jobPaused;
	}
	public String getJobId() {
		return jobId;
	}
	public Mode getJobMode() {
		return jobMode;
	}
	public DataSource getInputSource() {
		return inputSource;
	}
	public ResultDestination getOutputDestination() {
		return outputDestination;
	}
	public Algorithm getAlgorithm() {
		return algorithm;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public void setJobMode(Mode jobMode) {
		this.jobMode = jobMode;
	}
	public void setInputSource(DataSource inputSource) {
		this.inputSource = inputSource;
	}
	public void setOutputDestination(ResultDestination outputDestination) {
		this.outputDestination = outputDestination;
	}
	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
	public int getPulseInterval() {
		return pulseInterval;
	}
	public void setPulseInterval(int pulseInterval) {
		this.pulseInterval = pulseInterval;
	}
	public boolean getCompressFiles() {
		return compressFiles;
	}
	public boolean getCompressSteams() {
		return compressSteams;
	}
	public void setCompressFiles(boolean compressFiles) {
		this.compressFiles = compressFiles;
	}
	public void setCompressSteams(boolean compressSteams) {
		this.compressSteams = compressSteams;
	}
	public int getContinuation() {
		return continuation;
	}
	public int getGranularity() {
		return granularity;
	}
	public int getTaksTimeout() {
		return taksTimeout;
	}
	public void setContinuation(int continuation) {
		this.continuation = continuation;
	}
	public void setGranularity(int granularity) {
		this.granularity = granularity;
	}
	public void setTaksTimeout(int taksTimeout) {
		this.taksTimeout = taksTimeout;
	}

}
