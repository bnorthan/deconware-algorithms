package com.deconware.algorithms.fft.filters;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Interface for frequency filters
 * 
 * @author bnorthan
 *
 * @param <T>
 * @param <S>
 */
public interface FrequencyFilter<T extends RealType<T>, S extends RealType<S>>
	extends MultiThreaded, OutputAlgorithm<Img<T>>, Benchmark
{
	/**
	 * set the kernel
	 * @param kernel
	 */
	public void setKernel(RandomAccessibleInterval<S> kernel);
	
	/**
	 * 
	 * @param flipKernel (flag indicating wether to flip PSF quadrants, needs to be done to put PSF center at 0,0,0)
	 */
	public void setFlipKernel(boolean flipKernel);
	
	/**
	 * @param set up for semi noncirculant convolution with window size of k 
	 */
	public void setSemiNonCirculantConvolutionStrategy(long[] k);
}

