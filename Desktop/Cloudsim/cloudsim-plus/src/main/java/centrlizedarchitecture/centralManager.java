/**
 * 
 */
package centrlizedarchitecture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimpleCM;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.autoscaling.VerticalVmScalingSimple;
import org.cloudsimplus.autoscaling.resources.ResourceScalingInstantaneous;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hierarchicalarchitecture.localcontrollerwebapp.LocalControllerWA;


/**
 * @author Shyam Sundar V
 *
 */
public class centralManager {
	public static List<Host> hostListWebApplication = new LinkedList<>();
	public static List<Host> hostListDaas = new LinkedList<>();
	public DatacenterBrokerSimple Customer_A;
	public DatacenterBrokerSimple Customer_B;
	
	private int DaasVmType1 = 1;
	private int DaasVmType2 = 2;
	private int DaasVmType3 = 3;
	private int DaasVmType4 = 4;
	private int DaasVmType5 = 5;
	private int HighcpuMedium = 1;
	private int Extralarge = 2;
	private int Small = 3;
	private int Micro = 4;
	public static List<Vm> InitialvmListWebApplication = new ArrayList<>();
	public static List<Vm> InitialvmListDaas;
	List<Vm> FinishesVmlistDay1Daas = new ArrayList<Vm>();
	public static Map<Integer, Integer> vmToHostMapInitialPlacementWA;
	DatacenterSimpleCM  Datacenter;
	CloudSim Simulation;
	public static double HostUpperUtilizationThresholdDAAS;// = 0.85;
	public static double HostLowerUtilizationThresholdDAAS;// = 0.15;	
	public static double HostUpperUtilizationThresholdWA;// = 0.85;
	public static double HostLowerUtilizationThresholdWA;// = 0.15;
	public static Set<Host> OverLoadedHostssetWebApplication = new HashSet<Host>();
	public static Set<Host> UnderLoadedHostssetWebApplication = new HashSet<Host>();
	public Map<Long, Double> serverUtil = new HashMap<Long, Double>();
	private Host hostWithMinUtili;
	private boolean AggressiveConsolidation = false;
	private boolean ConsolidationWithoutAdditionalHosts;
	private double Availablemips;
	private double Availableram;
	private double Availablebw;
	private Set<Vm> vm;
	private Map<Long, Double> serverUtilMapDuplicate;
	private Host HostMinUtil;
	private long hostWithMinUtil;
	private List<Host> bestMinUtilHost = new ArrayList<Host>();
	public static Map<Long, Long> bestDynamicVmServerMapWA;
	public static List<Vm> VmstoMigrateFromOverAndUnderloadedHostsWA;
	public static List<Vm> VmstoMigrateFromOverUnderloadedHostsDAAS;
	public static List<Vm> VmstoMigrateFromOverloadedHostsWA = new ArrayList<>();
	public static List<Vm> VmsToMigrateWA = new ArrayList<Vm>();
	public static List<Vm> VmsToMigrateFromOverloadedHostsDAAS = new ArrayList<Vm>();

	public static Map<Long, Long> sourceVMHostMapWA; 
	public static Map<Long, Double> vmsCurrentCPUUtil; 
	public static Map<Long, Long> vmsCurrentRAMUtil; 
	public static Map<Long, Double> hostCurrentUtil; 
	public static List<Long> targetHostListWA; 
	public static Set<Host> OverLoadedHostssetDaas = new HashSet<Host>();
	public static Set<Host> UnderLoadedHostssetDaas = new HashSet<Host>();
	private static Map<Long, Long> sourceVMHostMapDAAS; 
	public static List<Long> targetHostListDAAS; 
	public static Map<Long, Long> bestDynamicVmServerMapDAAS;
	public Map<Long, ArrayList<Long>> dynamicserverVmMapDAAS;
	public static Map<Integer, Integer> vmToHostMapInitialPlacementDAAS;
	public static List<Vm> VmsToMigrateDAAS = new ArrayList<Vm>();
	
	private static final String CUSTOMER_A_SLA_CONTRACT = "centrlizedarchitecture/SLAWebApp.json";
	private static final String CUSTOMER_B_SLA_CONTRACT = "centrlizedarchitecture/SLADAAS.json";
   

    private SLAContract contractA_WA;
    private SLAContract contractB_DAAS;
    RequestAnalyserAndConfigurationManagerDaaS RACMDaaS ;
	/**
	 * This method is used to initiate virtual machine and cloudlet  creation for the appropriate datacenter brokers.
	 * 
	 * @param simulation
	 */
	public void modelUserRequests(CloudSim simulation) {
		this.contractA_WA = SLAContract.getInstance(CUSTOMER_A_SLA_CONTRACT);
		this.contractB_DAAS = SLAContract.getInstance(CUSTOMER_B_SLA_CONTRACT);
		HostUpperUtilizationThresholdWA = contractA_WA.getHostCpuUtilizationMetric().getMaxDimension().getValue();
		HostLowerUtilizationThresholdWA = contractA_WA.getHostCpuUtilizationMetric().getMinDimension().getValue();
		HostUpperUtilizationThresholdDAAS = contractB_DAAS.getHostCpuUtilizationMetric().getMaxDimension().getValue();
		HostLowerUtilizationThresholdDAAS = contractB_DAAS.getHostCpuUtilizationMetric().getMinDimension().getValue();
		
		System.out.println(" Customer A  arrives : (Web application)");

		RequestAnalyserAndConfigurationManagerWebApplication createCloudletandVmPlanetLab = new RequestAnalyserAndConfigurationManagerWebApplication();

		
		  createCloudletandVmPlanetLab.createOneVmAndCloudlet(100, HighcpuMedium, Customer_A);//100 
		  createCloudletandVmPlanetLab.createOneVmAndCloudlet(100, Extralarge, Customer_A);//100
		  createCloudletandVmPlanetLab.createOneVmAndCloudlet(200, Small,  Customer_A);// 300 
		  createCloudletandVmPlanetLab.createOneVmAndCloudlet(300, Micro, Customer_A);//300s
		  setInitialVmlistWebApp(createCloudletandVmPlanetLab.getVmListWebApplication());
		  
		  System.out.printf("# %s submitted %d VMs \n", Customer_A, InitialvmListWebApplication.size());
		  // Initialization: Desktop as a service----------> create virtual machines and  cloudlets
		  System.out.println(" Customer B arrives : (Desktop as a service)");

		  RequestAnalyserAndConfigurationManagerDaaS createCloudletsandVmDaaS = new RequestAnalyserAndConfigurationManagerDaaS();
		  createCloudletsandVmDaaS.setCentralManager(this);
		  createCloudletsandVmDaaS.createVm(30, DaasVmType1, Customer_B, -1, simulation);// 30 
		  createCloudletsandVmDaaS.createVm(30, DaasVmType2, Customer_B, -1,simulation);//30 
		  createCloudletsandVmDaaS.createVm(70, DaasVmType3,Customer_B, -1,simulation);//90 
		  createCloudletsandVmDaaS.createVm(40,DaasVmType4, Customer_B, -1,simulation);//40
		  createCloudletsandVmDaaS.createVm(50, DaasVmType5, Customer_B,-1,simulation);//50
		  RACMDaaS = createCloudletsandVmDaaS;
		  setInitialVmlistDaas(createCloudletsandVmDaaS.getvmQueue());
		  System.out.printf("# %s submitted %d VMs \n", Customer_B, InitialvmListDaas.size());
		  Customer_B.setVmDestructionDelay(0);
		  VmListForPlacementWA(InitialvmListWebApplication);
		  VmListForPlacementDAAS(InitialvmListDaas);

	}
	
	public List<Vm> getfinishedVmlistDaas(){
		return RACMDaaS.FinishedvmList;
	}
	public void setDcSimulation(DatacenterSimpleCM dc, CloudSim simulation) {
		this.Simulation = simulation;
		this.Datacenter = dc;
	//	hostClassifier(); // For activating the listner
	}
	/* activating listners here delays the implementation of current decisions. for instance decisons taken in time 300 gets implemented at 600 and so on.
	 * public void hostClassifier() { // Simulation.addOnClockTickListener(this::
	 * classifyActiveHostsInWebApplicationCluster);
	 * //Simulation.addOnClockTickListener(this::ClassifyActiveHostsInDaasCluster);
	 * }
	 */
	
	int previousTimeDAAS = 0;
	public int currentTimeDAAS;
	public int nextSchedulinginterval = 300;
	public Map<Vm, Map<Integer, Long>> DAASVmsRamUtilizationHistory;
	public void ClassifyActiveHostsInDaasCluster(int time) {
			
		currentTimeDAAS = time;
		if ((currentTime != 0) && (currentTimeDAAS != previousTimeDAAS) &&(nextSchedulinginterval == currentTimeDAAS) && (vmArrivalTime != currentTimeDAAS)) {
				previousTimeDAAS = time;
				nextSchedulinginterval = currentTimeDAAS + 300;
				hostListDaas = getHostlistDaas();
				DAASVmsRamUtilizationHistory = getVmRamUtilizationHistoryDAAS();
				if(!OnlyOneHostHasVmsOrNoHostHasVms()) {
					HostClassifierDAAS CategoriseHost = new HostClassifierDAAS();
					CategoriseHost.SetHostUpperAndLowerUtilizationThreshold(HostUpperUtilizationThresholdDAAS,HostLowerUtilizationThresholdDAAS);
					OverLoadedHostssetDaas = CategoriseHost.getOverloadedHosts(hostListDaas);
					UnderLoadedHostssetDaas = CategoriseHost.getUnderLoadedHosts(hostListDaas);
					if(!OverLoadedHostssetDaas.isEmpty()) {
						printOverUtilizedHosts(time, OverLoadedHostssetDaas);
					}
					
					if(!UnderLoadedHostssetDaas.isEmpty()) {
						printUnderUtilizedHosts(time, UnderLoadedHostssetDaas);
					}
					if(!UnderLoadedHostssetDaas.isEmpty() || !OverLoadedHostssetDaas.isEmpty()) {
						globalDecisionMakerDAAS();
					}
				}
			}
		}
	
	private void printUnderUtilizedHosts(int time, final Set<Host> UnderloadedHosts) {
		if (!UnderloadedHosts.isEmpty() && LOGGER.isWarnEnabled()) {
			final String hosts = UnderloadedHosts.stream().map(this::UnderloadedHostToString).collect(Collectors.joining(System.lineSeparator()));
			LOGGER.warn("{}: Local controller DaaS: Underloaded hosts in Daas cluster{}:{}{}",
					time, UnderloadedHosts.stream().findFirst().get().getDatacenter(), System.lineSeparator(), hosts);
	        }
	    }
	Logger LOGGER = LoggerFactory.getLogger(centralManager.class.getSimpleName());
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
	
	private String overloadedHostToString(final Host host) {
		double HostRamUtil = 0;
		double HostCap = host.getRam().getCapacity();
		for(Vm vm : host.getVmList()) {
			HostRamUtil += vm.getRam().getAllocatedResource();		
		}
	  	   
		return String.format(
				"      Host %d (upper threshold %.2f, CPU utilization: %.2f, Ram utilization: %.2f)",
				host.getId(), HostUpperUtilizationThresholdDAAS , host.getCpuPercentUtilization(), HostRamUtil / HostCap);
	}
	
	private String UnderloadedHostToString(final Host host) {
		double HostRamUtil = 0;
		double HostCap = host.getRam().getCapacity();
		for(Vm vm : host.getVmList()) {
			HostRamUtil += vm.getRam().getAllocatedResource();		
		}
	  	   
		return String.format(
				"      Host %d (lower threshold %.2f, CPU utilization: %.2f, Ram utilization: %.2f)",
				host.getId(), HostLowerUtilizationThresholdDAAS , host.getCpuPercentUtilization(), HostRamUtil / HostCap);
	}
	String descriptionDAAS = "Hosts from Desktop as a Service";
	private List<Host> getHostlistDaas() {
		return Datacenter.getHostList().stream().filter(host -> host.getDescription() == descriptionDAAS)
				.collect(Collectors.toList());
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
		} else {
			j = false;
		}
		return j;
	}
	
	int previousTime = 0;
	public int currentTime;
	int i=0;
	public Map<Vm, Map<Integer, Long>> WAVmsRamUtilizationHistory;
	/**
	 * @param evt
	 */
	public void classifyActiveHostsInWebApplicationCluster(int time){
		
		currentTime = time;
		if((currentTime != 0) && (currentTime != previousTime)) {
			previousTime = time;
			WAVmsRamUtilizationHistory = getVmRamUtilizationHistory();
			hostListWebApplication = getHostlistWebApplication();
			HostClassifierWA CategoriseHost= new HostClassifierWA();
			CategoriseHost.SetHostUpperAndLowerUtilizationThreshold(HostUpperUtilizationThresholdWA, HostLowerUtilizationThresholdWA);
			OverLoadedHostssetWebApplication = CategoriseHost.getOverloadedHosts(hostListWebApplication);
			if(!OverLoadedHostssetWebApplication.isEmpty()) {
				printOverUtilizedHostsWA(time, OverLoadedHostssetWebApplication);
				checkIfHostshaveOnlyOneExtraLargrVm(OverLoadedHostssetWebApplication);
			}
			if(!UnderLoadedHostssetWebApplication.isEmpty()) {
				printUnderUtilizedHostsWA(time, UnderLoadedHostssetWebApplication);
			}
			UnderLoadedHostssetWebApplication = CategoriseHost.getUnderLoadedHosts(hostListWebApplication);
			if(!OverLoadedHostssetWebApplication.isEmpty() && !UnderLoadedHostssetWebApplication.isEmpty()) {
				DecisionMakerWA();	
			}
		}
	}

	  /* Prints the over utilized hosts.
	   *
	   * @param overloadedHosts the over utilized hosts
	   */
	private void printOverUtilizedHostsWA(int time, final Set<Host> overloadedHosts) {
		if (!overloadedHosts.isEmpty() && LOGGER.isWarnEnabled()) {
			final String hosts = overloadedHosts.stream().map(this::overloadedHostToStringWA).collect(Collectors.joining(System.lineSeparator()));
			LOGGER.warn("{}: Local controller web app: Overloaded hosts in web application cluster{}:{}{}",
					time, overloadedHosts.stream().findFirst().get().getDatacenter(), System.lineSeparator(), hosts);
	        }
	    }

	private void printUnderUtilizedHostsWA(int time, final Set<Host> underloadedHosts) {
		if (!underloadedHosts.isEmpty() && LOGGER.isWarnEnabled()) {
			final String hosts = underloadedHosts.stream().map(this::underloadedHostToStringWA).collect(Collectors.joining(System.lineSeparator()));
			LOGGER.warn("{}: Local controller web app: Underloaded hosts in web application cluster{}:{}{}",
					time, underloadedHosts.stream().findFirst().get().getDatacenter(), System.lineSeparator(), hosts);
	        }
	    }
	private String overloadedHostToStringWA(final Host host) {
		double HostRamUtil = 0;
		double HostCap = host.getRam().getCapacity();
		for(Vm vm : host.getVmList()) {
			HostRamUtil += vm.getRam().getAllocatedResource();		
		}
	  	   
		return String.format(
				"      Host %d (upper threshold %.2f, CPU utilization: %.2f, Ram utilization: %.2f)",
				host.getId(), HostUpperUtilizationThresholdWA , host.getCpuPercentUtilization(), HostRamUtil / HostCap);
	}
	
	private String underloadedHostToStringWA(final Host host) {
		double HostRamUtil = 0;
		double HostCap = host.getRam().getCapacity();
		for(Vm vm : host.getVmList()) {
			HostRamUtil += vm.getRam().getAllocatedResource();		
		}
	  	   
		return String.format(
				"      Host %d (Lower threshold %.2f, CPU utilization: %.2f, Ram utilization: %.2f)",
				host.getId(), HostLowerUtilizationThresholdWA , host.getCpuPercentUtilization(), HostRamUtil / HostCap);
	}
	
	/**
	 * @param vmList
	 */
	public void VmListForPlacementWA(List<Vm> vmList) {
		placementManagerWA();
		for(Vm vm : vmList) {
			createVerticalRamScalingForVmWA(vm);
		}
		
	}
	private void checkIfHostshaveOnlyOneExtraLargrVm(Set<Host> overLoadedHostsWebApplication) {
		Set<Host> hostsTobeRemoved =  new HashSet<Host>();

		for(Host host : overLoadedHostsWebApplication) {
			if(host.getVmList().size() == 1) {
	//			Vm vm = host.getVmList().get(0);
		//		if(vm.getRam().getCapacity() >= 3480) {
					/*
					 * With auto scaling in effect, if a vm has more than 3480 mb of ram as its
					 * capacity and because of it the host got into violation then there is no
					 * alternative host for that vm. Both the types of hosts are given with 4 gb as
					 * its capacity. Even with the migration of that vm to the other will bring
					 * the other host to overloaded state. hence those vm's will not be migrated.
					 */
					printNoAlternativeHostForVm(host);
					hostsTobeRemoved.add(host);
			//	}
			}
		}
		
		for(Host host : hostsTobeRemoved) {
			OverLoadedHostssetWebApplication.remove(host);
		}
	}
	
	private void printNoAlternativeHostForVm(Host host) {
		LOGGER.warn("No alternative host for Extra large vm : {} in host {}", host.getVmList().get(0).getId(), host);
	}
	
	String description="Hosts from Webapplication cluster";
	/**
	 * @return
	 */
	private List<Host> getHostlistWebApplication() {		
		return Datacenter.getHostList().stream().filter(host -> host.getDescription() == description).collect(Collectors.toList());
	}
	
	public Map<Vm, Map<Integer, Long>> getVmRamUtilizationHistory() {
		return ModelConstructionForApplications.ModelConstruction.WAVmsRamUtilizationHistory;
	}
	
	public void placementManagerWA() {
		GADriverWA GaWA = new GADriverWA();
		GaWA.gaDriverInitialPlacement();
		schedulerWA(GaWA.vmToHostMapWebApp);	
	}
	
	/**
	 * @param vmToHostMapWA
	 */
	public void schedulerWA(Map<Integer, Integer> vmToHostMapWA) {
		vmToHostMapInitialPlacementWA = vmToHostMapWA;
	}
	
	int vmArrivalTime = 0;
	/**
	 * This method updates the InitialvmListDaas when the vm starts to arrive after the thinking time. The initially finished vms will be collected in
	 *  the FinishesVmlistDay1Daas.
	 * @param Vmlist
	 */
	public void updateVmListDaasAfterOffline(List<Vm> Vmlist, int time){
		vmArrivalTime = time;
		for(Vm Finishedvm : InitialvmListDaas) {
			if(!Finishedvm.isCreated()) {
				FinishesVmlistDay1Daas.add(Finishedvm); 
			}
		}		  
		if(InitialvmListDaas.size() >= 200) {
			InitialvmListDaas = new ArrayList<>();
		}
		if(!Vmlist.isEmpty()) {
			for(Vm NewVm : Vmlist) { 
				if(!InitialvmListDaas.contains(NewVm)) {
					InitialvmListDaas.add(NewVm);	
				}  
			}
		}
	}
	
	/**
	 * @param Vmlist
	 * @see #modelUserRequests(CloudSim) for vm creation 
	 */
	public void setInitialVmlistWebApp(List<Vm> Vmlist) {
		InitialvmListWebApplication = Vmlist;
	}

	/**
	 * @param Vmlist
	 * @see #modelUserRequests(CloudSim) for vm creation
	 */
	public void setInitialVmlistDaas(Queue<Vm> Vmlist) {
		InitialvmListDaas = new ArrayList<>(Vmlist);
	}
	/**
	 * This method is used to create brokers for the simulation.
	 * 
	 * @param simulation
	 */
	public void createDatacenterBrokers(CloudSim simulation) {
		this.Customer_A = new DatacenterBrokerSimple(simulation);
		this.Customer_B = new DatacenterBrokerSimple(simulation);
	}
	
	/**
	 * @param Hostlist
	 */
	public void SetHostlistWebApp(List<Host> Hostlist){
		hostListWebApplication = Hostlist;
	}
	
	/**
	 * @param Hostlist
	 */
	public void SetHostlistDaas(List<Host> Hostlist){
		hostListDaas = Hostlist;
	}
	
	/**
	 * This method helps maintain the load balanced in Web application setup by taking decisions such as aggressive consolidation, consolidation without adding any 
	 * additional hosts, adding additional hosts. 
	 * 
	 */

	public void DecisionMakerWA() {
		 double GlobalUtil = globalUtilizationComputationalUnit(hostListWebApplication);

		if (!OverLoadedHostssetWebApplication.isEmpty() || !UnderLoadedHostssetWebApplication.isEmpty()) {
			VmSelectionPolicyMaxAverageCPURAM VmSelection = new VmSelectionPolicyMaxAverageCPURAM();
			VmSelection.vmSelection(hostListWebApplication, OverLoadedHostssetWebApplication, UnderLoadedHostssetWebApplication,
					WAVmsRamUtilizationHistory, HostUpperUtilizationThresholdWA, HostLowerUtilizationThresholdWA, currentTime);

			VmsToMigrateWA = VmSelection.VmsToMigrate;
			VmstoMigrateFromOverloadedHostsWA = VmSelection.VmsToMigrateFromOverloadedHosts;	
		}else {
			VmsToMigrateWA = new ArrayList<Vm>();
		}
		 
		AggressiveConsolidation = false;
		if((GlobalUtil < 0.65) && !onlyOneHostHasVmsAndIsUnderLoaded(hostListWebApplication,UnderLoadedHostssetWebApplication)) {
			boolean hostHasMigratingInVms = false;
			Set<Vm> vmSet = selectHostwithNoMigratingInVms(hostListWebApplication, hostHasMigratingInVms);

			if((!vmSet.isEmpty()) || (!VmsToMigrateWA.isEmpty())) {
				AggressiveConsolidation = aggressiveConsolidationWA();
			}
		}

		ConsolidationWithoutAdditionalHosts = false;
		if ((AggressiveConsolidation == false) && (!VmsToMigrateWA.isEmpty())
				&& (!onlyOneHostHasVmsAndIsUnderLoaded(hostListWebApplication,UnderLoadedHostssetWebApplication))) {
			ConsolidationWithoutAdditionalHosts = ConsolidationWithoutAdditionalhostsWA();
		}
	
		boolean AllocatedWithAdditionalHosts = false;
		if ((!onlyOneHostHasVmsAndIsUnderLoaded(hostListWebApplication, UnderLoadedHostssetWebApplication))
				&& ((AggressiveConsolidation == false) && (ConsolidationWithoutAdditionalHosts == false))
				&& (!OverLoadedHostssetWebApplication.isEmpty())) {
			AllocatedWithAdditionalHosts = AddAdditionalhostWA();
		}
		
		if ((!onlyOneHostHasVmsAndIsUnderLoaded(hostListWebApplication, UnderLoadedHostssetWebApplication))
				&& ((AggressiveConsolidation == false) && (ConsolidationWithoutAdditionalHosts == false) && (AllocatedWithAdditionalHosts == false))
				&& (!OverLoadedHostssetWebApplication.isEmpty())) {
			SetPreviousMapAsCurrentMapWA();
	//	 System.out.println("Test Check: No case is suitable in GC---------->" );
		}
	}
	
	public void SetPreviousMapAsCurrentMapWA() {
		for (final Map.Entry<Long, Long> entryDaas : bestDynamicVmServerMapWA.entrySet()) {
			Vm vm = InitialvmListWebApplication.get(entryDaas.getKey().intValue());
			if(vm.isCreated()) {
				Host host = hostListWebApplication.get(entryDaas.getValue().intValue());
				bestDynamicVmServerMapWA.put(vm.getId(), host.getId());
			}
		}
	}
	
	/**
	 * This method globalUtilizationComputationalUnit is used for the computation of the global host utilization for any application.
	 * 
	 * @param HostList
	 * @return
	 */	
	  private double globalUtilizationComputationalUnit(List<Host> HostList) {
		  double GlobalClusterUtilization = 0.0; 
		  GlobalUtilizationComputation  GlobalUtilization = new GlobalUtilizationComputation();
		  GlobalUtilization.SetHostlist(HostList);
		  
		  GlobalClusterUtilization = GlobalUtilization.GetGlobalUtilizationBasedOnCPUAndRAM(); 
		  serverUtil =  GlobalUtilization.serverUtil;
		  
		  return GlobalClusterUtilization;
	  }
	  /**
		 * Selects a host if it has no migrating in vm's.
		 * 
		 * @param Hostlist
		 * @param hostHasMigratingInVms
		 * @return
		 */
		private Set<Vm> selectHostwithNoMigratingInVms(List<Host> Hostlist, boolean hostHasMigratingInVms) {
			vm = new HashSet<Vm>();
			serverUtilMapDuplicate = serverUtil;
			if (!serverUtilMapDuplicate.isEmpty()) {
				List<Host> hostlist = hostWithMinUtil(Hostlist, serverUtilMapDuplicate);
				if (!hostlist.isEmpty()) {
					hostWithMinUtili = hostlist.get(0);
					vm.addAll(hostWithMinUtili.getVmList());
				}
			}
			return vm;
		}
		
		/**
		 * This method picks a host with minimum utilization from the host list presented. It ignores if the all the vms in a host are migrating out,
		 * if all the vms of a host are in offline state, and if host has a vm migrating in. 
		 * 
		 * @param Hostlist
		 * @param serverUtilMap
		 * @return
		 */
		private List<Host> hostWithMinUtil(List<Host> Hostlist, Map<Long, Double> serverUtilMap) {
			if (!bestMinUtilHost.isEmpty()) {
				bestMinUtilHost.remove(0);
			}
			double minUtilValue = Collections.min(serverUtilMap.values());// cHECK if it brings in only active hosts which
																			// has vms running currently
			boolean pickhost = false;
			while (!pickhost) {
				Outerloop: 
					for (Entry<Long, Double> entry : serverUtilMap.entrySet()) {
					if (entry.getValue().equals(minUtilValue)) {

						hostWithMinUtil = entry.getKey().intValue();
						HostMinUtil = Hostlist.get((int) hostWithMinUtil);
						if ((HostMinUtil.getVmsMigratingIn().isEmpty()) && (!AllVmsMigratingOut(HostMinUtil))
								&& (!AllVmsOffline(HostMinUtil))) {
							bestMinUtilHost.add(HostMinUtil);
								break Outerloop;
						}
					}
				}
				pickhost = true;
			}
			return bestMinUtilHost;
		}

		/**
		 * This method checks if all the vm's of a host are migrating out. Returns true if all the vms are migrating out, false otherwise.
		 * 
		 * @param host
		 * @return
		 */
		private boolean AllVmsMigratingOut(Host host) {
			List<Vm> Vmlist = host.getVmList();
			boolean AllVmsMigratingOut = true;
			for (Vm vm : Vmlist) {
				if(vm.isInMigration() == true) {
					host.getVmsMigratingOut().contains(vm);
				}else {
					AllVmsMigratingOut = false;
				}
			}
			return AllVmsMigratingOut;
	}
		
		/**
		 * This method returns true if all the vms of the host are in offline state. False otherwise.
		 * @param host
		 * @return
		 */
		private boolean AllVmsOffline(Host host) {
			boolean AllVmsAreOffline = true;
			List<Vm> Vmlist = host.getVmList();
			Here: for (Vm vm : Vmlist) {
				if (vm.isCreated()) {
					AllVmsAreOffline = false;
					break Here;
				}
			}
			return AllVmsAreOffline;
		}

		/**
		 * This method initially tries to get a server with minimum utilization based on ram, cpu and tries to fit in all the vm's from that host into the other
		 * operational hosts. when the required resources by all those vm's are less than the free resources in all the operational hosts then aggresiive 
		 * cosolidation case a will take place. This process also takes into consideration of the vm's from overloaded and underloaded hosts and tries to alter its placement 
		 * by choosing the target host appropriately.
		 * The other case works the same way like it tries to fit in all the vm's from overloaded hosts and underloaded hosts in other operational hosts. 
		 * When there are enough resources available then it approves the decision and passes its decision on to the local controller for implemetation. 
		 *  
		 * @param LocalController
		 */
		private boolean aggressiveConsolidationWA() {
			boolean AggressiveConsolidationWorked = false;
			List<Vm> VmsInMInUtilHost = new ArrayList<Vm>();
			if (hostWithMinUtili != null) {
				if (!hostWithMinUtili.getVmList().isEmpty()) {
					for (Vm vm : hostWithMinUtili.getVmList()) {
						if (!vm.isInMigration()) {
							VmsInMInUtilHost.add(vm);
						}
					}
				}
			}
			for (Vm vm : VmsToMigrateWA) {
				if (!VmsInMInUtilHost.contains(vm)) {
					VmsInMInUtilHost.add(vm);
				}
			}

			List<Host> targetHostList = new ArrayList<Host>();

			hostListWebApplication.forEach(host -> {
				if((!host.getVmList().isEmpty()) && (!AllVmsOffline(host))
						&& (!OverLoadedHostssetWebApplication.contains(host)) && (host != hostWithMinUtili)
						&& (!UnderLoadedHostssetWebApplication.contains(host)) && (!AllVmsMigratingOut(host))) {
					targetHostList.add(host);
				}
			});

			double counter = 0 ;
			if(!targetHostList.isEmpty()) {
				counter = calculateAvailableFreeResourceWA(targetHostList,VmsInMInUtilHost);
			}
			if(VmsInMInUtilHost.size() <= counter) {				
				sendVMCharToGACallFromGCWA(serverUtil);
				SendTargerhostlistDynamicPlacementWA(targetHostList);
				placementManagerForDynamicPlacementWA(VmsInMInUtilHost, VmstoMigrateFromOverloadedHostsWA);
				AggressiveConsolidationWorked = checkCaseAworked(AggressiveConsolidationWorked);
			}
			
			if(CaseAWorkedWA != true){
				if(!VmsToMigrateWA.isEmpty()) {
					targetHostList.add(hostWithMinUtili);

					counter =0 ;
					if(!targetHostList.isEmpty()) {
						counter = calculateAvailableFreeResourceWA(targetHostList,VmsToMigrateWA);
					}
					if(VmsToMigrateWA.size() <= counter) { 	
						sendVMCharToGACallFromGCWA(serverUtil);	
						SendTargerhostlistDynamicPlacementWA(targetHostList);
						placementManagerForDynamicPlacementWA(VmsToMigrateWA, VmstoMigrateFromOverloadedHostsWA);
						if(GAChooseSourceMapWA == true && !VmstoMigrateFromOverloadedHostsWA.isEmpty()) {
							AggressiveConsolidationWorked = false;
						}else {		
							AggressiveConsolidationWorked = true;
						}
					}
				}
			}
			return AggressiveConsolidationWorked;
		}		
		/**
		 * @param LocalController
		 * @param AggressiveConsolidationWorked
		 * @return
		 */
		boolean CaseAWorkedWA = false;
		private boolean checkCaseAworked(boolean AggressiveConsolidationWorked) {		
			if(GAChooseSourceMapWA == true && !VmstoMigrateFromOverloadedHostsWA.isEmpty()) {
				CaseAWorkedWA = false;
			}else {
				CaseAWorkedWA = true;
				AggressiveConsolidationWorked = true;
			}
			return AggressiveConsolidationWorked;
		}
		
		/**
		 * @param LocalController
		 * @return
		 */
		private boolean ConsolidationWithoutAdditionalhostsWA() {
			boolean ConsolidationWithoutAdditionalHosts = false;
			List<Host> targetHostList = new ArrayList<Host>();
			Set<Host> UnderLoadedHosts = UnderLoadedHostssetWebApplication;
			double counter = 0 ;
			boolean ExitWhile = true;
			Set<Host> UnderLoadedHosts1 = new HashSet<Host>();
			while(!UnderLoadedHosts.isEmpty() && !ConsolidationWithoutAdditionalHosts && ExitWhile) {
				Optional<Host> host = UnderLoadedHosts.stream().filter(host1 -> !(UnderLoadedHosts1.contains(host1))).findAny();
				if(host.isPresent()) {
					if(!AllVmsMigratingOut(host.get())) {
						targetHostList.add(host.get());
					}
					UnderLoadedHosts1.add(host.get());		
				}else {
					ExitWhile = false;
				}
				
				counter = 0 ;
				if(!targetHostList.isEmpty()) {
					counter = calculateAvailableFreeResourceWA(targetHostList,VmsToMigrateWA);
				}
				if(VmsToMigrateWA.size() <= counter) { 
					ExitWhile = false;
				}
			}
			if(!targetHostList.isEmpty() && (VmsToMigrateWA.size() <= counter) ) {
				sendVMCharToGACallFromGCWA(serverUtil);
				SendTargerhostlistDynamicPlacementWA(targetHostList);
				placementManagerForDynamicPlacementWA(VmsToMigrateWA, VmstoMigrateFromOverloadedHostsWA);
				if(GAChooseSourceMapWA == true && !VmstoMigrateFromOverloadedHostsWA.isEmpty()) {
					ConsolidationWithoutAdditionalHosts = false;
				}else {
					ConsolidationWithoutAdditionalHosts = true;
				}	
			}
			return ConsolidationWithoutAdditionalHosts;
		}
		
		/**
		 * This method adds additional hosts to the targethostlist when there are not enough resources for overloaded Vm's in the current running hosts.
		 * This method runs untill the GA finds a new map for the overloaded vm's.
		 * @param LocalController
		 * @return
		 */
		private boolean AddAdditionalhostWA() {
			boolean AllocatedWithAdditionalHosts = false;
			List<Host> targetHostList = new ArrayList<Host>();
			
			hostListWebApplication.forEach(host -> {
				if ((!host.getVmList().isEmpty()) && (!OverLoadedHostssetWebApplication.contains(host))) {
					targetHostList.add(host);
				}			
			});

			boolean gaChooseSourceMapWhenHostOverloaded = true;
			boolean ExitWhile = true;
			double counter = 0;
			while(gaChooseSourceMapWhenHostOverloaded) {
				while(ExitWhile) {
					Optional<Host> additionalserver =	hostListWebApplication.stream().filter(host -> host.getVmList().isEmpty())
							.filter(host -> !(targetHostList.contains(host))).findAny();
					if(additionalserver.isPresent()&& !targetHostList.contains(additionalserver.get())){
						targetHostList.add(additionalserver.get());
					}		
					
					counter = 0 ;
					if(!targetHostList.isEmpty()) {
						counter = calculateAvailableFreeResourceWA(targetHostList,VmsToMigrateWA);
					}
					if(VmsToMigrateWA.size() <= counter) { 
						ExitWhile = false;
					}
				}
			
				sendVMCharToGACallFromGCWA(serverUtil);
				SendTargerhostlistDynamicPlacementWA(targetHostList);
				placementManagerForDynamicPlacementWA(VmsToMigrateWA,VmstoMigrateFromOverloadedHostsWA);
				if(GAChooseSourceMapWA == true && !VmstoMigrateFromOverloadedHostsWA.isEmpty()) {
					gaChooseSourceMapWhenHostOverloaded = true;
					ExitWhile = true;
				}else {
					gaChooseSourceMapWhenHostOverloaded = false;
				}
				AllocatedWithAdditionalHosts = true;
			}	
			return AllocatedWithAdditionalHosts;
		}

		/**
		 * @param targetHostList
		 */
		public void SendTargerhostlistDynamicPlacementWA(List<Host> TargetHostList) {
			targetHostListWA = new ArrayList<Long>();
			TargetHostList.forEach(host -> {
				targetHostListWA.add(host.getId());
			});
		}
		
		/**
		 * @param Vmlist
		 * @return
		 */
		private double TotalMipsRequired(List<Vm> Vmlist) {
			double TotalCpuMipsRequired = 0;
			for (Vm vm : Vmlist) {
				TotalCpuMipsRequired += vm.getTotalMipsCapacity();// vm.getMips();
			}
			return TotalCpuMipsRequired;
		}

		/**
		 * @param Vmlist
		 * @return
		 */
		private double TotalRamRequired(List<Vm> Vmlist) {
			double TotalRamRequired = 0;
			for (Vm vm : Vmlist) {
				TotalRamRequired += vm.getRam().getCapacity();
			}
			return TotalRamRequired;
		}

		/**
		 * @param Vmlist
		 * @return
		 */
		private double TotalBwRequired(List<Vm> Vmlist) {
			double TotalBWRequired = 0;
			for (Vm vm : Vmlist) {
				TotalBWRequired += vm.getBw().getCapacity();
			}
			return TotalBWRequired;
		}

		/**
		 * @param Hostlist
		 * @return
		 */
		private double TotalFreeMips(List<Host> Hostlist) {
	//		double TotalFREECpuMips = 0;
			double HostUsedMips = 0.0;
//			double HostTotalCapacity = 0;
			double hostFreemips = 0;
			double hosttotalresource = 0;
			for (Host host : Hostlist) {
				// check this
				for (Vm vm : host.getVmList()) {
					HostUsedMips += vm.getTotalMipsCapacity();
					// HostTotalCapacity += host.getTotalMipsCapacity();
					// TotalFREECpuMips += (HostUsedMips / HostTotalCapacity);
				}
				hosttotalresource += host.getTotalMipsCapacity();
	//			TotalFREECpuMips += host.getTotalAvailableMips();// host.getAvailableMips()--gives negative values some times
															// like -3000, -6000 etc
			}
			hostFreemips = hosttotalresource - HostUsedMips;
			
			return hostFreemips;
		}
		
		private double calculateAvailableFreeResourceWA(List<Host> Hostlist, List<Vm> VmstoMigrate){
			double TotalVMs = VmstoMigrate.size();
			double highestRam = 0;
			double prevRam = 0;
			for(Vm vm : InitialvmListWebApplication) {
				double ram = vm.getRam().getCapacity();
				if(ram > prevRam) {
					highestRam = ram;
					prevRam = ram;
				}
			}			
			
			double hostFreemips = 0;
			double hosttotalresource = 0;	
			double freeRam=0;
			double counter = 0;
			for(Host host : Hostlist) {
				double TotalUsedRam = 0;
				double TotalRam = 0;
				double HostUsedMips = 0.0;
				double hostRam = host.getRam().getCapacity();
				for (Vm vm : host.getVmList()) {
					HostUsedMips += vm.getTotalMipsCapacity();
					TotalUsedRam += vm.getRam().getCapacity();
				}
				hosttotalresource = host.getTotalMipsCapacity();
				TotalRam += host.getRam().getCapacity();
				hostFreemips = hosttotalresource - HostUsedMips;
				freeRam = TotalRam - TotalUsedRam;
				while((hostFreemips > 2500)&&(freeRam > highestRam)) {
					counter +=1;
					hostFreemips -= 2500;
					freeRam -= highestRam;
				}
				
				for(Vm vm : VmstoMigrate) {
					if(host.getVmList().contains(vm)){
						counter += 1;
					}
				}
			}
			return counter;
		}

		/**
		 * @param Hostlist
		 * @return
		 */
		private double TotalFreeRam(List<Host> Hostlist) {
		//	double TotalFreeRam = 0;
			double TotalUsedRam = 0;
			double TotalRam = 0;
			for (Host host : Hostlist) {
				for (Vm vm : host.getVmList()) {
					TotalUsedRam += vm.getRam().getCapacity();
				}
				TotalRam += host.getRam().getCapacity();
//				TotalFreeRam += host.getRam().getAvailableResource();
			}
			double freeRam = TotalRam - TotalUsedRam;
			return freeRam;
		}

		
		/**
		 * @param Hostlist
		 * @return
		 */
		private double TotalFreeBw(List<Host> Hostlist) {
			double TotalFreeBW = 0;
			for (Host host : Hostlist) {
				TotalFreeBW += host.getBw().getAvailableResource();
			}
			return TotalFreeBW;
		}

		/**
		 * @param Hostlist
		 * @param UnderloadedHostlist
		 * @return
		 */
		private boolean onlyOneHostHasVmsAndIsUnderLoaded(List<Host> Hostlist, Set<Host> UnderloadedHostlist) {
			int NoOfHostsWithVmsInIt = 0;
			int NoOfUnderloadedHostsWithVmsInIt = 0;
			for (Host host : Hostlist) {
				if (!host.getVmList().isEmpty()) {
					NoOfHostsWithVmsInIt += 1;
					if (UnderloadedHostlist.contains(host)) {
						NoOfUnderloadedHostsWithVmsInIt += 1;
					}
				}
			}
			boolean j;
			if ((NoOfHostsWithVmsInIt == 1) && (NoOfHostsWithVmsInIt == NoOfUnderloadedHostsWithVmsInIt)) {
				j = true;
			} else
				j = false;

			return j;
		}
		
		/**
		 * @param InitialvmListWA
		 * @param VmlisttoMigrate
		 * @return
		 */
		public boolean GAChooseSourceMapWA;
		public Map<Long, Long> placementManagerForDynamicPlacementWA(List<Vm> VmlisttoMigrate, List<Vm> VmlisttoMigrateFromOverloadedHosts) {

			VmstoMigrateFromOverAndUnderloadedHostsWA = new LinkedList<>();
			VmstoMigrateFromOverAndUnderloadedHostsWA = VmlisttoMigrate;// Also bring vms from min util hosts during

			VmstoMigrateFromOverloadedHostsWA = new ArrayList<>();
			VmstoMigrateFromOverloadedHostsWA = VmlisttoMigrateFromOverloadedHosts;
			if((!OverLoadedHostssetWebApplication.isEmpty()) || (!UnderLoadedHostssetWebApplication.isEmpty())
					|| (VmstoMigrateFromOverAndUnderloadedHostsWA != null)) {
				sendVMCharToGAWA();
				
				if((!OverLoadedHostssetWebApplication.isEmpty()) || (!UnderLoadedHostssetWebApplication.isEmpty()) || (!VmlisttoMigrate.isEmpty())) {
					activeHosts();
					
					GADriverWA GA = new GADriverWA();
					bestDynamicVmServerMapWA = GA.dynamicGA(sourceVMHostMapWA, targetHostListWA, hostListWebApplication, InitialvmListWebApplication);
					GAChooseSourceMapWA = GA.GAChooseSourceMapWA;
					if(numberOfActiveHosts >= GA.serverVmMapDynamic.size() && (OverLoadedHostssetWebApplication.isEmpty())) {
						bestDynamicVmServerMapWA = sourceVMHostMapWA;
					}
				}
			}	
			return bestDynamicVmServerMapWA;
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
		
		/**
		 * @param serverUtil
		 */
		public void sendVMCharToGACallFromGCWA(Map<Long, Double> serverUtil) {
			sourceVMHostMapWA = new HashMap<Long, Long>();
			if (bestDynamicVmServerMapWA != null) {
				sourceVMHostMapWA = bestDynamicVmServerMapWA;
			} else {
				sendVMCharToGAWA();
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
		

		public void sendVMCharToGAWA() {
			sourceVMHostMapWA = new HashMap<Long, Long>();
			vmsCurrentCPUUtil = new HashMap<Long, Double>();
			vmsCurrentRAMUtil = new HashMap<Long, Long>();
		
			InitialvmListWebApplication.forEach(vm -> {
				Host sourceHost = vm.getHost();

				if(!vm.isFailed()&&vm.isWorking() && sourceHost.getVmList().contains(vm)) {
					sourceHost = ifVmisinMigrationGetTargetHostWA(vm, sourceHost);		
					sourceVMHostMapWA.put(vm.getId(), sourceHost.getId());
					vmsCurrentCPUUtil.put(vm.getId(),vm.getTotalCpuMipsUtilization());
					vmsCurrentRAMUtil.put(vm.getId(), vm.getCurrentRequestedRam());
				}if(!sourceHost.getVmList().contains(vm)) {
					sourceVMHostMapWA.remove(vm.getId());
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
		private Host ifVmisinMigrationGetTargetHostWA(Vm vm, Host sourceHost) {
			if(vm.isInMigration()) {
				for(final Map.Entry<Long, Long> entryDaas : bestDynamicVmServerMapWA.entrySet()){
					if(vm.getId() == entryDaas.getKey().intValue()) {
						sourceHost = hostListWebApplication.get(entryDaas.getValue().intValue());			
					}
				}
			}
			return sourceHost;
		}
		
		
		double	hostCpuUtil;
		public void sendHostUtilToGAWA() {
			hostCurrentUtil = new HashMap<Long, Double>();
			targetHostListWA = new ArrayList<Long>();
			
			hostListWebApplication.forEach(host ->{
				if(host.isActive() && host.getVmList().size()!=0) {
					hostCpuUtil = host.getCpuPercentUtilization();
					/*
					 * If the host is not overloaded, add it to the target host list
					 */
					if(!targetHostListWA.contains(host.getId())&& !OverLoadedHostssetWebApplication.contains(host)) {
						targetHostListWA.add(host.getId());
					}
				} else {
					targetHostListWA.remove(host.getId());
				}
				hostCurrentUtil.put(host.getId(), hostCpuUtil);

			});
		}
		
		/**
		 * This method helps maintain the load balanced in Daas setup by taking decisions such as aggressive consolidation, consolidation without adding any 
		 * additional hosts, adding additional hosts. 
		 *  
		 * @param LocalController
		 */
		public void globalDecisionMakerDAAS() {

			double GlobalUtil = globalUtilizationComputationalUnit(hostListDaas); // Verified

			// vm selection and then dispatch
			
			if (!OverLoadedHostssetDaas.isEmpty() || !UnderLoadedHostssetDaas.isEmpty()) {
				VmSelectionPolicyMaxAverageCPURAM VmSelection = new VmSelectionPolicyMaxAverageCPURAM();
				VmSelection.vmSelection(hostListDaas, OverLoadedHostssetDaas, UnderLoadedHostssetDaas,
						DAASVmsRamUtilizationHistory, HostUpperUtilizationThresholdDAAS, HostLowerUtilizationThresholdDAAS, currentTime);

				VmsToMigrateDAAS = VmSelection.VmsToMigrate;
				VmsToMigrateFromOverloadedHostsDAAS = VmSelection.VmsToMigrateFromOverloadedHosts;
			} else {
				VmsToMigrateDAAS = new ArrayList<Vm>();
			}

			AggressiveConsolidation = false;
			if ((GlobalUtil < 0.65) && !onlyOneHostHasVmsAndIsUnderLoaded(hostListDaas, UnderLoadedHostssetDaas)) {
				boolean hostHasMigratingInVms = false;

				Set<Vm> vmSet = selectHostwithNoMigratingInVms(hostListDaas, hostHasMigratingInVms);

				if((!vmSet.isEmpty()) || (!VmsToMigrateDAAS.isEmpty())) {
					AggressiveConsolidation = aggressiveConsolidationDAAS();
				}
			}
			ConsolidationWithoutAdditionalHosts = false;
			if ((AggressiveConsolidation == false) && (!VmsToMigrateDAAS.isEmpty())
					&& (!onlyOneHostHasVmsAndIsUnderLoaded(hostListDaas,
							UnderLoadedHostssetDaas))) {
				ConsolidationWithoutAdditionalHosts = ConsolidationWithoutAdditionalhostsDAAS();
			}
			
			boolean AllocatedWithAdditionalHosts = false;
			if ((!onlyOneHostHasVmsAndIsUnderLoaded(hostListDaas, UnderLoadedHostssetDaas))
					&& ((AggressiveConsolidation == false) && (ConsolidationWithoutAdditionalHosts == false))
					&& (!OverLoadedHostssetDaas.isEmpty())) {
				AllocatedWithAdditionalHosts = AddAdditionalhostDAAS();
			}
		
			if ((!onlyOneHostHasVmsAndIsUnderLoaded(hostListDaas, UnderLoadedHostssetDaas))
					&& ((AggressiveConsolidation == false) && (ConsolidationWithoutAdditionalHosts == false) && (AllocatedWithAdditionalHosts == false))
					&& (!OverLoadedHostssetDaas.isEmpty())) {
				setPreviousMapAsCurrentMapDAAS();
		//		System.out.println("Test Check: No case is suitable in GC---------->" );
			}
		
		}
		Vm vm1;
		public void setPreviousMapAsCurrentMapDAAS() {
			if(bestDynamicVmServerMapDAAS != null) {
				
				for (final Map.Entry<Long, Long> entryDaas : bestDynamicVmServerMapDAAS.entrySet()) {
					vm1 = null;
					Here:
					for (Vm vm2 : InitialvmListDaas) {
	    				if(vm2.getId() == entryDaas.getKey().intValue()) {
	    					vm1 = vm2;
	    					break Here;
	    				}
	    			}
			//		Vm vm = vmListDaas.get(entryDaas.getKey().intValue());
					if(vm1 != null) {
						if(vm1.isCreated()) {
							Host host = hostListDaas.get(entryDaas.getValue().intValue());
							bestDynamicVmServerMapDAAS.put(vm1.getId(), host.getId());
						}
					}
					
				}	
			}
		}		
		/**
		 * This method initially tries to get a server with minimum utilization based on ram, cpu and tries to fit in all the vm's from that host into the other
		 * operational hosts. when the required resources by all those vm's are less than the free resources in all the operational hosts then aggresiive 
		 * cosolidation case a will take place. This process also takes into consideration of the vm's from overloaded and underloaded hosts and tries to alter its placement 
		 * by choosing the target host appropriately.
		 * The other case works the same way like it tries to fit in all the vm's from overloaded hosts and underloaded hosts in other operational hosts. 
		 * When there are enough resources available then it approves the decision and passes its decision on to the local controller for implemetation. 
		 *  
		 * @param LocalController
		 */
		private boolean aggressiveConsolidationDAAS() {
			boolean AggressiveConsolidationWorked = false;
			List<Vm> VmsInMInUtilHost = new ArrayList<Vm>();
			if (hostWithMinUtili != null) {
				if (!hostWithMinUtili.getVmList().isEmpty()) {
					for (Vm vm : hostWithMinUtili.getVmList()) {
						if (!vm.isInMigration()) {
							VmsInMInUtilHost.add(vm);
						}
					}
				}
			}
			for (Vm vm : VmsToMigrateDAAS) {
				if (!VmsInMInUtilHost.contains(vm)) {
					VmsInMInUtilHost.add(vm);
				}
			}

			List<Host> targetHostList = new ArrayList<Host>();

			hostListDaas.forEach(host -> {
				if ((!host.getVmList().isEmpty()) && (!AllVmsOffline(host))
						&& (!OverLoadedHostssetDaas.contains(host)) && (host != hostWithMinUtili)
						&& (!UnderLoadedHostssetDaas.contains(host)) && (!AllVmsMigratingOut(host))) {
					targetHostList.add(host);
				}
			});
			activeHostsDAAS();
			if(numberOfActiveHostsDAAS == UnderLoadedHostssetDaas.size()) {
				targetHostList.addAll(hostListDaas);
				targetHostList.remove(hostWithMinUtili);
				VmsToMigrateDAAS = hostWithMinUtili.getVmList();
				VmsInMInUtilHost = hostWithMinUtili.getVmList();
			}
			boolean CaseAWorked = false;
			double counter = 0 ;
			if(!targetHostList.isEmpty()) {
				counter = calculateAvailableFreeResourceDAAS(targetHostList, VmsInMInUtilHost);
			}
 			if (VmsInMInUtilHost.size() <= counter) {
				sendVMCharToGACallFromGCDAAS(serverUtil);
				SendTargerhostlistDynamicPlacementDAAS(targetHostList);
				placementManagerForDynamicPlacementDAAS(InitialvmListDaas, VmsInMInUtilHost, VmsToMigrateFromOverloadedHostsDAAS);
				if(GAChooseSourceMapDAAS == true && !VmsToMigrateFromOverloadedHostsDAAS.isEmpty()) {
					CaseAWorked = false;
				}else {
					CaseAWorked = true;
					AggressiveConsolidationWorked = true;
				}
			} 
			
			if(CaseAWorked == false){
				if(!VmsToMigrateDAAS.isEmpty() && !UnderLoadedHostssetDaas.contains(hostWithMinUtili)) {
					targetHostList.add(hostWithMinUtili);
					if(!targetHostList.isEmpty()) {
						counter = calculateAvailableFreeResourceDAAS(targetHostList,VmsToMigrateDAAS);
					}
					if (VmsToMigrateDAAS.size() <= counter) {
						sendVMCharToGACallFromGCDAAS(serverUtil);
						SendTargerhostlistDynamicPlacementDAAS(targetHostList);
						placementManagerForDynamicPlacementDAAS(InitialvmListDaas, VmsToMigrateDAAS,VmsToMigrateFromOverloadedHostsDAAS);
						if(GAChooseSourceMapDAAS == true && !VmsToMigrateFromOverloadedHostsDAAS.isEmpty()) {
							AggressiveConsolidationWorked = false;
						}else {	
							AggressiveConsolidationWorked = true;
						}
					}
				}
			}
			return AggressiveConsolidationWorked;
		}

		private double calculateAvailableFreeResourceDAAS(List<Host> Hostlist, List<Vm> VmstoMigrate){
			double TotalVMs = VmstoMigrate.size();
			double highestRam = 0;
			double prevRam = 0;
			for(Vm vm : InitialvmListDaas) {
				double ram = vm.getRam().getCapacity();
				if(ram > prevRam) {
					highestRam = ram;
					prevRam = ram;
				}
			}
			
			
			double hostFreemips = 0;
			double hosttotalresource = 0;	
			double freeRam=0;
			double counter =0;
			for(Host host : Hostlist) {
				double TotalUsedRam = 0;
				double TotalRam = 0;
				double HostUsedMips = 0.0;
				double hostRam = host.getRam().getCapacity();
				for (Vm vm : host.getVmList()) {
					HostUsedMips += vm.getTotalMipsCapacity();
					TotalUsedRam += vm.getRam().getCapacity();
				}
				hosttotalresource = host.getTotalMipsCapacity();
				TotalRam += host.getRam().getCapacity();
				hostFreemips = hosttotalresource - HostUsedMips;
				freeRam = TotalRam - TotalUsedRam;
			
				while((hostFreemips > 2000)&&(freeRam > highestRam)) {
					counter +=1;
					hostFreemips -= 2000;
					freeRam -= highestRam;
				}
				
				for(Vm vm : VmstoMigrate) {
					if(host.getVmList().contains(vm)){
						counter += 1;
					}
				}
			}
			return counter;
		}
		
		/**
		 * @param LocalController
		 * @return
		 */
		private boolean ConsolidationWithoutAdditionalhostsDAAS() {
			boolean ConsolidationWithoutAdditionalHosts = false;
			List<Host> targetHostList = new ArrayList<Host>();
			Set<Host> UnderLoadedHosts = new HashSet<Host>();
			Set<Host> UnderLoadedHosts1 = new HashSet<Host>();
			UnderLoadedHosts.addAll(UnderLoadedHostssetDaas);
			boolean ExitWhile = true;
			
			while (!UnderLoadedHosts.isEmpty() && !ConsolidationWithoutAdditionalHosts && ExitWhile) {
					
				Optional<Host> host = UnderLoadedHosts.stream().filter(host1 -> !(UnderLoadedHosts1.contains(host1))).findAny();
				if(host.isPresent()) {
					Host Host = host.get();
					if(!AllVmsMigratingOut(Host)) {
						targetHostList.add(Host);
					}
					UnderLoadedHosts1.add(Host);		
				}else {
					ExitWhile = false;
				}
			
				double counter =0 ;
				if(!targetHostList.isEmpty()) {
					counter = calculateAvailableFreeResourceDAAS(targetHostList, VmsToMigrateDAAS);
				}

				if(VmsToMigrateDAAS.size() <= counter) { 
					sendVMCharToGACallFromGCDAAS(serverUtil);
					SendTargerhostlistDynamicPlacementDAAS(targetHostList);
					placementManagerForDynamicPlacementDAAS(InitialvmListDaas, VmsToMigrateDAAS,VmsToMigrateFromOverloadedHostsDAAS);
					if(GAChooseSourceMapDAAS == true && !VmsToMigrateFromOverloadedHostsDAAS.isEmpty()) {
						ConsolidationWithoutAdditionalHosts = false;
					}else {
						ConsolidationWithoutAdditionalHosts = true;
					}
				}
			}
				
			if(hostListDaas.size() == UnderLoadedHostssetDaas.size()) {
				sendVMCharToGACallFromGCDAAS(serverUtil);
				SendTargerhostlistDynamicPlacementDAAS(targetHostList);
				placementManagerForDynamicPlacementDAAS(InitialvmListDaas, VmsToMigrateDAAS,VmsToMigrateFromOverloadedHostsDAAS);
				if(GAChooseSourceMapDAAS == true && !VmsToMigrateFromOverloadedHostsDAAS.isEmpty()) {
					ConsolidationWithoutAdditionalHosts = false;
				}else {
					ConsolidationWithoutAdditionalHosts = true;
				}
			}
			return ConsolidationWithoutAdditionalHosts;
		}



		/**
		 * @param LocalController
		 * @return
		 */
		private boolean AddAdditionalhostDAAS() {
			boolean AllocatedWithAdditionalHosts = false;
			List<Host> targetHostList = new ArrayList<Host>();

			hostListDaas.forEach(host -> {
				if ((!host.getVmList().isEmpty()) && (!OverLoadedHostssetDaas.contains(host))) {
					targetHostList.add(host);
				}			
			});
			boolean gaChooseSourceMapWhenHostOverloaded = true;
			boolean ExitWhile = true;
			double counter = 0;
			while(gaChooseSourceMapWhenHostOverloaded) {
				while(ExitWhile) {
					Optional<Host> additionalserver =	hostListDaas.stream().filter(host -> host.getVmList().isEmpty())
							.filter(host -> !(targetHostList.contains(host))).findAny();
					if(additionalserver.isPresent() && !targetHostList.contains(additionalserver.get())){
						targetHostList.add(additionalserver.get());
					}		
					
					counter = 0 ;
					if(!targetHostList.isEmpty()) {
						counter = calculateAvailableFreeResourceDAAS(targetHostList,VmsToMigrateDAAS);
					}
					if(VmsToMigrateDAAS.size() <= counter) { 
						ExitWhile = false;
					}
				}		

			sendVMCharToGACallFromGCDAAS(serverUtil);
			SendTargerhostlistDynamicPlacementDAAS(targetHostList);
			placementManagerForDynamicPlacementDAAS(InitialvmListDaas, VmsToMigrateDAAS,VmsToMigrateFromOverloadedHostsDAAS);
			if(GAChooseSourceMapDAAS == true && !VmsToMigrateFromOverloadedHostsDAAS.isEmpty()) {
				gaChooseSourceMapWhenHostOverloaded = true;
				ExitWhile = true;
			}else {
				gaChooseSourceMapWhenHostOverloaded = false;
			}
			AllocatedWithAdditionalHosts = true;
		}	
			return AllocatedWithAdditionalHosts;
		}
		
		private void placementManagerDAAS() {
			GADriverDaas GA = new GADriverDaas();
			GA.GAInitialplacement();
			vmToHostMapInitialPlacementDAAS = GA.vmToHostMapDaas;
			Host host = hostListDaas.get(0);
			double simultionTime = host.getDatacenter().getSimulation().clock();
			if(simultionTime > 1000) {
				updateDynamicMapDAAS();//Datacentersimple makes use of this for migrations.If it is not updated then the previous offline values are taken and vms are migrated
				sendVMCharToGADAAS();//This is to update the sourcevmserver map which is given as input to dynamic placement
			}
			schedulerDAAS(vmToHostMapInitialPlacementDAAS);
		}

		private void schedulerDAAS(Map<Integer, Integer> vmToHostMapDaas) {
			vmToHostMapInitialPlacementDAAS = vmToHostMapDaas;

		}
		private void updateDynamicMapDAAS(){
			bestDynamicVmServerMapDAAS = new HashMap<Long, Long>();
			vmToHostMapInitialPlacementDAAS.forEach((vm,server) -> {
				bestDynamicVmServerMapDAAS.put(vm.longValue(), server.longValue());
			});
		}
		
		public boolean GAChooseSourceMapDAAS;
		/*
		 * Initiate dynamic placement
		 */
		public Map<Long, Long> placementManagerForDynamicPlacementDAAS(List<Vm> initialvmList, List<Vm> vmlisttoMigrate,List<Vm> VmsToMigrateOverloadedHostsDAAS) {
			InitialvmListDaas = initialvmList;
			VmstoMigrateFromOverUnderloadedHostsDAAS = new LinkedList<>();
			VmstoMigrateFromOverUnderloadedHostsDAAS = vmlisttoMigrate;// Also bring vms from min util hosts during
																			// aggressive consolidation
			VmsToMigrateFromOverloadedHostsDAAS = new ArrayList<>();
			VmsToMigrateFromOverloadedHostsDAAS = VmsToMigrateOverloadedHostsDAAS;
			Vm vm = InitialvmListDaas.get(0);
			if ((!OverLoadedHostssetDaas.isEmpty()) || (!UnderLoadedHostssetDaas.isEmpty())
					|| (VmstoMigrateFromOverUnderloadedHostsDAAS != null)) {
				if((vm.getBroker().getVmWaitingList().isEmpty())) {
					activeHostsDAAS();
					sendVMCharToGADAAS();
					GADriverDaas ga = new GADriverDaas();
					// it selects the same host as source and target host.no chnage in placement.
					bestDynamicVmServerMapDAAS = ga.DynamicGeneticAlgorithmDriverDaas(sourceVMHostMapDAAS, targetHostListDAAS, hostListDaas, InitialvmListDaas);
					this.dynamicserverVmMapDAAS = ga.serverVmMapDynamic;
					GAChooseSourceMapDAAS = ga.GAChooseSourceMap;
					if(numberOfActiveHostsDAAS <= ga.serverVmMapDynamic.size() && (OverLoadedHostssetDaas.isEmpty())) {
						bestDynamicVmServerMapDAAS = sourceVMHostMapDAAS;
					}
				}
			}
			return bestDynamicVmServerMapDAAS;			
		}

		int numberOfActiveHostsDAAS;
		private void activeHostsDAAS() {
			numberOfActiveHostsDAAS = 0;
			hostListDaas.forEach(host ->{
				if(host.isActive() && host.getVmList().size()!=0) {
					numberOfActiveHostsDAAS += 1;
				}
			});
		}
		
		/**
		 * Shows updates every time the simulation clock advances.
		 * 
		 * @param evt information about the event happened (that for this Listener is
		 *            just the simulation time)
		 */

		private void sendVMCharToGADAAS() {
			sourceVMHostMapDAAS = new HashMap<Long, Long>();
			vmsCurrentCPUUtil = new HashMap<Long, Double>();
			vmsCurrentRAMUtil = new HashMap<Long, Long>();
			
			InitialvmListDaas.forEach(vm -> {
				
				ifVmisinWaitingListAddThemToSourceMap(vm);
				Host sourceHost = vm.getHost();
				
//				System.out.println("vm "+vm+" source host  "+sourceHost);
				if (!vm.isFailed() && vm.isWorking() && sourceHost.getVmList().contains(vm)) {
					
					sourceHost = ifVmisinMigrationGetTargetHost(vm, sourceHost);
					
					sourceVMHostMapDAAS.put(vm.getId(), sourceHost.getId());
					vmsCurrentCPUUtil.put(vm.getId(), vm.getTotalCpuMipsUtilization());
					vmsCurrentRAMUtil.put(vm.getId(), vm.getCurrentRequestedRam());
				}
				/*
				 * if (!sourceHost.getVmList().contains(vm)) {
				 * sourceVMHostMap.remove(vm.getId()); vmsCurrentCPUUtil.remove(vm.getId());
				 * vmsCurrentRAMUtil.remove(vm.getId()); }
				 */
			});
			sendHostUtilToGADAAS();
		}

		/**
		 * @param vm
		 */
		private void ifVmisinWaitingListAddThemToSourceMap(Vm vm) {
			if(!vm.getBroker().getVmWaitingList().isEmpty()) {
				vm.getBroker().getVmWaitingList().forEach(Vm1 -> {
					if(Vm1.getId() == vm.getId()) {
						for(final Map.Entry<Integer, Integer> entryDaas : vmToHostMapInitialPlacementDAAS.entrySet()){
								for (Vm vm1 : InitialvmListDaas) {
									if(vm1.getId() == entryDaas.getKey().intValue()) {
										sourceVMHostMapDAAS.put(vm.getId(), entryDaas.getValue().longValue());
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
				for(final Map.Entry<Long, Long> entryDaas : bestDynamicVmServerMapDAAS.entrySet()){
					if(vm.getId() == entryDaas.getKey().intValue()) {
						sourceHost = hostListDaas.get(entryDaas.getValue().intValue());			
					}
				}
			}
			return sourceHost;
		}

		double hostCpuUtilDAAS;
		private void sendHostUtilToGADAAS() {
			hostCurrentUtil = new HashMap<Long, Double>();
			hostListDaas.forEach(host -> {

		//		double hostTotalRamCapacity = (double) host.getRamProvisioner().getCapacity();
				hostCpuUtilDAAS = host.getCpuPercentUtilization();
		//		double hostRamUtil = host.getRamUtilization() / hostTotalRamCapacity;
		//		double hostBwUtil = host.getBwUtilization() / (double) host.getBw().getCapacity();

				if (host.isActive() && host.getVmList().size() != 0) {
					hostCurrentUtil.put(host.getId(), hostCpuUtilDAAS);
				}

			});
		}

		public void sendVMCharToGACallFromGCDAAS(Map<Long, Double> serverUtil) {
			sourceVMHostMapDAAS = new HashMap<Long, Long>();
			if (bestDynamicVmServerMapDAAS != null) {
				sourceVMHostMapDAAS = bestDynamicVmServerMapDAAS;
			} else {
				sendVMCharToGADAAS();
			}

			hostCurrentUtil = new HashMap<Long, Double>();
			hostCurrentUtil = serverUtil;
		}

		public void SendTargerhostlistDynamicPlacementDAAS(List<Host> targetHosts) {
			targetHostListDAAS = new ArrayList<Long>();
			targetHosts.forEach(host -> {
				targetHostListDAAS.add(host.getId());
			});
		}
		
		private void vmScalerDAAS() {
			for (Vm vm : InitialvmListDaas) {	
				createVerticalRamScalingForVmDAAS(vm);	
			}
		}

	
		  private void UpdateVmListDAAS(List<Vm> Vmlist){
			  
			  for(Vm Finishedvm : InitialvmListDaas) {
				  if(!Finishedvm.isCreated()) {
					  FinishesVmlistDay1Daas.add(Finishedvm); 
				  }
			  }
			  
			  if(InitialvmListDaas.size() >= 200) {
				  InitialvmListDaas = new ArrayList<>();
			  }
			  if(!Vmlist.isEmpty()) {
				  for(Vm NewVm : Vmlist) { 
					  if(!InitialvmListDaas.contains(NewVm)) {
						  InitialvmListDaas.add(NewVm);  
					  }
				  }
			  }
		  }
		
		public void VmListForPlacementDAAS(List<Vm> vmList) {
			InitialvmListDaas = vmList;
			vmScalerDAAS();
			placementManagerDAAS();
		}
		
		public void VmListForPlacementAfterOfflineDAAS(List<Vm> vmList) {
			UpdateVmListDAAS(vmList);
			vmScalerDAAS();
			placementManagerDAAS();
		}

		private void createVerticalRamScalingForVmDAAS(Vm vm) {
			VerticalVmScalingSimple verticalRamScaling = new VerticalVmScalingDAAS(Ram.class, 0.1);
			/*
			 * By uncommenting the line below, you will see that, instead of gradually
			 * increasing or decreasing the RAM, when the scaling object detects the RAM
			 * usage is above or below the defined thresholds, it will automatically
			 * calculate the amount of RAM to add/remove to move the VM from the over or
			 * underload condition.
			 */
			// verticalRamScaling.setResourceScaling(new ResourceScalingInstantaneous());
			verticalRamScaling.setLowerThresholdFunction(this::lowerRamUtilizationThresholdDAAS);
			verticalRamScaling.setUpperThresholdFunction(this::upperRamUtilizationThresholdDAAS);
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
		private double lowerRamUtilizationThresholdDAAS(Vm vm) {
			final List<Long> Ramhistory = new ArrayList<>();
			final Map<Integer, Long> vmRamUtilization = getVmRamUtilizationHistoryDAAS().get(vm);
			Ramhistory.addAll(vmRamUtilization.values());
			List<Double> RamHistory = Ramhistory.stream().map(Double::valueOf).collect(Collectors.toList());
			
			double Threshold = Ramhistory.size() > 10 ? (MathUtil.median(RamHistory) / vm.getRam().getCapacity()) * 0.4 : 0.4;//0.2
			return Threshold;
		}

		public Map<Vm, Map<Integer, Long>> getVmRamUtilizationHistoryDAAS() {
			return ModelConstructionForApplications.ModelConstruction.DAASVmsRamUtilizationHistory;
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
		private double upperRamUtilizationThresholdDAAS(Vm vm) {
			final List<Long> Ramhistory = new ArrayList<>();
			final Map<Integer, Long> vmRamUtilization = getVmRamUtilizationHistoryDAAS().get(vm);
			Ramhistory.addAll(vmRamUtilization.values());
			List<Double> RamHistory = Ramhistory.stream().map(Double::valueOf).collect(Collectors.toList());

			double Threshold = Ramhistory.size() > 10 ? (MathUtil.median(RamHistory) / vm.getRam().getCapacity()) * 1.4 : 0.8;//1.9
			return Threshold;
		}

		private void createVerticalRamScalingForVmWA(Vm vm) {
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
			verticalRamScaling.setLowerThresholdFunction(this::lowerRamUtilizationThresholdWA);
			verticalRamScaling.setUpperThresholdFunction(this::upperRamUtilizationThresholdWA);
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
		private double lowerRamUtilizationThresholdWA(Vm vm) {
			final List<Long> Ramhistory = new ArrayList<>();
			final Map<Integer, Long> vmRamUtilization = getVmRamUtilizationHistory().get(vm);
			Ramhistory.addAll(vmRamUtilization.values());
			List<Double> RamHistory = Ramhistory.stream().map(Double::valueOf).collect(Collectors.toList());
			
			double Threshold = Ramhistory.size() > 10 ? (MathUtil.median(RamHistory) / vm.getRam().getCapacity()) * 0.4 : 0.15;
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
		private double upperRamUtilizationThresholdWA(Vm vm) {
			final List<Long> Ramhistory = new ArrayList<>();
			final Map<Integer, Long> vmRamUtilization = getVmRamUtilizationHistory().get(vm);
			Ramhistory.addAll(vmRamUtilization.values());
			List<Double> RamHistory = Ramhistory.stream().map(Double::valueOf).collect(Collectors.toList());

			double Threshold = Ramhistory.size() > 10 ? (MathUtil.median(RamHistory) / vm.getRam().getCapacity()) * 1.6 : 0.8;
			return Threshold;
		}
}
