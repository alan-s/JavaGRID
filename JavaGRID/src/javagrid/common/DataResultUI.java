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
 * Representation of a data input or output result ({@link javagrid.common.DataResult}) for visual purposes e.g. data/result spaces
 */
public class DataResultUI {

	private StringProperty keyIndex;
	private StringProperty visualValue;

	/**
	 * @param keyIndex the key for the observable array list used to store this data result UI object
	 * @param visualValue the visual value used to display the data or result item
	 */
	public DataResultUI(int keyIndex, String visualValue){
		this.keyIndex = new SimpleStringProperty(String.valueOf(keyIndex));
		this.visualValue = new SimpleStringProperty(visualValue);
	}


	public StringProperty keyIndexProperty() {
		return this.keyIndex;
	}
	public void setKeyIndex(int index) {
		this.keyIndex.set(String.valueOf(keyIndex));
	}
	public String getKeyIndex() {
		return this.keyIndex.get();
	}
	public StringProperty visualValueProperty() {
		return this.visualValue;
	}
	public void setVisualValue(String visualValue) {
		this.visualValue.set(visualValue);
	}
	public String getVisualValue(){
		return this.visualValue.get();
	}


}
