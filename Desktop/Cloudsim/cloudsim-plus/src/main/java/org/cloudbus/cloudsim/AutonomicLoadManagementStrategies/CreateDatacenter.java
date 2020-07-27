
package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class is dedicated for creating {@link Datacenter} and implementing different Heuristic algorithms {@link VmAllocationPolicySimple}, which will be further used in the model construction.
 * 
 * @param hostList
 * @param SCHEDULE_INTERVAL
 * 
 * @see CreateDatacenter#createHosts(int numberHosts, int type) 
 * @see CreateDatacenter#getHostsList() 
 * @see HeuristicAlgorithms#loadConcentrationAlgorithm (VmAllocationPolicy allocationPolicy, Vm vm)
 * 
 * @author Abdulrahman Nahhas
 * @since CloudSim Plus 1.0
 */

public class CreateDatacenter 
{
	private List<Host> hostList = new ArrayList<>();
    private static final int  SCHEDULE_INTERVAL = 5;  
    
    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        new CreateDatacenter();
    }
   
    
    /**
     * Create Datacenter: This method is used to create a data center before starting the simulation.
     * We will first create hosts {@link Initialization#createHosts}
     * 
     * @param simulation : An instance of the base class of CloudSim to start the simulation
     * @param strategyCode : This can be used to decide between different heuristics to be used for the allocation of virtual machines on the physical hosts
     * 
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public DatacenterSimple creatingSimpleDatacenter(CloudSim simulation) 
    {   
    	
        /**  
         * 	Here are the steps needed to create a DatacenterSimple:
         *  1. We first create an instance of the class Initialization, which will be further used in the model construction.
         *  2. Then we will call the createHosts methods before submitting the created hosts to our created data center
         *  3. Finally, we need to create a DatacenterSimple object.
         *  4. By default the a load concentration algorithm with be used for allocation Virtual machines to physical hosts.
         *  5. We then set the Characteristics of the Datacenter 
         *  
         *  @see {@link DatacenterSimple}
         *  @see {@link DatacenterSimple#DatacenterCharacteristics}
        */
    	
        hostList.addAll(getHostsList());
        System.out.println(hostList);  
   
        DatacenterSimple dc = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
        dc.getCharacteristics()
        .setCostPerSecond(3.0)
        .setCostPerMem(0.05)
        .setCostPerStorage(0.1)
        .setCostPerBw(0.1);
        
        dc.setSchedulingInterval(SCHEDULE_INTERVAL);
 //       dc.setSchedulingInterval(SCHEDULE_INTERVAL).setLog(true);

        /**
         * Now We need to create implementations for the Heuristics and set the desired allocation mechanism.
         * 
         * @see HeuristicAlgorithms#loadConcentrationAlgorithm (VmAllocationPolicy allocationPolicy, Vm vm)
         * @see HeuristicAlgorithms#loadBalancingAlgorithm (VmAllocationPolicy allocationPolicy, Vm vm)
         * 
         * Create a data center and submit my simulation with the created hosts in the list and my chosen load management strategy.
        */ 

        return dc;  
    }
    
    /**
     * Create Hosts: This method can be used to create different hosts using predefined types
     * 
     * @param numberHosts : the number of hosts to be created
     * @param type : of which the hosts will be created
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public void createHosts(int numberHosts, int type) 
    {
    	final double MAX_POWER = 100;
    	final double STATIC_POWER_PERCENT = 0.7;
    	final PowerModel powerModel = new PowerModelLinear(MAX_POWER, STATIC_POWER_PERCENT);
    	final long   mips = 1000, //host Instruction Pre Second (Million)
    				 storage = 100000, //host storage (MEGABYTE)
    				 bw = 10000; //host storage (MEGABYTE)
      	
    	int    ram = 0; // Initial declaration of the ram capacity 
    	long   pesNumber = 0; // Initial declaration of the pesNumber
    	
    	// create Hosts with different ram and pes capacity based on the types --> The types might be extended and those information  might be read from a file
    	
    	switch (type) 
    	{
    		case 1:
    			ram = 32768; // host memory (MEGABYTE)
    			pesNumber = 16; // number of CPU cores
    			break;
    		case 2:
    			ram = 65536; // host memory (MEGABYTE)
     	       	pesNumber = 32; // number of CPU cores
     	       	break;
    		case 3:
    			ram = 131072; // host memory (MEGABYTE)
     	       	pesNumber = 64; // number of CPU cores
     	       	break;
    	}
    	
        // Create Hosts with its id and list of PEs and add them to the list of machines
       
        for(int i = 0; i < numberHosts; i++)
        {
            List<Pe> peList = new ArrayList<>();
            long mipsPe = 1000;
            for(int j = 0; j < pesNumber; j++)
            {
            	peList.add(new PeSimple(mipsPe, new PeProvisionerSimple()));
            }
            
        	Host host = new HostSimple(ram, bw, storage, peList)
        		.setRamProvisioner(new ResourceProvisionerSimple())
        		.setBwProvisioner(new ResourceProvisionerSimple())
        		.setVmScheduler(new VmSchedulerTimeShared());
        	host.setId(hostList.size());
        	host.setPowerModel(powerModel);
        	hostList.add(host);
        }        
    }
    
    /**
     * Gets the created hosts in a list
     * @return {@link #hostList}
     * @see #createHosts(int, int)
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public List<Host> getHostsList() 
    {
    	return hostList;
    }
}
