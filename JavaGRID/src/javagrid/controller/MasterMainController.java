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

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javagrid.common.ExceptionDialogWithPane;
import javagrid.common.Task;
import javagrid.enumeration.Notification;
import javagrid.master.MasterMainApp;


/**
 * The JavaFX controller, linked to FXML file in {javagrid.view} package
 */
public class MasterMainController implements Initializable{

	@FXML
	private TabPane tpMaster;
	@FXML
	private Tab tabJob;
	@FXML
	private Tab tabData;
	@FXML
	private Tab tabWorker;
	@FXML
	private Tab tabAmalgamated;
	@FXML
	private Tab tabScheduling;
	@FXML
	private Label lblHostname;
	@FXML
	private Label lblIP;
	@FXML
	private Label lblUUID;
	@FXML
	private Label lblTaskSpaceIP;
	@FXML
	private Label lblRecruited;
	@FXML
	private Label lblCPU;
	@FXML
	private Label lblRAM;
	@FXML
	private Label lblStatus;
	@FXML
	private Label lblJobID;
	@FXML
	private Label lblStart;
	@FXML
	private Label lblEnd;
	@FXML
	private Label lblDur;
	@FXML
	private Button btnExecute;
	@FXML
	private Button btnPause;
	@FXML
	private Button btnAbort;
	@FXML
	private Button btnFileOpen;
	@FXML
	private Button btnImportSpace;
	@FXML
	private ProgressIndicator piImport;
	@FXML
	private ProgressBar pbProgress;
	@FXML
	private ToggleGroup dataInputToggleGroup;
	@FXML
	private Label lblInputFile;
	@FXML
	private TextField tfJobID;
	@FXML
	RadioButton rbAmalDisk;
	@FXML
	private Button btnAmalgamatedOpen;
	@FXML
	private ToggleGroup resultOutputToggleGroup;
	@FXML
	private Label lblAmalgamatedFile;
	@FXML
	private ToggleGroup algorithmToggleGroup;
	@FXML
	private RadioButton rbRuntime;
	@FXML
	private RadioButton rbDataSpace;
	@FXML
	private RadioButton rbInputFile;
	@FXML
	private RadioButton rbResultSpace;
	@FXML
	private Slider slPulse;
	@FXML
	private Label lblPulse;
	@FXML
	private RadioButton rbDisk;
	@FXML
	private RadioButton rbStatic;
	@FXML
	RadioButton rbPriori;
	@FXML
	private RadioButton rbRTS;
	@FXML
	private ImageView ivJobOutcome;
	@FXML
	private CheckBox cbRange;
	@FXML
	private TextField tfIndexStart;
	@FXML
	private TextField tfIndexEnd;
	@FXML
	private TextField tfGranularity;
	@FXML
	private CheckBox cbTaskTimeout;
	@FXML
	private TextField tfTaskTimeout;
	@FXML
	private RadioButton rbSimple;
	@FXML
	private RadioButton rbAdvanced;
	@FXML
	private ToggleGroup modeToggleGroup;
	@FXML
	private Tab tabWorkerResult;
	@FXML
	private CheckBox cbCompressFiles;
	@FXML
	private CheckBox cbCompressStreams;


	private FileChooser fileChooser = new FileChooser();
	private FileChooser.ExtensionFilter extFilter;


	private boolean fileImportedUploaded;
	private File dataInput;
	public File amalgamatedOutput;


	public MasterMainController(){
	}

	// Reference to the main application.
    private MasterMainApp masterApp;
    public void setMainApp(MasterMainApp masterApp) {
        this.masterApp = masterApp;
    }


	@Override
	public void initialize(URL location, ResourceBundle resources) {

		btnExecute.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {

	        	try {

	        		if (validationPassed() == true){

	        			if (pbProgress.getProgress() == 0.0 || pbProgress.getProgress() == 1.0f){
			        		validationPassed();
			        		masterApp.saveJobConfig();

			        		if (getCbRange() == true){
			        			masterApp.addFirstTask(new Task(Integer.parseInt(getTfIndexStart()),
			        					Integer.parseInt(getTfIndexStart()) + Integer.parseInt(getTfGranularity()), false));
			        		}else{
			        			masterApp.addFirstTask(new Task(0, 0 + Integer.parseInt(getTfGranularity()), false));
			        		}

			        		masterApp.paused = false;
							setPbProgress(-1.0f);
							disableTabs(true);
							btnExecute.setDisable(true);
							btnPause.setDisable(false);
							btnAbort.setDisable(false);
							setIvJobOutcomeHide();
		        		}else if (pbProgress.getProgress() == 0.5f){
		        			masterApp.notifyTaskSpace(Notification.RESUME, "");

		        			setPbProgress(-1.0f);
		        			masterApp.paused = false;
							btnExecute.setDisable(true);
							btnPause.setDisable(false);
							btnAbort.setDisable(false);
		        		}
		        		masterApp.notifyTaskSpace(Notification.EXECUTE, masterApp.uuid);
	        		}
				} catch (RemoteException ex) {
					ExceptionDialogWithPane edp = new ExceptionDialogWithPane("btnExecute", "MasterMainController", ex);
					try {
						edp.start(MasterMainApp.getPrimaryStage());
					} catch (Exception ex1) {
						ex1.printStackTrace();
					}
				}
	        }
	    });


		btnPause.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {

	        	try{
					masterApp.notifyTaskSpace(Notification.PAUSE, masterApp.uuid);

					masterApp.paused = true;
		        	setPbProgress(0.5f);
					btnExecute.setDisable(false);
					btnPause.setDisable(true);
					btnAbort.setDisable(false);
					btnExecute.setText("Resume");
	        	}catch(Exception ex){
					ExceptionDialogWithPane edp = new ExceptionDialogWithPane("btnPause", "MasterMainController", ex);
					try {
						edp.start(MasterMainApp.getPrimaryStage());
					} catch (Exception ex1) {
						ex1.printStackTrace();
					}
	        	}

	        }
	    });

		btnAbort.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {

	        	try{
		        	masterApp.notifyTaskSpace(Notification.ABORT, masterApp.uuid);

		        	masterApp.paused = false;
		        	setPbProgress(0.0f);
		        	disableTabs(false);
		        	fileImportedUploaded = false;
		        	tpMaster.setDisable(false);
					btnExecute.setDisable(false);
					btnPause.setDisable(true);
					btnAbort.setDisable(true);
					btnExecute.setText("Execute");
	        	}catch(Exception ex){
					ExceptionDialogWithPane edp = new ExceptionDialogWithPane("btnAbort", "MasterMainController", ex);
					try {
						edp.start(MasterMainApp.getPrimaryStage());
					} catch (Exception ex1) {
						ex1.printStackTrace();
					}
	        	}
	        }
	    });

		btnFileOpen.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	        	openFileDialog();
	        }
	    });

		btnAmalgamatedOpen.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	        	amalgamatedFileDialog();
	        }
	    });


		modeToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
	        public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
	            RadioButton chk = (RadioButton)t1.getToggleGroup().getSelectedToggle(); // Cast object to radio button
	            if (chk.getId().equals("rbSimple")){
	            	setRbResultSpaceDisabled(false);
	            	setRbDiskDisabled(false);
	            	setRbDisk(false);
	            	setRbAmalDisk(false);
	            }else{
	            	setRbResultSpaceDisabled(true);
	            	setRbDiskDisabled(true);
	            	setRbAmalDisk(true);
	            	}
	        }
	    });

		dataInputToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
		        public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
		            RadioButton chk = (RadioButton)t1.getToggleGroup().getSelectedToggle(); // Cast object to radio button
		            if (chk.getId().equals("rbInputFile") || chk.getId().equals("rbDataSpace")){
		            	setBtnFileOpen(false);

		            	if(!getLblInputFile().equals("-")){
		            		setBtnImportSpace(false);
		            	}
		            }
		            else{
		            	setBtnFileOpen(true);
		            	setLblInputFile("-");
		            	dataInput = null;
		            	setBtnImportSpace(true);
		            	}
		        }
		    });


		  cbRange.selectedProperty().addListener(new ChangeListener<Boolean>() {
	          public void changed(ObservableValue<? extends Boolean> ov,
	            Boolean old_val, Boolean new_val) {

	            if(cbRange.isSelected()){
	            	setDisableTfIndexStart(false);
	            	setDisableTfIndexEnd(false);
	            }else{
	            	setDisableTfIndexStart(true);
	            	setDisableTfIndexEnd(true);
	            }
	         }
	       });

		  btnImportSpace.setOnAction(new EventHandler<ActionEvent>() {
		        @Override
		        public void handle(ActionEvent e) {
		        	disableTabs(true);
		        	setPiImport(true);
		        	setBtnFileOpen(true);
		        	setBtnImportSpace(true);
		            // separate non-FX thread
		            new Thread() {
		                // runnable for that thread
		                public void run() {

		                	if (rbInputFile.isSelected() == true){
			                	masterApp.indexCounter = 0;
			                	masterApp.saveJobConfig();
	        		        	masterApp.uploadImportAsFile(dataInput);
	        		        	fileImportedUploaded = true;
		                	}else if (rbDataSpace.isSelected() == true){
			                	masterApp.indexCounter = 0;
	        		        	masterApp.requestDataSpaceCleared();
	        		        	masterApp.uploadImportToDataSpace(dataInput);
	        		        	fileImportedUploaded = true;
		                	}
		                	disableTabs(false);
		                	setPiImport(false);
        		        	setBtnFileOpen(false);
        		        	setBtnImportSpace(false);
		                }
		            }.start();

		        }
		    });

		  tfIndexStart.textProperty().addListener(new ChangeListener<String>() {
			    @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			        if (newValue.matches("\\d*")) {
			        } else {
			        	tfIndexStart.setText(oldValue);
			        }
			    }
			});

		  tfIndexStart.focusedProperty().addListener(new ChangeListener<Boolean>(){
		      @Override
		      public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		          if (newPropertyValue){}
		          else{
		        	  tfIndexStart.setText(tfIndexStart.getText().replace(" ", ""));
		              if(tfIndexStart.getText().isEmpty() ){
		            	  tfIndexStart.setText("0");
		              }
		          }
		      }
		  });

		  tfIndexEnd.textProperty().addListener(new ChangeListener<String>() {
			    @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			        if (newValue.matches("\\d*")) {
			        } else {
			        	tfIndexEnd.setText(oldValue);
			        }
			    }
			});

		  tfGranularity.textProperty().addListener(new ChangeListener<String>() {
			    @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			        if (newValue.matches("\\d*")) {
			        } else {
			        	tfGranularity.setText(oldValue);
			        }
			    }
			});

		  tfGranularity.focusedProperty().addListener(new ChangeListener<Boolean>(){
		      @Override
		      public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		          if (newPropertyValue){}
		          else{
		        	  tfGranularity.setText(tfGranularity.getText().replace(" ", ""));
		              if(tfGranularity.getText().isEmpty() ){
		            	  tfGranularity.setText("1000000");
		              }
		          }
		      }
		  });

		  cbTaskTimeout.selectedProperty().addListener(new ChangeListener<Boolean>() {
			    @Override
			    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			        if(tfTaskTimeout.isDisabled() == true){
			        	setDisableTfTaskTimeout(false);
			        }else{
			        	setDisableTfTaskTimeout(true);
			        }

			    }
			});

		  tfTaskTimeout.textProperty().addListener(new ChangeListener<String>() {
			    @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			        if (newValue.matches("\\d*")) {
			        } else {
			        	tfTaskTimeout.setText(oldValue);
			        }
			    }
			});

		  tfTaskTimeout.focusedProperty().addListener(new ChangeListener<Boolean>(){
		      @Override
		      public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		          if (newPropertyValue){}
		          else{
		        	  tfTaskTimeout.setText(tfTaskTimeout.getText().replace(" ", ""));
		              if(tfTaskTimeout.getText().isEmpty() ){
		            	  tfTaskTimeout.setText("30");
		              }
		          }
		      }
		  });

		  tfJobID.focusedProperty().addListener(new ChangeListener<Boolean>(){
		      @Override
		      public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		          if (newPropertyValue){}
		          else{
		        	  setTfJobID(getTfJobID().trim());
		              if(getTfJobID().isEmpty()){
		            	  setTfJobID("unknown");
		              }
		          }
		      }
		  });


		  slPulse.valueProperty().addListener((observable, oldValue, newValue) -> {
			    if(newValue.intValue() == 0){
			    	setSlPulse(1);
			    	setLblPulse("1");
			    }else{
			    	setLblPulse(String.valueOf(newValue.intValue()));
			    }

			});

	}

	public void disableTabs(boolean state){
		tabJob.setDisable(state);
		tabData.setDisable(state);
		tabWorker.setDisable(state);
		tabAmalgamated.setDisable(state);
		tabScheduling.setDisable(state);
	}

	public void openFileDialog(){
		File selectedFile;
		fileChooser = new FileChooser();
		fileChooser.setTitle("Select Data Input File...");
		extFilter = new FileChooser.ExtensionFilter("Text (*.txt)", "*.txt");
		fileChooser.getExtensionFilters().add(extFilter);
		extFilter = new FileChooser.ExtensionFilter("Comma Seperated Values (*.csv)", "*.csv");
		fileChooser.getExtensionFilters().add(extFilter);
		File dir = new File(System.getProperty("user.home"));
        if(dir.canRead())
        	fileChooser.setInitialDirectory(dir);
        else fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		MasterMainApp.getInstance();
		selectedFile = fileChooser.showOpenDialog(MasterMainApp.getPrimaryStage());
		if (selectedFile != null) {
			dataInput = selectedFile;
			setLblInputFile(selectedFile.getName());
			if (rbDataSpace.isSelected() == true || rbDisk.isSelected() == true){
				setBtnImportSpace(false);
			}

		}
	}

	public void amalgamatedFileDialog(){
		File selectedFile;
		fileChooser = new FileChooser();
		fileChooser.setTitle("Select amalgamated file to save to...");
		extFilter = new FileChooser.ExtensionFilter("Text (*.txt)", "*.txt");
		fileChooser.getExtensionFilters().add(extFilter);
		extFilter = new FileChooser.ExtensionFilter("Comma Seperated Values (*.csv)", "*.csv");
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setInitialFileName("Amalgamated Results");
		File dir = new File(System.getProperty("user.home"));
        if(dir.canRead())
        	fileChooser.setInitialDirectory(dir);
        else fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		MasterMainApp.getInstance();
		selectedFile = fileChooser.showSaveDialog(MasterMainApp.getPrimaryStage());
		if (selectedFile != null) {
			amalgamatedOutput = new File(selectedFile.getAbsolutePath());
			setLblAmalgamatedFile(amalgamatedOutput.getName());
		}
		//return selectedFile;
	}

	public void resetButtons(){
		btnExecute.setText("Execute");
		setBtnExecute(false);
		setBtnPause(true);
		setBtnAbort(true);
	}

	private boolean validationPassed(){

		if (getTfJobID().trim().equals("")){
			warnErrorDialogBox(AlertType.ERROR, "Validation", "A job id is required.", "");
			return false;
		}

		if ((rbDataSpace.isSelected() == true || rbInputFile.isSelected() == true) && dataInput == null){
			warnErrorDialogBox(AlertType.ERROR, "Validation", "An input file has not been selected.", "");
			return false;
		}

		if ((rbDataSpace.isSelected() == true || rbInputFile.isSelected() == true) && fileImportedUploaded == false){
			warnErrorDialogBox(AlertType.ERROR, "Validation", "The input file has not been sent to the TaskSpace.", "");
			return false;
		}

		if (getCbRange() == true && getTfIndexStart().equals("")){
			warnErrorDialogBox(AlertType.ERROR, "Validation", "Continuation index value not set.", "");
			return false;
		}


		if (amalgamatedOutput.getPath().equals("")){
			warnErrorDialogBox(AlertType.ERROR, "Validation", "An output amalgamation file has not been set.", "");
			return false;
		}

		if(rbStatic.isSelected() == true && getTfGranularity().trim().equals("")){
			warnErrorDialogBox(AlertType.ERROR, "Validation", "A granularity has not been set.", "");
			return false;
		}

		if(getCbTaskTimeout() == true && getTfTaskTimeout().trim().equals("")){
			warnErrorDialogBox(AlertType.ERROR, "Validation", "A value for 'task timeout' must be set.", "");
			return false;
		}

		return true;
	}

	public boolean authError(String source){

		if (source.equals("TaskSpace")){
			warnErrorDialogBox(AlertType.ERROR, "Authentication Error", "The supplied authentication token was rejected by the TaskSpace \n"
					+  "Application will now exit!", "");
		}else if (source.equals("Worker")){
			warnErrorDialogBox(AlertType.ERROR, "Authentication Error", "The supplied authentication token was rejected by a Worker\n"
					+  "Application will now exit!", "");
			}
		return true;
	}

	public void updateWorkersUI(String recruited, String totalcpu, String totalram){

		lblRecruited.setText(recruited);
		lblCPU.setText(totalcpu);
		lblRAM.setText(totalram);
	}


	private boolean warnErrorDialogBox(AlertType alertParam, String title, String header, String content){
		Alert alert = new Alert(alertParam);
    	MasterMainApp.getInstance();
		alert.initOwner(MasterMainApp.getPrimaryStage());
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
	public void setLblUUID(String uuid) {
		lblUUID.setText(uuid);
	}
	public void setLblTaskSpaceIP(String taskSpaceIP) {
		lblTaskSpaceIP.setText(taskSpaceIP);
	}
	public void setLblStatus(String status) {
		lblStatus.setText(status);
	}
	public void setLblJobID(String jobID){
		lblJobID.setText(jobID);
	}
	public String getLblJobID(){
		return lblJobID.getText();
	}
	public void setPbProgress(float value){
		pbProgress.setProgress(value);
	}
	public void setBtnFileOpen(boolean state){
		btnFileOpen.setDisable(state);
	}
	public void setBtnImportSpace(boolean state){
		btnImportSpace.setDisable(state);
	}
	public void setLblInputFile(String value){
		lblInputFile.setText(value);
	}
	public String getLblInputFile(){
		return lblInputFile.getText();
	}
	public void setTfJobID(String value){
		tfJobID.setText(value);
	}
	public String getTfJobID(){
		return tfJobID.getText();
	}
	public void setLblStart(String value){
		lblStart.setText(value);
	}
	public void setLblEnd(String value){
		lblEnd.setText(value);
	}
	public void setLblDur(String value){
		lblDur.setText(value);
	}
	public void setBtnAmalgamatedOpen(boolean state){
		btnAmalgamatedOpen.setDisable(state);
	}
	public void setLblAmalgamatedFile(String value){
		lblAmalgamatedFile.setText(value);
	}
	public ToggleGroup getDataInputToggleGroup(){
		return dataInputToggleGroup;
	}
	public ToggleGroup getResultOutputToggleGroup(){
		return resultOutputToggleGroup;
	}
	public ToggleGroup getAlgorthimToggleGroup(){
		return algorithmToggleGroup;
	}
	public ToggleGroup getModeToggleGroup(){
		return modeToggleGroup;
	}
	public File getInputDataFile(){
		return dataInput;
	}
	public void setInputDataFile(File input){
		dataInput = input;
	}
	public File getAmalgamatedFile(){
		return amalgamatedOutput;
	}
	public void setAmalgamatedFile(File output){
		amalgamatedOutput = output;
	}
	public void setRbRuntime(boolean state){
		rbRuntime.setSelected(state);
	}
	public void setRbDataSpace(boolean state){
		rbDataSpace.setSelected(state);
	}
	public void setRbInputFile(boolean state){
		rbInputFile.setSelected(state);
	}
	public void setRbResultSpace(boolean state){
		rbResultSpace.setSelected(state);
	}
	public void setRbDisk(boolean state){
		rbDisk.setSelected(state);
	}

	public void setRbResultSpaceDisabled(boolean state){
		rbResultSpace.setDisable(state);
	}
	public void setRbDiskDisabled(boolean state){
		rbDisk.setDisable(state);
	}
	public void setRbStatic(boolean state){
		rbStatic.setSelected(state);
	}
	public void setRbPriori(boolean state){
		rbPriori.setSelected(state);
	}
	public void setRbRTS(boolean state){
		rbRTS.setSelected(state);
	}
	public void setIvJobOutcomeSucc(){
		 final Image image = new Image("/javagrid/resources/accept_button.png");
		 ivJobOutcome.setImage(image);
		 ivJobOutcome.setVisible(true);
	}
	public void setIvJobOutcomeFail(){
		 final Image image = new Image("/javagrid/resources/cross.png");
		 ivJobOutcome.setImage(image);
		 ivJobOutcome.setVisible(true);
	}
	public void setIvJobOutcomeHide(){
		ivJobOutcome.setVisible(false);
	}
	public void setCbRange(boolean state){
		cbRange.setSelected(state);
	}
	public boolean getCbRange(){
		return cbRange.selectedProperty().getValue();
	}
	public void setTfIndexStart(String value){
		tfIndexStart.setText(value);
	}
	public void setTfIndexEnd(String value){
		tfIndexEnd.setText(value);
	}
	public String getTfIndexStart(){
		return tfIndexStart.getText();
	}
	public String getTfIndexEnd(){
		return tfIndexEnd.getText();
	}
	public void setDisableTfIndexStart(boolean value){
		tfIndexStart.setDisable(value);
	}
	public void setDisableTfIndexEnd(boolean value){
		tfIndexEnd.setDisable(value);
	}
	public void setDisableTfGranularity(boolean state){
		tfGranularity.setDisable(state);
	}
	public void setTfGranularity(String value){
		tfGranularity.setText(value);
	}
	public String getTfGranularity(){
		return tfGranularity.getText();
	}
	public void setDisableTfTaskTimeout(boolean state){
		tfTaskTimeout.setDisable(state);
	}
	public void setTfTaskTimeout(String value){
		tfTaskTimeout.setText(value);
	}
	public String getTfTaskTimeout(){
		return tfTaskTimeout.getText();
	}
	public boolean getCbTaskTimeout(){
		return cbTaskTimeout.selectedProperty().getValue();
	}
	public void setCbTaskTimeout(boolean state) {
		cbTaskTimeout.setSelected(state);
	}
	public void setRbSimple(boolean state){
		rbSimple.setSelected(state);
	}
	public void setRbAdvanced(boolean state){
		rbAdvanced.setSelected(state);
	}
	public void setBtnExecute(boolean state){
		btnExecute.setDisable(state);
	}
	public void setBtnPause(boolean state){
		btnPause.setDisable(state);
	}
	public void setBtnAbort(boolean state){
		btnAbort.setDisable(state);
	}
	public int getSlPulse() {
		return (int)slPulse.getValue();
	}
	public void setSlPulse(int slPulse) {
		this.slPulse.setValue(slPulse);
	}
	public void setPiImport(boolean state){
    	piImport.setVisible(state);
	}
	public void setLblPulse(String value){
		lblPulse.setText(value);
	}
	public boolean getCbCompressFiles(){
		return cbCompressFiles.selectedProperty().getValue();
	}
	public void setCbCompressFiles(boolean state) {
		cbCompressFiles.setSelected(state);
	}
	public boolean getCbCompressStreams(){
		return cbCompressStreams.selectedProperty().getValue();
	}
	public void setCbCompressStreams(boolean state) {
		cbCompressStreams.setSelected(state);
	}
	public void setRbAmalDisk(boolean state){
		rbAmalDisk.setDisable(state);
	}
}
