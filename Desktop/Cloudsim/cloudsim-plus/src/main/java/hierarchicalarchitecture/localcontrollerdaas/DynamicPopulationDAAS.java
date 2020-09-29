package hierarchicalarchitecture.localcontrollerdaas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * This class is dedicated for creating the population for genetic algorithm in DaaS application.
 * 
 * @author Shyam Sundar V
 */
public class DynamicPopulationDAAS {

	public ArrayList<DynamicChromosomeDAAS> chromosomes;	
	Map<DynamicChromosomeDAAS, Double> chromosomewithFitness = new HashMap<DynamicChromosomeDAAS,Double>();
	Map<DynamicChromosomeDAAS, Double> sortedPopulation;
	
	public DynamicPopulationDAAS(int populationSize) {
		chromosomes = new ArrayList<DynamicChromosomeDAAS>(populationSize);
	}
	
	public DynamicPopulationDAAS intialize(int populationSize) {
		IntStream.range(0, populationSize).forEach(i ->	chromosomes.add(i, new DynamicChromosomeDAAS().initialize(i)));		
		return this;
	}
	
	public ArrayList<DynamicChromosomeDAAS> getChromosomes() {
		return chromosomes;
	}
	
	public DynamicPopulationDAAS sortChromosomesByFitness() {
		chromosomes.sort((chromosome1, chromosome2) -> {
			int flag = 0;
			if(chromosome1.getFitness() > chromosome2.getFitness()) flag = 1;
			if(chromosome1.getFitness() < chromosome2.getFitness()) flag = -1;
			return flag;
		});
	
		return this;
	}
	
	/**
	 * A map containing sorted chromosomeDAAS with their fitness values. 
	 * @return
	 */
	public Map<DynamicChromosomeDAAS,Double> sortChromosomeByFitness() {
		for(int i=0; i< chromosomes.size(); i++) {
			chromosomewithFitness.put(getChromosomes().get(i), getChromosomes().get(i).getFitness());
		}
		
		sortedPopulation = chromosomewithFitness.entrySet().stream().sorted(Map.Entry.<DynamicChromosomeDAAS, Double>comparingByValue().reversed())
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e2,LinkedHashMap::new));
	 
		return sortedPopulation;
	}

	public Map<DynamicChromosomeDAAS, Double> getSortedPopulation() {
		return sortedPopulation;
	}
	
}
