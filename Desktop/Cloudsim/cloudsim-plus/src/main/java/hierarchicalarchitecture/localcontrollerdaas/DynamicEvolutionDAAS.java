package hierarchicalarchitecture.localcontrollerdaas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * This class is dedicated to depict the evolution process of genetic algorithm for dynamic VM placement of DaaS application.
 *  
 * @author Shyam Sundar V
 */
public class DynamicEvolutionDAAS {
	private final int ELITE_CHROMOSOMES = 1;
	private final int TOURNAMENT_SELECTION_SIZE = 3;
	/** the rate of crossover for the algorithm. */
	private final double crossoverRate = 0.5;
	/** the rate of mutation for the algorithm. */
	private final double mutationRate = 0.7;
	private static RandomGenerator randomGenerator = new JDKRandomGenerator();
	private Random rand = new Random();
	long crossPoint1;
	long crossPoint2;
	DynamicChromosomeDAAS mutateChromosome;
	List<Long> VMID = new ArrayList<>();

	public DynamicPopulationDAAS evolve(DynamicPopulationDAAS population) {
		return mutatePopulation(crossoverPopulation(population));
	}

	/**
	 * This method returns the population of chromosomes that consists of chromosomes after crossover. 
	 * 
	 * @param population
	 * @return
	 */
	private DynamicPopulationDAAS crossoverPopulation(DynamicPopulationDAAS population) {
		DynamicPopulationDAAS crossoverpopulation = new DynamicPopulationDAAS(population.getChromosomes().size());
		/*
		 * Exclude elite chromosomeDAAS
		 */
		IntStream.range(0, ELITE_CHROMOSOMES).forEach(i -> crossoverpopulation.getChromosomes().add(population.getChromosomes().get(i)));

		for (int i = ELITE_CHROMOSOMES; i < population.getChromosomes().size(); i++) {
			/*
			 * Selecting Parent chromosomeDAAS to perform crossover
			 */
			if (crossoverRate > Math.random()) {
				DynamicChromosomeDAAS chromosome1 = selectPopulation(population).sortChromosomesByFitness().getChromosomes().get(0);
				DynamicChromosomeDAAS chromosome2 = selectPopulation(population).sortChromosomesByFitness().getChromosomes().get(0);
				crossoverpopulation.getChromosomes().add(crossoverChromosome(chromosome1, chromosome2));
			} else {
				crossoverpopulation.getChromosomes().add(population.getChromosomes().get(i));
			}
		}

		return crossoverpopulation;
	}

	/**
	 * This method returns the population of chromosomes after their mutation process.
	 * 
	 * @param population
	 * @return
	 */
	private DynamicPopulationDAAS mutatePopulation(DynamicPopulationDAAS population) {
		DynamicPopulationDAAS mutatePopulation = new DynamicPopulationDAAS(population.getChromosomes().size());
		/*
		 * Exclude elite chromosomeDAAS
		 */
		IntStream.range(0, ELITE_CHROMOSOMES).forEach(i -> mutatePopulation.getChromosomes().add(population.getChromosomes().get(i)));
		for (int i = ELITE_CHROMOSOMES; i < population.getChromosomes().size(); i++) {
			mutatePopulation.getChromosomes().add(mutateChromosome(population.getChromosomes().get(i)));
		}

		return mutatePopulation;
	}

	/**
	 * multi-point crossover
	 * 
	 * @param chromosome1
	 * @param chromosome2
	 * @return
	 */
	private DynamicChromosomeDAAS crossoverChromosome(DynamicChromosomeDAAS chromosome1, DynamicChromosomeDAAS chromosome2) {
		DynamicChromosomeDAAS crossoverChromosome = new DynamicChromosomeDAAS();
		int RandomVm = randomGenerator.nextInt(GADriverDaas.sourcevmList.size());
		crossPoint1 = GADriverDaas.sourcevmList.get(RandomVm);
		RandomVm = randomGenerator.nextInt(GADriverDaas.sourcevmList.size());
		crossPoint2 = GADriverDaas.sourcevmList.get(RandomVm);

		// Ensure crosspoints are different...
		if (crossPoint1 == crossPoint2) {
			if (crossPoint1 == 0) {
				crossPoint2++;
			} else {
				crossPoint1--;
			}
		}
		// .. and crosspoint1 is lower than crosspoint2

		if (crossPoint2 < crossPoint1) {
			Long temp = crossPoint1;
			crossPoint1 = crossPoint2;
			crossPoint2 = temp;
		}
		
		GADriverDaas.dynamicVmHostMap.forEach((vm, server) -> {
			if (vm < crossPoint1 || vm > crossPoint2) {
				crossoverChromosome.getGenes().put(vm, chromosome1.getGenes().get(vm));
			} else {
				crossoverChromosome.getGenes().put(vm, chromosome2.getGenes().get(vm));
			}
		});
		crossoverChromosome.serverVMMapSource(crossoverChromosome.getGenes());
		return crossoverChromosome;
	}

	/**
	 * Mutation is performed based on the mutation rate. mutate vms that are not yet created on the current active hosts.
	 * 
	 * @param chromosome
	 * @return
	 */
	private DynamicChromosomeDAAS mutateChromosome(DynamicChromosomeDAAS chromosome) {
		mutateChromosome = new DynamicChromosomeDAAS();
		LocalControllerDaas.VmstoMigrateFromOverloadedUnderloadedHosts.forEach(vm -> {
			VMID.add(vm.getId());
		});
		GADriverDaas.dynamicVmHostMap.forEach((vm, server) -> {
			Collections.sort(VMID);

			if (VMID != null) {
				fillGenes(chromosome, vm);
			} else {
				mutateChromosome.getGenes().put(vm, chromosome.getGenes().get(vm));
			}

		});

		mutateChromosome.serverVMMapSource(mutateChromosome.getGenes());
		return mutateChromosome;
	}

	/**
	 * @param chromosome
	 * @param vm
	 */
	private void fillGenes(DynamicChromosomeDAAS chromosome, Long vm) {
		if (VMID.contains(vm)) {
			mutateRandomGene(chromosome, vm);
		} else {
			mutateChromosome.getGenes().put(vm, chromosome.getGenes().get(vm));
		}
	}

	/**
	 * @param chromosome
	 * @param vm
	 */
	private void mutateRandomGene(DynamicChromosomeDAAS chromosome, Long vm) {
		if (Math.random() < mutationRate) {
			mutateChromosome.getGenes().put(vm,
					GADriverDaas.targetHostList.get(rand.nextInt(GADriverDaas.targetHostList.size())));
			VMID.remove(vm);
		} else {
			mutateChromosome.getGenes().put(vm, chromosome.getGenes().get(vm));
		}
	}

	/**
	 * population seleection follows a tournament selection strategy.
	 * 
	 * @param population
	 * @return
	 */
	public DynamicPopulationDAAS selectPopulation(DynamicPopulationDAAS population) {
		DynamicPopulationDAAS tournamentPopulation = new DynamicPopulationDAAS(TOURNAMENT_SELECTION_SIZE);
		for (int i = 0; i < TOURNAMENT_SELECTION_SIZE; i++) {
			tournamentPopulation.getChromosomes().add(population.getChromosomes().get((int) (Math.random() * population.getChromosomes().size())));
		}

		tournamentPopulation.sortChromosomesByFitness();
		return tournamentPopulation;
	}
}
