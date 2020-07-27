package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.EventInfo;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class DesktopAsAService {

	public CloudSim simulation;
    public List<Host> hostListDC3 = new LinkedList<>();
    public Queue<Vm> vmQueueDC3 = new LinkedList<>();
    public Queue<Cloudlet> cloudletListDC3 = new LinkedList<>();
    int VmType1=1;
    int VmType2=2;
    int VmType3=3;
    int VmType4=4;
    int VmType5=5;
	
    public static void main(String[] args) 
	{
		new DesktopAsAService();
	}
    

	
	public DesktopAsAService()
    {
		System.out.println("Starting " + getClass().getSimpleName());
		simulation = new CloudSim();
		long TIME_TO_TERMINATE_SIMULATION_DAAS= 2*24*60*60;
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
   	  /*	HeuristicAlgorithms setPolicyDC3 = new HeuristicAlgorithms();
        setPolicyDC3.setAllocationPolicy(dc3,1);*/
    //    brokerDC3.setVmDestructionDelay(0.0);
        InitializationDC3 createCloudletsandVmDC3 =new InitializationDC3();
        createCloudletsandVmDC3.setDc(dc3);   	 
        createCloudletsandVmDC3.setDCbroker(brokerDC3);
        
  /* Dummy datacenter code ignored   
   *   Host Dummyhost= createDC3.createHostDummy();
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
        createCloudletsandVmDC3.createVmDC3(20, VmType1, brokerDC3, -1,simulation);//30
        createCloudletsandVmDC3.createVmDC3(3, VmType2, brokerDC3, -1,simulation);//30
        createCloudletsandVmDC3.createVmDC3(90, VmType3, brokerDC3, -1,simulation);//90
        createCloudletsandVmDC3.createVmDC3(4, VmType4, brokerDC3, -1,simulation);//40
        createCloudletsandVmDC3.createVmDC3(5, VmType5, brokerDC3, -1,simulation);//50
        
        vmQueueDC3.addAll(createCloudletsandVmDC3.getvmQueueDC3());
      //  simulation.addOnClockTickListener(this::clockTickListener);
        simulation.start();
      
        SimulationResults printresults = new SimulationResults(); 
        printresults.printHostCpuUtilizationAndPowerConsumption(simulation, brokerDC3, hostListDC3);
    }
	
	private void clockTickListener( EventInfo info)
	 {
	      final int time = (int)info.getTime();
	    	  if(time == 1)
	       		{
	    		   System.out.println("time"+simulation.clock());//for Debugging
	    		   
	    		   
	    		    List<Vm> VmListDC3 = new LinkedList<>();
	    		    
	    		    for (Host host : hostListDC3){
	    		    System.out.println("Hostid :"+host.getId());
	    		    System.out.println("host ram utilisation"+host.getRamUtilization());
	    		    System.out.println("Host allocated ram"+host.getRam().getAllocatedResource());
	    		    for(Vm vm : VmListDC3) {
	    		    	
	    		    	System.out.println("--------------------------------------->"+ vm.getRam().getCapacity());
	    		    	System.out.println("--------------------------------------->"+ vm.getHostRamUtilization());
	    		    	long hostcapacity=vm.getHost().getRam().getCapacity();
	    		    	System.out.println("host total capacity"+hostcapacity);
	    		    }
	       		}
	    		    
	       		}
	  }

}