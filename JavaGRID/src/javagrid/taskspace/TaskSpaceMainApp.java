/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.taskspace;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javagrid.common.DataResult;
import javagrid.common.DataResultUI;
import javagrid.common.JobParameters;
import javagrid.common.MasterNode;
import javagrid.common.NodeSpecification;
import javagrid.common.Pulse;
import javagrid.common.Task;
import javagrid.common.TaskUI;
import javagrid.common.WorkerNode;
import javagrid.config.TaskSpaceXML;
import javagrid.controller.TaskSpaceMainController;
import javagrid.enumeration.DataSource;
import javagrid.enumeration.Notification;
import javagrid.enumeration.ResultDestination;
import javagrid.interfaces.AmalgamateInt;
import javagrid.interfaces.MasterInt;
import javagrid.interfaces.TaskSpaceInt;
import javagrid.interfaces.WorkerInt;
import javagrid.utils.DiskIO;
import javagrid.utils.DiskManagement;
import javagrid.utils.Networking;
import com.healthmarketscience.rmiio.GZIPRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

/**
 * The TaskSpace is one of the actors in the javaGRID framework. Its main
 * function is to act as a central repository for tasks and data input/output.
 */
public class TaskSpaceMainApp extends Application implements TaskSpaceInt {

	// javaFX
	private static Stage primaryStage;
	private BorderPane rootLayout;
	private FXMLLoader loader = new FXMLLoader();

	private static TaskSpaceMainApp instance;
	private static TaskSpaceMainController taskSpaceMainController;

	// Task queue + acquired space
	public ConcurrentLinkedQueue<Task> availableTasks = new ConcurrentLinkedQueue<Task>();
	public ConcurrentMap<Integer, Task> acquiredTasks = new ConcurrentHashMap<>();

	// DataSpace + ResultSpace
	public ArrayList<DataResult> dataSpace = new ArrayList<DataResult>();
	public static ConcurrentSkipListMap<Integer, DataResult> resultSpace = new ConcurrentSkipListMap<Integer, DataResult>();
	private Map<Integer, String> unsortedResultsTreeMap = new TreeMap<Integer, String>();
	public static Map<Integer, String> sortedResultsTreeMap = new TreeMap<Integer, String>();
	public static NavigableSet<Integer> ns;

	// for monitoring elapsed time on tasks
	public ConcurrentMap<String, TaskTimer> taskTimers = new ConcurrentHashMap<>();
	private ConcurrentMap<String, String> timedOutTasks = new ConcurrentHashMap<>();

	// collection of connected workers and masters
	public List<WorkerNode> workerNodes = FXCollections.observableArrayList();
	public List<MasterNode> masterNodes = FXCollections.observableArrayList();

	private ArrayList<String> hostnames = new ArrayList<String>();

	// RMI
	private WorkerInt workerCommon;
	private MasterInt masterCommon;
	private String authToken;

	private ScheduledExecutorService pulseTimer;
	private DiskManagement diskM;
	private URI uri;

	// termination flags
	private boolean terminationRaised = false;
	private boolean terminateImmediate = false;
	private String terminatedCallingWorker;
	private boolean finalTaskReturned;
	private boolean amalgmateStarted;

	// grid info
	private int recruited = 0;
	private int totalCpu = 0;
	private long totalRam = 0;

	// job parameters
	private JobParameters theJob;
	private String jobID;
	private DataSource datasource;
	private ResultDestination outputDestination;
	private int pulseInterval = 5;
	private int granularity;
	private int taskTimeout;

	private String baseDirectory = getClass().getResource("/javagrid/taskspace/jobs/").toString().replace("file:/", "");

	/**
	 * Constructor which initialises file and task timer classes
	 */
	public TaskSpaceMainApp() {
		synchronized (TaskSpaceMainApp.class) {
			instance = this;
		}

		diskM = new DiskManagement();
		pulseTimer = Executors.newSingleThreadScheduledExecutor();
		pulseTimer.scheduleAtFixedRate(pulsate, 3, pulseInterval, TimeUnit.SECONDS);
	}

	/**
	 * @return returns an instance of this app
	 */
	public static TaskSpaceMainApp getInstance() {
		return instance;
	}

	/**
	 * @return an instance of this apps controller
	 */
	public static TaskSpaceMainController getController() {
		return taskSpaceMainController;
	}

	/**
	 * @return the mains stage
	 */
	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws InterruptedException {
		TaskSpaceMainApp.primaryStage = primaryStage;
		TaskSpaceMainApp.primaryStage.setTitle("javaGRID TaskSpace Server");
		TaskSpaceMainApp.primaryStage.setResizable(false);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/javagrid/resources/javaGRIDsmall.png")));

		initRootLayout();
		showTaskSpaceMain();

		connection();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.application.Application#stop()
	 */
	// on close, exit and terminate application thread
	public void stop() throws Exception {
		super.stop();
		Platform.exit();
		System.exit(0);
	}

	/**
	 * @param args any arguments, which will be ignored
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * initialises the main stage and loads fxml file
	 */
	private void initRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/javagrid/view/TaskSpaceRootLayout.fxml"));
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
	 * show the main TaskSpace app window
	 */
	private void showTaskSpaceMain() {
		try {
			// Load server main screen.
			loader.setLocation(getClass().getResource("/javagrid/view/TaskSpaceMain.fxml"));
			AnchorPane masterMain = (AnchorPane) loader.load();

			// Set main screen into the center of root layout.
			rootLayout.setCenter(masterMain);

			// Give the controller access to the main app.
			taskSpaceMainController = loader.getController();
			taskSpaceMainController.setMainApp(this);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * start the RMI registry to expose objects
	 */
	private void connection() throws InterruptedException {

		while (startTaskSpaceRegister() == false) {
			Thread.sleep(5000);
		}

	}

	/**
	 * Start the RMI registry for the TaskSpace
	 * 
	 * @return true if successful, false otherwise
	 */
	private boolean startTaskSpaceRegister() {

		int rmiPort = 50000;
		Networking net = new Networking();
		try {
			// load the taskspace.policy file into the security manager
			uri = getClass().getResource("/javagrid/taskspace/taskspace.policy").toURI();
			System.setProperty("java.security.policy", uri.getPath());

			/*
			 * if (System.getSecurityManager() == null) { System.setSecurityManager(new
			 * SecurityManager()); }
			 */

			// load configuration file
			uri = getClass().getResource("/javagrid/taskspace/taskspace.config").toURI();
			TaskSpaceXML xmlReader = new TaskSpaceXML(uri.getPath());

			// check first avilable port in range to use
			for (int i = xmlReader.portStart; i <= xmlReader.portEnd; i++) {
				if (net.portAvailable(i)) {
					rmiPort = i;
					break;
				}
			}

			authToken = xmlReader.token;

			// start registry
			Registry taskSpaceRegistry = LocateRegistry.createRegistry(rmiPort);
			TaskSpaceInt stub = (TaskSpaceInt) UnicastRemoteObject.exportObject(this, rmiPort);
			taskSpaceRegistry.rebind("TaskSpaceRMI", stub);

			taskSpaceMainController.setLblHostname(InetAddress.getLocalHost().getHostName());
			taskSpaceMainController.setLblIP(InetAddress.getLocalHost().getHostAddress() + ":" + rmiPort);
			taskSpaceMainController.setLblPrimary("True");
			System.out.println("TaskSpace bound & ready on port: " + rmiPort);
			return true;
		} catch (Exception e) {
			System.err.println("Task Space binding exception:" + "\n");
			e.printStackTrace();
			return true;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#authenticate(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public boolean authenticate(String token, String clientType, String hostname) throws RemoteException {

		if (!token.equals(authToken)) {
			System.out.println(
					"A " + clientType + " node connected from host: " + hostname + " but failed authentication.");
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#registerNode(java.lang.String,
	 * java.lang.String, java.lang.String, int)
	 */
	public void registerNode(String uuid, String hostname, String clientType, int port) {

		try {
			String ip = UnicastRemoteObject.getClientHost();

			// if registering node is of type Master
			if (clientType.equals("Master")) {
				MasterNode mn = new MasterNode(uuid, hostname, ip, port, "Connected", "Idle");
				masterNodes.add(mn);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						taskSpaceMainController.addMasterToTree(mn);
					}
				});
			} else if (clientType.equals("Worker")) {

				// if registering node is of type Worker
				WorkerNode wn = new WorkerNode(uuid, hostname, ip, port, "Connected", 0);
				workerNodes.add(wn);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						taskSpaceMainController.addWorkerToTree(wn);
					}
				});

				if (!availableTasks.isEmpty()) {
					notifyWorkers(Notification.NEW_TASK, "");
				}
			}
			System.out.println(clientType + " connected from: " + ip + ":" + port);
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#receiveNodeSpecs(javagrid.common.
	 * NodeSpecification)
	 */
	public void receiveNodeSpecs(NodeSpecification nodeSpec) {

		if (nodeSpec.getClientType().equals("Master")) {
			for (int i = 0; i <= masterNodes.size() - 1; i++) {

				MasterNode mn = masterNodes.get(i);
				if (mn.getUUID().equals(nodeSpec.getUuid())) {
					mn.setOsArch(nodeSpec.getOsArch());
					mn.setOsName(nodeSpec.getOsName());
					mn.setOsVersion(nodeSpec.getOsVersion());
					mn.setJreVendor(nodeSpec.getJreVendor());
					mn.setJreVersion(nodeSpec.getJreVersion());
					mn.setCpuModel(nodeSpec.getCpuModel());
					mn.setCpuVendor(nodeSpec.getCpuVendor());
					mn.setMhz(nodeSpec.getMhz());
					mn.setTotalCores(nodeSpec.getTotalCores());
					mn.setSystemMemory(nodeSpec.getSystemMemory());
					mn.setJvmMaxMemory(nodeSpec.getJvmMaxMemory());
					mn.setFreeDisk(nodeSpec.getFreeDisk());
					break;
				}
			}
		} else if (nodeSpec.getClientType().equals("Worker")) {
			for (int i = 0; i <= workerNodes.size() - 1; i++) {

				WorkerNode wn = workerNodes.get(i);
				if (wn.getUUID().equals(nodeSpec.getUuid())) {
					wn.setOsArch(nodeSpec.getOsArch());
					wn.setOsName(nodeSpec.getOsName());
					wn.setOsVersion(nodeSpec.getOsVersion());
					wn.setJreVendor(nodeSpec.getJreVendor());
					wn.setJreVersion(nodeSpec.getJreVersion());
					wn.setCpuModel(nodeSpec.getCpuModel());
					wn.setCpuVendor(nodeSpec.getCpuVendor());
					wn.setMhz(nodeSpec.getMhz());
					wn.setTotalCores(nodeSpec.getTotalCores());
					wn.setSystemMemory(nodeSpec.getSystemMemory());
					wn.setJvmMaxMemory(nodeSpec.getJvmMaxMemory());
					wn.setFreeDisk(nodeSpec.getFreeDisk());

					recruited++;
					if (hostnames.contains(workerNodes.get(i).getHostname())) {
					} else {
						hostnames.add(wn.getHostname());
						totalCpu = totalCpu + nodeSpec.getMhz();
						totalRam = totalRam + nodeSpec.getSystemMemory();
					}

					notifyMastersGridInfo();

					break;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#pulse(javagrid.common.Pulse)
	 */
	public void pulse(Pulse pulseParam) {

		// if node type is Master, update information
		if (pulseParam.getClientType().equals("Master")) {
			for (int i = 0; i <= masterNodes.size() - 1; i++) {

				MasterNode mn = masterNodes.get(i);
				if (mn.getUUID().equals(pulseParam.getUuid())) {

					mn.setJvmTotalMemory(pulseParam.getJvmTotalMemory());
					mn.setJvmUsed(pulseParam.getJvmUsed());
					mn.setJvmFree(pulseParam.getJvmFree());
					mn.setUserCPU(pulseParam.getUserCPU());
					mn.setSystemCPU(pulseParam.getSystemCPU());
					mn.setIdleCPU(pulseParam.getIdleCPU());
					mn.setCombinedCPU(pulseParam.getCombinedCPU());
					mn.setJvmCPU(pulseParam.getJvmCPU());

					// check if any open dialogs and update
					for (int j = 0; j <= taskSpaceMainController.dialogs.size() - 1; j++) {
						NodeDialog currentDialog = taskSpaceMainController.dialogs.get(j);
						if (currentDialog != null && currentDialog.getUuid().equals(pulseParam.getUuid())) {

							Platform.runLater(new Runnable() {
								@Override
								public void run() {

									currentDialog.getController().updateStats(pulseParam.getJvmTotalMemory(),
											pulseParam.getJvmUsed(), pulseParam.getJvmFree(), pulseParam.getUserCPU(),
											pulseParam.getSystemCPU(), pulseParam.getIdleCPU(),
											pulseParam.getCombinedCPU(), pulseParam.getJvmCPU());
								}
							});

						}
					}

					break;
				}
			}

			// if node type is Worker, update information
		} else if (pulseParam.getClientType().equals("Worker")) {
			for (int i = 0; i <= workerNodes.size() - 1; i++) {

				WorkerNode wn = workerNodes.get(i);
				if (wn.getUUID().equals(pulseParam.getUuid())) {

					wn.setJvmTotalMemory(pulseParam.getJvmTotalMemory());
					wn.setJvmUsed(pulseParam.getJvmUsed());
					wn.setJvmFree(pulseParam.getJvmFree());
					wn.setUserCPU(pulseParam.getUserCPU());
					wn.setSystemCPU(pulseParam.getSystemCPU());
					wn.setIdleCPU(pulseParam.getIdleCPU());
					wn.setCombinedCPU(pulseParam.getCombinedCPU());
					wn.setJvmCPU(pulseParam.getJvmCPU());

					// check if any open dialogs and update
					for (int j = 0; j <= taskSpaceMainController.dialogs.size() - 1; j++) {
						NodeDialog currentDialog = taskSpaceMainController.dialogs.get(j);
						if (currentDialog != null && currentDialog.getUuid().equals(pulseParam.getUuid())) {

							Platform.runLater(new Runnable() {
								@Override
								public void run() {

									currentDialog.getController().updateStats(pulseParam.getJvmTotalMemory(),
											pulseParam.getJvmUsed(), pulseParam.getJvmFree(), pulseParam.getUserCPU(),
											pulseParam.getSystemCPU(), pulseParam.getIdleCPU(),
											pulseParam.getCombinedCPU(), pulseParam.getJvmCPU());
								}
							});

						}
					}

				}
			}
		}

	}

	/**
	 * Runnable class + method called by heartbeat timer
	 */
	private Runnable pulsate = new Runnable() {
		public void run() {
			PingAllNodes();
		}
	};

	/**
	 * ping all nodes, Master and Worker
	 */
	private void PingAllNodes() {

		for (int i = 0; i <= workerNodes.size() - 1; i++) {
			if (pingWorker(workerNodes.get(i).getIP(), Integer.parseInt(workerNodes.get(i).getPort()))) {
			} else {
				System.out.println("A worker has disconnected...");
				removeWorkerAndReassignTask(workerNodes.get(i).getUUID());
			}
		}

		for (int i = 0; i <= masterNodes.size() - 1; i++) {
			if (pingMaster(masterNodes.get(i).getIP(), Integer.parseInt(masterNodes.get(i).getPort()))) {
			} else {
				removeMaster(masterNodes.get(i).getUUID());
				System.out.println("The Master has disconnected.");
			}
		}

	}

	/**
	 * ping Worker by trying to connect to remote registry
	 * 
	 * @param ip   the ip of the Worker
	 * @param port the port of the Worker
	 * @return true if Worker responds, false otherwise
	 */
	private boolean pingWorker(String ip, int port) {

		try {
			Registry workerRegistry = LocateRegistry.getRegistry(ip, port);
			workerCommon = (WorkerInt) workerRegistry.lookup("WorkerRMI");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * ping Master by trying to connect to remote registry
	 * 
	 * @param ip   the ip of the Master
	 * @param port the port of the Master
	 * @return true if Master responds, false otherwise
	 */
	private boolean pingMaster(String ip, int port) {

		try {
			Registry masterRegistry = LocateRegistry.getRegistry(ip, port);
			masterCommon = (MasterInt) masterRegistry.lookup("MasterRMI");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javagrid.interfaces.TaskSpaceInt#jobParameters(javagrid.common.JobParameters)
	 */
	public void jobParameters(JobParameters jobParam) throws RemoteException {
		// update local variables after receiving job parameters
		theJob = jobParam;
		jobID = jobParam.getJobId();
		datasource = jobParam.getInputSource();
		outputDestination = jobParam.getOutputDestination();
		pulseInterval = jobParam.getPulseInterval();
		granularity = jobParam.getGranularity();
		taskTimeout = jobParam.getTaksTimeout();

		// terminate current timer and start again with new values
		pulseTimer.shutdownNow();
		pulseTimer = Executors.newSingleThreadScheduledExecutor();
		pulseTimer.scheduleWithFixedDelay(pulsate, 3, pulseInterval, TimeUnit.SECONDS);
	}

	/**
	 * Notify Workers on behalf of other methods
	 * 
	 * @param notification the notification type
	 * @param value        supplementary values
	 */
	private void notifyWorkers(Notification notification, String value) {

		for (int i = 0; i <= workerNodes.size() - 1; i++) {

			Registry workerRegistry;
			try {
				workerRegistry = LocateRegistry.getRegistry(workerNodes.get(i).getIP(),
						Integer.parseInt(workerNodes.get(i).getPort()));
				workerCommon = (WorkerInt) workerRegistry.lookup("WorkerRMI");

				if (!workerCommon.authenticate(authToken, "TaskSpace", InetAddress.getLocalHost().getHostName())) {
					if (taskSpaceMainController.authError("Worker")) {
					}
					throw new SecurityException(
							"Authentication error - The supplied authentication token was rejected by worker at: "
									+ workerNodes.get(i).getHostname());
				}

				workerCommon.notification(notification, value);
			} catch (NumberFormatException | RemoteException | NotBoundException | UnknownHostException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Update the Master of the current status of the grid, after authenticating
	 */
	private void notifyMastersGridInfo() {

		for (int i = 0; i <= masterNodes.size() - 1; i++) {
			Registry masterRegistry;
			try {
				// connect to the Master
				masterRegistry = LocateRegistry.getRegistry(masterNodes.get(i).getIP(),
						Integer.parseInt(masterNodes.get(i).getPort()));
				masterCommon = (MasterInt) masterRegistry.lookup("MasterRMI");

				// authenticate. If successful, continue, else exit
				if (!masterCommon.authenticate(authToken, "TaskSpace", InetAddress.getLocalHost().getHostName())) {
					if (taskSpaceMainController.authError("Master")) {
					}
					throw new SecurityException(
							"Authentication error - The supplied authentication token was rejected by the Master at: "
									+ masterNodes.get(i).getHostname());
				}
				// send the grid information
				masterCommon.gridInfo(recruited, totalCpu, totalRam);
			} catch (NumberFormatException | RemoteException | NotBoundException | UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Notify the Master
	 * 
	 * @param notification the notification type
	 * @param value        supplementary values
	 */
	private void notifyMaster(Notification notification, String value) {

		for (int i = 0; i <= masterNodes.size() - 1; i++) {

			Registry masterRegistry;
			try {
				masterRegistry = LocateRegistry.getRegistry(masterNodes.get(i).getIP(),
						Integer.parseInt(masterNodes.get(i).getPort()));
				masterCommon = (MasterInt) masterRegistry.lookup("MasterRMI");
				masterCommon.notification(notification, value);
			} catch (NumberFormatException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#notification(javagrid.enumeration.
	 * Notification, java.lang.String)
	 */
	public void notification(Notification type, String value) throws RemoteException {

		// notification received check
		if (type == Notification.INPUT_READY) {
			getFileFromMaster("InputFile", baseDirectory + value + ".jgf", theJob.getCompressSteams());
		} else if (type == Notification.RESULT_READY) {
			getFileFromWorker(value, theJob.getCompressSteams());
		} else if (type == Notification.EXECUTE) {
			setMasterJobStatus(value, "Executing");

		} else if (type == Notification.PAUSE) {
			setMasterJobStatus(value, "Paused");
			notifyWorkers(Notification.PAUSE, "");

		} else if (type == Notification.ABORT) {
			setMasterJobStatus(value, "Aborted");
			notifyWorkers(Notification.ABORT, "");

		} else if (type == Notification.SUCCESS) {
			setMasterJobStatus(value, "Completed");

		} else if (type == Notification.FAILURE) {
			setMasterJobStatus(value, "Failed");

		} else if (type == Notification.RESUME) {
			notifyWorkers(Notification.RESUME, "");

		} else if (type == Notification.RESET) {

			// clear & reset
			availableTasks.clear();
			acquiredTasks.clear();
			resultSpace.clear();
			timedOutTasks.clear();
			taskTimers.clear();
			unsortedResultsTreeMap.clear();
			sortedResultsTreeMap.clear();

			terminationRaised = false;
			terminateImmediate = false;
			terminatedCallingWorker = "";
			finalTaskReturned = false;
			amalgmateStarted = false;

			diskM.deleteDirectory(Paths.get(baseDirectory + jobID));
			diskM.createDirectory(Paths.get(baseDirectory + jobID));

			resetWorkerTaskExecutedCounter();
			notifyWorkers(Notification.RESET, "");

			// update UI
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					taskSpaceMainController.removeAllTasksAvailableQueue();
					taskSpaceMainController.removeAllTasksAcquiredQueue();
					taskSpaceMainController.removeAllOutputsResultSpace();

				}
			});

			if (datasource == DataSource.DATA_SPACE) {

			} else {
				clearDataSpace();
			}

		}

	}

	/**
	 * for all connected workers, reset the 'number of tasks executed' back to 0
	 */
	private void resetWorkerTaskExecutedCounter() {
		for (int i = 0; i <= workerNodes.size() - 1; i++) {
			workerNodes.get(i).setTasksExecuted(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#addTask(javagrid.common.Task)
	 */
	public void addTask(Task theTask) {

		// always set a new task to "NextTask" marker
		theTask.setID("NextTask");
		availableTasks.offer(theTask);
		TaskUI temp = new TaskUI("NextTask", theTask.getStartIndex(), theTask.getEndIndex());

		// update UI
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				taskSpaceMainController.addTaskAvailableQueue(temp);
			}
		});

		// notify Workers of available task
		notifyWorkers(Notification.NEW_TASK, "");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#takeTask(java.lang.String)
	 */
	public Task takeTask(String uuid) {

		Date date = new Date();
		Task theTask;

		// synchronised block used, to ensure we take a task from the queue and insert
		// another in one transaction
		synchronized (availableTasks) {

			theTask = availableTasks.poll();

			if (theTask == null) {
				return null;
			}

			// update the task details
			theTask.setID(UUID.randomUUID().toString());
			theTask.setUuid(uuid);
			theTask.setStatus(0);
			theTask.setStartTime(date);

			// put a copy of the task into the acquired task space
			acquiredTasks.put(acquiredTasks.size(), theTask);

			// if this task has previously failed, don't add subsequent task, otherwise
			// great a new task from it's end index
			if (theTask.getPreviouslyFailed() == true) {
				notifyWorkers(Notification.NEW_TASK, "");
			} else if (terminationRaised == false) {
				addTask(new Task(theTask.getEndIndex() + 1, theTask.getEndIndex() + granularity, false));
			}
		}

		// update UI
		TaskUI temp = new TaskUI(theTask.getID(), theTask.getStartIndex(), theTask.getEndIndex());
		temp.setUuid(uuid);
		temp.setStatus(0);
		temp.setStartTime(date);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				taskSpaceMainController.addTaskAcquiredQueue(temp);
				taskSpaceMainController.removeTaskAvailableQueue(0);
			}
		});

		// start task timer, if used in this job
		if (taskTimeout > 0) {
			TaskTimer theTimer = new TaskTimer(theTask.getID(), getIndexOfTask(theTask.getID()), taskTimeout);
			taskTimers.put(theTask.getID(), theTimer);
			taskTimers.get(theTask.getID()).startTimer();
		}

		return theTask;
	}

	/**
	 * populate the temporary treemap used to sort the ResultSpace
	 */
	private void populateResultsTreeMap() {

		for (Map.Entry<Integer, DataResult> entry : resultSpace.entrySet()) {
			DataResult tempData = entry.getValue();
			unsortedResultsTreeMap.put(tempData.getKeyIndex(), tempData.getVisualValue());
		}

	}

	/**
	 * populate the UI ResultsTable
	 */
	private void populateResultsTable() {

		ns = resultSpace.keySet();

		sortedResultsTreeMap = new TreeMap<Integer, String>(unsortedResultsTreeMap);

		for (Map.Entry<Integer, String> entry : sortedResultsTreeMap.entrySet()) {
			DataResultUI tempUI = new DataResultUI(entry.getKey(), entry.getValue());

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					taskSpaceMainController.addOutputResultSpace(tempUI);
				}
			});
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#addResult(javagrid.common.DataResult)
	 */
	public void addResult(DataResult dataResult) {
		// resultSpace.put(dataResult.getKeyIndex(), dataResult);
		resultSpace.put(dataResult.getKeyIndex(), dataResult);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#takeResult(int)
	 */
	public DataResult takeResult(int key) {

		taskSpaceMainController.removeOutputResultSpace(key);

		// return resultSpace.remove (key);
		return resultSpace.remove(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#raiseTermination(java.lang.String,
	 * boolean)
	 */
	public void raiseTermination(String uuid, boolean terminateNow) {

		// if user has defined immediate termination, notify all Workers to terminate
		// now
		if (terminateNow == true) {
			notifyWorkers(Notification.TERMINATE_IMMEDIATE, "");
			notifyMaster(Notification.AMALGAMATE, "");
			notifyMaster(Notification.SUCCESS, "");

			// if terminate at the end of current batch, continue checking until last task
			// returned, then call termination
		} else if (terminateNow == false && failedJobAwaiting() == false && activeTasksExist(uuid) == false) {
			notifyWorkers(Notification.TERMINATE_END, "");

			// if final task returned, amalgamate
			if (finalTaskReturned == true) {
				populateResultsTreeMap();
				populateResultsTable();
				notifyMaster(Notification.AMALGAMATE, "");
				notifyMaster(Notification.SUCCESS, "");
			}

		}
		terminationRaised = true;
		terminateImmediate = terminateNow;
		terminatedCallingWorker = uuid;
		finalTaskReturned = false;
	}

	/**
	 * method used to check if there are any currently active tasks i.e. not
	 * returned, apart from the one just returned by the calling Worker
	 * 
	 * @param uuid the id of the Worker raising termination
	 * @return true if active tasks exist, false otherwise
	 */
	private boolean activeTasksExist(String uuid) {

		int count = 0;

		for (Map.Entry<Integer, Task> entry : acquiredTasks.entrySet()) {
			Task tempData = entry.getValue();
			if (tempData.getStatus() == 0) {
				count++;
			}
		}

		if (count <= 1) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * confirm if any failed jobs are awaiting in the task queue and thus
	 * termination should not be called yet
	 * 
	 * @return true if failed jobs awaiting, false otherwise
	 */
	private boolean failedJobAwaiting() {

		Iterator<Task> itr = availableTasks.iterator();
		while (itr.hasNext()) {
			if (itr.next().getPreviouslyFailed() == true) {
				return true;
			}
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#taskComplete(javagrid.common.Task, int)
	 */
	public void taskComplete(Task theTask, int status) {

		// if task returned is one marked timed out by the TaskSpace, then do not accept
		if (timedOutTasks.containsKey(theTask.getID())) {

		} else {

			// check termination condition again, as this could be the final task
			if (terminationRaised == true) {
				finalTaskReturned = true;
				raiseTermination(terminatedCallingWorker, terminateImmediate);
			}

			// update the task details and increment Worker as returning one more job
			Date date = new Date();

			theTask.setEndTime(date);
			incrementWorkerTaskCount(theTask.getUuid());
			theTask.setStatus(status);

			// update the UI
			int theIndex = getIndexOfTask(theTask.getID());
			acquiredTasks.replace(theIndex, theTask);
			System.out.println("Task completed in: " + theTask.getTaskDuration() + " seconds");
			Platform.runLater(new Runnable() {
				@Override
				public void run() {

					taskSpaceMainController.setStatusAcquiredQueue(theIndex, status);
					taskSpaceMainController.setDurationAcquiredQueue(theIndex, theTask.getTaskDuration());
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#addData(javagrid.common.DataResult)
	 */
	public void addData(DataResult dataResult) throws RemoteException {

		dataSpace.add(dataResult);

		DataResultUI temp = new DataResultUI(dataResult.getKeyIndex(), dataResult.getVisualValue());

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				taskSpaceMainController.addInputDataSpace(temp);
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#takeData(int)
	 */
	public DataResult takeData(int key) throws RemoteException {

		if (key <= dataSpace.size() - 1) {
			return dataSpace.get(key);
		}

		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#clearDataSpace()
	 */
	public void clearDataSpace() {
		dataSpace.clear();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				taskSpaceMainController.removeAllInputsDataSpace();
			}
		});
	}

	/**
	 * necessary to get an index using the task id, for indexed based structures
	 * 
	 * @param taskId the task id of to lookup
	 * @return the corrosponding index
	 */
	public int getIndexOfTask(String taskId) {

		int theIndex = 0;

		for (Map.Entry<Integer, Task> entry : acquiredTasks.entrySet()) {
			Task tempData = entry.getValue();
			if (tempData.getID().equals(taskId)) {
				theIndex = entry.getKey();
				break;
			}
		}

		return theIndex;
	}

	/**
	 * set the status of the Master, in the UI, based on the execution status
	 * 
	 * @param uuid      the id of the Master to update
	 * @param jobStatus the status to update to
	 */
	private void setMasterJobStatus(String uuid, String jobStatus) {

		for (int i = 0; i <= masterNodes.size() - 1; i++) {
			if (masterNodes.get(i).getUUID().equals(uuid)) {
				masterNodes.get(i).setJobStatus(jobStatus);
				break;
			}
		}
	}

	/**
	 * increments the number of tasks recorded against the Worker, to have been
	 * completed and returned
	 * 
	 * @param uuid the id of the Worker to increment
	 */
	private void incrementWorkerTaskCount(String uuid) {

		for (int i = 0; i <= workerNodes.size() - 1; i++) {
			if (workerNodes.get(i).getUUID().equals(uuid)) {
				workerNodes.get(i).setTasksExecuted(Integer.parseInt(workerNodes.get(i).getTasksExecuted()) + 1);
				break;
			}

		}
	}

	/**
	 * get the current status of a task in question
	 * 
	 * @param taskIndex the task index to check
	 * @return the status of the task
	 */
	public int getStatusOfTaskByIndex(int taskIndex) {
		return acquiredTasks.get(taskIndex).getStatus();
	}

	/**
	 * remove a Master node from list and then the UI
	 * 
	 * @param uuid the id of the Master node to remove
	 */
	private void removeMaster(String uuid) {

		// iterate over entire list, find the node with the same id, then remove
		for (int i = 0; i <= masterNodes.size() - 1; i++) {
			if (masterNodes.get(i).getUUID().equals(uuid)) {
				masterNodes.remove(i);
				break;
			}
		}

		// update UI
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				taskSpaceMainController.removeMasterFromTree(uuid);
				if (taskSpaceMainController.warnErrorDialogBox(AlertType.ERROR, "Fatal Error",
						"System cannot return results since the Master has disconnected!"
								+ System.getProperty("line.separator") + "System will not exit.",
						"")) {
					try {
						stop();
					} catch (Exception e) {
					}
				}
			}
		});
	}

	/**
	 * removes the non-responsive Worker from the grid list and returns any
	 * currently active tasks belonging to this Worker, back into the task queue
	 * 
	 * @param uuid the id of failed Worker
	 */
	private void removeWorkerAndReassignTask(String uuid) {

		int instanceCount = 0;
		for (int i = 0; i <= workerNodes.size() - 1; i++) {
			if (workerNodes.get(i).getUUID().equals(uuid)) {

				for (int j = 0; j <= workerNodes.size() - 1; j++) {
					if (workerNodes.get(j).getHostname().equals(workerNodes.get(i).getHostname())) {
						instanceCount++;
					}
				}
				// update grid information by checking if another Worker instance is running on
				// the same physical host
				if (instanceCount == 1) {
					totalCpu = totalCpu - workerNodes.get(i).getMhz();
					totalRam = totalRam - workerNodes.get(i).getSystemMemory();
				}
				recruited--;
				workerNodes.remove(i);
				break;
			}
		}

		// notify the Master of the grid change
		notifyMastersGridInfo();

		// update UI
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				taskSpaceMainController.removeWorkerFromTree(uuid);
			}
		});
		// call the method to return tasks back to task queue
		returnTaskToQueueByWorkerID(uuid);
	}

	/**
	 * method to call when wanting to return tasks, belonging to a Worker and
	 * assumed failed, back to the task queue
	 * 
	 * @param uuid the id of the Worker
	 */
	private void returnTaskToQueueByWorkerID(String uuid) {

		// check acquired list of tasks for the ones belonging to the Worker
		for (Map.Entry<Integer, Task> entry : acquiredTasks.entrySet()) {
			Task tempData = entry.getValue();

			// find the failed ones
			if (tempData.getUuid().equals(uuid) && tempData.getStatus() == 0) {

				// mark that task as failed and put back in acquired task queue
				tempData.setPreviouslyFailed(true);
				tempData.setStatus(1);
				acquiredTasks.replace(entry.getKey(), tempData);

				taskSpaceMainController.setStatusAcquiredQueue(entry.getKey(), 1);

				addTask(new Task(tempData.getStartIndex(), tempData.getEndIndex(), true));
				break;
			}
		}

	}

	/**
	 * method to call when wanting to return a specific task, assumed failed, back
	 * to the task queue
	 * 
	 * @param index the index of the task, based on its position, to return to the
	 *              task queue
	 */
	public void returnTaskToQueueByIndex(int index) {

		Task temp = acquiredTasks.get(index);

		temp.setPreviouslyFailed(true);
		temp.setStatus(1);
		acquiredTasks.replace(index, temp);
		timedOutTasks.put(temp.getID(), "");

		taskSpaceMainController.setStatusAcquiredQueue(index, 4);

		addTask(new Task(temp.getStartIndex(), temp.getEndIndex(), true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#sendFile(java.lang.String, boolean)
	 */
	public RemoteInputStream sendFile(String fileType, boolean gzipCompressedStream) throws RemoteException {
		// create a RemoteStreamServer (note the finally block which only releases
		// the RMI resources if the method fails before returning.)

		RemoteInputStreamServer istream = null;
		String fileName = "";

		try {

			// send an input file to a Worker
			if (fileType.equals("InputFile")) {
				fileName = baseDirectory + jobID + ".jgf";

				// send amalgamated file to Master
			} else if (fileType.equals("AmalgamatedFile")) {
				fileName = baseDirectory + jobID + "/" + jobID + "_Amalgamated.txt";
			}

			// use compressed stream or not?
			if (gzipCompressedStream) {
				istream = new GZIPRemoteInputStream(new BufferedInputStream(new FileInputStream(fileName)));
			} else {
				istream = new SimpleRemoteInputStream(new BufferedInputStream(new FileInputStream(fileName)));
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
			if (istream != null)
				istream.close();
		}
	}

	/**
	 * request file from the Master
	 * 
	 * @param fileType             the type of file to request
	 * @param localFilePath        the local path to save it to
	 * @param gzipCompressedStream if gzip compressed streams should be used
	 */
	private void getFileFromMaster(String fileType, String localFilePath, boolean gzipCompressedStream) {

		try {
			InputStream istream;

			Registry masterRegistry = LocateRegistry.getRegistry(masterNodes.get(0).getIP(),
					Integer.parseInt(masterNodes.get(0).getPort()));
			masterCommon = (MasterInt) masterRegistry.lookup("MasterRMI");

			// wrap RemoteInputStream as InputStream (all compression issues are dealt with
			// in the wrapper code)
			if (gzipCompressedStream) {
				istream = RemoteInputStreamClient.wrap(masterCommon.sendFile(fileType, true));
			} else {
				istream = RemoteInputStreamClient.wrap(masterCommon.sendFile(fileType, false));
			}
			// BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
			DiskIO.copyInputStreamToFile(istream, new File(localFilePath));
		} catch (IOException | NotBoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * request a file from a Worker. A result file.
	 * 
	 * @param taskID               the task id which matches the result file to
	 *                             return
	 * @param gzipCompressedStream if gzip compressed streams should be used
	 */
	private void getFileFromWorker(String taskID, boolean gzipCompressedStream) {

		try {
			// connect to the Worker registry
			Registry workerRegistry;
			Task returnTask = acquiredTasks.get(getIndexOfTask(taskID));
			String requestingUUID = returnTask.getUuid();
			String fileName = returnTask.getRange();

			// find the corresponding Worker in registered list, and its ip and port number
			for (int i = 0; i <= workerNodes.size() - 1; i++) {
				if (workerNodes.get(i).getUUID().equals(requestingUUID)) {
					workerRegistry = LocateRegistry.getRegistry(workerNodes.get(i).getIP(),
							Integer.parseInt(workerNodes.get(i).getPort()));
					workerCommon = (WorkerInt) workerRegistry.lookup("WorkerRMI");
					break;
				}
			}

			InputStream istream;

			// wrap RemoteInputStream as InputStream (all compression issues are dealt with
			// in the wrapper code)
			if (gzipCompressedStream) {
				istream = RemoteInputStreamClient.wrap(workerCommon.sendFile(fileName, true));
			} else {
				istream = RemoteInputStreamClient.wrap(workerCommon.sendFile(fileName, false));
			}
			// BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
			DiskIO.copyInputStreamToFile(istream, new File(baseDirectory + jobID + "/" + fileName + ".jgf"));
		} catch (IOException | NotBoundException e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javagrid.interfaces.TaskSpaceInt#amalgamateResults(javagrid.interfaces.
	 * AmalgamateInt)
	 */
	public void amalgamateResults(AmalgamateInt amalgamate) throws RemoteException {

		if (amalgmateStarted == false) {
			amalgmateStarted = true;
			amalgamate.amalgamateInitialise(outputDestination, baseDirectory + jobID,
					baseDirectory + jobID + "/" + jobID + "_Amalgamated.txt");
		}
	}

}
