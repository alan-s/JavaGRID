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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javagrid.common.ExceptionDialogWithPane;
import javagrid.worker.WorkerMainApp;

/**
 * Configuration helper class, used to load Worker config file, as used in {@link javagrid.worker.WorkerMainApp}
 */
public class WorkerXML {

	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document doc;

	private NodeList nList;
	private Node nNode;
	private Element eElement;

	private File workerFile;

	public int portStart;
	public int portEnd;

	public String taskSpaceIP;
	public int taskSpacePort;

	public String masterIP;
	public int masterPort;
	public int fileServerPort;

	public String token;
	public String priority;

	public WorkerXML(String filePath) {
		workerFile = new File(filePath);
		readXML(workerFile);
	}

	/**
	 * XML elements schema read from config file in {@link javagrid.worker} package.
	 * Called from {@link javagrid.worker.WorkerMainApp}
	 *
	 * @param inputFile the configuration file to be read
	 */
	public void readXML(File inputFile) {

		try {
			dbFactory = DocumentBuilderFactory
					.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(workerFile);

			nList = doc.getElementsByTagName("workers");
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

			priority = eElement
					.getElementsByTagName("priority").item(0)
					.getTextContent();

			nList = doc.getElementsByTagName("taskSpace");
			nNode = nList.item(0);
			eElement = (Element) nNode;

			taskSpaceIP = eElement
					.getElementsByTagName("ip").item(0)
					.getTextContent();

			taskSpacePort = Integer.parseInt(eElement
					.getElementsByTagName("port").item(0)
					.getTextContent());

			nList = doc.getElementsByTagName("master");
			nNode = nList.item(0);
			eElement = (Element) nNode;

			masterIP = eElement
					.getElementsByTagName("ip").item(0)
					.getTextContent();

			masterPort = Integer.parseInt(eElement
					.getElementsByTagName("port").item(0)
					.getTextContent());

			fileServerPort = Integer.parseInt(eElement
					.getElementsByTagName("fileServerPort").item(0)
					.getTextContent());

		} catch (Exception ex) {
			ExceptionDialogWithPane edp = new ExceptionDialogWithPane("Error reading worker config file!", "WorkerXML", ex);
			try {
				edp.start(WorkerMainApp.getPrimaryStage());
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		}
	}


	/**
	 * XML elements schema written to a config file in {@link javagrid.worker.WorkerMainApp} package.
	 * Called from {@link javagrid.worker.WorkerMainApp}
	 *
	 * @param tokenParam the SHA-512 hashed token used to authenticate to other nodes
	 * @param priorityParam the OS priority to set the currently active thread when computing.  {MIN, MAX, NORMAL}
	 */
	public void writeXML(String tokenParam, String priorityParam) {

		try {
			dbFactory = DocumentBuilderFactory
					.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("configuration");
			doc.appendChild(rootElement);

			// comment
			Comment eComment = doc
					.createComment("javaGRID worker node file");
			doc.insertBefore(eComment, rootElement);

			// worker
			Element eworkers = doc.createElement("workers");
			rootElement.appendChild(eworkers);

			Element eportstart = doc.createElement("portStart");
			eportstart.appendChild(doc.createTextNode(String.valueOf(portStart)));
			eworkers.appendChild(eportstart);

			Element eportend = doc.createElement("portEnd");
			eportend.appendChild(doc.createTextNode(String.valueOf(portEnd)));
			eworkers.appendChild(eportend);

			Element etoken = doc.createElement("token");
			etoken.appendChild(doc.createTextNode(tokenParam));
			eworkers.appendChild(etoken);

			Element epriority = doc.createElement("priority");
			epriority.appendChild(doc.createTextNode(priorityParam));
			eworkers.appendChild(epriority);

			//taskspace
			Element etaskspace = doc.createElement("taskSpace");
			rootElement.appendChild(etaskspace);

			Element etsip = doc.createElement("ip");
			etsip.appendChild(doc.createTextNode(taskSpaceIP));
			etaskspace.appendChild(etsip);

			Element etsport = doc.createElement("port");
			etsport.appendChild(doc.createTextNode(String.valueOf(taskSpacePort)));
			etaskspace.appendChild(etsport);

			//master
			Element emaster = doc.createElement("master");
			rootElement.appendChild(emaster);

			Element emip = doc.createElement("ip");
			emip.appendChild(doc.createTextNode(masterIP));
			emaster.appendChild(emip);

			Element emport = doc.createElement("port");
			emport.appendChild(doc.createTextNode(String.valueOf(masterPort)));
			emaster.appendChild(emport);

			Element emfileport = doc.createElement("fileServerPort");
			emfileport.appendChild(doc.createTextNode(String.valueOf(fileServerPort)));
			emaster.appendChild(emfileport);


			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(workerFile);
			transformer.transform(source, result);


		} catch (ParserConfigurationException | TransformerException ex) {
			ExceptionDialogWithPane edp = new ExceptionDialogWithPane("Error writing worker config file!", "WorkerXML", ex);
			try {
				edp.start(WorkerMainApp.getPrimaryStage());
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		}
	}


}
