package com.deconware.algorithms.noise;

import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.IterableInterval;

import net.imglib2.RandomAccessibleInterval;

import net.imglib2.Cursor;
/*
public class OrderingFilter <T extends RealType<T>> implements MultiThreaded, OutputAlgorithm<Img<T>>, Benchmark
{

	public OrderingFilter(Img<T> input, long[] windowSize)
	{
		this.input=input;
		this.windowSize=windowSize;
	}
	
	String errorMessage="";
	int numThreads;
	long processingTime;
	RandomAccessibleInterval<T> input;
	Img<T> output;
	long[] windowSize;
	
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
		return output; 
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
		IterableInterval<T> iterator = Views.iterable(input);	
		return true;
	}
}
*/