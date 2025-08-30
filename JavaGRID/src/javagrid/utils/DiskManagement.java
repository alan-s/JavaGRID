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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class used to provide disk management functionality i.e. folder and file creation/deletion
 */
public class DiskManagement {

	/**
	 * checks if a directory exists already
	 *
	 * @param thePath the full path to be checked
	 * @return true if path exists, false otherwise
	 */
	public boolean checkDirectoryExists(Path thePath){

		if (Files.exists(thePath)) {
			return true;
		} else {
			return false;
			}
	}

	/**
	 * creates a specified directory
	 *
	 * @param thePath the path to directory to create
	 */
	public void createDirectory(Path thePath){

			try {
				Files.createDirectory(thePath);
			} catch (IOException e) {
				e.printStackTrace();
			}

	}

	/**
	 * deletes a specified directory
	 *
	 * @param thePath the directory to delete
	 */
	public void deleteDirectory(Path thePath){

    	File directory = new File(thePath.toUri());

    	//make sure directory exists
    	if(!directory.exists()){

        }else{

           try{
               delete(directory);
           }catch(IOException e){
               e.printStackTrace();
               System.exit(0);
           }
        }

	}

    /**
     * deletes a specified file
     *
     * @param file the file to delete
     * @throws IOException throw IO exception
     */
    public static void delete(File file) throws IOException{

        	if(file.isDirectory()){

        		//directory is empty, then delete it
        		if(file.list().length==0){
        		   file.delete();
        		}else{

        		   //list all the directory contents
            	   String files[] = file.list();

            	   for (String temp : files) {
            	      //construct the file structure
            	      File fileDelete = new File(file, temp);

            	      //recursive delete
            	     delete(fileDelete);
            	   }

            	   //check the directory again, if empty then delete it
            	   if(file.list().length==0){
               	     file.delete();
            	   }
        		}

        	}else{
        		//if file, then delete it
        		file.delete();
        	}
        }


}
