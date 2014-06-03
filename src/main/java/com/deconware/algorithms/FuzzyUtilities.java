package com.deconware.algorithms;

/**
 * implements utilities for fuzzy logic calculations
 * 
 * @author bnorthan
 *
 */
public class FuzzyUtilities 
{
	public static double sigmoid(double val, double alpha)
	{
		return 1/(1+java.lang.Math.exp(-alpha*val));
	}
}
