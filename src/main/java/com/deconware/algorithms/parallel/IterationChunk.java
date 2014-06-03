package com.deconware.algorithms.parallel;

import net.imglib2.type.numeric.NumericType;

/**
 * 
 * Interface for simple parallel iteration
 * 
 * @author bnorthan
 *
 */
public interface IterationChunk<T extends NumericType<T>>
{
	void execute(long startIndex, int stepSize, long numSteps);
}
