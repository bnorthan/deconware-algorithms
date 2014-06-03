package com.deconware.algorithms.noise;

import net.imglib2.img.Img;

import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.algorithm.OutputAlgorithm;

import net.imglib2.type.numeric.RealType;

import net.imglib2.Cursor;

import com.deconware.algorithms.StaticFunctions;
import com.deconware.algorithms.roi.*;

import com.deconware.algorithms.noise.FuzzyMembershipFunction;

public class FuzzyMedianFilter <T extends RealType<T>> implements MultiThreaded, OutputAlgorithm<Img<T>>, Benchmark
{
	String errorMessage="";
	int numThreads;
	long processingTime;
	
	FuzzyMembershipFunction membershipFunction;
	
	Img<T> noisy;
	Img<T> filtered;
	
	Img<T> noisyMinusMedian;
	Img<T> noisyMinusNClosest;
	
	int membershipSize1;
	int membershipSize2;
	
	public FuzzyMedianFilter(FuzzyMembershipFunction membershipFunction, Img<T> noisy)
	{
		this.membershipSize1 = membershipFunction.getMembershipSize1();
		this.membershipSize2 = membershipFunction.getMembershipSize2();
		
		this.membershipFunction = membershipFunction;
		
		this.noisy = noisy;
		
		filtered = noisy.factory().create(noisy, noisy.firstElement());
	}
	
	
	@Override
	public long getProcessingTime() 
	{ 		
		return processingTime; 
	}
		
	@Override
	public void setNumThreads() 
	{ 
		this.numThreads = Runtime.getRuntime().availableProcessors(); 
	}
	
	@Override
	public void setNumThreads( final int numThreads ) 
	{ 
		this.numThreads = numThreads; 
	}

	@Override
	public int getNumThreads() 
	{ 
		return numThreads; 
	}	
	
	@Override
	public Img<T> getResult() 
	{ 
		return filtered; 
	}

	@Override
	public boolean checkInput() 
	{
		return false;
	}
	
	@Override
	public String getErrorMessage()  
	{ 
		return errorMessage; 
	}
	
	@Override
	public boolean process() 
	{
		long size = 3;
		
		long sizeArray[] = new long[noisy.numDimensions()];
		
		for (int i=0;i<noisy.numDimensions();i++)
		{
			sizeArray[i]=size;
		}
		
		// generate median image		
		MedianFilter<T> medianFilter = new MedianFilter<T>(noisy.factory(), noisy.firstElement(), noisy, sizeArray);
		medianFilter.process();
		Img<T> median = medianFilter.getResult();
		
		// generate dif between image and median
		Img<T> difMedian = StaticFunctions.Subtract(noisy, median);
						
		// generate n closest image
		DifClosestFilter<T> difClosestFilter = new DifClosestFilter<T>(noisy, sizeArray);
		difClosestFilter.process();
		Img<T> difClosest = difClosestFilter.getResult();
		
		// loop through all pixels
		Cursor<T> noisyCursor = noisy.cursor();
		Cursor<T> medianCursor = median.cursor();
		Cursor<T> difClosestCursor = difClosest.cursor();
		Cursor<T> filteredCursor = filtered.cursor();
		
		Cursor<T> difMedianCursor = difMedian.cursor();
		
		while(noisyCursor.hasNext())
		{
			noisyCursor.fwd();
			medianCursor.fwd();
			difClosestCursor.fwd();
			filteredCursor.fwd();
			difMedianCursor.fwd();
			
			double val = noisyCursor.get().getRealDouble();
			double med = medianCursor.get().getRealDouble();
			
			//double parameter1 = val-med;
			double parameter1 = difMedianCursor.get().getRealDouble();
			double parameter2 = difClosestCursor.get().getRealDouble();
			
			double fuzzy = membershipFunction.getFuzzyMembership(parameter1, parameter2 );
			
			double filterVal = med+fuzzy*(val-med);
			
			filteredCursor.get().setReal(filterVal);
		}
		
		return true;
	}
}
