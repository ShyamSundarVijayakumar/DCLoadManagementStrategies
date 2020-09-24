/**
 * 
 */
package hierarchicalarchitecture.globalcontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import hierarchicalarchitecture.localcontrollerbatchprocess.LocalControllerBatchProcessing;
import hierarchicalarchitecture.localcontrollerdaas.LocalControllerDaas;
import hierarchicalarchitecture.localcontrollerwebapp.LocalControllerWA;

/**
 * This class is dedicated for designing the feature of global controller. The global controller has Request analyser and configuration manager(RACM), 
 * VM dispacher, Global decision maker, global utilization computational unit(GUCU).
 * 
 * The user requests are converted in VM configurations by the RACM module. The RACM sends those VM configurations to the VM dispacher unit, which then
 * seperates those configs according to the application type and dispaches them to their respective local controller. For instance, the VM's belonging to
 * the DaaS application will be given to the local controller DaaS.
 * 
 * The global decision maker component will make use of the GUCU to make decisions such as aggressive consolidation, consolidation without adding additional
 * host and adding additional hosts.  
 * 
 * @param vmListWebApplication
 * @param SCHEDULING_INTERVAL
 * @param cloudletListWebApplication
 * @param VMS_WEBAPP
 * @param VMS_DAAS
 * @param WebApplication
 * @param Daas
 * @param Customer_A
 * @param Customer_B
 * @param HostUpperUtilizationThreshold
 * @param HostLowerUtilizationThreshold
 * @param HighcpuMedium
 * @param Extralarge
 * @param Small
 * @param Micro
 * @param DaasVmType1
 * @param DaasVmType2
 * @param DaasVmType3
 * @param DaasVmType4
 * @param DaasVmType5
 * @param FinishesVmlistDay1Daas
 * @param serverUtil
 * @param HostWithMinUtili
 * @param AggressiveConsolidation
 * @param ConsolidationWithoutAdditionalHosts
 * @param AddExtraHost
 * @param Availablemips
 * @param Availableram
 * @param vm
 * @param serverUtilMapDuplicate
 * @param HostMinUtil
 * @param HostWithMinUtil
 * @param SelectiionPolicyserverVmsmapDAAS
 * @param SelectiionPolicyserverVmsmapWA
 *  
 * @see GlobalController#AddAdditionalhost(LocalControllerDaas)
 * @see GlobalController#aggressiveConsolidation(LocalControllerDaas)
 * @see GlobalController#aggressiveConsolidation(LocalControllerWA)
 * @see GlobalController#AllVmsMigratingOut(Host)
 * @see GlobalController#AllVmsOffline(Host)
 * @see GlobalController#checkForMigratingInVms(boolean)
 * @see GlobalController#ConsolidationWithoutAdditionalhosts(LocalControllerDaas)
 * @see GlobalController#ConsolidationWithoutAdditionalhosts(LocalControllerWA)
 * @see GlobalController#createDatacenterBrokers(CloudSim)
 * @see GlobalController#dispatcher()
 * @see GlobalController#dispatcherDynamic(List)
 * @see GlobalController#getInitialVmlistWebApp()
 * @see GlobalController#setInitialVmlistDaas(Queue)
 * @see GlobalController#GlobalDecisionMakerBP(List, Set, Set, LocalControllerBatchProcessing)
 * @see GlobalController#globalDecisionMakerDAAS(LocalControllerDaas)
 * @see GlobalController#GlobalDecisionMakerWA(LocalControllerWA)
 * @see GlobalController#globalUtilizationComputationalUnit(List)
 * @see GlobalController#globalUtilizationComputationalUnit(List, Map, List)
 * @see GlobalController#hostHasMigratingInVms(int, List)
 * @see GlobalController#hostWithMinUtil(List, Map)
 * @see GlobalController#modelUserRequests(CloudSim)
 * @see GlobalController#onlyOneHostHasVmsAndIsUnderLoaded(List, Set)
 * @see GlobalController#selectHostwithNoMigratingInVms(List, boolean)
 * @see GlobalController#setInitialVmlistDaas(Queue)
 * @see GlobalController#TotalBwRequired(List)
 * @see GlobalController#TotalFreeBw(List)
 * @see GlobalController#TotalFreeMips(List)
 * @see GlobalController#TotalRamRequired(List)
 * @see GlobalController#updateVmListDaasAfterOffline(List)
 * @see GlobalController#vmSelection(List, Set, Set)
 * @see GlobalController#vmSelectionWA(List, Set, Set)
 * 
 * @author Shyam Sundar V
 * @since CloudSim Plus 5.0
 */

public class GlobalController {

	private int HighcpuMedium = 1;
	private int Extralarge = 2;
	private int Small = 3;
	private int Micro = 4;
	public static List<Vm> InitialvmListWebApplication = new ArrayList<>();
	public static List<Vm> InitialvmListDaas;
	public Queue<Cloudlet> CloudletQueueBatchProcessing = new LinkedList<>();
	public static List<Vm> InitialvmListBatchProcessing;
	LocalControllerWA WebApplication = new LocalControllerWA();
	LocalControllerDaas Daas = new LocalControllerDaas();
	public DatacenterBrokerSimple Customer_A;
	public DatacenterBrokerSimple Customer_B;
	private int DaasVmType1 = 1;
	private int DaasVmType2 = 2;
	private int DaasVmType3 = 3;
	private int DaasVmType4 = 4;
	private int DaasVmType5 = 5;

	public static double HostUpperUtilizationThresholdWA;// = 0.80;
	public static double HostLowerUtilizationThresholdWA;// = 0.15;
	public static double HostUpperUtilizationThresholdDAAS;// = 0.85;
	public static double HostLowerUtilizationThresholdDAAS;// = 0.15;
	List<Vm> FinishesVmlistDay1Daas = new ArrayList<Vm>();
	public Map<Long, Double> serverUtil = new HashMap<Long, Double>();
	private Host HostWithMinUtili;
	private boolean AggressiveConsolidation = false;
	private boolean ConsolidationWithoutAdditionalHosts;
	private double Availablemips;
	private double Availableram;
	private double Availablebw;
	private Set<Vm> vm;
	private Map<Long, Double> serverUtilMapDuplicate;
	private Host HostMinUtil;
	private long HostWithMinUtil;
	private List<Host> BestMinUtilHost = new ArrayList<Host>();
	public static Map<Host, Vm> SelectiionPolicyserverVmsmapDAAS;
	public static Map<Host, Vm> SelectiionPolicyserverVmsmapWA;
	public static Map<Host, Vm> SelectiionPolicyserverVmsmapBP;
	public double Time = 0;
	
    /**
     * The file containing the Customer's SLA Contract in JSON format.
     */
	 
	private static final String CUSTOMER_A_SLA_CONTRACT = "hierarchicalarchitecture/globalcontroller/SLAWebApp.json";
	private static final String CUSTOMER_B_SLA_CONTRACT = "hierarchicalarchitecture/globalcontroller/SLADAAS.json";
   

    private SLAContract contractA_WA;
    private SLAContract contractB_DAAS;
	/**
	 * This method is used to create brokers for the simulation.
	 * 
	 * @param simulation
	 */
	public void createDatacenterBrokers(CloudSim simulation) {
		this.Customer_A = new DatacenterBrokerSimple(simulation);
		this.Customer_B = new DatacenterBrokerSimple(simulation);
		Time = simulation.clock();
		
		this.contractA_WA = SLAContract.getInstance(CUSTOMER_A_SLA_CONTRACT);
		this.contractB_DAAS = SLAContract.getInstance(CUSTOMER_B_SLA_CONTRACT);
	}

	/**
	 * This method is used to initiate virtual machine and cloudlet  creation for the appropriate datacenter brokers.
	 * 
	 * @param simulation
	 */
	RequestAnalyserAndConfigurationManagerDaaS RACMDaaS ;
	public void modelUserRequests(CloudSim simulation) {
		
		HostUpperUtilizationThresholdWA = contractA_WA.getHostCpuUtilizationMetric().getMaxDimension().getValue();
		HostLowerUtilizationThresholdWA = contractA_WA.getHostCpuUtilizationMetric().getMinDimension().getValue();
		HostUpperUtilizationThresholdDAAS = contractB_DAAS.getHostCpuUtilizationMetric().getMaxDimension().getValue();
		HostLowerUtilizationThresholdDAAS = contractB_DAAS.getHostCpuUtilizationMetric().getMinDimension().getValue();
		
		System.out.println(" Customer A  arrives : (Web application)");

		RequestAnalyserAndConfigurationManagerWebApplication createCloudletandVmPlanetLab = new RequestAnalyserAndConfigurationManagerWebApplication();

		
		createCloudletandVmPlanetLab.createOneVmAndCloudlet(100, HighcpuMedium, Customer_A);//100 
		createCloudletandVmPlanetLab.createOneVmAndCloudlet(100, Extralarge, Customer_A);//100
		createCloudletandVmPlanetLab.createOneVmAndCloudlet(200, Small,  Customer_A);// 300 
		createCloudletandVmPlanetLab.createOneVmAndCloudlet(300, Micro, Customer_A);//300 
		setInitialVmlistWebApp(createCloudletandVmPlanetLab.getVmListWebApplication());
		
		System.out.printf("# %s submitted %d VMs \n", Customer_A, InitialvmListWebApplication.size());
		
		// Initialization: Desktop as a service----------> create virtual machines and cloudlets
 
		System.out.println(" Customer B arrives : (Desktop as a service)");

		RequestAnalyserAndConfigurationManagerDaaS createCloudletsandVmDaaS = new RequestAnalyserAndConfigurationManagerDaaS();
		createCloudletsandVmDaaS.setGlobalControler(this);
		createCloudletsandVmDaaS.createVm(30, DaasVmType1, Customer_B, -1, simulation);// 30 																				
		createCloudletsandVmDaaS.createVm(30, DaasVmType2, Customer_B, -1,simulation);//30 
		createCloudletsandVmDaaS.createVm(70, DaasVmType3, Customer_B, -1,simulation);//90 
		createCloudletsandVmDaaS.createVm(40, DaasVmType4, Customer_B, -1,simulation);//40
		createCloudletsandVmDaaS.createVm(50, DaasVmType5, Customer_B, -1,simulation);//50
		RACMDaaS = createCloudletsandVmDaaS;
		setInitialVmlistDaas(createCloudletsandVmDaaS.getvmQueue());
		
		System.out.printf("# %s submitted %d VMs \n", Customer_B, InitialvmListDaas.size());

		Customer_B.setVmDestructionDelay(0);

		dispatcher();
	}

	/**
	 * This method has the finished vm's in DaaS cluster
	 * @return
	 */
	public List<Vm> getfinishedVmlistDaas(){
		return RACMDaaS.FinishedvmList;
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
	 * This method gets the virtual machines that are created initially during the start of the simulation for the web application.
	 * @return {@link #InitialvmListWebApplication}
	 * @see #modelUserRequests(CloudSim) for vm creation
	 */
	public List<Vm> getInitialVmlistWebApp() {
		return InitialvmListWebApplication;
	}

	/**
	 * This method gets the virtual machines that are created initially during the start of the simulation for the application desktop as a service.
	 * @return {@link #InitialvmListDaas}
	 * @see #modelUserRequests(CloudSim) for vm creation
	 */
	public List<Vm> getInitialVmlistDaas() {
		return InitialvmListDaas;
	}

	/**
	 * This method dispatches the initial virtual machines that are created during the start of the simulation to it's respective local controllers
	 */
	private void dispatcher() {
		WebApplication.VmListForPlacement(InitialvmListWebApplication);
		Daas.VmListForPlacement(InitialvmListDaas);
	}
	
	/**
	 * This method updates the InitialvmListDaas when the vm starts to arrive after the thinking time. The initially finished vms will be collected in
	 *  the FinishesVmlistDay1Daas.
	 * @param Vmlist
	 */
	private void updateVmListDaasAfterOffline(List<Vm> Vmlist){		
		List<Vm> Vmlistdummy = new ArrayList<>();
		for(Vm Finishedvm : InitialvmListDaas) {
			if(!Finishedvm.isCreated()) {
				FinishesVmlistDay1Daas.add(Finishedvm);
				Vmlistdummy.add(Finishedvm);
			}
		}	
		if(!Vmlistdummy.isEmpty()) {
			for(Vm vmsFinished : Vmlistdummy) {
				InitialvmListDaas.remove(vmsFinished);
			}	
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
	 * This method dispatcherDynamic() dispatches the virtual machines to the appropriate local controllers when they arrive after day 1.
	 * @param VmList
	 */
	public void dispatcherDynamic(List<Vm> VmList, int time) {
		updateVmListDaasAfterOffline(VmList);
		Daas.VmListForPlacementAfterOffline(VmList, time);
	}


	/**
	 * This method is used to calculate the global utilization of the datacenter based on all the operational hosts at any particular time instance.
	 * 
	 * @param HostList
	 * @param bestDynamicVmServerMap
	 * @param Vmlist
	 * @return 
	 */
	/*
	 * private double globalUtilizationComputationalUnit(List<Host> HostList,
	 * Map<Long, ArrayList<Long>> bestDynamicVmServerMap, List<Vm> Vmlist) { double
	 * GlobalClusterUtilization = 0.0; GlobalUtilizationComputation
	 * GlobalUtilization = new GlobalUtilizationComputation();
	 * GlobalUtilization.SetHostlist(HostList);
	 * 
	 * if (bestDynamicVmServerMap != null) { GlobalClusterUtilization =
	 * GlobalUtilization.GlobalUtilizationForNewPlacement(bestDynamicVmServerMap,
	 * HostList, Vmlist); serverUtil = GlobalUtilization.serverUtil; } else {
	 * GlobalClusterUtilization =
	 * GlobalUtilization.GetGlobalUtilizationBasedOnCPUAndRAM(); serverUtil =
	 * GlobalUtilization.serverUtil; }
	 * 
	 * return GlobalClusterUtilization; }
	 */

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
	  
		  GlobalClusterUtilization =  GlobalUtilization.GetGlobalUtilizationBasedOnCPUAndRAM(); 
		  serverUtil =  GlobalUtilization.serverUtil;
	  
		  return GlobalClusterUtilization; 
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
	 * This method helps maintain the load balanced in Daas setup by taking decisions such as aggressive consolidation, consolidation without adding any 
	 * additional hosts, adding additional hosts. 
	 *  
	 * @param LocalController
	 */
	public void globalDecisionMakerDAAS(LocalControllerDaas LocalController) {
		Time = LocalController.currentTime;
		double GlobalUtil = globalUtilizationComputationalUnit(LocalController.hostListDaas); 

		// vm selection and then dispatch
		SelectiionPolicyserverVmsmapDAAS = new HashMap<Host, Vm>();
		if (!LocalController.OverLoadedHostssetDaas.isEmpty() || !LocalController.UnderLoadedHostssetDaas.isEmpty()) {
			VmSelectionPolicyMaxAverageCPURAM VmSelection = new VmSelectionPolicyMaxAverageCPURAM();
			VmSelection.vmSelection(LocalController.hostListDaas, LocalController.OverLoadedHostssetDaas, LocalController.UnderLoadedHostssetDaas,
					LocalController.getVmRamUtilizationHistory(), HostUpperUtilizationThresholdDAAS, HostLowerUtilizationThresholdDAAS, Time);

			VmsToMigrate = VmSelection.VmsToMigrate;
			VmsToMigrateFromOverloadedHostsDAAS = VmSelection.VmsToMigrateFromOverloadedHosts;
		} else {
			VmsToMigrate = new ArrayList<Vm>();
		}

		AggressiveConsolidation = false;
		if ((GlobalUtil < 0.65) && !onlyOneHostHasVmsAndIsUnderLoaded(LocalController.hostListDaas,
				LocalController.UnderLoadedHostssetDaas)) {
			boolean hostHasMigratingInVms = false;

			Set<Vm> vmSet = selectHostwithNoMigratingInVms(LocalController.hostListDaas, hostHasMigratingInVms);

			if((!vmSet.isEmpty()) || (!VmsToMigrate.isEmpty())) {
				AggressiveConsolidation = aggressiveConsolidation(LocalController);
			}
		}
		ConsolidationWithoutAdditionalHosts = false;
		if ((AggressiveConsolidation == false) && (!VmsToMigrate.isEmpty())
				&& (!onlyOneHostHasVmsAndIsUnderLoaded(LocalController.hostListDaas,
						LocalController.UnderLoadedHostssetDaas))) {
			ConsolidationWithoutAdditionalHosts = ConsolidationWithoutAdditionalhosts(LocalController);
		
		}
		
		boolean AllocatedWithAdditionalHosts = false;
		if ((!onlyOneHostHasVmsAndIsUnderLoaded(LocalController.hostListDaas, LocalController.UnderLoadedHostssetDaas))
				&& ((AggressiveConsolidation == false) && (ConsolidationWithoutAdditionalHosts == false))
				&& (!LocalController.OverLoadedHostssetDaas.isEmpty())) {
			AllocatedWithAdditionalHosts = AddAdditionalhost(LocalController);
			
		}
	
		if ((!onlyOneHostHasVmsAndIsUnderLoaded(LocalController.hostListDaas, LocalController.UnderLoadedHostssetDaas))
				&& ((AggressiveConsolidation == false) && (ConsolidationWithoutAdditionalHosts == false) && (AllocatedWithAdditionalHosts == false))) {
			LocalController.setPreviousMapAsCurrentMap();

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
	private boolean aggressiveConsolidation(LocalControllerDaas LocalController) {
		boolean AggressiveConsolidationWorked = false;
		List<Vm> VmsInMInUtilHost = new ArrayList<Vm>();
		if (HostWithMinUtili != null) {
			if (!HostWithMinUtili.getVmList().isEmpty()) {
				for (Vm vm : HostWithMinUtili.getVmList()) {
					if (!vm.isInMigration()) {
						VmsInMInUtilHost.add(vm);
					}
				}
			}
		}
		for (Vm vm : VmsToMigrate) {
			if (!VmsInMInUtilHost.contains(vm)) {
				VmsInMInUtilHost.add(vm);
			}
		}
		List<Host> targetHostList = new ArrayList<Host>();
		LocalController.hostListDaas.forEach(host -> {//
			if ((!host.getVmList().isEmpty()) && (!AllVmsOffline(host))
					&& (!LocalController.OverLoadedHostssetDaas.contains(host)) && (host != HostWithMinUtili)
					&& (!LocalController.UnderLoadedHostssetDaas.contains(host)) && (!AllVmsMigratingOut(host))) {
				targetHostList.add(host);
			}
		});

		
		if(LocalController.hostListDaas.size() == LocalController.UnderLoadedHostssetDaas.size()) {
			targetHostList.addAll(LocalController.hostListDaas);
			targetHostList.remove(HostWithMinUtili);
			VmsToMigrate = HostWithMinUtili.getVmList();
			VmsInMInUtilHost = HostWithMinUtili.getVmList();
		}

		boolean CaseAWorked = false;
		double counter = 0 ;
		if(!targetHostList.isEmpty()) {
			counter = calculateAvailableFreeResourceDAAS(targetHostList, VmsInMInUtilHost);
		}
		
		if(VmsInMInUtilHost.size() <= counter) { 
			LocalController.sendVMCharToGACallFromGC(serverUtil);
			LocalController.SendTargerhostlistDynamicPlacement(targetHostList);
			LocalController.placementManagerForDynamicPlacement(InitialvmListDaas, VmsInMInUtilHost,VmsToMigrateFromOverloadedHostsDAAS);
			if(LocalController.GAChooseSourceMap == true && !VmsToMigrateFromOverloadedHostsDAAS.isEmpty()) {
				CaseAWorked = false;
			}else {
				CaseAWorked = true;
				AggressiveConsolidationWorked = true;
			}
		} 
		
		if(CaseAWorked == false){
			if(!VmsToMigrate.isEmpty() && !LocalController.UnderLoadedHostssetDaas.contains(HostWithMinUtili)) {
				targetHostList.add(HostWithMinUtili);
				if(!targetHostList.isEmpty()) {
					counter = calculateAvailableFreeResourceDAAS(targetHostList,VmsToMigrate);
				}
				if(VmsToMigrate.size() <= counter) { 
					LocalController.sendVMCharToGACallFromGC(serverUtil);
					LocalController.SendTargerhostlistDynamicPlacement(targetHostList);
					LocalController.placementManagerForDynamicPlacement(InitialvmListDaas, VmsToMigrate,VmsToMigrateFromOverloadedHostsDAAS);
					if(LocalController.GAChooseSourceMap == true && !VmsToMigrateFromOverloadedHostsDAAS.isEmpty()) {
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
	 * Without adding additional hosts, the hosts are brought back to the normal state, if the resources are enough.
	 * 
	 * @param LocalController
	 * @return
	 */
	private boolean ConsolidationWithoutAdditionalhosts(LocalControllerDaas LocalController) {
		boolean ConsolidationWithoutAdditionalHosts = false;
		List<Host> targetHostList = new ArrayList<Host>();
		Set<Host> UnderLoadedHosts = new HashSet<Host>();
		Set<Host> UnderLoadedHosts1 = new HashSet<Host>();
		UnderLoadedHosts.addAll(LocalController.UnderLoadedHostssetDaas);
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
				counter = calculateAvailableFreeResourceDAAS(targetHostList, VmsToMigrate);
			}

			if(VmsToMigrate.size() <= counter) { 
				LocalController.sendVMCharToGACallFromGC(serverUtil);
				LocalController.SendTargerhostlistDynamicPlacement(targetHostList);
				LocalController.placementManagerForDynamicPlacement(InitialvmListDaas, VmsToMigrate,VmsToMigrateFromOverloadedHostsDAAS);
				if(LocalController.GAChooseSourceMap == true && !VmsToMigrateFromOverloadedHostsDAAS.isEmpty()) {
					ConsolidationWithoutAdditionalHosts = false;
				}else {
					ConsolidationWithoutAdditionalHosts = true;
				}
			}
		}
			
		if(LocalController.hostListDaas.size() == LocalController.UnderLoadedHostssetDaas.size()) {
			LocalController.sendVMCharToGACallFromGC(serverUtil);
			LocalController.SendTargerhostlistDynamicPlacement(targetHostList);
			LocalController.placementManagerForDynamicPlacement(InitialvmListDaas, VmsToMigrate,VmsToMigrateFromOverloadedHostsDAAS);
			if(LocalController.GAChooseSourceMap == true && !VmsToMigrateFromOverloadedHostsDAAS.isEmpty()) {
				ConsolidationWithoutAdditionalHosts = false;
			}else {
				ConsolidationWithoutAdditionalHosts = true;
			}
		}
		return ConsolidationWithoutAdditionalHosts;
	}


	/**
	 * This method add additional hosts to the target hostlist when the resources are not enough.
	 * @param LocalController
	 * @return
	 */
	private boolean AddAdditionalhost(LocalControllerDaas LocalController) {
		boolean AllocatedWithAdditionalHosts = false;
		List<Host> targetHostList = new ArrayList<Host>();

		LocalController.hostListDaas.forEach(host -> {
			if ((!host.getVmList().isEmpty()) && (!LocalController.OverLoadedHostssetDaas.contains(host))) {
				targetHostList.add(host);
			}			
		});
		boolean gaChooseSourceMapWhenHostOverloaded = true;
		boolean ExitWhile = true;
		double counter = 0;
		while(gaChooseSourceMapWhenHostOverloaded) {
			while(ExitWhile) {
				Optional<Host> additionalserver =	LocalController.hostListDaas.stream().filter(host -> host.getVmList().isEmpty())
						.filter(host -> !(targetHostList.contains(host))).findAny();
				if(additionalserver.isPresent() && !targetHostList.contains(additionalserver.get())){
					targetHostList.add(additionalserver.get());
				}		
				
				counter = 0 ;
				if(!targetHostList.isEmpty()) {
					counter = calculateAvailableFreeResourceDAAS(targetHostList,VmsToMigrate);
				}
				if(VmsToMigrate.size() <= counter) { 
					ExitWhile = false;
				}
			}		

		LocalController.sendVMCharToGACallFromGC(serverUtil);
		LocalController.SendTargerhostlistDynamicPlacement(targetHostList);
		LocalController.placementManagerForDynamicPlacement(InitialvmListDaas, VmsToMigrate,VmsToMigrateFromOverloadedHostsDAAS);
		if(LocalController.GAChooseSourceMap == true && !VmsToMigrateFromOverloadedHostsDAAS.isEmpty()) {
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
				HostWithMinUtili = hostlist.get(0);
				vm.addAll(HostWithMinUtili.getVmList());
			}
		}
		return vm;
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


	public static List<Vm> VmsToMigrate = new ArrayList<Vm>();
	public static List<Vm> VmsToMigrateFromOverloadedHostsDAAS = new ArrayList<Vm>();
	public static List<Vm> VmsToMigrateFromOverloadedHostsWA = new ArrayList<Vm>();

	/**
	 * This method picks a host with minimum utilization from the host list presented. It ignores if the all the vms in a host are migrating out,
	 * if all the vms of a host are in offline state, and if host has a vm migrating in. 
	 * 
	 * @param Hostlist
	 * @param serverUtilMap
	 * @return
	 */
	private List<Host> hostWithMinUtil(List<Host> Hostlist, Map<Long, Double> serverUtilMap) {
		if (!BestMinUtilHost.isEmpty()) {
			BestMinUtilHost.remove(0);
		}
		double minUtilValue = Collections.min(serverUtilMap.values());
																		
		boolean pickhost = false;
		while (!pickhost) {
			Outerloop: for (Entry<Long, Double> entry : serverUtilMap.entrySet()) {
				if (entry.getValue().equals(minUtilValue)) {

					HostWithMinUtil = entry.getKey().intValue();
					HostMinUtil = Hostlist.get((int) HostWithMinUtil);
					if ((HostMinUtil.getVmsMigratingIn().isEmpty()) && (!AllVmsMigratingOut(HostMinUtil))
							&& (!AllVmsOffline(HostMinUtil))) {
						BestMinUtilHost.add(HostMinUtil);
							break Outerloop;
					}
				}
			}
			pickhost = true;
		}
		return BestMinUtilHost;
	}

	/**
	 * This method estimates the free resources that are available in the provided hostlist. It returns the counted number, that tells the hostlist has
	 * approximately enough resources for that number of vm's considering the custom vm size.
	 * 
	 * @param Hostlist
	 * @param VmstoMigrate
	 * @return
	 */
	private double calculateAvailableFreeResourceWA(List<Host> Hostlist, List<Vm> VmstoMigrate){
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
	 * This method estimates the free resources that are available in the provided hostlist. It returns the counted number, that tells the hostlist has
	 * approximately enough resources for that number of vm's considering the custom vm size.
	 * 
	 * @param Hostlist
	 * @param VmstoMigrate
	 * @return
	 */
	private double calculateAvailableFreeResourceDAAS(List<Host> Hostlist, List<Vm> VmstoMigrate){

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
	 * This method helps maintain to balance load in Web application setup by taking decisions such as aggressive consolidation, consolidation without adding any 
	 * additional hosts, adding additional hosts. 
	 * 
	 * @param LocalController
	 */
	public void GlobalDecisionMakerWA(LocalControllerWA LocalController) {
		Time = LocalController.currentTime;
		 double GlobalUtil = globalUtilizationComputationalUnit(LocalController.hostListWebApplication);
		
		// vm selection and then dispatch
		 SelectiionPolicyserverVmsmapWA = new HashMap<Host, Vm>();
		if (!LocalController.OverLoadedHostssetWebApplication.isEmpty() || !LocalController.UnderLoadedHostssetWebApplication.isEmpty()) {
			VmSelectionPolicyMaxAverageCPURAM VmSelection = new VmSelectionPolicyMaxAverageCPURAM();
			VmSelection.vmSelection(LocalController.hostListWebApplication, LocalController.OverLoadedHostssetWebApplication, LocalController.UnderLoadedHostssetWebApplication,
					LocalController.WAVmsRamUtilizationHistory, HostUpperUtilizationThresholdWA, HostLowerUtilizationThresholdWA, Time);
//			vmSelectionWA(LocalController.hostListWebApplication, LocalController.OverLoadedHostssetWebApplication, LocalController.UnderLoadedHostssetWebApplication);
			VmsToMigrate = VmSelection.VmsToMigrate;
			VmsToMigrateFromOverloadedHostsWA = VmSelection.VmsToMigrateFromOverloadedHosts;	
		}else {
			VmsToMigrate = new ArrayList<Vm>();
		}
		 
		AggressiveConsolidation = false;
		if ((GlobalUtil < 0.65) && !onlyOneHostHasVmsAndIsUnderLoaded(LocalController.hostListWebApplication,
				LocalController.UnderLoadedHostssetWebApplication)) {
			boolean hostHasMigratingInVms = false;

			Set<Vm> vmSet = selectHostwithNoMigratingInVms(LocalController.hostListWebApplication, hostHasMigratingInVms);
			if((!vmSet.isEmpty()) || (!VmsToMigrate.isEmpty())) {
				AggressiveConsolidation = aggressiveConsolidation(LocalController);
			}
		}

		ConsolidationWithoutAdditionalHosts = false;
		if ((AggressiveConsolidation == false) && (!VmsToMigrate.isEmpty())	&& (!onlyOneHostHasVmsAndIsUnderLoaded(LocalController.hostListWebApplication,
						LocalController.UnderLoadedHostssetWebApplication))) {
			ConsolidationWithoutAdditionalHosts = ConsolidationWithoutAdditionalhosts(LocalController);
		}
	
		boolean AllocatedWithAdditionalHosts = false;
		if ((!onlyOneHostHasVmsAndIsUnderLoaded(LocalController.hostListWebApplication, LocalController.UnderLoadedHostssetWebApplication))
				&& ((AggressiveConsolidation == false) && (ConsolidationWithoutAdditionalHosts == false))
				&& (!LocalController.OverLoadedHostssetWebApplication.isEmpty())) {
			AllocatedWithAdditionalHosts = AddAdditionalhostWA(LocalController);
		}
		if ((!onlyOneHostHasVmsAndIsUnderLoaded(LocalController.hostListWebApplication, LocalController.UnderLoadedHostssetWebApplication))
				&& ((AggressiveConsolidation == false) && (ConsolidationWithoutAdditionalHosts == false) && (AllocatedWithAdditionalHosts == false))
				&& (!LocalController.OverLoadedHostssetWebApplication.isEmpty())) {
			LocalController.SetPreviousMapAsCurrentMap();
		}
	}


	
	/**
	 * This method initially tries to get a server with minimum utilization based on ram, cpu and tries to fit in all the vm's from that host into the other
	 * operational hosts. when the required resources by all those vm's are less than the free resources in all the operational hosts then aggresiive 
	 * cosolidation case a will take place. This process also takes into consideration of the vm's from overloaded and underloaded hosts and tries to alter its placement 
	 * by choosing the target host appropriately.
	 * The other case works the same way, it tries to fit in all the vm's from overloaded hosts and underloaded hosts in other operational hosts. 
	 * When there are enough resources available then it approves the decision and passes its decision on to the local controller for implemetation. 
	 *  
	 * @param LocalController
	 */
	private boolean aggressiveConsolidation(LocalControllerWA LocalController) {
		boolean AggressiveConsolidationWorked = false; 
		List<Vm> VmsInMInUtilHost = new ArrayList<Vm>();
		if (HostWithMinUtili != null) {
			if (!HostWithMinUtili.getVmList().isEmpty()) {
				for (Vm vm : HostWithMinUtili.getVmList()) {
					if (!vm.isInMigration()) {
						VmsInMInUtilHost.add(vm);
					}
				}
			}
		}
		
		for (Vm vm : VmsToMigrate) {
			if (!VmsInMInUtilHost.contains(vm)) {
				VmsInMInUtilHost.add(vm);
			}
		}
		
		List<Host> targetHostList = new ArrayList<Host>();
		LocalController.hostListWebApplication.forEach(host -> {
			if((!host.getVmList().isEmpty()) && (!AllVmsOffline(host))
					&& (!LocalController.OverLoadedHostssetWebApplication.contains(host)) && (host != HostWithMinUtili)
					&& (!LocalController.UnderLoadedHostssetWebApplication.contains(host)) && (!AllVmsMigratingOut(host))) {
				targetHostList.add(host);
			}
		});

		double counter = 0 ;
		if(!targetHostList.isEmpty()) {
			counter = calculateAvailableFreeResourceWA(targetHostList,VmsInMInUtilHost);
		}
		if(VmsInMInUtilHost.size() <= counter) { 
			LocalController.sendVMCharToGACallFromGC(serverUtil);
			LocalController.SendTargerhostlistDynamicPlacement(targetHostList);
			LocalController.placementManagerForDynamicPlacement(InitialvmListWebApplication, VmsInMInUtilHost, VmsToMigrateFromOverloadedHostsWA);
			AggressiveConsolidationWorked = checkCaseAworked(LocalController, AggressiveConsolidationWorked);
		}
		
		if( CaseAWorkedWA != true){
			if(!VmsToMigrate.isEmpty()){
				targetHostList.add(HostWithMinUtili);

				counter = 0 ;
				if(!targetHostList.isEmpty()) {
					counter = calculateAvailableFreeResourceWA(targetHostList, VmsToMigrate);
				}
				if(VmsToMigrate.size() <= counter) {  
					LocalController.sendVMCharToGACallFromGC(serverUtil);	
					LocalController.SendTargerhostlistDynamicPlacement(targetHostList);
					LocalController.placementManagerForDynamicPlacement(InitialvmListWebApplication, VmsToMigrate, VmsToMigrateFromOverloadedHostsWA);
					if(LocalController.GAChooseSourceMapWA == true && !VmsToMigrateFromOverloadedHostsWA.isEmpty()) {
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
	private boolean checkCaseAworked(LocalControllerWA LocalController, boolean AggressiveConsolidationWorked) {
		
		if(LocalController.GAChooseSourceMapWA == true && !VmsToMigrateFromOverloadedHostsWA.isEmpty()) {
			CaseAWorkedWA = false;
		}else {
			CaseAWorkedWA = true;
			AggressiveConsolidationWorked = true;
		}
		return AggressiveConsolidationWorked;
	}

	/**
	 * Without adding additional hosts, the hosts are brought back to the normal state, if the resources are enough.
	 * 
	 * @param LocalController
	 * @return
	 */
	private boolean ConsolidationWithoutAdditionalhosts(LocalControllerWA LocalController) {
		boolean ConsolidationWithoutAdditionalHosts = false;
		List<Host> targetHostList = new ArrayList<Host>();
		Set<Host> UnderLoadedHosts = LocalController.UnderLoadedHostssetWebApplication;
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
				counter = calculateAvailableFreeResourceWA(targetHostList,VmsToMigrate);
			}
			if(VmsToMigrate.size() <= counter) { 
				ExitWhile = false;
			}
		}
		if(!targetHostList.isEmpty() && (VmsToMigrate.size() <= counter) ) {
			LocalController.sendVMCharToGACallFromGC(serverUtil);
			LocalController.SendTargerhostlistDynamicPlacement(targetHostList);
			LocalController.placementManagerForDynamicPlacement(InitialvmListWebApplication, VmsToMigrate, VmsToMigrateFromOverloadedHostsWA);
			if(LocalController.GAChooseSourceMapWA == true && !VmsToMigrateFromOverloadedHostsWA.isEmpty()) {
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
	 * 
	 * @param LocalController
	 * @return
	 */
	private boolean AddAdditionalhostWA(LocalControllerWA LocalController) {
		boolean AllocatedWithAdditionalHosts = false;
		List<Host> targetHostList = new ArrayList<Host>();
		
		LocalController.hostListWebApplication.forEach(host -> {
			if ((!host.getVmList().isEmpty()) && (!LocalController.OverLoadedHostssetWebApplication.contains(host))) {
				targetHostList.add(host);
			}			
		});

		boolean gaChooseSourceMapWhenHostOverloaded = true;
		boolean ExitWhile = true;
		double counter = 0;
		while(gaChooseSourceMapWhenHostOverloaded) {
			while(ExitWhile) {
				Optional<Host> additionalserver =	LocalController.hostListWebApplication.stream().filter(host -> host.getVmList().isEmpty())
						.filter(host -> !(targetHostList.contains(host))).findAny();
				if(additionalserver.isPresent()&& !targetHostList.contains(additionalserver.get())){
					targetHostList.add(additionalserver.get());
				}		
				
				counter = 0 ;
				if(!targetHostList.isEmpty()) {
					counter = calculateAvailableFreeResourceWA(targetHostList,VmsToMigrate);
				}
				if(VmsToMigrate.size() <= counter) { 
					ExitWhile = false;
				}
			}
		
			LocalController.sendVMCharToGACallFromGC(serverUtil);
			LocalController.SendTargerhostlistDynamicPlacement(targetHostList);
			LocalController.placementManagerForDynamicPlacement(InitialvmListWebApplication, VmsToMigrate,VmsToMigrateFromOverloadedHostsWA);
			if(LocalController.GAChooseSourceMapWA == true && !VmsToMigrateFromOverloadedHostsWA.isEmpty()) {
				gaChooseSourceMapWhenHostOverloaded = true;
				ExitWhile = true;
			}else {
				gaChooseSourceMapWhenHostOverloaded = false;
			}
			AllocatedWithAdditionalHosts = true;
		}	
		return AllocatedWithAdditionalHosts;
	}

}
