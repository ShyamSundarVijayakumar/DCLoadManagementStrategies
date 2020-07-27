/**
 * 
 */
package hierarchicalarchitecture.localcontrollerdaas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.autoscaling.VerticalVmScalingSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hierarchicalarchitecture.globalcontroller.GlobalController;

/**
 * @author Shyam Sundar V
 *
 */
public class LocalControllerDaas {
	public static List<Host> hostListDaas = new LinkedList<>();
	public static Set<Host> OverLoadedHostssetDaas = new HashSet<Host>();
	public static Set<Host> UnderLoadedHostssetDaas = new HashSet<Host>();
	public static List<Vm> vmListDaas = new ArrayList<>();
	private DatacenterSimple Datacenter;
	private GlobalController GC;
	String description = "Hosts from Desktop as a Service";
	public Map<Long, Long> sourceVMHostMap; // = new HashMap<Long, Long>();
	public Map<Long, Double> vmsCurrentCPUUtil; // = new HashMap<Long, Double>();
	public Map<Long, Long> vmsCurrentRAMUtil; // = new HashMap<Long, Long>();
	public Map<Long, Double> hostCurrentUtil; // = new HashMap<Long, Double>();
	public List<Long> targetHostList; // = new ArrayList<Long>();
	public static List<Vm> VmstoMigrateFromOverloadedUnderloadedHosts;// new LinkedList<>();
	public static List<Vm> VmstoMigrateFromOverloadedHostsDAAS = new ArrayList<>();
	public static Map<Long, Long> bestDynamicVmServerMap;
	public Map<Long, ArrayList<Long>> dynamicserverVmMap;
	public static Map<Integer, Integer> vmToHostMapInitialPlacement;

	public static double HostUpperUtilizationThreshold = GlobalController.HostUpperUtilizationThresholdDAAS;
	public static double HostLowerUtilizationThreshold = GlobalController.HostLowerUtilizationThresholdDAAS;
	
	public void setDcSimulationGlobalcontroller(DatacenterSimple dc, CloudSim simulation, GlobalController globalController) {
	//	this.Simulation = simulation;
		this.Datacenter = dc;
		this.GC = globalController;
	}


	private List<Host> getHostlistDaas() {
		return Datacenter.getHostList().stream().filter(host -> host.getDescription() == description)
				.collect(Collectors.toList());
	}


	Vm vm;
	public void setPreviousMapAsCurrentMap() {
		if(bestDynamicVmServerMap != null) {
			
			for (final Map.Entry<Long, Long> entryDaas : bestDynamicVmServerMap.entrySet()) {
				vm = null;
				Here:
				for (Vm vm1 : vmListDaas) {
    				if(vm1.getId() == entryDaas.getKey().intValue()) {
    					vm = vm1;
    					break Here;
    				}
    			}
		//		Vm vm = vmListDaas.get(entryDaas.getKey().intValue());
				if(vm != null) {
					if(vm.isCreated()) {
						Host host = hostListDaas.get(entryDaas.getValue().intValue());
						bestDynamicVmServerMap.put(vm.getId(), host.getId());
					}
				}
				
			}	
		}
	}

	private boolean OnlyOneHostHasVmsOrNoHostHasVms() {
		int NoOfHostsWithVmsInIt = 0;

		for (Host host : hostListDaas) {
			if (!host.getVmList().isEmpty()) {
				NoOfHostsWithVmsInIt += 1;
			}
		}
		boolean j;
		if ((NoOfHostsWithVmsInIt <= 1)) {
			j = true;
		} else
			j = false;

		return j;
	}

	int previousTime = 0;
	public int currentTime;
	public int nextSchedulinginterval = 300;
	/**
	 * This method classifies all the hosts during every scheduling interval and informs the 
	 * global controller about the hosts status(only when they are overloaded or underloaded).
	 * 
	 * This method also prints the overloaded and underloaded hosts and their utilization values
	 * @param time
	 */
	public void ClassifyActiveHostsInDaasCluster(int time) {
		currentTime = time;
		if ((currentTime != 0) && (currentTime != previousTime) &&(nextSchedulinginterval == currentTime) && (vmArrivalTime != currentTime)) {
			previousTime = time;//((int) evt.getTime());
			nextSchedulinginterval = currentTime + 300;
		//	System.out.println("Test Check ---------->" + evt.getTime());
			
			hostListDaas = getHostlistDaas();

			if(!OnlyOneHostHasVmsOrNoHostHasVms()) {
				HostClassifierDAAS CategoriseHost = new HostClassifierDAAS();
				CategoriseHost.SetHostUpperAndLowerUtilizationThreshold(GlobalController.HostUpperUtilizationThresholdDAAS, GlobalController.HostLowerUtilizationThresholdDAAS);
				OverLoadedHostssetDaas = CategoriseHost.getOverloadedHosts(hostListDaas);
				UnderLoadedHostssetDaas = CategoriseHost.getUnderLoadedHosts(hostListDaas);
				if(!OverLoadedHostssetDaas.isEmpty()) {
					printOverUtilizedHosts(time, OverLoadedHostssetDaas);	
				}
			
				if(!UnderLoadedHostssetDaas.isEmpty()) {
					printUnderUtilizedHosts(time, UnderLoadedHostssetDaas);
				}
				
				if(!UnderLoadedHostssetDaas.isEmpty() ||  !OverLoadedHostssetDaas.isEmpty()) {
					GC.globalDecisionMakerDAAS(this);
				}
		
		//		shutdowninactivehosts(); VM's are failing to find host "No suitable host for vm". when that happens at least once then after that no incoming vms are allocated.
			}
		}
	}

	/*
	 * private void shutdowninactivehosts() { for(Host host : hostListDaas) {
	 * if(host.getVmList().isEmpty()&& host.isActive()) {
	 * host.setIdleShutdownDeadline(0); } } }
	 */	Logger LOGGER = LoggerFactory.getLogger(LocalControllerDaas.class.getSimpleName());
	  /* Prints the over utilized hosts.
	   *
	   * @param overloadedHosts the over utilized hosts
	   */
	private void printOverUtilizedHosts(int time, final Set<Host> overloadedHosts) {
		if (!overloadedHosts.isEmpty() && LOGGER.isWarnEnabled()) {
			final String hosts = overloadedHosts.stream().map(this::overloadedHostToString).collect(Collectors.joining(System.lineSeparator()));
			LOGGER.warn("{}: Local controller DaaS: Overloaded hosts in Daas cluster{}:{}{}",
					time, overloadedHosts.stream().findFirst().get().getDatacenter(), System.lineSeparator(), hosts);
	        }
	    }
	
	private void printUnderUtilizedHosts(int time, final Set<Host> UnderloadedHosts) {
		if (!UnderloadedHosts.isEmpty() && LOGGER.isWarnEnabled()) {
			final String hosts = UnderloadedHosts.stream().map(this::underloadeddHostToString).collect(Collectors.joining(System.lineSeparator()));
			LOGGER.warn("{}: Local controller DaaS: Underloaded hosts in Daas cluster{}:{}{}",
					time, UnderloadedHosts.stream().findFirst().get().getDatacenter(), System.lineSeparator(), hosts);
	        }
	    }
	
	private String overloadedHostToString(final Host host) {
		double HostRamUtil = 0;
		double HostCap = host.getRam().getCapacity();
		for(Vm vm : host.getVmList()) {
			HostRamUtil += vm.getRam().getAllocatedResource();		
		}
	  	   
		return String.format(
				"      Host %d (upper threshold %.2f, CPU utilization: %.2f, Ram utilization: %.2f)",
				host.getId(), GlobalController.HostUpperUtilizationThresholdDAAS, host.getCpuPercentUtilization(), HostRamUtil / HostCap);
	}
	
	private String underloadeddHostToString(final Host host) {
		double HostRamUtil = 0;
		double HostCap = host.getRam().getCapacity();
		for(Vm vm : host.getVmList()) {
			HostRamUtil += vm.getRam().getAllocatedResource();		
		}
	  	   
		return String.format(
				"      Host %d (lower threshold %.2f, CPU utilization: %.2f, Ram utilization: %.2f)",
				host.getId(), GlobalController.HostLowerUtilizationThresholdDAAS , host.getCpuPercentUtilization(), HostRamUtil / HostCap);
	}
	
	public Map<Vm, Map<Integer, Long>> getVmRamUtilizationHistory() {
		return ModelConstructionForApplications.ModelConstruction.DAASVmsRamUtilizationHistory;
	}

	/**
	 * @param Hostlist
	 */
	public void SetHostlistDaas(List<Host> Hostlist){
		hostListDaas = Hostlist;
	}
	
	/**
	 * This method is used for the initial placement in the DaaS cluster
	 */
	private void placementManager() {
		GADriverDaas GA = new GADriverDaas();
		GA.GAInitialplacement();
		vmToHostMapInitialPlacement = GA.vmToHostMapDaas;
		Host host = hostListDaas.get(0);
		double simultionTime = host.getDatacenter().getSimulation().clock();
		if(simultionTime > 1000) {
			updateDynamicMap();//Datacentersimple makes use of this for migrations.If it is not updated then the previous offline values are taken and vms are migrated
			sendVMCharToGA();//This is to update the sourcevmserver map which is given as input to dynamic placement
		}
		scheduler(vmToHostMapInitialPlacement);
	}

	private void updateDynamicMap(){
		bestDynamicVmServerMap = new HashMap<Long, Long>();
		vmToHostMapInitialPlacement.forEach((vm,server) -> {
			bestDynamicVmServerMap.put(vm.longValue(), server.longValue());
		});
		
	}
	

	public boolean GAChooseSourceMap;
	/**
	 * This method is performs the dynamic placement and informs the global controller
	 * 
	 * @param initialvmListDaas
	 * @param vmlisttoMigrate
	 * @param VmlisttoMigrateFromOverloadedHosts
	 * @return
	 */
	public Map<Long, Long> placementManagerForDynamicPlacement(List<Vm> initialvmListDaas, List<Vm> vmlisttoMigrate,List<Vm> VmlisttoMigrateFromOverloadedHosts) {
		vmListDaas = initialvmListDaas;
		VmstoMigrateFromOverloadedUnderloadedHosts = new LinkedList<>();
		VmstoMigrateFromOverloadedUnderloadedHosts = vmlisttoMigrate;
		VmstoMigrateFromOverloadedHostsDAAS = new ArrayList<>();
		VmstoMigrateFromOverloadedHostsDAAS = VmlisttoMigrateFromOverloadedHosts;
		Vm vm = vmListDaas.get(0);
		if ((!OverLoadedHostssetDaas.isEmpty()) || (!UnderLoadedHostssetDaas.isEmpty())
				|| (VmstoMigrateFromOverloadedUnderloadedHosts != null)) {
			if((vm.getBroker().getVmWaitingList().isEmpty())) {
				sendVMCharToGA();
				activeHosts();
				GADriverDaas ga = new GADriverDaas();
				// it selects the same host as source and target host.no chnage in placement.
				bestDynamicVmServerMap = ga.DynamicGeneticAlgorithmDriverDaas(sourceVMHostMap, targetHostList, hostListDaas, vmListDaas);
				this.dynamicserverVmMap = ga.serverVmMapDynamic;
				GAChooseSourceMap = ga.GAChooseSourceMap;
				if(numberOfActiveHosts <= ga.serverVmMapDynamic.size() && (OverLoadedHostssetDaas.isEmpty())) {
					bestDynamicVmServerMap = sourceVMHostMap;
				}
			}	
		}
		return bestDynamicVmServerMap;			
	}

	int numberOfActiveHosts;
	private void activeHosts() {
		numberOfActiveHosts=0;
		hostListDaas.forEach(host ->{
			if(host.isActive() && host.getVmList().size()!=0) {
				numberOfActiveHosts += 1;
			}
		});
	}
	
	/**
	 * Shows updates every time the simulation clock advances.
	 * 
	 * @param evt information about the event happened (that for this Listener is
	 *            just the simulation time)
	 */
	private void sendVMCharToGA() {
		sourceVMHostMap = new HashMap<Long, Long>();
		vmsCurrentCPUUtil = new HashMap<Long, Double>();
		vmsCurrentRAMUtil = new HashMap<Long, Long>();
		vmListDaas.forEach(vm -> {
			
			ifVmisinWaitingListAddThemToSourceMap(vm);
			Host sourceHost = vm.getHost();			
			if (!vm.isFailed() && vm.isWorking() && sourceHost.getVmList().contains(vm)) {
				sourceHost = ifVmisinMigrationGetTargetHost(vm, sourceHost);		
				sourceVMHostMap.put(vm.getId(), sourceHost.getId());
				vmsCurrentCPUUtil.put(vm.getId(), vm.getTotalCpuMipsUtilization());
				vmsCurrentRAMUtil.put(vm.getId(), vm.getCurrentRequestedRam());
			}
		});
		sendHostUtilToGA();
	}

	/**
	 * @param vm
	 */
	private void ifVmisinWaitingListAddThemToSourceMap(Vm vm) {
		if(!vm.getBroker().getVmWaitingList().isEmpty()) {
			vm.getBroker().getVmWaitingList().forEach(Vm1 -> {
				if(Vm1.getId() == vm.getId()) {
					for(final Map.Entry<Integer, Integer> entryDaas : vmToHostMapInitialPlacement.entrySet()){
							for (Vm vm1 : vmListDaas) {
								if(vm1.getId() == entryDaas.getKey().intValue()) {
									sourceVMHostMap.put(vm.getId(), entryDaas.getValue().longValue());
								}
							}
					}
				}
			});
		}
	}

	/**
	 * @param vm
	 * @param sourceHost
	 * @return
	 */
	private Host ifVmisinMigrationGetTargetHost(Vm vm, Host sourceHost) {
		if(vm.isInMigration()) {
			for(final Map.Entry<Long, Long> entryDaas : bestDynamicVmServerMap.entrySet()){
				if(vm.getId() == entryDaas.getKey().intValue()) {
					sourceHost = hostListDaas.get(entryDaas.getValue().intValue());		
				}
			}
		}
		return sourceHost;
	}

	double hostCpuUtil;

	private void sendHostUtilToGA() {
		hostCurrentUtil = new HashMap<Long, Double>();
		hostListDaas.forEach(host -> {
			hostCpuUtil = host.getCpuPercentUtilization();
			if (host.isActive() && host.getVmList().size() != 0) {
				hostCurrentUtil.put(host.getId(), hostCpuUtil);
			}
		});
	}

	public void sendVMCharToGACallFromGC(Map<Long, Double> serverUtil) {
		sourceVMHostMap = new HashMap<Long, Long>();
		if (bestDynamicVmServerMap != null) {
			sourceVMHostMap = bestDynamicVmServerMap;
		} else {
			sendVMCharToGA();
		}

		hostCurrentUtil = new HashMap<Long, Double>();
		hostCurrentUtil = serverUtil;
	}

	public void SendTargerhostlistDynamicPlacement(List<Host> targetHosts) {
		targetHostList = new ArrayList<Long>();
		targetHosts.forEach(host -> {
			targetHostList.add(host.getId());
		});
	}


	/**
	 * This method performs the scheduling operation. It gets the map from the placement manager and 
	 * set them as the placement map, which will be used by the allocation policy.
	 * @param vmToHostMapDaas
	 */
	private void scheduler(Map<Integer, Integer> vmToHostMapDaas) {
		vmToHostMapInitialPlacement = vmToHostMapDaas;
	}

	
	/**
	 * Activates the scaling for all the Vms (Ram)
	 * @param vmList
	 */
	private void vmScaler(List<Vm> vmList) {
		for (Vm vm : vmList) {
			createVerticalRamScalingForVm(vm);	
		}
	}
	
	private void vmScaler() {
		for (Vm vm : vmListDaas) {
			createVerticalRamScalingForVm(vm);	
		}
	}

	public void VmListForPlacement(List<Vm> vmList) {
		vmListDaas = vmList;
		vmScaler();
		placementManager();
	}
	
	int vmArrivalTime = 0;
	public void VmListForPlacementAfterOffline(List<Vm> vmList, int time) {
		vmArrivalTime = time;
		vmScaler(vmList);
		placementManager();
	}	


	private void createVerticalRamScalingForVm(Vm vm) {
		VerticalVmScalingSimple verticalRamScaling = new VerticalVmScalingDAAS(Ram.class, 0.01);
		/*
		 * By uncommenting the line below, you will see that, instead of gradually
		 * increasing or decreasing the RAM, when the scaling object detects the RAM
		 * usage is above or below the defined thresholds, it will automatically
		 * calculate the amount of RAM to add/remove to move the VM from the over or
		 * underload condition.
		 */
		// verticalRamScaling.setResourceScaling(new ResourceScalingInstantaneous());
		verticalRamScaling.setLowerThresholdFunction(this::lowerRamUtilizationThreshold);
		verticalRamScaling.setUpperThresholdFunction(this::upperRamUtilizationThreshold);
		vm.setRamVerticalScaling(verticalRamScaling);
	}

	/**
	 * Defines the minimum RAM utilization percentage that indicates a Vm is
	 * underloaded. This function is using a statically defined threshold, but it
	 * would be defined a dynamic threshold based on any condition you want. A
	 * reference to this method is assigned to each Vertical VM Scaling created.
	 *
	 * @param vm the VM to check if its RAM underloaded. The parameter is not being
	 *           used internally, that means the same threshold is used for any Vm.
	 * @return the lower RAM utilization threshold
	 */
	private double lowerRamUtilizationThreshold(Vm vm) {
		final List<Long> Ramhistory = new ArrayList<>();
		final Map<Integer, Long> vmRamUtilization = getVmRamUtilizationHistory().get(vm);
		Ramhistory.addAll(vmRamUtilization.values());
		List<Double> RamHistory = Ramhistory.stream().map(Double::valueOf).collect(Collectors.toList());
		
		double Threshold = Ramhistory.size() > 10 ? (MathUtil.median(RamHistory) / vm.getRam().getCapacity()) * 0.4 : 0.4;//0.8
		return Threshold;
	}

	/**
	 * Defines the maximum RAM utilization percentage that indicates a Vm is
	 * overloaded. This function is using a statically defined threshold, but it
	 * would be defined a dynamic threshold based on any condition you want. A
	 * reference to this method is assigned to each Vertical VM Scaling created.
	 *
	 * @param vm the VM to check if its RAM is overloaded. The parameter is not
	 *           being used internally, that means the same threshold is used for
	 *           any Vm.
	 * @return the upper RAM utilization threshold
	 */
	private double upperRamUtilizationThreshold(Vm vm) {
		final List<Long> Ramhistory = new ArrayList<>();
		final Map<Integer, Long> vmRamUtilization = getVmRamUtilizationHistory().get(vm);
		Ramhistory.addAll(vmRamUtilization.values());
		List<Double> RamHistory = Ramhistory.stream().map(Double::valueOf).collect(Collectors.toList());

		double Threshold = Ramhistory.size() > 10 ? (MathUtil.median(RamHistory) / vm.getRam().getCapacity()) * 1.4 : 0.8;//1.2
		return Threshold;
	}
}
