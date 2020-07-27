package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
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
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantDL160G5XeonL5420;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.selectionpolicies.VmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.DatacenterBrokerEventInfo;

/**
 * This class is dedicated for creating {@link Datacenter 2} and implementing different Heuristic algorithms {@link VmAllocationPolicySimple},
 *  which will be further used in the model construction.
 * 
 * @param hostListDC2
 * @param SCHEDULE_INTERVAL
 * 
 * @see CreateDatacenterDC2#createHostsDC2(int numberHosts) 
 * @see CreateDatacenterDC2#getHostsListDC2() 
 * @see HeuristicAlgorithms#loadConcentrationAlgorithm (VmAllocationPolicy allocationPolicy, Vm vm)
 * 
 * @since CloudSim Plus 1.0
 */
public class CreateDatacenterDC2 {

	private List<Host> hostListDC2 = new LinkedList<>();
    private static final int  SCHEDULE_INTERVAL = 1;  
    
    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        new CreateDatacenter();
    }
   
    
    /**
     * Create Datacenter2 : This method is used to create data center 2 before starting the simulation.
     * We will first create hosts {@link InitializationDC2#createHostsDC2}
     * 
     * @param simulation : An instance of the base class of CloudSim to start the simulation
     * @param strategyCode : This can be used to decide between different heuristics to be used for the allocation of virtual machines on the physical hosts
     * 
     */
    private static final double HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION = 0.7;
    DatacenterBroker broker;
  //  private VmAllocationPolicyMigrationAbstractCustom allocationPolicyGA;
    private VmAllocationPolicyMigrationStaticThreshold allocationPolicy;
    public DatacenterSimple creatingSimpleDatacenterDC2(CloudSim simulation) 
    {   
    	this.allocationPolicy =
                new VmAllocationPolicyMigrationBestFitStaticThreshold(
                    new VmSelectionPolicyMinimumUtilization(),
                    HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION+0.1);
 //   	GAVMAPM = new VmAllocationPolicyMigrationGeneticAlgorithm(new VmSelectionPolicyMinimumUtilization());
     //   this.allocationPolicyGA = new VmAllocationPolicyMigrationGeneticAlgorithm(
       //             new VmSelectionPolicyMinimumUtilization());


        hostListDC2.addAll(getHostsListDC2());
        System.out.println(hostListDC2);  
   
        DatacenterSimple dc = new DatacenterSimple(simulation, hostListDC2, allocationPolicy);
        dc.getCharacteristics()
        .setCostPerSecond(3.0)
        .setCostPerMem(0.05)
        .setCostPerStorage(0.1)
        .setCostPerBw(0.1);
        
        dc.setSchedulingInterval(SCHEDULE_INTERVAL);
 //       dc.setSchedulingInterval(SCHEDULE_INTERVAL).setLog(true);


        return dc;  
    }
    private void onVmsCreatedListener(final DatacenterBrokerEventInfo evt) {
        allocationPolicy.setOverUtilizationThreshold(HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION);
        allocationPolicy.setUnderUtilizationThreshold(0.25);
        broker.removeOnVmsCreatedListener(evt.getListener());
    }
    /**
     * Create HostsDC2: This method can be used to create host for data center 2 using predefined type
     * 
     * @param numberHosts : the number of hosts to be created
     * 
     */
    
    public void createHostsDC2(int numberHosts) 
    {
 	   
    	final long storage = 100000, //host storage (MEGABYTE)
    				 bw = 10000; //host storage (MEGABYTE)
      	
    	System.out.println("Creating host HPProliant Dl160G5");
    	long mipsPe=2500; //2.5ghz=2500 mhz host Instruction Pre Second (Million)
    	int ram = 16384; // host memory (MEGABYTE) 16 gb=16384(binary)
    	long pesNumber = 8; // number of CPU core
    	
        // Create Hosts with its id and list of PEs and add them to the list of machines
       
        for(int i = 0; i < numberHosts; i++)
        {
        	final PowerModel powerModelHpProLiantDL160G5 = new PowerModelSpecPowerHpProLiantDL160G5XeonL5420();
            List<Pe> peList = new ArrayList<>();
            for(int j = 0; j < pesNumber; j++)
            {
            	peList.add(new PeSimple(mipsPe, new PeProvisionerSimple()));
            }
            
        	Host host = new HostSimple(ram, bw, storage, peList)
        		.setRamProvisioner(new ResourceProvisionerSimple())
        		.setBwProvisioner(new ResourceProvisionerSimple())
        		.setVmScheduler(new VmSchedulerTimeShared());
        	host.setId(hostListDC2.size());
        	host.setPowerModel(powerModelHpProLiantDL160G5);
        	hostListDC2.add(host);
        }        
    }
    
    /**
     * Gets the created hosts in a list
     * @return {@link #hostListDC2}
     * @see #createHostsDC2(int)
     */
    
    public List<Host> getHostsListDC2() 
    {
    	return hostListDC2;
    }
}
