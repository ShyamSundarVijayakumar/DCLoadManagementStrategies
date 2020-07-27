/**
 * 
 */
package hierarchicalarchitecture.localcontrollerwebapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;


/**
 * @author Shyam Sundar V
 *
 */
public class DynamicEvolutionWA {
	  /**  This way we keep the fittest chromosome from one generation to the other 
     * generation unchanged*/
    public final int ELITE_CHROMOSOMES = 1;
    
    
    public final int TOURNAMENT_SELECTION_SIZE = 3; 
    /** the rate of crossover for the algorithm. */
    private final double crossoverRate = 0.5;
    /** the rate of mutation for the algorithm. */
    private final double mutationRate = 0.8;
    private static RandomGenerator randomGenerator = new JDKRandomGenerator();
    
    public Random rand = new Random();
    long crossPoint1;
    long crossPoint2;
	 private List<Host> Host_List = new ArrayList<>();
	 private List<Vm> Vm_List = new ArrayList<>();
	public DynamicPopulationWA evolve(DynamicPopulationWA population, List<Host> hostlist,List<Vm> vmlist) {
		this.Host_List=hostlist;//------------------------------------>
		this.Vm_List=vmlist; 
		return mutatePopulation(crossoverPopulation(population));
	}
	
	
	public DynamicPopulationWA crossoverPopulation(DynamicPopulationWA population) {
		 DynamicPopulationWA crossoverpopulation = new DynamicPopulationWA(population.getChromosomes().size());
		
		/*
		 * Exclude elite chromosomeDAAS
		 */
		 IntStream.range(0,ELITE_CHROMOSOMES).forEach(i -> crossoverpopulation.getChromosomes().add(population.getChromosomes().get(i)));
		
		
		for(int i=ELITE_CHROMOSOMES; i < population.getChromosomes().size(); i++) {
			/*
			 * Selecting Parent chromosomeDAAS to perform crossover
			 */
			if(crossoverRate > Math.random()) {
				DynamicChromosomeWA chromosome1 = selectPopulation(population).sortChromosomesByFitness().getChromosomes().get(0);
				DynamicChromosomeWA chromosome2 = selectPopulation(population).sortChromosomesByFitness().getChromosomes().get(0);		
				crossoverpopulation.getChromosomes().add(crossoverChromosome(chromosome1, chromosome2));
			} else {
				crossoverpopulation.getChromosomes().add(population.getChromosomes().get(i));
			}
		}		
		return crossoverpopulation;
	}
	
	/**
	 * 
	 * @param population
	 * @return
	 */
	public DynamicPopulationWA mutatePopulation(DynamicPopulationWA population) {
		DynamicPopulationWA mutatePopulation = new DynamicPopulationWA(population.getChromosomes().size());
		/*
		 * Exclude elite chromosomeDAAS
		 */
		
		 IntStream.range(0,ELITE_CHROMOSOMES).forEach(i -> mutatePopulation.getChromosomes().add(population.getChromosomes().get(i)));
		 for(int i = ELITE_CHROMOSOMES; i < population.getChromosomes().size(); i++) {
			 if(Math.random() < mutationRate) {
				 mutatePopulation.getChromosomes().add(mutateChromosome(population.getChromosomes().get(i)));				
				}else {
					mutatePopulation.getChromosomes().add(population.getChromosomes().get(i));						
				}
		 }
		
		return mutatePopulation;
	}
	/**
	 * multi-point crossover
	 * @param chromosome1
	 * @param chromosome2
	 * @return
	 */
	public DynamicChromosomeWA crossoverChromosome(DynamicChromosomeWA chromosome1, DynamicChromosomeWA chromosome2) {
		
		DynamicChromosomeWA crossoverChromosome = new DynamicChromosomeWA();
		int RandomVm = randomGenerator.nextInt(GADriverWA.sourcevmList.size());
		crossPoint1 = GADriverWA.sourcevmList.get(RandomVm);
		RandomVm = randomGenerator.nextInt(GADriverWA.sourcevmList.size());
		crossPoint2 = GADriverWA.sourcevmList.get(RandomVm);
		
		// Ensure crosspoints are different...
	    if (crossPoint1 == crossPoint2){
	        if(crossPoint1 == 0){
	        	crossPoint2++;
	        } else {
	        	crossPoint1--;
	        }
	    }
	    // .. and crosspoint1 is lower than crosspoint2
	    //interchanging values 
	    if (crossPoint2 < crossPoint1) {
	        Long temp = crossPoint1;
	        crossPoint1 = crossPoint2;
	        crossPoint2 = temp;
	    }

	    GADriverWA.dynamicVmHostMap.forEach((vm,server) -> {
			if (vm < crossPoint1 || vm > crossPoint2) {
				crossoverChromosome.getGenes().put(vm, chromosome1.getGenes().get(vm));
				if(chromosome1.getGenes().get(vm) == null) {
					System.out.println("null values present");
					crossoverChromosome.getGenes().put(vm, server);
				}
				 
			}	else {
				crossoverChromosome.getGenes().put(vm, chromosome2.getGenes().get(vm));
				if(chromosome1.getGenes().get(vm) == null) {
					crossoverChromosome.getGenes().put(vm, server);
					System.out.println("null values present");
				}
				
			}
	    });
	    crossoverChromosome.getGenes().forEach((vm, server)->{
			if(server == null) {
				System.out.println("null values present");
			}
		});
	    crossoverChromosome.setHostlistAndVmlist(Host_List, Vm_List);
		crossoverChromosome.serverVMMap(crossoverChromosome.getGenes());
//		System.out.println("crossover chromosome....."+crossoverChromosome.getGenes());
		return crossoverChromosome;
	}
	
	/**
	 * Method to apply mutation. We apply mutation based on the mutation rate and 
	 * we mutate vms on the current active hosts 
	 * @param chromosome
	 * @return
	 */
	DynamicChromosomeWA mutateChromosome;//= new DynamicChromosomeWA();
	public DynamicChromosomeWA mutateChromosome(DynamicChromosomeWA chromosome) {
		mutateChromosome = new DynamicChromosomeWA();
//		System.out.println(TestDriver.sourcehostList.get(randomGenerator.nextInt(TestDriver.sourcehostList.size())));
		GADriverWA.dynamicVmHostMap.forEach((vm,server) -> {
			
			if(chromosome.availabitlityViolatedServerVms != null && !chromosome.availabitlityViolatedServerVms.isEmpty()) {			
				if(chromosome.availabitlityViolatedServerVms.contains(vm)) {
					fillgenesForViolatedServers(chromosome, vm, server);	
				}else mutateChromosome.getGenes().put(vm, chromosome.getGenes().get(vm));
			}else { 
				if(Math.random() < mutationRate && LocalControllerWA.VmstoMigrateFromOverloadedAndUnderloadedHosts.contains(Vm_List.get(vm.intValue()))) {
					mutateChromosome.getGenes().put(vm, GADriverWA.targetHostList.get(randomGenerator.nextInt(GADriverWA.targetHostList.size())));
				} else mutateChromosome.getGenes().put(vm, chromosome.getGenes().get(vm));
			}
			
			
			/*for(Long violatedServer : chromosome.availabitlityViolatedServers){
				List<Long> serverVms = chromosome.ServerwithVmlist.get(violatedServer);		
				long vm1 = serverVms.get(0);
				int Host = generateAnotherRandomHost(chromosome,violatedServer);	
				mutateChromosome.genes[vm] = Host;
			}
			
			if(LocalControllerWA.VmstoMigrateFromOverloadedAndUnderloadedHosts.contains(vm)) {
				if(Math.random() < mutationRate) {
					mutateChromosome.getGenes().put(vm, GADriverWA.sourcehostList.get(randomGenerator.nextInt(GADriverWA.sourcehostList.size())));
//					 System.out.println("mutate chromosome....."+mutateChromosome.getGenes());
				} else mutateChromosome.getGenes().put(vm, chromosome.getGenes().get(vm));
			}*/
		});
		mutateChromosome.setHostlistAndVmlist(Host_List, Vm_List);
		mutateChromosome.serverVMMapSource(mutateChromosome.getGenes());
		
		/*
		 * if(mutateChromosome.slaViolations() != 0){ mutateChromosome = new
		 * DynamicChromosomeWA();
		 		while(mutateChromosome.slaViolations() != 0) {
				GADriverWA.dynamicVmHostMap.forEach((vm,server) -> {
					if(LocalControllerWA.VmstoMigrateFromOverloadedAndUnderloadedHosts.contains(vm)) {

						if(Math.random() < mutationRate) {
							mutateChromosome.getGenes().put(vm, GADriverWA.sourcehostList.get(randomGenerator.nextInt(GADriverWA.sourcehostList.size())));
//							 System.out.println("mutate chromosome....."+mutateChromosome.getGenes());
						} else mutateChromosome.getGenes().put(vm, chromosome.getGenes().get(vm));
					}
				});

				mutateChromosome.serverVMMapSource(mutateChromosome.getGenes());
			}
		}*/
		return mutateChromosome;
	}


	/**
	 * @param chromosome
	 * @param vm
	 * @param server
	 */
	private void fillgenesForViolatedServers(DynamicChromosomeWA chromosome, Long vm, Long server) {
		if(LocalControllerWA.VmstoMigrateFromOverloadedAndUnderloadedHosts.stream().anyMatch(Vm -> (Vm.getId() == vm))) {
			List<Vm> ListVm = LocalControllerWA.VmstoMigrateFromOverloadedAndUnderloadedHosts.stream().filter(VM -> (VM.getId()== vm.intValue())).collect(Collectors.toList());
			fillGenesForOverloadedandUnderloadedHostVms(chromosome, ListVm.get(0));		
		}else mutateChromosome.getGenes().put(vm, chromosome.getGenes().get(vm));
	}
	
	/**
	 * @param vm
	 * @param server
	 * @param vm1
	 */
	private void fillGenesForOverloadedandUnderloadedHostVms(DynamicChromosomeWA chromosome, Vm vm1) {
		if(vm1.getMips() == 2500){
			fillGenesForHostsWithHghMips(chromosome, vm1.getId());
		}
		else if(vm1.getMips() < 2500) {
			mutateChromosome.getGenes().put(vm1.getId(), GADriverWA.targetHostList.get(randomGenerator.nextInt(GADriverWA.targetHostList.size())));
		}
	}
	/**
	 * @param vm
	 * @param server
	 */
	private void fillGenesForHostsWithHghMips(DynamicChromosomeWA chromosome, Long vm) {
		if(chromosome.hostsWithHighMips.size() > 1) {			
			mutateChromosome.getGenes().put(vm, chromosome.hostsWithHighMips.get(randomGenerator.nextInt(chromosome.hostsWithHighMips.size())).getId());
		} else {
			mutateChromosome.getGenes().put(vm, chromosome.getGenes().get(vm));
		}
	}
	/** 
	 * @param population
	 * @return
	 */
	public DynamicPopulationWA selectPopulation(DynamicPopulationWA population){
		DynamicPopulationWA tournamentPopulation = new DynamicPopulationWA(TOURNAMENT_SELECTION_SIZE);
		for(int i=0; i < TOURNAMENT_SELECTION_SIZE; i++) {
			tournamentPopulation.getChromosomes().add(population.getChromosomes().get((int)(Math.random()*population.getChromosomes().size())));
		//	tournamentPopulation.getChromosomes().set(i, population.getChromosomes().get((int)(Math.random()*population.getChromosomes().size())));
		}
		tournamentPopulation.sortChromosomesByFitness();
		return tournamentPopulation;
	}
}
