package org.cloudbus.cloudsim.distributions;

import org.apache.commons.math3.distribution.TriangularDistribution;
import org.apache.commons.math3.random.RandomGenerator;

public class TriangularDistr  extends TriangularDistribution implements ContinuousDistribution{
	private long seed;

	
	public TriangularDistr(final double a,final double c,final double b) {
        this(a, c , b, ContinuousDistribution.defaultSeed());
    }
	
	public TriangularDistr(final double a,final double c,final double b, final long seed) {
		this(a, c , b,seed, ContinuousDistribution.newDefaultGen(seed));
	}
	
	 public TriangularDistr(final double a,final double c,final double b, final long seed, final RandomGenerator rng) {
	        super(rng,a,c,b);
	        if(seed < 0){
	            throw new IllegalArgumentException("Seed cannot be negative");
	        }
	        this.seed = seed;
	    }
	  @Override
	    public long getSeed() {
	        return seed;
	    }
	  @Override
	    public void reseedRandomGenerator(final long seed) {
	        super.reseedRandomGenerator(seed);
	        this.seed = seed;
	    }
}
