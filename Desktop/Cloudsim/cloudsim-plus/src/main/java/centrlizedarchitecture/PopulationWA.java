/**
 * 
 */
package centrlizedarchitecture;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * @author Shyam Sundar V
 *
 */
public class PopulationWA {
	public ChromosomeWA[] ChromosomeWA;
	final int numberOfVMs;
	Map<ChromosomeWA, Double> chromosomewithFitness = new HashMap<ChromosomeWA,Double>();
	Map<ChromosomeWA,Double> sortedPopulation;

	public PopulationWA(int length,int noOfvms)  {
		
		ChromosomeWA = new ChromosomeWA[length];
		this.numberOfVMs = noOfvms;
	}


	public PopulationWA initializePopulation(int numberOfHosts, List<Host> hostList, List<Vm> vmList) {
		for (int i=0; i < ChromosomeWA.length; i++) {
			/*
			 * intialize a chromosome with a size of vms
			 */
			ChromosomeWA[i] = new ChromosomeWA(numberOfVMs).initializeChromosome(numberOfHosts, hostList, vmList);
		}
		sortChromosomesByFitness();
		return this;
		
		}

	public ChromosomeWA[] getChromosomes() {
		return ChromosomeWA;
	}


	public int getPopulationSize() {
		return this.ChromosomeWA.length;
	}

	public Map<ChromosomeWA,Double> getSortedPopulation(){
		return sortedPopulation;
	}

	/**
	 * make a map for ChromosomeWA and their fitness values;
	 * sort them in ascending order of their fitness values
	 * @param ChromosomeWA
	 * @return 
	 */
	
	public void sortChromosomesByFitness() {
		Arrays.sort(ChromosomeWA, (chromosome1, chromosome2) ->{     
		  int flag = 0;
	         if(chromosome1.getFitness() > chromosome2.getFitness()) {
	        	 flag = 1;
	         }  else if(chromosome1.getFitness() < chromosome2.getFitness()){ 
	        	 flag = -1;
	         }
	         return flag;
	        });
		sortChromosomeByFitness();
	}

	/**
	 * A method to store the map of sorted ChromosomeWA and their fitness values.
	 * @return
	 */
	public Map<ChromosomeWA,Double> sortChromosomeByFitness() {
		for(int i=0; i< ChromosomeWA.length; i++) {
			chromosomewithFitness.put(this.getChromosomes()[i], this.getChromosomes()[i].getFitness());
		}
			 sortedPopulation = chromosomewithFitness.entrySet().stream().sorted(Map.Entry.<ChromosomeWA, Double>comparingByValue())//.reversed())
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e2,LinkedHashMap::new));
	 return sortedPopulation;
	}
	  
}
