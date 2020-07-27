/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationBestFitStaticThreshold;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
//import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReaderDC2;
//import org.cloudbus.cloudsim.util.WorkloadFileReader;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelPlanetLab;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.listeners.CloudletVmEventInfo;
import org.cloudsimplus.listeners.DatacenterBrokerEventInfo;
import org.cloudsimplus.listeners.EventInfo;

import com.sun.glass.ui.Application;

import static java.util.Comparator.comparingLong;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * This class is dedicated to construct the a simulation model using different parameters to call different methods in the written classes .
 * @param  {@link ModelConstruction#SCHEDULE_INTERVAL}
 * @param  {@link ModelConstruction#hostList}
 * @param  {@link ModelConstruction#vmList}
 * @param  {@link ModelConstruction#cloudletList}
 * @param  {@link ModelConstruction#simulation}
 * @param  {@link ModelConstruction#loadManagementStrategy}
 *  
 * @author Abdulrahman Nahhas
 * @since CloudSim Plus 1.0
 * 
 */
public class ModelConstruction
{

    public CloudSim simulation;
    public int loadManagementStrategy = 2;
    public static final int  SCHEDULE_INTERVAL = 1;//for dc1
   
    //New DC
	public List<Host> hostsListDC1 = new ArrayList<>();
    public List<Vm> vmListDC1 = new ArrayList<>();
    public List<Cloudlet> cloudletListDC1 = new ArrayList<>();
    
    public List<Host> hostsListDC2 = new LinkedList<>();
    public List<Vm> vmListDC2 = new LinkedList<>();
    public Queue<Cloudlet> CloudletQueueDC2 = new LinkedList<>();
    
    public List<Host> hostListDC3 = new LinkedList<>();
    public Queue<Vm> vmQueueDC3 = new LinkedList<>();
    public Queue<Cloudlet> cloudletListDC3 = new LinkedList<>();
    
    private static final double TIME_TO_TERMINATE_SIMULATION=2.5;
    private static final double MIN_TIME_BETWEEN_EVENTS = 0.01;
    String DummyDatacenter;
    public List<Host> DummyHostList = new ArrayList<>();
    private int HighcpuMedium=1;
    private int Extralarge=2;
    private int Small=3;
    private int Micro=4;
    private int Day1=1;
    private int Day2=2;
    /**
     * Defines the maximum number of cloudlets to be created
     * from the given workload file.
     * The value -1 indicates that every job inside the workload file
     * will be created as one cloudlet.
     */
    public int maximumNumberOfCloudletsToCreateFromTheWorkloadFileDC1 =8; //trial
    
    /**
     *
     * @param args
     */
    public static void main(String[] args) 
    {
        new ModelConstruction();
    }
  
    public ModelConstruction()
    {  	      	
    	  System.out.println("Starting " + getClass().getSimpleName());
    	  simulation = new CloudSim(); //MIN_TIME_BETWEEN_EVENTS for DC1
	  
    	  	  DatacenterPlanetLab();
    	//  	DatacenterBatchJobs();
    	  		DatacenterDAAS();
       	 
    }
    private VmAllocationPolicyMigrationStaticThreshold allocationPolicy;
    DatacenterBroker BrokerDC1;
    DatacenterSimple DC1;
    private void DatacenterPlanetLab() 
    {  // Planetlab expeimental setup:start
 double TIME_TO_TERMINATE_SIMULATION_Planetlab=(2*24*60*60)-1200;//172800
    	simulation.terminateAt(TIME_TO_TERMINATE_SIMULATION_Planetlab);
 	     
	    CreateDatacenterDC1 createDC1 = new CreateDatacenterDC1();
	    for(int i=0;i<8;i++)
	    {
		  createDC1.createHostsDC1(1,1);//"HP Proliant G4"); //400
		  createDC1.createHostsDC1(1,2);//"HP Proliant G5");
	    }
	    DatacenterBroker brokerDC1 = new DatacenterBrokerSimple(simulation);
	    this.BrokerDC1=brokerDC1;
	    DatacenterSimple dc1 = createDC1.creatingSimpleDatacenterDC1(simulation,brokerDC1);
	    this.DC1=dc1;
	    HeuristicAlgorithms setPolicyDC1 = new HeuristicAlgorithms();
	    setPolicyDC1.setAllocationPolicy (dc1,1);
	  //  DatacenterBroker brokerDC1 = new DatacenterBrokerSimple(simulation);
	    InitializationDC1 createCloudletandVm =new InitializationDC1();
	//    createCloudletandVm.createOneVmAndCloudlet(1, HighcpuMedium, brokerDC1);//100
	  //  createCloudletandVm.createOneVmAndCloudlet(1, Extralarge, brokerDC1);//100
	    //createCloudletandVm.createOneVmAndCloudlet(3, Small, brokerDC1);//300
	   // createCloudletandVm.createOneVmAndCloudlet(3, Micro, brokerDC1);//300
	//    vmListDC1 = createCloudletandVm.getVmListDC1();  
	    
//	    this.allocationPolicy=dc1.getVmAllocationPolicy();
	    hostsListDC1.addAll(createDC1.getHostsListDC1());
	    simulation.addOnClockTickListener(this::DestroyAllRunningVmsAndCreateNew);
	    simulation.start();
	    SimulationResults printresults = new SimulationResults(); 
	//    hostsListDC1.addAll(createDC1.getHostsListDC1());
	    printresults.printHostCpuUtilizationAndPowerConsumption(simulation, brokerDC1, hostsListDC1);      
    }

    
    private void DestroyAllRunningVmsAndCreateNew(EventInfo info) {
    	int time=(int)info.getTime();
    	if(time == (86100-600)) {//(24*60*60)-600)
    		
    		for(Host host : DC1.getHostList()) {
    			host.destroyAllVms();
    		host.setShutdownTime(86100-600);
    			}
    		  CreateDatacenterDC1 createDC1 = new CreateDatacenterDC1();
    		    for(int i=0;i<8;i++)
    		    {
    			  createDC1.createHostsDC1(1,1);//"HP Proliant G4"); //400
    			  createDC1.createHostsDC1(1,2);//"HP Proliant G5");
    		    }
/*		    InitializationDC1 createCloudletandVm2ndDay =new InitializationDC1();
		    createCloudletandVm2ndDay.createOneVmAndCloudlet(1, HighcpuMedium, BrokerDC1,Day2);//100
		    createCloudletandVm2ndDay.createOneVmAndCloudlet(1, Extralarge, BrokerDC1,Day2);//100
		    createCloudletandVm2ndDay.createOneVmAndCloudlet(3, Small, BrokerDC1,Day2);//300
		    createCloudletandVm2ndDay.createOneVmAndCloudlet(3, Micro, BrokerDC1,Day2);//300
		    hostsListDC1.addAll(createDC1.getHostsListDC1());*/
		    SimulationResults printresults = new SimulationResults();
			    printresults.printHostCpuUtilizationAndPowerConsumption(simulation, BrokerDC1, hostsListDC1);   
    	}
    }
  
    private void DatacenterBatchJobs() 
    {
    	long TIME_TO_TERMINATE_SIMULATION_BatchJobs=1000;
    	simulation.terminateAt(TIME_TO_TERMINATE_SIMULATION_BatchJobs);
	    CreateDatacenterDC2 createDC2 = new CreateDatacenterDC2();
	    createDC2.createHostsDC2(320); //320
	    DatacenterSimple dc2 = createDC2.creatingSimpleDatacenterDC2(simulation);
	    HeuristicAlgorithms setPolicyDC2 = new HeuristicAlgorithms();
	    setPolicyDC2.setAllocationPolicy (dc2,1);
	    DatacenterBroker brokerDC2 = new DatacenterBrokerSimple(simulation);
	    brokerDC2.setVmDestructionDelay(0.0);
	   //     brokerDC1.setDatacenterSupplier(createDC1);
	    InitializationDC2 createCloudletsandVmDC2 =new InitializationDC2();
	    //brokerDC2.setDatacenterSupplier(createDC);
	    //brokerDC2.setVmDestructionDelayFunction(vm->10.0);
//	    createCloudletsandVmDC2.CreateCloudletAndVmForApplication1DC2FromWorkloadFile(10,brokerDC2,simulation);
//	    createCloudletsandVmDC2.CreateCloudletAndVmForApplication2DC2FromWorkloadFile(10,brokerDC2,simulation);
//	    createCloudletsandVmDC2.CreateCloudletAndVmForApplication3DC2FromWorkloadFile(10,brokerDC2,simulation);
	    createCloudletsandVmDC2.CreateCloudletAndVmForApplication4DC2FromWorkloadFile(10,brokerDC2,simulation);
	        
	    CloudletQueueDC2.addAll(createCloudletsandVmDC2.getCloudletQueueDC2());
	        	
	 //   createCloudletsandVmDC2.createVmDC2(CloudletQueueDC2.size());
	    
	  /*  vmListDC2 = createCloudletsandVmDC2.getVmListDC2();
	    
	    brokerDC2.submitVmList(vmListDC2);
	 */      //   brokerDC2.submitCloudletList(CloudletQueueDC2);
	          	
	    simulation.start();
	    SimulationResults printresults = new SimulationResults(); 
	    this.hostsListDC2.addAll(createDC2.getHostsListDC2());
	    printresults.printHostCpuUtilizationAndPowerConsumption(simulation, brokerDC2, hostsListDC2);
    }
    
    
    private void DatacenterDAAS()
    {
    	long TIME_TO_TERMINATE_SIMULATION_DAAS=2*24*60*60;
    	simulation.terminateAt(TIME_TO_TERMINATE_SIMULATION_DAAS);
   	  	@SuppressWarnings("unused")
   	  	CreateDatacenterDC3 createDC3 = new CreateDatacenterDC3();	
   	  	for(int i=0;i<8;i++)
   	  	{
   	  		createDC3.createHostsDC3(1);	
   	  	}
   	 DatacenterBroker brokerDC3 = new DatacenterBrokerSimple(simulation);
   	  	hostListDC3.addAll(createDC3.getHostsListDC3());
   	  	DatacenterSimple dc3 = createDC3.creatingSimpleDatacenterDC3(simulation,hostListDC3,brokerDC3); 
   	  	HeuristicAlgorithms setPolicyDC3 = new HeuristicAlgorithms();
        setPolicyDC3.setAllocationPolicy(dc3,1);
         
    //    DatacenterBroker brokerDC3 = new DatacenterBrokerSimple(simulation);
        brokerDC3.setVmDestructionDelay(0.0);
        InitializationDC3 createCloudletsandVmDC3 =new InitializationDC3();
        createCloudletsandVmDC3.setDc(dc3);   	 
        createCloudletsandVmDC3.setDCbroker(brokerDC3);
  /*      Host Dummyhost= createDC3.createHostDummy();
        DummyHostList.add(Dummyhost);
        createCloudletsandVmDC3.setDummyHost(Dummyhost);
        DatacenterSimple dcDummy= createDC3.creatingSimpleDatacenterDC3(simulation,DummyHostList);   
        HeuristicAlgorithms setPolicyDummyDC = new HeuristicAlgorithms(); 
        setPolicyDummyDC.setAllocationPolicy(dcDummy,1);
        DatacenterBroker brokerDummyDC = new DatacenterBrokerSimple(simulation);
        brokerDummyDC.setDatacenterSupplier(createCloudletsandVmDC3.DummydatacenterSupplier());
        createCloudletsandVmDC3.setDummyDCbroker(brokerDummyDC);
        createCloudletsandVmDC3.setDc1(dcDummy);
   */     
         // -1 is given as a initial trigger value for setting Vmid based on vm list size.
        createCloudletsandVmDC3.createVmDC3(30, 1, brokerDC3, -1,simulation);//30
        createCloudletsandVmDC3.createVmDC3(30, 2, brokerDC3, -1,simulation);//30
        createCloudletsandVmDC3.createVmDC3(90, 3, brokerDC3, -1,simulation);//90
        createCloudletsandVmDC3.createVmDC3(40, 4, brokerDC3, -1,simulation);//40
        createCloudletsandVmDC3.createVmDC3(50, 5, brokerDC3, -1,simulation);//50
        
        vmQueueDC3.addAll(createCloudletsandVmDC3.getvmQueueDC3());
        simulation.start();
      
        SimulationResults printresults = new SimulationResults(); 
        printresults.printHostCpuUtilizationAndPowerConsumption(simulation, brokerDC3, hostListDC3);
    }
}