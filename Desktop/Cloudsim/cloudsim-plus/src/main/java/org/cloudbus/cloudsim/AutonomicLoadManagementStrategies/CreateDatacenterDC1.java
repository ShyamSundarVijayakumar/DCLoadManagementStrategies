package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationBestFitStaticThreshold;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationWorstFitStaticThreshold;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.selectionpolicies.VmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.DatacenterBrokerEventInfo;
import org.cloudbus.cloudsim.power.models.*;

/**
 * This class is dedicated for creating {@link Datacenter} and implementing different Heuristic algorithms {@link VmAllocationPolicySimple},
 *  which will be further used in the model construction.
 * 
 * @param hostListDC1
 * @param SCHEDULE_INTERVAL
 * 
 * @see CreateDatacenterDC1#createHostsDC1(int numberHosts, int type) 
 * @see CreateDatacenterDC1#getHostsListDC1() 
 * @see HeuristicAlgorithms#loadConcentrationAlgorithm (VmAllocationPolicy allocationPolicy, Vm vm)
 * 
 * @since CloudSim Plus 1.0
 */

public class CreateDatacenterDC1  {

	private List<Host> hostListDC1 = new ArrayList<>();
    private static final int  SCHEDULE_INTERVAL = 300;  
    private VmAllocationPolicyMigrationStaticThreshold allocationPolicy;
    public static void main(String[] args) 
    {
   
    	new CreateDatacenterDC1();
    	
    }
 
    
    /**
     * Create Datacenter1: This method is used to create a data center before starting the simulation.
     * We will first create hosts {@link InitializationDC1#createHostsDC1}
     * 
     * @param simulation : An instance of the base class of CloudSim to start the simulation
     * @param strategyCode : This can be used to decide between different heuristics to be used for the allocation 
     * of virtual machines on the physical hosts
     * 
     */
    private static final double HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION = 0.7;
    DatacenterBroker broker;
  //  private VmAllocationPolicyMigrationAbstractCustom allocationPolicyGA;
    public DatacenterSimple creatingSimpleDatacenterDC1(CloudSim simulation, DatacenterBroker brokerDC1) 
    {
    	this.allocationPolicy =
                new VmAllocationPolicyMigrationBestFitStaticThreshold(
                    new VmSelectionPolicyMinimumUtilization(),
                    HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION+0.1);
 //   	GAVMAPM = new VmAllocationPolicyMigrationGeneticAlgorithm(new VmSelectionPolicyMinimumUtilization());
    //    this.allocationPolicyGA =
     //           new VmAllocationPolicyMigrationGeneticAlgorithm(
      //              new VmSelectionPolicyMinimumUtilization());
    	  hostListDC1.addAll(getHostsListDC1());
          System.out.println(hostListDC1);  
          
          DatacenterSimple dc1 = new DatacenterSimple(simulation, hostListDC1, this.allocationPolicy);//VmAllocationPolicySimple()
          dc1.getCharacteristics()
          .setCostPerSecond(3.0)
          .setCostPerMem(0.05)
          .setCostPerStorage(0.1)
          .setCostPerBw(0.1);
          
          dc1.setSchedulingInterval(SCHEDULE_INTERVAL);
          this.broker=brokerDC1;
          broker.addOnVmsCreatedListener(this::onVmsCreatedListener);
   //       dc1.setSchedulingInterval(SCHEDULE_INTERVAL).setLog(true);
          return dc1;  
    }
    
    private void onVmsCreatedListener(final DatacenterBrokerEventInfo evt) {
        allocationPolicy.setOverUtilizationThreshold(HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION);
        allocationPolicy.setUnderUtilizationThreshold(0.10);
        broker.removeOnVmsCreatedListener(evt.getListener());
    }
    /**
     * Create HostsDC1: This method can be used to create different hosts using predefined types for data center 1
     * 
     * @param numberHosts : the number of hosts to be created
     * @param type : of which the hosts will be created
     * 
     */
    public void createHostsDC1(int numberHosts, int type) 
    {    	
    	
    	final double MAX_POWER = 100;
    	final double STATIC_POWER_PERCENT = 0.7;
    	final PowerModel powerModel = new PowerModelLinear(MAX_POWER, STATIC_POWER_PERCENT);
    	final PowerModel powerModelHPProliantG4 =new PowerModelSpecPowerHpProLiantMl110G4Xeon3040();
    	final PowerModel powerModelHPProliantG5 =new PowerModelSpecPowerHpProLiantMl110G5Xeon3075();
    	final long   storage = 512000; //host storage (MEGABYTE)
    	int bw = 0; //host storage (MEGABYTE)      	
    	int    ram = 0; // Initial declaration of the ram capacity 
    	long   pesNumber = 0; // Initial declaration of the pesNumber
    	long mipsPe = 0;  //host Instruction Pre Second (Million)
    /*	type = "HP Proliant G4";
    	type = "HP Proliant G5";*/
    	String cpuType;
     	
    	switch (type) 
    	{
    		case 1://"HP Proliant G4":
    			cpuType = "Intel_Xeon 3040";
    			ram = 4096; // host memory (MEGABYTE), 4 GB 
    			pesNumber = 2; // number of CPU cores
    			mipsPe= 1860;  //1.86 (GHz)= 1860 (MHz)
    			bw = 1024; //1 GB    			
    			  
    	        for(int i = 0; i < numberHosts; i++)
    	        {
    	            List<Pe> peList = new ArrayList<>();
    	            for(int j = 0; j < pesNumber; j++)
    	            {
    	            	peList.add(new PeSimple(mipsPe, new PeProvisionerSimple()));
    	            }
    	            
    	        	Host host = new HostSimple(ram, bw, storage, peList)
    	        		.setRamProvisioner(new ResourceProvisionerSimple())
    	        		.setBwProvisioner(new ResourceProvisionerSimple())
    	        		.setVmScheduler(new VmSchedulerTimeShared());
    	        	host.setId(hostListDC1.size());
    	        	host.setPowerModel(powerModelHPProliantG4);
    	        	hostListDC1.add(host);
    	        }     			
    			break;
    			
    		case 2://"HP Proliant G5":
    			cpuType = "Intel_Xeon 3075";
    			ram = 4096; // host memory (MEGABYTE), 4 GB
     	       	pesNumber = 2; // number of CPU cores
     	       	long mipsPe1 = 2660; //2.66 (GHz) = 2660 (MHz) 
     	       	bw = 1024; // 1 GB
     	       	     	       
     	        for(int i = 0; i < numberHosts; i++)
     	        {
     	            List<Pe> peList = new ArrayList<>();
     	            for(int j = 0; j < pesNumber; j++)
     	            {
     	            	peList.add(new PeSimple(mipsPe1, new PeProvisionerSimple()));
     	            }
     	            
     	        	Host host = new HostSimple(ram, bw, storage, peList)
     	        		.setRamProvisioner(new ResourceProvisionerSimple())
     	        		.setBwProvisioner(new ResourceProvisionerSimple())
     	        		.setVmScheduler(new VmSchedulerTimeShared());
     	        	host.setId(hostListDC1.size());
     	        	host.setPowerModel(powerModelHPProliantG5);
     	        	hostListDC1.add(host);
     	        }       	       	
     	       	break;
    	
    	}            
    }
    
    /**
     * Gets the created hosts in a list
     * @return {@link #hostListDC1}
     * @see #createHosts(int, int)
     *
     */
    public List<Host> getHostsListDC1() 
    {
    	return hostListDC1;
    }
    
}
