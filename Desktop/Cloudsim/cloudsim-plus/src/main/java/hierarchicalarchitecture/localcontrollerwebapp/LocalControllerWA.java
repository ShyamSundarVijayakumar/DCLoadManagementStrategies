/**
 * 
 */
package hierarchicalarchitecture.localcontrollerwebapp;


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
import org.cloudsimplus.autoscaling.resources.ResourceScalingInstantaneous;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hierarchicalarchitecture.globalcontroller.GlobalController;



/**
 * @author Shyam Sundar V
 *
 */
public class LocalControllerWA {
	public static List<Host> hostListWebApplication = new LinkedList<>();
	public static Set<Host> OverLoadedHostssetWebApplication = new HashSet<Host>();
	public static Set<Host> UnderLoadedHostssetWebApplication = new HashSet<Host>();
	public static List<Vm> vmListWebApplication = new ArrayList<>();	
	DatacenterSimple  Datacenter;
	CloudSim Simulation;
	GlobalController GC ;
	public static Map<Integer, Integer> vmToHostMapInitialPlacement;
	public static Map<Long, Long> bestDynamicVmServerMap;
	//public Map<Long, ArrayList<Long>> serverVmMapDynamic;
	public static List<Vm> VmstoMigrateFromOverloadedAndUnderloadedHosts;
	public static List<Vm> VmstoMigrateFromOverloadedHosts = new ArrayList<>();
	public static Map<Long, Long> sourceVMHostMap; // = new HashMap<Long, Long>();
	public static Map<Long, Double> vmsCurrentCPUUtil; // = new HashMap<Long, Double>();
	public static Map<Long, Long> vmsCurrentRAMUtil; // = new HashMap<Long, Long>();
	public static Map<Long, Double> hostCurrentUtil; // = new HashMap<Long, Double>();
	public static List<Long> targetHostList; // = new ArrayList<Long>();
	
	/**
	 * @param DC
	 * @param simulation
	 * @param GlobalController
	 */
	public void setDcSimulationGlobalcontroller(DatacenterSimple DC,CloudSim simulation,GlobalController GlobalController) {
		this.Simulation = simulation;
		this.Datacenter = DC;
		this.GC = GlobalController;
	//	hostClassifier(); // For activating the listner
	}
	
	String description="Hosts from Webapplication cluster";
	/**
	 * @return
	 */
	private List<Host> getHostlistWebApplication() {		
		return Datacenter.getHostList().stream().filter(host -> host.getDescription() == description).collect(Collectors.toList());
	}	
	
	
	int previousTime = 0;
	public int currentTime;
	public int nextSchedulinginterval = 300;
	public Map<Vm, Map<Integer, Long>> WAVmsRamUtilizationHistory;
	/**
	 * @param evt
	 */
	public void classifyActiveHostsInWebApplicationCluster(int time){
		
		currentTime = time;
		if((currentTime != 0) && (currentTime != previousTime)&&(nextSchedulinginterval == currentTime)) {
			previousTime = time;
			nextSchedulinginterval = currentTime + 300;
	//		System.out.println("Time in lcwa :"+ time);
			WAVmsRamUtilizationHistory = getVmRamUtilizationHistory();
			double HostUpperUtilizationThreshold = GlobalController.HostUpperUtilizationThresholdWA;
			double HostLowerUtilizationThreshold = GlobalController.HostLowerUtilizationThresholdWA;
			hostListWebApplication = getHostlistWebApplication();
			HostClassifierWA CategoriseHost= new HostClassifierWA();
			CategoriseHost.SetHostUpperAndLowerUtilizationThreshold(HostUpperUtilizationThreshold, HostLowerUtilizationThreshold);
			OverLoadedHostssetWebApplication = CategoriseHost.getOverloadedHosts(hostListWebApplication);
			if(!OverLoadedHostssetWebApplication.isEmpty()) {
				printOverUtilizedHosts(time, OverLoadedHostssetWebApplication);
				checkIfHostshaveOnlyOneExtraLargrVm(OverLoadedHostssetWebApplication);
			}
			
			UnderLoadedHostssetWebApplication = CategoriseHost.getUnderLoadedHosts(hostListWebApplication);		
			
			if(!UnderLoadedHostssetWebApplication.isEmpty()) {
				printUnderUtilizedHosts(time, UnderLoadedHostssetWebApplication);
			}
			if(!OverLoadedHostssetWebApplication.isEmpty() || !UnderLoadedHostssetWebApplication.isEmpty()) {
				GC.GlobalDecisionMakerWA(this);		
			}
			
		}
	}
	private void checkIfHostshaveOnlyOneExtraLargrVm(Set<Host> OverLoadedHostsWebApplication) {
		Set<Host> hostsTobeRemoved =  new HashSet<Host>();

		for(Host host : OverLoadedHostsWebApplication) {
			if(host.getVmList().size() == 1) {
	//			Vm vm = host.getVmList().get(0);
	//			if(vm.getRam().getCapacity() >= 3480) {
					/*
					 * With auto scaling in effect, if a vm has more than 3480 mb of ram as its
					 * capacity and because of it the host got into violation then there is no
					 * alternative host for that vm. Both the types of hosts are given with 4 gb as
					 * its capacity. Even with the migration of that vm to the other will bring
					 * the other host to overloaded state. hence those vm's will not be migrated.
					 */
					printNoAlternativeHostForVm(host);
					hostsTobeRemoved.add(host);
		//		}
			}
		}
		
		for(Host host : hostsTobeRemoved) {
			OverLoadedHostssetWebApplication.remove(host);
		}
		
	}
	
	private void printNoAlternativeHostForVm(Host host) {
		LOGGER.warn("No alternative host for Extra large vm : {} in host {}", host.getVmList().get(0).getId(), host);
	}
	
	public Map<Vm, Map<Integer, Long>> getVmRamUtilizationHistory() {
		return ModelConstructionForApplications.ModelConstruction.WAVmsRamUtilizationHistory;
	}
	
	
	 Logger LOGGER = LoggerFactory.getLogger(LocalControllerWA.class.getSimpleName());
	  /* Prints the over utilized hosts.
	   *
	   * @param overloadedHosts the over utilized hosts
	   */
	private void printOverUtilizedHosts(int time, final Set<Host> overloadedHosts) {
		if (!overloadedHosts.isEmpty() && LOGGER.isWarnEnabled()) {
			final String hosts = overloadedHosts.stream().map(this::overloadedHostToString).collect(Collectors.joining(System.lineSeparator()));
			LOGGER.warn("{}: Local controller web app: Overloaded hosts in web application cluster{}:{}{}",
					time, overloadedHosts.stream().findFirst().get().getDatacenter(), System.lineSeparator(), hosts);
		}
	}
	
	private void printUnderUtilizedHosts(int time, final Set<Host> underloadedHosts) {
		if (!underloadedHosts.isEmpty() && LOGGER.isWarnEnabled()) {
			final String hosts = underloadedHosts.stream().map(this::UnderloadedHostToString).collect(Collectors.joining(System.lineSeparator()));
			LOGGER.warn("{}: Local controller web app: Underloaded hosts in web application cluster{}:{}{}",
					time, underloadedHosts.stream().findFirst().get().getDatacenter(), System.lineSeparator(), hosts);
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
				host.getId(), GlobalController.HostUpperUtilizationThresholdWA , host.getCpuPercentUtilization(), HostRamUtil / HostCap);
	}
	 
	private String UnderloadedHostToString(final Host host) {
		double HostRamUtil = 0;
		double HostCap = host.getRam().getCapacity();
		for(Vm vm : host.getVmList()) {
			HostRamUtil += vm.getRam().getAllocatedResource();		
		}
	  	   
		return String.format(
				"      Host %d (Lower threshold %.2f, CPU utilization: %.2f, Ram utilization: %.2f)",
				host.getId(), GlobalController.HostLowerUtilizationThresholdWA , host.getCpuPercentUtilization(), HostRamUtil / HostCap);
	}
	
	/**
	 * @param Hostlist
	 */
	public void SetHostlistWebApp(List<Host> Hostlist){
		hostListWebApplication = Hostlist;
	}

	/**
	 * 
	 */
	public void placementManager() {
		GADriverWA GaWA = new GADriverWA();
		GaWA.gaDriverInitialPlacement();
		vmToHostMapInitialPlacement = GaWA.vmToHostMapWebApp;//new GeneticAlgorithmDriverDaas().vmToHostMapDaas;
		scheduler(vmToHostMapInitialPlacement);	
	}
	

	/**
	 * @param InitialvmListWA
	 * @param VmlisttoMigrate
	 * @return
	 */
	public boolean GAChooseSourceMapWA;
	public Map<Long, Long> placementManagerForDynamicPlacement(List<Vm> InitialvmListWA, List<Vm> VmlisttoMigrate, List<Vm> VmlisttoMigrateFromOverloadedHosts) {
		vmListWebApplication = InitialvmListWA;
		VmstoMigrateFromOverloadedAndUnderloadedHosts = new LinkedList<>();
		VmstoMigrateFromOverloadedHosts = new ArrayList<>();
		VmstoMigrateFromOverloadedAndUnderloadedHosts = VmlisttoMigrate;
		VmstoMigrateFromOverloadedHosts = VmlisttoMigrateFromOverloadedHosts;
	
		if((!OverLoadedHostssetWebApplication.isEmpty()) || (!UnderLoadedHostssetWebApplication.isEmpty())
				|| (VmstoMigrateFromOverloadedAndUnderloadedHosts != null)) {
			sendVMCharToGA();
			
			if((!OverLoadedHostssetWebApplication.isEmpty()) || (!UnderLoadedHostssetWebApplication.isEmpty()) || (!VmlisttoMigrate.isEmpty())) {	
				activeHosts();
				GADriverWA GA = new GADriverWA();
				bestDynamicVmServerMap = GA.dynamicGA(sourceVMHostMap, targetHostList, hostListWebApplication, vmListWebApplication);
				GAChooseSourceMapWA = GA.GAChooseSourceMapWA;
				if(numberOfActiveHosts <= GA.serverVmMapDynamic.size() && (OverLoadedHostssetWebApplication.isEmpty())) {
					bestDynamicVmServerMap = sourceVMHostMap;
				}		
			}
		}	
		return bestDynamicVmServerMap;
	}
	
	int numberOfActiveHosts;
	private void activeHosts() {
		numberOfActiveHosts=0;
		hostListWebApplication.forEach(host ->{
			if(host.isActive() && host.getVmList().size()!=0) {
				numberOfActiveHosts += 1;
			}
		});
	}
	
	public void SetPreviousMapAsCurrentMap() {
		for (final Map.Entry<Long, Long> entryDaas : bestDynamicVmServerMap.entrySet()) {
			Vm vm = vmListWebApplication.get(entryDaas.getKey().intValue());
			if(vm.isCreated()) {
				Host host = hostListWebApplication.get(entryDaas.getValue().intValue());
				bestDynamicVmServerMap.put(vm.getId(), host.getId());
			}
		}
	}
	
	/**
	 * @param targetHostList
	 */
	public void SendTargerhostlistDynamicPlacement(List<Host> TargetHostList) {
		targetHostList = new ArrayList<Long>();
		TargetHostList.forEach(host -> {
			targetHostList.add(host.getId());
		});
	}
	
	/**
	 * @param serverUtil
	 */
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
	
	/**
	 * Shows updates every time the simulation clock advances.
	 * 
	 * @param evt information about the event happened (that for this Listener is
	 *            just the simulation time)
	 */
	public void sendVMCharToGA() {
		sourceVMHostMap = new HashMap<Long, Long>();
		vmsCurrentCPUUtil = new HashMap<Long, Double>();
		vmsCurrentRAMUtil = new HashMap<Long, Long>();
	
		vmListWebApplication.forEach(vm -> {
			Host sourceHost = vm.getHost();
			if(!vm.isFailed()&&vm.isWorking() && sourceHost.getVmList().contains(vm)) {
				sourceHost = ifVmisinMigrationGetTargetHost(vm, sourceHost);
				sourceVMHostMap.put(vm.getId(), sourceHost.getId());
				vmsCurrentCPUUtil.put(vm.getId(),vm.getTotalCpuMipsUtilization());
				vmsCurrentRAMUtil.put(vm.getId(), vm.getCurrentRequestedRam());
			}if(!sourceHost.getVmList().contains(vm)) {
				sourceVMHostMap.remove(vm.getId());
				vmsCurrentCPUUtil.remove(vm.getId());
				vmsCurrentRAMUtil.remove(vm.getId());
			}
		});
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
					sourceHost = hostListWebApplication.get(entryDaas.getValue().intValue());			
				}
			}
		}
		return sourceHost;
	}
	
	double	hostCpuUtil;
	public void sendHostUtilToGA() {
		hostCurrentUtil = new HashMap<Long, Double>();
		targetHostList = new ArrayList<Long>();
		
		hostListWebApplication.forEach(host ->{
			if(host.isActive() && host.getVmList().size()!=0) {
				hostCpuUtil = host.getCpuPercentUtilization();
				/*
				 * If the host is not overloaded, add it to the target host list
				 */
					if(!targetHostList.contains(host.getId())&& !OverLoadedHostssetWebApplication.contains(host)) {
						targetHostList.add(host.getId());
				}
			} else {
				targetHostList.remove(host.getId());
			}
			hostCurrentUtil.put(host.getId(), hostCpuUtil);
		});
	}

	
	/**
	 * @param vmToHostMapWA
	 */
	public void scheduler(Map<Integer, Integer> vmToHostMapWA) {
		vmToHostMapInitialPlacement = vmToHostMapWA;
	}
	
	/**
	 * @param vmList
	 */
	public void VmListForPlacement(List<Vm> vmList) {
		vmListWebApplication = vmList;
		placementManager();
		for(Vm vm : vmList) {
			createVerticalRamScalingForVm(vm);
		}
	}
	
	private void createVerticalRamScalingForVm(Vm vm) {
		VerticalVmScalingSimple verticalRamScaling = new VerticalVmScalingWA(Ram.class, 0.1);
		/*
		 * By uncommenting the line below, you will see that, instead of gradually
		 * increasing or decreasing the RAM, when the scaling object detects the RAM
		 * usage is above or below the defined thresholds, it will automatically
		 * calculate the amount of RAM to add/remove to move the VM from the over or
		 * underload condition.
		 */
		verticalRamScaling.setResourceScaling(new ResourceScalingInstantaneous());
	//	verticalCpuScaling.setResourceScaling(vs -> 2*vs.getScalingFactor()*vs.getAllocatedResource());
		verticalRamScaling.setLowerThresholdFunction(this::lowerRamUtilizationThreshold);
		verticalRamScaling.setUpperThresholdFunction(this::upperRamUtilizationThreshold);
		vm.setPeVerticalScaling(verticalRamScaling);
	}

	/**
	 * Defines the minimum cpu utilization percentage that indicates a Vm is
	 * underloaded. This function is using a statically defined threshold, but it
	 * would be defined a dynamic threshold based on any condition you want. A
	 * reference to this method is assigned to each Vertical VM Scaling created.
	 *
	 * @param vm the VM to check if its cpu underloaded. The parameter is not being
	 *           used internally, that means the same threshold is used for any Vm.
	 * @return the lower RAM utilization threshold
	 */
	private double lowerRamUtilizationThreshold(Vm vm) {
		final List<Long> Ramhistory = new ArrayList<>();
		final Map<Integer, Long> vmRamUtilization = getVmRamUtilizationHistory().get(vm);
		Ramhistory.addAll(vmRamUtilization.values());
		List<Double> RamHistory = Ramhistory.stream().map(Double::valueOf).collect(Collectors.toList());
		
		double Threshold = Ramhistory.size() > 10 ? (MathUtil.median(RamHistory) / vm.getRam().getCapacity()) * 0.4 : 0.15;//0.2 0.15
		return Threshold;		
	}

	/**
	 * Defines the maximum cpu utilization percentage that indicates a Vm is
	 * overloaded. This function is using a statically defined threshold, but it
	 * would be defined a dynamic threshold based on any condition you want. A
	 * reference to this method is assigned to each Vertical VM Scaling created.
	 *
	 * @param vm the VM to check if its cpu is overloaded. The parameter is not
	 *           being used internally, that means the same threshold is used for
	 *           any Vm.
	 * @return the upper RAM utilization threshold
	 */
	private double upperRamUtilizationThreshold(Vm vm) {
		final List<Long> Ramhistory = new ArrayList<>();
		final Map<Integer, Long> vmRamUtilization = getVmRamUtilizationHistory().get(vm);
		Ramhistory.addAll(vmRamUtilization.values());
		List<Double> RamHistory = Ramhistory.stream().map(Double::valueOf).collect(Collectors.toList());

		double Threshold = Ramhistory.size() > 10 ? (MathUtil.median(RamHistory) / vm.getRam().getCapacity()) * 1.6 : 0.8;
		return Threshold;
	}
}
