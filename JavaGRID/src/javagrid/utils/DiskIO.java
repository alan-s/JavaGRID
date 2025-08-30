/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * This class is used to read and write from text files of format .txt
 */
public class DiskIO {

	//readers and writers
	private PrintWriter PW;
	private InputStream IS;
	private BufferedReader BR;


	/**
	 * Set the class for reading, of an already existing file
	 *
	 * @param inputFile the file to be set
	 */
	public void setFile(File inputFile){

			try {
				IS = new FileInputStream(inputFile);
		        BR = new BufferedReader(new InputStreamReader(IS));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

	}

	/**
	 * create a new file
	 *
	 * @param inputFile the file to be created
	 */
	public void newFile(File inputFile){
		try {
			PW = new PrintWriter(inputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	/**
	 * appends the passed value to the end of the file
	 *
	 * @param item the item to append
	 */
	public void appendItem(String item) {
		PW.println(item);
	}


	/**
	 * reads the entire contents of a text file
	 *
	 * @return the single value read from the line
	 */
	public String readAllFromDisk(){

		String line;
		try {
			line = BR.readLine();

			if (line == null) {
				closeReadersAndWriters();
			}
			return line;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 *close gracefully, the readers and writers to release any locks and flush buffers
	 */
	public void closeReadersAndWriters(){

		try {
			if (PW != null){
				PW.close();
			}
			if (IS != null){
				IS.close();
			}
			if (BR != null){
				BR.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

  	/**
  	 * Used in conjunction with RMIIO, to copy the contents of InputStream to a file
  	 *
  	 * @param in the input stream to be saved from
  	 * @param file the file to save to
  	 */
  	public static void copyInputStreamToFile(InputStream in, File file ) {
  	    try {
  	        OutputStream out = new FileOutputStream(file);
  	        byte[] buf = new byte[1024];
  	        int len;
  	        while((len=in.read(buf))>0){
  	            out.write(buf,0,len);
  	        }
  	        out.close();
  	        in.close();
  	    } catch (Exception e) {
  	        e.printStackTrace();
  	    }
  	}

}
