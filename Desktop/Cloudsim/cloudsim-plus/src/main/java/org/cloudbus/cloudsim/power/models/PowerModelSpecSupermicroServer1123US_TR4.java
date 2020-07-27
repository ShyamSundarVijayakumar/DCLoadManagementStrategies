package org.cloudbus.cloudsim.power.models;

public class PowerModelSpecSupermicroServer1123US_TR4 extends PowerModelSpecPower{

	/**
     * The power consumption according to the utilization percentage.
     * @see #getPowerData(int)
     */

	private final double[] power = { 98.4, 154, 173, 189, 204, 220, 236, 251, 266, 282, 303 };


	@Override
	protected double getPowerData(final int index) {
		return power[index];
	}
}
