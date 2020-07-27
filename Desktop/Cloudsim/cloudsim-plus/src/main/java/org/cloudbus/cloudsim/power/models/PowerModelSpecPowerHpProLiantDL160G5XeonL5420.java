package org.cloudbus.cloudsim.power.models;

public class PowerModelSpecPowerHpProLiantDL160G5XeonL5420 extends PowerModelSpecPower{
    
   private final double[] power = {140, 159, 167, 175, 184, 194, 204, 213, 220, 227, 233};

   @Override
   protected double getPowerData(final int index) {
       return power[index];
}
}