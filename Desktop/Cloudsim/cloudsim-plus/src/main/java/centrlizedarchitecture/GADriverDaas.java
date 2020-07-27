/**
 * 
 */
package centrlizedarchitecture;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;
/**
 * @author Shyam Sundar V
 *
 */
public class GADriverDaas {
		/*
		 * In chromosome, each each VM is associated with the host Id.
		 * Initially, The population is generated with random ids, which represent host ids.
		 * This length represents the number of hosts in the given datacenter. 
		 */

		
		 /* Total number of initial vms in a datacenter */
		 
		private static int POPULATION_SIZE = 25;
		public static int GENERATIONS = 60;
		
		 /* Map to be used in simulation for initial allocation */
		 
		public Map<Integer, Integer> vmToHostMapDaas = new HashMap<Integer, Integer>();
		public Map<Integer, ArrayList<Integer>> serverVmsmapDaas = new HashMap<Integer, ArrayList<Integer>>();	
		public Map<Double, ChromosomeDAAS> bestOfTheBestDaas = new TreeMap<Double, ChromosomeDAAS>();
		public ChromosomeDAAS bestFinalDaas;
		
		 /* ******Dynamic VM Allocation variables*****/
		 
		public static Map<Long, Long> dynamicVmHostMap = new HashMap<Long, Long>();
		private static int Dynamic_POPULATION_SIZE = 10;
		private static int Dynamic_GENERATIONS = 50;
		
		public static List<Long> sourcevmList;
		public static List<Long> sourcehostList;
		public static List<Long> targetHostList;	
		public Map<Long, ArrayList<Long>> serverVmMapDynamic = new HashMap<Long, ArrayList<Long>>();
		
		 /* Map to be used in simulation for dynamic allocation */
		 
		public static  Map<Long, Long> bestDynamicVmServerMap = new HashMap<Long, Long>();
		public DynamicChromosomeDAAS bestFinalDynamic;

		public static List<Host> hostListDAAS;
		public static List<Vm> vmListDAAS;
		DynamicPopulationDAAS initialPopulation;
		PopulationDAAS generatePopulation;
		public static int hostlist_Size;
		public static int vmlist_Size;
		
		 /* Constructor for initial VM Allocation */
		 
		public void GAInitialplacement() {
			
			List<Host> hostListDaas = ModelConstructionForApplications.CreateDatacenterCA.getHostsListDaaS();
			List<Vm> VM_List = centralManager.InitialvmListDaas;
			hostlist_Size = hostListDaas.size();
			vmlist_Size = VM_List.size();
			generatePopulation = new PopulationDAAS(POPULATION_SIZE,VM_List.size()).initializePopulation(hostListDaas.size(), hostListDaas ,VM_List);
			
			EvolutionDAAS gen = new EvolutionDAAS(VM_List.size());
			int generationNumber = 0;
			
				while(generationNumber <= GENERATIONS) {
					generationNumber++;
				
					PopulationDAAS populationDAAS = gen.evolve(generatePopulation,hostListDaas,VM_List);
					populationDAAS.sortChromosomesByFitness();
					
					 /* remove old population and add offspring to the new population. */
				
					this.generatePopulation = new PopulationDAAS(POPULATION_SIZE,VM_List.size());
					for(int i=0; i < populationDAAS.getPopulationSize(); i++) {
						generatePopulation.chromosomeDAAS[i] = populationDAAS.chromosomeDAAS[i];	
					}
					
					 /* select best individual to place a given vm */
					/* select best individual to place a given vm */
					bestFinalDaas = new ChromosomeDAAS(VM_List.size());
					for(int i=0; i<populationDAAS.getChromosomes().length;i++) {
						if((populationDAAS.getChromosomes()[i].SLA_VIOLATIONS_IN_CHROMOSOME==0) && (populationDAAS.getChromosomes()[i].TotalNoOfMigrations==0)
								&& (populationDAAS.getChromosomes()[i].ACTIVE_SERVERS != 0)) {
							bestFinalDaas = populationDAAS.getChromosomes()[i];
							break;
						}
					}
					if(bestFinalDaas != null) {
						bestOfTheBestDaas.put(bestFinalDaas.getFitness(), bestFinalDaas);			
					}
					if((bestOfTheBestDaas.isEmpty()) && (GENERATIONS == generationNumber)) {
						GENERATIONS += 15;
					}
				}
				
				vmToHostMapDaas = bestOfTheBestDaas.values().stream().findFirst().get().getVmToServerMap();
				serverVmsmapDaas = bestOfTheBestDaas.values().stream().findFirst().get().getServersWithVmList();
				vmToHostMapDaas = bestOfTheBestDaas.values().stream().findFirst().get().getVmToServerMap();
				
				double simultionTime = hostListDaas.get(0).getDatacenter().getSimulation().clock();
				Map<Integer, Integer> vmToHostMap = new HashMap<Integer, Integer>();
				if(simultionTime > 1000) {
					for(int i=0 ; i < VM_List.size() ; i++ ) {
						int VmID = (int) VM_List.get(i).getId();
						int placedHost = vmToHostMapDaas.get(i);
						vmToHostMap.put(VmID, placedHost);		
					}
					vmToHostMapDaas = vmToHostMap;
				}
			}
	
		public	Map<Integer, Integer> getvmToHostMapDaas(){
			return vmToHostMapDaas;
		}
		 
		public boolean GAChooseSourceMap;
		DynamicPopulationDAAS bestPopulation = new DynamicPopulationDAAS(Dynamic_GENERATIONS);
	 /**
	  * Constructor for Dynamic VM Allocation
	  * @param sourceAllocationMap
	  * @param vmCurrentCpuChar
	  * @param vmCurrentRAMChar
	  */
		public Map<Long, Long> DynamicGeneticAlgorithmDriverDaas(Map<Long,Long> sourceAllocationMap, List<Long> targetHostsList,
				 List<Host> hostList, List<Vm> vmList ) {
			 dynamicVmHostMap = sourceAllocationMap;
			 sourcevmList = new ArrayList<Long>(sourceAllocationMap.keySet());
			 sourcehostList = new ArrayList<Long>(sourceAllocationMap.values());
			 targetHostList = new ArrayList<Long>(targetHostsList);
			 hostListDAAS = hostList;
			 vmListDAAS = vmList;
			 GAChooseSourceMap = false;
			 DynamicEvolutionDAAS gen = new DynamicEvolutionDAAS();
			 initialPopulation = new DynamicPopulationDAAS(Dynamic_POPULATION_SIZE).intialize(Dynamic_POPULATION_SIZE);
	
			 int generationNumber = 0;
			 
				while(generationNumber <= Dynamic_GENERATIONS) {
					generationNumber++;
					DynamicPopulationDAAS population = gen.evolve(initialPopulation);
					population.sortChromosomesByFitness();
					
					 /* remove old population and add offspring to the new population. */
					 
					initialPopulation = new DynamicPopulationDAAS(Dynamic_POPULATION_SIZE);
	
					IntStream.range(0, population.chromosomes.size()).forEach(i ->
					initialPopulation.chromosomes.add(i, population.chromosomes.get(i)));		
					
					
					 /* select best individual to place the selected vms from the overloaded/underloaded hosts */
					Exit:
					for(int i=0 ; i < population.chromosomes.size(); i++) {
						if(population.getChromosomes().get(i).SLA_VIOLATIONS_IN_CHROMOSOME == 0) {
							bestPopulation.getChromosomes().add(population.getChromosomes().get(i));
//							bestFinalDynamic = population.getChromosomes().get(i);	 
	//						serverVmMapDynamic = bestFinalDynamic.getServerwithVMList();
		//					bestDynamicVmServerMap = bestFinalDynamic.getGenes();
							break Exit;
						}
					}
				Here:	
					 if((bestPopulation.chromosomes.isEmpty())  && (Dynamic_GENERATIONS == generationNumber)) {
						 if(Dynamic_GENERATIONS >= 80) {
							 bestDynamicVmServerMap = sourceAllocationMap;
							 GAChooseSourceMap = true;
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
