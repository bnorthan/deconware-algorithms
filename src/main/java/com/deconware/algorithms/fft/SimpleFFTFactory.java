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
	 * Extension is often used in preparation for FFT filtering in which case
	 * it is desired to extend to the nearest fast size.  The nearest fast size
	 * can differ depending on the algorithm and hardware used. 
	 */
	static public enum FFTTarget
	{
		MINES_SPEED, MINES_SIZE, CUFFT, NONE
	}
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
		
		// TODO:  Need to take FFTTarget as an input and return different types of fft
	}
	
	/**
	 * returns the padded size for the fft implementation.
	 * 
	 * @param dimensions
	 * @return
	 */
	public static int[] GetPaddedInputSize(long[] dimensions, FFTTarget target)
	{
		
		long[] paddedDimensions = GetPaddedInputSizeLong(dimensions, target);
		
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
	public static long[] GetPaddedInputSizeLong(long[] dimensions, FFTTarget target)
	{
		long[] paddedDimensions = new long[dimensions.length];
		long[] fftDimensions = new long[dimensions.length];
		
		// 
		if (target==FFTTarget.NONE)
		{
			paddedDimensions=dimensions;
		}
		
		// for CUFFT use power of 2
		else if (target==FFTTarget.CUFFT)
		{
			for (int d=0;d<dimensions.length;d++)
			{
				int i=0;
				long powerOf2=(int)(java.lang.Math.pow(2, i));
				
				while (powerOf2<dimensions[d])
				{
					i++;
					powerOf2=(int)(java.lang.Math.pow(2, i));
				}
				
				paddedDimensions[d]=powerOf2;
			}
		}
		
		// default to Mines fft dimensions
		else
		{
			FFTMethods.dimensionsRealToComplexFast( FinalDimensions.wrap(dimensions), paddedDimensions, fftDimensions );
		}
		
		return paddedDimensions;
	}
}
