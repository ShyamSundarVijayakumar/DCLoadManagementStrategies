package centrlizedarchitecture;


public class SLAMetricDimension {
	private static final String MAX_VALUE_NAME ="maxValue";
    private static final String MIN_VALUE_NAME ="minValue";

    private String name;
    private String unit;
    private double value;
   
    public SLAMetricDimension(){
        this(0);
    }

    public SLAMetricDimension(final double value){
        this.name = "";
        this.unit = "";
        setValue(value);
    }

    public String getName() {
        return name;
    }

    public SLAMetricDimension setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the value of the dimension,
     * in absolute or percentage, according to the
     * {@link #getUnit()}.
     *
     * <p>When the unit is "Percent", the values are defined
     * in scale from 0 to 100%, but they are stored in this class
     * in scale from 0 to 1, because everywhere percentage values
     * are defined in this scale.</p>
     * @return
     */
    public double getValue() {
        return isPercent() ? value/100.0 : value;
    }

    public SLAMetricDimension setValue(final double value) {
        this.value = value;
        return this;
    }

    public boolean isMaxValue(){
        return this.name.trim().equals(MAX_VALUE_NAME);
    }

    public boolean isMinValue(){
        return this.name.trim().equals(MIN_VALUE_NAME);
    }

    /**
     * Checks if the unit is defined in percentage values.
     * @return
     */
    public boolean isPercent() {
        return "Percent".equalsIgnoreCase(unit);
    }

    @Override
    public String toString() {
        return String.format(
                    "Dimension{name = %s, value = %s}", name,
                    value == Double.MAX_VALUE ? "Double.MAX_VALUE" : String.format("%.4f", value));
    }

    /**
     * Gets the unit of the dimension, if "Percent" or "Absolute".
     * When the unit is "Percent", the values are defined
     * in scale from 0 to 100%, but they are stored in this class
     * in scale from 0 to 1, because everywhere percentage values
     * are defined in this scale.
     * @return
     */
    public String getUnit() {
        return unit;
    }

    public SLAMetricDimension setUnit(String unit) {
        this.unit = unit;
        return this;
    }
    
}
