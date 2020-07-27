/**
 * 
 */
package hierarchicalarchitecture.localcontrollerwebapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.autoscaling.VerticalVmScaling;
import org.cloudsimplus.autoscaling.VerticalVmScalingSimple;
import org.cloudsimplus.autoscaling.resources.ResourceScaling;
import org.cloudsimplus.autoscaling.resources.ResourceScalingGradual;
import org.cloudsimplus.autoscaling.resources.ResourceScalingInstantaneous;
import org.cloudsimplus.listeners.VmHostEventInfo;


/**
 * @author Shyam Sundar V
 *
 */
public class VerticalVmScalingWA extends VerticalVmScalingSimple{
	
		 private ResourceScaling resourceScaling = VerticalVmScalingSimple.resourceScaling;
		 private Function<Vm, Double> upperThresholdFunction;
		 private Function<Vm, Double> lowerThresholdFunction;
		 
		 public VerticalVmScalingWA(final Class<? extends ResourceManageable> resourceClassToScale, final double scalingFactor){
		        super(resourceClassToScale, scalingFactor);
		        this.setResourceScaling(new ResourceScalingGradual());
		        this.lowerThresholdFunction = VerticalVmScaling.NULL.getLowerThresholdFunction();
		        this.upperThresholdFunction = VerticalVmScaling.NULL.getUpperThresholdFunction();
		        this.setResourceClass(resourceClassToScale);
		        this.setScalingFactor(scalingFactor);
		    }
		    @Override
		    public final VerticalVmScaling setUpperThresholdFunction(final Function<Vm, Double> upperThresholdFunction) {
		        validateFunctions(lowerThresholdFunction, upperThresholdFunction);
		        this.upperThresholdFunction = upperThresholdFunction;
		        return this;
		    }
		    
		    @Override
		    public final VerticalVmScaling setLowerThresholdFunction(final Function<Vm, Double> lowerThresholdFunction) {
		        validateFunctions(lowerThresholdFunction, upperThresholdFunction);
		        this.lowerThresholdFunction = lowerThresholdFunction;
		        return this;
		    }
		    private void validateFunctions(
		            final Function<Vm, Double> lowerThresholdFunction,
		            final Function<Vm, Double> upperThresholdFunction)
		        {
		            Objects.requireNonNull(lowerThresholdFunction);
		            Objects.requireNonNull(upperThresholdFunction);
		            if(upperThresholdFunction.equals(lowerThresholdFunction)){
		                throw new IllegalArgumentException("Lower and Upper utilization threshold functions cannot be equal.");
		            }
		        }
		    final List<Double> ramUtilizationhistory = new ArrayList<>();
		    private int CurrentTime = 0;
		    int previousTime = 0;
			public int nextSchedulinginterval = 300;
		    @Override
		    public final boolean requestUpScalingIfPredicateMatches(final VmHostEventInfo evt) {
		    	CurrentTime = (int) evt.getTime();
		    	if(!isTimeToCheckPredicate(evt.getTime())) {
		            return false;
		        }
		    	
		 //   	if(vmnextCheckTime == CurrentTime) { vmallocatedrecently = false; }
		    	if(vmallocatedrecently == true) { return false; }
				
		    	if((CurrentTime != previousTime)) {
					previousTime = ((int) evt.getTime());
					ramUtilizationhistory.add(getResource().getPercentUtilization());
			        final boolean requestedScaling = (isVmUnderloaded() || isVmOverloaded()) && requestUpScaling(evt.getTime());
			        setLastProcessingTime(evt.getTime());
			        return requestedScaling;
				}else {
					return false;
				}
		        
		    }
		    double[] lastThreeutilizations = {0,0,0,0,0};
		    boolean vmallocatedrecently = false;
		    double vmallocatedtime=0;
		    int vmnextCheckTime = 0;
		    @Override
		    public boolean isVmUnderloaded() {
				/*
				 * if(vmnextCheckTime == CurrentTime) { vmallocatedrecently = false; }
				 * if(vmallocatedrecently == true) { return false; }
				 */
		    	
		    	if((CurrentTime < (5*300)) || (getResource().getCapacity() < 250)) {
		    		return false;
		    	}else {
		    		
		    		for(int i = 0; i < 5 ; i++) {
		    			lastThreeutilizations[i] = ramUtilizationhistory.get((ramUtilizationhistory.size()-1) - i);	
		    		}
		    		
		    		double lowerUtilizationThreshold = lowerThresholdFunction.apply(getVm());
		    		if((lastThreeutilizations[0] < lowerUtilizationThreshold) && (lastThreeutilizations[1] < lowerUtilizationThreshold)
		    				&& (lastThreeutilizations[2] < lowerUtilizationThreshold) && (lastThreeutilizations[3] < lowerUtilizationThreshold)
		    				&& (lastThreeutilizations[4] < lowerUtilizationThreshold)){
		    			vmallocatedrecently = true;
		   // 			vmallocatedtime = CurrentTime;
		    			vmnextCheckTime = CurrentTime + 1500;
		    			return true;
		    				
		    		} else {
		    			return false;
		    		}
		    	}
		    }

		    @Override
		    public boolean isVmOverloaded() {
		    	boolean inMigration = false;
		    	Map<Long, Long> VmServerMap = hierarchicalarchitecture.localcontrollerwebapp.LocalControllerWA.bestDynamicVmServerMap;
				
				/*
				 * if(vmnextCheckTime == CurrentTime) { vmallocatedrecently = false; }
				 * if(vmallocatedrecently == true) { return false; }
				 */
				 
				 if(VmServerMap != null) {
		    		here:
		    		for( Map.Entry<Long, Long> entryDaas : VmServerMap.entrySet()){
						if(getVm().getId() ==  entryDaas.getKey()) {
							if(getVm().getHost().getId() == entryDaas.getValue()){
								inMigration = false;
							}else {
								inMigration = true;
							}
							break here;
						}
					}	
		    	}
		    	
		    	if((CurrentTime < (5*300)) || (!getVm().getHost().getVmsMigratingIn().isEmpty()) || (inMigration)) {
		    		return false;
		    	}else {
		    		for(int i = 0; i < 5 ; i++) {
		    			lastThreeutilizations[i] = ramUtilizationhistory.get((ramUtilizationhistory.size()-1) - i);	
		    		}
		    		double upperthreshold = upperThresholdFunction.apply(getVm());
		    		if((lastThreeutilizations[0] > upperthreshold) && (lastThreeutilizations[1] > upperthreshold)
		    				&& (lastThreeutilizations[2] > upperthreshold) && (lastThreeutilizations[3] > upperthreshold)
		    				&& (lastThreeutilizations[4] > upperthreshold)){
		    			vmallocatedrecently = true;
	//	    			vmallocatedtime = CurrentTime;
		    			vmnextCheckTime = CurrentTime + 1500;	
		    			return true;	
		    		} else {
		    			return false;
		    		}
		    	}
		    }

		    /**
		     * {@inheritDoc}
		     *
		     * <p>If a {@link ResourceScaling} implementation such as
		     * {@link ResourceScalingGradual} or {@link ResourceScalingInstantaneous} are used,
		     * it will rely on the {@link #getScalingFactor()} to compute the amount of resource to scale.
		     * Other implementations may use the scaling factor by it is up to them.
		     * </p>
		     *
		     * <h3>NOTE:</h3>
		     * <b>The return of this method is rounded up to avoid
		     * values between ]0 and 1[</b>. For instance, up scaling the number of CPUs in 0.5
		     * means that half of a CPU should be added to the VM. Since number of CPUs is
		     * an integer value, this 0.5 will be converted to zero, causing no effect.
		     * For other resources such as RAM, adding 0.5 MB has not practical advantages either.
		     * This way, the value is always rounded up.
		     *
		     * @return {@inheritDoc}
		     */
		    @Override
		    public double getResourceAmountToScale() {
		        return Math.ceil(resourceScaling.getResourceAmountToScale(this));
		    }


		    @Override
		    protected boolean requestUpScaling(final double time) {
		        final DatacenterBroker broker = this.getVm().getBroker();
		        //@TODO Previously, the src was the VM and the dest the broker. However, the VM isn't a SimEntity. See if this change brakes anything
		        broker.getSimulation().sendNow(broker, broker, CloudSimTags.VM_VERTICAL_SCALING, this);
		        return true;
		    }

}
