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

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javagrid.controller.NodeDialogController;

/**
 * This JavaFX application is used within the TaskSpace to visualise information about connected nodes
 */
public class NodeDialog{

	//javaFX
	private Stage stage;
	private BorderPane rootLayout;
	private FXMLLoader loader = new FXMLLoader();

	private NodeDialogController nodeDialogController;

	//node details
	private String uuid;
	private String osArch;
	private String osName;
	private String osVersion;
	private String jreVendor;
	private String jreVersion;
	private String cpuModel;
	private String cpuVendor;
	private int mhz;
	private int totalCores;
	private long systemMemory;
	private long jvmMaxMemory;
	private long freeDisk;


	/**
	 * Constructor to launch node application with following parameters
	 * 
	 * @param uuid the nodes unique id
	 * @param osArch the chipset architecture
	 * @param osName operating system name
	 * @param osVersion the OS version
	 * @param jreVendor the Java Runtime Environment (JRE) Vendor
	 * @param jreVersion the jre version
	 * @param cpuModel the cpu model
	 * @param cpuVendor the cpu vendor
	 * @param mhz the clock speed of the cpu
	 * @param totalCores the number of cores
	 * @param systemMemory the maximum system memory available
	 * @param jvmMaxMemory the maximum allocated memory to the jvm
	 * @param freeDisk the amount of free disk space
	 */
	public NodeDialog(String uuid, String osArch,
			String osName, String osVersion, String jreVendor,
			String jreVersion, String cpuModel, String cpuVendor, int mhz,
			int totalCores, long systemMemory, long jvmMaxMemory, long freeDisk) {


		this.uuid = uuid;
		this.osArch = osArch;
		this.osName = osName;
		this.osVersion = osVersion;
		this.jreVendor = jreVendor;
		this.jreVersion = jreVersion;
		this.cpuModel = cpuModel;
		this.cpuVendor = cpuVendor;
		this.mhz = mhz;
		this.totalCores = totalCores;
		this.systemMemory = systemMemory;
		this.jvmMaxMemory = jvmMaxMemory;
		this.freeDisk = freeDisk;

        initRootLayout();
        showNodeMain();
		setinformation();

	}

	/**
	 * @return an instance of this application
	 */
	public NodeDialogController getController(){
		return nodeDialogController;
	}

	/**
	 * set initial fxml file and initialise
	 */
	private void initRootLayout() {
        try {

    		stage = new Stage();
    		stage.setTitle("Node Information");
            stage.setResizable(false);

            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(getClass().getResource("/javagrid/view/NodeDialogRootLayout.fxml"));
    		stage.getIcons().add(new Image(getClass().getResourceAsStream("/javagrid/resources/javaGRIDsmall.png")));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * launch the application pane
	 */
	private void showNodeMain() {
		try {
	        // Load server main screen.
	        loader.setLocation(getClass().getResource("/javagrid/view/NodeDialogMain.fxml"));
	        AnchorPane nodeMain = (AnchorPane) loader.load();
	        rootLayout.setCenter(nodeMain);

	        // Give the controller access to the main app.
	        nodeDialogController = loader.getController();


		} catch (IOException e) {
			e.printStackTrace();
		}

    }


    /**
     * get the id of the Master/Worker this dialog is displaying
     * 
     * @return the uuid of Master/Worker
     */
    public String getUuid(){
    	return this.uuid;
    }

    /**
     * Set the values in the constructor to the controller
     */
    private void setinformation() {

	          	  nodeDialogController.setLblArch(osArch);
	          	  nodeDialogController.setLblOSName(osName);
	          	  nodeDialogController.setLblOSversion(osVersion);
	          	  nodeDialogController.setLblJREvendor(jreVendor);
	          	  nodeDialogController.setLblJREversion(jreVersion);
	          	  nodeDialogController.setLblCPUvendor(cpuVendor);
	          	  nodeDialogController.setLblCPUmodel(cpuModel);
	          	  nodeDialogController.setLblCPUclockSpeed(formatNumber(mhz));
	          	  nodeDialogController.setLblCPUcores(String.valueOf(totalCores));
	          	  nodeDialogController.setLblSystemMemory(formatNumber(systemMemory));
	          	  nodeDialogController.setLblJVMmaxMemory(formatNumber(jvmMaxMemory));
	          	  nodeDialogController.setLblFreeDisk(formatNumber(freeDisk));

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

}
