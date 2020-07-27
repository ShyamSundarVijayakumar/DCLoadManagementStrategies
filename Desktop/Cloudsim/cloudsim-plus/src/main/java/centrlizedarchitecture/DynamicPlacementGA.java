/**
 * 
 */
package centrlizedarchitecture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.selectionpolicies.VmSelectionPolicy;
import org.cloudbus.cloudsim.vms.Vm;


/**
 * @author Shyam Sundar V
 *
 */
public class DynamicPlacementGA extends VmAllocationPolicyMigrationAbstract{

	public double safetyParameter;
	CloudSim simulation;
	public static double time;
	
	public static Map<Double,Long> hostRamUsage = new TreeMap<>();
	
//	public static double Set_Upper_Threshold = 0.85;
	
	
	public List<Double> hostCpuHistory = new ArrayList<Double>();
	public List<Double> hostRamHistory = new ArrayList<Double>();
	
	
	public double schedulingInterval;
	public static double Scheduling_Interval = 1;
	/*
	 * A map contains optimized placement of vms in hosts. 
	 */
	public  HashMap<Vm,Host> optimizedMap ;
//	public Map<Integer, ArrayList<Integer>> serverVmsmap = new TestDriver().serverVmsmap;
	
/**
 * Main method to perform live vm migration.
 * {@link https://en.wikipedia.org/wiki/Live_migration}
 * VM Migration theory and a SLA based approach for selection, PM, VM Specs and Experimental setup. Experiments.
 * {@link https://shodhganga.inflibnet.ac.in/bitstream/10603/182886/12/12_chapter%204.pdf}
 * 
 * @param vmSelectionPolicy
 *
 * Selectionpolicy given to super class does not work.It's function has been disabled in optimizedAllocationMap method. And has been manually 
 * called in global controller,
 */
	
	public DynamicPlacementGA(VmSelectionPolicy vmSelectionPolicy) {
	super(vmSelectionPolicy);
	}
 
    @Override
    public double getOverUtilizationThreshold(final Host host) {
        return  centralManager.HostUpperUtilizationThresholdDAAS;
    }
	/**
	 * Method to perform Live VM migration with a fallback and safety parameter. Safety parameter defines how aggresively the system consolidates 
	 * VMs on Servers. If the safety parameter is too tight, opportunities for energy savings become too low. 
	 * On the other hand, if the safety parameter is too relaxed, the levels of service level agreement violations become too high.
	 * 
	 * Experimental Scheduling Intervals from the paper for a datacenter with 800 hosts is 300 sec.
	 * {@link  https://link.springer.com/article/10.1186/s13677-019-0130-2}
	 * 
	 * @param vmSelectionPolicy
	 * @param safetyParameter
	 * @param fallbackVmAllocationPolicy
	 *
	 *Selectionpolicy given to super class does not work.It's function has been disabled in optimizedAllocationMap method. And has been manually 
	 * called in global controller,
	 * /
	
	public DynamicPlacementGA(VmSelectionPolicy vmSelectionPolicy, double safetyParameter,
			final double overUtilizationThreshold,
			final BiFunction<VmAllocationPolicy, Vm, Optional<Host>> findHostForVmFunction){
		super(vmSelectionPolicy, overUtilizationThreshold, findHostForVmFunction);
   	}

List<Vm> intialVms;
Map<Integer, ArrayList<Integer>> serverwithVMList;
/*
 * A map contains optimized placement of vms in hosts. 
 */
private Map<Integer, Integer> vmToHostMapWebApp = centralManager.vmToHostMapInitialPlacementWA;
//	Map<Integer, Integer> vmToHostMapDaas = new LocalControllerDaas().vmToHostMapInitialPlacement;
private Map<Integer, Integer> vmToHostMapDaas = centralManager.vmToHostMapInitialPlacementDAAS;
//public Map<Integer, Integer> vmToHostMapBatchProcess = new GeneticAlgorithmDriverBP().vmToHostMapBatchProcess;
//	public Map<Integer, Integer> vmToHostMapDaas= a.vmToHostMapInitialPlacement;
//	public GeneticAlgorithmDriverWA GA = new GeneticAlgorithmDriverWA();
int hostId;
List<Host> hostListWebApplication;
List<Host> hostListDaas;
List<Host> hostListBatchProcess;
Host hostToPlaceVm;

/**
 * Initial Placement using GA
 * @see GeneticAlgorithmPlacement
 */
 @Override
protected Optional<Host> defaultFindHostForVm(Vm vm) {
	 
			int vmId;
			String Applicationtype = vm.getDescription();
			if(Applicationtype == "Web_Application" ) {
				vmId = (int)vm.getId();
			 
				hostId = vmToHostMapWebApp.get(vmId);
				hostListWebApplication = getHostList().stream().
						filter(host -> host.getDescription() == "Hosts from Webapplication cluster").collect(Collectors.toList());
				
				//System.out.println(vmToHostMapWebApp);
				hostToPlaceVm = hostListWebApplication.get(hostId);
			}
			else if(Applicationtype == "1 Desktop as a service" || (Applicationtype == "2 Desktop as a service" ) || 
					(Applicationtype == "3 Desktop as a service" ) || (Applicationtype == "4 Desktop as a service" ) ||
					(Applicationtype == "5 Desktop as a service" )) {
				vmId = (int)vm.getId();

				hostListDaas = getHostList().stream().
						filter(host -> host.getDescription() == "Hosts from Desktop as a Service").collect(Collectors.toList());		
				if(getDatacenter().getSimulation().clock() > 1000) {
					vmToHostMapDaas = centralManager.vmToHostMapInitialPlacementDAAS;
				//	System.out.println(vmToHostMapDaas);
			//		System.out.println(vmId);
					hostId = vmToHostMapDaas.get(vmId);
					hostToPlaceVm = hostListDaas.get(hostId);
				}else {
					hostId = vmToHostMapDaas.get(vmId);
					hostToPlaceVm = hostListDaas.get(hostId);
				}
			}
			/*else if(Applicationtype == "Batch processing" ) {
				vmId = (int)vm.getId();
				hostId = vmToHostMapBatchProcess.get(vmId);
				hostListBatchProcess = getHostList().stream().
						filter(host -> host.getDescription() == "Host from Batch processing cluster").collect(Collectors.toList());
				hostToPlaceVm = hostListBatchProcess.get(hostId);
			}*/
			
			
//					System.out.println("Host "+ hostToPlaceVm.getAvailableMips());
			if(hostToPlaceVm.isSuitableForVm(vm)) {			
				return Optional.of(hostToPlaceVm);
			}else {
				vm.getBroker().destroyVm(vm);
				vm.getBroker().getVmWaitingList().remove(vm);
				centralManager.InitialvmListDaas.remove(vm);
			}
			
			return Optional.empty();
		
 } 
 
 @Override
public double getUnderUtilizationThreshold() {
	 return centralManager.HostUpperUtilizationThresholdDAAS;
 }
 
 @Override
	public boolean isHostOverloaded(Host host) {// Not used anywhere.. Just extended as it is in the super class. Otherwise not useful to our architecture.
	 String ApplicationType = host.getDescription();
	 if (ApplicationType == "Hosts from Webapplication cluster") {
		 return centralManager.OverLoadedHostssetWebApplication.contains(host);  
	  }
	  
		/*
		 * if (ApplicationType == "Host from Batch processing cluster") { return
		 * LocalControllerBatchProcessing.OverLoadedHostssetBatchProcessing.contains(
		 * host); }
		 */
	  
	  if (ApplicationType == "Hosts from Desktop as a Service") {
		  return centralManager.OverLoadedHostssetDaas.contains(host); 
	  }
	  System.out.println("Test check in DynamicPlacementGA :SHould not enter here : isHostOverloaded------------------------------------>");
	return false; //For check: should not enter here
	}
 
 @Override
 public boolean isHostUnderloaded(Host host) {
	 String ApplicationType = host.getDescription();
	 if (ApplicationType == "Hosts from Webapplication cluster") {
		 return centralManager.UnderLoadedHostssetWebApplication.contains(host);  
	  }
	  
		/*
		 * if (ApplicationType == "Host from Batch processing cluster") { return
		 * LocalControllerBatchProcessing.UnderLoadedHostssetBatchProcessing.contains(
		 * host); }
		 */
	  
	  if (ApplicationType == "Hosts from Desktop as a Service") {
		  return centralManager.UnderLoadedHostssetDaas.contains(host); 
	  }
	 	return false; 
 }
 
 /*
  * All the vms has to be migrated if possible from underloaded host.default method does the same process.
  * 
  * @Override
protected List<? extends Vm> getVmsToMigrateFromUnderUtilizedHost(Host host) {
	List<Vm> underloadVms = host.getMigratableVms().stream()
			.filter(vm -> GeneticAlgorithmDriver.bestDynamicVmServerMap.containsKey(vm.getId())
		//			&& !customerSLAContract.getMigratableVMs().getMigratableVMsMetric().getList().contains((int)vm.getId()))
			.collect(Collectors.toList()));
//	System.out.println("host vms"+ host.getMigratableVms()+" | filtered vms  "+underloadVms);
return underloadVms;
	//	return super.getVmsToMigrateFromUnderUtilizedHost(host);
}*/
 
 
 

 /**
  * This method used to optimize the VM allocation(dynamic vm placement) 
  * @see DynamicEvolutionDAAS  
  */

 private long hostIdDP ;
 @Override
 protected Optional<Host> findHostForVmInternal(Vm vm, Stream<Host> hostStream) {
		
		// * get the current vm allocation from GA 	 
		 
		 if(vm.getDescription() == "Web_Application") {
			 hostIdDP =  centralManager.bestDynamicVmServerMapWA.get(vm.getId());
			  
			 Stream<Host> hostStreamWA = hostStream.filter(host -> host.getDescription() == "Hosts from Webapplication cluster");
	
			 return Optional.of(hostStreamWA.
					 filter(host -> (host.getId() == hostIdDP && host.isSuitableForVm(vm)))
					 .findFirst()
					 .orElse(vm.getHost()));
		 }/*  else if(vm.getDescription() == "Batch processing") {
			 hostIdDP = GeneticAlgorithmDriverBP.bestDynamicVmServerMap.get(vm.getId());
			 
			 Stream<Host> hostStreamBP = hostStream.filter(host -> host.getDescription() == "Host from Batch processing cluster");
	
			 return Optional.of(hostStreamBP.
					 filter(host -> (host.getId() == hostIdDP && host.isSuitableForVm(vm)))
					 .findFirst()
					 .orElse(vm.getHost()));
				
		 }*/ else if((vm.getDescription() == "1 Desktop as a service") || (vm.getDescription() == "2 Desktop as a service" ) || 
				 (vm.getDescription() == "3 Desktop as a service" ) || (vm.getDescription() == "4 Desktop as a service" ) ||
				 (vm.getDescription() == "5 Desktop as a service" )) {
			 hostIdDP = centralManager.bestDynamicVmServerMapDAAS.get(vm.getId());
			 
			 Stream<Host> hostStreamDAAS = hostStream.filter(host -> host.getDescription() == "Hosts from Desktop as a Service");
	
			 return Optional.of(hostStreamDAAS.
					 filter(host -> (host.getId() == hostIdDP && host.isSuitableForVm(vm)))
					 .findFirst()
					 .orElse(vm.getHost()));
		 }
	//	System.out.printf("Host Id....%d \n",hostId);
	
	 // If the detination host has enough resources and not overloaded after placing vm, we migrate or else, we abort
	 
		 System.out.println("Should not get here : check findHostForVmInternal---------------->");
		 return Optional.empty();//Should not get here
			
	 }

}
