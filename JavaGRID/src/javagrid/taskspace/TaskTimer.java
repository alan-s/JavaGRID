/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.taskspace;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An instance of this class is created to monitor each new task and count down the timer.
 */
public class TaskTimer {

    private Timer aTimer;
    private String taskID;
    private int taskTimeout;
    private int taskIndex;

    /**
     * Constructor which initialises variables and timer
     * 
     * @param taskID the task id which has been created and timer will monitor
     * @param taskIndex the index of the task, in the acquired list
     * @param taskTimeout this job's timout value, set by the user
     */
    public TaskTimer(String taskID, int taskIndex, int taskTimeout){
    	this.taskID = taskID;
    	this.taskIndex = taskIndex;
    	this.taskTimeout = taskTimeout;
    	aTimer = new Timer(true);
    }

    private TimerTask taskCountdownTimer = new TimerTask() {
        @Override public void run() {

        	//get the status of the task
        	int status = TaskSpaceMainApp.getInstance().getStatusOfTaskByIndex(taskIndex);

        	//if the status is active, it means it has not been returned and thus we can assume failed.  Return to task queue
        	if (status == 0){
        		TaskSpaceMainApp.getInstance().returnTaskToQueueByIndex(taskIndex);
        	}
        	//cancel this timer and remove itself.
        	cancelTimer();
        	TaskSpaceMainApp.getInstance().taskTimers.remove(taskID);
        }
    };

    /**
     * starts the timer to specifies interval based on job parameters
     */
    public void startTimer(){
    	aTimer.schedule(taskCountdownTimer, taskTimeout * 1000);
    }
    
    /**
     * cancels the currently running task timer
     */
    public void cancelTimer(){
    	aTimer.cancel();
    }
    
    /**
     * get the task id this timer is monitoring
     * @return the task id
     */
    public String getTaskID(){
    	return taskID;
    }
    
    /**
     * get an instance of this timer
     * 
     * @return the instance of the timer
     */
    public Timer getTimer(){
    	return aTimer;
    }

}
