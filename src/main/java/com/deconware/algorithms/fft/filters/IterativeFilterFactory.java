package com.deconware.algorithms.fft.filters;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

/**
 * 
 * Factory to create iterative filters
 * 
 * @author bnorthan
 *
 */
public class IterativeFilterFactory 
{
	/**
	 * Define iterative filter types
	 *
	 */
	public static enum IterativeFilterType{RICHARDSON_LUCY, TOTAL_VARIATION_RICHARDSON_LUCY};
	
	/**
	 * construct an iterative filter depending on value of type
	 * @param type
	 * @param image
	 * @param kernel
	 * @param imgFactory
	 * @param kernelImgFactory
	 * @return
	 */
	public static <T extends RealType<T>, S extends RealType<S>> IterativeFilter<T,S> GetIterativeFilter(
			IterativeFilterType type,
			final RandomAccessibleInterval<T> image, 
			final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory,
			final ImgFactory<S> kernelImgFactory)
	{
		try
		{
			if (type==IterativeFilterType.RICHARDSON_LUCY)
			{
				return new RichardsonLucyFilter<T,S>(image, kernel, imgFactory, kernelImgFactory);
			}
			if (type==IterativeFilterType.TOTAL_VARIATION_RICHARDSON_LUCY)
			{
				return new TotalVariationRL<T,S>(image, kernel, imgFactory, kernelImgFactory);
			}
		}
		catch (IncompatibleTypeException ex)
		{
			return null;
		}
		
		return null;
	}
}
