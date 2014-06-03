package com.deconware.algorithms.fft.filters;


import net.imglib2.RandomAccessibleInterval;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Interface for an iterative deconvolution filter
 * 
 * @author bnorthan
 *
 * @param <T>
 * @param <S>
 */
public interface IterativeFilter<T extends RealType<T>, S extends RealType<S>> extends FrequencyFilter<T, S>
{
	/**
	 * first guess types 
	 *
	 * MEASURED - the measured image (the input image)  
	 * CONSTANT - flat sheet
	 * BLURRED_INPUT - blurred version of the measured input image
	 * USER_IMAGE - user specifies first guess
	 */
	public static enum FirstGuessType{MEASURED, CONSTANT, BLURRED_MEASURED, USER_INPUT, INVERSE_FILTER};
	
	/**
	 * convolution strategy
	 *
	 * circulant - convolution and correlation performed using circulant model
	 * 
	 * noncirculant - images have been zero extended as to avoid circular deconvolution.  
	 * 	http://bigwww.epfl.ch/deconvolution/challenge/index.html?p=documentation/overview
	 * 
	 * seminoncirculant - images have been zero extended as to avoid circular deconvolution
	 * 		from the highest intensity region of the PSF.  
	 */
	public static enum ConvolutionStrategy{CIRCULANT, NON_CIRCULANT, SEMI_NONCIRCULANT};
	
	/**
	 * 
	 * 
	 * @author bnorthan
	 *
	 */
	public static enum AccelerationStrategy{NONE, VECTOR, MULTIPLICATIVE_VECTOR};

	/**
	 * return the current estimate
	 */
	public Img<T> getEstimate();
	
	/**
	 * Explicitly set the image estimate 
	 */
	public void setEstimate(RandomAccessibleInterval<T> estimate);
	
	/**
	 * set the maximum number of iterations 
	 * the algorithm will stop at this iteration no matter what.
	 * the algorithm may stop at an early iteration if an alternative 
	 * stopping criteria is used
	 */
	public void setMaxIterations(int maxIterations);
	
	// TODO add means to set stopping criteria
	
	/**
	 * set a callback for status updates
	 */
	public void setCallback(IterativeFilterCallback<T> callback);
	
	/**
	 * set first guess type
	 */
	public void setFirstGuessType(FirstGuessType firstGuessType);
	
	/**
	 * set acceleration strategy
	 */
	public void setAccelerationType(AccelerationStrategy accelerationStrategy);
	
	/**
	 * set known "truth" image.  This is useful for evaluating results when using a simulation 
	 */
	public void setTruth(Img<T> truth);
	
	/**
	 * return the reblurred image
	 */
	public Img<T> getReblurred();
	
	/**
	 * initialize the filter
	 */
	public boolean initialize();
	
	/**
	 * perform n iterations
	 * @param n
	 * @return true if successful
	 */
	public boolean performIterations(int n);
		
	/**
	 * set up for non-circulant convolution model.  the "measurement window size" and the
	 * "psf window size" are needed to calculate the normalization factor
	 *  
	 * @param k - measurement window size
	 * @param l - psf window size
	 *
	 * return true if the algorithm supports non-circulant mode, false otherwise.
	 */
	public boolean setNonCirculantConvolutionStrategy(long[] k, long[] l);
			
}
