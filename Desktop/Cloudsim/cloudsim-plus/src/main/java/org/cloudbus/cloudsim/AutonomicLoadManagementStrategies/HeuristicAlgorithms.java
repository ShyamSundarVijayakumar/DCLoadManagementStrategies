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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class is dedicated for creating implementations for the Heuristics and set the desired allocation mechanism.
 * {@link VmAllocationPolicySimple}, which will be further used in the model construction.
 * {@link loadConcentration}, {@link loadBalancingAlgorithm}
 *  
 * 
 * @see HeuristicAlgorithms#loadConcentrationAlgorithm (VmAllocationPolicy allocationPolicy, Vm vm)
 * @see HeuristicAlgorithms#loadBalancingAlgorithm (VmAllocationPolicy allocationPolicy, Vm vm)
 * 
 * @author Abdulrahman Nahhas
 * @since CloudSim Plus 1.0
 */

public class HeuristicAlgorithms
{  
	public List<Host> hostListWebApplication = new LinkedList<>();
	public List<Vm> VmList = new LinkedList<>();
	

	/**
	 * @param vmList the vmList to set
	 */
	public void setVmListWebApplication(List<Vm> vmList) {
		VmList = vmList;
	}


	/**
	 * @param hostList the hostList to set
	 */
	public void setHostListWebApplication(List<Host> hostList) {
		this.hostListWebApplication = hostList;
	}

	/**
     * @param args
     */
    public static void main(String[] args) 
    {
        new HeuristicAlgorithms();
    }
    
	public HeuristicAlgorithms() 
	{
		
	}

	/**
     * setAllocationPolicy: This method is used to set the used allocation policy for virtual machines allocation.
     * This method is under development and can be marginally extended "Accepting a set of datacenters and a set of codes and assigning them"
     * 
     * @param dc : The created data center 
     * @param strategyCode : This can be used to decide between different heuristics to be used for the allocation of virtual machines on the physical hosts
     * 
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public void setAllocationPolicy(DatacenterSimple dc, int strategyCode) {   
        /**
         * Now We need to create implementations for the Heuristics and set the desired allocation mechanism.
         * 
         * @see HeuristicAlgorithms#loadConcentrationAlgorithm (VmAllocationPolicy allocationPolicy, Vm vm)
         * @see HeuristicAlgorithms#loadBalancingAlgorithm (VmAllocationPolicy allocationPolicy, Vm vm)
         * 
         * Assign the received data center the chosen load management strategy.
        */ 
    	
        final VmAllocationPolicySimple loadConcentration = new VmAllocationPolicySimple(this::loadConcentrationAlgorithm);
        final VmAllocationPolicySimple loadBalancing = new VmAllocationPolicySimple(this::loadBalancingAlgorithm);

        switch (strategyCode)
    	{
    		case 1:
    			dc.setVmAllocationPolicy(loadBalancing);
    			break;
    		case 2:
    			dc.setVmAllocationPolicy(loadConcentration); 
    			break;
    	}
    }
  
    /**
     * This method implements a load concentration strategy that can be used to allocate virtual machines to physical hosts using the provided interface VmAllocationPolicy.
     * @param allocationPolicy : An instance of the allocation policy to implement the strategy
     * @param vm : The Virtual machines that will be allocated using the strategy 
     * 
     * @return VmAllocationPolicy: This policy concentrate the virtual machines on the available physical host
     * @see VmAllocationPolicy
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public Optional<Host> loadConcentrationAlgorithm(VmAllocationPolicy allocationPolicy, Vm vm) {
    	
    	System.out.println("Starting LoadConcentration"); // Used just for debugging 
    	return allocationPolicy.getHostList().stream().filter(host -> host.isSuitableForVm(vm)).min(Comparator.comparingInt(Host :: getFreePesNumber));
        
    	/*if(vm.getDescription() == "Web_Application") {
    	return hostListWebApplication
            .stream()
            .filter(host -> host.isSuitableForVm(vm))
            .min(Comparator.comparingInt(Host::getFreePesNumber));
    	}
    	else
    	{
    		return hostListWebApplication
    	            .stream()
    	            .filter(host -> host.isSuitableForVm(vm))
    	            .min(Comparator.comparingInt(Host::getFreePesNumber));//return null;
    	}*/
    }

    /**
     * This method implements a load balancing strategy that can be used to allocate virtual machines to physical hosts using the provided interface VmAllocationPolicy.
     * @param allocationPolicy : An instance of the allocation policy to implement the strategy
     * @param vm : The Virtual machines that will be allocated using the strategy 
     * 
     * @return VmAllocationPolicy: This policy balance the virtual machines on the available physical host
     * @see VmAllocationPolicy
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public Optional<Host> loadBalancingAlgorithm(VmAllocationPolicy allocationPolicy, Vm vm){
    	System.out.println("Starting LoadBalancing"); // Used just for debugging 
    	if(vm.getDescription() == "Web_Application") {
        	return hostListWebApplication
                .stream()
                .filter(host -> host.isSuitableForVm(vm))
                .min(Comparator.comparingInt(Host::getFreePesNumber));
        	}
        	else
        	{
        		return null;
        	}
    }
    
    
    
}
