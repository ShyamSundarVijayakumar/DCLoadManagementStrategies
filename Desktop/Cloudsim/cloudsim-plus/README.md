# Comparison hierarchical data center load management strategies to central

- **[ModelConstructionForApplication.class](https://github.com/ShyamSundarVijayakumar/DCLoadManagementStrategies/blob/master/Desktop/Cloudsim/cloudsim-plus/src/main/java/ModelConstructionForApplications/ModelConstruction.java)** acts as the main driver class for running the setups. For running the hierarchical architecture comment the `centralArchitecture()` method inside the ModelConstruction method. Simillarly for running the central architeture comment the `hierarchicalArchitecture()` method. For simplicity the central architecture driver method is already commented. 

- The initial and dynamic virtual machine placement problem is resolved using various types of meta-heuristic multi-objective genetic algorithms.

- Two applications, Web application and Desktop as a Service(DaaS) are considered. 
     - For VM's in web application the workload trace files from CoMon project a monitoring infrastructure from planet lab is used to depict the realistic nature of workloads.
     
     - For VM's in DaaS a dynamic utilization model and stochastic utilization model is used.
     
     - To change different workload's from planet lab for each simulation run, change the workload directory in **[RequestAnalyserAndConfigurationManagerWebApplication.java](https://github.com/ShyamSundarVijayakumar/DCLoadManagementStrategies/blob/master/Desktop/Cloudsim/cloudsim-plus/src/main/java/hierarchicalarchitecture/globalcontroller/RequestAnalyserAndConfigurationManagerWebApplication.java)** for hierarchical architecture and in **[RequestAnalyserAndConfigurationManagerWebApplication.java](https://github.com/ShyamSundarVijayakumar/DCLoadManagementStrategies/blob/master/Desktop/Cloudsim/cloudsim-plus/src/main/java/centrlizedarchitecture/RequestAnalyserAndConfigurationManagerWebApplication.java)** for central architecture.
     
##Genetic Algorithm (GA) fundamentals
  - The GA is one of the evolutionary computation algorithms that is studied to have a
robust stochastic search nature that works on the fundamental theory of Darwin’s natural
selection.The functioning of the GA involves the following characteristics:
construction of the initial population, formulating the fitness function and evaluating the
population, parent selection based on the fitness values for the crossover operation, performing
crossover and generating offsprings, modifying genes of the individuals by mutation
and these new individuals selected for the next generation population. This process iterates
for repetitious generations until the possible solution gradually converges to a near-optimal
solution.These population-based optimization
techniques use dominance-based approach to find a Pareto optimal solution. For
instance, a chromosome or an individual is said to be dominant in a population if, according
to the objectives, the chromosome solution is better than the other chromosomes
solutions. These non-dominant solutions are called as Pareto optimal solutions.

### Encoding and construction of the population
- In the encoding step, the fundamental elements of the VMP problem are expressed as
chromosomes and their genes. The group encoding scheme similar to is employed
in this approach. Initially, the population is constructed with multiple chromosomes (individuals)
where each chromosome represents a candidate solution for the optimization
problem. Each chromosome consists of multiple genes within it, in our case genes are considered
to be the hosts and chromosomes are a map consisting of hosts and a list of VM’s
associated with that host. Hosts are given as keys and the value column of a key is associated
with the VM’s allocated to that particular host. [Figure](https://github.com/ShyamSundarVijayakumar/DCLoadManagementStrategies/blob/master/Desktop/Cloudsim/cloudsim-plus/classDiagrams/GAsolutionRepresentation.jpg) shows a diagrammatic
representation of the solution domain including PM’s - VM’s assignment, population, and
chromosome encoding. Where C1, C2,.., Cn in the figure represent the number of chromosomes
in the population. H1, H2,...., Hn represent the number of hosts in an application
and V1, V2,..., Vn represent the number of incoming customers demanded VM’s.

### Structural representation of the framework
- The [images in the directory](https://github.com/ShyamSundarVijayakumar/DCLoadManagementStrategies/tree/master/Desktop/Cloudsim/cloudsim-plus/classDiagrams) shows the structural representation.