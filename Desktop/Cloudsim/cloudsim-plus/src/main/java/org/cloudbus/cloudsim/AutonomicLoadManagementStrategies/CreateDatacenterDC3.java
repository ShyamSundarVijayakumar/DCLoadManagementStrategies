package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationBestFitStaticThreshold;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.selectionpolicies.VmSelectionPolicyMaximumUtilization;
import org.cloudbus.cloudsim.selectionpolicies.VmSelectionPolicyMinimumUtilization;
//import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.DatacenterBrokerEventInfo;

/**
 * This class is dedicated for creating {@link Datacenter 3 } and implementing different Heuristic algorithms {@link VmAllocationPolicySimple},
 *  which will be further used in the model construction.
 * 
 * @param hostListDC3
 * @param SCHEDULE_INTERVAL
 * 
 * @see CreateDatacenter#createHostsDC3(int numberHosts) 
 * @see CreateDatacenter#getHostsListDC3() 
 * @see HeuristicAlgorithms#loadConcentrationAlgorithm (VmAllocationPolicy allocationPolicy, Vm vm)
 * 
 * @since CloudSim Plus 1.0
 */
public class CreateDatacenterDC3 {

	public List<Host> hostListDC3 = new ArrayList<>();
    private static final double  SCHEDULE_INTERVAL = 300;  
	
	public static void main(String[] args) {
		new CreateDatacenterDC3();
	}
	
	  /**
     * Create Datacenter3 : This method is used to create data center 3 before starting the simulation.
     * We will first create hosts {@link Initialization#createHostsDC3}
     * 
     * @param simulation : An instance of the base class of CloudSim to start the simulation
     * @param strategyCode : This can be used to decide between different heuristics to be used for the allocation of virtual machines on the physical hosts
     * 
     */
	
    private static final double HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION = 0.7;
    DatacenterBroker broker;


	  private VmAllocationPolicyMigrationStaticThreshold allocationPolicy;
//	  private VmAllocationPolicyMigrationAbstractCustom allocationPolicyGA;
	public DatacenterSimple creatingSimpleDatacenterDC3(CloudSim simulation,List<Host> hostList, DatacenterBroker brokerDC3){   

    	this.allocationPolicy =
                new VmAllocationPolicyMigrationBestFitStaticThreshold(
                    new VmSelectionPolicyMinimumUtilization(),
                    HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION+0.2);
    	
   // 	this.allocationPolicyGA =
    //            new VmAllocationPolicyMigrationGeneticAlgorithm(
  //                  new VmSelectionPolicyMaximumUtilization());
    	
    	
	       hostListDC3.addAll(getHostsListDC3());
	        System.out.println("----------------------------------------------------------------------------------"+hostListDC3);  
	        DatacenterSimple dc = new DatacenterSimple(simulation,hostList ,allocationPolicy);//new VmAllocationPolicySimple()); 
	        dc.getCharacteristics()
	        .setCostPerSecond(3.0)
	        .setCostPerMem(0.05)
	        .setCostPerStorage(0.1)
	        .setCostPerBw(0.1);
	        dc.setSchedulingInterval(SCHEDULE_INTERVAL);
	          this.broker=brokerDC3;
	          broker.addOnVmsCreatedListener(this::onVmsCreatedListener);
	        return dc;  
	    }
	    
    private void onVmsCreatedListener(final DatacenterBrokerEventInfo evt) {
        allocationPolicy.setOverUtilizationThreshold(HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION);
        allocationPolicy.setUnderUtilizationThreshold(0.15);
        broker.removeOnVmsCreatedListener(evt.getListener());
    }
    /**
     * Create HostsDC3: This method can be used to create host for data center 3 using predefined type
     * 
     * @param numberHosts : the number of hosts to be created
     * 
     */
	
	public void createHostsDC3(int numberHosts) 
	    {
		  //Main memory 500 GB=512000-only detali
	    	final double MAX_POWER = 100;
	    	final double STATIC_POWER_PERCENT = 0.7;
	    	final PowerModel powerModel = new PowerModelLinear(MAX_POWER, STATIC_POWER_PERCENT);
	    	final long   mips = 1000, //host Instruction Pre Second (Million)
	    				 storage = 100000, //host storage (MEGABYTE)
	    				 bw = 10000; //host storage (MEGABYTE)
	      	
	    	int    ram = 512000; // Initial declaration of the ram capacity host memory (MEGABYTE)
	    	long   pesNumber = 64; // Initial declaration of the pesNumber,number of CPU cores

	       // Create Hosts with its id and list of PEs and add them to the list of machines
	       
	        for(int i = 0; i < numberHosts; i++)
	        {
	            List<Pe> peList = new ArrayList<>();
	            long mipsPe = 1000;
	            for(int j = 0; j < pesNumber; j++)
	            {
	            	peList.add(new PeSimple(mipsPe, new PeProvisionerSimple()));
	            }
	        	Host host =
	        			new HostSimple(ram, bw, storage, peList)
	        		.setRamProvisioner(new ResourceProvisionerSimple())
	        		.setVmScheduler(new VmSchedulerSpaceShared());
	        	host.setId(hostListDC3.size());
	        	host.setPowerModel(powerModel);
	        	hostListDC3.add(host);
	        }        
	    }
	
	
	/**
      * Gets the created hosts in a list
      * @return {@link #hostListDC3}
      * @see #createHostsDC3(int)
      */
    
	public List<Host> getHostsListDC3() 
	 {
	       return hostListDC3;
	  }
	  List<Host> hostListDummyDC = new ArrayList<>();
	    public Host createHostDummy() {
	   
	        long mips = 1000;
	        List<Pe> peList0 = new ArrayList<>();
	        for(int j = 0; j < 5064; j++)
	        {
	        	peList0.add(new PeSimple(mips, new PeProvisionerSimple()));
	        }
	        int hostId = 10; 
	        int ram = 900000000; //host memory (Megabyte)
	        long storage = 900000000; //host storage
	        long bw = 1000000000;

	        Host host0 = new HostSimple(ram, bw, storage, peList0)
	            .setRamProvisioner(new ResourceProvisionerSimple())
	            .setBwProvisioner(new ResourceProvisionerSimple())
	            .setVmScheduler(new VmSchedulerTimeShared());
	        host0.setId(hostId);
	        hostListDummyDC.add(host0);//trial
	        return host0;
	        
	    }
	    
	    public List<Host> getHostsListDummyVmDC3() 
		 {
		       return hostListDummyDC;
		  }
}
