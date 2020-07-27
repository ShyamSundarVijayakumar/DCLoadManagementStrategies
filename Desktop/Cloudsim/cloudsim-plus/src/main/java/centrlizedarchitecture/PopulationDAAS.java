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
public class PopulationDAAS {
	public ChromosomeDAAS[] chromosomeDAAS;


	//------------------final int numberOfVMs = TestDriver.INITIAL_VMS;
	final int numberOfVMs;
	/*
	 * for test purposes
	 */
	//List<Host> hostsList = new ArrayList<Host>(3);
	//List<Vm> intialVms; //--------------------------> not used anywhere [may be for test purpose]
	//List<Vm> initialVms;
	Map<ChromosomeDAAS, Double> chromosomewithFitness = new HashMap<ChromosomeDAAS,Double>();
	Map<ChromosomeDAAS,Double> sortedPopulation;

	public PopulationDAAS(int length,int noOfvms)  {
		
		chromosomeDAAS = new ChromosomeDAAS[length];
//		intialVms = new ArrayList<Vm>(6);
		this.numberOfVMs = noOfvms;
//		initializePopulation(hostsList, intialVms);
	}


	/*
	 * public void getVms(List<Vm> vmList){ initialVms = new
	 * ArrayList<Vm>(vmList.size()); initialVms.addAll(vmList); //
	 * System.out.println("intialVms....in PopulationDAAS...." + initialVms);
	 * 
	 * }
	 */

	public PopulationDAAS initializePopulation(int numberOfHosts, List<Host> hostList, List<Vm> vmList) {
		for (int i=0; i < chromosomeDAAS.length; i++) {
			/*
			 * intialize a chromosome with a size of vms
			 */
			chromosomeDAAS[i] = new ChromosomeDAAS(numberOfVMs).initializeChromosome(numberOfHosts, hostList, vmList);
		}
		sortChromosomesByFitness();
		return this;
		
		}

	public ChromosomeDAAS[] getChromosomes() {
		
		return chromosomeDAAS;
	}


	public int getPopulationSize() {
		return this.chromosomeDAAS.length;
	}

	public Map<ChromosomeDAAS,Double> getSortedPopulation(){
		return sortedPopulation;
	}

	/**
	 * make a map for chromosomeDAAS and their fitness values;
	 * sort them in ascending order of their fitness values
	 * @param chromosomeDAAS
	 * @return 
	 */
	public void sortChromosomesByFitness() {
		 Arrays.sort(chromosomeDAAS, (chromosome1, chromosome2) ->{
	         int flag = 0;
	         if(chromosome1.getFitness() > chromosome2.getFitness()) {
	        	 flag = 1;
	         }  else { 
	        	 flag = -1;
	         }
	         return flag;
	        });
		
		 sortChromosomeByFitness();
	}

	/**
	 * A method to store the map of sorted chromosomeDAAS and their fitness values.
	 * @return
	 */
	private Map<ChromosomeDAAS,Double> sortChromosomeByFitness() {
		for(int i=0; i< chromosomeDAAS.length; i++) {
			chromosomewithFitness.put(this.getChromosomes()[i], this.getChromosomes()[i].getFitness());
		}
			 sortedPopulation = chromosomewithFitness.entrySet().stream().sorted(Map.Entry.<ChromosomeDAAS, Double>comparingByValue())//.reversed()
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e2,LinkedHashMap::new));
//		 System.out.println("chromosome+fitness...."+sortedPopulation);
	 return sortedPopulation;
	}
}
