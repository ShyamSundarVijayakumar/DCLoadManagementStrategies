package org.cloudbus.cloudsim.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

import org.cloudbus.cloudsim.AutonomicLoadManagementStrategies.InitializationDC2;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.EventInfo;

public class SwfWorkloadFileReaderApp3DC2 extends TraceReaderAbstract{
    /**
     * Field index of job number.
     * Jub number values start from 1.
     */
    private static final int JOB_NUM_INDEX = 0;

    /**
     * Field index of submit time of a job (in seconds).
     */
    private static final int SUBMIT_TIME_INDEX = 1;

    /**
     * Field index of execution time of a job (in seconds).
     * The wall clock time the job was running (end time minus start time).
     */
    private static final int RUN_TIME_INDEX = 3;

    /**
     * Field index of number of processors needed for a job.
     * In most cases this is also the number of processors the job uses; if the job does not use all of them, we typically don't know about it.
     */
    private static final int NUM_PROC_INDEX = 4;

    /**
     * Field index of required number of processors.
     */
    private static final int REQ_NUM_PROC_INDEX = 7;

    /**
     * Field index of required running time.
     * This can be either runtime (measured in wallclock seconds), or average CPU time per processor (also in seconds)
     * -- the exact meaning is determined by a header comment.
     * If a log contains a request for total CPU time, it is divided by the number of requested processors.
     */
    private static final int REQ_RUN_TIME_INDEX = 8;

    /**
     * Field index of user who submitted the job.
     */
    private static final int USER_ID_INDEX = 11;

    /**
     * Field index of group of the user who submitted the job.
     */
    private static final int GROUP_ID_INDEX = 12;

    /**
     * Max number of fields in the trace reader.
     */
    private static final int FIELD_COUNT = 18;

    /**
     * If the field index of the job number ({@link #JOB_NUM_INDEX}) is equals to this
     * constant, it means the number of the job doesn't have to be gotten from
     * the trace reader, but has to be generated by this workload generator class.
     */
    private static final int IRRELEVANT = -1;

    /**
     * @see #getMips()
     */
    private int mips;

    /**
     * @see #setPredicate(Predicate)
     */
    private Predicate<Cloudlet> predicate;

    /**
     * Gets a {@link SwfWorkloadFileReader} instance from a workload file
     * inside the <b>application's resource directory</b>.
     * Use the available constructors if you want to load a file outside the resource directory.
     *
     * @param fileName the workload trace <b>relative file name</b> in one of the following formats: <i>ASCII text, zip, gz.</i>
     * @param mips     the MIPS capacity of the PEs from the VM where each created Cloudlet is supposed to run.
     *                 Considering the workload reader provides the run time for each
     *                 application registered inside the reader, the MIPS value will be used
     *                 to compute the {@link Cloudlet#getLength() length of the Cloudlet (in MI)}
     *                 so that it's expected to execute, inside the VM with the given MIPS capacity,
     *                 for the same time as specified into the workload reader.
     * @throws IllegalArgumentException when the workload trace file name is null or empty; or the resource PE mips <= 0
     * @throws UncheckedIOException     when the file cannot be accessed (such as when it doesn't exist)
     */
    public static SwfWorkloadFileReaderApp3DC2 getInstance(final String fileName, final int mips) {
        final InputStream reader = ResourceLoader.newInputStream(fileName, SwfWorkloadFileReader.class);
        return new SwfWorkloadFileReaderApp3DC2(fileName, reader, mips);
    }

    /**
     * Create a new SwfWorkloadFileReader object.
     *
     * @param filePath the workload trace file path in one of the following formats: <i>ASCII text, zip, gz.</i>
     * @param mips     the MIPS capacity of the PEs from the VM where each created Cloudlet is supposed to run.
     *                 Considering the workload reader provides the run time for each
     *                 application registered inside the reader, the MIPS value will be used
     *                 to compute the {@link Cloudlet#getLength() length of the Cloudlet (in MI)}
     *                 so that it's expected to execute, inside the VM with the given MIPS capacity,
     *                 for the same time as specified into the workload reader.
     * @throws IllegalArgumentException when the workload trace file name is null or empty; or the resource PE mips <= 0
     * @throws FileNotFoundException    when the file is not found
     * @see #getInstance(String, int)
     */
    public SwfWorkloadFileReaderApp3DC2(final String filePath, final int mips) throws IOException {
        this(filePath, Files.newInputStream(Paths.get(filePath)), mips);
    }

    /**
     * Create a new SwfWorkloadFileReader object.
     *
     * @param filePath the workload trace file path in one of the following formats: <i>ASCII text, zip, gz.</i>
     * @param reader   a {@link InputStreamReader} object to read the file
     * @param mips     the MIPS capacity of the PEs from the VM where each created Cloudlet is supposed to run.
     *                 Considering the workload reader provides the run time for each
     *                 application registered inside the reader, the MIPS value will be used
     *                 to compute the {@link Cloudlet#getLength() length of the Cloudlet (in MI)}
     *                 so that it's expected to execute, inside the VM with the given MIPS capacity,
     *                 for the same time as specified into the workload reader.
     * @throws IllegalArgumentException when the workload trace file name is null or empty; or the resource PE mips <= 0
     * @see #getInstance(String, int)
     */
    private SwfWorkloadFileReaderApp3DC2(final String filePath, final InputStream reader, final int mips) {
        super(filePath, reader);

        this.setMips(mips);
     //   this.CloudletQueueDC2 = new LinkedList<>();

        /*
        A default predicate which indicates that a Cloudlet will be
        created for any job read from the workload reader.
        That is, there isn't an actual condition to create a Cloudlet.
        */
        this.predicate = cloudlet -> true;
    }

    /**
     * Generates a list of jobs ({@link Cloudlet Cloudlets}) to be executed,
     * if it wasn't generated yet.
     *
     * @return a generated Cloudlet list
     */
    DatacenterBroker broker;
    CloudSim simulation;
    Queue<Cloudlet> CloudletQueueDC2;
    public Queue<Cloudlet> generateWorkload(DatacenterBroker broker,CloudSim simulation,Queue<Cloudlet> CloudletQueueDC2) {
    	this.broker=broker;
    	this.CloudletQueueDC2=CloudletQueueDC2;
    	this.simulation=simulation;
   // 	if (CloudletQueueDC2.isEmpty()) {
            readFile(this::createCloudletFromTraceLine);
     //   }

        return CloudletQueueDC2;
    }

    /**
     * Defines a {@link Predicate} which indicates when a {@link Cloudlet}
     * must be created from a trace line read from the workload file.
     * If a Predicate is not set, a Cloudlet will be created for any line read.
     *
     * @param predicate the predicate to define when a Cloudlet must be created from a line read from the workload file
     * @return
     */
    public SwfWorkloadFileReaderApp3DC2 setPredicate(final Predicate<Cloudlet> predicate) {
        this.predicate = predicate;
        return this;
    }

    /**
     * Extracts relevant information from a given array of fields, representing
     * a line from the trace reader, and creates a cloudlet using this
     * information.
     *
     * @param parsedLineArray an array containing the field values from a parsed trace line
     * @return true if the parsed line is valid and the Cloudlet was created, false otherwise
     */
    InitializationDC2 createVm=new InitializationDC2();
    String ApplicationNumber="3";
    private boolean createCloudletFromTraceLine(final String[] parsedLineArray) {
        //If all the fields couldn't be read, don't create the Cloudlet.
        if (parsedLineArray.length < FIELD_COUNT) {
            return false;
        }

        final int id = JOB_NUM_INDEX <= IRRELEVANT ? CloudletQueueDC2.size() + 1 : Integer.parseInt(parsedLineArray[JOB_NUM_INDEX].trim());

        /* according to the SWF manual, runtime of 0 is possible due
         to rounding down. E.g. runtime is 0.4 seconds -> runtime = 0*/
        final int runTime = Math.max(Integer.parseInt(parsedLineArray[RUN_TIME_INDEX].trim()), 1);

        /* if the required num of allocated processors field is ignored
        or zero, then use the actual field*/
        final int maxNumProc = Math.max(
                                    Integer.parseInt(parsedLineArray[REQ_NUM_PROC_INDEX].trim()),
                                    Integer.parseInt(parsedLineArray[this.NUM_PROC_INDEX].trim())
                               );
        final int numProc = Math.max(maxNumProc, 1);
        final long submitTime = Long.parseLong(parsedLineArray[this.SUBMIT_TIME_INDEX].trim());
        
        Vm vm1=createVm.createVmDC2(1,-1,submitTime,ApplicationNumber,1);
        broker.submitVm(vm1);
        
        
        Vm vm2=createVm.createVmDC2(1,-1,submitTime,ApplicationNumber,4);
        broker.submitVm(vm2);
        
        
        Vm vm3=createVm.createVmDC2(1,-1,submitTime,ApplicationNumber,2);
        broker.submitVm(vm3);
        Cloudlet cloudlet1=createCloudletType1(id);
        Cloudlet cloudlet2=createCloudletType2(id);
        Cloudlet cloudlet3=createCloudletType3(id);
        
        

        broker.bindCloudletToVm(cloudlet1, vm1);
        broker.bindCloudletToVm(cloudlet2, vm2);
        broker.bindCloudletToVm(cloudlet3, vm3);
        	
        
       
     //   if((id==0) || (id%2)==0) 
     //  	{
      		Vm vm4=createVm.createVmDC2(1,-1,submitTime,ApplicationNumber,1);
       		broker.submitVm(vm4);
       		Vm vm5=createVm.createVmDC2(1,-1,submitTime,ApplicationNumber,2);
       		broker.submitVm(vm5);
       		Cloudlet cloudlet4=createCloudletType4(id);
       		Cloudlet cloudlet5=createCloudletType5(id);
        	broker.bindCloudletToVm(cloudlet4, vm4);
       		broker.bindCloudletToVm(cloudlet5, vm5);
  //     	}

       for(Cloudlet cloudlet : this.CloudletQueueDC2)
       {
            if(predicate.test(cloudlet))
            {
           // cloudlets.add(cloudlet);
            return true;
            }
       }

       return false;
    }

 	boolean Cloudlet1Status;
    boolean Cloudlet2Status;
 	boolean Cloudlet3Status;
    boolean Cloudlet4Status;
	boolean Cloudlet5Status;
	
	private boolean getCloudlet1status() 
	{
			return Cloudlet1Status;
	}
	private void setCloudlet1status(boolean Status)
	{
			Cloudlet1Status=Status;
	}
	
	private boolean getCloudlet2status() 
	{
			return Cloudlet2Status;
	}
	private void setCloudlet2status(boolean Status)
	{
			Cloudlet2Status=Status;
	}
	
	private boolean getCloudlet3status()
	{
			return Cloudlet3Status;
	}
	private void setCloudlet3status(boolean Status)
	{
			Cloudlet3Status=Status;
	}
	
	private boolean getCloudlet4status() 
	{
			return Cloudlet4Status;
	}
	private void setCloudlet4status(boolean Status)
	{
			Cloudlet4Status=Status;
	}
	 
	private boolean getCloudlet5status()
	{
			return Cloudlet5Status;
	}
	private void setCloudlet5status(boolean Status)
	{
			Cloudlet5Status=Status;
	}

	/**
	 * CreateCloudletsAgain method checks if the cloudlets have finished executing and initiates cloudlet creation
	 * with a thinking time.
	 * cloudlet8 and cloudlet9 will be created with a visit ratio of 0.5. Which means they will be created only for
	 * every second job arrival
	 * 
	 * @see {@link #getCloudletstatus()},{@link #getCloudletstatus1()},{@link #getCloudletstatus2()},
	 * {@link #getCloudletstatus3()},{@link #getCloudletstatus4()}
	 * 
	 */
	Queue<Integer> OfflineTime= new LinkedList<>();
	int ThinkingTime=4;
	Vm vm1,vm2,vm3,vm4,vm5;
	private void clockTickListener( EventInfo info)//	private void CreateCloudletsAgain(int id)
	{
		//System.out.println("id test"+id);
	//	if((id==0) || ((id%2)== 0)) 
	//	{&& getCloudlet5status() == true
			if (getCloudlet1status() == true && getCloudlet2status() == true && getCloudlet3status() == true && getCloudlet4status() == true )
			{
				vm1=createVm.createVmDC2(1,QueueFinishedVmid.poll(),ThinkingTime,ApplicationNumber,1);
			    broker.submitVm(vm1);
			    vm2=createVm.createVmDC2(1,QueueFinishedVmid.poll(),ThinkingTime,ApplicationNumber,4);
			    broker.submitVm(vm2);
			    vm3=createVm.createVmDC2(1,QueueFinishedVmid.poll(),ThinkingTime,ApplicationNumber,2);
			    broker.submitVm(vm3);
			    vm4=createVm.createVmDC2(1,QueueFinishedVmid.poll(),ThinkingTime,ApplicationNumber,1);
			    broker.submitVm(vm4);
			    vm5=createVm.createVmDC2(1,QueueFinishedVmid.poll(),ThinkingTime,ApplicationNumber,2);
			    broker.submitVm(vm5); 
			    int id=0;
			    Cloudlet cloudlet1=createCloudletType1(id);
			    Cloudlet cloudlet2=createCloudletType2(id);
			    Cloudlet cloudlet3=createCloudletType3(id);
			    Cloudlet cloudlet4=createCloudletType4(id);
			    Cloudlet cloudlet5=createCloudletType5(id);
			    broker.bindCloudletToVm(cloudlet1, vm1);
			    broker.bindCloudletToVm(cloudlet2, vm2);
			    broker.bindCloudletToVm(cloudlet3, vm3);
			    broker.bindCloudletToVm(cloudlet4, vm4);
			    broker.bindCloudletToVm(cloudlet5, vm5);
			    
			    OfflineTime.add(ApplicationFinishTime.poll() + ThinkingTime);
				setCloudlet1status(false);
				setCloudlet2status(false);
				setCloudlet3status(false);
				setCloudlet4status(false);
				setCloudlet5status(false);
	//		}
		}
		
		/*if((id==1) || (id%2) == 1) 
		{
			if (getCloudlet1status() == true && getCloudlet2status() == true && getCloudlet3status() == true )
			{
		    	Vm vm1=createVm.createVmDC2(1,QueueFinishedVmid.poll(),ThinkingTime,ApplicationNumber);
			    broker.submitVm(vm1);
			    Vm vm2=createVm.createVmDC2(1,QueueFinishedVmid.poll(),ThinkingTime,ApplicationNumber);
			    broker.submitVm(vm2);
			    Vm vm3=createVm.createVmDC2(1,QueueFinishedVmid.poll(),ThinkingTime,ApplicationNumber);
			    broker.submitVm(vm3);
			    Cloudlet cloudlet1=createCloudletType1(id);
			    Cloudlet cloudlet2=createCloudletType2(id);
			    Cloudlet cloudlet3=createCloudletType3(id);
			    broker.bindCloudletToVm(cloudlet1, vm1);
			    broker.bindCloudletToVm(cloudlet2, vm2);
			    broker.bindCloudletToVm(cloudlet3, vm3);
				setCloudlet1status(false);
				setCloudlet2status(false);
				setCloudlet3status(false);
			}
		}*/
	}
		
	/**
	 * Creates a Cloudlet with the given information.
	 *
	 * @param id a Cloudlet ID
	 * @param broker of the data center 2
	 * @param OfflineTime of the cloudlet.offline time is the time cloudlet spends waiting 
	 * @return the created CloudletQueueDC2
	 * 
	 */
	List<Vm> FinishedvmListApplication3DC2 = new LinkedList<>();
	Queue<Integer> QueueFinishedVmid = new LinkedList<>();
	Queue<Integer> ApplicationFinishTime= new LinkedList<>();
	public Cloudlet createCloudletType1(final int id) 
    {
        long fileSize = 300; 
        long outputSize = 300; 
        long mips=2500;
        int LetID = CloudletQueueDC2.size();
        int	pesNumber=1;
        double ServiceTime5 = 0.005;
    	double cloudletlength= (ServiceTime5 * mips)/pesNumber;
        long length=(long)cloudletlength;
    	Cloudlet cloudlet = new CloudletSimple(LetID, length, pesNumber)
    							.setFileSize(fileSize)
    							.setOutputSize(outputSize)
    							.setUtilizationModelCpu(new UtilizationModelDynamic(0.1))
    							.setUtilizationModelRam(new UtilizationModelDynamic(0.5));
   // 	cloudlet.setJobId(id);
    	CloudletQueueDC2.add(cloudlet); 
    	broker.submitCloudlet(cloudlet); 
    		
        cloudlet.addOnStartListener( info ->
        {
 		 	Vm VM=info.getCloudlet().getVm();
 		 	createVm.getvmQueueDC2().remove(VM);
 		 });
            
    	cloudlet.addOnFinishListener(info ->
    	{
    		System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n",info.getTime(), info.getCloudlet());
    		Vm FinishedVm=cloudlet.getVm();
    		FinishedvmListApplication3DC2.add(FinishedVm);
    		int FinishedVmid=(int) FinishedVm.getId();
    		QueueFinishedVmid.add(FinishedVmid);
    		Cloudlet1Status=cloudlet.isFinished();
    		setCloudlet1status(Cloudlet1Status);
    		int JobId=(int)cloudlet.getJobId();
    	//	CreateCloudletsAgain(JobId);
    		simulation.addOnClockTickListener(this::clockTickListener);
    	});
    
    	return cloudlet;
    }
	
	public Cloudlet createCloudletType2(final int id)
	{
        long fileSize = 300; 
        long outputSize = 300; 
        long mips=2500;
        int LetID = CloudletQueueDC2.size();
    	double ServiceTime6 = 0.02;	       
    	int pesNumber=4;
    	double cloudletlength= (ServiceTime6 * mips)/pesNumber;
        long length=(long)cloudletlength;
    	Cloudlet cloudlet = new CloudletSimple(LetID, length, pesNumber)
    							.setFileSize(fileSize)
    							.setOutputSize(outputSize)
    							.setUtilizationModelCpu(new UtilizationModelDynamic(0.1))
    							.setUtilizationModelRam(new UtilizationModelDynamic(0.5));
  //  	cloudlet.setJobId(id);
	    CloudletQueueDC2.add(cloudlet); 
	    broker.submitCloudlet(cloudlet); 
	        
	    cloudlet.addOnStartListener( info ->
	    {
	    	Vm VM=info.getCloudlet().getVm();
 		 	createVm.getvmQueueDC2().remove(VM);
 		});
	
	    cloudlet.addOnFinishListener(info ->
	    {
	    	System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n",info.getTime(), info.getCloudlet());
	    	Vm FinishedVm=cloudlet.getVm();
			FinishedvmListApplication3DC2.add(FinishedVm);
			int FinishedVmid=(int) FinishedVm.getId();
			QueueFinishedVmid.add(FinishedVmid);
			Cloudlet2Status=cloudlet.isFinished();
			setCloudlet2status(Cloudlet2Status);
			int JobId=(int)cloudlet.getJobId();
			ApplicationFinishTime.add((int)info.getTime());
			simulation.addOnClockTickListener(this::clockTickListener);//CreateCloudletsAgain(JobId);
	        });
	        
	    return cloudlet;
	}
	
	public Cloudlet	createCloudletType3(final int id)
	{
        long fileSize = 300; 
        long outputSize = 300; 
        long mips=2500;
        int LetID = CloudletQueueDC2.size();
	    double ServiceTime7 = 0.01;	       
	    int pesNumber=2;
    	double cloudletlength= (ServiceTime7 * mips)/pesNumber;
        long length=(long)cloudletlength;
    	Cloudlet cloudlet = new CloudletSimple(LetID, length, pesNumber)
    							.setFileSize(fileSize)
    							.setOutputSize(outputSize)
    							.setUtilizationModelCpu(new UtilizationModelDynamic(0.1))
    							.setUtilizationModelRam(new UtilizationModelDynamic(0.5)); 
  //  	cloudlet.setJobId(id);
    	CloudletQueueDC2.add(cloudlet); 
	    broker.submitCloudlet(cloudlet); 
	        
	   cloudlet.addOnStartListener( info ->
	   {
 			Vm VM=info.getCloudlet().getVm();
 			createVm.getvmQueueDC2().remove(VM);
 		});
	    
	   cloudlet.addOnFinishListener(info -> 
	   {final int time =(int) info.getTime();
	       System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n",info.getTime(), info.getCloudlet());
	       Vm FinishedVm=cloudlet.getVm();
	       FinishedvmListApplication3DC2.add(FinishedVm);
	       int FinishedVmid=(int) FinishedVm.getId();
	       QueueFinishedVmid.add(FinishedVmid);
	       Cloudlet3Status=cloudlet.isFinished();
	       setCloudlet3status(Cloudlet3Status);
	       int JobId=(int)cloudlet.getJobId();
	       simulation.addOnClockTickListener(this::clockTickListener);//CreateCloudletsAgain(JobId);
	   });
	
	   return cloudlet;
	}
    
	public Cloudlet createCloudletType4(final int id)
	{
		long fileSize = 300; 
        long outputSize = 300; 
        long mips=2500;
        int LetID = CloudletQueueDC2.size();
        double ServiceTime8 = 0.01;	       
        int pesNumber=1;
        double cloudletlength= (ServiceTime8 * mips)/pesNumber;
        long length=(long)cloudletlength;
        
        Cloudlet cloudlet = new CloudletSimple(LetID, length, pesNumber)
    							.setFileSize(fileSize)
    							.setOutputSize(outputSize)
    							.setUtilizationModelCpu(new UtilizationModelDynamic(0.1))
    							.setUtilizationModelRam(new UtilizationModelDynamic(0.5));
  //      cloudlet.setJobId(id);
        CloudletQueueDC2.add(cloudlet); 
        broker.submitCloudlet(cloudlet); 
    		
        cloudlet.addOnStartListener( info ->
        {
        	Vm VM=info.getCloudlet().getVm();
        	createVm.getvmQueueDC2().remove(VM);
        });
    	
        cloudlet.addOnFinishListener(info -> 
        {
    		System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n",info.getTime(), info.getCloudlet());
    		Vm FinishedVm=cloudlet.getVm();
			FinishedvmListApplication3DC2.add(FinishedVm);
			int FinishedVmid=(int) FinishedVm.getId();
			QueueFinishedVmid.add(FinishedVmid);
			Cloudlet4Status=cloudlet.isFinished();
			setCloudlet4status(Cloudlet4Status);
			int JobId=(int)cloudlet.getJobId();
			simulation.addOnClockTickListener(this::clockTickListener);//CreateCloudletsAgain(JobId);
        });
    	
        return cloudlet;
	}
        
	public Cloudlet	createCloudletType5(final int id) 
	{
		long fileSize = 300; 
		long outputSize = 300; 
		long mips=2500;
		int LetID = CloudletQueueDC2.size();
		int	pesNumber=2;
		double ServiceTime10 = 0.02;	       
		double cloudletlength= (ServiceTime10 * mips)/pesNumber;
		long length=(long)cloudletlength;
		
		Cloudlet cloudlet = new CloudletSimple(LetID, length, pesNumber)
	    		   				.setFileSize(fileSize)
	    		   				.setOutputSize(outputSize)
	    		   				.setUtilizationModelCpu(new UtilizationModelDynamic(0.1))
    							.setUtilizationModelRam(new UtilizationModelDynamic(0.5));

	//	cloudlet.setJobId(id);
		CloudletQueueDC2.add(cloudlet); 
		broker.submitCloudlet(cloudlet); 
        	
		cloudlet.addOnStartListener( info ->
		{
			Vm VM=info.getCloudlet().getVm();
			createVm.getvmQueueDC2().remove(VM);
		});
   	    
		cloudlet.addOnFinishListener(info ->
		{
   	       System.out.printf("\t# %.2f: Requesting creation of new Cloudlet after %s finishes executing.\n",info.getTime(), info.getCloudlet());
   	       Vm FinishedVm=cloudlet.getVm();
   	       FinishedvmListApplication3DC2.add(FinishedVm);
   	       int FinishedVmid=(int) FinishedVm.getId();
   	       QueueFinishedVmid.add(FinishedVmid);
   	       Cloudlet5Status=cloudlet.isFinished();
   	       setCloudlet5status(Cloudlet5Status);
   	       int JobId=(int)cloudlet.getJobId();
   	       simulation.addOnClockTickListener(this::clockTickListener);//CreateCloudletsAgain(JobId);;
		});
		
		return cloudlet;
    }

    /**
     * Gets the MIPS capacity of the PEs from the VM where each created Cloudlet is supposed to run.
     * Considering the workload reader provides the run time for each
     * application registered inside the reader, the MIPS value will be used
     * to compute the {@link Cloudlet#getLength() length of the Cloudlet (in MI)}
     * so that it's expected to execute, inside the VM with the given MIPS capacity,
     * for the same time as specified into the workload reader.
     */
    public int getMips() {
        return mips;
    }

    /**
     * Sets the MIPS capacity of the PEs from the VM where each created Cloudlet is supposed to run.
     * Considering the workload reader provides the run time for each
     * application registered inside the reader, the MIPS value will be used
     * to compute the {@link Cloudlet#getLength() length of the Cloudlet (in MI)}
     * so that it's expected to execute, inside the VM with the given MIPS capacity,
     * for the same time as specified into the workload reader.
     *
     * @param mips the MIPS value to set
     */
    public SwfWorkloadFileReaderApp3DC2 setMips(final int mips) {
        if (mips <= 0) {
            throw new IllegalArgumentException("MIPS must be greater than 0.");
        }
        this.mips = mips;
        return this;
    }
}
