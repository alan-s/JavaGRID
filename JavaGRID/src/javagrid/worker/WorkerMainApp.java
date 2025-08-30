/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.worker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.SigarException;
import com.healthmarketscience.rmiio.GZIPRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javagrid.common.DataResult;
import javagrid.common.JobParameters;
import javagrid.common.NodeSpecification;
import javagrid.common.Pulse;
import javagrid.common.Task;
import javagrid.config.WorkerXML;
import javagrid.controller.WorkerMainController;
import javagrid.enumeration.DataSource;
import javagrid.enumeration.Mode;
import javagrid.enumeration.Notification;
import javagrid.enumeration.ResultDestination;
import javagrid.interfaces.JobInt;
import javagrid.interfaces.MasterInt;
import javagrid.interfaces.TaskSpaceInt;
import javagrid.interfaces.WorkerInt;
import javagrid.utils.DiskIO;
import javagrid.utils.DiskManagement;
import javagrid.utils.Heartbeat;
import javagrid.utils.Networking;
import javagrid.utils.Security;
import javagrid.utils.ShortUUID;
import javagrid.utils.javaGridFile;

/**
 * The Worker is the lead actor in our theatre.  It requests tasks and data from the TaskSpace, computes them according to
 * job from the Master and then returns the results.
 */
public class WorkerMainApp extends Application implements WorkerInt {

	//javaFX
	private static Stage primaryStage;
    private BorderPane rootLayout;
    private FXMLLoader loader = new FXMLLoader();

	private static WorkerMainApp instance;
    private static WorkerMainController workerMainController;

    //generate unique id
	final static String uuid = "W-" + ShortUUID.generateUUID();
	private WorkerXML xmlReader;

	//RMI
	private int rmiPort=52000;
	private URI uri;

	private String taskSpaceIP = "127.0.0.1";
	private int taskSpacePort = 50000;
	private static TaskSpaceInt taskSpaceCommon;

	private MasterInt masterCommon;
	private String masterIP = "127.0.0.1";
	private int masterPort = 51000;
	private int fileServerPort = 51010;

	private String authToken;
	public boolean tokenChanged;
	private String priority;
	private int priorityInt;

	private ExecutorService opsExecutor;

	//task parameters
	private JobInt receivedJob;
	private Task currentTask;
	private String fileName;
	private int startIndex;
	private int endIndex;

	//termination condition
	private boolean taskAvailble = false;
	private static boolean terminate = false;
	private static boolean terminateImmediate = false;
	private static boolean terminationRaised = false;
	private static boolean terminatingWorker;
	private boolean paused;

	//job parameters
	private String jobID;
	private Mode jobMode;
	private DataSource inputSource;
	private ResultDestination outputDestination;
	private int pulseInterval = 5;
	private ScheduledExecutorService pulseTimer;
	private boolean compressStreams;

	private Networking net;
	private Heartbeat pulse;
	private DiskManagement diskM;
	private javaGridFile inputFile;
	private javaGridFile resultFile;
	private Security sec;

	//task duration
	private long startTime;
	private long endTime;
	private double taskDuration;
	private int tasksGrabbed = 0;
	private int tasksReturned = 0;

	private String baseDirectory = getClass().getResource("/javagrid/worker/jobs/").toString().replace("file:/", "");


    /**
     * constructor which initialises heartbeat, disk, networking and security classes.
     * also starts pulsing timer to send information to the TaskSpace
     *
     * @throws SigarException throw sigar exception when unable to read host information
     */
    public WorkerMainApp() throws SigarException {

    	net = new Networking();
    	diskM = new DiskManagement();
     	pulse = new Heartbeat();
     	sec = new Security();

		pulseTimer = Executors.newSingleThreadScheduledExecutor();
	    pulseTimer.scheduleAtFixedRate(sendPulse, 3, pulseInterval, TimeUnit.SECONDS);

	    opsExecutor = Executors.newFixedThreadPool(2);

    }


	/**
	 * get instance of this Worker app
	 *
	 * @return this instance of Worker app
	 */
	public static WorkerMainApp getInstance() {
		return instance;
	}

	/**
	 * get instance of app's controller
	 *
	 * @return this app's controller
	 */
	public static WorkerMainController getController(){
		return workerMainController;
	}

	/**
	 * return the primary stage
	 *
	 * @return get the primary stage of this app
	 */
	public static Stage getPrimaryStage() {
		return primaryStage;
	}

    /* (non-Javadoc)
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) throws InterruptedException {

    try{
    	//javaFX
	    WorkerMainApp.primaryStage = primaryStage;
	    WorkerMainApp.primaryStage.setTitle("javaGRID Worker Node");
	    WorkerMainApp.primaryStage.setResizable(false);
	    primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/javagrid/resources/javaGRIDsmall.png")));

	    initRootLayout();
	    showWorkerMain();

	    //load configuration
		loadWorkerConfig();
	    setLabels();

	    //start exposing objects and connect to TaskSpace and Master
	    startWorkerRegister();
	    connection();
    }catch (Exception e){
    	System.out.println(e);
    }


    	}

	/* (non-Javadoc)
	 * @see javafx.application.Application#stop()
	 */
	public void stop() throws Exception {
		super.stop();
		Platform.exit();
		System.exit(0);
	}


	/**
	 *load the FXML file which will stores the UI components
	 */
	private void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/javagrid/view/WorkerRootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * launch the main app for Worker
	 */
	private void showWorkerMain() {
        try {
            // Load server main screen.
            loader.setLocation(WorkerMainApp.class.getResource("/javagrid/view/WorkerMain.fxml"));
            AnchorPane workerMain = (AnchorPane) loader.load();

            // Set main screen into the centre of root layout.
            rootLayout.setCenter(workerMain);

            // Give the controller access to the main app.
            workerMainController = loader.getController();
            workerMainController.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * used for Eclipse to launch application
	 *
	 * @param args passed command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * set the labels in the UI
	 */
	private void setLabels(){

    	try {
    		workerMainController.setLblHostname(InetAddress.getLocalHost().getHostName());
    		workerMainController.setLblIP(InetAddress.getLocalHost().getHostAddress() + ":" + rmiPort);
    		workerMainController.setLblUUID(uuid);
    		workerMainController.setLblTaskSpaceIP(taskSpaceIP + ":" + taskSpacePort);
    		workerMainController.setLblMasterIP(masterIP + ":" + masterPort);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}


	}


	/* (non-Javadoc)
	 * @see javagrid.interfaces.WorkerInt#authenticate(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean authenticate(String token, String clientType, String hostname) throws RemoteException{

		if (!token.equals(authToken)) {
			System.out.println("A " + clientType + " node connected from host: " + hostname + " but failed authentication.");
			return false;
		}
		return true;
	}

    /* (non-Javadoc)
     * @see javagrid.interfaces.WorkerInt#executeJob(javagrid.interfaces.JobInt)
     */
    @SuppressWarnings("unchecked")
	public void executeJob(JobInt job) throws RemoteException{

    	//receive the job from the Master, start new FixedThreadPool to run the actual computation
    	receivedJob = job;
		opsExecutor.submit(workerExecutionTask);

    }

    @SuppressWarnings("rawtypes")
	private Callable workerExecutionTask = new Callable() {

		@Override
		public Object call() throws Exception {

			//firstly run initialisation to create working directory, get/generate input files
			jobInitialisation();


				//user an infinite loop to keep the thread running
    	    	while(true){

    	    		try {
    	    			//sleep for 1 millisecond to process notifications
    					Thread.sleep(1);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}

    	    		//condition check to know that a task is ready and available to be computed.  Call job logic
    	    		if(taskAvailble ==  true && terminate == false && paused == false){

    	    			taskAvailble = false;
    	    			if (getNextTask() == false){
    	    				continue;
    	    			}

    	    			//update UI
	    	  		      Platform.runLater(new Runnable() {
	    			          @Override
	    			          public void run() {
	    			        	  workerMainController.setLblCurrent("Computing the task with range: " + formatNumber(startIndex) + " - " + formatNumber(endIndex));
	    			        	  workerMainController.setLblGrabbed(String.valueOf(tasksGrabbed));
	    			          }
	    			      });

	    	  		      //update thread priority based on UI level selected
        	        	if (priorityInt == 0){
        	            	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        	        	}else if(priorityInt == 1){
        	            	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	        	}else if(priorityInt == 2){
        	            	Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        	        	}

    	    			startTime = System.nanoTime();

    	    			//simple, function based mode
    	    			if (jobMode == Mode.SIMPLE){

    			    		for (int i = startIndex; i <= endIndex; i++){

    			    			String diskInput = "";
    			    			DataResult dataSpaceInput;
    			    			Object result = null;

    			    			//get the input value,pass to job computation method and take the result
    			    	    	if (inputSource == DataSource.RUNTIME || inputSource == DataSource.FILE){
    			    	    		diskInput = inputFile.readValue(i);
    			    	    		result = receivedJob.computation(diskInput);
    			    	    	} else if (inputSource == DataSource.DATA_SPACE){
    			    	    		dataSpaceInput = taskSpaceCommon.takeData(i);
    			    	    		if (dataSpaceInput == null){
    			    	    			result = receivedJob.computation(null);
    			    	    		}else{
    			    	    			result = receivedJob.computation(dataSpaceInput.getTheObject());
    			    	    		}
    			    	    	}

    			    	    	//save/send result to correct destination
    			    	    	if (outputDestination == ResultDestination.RESULT_SPACE && result != null){
    			    	    		//check object type here
    			    	    			taskSpaceCommon.addResult(new DataResult(i, (String) result, result));

    			    	    	} else if (outputDestination == ResultDestination.FILE){

    			    	    		if ( i == startIndex){
    			    	    			resultFile = new javaGridFile(true, baseDirectory, jobID, fileName);

    			    	    		}

    			    	    		if (result != null){
    			    	    			resultFile.saveValue((String)result);
    			    	    		}

    			    	    	}

    			    	    	//if last input caused termination condition, check if we should terminate immediately
    			    			if (terminateImmediate == true || terminatingWorker == true){
    			    				workerMainController.setBtnUpdateVisible(true);
    			    	    		workerMainController.setPiJob(false);
    			    				break;
    			    			}

    			    		}

    			    		//if outputting to a file, commit all values left in the buffer, and close sql connection
    			    		if (outputDestination == ResultDestination.FILE){
    				    		resultFile.commit();
    				    		resultFile.closeConnection();
    				    		resultFileReady(currentTask.getID());
    			    		}

    			    	//task based advanced mode.  Complete responsibility passed to user logic in job class
    	    		} else if(jobMode == Mode.ADVANCED){

    	    			if(receivedJob.computation(fileName, startIndex, endIndex)){

    	    				//if file used, we return the file to the TaskSpace
    	    				resultFileReady(currentTask.getID());
    	    			}
    	    		}//task is complete, lets wrap up
    	    			completedTask();

    	    		}
    	    	}
		}

      };


    /**
     * Notify the TaskSpace that a result file is ready for collection
     *
     * @param fileName the name of the file to be collected
     */
    public static void resultFileReady(String fileName){
			try {
				taskSpaceCommon.notification(Notification.RESULT_READY, fileName);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
      }

    /**
     * initialisation method, called at the start of execution to create a working directory if not already one available
     * and then generate/download input file
     */
    private void jobInitialisation(){

			if (inputSource == DataSource.RUNTIME){

				//if one Worker or more is already running on this host, do not create directory
        		if(instanceRunning() == false){
  	  		      Platform.runLater(new Runnable() {
			          @Override
			          public void run() {
			        	  workerMainController.setLblCurrent("Generating runtime input file...");
			          }
			      });
            		inputFile = new javaGridFile(true, baseDirectory, "", jobID);
            		receivedJob.initialise(baseDirectory, jobID, inputFile);
            		receivedJob.runtimeGeneration();
            		inputFile.commit();
            		inputFile = new javaGridFile(false, baseDirectory, "", jobID);
    	  		      Platform.runLater(new Runnable() {
    			          @Override
    			          public void run() {
    			        	  workerMainController.setLblCurrent("File generation complete.");
    			          }
    			      });
        		}else{
        			inputFile = new javaGridFile(false, baseDirectory, "", jobID);
        			receivedJob.initialise(baseDirectory, jobID, inputFile);
        		}

        	} else if (inputSource == DataSource.DATA_SPACE){

        	} else if (inputSource == DataSource.FILE){

        		//if one Worker or more is already running on this host, do not download input set again
        		if(instanceRunning() == false){
    	  		      Platform.runLater(new Runnable() {
    			          @Override
    			          public void run() {
    			        	  workerMainController.setLblCurrent("Retrieving input file from TaskSpace...");
    			          }
    			      });

    	  		      //download input file
        			getFileFromTaskSpace(baseDirectory + jobID + ".jgf", compressStreams);

        			//update UI
  	  		      Platform.runLater(new Runnable() {
			          @Override
			          public void run() {
			        	  workerMainController.setLblCurrent("Input file transfer complete.");
			          }
			      });
        		}

        		inputFile = new javaGridFile(false, baseDirectory, "", jobID);
        		receivedJob.initialise(baseDirectory, jobID, inputFile);
        	}


        	if (outputDestination == ResultDestination.RESULT_SPACE){

        	} else if (outputDestination == ResultDestination.FILE){
        		//create a new working directory using job id
            		if(instanceRunning() == false){
            			Path workingDir = Paths.get(baseDirectory + jobID);
        				diskM.deleteDirectory(workingDir);
        				diskM.createDirectory(workingDir);
            		}
        	}
      }

      /**
       * task completion subroutine.  Work out a duration elapsed and request TaskSpace to collect result file, if in file mode
     * @throws RemoteException
     */
    private void completedTask() throws RemoteException{

  		endTime = System.nanoTime();
  		taskDuration = (endTime - startTime);
  		taskDuration = round(taskDuration / 1000000000.0, 2);
  		currentTask.setTaskDuration(taskDuration);
  		tasksReturned++;
  		if (terminateImmediate == true){
  			taskSpaceCommon.taskComplete(currentTask, 3);
  		}else{
  			taskSpaceCommon.taskComplete(currentTask, 2);
  		}
  			//update UI
		      Platform.runLater(new Runnable() {
		          @Override
		          public void run() {
		        	workerMainController.setLblReturned(String.valueOf(tasksReturned));
		          }
		      });
      }


    /* (non-Javadoc)
     * @see javagrid.interfaces.WorkerInt#jobParameters(javagrid.common.JobParameters)
     */
    public void jobParameters(JobParameters jobParam) throws RemoteException{

    	//update class variables with job parameters received
    	paused = jobParam.getJobPaused();
    	jobID = jobParam.getJobId().replace(" ", "");
    	jobMode = jobParam.getJobMode();
    	inputSource = jobParam.getInputSource();
    	outputDestination = jobParam.getOutputDestination();
    	pulseInterval = jobParam.getPulseInterval();
    	compressStreams = jobParam.getCompressSteams();
		workerMainController.setLblJobID(jobID);

		//cancel current pulse and executeJob methods and restart with new intervals
		pulseTimer.shutdownNow();
		pulseTimer = Executors.newSingleThreadScheduledExecutor();
	    pulseTimer.scheduleWithFixedDelay(sendPulse, 3, pulseInterval, TimeUnit.SECONDS);

    }

    /**
     * connect to TaskSpace and Master
     * @throws InterruptedException in case of thread interruption
     */
    public void connection() throws InterruptedException {
    	connectToTaskSpace();
    	connecToMaster();
    }

    /**
     * Use a semaphore (ports) to check if there is any currently running instances of Worker
     *
     * @return true if at least on instance running, false otherwise
     */
    private boolean instanceRunning(){

    	boolean instanceRunning = false;
    	try {
    		//get the port range from the configuration file
			uri = getClass().getResource("/javagrid/worker/worker.config").toURI();
	    	xmlReader = new WorkerXML(uri.getPath());

	    	//check each in turn to see if open and available
	    	for(int i = xmlReader.portStart; i <= xmlReader.portEnd; i++){

	    		if (!net.portAvailable(i) && i != rmiPort){
	    			instanceRunning = true;
	    			break;
	    		}
	    	}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

    	return instanceRunning;
    }

    /**
     * launches the RMI registry to expose this Workers remote methods
     *
     * @return true if successful, false otherwise
     */
    private boolean startWorkerRegister(){

    	//expose worker objects
        try {

        	//load worker.policy file into security manager
        	uri = getClass().getResource("/javagrid/worker/worker.policy").toURI();
        	System.setProperty("java.security.policy", uri.getPath());

			/*
			 * if (System.getSecurityManager() == null) { System.setSecurityManager(new
			 * SecurityManager()); }
			 */

            //load configuration file to get RMI details
        	uri = getClass().getResource("/javagrid/worker/worker.config").toURI();
        	xmlReader = new WorkerXML(uri.getPath());
	    	taskSpaceIP= xmlReader.taskSpaceIP;
	    	taskSpacePort = xmlReader.taskSpacePort;

	    	//find an available port launch on
        	for(int i = xmlReader.portStart; i <= xmlReader.portEnd; i++){
        		if (net.portAvailable(i)){
        			rmiPort = i;
        			break;
        		}
        	}
			Registry workerRegistry = LocateRegistry.createRegistry(rmiPort);
            WorkerInt stub = (WorkerInt) UnicastRemoteObject.exportObject(this, rmiPort);
			workerRegistry.rebind("WorkerRMI", stub);
            System.out.println("Worker: " + uuid + " ready on port: " + rmiPort );
            return true;
        } catch (Exception e) {
            System.err.println("Worker binding exception:" + "\\n");
            e.printStackTrace();
            return false;
        }

    }

    /**
     * connect to the Master and register
     *
     * @return true if successful, false otherwise
     */
    private boolean connecToMaster(){

        //connect to master
        try {
        	//load configuration file to get ip and port of the Master.  Also get the port the WebServer is running on
        	uri = getClass().getResource("/javagrid/worker/worker.config").toURI();
        	xmlReader = new WorkerXML(uri.getPath());
        	masterIP = xmlReader.masterIP;
        	masterPort = xmlReader.masterPort;
        	fileServerPort = xmlReader.fileServerPort;

        	Registry masterRegistry = LocateRegistry.getRegistry(masterIP, masterPort);
        	masterCommon = (MasterInt) masterRegistry.lookup("MasterRMI");

        	//authenticate to the Master server
	    	if (!masterCommon.authenticate(authToken, "Worker", InetAddress.getLocalHost().getHostName())){
	    		System.out.println("Authentication error - The supplied authentication token was rejected by the Master");
	    		return false;
	    	}
	    	//if successful, register
	    	workerMainController.setLblStatusMaster("Connected");
        	masterCommon.registerNode(uuid, InetAddress.getLocalHost().getHostName(), rmiPort);
        	System.out.println("Registered with Master");
        	return true;
        } catch (Exception e) {
            System.err.println("Exception connecting to the Master:");
            e.printStackTrace();
            return false;
        }
    }


    /**
     * terminate job method, called by the job class depending on the termination condition programmed.
     * raises the termination call with the TaskSpace
     *
     * @param terminateInstant if termination request is immediate or delayed
     */
    public static void terminateJob(boolean terminateInstant){

    	try {
    		if (terminationRaised == true){

    		}else{
    			terminationRaised = true;
    			terminateImmediate = terminateInstant;
    			terminatingWorker = true;
        		taskSpaceCommon.raiseTermination(uuid, terminateInstant);
    		}

		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }

    String message = "";
	/* (non-Javadoc)
	 * @see javagrid.interfaces.WorkerInt#notification(javagrid.enumeration.Notification, java.lang.String)
	 */
	public void notification(Notification type, String value) throws RemoteException {

		//notfication types
		if (type == Notification.TERMINATE_IMMEDIATE){
			terminate = true;
			terminateImmediate = true;
			workerMainController.setBtnUpdateVisible(true);
    		workerMainController.setPiJob(false);
    		message = "Termination condition met, processing complete!";
		}else if (type == Notification.TERMINATE_END){
			terminate = true;
			terminateImmediate = false;
			workerMainController.setBtnUpdateVisible(true);
    		workerMainController.setPiJob(false);
			message = "Termination condition met, processing complete!";
		}else if(type == Notification.PAUSE){
			paused = true;
			workerMainController.setBtnUpdateVisible(false);
			message = "Job paused...";
    		workerMainController.setPiJob(true);
		}else if(type == Notification.RESUME){
			paused = false;
			workerMainController.setBtnUpdateVisible(false);
    		workerMainController.setPiJob(true);
    		message = "Job resume...";
		}else if(type == Notification.ABORT){
			terminate = true;
			terminateImmediate = true;
			workerMainController.setBtnUpdateVisible(true);
    		workerMainController.setPiJob(false);
    		message = "Job aborted!";
		}else if (type == Notification.RESET){
			taskAvailble = false;
			terminationRaised = false;
			terminate = false;
			terminateImmediate = false;
			paused = false;
			terminatingWorker = false;
			workerMainController.setBtnUpdateVisible(true);
    		workerMainController.setPiJob(false);
    		tasksGrabbed = 0;
    		tasksReturned = 0;

		}

		if (type == Notification.NEW_TASK){
			taskAvailble = true;
    		workerMainController.setBtnUpdateVisible(false);
    		workerMainController.setPiJob(true);
    	}else{
  	      Platform.runLater(new Runnable() {
	          @Override
	          public void run() {
	        	  workerMainController.setLblCurrent(message);
	          }
	      });
    	}

	}

    	/**
    	 * Connects to the TaskSpace
    	 *
    	 * @return if successful return true otherwise false
    	 */
    	private boolean connectToTaskSpace(){

    	    try {
    	    	//load the configuration file to get the TaskSpace ip and port
            	URI uri = getClass().getResource("/javagrid/worker/worker.config").toURI();
            	xmlReader = new WorkerXML(uri.getPath());
    	    	taskSpaceIP= xmlReader.taskSpaceIP;
    	    	taskSpacePort = xmlReader.taskSpacePort;
    	    	Registry taskSpaceregistry = LocateRegistry.getRegistry(taskSpaceIP, taskSpacePort);
    	    	taskSpaceCommon = (TaskSpaceInt) taskSpaceregistry.lookup("TaskSpaceRMI");

    	    	//authenticate with the TaskSpace
    	    	if (!taskSpaceCommon.authenticate(authToken, "Worker", InetAddress.getLocalHost().getHostName())){
    	    		workerMainController.authError("TaskSpace");
    	    		System.out.println("Authentication error - The supplied authentication token was rejected by the TaskSpace");
    	    		return false;
    	    	}
    	    	//if successful, register and send node details
    	    	workerMainController.setLblStatus("Connected");
    	    	taskSpaceCommon.registerNode(uuid, InetAddress.getLocalHost().getHostName(), "Worker", rmiPort);
    	    	taskSpaceCommon.receiveNodeSpecs(new NodeSpecification("Worker", uuid, pulse.getOsArch(), pulse.getOsName(), pulse.getOsVersion(), pulse.getJreVendor(),
    	    			pulse.getJreVersion(), pulse.getCpuModel(), pulse.getCpuVendor(), pulse.getMhz(), pulse.getTotalCores(),
    	    			pulse.getSystemMemory(), pulse.getJvmMaxMemory(), pulse.getFreeDisk()));
    	    	System.out.println("Registered with TaskSpace");
    	    	return true;
    	    } catch (Exception e) {
    	        System.err.println("Exception connecting to taskspace:");
    	        e.printStackTrace();
    	        return false;
    	    }
    	}


        /**
         * pulse method inside new Runnable class, called by pulse timer, which collects host details and transmits to the
         * TaskSpace
         */
        private Runnable sendPulse = new Runnable() {
            public void run() {
            	pulse.generatePulse();

  		      Platform.runLater(new Runnable() {
		          @Override
		          public void run() {
	            	workerMainController.setLblCPU(CpuPerc.format(pulse.getCombinedCPU()));
	            	workerMainController.setLblJVMcpu(CpuPerc.format(pulse.getJvmCPU()));
	            	workerMainController.setLblJVMmem(CpuPerc.format((1.0 * pulse.getJvmUsed() / pulse.getJvmTotalMemory())));

		          }
		      });

        		try {
    				taskSpaceCommon.pulse(new Pulse("Worker", uuid, pulse.getJvmTotalMemory(), pulse.getJvmUsed(), pulse.getJvmFree(),
    						pulse.getUserCPU(), pulse.getSystemCPU(), pulse.getIdleCPU(), pulse.getCombinedCPU(), pulse.getJvmCPU()));

    			} catch (RemoteException e) {
    				e.printStackTrace();
    			}
            }
          };

        /**
         * method for this Worker to connect to the TaskSpace and grab the next available task after notification that one is available
         *
         * @return true if a task was grabbed from the TaskSpace, false otherwise
         */
        private boolean getNextTask(){

    			try {
    				//request next available task from the TaskSpace
					currentTask = taskSpaceCommon.takeTask(uuid);
					//if not empty task, extract details
					if (currentTask != null){
						currentTask.setUuid(uuid);
						startIndex = currentTask.getStartIndex();
						endIndex = currentTask.getEndIndex();
						fileName = currentTask.getRange();
						tasksGrabbed++;
						return true;
					}else{
						//update UI
	    	  		      Platform.runLater(new Runnable() {
	    			          @Override
	    			          public void run() {
	    			        	  workerMainController.setLblCurrent("Awaiting available task...");
	    			        	  workerMainController.setLblGrabbed(String.valueOf(tasksGrabbed));
	    			          }
	    			      });
						return false;
					}

    			} catch (RemoteException e) {
    				e.printStackTrace();
    			}
    			return false;
        }

        /**
         * send a result back to the ResultSpace on the TaskSpace
         *
         * @param dataReslt the result item to send to the TaskSpace
         * @throws RemoteException RMI calls must throw RemoteException
         */
        public static void addToResultSpace(DataResult dataReslt) throws RemoteException{
        	taskSpaceCommon.addResult(dataReslt);
        }

        /* (non-Javadoc)
         * @see javagrid.interfaces.WorkerInt#getUUID()
         */
        public String getUUID(){
        	return uuid;
        }

        /**
         * set the JVm priority based on UI option
         *
         * @param priorityParam the priority level to use for execution thread
         */
        public void setJVMpriority(int priorityParam){
        	priorityInt = priorityParam;
        }

        /**
         * rounding method
         *
         * @param value the number to be rounded
         * @param places the number of places to round to
         * @return the resultant value
         */
        private static double round(double value, int places) {
    	    if (places < 0) throw new IllegalArgumentException();

    	    BigDecimal bd = new BigDecimal(value);
    	    bd = bd.setScale(places, RoundingMode.HALF_UP);
    	    return bd.doubleValue();
    	}

    	/**
    	 * load the configuration file for the Worker
    	 */
    	public void loadWorkerConfig(){

    		URI uri;
    		try {

    			//read the Worker configuration file
            	uri = getClass().getResource("/javagrid/worker/worker.config").toURI();
            	xmlReader = new WorkerXML(uri.getPath());

            	//get the Master ip, port and Http port
            	masterIP = xmlReader.masterIP;
            	masterPort = xmlReader.masterPort;
            	fileServerPort = xmlReader.fileServerPort;

            	//must be location of the exact jar file for codebase. no just directory but full path
            	System.setProperty("java.rmi.server.codebase", "http://" + masterIP + ":" + fileServerPort +  "/job.jar");

    			authToken = xmlReader.token;
    			priority = xmlReader.priority;

    			workerMainController.setPfToken(authToken);
    			tokenChanged = false;

    			//update thread priority based on local config
    			if (priority.equals("MIN")){
    				workerMainController.setCbPriority(0);
    			}else if (priority.equals("MAX")){
    				workerMainController.setCbPriority(1);
    			} else if (priority.equals("NORM")){
    				workerMainController.setCbPriority(2);
    			}

    		} catch (URISyntaxException e) {
    			e.printStackTrace();
    		}
    	}

    	/**
    	 * save the configuration settings to a file
    	 */
    	public void saveWorkerConfig(){

    		String tokenToSave;
    		int priority;
    		String priorityOut = "NORM";

    		//a SHA-512 hash is computed for the token entered in the UI
    		if (tokenChanged == true){
    			tokenToSave = sec.generateSHAhash(workerMainController.getPfToken());
    			workerMainController.setPfToken(tokenToSave);
    			tokenChanged = false;
    		}else{
    			tokenToSave = authToken;
    		}

    		//save priority
    		priority = workerMainController.getCbPriority();

			if (priority == 0){
				priorityOut = "MIN";
			}else if (priority == 1){
				priorityOut = "MAX";
			} else if (priority == 2){
				priorityOut = "NORM";
			}

			//call XML file writer
    		xmlReader.writeXML(tokenToSave, priorityOut);

    	}

    	/**
    	 * a generic warning dialog box used to display an exception or other notification
    	 *
    	 * @param alertParam the alert type
    	 * @param title the title of the alert
    	 * @param header the header to use
    	 * @param content the content of the message
    	 * @return true if dialog closed, false otherwise
    	 */
    	private boolean warnErrorDialogBox(AlertType alertParam, String title, String header, String content){
    		Alert alert = new Alert(alertParam);
        	WorkerMainApp.getInstance();
			alert.initOwner(WorkerMainApp.getPrimaryStage());
        	alert.setTitle(title);
        	alert.setHeaderText(header);
        	alert.setContentText(content);
        	Optional<ButtonType> result = alert.showAndWait();
        	if (result.get() == ButtonType.OK) {
        		return true;
        	} else {
        		return false;
        	}
    	}

    	/**
    	 * call an authentication dialog error
    	 *
    	 * @param source the source of the authentication failure
    	 * @return true if OK clicked on dialog, false otherwise
    	 */
    	public boolean authError(String source){

    		if (source.equals("TaskSpace")){
    			warnErrorDialogBox(AlertType.ERROR, "Authentication Error", "The supplied authentication token was rejected by the TaskSpace", "");
    		}else if (source.equals("Master")){
    			warnErrorDialogBox(AlertType.ERROR, "Authentication Error", "The supplied authentication token was rejected by the Master", "");
    			}
    		return true;
    	}

    	/**
    	 * UK number format
    	 *
    	 * @param num the number to be formated
    	 * @return the formated number
    	 */
    	private String formatNumber(Number num){
    		return NumberFormat.getNumberInstance(Locale.UK).format(num);
    	}


    	/* (non-Javadoc)
    	 * @see javagrid.interfaces.WorkerInt#sendFile(java.lang.String, boolean)
    	 */
    	public RemoteInputStream sendFile(String taskFileName, boolean gzipCompressedStream) throws RemoteException {
    	    // create a RemoteStreamServer (note the finally block which only releases
    	    // the RMI resources if the method fails before returning.)
    	    RemoteInputStreamServer istream = null;
    	    taskFileName = baseDirectory + jobID + "/" + taskFileName + ".jgf";
    		    try {
    		    	if (gzipCompressedStream){
    		    		istream = new GZIPRemoteInputStream(new BufferedInputStream(new FileInputStream(taskFileName)));
    		    	}else{
    		    		istream = new SimpleRemoteInputStream(new BufferedInputStream(new FileInputStream(taskFileName)));
    		    	}

    		      // export the final stream for returning to the client
    		      RemoteInputStream result = istream.export();

    		      // after all the hard work, discard the local reference (we are passing
    		      // responsibility to the client)
    		      istream = null;
    		      return result;
    	    } catch (FileNotFoundException e) {
    			e.printStackTrace();
    			return null;
    		} catch (IOException e) {
    			e.printStackTrace();
    			return null;
    		} finally {
    	      // we will only close the stream here if the server fails before
    	      // returning an exported stream
    	      if(istream != null) istream.close();
    	    }
    	  }


    	/**
    	 * Request to get file from TaskSpace.  Only input set file from the TaskSpace to the Worker.
    	 *
    	 * @param localFilePath the local file path to save the file to
    	 * @param gzipCompressedStream if gzip compressed stream should be used
    	 */
    	public void getFileFromTaskSpace(String localFilePath, boolean gzipCompressedStream){
  		  try {
  			InputStream istream;
  	    	if (gzipCompressedStream){
  	    		istream = RemoteInputStreamClient.wrap(taskSpaceCommon.sendFile("InputFile", true));
	    	}else{
	    		istream = RemoteInputStreamClient.wrap(taskSpaceCommon.sendFile("InputFile", false));
	    	}
  	    	DiskIO.copyInputStreamToFile(istream, new File(localFilePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
  	  }


}

