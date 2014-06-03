package com.deconware.algorithms.noise;

import net.imglib2.img.Img;

import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.img.Img;

import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;

import net.imglib2.algorithm.stats.ComputeMinMax;

import com.deconware.algorithms.StaticFunctions;
import com.deconware.algorithms.roi.*;
import com.deconware.algorithms.noise.FuzzyMembershipFunction;

import java.io.*;

public class FuzzyMedianFilterTrainer <T extends RealType<T>> implements MultiThreaded, OutputAlgorithm<FuzzyMembershipFunction>, Benchmark
{
	String errorMessage="";
	int numThreads;
	long processingTime;
	
	Img<T> original;
	Img<T> noisy;
	Img<T> filtered;
	
	Img<T> noisyMinusMedian;
	Img<T> noisyMinusNClosest;
	
	FuzzyMembershipFunction fuzzyMembership=null;
	
	double update = 0.000001;
	
	public FuzzyMedianFilterTrainer()
	{
		this(10, 10);
	}
	
	public FuzzyMedianFilterTrainer(int membershipSize1, int membershipSize2)
	{
		fuzzyMembership = new FuzzyMembershipFunction(membershipSize1, membershipSize2);
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
	public FuzzyMembershipFunction getResult() 
	{ 
		return fuzzyMembership; 
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
		
		long filterSizeArray[] = new long[noisy.numDimensions()];
		
		for (int i=0;i<noisy.numDimensions();i++)
		{
			filterSizeArray[i]=size;
		}
		
		// generate median image		
		MedianFilter<T> medianFilter = new MedianFilter<T>(noisy.factory(), noisy.firstElement(), noisy, filterSizeArray);
		medianFilter.process();
		Img<T> median = medianFilter.getResult();
		
		// generate dif between image and median
		Img<T> difMedian = StaticFunctions.Subtract(noisy, median);
				
		// generate n closest image
		DifClosestFilter<T> difClosestFilter = new DifClosestFilter<T>(noisy, filterSizeArray);
		difClosestFilter.process();
		Img<T> difClosest = difClosestFilter.getResult();
					
		ComputeMinMax<T> maxmin = new ComputeMinMax<T>(noisy, noisy.firstElement(), noisy.firstElement());
		ComputeMinMax<T> maxminDifMedian = new ComputeMinMax<T>(difMedian, difMedian.firstElement(), difMedian.firstElement());
		ComputeMinMax<T> maxminDifClosest = new ComputeMinMax<T>(difClosest, difClosest.firstElement(), difClosest.firstElement());
			
		maxmin.process();
		maxminDifMedian.process();
		maxminDifClosest.process();
		
		double min = maxmin.getMin().getRealDouble();
		double max = maxmin.getMax().getRealDouble();
		
		double minDifMedian = maxminDifMedian.getMin().getRealDouble();
		double maxDifMedian = maxminDifMedian.getMax().getRealDouble()*1.001;
		
		double minDifClosest = maxminDifClosest.getMin().getRealDouble();
		double maxDifClosest = maxminDifClosest.getMax().getRealDouble()*1.001;
		
		fuzzyMembership.setMinParameter1(minDifMedian);
		fuzzyMembership.setMaxParameter1(maxDifMedian);
		
		fuzzyMembership.setMinParameter2(minDifClosest);
		fuzzyMembership.setMaxParameter2(maxDifClosest);
		
		System.out.println("min: max:"+min+" : "+max);
		System.out.println("minMVD: maxMVD:"+minDifMedian+" : "+maxDifMedian);
		System.out.println("minDC: maxDC:"+minDifClosest+" : "+maxDifClosest);
				
		double bucketSize1=(maxDifMedian-minDifMedian)/fuzzyMembership.getMembershipSize1();
		double bucketSize2=(maxDifClosest-minDifClosest)/fuzzyMembership.getMembershipSize2();
		
		RandomAccess<T> noisyRandom = noisy.randomAccess();
		RandomAccess<T> originalRandom = original.randomAccess();
		RandomAccess<T> medianRandom = median.randomAccess();
		RandomAccess<T> difClosestRandom = difClosest.randomAccess();
		
		// loop through all pixels
		Cursor<T> noisyCursor = noisy.cursor();
		Cursor<T> originalCursor = original.cursor();
		Cursor<T> medianCursor = median.cursor();
		Cursor<T> difClosestCursor = difClosest.cursor(); 
		
		long imageSizeArray[] = new long[noisy.numDimensions()];
		int numVoxels=1;
		
		for (int i=0;i<noisy.numDimensions();i++)
		{
			imageSizeArray[i]=noisy.dimension(i);
			numVoxels*=noisy.dimension(i);
		}
		
		int numTraining=numVoxels*40;
		
		System.out.println("training bucketSize1: "+bucketSize1);
		System.out.println("training bucketSize2: "+bucketSize2);
	
		for(int i=0;i<numTraining;i++)
		{
			long[] randomPos = new long[noisy.numDimensions()];
			
			// create a random position
			for (int j=0;j<noisy.numDimensions();j++)
			{
				randomPos[j]=(long)(imageSizeArray[j]*java.lang.Math.random());
			}
			
			noisyRandom.setPosition(randomPos);
			originalRandom.setPosition(randomPos);
			medianRandom.setPosition(randomPos);
			difClosestRandom.setPosition(randomPos);
			
			double val = noisyRandom.get().getRealDouble();
			double original = originalRandom.get().getRealDouble();
			double med = medianRandom.get().getRealDouble();
			
			double difMedianValue = val-med-minDifMedian;
			double difClosestValue = difClosestRandom.get().getRealDouble()-minDifClosest;
			
			int bucket1 = (int)(difMedianValue/bucketSize1);
			int bucket2 = (int)(difClosestValue/bucketSize2);
			
			/*System.out.println("");
			
			System.out.println("min: max:"+min+" : "+max);
			System.out.println("minMVD: maxMVD:"+minMVD+" : "+maxMVD);
			System.out.println("minDC: maxDC:"+minDC+" : "+maxDC);
			
			System.out.println("membershipSize1: "+membershipSize1);
			System.out.println("membershipSize2: "+membershipSize2);
		
			System.out.println("value "+val);
			System.out.println("median "+med);
			System.out.println("value minus median"+val_m_med);
			System.out.println("dif "+dif);
			
			System.out.println("buckets: "+bucket1+" "+bucket2);
			System.out.println("Value is: "+val);*/
			
			double fuzzy = fuzzyMembership.getMembershipFunction()[bucket1][bucket2];
			
			double filterVal = med+fuzzy*(val-med);
			
			double thousandRemainder = java.lang.Math.IEEEremainder(i, 1000.0);
			
			if (thousandRemainder==0)
			{
				//System.out.println("Finished "+i+" of "+numTraining);
			}
			
			// update the membership function
			double newMembershipFunction=fuzzyMembership.getMembershipFunction()[bucket1][bucket2]
					+update*(val-med)*(original-filterVal);
			
		/*	System.out.println("");
			System.out.println("stats");
			System.out.println("value "+val);
			System.out.println("median "+med);
			System.out.println("original "+original);
			System.out.println("filterVal "+filterVal);
			System.out.println("old: "+membershipFunction[bucket1][bucket2]);
			System.out.println("new: "+newMembershipFunction);
			System.out.println("");*/
			
			if ( (newMembershipFunction>0.0)&(newMembershipFunction<1.0))
			{
				fuzzyMembership.getMembershipFunction()[bucket1][bucket2]=newMembershipFunction;
			}
		}
		
		return true;
	}
	
	public void setOriginal(Img<T> original)
	{
		this.original=original;
	}
	
	public void SetNoisy(Img<T> noisy)
	{
		this.noisy=noisy;
	}
	
	public void SetUpdate(double update)
	{
		this.update = update;
	}
}
