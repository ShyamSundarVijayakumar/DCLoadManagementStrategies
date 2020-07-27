/**
 * 
 */
package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyFirstFit;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudsimplus.listeners.EventInfo;

/**
 * @author Shyam Sundar V
 *
 */
public class PlanetLabMain {

    public CloudSim simulation;
	public List<Host> hostsListDC1 = new ArrayList<>();
    private int HighcpuMedium=1;
    private int Extralarge=2;
    private int Small=3;
    private int Micro=4;

    double TIME_TO_TERMINATE_SIMULATION_Planetlab=50000;//(80100-300);//(24*60*60)-600;

    double TIME_TO_TERMINATE_SIMULATION_PlanetlabDay2=(172200-300);//(24*60*60)-600;
    
    public static void main(String[] args) 
    {
        new PlanetLabMain();
    }

	
    public PlanetLabMain() 
    {  
    	System.out.println("Starting " + getClass().getSimpleName());
  	  	simulation = new CloudSim();
    //	simulation.terminateAt(TIME_TO_TERMINATE_SIMULATION_Planetlab);
 	     
	    CreateDatacenterDC1 createDC1 = new CreateDatacenterDC1();
	    for(int i=0;i<4;i++)
	    {
		  
		  createDC1.createHostsDC1(1,2);//"HP Proliant G5");
		  createDC1.createHostsDC1(1,1);//"HP Proliant G4"); //400
		  
	    }
	    DatacenterBroker brokerDC1 = new DatacenterBrokerSimple(simulation);
	    DatacenterSimple dc1 = createDC1.creatingSimpleDatacenterDC1(simulation,brokerDC1);
	    this.BrokerDC1= brokerDC1;
	    /*HeuristicAlgorithms setPolicyDC1 = new HeuristicAlgorithms();
	    setPolicyDC1.setAllocationPolicy (dc1,1);*/
	    
	    
	    InitializationDC1 createCloudletandVm = new InitializationDC1();
	    this.CreateCloudletandVm = createCloudletandVm;
	    createCloudletandVm.createOneVmAndCloudlet(1, HighcpuMedium, brokerDC1, 0);//100
	    createCloudletandVm.createOneVmAndCloudlet(2, Extralarge, brokerDC1, 0);//100
	    createCloudletandVm.createOneVmAndCloudlet(2, Small, brokerDC1, 0);//300
	    createCloudletandVm.createOneVmAndCloudlet(5, Micro, brokerDC1, 0);//300
  
	 /*   CreateCloudletandVm.createOneVmAndCloudlet(1, HighcpuMedium, BrokerDC1,80100-300);//100
	    CreateCloudletandVm.createOneVmAndCloudlet(2, Extralarge, BrokerDC1,80100-300);//100
	    CreateCloudletandVm.createOneVmAndCloudlet(2, Small, BrokerDC1,80100-300);//300
	    CreateCloudletandVm.createOneVmAndCloudlet(5, Micro, BrokerDC1,80100-300);//300
*/	    hostsListDC1.addAll(createDC1.getHostsListDC1());
	//    simulation.addOnClockTickListener(this::clockTickListener);
	//    simulation.addOnClockTickListener(this::clockTickListener1);
	    simulation.start();
	    SimulationResults printresults = new SimulationResults(); 
	    printresults.printHostCpuUtilizationAndPowerConsumption(simulation, brokerDC1, hostsListDC1);      
    }
    
    DatacenterBroker BrokerDC1;
    InitializationDC1 CreateCloudletandVm;
	 private void clockTickListener( EventInfo info)
	 {
	      final int time = (int)info.getTime();	 	    
    	  if(time == TIME_TO_TERMINATE_SIMULATION_Planetlab)
	       		{
	    		   System.out.println("time------------------------------------------------------------------------>"+simulation.clock());//for Debugging
	    		//   simulation.terminate();
	    		   
	    		   for(Host host : hostsListDC1) {
	    			   
	    			  List<Host> Vmlist = new ArrayList<>();
	    			  System.out.println("Vmlist before destruction"+host.getVmList());
	    			  host.destroyAllVms();
	    			  System.out.println("Vmlist after destruction"+host.getVmList());
	    			  
	    		   }
	    		   	       		} 
	  }
	 
	 private void clockTickListener1( EventInfo info)
	 {
	      final int time = (int)info.getTime();	 	    
    	  if(time == TIME_TO_TERMINATE_SIMULATION_Planetlab)
	       		{
	    		   System.out.println("time DAy2------------------------------------------------------------------------>"+simulation.clock());//for Debugging
	    		   
	    		   for(Host host : hostsListDC1) {
	    			   
		    			  List<Host> Vmlist = new ArrayList<>();
		    			  System.out.println("Vmlist before destruction"+host.getVmList());
		    			  host.destroyAllVms();
		    			  System.out.println("Vmlist after destruction"+host.getVmList());
		    			  
		    		   }
	    		   
	    		   simulation.terminate();
	       		}
	 }
}
