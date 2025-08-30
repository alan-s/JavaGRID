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
 * Object representation of a data input or output result used throughout the system
 */
public class DataResult implements Serializable {

	private static final long serialVersionUID = 7474590408686825961L;
	private int keyIndex;
	private int uiIndex;
	private String visualValue;
	private Object theObject;

	/**
	 * Constructor
	 * @param keyIndex the key value of {K, V} pair used for the storage of data input concurrent hash map
	 * @param visualValue the value for functions where it is represented in a table view
	 * @param theObject the intrinsic object itself i.e. the object being computed
	 */
	public DataResult(int keyIndex, String visualValue, Object theObject){
		this.keyIndex = keyIndex;
		this.visualValue = visualValue;
		this.theObject = theObject;
	}


	/**
	 * sets the key value for the data or result
	 *
	 * @param index the key value of the data or result to be used
	 */
	public void setKeyIndex(int index) {
		this.keyIndex = index;
	}
	/**
	 * @return the key value for the data or result
	 */
	public int getKeyIndex() {
		return keyIndex;
	}
	/**
	 * sets the UI index of the task or result
	 *
	 * @param index the UI index to set this object to
	 */
	public void setUiIndex(int index) {
		this.uiIndex = index;
	}
	/**
	 * @return the UI index of the task or result
	 */
	public int getUiIndex() {
		return uiIndex;
	}
	/**
	 * sets the visual value, used for representing the object value on screen
	 *
	 * @param visualValue the value to use for visual representation
	 */
	public void setVisualValue(String visualValue) {
		this.visualValue = visualValue;
	}
	/**
	 * @return the visual value of the task or result
	 */
	public String getVisualValue(){
		return this.visualValue;
	}
	/**
	 * sets the object value to the instance provided.  This is the value that computations will be applied to
	 *
	 * @param theObject object input to be computed on
	 */
	public void setTheObject(Object theObject) {
		this.theObject = theObject;
	}
	/**
	 * @return the object instance of the data or result
	 */
	public Object getTheObject() {
		return theObject;
	}

}
