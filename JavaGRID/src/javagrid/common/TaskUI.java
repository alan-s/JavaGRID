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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Object class for the visual representation of a {@link javagrid.common.Task} for use in the UI
 */
public class TaskUI {

	private SimpleStringProperty key;
	private SimpleStringProperty startIndex;
	private SimpleStringProperty endIndex;
	private SimpleStringProperty range;
	private StringProperty uuid;
	private SimpleStringProperty status;
	private SimpleStringProperty startTime;
	private SimpleStringProperty endTime;
	private SimpleStringProperty taskDuration;
	private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	/**
	 * @param key the task key displayed
	 * @param startIndex self-explanatory
	 * @param endIndex self-explanatory
	 */
	public TaskUI(String key, int startIndex, int endIndex){

		this.key = new SimpleStringProperty(key);
		this.startIndex = new SimpleStringProperty(String.valueOf(startIndex));
		this.endIndex = new SimpleStringProperty(String.valueOf(endIndex));
		this.range = new SimpleStringProperty(startIndex + "-" + endIndex);
		this.uuid = new SimpleStringProperty();
		this.status = new SimpleStringProperty("Active");
		this.startTime = new SimpleStringProperty();
		this.endTime = new SimpleStringProperty();
		this.taskDuration = new SimpleStringProperty();
	}


	public StringProperty keyProperty() {
		return this.key;
	}
	public void setID(String key) {
		this.key.set(key);
	}
	public String getID(){
		return this.key.get();
	}
	public StringProperty startIndexProperty() {
		return this.startIndex;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex.set(String.valueOf(startIndex));
	}
	public String getStartIndex() {
		return this.startIndex.get();
	}
	public StringProperty endIndexProperty() {
		return this.endIndex;
	}
	public void setEndIndex(int endIndex) {
		this.endIndex.set(String.valueOf(endIndex));
	}
	public String getEndIndex() {
		return endIndex.get();
	}
	public StringProperty rangeProperty() {
		return this.range;
	}
	public void setRange(String range) {
		this.range.set(String.valueOf(range));
	}
	public String getRange(){
		return this.range.get();
	}
	public StringProperty uuidProperty() {
		return this.uuid;
	}
	public void setUuid(String uuid) {
		this.uuid.set(uuid);
	}
	public String getUuid(){
		return this.uuid.get();
	}
	public StringProperty statusProperty() {
		return this.status;
	}
	public void setStatus(int status) {

		if (status == -1){
			this.status.set("Awaiting");
		}else if (status == 0){
			this.status.set("Active");
		}else if (status == 1){
			this.status.set("Failed");
		}else if (status == 2){
			this.status.set("Returned");
		}else if (status == 3){
			this.status.set("Terminated");
		}else if (status == 4){
			this.status.set("Timed Out");
		}

	}
	public String getStatus(){
		return this.status.get();
	}
	public StringProperty startTimeProperty() {
		return this.startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime.set(dateFormat.format(startTime));
	}
	public String getStartTime() {
		return startTime.get();
	}
	public StringProperty endTimeProperty() {
		return this.endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime.set(dateFormat.format(endTime));
	}
	public String getEndTime() {
		return endTime.get();
	}
	public StringProperty taskDurationProperty() {
		return this.taskDuration;
	}
	public void setTaskDuration(double taskDuration) {
		this.taskDuration.set(String.valueOf(taskDuration));
	}
	public String getTaskDuration() {
		return taskDuration.get();
	}
}
