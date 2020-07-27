/**
 * 
 */
package hierarchicalarchitecture.localcontrollerwebapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import hierarchicalarchitecture.globalcontroller.GlobalController;

/**
 * @author Shyam Sundar V
 *
 */
public class GADriverWA {
	/*
	 * In chromosome, each each VM is associated with the host Id.
	 * Initially, The population is generated with random ids, which represent host ids.
	 * This length represents the number of hosts in the given datacenter. 
	 */
	
	public static int POPULATION_SIZE = 40;
	public static int GENERATIONS = 90;
	/*
	 * Map to be used in simulation for initial allocation
	 */
	public Map<Integer, Integer> vmToHostMapWebApp = new HashMap<Integer, Integer>();
	public Map<Integer, ArrayList<Integer>> serverVmsmapWebApp = new HashMap<Integer, ArrayList<Integer>>();	
	public Map<Double, ChromosomeWA> bestOfTheBestWebApp = new TreeMap<Double, ChromosomeWA>();
	public ChromosomeWA bestchromosomeofthegeneration;
	/*
	 * ******Dynamic VM Allocation variables*****
	 */
	public static Map<Long, Long> dynamicVmHostMap = new HashMap<Long, Long>();
	
	
	public static Map<Long, Double> hostCurrentUtil = new HashMap<Long, Double>();
	public static int Dynamic_POPULATION_SIZE = 8;
	public static int Dynamic_GENERATIONS = 25;
	
	
	public static List<Long> sourcevmList;
	public static List<Long> sourcehostList;
	public static List<Long> targetHostList;
	public Map<Long, ArrayList<Long>> serverVmMapDynamic = new HashMap<Long, ArrayList<Long>>();
	/*
	 * Map to be used in simulation for dynamic allocation
	 */
	public static  Map<Long, Long> bestDynamicVmServerMap = new HashMap<Long, Long>();
	public DynamicChromosomeWA bestFinalDynamic;

	/**
	 * Constructor for initial VM Allocation
	 */
	public static int Hostlist_sizeIP;
	public static int vmlist_sizeIP;
	public void gaDriverInitialPlacement() {
		
		List<Host> Host_List_IP = ModelConstructionForApplications.CreateDatacenter.getHostsListWebApplication();
		Hostlist_sizeIP = Host_List_IP.size();
		List<Vm> VM_List = GlobalController.InitialvmListWebApplication;
		vmlist_sizeIP = VM_List.size();
		generatePopulation = new PopulationWA(POPULATION_SIZE,VM_List.size()).initializePopulation(Host_List_IP.size(), Host_List_IP ,VM_List);
		
		EvolutionWA gen = new EvolutionWA(VM_List.size());
		int generationNumber = 0;
		
			while(generationNumber < GENERATIONS) {
				generationNumber++;			
				PopulationWA PopulationWA = gen.evolve(generatePopulation,Host_List_IP,VM_List);
				PopulationWA.sortChromosomesByFitness();
				
				/* remove old population and add offspring to the new population. */
				for(int i=0; i < PopulationWA.getPopulationSize(); i++) {
					generatePopulation.ChromosomeWA[i] = PopulationWA.ChromosomeWA[i];	
				}	
				
				 /* select best individual to place a given vm */
				bestchromosomeofthegeneration = new ChromosomeWA(VM_List.size());
				for(int i=0; i<PopulationWA.getChromosomes().length;i++) {
					if((PopulationWA.getChromosomes()[i].resource_Availability_Violation == 0) && (PopulationWA.getChromosomes()[i].ACTIVE_SERVERS > 0)) {
						bestchromosomeofthegeneration = PopulationWA.getChromosomes()[i];
						break;
					}
				}
			//	bestFinalWebApplication =	PopulationWA.getChromosomes()[0];
				if(bestchromosomeofthegeneration != null && bestchromosomeofthegeneration.ACTIVE_SERVERS !=0) {
					bestOfTheBestWebApp.put(bestchromosomeofthegeneration.getFitness(), bestchromosomeofthegeneration);			
				}
			//	bestOfTheBestWebApplication.put(bestchromosomeofthegeneration.getFitness(), bestchromosomeofthegeneration);			
			//	vmToHostMapWebApplication = bestchromosomeofthegeneration.getVmToServerMap();
			//	serverVmsmapWebApplication = bestchromosomeofthegeneration.getServersWithVmList();
				if((bestOfTheBestWebApp.isEmpty()) && (GENERATIONS == generationNumber)) {
					GENERATIONS += 15;
				}
			}
		
			vmToHostMapWebApp = bestOfTheBestWebApp.values().stream().findFirst().get().getVmToServerMap();
			vmToHostMapWebApp = bestOfTheBestWebApp.values().stream().findFirst().get().getVmToServerMap();
			serverVmsmapWebApp = bestOfTheBestWebApp.values().stream().findFirst().get().getServersWithVmList();
	}
	
	
	PopulationWA generatePopulation;		  
	/**
	 * Constructor for Dynamic VM Allocation
	 * @param sourceAllocationMap
	 * @param vmCurrentCpuChar
	 * @param vmCurrentRAMChar
	 */

	 DynamicPopulationWA initialPopulation;
	 DynamicPopulationWA bestPopulation = new DynamicPopulationWA(Dynamic_GENERATIONS); 
	 public boolean GAChooseSourceMapWA;
	 public Map<Long, Long> dynamicGA(Map<Long,Long> sourceAllocationMap, List<Long> targetHosts, List<Host> hostList, List<Vm> vmList ) {
		 
		 dynamicVmHostMap = sourceAllocationMap;
		 sourcevmList = new ArrayList<Long>(sourceAllocationMap.keySet());
		 sourcehostList = new ArrayList<Long>(sourceAllocationMap.values());
		 targetHostList = new ArrayList<Long>(targetHosts);
		 
		 DynamicEvolutionWA gen = new DynamicEvolutionWA();
		 initialPopulation = new DynamicPopulationWA(Dynamic_POPULATION_SIZE, hostList, vmList).intialize(Dynamic_POPULATION_SIZE);
		 int generationNumber = 0;
		 while(generationNumber < Dynamic_GENERATIONS) {
			 generationNumber++;
			 DynamicPopulationWA population = gen.evolve(initialPopulation,hostList,vmList);
			 population.sortChromosomesByFitness();
				
				 /* select best individual to place the selected vms from the oveloaded/underloaded hosts
				 */ 
			 initialPopulation = new DynamicPopulationWA(Dynamic_POPULATION_SIZE);
	
			 IntStream.range(0, population.chromosomes.size()).forEach(i ->
			 initialPopulation.chromosomes.add(i, population.chromosomes.get(i)));	
			 for(int i=0 ; i < population.chromosomes.size(); i++) {
				 if(population.getChromosomes().get(i).SLA_VIOLATIONS_IN_CHROMOSOME == 0 && population.getChromosomes().get(i).Count == 0) {
					 bestPopulation.getChromosomes().add(population.getChromosomes().get(i));
				 }
			 }
			 Here:
				 if((bestPopulation.chromosomes.isEmpty()) && (Dynamic_GENERATIONS == generationNumber)) {
					 if(Dynamic_GENERATIONS >= 75) {
						 bestDynamicVmServerMap = sourceAllocationMap;
						 GAChooseSourceMapWA = true;
						 break Here;
					 }
					 Dynamic_GENERATIONS += 10; 
				 }
		 }		 
		 if((!bestPopulation.chromosomes.isEmpty())) {
			 bestPopulation.sortChromosomesByFitness();
			 bestDynamicVmServerMap = bestPopulation.getChromosomes().get(0).getGenes();
			 serverVmMapDynamic = bestPopulation.getChromosomes().get(0).getServerwithVMList();
			 return bestDynamicVmServerMap;
		 }else {
			 return bestDynamicVmServerMap;
		 }
	 	}
	}
