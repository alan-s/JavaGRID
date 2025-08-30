/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.config;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javagrid.common.ExceptionDialogWithPane;
import javagrid.taskspace.TaskSpaceMainApp;

/**
 * Configuration helper class, used to load TaskSpace config file, as used in {@link javagrid.taskspace.TaskSpaceMainApp}
 */
public class TaskSpaceXML {

	private NodeList nList;
	private Node nNode;
	private Element eElement;

	public int portStart;
	public int portEnd;
	public String token;

	public TaskSpaceXML(String filePath) {
		readXML(new File(filePath));
	}

	/**
	 * XML elements schema read from config file in {@link javagrid.taskspace} package.
	 * Called from {@link javagrid.taskspace.TaskSpaceMainApp}
	 *
	 * @param inputFile the configuration file to be read
	 */
	public void readXML(File inputFile) {

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);

			doc.getDocumentElement().normalize();

			nList = doc.getElementsByTagName("configuration");
			nNode = nList.item(0);
			eElement = (Element) nNode;

			portStart = Integer.parseInt(eElement
					.getElementsByTagName("portStart").item(0)
					.getTextContent());

			portEnd = Integer.parseInt(eElement
					.getElementsByTagName("portEnd").item(0)
					.getTextContent());

			token = eElement
					.getElementsByTagName("token").item(0)
					.getTextContent();

		} catch (Exception ex) {
			ExceptionDialogWithPane edp = new ExceptionDialogWithPane("Error reading TaskSpace config file!", "TaskSpaceXML", ex);
			try {
				edp.start(TaskSpaceMainApp.getPrimaryStage());
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		}
	}

}
