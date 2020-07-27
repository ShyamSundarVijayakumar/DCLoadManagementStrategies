/**
 * 
 */
package hierarchicalarchitecture.globalcontroller;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;
import org.cloudbus.cloudsim.distributions.TriangularDistr;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.listeners.EventInfo;

/**
 * This class is dedicated for initializing virtual machines and cloudlets at appropriate time intervals for desktop as a service.
 * Creating different types of {@link Vm} and it's {@link Cloudlet}
 * 
 * @param vmQueue
 * @param FinishedvmList
 * @param cloudletQueue
 * @param OflineTimeForVm
 * @param PreviousCloudletFinishtime
 * @param VMIdWithItsOfflineDelayMap
 * @param VMTypeWithItsOfflineDelayMap
 * @param simulation
 * @param OnlineTime
 * @param OflineTime
 * @param VMIdWithItsOfflineDelayMapSorted
 * @param VMTypeWithItsOfflineDelayMapSorted
 * @param broker
 * @param VmlistForDispatcher
 * @param um
 * 
 * @see RequestAnalyserAndConfigurationManagerDaaS#createCloudlets(int, int, Vm, DatacenterBroker) 
 * @see RequestAnalyserAndConfigurationManagerDaaS#createVm(int, int, DatacenterBroker, long, CloudSim)
 * @see RequestAnalyserAndConfigurationManagerDaaS#createVmAfterOfflineTime(int, List, int, int, long)
 * @see RequestAnalyserAndConfigurationManagerDaaS#getCloudletQueue()
 * @see RequestAnalyserAndConfigurationManagerDaaS#getDCbroker()
 * @see RequestAnalyserAndConfigurationManagerDaaS#setGlobalControler(GlobalController)
 * @see RequestAnalyserAndConfigurationManagerDaaS#VMIdWithItsOfflineDelayMapSorted()
 * @see RequestAnalyserAndConfigurationManagerDaaS#VMTypeWithItsOfflineDelayMapSorted()
 * 
 * @author Shyam Sundar V
 * @since CloudSim Plus 5.0
 */

public class RequestAnalyserAndConfigurationManagerDaaS {

	private static Queue<Vm> vmQueue = new LinkedList<>();
	private Queue<Cloudlet> cloudletQueue = new LinkedList<>();
	public List<Vm> FinishedvmList = new LinkedList<>();
    int OflineTimeForVm=0;
    double PreviousCloudletFinishtime=0;
	private Map<Long, Integer> VMIdWithItsOfflineDelayMap = new HashMap<>();
//	private Map<Integer, Integer> VMTypeWithItsOfflineDelayMap = new HashMap<>();
	CloudSim simulation;
	private int VmID;
	private ContinuousDistribution OnlineTime;
	private ContinuousDistribution OflineTime;
	private Map<Long, Integer>  VMIdWithItsOfflineDelayMapSorted;
//	private Map<Integer, Integer> VMTypeWithItsOfflineDelayMapSorted;
	private Map<Long, Map<Integer,Integer>> VMIdTypeOfflineDelayMap = new HashMap<Long, Map<Integer,Integer>>();
	DatacenterBroker broker;
	List<Vm> VmlistForDispatcher = new ArrayList<>();
	GlobalController GC;
	private UtilizationModelStochastic um;
	private static final boolean MULTIPLE_UTILIZATION_MODELS = false;
	private static final long SEED = 123456;
	private static final boolean ALWAYS_GENERATE_NEW_RANDOM_UTILIZATION = true;
	
	/**	
	 * This method createVmAndCloudlet is used to create virtual machines of the pre defined types also its approproate cloudlets
	 *	creation is initiated in this method.
	 *	
	 *@param numberVm The number of virtual machines that should be created
	 *@param type The pre-defined type of virtual machine that should be created
	 *@param broker The broker with which the cloudlets and virtual machines are assigned to.
	 *@param OflineTimeDelay The offline time delay for cloudlet creation
	 */
	public void createVm(int numberVm, int type, DatacenterBroker broker,long VMID,CloudSim simulation){
	
		final long mips = 2000, storage = 100, bw = 100;//(no data in paper)
	    int ram = 0;
	    long pesNumber = 0;
	    this.simulation = simulation;
	    this.broker = broker;	
	    	
	    switch (type){
	    
	    case 1://Research assistant VMs

	    	ram = 4096; // vm memory (MEGABYTE) 4
	    	pesNumber = 1; // number of CPU cores(no data for pesnumber in paper)
	    			
	    	for (int i = 0; i < numberVm; i++){
	    			
	    		if(VMID==-1) { VmID = vmQueue.size(); }
	    		if(VMID !=-1) { VmID=(int) VMID; }
	    					
	    		Vm vm =new VmSimple(VmID, mips, pesNumber)
	    				.setRam(ram).setBw(bw).setSize(storage);//.setDescription("1");
	    		vm.getUtilizationHistory().enable(); // Remove this line for Complex models
	    		vm.setDescription("1 Desktop as a service");
	    //		createVerticalRamScalingForVm(vm);
	    		vmQueue.add(vm);
	    		broker.submitVm(vm);
	    		createCloudlets(1,1,vm,broker);
	    	
	    	}
	    	break;
	    		
	    		
	    case 2://Researcher VMs
	    		
	    	ram = 8192; // vm memory (MEGABYTE) 8
	    	pesNumber = 1; // number of CPU cores
	     	       
	    	for (int i = 0; i < numberVm; i++){
	    		
	    		if(VMID==-1) { VmID = vmQueue.size(); }
	    		if(VMID !=-1) { VmID=(int) VMID; }
					
	    		Vm vm = new VmSimple(VmID, mips, pesNumber)
	    				.setRam(ram).setBw(bw).setSize(storage);//.setDescription("2");
	    		vm.getUtilizationHistory().enable();
	    		vm.setDescription("2 Desktop as a service");
	    //		createVerticalRamScalingForVm(vm);
	    		vmQueue.add(vm);
	    		broker.submitVm(vm);
	    		createCloudlets(1,2,vm,broker);
	     	       	
	    	}
	    	break;
	    		
	    			
	    case 3://SAP system access 1
	    	ram = 10240; // vm memory (MEGABYTE) 10
	    	pesNumber = 1; // number of CPU cores
	    	for (int i = 0; i < numberVm; i++){
	    			
	    		if(VMID==-1) { VmID = vmQueue.size(); }
	    		if(VMID !=-1) { VmID=(int) VMID; }
					
	    		Vm vm =new VmSimple(VmID, mips, pesNumber)
	    				.setRam(ram).setBw(bw).setSize(storage);//.setDescription("3");
	    		vm.getUtilizationHistory().enable();
	    		vm.setDescription("3 Desktop as a service");
	    //		createVerticalRamScalingForVm(vm);
	    		vmQueue.add(vm);
	    		broker.submitVm(vm);
	    		createCloudlets(1,3,vm,broker);
	     	       	
	    	}
	    	break;
	    		
	    		
	    case 4://SAP system access 2
	    	ram = 12288; // vm memory (MEGABYTE) 12
	    	pesNumber = 1; // number of CPU cores
	    	for (int i = 0; i < numberVm; i++){
	    			
	    		if(VMID==-1) {	VmID = vmQueue.size(); }
	    		if(VMID !=-1) {	VmID=(int) VMID; }
	    				
	    		Vm vm =new VmSimple(VmID, mips, pesNumber)
	    				.setRam(ram).setBw(bw).setSize(storage);//.setDescription("4");
	    		vm.getUtilizationHistory().enable();
	    		vm.setDescription("4 Desktop as a service");
	    	//	createVerticalRamScalingForVm(vm);
	    		vmQueue.add(vm);
	    		broker.submitVm(vm);
	    		createCloudlets(1,4,vm,broker);	    	        	
	    	        
	    	}
	    	break;
	     		
	    		
	    case 5://SAP system access 3
	    	ram = 14336; // vm memory (MEGABYTE) 14
	    	pesNumber = 1; // number of CPU cores
	    	for (int i = 0; i < numberVm; i++){
	    		
	    		if(VMID==-1) { VmID = vmQueue.size(); }
	    		if(VMID !=-1) { VmID=(int) VMID; }
	     	       		
	    		Vm vm = new VmSimple(VmID, mips, pesNumber)
	    				.setRam(ram).setBw(bw).setSize(storage);//.setDescription("5");
	    		vm.getUtilizationHistory().enable(); // Remove this line for Complex models
	    		vm.setDescription("5 Desktop as a service");
	    	//	createVerticalRamScalingForVm(vm);
	    		vmQueue.add(vm);
	    		broker.submitVm(vm);
	    		createCloudlets(1,5,vm,broker);
	    	}
	    	break;
	    }
	}
	 
    
	/**
	 * Gets the create virtual machines in a queue
	 * @return {@link #vmList}
	 * @see #createVm(int, int, DatacenterBroker, long)
	 */
	public Queue<Vm> getvmQueue(){
		return vmQueue;
	}
	

	/**
	 * @param Gc
	 */
	public void setGlobalControler(GlobalController Gc) {
		GC = Gc;
	}
	
	/**
	 * Online_Time: Online time for the virtual machines in seconds is given in this array.
	 * The distribution function picks a value from the available samples i.e 1 hour to 14 hours.
	 * For example: The value of 1 hour = 60 Seconds * 60 Minutes
	 *  
	 */
	private static final long[] Online_Time = {0, 3600, 7200, 10800, 14400, 18000, 21600,
			   25200, 28800, 32400, 36000, 39600, 43200, 46800,50400};
	
	/**
	 * Ofline_Time: Offline time for virtual machines are given in this array. Since the distribution function picks 
	 * a sample only from 14th to 30th, previous values are left zero. These values are calculated same as the Online_Time.
	 */	 
	 private static final long[] Ofline_Time = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			 50400,54000,57600,61200,64800,68400,72000,75600,79200,82800,86400,90000,93600,97200,100800,104400,108000,108000};
	 
	 /**
	  * clockTickListener gets the first offline time of a vm sorted in ascending order(from VmsSortedBasedOnOfflineTime) and gets the type of vm its description.
	  * when the simulation time equals offline (i.e offline time of Vm finished),it creates a vm and removes the vm and its delay time 
	  * from the VmsSortedBasedOnOfflineTime and VmWithItsOfflineDelayMap. So that the next Offline time will be firstOflineTime and the process goes on.
	  *
	  */
	 private void listenerToInitiateNextDayVms(EventInfo info){
		 
		 final int time = (int)info.getTime();
		 VMIdWithItsOfflineDelayMapSorted();
		 
		 if(VMIdWithItsOfflineDelayMapSorted.keySet().stream().findFirst().isPresent()==true){
			 int firstOflineTime = VMIdWithItsOfflineDelayMapSorted.values().stream().findFirst().get();
			 long Vmid=VMIdWithItsOfflineDelayMapSorted.keySet().stream().findFirst().get();
			 Map<Integer,Integer> TypeAndDelay = VMIdTypeOfflineDelayMap.get(Vmid);
			 createVmAfterOfflineTime(time, VmlistForDispatcher, firstOflineTime, TypeAndDelay.keySet().stream().findFirst().get(), Vmid);
	     }else {
	    	 VmlistForDispatcher = new ArrayList<>();
	     }
		 
		 if(!VmlistForDispatcher.isEmpty() && (VMIdWithItsOfflineDelayMapSorted != null)) {
				/*
				 * Collecting all vm's that are created at a particular time instance and sending them to dispatcher all in one. 
				 * Instead of sending each vm one by one. This may cause GA to run multiple times, when there are more than one vms
				 * to be created at a single simulation time.
				 */
			 if(!(VMIdWithItsOfflineDelayMapSorted.values().stream().findFirst().get() == time)) {
				 GC.dispatcherDynamic(VmlistForDispatcher, time); 
				 VmlistForDispatcher = new ArrayList<>();
			 }
		 }
	 }

	/**
	 * This method is used to initiate the creation of virtual machines after its offline time.
	 * 
	 * @param time
	 * @param Vmlist
	 * @param firstOflineTime
	 * @param VmType
	 * @param Vmid
	 */
	private void createVmAfterOfflineTime(final int time, List<Vm> Vmlist, int firstOflineTime, int VmType, long Vmid) {
		if(time == firstOflineTime){
			 int VMid=(int)Vmid;

			 createVm(1, VmType, broker, VMid,simulation);
			 for(Vm vm : vmQueue) {
				 if(vm.getId() == VMid) {
					 Vm ThisVm =vm;
					 Vmlist.add(ThisVm);
				 }	 
			 }
			 
			 VMIdWithItsOfflineDelayMapSorted.remove(Vmid, firstOflineTime);
			 VMIdWithItsOfflineDelayMap.remove(Vmid, firstOflineTime);
			 VMIdTypeOfflineDelayMap.remove((long) VMid);
		 }
	}

	 /**
	   * This method SortOflineTimeOfVms gets input from the VmWithItsOfflineDelayMap.
	   * VmWithItsOfflineDelayMap has each vm with its offline delay time mapped.
	   * This method sorts the vms in ascending order based on the offline time and collects the output in VmsSortedBasedOnOfflineTime.
	   *
	   */
	private void  VMIdWithItsOfflineDelayMapSorted(){
		/* VmsSortedBasedOnOfflineTime = VmWithItsOfflineDelayMap.entrySet().stream().
									   sorted(Map.Entry.comparingByKey()).
									   collect(toMap(Map.Entry::getKey,Map.Entry::getValue, (e1, e2) -> e2,LinkedHashMap::new));
	*/	 
		VMIdWithItsOfflineDelayMapSorted = VMIdWithItsOfflineDelayMap.entrySet().stream().
				sorted(Map.Entry.comparingByValue()).
				collect(toMap(Map.Entry::getKey,Map.Entry::getValue, (e1, e2) -> e2,LinkedHashMap::new));
		
	 }	
	 
		/*
		 * private void VMTypeWithItsOfflineDelayMapSorted(){
		 * 
		 * VMTypeWithItsOfflineDelayMapSorted =
		 * VMTypeWithItsOfflineDelayMap.entrySet().stream().
		 * sorted(Map.Entry.comparingByValue()).
		 * collect(toMap(Map.Entry::getKey,Map.Entry::getValue, (e1, e2) ->
		 * e2,LinkedHashMap::new)); }
		 */
	 
	 /**
	   * This method is used to initiate the creation of cloudlets for a broker and the cloudlets will be binded with the vm 
	   * 
	   * @param numberCloudlets number of cloudlets to be created for data center 3
	   * @param	type of the cloudlet to be created
	   * @param broker for Data center 2
	   *
	   */
	// Map<Integer, Map<Integer,Long>> VmWithItsOfflineDelayMap = new HashMap<>();
	// Map<Integer, Long> treeMap1 = new HashMap<>();

	private void createCloudlets(int numberCloudlets, int type,Vm vm,DatacenterBroker broker){
		
        //cloudlet parameters	
		long fileSize = 300; 
		long outputSize = 300; 
		long length = 0; // Initial declaration of the length
		int pesNumber = 0; // Initial declaration of the pesNumber
		final long mips = 2000;
	
		this.um = MULTIPLE_UTILIZATION_MODELS || this.um == null ? new UtilizationModelStochastic(SEED) : this.um;
		this.um.setAlwaysGenerateNewRandomUtilization(ALWAYS_GENERATE_NEW_RANDOM_UTILIZATION);
	
		switch (type){
		case 1:
	     
			pesNumber = 1; 
			for(int i = 0; i < numberCloudlets; i++){
				
        	OnlineTime= new TriangularDistr(1,3,6);//ResearchAssistant VM's
			length = Online_Time[(int)OnlineTime.sample()]*(int) (mips * 0.55);
			int LetID = -1;
		      
			Cloudlet cloudlet = new CloudletSimple(LetID++, length, pesNumber)
					.setFileSize(fileSize)
					.setOutputSize(outputSize).setVm(vm)
					.setUtilizationModelCpu(new UtilizationModelDynamic(0.55))// note that changing this affects the online time.Consider also changing cloudlet length equation
					.setUtilizationModelRam(um);//new UtilizationModelDynamic(0.90)
		 		cloudlet.setJobId(1);
		 		broker.bindCloudletToVm(cloudlet, vm);
		 		cloudletQueue.add(cloudlet);
		 		broker.submitCloudlet(cloudletQueue.poll());	
		 	
		 		cloudlet.addOnStartListener( info ->{
		 			Vm VM=info.getCloudlet().getVm();
		 			vmQueue.remove(VM);
		 		});
		 	
		 		cloudlet.addOnFinishListener(info -> {
		 			System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n", info.getTime(), info.getCloudlet());
		 		
		 			OflineTime= new TriangularDistr(22,24,30);
		 			long OflineTimeDelayResearchAssistant = Ofline_Time[(int) OflineTime.sample()];
		 			this.PreviousCloudletFinishtime=info.getTime();
		 			OflineTimeForVm=(int) (PreviousCloudletFinishtime + OflineTimeDelayResearchAssistant);
	
		 			Vm vm1=cloudlet.getVm();
		 			FinishedvmList.add(vm1);
		 			long Vmid=cloudlet.getVm().getId();
		 			String Type=vm1.getDescription();
		 			String FirstElement = Type.substring(0, 1);
		 			int VmType=Integer.parseInt(FirstElement);
	 		
		 	//	vmQueueDC3.add(vm1);
		 	//	treeMap1.put(VmType, Vmid);
		//		DummyDCbroker.submitVm(vm1);							
	//	 		VmWithItsOfflineDelayMap.put(OflineTimeForVm,treeMap1.put(VmType, Vmid));//treeMap1);//.put(Vmid,VmType, OflineTimeForVm);
		 			OflineTimeForVm = udjustOfflineTimeasperSchedulingTime(OflineTimeForVm);
		 			VMIdWithItsOfflineDelayMap.put(Vmid, OflineTimeForVm);
		 			Map<Integer, Integer> VMTypeWithItsOfflineDelayMap = new HashMap<>();
		 			VMTypeWithItsOfflineDelayMap.put(VmType, OflineTimeForVm);
		 			VMIdTypeOfflineDelayMap.put(Vmid,VMTypeWithItsOfflineDelayMap);
		 			cloudlet.setStatus(Cloudlet.Status.SUCCESS);
		 			vm.getHost().destroyVm(vm);
		 		GC.getInitialVmlistDaas().remove(vm);
		 			simulation.addOnClockTickListener(this::listenerToInitiateNextDayVms);
		 		});
			}
			break;
		  
		  case 2: 
			  pesNumber = 1;
			  for(int i = 0; i < numberCloudlets; i++){	
				  OnlineTime= new TriangularDistr(6,8,14); //Researcher VMs 
				  length = Online_Time[(int)OnlineTime.sample()]*(int) (mips * 0.55);
				  int LetID = -1;//cloudletQueueDC3.size();
				  Cloudlet cloudlet = new CloudletSimple(LetID, length, pesNumber)
						  .setFileSize(fileSize)
						  .setOutputSize(outputSize).setVm(vm)
						  .setUtilizationModelCpu(new UtilizationModelDynamic(0.55))
						  .setUtilizationModelRam(um);
				  cloudlet.setJobId(2);
				  broker.bindCloudletToVm(cloudlet, vm);			 									
				  cloudletQueue.add(cloudlet);
				  broker.submitCloudlet(cloudletQueue.poll());
			 
				  cloudlet.addOnStartListener( info ->{
					  Vm VM=info.getCloudlet().getVm();
					  vmQueue.remove(VM);
				  });

				  cloudlet.addOnFinishListener(info -> {
					  System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n", info.getTime(), info.getCloudlet());
				
					  OflineTime= new TriangularDistr(14,16,18);
					  long OflineTimeDelayResearcher = Ofline_Time[(int) OflineTime.sample()];
					  this.PreviousCloudletFinishtime=info.getTime();
					  OflineTimeForVm=(int) (PreviousCloudletFinishtime + OflineTimeDelayResearcher);
			 	 
					  Vm vm1=cloudlet.getVm();
					  FinishedvmList.add(vm1);
			// 	 vmQueueDC3.add(vm1);
			// 	 DummyDCbroker.submitVm(vm1);							
	//		 	 VmWithItsOfflineDelayMap.put(vm1, OflineTimeForVm);
					  String Type=vm1.getDescription();
					  long Vmid=cloudlet.getVm().getId();
					  String FirstElement = Type.substring(0, 1);
					  int VmType=Integer.parseInt(FirstElement);
					  OflineTimeForVm = udjustOfflineTimeasperSchedulingTime(OflineTimeForVm);
					  VMIdWithItsOfflineDelayMap.put(Vmid, OflineTimeForVm);
					  Map<Integer, Integer> VMTypeWithItsOfflineDelayMap = new HashMap<>();
			 			VMTypeWithItsOfflineDelayMap.put(VmType, OflineTimeForVm);
					  VMIdTypeOfflineDelayMap.put(Vmid,VMTypeWithItsOfflineDelayMap);
					  cloudlet.setStatus(Cloudlet.Status.SUCCESS);
					  vm.getHost().destroyVm(vm);
					  GC.getInitialVmlistDaas().remove(vm);
					 /* Host host = vm.getHost();
					  System.out.println("Vmlist ------------>"+ host.getVmList());
					 
					  
					  System.out.println("Vm destroyed"+ host.getVmList());*/
					  simulation.addOnClockTickListener(this::listenerToInitiateNextDayVms);
				  });
			  }
			  break;
		  
		  case 3:
			  pesNumber = 1;
			  for(int i = 0; i < numberCloudlets; i++){	
				  OnlineTime= new TriangularDistr(2,5,8);//SAP system access 1 
				  length=Online_Time[(int)OnlineTime.sample()]*(int) (mips * 0.55);
				  int LetID = -1;
				  Cloudlet cloudlet = new CloudletSimple(LetID++, length, pesNumber)
						  .setFileSize(fileSize)
						  .setOutputSize(outputSize).setVm(vm)
						  .setUtilizationModelCpu(new UtilizationModelDynamic(0.55))
						  .setUtilizationModelRam(um);
				  cloudlet.setJobId(3);
				  broker.bindCloudletToVm(cloudlet, vm);						 									
				  cloudletQueue.add(cloudlet);
				  broker.submitCloudlet(cloudletQueue.poll());
				
				  cloudlet.addOnStartListener( info ->{
					  Vm VM=info.getCloudlet().getVm();
					  vmQueue.remove(VM);
				  });

				  cloudlet.addOnFinishListener(info ->	{
					  System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n", info.getTime(), info.getCloudlet());
				
					  this.PreviousCloudletFinishtime=info.getTime();
					  OflineTime= new TriangularDistr(16,19,22);
					  long OflineTimeDelaySAPsystemAccess1 = Ofline_Time[(int) OflineTime.sample()];
					  OflineTimeForVm=(int) (PreviousCloudletFinishtime + OflineTimeDelaySAPsystemAccess1);
		 		
					  Vm vm1=cloudlet.getVm();
					  FinishedvmList.add(vm1);
			// 	vmQueueDC3.add(vm1);
					  String Type=vm1.getDescription();
					  long Vmid=cloudlet.getVm().getId();
					  String FirstElement = Type.substring(0, 1);
					  int VmType=Integer.parseInt(FirstElement);
					  OflineTimeForVm = udjustOfflineTimeasperSchedulingTime(OflineTimeForVm);
					  VMIdWithItsOfflineDelayMap.put(Vmid, OflineTimeForVm);
					  Map<Integer, Integer> VMTypeWithItsOfflineDelayMap = new HashMap<>();
			 			VMTypeWithItsOfflineDelayMap.put(VmType, OflineTimeForVm);
					  VMIdTypeOfflineDelayMap.put(Vmid,VMTypeWithItsOfflineDelayMap);
					  cloudlet.setStatus(Cloudlet.Status.SUCCESS);
					  vm.getHost().destroyVm(vm);
					  GC.getInitialVmlistDaas().remove(vm);
					  simulation.addOnClockTickListener(this::listenerToInitiateNextDayVms);	 	   
				  });
			  }
			  break;
		 
		  case 4:
			  pesNumber = 1;
			  for(int i = 0; i < numberCloudlets; i++){
				  
				  OnlineTime= new TriangularDistr(2,5,8);//SAP system access 2 
				  length=Online_Time[(int)OnlineTime.sample()]*(int) (mips * 0.55);
				  int LetID = -1;
				  Cloudlet cloudlet = new CloudletSimple(LetID++, length, pesNumber)
						  .setFileSize(fileSize)
						  .setOutputSize(outputSize).setVm(vm)
						  .setUtilizationModelCpu(new UtilizationModelDynamic(0.55))
						  .setUtilizationModelRam(um);
				  cloudlet.setJobId(4);
				  broker.bindCloudletToVm(cloudlet, vm);
				  cloudletQueue.add(cloudlet);
				  broker.submitCloudlet(cloudletQueue.poll());
				
				  cloudlet.addOnStartListener( info ->	{
					  
					  Vm VM=info.getCloudlet().getVm();
					  vmQueue.remove(VM);
				  });

				  cloudlet.addOnFinishListener(info ->	{
					  System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n", info.getTime(), info.getCloudlet());
		 		
					  OflineTime= new TriangularDistr(16,19,22);
					  this.PreviousCloudletFinishtime=info.getTime();
					  long OflineTimeDelaySAPsystemAccess2 = Ofline_Time[(int) OflineTime.sample()];
					  OflineTimeForVm=(int) (PreviousCloudletFinishtime + OflineTimeDelaySAPsystemAccess2);
		 		
					  Vm vm1=cloudlet.getVm();
					  FinishedvmList.add(vm1);
					  String Type=vm1.getDescription();
					  long Vmid=cloudlet.getVm().getId();
					  String FirstElement = Type.substring(0, 1);
					  int VmType=Integer.parseInt(FirstElement);
					  OflineTimeForVm = udjustOfflineTimeasperSchedulingTime(OflineTimeForVm);
					  VMIdWithItsOfflineDelayMap.put(Vmid, OflineTimeForVm);
					  Map<Integer, Integer> VMTypeWithItsOfflineDelayMap = new HashMap<>();
			 			VMTypeWithItsOfflineDelayMap.put(VmType, OflineTimeForVm);
					  VMIdTypeOfflineDelayMap.put(Vmid,VMTypeWithItsOfflineDelayMap);
					  cloudlet.setStatus(Cloudlet.Status.SUCCESS);
					  vm.getHost().destroyVm(vm);
					  GC.getInitialVmlistDaas().remove(vm);
					  simulation.addOnClockTickListener(this::listenerToInitiateNextDayVms);
				  });
			  }
			  break;
			  
		  
		  case 5:
			  pesNumber = 1;
		 for(int i = 0; i < numberCloudlets; i++){
			 
			 OnlineTime= new TriangularDistr(2,5,8);//SAP system access 3
			 length=Online_Time[(int)OnlineTime.sample()]*(int) (mips * 0.55);
			 int LetID = -1;
			 Cloudlet cloudlet = new CloudletSimple(LetID++, length, pesNumber)
					 .setFileSize(fileSize)
					 .setOutputSize(outputSize).setVm(vm)
					 .setUtilizationModelCpu(new UtilizationModelDynamic(0.55))
					 .setUtilizationModelRam(um);
			 cloudlet.setJobId(4);
			 broker.bindCloudletToVm(cloudlet, vm);
			 cloudletQueue.add(cloudlet);
			 broker.submitCloudlet(cloudletQueue.poll());
		
			 cloudlet.addOnStartListener( info -> {
				 Vm VM=info.getCloudlet().getVm();
				 vmQueue.remove(VM);
			 });

			 cloudlet.addOnFinishListener(info ->  {
				 System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n", info.getTime(), info.getCloudlet());
			
				 OflineTime= new TriangularDistr(16,19,22);
				 long OflineTimeDelaySAPsystemAccess3 = Ofline_Time[(int) OflineTime.sample()];
				 this.PreviousCloudletFinishtime=info.getTime();
				 OflineTimeForVm=(int) (PreviousCloudletFinishtime + OflineTimeDelaySAPsystemAccess3);
		 		
				 Vm vm1=cloudlet.getVm();
				 FinishedvmList.add(vm1);
				 String Type=vm1.getDescription();
				 long Vmid=cloudlet.getVm().getId();
				 String FirstElement = Type.substring(0, 1);
				 int VmType=Integer.parseInt(FirstElement);
				 OflineTimeForVm = udjustOfflineTimeasperSchedulingTime(OflineTimeForVm);
				 VMIdWithItsOfflineDelayMap.put(Vmid, OflineTimeForVm);
				 Map<Integer, Integer> VMTypeWithItsOfflineDelayMap = new HashMap<>();
				 VMTypeWithItsOfflineDelayMap.put(VmType, OflineTimeForVm);
				 VMIdTypeOfflineDelayMap.put(Vmid,VMTypeWithItsOfflineDelayMap);
				 cloudlet.setStatus(Cloudlet.Status.SUCCESS);
				 vm.getHost().destroyVm(vm);
				 GC.getInitialVmlistDaas().remove(vm);
				 simulation.addOnClockTickListener(this::listenerToInitiateNextDayVms);
			 });
		 }							
		 break;
		}
	}

	private int udjustOfflineTimeasperSchedulingTime(double OflineTimeForVm){
	int udjustedOfflineTime = 0;
	int schedulingTime = 300;
	if((OflineTimeForVm % schedulingTime)==0 ) {
		udjustedOfflineTime = (int) OflineTimeForVm;
	}else {
		int remainder = (int) OflineTimeForVm % schedulingTime;
		int additionalTimetobeAdded = schedulingTime - remainder;
		udjustedOfflineTime = (int) (OflineTimeForVm + additionalTimetobeAdded);
	}
	return udjustedOfflineTime;
	}
	/**
	  * Gets the created Cloudlets in a Queue
	  * @return {@link #cloudletQueue}
	  * @see #createCloudlets(int, int, Vm, DatacenterBroker, long)
	  */ 
	public Queue<Cloudlet> getCloudletQueue(){
		return cloudletQueue;
	}
	
	
 	public DatacenterBroker getDCbroker(){
 		return broker;
 	}

/*	public DatacenterSimple getDc(){
		return dc;
	}
	
	public void setDc(DatacenterSimple dc){
		this.dc = dc;
	}
		
	public Supplier<Datacenter> datacenterSupplier(){
		Supplier<Datacenter> SupplierDatacenter=this::getDc;
		return SupplierDatacenter;
	}*/
	       
}
