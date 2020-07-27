/**
 * 
 */
package hierarchicalarchitecture.localcontrollerdaas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.Collections;

import hierarchicalarchitecture.globalcontroller.GlobalController;

/**
 * @author Shyam Sundar V
 *
 */
public class DynamicChromosomeDAAS {
	private HashMap<Long, Long> genes;

	/*
	 * for each server store server id as a key and it's utilization as a value.
	 */
	private Map<Long, Double> serverUtil = new HashMap<Long, Double>();

	private double DC_POWER;

	/*
	 * for each server store server id as a key and it's current powerConsumption as
	 * a value.
	 */
	private Map<Long, Double> serverPower = new HashMap<Long, Double>();

	private double fitness = 0;
	public Map<Long, ArrayList<Long>> ServerwithVmlist = new HashMap<Long, ArrayList<Long>>();
	Random rand = new Random();

	double vmsCpuUtil = 0;
	double vmsRAMUtil = 0;

	double serverCPUUtil = 0;

	private double dcMaxPower = 0;

	/**
	 * construct a chromosome based on the current allocation size
	 */
	
	public DynamicChromosomeDAAS() {
		genes = new HashMap<Long, Long>(GADriverDaas.dynamicVmHostMap.size());
	}

	/**
	 * method to initialize individual. Keeping the source map as a member in the
	 * population. Rest of the population intialized by mutating the current
	 * population.
	 * 
	 * @param i
	 * @return
	 */
	List<Long> VMID = new ArrayList<>();

	public DynamicChromosomeDAAS initialize(int i) throws IllegalArgumentException {
	
		LocalControllerDaas.VmstoMigrateFromOverloadedUnderloadedHosts.forEach(vm -> {
			VMID.add(vm.getId());
		});
		/**
		 * keeping the source allocation as a member of the population
		 */
		
		  if(i==0) {
			  GADriverDaas.dynamicVmHostMap.forEach((vm, server) -> {
					if(!LocalControllerDaas.VmstoMigrateFromOverloadedHostsDAAS.isEmpty()){
						fillgenesForOverloadedHosts(vm, server);
					}else {
						findRandomHostForVm(vm, server);
			//			genes.put(vm, server);
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
			GADriverDaas.dynamicVmHostMap.forEach((vm, server) -> {
				Collections.sort(VMID);
			
				if (VMID != null) {
					findRandomHostForVm(vm, server);
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
	private void fillgenesForOverloadedHosts(Long vm, Long server) {
		if(LocalControllerDaas.VmstoMigrateFromOverloadedHostsDAAS.stream().anyMatch(Vm -> (Vm.getId() == vm))) {
			genes.put(vm, GADriverDaas.targetHostList.get(rand.nextInt(GADriverDaas.targetHostList.size())));
		} else {
			genes.put(vm, server);
		}
	}

	/**
	 * @param vm
	 * @param server
	 */
	private void findRandomHostForVm(Long vm, Long server) {
		if (VMID.contains(vm)) {
			genes.put(vm, GADriverDaas.targetHostList.get(rand.nextInt(GADriverDaas.targetHostList.size())));
			VMID.remove(vm);
		} else {
			genes.put(vm, server);
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

		serverCurrentUtilization(ServerwithVmlist);
		slaViolations();
	}

	/**
	 * Make a map with host and their current allocated vms to calculate fitness
	 * 
	 * @param genes
	 */
	List<Long> serveridSV = new ArrayList<Long>();
	public void serverVMMap(Map<Long, Long> genes) {

		/*
		 * a map containing the servers with the placed vmList on them
		 */
		ServerwithVmlist = new HashMap<>(genes.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue))
				.values().stream().collect(Collectors.toMap(item -> item.get(0).getValue(), item -> new ArrayList<Long>(
						item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));

	

		/*
		 * while (slaViolations() != 0) { serveridSV = new ArrayList<Long>();
		 * LocalControllerDaas.VmstoMigrateFromOverloadedUnderloadedHosts.forEach(vm ->
		 * { VMID.add(vm.getId()); });
		 * 
		 * for (Entry<Long, Boolean> entry : ServerViolationMap.entrySet()) { if
		 * (entry.getValue().equals(true)) { serveridSV.add(entry.getKey().longValue());
		 * } }
		 * 
		 * for (Entry<Long, Long> entry : this.genes.entrySet()) { if
		 * (serveridSV.contains(entry.getValue().longValue())) { if (VMID != null) {
		 * findRandomHostforVm(entry.getKey(), entry.getValue(), serveridSV); } } }
		 * 
		 * ServerwithVmlist = new HashMap<>(
		 * this.genes.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::
		 * getValue)).values().stream() .collect(Collectors.toMap(item ->
		 * item.get(0).getValue(), item -> new ArrayList<Long>(
		 * item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));
		 
		}*/

		serverCurrentUtilization(ServerwithVmlist);
	}

	public int SLA_VIOLATIONS_UpperThreshold;
	private void serverCurrentUtilization(Map<Long, ArrayList<Long>> NewServerwithVmlist) {

		SLA_VIOLATIONS_UpperThreshold = 0;
		/*
		 * calculate server utilizations for servers and vms
		 */
		NewServerwithVmlist.forEach((server, vmList) -> {
			serverCPUUtil = 0;

			double serverRam = GADriverDaas.hostListDAAS.get(server.intValue()).getRam().getCapacity();
			double serverCPU = GADriverDaas.hostListDAAS.get(server.intValue()).getTotalMipsCapacity();

			vmsCpuUtil = 0;
			vmsRAMUtil = 0;
			vmList.forEach(vm -> {

				if (GADriverDaas.vmListDAAS.stream().anyMatch(vm1 -> vm1.getId() == vm.intValue())) {
					GADriverDaas.vmListDAAS.forEach(Vm1 -> {
						if (Vm1.getId() == vm.intValue()) {
							vmsCpuUtil += Vm1.getCurrentRequestedTotalMips();
							vmsRAMUtil += Vm1.getRam().getAllocatedResource();//getRam().getAllocatedResource();getCurrentRequestedRam()
						}
					});
				}
			});

			serverCPUUtil = vmsCpuUtil / serverCPU;
			double serverRamUtil = 0;
			serverRamUtil = vmsRAMUtil / serverRam;

			double UpperUtilizationThreshold = GlobalController.HostUpperUtilizationThresholdDAAS;

			if ((serverCPUUtil > UpperUtilizationThreshold) || (serverRamUtil > UpperUtilizationThreshold)) {
				SLA_VIOLATIONS_UpperThreshold += 1;
			}

//			double serverUsage = (0.5 * serverCPUUtil) + (0.5 * serverRamUtil);

			serverUtil.put(server, serverCPUUtil); //To calculate power consumption we only need servers cpu utilization
		});
		/*
		 * map with server and it's power consumptions
		 */
		serverUtil.forEach((server, util) -> {
			double serverPowerConsump = 0;

			if (util > 1.0) {
				util = 1.0;
				serverPowerConsump = GADriverDaas.hostListDAAS.get(server.intValue()).getPowerModel()
						.getPower(util) + 50;
			} else if (util < 0.0) {
				util = 0.0;
				serverPowerConsump = GADriverDaas.hostListDAAS.get(server.intValue()).getPowerModel()
						.getPower(util);
			} else if (0.0 < util && util < 1.0) {
				serverPowerConsump = GADriverDaas.hostListDAAS.get(server.intValue()).getPowerModel()
						.getPower(util);
			}
			serverPower.put(server, serverPowerConsump);
		});
		datacenterPowerConsumption(serverPower);
		getFitness();
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
			dcMaxPower += GADriverDaas.hostListDAAS.get(server.intValue()).getPowerModel().getMaxPower();
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
	private double normalizeDataCenterPower(double datacenterPower, double datacenterMaxPower) {

		double normalizedDcPower = 0;
		double dcMinPower = 0;
		datacenterMaxPower = 8 * 303;
//		normalizedDcPower = datacenterPower / datacenterMaxPower;
		 normalizedDcPower = (datacenterMaxPower-datacenterPower)/(datacenterMaxPower-dcMinPower);
//		System.out.println(normalizedDcPower);

		return 1-normalizedDcPower;// 1-normalizedDcPower;
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
	private Map<Long, Boolean> ServerViolationMap;
	public double slaViolations() {
		SLA_VIOLATIONS_IN_CHROMOSOME = 0;
		ServerViolationMap = new HashMap<Long, Boolean>();
		ServerwithVmlist.forEach((Server, Vmlist) -> {
			AllvmsCpuCapacityRequirement = 0;
			AllvmsRAMCapacityRequirement = 0;

			double serverRamCapacity = GADriverDaas.hostListDAAS.get(Server.intValue()).getRam().getCapacity();
			double serverCPUCapacity = GADriverDaas.hostListDAAS.get(Server.intValue()).getTotalMipsCapacity();

			Vmlist.forEach(vm -> {
				if (GADriverDaas.vmListDAAS.stream().anyMatch(vm1 -> vm1.getId() == vm.intValue())) {
					GADriverDaas.vmListDAAS.forEach(Vm1 -> {
						if (Vm1.getId() == vm.intValue()) {
							AllvmsCpuCapacityRequirement += Vm1.getTotalMipsCapacity();
							AllvmsRAMCapacityRequirement += Vm1.getRam().getCapacity();
						}
					});
				}
			});

			if ((serverRamCapacity < AllvmsRAMCapacityRequirement) || (serverCPUCapacity < AllvmsCpuCapacityRequirement)) {
				SLA_VIOLATIONS_IN_CHROMOSOME += 1;
				ServerViolationMap.put(Server, true);
			} else {
				ServerViolationMap.put(Server, false);
			}
		});
		return SLA_VIOLATIONS_IN_CHROMOSOME;
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

		fitness = calculateFitness();
		return fitness;
	}

	int TotalNoOfMigrations;
	public int TotalNumberOfMigrations() {
		TotalNoOfMigrations = 0;
		Map<Long, Long> SourceMap = GADriverDaas.dynamicVmHostMap;

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

		return TotalNoOfMigrations;
	}

	public int ACTIVE_SERVERS;
	public double calculateFitness() {
		double chromosomeFitness = 0;
		chromosomeFitness = (0.1 * ((slaViolations()/ GADriverDaas.hostListDAAS.size()) + (SLA_VIOLATIONS_UpperThreshold / GADriverDaas.hostListDAAS.size())))
				+ 0.1 * (TotalNumberOfMigrations()/ GADriverDaas.sourcevmList.size()) + 0.6 * (ACTIVE_SERVERS / GADriverDaas.hostListDAAS.size())
				+ 0.2 * normalizeDataCenterPower(DC_POWER, dcMaxPower);
		return chromosomeFitness;
	}

}
