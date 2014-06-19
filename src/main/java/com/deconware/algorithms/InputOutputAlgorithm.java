package com.deconware.algorithms;
import net.imglib2.algorithm.MultiThreadedBenchmarkAlgorithm;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.img.Img;
import net.imglib2.RandomAccessibleInterval;

import net.imglib2.type.numeric.RealType;

/**
 * abstract high level class that implements output algorithm 
 * 
 * @author bnorthan
 *
 * @param <T>
 */
public abstract class InputOutputAlgorithm<T extends RealType<T>, S> extends MultiThreadedBenchmarkAlgorithm implements OutputAlgorithm<S>
{
	protected RandomAccessibleInterval<T> input;
	protected S output;
	//protected Img<T> output;
	
	String errorMessage="";
	int numThreads;
	long processingTime;
	
	public abstract boolean process(); 
	
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
	public S getResult()   
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
}
