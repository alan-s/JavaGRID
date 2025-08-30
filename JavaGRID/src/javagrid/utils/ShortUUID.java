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

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Short UUID is a class to generate ~12 character unique random ids for the Master and Workers
 */
public class ShortUUID {

	/**
	 * generate a random short UUID
	 *
	 * @return the generated UUID to return
	 */
	public static String generateUUID() {
		  UUID uuid = UUID.randomUUID();
		  long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();

		  String temp = Long.toString(l, Character.MAX_RADIX);

		  if (temp.length() == 13){
			  temp = temp.substring(0, Math.min(temp.length(), 12));
			  return temp.replaceAll("(.{4})(?!$)", "$1-");
		  }else{
			  return temp.replaceAll("(.{4})(?!$)", "$1-");
		  }

		}

}
