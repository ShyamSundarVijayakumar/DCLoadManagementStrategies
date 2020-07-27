package hierarchicalarchitecture.globalcontroller;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelPlanetLab;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

/**
 * This class is dedicated for initializing  web application, by creating different types of {@link Vm} and {@link Cloudlet}
 * 
 * @param vmListWebApplication
 * @param SCHEDULING_INTERVAL
 * @param cloudletListWebApplication
 * 
 * @see RequestAnalyserAndConfigurationManagerWebApplication#createCloudlet(long)
 * @see RequestAnalyserAndConfigurationManagerWebApplication#createOneVmAndCloudlet(int, int, DatacenterBroker)
 * @see RequestAnalyserAndConfigurationManagerWebApplication#getCloudletListWA()
 * @see RequestAnalyserAndConfigurationManagerWebApplication#getVmListWebApplication()
 * 
 * @author Shyam Sundar V
 * @since CloudSim Plus 5.0
 */
public class RequestAnalyserAndConfigurationManagerWebApplication {

	static List<Vm> vmListWebApplication = new ArrayList<>();
	public List<Cloudlet> cloudletListWebApplication = new ArrayList<>();
	private UtilizationModelStochastic um;
	private static final boolean MULTIPLE_UTILIZATION_MODELS = false;
	private static final long SEED = 123456;
	private static final boolean ALWAYS_GENERATE_NEW_RANDOM_UTILIZATION = true;
    private static final int SCHEDULING_INTERVAL = 300;	
    private String TRACE_FILE = "";
    List<File> fileList = new ArrayList<>();  
    int i=0;
    private File Folder;

    /**
     * Creates a Cloudlet with the given information.
     *
     * @param id      a Cloudlet ID
     * @param runTime The number of seconds the Cloudlet has to run.
     *                {@link Cloudlet#getLength()} is computed based on
     *                the {@link #getMips() mips} and this value.
     * @param numProc number of Cloudlet's PEs
     * @return the created Cloudlet
     * @see #mips
     */
    private Cloudlet createCloudlet(long len) {
    	/*	Get the name of each workload file in the folder and assign them to cpu utilization.
    	 * 	Each cloudlet will be assigned a separate file
    	 */ 	
    	int Id=-1;
    	int numProc=1;    	
    	Folder = new File("G:\\Digital Engineering\\load balencing\\trace data\\planet lab\\planetlab-workload-traces-master\\20110411");
    	this.um = MULTIPLE_UTILIZATION_MODELS || this.um == null ? new UtilizationModelStochastic(SEED) : this.um;
		this.um.setAlwaysGenerateNewRandomUtilization(ALWAYS_GENERATE_NEW_RANDOM_UTILIZATION);
//		this.um.setHistoryEnabled(true);
	//	this.um.setRandomGenerator();
		UtilizationModelDynamic utilizationRam = new UtilizationModelDynamic(0.8);
		utilizationRam.setUtilizationUpdateFunction(this::utilizationUpdate);
        File[] listOfFiles = Folder.listFiles();
    	TRACE_FILE = listOfFiles[i++].toString();
    	final UtilizationModel utilizationCpu = UtilizationModelPlanetLab.getInstance(TRACE_FILE, SCHEDULING_INTERVAL);

        Cloudlet cloudlet=new CloudletSimple(Id, len, numProc)
        			.setFileSize(1024)
                    .setOutputSize(1024)
                    .setUtilizationModelCpu(utilizationCpu)
                    .setUtilizationModelRam(um);
  //                  .setUtilizationModelBw(new UtilizationModelDynamic(0.1));
                    
         return cloudlet;
    }
	private double utilizationUpdate(UtilizationModelDynamic utilizationModel) {

		return utilizationModel.getUtilization() + utilizationModel.getTimeSpan() * 0.001;
	}
    /**
     * Gets the created Cloudlets in a list
     * @return {@link #cloudletListDC1}
     * @see #createCloudletsFromWorkloadFile(DatacenterBroker)
     */
    
    public List<Cloudlet> getCloudletListWA() {
    	return cloudletListWebApplication;
    }
    
    /**
     * Gets the created Cloudlets in an Ascending order in a list
     * @return {@link #cloudletListDC1}
     * @see #createCloudletsFromWorkloadFile(DatacenterBroker)
     */
    
    public List<Cloudlet> getAscendingCloudletListDC1() {    	
    	Comparator<Cloudlet> sortCloudLet = Comparator.comparingLong(Cloudlet::getLength);
    	cloudletListWebApplication.sort(sortCloudLet);
    	return cloudletListWebApplication;
    }
    
    /**
     * Gets the created Cloudlets in an Descending order in a list
     * @return {@link #cloudletListDC1}
     * @see #createCloudletsFromWorkloadFile(DatacenterBroker)
     */
    
    public List<Cloudlet> getDescendingCloudletListDC1() 
    {    	
    	Comparator<Cloudlet> sortCloudLet = Comparator.comparingLong(Cloudlet::getLength).reversed();
    	cloudletListWebApplication.sort(sortCloudLet);
    	return cloudletListWebApplication;
    }
    
    /**
     * Create Vm DC1: This method can be used to create different Cloudlets for data center 1 using predefined types
     * 
     * @param numberVm  the number of Cloudlets to be created
     * @param type of which the Cloudlets will be created
     * @param broker of data center 1
     * @param cloudlet of data center 1
     */
   
    public void createOneVmAndCloudlet(int numberVm, int type, DatacenterBroker broker) {
    	long VmID = -1;
    	final long storage = 102400, bw = 512;
    	int mips = 0;
    	int    ram = 0;
    	long   pesNumber = 0;
    	long Cloudletlength=0;
    	String VmType="0";
    	String ApplicationType = "Web_Application";
       /* type = "High-cpu Medium";
    	type = "Extra large";
    	type = "Small";
    	type = "Micro";*/
    	
    	switch (type)
    	{
    		case 1://"High-cpu Medium":
    			mips = 2500;
    			ram = 870; // vm memory (MEGABYTE), 0.85gb = 870mb (binary)
    			pesNumber = 1; // number of CPU cores
    			Cloudletlength=216000000;//216000000 108000000(50%)
    			VmType="High-cpu Medium";
    			break;
    		case 2://"Extra large":
     	       	mips = 2000;
    			ram = 3840; // vm memory (MEGABYTE),3.75gb = 3840mb (binary)
     	       	pesNumber = 1; // number of CPU cores
     	        Cloudletlength=172800000;//172800000  (86400000)50%
     	        VmType="Extra-Large";	       
     	       	break;
    		case 3://"Small":
     	       	mips = 1000;
    			ram = 1740; // vm memory (MEGABYTE), 1.75gb = 1740mb (binary)
     	       	pesNumber = 1; // number of CPU cores
     	        Cloudletlength=86400000;//86400000 (43200000)50%
     	        VmType="Small";        
     	       	break;
    		case 4://"Micro":
    			mips = 500;
    			ram = 624; // vm memory (MEGABYTE), 0.61gb = 624mb (binary)
    			pesNumber = 1; // number of CPU cores
    			Cloudletlength=43200000;//43200000 (21600000)50%
    			VmType="Micro";
     	       	break;
     	
    	}  

        	for (int i = 0; i < numberVm; i++){
        	Vm vm = new VmSimple(VmID, mips, pesNumber)
                    .setRam(ram).setBw(bw).setSize(storage)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());
    //    	vm.setPeVerticalScaling(createVerticalPeScaling());
        	vm.getUtilizationHistory().enable();
        	vm.setDescription(ApplicationType);// vm.setDescription(VmType);
        	vmListWebApplication.add(vm);        
            Cloudlet cloudlet=createCloudlet( Cloudletlength);
 
            broker.submitVm(vm);
            broker.submitCloudlet(cloudlet);
            broker.bindCloudletToVm(cloudlet, vm);
        	}
    }
    
    /**
     * Gets the create virtual machines in a list
     * @return {@link #vmListDC1}
     * @see #createOneVmForEachCloudlet(int, int, DatacenterBroker, Cloudlet)
     */
    public static List<Vm> getVmListWebApplication() {
    	return vmListWebApplication;
    }  
    
}
