/**
 * 
 */
package centrlizedarchitecture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * @author Shyam Sundar V
 *
 */
public class DynamicChromosomeWA {
	private HashMap<Long, Long> genes;
	Map<Long, Double> serverUtil = new HashMap<Long, Double>();

	public double DC_POWER;
	Map<Long, Double> serverPower = new HashMap<Long, Double>();

	private double fitness = 0;
	public boolean isFitnessChanged = true;
	public Map<Long, ArrayList<Long>> ServerwithVmlist = new HashMap<Long, ArrayList<Long>>();
	Random rand = new Random();
	double vmsCpuUtil = 0;
	double vmsRAMUtil = 0;
	double serverCPUUtil = 0;
	double dcMaxPower = 0;

	/**
	 * construct a chromosome based on the current allocation size
	 */
	
	public DynamicChromosomeWA() {
		genes = new HashMap<Long, Long>(GADriverWA.dynamicVmHostMap.size());
	}

	/**
	 * method to initialize individual. Keeping the source map as a member in the
	 * population. Rest of the population intialized by mutating the current
	 * population.
	 * 
	 * @param i
	 * @return
	 */
	private List<Host> host_List = new ArrayList<>();
	public List<Host> hostsWithHighMips = new ArrayList<>();
	private List<Vm> vm_List = new ArrayList<>();

	public DynamicChromosomeWA initialize(int i, List<Host> hostList, List<Vm> vmList) throws IllegalArgumentException {

		this.host_List = hostList;
		this.vm_List = vmList;
		for (Long host : GADriverWA.targetHostList) {
			Host Host1 = host_List.get(host.intValue());
			if (Host1.getMips() > 2500) {
				hostsWithHighMips.add(Host1);
			}
		}
		/**
		 * keeping the source allocation as a member of the population
		 */
		if(i == 0){
			GADriverWA.dynamicVmHostMap.forEach((vm, server) -> {
				if(!centralManager.VmstoMigrateFromOverloadedHostsWA.isEmpty()){
					fillgenesForOverloadedHostVms(vm, server);
				}else {
					genes.put(vm, server);
				}
			});		
			serverVMMapSource(genes);
		}
		/*
		 * rest of the initial population is created by mutating the host list that
		 * consists hosts that are not switched off and not overloaded at the current
		 * scheduling interval.
		 */

		if (i >= 1) {
			GADriverWA.dynamicVmHostMap.forEach((vm, server) -> {

				if (centralManager.VmstoMigrateFromOverAndUnderloadedHostsWA.stream()
						.anyMatch(Vm -> (Vm.getId() == vm))) {
					List<Vm> ListVm = centralManager.VmstoMigrateFromOverAndUnderloadedHostsWA.stream()
							.filter(VM -> (VM.getId() == vm.intValue())).collect(Collectors.toList());
					fillGenesOverandUnderloadedHostVms(vm, server, ListVm.get(0));
				} else {
					genes.put(vm, server);
				}
			});
			serverVMMap(genes);
		}
		/*
		 * map containing hosts as keys and placed VMs list as values. To be used in
		 * fitness calculations.
		 */
		return this;
	}

	/**
	 * @param vm
	 * @param server
	 */
	private void fillgenesForOverloadedHostVms(Long vm, Long server) {
		if(centralManager.VmstoMigrateFromOverloadedHostsWA.stream()
				.anyMatch(Vm -> (Vm.getId() == vm))) {
			List<Host> NewHosts = new ArrayList<>();
			for(Long host : GADriverWA.targetHostList) {
				if(host_List.get(host.intValue()).getVmList().isEmpty()) {
					NewHosts.add(host_List.get(host.intValue()));
				}
			}
			if(NewHosts.isEmpty()) {
				genes.put(vm, GADriverWA.targetHostList.get(rand.nextInt(GADriverWA.targetHostList.size())));
			}else {
				genes.put(vm, NewHosts.get(rand.nextInt(NewHosts.size())).getId());
			}
		} else {
			genes.put(vm, server);
		}
	}

	/**
	 * @param vm
	 * @param server
	 * @param vm1
	 */
	private void fillGenesOverandUnderloadedHostVms(Long vm, Long server, Vm vm1) {
		if (vm1.getMips() == 2500) {
			fillGenesForHostsWithHghMips(vm, server);
		} else if (vm1.getMips() < 2500) {
			genes.put(vm, GADriverWA.targetHostList.get(rand.nextInt(GADriverWA.targetHostList.size())));
		}
	}

	/**
	 * @param vm
	 * @param server
	 */
	private void fillGenesForHostsWithHghMips(Long vm, Long server) {
		if (!hostsWithHighMips.isEmpty()) {
			if (hostsWithHighMips.size() > 1) {
				genes.put(vm, hostsWithHighMips.get(rand.nextInt(hostsWithHighMips.size())).getId());
			} else if (hostsWithHighMips.size() <= 1) {
				genes.put(vm, hostsWithHighMips.get(0).getId());
			}
		}
	}

	/**
	 * This method keeps the original current source allocation map Make a map with
	 * host and their current allocated vms to calculate fitness
	 * 
	 * @param genes
	 */
	public void serverVMMapSource(Map<Long, Long> genes) {

		/*
		 * a map containing the servers with the placed vmList on them
		 */
		ServerwithVmlist = new HashMap<>(genes.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue))
				.values().stream().collect(Collectors.toMap(item -> item.get(0).getValue(), item -> new ArrayList<Long>(
						item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));

		// new
		// HashMap<>(genes.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::)))

//		System.out.println("..."+ServerwithVmlist);

		serverCurrentUtilization(ServerwithVmlist);
		slaViolations();
	}

	/**
	 * Make a map with host and their current allocated vms to calculate fitness
	 * 
	 * @param genes
	 */
	public void serverVMMap(Map<Long, Long> genes) {

		/*
		 * a map containing the servers with the placed vmList on them
		 */
		ServerwithVmlist = new HashMap<>(genes.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue))
				.values().stream().collect(Collectors.toMap(item -> item.get(0).getValue(), item -> new ArrayList<Long>(
						item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));

		// new
		// HashMap<>(genes.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::)))

//		System.out.println("..."+ServerwithVmlist);

		/*
		 * while(slaViolations() != 0 ) {//|| CheckVmResourceAvailability() !=0) {
		 * 
		 * this.genes = new HashMap<Long, Long>(GADriverWA.dynamicVmHostMap.size());
		 * 
		 * GADriverWA.dynamicVmHostMap.forEach((vm,server) -> {
		 * if(LocalControllerWA.VmstoMigrateFromOverloadedAndUnderloadedHosts.contains(
		 * vm)) { this.genes.put(vm,
		 * GADriverWA.targetHostList.get(rand.nextInt(GADriverWA.targetHostList.size()))
		 * ); } this.genes.put(vm, server); });
		 * 
		 * ServerwithVmlist = new
		 * HashMap<>(this.genes.entrySet().stream().collect(Collectors.groupingBy(Map.
		 * Entry::getValue)).values().stream() .collect(Collectors.toMap(item ->
		 * item.get(0).getValue(), item -> new
		 * ArrayList<Long>(item.stream().map(Map.Entry::getKey).collect(Collectors.
		 * toList()))))); }
		 */

		serverCurrentUtilization(ServerwithVmlist);
		slaViolations();

	}

	public void setHostlistAndVmlist(List<Host> hostlist, List<Vm> Vmlist) {
		this.host_List = hostlist;
		this.vm_List = Vmlist;
	}


	double serverRamUtil = 0;
	public void serverCurrentUtilization(Map<Long, ArrayList<Long>> NewServerwithVmlist) {

		/*
		 * calculate server utilizations for servers and vms
		 */
		NewServerwithVmlist.forEach((server, vmList) -> {
			serverCPUUtil = 0;
			serverRamUtil = 0;

			double serverRam = host_List.get(server.intValue()).getRam().getCapacity();
			double serverCPU = host_List.get(server.intValue()).getTotalMipsCapacity();

			vmsCpuUtil = 0;
			vmsRAMUtil = 0;
			vmList.forEach(vm -> {
				vmsCpuUtil += vm_List.get(vm.intValue()).getCurrentRequestedTotalMips();
				vmsRAMUtil += vm_List.get(vm.intValue()).getRam().getAllocatedResource();
			});
			serverCPUUtil = vmsCpuUtil / serverCPU;
			serverRamUtil = vmsRAMUtil/serverRam;

			serverUtil.put(server, serverCPUUtil); // to calculate servers power we only need cpu utilization
			
		});
		/*
		 * map with server and it's power consumptions
		 */
		serverUtil.forEach((server, util) -> {
			double serverPowerConsump = 0;
//			System.out.println("server "+server+" Util "+util);

			if (util > 1.0) {
				util = 1.0;
				serverPowerConsump = host_List.get(server.intValue()).getPowerModel().getPower(util) + 50;
			} else if (util < 0.0) {
				util = 0.0;
				serverPowerConsump = host_List.get(server.intValue()).getPowerModel().getPower(util);
			} else if (0.0 < util && util < 1.0) {
				serverPowerConsump = host_List.get(server.intValue()).getPowerModel().getPower(util);
			}

			serverPower.put(server, serverPowerConsump);
		});
		datacenterPowerConsumption(serverPower);
	};

	/**
	 * method to calculate datacenter power consumption
	 * 
	 * @param servePower
	 * @return
	 */

	public void datacenterPowerConsumption(Map<Long, Double> serverPower) {
		dcMaxPower = 0;
		DC_POWER = 0;
		serverPower.forEach((server, power) -> {

			dcMaxPower += host_List.get(server.intValue()).getPowerModel().getMaxPower();
			DC_POWER += power;
		});
		ACTIVE_SERVERS = serverPower.keySet().size();
		normalizeDataCenterPower(DC_POWER, dcMaxPower);
	}

	/**
	 * objective function for datacenter power usage. We normalize the dc power with
	 * max min.
	 * 
	 * @param datacenterPower
	 * @return
	 */
	public double normalizeDataCenterPower(double datacenterPower, double datacenterMaxPower) {

		double normalizedDcPower = 0;
		double dcMinPower = 0;

	//	normalizedDcPower = datacenterPower / datacenterMaxPower;
		normalizedDcPower = (datacenterMaxPower-datacenterPower)/(datacenterMaxPower-dcMinPower);
//		System.out.println(normalizedDcPower);

		return normalizedDcPower;// 1-normalizedDcPower;
	}

	/**
	 * objective function for sla violations; Considered SLAs: Availability, latency
	 * and throughput Placement Groups from AWS, IBM From IBM Site "Placement groups
	 * give you a measure of control over the host on which a new public virtual
	 * server is placed. With this release, there is a �spread� rule, which
	 * means that virtual servers within a placement group are all spread onto
	 * different hosts. You can build a high availability application within a data
	 * center knowing your virtual servers are isolated from each other."
	 * 
	 * @return
	 */

	public int SLA_VIOLATIONS_IN_CHROMOSOME = 0;

	double AllvmsCpuCapacityRequirement = 0;
	double AllvmsRAMCapacityRequirement = 0;
	public List<Long> availabitlityViolatedServerVms;

	public double slaViolations() {
		SLA_VIOLATIONS_IN_CHROMOSOME = 0;
		availabitlityViolatedServerVms = new ArrayList<Long>();
		serverCurrentUtilization(ServerwithVmlist);
		ServerwithVmlist.forEach((Server, Vmlist) -> {
			AllvmsCpuCapacityRequirement = 0;
			AllvmsRAMCapacityRequirement = 0;

			double serverRamCapacity = host_List.get(Server.intValue()).getRam().getCapacity();
			double serverCPUCapacity = host_List.get(Server.intValue()).getTotalMipsCapacity();


			Vmlist.forEach(vm -> {
				Vm vm1 = vm_List.get(vm.intValue());
				AllvmsCpuCapacityRequirement += vm1.getTotalMipsCapacity();
				AllvmsRAMCapacityRequirement += vm1.getRam().getCapacity();
			});
			if ((serverRamCapacity < AllvmsRAMCapacityRequirement)
					|| (serverCPUCapacity < AllvmsCpuCapacityRequirement)) {
				SLA_VIOLATIONS_IN_CHROMOSOME += 1;
				for(Long Vm : Vmlist) {
					if(centralManager.VmstoMigrateFromOverAndUnderloadedHostsWA.contains(vm_List.get(Vm.intValue()))) {
						availabitlityViolatedServerVms.add(Vm);	
					}	
				}
			}

		});

		return SLA_VIOLATIONS_IN_CHROMOSOME/GADriverWA.sourcehostList.size();
	}

	public Map<Long, ArrayList<Long>> getServerwithVMList() {
		return ServerwithVmlist;
	}

	public HashMap<Long, Long> getGenes() {
		return genes;
	}

	public void setGenes(HashMap<Long, Long> genes) {
		this.genes = genes;
	}

	public int getSLA_VIOLATIONS() {
		return SLA_VIOLATIONS_IN_CHROMOSOME;
	}

	public double getFitness() {
		if (isFitnessChanged == true) {
			fitness = calculateFitness();
			isFitnessChanged = false;
		}
		return fitness;
	}

	int TotalNoOfMigrations;

	public int TotalNumberOfMigrations() {
		TotalNoOfMigrations = 0;
		Map<Long, Long> SourceMap = GADriverWA.dynamicVmHostMap;

		SourceMap.forEach((Vm, Server) -> {
			if (!(genes.get(Vm) == Server)) {

				/*
				 * Check if the source host and the destination (randomly selected new) host are
				 * same. If same they indicate that no migrations will be necessary. When they
				 * are different then migration count is incremented by 1.
				 */
				TotalNoOfMigrations += 1;
			}
		});

		return TotalNoOfMigrations/GADriverWA.sourcevmList.size();
	}

	int Count = 0;
	boolean vmisPresent = false;
	public int getSLAVUP() {
		ServerwithVmlist.forEach((server, vmList) -> {
			serverCPUUtil = 0;
			serverRamUtil = 0;
			vmisPresent = false;
			double serverRam = host_List.get(server.intValue()).getRam().getCapacity();
			double serverCPU = host_List.get(server.intValue()).getTotalMipsCapacity();
			vmsCpuUtil = 0;
			vmsRAMUtil = 0;
			vmList.forEach(vm -> {	
				vmsRAMUtil += vm_List.get(vm.intValue()).getRam().getAllocatedResource();
				vmsCpuUtil += vm_List.get(vm.intValue()).getTotalCpuMipsUtilization();
				if(centralManager.VmstoMigrateFromOverAndUnderloadedHostsWA.contains(vm_List.get(vm.intValue()))) {
					vmisPresent = true;
				}
			});
			
			serverCPUUtil = vmsCpuUtil / serverCPU;
			serverRamUtil = vmsRAMUtil/serverRam;	
				
			double UpperUtilizationThreshold = centralManager.HostUpperUtilizationThresholdWA;
			if(vmisPresent == true) {
				if ((serverCPUUtil > UpperUtilizationThreshold) || (serverRamUtil > UpperUtilizationThreshold)) {
				Count += 1;
				for(Long Vm : vmList) {
					if(centralManager.VmstoMigrateFromOverAndUnderloadedHostsWA.contains(vm_List.get(Vm.intValue()))) {
						availabitlityViolatedServerVms.add(Vm);	
					}		
				}
			}
		}
		});
		return Count/GADriverWA.sourcehostList.size();
	}
	
	public int ACTIVE_SERVERS;

	public double calculateFitness() {
		double chromosomeFitness = 0;

		chromosomeFitness = (0.5 * (ACTIVE_SERVERS/GADriverWA.sourcehostList.size())) + (0.3 * (slaViolations() + getSLAVUP()))
						+ (0.1 + TotalNumberOfMigrations()) + (0.1 * (normalizeDataCenterPower(DC_POWER, dcMaxPower)));
		return chromosomeFitness;
	}
}
