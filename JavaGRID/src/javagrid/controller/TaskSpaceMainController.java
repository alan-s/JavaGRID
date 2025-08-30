/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.controller;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javagrid.common.DataResultUI;
import javagrid.common.MasterNode;
import javagrid.common.Node;
import javagrid.common.TaskUI;
import javagrid.common.WorkerNode;
import javagrid.taskspace.NodeDialog;
import javagrid.taskspace.TaskSpaceMainApp;

/**
 * The JavaFX controller, linked to FXML file in {javagrid.view} package
 */
public class TaskSpaceMainController implements Initializable{

	@FXML
	private Label lblHostname;
	@FXML
	private Label lblIP;
	@FXML
	private Label lblPrimary;
	@FXML
	private TreeTableView<MasterNode> ttvMasters;
	@FXML
	private TreeTableColumn<MasterNode, String> ttcMasterNode;
	@FXML
	private TreeTableColumn<MasterNode, String> ttcMasterIP;
	@FXML
	private TreeTableColumn<MasterNode, String> ttcMasterUUID;
	@FXML
	private TreeTableColumn<MasterNode, String> ttcMasterStatus;
	@FXML
	private TreeTableColumn<MasterNode, String> ttcMasterJob;
	@FXML
	private TreeTableView<WorkerNode> ttvWorkers;
	@FXML
	private TreeTableColumn<WorkerNode, String> ttcWorkerNode;
	@FXML
	private TreeTableColumn<WorkerNode, String> ttcWorkerIP;
	@FXML
	private TreeTableColumn<WorkerNode, String> ttcWorkerUUID;
	@FXML
	private TreeTableColumn<WorkerNode, String> ttcWorkerStatus;
	@FXML
	private TreeTableColumn<WorkerNode, String> ttcWorkerTasksExec;
	@FXML
	private TableView<TaskUI> tvAvailable;
	@FXML
	private TableColumn<TaskUI, String> tcTaskIdAvailable;
	@FXML
	private TableColumn<TaskUI, String> tcTaskValueAvailable;
	@FXML
	private TableView<TaskUI> tvAcquired;
	@FXML
	private TableColumn<TaskUI, String> tcTaskIdAcquired;
	@FXML
	private TableColumn<TaskUI, String> tcTaskValueAcquired;
	@FXML
	private TableColumn<TaskUI, String> tcWorkerAcquired;
	@FXML
	private TableColumn<TaskUI, String> tcStartAcquired;
	@FXML
	private TableColumn<TaskUI, String> tcDurationAcquired;
	@FXML
	private TableColumn<TaskUI, String> tcStatusAcquired;
	@FXML
	private TableView<DataResultUI> tvData;
	@FXML
	private TableColumn<DataResultUI, String> tcTaskIdData;
	@FXML
	private TableColumn<DataResultUI, String> tcTaskValueData;
	@FXML
	private TableView<DataResultUI> tvResult;
	@FXML
	private TableColumn<DataResultUI, String> tcTaskIdResult;
	@FXML
	private TableColumn<DataResultUI, String> tcTaskValueResult;
	@FXML
	private Label lblMasterCount;
	@FXML
	private Label lblWorkerCount;
	@FXML
	private Label lblAvailableCount;
	@FXML
	private Label lblAcquiredCount;
	@FXML
	private Label lblDataCount;
	@FXML
	private Label lblResultCount;

	private TreeItem<MasterNode> mastersRoot;
	private TreeItem<WorkerNode> workersRoot;

	private ObservableList<TaskUI> availableList;
	private ObservableList<TaskUI> acquiredList;
	private ObservableList<DataResultUI> dataList;
	private ObservableList<DataResultUI> resultList;

	public ArrayList<NodeDialog> dialogs = new ArrayList<NodeDialog>();

	// Reference to the main application.
    private TaskSpaceMainApp taskApp;
    public void setMainApp(TaskSpaceMainApp taskApp) {
        this.taskApp = taskApp;
    }

	public TaskSpaceMainController(){

        //Creating the root element
        mastersRoot = new TreeItem<>(new MasterNode("root", "root", "", 0, "", ""));
        mastersRoot.setExpanded(false);

		workersRoot = new TreeItem<>(new WorkerNode("root", "root", "", 0, "", 0));
        workersRoot.setExpanded(false);

        availableList = FXCollections.observableArrayList();
        acquiredList = FXCollections.observableArrayList();
        dataList = FXCollections.observableArrayList();
        resultList = FXCollections.observableArrayList();

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

        //Defining cell content for masters
        ttcMasterNode.setCellValueFactory( param -> param.getValue().getValue().hostnameProperty());
        ttcMasterIP.setCellValueFactory( param -> param.getValue().getValue().ipPortProperty());
        ttcMasterUUID.setCellValueFactory( param -> param.getValue().getValue().uuidProperty());
        ttcMasterStatus.setCellValueFactory( param -> param.getValue().getValue().statusProperty());
        ttcMasterJob.setCellValueFactory( param -> param.getValue().getValue().jobStatusProperty());

        //Creating a tree table view
        ttvMasters.setRoot(mastersRoot);
        ttvMasters.setShowRoot(false);

        //Defining cell content for workers
        ttcWorkerNode.setCellValueFactory( param -> param.getValue().getValue().hostnameProperty());
        ttcWorkerIP.setCellValueFactory( param -> param.getValue().getValue().ipPortProperty());
        ttcWorkerUUID.setCellValueFactory( param -> param.getValue().getValue().uuidProperty());
        ttcWorkerStatus.setCellValueFactory( param -> param.getValue().getValue().statusProperty());
        ttcWorkerTasksExec.setCellValueFactory( param -> param.getValue().getValue().tasksExecutedProperty());

        //Creating a tree table view
        ttvWorkers.setRoot(workersRoot);
        ttvWorkers.setShowRoot(false);

        //available queue
    	tcTaskIdAvailable.setCellValueFactory(new PropertyValueFactory<TaskUI, String>("key"));
    	tcTaskValueAvailable.setCellValueFactory(new PropertyValueFactory<TaskUI, String>("range"));

    	//acquired queue
    	tcTaskIdAcquired.setCellValueFactory(new PropertyValueFactory<TaskUI, String>("key"));
    	tcTaskValueAcquired.setCellValueFactory(new PropertyValueFactory<TaskUI, String>("range"));
    	tcWorkerAcquired.setCellValueFactory(new PropertyValueFactory<TaskUI, String>("uuid"));
    	tcStartAcquired.setCellValueFactory(new PropertyValueFactory<TaskUI, String>("startTime"));
    	tcDurationAcquired.setCellValueFactory(new PropertyValueFactory<TaskUI, String>("taskDuration"));
    	tcStatusAcquired.setCellValueFactory(new PropertyValueFactory<TaskUI, String>("status"));

    	tcStatusAcquired.setCellFactory(column -> {
    	    return new TableCell<TaskUI, String>() {
    	        @Override
    	        protected void updateItem(String item, boolean empty) {
    	            super.updateItem(item, empty);

    	            if (item == null || empty) {
    	                setText(null);
    	                setStyle("");
    	            } else {
    	            	setText(item);
    	                if (item.equals("Failed")) {
    	                    setStyle("-fx-background-color: red");
    	                } else if (item.equals("Returned")){
    	                    setStyle("-fx-background-color: green");
    	                }else if (item.equals("Terminated")){
    	                    setStyle("-fx-background-color: orange");
    	                } else if (item.equals("Timed Out")){
    	                    setStyle("-fx-background-color: orange");
    	                }else{
    	                    setStyle("");
    	                }
    	            }
    	        }
    	    };
    	});

    	//data space
    	tcTaskIdData.setCellValueFactory(new PropertyValueFactory<DataResultUI, String>("keyIndex"));
    	tcTaskValueData.setCellValueFactory(new PropertyValueFactory<DataResultUI, String>("visualValue"));

    	//results space
    	tcTaskIdResult.setCellValueFactory(new PropertyValueFactory<DataResultUI, String>("keyIndex"));
    	tcTaskValueResult.setCellValueFactory(new PropertyValueFactory<DataResultUI, String>("visualValue"));

    	tvAvailable.setItems(availableList);
    	tvAcquired.setItems(acquiredList);
    	tvData.setItems(dataList);
    	tvResult.setItems(resultList);

    	ttvMasters.setOnMousePressed(new EventHandler<MouseEvent>() {
    	    @Override
    	    public void handle(MouseEvent event) {
    	        if (event.isPrimaryButtonDown() && event.getClickCount() == 2 && ttvMasters.getSelectionModel().getSelectedItems().size() > 0) {
    	        	launchNodeDialog(ttvMasters.getSelectionModel().getSelectedItem().getValue());
    	        }
    	    }
    	});

    	ttvWorkers.setOnMousePressed(new EventHandler<MouseEvent>() {
    	    @Override
    	    public void handle(MouseEvent event) {
    	        if (event.isPrimaryButtonDown() && event.getClickCount() == 2 && ttvWorkers.getSelectionModel().getSelectedItems().size() > 0) {
    	        	launchNodeDialog(ttvWorkers.getSelectionModel().getSelectedItem().getValue());
    	        }
    	    }
    	});

	}


    ////////////////////////////////////////////////////////
    //	masters tree

    public void addMasterToTree(MasterNode mNode){

		 //Creating new tree item
        TreeItem<MasterNode> masterNode = new TreeItem<>(mNode);
        masterNode.setExpanded(true);

        mastersRoot.getChildren().add(masterNode);
        setLblMasterCount(formatNumber(taskApp.masterNodes.size()));
    }

    public void removeMasterFromTree(String uuid){

    	for (int i = 0; i <= mastersRoot.getChildren().size() - 1; i++){
    		if (mastersRoot.getChildren().get(i).getValue().getUUID().equals(uuid) ){
    			mastersRoot.getChildren().remove(i);

    			break;
    		}
    	}
    	setLblMasterCount(formatNumber(taskApp.masterNodes.size()));

    }

    public void removeAllMasters(){

    	taskApp.masterNodes.clear();
    	mastersRoot.getChildren().clear();
    	setLblMasterCount(formatNumber(taskApp.masterNodes.size()));
    }


    ////////////////////////////////////////////////////////
    //	workers tree

    public void addWorkerToTree(WorkerNode wNode){

		 //Creating new tree item
        TreeItem<WorkerNode> workerNode = new TreeItem<>(wNode);
        workerNode.setExpanded(true);

        workersRoot.getChildren().add(workerNode);

        setLblWorkerCount(formatNumber(taskApp.workerNodes.size()));
    }

    public void removeWorkerFromTree(String uuid){

    	for (int i = 0; i <= workersRoot.getChildren().size() - 1; i++){
    		if (workersRoot.getChildren().get(i).getValue().getUUID().equals(uuid) ){
    			workersRoot.getChildren().remove(i);
    			break;
    		}

    	}
    	setLblWorkerCount(formatNumber(taskApp.workerNodes.size()));
    }

    public void removeAllWorkers(){

    	taskApp.workerNodes.clear();
    	workersRoot.getChildren().clear();
    	setLblWorkerCount(formatNumber(taskApp.workerNodes.size()));
    }


    ////////////////////////////////////////////////////////
    //	available tasks queue

    public void addTaskAvailableQueue(TaskUI theTaskUI){

    	availableList.add(theTaskUI);

    	tvAvailable.getSortOrder().add(tcTaskIdAvailable);
    	tcTaskIdAvailable.setSortType(SortType.ASCENDING);
    	tcTaskIdAvailable.setSortable(true);
    	tcTaskIdAvailable.setSortable(false);

    	setLblAvailableCount(formatNumber(availableList.size()));
    }

    public void removeTaskAvailableQueue(int index){

    	availableList.remove(index);
    	setLblAvailableCount(formatNumber(availableList.size()));
    }

    public void removeAllTasksAvailableQueue(){

    	availableList.clear();
    	setLblAvailableCount(formatNumber(availableList.size()));
    }


    ////////////////////////////////////////////////////////
    //	acquired tasks queue

    public void addTaskAcquiredQueue(TaskUI theTaskUI){

    	acquiredList.add(theTaskUI);
    	setLblAcquiredCount(formatNumber(acquiredList.size()));

    }

    public void setStatusAcquiredQueue(int index, int status){

    	if(index <= acquiredList.size() - 1){
    		acquiredList.get(index).setStatus(status);
        	Refresh(tvAcquired);
    	}

    }

    public void setDurationAcquiredQueue(int index, double duration){

    	acquiredList.get(index).setTaskDuration(duration);
    	Refresh(tvAcquired);

    }

    public void removeAllTasksAcquiredQueue(){

    	acquiredList.clear();
    	setLblAcquiredCount(formatNumber(acquiredList.size()));
    }

    public static <T> void Refresh(final TableView<T> table) {

	      Platform.runLater(new Runnable() {
	          @Override
	          public void run() {
	              table.getColumns().get(0).setVisible(false);
	              table.getColumns().get(0).setVisible(true);
	          }
	      });
}


    ////////////////////////////////////////////////////////
    //	data space

    public void addInputDataSpace(DataResultUI dataResultUI){

    	dataList.add(dataResultUI);
    	setLblDataCount(formatNumber(dataList.size()));
    }

    public void removeInputDataSpace(int index){

    	setLblDataCount(formatNumber(dataList.size()));
    }

    public void removeAllInputsDataSpace(){

    	dataList.clear();
    	setLblDataCount(formatNumber(dataList.size()));
    }

    ////////////////////////////////////////////////////////
    //	result space

    public void addOutputResultSpace(DataResultUI dataResultUI){

    	resultList.add(dataResultUI);
    	setLblResultCount(formatNumber(resultList.size()));
    }

    public void removeOutputResultSpace(int index){

    	setLblResultCount(formatNumber(resultList.size()));
    }

    public void removeAllOutputsResultSpace(){

    	resultList.clear();
    	setLblResultCount(formatNumber(resultList.size()));
    }


	private String formatNumber(Number num){
		return NumberFormat.getNumberInstance(Locale.UK).format(num);
	}

	private void launchNodeDialog(Node item) {

		Node selectedNode = item;

		NodeDialog nd = new NodeDialog(selectedNode.getUUID(), selectedNode.getOsArch(), selectedNode.getOsName(), selectedNode.getOsVersion(), selectedNode.getJreVendor(),
				selectedNode.getJreVersion(), selectedNode.getCpuModel(), selectedNode.getCpuVendor(), selectedNode.getMhz(), selectedNode.getTotalCores(),
				selectedNode.getSystemMemory(), selectedNode.getJvmMaxMemory(), selectedNode.getFreeDisk());
		dialogs.add(nd);
	}

	public boolean authError(String source){

		if (source.equals("Master")){
			warnErrorDialogBox(AlertType.ERROR, "Authentication Error", "The supplied authentication token was rejected by the Master \n"
					+  "Application will now exit!", "");
		}else if (source.equals("Worker")){
			warnErrorDialogBox(AlertType.ERROR, "Authentication Error", "The supplied authentication token was rejected by a Worker\n"
					+  "Application will now exit!", "");
			}
		return true;
	}

	public boolean warnErrorDialogBox(AlertType alertParam, String title, String header, String content){
		Alert alert = new Alert(alertParam);
    	TaskSpaceMainApp.getInstance();
		alert.initOwner(TaskSpaceMainApp.getPrimaryStage());
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


	public void setLblHostname(String hostname) {
		lblHostname.setText(hostname);
	}
	public void setLblIP(String ip) {
		lblIP.setText(ip);
	}
	public void setLblPrimary(String primary){
		lblPrimary.setText(primary);
	}
	public void setLblMasterCount(String masters){
		lblMasterCount.setText(masters);
	}
	public void setLblWorkerCount(String workers){
		lblWorkerCount.setText(workers);
	}
	public void setLblAvailableCount(String avaialble){
		lblAvailableCount.setText(avaialble);
	}
	public void setLblAcquiredCount(String acquired){
		lblAcquiredCount.setText(acquired);
	}
	public void setLblDataCount(String data){
		lblDataCount.setText(data);
	}
	public void setLblResultCount(String result){
		lblResultCount.setText(result);
	}

}
