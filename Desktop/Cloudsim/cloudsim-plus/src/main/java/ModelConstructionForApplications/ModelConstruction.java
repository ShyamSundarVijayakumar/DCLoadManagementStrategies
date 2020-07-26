/**
 * 
 */
package ModelConstructionForApplications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.cloudbus.cloudsim.AutonomicLoadManagementStrategies.SimulationResults;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterSimpleCM;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.EventInfo;

import centrlizedarchitecture.centralManager;
import hierarchicalarchitecture.globalcontroller.GlobalController;
import hierarchicalarchitecture.localcontrollerdaas.LocalControllerDaas;
import hierarchicalarchitecture.localcontrollerwebapp.LocalControllerWA;


/**
 * This class is dedicated to construct the simulation.
 * Initially the datacenter will be created and the hosts associated to the datacenter are created seperately as per the application.
 * 
 * For web application 750 hosts of two different types are created.
 * for Daas 8 homogeneous hosts are created.
 * 
 * Hierarchical architecture:
 * The global controller is instantiated. In this the user arrivals are modelled and its VM types are created.
 * Then the local controllers are created.
 * 
 * The same process is carried on for central architecture with central manager.
 * 
 * 
 * @author Shyam Sundar V
 *
 */
public class ModelConstruction {
    public CloudSim simulation;
    public List<Host> hostsListDesktopAsAService = new ArrayList<>();
    public List<Host> hostsListPlanetLab = new ArrayList<>();
    
    /**
     * A map to store RAM utilization history for every VM.
     * Each key is a VM and each value is another map.
     * This entire data structure is usually called a multi-map.
     *
     * Such an internal map stores RAM utilization for a VM.
     * The keys of this internal map are the time the utilization was collected (in seconds)
     * and the value the utilization percentage (from 0 to 1).
     */
    public static Map<Vm, Map<Integer, Long>> DAASVmsRamUtilizationHistory;
    public static Map<Vm, Map<Integer, Long>> WAVmsRamUtilizationHistory;
    
    private List<Vm> VmlistDAAS;
    private List<Vm> VmlistWA;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ModelConstruction();
	}     

	public ModelConstruction() {
		
    	System.out.println("Starting " + getClass().getSimpleName());
    	
    	// please change the workload file directory in requestAnalyserandconfigurationmanager web app class
  	  	simulation = new CloudSim();
    	simulation.terminateAt(2*24*60*60);
    	double StartTime = System.nanoTime();
    	System.out.println("StartTime in nanoSec= "+ StartTime);
    	System.out.println("StartTime in millisec = "+ System.currentTimeMillis());
    	double StartTimeinMS = System.currentTimeMillis();
	    hierarchicalArchitecture();
//	    centralArchitecture();
	    
	//    System.out.println("StartTime "+ StartTime); Host CPU or ram has reached
//	    System.out.println("Total elaspsed Time "+ (EndTime-StartTime));
	    System.out.println(" Simulation total elapsed time in nanoseconds "+ (EndTime - StartTime));
	    System.out.printf(" Simulation start time | %f, end time | %f , Total time in ms %f %n", StartTimeinMS , EndTimeinMS , (EndTimeinMS - StartTimeinMS));
	}

	centralManager Centralmanager;
	private void centralArchitecture() {
		
		CreateDatacenterCA createDC = new CreateDatacenterCA();
		   
		 // Creating 400 hosts of type "HP Proliant G5" and 400 hosts of type "HP Proliant G4" for web application.
		for(int i=0;i<350;i++){//Also change the static host count given above
			createDC.createHostsWebApplication(1,2);  //"HP Proliant G5");
			createDC.createHostsWebApplication(1,1);  //"HP Proliant G4"); //400
		}
		 hostsListPlanetLab.addAll(createDC.getHostsListWebApplication());
		    
		 // Creating 8 hosts for desktop as a service application
		 for(int i=0;i<8;i++){//Also change the static host count given above
			 createDC.createHostsDaaS(1);	
		 }
		 hostsListDesktopAsAService.addAll(createDC.getHostsListDaaS());
		   
		 centralManager CM = new centralManager();
		 CM.SetHostlistDaas(hostsListDesktopAsAService);
		 CM.SetHostlistWebApp(hostsListPlanetLab);
		 CM.createDatacenterBrokers(simulation);
		 CM.modelUserRequests(simulation);
		
		DatacenterSimpleCM dc1 = createDC.creatingSimpleDatacenter(simulation);  
		CM.setDcSimulation(dc1, simulation);
    
		DAASVmsRamUtilizationHistory = initializeUtilizationHistory(CM.InitialvmListDaas);
		WAVmsRamUtilizationHistory = initializeUtilizationHistory(CM.InitialvmListWebApplication);
		VmlistDAAS = CM.InitialvmListDaas;
	    VmlistWA = CM.InitialvmListWebApplication;
	    Centralmanager = CM;
	    simulation.addOnClockTickListener(this::onClockTickListenerCentralized);
	    simulation.start();
	    EndTime = System.nanoTime();
	    EndTimeinMS = System.currentTimeMillis();
	    SimulationResults printresults = new SimulationResults();
	    
	    printresults.printHostCpuUtilizationAndPowerConsumptionNewDaas(simulation, CM.Customer_B, hostsListDesktopAsAService); 
		    
	    double daasclusterPowerKWattsHour = printresults.getdcClusterPowerinKWattsHour();
	    Map<Host, Double> DaaSHostwithIdealperiod = printresults.HostwithIdealperiod;
	    printresults.setdcClusterpowertozero();
	    printresults.SetHostIdealperiodMapTonew();
	    double averageExecutionTime = (printresults.totalexecutiontimeofallCloudlets / printresults.totalFinishedCloudlets);
	  
	    printresults.printHostCpuUtilizationAndPowerConsumptionNew(simulation, CM.Customer_A, hostsListPlanetLab);
	    double PLclusterPowerKWattsHour = printresults.getdcClusterPowerinKWattsHour();
	    printresults.setdcClusterpowertozero();  
	 
	    Map<Host, Double> WAHostwithIdealperiod = printresults.HostwithIdealperiod;
	    List<Vm> FinishedVmlist = CM.getfinishedVmlistDaas();
	    Map<Vm, Double> VmWithPercentViolation = dc1.getVmMigrationVioSla();
	   
	    VmWithPercentViolation.forEach((vm, violationtime)->{
	    	VmtotalexecTime = 0;
	    	TotalCPUrequestedByVm = 0;
	    	FinishedVmlist.forEach((vm1)->{
	    		if((vm.getId() == vm1.getId())) {
	    			VmtotalexecTime += vm1.getTotalExecutionTime();
	    			TotalCPUrequestedByVm = vm.getMips() * VmtotalexecTime;
	    		}
	    	});
	    	CM.InitialvmListDaas.forEach((vm2)->{
	    		VmtotalexecTime1 = 0;
	    		if((vm.getId() == vm2.getId())) {
	    			VmtotalexecTime1 += vm2.getTotalExecutionTime();
	    			TotalCPUrequestedByVm += vm.getMips() * VmtotalexecTime1;
	    		}
	    	});
	    	ViolationDuetoMigration +=	(violationtime / TotalCPUrequestedByVm);
	    });
	   
	    Map<Vm, Double> VmWithPercentViolationWA = dc1.getVmMigrationVioSlaWA();    
	    VmWithPercentViolationWA.forEach((vm, violationtime)->{
	    	TotalCPUrequestedByVmWA = 0;
		    VmtotalexecTimeWA = 0;
		    VmlistWA.forEach((vm2)->{
		    	if(vm.getId() == vm2.getId()) {
		    		VmtotalexecTimeWA += vm.getTotalExecutionTime();
		    		TotalCPUrequestedByVmWA = vm.getMips() * VmtotalexecTimeWA;
		    	}
		    });
		    ViolationDuetoMigrationWA += (violationtime / TotalCPUrequestedByVmWA);
	    });
	    
	    printHostIdealtime(WAHostwithIdealperiod);
	    printHostIdealtime(DaaSHostwithIdealperiod);
	    System.out.printf(" DaaS cluster power in KWattsHour = %.4f %n", daasclusterPowerKWattsHour);
	    System.out.printf(" WAcluster power in KWattsHour = %.4f %n", PLclusterPowerKWattsHour);
	    System.out.println(" Total no of Migrations DaaS = "+ dc1.totalnoofMigrationsDaaS);
	    System.out.println(" Total no of Migrations WA  = "+ dc1.totalnoofMigrationsWA);
	    System.out.printf(" ViolationDuetoMigration DaaS = %f | Total DaaS vm's = %4d %n", ViolationDuetoMigration ,(VmlistDAAS.size() + FinishedVmlist.size()));
	    System.out.println(" ViolationDuetoMigration PDM = " + ViolationDuetoMigration/(VmlistDAAS.size()+FinishedVmlist.size()));
	    System.out.printf(" ViolationDuetoMigrationWA = %f | Total WA vm's = %d %n", ViolationDuetoMigrationWA, VmlistWA.size());
	    System.out.println(" ViolationDuetoMigrationWA PDM = "+ (ViolationDuetoMigrationWA / VmlistWA.size()));
	    System.out.println(" averageExecutionTime DaaS = "+ averageExecutionTime );
	    
	}
	
	LocalControllerDaas lcDaas ;
	LocalControllerWA lcWA ;
	/**
	 * @param createDC
	 */
	double EndTime = 0;
	double EndTimeinMS = 0;
	private void hierarchicalArchitecture() {	
		CreateDatacenter createDC = new CreateDatacenter();
		   
		// Creating 400 hosts of type "HP Proliant G5" and 400 hosts of type "HP Proliant G4" for web application.
		for(int i=0;i<350;i++){//Also change the static host count given above+
			createDC.createHostsWebApplication(1,2);  //"HP Proliant G5");
			createDC.createHostsWebApplication(1,1);  //"HP Proliant G4"); //400
		}
		hostsListPlanetLab.addAll(createDC.getHostsListWebApplication());
		    
		// Creating 8 hosts for desktop as a service application
		for(int i=0;i<8;i++){//Also change the static host count given above
			createDC.createHostsDaaS(1);	
		}
		hostsListDesktopAsAService.addAll(createDC.getHostsListDaaS());
		   
		LocalControllerWA localcontrollerWebApplication = new LocalControllerWA();
	    localcontrollerWebApplication.SetHostlistWebApp(hostsListPlanetLab); //initialise allocation policy
	    lcWA = localcontrollerWebApplication;
	    LocalControllerDaas localcontrollerDaas = new LocalControllerDaas();
	    localcontrollerDaas.SetHostlistDaas(hostsListDesktopAsAService);
	//    localcontrollerDaas.setHostlistDaas(hostsListDesktopAsAService);
	    //Modelling brokers and initialising vms and cloudlets
	    GlobalController Globalcontroller = new GlobalController();
	    Globalcontroller.createDatacenterBrokers(simulation);//model cloud customers
	    Globalcontroller.modelUserRequests(simulation);//, dc1
	    
	    lcDaas = localcontrollerDaas;
	    DatacenterSimple dc1 = createDC.creatingSimpleDatacenter(simulation);//,brokerDC1);
	    localcontrollerDaas.setDcSimulationGlobalcontroller(dc1, simulation,Globalcontroller);  
	    localcontrollerWebApplication.setDcSimulationGlobalcontroller(dc1, simulation, Globalcontroller);
	   
	    DAASVmsRamUtilizationHistory = initializeUtilizationHistory(Globalcontroller.getInitialVmlistDaas());
	    WAVmsRamUtilizationHistory = initializeUtilizationHistory(Globalcontroller.getInitialVmlistWebApp());
	    VmlistDAAS = Globalcontroller.getInitialVmlistDaas();
	    VmlistWA = Globalcontroller.getInitialVmlistWebApp();
	    simulation.addOnClockTickListener(this::onClockTickListenerHierarchical);
	    simulation.start();
	    EndTime = System.nanoTime();
	    EndTimeinMS = System.currentTimeMillis();
	    
	    SimulationResults printresults = new SimulationResults(); 
	    printresults.printHostCpuUtilizationAndPowerConsumptionNewDaas(simulation, Globalcontroller.Customer_B, hostsListDesktopAsAService); 

	    double daasclusterPowerKWattsHour = printresults.getdcClusterPowerinKWattsHour();
	    
	    Map<Host, Double> DaaSHostwithIdealperiod = printresults.HostwithIdealperiod;
	    printresults.setdcClusterpowertozero();
	    printresults.SetHostIdealperiodMapTonew();
	    double averageExecutionTime = (printresults.totalexecutiontimeofallCloudlets / printresults.totalFinishedCloudlets);
	    
	    printresults.printHostCpuUtilizationAndPowerConsumptionNew(simulation, Globalcontroller.Customer_A, hostsListPlanetLab);
	    double PLclusterPowerKWattsHour = printresults.getdcClusterPowerinKWattsHour();
	    printresults.setdcClusterpowertozero(); 
	    
	    Map<Host, Double> WAHostwithIdealperiod = printresults.HostwithIdealperiod;
	    List<Vm> FinishedVmlist = Globalcontroller.getfinishedVmlistDaas();
	    Map<Vm, Double> VmWithPercentViolation = dc1.getVmMigrationVioSla();
	    
	    VmWithPercentViolation.forEach((vm, violationtime)->{
	    	VmtotalexecTime = 0;
	    	TotalCPUrequestedByVm = 0;
	    	FinishedVmlist.forEach((vm1)->{
	    		if((vm.getId() == vm1.getId())) {
	    			VmtotalexecTime += vm1.getTotalExecutionTime();
	    			TotalCPUrequestedByVm += vm.getMips() * VmtotalexecTime;
	    		}
	    	});
	    	localcontrollerDaas.vmListDaas.forEach((vm2)->{
	    		VmtotalexecTime1 = 0;
	    		if((vm.getId() == vm2.getId())) {
	    			VmtotalexecTime1 += vm2.getTotalExecutionTime();
	    			TotalCPUrequestedByVm += vm.getMips() * VmtotalexecTime1;
	    		}
	    	});
	    	if(TotalCPUrequestedByVm != 0) {
	    		ViolationDuetoMigration +=	(violationtime / TotalCPUrequestedByVm);	
	    	}
	    });
	   
	    Map<Vm, Double> VmWithPercentViolationWA = dc1.getVmMigrationVioSlaWA();
	    VmWithPercentViolationWA.forEach((vm, violationtime)->{
	    	VmtotalexecTimeWA = 0;
			TotalCPUrequestedByVmWA = 0;
			VmlistWA.forEach((vm2)->{
				if(vm.getId() == vm2.getId()) {
					VmtotalexecTimeWA += vm.getTotalExecutionTime();
					TotalCPUrequestedByVmWA = vm.getMips() * VmtotalexecTimeWA;
				}
			});
			ViolationDuetoMigrationWA += (violationtime / TotalCPUrequestedByVmWA);
	    });
	    
	    printHostIdealtime(WAHostwithIdealperiod);
	    printHostIdealtime(DaaSHostwithIdealperiod);
	    System.out.printf(" DaaScluster Power in KWattsHour = %.4f %n", daasclusterPowerKWattsHour);
	    System.out.printf(" WAcluster Power in KWattsHour = %.4f %n", PLclusterPowerKWattsHour);
	    System.out.println(" totalnoofMigrationsDaaS = "+ dc1.totalnoofMigrationsDaaS);
	    System.out.println(" totalnoofMigrationsWA  = "+ dc1.totalnoofMigrationsWA);
	    System.out.printf(" ViolationDuetoMigration DaaS = %f | Total DaaS vm's = %4d %n", ViolationDuetoMigration ,(VmlistDAAS.size() + FinishedVmlist.size()));
	    System.out.println(" ViolationDuetoMigration PDM = " + ViolationDuetoMigration/(VmlistDAAS.size()+FinishedVmlist.size()));
	    System.out.printf(" ViolationDuetoMigrationWA = %f | Total WA vm's = %4d %n", ViolationDuetoMigrationWA, VmlistWA.size());
	    System.out.println(" ViolationDuetoMigrationWA PDM = "+ (ViolationDuetoMigrationWA / VmlistWA.size()));
	    System.out.println(" averageExecutionTime DaaS= "+ averageExecutionTime );
	} 
	
	double TotalCPUrequestedByVmWA;
	double ViolationDuetoMigrationWA;
	double VmtotalexecTimeWA=0;
	double TotalCPUrequestedByVm;
	double ViolationDuetoMigration;
	double VmtotalexecTime=0;
	double VmtotalexecTime1=0;
	private void printHostIdealtime(Map<Host, Double> HostwithIdealperiod) {
		HostwithIdealperiod.forEach((host,idealtime)->{
			System.out.printf("\tHost %3d | Activetime: %f %n", host.getId(), (simulation.clock() - idealtime));
		});
	}
	
	/*
	 * VmWithPercentViolation.forEach((vm, violationtime)->{ VmtotalexecTime = 0;
	 * TotalCPUrequestedByVm = 0; VmtotalexecTimeWA = 0; TotalCPUrequestedByVmWA =
	 * 0;
	 * 
	 * FinishedVmlist.forEach((vm1)->{ if((vm.getId() == vm1.getId()) &&
	 * ((vm.getDescription() == "1 Desktop as a service") || (vm.getDescription() ==
	 * "2 Desktop as a service") || (vm.getDescription() ==
	 * "3 Desktop as a service") || (vm.getDescription() ==
	 * "4 Desktop as a service") || (vm.getDescription() ==
	 * "5 Desktop as a service"))) { VmtotalexecTime += vm1.getTotalExecutionTime();
	 * TotalCPUrequestedByVm = vm.getMips() * VmtotalexecTime; }else {
	 * VmlistWA.forEach((vm2)->{ if(vm.getId() == vm2.getId()) { VmtotalexecTimeWA
	 * += vm1.getTotalExecutionTime(); TotalCPUrequestedByVmWA = vm.getMips() *
	 * VmtotalexecTimeWA; } }); } }); ViolationDuetoMigration += (violationtime /
	 * TotalCPUrequestedByVm); ViolationDuetoMigrationWA += (violationtime /
	 * TotalCPUrequestedByVmWA); });
	 */
	
	
    /**
     * Initializes a map that will store utilization history for
     * some resource (such as RAM or BW) of every VM.
     * It also creates an empty internal map to store
     * the resource utilization for every VM along the simulation execution.
     * The internal map for every VM will be empty.
     * They are filled inside the {@link #onClockTickListener(EventInfo)}.
     */
    private Map<Vm, Map<Integer, Long>> initializeUtilizationHistory(List<Vm> vmList) {
        //TreeMap sorts entries based on the key
        final Map<Vm, Map<Integer, Long>> map = new HashMap<>();
        for (Vm vm : vmList) {
            map.put(vm, new TreeMap<>());
        }
        return map;
    }
    
    /**
     * Keeps track of simulation clock.
     * Every time the clock changes, this method is called.
     * To enable this method to be called at a defined
     * interval, you need to set the {@link Datacenter#setSchedulingInterval(double) scheduling interval}.
     *
     * @param evt information about the clock tick event
     * @see #SCHEDULING_INTERVAL
     */
    private void onClockTickListenerHierarchical(final EventInfo evt) {
        collectVmResourceUtilizationDAAS(DAASVmsRamUtilizationHistory, Ram.class);
        
        collectVmResourceUtilizationWA(WAVmsRamUtilizationHistory, Ram.class);
        lcDaas.ClassifyActiveHostsInDaasCluster((int) simulation.clock());
        lcWA.classifyActiveHostsInWebApplicationCluster((int) simulation.clock());
    }
  
    private void onClockTickListenerCentralized(final EventInfo evt) {
        collectVmResourceUtilizationDAAS(DAASVmsRamUtilizationHistory, Ram.class);
        
        collectVmResourceUtilizationWA(WAVmsRamUtilizationHistory, Ram.class);
        Centralmanager.classifyActiveHostsInWebApplicationCluster((int) simulation.clock());
        Centralmanager.ClassifyActiveHostsInDaasCluster((int) simulation.clock());
        
    }
    
    /**
     * Collects the utilization percentage of a given VM resource for every VM.
     * CloudSim Plus already has built-in features to obtain VM's CPU utilization.
     * Check {@link org.cloudsimplus.examples.power.PowerExample}.
     *
     * @param allVmsUtilizationHistory the map where the collected utilization for every VM will be stored
     * @param resourceClass the kind of resource to collect its utilization (usually {@link Ram} or {@link Bandwidth}).
     */
    private void collectVmResourceUtilizationDAAS(final Map<Vm, Map<Integer, Long>> allVmsUtilizationHistory, Class<? extends ResourceManageable> resourceClass) {
        for (Vm vm : VmlistDAAS) {
            /*Gets the internal resource utilization map for the current VM.
            * The key of this map is the time the usage was collected (in seconds)
            * and the value the percentage of utilization (from 0 to 1). */
            final Map<Integer, Long> vmUtilizationHistory = allVmsUtilizationHistory.get(vm);
            vmUtilizationHistory.put((int) simulation.clock(), vm.getResource(resourceClass).getAllocatedResource());
        }
    }
    
    private void collectVmResourceUtilizationWA(final Map<Vm, Map<Integer, Long>> allVmsUtilizationHistory, Class<? extends ResourceManageable> resourceClass) {

    //	System.out.println(" Model const111111111" + simulation.clock());
    	for (Vm vm : VmlistWA) {
        	
            /*Gets the internal resource utilization map for the current VM.
            * The key of this map is the time the usage was collected (in seconds)
            * and the value the percentage of utilization (from 0 to 1). */
            final Map<Integer, Long> vmUtilizationHistory = allVmsUtilizationHistory.get(vm);
           
  //          double rampercent = vm.getResource(resourceClass).getPercentUtilization();
   //         System.out.println("rampercent"+ rampercent);
    //        double getCurrentRequestedRam = vm.getCurrentRequestedRam();
     //       System.out.println("getCurrentRequestedRam"+ getCurrentRequestedRam);
      //      double HostRamUtil = vm.getHostRamUtilization();
       //     System.out.println("HostRamUtil"+ HostRamUtil);
            
            vmUtilizationHistory.put((int) simulation.clock(), vm.getResource(resourceClass).getAllocatedResource());//etCurrentRequestedRam(), vm.getResource(resourceClass).getPercentUtilization());
        }
    }	
}
