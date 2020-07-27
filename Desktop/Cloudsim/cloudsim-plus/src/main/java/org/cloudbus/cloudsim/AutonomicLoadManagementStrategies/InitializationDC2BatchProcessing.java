/**
 * 
 */
package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantDL160G5XeonL5420;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp1BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp2BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp2DC2;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp3BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp3DC2;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp4BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp4DC2;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp5BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp6BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderApp7BatchProcessing;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderDC2;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

/**
 * @author Shyam Sundar V
 *
 */
public class InitializationDC2BatchProcessing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	List<Vm> vmListDC2 = new LinkedList<>();
	List<Host> hostListDC2 = new LinkedList<>();
	Queue<Cloudlet> CloudletQueueDC2 = new LinkedList<>();

    private static final int  SCHEDULE_INTERVAL = 5;  
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
   
    /**
     * Create Cloudlets for Application 1,2,3,4 from workloadfile: These method reads 
     * {@link #maximumNumberOfCloudletsToCreateFromTheWorkloadFile} and create different Cloudlets as per
     * the predefined types.
     * 
     * @param NumberOfApplicationsToCreate creates the number of applications.Each application has set of cloudlets which will
     * be created as per the application.for example application 1 has 3 cloudlets
     * @param broker for Data center 2
     * @return CloudletQueueDC2
     */
    DatacenterBroker broker;
    public  void CreateCloudletAndVmForApplication1DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
    	this.broker=broker;
    	final String fileName = WORKLOAD_BASE_DIR_Ap1 + WORKLOAD_FILENAMEAp1;
    	SwfWorkloadFileReaderApp1BatchProcessing reader = SwfWorkloadFileReaderApp1BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueueDC2);
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueueDC2.size(), broker);     
    }

    public  void CreateCloudletAndVmForApplication2DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
        final String fileName = WORKLOAD_BASE_DIR_Ap2 + WORKLOAD_FILENAMEAp2;
        SwfWorkloadFileReaderApp2BatchProcessing reader = SwfWorkloadFileReaderApp2BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueueDC2); 
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueueDC2.size(), broker);     
    }

    public  void CreateCloudletAndVmForApplication3DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
        final String fileName = WORKLOAD_BASE_DIR_Ap1 + WORKLOAD_FILENAMEAp1;
        SwfWorkloadFileReaderApp3BatchProcessing reader = SwfWorkloadFileReaderApp3BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueueDC2);
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueueDC2.size(), broker);     
    }

    public  void CreateCloudletAndVmForApplication4DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
        final String fileName = WORKLOAD_BASE_DIR_Ap2 + WORKLOAD_FILENAMEAp2;
        SwfWorkloadFileReaderApp4BatchProcessing reader = SwfWorkloadFileReaderApp4BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueueDC2);
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueueDC2.size(), broker);     
    }
    
    public  void CreateCloudletAndVmForApplication5DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
    	this.broker=broker;
    	final String fileName = WORKLOAD_BASE_DIR_Ap3 + WORKLOAD_FILENAMEAp3;
    	SwfWorkloadFileReaderApp5BatchProcessing reader = SwfWorkloadFileReaderApp5BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueueDC2);
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueueDC2.size(), broker);     
    }
   
    public  void CreateCloudletAndVmForApplication6DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
        final String fileName = WORKLOAD_BASE_DIR_Ap4 + WORKLOAD_FILENAMEAp4;
        SwfWorkloadFileReaderApp6BatchProcessing reader = SwfWorkloadFileReaderApp6BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueueDC2);
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueueDC2.size(), broker);     
    }
    
    public  void CreateCloudletAndVmForApplication7DC2FromWorkloadFile(int NumberOfApplicationsToCreate,DatacenterBroker broker,CloudSim simulation) {
        final String fileName = WORKLOAD_BASE_DIR_Ap4 + WORKLOAD_FILENAMEAp4;
        SwfWorkloadFileReaderApp7BatchProcessing reader = SwfWorkloadFileReaderApp7BatchProcessing.getInstance(fileName, CLOUDLETS_MIPS);       
        int maximumNumberOfCloudletsToCreateFromTheWorkloadFile=NumberOfApplicationsToCreate;
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        reader.generateWorkload(broker,simulation,CloudletQueueDC2); 
        System.out.printf("# Created %d Cloudlets for %s\n", CloudletQueueDC2.size(), broker);     
    }
    
    /**
     * Gets the created Cloudlets in a Queue
     * @return {@link #CloudletQueueDC2}
     * @see #CreateCloudletForApplication1DC2FromWorkloadFile(int, DatacenterBroker)
     * @see #CreateCloudletForApplication2DC2FromWorkloadFile(int, DatacenterBroker)
     * @see #CreateCloudletForApplication3DC2FromWorkloadFile(int, DatacenterBroker)
     * @see #CreateCloudletForApplication4DC2FromWorkloadFile(int, DatacenterBroker)
     */
    
    public Queue<Cloudlet> getCloudletQueueDC2() 
    {
    	return CloudletQueueDC2;
    }
    
    
    /**
     * Create HostsDC2: This method can be used to create different hosts for data center 2 using predefined types
     * 
     * @param numberHosts  the number of hosts to be created
     * @param type of which the hosts will be created
     * 
     */
    
    public void createHostsDC2(int numberHosts, int type) 
	{
	    	
	   	    final PowerModel powerModelHpProLiantDL160G5 = new PowerModelSpecPowerHpProLiantDL160G5XeonL5420();
	    	final long storage = 100000, //host storage (MEGABYTE)
	    				 bw = 10000; //host storage (MEGABYTE)
	      	
	    	System.out.println("Creating host HPProliant Dl160G5");
	    	long mipsPe=2500; //2.5ghz=2500 mhz host Instruction Pre Second (Million)
	    	int ram = 16384; // host memory (MEGABYTE) 16 gb=16384(binary)
	    	long pesNumber = 8; // number of CPU core
	    	
	        // Create Hosts with its id and list of PEs and add them to the list of machines
	       
	        for(int i = 0; i < numberHosts; i++)
	        {
	            List<Pe> peList = new ArrayList<>();
	         //   long mipsPe = 1000;//Check once 1000 was there initially
	            for(int j = 0; j < pesNumber; j++)
	            {
	            	peList.add(new PeSimple(mipsPe, new PeProvisionerSimple()));
	            }
	            
	        	Host host = new HostSimple(ram, bw, storage, peList)
	        		.setRamProvisioner(new ResourceProvisionerSimple())
	        		.setBwProvisioner(new ResourceProvisionerSimple())
	        		.setVmScheduler(new VmSchedulerSpaceShared());
	        	host.setId(hostListDC2.size());
	        	host.setPowerModel(powerModelHpProLiantDL160G5);
	        	hostListDC2.add(host);
	        }        
	    }


    /**
     * Gets the created hosts in a list
     * @return {@link #hostListDC2}
     * @see #createHostsDC2(int, int)
     */
 
    public List<Host> getHostsListDC2() 
	    {
	    	return hostListDC2;
	    }
	    
    /**
     * Create Virtual machinesDC2: This method can be used to create virtual machines for data center 2
     * 
     * @param numberVm  the number of virtual machines to be created
     */
   Queue<Vm> vmQueueDC2 = new LinkedList<>();
   public Vm createVmDC2(int numberVm,long submitTime,String ApplicationType,long pesNumber) 
      {
      	final long   mips = 1000, storage = 10000, bw = 1000;
      	int ram = 1024; // vm memory (MEGABYTE) 1gb
      	long VmID = -1;
      	double StartTime=12;
      	
      //	for (int i = 0; i < numberVm; i++){		
          	Vm vm =new VmSimple(VmID, mips, pesNumber)
                      .setRam(ram).setBw(bw).setSize(storage)
                      .setCloudletScheduler(new CloudletSchedulerTimeShared()).setDescription(ApplicationType);
                      vm.setSubmissionDelay(submitTime);
          	vm.getUtilizationHistory().enable(); // Remove this line for Complex mode
          	vmQueueDC2.add(vm);

          	return vm;
          //}
      }
      
     
   /**
     * Gets the create virtual machines in a list
     * @return {@link #vmListDC2}
     * @see #createVmDC2(int)
     */
      
   public Queue<Vm> getvmQueueDC2() 
    {
      	return vmQueueDC2;
     }
      
	
}
