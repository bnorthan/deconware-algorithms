package com.deconware.algorithms.fft.filters;

import com.deconware.algorithms.StaticFunctions;
import com.deconware.algorithms.fft.SimpleFFT;
import com.deconware.algorithms.fft.SimpleFFTFactory;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;

/**  
 * Implementation of Richardson Lucy algorithm
 * 
 * @author bnorthan
 * 
 * @param <T>
 * @param <S>
 */
public class RichardsonLucyFilter <T extends RealType<T>, S extends RealType<S>> extends AbstractIterativeFilter<T,S>
{
	/**
	 * Constructor
	 */
	public static <T extends RealType<T>, S extends RealType<S>> Img<T> deconvolve(final Img<T> img, final Img<S> kernel, int maxIterations, final Img<T> truth) throws IncompatibleTypeException
	{
		final RichardsonLucyFilter<T,S> rl = new RichardsonLucyFilter<T,S>(img, kernel);
		rl.setMaxIterations(maxIterations);
		rl.setTruth(truth);
		rl.process();
		return rl.getResult();
	}
	
	/**
	 * Constructor
	 */
	public static <T extends RealType<T>, S extends RealType<S>> Img<T> deconvolve(final Img<T> img, final Img<S> kernel, int maxIterations) throws IncompatibleTypeException
	{
		return deconvolve(img, kernel, maxIterations, null);
	}
	
	/**
	 * Constructor
	 */
	public RichardsonLucyFilter( final RandomAccessibleInterval<T> image, final RandomAccessibleInterval<S> kernel,
			   final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory,
			   final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, imgFactory, kernelImgFactory, fftImgFactory );
	}
	
	/**
	 * Constructor
	 */
	public RichardsonLucyFilter( final Img<T> image, final Img<S> kernel, final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, fftImgFactory );
	}
	
	/**
	 * Constructor
	 */
	public RichardsonLucyFilter( final Img< T > image, final Img< S > kernel ) throws IncompatibleTypeException
	{	
		super( image, kernel );
	}
	
	/**
	 * Constructor
	 */
	public RichardsonLucyFilter(final RandomAccessibleInterval<T> image, 
			final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory,
			final ImgFactory<S> kernelImgFactory) throws IncompatibleTypeException
	{
		super(image, kernel, imgFactory, kernelImgFactory);
	}
	
	/**
	 * 
	 * Constructor for the case where the output is passed in as a RandomAccessibleInterval
	 * 
	 * @param image
	 * @param kernel
	 * @param output
	 * @throws IncompatibleTypeException
	 */
	public RichardsonLucyFilter(final RandomAccessibleInterval<T> image,
			final RandomAccessibleInterval<S> kernel,
			final RandomAccessibleInterval<T> output)
			throws IncompatibleTypeException {
		super(image, kernel, output);
	}
	
	/**
	 * Initialize
	 */
	public boolean initialize()
	{
		// create a new fft		
		fftInput = 
				SimpleFFTFactory.GetSimpleFFt(image, imgFactory, fftImgFactory, new ComplexFloatType() );
					
		return super.initialize();
	}
	
	/**
	 * Perform an iteration of the Richardson Lucy algorithm
	 */
	protected boolean performIteration( final Img< ComplexFloatType > a, final Img< ComplexFloatType > b )
	{
		// 1. Reblurred will have allready been created in previous iteration
		
		long startTime=System.currentTimeMillis();
		
		// 2.  divide observed image by reblurred
		StaticFunctions.InPlaceDivide3(reblurred, image);
		
		long divideTime=System.currentTimeMillis()-startTime;
		
		System.out.println("divide time: "+divideTime);
		
		
		// 3. correlate psf with the output of step 2.			
		Img<T> correlation = correlationStep();
		
		if (correlation==null)
		{
			return false;
		}
		
		// compute estimate - 
		// for standard RL this step will multiply output of correlation step and current estimate
		// (Note: ComputeEstimate can be overridden to achieve regularization)
		ComputeEstimate(correlation);
		
		// normalize for non-circulant deconvolution 
		if ( (this.convolutionStrategy==ConvolutionStrategy.NON_CIRCULANT)
				|| (this.convolutionStrategy==ConvolutionStrategy.SEMI_NONCIRCULANT) )
		{
			StaticFunctions.InPlaceDivide2(normalization, estimate);
		}
		
		// TODO: there is an extra create reblurred here!
		// create reblurred so we can use it to calculate likelihood and so it is ready for next time
		createReblurred();
			
		return true;
	}
	
	/**
	 * computes correlation between reblurred and kernel
	 */
	protected Img<T> correlationStep()
	{
		System.out.println("\n---------------------");
		System.out.println("Correlation step: ");
		
		long startTime=System.currentTimeMillis();
		
		SimpleFFT<T, ComplexFloatType> fftTemp = 
				SimpleFFTFactory.GetSimpleFFt(reblurred, imgFactory, fftImgFactory, new ComplexFloatType() );
		
		long createFFTTime=System.currentTimeMillis()-startTime;
		System.out.println("create fft time: "+createFFTTime);
	
		startTime=System.currentTimeMillis();
		Img<ComplexFloatType> reblurredFFT= fftTemp.forward(reblurred);
		long forwardFFTTime=System.currentTimeMillis()-startTime;
		System.out.println("forward fft time: "+forwardFFTTime);
		
		startTime=System.currentTimeMillis();
		
		// complex conjugate multiply fft of output of step 2 and fft of psf.  		
		StaticFunctions.InPlaceComplexConjugateMultiply(reblurredFFT, kernelFFT);
		
		long conjugateMultiplyTime=System.currentTimeMillis()-startTime;
		System.out.println("Conjugate multiply time: "+conjugateMultiplyTime);
		
		return fftInput.inverse(reblurredFFT);
	}
	
	protected void ComputeEstimate(Img<T> correlation)
	{
		StaticFunctions.InPlaceMultiply(estimate, correlation);
	}

}
