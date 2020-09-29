/**
 * 
 */
package hierarchicalarchitecture.localcontrollerdaas;

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

import hierarchicalarchitecture.globalcontroller.GlobalController;


/**
 * @author Shyam Sundar V
 */
public class ChromosomeDAAS {

	/*
	 * a gene represent a host 
	 */
	public int[] genes;
	public double fitness = 0;
	public boolean isFitnessChanged = true;
	public double SERVER_POWER;
	public double DC_POWER;
	double dcMaxPower = 0 ;
	public int SERVERS;
	public int ACTIVE_SERVERS;
	public int VMS;
	public int SLA_VIOLATIONS_IN_CHROMOSOME = 0;
	public double datacenterActivePower = 0;
	public Map<Integer, Integer> serverVmallocation;
	public Map<Integer, Integer> vmToServerMap = new HashMap<Integer, Integer>();
	public Map<Integer, ArrayList<Integer>> serverwithVMList = new HashMap<Integer, ArrayList<Integer>>();

	/*
	 * for each server map it's id as a key and it's utilization as a value.
	 */
	Map<Integer, Double> serverUtil = new HashMap<Integer, Double>();
	/*
	 * for each server map it's id as a key and it's powerConsumption as a value.
	 */
	Map<Integer, Double> serverPower = new HashMap<Integer, Double>();
	
	double vmsCpuUtil = 0;
	double vmsRAMUtil = 0;
	double serverCPUUtil = 0;
	int randomInt;
	private boolean atleastOneVmisCreated = false;
	private List<Host> Host_List = new ArrayList<>();
	private List<Vm> VM_List = new ArrayList<>();
	public int SLA_VIOLATIONS_UpperThreshold;
	double VmResourceAvailabilityViolation;
	int TotalNoOfMigrations;
	int Host;
	
	
	/**
	 * Initialize genes
	 * @param length
	 */
	public ChromosomeDAAS(int numberofVms) {
		genes = new int[numberofVms];
	}

	/**
	 * generate a random int within the range of host list
	 * @return
	 */
	public int randomNumberGenerator(){
		return randomInt = ThreadLocalRandom.current().nextInt(1, genes.length+1);
	}
	
	/**
	 * Intialize a chromosome, each gene represent a host and each gene consists Vms.
	 * Genes are filled with random vms, each vm is associated with a random number(host id) 
	 * Here the size of a chromosome is equal to the number of hosts which is same as number of genes.
	 * 
	 * @param hostList
	 * @return
	 */
	public ChromosomeDAAS initializeChromosome(int numberOfHosts, List<Host> hostList, List<Vm> vmList) {
		this.Host_List = hostList;
		this.VM_List = vmList;
		this.SERVERS = hostList.size();
		Host host = Host_List.get(0);		
		double simultionTime = host.getDatacenter().getSimulation().clock();
		
		if(simultionTime > 1000) {
			fillGenesForVmsThatArriveAfterOffline(numberOfHosts);
		} else {
			for(int i=0; i < genes.length; i++) {
				genes[i] = ThreadLocalRandom.current().nextInt(0, numberOfHosts);
			}
		}
		
		makeVMServerMap(genes);
		calculateFitness();
		return this;
	}
	
	
	/**
	 * @param numberOfHosts
	 */
	private void fillGenesForVmsThatArriveAfterOffline(int numberOfHosts) {
		for(int i=0; i < genes.length; i++) {
			if(VM_List.get(i).isCreated()) {
				Vm vm = VM_List.get(i);
				genes[i] = (int) fillGenesForVmsinMigration(vm);
				
				atleastOneVmisCreated = true;
			}else {
				genes[i] = ThreadLocalRandom.current().nextInt(0, numberOfHosts);
			}		
		}
	}
	
	/**
	 * @param vm
	 */
	private long fillGenesForVmsinMigration(Vm vm) {
		long HostId=0;
			Here:
			for(final Map.Entry<Long, Long> entryDaas : LocalControllerDaas.bestDynamicVmServerMap.entrySet()){
				if(vm.getId() == entryDaas.getKey().intValue()) {
					HostId = entryDaas.getValue();
					break Here;
				}		
			}
		return HostId;
	}
	
	/**
	 * Method to make maps between VMs and Servers
	 * 1.vmToServerMap -> key: vmId; value: serverId.
	 * 2.serverVmAllocation -> key: server; value: numberOfVms
	 * 3.serverwithVMList -> key: server; value: placed VM list
	 * @param genes
	 */
	public void makeVMServerMap(int[] genes) {
	
		vmToServerMap = IntStream.range(0, genes.length).boxed().
				collect(Collectors.toMap(key -> key, value -> genes[value]));
	
		serverVmallocation = Arrays.stream(genes).boxed().collect(Collectors.groupingBy(e -> e,
				Collectors.reducing(0, e -> 1, Integer::sum)));
				
		serverwithVMList = new HashMap<>(vmToServerMap.entrySet().stream()
				.collect(Collectors.groupingBy(Map.Entry::getValue)).values().stream()
				.collect(Collectors.toMap(item -> item.get(0).getValue(),
						item -> new ArrayList<Integer>(item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));
			
			serverUtilizationAndPower(serverwithVMList);
	}
	
	/**
	 * @param genes
	 */
	public void makeVMServerMapDuringCrossoverAndMutation(int[] genes) {
		
		vmToServerMap = IntStream.range(0, genes.length).boxed().collect(Collectors.toMap(key -> key, value -> genes[value]));
		
		serverVmallocation = Arrays.stream(genes).boxed().collect(Collectors.groupingBy(e -> e,
					Collectors.reducing(0, e -> 1, Integer::sum)));
	
		serverwithVMList = new HashMap<>(vmToServerMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue)).values().stream()
											.collect(Collectors.toMap(item -> item.get(0).getValue(),
													item -> new ArrayList<Integer>(item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));
	
		serverUtilizationAndPower(serverwithVMList);
	}
	
	/**
	 * Added specifically for crossover chromosome (hostlist-IndexOutOfBoundsException: Index: 0, Size: 0) 
	 * 
	 * @param hostlist
	 * @param vmList
	 */
	public void setHostlistAndVmlist(List<Host> hostlist, List<Vm> vmList) { 
		this.Host_List=hostlist;
		this.VM_List=vmList;
	}
	
	
	/**
	 * method to calculate utilization and power consumption for heterogeneous servers and 
	 * vms.   
	 * @param vmsOnServers
	 */
	public void serverUtilizationAndPower(Map<Integer, ArrayList<Integer>>  vmsOnServers) {
		SLA_VIOLATIONS_UpperThreshold = 0;

		vmsOnServers.forEach((server,vmList) -> {
	
			double serverRam = this.Host_List.get(server).getRam().getCapacity();
			double serverCPU = this.Host_List.get(server).getTotalMipsCapacity();
		
			 vmsCpuUtil = 0;
			 vmsRAMUtil = 0;
		vmList.forEach(vm -> {
			vmsCpuUtil += this.VM_List.get(vm).getCurrentRequestedTotalMips(); //Vm cpu total capacity
			vmsRAMUtil += this.VM_List.get(vm).getRam().getAllocatedResource(); //vm ram total capacity
		});
		serverCPUUtil = vmsCpuUtil / serverCPU;
		double serverRamUtil = 0;
		serverRamUtil = (vmsRAMUtil/serverRam);
		double UpperUtilizationThreshold = GlobalController.HostUpperUtilizationThresholdDAAS;
	
		if ((serverCPUUtil > UpperUtilizationThreshold) || (serverRamUtil > UpperUtilizationThreshold)) {
			SLA_VIOLATIONS_UpperThreshold += 1;
		}
		
		serverUtil.put(server, serverCPUUtil);
		});
		
		serverUtil.forEach((server, util) -> {
			double serverPowerConsump = 0;
		
			if(util > 1) {//------------> When the requested resources by all the vms go beyond the capacity of the host then this util variable goes beyond 1.
				util = 1.0;
				serverPowerConsump = this.Host_List.get(server).getPowerModel().getPower(util)+50;
			}else if (util < 0) { 
				util = 0.0;
				serverPowerConsump = this.Host_List.get(server).getPowerModel().getPower(util);
			} else {
				serverPowerConsump = this.Host_List.get(server).getPowerModel().getPower(util);
			}
			serverPower.put(server, serverPowerConsump);
		});
	
		datacenterPowerConsumption(serverPower);//---->
		slaViolations();		
	}
	
	/**
	 * @param serverPower
	 */
	public void datacenterPowerConsumption(Map<Integer, Double> serverPower) {
		this.SERVERS=this.Host_List.size();
		dcMaxPower = 0 ;
		DC_POWER = 0;
		
		serverPower.forEach((server, power) -> {	
			dcMaxPower += this.Host_List.get(server).getPowerModel().getMaxPower();
			DC_POWER += power;	
		});
		
		ACTIVE_SERVERS = serverPower.keySet().size();
		normalizeDataCenterPower(DC_POWER, dcMaxPower, ACTIVE_SERVERS);
	}
	
	/**
	 * objective function for datacenter power usage in the rage [0, 1]
	 * 
	 * @param datacenterPower
	 * @return
	 */
	public double normalizeDataCenterPower(double datacenterPower, double datacenterMaxPower, int activeServers) {
		
		double normalizedDcPower = 0;
		double dcMinPower = 0;
		datacenterMaxPower = 8 * 303; //303 is the max power a host can consume at its full utilization.
		normalizedDcPower = (datacenterMaxPower-datacenterPower)/(datacenterMaxPower-dcMinPower);
		
		return 1-normalizedDcPower;
	}
	
	/**
	 * objective function for sla violations;
	 * Considered SLAs: resource availability, host upper utilization.
	 * 
	 * @param serverUtil
	 * @return
	 */
	double AllvmsCpuCapacityRequirement = 0;
	double AllvmsRAMCapacityRequirement = 0;
	private Map<Integer, Boolean> ServerViolationMap;
	public List<Integer> availabitlityViolatedServers;
	public double slaViolations() {	
		SLA_VIOLATIONS_IN_CHROMOSOME = 0;
		VmResourceAvailabilityViolation = 0;
		availabitlityViolatedServers = new ArrayList<Integer>();
		ServerViolationMap = new HashMap<Integer,Boolean>();
		serverwithVMList.forEach((Server, Vmlist) -> {
			AllvmsCpuCapacityRequirement = 0;
			AllvmsRAMCapacityRequirement = 0;
			double serverRamCapacity = this.Host_List.get(Server).getRam().getCapacity();
			double serverCPUCapacity = this.Host_List.get(Server).getTotalMipsCapacity();
			double serverSingleCoreMips = this.Host_List.get(Server).getMips();
			Vmlist.forEach(vm -> {
				Vm vm1=this.VM_List.get(vm);
				if(vm1.isCreated()) {
					atleastOneVmisCreated = true;	
				}
				AllvmsCpuCapacityRequirement += vm1.getTotalMipsCapacity();;
				AllvmsRAMCapacityRequirement += vm1.getRam().getCapacity();
				double vmsCpuSinglecoreMipsRequirement = 0;
				vmsCpuSinglecoreMipsRequirement = vm1.getTotalMipsCapacity();
				
				if ((serverSingleCoreMips < vmsCpuSinglecoreMipsRequirement)) {
					VmResourceAvailabilityViolation += 1;// this does not have any effect for DaaS
				}
			});
			if ((serverRamCapacity < (AllvmsRAMCapacityRequirement)) || (serverCPUCapacity < AllvmsCpuCapacityRequirement)) {
				SLA_VIOLATIONS_IN_CHROMOSOME += 1;
				ServerViolationMap.put(Server, true);
				availabitlityViolatedServers.add(Server);
			}else {
				ServerViolationMap.put(Server, false);
			}
		});
		
		return SLA_VIOLATIONS_IN_CHROMOSOME;
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
	
	public int getSLAViolations() {
		return SLA_VIOLATIONS_IN_CHROMOSOME;
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
	
	public int TotalNumberOfMigrations() {
		TotalNoOfMigrations = 0;
		Map<Long, Long> SourceMap = LocalControllerDaas.bestDynamicVmServerMap;
		List<Vm> vmlist ;
		if(GADriverDaas.vmListDAAS == null) {
			vmlist = this.VM_List;
		}else {
			vmlist = GADriverDaas.vmListDAAS;
		}
		vmlist.forEach((Vm) -> {
			if(Vm.isCreated()) {
				Here:
				for(final Map.Entry<Long, Long> entryDaas : SourceMap.entrySet()){
					if(Vm.getId() == entryDaas.getKey()) {
						Host = entryDaas.getValue().intValue();	
						break Here;
					}
				}
				if(!(genes[vmlist.indexOf(Vm)] == Host)) {
					/*
					 * Check if the source host and the destination (randomly selected new) host are
					 * same. If they are same, that indicate that no migrations will be necessary. When they
					 * are different then migration count is incremented by 1.
					 */
					TotalNoOfMigrations += 1;
				}
			}
		});
		return TotalNoOfMigrations;
	}
	
	public double calculateFitness() {
		double chromosomeFitness = 0;	 
		if(atleastOneVmisCreated == false) {
			double normalisedSLA = slaViolations() / GADriverDaas.hostlist_Size;
			double normalisedActiveHosts = (ACTIVE_SERVERS/GADriverDaas.hostlist_Size);
			double normalisedPower = normalizeDataCenterPower(DC_POWER, dcMaxPower, ACTIVE_SERVERS);
			
			chromosomeFitness = (0.1 * normalisedSLA) + (0.7 * normalisedActiveHosts) + (0.2 * normalisedPower); 
		}else { // this case is specific for the vm's that arrive after offline
			double normalisedSLA = (slaViolations() / GADriverDaas.hostlist_Size) + (SLA_VIOLATIONS_UpperThreshold/GADriverDaas.hostlist_Size);
			double normalisedActiveHosts = (ACTIVE_SERVERS/GADriverDaas.hostlist_Size);
			double normalisedPower = normalizeDataCenterPower(DC_POWER, dcMaxPower, ACTIVE_SERVERS);
			double normalisedTotalNoOfMigrations = TotalNumberOfMigrations() / GADriverDaas.vmlist_Size;
			
			chromosomeFitness = (0.4 * normalisedSLA) + (0.1 * normalisedTotalNoOfMigrations) + (0.4 * normalisedActiveHosts) + (0.1 * normalisedPower);	
		}
				 
		return chromosomeFitness;
	} 
}
