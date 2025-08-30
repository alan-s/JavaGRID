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
import java.util.ResourceBundle;

import org.hyperic.sigar.CpuPerc;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;

/**
 * The controller responsible for accepting and displaying values related to a Master and Worker node's specification.
 */
public class NodeDialogController implements Initializable{

	@FXML
	private Accordion acNode;
	@FXML
	private Label lblArch;
	@FXML
	private Label lblOSName;
	@FXML
	private Label lblOSversion;
	@FXML
	private Label lblJREvendor;
	@FXML
	private Label lblJREversion;
	@FXML
	private Label lblCPUvendor;
	@FXML
	private Label lblCPUmodel;
	@FXML
	private Label lblCPUclockSpeed;
	@FXML
	private Label lblCPUcores;
	@FXML
	private Label lblSystemMemory;
	@FXML
	private Label lblJVMmaxMemory;
	@FXML
	private Label lblFreeDisk;
	@FXML
	private Label lblUserCPU;
	@FXML
	private Label lblSysCPU;
	@FXML
	private Label lblIdleCPU;
	@FXML
	private Label lblCombCPU;
	@FXML
	private Label lblJVMtotal;
	@FXML
	private Label lblJVMused;
	@FXML
	private Label lblJVMfree;
	@FXML
	private Label lblJVMcpu;
	@FXML
	private Label lblJVMPerc;
	@FXML
	private LineChart<Number, Number> lcHeartbeat;

	private Series<Number, Number> idleCPUseries;
	private Series<Number, Number> userCPUseries;
	private Series<Number, Number> sysCPUseries;
	private Series<Number, Number> combCPUseries;
	private Series<Number, Number> jvmCPUseries;
	private Series<Number, Number> jvmMemUsedSeries;

	private static int MAX_DATA_POINTS = 20;
	private int tick = 0;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		acNode.setExpandedPane(acNode.getPanes().get(1));
		setChart();
	}

	@SuppressWarnings("unchecked")
	private void setChart(){

		idleCPUseries = new Series<Number, Number>();
		userCPUseries = new Series<Number, Number>();
		sysCPUseries = new Series<Number, Number>();
		combCPUseries = new Series<Number, Number>();
		jvmCPUseries = new Series<Number, Number>();
		jvmMemUsedSeries = new Series<Number, Number>();

		idleCPUseries.setName("Idle CPU");
		userCPUseries.setName("User CPU");
		sysCPUseries.setName("System CPU");
		combCPUseries.setName("Combined CPU");
		jvmCPUseries.setName("JVM CPU");
		jvmMemUsedSeries.setName("JVM Mem. Used");

		lcHeartbeat.getXAxis().setAutoRanging(true);
		lcHeartbeat.getYAxis().setAutoRanging(true);

		lcHeartbeat.getXAxis().setTickLabelsVisible(false);
		lcHeartbeat.getXAxis().setOpacity(0);

		lcHeartbeat.getData().addAll(idleCPUseries, userCPUseries, sysCPUseries, combCPUseries, jvmCPUseries, jvmMemUsedSeries);

	}

	public void updateStats(Number jvmTotalMemory, Number jvmUsed, Number jvmFree, Number userCPU, Number systemCPU,
			Number idleCPU, Number combinedCPU, Number jvmCPU){

		setLblUserCPU(CpuPerc.format((Double)userCPU));
		setLblSysCPU(CpuPerc.format((Double)systemCPU));
		setLblIdleCPU(CpuPerc.format((Double)idleCPU));
		setLblCombCPU(CpuPerc.format((Double)combinedCPU));
		setLblJVMcpu(CpuPerc.format((Double)jvmCPU));


		setLblJVMtotal(String.valueOf(jvmTotalMemory));
		setLblJVMused(String.valueOf(jvmUsed));
		setLblJVMfree(String.valueOf(jvmFree));
		setLblJVMPerc(CpuPerc.format((jvmUsed.doubleValue() / jvmTotalMemory.doubleValue())));

		idleCPUseries.getData().add(new Data<Number, Number>(tick, idleCPU.doubleValue() * 100));
		userCPUseries.getData().add(new Data<Number, Number>(tick, userCPU.doubleValue() * 100));
		sysCPUseries.getData().add(new Data<Number, Number>(tick, systemCPU.doubleValue() * 100));
		combCPUseries.getData().add(new Data<Number, Number>(tick, combinedCPU.doubleValue() * 100));
		jvmCPUseries.getData().add(new Data<Number, Number>(tick, jvmCPU.doubleValue() * 100));
		jvmMemUsedSeries.getData().add(new Data<Number, Number>(tick, (jvmUsed.doubleValue() / jvmTotalMemory.doubleValue()) * 100));


		tick++;

        if(tick % MAX_DATA_POINTS == 0) {
			idleCPUseries.getData().remove(0, 2);
			userCPUseries.getData().remove(0, 2);
			sysCPUseries.getData().remove(0, 2);
			combCPUseries.getData().remove(0, 2);
			jvmCPUseries.getData().remove(0, 2);
			jvmMemUsedSeries.getData().remove(0, 2);

			((ValueAxis<Number>) lcHeartbeat.getXAxis()).setLowerBound((idleCPUseries.getData().get(0).getXValue().doubleValue()));
            ((ValueAxis<Number>) lcHeartbeat.getXAxis()).setUpperBound(idleCPUseries.getData().get(idleCPUseries.getData().size()-1).getXValue().doubleValue());
        }

	}

	public void setLblArch(String value) {
		lblArch.setText(value);
	}
	public void setLblOSName(String value) {
		lblOSName.setText(value);
	}
	public void setLblOSversion(String value) {
		lblOSversion.setText(value);
	}
	public void setLblJREvendor(String value) {
		lblJREvendor.setText(value);
	}
	public void setLblJREversion(String value) {
		lblJREversion.setText(value);
	}
	public void setLblCPUvendor(String value) {
		lblCPUvendor.setText(value);
	}
	public void setLblCPUmodel(String value) {
		lblCPUmodel.setText(value);
	}
	public void setLblCPUclockSpeed(String value) {
		lblCPUclockSpeed.setText(value);
	}
	public void setLblCPUcores(String value) {
		lblCPUcores.setText(value);
	}
	public void setLblSystemMemory(String value) {
		lblSystemMemory.setText(value);
	}
	public void setLblJVMmaxMemory(String value) {
		lblJVMmaxMemory.setText(value);
	}
	public void setLblFreeDisk(String value) {
		lblFreeDisk.setText(value);
	}
	public void setLblUserCPU(String value) {
		this.lblUserCPU.setText(value);
	}
	public void setLblSysCPU(String value) {
		this.lblSysCPU.setText(value);
	}
	public void setLblIdleCPU(String value) {
		this.lblIdleCPU.setText(value);
	}
	public void setLblCombCPU(String value) {
		this.lblCombCPU.setText(value);
	}
	public void setLblJVMtotal(String value) {
		this.lblJVMtotal.setText(value);
	}
	public void setLblJVMused(String value) {
		this.lblJVMused.setText(value);
	}
	public void setLblJVMfree(String value) {
		this.lblJVMfree.setText(value);
	}
	public void setLblJVMcpu(String value) {
		this.lblJVMcpu.setText(value);
	}
	public void setLblJVMPerc(String value) {
		this.lblJVMPerc.setText(value);
	}

}
