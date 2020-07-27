/**
 * 
 */
package centrlizedarchitecture;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * @author Shyam Sundar V
 *
 */
public class VmSelectionPolicyMaxAverageCPURAM {
	public List<Vm> VmsToMigrate = new ArrayList<Vm>();
	public List<Vm> VmsToMigrateFromOverloadedHosts = new ArrayList<Vm>();
	private Set<Host> overloadedHosts;
	public Map<Host, Vm> SelectiionPolicyserverVmsmap;
	/**
	 * A map containing migratable vm as key and it's relative cpu percent to the host as value
	 */
	Map<Vm, Double> vmsCpuUtil = new HashMap<Vm, Double>();
	
	/**
	 * A map containing migratable vm as key and it's relative ram percent to the host as value
	 */
	Map<Vm, Double> vmsRamUtil = new HashMap<Vm, Double>();
	/**
	 * This method selects the virtual machines to migrate from the overloaded and underloaded hosts and adds them to the VmsToMigrate set.
	 * It takes the utilization values of cpu to identify the ideal vm to migrate.
	 * 
	 * @param ApplicationHostList
	 * @param OverloadedhostList
	 * @param UnderloadedhostList
	 */
	public void vmSelection(List<Host> ApplicationHostList, Set<Host> OverloadedhostList,Set<Host> UnderloadedhostList,
			Map<Vm, Map<Integer, Long>> WAVmsRamUtilizationHistory, double HostUpperUtilizationThreshold, double HostLowerUtilizationThreshold, double time ) {
		overloadedHosts = new HashSet<>();
		VmsToMigrate = new ArrayList<>();
		VmsToMigrateFromOverloadedHosts = new ArrayList<>();
		SelectiionPolicyserverVmsmap = new HashMap<Host, Vm>();
		if (!UnderloadedhostList.isEmpty()) {
			for (Host host : UnderloadedhostList) {
				for(Vm vm : host.getVmList()){
					if(!vm.isInMigration()) {
						VmsToMigrate.add(vm);
					}
				}
			}
		}
		if (!OverloadedhostList.isEmpty()) {
			overloadedHosts.addAll(OverloadedhostList);
		}

		for (Host host : overloadedHosts) {

			double HostCapacity = host.getRam().getCapacity();
		  	double RamUtilisation =0;
			for(Vm vm : host.getVmList()) {
				RamUtilisation += vm.getRam().getAllocatedResource(); // This method gives the current requested Ram not allocated.	
			}
			double RamUtilisationPercentage = RamUtilisation / HostCapacity;

			/*
			 * If a host is overloaded with cpu resource it can still show underloaded with
			 * ram resource in that case, only the overloaded if loop will run and return a
			 * vm.
			 */
			Here:
			if (RamUtilisationPercentage >= HostUpperUtilizationThreshold) {
				
				final List<? extends Vm> migratablevms = host.getMigratableVms();
				if(migratablevms.size() == 1) {
					VmsToMigrate.add(migratablevms.get(0));
					VmsToMigrateFromOverloadedHosts.add(migratablevms.get(0));
					SelectiionPolicyserverVmsmap.put(host, migratablevms.get(0));
					break Here;
				}
				
				vmsRamUtil = new HashMap<Vm, Double>();
				for(Vm vm : host.getVmList()) {
					if((!vm.isInMigration()) && (WAVmsRamUtilizationHistory.get(vm).entrySet().size() >= 5)) {
						vmsRamUtil.put(vm, calculateVmRelativeRam(vm, time, WAVmsRamUtilizationHistory));	
					}
				}
				if(!vmsRamUtil.isEmpty()){
					Vm SelectedVm = vmsRamUtil.entrySet().stream()
							.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).findFirst().get().getKey(); 	
					VmsToMigrate.add(SelectedVm);
					VmsToMigrateFromOverloadedHosts.add(SelectedVm);
					SelectiionPolicyserverVmsmap.put(host, SelectedVm);
				}else {
					final List<? extends Vm> migratableVms = host.getMigratableVms();
					if(migratableVms.size() == 1) {
						VmsToMigrate.add(migratableVms.get(0));
						VmsToMigrateFromOverloadedHosts.add(migratableVms.get(0));
						SelectiionPolicyserverVmsmap.put(host, migratableVms.get(0));
						break Here;
					}
					final Predicate<Vm> inMigration = Vm::isInMigration;
					final Comparator<? super Vm> ramUsageComparator =
							Comparator.comparingDouble(vm -> vm.getHostRamUtilization());
					final Optional<? extends Vm> optional = migratableVms.stream()
				                                                             .filter(inMigration.negate())
				                                                             .min(ramUsageComparator);
					if (optional.isPresent()) {
						VmsToMigrate.add(optional.get());
						VmsToMigrateFromOverloadedHosts.add(optional.get());
						SelectiionPolicyserverVmsmap.put(host, optional.get());
					}   
				}				
			} else {//(HostCpuUtilization >= HostUpperUtilizationThreshold) {
				vmsCpuUtil = new HashMap<Vm, Double>();
				for(Vm vm : host.getVmList()) {
					if((!vm.isInMigration()) && (WAVmsRamUtilizationHistory.get(vm).entrySet().size() >= 5)) {	
						vmsCpuUtil.put(vm, calculateVmRelativeCpu(vm, time));	
					}
				}
				
				if(!vmsCpuUtil.isEmpty()){
					Vm SelectedVm = vmsCpuUtil.entrySet().stream()
							.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).findFirst().get().getKey(); 	
					VmsToMigrate.add(SelectedVm);
					VmsToMigrateFromOverloadedHosts.add(SelectedVm);
					SelectiionPolicyserverVmsmap.put(host, SelectedVm);
				}else {
					final List<? extends Vm> migratableVms = host.getMigratableVms();
					final Predicate<Vm> inMigration = Vm::isInMigration;
					final Comparator<? super Vm> cpuUsageComparator =
							Comparator.comparingDouble(vm -> vm.getCpuPercentUtilization(vm.getSimulation().clock()));
					final Optional<? extends Vm> optional = migratableVms.stream()
				                                                             .filter(inMigration.negate())
				                                                             .min(cpuUsageComparator);
					if (optional.isPresent()) {
						VmsToMigrate.add(optional.get());
						VmsToMigrateFromOverloadedHosts.add(optional.get());
						SelectiionPolicyserverVmsmap.put(host, optional.get());
					}   
				}				
			}
		}	
	}
	/**
	 * For each running vm on host, calculate relative cpu utilization by
	 * calculating the mean of the current and previous cpu utilizations of vm. .
	 * @param vm
	 * @return relative cpu util
	 */
	private double calculateVmRelativeCpu(Vm vm, double time) {
		TreeMap<Double, Double> vmCpuHistory = new TreeMap<Double, Double>();
	
		vmCpuHistory.put(time, vm.getHostCpuUtilization(time));
		vmCpuHistory.put(time-1, vm.getHostCpuUtilization(time-1));
		vmCpuHistory.put(time-2, vm.getHostCpuUtilization(time-2));
		vmCpuHistory.put(time-3, vm.getHostCpuUtilization(time-3));
		
	//	Collection<Double>  vmCpuUtilTime = new ArrayList<Double>();
		
		/**
		 * An array containing the relative cpu utilizations of Vm to host.
		 * This way when the vm scaled up/down,  we can calculate the modified utilization of 
		 * underlying host.
		 */
		List<Double>  vmCpuUtil = new ArrayList<Double>();
		
		for(Map.Entry<Double, Double> m: vmCpuHistory.entrySet()){    
			
		//	vmCpuUtilTime.add(m.getKey());
			vmCpuUtil.add(m.getValue());
			 
		}  
//		System.out.println(vmCpuUtil);
//		System.out.printf("vmId: %d;<> vmCpuUtil: %f\n",vm.getId(), MathUtil.mean(vmCpuUtil));  	
	return	MathUtil.mean(vmCpuUtil);
	}
	
	private double calculateVmRelativeRam(Vm vm, double time, Map<Vm, Map<Integer, Long>> VmsRamUtilizationHistory) {
	TreeMap<Double, Long> vmRamHistory = new TreeMap<Double, Long>();
		
		final Map<Integer, Long> vmRamUtilization = VmsRamUtilizationHistory.get(vm);
		
		vmRamHistory.put(time, vmRamUtilization.get((int) time));
		Object lastKey = vmRamUtilization.keySet().toArray()[vmRamUtilization.size()-2];
		long utilvalue = vmRamUtilization.get(lastKey);
		vmRamHistory.put(time-(1*300), utilvalue);
		lastKey = vmRamUtilization.keySet().toArray()[vmRamUtilization.size()-3];
		utilvalue = vmRamUtilization.get(lastKey);
		vmRamHistory.put(time-(2*300), utilvalue);
		lastKey = vmRamUtilization.keySet().toArray()[vmRamUtilization.size()-4];
		utilvalue = vmRamUtilization.get(lastKey);
		vmRamHistory.put(time-(3*300), utilvalue);

		/**
		 * An array containing the relative cpu utilizations of Vm to host.
		 * This way when the vm scaled up/down,  we can calculate the modified utilization of 
		 * underlying host.
		 */
		List<Double>  vmRamUtil = new ArrayList<Double>();
		
		for(Map.Entry<Double, Long> m: vmRamHistory.entrySet()){    
			vmRamUtil.add(m.getValue().doubleValue());
		} 
		return	MathUtil.mean(vmRamUtil);
	}
}
