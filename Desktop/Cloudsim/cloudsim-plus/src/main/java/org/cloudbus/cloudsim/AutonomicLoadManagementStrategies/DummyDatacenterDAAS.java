package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;

public class DummyDatacenterDAAS {
	private static final double  SCHEDULE_INTERVAL = 1; 
	/*  public DatacenterSimple createDatacenterDummy(CloudSim simulation) {
	        
	        allocationPolicy =
	                new VmAllocationPolicyMigrationBestFitStaticThreshold(
	                    new PowerVmSelectionPolicyMinimumUtilization(),
	                    HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION);
	        DatacenterSimple dcdummy = new DatacenterSimple(simulation,hostListDummyDC,new VmAllocationPolicySimple());//new VmAllocationPolicySimple());
	        dcdummy.getCharacteristics()
	            .setCostPerSecond(3.0)
	            .setCostPerMem(0.05)
	            .setCostPerStorage(0.1)
	            .setCostPerBw(0.1);
	dcdummy.enableMigrations();
	dcdummy.setSchedulingInterval(SCHEDULE_INTERVAL);

	        return dcdummy;
	    }*/
	  
	
}
