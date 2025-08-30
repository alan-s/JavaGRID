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

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * HTTP file server run on the Master system, hosting the Job + Amalgamation package for Workers
 * Publishes on the set port in the configuration file
 */
public class HttpFileServer{

	public HttpFileServer(int serverPort, String wwwHome){

		int port = serverPort;
		String wwwhome = wwwHome;

		// open server socket
		ServerSocket socket = null;
		try {
		    socket = new ServerSocket(port);
		} catch (IOException e) {
		    System.err.println("Could not start server: " + e);
		    System.exit(-1);
		}
		System.out.println("Master codebase server accepting connections on port: " + port);

		// request handler loop
		while (true) {
		    Socket connection = null;
		    try {
			// wait for request
			connection = socket.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			OutputStream out = new BufferedOutputStream(connection.getOutputStream());
			PrintStream pout = new PrintStream(out);

			// read first line of request (ignore the rest)
			String request = in.readLine();
			if (request==null)
			    continue;
			while (true) {
			    String misc = in.readLine();
			    if (misc==null || misc.length()==0)
				break;
			}

			// parse the line
			if (!request.startsWith("GET") || request.length()<14 ||
			    !(request.endsWith("HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
			    // bad request
			    errorReport(pout, connection, "400", "Bad Request",
					"Your browser sent a request that " +
					"this server could not understand.");
			} else {
			    String req = request.substring(4, request.length()-9).trim();
			    if (req.indexOf("..")!=-1 ||
				req.indexOf("/.ht")!=-1 || req.endsWith("~")) {
				// evil hacker trying to read non-wwwhome or secret file
				errorReport(pout, connection, "403", "Forbidden",
					    "You don't have permission to access the requested URL.");
			    } else {
				String path = wwwhome + "/" + req;
				File f = new File(path);
				if (f.isDirectory() && !path.endsWith("/")) {
				    // redirect browser if referring to directory without final '/'
				    pout.print("HTTP/1.0 301 Moved Permanently\r\n" +
					       "Location: http://" +
					       connection.getLocalAddress().getHostAddress() + ":" +
					       connection.getLocalPort() + "/" + req + "/\r\n\r\n");
				} else {
				    if (f.isDirectory()) {
					// if directory, implicitly add 'index.html'
					path = path + "index.html";
					f = new File(path);
				    }
				    try {
					// send file
					InputStream file = new FileInputStream(f);
					pout.print("HTTP/1.0 200 OK\r\n" +
						   "Content-Type: " + guessContentType(path) + "\r\n" +
						   "Date: " + new Date() + "\r\n" +
						   "Server: FileServer 1.0\r\n\r\n");
					sendFile(file, out); // send raw file
				    } catch (FileNotFoundException e) {
					// file not found
					errorReport(pout, connection, "404", "Not Found",
						    "The requested URL was not found on this server.");
				    }
				}
			    }
			}
			out.flush();
		    } catch (IOException e) { System.err.println(e); }
		    try {
			if (connection != null) connection.close();
		    } catch (IOException e) { System.err.println(e); }
		}


	}

    private static void errorReport(PrintStream pout, Socket connection,
				    String code, String title, String msg)
    {
	pout.print("HTTP/1.0 " + code + " " + title + "\r\n" +
		   "\r\n" +
		   "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" +
		   "<TITLE>" + code + " " + title + "</TITLE>\r\n" +
		   "</HEAD><BODY>\r\n" +
		   "<H1>" + title + "</H1>\r\n" + msg + "<P>\r\n" +
		   "<HR><ADDRESS>FileServer 1.0 at " +
		   connection.getLocalAddress().getHostName() +
		   " Port " + connection.getLocalPort() + "</ADDRESS>\r\n" +
		   "</BODY></HTML>\r\n");
    }

    private static String guessContentType(String path)
    {
	if (path.endsWith(".html") || path.endsWith(".htm"))
	    return "text/html";
	else if (path.endsWith(".txt") || path.endsWith(".java"))
	    return "text/plain";
	else if (path.endsWith(".gif"))
	    return "image/gif";
	else if (path.endsWith(".class"))
	    return "application/octet-stream";
	else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
	    return "image/jpeg";
	else
	    return "text/plain";
    }

    private static void sendFile(InputStream file, OutputStream out)
    {
	try {
	    byte[] buffer = new byte[1000];
	    while (file.available()>0)
		out.write(buffer, 0, file.read(buffer));
	} catch (IOException e) { System.err.println(e); }
    }
}