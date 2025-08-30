/*
 * javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems
 *  
 * Submitted as part of a Master's thesis in Advanced Software Engineering
 *
 * Author: Alan Suleiman - alan.suleiman@kcl.ac.uk
 * 
 * August 2015
 */
package javagrid.utils;

import java.io.File;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * The heartbeat class is used to collect information about each connecting node, including system resource usage
 * Uses Hyperic Sigar API
 */
public class Heartbeat {

	private CpuInfo[] infos;
	private CpuInfo info;
	private Mem memory;
	private Runtime runtime;

    public String osArch;
    public String osName;
    public String osVersion;
    public String jreVendor;
    public String jreVersion;

    public String cpuVendor;
    public String cpuModel;
    public int mhz;
    public int totalCores;
    public long freeDisk;

    private int mb = 1024*1024;
    public long systemMemory;
    public long jvmMaxMemory;
    public long jvmTotalMemory;
    public long jvmUsed;
    public long jvmFree;

    public double userCPU;
    public double systemCPU;
    public double idleCPU;
    public double combinedCPU;
    public double jvmCPU;

	private static final int TOTAL_TIME_UPDATE_LIMIT = 2000;

    private  Sigar sigar;
    private  int cpuCount;
    private  long pid;
    private ProcCpu prevPc;

	/**
	 * Constructor that instantiates APIs used
	 *
	 * @throws SigarException throw exception in case of failure to generate a reading
	 */
	public Heartbeat() throws SigarException{

        sigar = new Sigar();
        cpuCount = sigar.getCpuList().length;
        pid = sigar.getPid();
        prevPc = sigar.getProcCpu(pid);
        jvmCPU = 0;
        getSystemStatistics();

	}



    /**
     * method calls JRE System.getProperty()  to get the host system information
     *
     * @throws SigarException
     */
    private void getSystemStatistics() throws SigarException{

    	infos = sigar.getCpuInfoList();
    	info = infos[0];
    	memory = sigar.getMem();
    	runtime = Runtime.getRuntime();

        osArch = System.getProperty("os.arch");
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
        jreVendor = System.getProperty("java.vendor");
        jreVersion = System.getProperty("java.version");

        cpuVendor = info.getVendor();
        cpuModel =info.getModel();
        mhz = info.getMhz();
        totalCores = info.getTotalCores();
        freeDisk = (new File("C:\\").getUsableSpace() / mb) / 1024;
        systemMemory = memory.getRam();
        jvmMaxMemory = runtime.maxMemory() / mb;

    }


    /**
     * generate a pulse of real-time information, sent to the TaskSpace
     */
    public void generatePulse(){

        try {
        	//CPU information
        	CpuPerc cpu = sigar.getCpuPerc();
            userCPU = cpu.getUser();
            systemCPU = cpu.getSys();
            idleCPU = cpu.getIdle();
            combinedCPU = cpu.getCombined();

            //jvm
            jvmTotalMemory = runtime.totalMemory() / mb;
            jvmUsed = (runtime.totalMemory() - runtime.freeMemory()) / mb;
            jvmFree = runtime.freeMemory() / mb;

            ProcCpu curPc = sigar.getProcCpu(pid);
            long totalDelta = curPc.getTotal() - prevPc.getTotal();
            long timeDelta = curPc.getLastTime() - prevPc.getLastTime();
            if (totalDelta == 0) {
                if (timeDelta > TOTAL_TIME_UPDATE_LIMIT) jvmCPU = 0;
                if (jvmCPU == 0) prevPc = curPc;
            } else {
            	jvmCPU = 1.0 * totalDelta / timeDelta / cpuCount;
                prevPc = curPc;
            }
        } catch (SigarException ex) {
            throw new RuntimeException(ex);
        }
    }


	public String getOsArch() {
		return osArch;
	}
	public String getOsName() {
		return osName;
	}
	public String getOsVersion() {
		return osVersion;
	}
	public String getJreVendor() {
		return jreVendor;
	}
	public String getJreVersion() {
		return jreVersion;
	}
	public String getCpuVendor() {
		return cpuVendor;
	}
	public String getCpuModel() {
		return cpuModel;
	}
	public int getMhz() {
		return mhz;
	}
	public int getTotalCores() {
		return totalCores;
	}
	public long getSystemMemory() {
		return systemMemory;
	}
	public long getJvmMaxMemory() {
		return jvmMaxMemory;
	}
	public long getJvmTotalMemory() {
		return jvmTotalMemory;
	}
	public long getJvmUsed() {
		return jvmUsed;
	}
	public long getJvmFree() {
		return jvmFree;
	}
	public double getUserCPU() {
		return userCPU;
	}
	public double getSystemCPU() {
		return systemCPU;
	}
	public double getIdleCPU() {
		return idleCPU;
	}
	public double getCombinedCPU() {
		return combinedCPU;
	}
	public int getCpuCount() {
		return cpuCount;
	}
	public double getJvmCPU() {
		return jvmCPU;
	}
	public long getFreeDisk() {
		return freeDisk;
	}

}
