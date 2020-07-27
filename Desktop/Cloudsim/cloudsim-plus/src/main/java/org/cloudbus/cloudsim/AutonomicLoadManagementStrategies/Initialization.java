package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import static java.util.Comparator.comparingDouble;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * This class is dedicated for initializing models by creating different types of {@link Host}, {@link Vm} and {@link Cloudlet}
 * 
 * @param vmList
 * @param hostList
 * @param cloudletList
 * 
 * @see Initialization#createVm(int numberVm, int type) 
 * @see Initialization#getVmList() 
 * 
 * @see Initialization#createCloudlets(int numberCloudlets, int type)
 * @see Initialization#getCloudletList() 
 * @see Initialization#getAscendingCloudletList() 
 * @see Initialization#getDescendingCloudletList() 
 * 
 * @author Abdulrahman Nahhas
 * @since CloudSim Plus 1.0
 */

public class Initialization 
{
	List<Vm> vmList = new ArrayList<>();
	List<Host> hostList = new ArrayList<>();
	List<Cloudlet> cloudletList = new ArrayList<>();
    
	/**
     * Starts the Initialization of the simulation model.
     * @param args
     */
    public static void main(String[] args) 
    {
        new Initialization();
    }
    
    /**
     * Create Virtual mahcines: This method can be used to create different virtual machines using predefined types
     * 
     * @param numberVm  the number of virtual machines to be created
     * @param type of which the virtual machines will be created
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public void createVm(int numberVm, int type) 
    {
    	final long   mips = 1000, storage = 10000, bw = 1000;
    	int    ram = 0;
    	long   pesNumber = 0;
    	
    	switch (type)
    	{
    		case 1:
    			ram = 512; // vm memory (MEGABYTE)
    			pesNumber = 1; // number of CPU cores
    			break;
    		case 2:
     	       	ram = 1024; // vm memory (MEGABYTE)
     	       	pesNumber = 2; // number of CPU cores
     	       	break;
    		case 3:
     	       	ram = 2048; // vm memory (MEGABYTE)
     	       	pesNumber = 4; // number of CPU cores
     	       	break;
    		case 4:
    			ram = 4096; // vm memory (MEGABYTE)
    			pesNumber = 8; // number of CPU cores
     	       	break;
     		case 5:
     	       	ram = 8192; // vm memory (MEGABYTE)
     	       	pesNumber = 16; // number of CPU cores
     	       	break;
    	}  
        
    	for (int i = 0; i < numberVm; i++)
        {
    		int VmID = vmList.size();
        	Vm vm =
        			new VmSimple(VmID, mips, pesNumber)
                    .setRam(ram).setBw(bw).setSize(storage)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());
        	vm.getUtilizationHistory().enable(); // Remove this line for Complex models
        	vmList.add(VmID, vm);
        	
        }
    }
    
    
    /**
     * Gets the create virtual machines in a list
     * @return {@link #vmList}
     * @see #createVm(int, int)
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public List<Vm> getVmList() 
    {
    	return vmList;
    }
    
    /**
     * Create Hosts: This method can be used to create different hosts using predefined types
     * 
     * @param numberHosts  the number of hosts to be created
     * @param type of which the hosts will be created
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public void createHosts(int numberHosts, int type) 
    {
    	final double MAX_POWER = 100;
    	final double STATIC_POWER_PERCENT = 0.7;
    	final PowerModel powerModel = new PowerModelLinear(MAX_POWER, STATIC_POWER_PERCENT);
    	final long   mips = 1000, //host Instruction Pre Second (Million)
    				 storage = 100000, //host storage (MEGABYTE)
    				 bw = 10000; //host storage (MEGABYTE)
      	
    	int    ram = 0; // Initial declaration of the ram capacity 
    	long   pesNumber = 0; // Initial declaration of the pesNumber
    	
    	// create Hosts with different ram and pes capacity based on the types --> The types might be extended and those information  might be read from a file
    	
    	switch (type) 
    	{
    		case 1:
    			ram = 32768; // host memory (MEGABYTE)
    			pesNumber = 16; // number of CPU cores
    			break;
    		case 2:
    			ram = 65536; // host memory (MEGABYTE)
     	       	pesNumber = 32; // number of CPU cores
     	       	break;
    		case 3:
    			ram = 131072; // host memory (MEGABYTE)
     	       	pesNumber = 64; // number of CPU cores
     	       	break;
    	}
    	
        // Create Hosts with its id and list of PEs and add them to the list of machines
       
        for(int i = 0; i < numberHosts; i++)
        {
            List<Pe> peList = new ArrayList<>();
            long mipsPe = 1000;
            for(int j = 0; j < pesNumber; j++)
            {
            	peList.add(new PeSimple(mipsPe, new PeProvisionerSimple()));
            }
            
        	Host host = new HostSimple(ram, bw, storage, peList)
        		.setRamProvisioner(new ResourceProvisionerSimple())
        		.setBwProvisioner(new ResourceProvisionerSimple())
        		.setVmScheduler(new VmSchedulerTimeShared());
        	host.setId(hostList.size());
        	host.setPowerModel(powerModel);
        	hostList.add(host);
        }        
    }
    
    /**
     * Gets the created hosts in a list
     * @return {@link #hostList}
     * @see #createHosts(int, int)
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public List<Host> getHostsList() 
    {
    	return hostList;
    }

    
    /**
     * Create Cloudlets: This method can be used to create different Cloudlets using predefined types
     * 
     * @param numberCloudlets the number of hosts to be created
     * @param type of which the hosts will be created
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
  
    public void createCloudlets(int numberCloudlets, int type) 
    {
    	
        //cloudlet parameters
        long fileSize = 300; 
        long outputSize = 300; 
        long length = 0; // Initial declaration of the length
        int pesNumber = 0; // Initial declaration of the pesNumber
        
        UtilizationModel utilizationModel = new UtilizationModelFull();
        
        
        // create Cloudlets with different lengths based on the types --> The types might be extended and the lengthes of the cloudlets might be read from a file
        
        switch (type)
    	{
    		case 1:
    			length = 10000; 
    			pesNumber = 1; 
    			break;
    		case 2:
    			length = 50000; 
    			pesNumber = 1;
     	       	break;
    		case 3:
    			length = 100000; 
    			pesNumber = 2;
     	       break;
    		case 4:
    			length = 500000; 
    			pesNumber = 2;
     	       	break;
     		case 5:
    			length = 1000000; 
    			pesNumber = 2;
    			break;
    	}
        
        for (int i = 0; i < numberCloudlets; i++) 
        {
            int LetID = cloudletList.size();
            // For generating random length
//            Random r = new Random();
//            length = length + r.nextInt(50);
            Cloudlet cloudlet = new CloudletSimple(LetID, length, pesNumber)
                            .setFileSize(fileSize)
                            .setOutputSize(outputSize)
                            .setUtilizationModel(utilizationModel);
            cloudletList.add(cloudlet);
        }
    }
    
    /**
     * Gets the created Cloudlets in a list
     * @return {@link #cloudletList}
     * @see #createCloudlets(int, int)
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public List<Cloudlet> getCloudletList() 
    {
    	return cloudletList;
    }
    
    /**
     * Gets the created Cloudlets in an Ascending order in a list
     * @return {@link #cloudletList}
     * @see #createCloudlets(int, int)
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public List<Cloudlet> getAscendingCloudletList() 
    {    	
    	Comparator<Cloudlet> sortCloudLet = Comparator.comparingLong(Cloudlet::getLength);
    	cloudletList.sort(sortCloudLet);
    	return cloudletList;
    }
    
    /**
     * Gets the created Cloudlets in an Descending order in a list
     * @return {@link #cloudletList}
     * @see #createCloudlets(int, int)
     * @author Abdulrahman Nahhas
     * @since CloudSim Plus 1.0
     */
    
    public List<Cloudlet> getDescendingCloudletList() 
    {    	
    	Comparator<Cloudlet> sortCloudLet = Comparator.comparingLong(Cloudlet::getLength).reversed();
    	cloudletList.sort(sortCloudLet);
    	return cloudletList;
    }
}
