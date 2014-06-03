package com.deconware.algorithms.parallel;

import net.imglib2.type.numeric.NumericType;

/**
 * interface for a simple parallel array operation
 * 
 * @author bnorthan
 *
 * @param <T>
 */
public interface SimpleChunk<T extends NumericType<T>>
{
	/**
	 * 
	 * @param startIndex 
	 * @param stepSize 
	 * @param numSteps
	 * @param out - holds partial result
	 */
	void execute(int startIndex, int stepSize, int numSteps);
}