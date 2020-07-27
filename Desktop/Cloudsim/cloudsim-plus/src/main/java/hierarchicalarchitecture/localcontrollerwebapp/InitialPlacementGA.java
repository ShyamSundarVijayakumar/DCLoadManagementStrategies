package hierarchicalarchitecture.localcontrollerwebapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyAbstract;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import hierarchicalarchitecture.localcontrollerdaas.LocalControllerDaas;

	/**
	 * @author Shyam Sundar V
	 *
	 */

	public class InitialPlacementGA extends VmAllocationPolicyAbstract{
		
		List<Vm> intialVms;
		Map<Integer, ArrayList<Integer>> serverwithVMList;
		/*
		 * A map contains optimized placement of vms in hosts. 
		 */

		Map<Integer, Integer> vmToHostMapDaas = LocalControllerDaas.vmToHostMapInitialPlacement;
		private Map<Integer, Integer> vmToHostMapWebApp = LocalControllerWA.vmToHostMapInitialPlacement;
		int hostId;
		List<Host> hostListWebApplication;
		List<Host> hostListDaas;
		Host hostToPlaceVm;
		@Override
		protected Optional<Host> defaultFindHostForVm(Vm vm) {
		
		int vmId;
		String Applicationtype = vm.getDescription();
		if(Applicationtype == "Web_Application" ) {
			vmId = (int)vm.getId();
			hostId = vmToHostMapWebApp.get(vmId);
			hostListWebApplication = getHostList().stream().
					filter(host -> host.getDescription() == "Hosts from Webapplication cluster").collect(Collectors.toList());
			hostToPlaceVm = hostListWebApplication.get(hostId);
		}
		else if(Applicationtype == "1 Desktop as a service" || (Applicationtype == "2 Desktop as a service" ) || 
				(Applicationtype == "3 Desktop as a service" ) || (Applicationtype == "4 Desktop as a service" ) ||
				(Applicationtype == "5 Desktop as a service" )) {
			vmId = (int)vm.getId();
		
			hostListDaas = getHostList().stream().
					filter(host -> host.getDescription() == "Hosts from Desktop as a Service").collect(Collectors.toList());		
			if(getDatacenter().getSimulation().clock() > 1000) {
				vmToHostMapDaas = LocalControllerDaas.vmToHostMapInitialPlacement;
				hostId = vmToHostMapDaas.get(vmId);
				hostToPlaceVm = hostListDaas.get(hostId);
			}else {
				hostId = vmToHostMapDaas.get(vmId);
				hostToPlaceVm = hostListDaas.get(hostId);
			}
		}
		
		if(hostToPlaceVm.isSuitableForVm(vm)) {			
			return Optional.of(hostToPlaceVm);
		}
		
		return Optional.empty();
	
		}
		
	}