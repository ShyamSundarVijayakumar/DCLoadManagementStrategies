/**
 * 
 */
package hierarchicalarchitecture.localcontrollerwebapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * @author Shyam Sundar V
 *
 */
public class EvolutionWA {

	public int generationsEvolved;
	private static RandomGenerator randomGenerator = new JDKRandomGenerator();
	    /** the rate of crossover for the algorithm. */
	    private final double crossoverRate = 0.5;
	    /** the rate of mutation for the algorithm. */
	    private final double mutationRate = 0.8;
	    /** Inital placement elitism. This way we keep the fittest chromosome from one generation to the other 
	     * generation unchanged*/
	    public final int ELITE_CHROMOSOMES = 1;
	    
	    public final int TOURNAMENT_SELECTION_SIZE = 4; 
	    
	    public final int CHROMOSOME_LENGTH;//--------------------------> = TestDriver.INITIAL_VMS;

	 public EvolutionWA(int INITIAL_VMS) {
		 this.CHROMOSOME_LENGTH = INITIAL_VMS;
	 }
	 
	 private List<Host> Host_List = new ArrayList<>();
	 private List<Vm> Vm_List = new ArrayList<>();
	public PopulationWA evolve(PopulationWA PopulationWA, List<Host> hostlist,List<Vm> vmlist) {
//			System.out.println("evolve");
		this.Host_List=hostlist;//------------------------------------>
		this.Vm_List=vmlist;      
		return mutatePopulation(crossoverPopulation(PopulationWA));
			}
	
	public PopulationWA crossoverPopulation(PopulationWA PopulationWA){
		PopulationWA crossoverpopulation = new PopulationWA(PopulationWA.getChromosomes().length, CHROMOSOME_LENGTH);
		/*
		 * Exclude elite ChromosomeWA
		 */
		for(int i=0; i < ELITE_CHROMOSOMES; i++) {
			crossoverpopulation.getChromosomes()[i] = PopulationWA.getChromosomes()[i];
//			System.out.println("elite_crossover_chromosome"+crossoverpopulation.getChromosomes()[i]);
		}
		for(int i=ELITE_CHROMOSOMES; i < PopulationWA.getChromosomes().length; i++) {
			ChromosomeWA chromosome1 = selectPopulation(PopulationWA).getChromosomes()[0];
			ChromosomeWA chromosome2 = selectPopulation(PopulationWA).getChromosomes()[0];
			crossoverpopulation.getChromosomes()[i] = crossoverChromosome(chromosome1, chromosome2);
		}
		return crossoverpopulation;
	}
	
	
	public PopulationWA mutatePopulation(PopulationWA PopulationWA) {
		PopulationWA mutatepopulation = new PopulationWA(PopulationWA.getChromosomes().length, CHROMOSOME_LENGTH);
		for(int i=0; i < ELITE_CHROMOSOMES; i++) {
			mutatepopulation.getChromosomes()[i] = PopulationWA.getChromosomes()[i];
	//		System.out.println("-------------------------->elite_mutate_chromosome"+mutatepopulation.getChromosomes()[i]);
		}
		/*
		 * mutate all except the elite chromosome from each gen
		 */
		for(int i=ELITE_CHROMOSOMES; i < PopulationWA.getChromosomes().length; i++) {
			if(Math.random() < mutationRate) {
				mutatepopulation.getChromosomes()[i] = mutateChromosome(PopulationWA.getChromosomes()[i]);				
			}else {
				mutatepopulation.getChromosomes()[i] = PopulationWA.getChromosomes()[i];						
			}
		}
	//	mutatepopulation.sortChromosomesByFitness();
		return mutatepopulation;
	}
	
	/**
	 * For initial placement, random gene selection from each parent chromosome
	 * @param chromosome1
	 * @param chromosome2
	 * @return
	 */
	public ChromosomeWA crossoverChromosome(ChromosomeWA chromosome1, ChromosomeWA chromosome2) {
//		System.out.println("crossover chromosome 1......"+ chromosome1);
//		System.out.println("crossover chromosome 2......"+ chromosome2);
		ChromosomeWA crossoverChromosome = new ChromosomeWA(CHROMOSOME_LENGTH);
		for(int i=0; i< chromosome1.getGenes().length; i++) {
			if(Math.random() < crossoverRate) {
			 crossoverChromosome.getGenes()[i] = chromosome1.getGenes()[i];
			 }
		else {
				 crossoverChromosome.getGenes()[i] = chromosome2.getGenes()[i];
			 }
		}
		crossoverChromosome.setHostlistAndVmlist(Host_List, Vm_List);
		crossoverChromosome.makeVMServerMapDuringCrossoverAndMutation(crossoverChromosome.getGenes());
//		System.out.println("crossover chromosome....."+crossoverChromosome.getFitness());
		return crossoverChromosome;
	}
	
	ChromosomeWA mutateChromosomeInput;
	public ChromosomeWA mutateChromosome(ChromosomeWA Chromosome) {
		this.mutateChromosomeInput = Chromosome;
	//	int[] genes;
		ChromosomeWA mutateChromosome = new ChromosomeWA(CHROMOSOME_LENGTH);
		/*	for(int i=0; i< chromosome.getGenes().length; i++) {
			/*
			 * swap genes with random id within the chromosome
			 *
			int index = randomGenerator.nextInt(mutateChromosome.getGenes().length-1);	
			if(Math.random() < mutationRate) {
				Array.set(mutateChromosome.getGenes(), mutateChromosome.getGenes()[i],  mutateChromosome.getGenes()[index]);
			 }	else {
				 mutateChromosome.getGenes()[i] = chromosome.getGenes()[i];
			 }
		}*/
		
		for(int i=0; i < Chromosome.genes.length; i++) {
			mutateChromosome.getGenes()[i] = Chromosome.getGenes()[i];			
		}
		
		for(int violatedServer : Chromosome.availabitlityViolatedServers) {
			List<Integer> serverVms = Chromosome.serverwithVMList.get(violatedServer);		
			int vm = serverVms.get(0);
			int Host = generateAnotherRandomHost(Chromosome,violatedServer);	
			mutateChromosome.genes[vm] = Host;
		}
//		if(Chromosome.availabitlityViolatedServers.size() == 0) {
//			for(int i=0; i< Chromosome.getGenes().length; i++) {				
	//			int index = randomGenerator.nextInt(mutateChromosome.getGenes().length-1);
	//			if(Math.random() < mutationRate) {
	//				 mutateChromosome.getGenes()[i] =  randomGenerator.nextInt(GADriverWA.Hostlist_sizeIP);
		//		}	else {
		//			mutateChromosome.getGenes()[i] = Chromosome.getGenes()[i];
		//		}
		//	}
	//	}
		
//		for(int violatedVm : Chromosome.slaViolatedVms) {
//			int violatedServer = Chromosome.genes[violatedVm];		
//			int Host = generateAnotherRandomHost(Chromosome,violatedServer);
//			mutateChromosome.genes[violatedVm] = Host;
//		}	
		
		
		
		
		
//		Map<Integer, ArrayList<Integer>> serverVMList = Chromosome.serverwithVMList.entrySet().stream().filter(map -> (map.getValue().size() >= 4)).collect(Collectors.toMap(map -> map.getKey(),map -> map.getValue()));
//		List<Integer> serverList = new ArrayList<>();
//		if(!serverVMList.isEmpty()) {
	//		for(final Map.Entry<Integer, ArrayList<Integer>> entry : serverVMList.entrySet()){		
	//			serverList.add(entry.getKey());
//			}
	//	}
		
//		int RandomHost=0;
	//	List<Integer> serverListFinal = new ArrayList<>();
	//	if(!serverList.isEmpty()) {
	//		serverListFinal = pickTwoRandomHostFromList(serverList,serverListFinal);
	//	}else {
	//		RandomHost = pickARandomActiveHost(Chromosome);
	//	}
		
		/*for(int i=0; i <= RandomServersVmList.size(); i++) {
			RandomServersVmList.indexOf(i);
		}*/
		
	//	if(!serverListFinal.isEmpty()) {
	//		for(int i=0; i < Chromosome.genes.length; i++) {
	//			if(serverList.contains(Chromosome.genes[i])) {
	//				int RandomHostForVMs = GenerateRandomHostIgnoringParticularHost(Chromosome,Chromosome.genes[i]);
	//				mutateChromosome.getGenes()[i] = RandomHostForVMs;
				//	chromosome.genes[i] = RandomHostForVMs;
	//			}else {
	//				mutateChromosome.getGenes()[i] = Chromosome.getGenes()[i];
	//			}			
	//		}	
	//	}else {
	//		for(int i=0; i < Chromosome.genes.length; i++) {
	//			if(Chromosome.genes[i] == RandomHost) {
	//				int RandomHostForVMs = GenerateRandomHostIgnoringParticularHost(Chromosome,RandomHost);
	//				mutateChromosome.getGenes()[i] = RandomHostForVMs;
				//	chromosome.genes[i] = RandomHostForVMs;
	//			}else {
	//				mutateChromosome.getGenes()[i] = Chromosome.getGenes()[i];
	//			}			
	//		}	
	//	}
		
		
		mutateChromosome.setHostlistAndVmlist(Host_List, Vm_List);
		mutateChromosome.makeVMServerMapDuringCrossoverAndMutation(mutateChromosome.getGenes());
		
	//	while(mutateChromosome.ResourceAvailabilityViolation() != 0) {
	//		mutateChromosome = new ChromosomeWA(CHROMOSOME_LENGTH);
		//----------------------------------------------------------------------
		//	for(int i=0; i< ChromosomeWA.getGenes().length; i++) {				
	//			int index = randomGenerator.nextInt(mutateChromosome.getGenes().length-1);
		//		if(Math.random() < mutationRate) {
		//			Array.set(mutateChromosome.getGenes(), mutateChromosome.getGenes()[i],  mutateChromosome.getGenes()[index]);
		//		 }	else {
			//		 mutateChromosome.getGenes()[i] = ChromosomeWA.getGenes()[i];
			//	 }
	//		}---------------------------------------------------------------------------------------------
		
	//		RandomHost = GenerateRandomHostIgnoringInactive(ChromosomeWA);
	//		for(int i=0; i < ChromosomeWA.genes.length; i++) {
	//			if(ChromosomeWA.genes[i] == RandomHost) {
	//				int RandomHostForVMs = GenerateRandomHostIgnoringParticularHost(ChromosomeWA,RandomHost);
	//				mutateChromosome.getGenes()[i] = RandomHostForVMs;
				//	chromosome.genes[i] = RandomHostForVMs;
	//			}else {
	//				mutateChromosome.getGenes()[i] = ChromosomeWA.getGenes()[i];
	//			}			
	//		}

	//		mutateChromosome.setHostlistAndVmlist(Host_List, Vm_List);
	//		mutateChromosome.makeVMServerMapDuringCrossoverAndMutation(mutateChromosome.getGenes());
	//	}
		return mutateChromosome;
	}
	
	private int generateAnotherRandomHost(ChromosomeWA ChromosomeWA, int IgnoreThisHost) {
		int RandomHostForGene = ThreadLocalRandom.current().nextInt(0, ChromosomeWA.servers);				
		/*if((IgnoreThisHost == RandomHostForGene) || 
				(!chromosome.vmToServerMap.containsKey(RandomHostForGene))) {
			GenerateRandomHostIgnoringParticularHost(chromosome,IgnoreThisHost);
		}*/
		while((IgnoreThisHost == RandomHostForGene)) {// || (!ChromosomeWA.vmToServerMap.containsKey(RandomHostForGene))) {
			RandomHostForGene = ThreadLocalRandom.current().nextInt(0, ChromosomeWA.servers);
		}
		
		return RandomHostForGene;
	}
	
	/*
	 * private List<Integer> pickTwoRandomHostFromList(List<Integer>
	 * serverList,List<Integer> serverListFinal) { int i=0; serverListFinal= new
	 * ArrayList<>(); while(i < 5){ int RandomHost =
	 * serverList.get(ThreadLocalRandom.current().nextInt(0, serverList.size()));
	 * if(serverListFinal.isEmpty()){ serverListFinal.add(RandomHost); i++; }
	 * 
	 * if(serverList.size() >= 2) {
	 * serverListFinal.add(GenerateRandomHostIgnoringOneParticularHost(RandomHost,
	 * serverList,serverListFinal)); i++; }else { i++; }
	 * 
	 * }
	 * 
	 * return serverListFinal; }
	 */
	
	/*
	 * private int GenerateRandomHostIgnoringOneParticularHost(int RandomHost,
	 * List<Integer> serverList, List<Integer> serverListFinal) {
	 * 
	 * int RandomHost1 =
	 * serverList.get(ThreadLocalRandom.current().nextInt(0,serverList.size()));
	 * while(serverListFinal.contains(RandomHost1)) { int randomnumber
	 * =ThreadLocalRandom.current().nextInt(0,serverList.size()); RandomHost1 =
	 * serverList.get(randomnumber); } return RandomHost1; }
	 */
	
	
	/*
	 * private int pickARandomActiveHost(ChromosomeWA Chromosome) { int RandomHost =
	 * ThreadLocalRandom.current().nextInt(0, Chromosome.servers);
	 * while(!Chromosome.getServersWithVmList().containsKey(RandomHost)) {
	 * RandomHost = ThreadLocalRandom.current().nextInt(0, Chromosome.servers); }
	 * return RandomHost; }
	 */
	
	/** 
	 * @param PopulationWA
	 * @return
	 */
	public PopulationWA selectPopulation(PopulationWA PopulationWA){
		PopulationWA tournamentPopulation = new PopulationWA(TOURNAMENT_SELECTION_SIZE, CHROMOSOME_LENGTH);
		for(int i=0; i < TOURNAMENT_SELECTION_SIZE; i++) {
			tournamentPopulation.getChromosomes()[i] = PopulationWA.getChromosomes()[(int)(Math.random()*PopulationWA.getChromosomes().length)];
		}
		
		tournamentPopulation.sortChromosomesByFitness();
		return tournamentPopulation;
	}
	 /**
     * Set the (static) random generator.
     *
     * @param random random generator
     */
    public static synchronized void setRandomGenerator(final RandomGenerator random) {
        randomGenerator = random;
    }
    /**
     * Returns the (static) random generator.
     *
     * @return the static random generator shared by GA implementation classes
     */
    public static synchronized RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }
/**
 * Returns the crossover rate.
 * @return crossover rate
 */
public double getCrossoverRate() {
    return crossoverRate;
}
/**
 * Returns the mutation rate.
 * @return mutation rate
 */
public double getMutationRate() {
    return mutationRate;
}
/**
 * Returns the number of generations evolved to reach {@link StoppingCondition} in the last run.
 *
 * @return number of generations evolved
 */
public int getGenerationsEvolved() {
    return generationsEvolved;
}	
	
	
}
