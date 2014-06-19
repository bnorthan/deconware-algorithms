package com.deconware.algorithms.dim;

import com.deconware.algorithms.InputOutputAlgorithm;

import net.imglib2.RandomAccessible;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.Cursor;

/**
 * algorithm to extend an image
 * 
 * @author bnorthan
 *
 * @param <T>
 */
public class ExtendImage<T extends RealType<T>> extends InputOutputAlgorithm<T, Img<T>>
{
	final int[] outputDim;
	final ImgFactory<T> outputFactory;
	final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory;
	final T type;
	
	public static <T extends RealType<T>> Img<T> Extend(RandomAccessibleInterval<T> input, ImgFactory<T> outputFactory, int[] outputDim, final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory, T type)
	{
		final ExtendImage<T> extend = new ExtendImage<T>(input, outputFactory, outputDim, outOfBoundsFactory,type);
		
		extend.process();
		
		return extend.output;
	}
	
	public static <T extends RealType<T>> Img<T> Extend(RandomAccessibleInterval<T> input, ImgFactory<T> outputFactory, long[] outputDim, final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory, T type)
	{
		
		final ExtendImage<T> extend = new ExtendImage<T>(input, outputFactory, outputDim, outOfBoundsFactory,type);
		
		extend.process();
		
		return extend.output;
	}
	
	public ExtendImage(RandomAccessibleInterval<T> input, ImgFactory<T> outputFactory, long[] outputDim, final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory, T type)
	{
		int[] outputDimInt=new int[input.numDimensions()];
		
		for (int d=0;d<input.numDimensions();d++)
		{
			outputDimInt[d]=(int)outputDim[d];
		}
		
		this.input=input;
		this.outputFactory=outputFactory;
		this.outputDim = outputDimInt;
		this.outOfBoundsFactory=outOfBoundsFactory;
		this.type=type;
	}
	
	public ExtendImage(RandomAccessibleInterval<T> input, ImgFactory<T> outputFactory, int[] outputDim, final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory, T type)
	{
		this.input=input;
		this.outputFactory=outputFactory;
		this.outputDim = outputDim;
		this.outOfBoundsFactory=outOfBoundsFactory;
		this.type=type;
	}
	
	public boolean process()
	{
		boolean result=true;
	
		int[] inputDim = new int[input.numDimensions()];
		int[] border = new int[input.numDimensions()];
		
		long[] start = new long[input.numDimensions()];
		long[] size = new long[input.numDimensions()];
		
		for (int i=0;i<input.numDimensions();i++)
		{
			inputDim[i]=(int)input.dimension(i);
			border[i]=(outputDim[i]-inputDim[i])/2;
			
			start[i]= - border[i];
			size[i]=outputDim[i];
		}
		
		final RandomAccessible< T > temp = Views.extend( input, outOfBoundsFactory );
		
		final RandomAccessibleInterval<T> extendedInput = Views.offsetInterval(temp, start, size);
		
		final IterableInterval<T> iterableInput = Views.iterable(extendedInput);

		output=outputFactory.create(outputDim, type);
		
		final Cursor<T> cursorIn = iterableInput.localizingCursor();
		final Cursor<T> cursorOut = output.cursor();
		
		while (cursorOut.hasNext())
		{
			cursorIn.fwd();
			cursorOut.fwd();
			
			
			cursorOut.get().set(cursorIn.get());
		}
		
		return result;
	}
}
