package hierarchicalarchitecture.localcontrollerdaas;

import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * This class is dedicated for bringing in the host classifier module functionality.
 * @author Shyam Sundar V
 */
public class HostClassifierDAAS {
	private double HostUpperUtilizationThreshold = 0.0;
	private double HostLowerUtilizationThreshold = 0.0;
	
	public void SetHostUpperAndLowerUtilizationThreshold(double UpperThreshold, double LowerThreshold) {
	   this.HostUpperUtilizationThreshold = UpperThreshold;
	   this.HostLowerUtilizationThreshold = LowerThreshold;
   }
	
	 /**
     * Gets the List of overloaded hosts.
     * If a Host is overloaded but it has VMs migrating out,
     * then it's not included in the returned List
     * because the VMs to be migrated to move the Host from
     * the overload state already are in migration.
     *
     * @param list of host that has to be classified
     * @return the over utilized hosts
     */
   public Set<Host> getOverloadedHosts(List<Host> hostListToClassify) {
        return hostListToClassify.stream()
            .filter(this::isHostOverloaded)
            .filter(host -> host.getVmsMigratingOut().isEmpty())
            .collect(toSet());
    }	
	
   /**
    * {@inheritDoc}
    * It's based on current CPU usage.
    *
    * @param host {@inheritDoc}
    * @return {@inheritDoc}
    */
   private boolean isHostOverloaded(final Host host) {
       return isHostOverloaded(host, host.getCpuPercentUtilization());
   }
   
   /**
    * Checks if a Host is overloaded based on the given CPU utilization percent.
    * @param host the Host to check
    * @param cpuUsagePercent the Host's CPU utilization percent. The values may be:
    *                        <ul>
    *                          <li>the current CPU utilization if you want to check if the Host is overloaded right now;</li>
    *                          <li>the requested CPU utilization after temporarily placing a VM into the Host
    *                          just to check if it supports that VM without being overloaded.
    *                          In this case, if the Host doesn't support the already placed temporary VM,
    *                          the method will return true to indicate the Host will be overloaded
    *                          if the VM is actually placed into it.
    *                          </li>
    *                        </ul>
    * @return true if the Host is overloaded, false otherwise
    */
   
   private boolean isHostOverloaded(final Host host, final double cpuUsagePercent){
   	double OverUtilizationThreshold = this.HostUpperUtilizationThreshold;
   	double HostCapacity = host.getRam().getCapacity();
   	double RamUtilisation = 0;
   	for(Vm vm: host.getVmList()) { 
   		RamUtilisation += vm.getRam().getAllocatedResource(); // This method gives the current requested Ram not allocated.
   	}
   	double RamUtilisationPercentage = RamUtilisation/HostCapacity;
   	
   	if((cpuUsagePercent == 100) || (RamUtilisationPercentage == 100)) {
   		System.out.println("Host CPU or ram has reached 100% utilization"+ host.getId());
   	}
   	return ((cpuUsagePercent > OverUtilizationThreshold) || (RamUtilisationPercentage > OverUtilizationThreshold));
   }
 
   
	  /**
     * Gets the most underloaded Hostlist.
     * If a Host is underloaded but it has VMs migrating in,
     * then it's not included in the returned List
     * because the VMs to be migrated to move the Host from
     * the underload state already are in migration to it.
     * Likewise, if all VMs are migrating out, nothing has to be
     * done anymore. It just has to wait the VMs to finish
     * the migration.
     *
     * @param hostListToClassify : the list of host's that has to be classified
     * @return the most under utilized host or {@link Host#NULL} if no Host is found
     */
   
    public Set<Host> getUnderLoadedHosts(List<Host> hostListToClassify) {
        return hostListToClassify.stream()
        		.filter(Host -> !isHostOverloaded(Host, Host.getCpuPercentUtilization()))
        		.filter(host -> host.getVmsMigratingIn().isEmpty())
                .filter(this::notAllVmsAreMigratingOut)
        		.filter(Host ->isHostUnderloaded(Host))
        		.collect(toSet());
    }
	
	/**
     * Checks if a host is under utilized, based on current CPU usage.
     *
     * @param host the host
     * @return true, if the host is under utilized; false otherwise
     */
    private boolean isHostUnderloaded(final Host host) {   
    	double UnderUtilizationThreshold = HostLowerUtilizationThreshold;
    	double CpuUtilisationPercentage=getHostCpuPercentRequested(host);
    	double HostCapacity=host.getRam().getCapacity();
       	double RamUtilisation = 0;
       	for(Vm vm: host.getVmList()) { 
       		RamUtilisation += vm.getRam().getAllocatedResource(); // This method gives the current requested Ram not allocated.
       	}
       	double RamUtilisationPercentage = RamUtilisation/HostCapacity;

       	return ((CpuUtilisationPercentage < UnderUtilizationThreshold) || (RamUtilisationPercentage < UnderUtilizationThreshold));
    }
    
    private double getHostCpuPercentRequested(final Host host) {
        return getHostTotalRequestedMips(host) / host.getTotalMipsCapacity();
    }

    /**
     * Gets the total MIPS that is currently being used by all VMs inside the Host.
     * @param host
     * @return
     */
    private double getHostTotalRequestedMips(final Host host) {
        return host.getVmList().stream()
            .mapToDouble(Vm::getCurrentRequestedTotalMips)
            .sum();
    }
	

    /**
     * Checks if all VMs of a Host are <b>NOT</b> migrating out.
     * In this case, the given Host will not be selected as an underloaded Host at the current moment.
     * That is: not all VMs are migrating out if at least one VM isn't in migration process.
     *
     * @param host the host to check
     * @return true if at least one VM isn't migrating, false if all VMs are migrating
     */
    private boolean notAllVmsAreMigratingOut(final Host host) {
        return host.getVmList().stream().anyMatch(vm -> !vm.isInMigration());
    }
    
}
