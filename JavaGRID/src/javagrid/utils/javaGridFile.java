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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * javaGIRD file format, utilises SQLite, to provide a fast and efficient method for reading and writing to disk
 */
public class javaGridFile {

	public Path fullPath;

	private Connection conn = null;
	private PreparedStatement pstCreate;
	private PreparedStatement pstInsert;
	private PreparedStatement pstSelect;
	private PreparedStatement pstSize;
	private ResultSet rs;

	private int index = 0;
	private int count = 0;
	private String create;
	private String insert;
	private String select;
	private String size;


	/**
	 * constructor that will either create a new jgf file or set it to be read
	 * 
	 * @param newFile create a new jgf file or not?
	 * @param baseDir the base directory where the file is or will be created
	 * @param workingDir the working directory to create the file
	 * @param fileName the file name to use
	 */
	public javaGridFile(boolean newFile, String baseDir, String workingDir, String fileName){

		try{
			Class.forName("org.sqlite.JDBC");
			fullPath = Paths.get(baseDir + workingDir + "/"+ fileName + ".jgf");

			if (newFile){
				setUpNewFile();
			}else{
				setUpRead();
			}
		}catch(Exception e){
			System.out.println(e);
		}
	}

	/**
	 * request to create a new jgh file, using constructor supplied values
	 */
	private void setUpNewFile(){
		boolean dbExist = Files.exists(fullPath);

		try {
			
			// if file already exists, delete it
			if(dbExist){
				Files.delete(fullPath);
			}
			
			//call sqlite wrapper to connect, which will also create the file
			conn = DriverManager.getConnection("jdbc:sqlite:" + fullPath);
			conn.setAutoCommit(false);
			createTable();

			//sql insert statement
			insert = "INSERT INTO Inputs (aIndex, value) VALUES (?,?)";
			pstInsert = conn.prepareStatement(insert);

			//sql read state
			select = "SELECT * FROM Inputs WHERE aIndex = ?";
			pstSelect = conn.prepareStatement(select);

			//check table size
			size = "SELECT Count(*) AS size FROM Inputs";
			pstSize = conn.prepareStatement(size);

		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * method called when jgf file already exists, and you want to read from it only
	 */
	private void setUpRead(){

			try {
				//set up sqlite wrapper to connect to the file
				conn = DriverManager.getConnection("jdbc:sqlite:" + fullPath);
				conn.setAutoCommit(false);

				//sql insert statement
				insert = "INSERT INTO Inputs (aIndex, value) VALUES (?,?)";
				pstInsert = conn.prepareStatement(insert);

				//sql read state
				select = "SELECT * FROM Inputs WHERE aIndex = ?";
				pstSelect = conn.prepareStatement(select);

				//check table size
				size = "SELECT Count(*) AS size FROM Inputs";
				pstSize = conn.prepareStatement(size);

			} catch (SQLException e) {
				e.printStackTrace();
			}

	}

	/**
	 * after the creation of a new empty sqlite file, a table named "Input" needs to be created.
	 */
	private void createTable(){
		try {
			create = "CREATE  TABLE \"main\".\"Inputs\" (\"aIndex\" INTEGER PRIMARY KEY NOT NULL , \"value\" TEXT NOT NULL)";
			pstCreate = conn.prepareStatement(create);
			pstCreate.execute();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	/**
	 * Append the passed value to the end of the table
	 * 
	 * @param value the value to be saved
	 */
	public void saveValue(String value){

		try {
			pstInsert.setInt(1, index);
			pstInsert.setString(2, value);
			pstInsert.addBatch();
			index++;

			//count the number items queud for insert so far.  After 1 million, commit.  This has been verified as the optimal value
			if(++count % 1000000 == 0) {
				commit();
				}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * commit all batch statements to sqlite
	 */
	public void commit(){
		try {
			pstInsert.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * read a single value from the jgf file
	 * 
	 * @param index the index to be read
	 * @return the item read
	 */
	public String readValue(int index){

		try {
			pstSelect.setInt(1, index);
			rs = pstSelect.executeQuery();
			while (rs.next()) {
				return rs.getString("value");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * return the size of the table, of this file.  Size = number of rows
	 * 
	 * @return the size of the table
	 */
	public int size(){

		try {
			rs = pstSize.executeQuery();
			while (rs.next()) {
				return rs.getInt("size");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * cleanly close all connections, thus allowing the file to be re-used
	 */
	public void closeConnection(){

		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


}
