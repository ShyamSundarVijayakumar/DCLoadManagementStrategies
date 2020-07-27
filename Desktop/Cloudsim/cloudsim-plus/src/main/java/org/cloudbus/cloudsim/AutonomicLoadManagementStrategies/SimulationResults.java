/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.AutonomicLoadManagementStrategies;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.power.models.PowerAware;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to print the simulation results. This class is still under
 * development
 * 
 * @see SimulationResults#printHostCpuUtilizationAndPowerConsumption(CloudSim
 *      simulation, DatacenterBroker broker, List<Host> hostList)
 *
 */
public class SimulationResults {
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new SimulationResults();
	}

	private boolean showAllHostUtilizationHistoryEntries;

	/**
	 * @param simulation is the created instance of the class {@link CloudSim}
	 *                   through which the simulation engine is launched
	 * @param broker     is the created data center broker used in the simulation
	 * @param hostList   is the created hosts during the simulation
	 * 
	 * @author Abdulrahman Nahhas
	 * @since CloudSim Plus 1.0
	 */
	public void SetHostIdealperiodMapTonew() {
		HostwithIdealperiod = new HashMap<Host, Double>();
	}

	double AllhostpowerKWattsHour;
	public Map<Host, Double> HostwithIdealperiod = new HashMap<Host, Double>();
	public double totalexecutiontimeofallCloudlets = 0;
	public double totalFinishedCloudlets = 0;

	public void printHostCpuUtilizationAndPowerConsumption(CloudSim simulation, DatacenterBroker broker,
			List<Host> hostList) {

		totalFinishedCloudlets = 0;
		totalexecutiontimeofallCloudlets = 0;
		List<Cloudlet> newList = broker.getCloudletFinishedList();
		newList.forEach((cloudlet) -> {
			if (cloudlet.isFinished()) {
				totalFinishedCloudlets += 1;
				totalexecutiontimeofallCloudlets += cloudlet.getExecStartTime();
			}
		});

		new CloudletsTableBuilder(newList).build();
		System.out.println(getClass().getSimpleName() + " finished!");

		/**
		 * Since the utilization history are stored in the reverse chronological order,
		 * the values are presented in this way.
		 */

		for (Host host : hostList) {
			System.out.printf("Host %d CPU utilization and power consumption\n", host.getId());
			System.out.println(
					"-------------------------------------------------------------------------------------------");
			double prevUtilizationPercent = -1, prevWattsPerInterval = -1;
			final Map<Double, DoubleSummaryStatistics> utilizationPercentHistory = host.getUtilizationHistory();

			double totalPowerWattsSec = 0;
			// double time = simulation.clock();
			// time difference from the current to the previous line in the history
			double utilizationHistoryTimeInterval;
			double prevTime = 0;
			for (Map.Entry<Double, DoubleSummaryStatistics> entry : utilizationPercentHistory.entrySet()) {
				utilizationHistoryTimeInterval = entry.getKey() - prevTime;
				final double utilizationPercent = entry.getValue().getSum();

				/**
				 * The power consumption is returned in Watt-second, but it's measured the
				 * continuous consumption before a given time, according to the time interval
				 * defined by {@link #SCHEDULING_INTERVAL} set to the Datacenter.
				 */

				final double wattsSec = host.getPowerModel().getPower(utilizationPercent);

				final double wattsPerInterval = wattsSec * utilizationHistoryTimeInterval;
				if (!(utilizationHistoryTimeInterval > 2000)) {// to ignore the ideal host period (considered as host
																// shut down)
					totalPowerWattsSec += wattsPerInterval;

					if (showAllHostUtilizationHistoryEntries || prevUtilizationPercent != utilizationPercent
							|| prevWattsPerInterval != wattsPerInterval) {
						System.out.printf(
								"\tTime %8.2f | CPU Utilization %6.2f%% | Power Consumption: %8.0f Watt-Second in %f Seconds\n",
								entry.getKey(), utilizationPercent * 100, wattsPerInterval,
								utilizationHistoryTimeInterval);
					}
					// commented for minimizing console output
				} else {
					HostwithIdealperiod.put(host, utilizationHistoryTimeInterval);
				}
				prevUtilizationPercent = utilizationPercent;
				prevWattsPerInterval = wattsPerInterval;
				prevTime = entry.getKey();

			}
			System.out.printf(
					"Total Host %d Power Consumption in %.0f secs: %.2f Watt-Sec (mean of %.2f Watt-Second)\n",
					host.getId(), simulation.clock(), totalPowerWattsSec, totalPowerWattsSec / simulation.clock());
			final double powerWattsSecMean = totalPowerWattsSec / simulation.clock();
	//		AllhostpowerconsumptioninWattsSec += totalPowerWattsSec / simulation.clock();
			System.out.printf("Mean %.2f Watt-Sec for %d usage samples (%.5f KWatt-Hour)\n", powerWattsSecMean,
					utilizationPercentHistory.size(), PowerAware.wattsSecToKWattsHour(powerWattsSecMean));
			System.out.println(
					"-------------------------------------------------------------------------------------------\n");

		}
	}

	public void printHostCpuUtilizationAndPowerConsumptionNew(CloudSim simulation, DatacenterBroker broker,
			List<Host> hostList) {
		totalFinishedCloudlets = 0;
		totalexecutiontimeofallCloudlets = 0;
		List<Cloudlet> newList = broker.getCloudletFinishedList();
		newList.forEach((cloudlet) -> {
			if (cloudlet.isFinished()) {
				totalFinishedCloudlets += 1;
				totalexecutiontimeofallCloudlets += cloudlet.getExecStartTime();
			}
		});

		new CloudletsTableBuilder(newList).build();
		for (final Host host : hostList) {
			printHostCpuUtilizationAndPowerConsumption(host, simulation);
		}
	}

	public void printHostCpuUtilizationAndPowerConsumptionNewDaas(CloudSim simulation, DatacenterBroker broker,
			List<Host> hostList) {
		totalFinishedCloudlets = 0;
		totalexecutiontimeofallCloudlets = 0;
		List<Cloudlet> newList = broker.getCloudletFinishedList();
		newList.forEach((cloudlet) -> {
			if (cloudlet.isFinished()) {
				totalFinishedCloudlets += 1;
				totalexecutiontimeofallCloudlets += cloudlet.getExecStartTime();
			}
		});

		new CloudletsTableBuilder(newList).build();
		for (final Host host : hostList) {
			printHostCpuUtilizationAndPowerConsumptionDaaS(host, simulation);
		}
		// printVmsCpuUtilizationAndPowerConsumption();
	}

	/*
	 * private void printVmsCpuUtilizationAndPowerConsumption() {
	 * 
	 * 
	 * for (Vm vm : vmList) { System.out.println("Vm " + vm.getId() + " at Host " +
	 * vm.getHost().getId() + " CPU Usage and Power Consumption");
	 * System.out.println(
	 * "----------------------------------------------------------------------------------------------------------------------"
	 * ); double vmPower; //watt-sec double utilizationHistoryTimeInterval, prevTime
	 * = 0; final UtilizationHistory history = vm.getUtilizationHistory(); for
	 * (final double time : history.getHistory().keySet()) {
	 * utilizationHistoryTimeInterval = time - prevTime; vmPower =
	 * history.powerConsumption(time); final double wattsPerInterval =
	 * vmPower*utilizationHistoryTimeInterval; System.out.printf(
	 * "\tTime %8.1f | Host CPU Usage: %6.1f%% | Power Consumption: %8.0f Watt-Sec * %6.0f Secs = %10.2f Watt-Sec%n"
	 * , time, history.getHostCpuUtilization(time) *100, vmPower,
	 * utilizationHistoryTimeInterval, wattsPerInterval); prevTime = time; }
	 * System.out.println(); }
	 * 
	 * 
	 * }
	 */

	private void printHostCpuUtilizationAndPowerConsumption(final Host host, CloudSim simulation) {
		System.out.printf("Host %d CPU utilization and power consumption%n", host.getId());
		System.out.println(
				"----------------------------------------------------------------------------------------------------------------------");
		final Map<Double, DoubleSummaryStatistics> utilizationPercentHistory = host.getUtilizationHistory();
		double totalWattsSec = 0;
		double prevUtilizationPercent = -1, prevWattsSec = -1;
		// time difference from the current to the previous line in the history
		double utilizationHistoryTimeInterval;
		double prevTime = 0;
		for (Map.Entry<Double, DoubleSummaryStatistics> entry : utilizationPercentHistory.entrySet()) {
			utilizationHistoryTimeInterval = entry.getKey() - prevTime;
			// The total Host's CPU utilization for the time specified by the map key
			final double utilizationPercent = entry.getValue().getSum();
			final double watts = host.getPowerModel().getPower(utilizationPercent);
			// Energy consumption in the time interval
			final double wattsSec = watts * utilizationHistoryTimeInterval;
			// Energy consumption in the entire simulation time
			totalWattsSec += wattsSec;
			// only prints when the next utilization is different from the previous one, or
			// it's the first one
			if (showAllHostUtilizationHistoryEntries || prevUtilizationPercent != utilizationPercent
					|| prevWattsSec != wattsSec) {
				// System.out.printf(
				// "\tTime %8.1f | Host CPU Usage: %6.1f%% | Power Consumption: %8.0f Watts *
				// %6.0f Secs = %10.2f Watt-Sec%n",
				// entry.getKey(), utilizationPercent * 100, watts,
				// utilizationHistoryTimeInterval, wattsSec);
			}
			prevUtilizationPercent = utilizationPercent;
			prevWattsSec = wattsSec;
			prevTime = entry.getKey();
		}

		System.out.printf("Total Host %d Power Consumption in %.0f secs: %.0f Watt-Sec (%.5f KWatt-Hour)%n",
				host.getId(), simulation.clock(), totalWattsSec, PowerAware.wattsSecToKWattsHour(totalWattsSec));
		final double powerWattsSecMean = totalWattsSec / simulation.clock();
//		AllhostpowerconsumptioninWattsSec += totalWattsSec;
		AllhostpowerKWattsHour += PowerAware.wattsSecToKWattsHour(totalWattsSec);
		System.out.printf("Mean %.2f Watt-Sec for %d usage samples (%.5f KWatt-Hour)%n", powerWattsSecMean,
				utilizationPercentHistory.size(), PowerAware.wattsSecToKWattsHour(powerWattsSecMean));
		System.out.printf(
				"----------------------------------------------------------------------------------------------------------------------%n%n");
	}

	private void printHostCpuUtilizationAndPowerConsumptionDaaS(final Host host, CloudSim simulation) {
		HostwithIdealperiod = new HashMap<Host, Double>();
		System.out.printf("Host %d CPU utilization and power consumption%n", host.getId());
		System.out.println(
				"----------------------------------------------------------------------------------------------------------------------");
		final Map<Double, DoubleSummaryStatistics> utilizationPercentHistory = host.getUtilizationHistory();
		double totalWattsSec = 0;
		double prevUtilizationPercent = -1, prevWattsSec = -1;
		// time difference from the current to the previous line in the history
		double utilizationHistoryTimeInterval;
		double prevTime = 0;
		for (Map.Entry<Double, DoubleSummaryStatistics> entry : utilizationPercentHistory.entrySet()) {
			utilizationHistoryTimeInterval = entry.getKey() - prevTime;
			// The total Host's CPU utilization for the time specified by the map key
			final double utilizationPercent = entry.getValue().getSum();

			if (!(utilizationHistoryTimeInterval > 2000)) {// to ignore the ideal host period (considered as host shut
															// down)
				final double watts = host.getPowerModel().getPower(utilizationPercent);
				final double wattsSec = watts * utilizationHistoryTimeInterval;
				totalWattsSec += wattsSec;

				if (showAllHostUtilizationHistoryEntries || prevUtilizationPercent != utilizationPercent
						|| prevWattsSec != wattsSec) {
					 System.out.printf("\tTime %8.1f | Host CPU Usage: %6.1f%% | Power Consumption: %8.0f Watts *%6.0f Secs = %10.2f Watt-Sec%n",
					 entry.getKey(), utilizationPercent * 100, watts,
					 utilizationHistoryTimeInterval, wattsSec);
				}
				prevUtilizationPercent = utilizationPercent;
				prevWattsSec = wattsSec;
				prevTime = entry.getKey();
			} else {
				HostwithIdealperiod.put(host, utilizationHistoryTimeInterval);
			}
		}

//		System.out.printf(
	//			"Total Host %d Power Consumption in %.0f secs (Ignoring ideal period) : %.0f Watt-Sec (%.5f KWatt-Hour)%n",
//				host.getId(), simulation.clock(), totalWattsSec, PowerAware.wattsSecToKWattsHour(totalWattsSec));
		double powerWattsSecMean =0;
		if((HostwithIdealperiod != null) && HostwithIdealperiod.containsKey(host)) {
			powerWattsSecMean = totalWattsSec / (simulation.clock() - HostwithIdealperiod.get(host));
		}else {
			powerWattsSecMean = totalWattsSec / simulation.clock();
		}
		
//		AllhostpowerconsumptioninWattsSec += totalWattsSec;
		AllhostpowerKWattsHour += PowerAware.wattsSecToKWattsHour(totalWattsSec);

		System.out.printf("Mean %.2f Watt-Sec for %d usage samples (%.5f KWatt-Hour)%n", powerWattsSecMean,
				utilizationPercentHistory.size(), PowerAware.wattsSecToKWattsHour(powerWattsSecMean));
		System.out.printf(
				"----------------------------------------------------------------------------------------------------------------------%n%n");
	}

	public void setdcClusterpowertozero() {
		AllhostpowerKWattsHour = 0;
	//	AllhostpowerconsumptioninWattsSec = 0;
	}

	public double getdcClusterPowerinKWattsHour() {
		// AllhostpowerKWattsHour =
		// PowerAware.wattsSecToKWattsHour(AllhostpowerWattsSecMean);
		return AllhostpowerKWattsHour;
	}
}
