package com.deconware.algorithms.fft;

import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFT;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

/**
 * wraps imglib2 fft2 with simplefft interface
 * 
 * @author bnorthan
 *
 * @param <T>
 * @param <S>
 */
public class SimpleImgLib2FFT2 <T extends RealType<T>, S extends ComplexType<S>> implements SimpleFFT<T,S>
{
	final ImgFactory<S> fftImgFactory;
	
	final S complexType;
	
	Img<T> inverse;
	
	final long[] dimensions;
	final long[] paddedDimensions;
	final long[] fftDimensions;
	
	public SimpleImgLib2FFT2(RandomAccessibleInterval<T> input, ImgFactory<T> imgImgFactory, ImgFactory<S> fftImgFactory, final S complexType)
	{
		
		this.fftImgFactory=fftImgFactory;
		this.complexType = complexType;
		
		int numDimensions = input.numDimensions();
		
		dimensions = new long[ numDimensions ];
		
		for ( int d = 0; d < numDimensions; ++d )
			dimensions[ d ] = input.dimension( d );
		
		paddedDimensions = new long[ numDimensions ];
		fftDimensions = new long[ numDimensions ];
		
		T inputType=Util.getTypeFromInterval(input);
		
		inverse=imgImgFactory.create(dimensions, inputType);
		
	}
	
	@Override 
	public Img<S> forward(final RandomAccessibleInterval<T> input)
	{
		int numProcessors=Runtime.getRuntime().availableProcessors();
		
		// based on current dimensions get the padded-dimensions and fft-dimensions
		FFTMethods.dimensionsRealToComplexFast( FinalDimensions.wrap(dimensions), paddedDimensions, fftDimensions );
		
		// pad from current dimensions to fft dimensions.  
		OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory = new OutOfBoundsMirrorFactory< T, RandomAccessibleInterval<T> >( Boundary.SINGLE );
		
		return FFT.realToComplex( Views.extend(input, outOfBoundsFactory), input, fftImgFactory, complexType, numProcessors);
	}
	
	public Img<T> inverse(final Img<S> fft)
	{
		int numProcessors=Runtime.getRuntime().availableProcessors();
		
		FFT.complexToRealUnpad(fft, inverse, numProcessors);
		
		return inverse;
	}
	
	@Override
	public int[] GetPaddedInputSize(final Img<T> input)
	{
		FFTMethods.dimensionsRealToComplexFast( FinalDimensions.wrap(dimensions), paddedDimensions, fftDimensions );
		
		int[] intPaddedDimensions = new int[paddedDimensions.length];
		
		for (int i=0;i<paddedDimensions.length;i++)
		{
			intPaddedDimensions[i]=(int)paddedDimensions[i];
		}
		
		return intPaddedDimensions;
	}

}
