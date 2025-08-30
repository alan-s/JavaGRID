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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Security class used to generate hash tokens used for authenticating
 */
public class Security {

	private MessageDigest md;

	/**
	 * constructor used to initialise Message Digest of type SHA-512
	 */
	public Security(){

		try {
			md = MessageDigest.getInstance("SHA-512");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * generates a SHA-512 hash of the passed string
	 *
	 * @param token the token to hash
	 * @return the hashed value to return
	 */
	public String generateSHAhash(String token){

        md.update(token.getBytes());
		byte[] mb = md.digest();
		String hashedString = "";

		for (int i = 0; i < mb.length; i++) {
		    byte temp = mb[i];
		    String s = Integer.toHexString(new Byte(temp));
		    while (s.length() < 2) {
		        s = "0" + s;
		    }
		    s = s.substring(s.length() - 2);
		    hashedString += s;
		}

		return hashedString;
	}

}
