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
import java.util.Date;

/**
 * Class used to store a task's attributes from generation to completion.  Base class for {@link javagrid.common.TaskUI}
 */
public class Task implements Serializable {

	private static final long serialVersionUID = -5936810493043562710L;
	private String key;
	private int startIndex;
	private int endIndex;
	private String range;
	private String uuid;
	private int status;	// -1 = awaiting, 0 = active, 1 = failed, 2 = returned, 3 = terminated, 4 = timed out
	private boolean previouslyFailed;
	private Date startTime;
	private Date endTime;
	private double taskDuration;

	/**
	 * @param startIndex self-explanatory
	 * @param endIndex self-explanatory
	 * @param previousFailure flag to indicate task has been returned to the available task queue and thus should be processed differently
	 */
	public Task(int startIndex, int endIndex, boolean previousFailure){
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.status = -1;
		this.previouslyFailed = previousFailure;
		this.range = String.format("%010d", startIndex) + "-" +  String.format("%010d", endIndex);

	}


	public void setID(String key) {
		this.key = key;
	}
	public String getID(){
		return this.key;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	public int getStartIndex() {
		return startIndex;
	}
	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}
	public int getEndIndex() {
		return endIndex;
	}
	public void setRange(String range) {
		this.range = range;
	}
	public String getRange(){
		return this.range;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getUuid(){
		return this.uuid;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getStatus(){
		return status;
	}
	public void setPreviouslyFailed(boolean failed) {
		this.previouslyFailed = failed;
	}
	public boolean getPreviouslyFailed(){
		return previouslyFailed;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public double getTaskDuration() {
		return taskDuration;
	}
	public void setTaskDuration(double taskDuration) {
		this.taskDuration = taskDuration;
	}

}