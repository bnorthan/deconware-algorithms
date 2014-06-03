package com.deconware.algorithms.parallel;

import net.imglib2.type.numeric.NumericType;

/**
 * interface for parallel reduction (sum, min, dot product etc).
 * 
 * @author bnorthan
 *
 * @param <T>
 */
public interface ReductionChunk<T extends NumericType<T>, S extends NumericType<S>>
{
	/**
	 * 
	 * @param startIndex 
	 * @param stepSize 
	 * @param numSteps
	 * @param out - holds partial result
	 */
	void evaluate(long startIndex, long stepSize, long numSteps, final S out);
	
	void evaluate(final S in, final S out);
	
	void initialize(final S out);
}
