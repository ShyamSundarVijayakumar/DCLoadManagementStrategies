/**
 * 
 */
package hierarchicalarchitecture.globalcontroller;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp1BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp2BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp3BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp4BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp5BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp6BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp7BatchProcessing;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

/**
 * @author Shyam Sundar V
 *
 */
public class RequestAnalyserAndConfigurationManagerBatchProcessing {

	Queue<Vm> vmQueue = new LinkedList<>();
	List<Vm> vmList = new LinkedList<>();
	Queue<Cloudlet> CloudletQueue = new LinkedList<>();

 //   private static final int  SCHEDULE_INTERVAL = 5;  
    /**
     * The workload file to be read.
     */  
    
    private static final String WORKLOAD_FILENAMEAp1 = "SDSC-Par-1995-3.1-cln.swf";
    private static final String WORKLOAD_FILENAMEAp2 = "SDSC-Par-1996-3.1-cln.swf";
    private static final String WORKLOAD_FILENAMEAp3 = "SDSC-BLUE-2000-4.swf";
    private static final String WORKLOAD_FILENAMEAp4 = "SDSC-DS-2004-2.swf";
   
    
    /**
     * The base directory inside the resource directory to get SWF workload files.
     */
    private static final String WORKLOAD_BASE_DIR_Ap1 = "G://Digital Engineering//load balencing//trace data//SDSC Paragon 1995/";
    private static final String WORKLOAD_BASE_DIR_Ap2 = "G://Digital Engineering//load balencing//trace data//SDSC Paragon 1996/";
    private static final String WORKLOAD_BASE_DIR_Ap3 = "G://Digital Engineering//load balencing//trace data//SDSC Blue horizon/";
    private static final String WORKLOAD_BASE_DIR_Ap4 = "G:\\Digital Engineering\\load balencing\\trace data\\SDSC Datastar log/";
    private static final int CLOUDLETS_MIPS = 2500; //assigned randomly(has to be changed)

    DatacenterBroker broker;

    /**
     * Create Cloudlets for Application 1,2,3,4 from workloadfile: These method reads 
     * {@link #maximumNumberOfCloudletsToCreateFromTheWorkloadFile} and create different Cloudlets as per
     * the predefined types.
     * 
     * @param NumberOfApplicationsToCreate creates the number of applications.Each application has set of cloudlets which will
     * be created as per the application.for example application 1 has 3 cloudlets
     * @param broker for Data center 2
     * @return CloudletQueue
     */
    public  void CreateCloudletAndVmForApplication1DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
    	this.broker=broker;
    	final String fileName = WORKLOAD_BASE_DIR_Ap1 + WORKLOAD_FILENAMEAp1;
    	SwfWorkloadFileReaderApp1BatchProcessing reader = SwfWorkloadFileReaderApp1BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueue);
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueue.size(), broker);     
    }

    public  void CreateCloudletAndVmForApplication2DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
    	final String fileName = WORKLOAD_BASE_DIR_Ap2 + WORKLOAD_FILENAMEAp2;
        SwfWorkloadFileReaderApp2BatchProcessing reader = SwfWorkloadFileReaderApp2BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueue); 
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueue.size(), broker);     
    }

    public void CreateCloudletAndVmForApplication3DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
        final String fileName = WORKLOAD_BASE_DIR_Ap1 + WORKLOAD_FILENAMEAp1;
        SwfWorkloadFileReaderApp3BatchProcessing reader = SwfWorkloadFileReaderApp3BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueue);
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueue.size(), broker);     
    }

    public void CreateCloudletAndVmForApplication4DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
        final String fileName = WORKLOAD_BASE_DIR_Ap2 + WORKLOAD_FILENAMEAp2;
        SwfWorkloadFileReaderApp4BatchProcessing reader = SwfWorkloadFileReaderApp4BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueue);
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueue.size(), broker);     
    }
    
    public void CreateCloudletAndVmForApplication5DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
    	this.broker=broker;
    	final String fileName = WORKLOAD_BASE_DIR_Ap3 + WORKLOAD_FILENAMEAp3;
    	SwfWorkloadFileReaderApp5BatchProcessing reader = SwfWorkloadFileReaderApp5BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueue);
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueue.size(), broker);     
    }
   
    public void CreateCloudletAndVmForApplication6DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
        final String fileName = WORKLOAD_BASE_DIR_Ap4 + WORKLOAD_FILENAMEAp4;
        SwfWorkloadFileReaderApp6BatchProcessing reader = SwfWorkloadFileReaderApp6BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueue);
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueue.size(), broker);     
    }
    
    public void CreateCloudletAndVmForApplication7DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
        final String fileName = WORKLOAD_BASE_DIR_Ap4 + WORKLOAD_FILENAMEAp4;
        SwfWorkloadFileReaderApp7BatchProcessing reader = SwfWorkloadFileReaderApp7BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueue); 
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueue.size(), broker);     
    }
    
    /**
     * Gets the created Cloudlets in a Queue
     * @return {@link #CloudletQueue}
     * @see #CreateCloudletForApplication1DC2FromWorkloadFile(int, DatacenterBroker)
     * @see #CreateCloudletForApplication2DC2FromWorkloadFile(int, DatacenterBroker)
     * @see #CreateCloudletForApplication3DC2FromWorkloadFile(int, DatacenterBroker)
     * @see #CreateCloudletForApplication4DC2FromWorkloadFile(int, DatacenterBroker)
     */
    
    public Queue<Cloudlet> getCloudletQueue(){
    	return CloudletQueue;
    }
    
    /**
     * Create virtual machines: This method can be used to create virtual machines for batch processing
     * 
     * @param numberVm  the number of virtual machines to be created
     */
    public Vm createVm(int numberVm,long submitTime,String ApplicationType,long pesNumber){
    	final long   mips = 1000, storage = 10000, bw = 1000;
      	int ram = 1024; // vm memory (MEGABYTE) 1gb
      	long VmID = -1;
  //    	double StartTime=12;
      	
      //	for (int i = 0; i < numberVm; i++){		
      	Vm vm =new VmSimple(VmID, mips, pesNumber)
      			.setRam(ram).setBw(bw).setSize(storage)
      			.setCloudletScheduler(new CloudletSchedulerTimeShared()).setDescription(ApplicationType);
      	vm.setSubmissionDelay(submitTime);
      	vm.getUtilizationHistory().enable(); // Remove this line for Complex mode
      	vm.setDescription("Batch processing");
      	vmQueue.add(vm);

      	return vm;
    }

   /**
    * Gets the create virtual machines in a list
    * @return {@link #vmList}
    * @see #createVm(int)
    */
    public Queue<Vm> getvmQueue(){
    	return vmQueue;
    }

}
