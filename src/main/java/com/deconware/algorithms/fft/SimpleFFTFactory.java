package com.deconware.algorithms.fft;

import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

/**
 * factory for creating a simple fft
 * 
 * @author bnorthan
 *
 * @param <T>
 * @param <S>
 */
public class SimpleFFTFactory<T extends RealType<T>, S extends ComplexType<S>>
{
	/**
	 * 
	 * creates and returns an appropriate simple fft
	 * 
	 * @param image
	 * @param imgImgFactory
	 * @param fftImgFactory
	 * @param complexType
	 * @return
	 */
	public static <T extends RealType<T>, S extends ComplexType<S>> SimpleFFT<T,S> GetSimpleFFt(RandomAccessibleInterval<T> image, ImgFactory<T> imgImgFactory, ImgFactory<S> fftImgFactory, final S complexType)
	{
		// return the imglib2 fft2...
		return new SimpleImgLib2FFT2<T,S>(image, imgImgFactory, fftImgFactory, complexType);
		//return new SimpleImgLib2FFT<T,S>(image, imgImgFactory, fftImgFactory, complexType);
		
		// Todo:
		// this function could be expanded to return different types of fft depending on operating system, hardware
		// etc. 
	}
	
	/**
	 * returns the padded size for the fft implementation.
	 * 
	 * @param dimensions
	 * @return
	 */
	public static int[] GetPaddedInputSize(long[] dimensions)
	{
		
		long[] paddedDimensions = GetPaddedInputSizeLong(dimensions);
		
		int[] intPaddedDimensions = new int[paddedDimensions.length];
		
		for (int i=0;i<paddedDimensions.length;i++)
		{
			intPaddedDimensions[i]=(int)paddedDimensions[i];
		}
		
		return intPaddedDimensions;
	}
	
	/**
	 * returns the padded size for the fft implementation.
	 * 
	 * @param dimensions
	 * @return
	 */
	public static long[] GetPaddedInputSizeLong(long[] dimensions)
	{
		
		long[] paddedDimensions = new long[dimensions.length];
		long[] fftDimensions = new long[dimensions.length];
		
		FFTMethods.dimensionsRealToComplexFast( FinalDimensions.wrap(dimensions), paddedDimensions, fftDimensions );
		
		return paddedDimensions;
	}
}
