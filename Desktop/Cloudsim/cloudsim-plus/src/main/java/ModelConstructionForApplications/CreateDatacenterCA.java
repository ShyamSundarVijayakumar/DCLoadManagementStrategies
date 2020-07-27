/**
 * 
 */
package ModelConstructionForApplications;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimpleCM;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantDL160G5XeonL5420;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;
import org.cloudbus.cloudsim.power.models.PowerModelSpecSupermicroServer1123US_TR4;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;

import hierarchicalarchitecture.globalcontroller.VmSelectionPolicyCpuAndRamBased;

/**
 * @author Shyam Sundar V
 *
 */
public class CreateDatacenterCA {
	public static List<Host> hostListDaaS = new ArrayList<>();
	public static List<Host> hostListWebApplication = new ArrayList<>();
	public static List<Host> hostListBatchProcessing = new ArrayList<>();
	private List<Host> hostListAggregate = new ArrayList<>();
    private static final int  SCHEDULE_INTERVAL = 300;  
//    private VmAllocationPolicyMigrationStaticThreshold allocationPolicy;
 //   private static final double HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION = 0.7;
    DatacenterBroker broker;
 
    
    /**
     * This method is used to create a data center before starting the simulation.
     * We will first create hosts for all the three applications {@link CreateDatacenter #createHostsDaaS(int)},
     * {@link CreateDatacenter #createHostsWebApplication(int, int)} and {@link CreateDatacenter #createHostsBatchProcessing(int)}
     * 
     * @param simulation : An instance of the base class of CloudSim to start the simulation
     * @param DatacenterBroker : Represents a broker acting on behalf of a customer
     * 
     */

    public DatacenterSimpleCM creatingSimpleDatacenter(CloudSim simulation) { //, DatacenterBroker brokerDC1
		/*
		 * this.allocationPolicy = new
		 * VmAllocationPolicyMigrationBestFitStaticThreshold( new
		 * VmSelectionPolicyMinimumUtilization(),
		 * HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION+0.1);
		 */
        	getHostsListFromAllApplication();
       // 	hostListAggregate.addAll(hostListAggregate);
        	System.out.println(this.hostListAggregate); 
          DatacenterSimpleCM dc1 = new DatacenterSimpleCM(simulation, this.hostListAggregate,
        		   new centrlizedarchitecture.DynamicPlacementGA(new VmSelectionPolicyCpuAndRamBased()));
        			   dc1.getCharacteristics()
        		          .setCostPerSecond(3.0)
        		          .setCostPerMem(0.05)
        		          .setCostPerStorage(0.1)
        		          .setCostPerBw(0.1);
        		          
        		          dc1.setSchedulingInterval(SCHEDULE_INTERVAL);
        		    //      this.broker=brokerDC1;
        		       //   broker.addOnVmsCreatedListener(this::onVmsCreatedListener);
        		   //       dc1.setSchedulingInterval(SCHEDULE_INTERVAL).setLog(true);
        		          return dc1;  
    }
    
	/*
	 * private void onVmsCreatedListener(final DatacenterBrokerEventInfo evt) {
	 * allocationPolicy.setOverUtilizationThreshold(
	 * HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION);
	 * allocationPolicy.setUnderUtilizationThreshold(0.10);
	 * broker.removeOnVmsCreatedListener(evt.getListener()); }
	 */
    
    /**
     * This method can be used to create host for the application Desktop as a Service using the predefined type
     * 
     * @param numberHosts : Number of hosts to be created
     * 
     */
	
	public void createHostsDaaS(int numberHosts) {
		  //Main memory 500 GB=512000-only detail in paper
	//    	final double MAX_POWER = 100;
	 //   	final double STATIC_POWER_PERCENT = 0.7;
	  //  	final PowerModel powerModel = new PowerModelLinear(MAX_POWER, STATIC_POWER_PERCENT);
	    	final PowerModel powerModel = new PowerModelSpecSupermicroServer1123US_TR4();
	    	final long   storage = 1000000, //host storage (MEGABYTE)
	    				 bw = 10000; //host storage (MEGABYTE)
	      	
	    	int    ram = 512000; // Initial declaration of the ram capacity host memory (MEGABYTE)
	    	long   pesNumber = 64; // Initial declaration of the pesNumber,number of CPU cores

	       // Create Hosts with its id and list of PEs and add them to the list of machines
	       
	        for(int i = 0; i < numberHosts; i++) {
	            List<Pe> peList = new ArrayList<>();
	            long mipsPe = 2000;
	            
	            for(int j = 0; j < pesNumber; j++) {
	            	peList.add(new PeSimple(mipsPe, new PeProvisionerSimple()));
	            }
	        	
	            Host host =
	        			new HostSimple(ram, bw, storage, peList)
	        		.setRamProvisioner(new ResourceProvisionerSimple())
	        		.setVmScheduler(new VmSchedulerSpaceShared());
	        	host.setId(hostListDaaS.size());
	        	host.setDescription("Hosts from Desktop as a Service");
	        	host.setPowerModel(powerModel);
	        	hostListDaaS.add(host);
	        }        
	    }
	
	
	/**
      * Gets the host list for Desktop as a service cluster
      * @return {@link #hostListDaaS}
      * @see #createHostsDaaS(int)
      */

	public static List<Host> getHostsListDaaS() {
	       return hostListDaaS;
	}
    
    
    /**
     * This method can be used to create different hosts for web application using the predefined types
     * 
     * @param numberHosts : Number of hosts to be created
     * @param type : Type of the host to be created
     * 
     */
	
    public void createHostsWebApplication(int numberHosts, int type) {    	
    	
  //  	final double MAX_POWER = 100;
    //	final double STATIC_POWER_PERCENT = 0.7;
    //	final PowerModel powerModel = new PowerModelLinear(MAX_POWER, STATIC_POWER_PERCENT);
    	final PowerModel powerModelHPProliantG4 =new PowerModelSpecPowerHpProLiantMl110G4Xeon3040();
    	final PowerModel powerModelHPProliantG5 =new PowerModelSpecPowerHpProLiantMl110G5Xeon3075();
    	final long   storage = 5120000; //host storage (MEGABYTE)
    	int bw = 0; //host storage (MEGABYTE)      	
    	int    ram = 0; // Initial declaration of the ram capacity 
    	long   pesNumber = 0; // Initial declaration of the pesNumber
    	long mipsPe = 0;  //host Instruction Pre Second (Million)
    /*	type = "HP Proliant G4";
    	type = "HP Proliant G5";*/
    //	String cpuType;
     	
    	switch (type) {
    	
    	case 1://"HP Proliant G4":
    //		cpuType = "Intel_Xeon 3040";
    		ram = 4096; // host memory (MEGABYTE), 4 GB 
    		pesNumber = 2; // number of CPU cores
    		mipsPe= 1860;  //1.86 (GHz)= 1860 (MHz)
    		bw = 2048 + 2048; //1 GB   - 2 GB  			
    			  
    		for(int i = 0; i < numberHosts; i++){
    			List<Pe> peList = new ArrayList<>();
    			for(int j = 0; j < pesNumber; j++){
    				peList.add(new PeSimple(mipsPe, new PeProvisionerSimple()));
    			}
    	            
    			Host host = new HostSimple(ram, bw, storage, peList)
    					.setRamProvisioner(new ResourceProvisionerSimple())
    	        		.setBwProvisioner(new ResourceProvisionerSimple())
    	        		.setVmScheduler(new VmSchedulerTimeShared());
    			host.setId(hostListWebApplication.size());
    			host.setDescription("Hosts from Webapplication cluster");
    			host.setPowerModel(powerModelHPProliantG4);
    			hostListWebApplication.add(host);
    		}     			
    		break;
    			
    	case 2://"HP Proliant G5":
  //  		cpuType = "Intel_Xeon 3075";
    		ram = 4096; // host memory (MEGABYTE), 4 GB
    		pesNumber = 2; // number of CPU cores
    		long mipsPe1 = 2660; //2.66 (GHz) = 2660 (MHz) 
    		bw = 2048 + 2048; // 1 GB- 2 GB
    		
    		for(int i = 0; i < numberHosts; i++){
    			List<Pe> peList = new ArrayList<>();
     	            
    			for(int j = 0; j < pesNumber; j++){
    				peList.add(new PeSimple(mipsPe1, new PeProvisionerSimple()));
    			}
     	            
    			Host host = new HostSimple(ram, bw, storage, peList)
    					.setRamProvisioner(new ResourceProvisionerSimple())
     	        		.setBwProvisioner(new ResourceProvisionerSimple())
     	        		.setVmScheduler(new VmSchedulerTimeShared());
    			host.setId(hostListWebApplication.size());
    			host.setDescription("Hosts from Webapplication cluster");
    			host.setPowerModel(powerModelHPProliantG5);
    			hostListWebApplication.add(host);
    		}       	       	
    		break;
    	
    	}            
    }
    
    /**
     * Gets the host list for web application cluster
     * @return {@link #hostListWebApplication}
     * @see #createHostsWebApplication(int, int)
     *
     */
    
    public static List<Host> getHostsListWebApplication() {
    	return hostListWebApplication;
    }
    
    /**
     * This method can be used to create host for batch processing application using the predefined type
     * 
     * @param numberHosts : Number of hosts to be created
     * 
     */
    
    public void createHostsBatchProcessing(int numberHosts) 
    {
 	   
    	final long storage = 100000, //host storage (MEGABYTE)
    				 bw = 10000; //host storage (MEGABYTE)
      	
    	System.out.println("Creating host HPProliant Dl160G5");
    	long mipsPe=2500; //2.5ghz=2500 mhz host Instruction Pre Second (Million)
    	int ram = 16384; // host memory (MEGABYTE) 16 gb=16384(binary)
    	long pesNumber = 8; // number of CPU core
    	
        // Create Hosts with its id and list of PEs and add them to the list of machines
       
        for(int i = 0; i < numberHosts; i++){
        	final PowerModel powerModelHpProLiantDL160G5 = new PowerModelSpecPowerHpProLiantDL160G5XeonL5420();
            List<Pe> peList = new ArrayList<>();
            
            for(int j = 0; j < pesNumber; j++){
            	peList.add(new PeSimple(mipsPe, new PeProvisionerSimple()));
            }
            
        	Host host = new HostSimple(ram, bw, storage, peList)
        		.setRamProvisioner(new ResourceProvisionerSimple())
        		.setBwProvisioner(new ResourceProvisionerSimple())
        		.setVmScheduler(new VmSchedulerTimeShared());
        	host.setId(hostListBatchProcessing.size());
        	host.setDescription("Host from Batch processing cluster");
        	host.setPowerModel(powerModelHpProLiantDL160G5);
        	hostListBatchProcessing.add(host);
        }        
    }
    
    /**
     * Gets the host list for batch processing cluster
     * @return {@link #hostListBatchProcessing}
     * @see #createHostsBatchProcessing(int))
     */
    
    public static List<Host> getHostsListBatchProcessing() {
    	return hostListBatchProcessing;
    }
    
    /**
     * Gets the created hosts in all the three application list
     * @return {@link #hostListAggregate}
     * 
     */

	public List<Host> getHostsListFromAllApplication() {
		/*
		 * System.out.println("----------------------------"+hostListDaaS);
		 * System.out.println("----------------------------"+hostListWebApplication);
		 * System.out.println("----------------------------"+hostListBatchProcessing);
		 */
		hostListAggregate.addAll(hostListDaaS);
		hostListAggregate.addAll(hostListWebApplication);
		hostListAggregate.addAll(hostListBatchProcessing);

		return hostListAggregate;
	}	
}
