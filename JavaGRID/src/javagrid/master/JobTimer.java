/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.master;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Timer used to measure the elapsed time since execution
 */
public class JobTimer { 

    private long start;
    
    /**
     * Begins the timer
     */
    public void startTimer(){
    	start = System.currentTimeMillis();
    }

    /**
     * Stops the timer, and calculates an elapsed time using System.currentTimeMillis();
     * 
     * @return the elapsed time in milliseconds
     */
    public String elapsedTime() {
        long now = System.currentTimeMillis();
        
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        String time = df.format(new Date(now - start));
        return time;
    }
      
    /**
     * Method to get the current time
     * 
     * @return the current time
     */
    public String getCurrentTime(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(cal.getTime());
    }

} 