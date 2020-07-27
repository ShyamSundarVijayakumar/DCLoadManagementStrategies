/**
 * 
 */
package hierarchicalarchitecture.localcontrollerdaas;


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
public class EvolutionDAAS {

	
	public int generationsEvolved;
	private static RandomGenerator randomGenerator = new JDKRandomGenerator();
	    /** the rate of crossover for the algorithm. */
	    private final double crossoverRate = 0.5;
	    /** the rate of mutation for the algorithm. */
	    private final double mutationRate = 0.5;
	    /** Inital placement elitism. This way we keep the fittest chromosome from one generation to the other 
	     * generation unchanged*/
	    private final int ELITE_CHROMOSOMES = 1;
	    
	    private final int TOURNAMENT_SELECTION_SIZE = 4; 
	    
	    private final int CHROMOSOME_LENGTH;

	 public EvolutionDAAS(int INITIAL_VMS) {
		 this.CHROMOSOME_LENGTH = INITIAL_VMS;
		 
	 }
	 private List<Host> Host_List = new ArrayList<>();
	 private List<Vm> Vm_List = new ArrayList<>();
	public PopulationDAAS evolve(PopulationDAAS populationDAAS, List<Host> hostlist,List<Vm> vmlist) {

		this.Host_List=hostlist;
		this.Vm_List=vmlist;      
		return mutatePopulation(crossoverPopulation(populationDAAS));
			}
	
	public PopulationDAAS crossoverPopulation(PopulationDAAS populationDAAS){
		PopulationDAAS crossoverpopulation = new PopulationDAAS(populationDAAS.getChromosomes().length, CHROMOSOME_LENGTH);
		/*
		 * Exclude elite chromosomeDAAS
		 */
		for(int i=0; i < ELITE_CHROMOSOMES; i++) {
			crossoverpopulation.getChromosomes()[i] = populationDAAS.getChromosomes()[i];
		}
		for(int i=ELITE_CHROMOSOMES; i < populationDAAS.getChromosomes().length; i++) {
			ChromosomeDAAS chromosome1 = selectPopulation(populationDAAS).getChromosomes()[0];
			ChromosomeDAAS chromosome2 = selectPopulation(populationDAAS).getChromosomes()[0];
			crossoverpopulation.getChromosomes()[i] = crossoverChromosome(chromosome1, chromosome2);
		}
		return crossoverpopulation;
	}
	
	
	public PopulationDAAS mutatePopulation(PopulationDAAS populationDAAS) {
		PopulationDAAS mutatepopulation = new PopulationDAAS(populationDAAS.getChromosomes().length, CHROMOSOME_LENGTH);
		for(int i=0; i < ELITE_CHROMOSOMES; i++) {
			mutatepopulation.getChromosomes()[i] = populationDAAS.getChromosomes()[i];
		}
		/*
		 * mutate all except the elite chromosome from each gen
		 */
		for(int i=ELITE_CHROMOSOMES; i < populationDAAS.getChromosomes().length; i++) {
			if(Math.random() < mutationRate) {
				mutatepopulation.getChromosomes()[i] = mutateChromosome(populationDAAS.getChromosomes()[i]);				
			}else {
				mutatepopulation.getChromosomes()[i] = populationDAAS.getChromosomes()[i];						
			}
		}
		return mutatepopulation;
	}
	
	/**
	 * For initial placement, random gene selection from each parent chromosome
	 * @param chromosome1
	 * @param chromosome2
	 * @return
	 */
	public ChromosomeDAAS crossoverChromosome(ChromosomeDAAS chromosome1, ChromosomeDAAS chromosome2) {
		ChromosomeDAAS crossoverChromosome = new ChromosomeDAAS(CHROMOSOME_LENGTH);
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
		return crossoverChromosome;
	}
	
	public ChromosomeDAAS mutateChromosome(ChromosomeDAAS Chromosome) {	
		ChromosomeDAAS mutateChromosome = new ChromosomeDAAS(CHROMOSOME_LENGTH);
		int index;
		if(GADriverDaas.GENERATIONS < 60) {
			for(int i=0; i< Chromosome.getGenes().length; i++) {

				if(Chromosome.getGenes().length == 1) {
					index = Chromosome.getGenes().length;
				}else {
					index = randomGenerator.nextInt(mutateChromosome.getGenes().length-1);		
				}			

				if(Chromosome.getGenes().length == 1) {
					mutateChromosome.getGenes()[i] = Chromosome.getGenes()[i];
				}else {
					swapingGenesforVMsnotCreated(Chromosome, mutateChromosome, i, index);	
				}
			}
		}else {
			for(int violatedServer : Chromosome.availabitlityViolatedServers) {
				mutateChromosome = Chromosome;
				  List<Integer> serverVms = Chromosome.serverwithVMList.get(violatedServer);
				  for(Integer VMs : serverVms) {
					  if(!Vm_List.get(VMs).isCreated()) {
						  int Host = generateRandomHostIgnoringSelectedHost(Chromosome,violatedServer);
						  mutateChromosome.genes[VMs] = Host;
					  }
				  }
				 
			}
		}
		mutateChromosome.setHostlistAndVmlist(Host_List, Vm_List);
		mutateChromosome.makeVMServerMapDuringCrossoverAndMutation(mutateChromosome.getGenes());
		
		/*
		 * for(int vs : mutateChromosome.availabitlityViolatedServers) { List<Integer>
		 * serverVms = mutateChromosome.serverwithVMList.get(vs); for(Integer VMs :
		 * serverVms) { if(!Vm_List.get(VMs).isCreated()) {
		 * System.out.println("Reached"); } } }
		 */
		return mutateChromosome;
	}

	/*
	 * private int generateAnotherRandomHost(ChromosomeDAAS Chromosome, int
	 * IgnoreThisHost) { int RandomHostForGene =
	 * ThreadLocalRandom.current().nextInt(0,Host_List.size() );
	 * 
	 * while((IgnoreThisHost == RandomHostForGene)) { RandomHostForGene =
	 * ThreadLocalRandom.current().nextInt(0, Host_List.size()); }
	 * 
	 * return RandomHostForGene; }
	 */
	/**
	 * @param chromosomeDAAS
	 * @param mutateChromosome
	 * @param i
	 * @param index
	 */
	private void swapingGenesforVMsnotCreated(ChromosomeDAAS chromosomeDAAS, ChromosomeDAAS mutateChromosome, int i,int index) {
		if(Vm_List.get(i).isCreated()) {//mutateChromosome.getGenes()[i])
			 mutateChromosome.getGenes()[i] = chromosomeDAAS.getGenes()[i];
		}else {
	//		if(Math.random() < 0.5) {
		//		mutateChromosome.getGenes()[i] = mutateChromosome.getGenes()[index];
				//	Array.set(mutateChromosome.getGenes(), mutateChromosome.getGenes()[i], mutateChromosome.getGenes()[index]);
	//		}else {
		//		mutateChromosome.getGenes()[i] = chromosomeDAAS.getGenes()[i];	
		//	}
			if(Math.random() < 0.5) {
				int RandomHostForGene = ThreadLocalRandom.current().nextInt(0, chromosomeDAAS.SERVERS);	
				mutateChromosome.getGenes()[i] = RandomHostForGene;
			}else {
				mutateChromosome.getGenes()[i] = chromosomeDAAS.getGenes()[i];
			}
		}
	}
	
	private int generateRandomHostIgnoringSelectedHost(ChromosomeDAAS chromosomeDAAS, int IgnoreThisHost) {
		int RandomHostForGene = ThreadLocalRandom.current().nextInt(0, chromosomeDAAS.SERVERS);				

		if(! (chromosomeDAAS.vmToServerMap.keySet().size() == 1)) {
			while((IgnoreThisHost == RandomHostForGene) && chromosomeDAAS.availabitlityViolatedServers.contains(RandomHostForGene)) {//|| (!chromosomeDAAS.vmToServerMap.containsKey(RandomHostForGene)
				RandomHostForGene = ThreadLocalRandom.current().nextInt(0, chromosomeDAAS.SERVERS);
			}
		}else {
			RandomHostForGene = IgnoreThisHost;
		}
		return RandomHostForGene;
	}
//	
//	private int GenerateRandomHostIgnoringInactive(ChromosomeDAAS chromosomeDAAS) {
//		int RandomHost = ThreadLocalRandom.current().nextInt(0, chromosomeDAAS.SERVERS);				
//
//		while((!chromosomeDAAS.getServersWithVmList().containsKey(RandomHost)) && (Host_List.get(RandomHost).getVmList().isEmpty())) {
//			
//			RandomHost = ThreadLocalRandom.current().nextInt(0, chromosomeDAAS.SERVERS);	
//		}
//		return RandomHost;
//	}
	
	/** 
	 * @param populationDAAS
	 * @return
	 */
	public PopulationDAAS selectPopulation(PopulationDAAS populationDAAS){
		PopulationDAAS tournamentPopulation = new PopulationDAAS(TOURNAMENT_SELECTION_SIZE, CHROMOSOME_LENGTH);
		for(int i=0; i < TOURNAMENT_SELECTION_SIZE; i++) {
			tournamentPopulation.getChromosomes()[i] = populationDAAS.getChromosomes()[(int)(Math.random()*populationDAAS.getChromosomes().length)];
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
