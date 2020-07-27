package hierarchicalarchitecture.globalcontroller;

import java.util.ArrayList;
import java.util.List;

public class SLAMetric {
	
	 public static final SLAMetric NULL = new SLAMetric();
	    private static final SLAMetricDimension DEFAULT_MIN_DIMENSION = new SLAMetricDimension(-1);
	    private static final SLAMetricDimension DEFAULT_MAX_DIMENSION = new SLAMetricDimension(Double.MAX_VALUE);

	    private List<SLAMetricDimension> dimensions;
	    private String name;

	    public SLAMetric(){
	        this("");
	    }

	    public SLAMetric(final String name){
	        this.name = name;
	        this.dimensions = new ArrayList<>();
	    }

	    public List<SLAMetricDimension> getDimensions() {
	        return dimensions;
	    }

	    public SLAMetric setDimensions(List<SLAMetricDimension> dimensions) {
	        this.dimensions = dimensions == null ? new ArrayList<>() : dimensions;
	        return this;
	    }

	    public String getName() {
	        return name;
	    }

	    public SLAMetric setName(String name) {
	        this.name = name == null ? "" : name;
	        return this;
	    }

	    @Override
	    public String toString() {
	        return "Metric{name = " + name + ",  dimensions = " + dimensions + '}';
	    }

	    /**
	     * Gets a {@link SlaMetricDimension} representing the minimum value expected for the metric.
	     * If the {@link SlaMetricDimension#getValue()} is a negative number, it means
	     * there is no minimum value.
	     * @return
	     */
	    public SLAMetricDimension getMinDimension() {
	        return dimensions.stream()
	            .filter(SLAMetricDimension::isMinValue)
	            .findFirst().orElse(DEFAULT_MIN_DIMENSION);
	    }

	    /**
	     * Gets a {@link SlaMetricDimension} representing the maximum value expected for the metric.
	     * If the {@link SlaMetricDimension#getValue()} is equals to {@link Double#MAX_VALUE}, it means
	     * there is no maximum value.
	     * @return
	     */
	    public SLAMetricDimension getMaxDimension() {
	        return dimensions.stream()
	            .filter(SLAMetricDimension::isMaxValue)
	            .findFirst().orElse(DEFAULT_MAX_DIMENSION);
	    }

}
