/**
 * 
 */
package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import java.util.LinkedList;
import java.util.List;
import java.util.OptionalDouble;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudsimplus.listeners.EventInfo;

/**
 * @author Shyam Sundar V
 *
 */
public class CentralManager {
	double RamUtilization;
	double CpuUtilization;
	VmAllocationPolicy allocationPolicy;
	public List<Host> hostList = new LinkedList<>();
	CloudSim Simulation;
	DatacenterSimple DC;
	
	public CentralManager(DatacenterSimple dc,CloudSim simulation) 
	{
		this.allocationPolicy=dc.getVmAllocationPolicy();
		this.Simulation=simulation;
		this.DC=dc;
		simulation.addOnClockTickListener(this :: clockTickListener);		
	}
	
	private double Utilization_Threshold_High=0.9;
	private double Utilization_Threshold_Low=0.25;
	
	private void clockTickListener(EventInfo info)
	{
	      final int time = (int)info.getTime();
	      if(time!=0) 
	      {
	    	  hostList=DC.getHostList();
	    	  int NoOfActiveHosts=getNumberOfActiveHosts(hostList);
	    	  double GlobalUtilizationCpu=GetGlobalCpuUtilization();
	    	  double GlobalUtilizationRam=GetGlobalRamUtilization();
	    	  
	    	  if(((NoOfActiveHosts*Utilization_Threshold_High) <= GlobalUtilizationCpu) ||  ((NoOfActiveHosts*Utilization_Threshold_Low) >= GlobalUtilizationCpu))
	    	  {
	    		  //load balance
	    		  //select source host and target host for migration i.e there in allocationpolicymigrationthreshold
	    		  
	    	  }
	    	  
	    	  if(((NoOfActiveHosts*Utilization_Threshold_High) <= GlobalUtilizationRam) ||  ((NoOfActiveHosts*Utilization_Threshold_Low) >= GlobalUtilizationRam))
	    	  {
	    		  //load balance
	    	  }
	    	  NumberofActiveHosts=0;
	    	  GlobalCpuUtilization=0.0;
	    	  GlobalRamUtilization=0.0;
	 
	      }
	 }

	
	private int NumberofActiveHosts=0;
	private int  getNumberOfActiveHosts(List<Host> hostList)
	{
		for(Host host : hostList)
		{
			if(host.isActive()==true)
			{
				NumberofActiveHosts=+1;
			}
		}
		return NumberofActiveHosts;
	}
	
	double GlobalCpuUtilization=0.0;
	private double GetGlobalCpuUtilization()
	{
		for(Host host : hostList)
		{
			if(host.isActive()==true)
			{
				GlobalCpuUtilization=+host.getCpuMipsUtilization();
			}
		}
		return GlobalCpuUtilization;
	}
	
	double GlobalRamUtilization=0.0;
	private double GetGlobalRamUtilization()
	{
		for(Host host : hostList)
		{
			if(host.isActive()==true)
			{
				GlobalRamUtilization=+host.getRam().getPercentUtilization();
			}
		}
		return GlobalRamUtilization;
	}
}
