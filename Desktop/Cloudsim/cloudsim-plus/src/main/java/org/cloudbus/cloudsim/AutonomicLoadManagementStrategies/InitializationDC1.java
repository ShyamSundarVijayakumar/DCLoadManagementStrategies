package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
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
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReader;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelPlanetLab;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

/**
 * This class is dedicated for initializing models by creating different types of {@link Host}, {@link Vm} and {@link Cloudlet}
 * 
 * @param vmListDC1
 * @param hostListDC1
 * @param cloudletListDC1
 * 
 * @see InitializationDC1#createHostsDC1(int, int)
 * @see InitializationDC1#getHostsListDC1()
 * @see InitializationDC1#createVmDC1(int numberVm, int type) 
 * @see InitializationDC1#getVmListDC1() 
 * 
 * @see InitializationDC1#createCloudletsFromWorkloadFile(DatacenterBroker)
 * @see InitializationDC1#getCloudletListDC1() 
 * @see InitializationDC1#getAscendingCloudletListDC1()
 * @see InitializationDC1#getDescendingCloudletListDC1() 
 * 
 * @author Shyam Sundar V
 * @since CloudSim Plus 1.0
 */
public class InitializationDC1 {
	List<Vm> vmListDC1 = new ArrayList<>();
	List<Host> hostListDC1 = new ArrayList<>();
	List<Cloudlet> cloudletListDC1 = new ArrayList<>();
    
	/**
     * Starts the Initialization of the simulation model.
     * @param args
     */
    public static void main(String[] args) 
    {
        new InitializationDC1();
    }
 

    /**
     * Create HostsDC1: This method can be used to create different hosts for data center 1 using predefined types
     * 
     * @param numberHosts  the number of hosts to be created
     * @param type of which the hosts will be created
     * 
     */
	
    public void createHostsDC1(int numberHosts, int type) 
    {    	
    	
    	final PowerModel powerModelHPProliantG4 =new PowerModelSpecPowerHpProLiantMl110G4Xeon3040();
    	final PowerModel powerModelHPProliantG5 =new PowerModelSpecPowerHpProLiantMl110G5Xeon3075();
    	final long   storage = 100000; //host storage (MEGABYTE) 
    	int bw = 0; //host storage (MEGABYTE)      	
    	int    ram = 0; // Initial declaration of the ram capacity 
    	long   pesNumber = 0; // Initial declaration of the pesNumber
    	long mipsPe = 0;  //host Instruction Pre Second (Million)
    	/*type = "HP Proliant G4";
    	type = "HP Proliant G5";*/
    	String cpuType;
    	
    	switch (type) 
    	{
    		case 1://"HP Proliant G4":
    			cpuType = "Intel_Xeon 3040";
    			ram = 4096; // host memory (MEGABYTE), 4 GB 
    			pesNumber = 2; // number of CPU cores
    			mipsPe = 1860;  //1.86 (GHz)= 1860 (MHz)
    			bw = 1024; //1 GB    			
    			  
    	        for(int i = 0; i < numberHosts; i++)
    	        {
    	            List<Pe> peList = new ArrayList<>();  	            
    	            for(int j = 0; j < pesNumber; j++)
    	            {
    	            	peList.add(new PeSimple(mipsPe, new PeProvisionerSimple()));
    	            }
    	            
    	        	Host host = new HostSimple(ram, bw, storage, peList)
    	        		.setRamProvisioner(new ResourceProvisionerSimple())
    	        		.setBwProvisioner(new ResourceProvisionerSimple())
    	        		.setVmScheduler(new VmSchedulerTimeShared());
    	        	host.setId(hostListDC1.size());
    	        	host.setPowerModel(powerModelHPProliantG4);
    	        	hostListDC1.add(host);
    	        }     			
    			break;
    			
    		case 2://"HP Proliant G5":
    			cpuType = "Intel_Xeon 3075";
    			ram = 4096; // host memory (MEGABYTE), 4 GB
     	       	pesNumber = 2; // number of CPU cores
     	       	long mipsPe1 = 2660; //2.66 (GHz) = 2660 (MHz) 
     	       	bw = 1024; // 1 GB
     	       	     	       
     	        for(int i = 0; i < numberHosts; i++)
     	        {
     	            List<Pe> peList = new ArrayList<>();
     	            for(int j = 0; j < pesNumber; j++)
     	            {
     	            	peList.add(new PeSimple(mipsPe1, new PeProvisionerSimple()));
     	            }
     	            
     	        	Host host = new HostSimple(ram, bw, storage, peList)
     	        		.setRamProvisioner(new ResourceProvisionerSimple())
     	        		.setBwProvisioner(new ResourceProvisionerSimple())
     	        		.setVmScheduler(new VmSchedulerTimeShared());
     	        	host.setId(hostListDC1.size());
     	        	host.setPowerModel(powerModelHPProliantG5);
     	        	hostListDC1.add(host);
     	        }       	       	
     	       	break;
    	}            
    }
    
    /**
     * Gets the created hosts in a list
     * @return {@link #hostListDC1}
     * @see #createHostsDC1(int, int)
     */
    
    public List<Host> getHostsListDC1() 
    {
    	return hostListDC1;
    }
    
    /**
     * Creates a Cloudlet with the given information.
     *
     * @param id      a Cloudlet ID
     * @param runTime The number of seconds the Cloudlet has to run.
     *                {@link Cloudlet#getLength()} is computed based on
     *                the {@link #getMips() mips} and this value.
     * @param numProc number of Cloudlet's PEs
     * @return the created Cloudlet
     * @see #mips
     */
    
    private static final int SCHEDULING_INTERVAL = 300;	
    private String TRACE_FILE = "";
    List<File> fileList = new ArrayList<>();  
    public List<Cloudlet> cloudletList = new ArrayList<>();
    int i=0;
    File Folder;
    long FinishedCloudletlength=0;
    private Cloudlet createCloudlet( long len)
    {
    	/*	Get the name of each workload file in the folder and assign them to cpu utilization.
    	 * 	Each cloudlet will be assigned a separate file
    	 */ 	
    	int Id=-1;
    	int numProc=1;    	
    	Folder = new File("G:\\Digital Engineering\\load balencing\\trace data\\planet lab\\planetlab-workload-traces-master\\20110303");

        File[] listOfFiles = Folder.listFiles();
    	TRACE_FILE = listOfFiles[i++].toString();
    	final UtilizationModel utilizationCpu = UtilizationModelPlanetLab.getInstance(TRACE_FILE, SCHEDULING_INTERVAL);

        Cloudlet cloudlet=new CloudletSimple(Id, len, numProc)
        			.setFileSize(1024)
                    .setOutputSize(1024)
                    .setUtilizationModelCpu(utilizationCpu)
                    .setUtilizationModelRam(new UtilizationModelDynamic(0.01))
                    .setUtilizationModelBw(new UtilizationModelDynamic(0.1));
                    
        cloudlet.addOnFinishListener( info ->{
	 		Vm VM=info.getCloudlet().getVm();
	 		FinishedCloudletlength = cloudlet.getTotalLength();
	 		System.out.println("FinishedCloudletLength-------------------------------------------------------------------->>>"+FinishedCloudletlength);
	 		createCloudletDay2(FinishedCloudletlength,VM);
	 	});
         return cloudlet;
    }


    private void createCloudletDay2( long len, Vm vm)
    {
    	/*	Get the name of each workload file in the folder and assign them to cpu utilization.
    	 * 	Each cloudlet will be assigned a separate file
    	 */ 	
    	int Id=-1;
    	int numProc=1;    	
    	Folder = new File("G:\\Digital Engineering\\load balencing\\trace data\\planet lab\\planetlab-workload-traces-master\\20110303");

        File[] listOfFiles = Folder.listFiles();
    	TRACE_FILE = listOfFiles[i++].toString();
    	final UtilizationModel utilizationCpu = UtilizationModelPlanetLab.getInstance(TRACE_FILE, SCHEDULING_INTERVAL);

        Cloudlet cloudlet=new CloudletSimple(Id, len, numProc)
        			.setFileSize(1024)
                    .setOutputSize(1024)
                    .setUtilizationModelCpu(utilizationCpu)
                    .setUtilizationModelRam(new UtilizationModelDynamic(0.01))
                    .setUtilizationModelBw(new UtilizationModelDynamic(0.1));
                    
        Broker.submitCloudlet(cloudlet);
        Broker.bindCloudletToVm(cloudlet, vm);
    }
    /**
     * Gets the created Cloudlets in a list
     * @return {@link #cloudletListDC1}
     * @see #createCloudletsFromWorkloadFile(DatacenterBroker)
     */
    
    public List<Cloudlet> getCloudletListDC1() 
    {
    	return cloudletListDC1;
    }
    
    /**
     * Gets the created Cloudlets in an Ascending order in a list
     * @return {@link #cloudletListDC1}
     * @see #createCloudletsFromWorkloadFile(DatacenterBroker)
     */
    
    public List<Cloudlet> getAscendingCloudletListDC1() 
    {    	
    	Comparator<Cloudlet> sortCloudLet = Comparator.comparingLong(Cloudlet::getLength);
    	cloudletListDC1.sort(sortCloudLet);
    	return cloudletListDC1;
    }
    
    /**
     * Gets the created Cloudlets in an Descending order in a list
     * @return {@link #cloudletListDC1}
     * @see #createCloudletsFromWorkloadFile(DatacenterBroker)
     */
    
    public List<Cloudlet> getDescendingCloudletListDC1() 
    {    	
    	Comparator<Cloudlet> sortCloudLet = Comparator.comparingLong(Cloudlet::getLength).reversed();
    	cloudletListDC1.sort(sortCloudLet);
    	return cloudletListDC1;
    }
    
    /**
     * Create Vm DC1: This method can be used to create different Cloudlets for data center 1 using predefined types
     * 
     * @param numberVm  the number of Cloudlets to be created
     * @param type of which the Cloudlets will be created
     * @param broker of data center 1
     * @param cloudlet of data center 1
     */
    DatacenterBroker Broker;
    public void createOneVmAndCloudlet(int numberVm, int type, DatacenterBroker broker,int DelayTime) 
    {
    	this.Broker = broker;
    	long VmID = -1;
    	final long storage = 102400, bw = 512;
    	int mips = 0;
    	int    ram = 0;
    	long   pesNumber = 0;
    	long Cloudletlength=0;
    	String VmType="0";
       /* type = "High-cpu Medium";
    	type = "Extra large";
    	type = "Small";
    	type = "Micro";*/
    	
    	switch (type)
    	{
    		case 1://"High-cpu Medium":
    			mips = 2500;
    			ram = 870; // vm memory (MEGABYTE), 0.85gb = 870mb (binary)
    			pesNumber = 1; // number of CPU cores
    			Cloudletlength = 216000000;//216000000 108000000(50%)
    			VmType="High-cpu Medium";
    			break;
    		case 2://"Extra large":
     	       	mips = 2000;
    			ram = 3840; // vm memory (MEGABYTE),3.75gb = 3840mb (binary)
     	       	pesNumber = 1; // number of CPU cores
     	        Cloudletlength = 172800000;//172800000  (86400000)50%
     	        VmType="Extra-Large";	       
     	       	break;
    		case 3://"Small":
     	       	mips = 1000;
    			ram = 1740; // vm memory (MEGABYTE), 1.75gb = 1740mb (binary)
     	       	pesNumber = 1; // number of CPU cores
     	        Cloudletlength = 86400000;//86400000 (43200000)50%
     	        VmType = "Small";        
     	       	break;
    		case 4://"Micro":
    			mips = 500;
    			ram = 624; // vm memory (MEGABYTE), 0.61gb = 624mb (binary)
    			pesNumber = 1; // number of CPU cores
    			Cloudletlength = 43200000;//43200000 (21600000)50%
    			VmType = "Micro";
     	       	break;
     	
    	}  
    		
    	for (int i = 0; i < numberVm; i++)
        {
        	Vm vm = new VmSimple(VmID, mips, pesNumber)
                    .setRam(ram).setBw(bw).setSize(storage)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());
        	vm.setSubmissionDelay(DelayTime);
        	vm.getUtilizationHistory().enable();
        	vm.setDescription(VmType);
            vmListDC1.add(vm);        
            Cloudlet cloudlet=createCloudlet( Cloudletlength);
 
            broker.submitVm(vm);
            broker.submitCloudlet(cloudlet);
            broker.bindCloudletToVm(cloudlet, vm);
        	}
   	
        System.out.printf("# Created %d VMs for the %s\n", vmListDC1.size(), broker);
    }

    /**
     * Gets the create virtual machines in a list
     * @return {@link #vmListDC1}
     * @see #createOneVmForEachCloudlet(int, int, DatacenterBroker, Cloudlet)
     */
    
    public List<Vm> getVmListDC1() 
    {
    	return vmListDC1;
    }  
    
}


