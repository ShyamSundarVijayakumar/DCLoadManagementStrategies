package hierarchicalarchitecture.globalcontroller;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.util.ResourceLoader;
import org.cloudsimplus.slametrics.SlaContract;

import com.google.gson.Gson;

public class SLAContract {
	 private static final String AVAILABILITY = "availability";
	 private static final String HOST_CPU_UTILIZATION = "HostCpuUtilization";
	 private static final String HOST_RAM_UTILIZATION = "HostRamUtilization";
	 private static final String VM_RAM_UTILIZATION = "VmRamUtilization";
	 private static final String WAIT_TIME = "waitTime";
	 private static final String FAULT_TOLERANCE_LEVEL = "faultToleranceLevel";

	    private List<SLAMetric> metrics;

	    /**
	     * Creates a {@link SlaContract}.
	     * If you want to get a contract from a JSON file,
	     * you shouldn't call the constructor directly.
	     * Instead, use some methods of the class methods.
	     *
	     * <p>This constructor is just provided to enable the {@link Gson} object
	     * to use reflection to instantiate a SlaContract.</p>
	     *
	     * @see #getInstance(String)
	     *
	     */
	    public SLAContract() {
	        this.metrics = new ArrayList<>();
	    }

	    /**
	     * Gets an {@link SlaContract} from a JSON file inside the <b>application's resource directory</b>.
	     * @param jsonFilePath the <b>relative path</b> to the JSON file representing the SLA contract to read
	     * @return a {@link SlaContract} read from the JSON file
	     */
	    public static SLAContract getInstance(final String jsonFilePath) {
	        return getInstanceInternal(ResourceLoader.newInputStream(jsonFilePath, SlaContract.class));
	    }

	    /**
	     * Gets an {@link SlaContract} from a JSON file.
	     * Use the available constructors if you want to load a file outside the resource directory.
	     *
	     * @param inputStream a {@link InputStream} to read the file
	     * @return a {@link SlaContract} read from the JSON file
	     */
	    private static SLAContract getInstanceInternal(final InputStream inputStream) {
	        return new Gson().fromJson(new InputStreamReader(inputStream), SLAContract.class);
	    }

	    /**
	     * @return the metrics
	     */
	    public List<SLAMetric> getMetrics() {
	        return metrics;
	    }

	    /**
	     * @param metrics the metrics to set
	     */
	    public void setMetrics(final List<SLAMetric> metrics) {
	        /*Since the contract can be read from a file, the metrics
	        * can be in fact null. This way, instantiates an empty list
	        * instead of using Objects.requiredNonNull().*/
	        this.metrics = metrics == null ? new ArrayList<>() : metrics;
	    }

	    private SLAMetric getSLAMetric(final String metricName) {
	        return metrics
	                .stream()
	                .filter(metric -> metricName.equals(metric.getName()))
	                .findFirst()
	                .orElse(SLAMetric.NULL);
	    }

	    public SLAMetric getAvailabilityMetric() {
	        return getSLAMetric(AVAILABILITY);
	    }

	    public SLAMetric getHostCpuUtilizationMetric() {
	        return getSLAMetric(HOST_CPU_UTILIZATION);
	    }
	   
	    public SLAMetric getHostRamUtilizationMetric() {
	        return getSLAMetric(HOST_RAM_UTILIZATION);
	    }
	    
	    public SLAMetric getVMRamUtilizationMetric() {
	        return getSLAMetric(VM_RAM_UTILIZATION);
	    }
	    
	    public SLAMetric getWaitTimeMetric() {
	        return getSLAMetric(WAIT_TIME);
	    }

	    public SLAMetric getFaultToleranceLevel() {
	        return getSLAMetric(FAULT_TOLERANCE_LEVEL);
	    }

	    public int getMinFaultToleranceLevel() {
	        return (int)Math.floor(getFaultToleranceLevel().getMinDimension().getValue());
	    }
	    
	    @Override
	    public String toString() {
	        return metrics.toString();
	    }

}
