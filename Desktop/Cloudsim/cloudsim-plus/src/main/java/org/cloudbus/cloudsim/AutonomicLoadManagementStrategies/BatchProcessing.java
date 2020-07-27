/**
 * 
 */
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

/**
 * @author Shyam Sundar V
 *
 */
public class BatchProcessing 
{
   	public Queue<Cloudlet> CloudletQueueDC2 = new LinkedList<>();
   	public List<Host> hostsListDC2 = new LinkedList<>();
   	public CloudSim simulation;
	  	
    public static void main(String[] args) 
    {
        new BatchProcessing();
    }
	
    public BatchProcessing() 
	{
    	 System.out.println("Starting " + getClass().getSimpleName());
    	 simulation = new CloudSim();
	    	long TIME_TO_TERMINATE_SIMULATION_BatchJobs=1000;
	//    	simulation.terminateAt(TIME_TO_TERMINATE_SIMULATION_BatchJobs);
		    CreateDatacenterDC2 createDC2 = new CreateDatacenterDC2();
		    createDC2.createHostsDC2(3); //320
		    DatacenterSimple dc2 = createDC2.creatingSimpleDatacenterDC2(simulation);
		    HeuristicAlgorithms setPolicyDC2 = new HeuristicAlgorithms();
		    setPolicyDC2.setAllocationPolicy (dc2,1);
		    DatacenterBroker brokerDC2 = new DatacenterBrokerSimple(simulation);
		//    brokerDC2.setVmDestructionDelay(0.0);
		   //     brokerDC1.setDatacenterSupplier(createDC1);
		    InitializationDC2BatchProcessing createCloudletsandVmDC2 =new InitializationDC2BatchProcessing();
		    //brokerDC2.setDatacenterSupplier(createDC);
		    //brokerDC2.setVmDestructionDelayFunction(vm->10.0);
		    createCloudletsandVmDC2.CreateCloudletAndVmForApplication1DC2FromWorkloadFile(5,brokerDC2,simulation);
		  /*  createCloudletsandVmDC2.CreateCloudletAndVmForApplication2DC2FromWorkloadFile(40,brokerDC2,simulation);
		    createCloudletsandVmDC2.CreateCloudletAndVmForApplication3DC2FromWorkloadFile(40,brokerDC2,simulation);
		*/   /* createCloudletsandVmDC2.CreateCloudletAndVmForApplication4DC2FromWorkloadFile(400,brokerDC2,simulation);
		    createCloudletsandVmDC2.CreateCloudletAndVmForApplication5DC2FromWorkloadFile(400,brokerDC2,simulation);
		    createCloudletsandVmDC2.CreateCloudletAndVmForApplication6DC2FromWorkloadFile(400,brokerDC2,simulation);
		    createCloudletsandVmDC2.CreateCloudletAndVmForApplication7DC2FromWorkloadFile(400,brokerDC2,simulation);
		   */ 
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
}
