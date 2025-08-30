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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javagrid.common.DataResult;
import javagrid.common.JobParameters;
import javagrid.common.NodeSpecification;
import javagrid.common.Pulse;
import javagrid.common.Task;
import javagrid.config.JobXML;
import javagrid.config.MasterXML;
import javagrid.controller.MasterMainController;
import javagrid.enumeration.Algorithm;
import javagrid.enumeration.DataSource;
import javagrid.enumeration.Mode;
import javagrid.enumeration.Notification;
import javagrid.enumeration.ResultDestination;
import javagrid.interfaces.MasterInt;
import javagrid.interfaces.TaskSpaceInt;
import javagrid.interfaces.WorkerInt;
import javagrid.master.job.Amalgamate;
import javagrid.master.job.Job;
import javagrid.utils.javaGridFile;
import javagrid.utils.DiskIO;
import javagrid.utils.Heartbeat;
import javagrid.utils.Networking;
import javagrid.utils.ShortUUID;

import org.hyperic.sigar.SigarException;

import com.healthmarketscience.rmiio.GZIPRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

/**
 * The Master is one of the actors in javaGRID framework hosting the job package and job parameters.  It also submits the first task.  It is where
 * the amalgamated results are sent back to.  Implements {@link javagrid.interfaces.MasterInt}.
 */
public class MasterMainApp extends Application implements MasterInt {

	//JavaFX setup
	private static Stage primaryStage;
    private BorderPane rootLayout;
    private FXMLLoader loader = new FXMLLoader();

	private static MasterMainApp instance;
    private static MasterMainController masterMainController;

    //Master is given a unique id
	public final String uuid = "M-" + ShortUUID.generateUUID();

	//Specify our working directory
	private String baseDirectory = getClass().getResource("/javagrid/master/").toString().replace("file:/", "");
	private URI uri;

	//RMI configuration
	private MasterXML xmlReader;
	private int rmiPort=51000;
	private String taskSpaceIP = "127.0.0.1";
	private int taskSpacePort = 50000;
	private String authToken;

	private WorkerInt workerCommon;
	private TaskSpaceInt taskSpaceCommon;

	//Job configuration
	private Job prob;
	private Amalgamate amal;
	public boolean paused;
	private JobXML jobConfig;
	public int indexCounter;

	private String jobID;
	private Mode jobMode;
	private DataSource inputSource;
	private int continuation;
	private ResultDestination outputDestination;
	private Algorithm algorithm;
	private int granularity;
	private int taskTimeout;
	private int pulseInterval = 5;
	private boolean compressFiles;
	private boolean compressStreams;

	//Timers and heartbeat
	public JobTimer jobT;
	private Heartbeat pulse;
	private ScheduledExecutorService pulseTimer;


    /**
     * Constructor and instantiates certain classes before launch
     * 
     * @throws SigarException Sigar library requires to throw this error incase of any exceptions when collecting information
     */
    public MasterMainApp() throws SigarException{

		synchronized (MasterMainApp.class) {
			instance = this;
		}
		prob = new Job();
		amal = new Amalgamate();
		pulse = new Heartbeat();
		jobT = new JobTimer();
    }



    /**
     * Returns an instance of running app
     * 
     * @return instance of running app
     */
    public static MasterMainApp getInstance() {
		return instance;
	}

	/**
	 * Returns an instance of running app's controller
	 * 
	 * @return instnce of running app's controller
	 */
	public static MasterMainController getController(){
		return masterMainController;
	}

	
	/**
	 * Return the primary stage
	 * @return the primary stage
	 */
	public static Stage getPrimaryStage() {
		return primaryStage;
	}


    /* (non-Javadoc)
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
	//starts JavaFX application thread and loads job configuration files
    @Override
    public void start(Stage primaryStage) throws InterruptedException {

            MasterMainApp.primaryStage = primaryStage;
            MasterMainApp.primaryStage.setTitle("javaGRID Master Server");
            MasterMainApp.primaryStage.setResizable(false);
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/javagrid/resources/javaGRIDsmall.png")));

            initRootLayout();
            showMasterMain();

            connection();

        	loadJobConfig();
	        setLabels();
    	}

	/* (non-Javadoc)
	 * @see javafx.application.Application#stop()
	 */
    //on close, terminate the application thread
	public void stop() throws Exception {
		super.stop();
		Platform.exit();
		System.exit(0);
	}

	/**
	 * set the main rootlayout and stages for JavaFX
	 */
	private void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/javagrid/view/MasterRootLayout.fxml"));
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
	 * launch the application
	 */
	private void showMasterMain() {
        try {
            // Load server main screen.
            loader.setLocation(MasterMainApp.class.getResource("/javagrid/view/MasterMain.fxml"));
            AnchorPane masterMain = (AnchorPane) loader.load();

            // Set main screen into the center of root layout.
            rootLayout.setCenter(masterMain);

            // Give the controller access to the main app.
            masterMainController = loader.getController();
            masterMainController.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * used for Eclipse to launch
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Method to start a Http server instance where job package is stored
	 */
	private void startWebServer(){

		Thread webServerThread = new Thread() {
		    public void run() {
		    	HttpFileServer fs = new HttpFileServer(xmlReader.fileServerPort, baseDirectory + "\\job");
		    }
		};
		webServerThread.start();
	}


	/**
	 * starts the RMI registry for incoming connections but also connects to the TaskSpace
	 */
	private void connection() throws InterruptedException {

    	while(startMasterRegister() == false){
        	Thread.sleep(5000);
    	}

    	startWebServer();

    	while(connectToTaskSpace() == false){
        	Thread.sleep(5000);
    	}

    }

	/**
	 * set labels of the ui on startup
	 */
	private void setLabels(){

    	try {
			masterMainController.setLblHostname(InetAddress.getLocalHost().getHostName());
			masterMainController.setLblIP(InetAddress.getLocalHost().getHostAddress() + ":" + rmiPort);
			masterMainController.setLblUUID(uuid);
			masterMainController.setLblTaskSpaceIP(taskSpaceIP + ":" + taskSpacePort);
			masterMainController.setLblStatus("Connected");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * launches the RMI registry
	 * 
	 * @return true if sucessfull, false otherwise 
	 */
	private boolean startMasterRegister() {

		Networking net = new Networking();
	    try {
	    	//load the master.policy file into the security manager
			URI uri = getClass().getResource("/javagrid/master/master.policy").toURI();
			System.setProperty("java.security.policy", uri.getPath());

			/*
			 * if (System.getSecurityManager() == null) { System.setSecurityManager(new
			 * SecurityManager()); }
			 */

		    //load the master configuration file
	    	uri = getClass().getResource("/javagrid/master/master.config").toURI();
	    	xmlReader = new MasterXML(uri.getPath());

	    	taskSpaceIP= xmlReader.taskSpaceIP;
	    	taskSpacePort = xmlReader.taskSpacePort;

	    	//check ports not in use, in range specified and expose on that port
	    	for(int i = xmlReader.portStart; i <= xmlReader.portEnd; i++){
	    		if (net.portAvailable(i)){
	    			rmiPort = i;
	    			break;
	    		}
	    	}
	    	authToken = xmlReader.token;

	    	Registry masterRegistry = LocateRegistry.createRegistry(rmiPort);
	        MasterInt stub = (MasterInt) UnicastRemoteObject.exportObject(this, rmiPort);
			masterRegistry.rebind("MasterRMI", stub);
	        System.out.println("Master bound & ready on port: " + rmiPort );
	        return true;

	    } catch (Exception e) {
	        System.err.println("Master binding exception:" + "\n");
	        e.printStackTrace();
	        return false;
	    }

	}

	/**
	 * connect to the TaskSpace based on IP address and port in the configuration file
	 * 
	 * @return true if successfull, false otherwise
	 */
	private boolean connectToTaskSpace(){

	    try {
	    	Registry taskSpaceRegistry = LocateRegistry.getRegistry(taskSpaceIP, taskSpacePort);
	    	taskSpaceCommon = (TaskSpaceInt) taskSpaceRegistry.lookup("TaskSpaceRMI");

	    	//provide security token to TaskSpace.  If not accepted, terminate
	    	if (!taskSpaceCommon.authenticate(authToken, "Master", InetAddress.getLocalHost().getHostName())){
				if(masterMainController.authError("TaskSpace")){
					System.exit(0);
				}
	    		return false;
	    	}

	    	//valid security token.  Register with the TaskSpace and provide relevant node details
	    	taskSpaceCommon.registerNode(uuid, InetAddress.getLocalHost().getHostName(), "Master", rmiPort);
	    	taskSpaceCommon.receiveNodeSpecs(new NodeSpecification("Master", uuid, pulse.getOsArch(), pulse.getOsName(), pulse.getOsVersion(), pulse.getJreVendor(),
	    			pulse.getJreVersion(), pulse.getCpuModel(), pulse.getCpuVendor(), pulse.getMhz(), pulse.getTotalCores(),
	    			pulse.getSystemMemory(), pulse.getJvmMaxMemory(), pulse.getFreeDisk()));

	    	System.out.println("Registered with TaskSpace");
	    	return true;
	    } catch (Exception e) {
	        System.err.println("Exception connecting to taskspace:" + "\n");
	        e.printStackTrace();
	        return false;
	    }
	}

	/* (non-Javadoc)
	 * @see javagrid.interfaces.MasterInt#authenticate(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean authenticate(String token, String clientType, String hostname) throws RemoteException{

		if (!token.equals(authToken)) {
			System.out.println("A " + clientType + " node connected from host: " + hostname + " but failed authentication.");
			return false;
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see javagrid.interfaces.MasterInt#registerNode(java.lang.String, java.lang.String, int)
	 */
	public void registerNode(String uuid, String hostname, int port) throws RemoteException {
		try {
			String ip = UnicastRemoteObject.getClientHost();
			System.out.println("Worker connected from: " + ip + ":" + port);
			connectToWorker(ip, port);
			saveJobConfig();
			loadJobConfig();
			workerCommon.jobParameters(new JobParameters(paused, jobID, jobMode, inputSource, continuation, outputDestination,
					pulseInterval, compressFiles, compressStreams, algorithm, granularity, taskTimeout));
			startJob();
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
	}

	/**
	 * callback connection to the Worker.  
	 * 
	 * @param ip the ip address of the Worker
	 * @param port the port of the Worker
	 * @return true if successful, false otherwise
	 */
	private boolean connectToWorker(String ip, int port){

	    try {
	    	Registry workerRegistry = LocateRegistry.getRegistry(ip, port);
	    	workerCommon = (WorkerInt) workerRegistry.lookup("WorkerRMI");

	    	//on calling back the authenticated Worker, provide own security token.  Terminate if not accepted
	    	if (!workerCommon.authenticate(authToken, "Master", InetAddress.getLocalHost().getHostName())){
				if(masterMainController.authError("Worker")){
					System.exit(0);
				}
	    		return false;
	    	}

	    	return true;
	    } catch (Exception e) {
	        return false;
	    }
	}

	/**
	 * Method to begin the execution process on the Worker by calling remote method and supplying the job
	 */
	private void startJob(){

        try {
            workerCommon.executeJob(prob);
		} catch (RemoteException e) {
		System.out.println("An exception occured attempting to execute a job on a worker...");
			e.printStackTrace();
		}
	}
	
	/**
	 * After all results returned, begin amalgamation by notifying the TaskSpace
	 */
	private void startAmalgamation(){
		
		try {
			taskSpaceCommon.amalgamateResults(amal);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}

	
    /**
     * Pulse method, called by a timer, which collects this nodes status and information to send to the TaskSpace
     */
    private Runnable sendPulse = new Runnable() {
        public void run() {
        	pulse.generatePulse();
    		try {
				taskSpaceCommon.pulse(new Pulse("Master", uuid, pulse.getJvmTotalMemory(), pulse.getJvmUsed(), pulse.getJvmFree(),
						pulse.getUserCPU(), pulse.getSystemCPU(), pulse.getIdleCPU(), pulse.getCombinedCPU(), pulse.getJvmCPU()));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
        }
      };

	/* (non-Javadoc)
	 * @see javagrid.interfaces.MasterInt#gridInfo(int, int, long)
	 */
	public void gridInfo(int recruited, int totalCPU, long totalRam) throws RemoteException{

	      Platform.runLater(new Runnable() {
	          @Override
	          public void run() {
	        	  masterMainController.updateWorkersUI(String.valueOf(recruited), formatNumber(totalCPU), formatNumber(totalRam));
	          }
	      });

	}


	/**
	 * Method to begin execution on the grid by adding the first task into the TaskSpace
	 * 
	 * @param firstTask the task to be input into the task queue
	 * @throws RemoteException RMI calls must throw RemoteException RMI calls must throw RemoteException
	 */
	public void addFirstTask(Task firstTask) throws RemoteException{

		masterMainController.setLblStart(jobT.getCurrentTime());
		jobT.startTimer();
		taskSpaceCommon.notification(Notification.RESET, "");
		taskSpaceCommon.addTask(firstTask);

	}

	/* (non-Javadoc)
	 * @see javagrid.interfaces.MasterInt#notification(javagrid.enumeration.Notification, java.lang.String)
	 */
	public void notification(Notification type, String value) throws RemoteException {

		//start amalgation
		if(type == Notification.AMALGAMATE){
			
			startAmalgamation();
		
			//job success, update the Master
		} else if (type == Notification.SUCCESS){

			getFileFromTaskSpace(masterMainController.getAmalgamatedFile().getPath(), compressStreams);
			taskSpaceCommon.notification(Notification.SUCCESS, uuid);

		      Platform.runLater(new Runnable() {
		          @Override
		          public void run() {
		        	masterMainController.setLblEnd(jobT.getCurrentTime());
		        	masterMainController.setLblDur(jobT.elapsedTime());
		  			masterMainController.setIvJobOutcomeSucc();
					masterMainController.setPbProgress(1.0f);
					masterMainController.resetButtons();
					masterMainController.disableTabs(false);
		          }
		      });

		} else if (type == Notification.FAILURE){
			masterMainController.setIvJobOutcomeFail();
			masterMainController.setPbProgress(0.0f);
		}
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


	/**
	 * Called when job uses input file for data set and DataSpace is used
	 * 
	 * @param importFile the file to be imported of type TXT, with one value per line
	 */
	public void uploadImportToDataSpace(File importFile) {

		//read file using API class
		DiskIO dio = new DiskIO();
		dio.setFile(importFile);
		String inputValue;

		while(true){

			//read all items from the disk
			inputValue = dio.readAllFromDisk();

			if (inputValue != null){
				try {
					//every item read, is sent to TaskSpace to go into DataSpace
					taskSpaceCommon.addData(new DataResult(indexCounter, inputValue, inputValue));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				indexCounter++;

			}else{
				break;
			}

		}

	}

	/**
	 * Called when job uses input file for data set and file transfer is used
	 * 
	 * @param importFile the file to be imported of type TXT, with one value per line
	 */
	public void uploadImportAsFile(File importFile){

		//read file using API class
		DiskIO inputFile = new DiskIO();
		inputFile.setFile(importFile);

		//create new javaGRID file
		javaGridFile outputFile = new javaGridFile(true, baseDirectory + "jobs/", "", masterMainController.getTfJobID());

		String inputValue;

		//for every item read, send to new javaGRID file
		while(true){

			inputValue = inputFile.readAllFromDisk();

			if (inputValue != null){
				outputFile.saveValue(inputValue);
				indexCounter++;
			}else{
				//commit values
				outputFile.commit();
				outputFile.closeConnection();
				break;
			}
		}

		try {
			//notify TaskSpace that the input file is ready for collection
			taskSpaceCommon.notification(Notification.INPUT_READY, masterMainController.getTfJobID());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Notification method to inform the TaskSpace
	 * 
	 * @param notification the type of notification
	 * @param value any supplementary value
	 */
	public void notifyTaskSpace(Notification notification, String value){

				try {
			    	taskSpaceCommon.notification(notification, value);
				} catch (RemoteException e) {
					e.printStackTrace();
				}

	}

	/**
	 * Call the TaskSpaced to request DataSpace is cleared
	 */
	public void requestDataSpaceCleared(){

		try {
			taskSpaceCommon.clearDataSpace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read the supplied job configuration file and update all necessary variables and UI
	 */
	private void loadJobConfig(){

		try {
			uri = getClass().getResource("/javagrid/master/job/job.config").toURI();
			jobConfig = new JobXML(new File(uri.getPath()));

			//read file and set jobID
			jobConfig.readXML();
			jobID = jobConfig.id;
			masterMainController.setTfJobID(jobConfig.id);
			masterMainController.setLblJobID(jobConfig.id);

			//set the job mode, simple or advanced
			if(jobConfig.mode.equals("SIMPLE")){
				jobMode = Mode.SIMPLE;
				masterMainController.setRbSimple(true);
			}else if(jobConfig.mode.equals("ADVANCED")){
				jobMode = Mode.ADVANCED;
				masterMainController.setRbAdvanced(true);
			}

			//set the job's data source
			if (jobConfig.dataSource.equals("RUNTIME")){
				inputSource = DataSource.RUNTIME;
				masterMainController.setRbRuntime(true);
			}else if(jobConfig.dataSource.equals("DATA_SPACE")){
				inputSource = DataSource.DATA_SPACE;
				masterMainController.setInputDataFile(new File(jobConfig.dataFileLocation));
				masterMainController.setLblInputFile(new File(jobConfig.dataFileLocation).getName());
				masterMainController.setRbDataSpace(true);
			} else if (jobConfig.dataSource.equals("FILE")){
				inputSource = DataSource.FILE;
				masterMainController.setRbInputFile(true);
				masterMainController.setInputDataFile(new File(jobConfig.dataFileLocation));
				masterMainController.setLblInputFile(new File(jobConfig.dataFileLocation).getName());
			}

			//set if the job starts from the beginning or at an offset
			if(jobConfig.range.equals("true")){
				masterMainController.setCbRange(true);
				masterMainController.setDisableTfIndexStart(false);
				masterMainController.setDisableTfIndexEnd(false);
				continuation = Integer.parseInt(jobConfig.startIndex);
			}else if (jobConfig.range.equals("false")){
				masterMainController.setCbRange(false);
				masterMainController.setDisableTfIndexStart(true);
				masterMainController.setDisableTfIndexEnd(true);
			}
			//set the start and end index
			masterMainController.setTfIndexStart(jobConfig.startIndex);
			masterMainController.setTfIndexEnd(jobConfig.endIndex);

			//set the result desintation
			if (jobConfig.resultDestination.equals("RESULT_SPACE")){
				outputDestination = ResultDestination.RESULT_SPACE;
				masterMainController.setRbResultSpace(true);
			}else if(jobConfig.resultDestination.equals("FILE")){
				outputDestination = ResultDestination.FILE;
				masterMainController.setRbDisk(true);
			}
			//set the pulse timer value
			pulseInterval = Integer.parseInt(jobConfig.pulseInterval);
			masterMainController.setSlPulse(pulseInterval);

			//and commence
			pulseTimer = Executors.newSingleThreadScheduledExecutor();
		    pulseTimer.scheduleWithFixedDelay(sendPulse, 3, pulseInterval, TimeUnit.SECONDS);

		    //set amalgamation destination and if compressed streams used
			masterMainController.setAmalgamatedFile(new File(jobConfig.amalgamatedFileLocation));
			masterMainController.setLblAmalgamatedFile(new File(jobConfig.amalgamatedFileLocation).getName());
			masterMainController.setCbCompressFiles(Boolean.parseBoolean(jobConfig.compressFiles));
			masterMainController.setCbCompressStreams(Boolean.parseBoolean(jobConfig.compressStreams));

			// (reserved/deprecated) balancing algorithm
			if (jobConfig.algorithm.equals("STATIC")){
				algorithm = Algorithm.STATIC;
				masterMainController.setRbStatic(true);
			}else if(jobConfig.algorithm.equals("PRIORI")){
				algorithm = Algorithm.PRIORI;
				masterMainController.setRbPriori(true);
			} else if (jobConfig.algorithm.equals("RTS")){
				algorithm = Algorithm.RTS;
				masterMainController.setRbRTS(true);
			}
			//set the granularity
			masterMainController.setTfGranularity(String.valueOf(jobConfig.granulairty));

			//set if task time out is used in this job
			if (Integer.parseInt(jobConfig.taskTimeout) == -1){
				masterMainController.setTfTaskTimeout(String.valueOf(30));
				masterMainController.setCbTaskTimeout(false);
				masterMainController.setDisableTfTaskTimeout(true);
			}else{
				masterMainController.setTfTaskTimeout(String.valueOf(jobConfig.taskTimeout));
				masterMainController.setCbTaskTimeout(true);
				masterMainController.setDisableTfTaskTimeout(false);
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the job settings to a configuration file
	 */
	public void saveJobConfig(){

		//get the value for radiobuttons
		RadioButton rbInput = (RadioButton)masterMainController.getDataInputToggleGroup().getSelectedToggle();
		RadioButton rbOutput = (RadioButton)masterMainController.getResultOutputToggleGroup().getSelectedToggle();
		RadioButton rbAlgorthim = (RadioButton)masterMainController.getAlgorthimToggleGroup().getSelectedToggle();
		RadioButton rbMode = (RadioButton)masterMainController.getModeToggleGroup().getSelectedToggle();

		String modeS = "";

		String sourceType = "";
		String sourceLocation = "";
		String sourceFormat = "";

		String destinationType = "";

		String amalgamatedLocation = "";
		String amalgamatedFormat = "";

		String algorthim = "";

		//set job id but also update UI incase updated
		jobID = masterMainController.getTfJobID();
		
	      Platform.runLater(new Runnable() {
	          @Override
	          public void run() {
	        	  masterMainController.setLblJobID(jobID);
	          }
	      });
	    
	    //set the granularity based on the UI value
	    granularity = Integer.parseInt(masterMainController.getTfGranularity());
	    pulseInterval = masterMainController.getSlPulse();

	    
	    //if offset used
		if(masterMainController.getTfIndexStart().equals("")){
			continuation = 0;
		}else{
			continuation = Integer.parseInt(masterMainController.getTfIndexStart());
		}

		//stop current pulse timer, and restart with new set value
		pulseTimer.shutdownNow();
		pulseTimer = Executors.newSingleThreadScheduledExecutor();
	    pulseTimer.scheduleWithFixedDelay(sendPulse, 3, pulseInterval, TimeUnit.SECONDS);

	    //job mode
		if(rbMode.getId().equals("rbSimple")){
			jobMode = Mode.SIMPLE;
			modeS = "SIMPLE";
		}else if(rbMode.getId().equals("rbAdvanced")){
			jobMode = Mode.ADVANCED;
			modeS = "ADVANCED";
		}

		//source for input set
		if (rbInput.getId().equals("rbRuntime")){
			inputSource = DataSource.RUNTIME;
			sourceType = "RUNTIME";
			sourceLocation = "";
			sourceFormat = "";
		}else if(rbInput.getId().equals("rbDataSpace")){
			inputSource = DataSource.DATA_SPACE;
			sourceType = "DATA_SPACE";
			sourceLocation = masterMainController.getInputDataFile().getPath();
			sourceFormat = sourceLocation.substring(sourceLocation.indexOf(".") + 1, sourceLocation.length());
		} else if (rbInput.getId().equals("rbInputFile")){
			inputSource = DataSource.FILE;
			sourceType = "FILE";
			sourceLocation = masterMainController.getInputDataFile().getPath();
			sourceFormat = sourceLocation.substring(sourceLocation.indexOf(".") + 1, sourceLocation.length());
		}

		//destination for outputs by the Workers
		if (rbOutput.getId().equals("rbResultSpace")){
			outputDestination = ResultDestination.RESULT_SPACE;;
			destinationType = "RESULT_SPACE";
		}else if (rbOutput.getId().equals("rbDisk")){
			outputDestination = ResultDestination.FILE;;
			destinationType = "FILE";
		}
		
		//amalgamation and compression stream details
		amalgamatedLocation = masterMainController.getAmalgamatedFile().getPath();
		amalgamatedFormat = amalgamatedLocation.substring(amalgamatedLocation.indexOf(".") + 1, amalgamatedLocation.length());
		compressFiles = masterMainController.getCbCompressFiles();
		compressStreams = masterMainController.getCbCompressStreams();

		// (reserved/deprecated) balancing algorithm
		if (rbAlgorthim.getId().equals("rbStatic")){
			algorithm = Algorithm.STATIC;
			algorthim = "STATIC";
		}else if(rbAlgorthim.getId().equals("rbPriori")){
			algorithm = Algorithm.PRIORI;
			algorthim = "PRIORI";
		}else if(rbAlgorthim.getId().equals("rbRTS")){
			algorithm = Algorithm.RTS;
			algorthim = "RTS";
		}

		//task time out used?
		if (masterMainController.getCbTaskTimeout() == true){
			taskTimeout = Integer.parseInt(masterMainController.getTfTaskTimeout());
		}else{
			taskTimeout = -1;
		}

		//send job param to taskspace as well as saving to disk
		try {
			taskSpaceCommon.jobParameters(new JobParameters(paused, jobID, jobMode, inputSource, continuation, outputDestination,
					pulseInterval, compressFiles, compressStreams, algorithm, granularity, taskTimeout));
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		//send the parameters to jobConfig class to write to disk
		jobConfig.writeXML(masterMainController.getTfJobID(), modeS, sourceType, sourceLocation, sourceFormat,
				masterMainController.getCbRange(), String.valueOf(continuation),
				masterMainController.getTfIndexEnd(), destinationType, amalgamatedLocation, amalgamatedFormat,
				compressFiles, compressStreams, algorthim, masterMainController.getTfGranularity(), String.valueOf(taskTimeout), String.valueOf(pulseInterval));
	}



	/* (non-Javadoc)
	 * @see javagrid.interfaces.MasterInt#sendFile(java.lang.String, boolean)
	 */
	public RemoteInputStream sendFile(String fileType, boolean gzipCompressedStream) throws RemoteException {
	    // create a RemoteStreamServer (note the finally block which only releases
	    // the RMI resources if the method fails before returning.)

	    RemoteInputStreamServer istream = null;
	    String fileName = "";

		    try {
		    	//if inputFile, send input file to Worker
		    	if (fileType.equals("InputFile")){
		    		fileName = baseDirectory + "/jobs/" + masterMainController.getTfJobID() + ".jgf";
		    	}
		    	
		    	//use compressed stream?
		    	if (gzipCompressedStream){
		    		istream = new GZIPRemoteInputStream(new BufferedInputStream(new FileInputStream(fileName)));
		    		System.out.println("ms compress stream");
		    	}else{
		    		istream = new SimpleRemoteInputStream(new BufferedInputStream(new FileInputStream(fileName)));
		    		System.out.println("ms standard stream");
		    	}

		      // export the final stream for returning to the client
		      RemoteInputStream result = istream.export();
		      //taskSpaceCommon.uploadComplete();

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
	 * Request to collect file from TaskSpace.  Only amalgamated results available.  Option to use compressed stream.
	 * 
	 * @param localFilePath the local file path to save the file to
	 * @param gzipCompressedStream if gzip compressed stream should be used
	 */
	public void getFileFromTaskSpace(String localFilePath, boolean gzipCompressedStream){
		  try {
			InputStream istream;
	    	if (gzipCompressedStream){
	    		istream = RemoteInputStreamClient.wrap(taskSpaceCommon.sendFile("AmalgamatedFile", true));
	    	}else{
	    		istream = RemoteInputStreamClient.wrap(taskSpaceCommon.sendFile("AmalgamatedFile", false));
	    	}
	    	DiskIO.copyInputStreamToFile(istream, new File(localFilePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	  }

}