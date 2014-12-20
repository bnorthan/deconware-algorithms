package com.deconware.algorithms.fft.filters;


import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;

/**
 * 
 * Base class for linear frequency filters
 * 
 * @author bnorthan
 *
 * @param <T>
 * @param <S>
 */
public abstract class LinearFilter<T extends RealType<T>, S extends RealType<S>> extends AbstractFrequencyFilter<T,S>
{
	public LinearFilter( final RandomAccessibleInterval<T> image, final RandomAccessibleInterval<S> kernel,
			   final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory,
			   final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, imgFactory, kernelImgFactory, fftImgFactory );
	}
	
	public LinearFilter(final RandomAccessibleInterval<T> image, 
			final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory,
			final ImgFactory<S> kernelImgFactory) throws IncompatibleTypeException
	{
		super(image, kernel, imgFactory, kernelImgFactory);
	}
	
	public LinearFilter(final RandomAccessibleInterval<T> image, 
			final RandomAccessibleInterval<S> kernel,
			final RandomAccessibleInterval<T> output) throws IncompatibleTypeException
	{
		super(image, kernel, output);
	}
	
	public LinearFilter( final Img<T> image, final Img<S> kernel, final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, fftImgFactory );
	}
	
	public LinearFilter( final Img< T > image, final Img< S > kernel ) throws IncompatibleTypeException
	{
		super( image, kernel );
	}
	
	@Override
	public boolean process() 
	{		
		boolean result;
		
		// perform the input fft
		result = performInputFFT();
		
		if (!result)
		{
			return result;
		}
		
		// perform the psf fft
		result = performPsfFFT();
		
		if (!result)
		{
			return result;
		}
				
		// perform the filtering operation in the frequency domain
		frequencyOperation(imgFFT, kernelFFT);
		
		// perform the inverse fft to go back to spatial domain
		fftInput.inverse(imgFFT, outputInterval);
		
		return true;
	}
	
	protected abstract void frequencyOperation( final Img< ComplexFloatType > a, final Img< ComplexFloatType > b );
}
