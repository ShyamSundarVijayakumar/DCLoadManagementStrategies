/**
 * 
 */
package hierarchicalarchitecture.globalcontroller;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.selectionpolicies.VmSelectionPolicy;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * @author Shyam Sundar V
 *
 */
public class VmSelectionPolicyCpuAndRamBased implements VmSelectionPolicy{
	@Override
	public Vm getVmToMigrate(final Host host) {
		final List<Vm> migratableVms = host.getMigratableVms();
		if (migratableVms.isEmpty()) {
			return Vm.NULL;
		}
	
		String Applicationtype = host.getDescription();
		if((Applicationtype == "1 Desktop as a service") || (Applicationtype == "2 Desktop as a service" ) || 
					(Applicationtype == "3 Desktop as a service" ) || (Applicationtype == "4 Desktop as a service" ) ||
					(Applicationtype == "5 Desktop as a service" )){
			Map<Host, Vm> SelectiionPolicyserverVmsmapDAAS = GlobalController.SelectiionPolicyserverVmsmapDAAS;
			if(SelectiionPolicyserverVmsmapDAAS.containsKey(host)) {
				return SelectiionPolicyserverVmsmapDAAS.get(host);
			}
		}
		
		else if(Applicationtype == "Web_Application" ){
			Map<Host, Vm> SelectiionPolicyserverVmsmapWA = GlobalController.SelectiionPolicyserverVmsmapWA;
			if(SelectiionPolicyserverVmsmapWA.containsKey(host)) {	
				return SelectiionPolicyserverVmsmapWA.get(host);
			}
		}

		else if(Applicationtype == "Batch processing" ){
			Map<Host, Vm> SelectiionPolicyserverVmsmapBP = GlobalController.SelectiionPolicyserverVmsmapBP;
			if(SelectiionPolicyserverVmsmapBP.containsKey(host)) {
				return SelectiionPolicyserverVmsmapBP.get(host);
			}
		}
		
		return Vm.NULL;
		
	/*	VmAllocationPolicyMigration allocationPolicy = (VmAllocationPolicyMigration) host.getDatacenter().getVmAllocationPolicy();
	        double overUtilizationThreshold = allocationPolicy.getOverUtilizationThreshold(host);
	        double underUtilizationThreshold = allocationPolicy.getUnderUtilizationThreshold();
		        
	        double HostCapacity=host.getRam().getCapacity();
	    	double RamUtilisation=host.getRamUtilization();
	    	double RamUtilisationPercentage= RamUtilisation/HostCapacity;
	        double HostCpuUtilization = host.getCpuPercentUtilization();
	   
	    *If a host is overloaded with cpu resource it can still show underloaded with ram resource in that case,
	    *only the overloaded if loop will run and return a vm. 
	         
	        if(RamUtilisationPercentage >= overUtilizationThreshold) {
	        	final Predicate<Vm> inMigration = Vm::isInMigration;
	        	final Comparator<? super Vm> RamUsageComparator =
	        			Comparator.comparingDouble(vm -> vm.getHostRamUtilization());
	        	final Optional<? extends Vm> optional = migratableVms.stream()
	        			.filter(inMigration.negate())
	        			.max(RamUsageComparator);
	        	return optional.isPresent() ? optional.get() : Vm.NULL;
	        }
	        else if(HostCpuUtilization >= overUtilizationThreshold) {
	        	final Predicate<Vm> inMigration = Vm::isInMigration;
	        	final Comparator<? super Vm> cpuUsageComparator =
	        			Comparator.comparingDouble(vm -> vm.getHostCpuUtilization());//getCpuPercentUtilization(vm.getSimulation().clock())
	        	final Optional<? extends Vm> optional = migratableVms.stream()
	        			.filter(inMigration.negate())
	        			.max(cpuUsageComparator);
	        	return optional.isPresent() ? optional.get() : Vm.NULL;
	        }

	        else if((HostCpuUtilization <= underUtilizationThreshold)||(RamUtilisationPercentage <= underUtilizationThreshold)){
	        	final Predicate<Vm> inMigration = Vm::isInMigration;
        	final Comparator<? super Vm> cpuUsageComparator =
        			Comparator.comparingDouble(vm -> vm.getHostCpuUtilization());
        	final Optional<? extends Vm> optional = migratableVms.stream()
        			.filter(inMigration.negate())
        			.min(cpuUsageComparator);
        	return optional.isPresent() ? optional.get() : Vm.NULL;
	        }
	        
	        return Vm.NULL;
*/	}
}
