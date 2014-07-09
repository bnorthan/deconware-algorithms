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
import net.imglib2.Interval;

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
	
	//final long[] dimensions;
	final long[] paddedDimensions;
	final long[] fftDimensions;
	
	public SimpleImgLib2FFT2(RandomAccessibleInterval<T> input, ImgFactory<T> imgImgFactory, ImgFactory<S> fftImgFactory, final S complexType)
	{
		final long dimensions[] = new long[ input.numDimensions() ];
		
		for ( int d = 0; d < input.numDimensions(); ++d )
			dimensions[ d ] = input.dimension( d );
	
		T inputType=Util.getTypeFromInterval(input);
		
		inverse=imgImgFactory.create(dimensions, inputType);
	
		this.fftImgFactory=fftImgFactory;
		this.complexType = complexType;
		
		int numDimensions = input.numDimensions();
	
		paddedDimensions = new long[ numDimensions ];
		fftDimensions = new long[ numDimensions ];	
	}
	
	public SimpleImgLib2FFT2(RandomAccessibleInterval<T> input, ImgFactory<S> fftImgFactory, final S complexType)
	{
		this.fftImgFactory=fftImgFactory;
		this.complexType = complexType;
		
		int numDimensions = input.numDimensions();
	
		paddedDimensions = new long[ numDimensions ];
		fftDimensions = new long[ numDimensions ];
	}
	
	@Override 
	public Img<S> forward(final RandomAccessibleInterval<T> input)
	{
		// compute the size of the complex-valued output and the required
		// padding
		final long[] paddedDimensions = new long[ input.numDimensions() ];
		final long[] fftDimensions = new long[ input.numDimensions() ];

		FFTMethods.dimensionsRealToComplexFast( input, paddedDimensions, fftDimensions );

		// create the output Img
		final Img< S > fft = fftImgFactory.create( fftDimensions, complexType );
	
		int numProcessors=Runtime.getRuntime().availableProcessors();
		
		// if the input size is not the right size adjust the interval
		if ( !FFTMethods.dimensionsEqual( input, paddedDimensions ) )
		{
			Interval inputInterval = FFTMethods.paddingIntervalCentered( input, FinalDimensions.wrap( paddedDimensions ) );
			// real-to-complex fft
			
			// pad from current dimensions to fft dimensions.
			OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory = new OutOfBoundsMirrorFactory< T, RandomAccessibleInterval<T> >( Boundary.SINGLE );

			
			FFT.realToComplex( Views.interval( Views.extend(input, outOfBoundsFactory), inputInterval ), fft, numProcessors );
			
			return fft;
		}
		
		// real-to-complex fft
		FFT.realToComplex( input, fft, numProcessors );
	
		return fft;
	}
	
/*	@Override
public Img<S> forward(final RandomAccessibleInterval<T> input)
{
int numProcessors=Runtime.getRuntime().availableProcessors();

// based on current dimensions get the padded-dimensions and fft-dimensions
FFTMethods.dimensionsRealToComplexFast( FinalDimensions.wrap(dimensions), paddedDimensions, fftDimensions );

// pad from current dimensions to fft dimensions.
OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory = new OutOfBoundsMirrorFactory< T, RandomAccessibleInterval<T> >( Boundary.SINGLE );

return FFT.realToComplex( Views.extend(input, outOfBoundsFactory), input, fftImgFactory, complexType, numProcessors);
}*/
	
	public Img<T> inverse(final Img<S> fft)
	{
		int numProcessors=Runtime.getRuntime().availableProcessors();
		
		FFT.complexToRealUnpad(fft, inverse, numProcessors);
		
		return inverse;
	}
	
	public void inverse(final Img<S> fft, RandomAccessibleInterval<T> out)
	{
		int numProcessors=Runtime.getRuntime().availableProcessors();
		
		FFT.complexToRealUnpad(fft, out, numProcessors);
	}
}
