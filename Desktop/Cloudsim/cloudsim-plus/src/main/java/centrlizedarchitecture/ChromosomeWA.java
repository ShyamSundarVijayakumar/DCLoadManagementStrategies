/**
 * 
 */
package centrlizedarchitecture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * @author Shyam Sundar V
 *
 */
public class ChromosomeWA {
	/*
	 *a gene represent a host 
	 */
	public int[] genes;
	public double fitness = 0;
	public boolean isFitnessChanged = true;
	public double DC_POWER;
	double dcMaxPower = 0 ;
	/*
	 *	Includes active and inactive servers i.e total servers in that application.
	 */
	public int servers;
	public int ACTIVE_SERVERS;
	public int VMS;
	public int resource_Availability_Violation = 0;
	public double datacenterActivePower = 0;
	public Map<Integer, Integer> serverVmallocation;	
	public Map<Integer, Integer> vmToServerMap = new HashMap<Integer, Integer>();
	public Map<Integer, ArrayList<Integer>> serverwithVMList = new HashMap<Integer, ArrayList<Integer>>();

	/*
	 * for each server store server id as a key and it's utilization as a value.
	 */
	Map<Integer, Double> serverUtil = new HashMap<Integer, Double>();
	/*
	 * for each server store server id as a key and it's powerConsumption as a value.
	 */
	Map<Integer, Double> serverPower = new HashMap<Integer, Double>();	
	double vmsCpuUtil = 0;
	double vmsRAMUtil = 0;
	double serverCPUUtil = 0;
	int randomInt;
	
	/**
	 * Initialize genes
	 * @param length
	 */
	public ChromosomeWA(int numberofVms) {
		genes = new int[numberofVms]; // genes------> array size should be same as number of hosts..		
	}
	
	/**
	 * generate a random int within the range of host list
	 * @return
	 */
	public int randomNumberGenerator(){
		/*
		 * generates a random int inclusive inner bound and exclusive of upper bound
		 * 
		 */
		 randomInt = ThreadLocalRandom.current().nextInt(1, genes.length+1);
		
		return randomInt;
	}
	
	/**
	 * Intialize a chromosome. each gene represent a host and each gene consists Vms.
	 * Initially a gene is filled with random vms, each vm is associated with a random number(host id) 
	 * Here the size of a chromosome is equal to  the number of hosts which is same as number of genes.
	 * @param hostList
	 * @return
	 */
	private List<Host> host_List = new ArrayList<>();
	private List<Host> hostsWithHighMips = new ArrayList<>();
	private List<Vm> vm_List = new ArrayList<>();
	public ChromosomeWA initializeChromosome(int numberOfHosts,List<Host> hostList, List<Vm> vmList) {
		
		this.host_List = hostList;
		this.vm_List = vmList;
		this.servers=hostList.size();
		
		for(Host host : host_List) {
			if(host.getMips() > 2500) {
				hostsWithHighMips.add(host);
			}
		}
		/*
		 * for each gene add associated host id
		 */
		for(int i=0; i < genes.length; i++) {
		/*
		 * generate a random Id which is associated with host id
		 */
			Vm vm = vm_List.get(i);
			if(vm.getMips() == 2500) {
				Host host = hostsWithHighMips.get(ThreadLocalRandom.current().nextInt(0, hostsWithHighMips.size()));
				genes[i] = (int) host.getId();	
			} else {
				genes[i] = ThreadLocalRandom.current().nextInt(0, numberOfHosts);
			}
		}
	//	System.out.println("\n Server->Vms"+serverVmallocation);
		
		
		makeVMServerMap(genes);//----------------> here you consider only one chromosome at a time with its genes.Here genes have hosts with random ids.
							//------------------> for example gene[1]= some random number. This random number is assumed to be a host id.
		calculateFitness();
		return this;
	}
	/**
	 * Method to make maps between VMs and Servers
	 * 1.vmToServerMap -> key: vmId; value: serverId.
	 * 2.serverVmAllocation -> key: server; value: numberOfVms
	 * 3.serverwithVMList -> key: server; value: placed VM list
	 * @param genes
	 */
	List<Long> serveridSV = new ArrayList<Long>();
	List<Integer> availabilityNotViolatedServers;
	public void makeVMServerMap(int[] genes) {
		/*
		 * Map to store vm as a key and placed in server as value
		 */
		//---------------------------------------> genes length is the number of vm's. in each gene value a rondom host id is given.
		//---------------------------------------> Because the stream will go upto the number of vms, each vm (i.e here its key) a random host is mapped.
		
			vmToServerMap = IntStream.range(0, genes.length).boxed().collect(Collectors.toMap(key -> key, value -> genes[value]));//------------> vmToServerMap==vmIdServerIdMap
			
		//	System.out.println("---------------------------->vmToServerMap "+vmToServerMap);
			/*
			 * make a map to get the number of vms placed in 
			 * each host
			 */
			serverVmallocation = Arrays.stream(genes).boxed().collect(Collectors.groupingBy(e -> e,
					Collectors.reducing(0, e -> 1, Integer::sum)));
	//		System.out.println("---------------------------->serverVmallocation   "+serverVmallocation);
			/*
	//		 * a map containing the servers with the placed vmList on them
	//		 */
			
			//Workaround for bug
			serverwithVMList = new HashMap<>(vmToServerMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue)).values().stream()
											.collect(Collectors.toMap(item -> item.get(0).getValue(),
													item -> new ArrayList<Integer>(item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));
			
		//	System.out.println("---------------------------->serverwithVMList   "+serverwithVMList);
			/* serverwithVMList = new HashMap<>(
				    vmToServerMap.entrySet().stream()
				        .collect(Collectors.groupingBy(Map.Entry::getValue)).values().stream()
				        .collect(Collectors.toMap(
				                item -> item.get(0).getValue(),
				                item -> new ArrayList<>(
				                    item.stream()
				                        .map(Map.Entry::getKey)
				                        .collect(Collectors.toList())
				                ))
				        ));*/
	//		 System.out.println("\n Server -> Vm"+serverwithVMList);
	// System.out.println("\nIn chromosome makeVMServerMap() Vm -> Server"+vmToServerMap);
		//	while(ResourceAvailabilityViolation() != 0 ) {
				 
	//			if(!ViolatedServersList.isEmpty()) {
	//				for(int i=0; i<ViolatedServersList.size();i++) {
		//				int Violatedserver = ViolatedServersList.get(i);	
		//				moveAVmToRandomHost(Violatedserver);
	//			}	
		//		}
	//			
				vmToServerMap = IntStream.range(0, genes.length).boxed().collect(Collectors.toMap(key -> key, value -> genes[value]));//------------> vmToServerMap==vmIdServerIdMap
	//
		//		serverVmallocation = Arrays.stream(genes).boxed().collect(Collectors.groupingBy(e -> e,
		//					Collectors.reducing(0, e -> 1, Integer::sum)));
				
		//		serverwithVMList = new HashMap<>(vmToServerMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue)).values().stream()
		//											.collect(Collectors.toMap(item -> item.get(0).getValue(),
		//													item -> new ArrayList<Integer>(item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));
		//	}
			
			/*
			 * make a map for server utilizations and powerconsumption
			 */
			serverUtilizationAndPower(serverwithVMList);
	}
	
//	private void moveAVmToRandomHost(int Violatedserver) {
//		Random	rand = new Random();
//		Here:
//		for(int i=0; i < this.genes.length; i++) {
//			if(Violatedserver == genes[i]) {	
//				genes[i] = ThreadLocalRandom.current().nextInt(0, host_List.size());
//			//genes[i] = serveridNoViolation.get(serveridNoViolation.size());
//			break Here;
//			}					
//		}
//	}
	
	public void makeVMServerMapDuringCrossoverAndMutation(int[] genes) {
		
		vmToServerMap = IntStream.range(0, genes.length).boxed().collect(Collectors.toMap(key -> key, value -> genes[value]));//------------> vmToServerMap==vmIdServerIdMap
	
		serverVmallocation = Arrays.stream(genes).boxed().collect(Collectors.groupingBy(e -> e,
					Collectors.reducing(0, e -> 1, Integer::sum)));
	
		serverwithVMList = new HashMap<>(vmToServerMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue)).values().stream()
											.collect(Collectors.toMap(item -> item.get(0).getValue(),
													item -> new ArrayList<Integer>(item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));
			
			serverUtilizationAndPower(serverwithVMList);
	}
	
	public void setHostlistAndVmlist(List<Host> hostlist, List<Vm> vmList) { // Added specifically for crossover chromosome (hostlist-IndexOutOfBoundsException: Index: 0, Size: 0) 
		this.host_List=hostlist;
		this.vm_List=vmList;
	}
	/**
	 * method to calculate utilization and power consumption for heterogeneous servers and 
	 * vms.   
	 * @param vmsOnServers
	 */
	public void serverUtilizationAndPower(Map<Integer, ArrayList<Integer>>  vmsOnServers) {
	
	/*
	 * calculate server utilizations for heterogeneous servers and heterogeneous vms	
	 */
	vmsOnServers.forEach((server,vmList) -> {
		double serverCPU = this.host_List.get(server).getTotalMipsCapacity();
		
		//----------------------------------------------> Here it is not utilization it is the requested capacity.
		 vmsCpuUtil = 0;
		 vmsRAMUtil = 0;
		vmList.forEach(vm -> {
			vmsCpuUtil += this.vm_List.get(vm).getCurrentRequestedTotalMips(); 
			vmsRAMUtil += this.vm_List.get(vm).getCurrentRequestedRam(); 
		});
		serverCPUUtil = vmsCpuUtil / serverCPU;
		serverUtil.put(server, serverCPUUtil); 
	});
	/*
	 *map with server and it's power consumptions 
	 */
	serverUtil.forEach((server, util) -> {
		double serverPowerConsump = 0;
		
		if(util > 1) {//------------> When the requested resources by all the vms go beyond the capacity of the host then this util variable goes beyond 1.
			util = 1.0;
		 serverPowerConsump = this.host_List.get(server).getPowerModel().getPower(util)+50;
		}	else if (util < 0) { 
			util = 0.0;
			serverPowerConsump = this.host_List.get(server).getPowerModel().getPower(util);
		} else {
			serverPowerConsump = this.host_List.get(server).getPowerModel().getPower(util);
		}
		
		serverPower.put(server, serverPowerConsump);
	});
		datacenterPowerConsumption(serverPower);
		resourceAvailabilityViolation();	
	}
	
	public void datacenterPowerConsumption(Map<Integer, Double> serverPower) {
		this.servers=this.host_List.size();
		 dcMaxPower = 0 ;
		 DC_POWER = 0;
		serverPower.forEach((server, power) -> {
			dcMaxPower += this.host_List.get(server).getPowerModel().getMaxPower();
			DC_POWER += power;	
		});
		
		ACTIVE_SERVERS = serverPower.keySet().size();
		normalizeDataCenterPower(DC_POWER, dcMaxPower, ACTIVE_SERVERS);
	}
	
	/**
	 * objective function for datacenter power usage in the rage [0, 1]
	 * 
	 * 
	 * @param datacenterPower
	 * @return
	 */
	public double normalizeDataCenterPower(double datacenterPower, double datacenterMaxPower, int activeServers) {
		
		double normalizedDcPower = 0;
		double dcMinPower = 0;
		
//		normalizedDcPower = datacenterPower / datacenterMaxPower;
	normalizedDcPower = (datacenterMaxPower-datacenterPower)/(datacenterMaxPower-dcMinPower); 
	//	System.out.println(normalizedDcPower);
	
		return normalizedDcPower;//1-normalizedDcPower;
	}
	
	/**
	 * objective function for sla violations;
	 * Considered SLAs: Availability, latency and throughput
	 * Placement Groups from AWS, IBM
	 * From IBM Site "Placement groups give you a measure of control over the host on which a new public virtual server is placed.
	 *  With this release, there is a �spread� rule, which means that virtual servers within a placement group are all spread onto different hosts. 
	 * You can build a high availability application within a data center knowing your virtual servers are isolated from each other." 
	 * @param serverUtil
	 * @return
	 */
	double allvmsCpuCapacityRequirement = 0;
	double allvmsRAMCapacityRequirement = 0;
	public List<Integer> availabitlityViolatedServers;
	public double resourceAvailabilityViolation() {	
	
		resource_Availability_Violation = 0;
		availabitlityViolatedServers = new ArrayList<Integer>();
		availabilityNotViolatedServers = new ArrayList<Integer>();
		serverwithVMList.forEach((Server, Vmlist) -> {
			allvmsCpuCapacityRequirement = 0;
			allvmsRAMCapacityRequirement = 0;
			double serverRamCapacity = this.host_List.get(Server).getRam().getCapacity();
			double serverCPUCapacity = this.host_List.get(Server).getTotalMipsCapacity();		
			Vmlist.forEach(vm -> {
				Vm vm1=this.vm_List.get(vm);
				allvmsCpuCapacityRequirement += vm1.getTotalMipsCapacity();
				allvmsRAMCapacityRequirement += vm1.getRam().getCapacity();
				});
			if ((serverRamCapacity < allvmsRAMCapacityRequirement) || (serverCPUCapacity < allvmsCpuCapacityRequirement)) {
				resource_Availability_Violation += 1;
				availabitlityViolatedServers.add(Server);
			}else {
				availabilityNotViolatedServers.add(Server);
			}		
		});
		return resource_Availability_Violation;
	}
	
	List<Integer> slaViolatedVms = new ArrayList<>();
	  public double SLAViolation() { 
		  slaViolation= 0; 
		  serverwithVMList.forEach((Server, Vmlist) -> { 
			  double serverSingleCoreMips = this.host_List.get(Server).getMips(); 
			  Vmlist.forEach(vm -> { 
				  double vmsCpuSinglecoreMipsRequirement = 0; 
				  Vm vm1=this.vm_List.get(vm);
	  			  vmsCpuSinglecoreMipsRequirement = vm1.getTotalMipsCapacity();
	  
				  if((serverSingleCoreMips < vmsCpuSinglecoreMipsRequirement)) {
					  slaViolation += 1; 
					  slaViolatedVms.add(vm);
					  } 
				  }); 
			  }); 
		  return slaViolation/GADriverWA.vmlist_sizeIP;
	  }
	 
	
	public int[] getGenes() {
		isFitnessChanged = true;
		return genes;
	}
	
	public Map<Integer, Integer> getVmToServerMap(){
		return vmToServerMap;
	}
	
	public  Map<Integer, ArrayList<Integer>> getServersWithVmList(){
		return serverwithVMList;
	}
	
	public double getSLAViolations() {
		return slaViolation;
	}
	
	public double getFitness() {
		if(isFitnessChanged == true) {
			fitness =  calculateFitness();
			isFitnessChanged = false;
		}
		return fitness;
	}
	
	public String toString() {
		return Arrays.toString(this.genes);
	}
	double slaViolation;
	
	public double calculateFitness() {
		double chromosomeFitness = 0;
		
		 chromosomeFitness = 
				 (0.3 * (ACTIVE_SERVERS / centrlizedarchitecture.GADriverWA.Hostlist_sizeIP)) + (0.45 * (resourceAvailabilityViolation()/GADriverWA.Hostlist_sizeIP)) +
				 (0.25 *(normalizeDataCenterPower(DC_POWER, dcMaxPower, ACTIVE_SERVERS)));
			
		
		return chromosomeFitness;
	}
	
}
