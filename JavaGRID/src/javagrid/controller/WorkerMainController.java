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
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javagrid.common.ExceptionDialogWithPane;
import javagrid.worker.WorkerMainApp;

/**
 * The JavaFX controller, linked to FXML file in {javagrid.view} package
 */
public class WorkerMainController implements Initializable{

	@FXML
	private Label lblHostname;
	@FXML
	private Label lblIP;
	@FXML
	private Label lblUUID;
	@FXML
	private Label lblTaskSpaceIP;
	@FXML
	private Label lblStatus;
	@FXML
	private Label lblMasterIP;
	@FXML
	private Label lblStatusMaster;
	@FXML
	private Label lblCPU;
	@FXML
	private Label lblJVMcpu;
	@FXML
	private Label lblJVMmem;
	@FXML
	private Label lblGrabbed;
	@FXML
	private Label lblReturned;
	@FXML
	private Label lblRate;
	@FXML
	private Label lblJobID;
	@FXML
	private Label lblCurrent;
	@FXML
	private PasswordField pfToken;
	@FXML
	private ChoiceBox<String> cbPriority;
	@FXML
	private ProgressIndicator piJob;
	@FXML
	private Button btnUpdate;


	// Reference to the main application.
    private WorkerMainApp workerApp;
    public void setMainApp(WorkerMainApp workerApp) {
        this.workerApp = workerApp;
    }


	@Override
	public void initialize(URL location, ResourceBundle resources) {

		cbPriority.setItems(FXCollections.observableArrayList("MIN", "MAX", "NORM"));
		setCbPriority(2);

		cbPriority.getSelectionModel().selectedIndexProperty()
        .addListener(new ChangeListener<Number>() {
          public void changed(ObservableValue ov, Number value, Number new_value) {
        	  workerApp.setJVMpriority(new_value.intValue());

          }
        });

		btnUpdate.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	        	workerApp.saveWorkerConfig();
	        	workerApp.loadWorkerConfig();
	        	try {
					workerApp.connection();
				} catch (InterruptedException ex) {
					ExceptionDialogWithPane edp = new ExceptionDialogWithPane("btnUpdate", "MasterMainController", ex);
					try {
						edp.start(WorkerMainApp.getPrimaryStage());
					} catch (Exception ex1) {
						ex1.printStackTrace();
					}
				}
	        }
	    });


		pfToken.textProperty().addListener(new ChangeListener<String>() {
			    @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			    	workerApp.tokenChanged = true;
			    }
			});

	}

	public boolean authError(String source){

		if (source.equals("TaskSpace")){
			warnErrorDialogBox(AlertType.ERROR, "Authentication Error", "The supplied authentication token was rejected by the TaskSpace", "");
		}else if (source.equals("Master")){
			warnErrorDialogBox(AlertType.ERROR, "Authentication Error", "The supplied authentication token was rejected by the Master", "");
			}
		return true;
	}

	private boolean warnErrorDialogBox(AlertType alertParam, String title, String header, String content){
		Alert alert = new Alert(alertParam);
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


	public String getPfToken() {
		return pfToken.getText();
	}
	public void setPfToken(String pfToken) {
		this.pfToken.setText(pfToken);
	}
	public int getCbPriority() {
		return cbPriority.getSelectionModel().selectedIndexProperty().getValue();
	}
	public void setCbPriority(int cbPriority) {
		this.cbPriority.getSelectionModel().select(cbPriority);
	}
	public void setLblHostname(String lblHostname) {
		this.lblHostname.setText(lblHostname);
	}
	public void setLblIP(String lblIP) {
		this.lblIP.setText(lblIP);
	}
	public void setLblUUID(String lblUUID) {
		this.lblUUID.setText(lblUUID);
	}
	public void setLblTaskSpaceIP(String lblTaskSpaceIP) {
		this.lblTaskSpaceIP.setText(lblTaskSpaceIP);
	}
	public void setLblMasterIP(String lblMasterIP) {
		this.lblMasterIP.setText(lblMasterIP);
	}
	public void setLblStatusMaster(String lblStatusMaster) {
		this.lblStatusMaster.setText(lblStatusMaster);
	}
	public void setLblCPU(String lblCPU) {
		this.lblCPU.setText(lblCPU);
	}
	public void setLblJVMcpu(String lblJVMcpu) {
		this.lblJVMcpu.setText(lblJVMcpu);
	}
	public void setLblJVMmem(String lblJVMmem) {
		this.lblJVMmem.setText(lblJVMmem);
	}
	public void setLblGrabbed(String lblGrabbed) {
		this.lblGrabbed.setText(lblGrabbed);
	}
	public void setLblReturned(String lblReturned) {
		this.lblReturned.setText(lblReturned);
	}
	public void setLblRate(String lblRate) {
		this.lblRate.setText(lblRate);
	}
	public void setLblJobID(String lblJobID) {
		this.lblJobID.setText(lblJobID);
	}
	public void setPiJob(boolean state) {
		this.piJob.setVisible(state);
	}
	public void setBtnUpdate(boolean state) {
		this.btnUpdate.setDisable(state);
	}
	public void setBtnUpdateVisible(boolean state) {
		this.btnUpdate.setVisible(state);
	}
	public void setLblStatus(String lblStatus) {
		this.lblStatus.setText(lblStatus);
	}
	public void setLblCurrent(String lblCurrent) {
		this.lblCurrent.setText(lblCurrent);
	}

}
