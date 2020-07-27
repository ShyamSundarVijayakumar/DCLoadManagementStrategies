package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
//import org.apache.commons.math3.distribution.TriangularDistribution;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;
import org.cloudbus.cloudsim.distributions.NormalDistr;
import org.cloudbus.cloudsim.distributions.TriangularDistr;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.resources.Processor;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.ResourceAbstract;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.resources.Storage;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.network.CloudletTaskScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel.Unit;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.autoscaling.VerticalVmScaling;
import org.cloudsimplus.listeners.DatacenterBrokerEventInfo;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.listeners.VmHostEventInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Object;
import java.util.Collections;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

/**
 * This class is dedicated for initializing models by creating different types of {@link Host}, {@link Vm} and {@link Cloudlet}
 * 
 * @param vmListDC3
 * @param hostListDC3
 * @param CloudletQueueDC3
 * 
 * @see InitializationDC3#createHostsDC3(int)
 * @see InitializationDC3#getHostsListDC3()
 * @see InitializationDC3#createVmDC3(int, int, DatacenterBroker, long) 
 * @see InitializationDC3#getVmListDC3() 
 * 
 * @see InitializationDC3#createCloudletsDC3(int, int, Vm, DatacenterBroker, long)
 * @see InitializationDC3#getCloudletQueueDC3() 
 * 
 * @author Shyam Sundar V
 * @since CloudSim Plus 1.0
 */
public class InitializationDC3 
{
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(InitializationDC3.class.getSimpleName());
	Queue<Vm> vmQueueDC3 = new LinkedList<>();
	Queue<Cloudlet> cloudletQueueDC3 = new LinkedList<>();
	List<Vm> FinishedvmListDC3 = new LinkedList<>();
    final Map<Vm, Host> migrationMap = new HashMap<>();
    int OflineTimeForVm=0;
    double PreviousCloudletFinishtime=0;
	Map<Long, Integer> VMIdWithItsOfflineDelayMap = new HashMap<>();
	Map<Integer, Integer> VMTypeWithItsOfflineDelayMap = new HashMap<>();
	CloudSim simulation;
	int VmID;
	private ContinuousDistribution OnlineTime;
	private ContinuousDistribution OflineTime;
	List<Double> SortSubmissionDelay = new LinkedList<>();
	Map<Long, Integer>  VMIdWithItsOfflineDelayMapSorted;
	Map<Integer, Integer> VMTypeWithItsOfflineDelayMapSorted ;
	DatacenterBroker DummyDCbroker;
	DatacenterBroker brokerDC3;
	Host dummyHost;
	DatacenterSimple dc3;
	DatacenterSimple dcDummy;
	
	public static void main(String[] args) 
	{
		new InitializationDC3();
	}
	
	/**	This method createVmAndCloudletDC3 is used to create virtual machines of the pre defined types also cloudlets
	 *	creation method is assigned to this method.
	 *	
	 *@param numberVm The number of virtual machines that should be created
	 *@param  type The pre-defined type of virtual machine that should be created
	 *@param broker The broker with which the cloudlets and virtual machines are assigned to.
	 *@param OflineTimeDelay The offline time delay for cloudlet creation
	 */

	public void createVmDC3(int numberVm, int type,DatacenterBroker broker,long VMID,CloudSim simulation) 
	{
	    	final long   mips = 1000, storage = 100, bw = 100;//(no data in paper)
	    	int    ram = 0;
	    	long   pesNumber = 0;
	    	this.simulation=simulation;
	    	
	    	switch (type)
	    	{ 
	    	case 1://Research assistant VMs
	    			ram = 4096; // vm memory (MEGABYTE) 4
	    			pesNumber = 1; // number of CPU cores(no data for pesnumber in paper)
	    			
	    			for (int i = 0; i < numberVm; i++)
	    			{
	    				if(VMID==-1) { VmID = vmQueueDC3.size(); }
	    				if(VMID !=-1) { VmID=(int) VMID; }
	    					
	    				Vm vm =new VmSimple(VmID, mips, pesNumber)
	    	                    .setRam(ram).setBw(bw).setSize(storage).setDescription("1");
	    	        	vm.getUtilizationHistory().enable(); // Remove this line for Complex models
	    	        	vmQueueDC3.add(vm);
	    	        	broker.submitVm(vm);
	    	        	createCloudletsDC3(1,1,vm,broker);
	    	        	
	    			}
	    			break;
	    		
	    		case 2://Researcher VMs
	     	       	ram = 8192; // vm memory (MEGABYTE) 8
	     	       	pesNumber = 1; // number of CPU cores
	     	       
	     	       	for (int i = 0; i < numberVm; i++)
	     	       	{
	     	       		if(VMID==-1) { VmID = vmQueueDC3.size(); }
	     	       		if(VMID !=-1) { VmID=(int) VMID; }
					
	     	       		Vm vm = new VmSimple(VmID, mips, pesNumber)
	     	       				.setRam(ram).setBw(bw).setSize(storage).setDescription("2");
	     	       		vm.getUtilizationHistory().enable();
	     	       		vmQueueDC3.add(vm);
	     	       		broker.submitVm(vm);
	     	       		createCloudletsDC3(1,2,vm,broker);
	     	       	}
	     	       	break;
	    		
	    		case 3://SAP system access 1
	     	       	ram = 10240; // vm memory (MEGABYTE) 10
	     	       	pesNumber = 1; // number of CPU cores
	     	       	for (int i = 0; i < numberVm; i++)
	     	       	{
	     	       		if(VMID==-1) { VmID = vmQueueDC3.size(); }
	     	       		if(VMID !=-1) { VmID=(int) VMID; }
					
	     	       		Vm vm =new VmSimple(VmID, mips, pesNumber)
	     	       				.setRam(ram).setBw(bw).setSize(storage).setDescription("3");
	     	       		vm.getUtilizationHistory().enable(); 
	     	       		vmQueueDC3.add(vm);
	     	       		broker.submitVm(vm);
	     	       		createCloudletsDC3(1,3,vm,broker);
	     	       	}
	     	        break;
	    		
	    		case 4://SAP system access 2
	    			ram = 12288; // vm memory (MEGABYTE) 12
	    			pesNumber = 1; // number of CPU cores
	    			for (int i = 0; i < numberVm; i++)
	    	        {
	    				if(VMID==-1) {	VmID = vmQueueDC3.size(); }
	    				if(VMID !=-1) {	VmID=(int) VMID; }
	    				
	    	        	Vm vm =new VmSimple(VmID, mips, pesNumber)
	    	                    .setRam(ram).setBw(bw).setSize(storage).setDescription("4");
	    	        	vm.getUtilizationHistory().enable();
	    	        	vmQueueDC3.add(vm);
	    	        	broker.submitVm(vm);
	    	        	createCloudletsDC3(1,4,vm,broker);	    	        	
	    	        }
	    			break;
	     		
	    		case 5://SAP system access 3
	     	       	ram = 14336; // vm memory (MEGABYTE) 14
	     	       	pesNumber = 1; // number of CPU cores
	     	       	for (int i = 0; i < numberVm; i++)
	     	       	{
	     	       		if(VMID==-1) { VmID = vmQueueDC3.size(); }
	     	       		if(VMID !=-1) { VmID=(int) VMID; }
	     	       		
	     	       		Vm vm = new VmSimple(VmID, mips, pesNumber)
	     	       				.setRam(ram).setBw(bw).setSize(storage).setDescription("5");
	     	       		vm.getUtilizationHistory().enable(); // Remove this line for Complex models
	     	       		vmQueueDC3.add(vm);
    	        		broker.submitVm(vm);
    	        		createCloudletsDC3(1,5,vm,broker);
	     	       	}
	     	       	break;
	    	}
	    }
	 
	/**
	  * Gets the create virtual machines in a list
	  * @return {@link #vmListDC3}
	  * @see #createVmDC3(int, int, DatacenterBroker, long)
	  */
	   
	public Queue<Vm> getvmQueueDC3() 
	{
	    return vmQueueDC3;
	}
	   	 
	 /**
	  * Online_Time: Online time for the virtual machines in seconds is given in this array.
	  * The distribution function picks a value from all the available samples i.e 1 hour to 14 hours.
	  *  
	  */
	 
	 private static final long[] Online_Time = {0, 3600, 7200, 10800, 14400, 18000, 21600,
			   25200, 28800, 32400, 36000, 39600, 43200, 46800,50400};
	 /**
	  * Ofline_Time: Offline time for virtual machines are given in this array.Since the distribution function picks 
	  * a sample only from 14th to 30th, previous values are left zero.
	  * 
	  */
	 
	 private static final long[] Ofline_Time = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			 50400,54000,57600,61200,64800,68400,72000,75600,79200,82800,86400,90000,93600,97200,100800,104400,108000,108000};
	 
	 
	 /**
	   * clockTickListener gets the first offline time of a vm sorted in ascending order(from VmsSortedBasedOnOfflineTime) and gets the type of vm its description.
	   * when the simulation time equals offline (i.e offline time of Vm finished),it creates a vm and removes the vm and its delay time 
	   * from the VmsSortedBasedOnOfflineTime and VmWithItsOfflineDelayMap.So that the next Offline time will be firstOflineTime and the process goes on.
	   *
	   */
	
	 private void clockTickListener( EventInfo info)
	 {
	      final int time = (int)info.getTime();
	      VMTypeWithItsOfflineDelayMapSorted();
	      VMIdWithItsOfflineDelayMapSorted();
	       
	      if(VMIdWithItsOfflineDelayMapSorted.keySet().stream().findFirst().isPresent()==true) 
	       {
	    	  int firstOflineTime = VMIdWithItsOfflineDelayMapSorted.values().stream().findFirst().get();
	    	  int VmType=VMTypeWithItsOfflineDelayMapSorted.keySet().stream().findFirst().get();
	    	  long Vmid=VMIdWithItsOfflineDelayMapSorted.keySet().stream().findFirst().get();
	   // 	  int VMType=VmsSortedBasedOnOfflineTime.values().stream().findFirst().get().keySet().stream().findFirst().get();
	   // 	  long Vmid =VmsSortedBasedOnOfflineTime.values().stream().findFirst().get().values().stream().findFirst().get(); 
	    
	    	  if(time == firstOflineTime)
	       		{
	    		   System.out.println("time"+simulation.clock());//for Debugging
	    			  System.out.println("Vmtype test"+VmType);
	    	    	  System.out.println("Delay test"+firstOflineTime);
	    	    	  System.out.println("Vmid test"+Vmid);
	    	    	  int VMid=(int)Vmid;

	    		//   vmQueueDC3.remove(vm1);
	    //		   System.out.println("VMtype"+vmQueueDC3);//for debugging
	    		  
	    		   createVmDC3(1, VmType, brokerDC3, VMid,simulation);
	    	//	   VmsSortedBasedOnOfflineTime.remove(firstOflineTime,treeMap1.remove(VMType,Vmid));
	    //		   VmWithItsOfflineDelayMap.remove(firstOflineTime,treeMap1.remove(VMType,Vmid));
	    		   
	    		   VMIdWithItsOfflineDelayMapSorted.remove(Vmid, firstOflineTime);
	    		   VMIdWithItsOfflineDelayMap.remove(Vmid, firstOflineTime);
	    		   VMTypeWithItsOfflineDelayMap.remove(VmType, firstOflineTime);
	    		   VMTypeWithItsOfflineDelayMapSorted.remove(VmType, firstOflineTime);
	       		}
	       }
	  }

	 /**
	   * This method SortOflineTimeOfVms gets input from the VmWithItsOfflineDelayMap.
	   * VmWithItsOfflineDelayMap has each vm with its offline delay time mapped.
	   * This method sorts the vms in ascending order based on the offline time and collects the output in  VmsSortedBasedOnOfflineTime.
	   * 
	   *
	   */
//	 Map<Integer, Map<Integer,Long>> VmsSortedBasedOnOfflineTime= new HashMap<>();
	 public void  VMIdWithItsOfflineDelayMapSorted() 
	 {
		/* VmsSortedBasedOnOfflineTime = VmWithItsOfflineDelayMap.entrySet().stream().
									   sorted(Map.Entry.comparingByKey()).
									   collect(toMap(Map.Entry::getKey,Map.Entry::getValue, (e1, e2) -> e2,LinkedHashMap::new));
	*/	 VMIdWithItsOfflineDelayMapSorted = VMIdWithItsOfflineDelayMap.entrySet().stream().
				   sorted(Map.Entry.comparingByValue()).
				   collect(toMap(Map.Entry::getKey,Map.Entry::getValue, (e1, e2) -> e2,LinkedHashMap::new));
		
	 }	
	 
	 public void VMTypeWithItsOfflineDelayMapSorted() 
	 {

		 VMTypeWithItsOfflineDelayMapSorted = VMTypeWithItsOfflineDelayMap.entrySet().stream().
				   sorted(Map.Entry.comparingByValue()).
				   collect(toMap(Map.Entry::getKey,Map.Entry::getValue, (e1, e2) -> e2,LinkedHashMap::new));
		
	 }	
	 
	 /**
	   * Create Cloudlets for data center 3
	   * 
	   * @param numberCloudlets number of cloudlets to be created for data center 3
	   * @param	type of the cloudlet to be created
	   * @param broker for Data center 2
	   *
	   */
	// Map<Integer, Map<Integer,Long>> VmWithItsOfflineDelayMap = new HashMap<>();
	// Map<Integer, Long> treeMap1 = new HashMap<>();
	public void createCloudletsDC3(int numberCloudlets, int type,Vm vm,DatacenterBroker broker) 
	{
        //cloudlet parameters	
		 long fileSize = 300; 
         long outputSize = 300; 
         long length = 0; // Initial declaration of the length
         int pesNumber = 0; // Initial declaration of the pesNumber
         final long mips = 100;
         
	     switch (type)
	   	{
	     case 1:
	     
		 pesNumber = 1; 
		 for (int i = 0; i < numberCloudlets; i++) 
	     {	
			 double UM= 0.30;
        	OnlineTime= new TriangularDistr(1,3,6);//ResearchAssistant VM's
			length=(Online_Time[(int)OnlineTime.sample()]* (int) (mips * 3) ) ;
		 	int LetID = -1;
		 	Cloudlet cloudlet = new CloudletSimple(LetID++, length, pesNumber)
		 						.setFileSize(fileSize)
		 						.setOutputSize(outputSize).setVm(vm)
		 						.setUtilizationModelCpu(new UtilizationModelDynamic(0.3))
		 						.setUtilizationModelRam(new UtilizationModelDynamic(0.85));
		 	cloudlet.setJobId(1);
		 	broker.bindCloudletToVm(cloudlet, vm);
		 	cloudletQueueDC3.add(cloudlet);
		 	broker.submitCloudlet(cloudletQueueDC3.poll());	
		 	
		 	cloudlet.addOnStartListener( info ->{
		 		Vm VM=info.getCloudlet().getVm();
		 		vmQueueDC3.remove(VM);
		 	});
		 	
		 	cloudlet.addOnFinishListener(info -> {
		 		System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n", info.getTime(), info.getCloudlet());
		 		
		 		OflineTime= new TriangularDistr(22,24,30);
		 		long OflineTimeDelayResearchAssistant = Ofline_Time[(int) OflineTime.sample()];
		 		this.PreviousCloudletFinishtime=info.getTime();
		 		OflineTimeForVm=(int) (PreviousCloudletFinishtime + OflineTimeDelayResearchAssistant);
	
		 		Vm vm1=cloudlet.getVm();
		 		FinishedvmListDC3.add(vm1);
		 		long Vmid=cloudlet.getVm().getId();
		 		String Type=vm1.getDescription();
	    	    int VmType=Integer.parseInt(Type);
	 		
		 	//	vmQueueDC3.add(vm1);
		 	//	treeMap1.put(VmType, Vmid);
		//		DummyDCbroker.submitVm(vm1);							
	//	 		VmWithItsOfflineDelayMap.put(OflineTimeForVm,treeMap1.put(VmType, Vmid));//treeMap1);//.put(Vmid,VmType, OflineTimeForVm);
    	    
		 		VMIdWithItsOfflineDelayMap.put(Vmid, OflineTimeForVm);
		 		VMTypeWithItsOfflineDelayMap.put(VmType, OflineTimeForVm);
		 		cloudlet.setStatus(Cloudlet.Status.SUCCESS);
	    	    vm.getHost().destroyVm(vm);
		 		
		 		simulation.addOnClockTickListener(this::clockTickListener);
		 	});
		 }
		  break;
		  
		  case 2: 
		  pesNumber = 1;
		  for (int i = 0; i < numberCloudlets; i++) 
		     {	
			 OnlineTime= new TriangularDistr(6,8,14); //Researcher VMs 
			 length=Online_Time[(int)OnlineTime.sample()]*(int) (mips * 3) ;
			 int LetID = -1;//cloudletQueueDC3.size();
			 Cloudlet cloudlet = new CloudletSimple(LetID, length, pesNumber)
			 		                            .setFileSize(fileSize)
			 		                            .setOutputSize(outputSize).setVm(vm)
			 		                            .setUtilizationModelCpu(new UtilizationModelDynamic(0.3))
			 		                            .setUtilizationModelRam(new UtilizationModelDynamic(0.85));
			 cloudlet.setJobId(2);
			 broker.bindCloudletToVm(cloudlet, vm);			 									
			 cloudletQueueDC3.add(cloudlet);
			 broker.submitCloudlet(cloudletQueueDC3.poll());
			 
			 	cloudlet.addOnStartListener( info ->
			 	{
			 		Vm VM=info.getCloudlet().getVm();
			 		vmQueueDC3.remove(VM);
			 	});

			 	cloudlet.addOnFinishListener(info -> {
				 System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n", info.getTime(), info.getCloudlet());
				
				 OflineTime= new TriangularDistr(14,16,18);
			 	 long OflineTimeDelayResearcher = Ofline_Time[(int) OflineTime.sample()];
				 this.PreviousCloudletFinishtime=info.getTime();
			 	 OflineTimeForVm=(int) (PreviousCloudletFinishtime + OflineTimeDelayResearcher);
			 	 
			 	 Vm vm1=cloudlet.getVm();
			 	 FinishedvmListDC3.add(vm1);
			// 	 vmQueueDC3.add(vm1);
			// 	 DummyDCbroker.submitVm(vm1);							
	//		 	 VmWithItsOfflineDelayMap.put(vm1, OflineTimeForVm);
			 	 String Type=vm1.getDescription();
			 	 long Vmid=cloudlet.getVm().getId();
			 	 int VmType=Integer.parseInt(Type);
			 	 VMIdWithItsOfflineDelayMap.put(Vmid, OflineTimeForVm);
			 	 VMTypeWithItsOfflineDelayMap.put(VmType, OflineTimeForVm);
			 	 cloudlet.setStatus(Cloudlet.Status.SUCCESS);
			 	 vm.getHost().destroyVm(vm);
				 simulation.addOnClockTickListener(this::clockTickListener);
			 });
			 }
		 break;
		  
		  case 3:
		  pesNumber = 1;
		  for (int i = 0; i < numberCloudlets; i++) 
		     {	
			  OnlineTime= new TriangularDistr(2,5,8);//SAP system access 1 
			  length=Online_Time[(int)OnlineTime.sample()]*mips;
			  int LetID = -1;
			  Cloudlet cloudlet = new CloudletSimple(LetID++, length, pesNumber)
	                     			.setFileSize(fileSize)
	                     			.setOutputSize(outputSize).setVm(vm)
	                     			.setUtilizationModelCpu(new UtilizationModelDynamic(0.30))
	                     			.setUtilizationModelRam(new UtilizationModelDynamic(0.95));
			  cloudlet.setJobId(3);
			  broker.bindCloudletToVm(cloudlet, vm);						 									
			  cloudletQueueDC3.add(cloudlet);
			  broker.submitCloudlet(cloudletQueueDC3.poll());
				
				cloudlet.addOnStartListener( info ->
			 	{
			 		Vm VM=info.getCloudlet().getVm();
			 		vmQueueDC3.remove(VM);
			 	});

			cloudlet.addOnFinishListener(info ->
			{
				System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n", info.getTime(), info.getCloudlet());
				
				this.PreviousCloudletFinishtime=info.getTime();
		 		OflineTime= new TriangularDistr(16,19,22);
		 		long OflineTimeDelaySAPsystemAccess1 = Ofline_Time[(int) OflineTime.sample()];
		 		OflineTimeForVm=(int) (PreviousCloudletFinishtime + OflineTimeDelaySAPsystemAccess1);
		 		
		 		Vm vm1=cloudlet.getVm();
		 		FinishedvmListDC3.add(vm1);
			// 	vmQueueDC3.add(vm1);
			 	String Type=vm1.getDescription();
			 	long Vmid=cloudlet.getVm().getId();
			 	int VmType=Integer.parseInt(Type);
			 	VMIdWithItsOfflineDelayMap.put(Vmid, OflineTimeForVm);
			 	VMTypeWithItsOfflineDelayMap.put(VmType, OflineTimeForVm);
			 	cloudlet.setStatus(Cloudlet.Status.SUCCESS);
			 	vm.getHost().destroyVm(vm);
				simulation.addOnClockTickListener(this::clockTickListener);	 	   
			});
		}
		 break;
		 case 4:
		 pesNumber = 1;
		 for (int i = 0; i < numberCloudlets; i++) 
	     {	
			 OnlineTime= new TriangularDistr(2,5,8);//SAP system access 2 
			 length=Online_Time[(int)OnlineTime.sample()]*mips;
			 int LetID = -1;
			 Cloudlet cloudlet = new CloudletSimple(LetID++, length, pesNumber)
	                     			.setFileSize(fileSize)
	                     			.setOutputSize(outputSize).setVm(vm)
	                     			.setUtilizationModelCpu(new UtilizationModelDynamic(0.35))
	                     			.setUtilizationModelRam(new UtilizationModelDynamic(0.90));
			cloudlet.setJobId(4);
			broker.bindCloudletToVm(cloudlet, vm);
 			cloudletQueueDC3.add(cloudlet);
			broker.submitCloudlet(cloudletQueueDC3.poll());
				
			cloudlet.addOnStartListener( info ->
		 	{
		 		Vm VM=info.getCloudlet().getVm();
		 		vmQueueDC3.remove(VM);
		 	});

		 	cloudlet.addOnFinishListener(info -> 
		 	{
		 		System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n", info.getTime(), info.getCloudlet());
		 		
		 		OflineTime= new TriangularDistr(16,19,22);
		 		this.PreviousCloudletFinishtime=info.getTime();
		 		long OflineTimeDelaySAPsystemAccess2 = Ofline_Time[(int) OflineTime.sample()];
		 		OflineTimeForVm=(int) (PreviousCloudletFinishtime + OflineTimeDelaySAPsystemAccess2);
		 		
		 		Vm vm1=cloudlet.getVm();
		 		FinishedvmListDC3.add(vm1);
			 	String Type=vm1.getDescription();
			 	long Vmid=cloudlet.getVm().getId();
			 	int VmType=Integer.parseInt(Type);
			 	VMIdWithItsOfflineDelayMap.put(Vmid, OflineTimeForVm);
			 	VMTypeWithItsOfflineDelayMap.put(VmType, OflineTimeForVm);
			 	cloudlet.setStatus(Cloudlet.Status.SUCCESS);
			 	vm.getHost().destroyVm(vm);
				simulation.addOnClockTickListener(this::clockTickListener);
		 	});
		  }
		 break;
		 case 5:
		 pesNumber = 1;
		 for (int i = 0; i < numberCloudlets; i++) 
	     {	
			 OnlineTime= new TriangularDistr(2,5,8);//SAP system access 3
			 length=Online_Time[(int)OnlineTime.sample()]*mips;
			 int LetID = -1;
			 Cloudlet cloudlet = new CloudletSimple(LetID++, length, pesNumber)
                     			.setFileSize(fileSize)
                     			.setOutputSize(outputSize).setVm(vm)
                     			.setUtilizationModelCpu(new UtilizationModelDynamic(0.30))
                     			.setUtilizationModelRam(new UtilizationModelDynamic(0.55));
			cloudlet.setJobId(4);
			broker.bindCloudletToVm(cloudlet, vm);
			cloudletQueueDC3.add(cloudlet);
			broker.submitCloudlet(cloudletQueueDC3.poll());
		
			cloudlet.addOnStartListener( info ->
		 	{
		 		Vm VM=info.getCloudlet().getVm();
		 		vmQueueDC3.remove(VM);
		 	});

			  cloudlet.addOnFinishListener(info -> 
			  {
				System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n", info.getTime(), info.getCloudlet());
			
				OflineTime= new TriangularDistr(16,19,22);
				long OflineTimeDelaySAPsystemAccess3 = Ofline_Time[(int) OflineTime.sample()];
				this.PreviousCloudletFinishtime=info.getTime();
				OflineTimeForVm=(int) (PreviousCloudletFinishtime + OflineTimeDelaySAPsystemAccess3);
		 		
				Vm vm1=cloudlet.getVm();
				FinishedvmListDC3.add(vm1);
				String Type=vm1.getDescription();
			 	long Vmid=cloudlet.getVm().getId();
			 	int VmType=Integer.parseInt(Type);
			 	VMIdWithItsOfflineDelayMap.put(Vmid, OflineTimeForVm);
			 	VMTypeWithItsOfflineDelayMap.put(VmType, OflineTimeForVm);
			 	cloudlet.setStatus(Cloudlet.Status.SUCCESS);
			 	vm.getHost().destroyVm(vm);
				simulation.addOnClockTickListener(this::clockTickListener);
			  });
		 }							
		 break;
	  }
   }

	/**
	  * Gets the created Cloudlets in a Queue
	  * @return {@link #cloudletQueueDC3}
	  * @see #createCloudletsDC3(int, int, Vm, DatacenterBroker, long)
	  */ 
	public Queue<Cloudlet> getCloudletQueueDC3() 
	{
	   	return cloudletQueueDC3;
	}
	
	
 	public DatacenterBroker getDCbroker() 
 	{
 		return brokerDC3;
 	}

 	public void setDCbroker(DatacenterBroker dCbroker) 
 	{
 		brokerDC3 = dCbroker;
 	}

	/**
	 * @return the dummyDCbroker
	 */

	public DatacenterBroker getDummyDCbroker() 
	{
		return this.DummyDCbroker;
	}

	/**
	 * @param dummyDCbroker the dummyDCbroker to set
	 */
	public void setDummyDCbroker(DatacenterBroker dummyDCbroker)
	{
		this.DummyDCbroker = dummyDCbroker;
	}

	/**
	 * @return the dummyHost
	 */
	
	public Host getDummyHost() 
	{
		return dummyHost;
	}

	/**
	 * @param dummyHost the dummyHost to set
	 */
	public void setDummyHost(Host dummyHost) 
	{
		this.dummyHost = dummyHost;
	}

	
	public DatacenterSimple getDc()
	{
		return dc3;
	}
	
	public void setDc(DatacenterSimple dc) 
	{
		this.dc3 = dc;
	}
		
	public Supplier<Datacenter> datacenterSupplier()
	{
		Supplier<Datacenter> SupplierDatacenter=this::getDc;
		return SupplierDatacenter;
	}
	    
	
	public DatacenterSimple getDc1()
	{
		return dcDummy;
	}
		
	public void setDc1(DatacenterSimple dc)
	{
		this.dcDummy = dc;
	}

	Supplier<Datacenter> DummydatacenterSupplier()
	{
	    Supplier<Datacenter> SupplierDatacenter=this::getDc1;
	    return SupplierDatacenter;
	}	
   
}
