package com.deconware.algorithms.fft.filters;

import net.imglib2.img.Img;
import net.imglib2.RandomAccessibleInterval;

import com.deconware.algorithms.StaticFunctions;

import net.imglib2.type.numeric.RealType;

/**
 * Class used to calculate statistics that are relevant to deconvolution algorithms
 * 
 * @author bnorthan
 *
 * @param <T>
 */
public class DeconvolutionStats<T extends RealType<T>> 
{
	// arrays for various statistics
	double[] arraySumEstimate; 
	double[] arrayPowerEstimate;
	double[] arrayStdEstimate;
	double[] arrayTvEstimate;
	double[] arrayRelativeChange;
	double[] arrayLikelihood;
	double[] arrayError;
	double[] arrayMax;
	
	public DeconvolutionStats(int numIterations)
	{
		InitializeStatsArrays(numIterations);
	}
	
	public void CalculateStats(int iteration, RandomAccessibleInterval<T> image, Img<T> estimate, Img<T> reblurred, Img<T> lastEstimate, Img<T> truth, Img<T> psf, boolean printStats)
	{
		if (truth!=null)
		{
			arrayError[iteration]=StaticFunctions.squaredError(estimate, truth);
		}
		
		arraySumEstimate[iteration] = StaticFunctions.sum(estimate);
		arrayPowerEstimate[iteration] = StaticFunctions.squaredSum(estimate);
		arrayStdEstimate[iteration] = StaticFunctions.standardDeviation(estimate);
		arrayTvEstimate[iteration] = StaticFunctions.totalVariation(estimate);
		
		if (lastEstimate!=null)
		{
			arrayRelativeChange[iteration] = StaticFunctions.relativeChange(lastEstimate, estimate);
		}
		
		if (reblurred!=null)
		{
			arrayLikelihood[iteration] = StaticFunctions.likelihood(image, reblurred);
		}
		
		arrayMax[iteration]=StaticFunctions.getMax(estimate);
		
		if (printStats)
		{
			if (truth!=null)
			{
				System.out.println("Squared Error is: "+arrayError[iteration]);
			}
			
			System.out.println("Sum is: "+arraySumEstimate[iteration]);
			System.out.println("Power is: "+arrayPowerEstimate[iteration]);
			System.out.println("Std is: "+arrayStdEstimate[iteration]);
			System.out.println("Tv is: "+arrayTvEstimate[iteration]);
			
			if (lastEstimate!=null)
			{
				System.out.println("Relative change is: "+arrayRelativeChange[iteration]);
			}
			
			System.out.println("Likelihood is: "+arrayLikelihood[iteration]);
			System.out.println("Max is: "+arrayMax[iteration]);
		}
	}
	
	private void InitializeStatsArrays(int maxIterations)
	{
		arraySumEstimate=new double[maxIterations]; 
		arrayPowerEstimate=new double[maxIterations];
		arrayStdEstimate=new double[maxIterations];
		arrayTvEstimate=new double[maxIterations];
		arrayRelativeChange=new double[maxIterations];
		arrayLikelihood=new double[maxIterations];
		arrayError = new double[maxIterations];
		arrayMax = new double[maxIterations];	
	}
	
	public double[] getArraySumEstimate()
	{
		return arraySumEstimate; 
	}
	
	public double[] getArrayPowerEstimate()
	{
		return arrayPowerEstimate;
	}
	
	public double[] getArrayStdEstimate()
	{
		return arrayStdEstimate;
	}
	
	public double[] getArrayTvEstimate()
	{
		return arrayTvEstimate;
	}
	
	public double[] getArrayRelativeChange()
	{
		return arrayRelativeChange;
	}
	
	public double[] getArrayLikelihood()
	{
		return arrayLikelihood;
	}
	
	public double[] getArrayError()
	{
		return arrayError;
	}
	
	public double[] getArrayMax()
	{
		return arrayMax;
	}

}
