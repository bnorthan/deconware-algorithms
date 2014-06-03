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
 * Wiener filter
 * 
 * @author bnorthan
 *
 * @param <T>
 * @param <S>
 */
public class WienerFilter<T extends RealType<T>, S extends RealType<S>> extends LinearFilter<T,S>
{
	/**
	 * @param img
	 * @param kernel
	 * @return
	 * @throws IncompatibleTypeException
	 */
	public static <T extends RealType<T>, S extends RealType<S>> Img<T> inverse(final Img<T> img, final Img<S> kernel) throws IncompatibleTypeException
	{
		final WienerFilter<T,S> inverse = new WienerFilter<T,S>(img, kernel);
		inverse.process();
		return inverse.getResult();
	}
	
	/**
	 * @param img
	 * @param kernel
	 * @param imgFactory
	 * @param kernelImgFactory
	 * @param fftImgFactory
	 * @return
	 */
	public static<T extends RealType<T>, S extends RealType<S>> Img<T> inverse(final RandomAccessibleInterval<T> img, final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory, final ImgFactory<ComplexFloatType> fftImgFactory)
	{
		final WienerFilter<T,S> inverse = new WienerFilter<T,S>(img, kernel, imgFactory, kernelImgFactory, fftImgFactory);
		inverse.process();
		return inverse.getResult();
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
	public static<T extends RealType<T>, S extends RealType<S>> Img<T> inverse(final RandomAccessibleInterval<T> img, final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory, final ImgFactory<ComplexFloatType> fftImgFactory,
			long[] begin, long[] end)
	{
		RandomAccessibleInterval< T > view =
            Views.interval( img, begin, end );
		
		final WienerFilter<T,S> inverse = new WienerFilter<T,S>(view, kernel, imgFactory, kernelImgFactory, fftImgFactory);
		inverse.process();
		return inverse.getResult();
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
		
		final WienerFilter<T,S> inverse = new WienerFilter<T,S>(view, kernel, imgFactory, kernelImgFactory, fftImgFactory);
		inverse.process();
		
		RandomAccessibleInterval<T> result =inverse.getResult();
		
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
	public WienerFilter( final RandomAccessibleInterval<T> image, final RandomAccessibleInterval<S> kernel,
			   final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory,
			   final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, imgFactory, kernelImgFactory, fftImgFactory );
	}
	
	/**
	 * 
	 * @param image
	 * @param kernel
	 * @param imgFactory
	 * @param kernelImgFactory
	 * @throws IncompatibleTypeException
	 */
	public WienerFilter(final RandomAccessibleInterval<T> image, 
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
	public WienerFilter( final Img<T> image, final Img<S> kernel, final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, fftImgFactory );
	}

	/**
	 * @param image
	 * @param kernel
	 * @throws IncompatibleTypeException
	 */
	public WienerFilter( final Img< T > image, final Img< S > kernel ) throws IncompatibleTypeException
	{
		super( image, kernel );
	}
	
	double regularizationFactor=1.0;
	
	/**
	 * Wiener Filter - 
	 * 
	 * @param a - 
	 * @param b - 
	 */
	@Override
	protected void frequencyOperation( final Img< ComplexFloatType > a, final Img< ComplexFloatType > b ) 
	{
		final Cursor<ComplexFloatType> cursorA = a.cursor();
		final Cursor<ComplexFloatType> cursorB = b.cursor();
		
		ComplexFloatType zero = new ComplexFloatType();
		zero.setReal(0.0);
		zero.setImaginary(0.0);
		
		ComplexFloatType Pn= new ComplexFloatType();
		Pn.setReal(regularizationFactor);
		Pn.setImaginary(regularizationFactor);
		
		while ( cursorA.hasNext() )
		{
			cursorA.fwd();
			cursorB.fwd();
			
			ComplexFloatType conjA = cursorA.get().copy();
			conjA.complexConjugate();
			
			ComplexFloatType Pf = cursorA.get().copy();
			Pf.mul(conjA);
				
			ComplexFloatType conjB = cursorB.get().copy();
			conjB.complexConjugate();
			
			ComplexFloatType normB = cursorB.get().copy();
			normB.mul(conjB);
			
			float abs = normB.getRealFloat()+normB.getImaginaryFloat();
			abs = (float)java.lang.Math.sqrt(abs);
			
		//	if (abs>0.0)
			{
				ComplexFloatType ratio = Pn.copy();
				ratio.div(Pf);
				
				normB.add(Pn);
				conjB.div(normB);
				cursorA.get().mul(conjB);
			}
		//	else
			{
				cursorA.get().set(zero);
			}
		}
	}
	
	/**
	 * 
	 * @param regularizationFactor
	 */
	public void setRegularizationFactor(double regularizationFactor)
	{
		this.regularizationFactor=regularizationFactor;
	}
	
}

