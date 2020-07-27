/**
 * 
 */
package centrlizedarchitecture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * @author Shyam Sundar V
 *
 */
public class GlobalUtilizationComputation {
	DatacenterSimple DC;
	double vmsCpuUtil;
	double vmsRAMUtil;
	double serverUsage;
	public Map<Long, Double> serverUtil;// = new HashMap<Long, Double>();
	public List<Host> hostList = new LinkedList<>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new GlobalUtilizationComputation();
	}

	public double GlobalUtilizationForNewPlacement(Map<Long, ArrayList<Long>> NewServerwithVmlist, List<Host> Hostlist,List<Vm> Vmlist) {
		serverUsage = 0;
		serverUtil = new HashMap<Long, Double>();
		NewServerwithVmlist.forEach((server,vmList) -> {
			double serverCPUUtil = 0;	
			double serverRamUtil = 0;
			double serverRam = Hostlist.get(server.intValue()).getRam().getCapacity();
			double serverCPU = Hostlist.get(server.intValue()).getTotalMipsCapacity();
			
			vmsCpuUtil = 0;
			vmsRAMUtil = 0;
			 vmList.forEach(vm -> {
				 vmsCpuUtil += Vmlist.get(vm.intValue()).getCurrentRequestedTotalMips();
				 vmsRAMUtil += Vmlist.get(vm.intValue()).getRam().getAllocatedResource(); // This method gives the current requested Ram not allocated.
			 });
			 serverCPUUtil = vmsCpuUtil / serverCPU;
			 serverRamUtil = vmsRAMUtil / serverRam;
			 
			 serverUsage = (0.5*serverCPUUtil)+(0.5*serverRamUtil);
			 serverUtil.put(server, serverUsage);
		});
		int NoofActiveServers= NewServerwithVmlist.entrySet().size();
		return serverUtil.values().stream().mapToDouble(i->i).sum() / NoofActiveServers;
	}
	
	
	public void SetHostlist(List<Host> HostList) {
		this.hostList = HostList;
	}
	
	public double GetGlobalUtilizationBasedOnCPUAndRAM() {
		
		double GlobalRamAndCPUUtilization = GetGlobalRamAndCPUUtilization();
		double GlobalUtilization=((GlobalRamAndCPUUtilization) / getNumberOfActiveHosts(hostList));
		return GlobalUtilization;
	}
	
	private double GetGlobalRamAndCPUUtilization(){
		double GlobalUtilization = 0.0;	
		double HostUtilization = 0.0;
		serverUtil = new HashMap<Long, Double>();
		for(Host host : hostList){
			if(!host.getVmList().isEmpty()){
				double CpuUtilization = 0.0;
				double RamUtilization = 0.0;
				CpuUtilization = host.getCpuPercentUtilization();//.getCpuMipsUtilization();
				double HostCapacity = host.getRam().getCapacity();	
				for(Vm vm : host.getVmList()) {
					RamUtilization += vm.getRam().getAllocatedResource(); // This method gives the current requested Ram not allocated.	
				}
			//	RamUtilization = host.getRam().getPercentUtilization();
				double RamutilizationPercentage = RamUtilization / HostCapacity;
			//	HostUtilization = ((CpuUtilization + RamUtilization) / 2);
				HostUtilization = (0.5 * CpuUtilization) + (0.5 * RamutilizationPercentage);
				serverUtil.put(host.getId(), HostUtilization);
			}
			GlobalUtilization += HostUtilization;
		}
		return GlobalUtilization;
	}
	
	/*
	 *Below method gets the individually the global cpu and ram utilization of hosts seperately and then calculates global utilization.
	 *so it is ignored for now. Can use those methods when cpu or ram global utilization is needed individually.
	 */
	
	public double GetGlobalUtilization() {
		double GlobalCpuUtilizationPercentage = GetGlobalCpuUtilizationPercentage();
		double GlobalRamUtilizationPercentage = GetGlobalRamUtilizationPercentage();
		double GlobalUtilization=((GlobalCpuUtilizationPercentage+GlobalRamUtilizationPercentage) / 2);
		return GlobalUtilization;
	}
	
	public double GetGlobalCpuUtilizationPercentage() {
		double GlobalCpuUtilizationPercentage = GetGlobalCpuUtilization() / getNumberOfActiveHosts(hostList);
		return GlobalCpuUtilizationPercentage;
	}
	
	public double GetGlobalRamUtilizationPercentage() {
		double GlobalRamUtilizationPercentage = GetGlobalRamUtilization() / getNumberOfActiveHosts(hostList);
		return GlobalRamUtilizationPercentage;
	}
	
	private double GetGlobalCpuUtilization(){
		double GlobalCpuUtilization = 0.0;
		for(Host host : hostList){
			if(!host.getVmList().isEmpty()) {//host.isActive()==true){
				GlobalCpuUtilization += host.getCpuMipsUtilization();
			}
		}
		return GlobalCpuUtilization;
	}
	
	
	private double GetGlobalRamUtilization(){
		double GlobalRamUtilization = 0.0;
		for(Host host : hostList){
			if(!host.getVmList().isEmpty()) {
				double RamUtilization = 0;
				double HostCapacity = host.getRam().getCapacity();	
				for(Vm vm : host.getVmList()) {
					RamUtilization += vm.getRam().getAllocatedResource(); // This method gives the current requested Ram not allocated.	
				}
			//	RamUtilization = host.getRam().getPercentUtilization();
				double RamutilizationPercentage = RamUtilization / HostCapacity;
			
				GlobalRamUtilization += RamutilizationPercentage;
			}
		}
		return GlobalRamUtilization;
	}
	
	private int  getNumberOfActiveHosts(List<Host> hostList){
		int NumberofActiveHosts = 0;
		for(Host host : hostList){
			if(!host.getVmList().isEmpty()){
				NumberofActiveHosts += 1;
			}
		}
		return NumberofActiveHosts;
	}
}
