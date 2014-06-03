package com.deconware.algorithms.fft.filters;

import com.deconware.algorithms.StaticFunctions;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.IterableInterval;

import net.imglib2.exception.IncompatibleTypeException;

import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;

import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.view.Views;

/**
 * 
 * @author bnorthan
 *
 * Convolution class based loosely on Stephan Preibisch's imglib2 FFT convolution code.  
 * @param <T>
 * @param <S>
 */
public class Convolution<T extends RealType<T>, S extends RealType<S>> extends LinearFilter<T,S>
{
	/**
	 * 
	 * @param img
	 * @param kernel
	 * @return
	 * @throws IncompatibleTypeException
	 */
	public static <T extends RealType<T>, S extends RealType<S>> Img<T> convolve(final Img<T> img, final Img<S> kernel) throws IncompatibleTypeException
	{
		final Convolution<T,S> convolution = new Convolution<T,S>(img, kernel);
		convolution.process();
		return convolution.getResult();
	}
	
	/**
	 * @param img
	 * @param kernel
	 * @param imgFactory
	 * @param kernelImgFactory
	 * @param fftImgFactory
	 * @return
	 */
	public static<T extends RealType<T>, S extends RealType<S>> Img<T> convolve(final RandomAccessibleInterval<T> img, final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory, final ImgFactory<ComplexFloatType> fftImgFactory)
	{
		final Convolution<T,S> convolution = new Convolution<T,S>(img, kernel, imgFactory, kernelImgFactory, fftImgFactory);
		convolution.process();
		return convolution.getResult();
	}
		
	/**
	 * @param img
	 * @param kernel
	 * @param imgFactory
	 * @param kernelImgFactory
	 * @param fftImgFactory
	 * @param begin
	 * @param end
	 * @return
	 */
	public static<T extends RealType<T>, S extends RealType<S>> Img<T> convolve(final RandomAccessibleInterval<T> img, final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory, final ImgFactory<ComplexFloatType> fftImgFactory,
			long[] begin, long[] end)
	{
		RandomAccessibleInterval< T > view =
            Views.interval( img, begin, end );
		
		final Convolution<T,S> convolution = new Convolution<T,S>(view, kernel, imgFactory, kernelImgFactory, fftImgFactory);
		convolution.process();
		return convolution.getResult();
	}

	/**
	 * @param img
	 * @param kernel
	 * @param imgFactory
	 * @param kernelImgFactory
	 * @param fftImgFactory
	 * @param begin
	 * @param end
	 */
	public static<T extends RealType<T>, S extends RealType<S>> void convolveInPlace(final RandomAccessibleInterval<T> img, final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory, final ImgFactory<ComplexFloatType> fftImgFactory,
			long[] begin, long[] end)
	{
		RandomAccessibleInterval< T > view =
            Views.interval( img, begin, end );
		
		final Convolution<T,S> convolution = new Convolution<T,S>(view, kernel, imgFactory, kernelImgFactory, fftImgFactory);
		convolution.process();
		
		RandomAccessibleInterval<T> result =convolution.getResult();
		
		IterableInterval<T> iterableTarget = Views.iterable(view);
		IterableInterval<T> iterableSource = Views.iterable(result);
		
		StaticFunctions.copy(iterableSource, iterableTarget);
		
	} 

	/**
	 * @param image
	 * @param kernel
	 * @param imgFactory
	 * @param kernelImgFactory
	 * @param fftImgFactory
	 */
	public Convolution( final RandomAccessibleInterval<T> image, final RandomAccessibleInterval<S> kernel,
			   final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory,
			   final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, imgFactory, kernelImgFactory, fftImgFactory );
	}

	/**
	 * @param image
	 * @param kernel
	 * @param imgFactory
	 * @param kernelImgFactory
	 * @throws IncompatibleTypeException
	 */
	public Convolution(final RandomAccessibleInterval<T> image, 
			final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory,
			final ImgFactory<S> kernelImgFactory) throws IncompatibleTypeException
	{
		super(image, kernel, imgFactory, kernelImgFactory);
	}

	/**
	 * @param image
	 * @param kernel
	 * @param fftImgFactory
	 */
	public Convolution( final Img<T> image, final Img<S> kernel, final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, fftImgFactory );
	}

	/**
	 * @param image
	 * @param kernel
	 * @throws IncompatibleTypeException
	 */
	public Convolution( final Img< T > image, final Img< S > kernel ) throws IncompatibleTypeException
	{
		super( image, kernel );
	}
	
	/**
	 * Override frequency operation to implement convolution, multiply a and b in the frequency domain 
	 * 
	 * @param a - input signal  
	 * @param b - input signal
	 */
	@Override
	protected void frequencyOperation( final Img< ComplexFloatType > a, final Img< ComplexFloatType > b ) 
	{
		final Cursor<ComplexFloatType> cursorA = a.cursor();
		final Cursor<ComplexFloatType> cursorB = b.cursor();
		
		while ( cursorA.hasNext() )
		{
			cursorA.fwd();
			cursorB.fwd();
		
			cursorA.get().mul( cursorB.get() );
		}
	}
}

