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
 */
public class DynamicChromosomeDAAS {
	private HashMap<Long, Long> genes;

	/*
	 * for each server, map it's id as a key and it's utilization as a value.
	 */
	private Map<Long, Double> serverUtil = new HashMap<Long, Double>();
	/*
	 * for each server, map it's id as a key and it's powerConsumption as a value.
	 */
	private Map<Long, Double> serverPower = new HashMap<Long, Double>();
	public Map<Long, ArrayList<Long>> ServerwithVmlist = new HashMap<Long, ArrayList<Long>>();
	private double DC_POWER;
	private double fitness = 0;
	Random rand = new Random();
	double vmsCpuUtil = 0;
	double vmsRAMUtil = 0;
	double serverCPUUtil = 0;
	private double dcMaxPower = 0;
	List<Long> VMID = new ArrayList<>();
	List<Long> serveridSV = new ArrayList<Long>();
	public int SLA_VIOLATIONS_UpperThreshold;
	public int SLA_VIOLATIONS_IN_CHROMOSOME = 0;
	double AllvmsCpuCapacityRequirement = 0;
	double AllvmsRAMCapacityRequirement = 0;
	private Map<Long, Boolean> ServerViolationMap;
	int TotalNoOfMigrations;
	public int ACTIVE_SERVERS;

	/**
	 * construct a chromosome based on the current allocation size
	 */
	public DynamicChromosomeDAAS() {
		genes = new HashMap<Long, Long>(GADriverDaas.dynamicVmHostMap.size());
	}

	/**
	 * method to initialize individual. Keeping the source map as a member in the
	 * population. Rest of the population is intialized by mutating the current population.
	 * 
	 * @param i
	 * @return
	 */
	public DynamicChromosomeDAAS initialize(int i) throws IllegalArgumentException {
	
		LocalControllerDaas.VmstoMigrateFromOverloadedUnderloadedHosts.forEach(vm -> {
			VMID.add(vm.getId());
		});

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
		return this;
	}

	private void fillgenesForOverloadedHosts(Long vm, Long server) {
		if(LocalControllerDaas.VmstoMigrateFromOverloadedHostsDAAS.stream().anyMatch(Vm -> (Vm.getId() == vm))) {
			genes.put(vm, GADriverDaas.targetHostList.get(rand.nextInt(GADriverDaas.targetHostList.size())));
		} else {
			genes.put(vm, server);
		}
	}

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
	 * @param genes
	 */
	public void serverVMMap(Map<Long, Long> genes) {

		/*
		 * a map containing the servers with the placed vmList on them
		 */
		ServerwithVmlist = new HashMap<>(genes.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue))
				.values().stream().collect(Collectors.toMap(item -> item.get(0).getValue(), item -> new ArrayList<Long>(
						item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));

		serverCurrentUtilization(ServerwithVmlist);
	}


	private void serverCurrentUtilization(Map<Long, ArrayList<Long>> NewServerwithVmlist) {
		SLA_VIOLATIONS_UpperThreshold = 0;
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

			serverUtil.put(server, serverCPUUtil);
		});

		serverUtil.forEach((server, util) -> {
			double serverPowerConsump = 0;

			if (util > 1.0) {
				util = 1.0;
				serverPowerConsump = GADriverDaas.hostListDAAS.get(server.intValue()).getPowerModel().getPower(util) + 50;
			} else if (util < 0.0) {
				util = 0.0;
				serverPowerConsump = GADriverDaas.hostListDAAS.get(server.intValue()).getPowerModel().getPower(util);
			} else if (0.0 < util && util < 1.0) {
				serverPowerConsump = GADriverDaas.hostListDAAS.get(server.intValue()).getPowerModel().getPower(util);
			}
			serverPower.put(server, serverPowerConsump);
		});
		datacenterPowerConsumption(serverPower);
		getFitness();
	};

	/**
	 * method to calculate datacenter power consumption
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
	 * objective function for datacenter power usage. We normalize the dc power with max min.
	 * 
	 * @param datacenterPower
	 * @return
	 */
	private double normalizeDataCenterPower(double datacenterPower, double datacenterMaxPower) {
		double normalizedDcPower = 0;
		double dcMinPower = 0;
		datacenterMaxPower = 8 * 303;

		normalizedDcPower = (datacenterMaxPower-datacenterPower)/(datacenterMaxPower-dcMinPower);
		return 1-normalizedDcPower;
	}

	/**
	 * objective function for sla violations; Considered SLAs: Resource availability and host upper utilization.
	 * @return
	 */
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
	
	public double calculateFitness() {	
		double normalisedSLA = (slaViolations() / GADriverDaas.hostListDAAS.size()) + (SLA_VIOLATIONS_UpperThreshold / GADriverDaas.hostListDAAS.size());
		double NormalisedTotalNoOfMigrations = TotalNumberOfMigrations()/ GADriverDaas.sourcevmList.size();
		double NormalisedNoOfActiveServers = ACTIVE_SERVERS / GADriverDaas.hostListDAAS.size();
		double NormalisedDatacenterPower = normalizeDataCenterPower(DC_POWER, dcMaxPower);
		
		double chromosomeFitness = (0.1 * normalisedSLA) + (0.1 * NormalisedTotalNoOfMigrations) + (0.6 * NormalisedNoOfActiveServers)
				+ (0.2 * NormalisedDatacenterPower);
		return chromosomeFitness;
	}

}
