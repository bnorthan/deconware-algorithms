package com.deconware.algorithms.psf;

import com.deconware.algorithms.InputOutputAlgorithm;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.IterableInterval;
import net.imglib2.iterator.LocalizingZeroMinIntervalIterator;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;   

/**
 * 
 * Class used to for rearanging quadrants of a centered psf
 * 
 * @author bnorthan based on code written by Stephan Priebisch
 *
 * @param <T>
 */
public class FlipPsfQuadrants<T extends RealType<T>> extends InputOutputAlgorithm<T, RandomAccessibleInterval<T>>
{
	final int outputDim[];
	//final ImgFactory<T> imageFactory;
	
	public static <T extends RealType<T>> Img<T> flip(RandomAccessibleInterval<T> input, ImgFactory<T> outputFactory, int outputDim[])
	{
		T type = Util.getTypeFromInterval( input );
		Img<T> output=outputFactory.create(outputDim, type);
		
		final FlipPsfQuadrants<T> flipPsfQuadrants=new FlipPsfQuadrants<T>(input, output, outputDim);
		flipPsfQuadrants.process();
		
		return output;
	}
	
	public static <T extends RealType<T>> void flip(RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output, int outputDim[])
	{
		final FlipPsfQuadrants<T> flipPsfQuadrants=new FlipPsfQuadrants<T>(input, output, outputDim);
		flipPsfQuadrants.process();
	}
	
	public FlipPsfQuadrants(RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output, int outputDim[])
	{
		this.input=input;
		this.output=output;
		this.outputDim=outputDim;
	}
	
	public boolean process()
	{
		final int numDimensions = outputDim.length;
		
		int[] inputDim = new int[numDimensions];
		
		for ( int d = 0; d < numDimensions; ++d )
		{
			inputDim[d]=(int)input.dimension(d);
		}
		
		final long startTime = System.currentTimeMillis();
		
		final RandomAccess<T> inputCursor = input.randomAccess();
		final RandomAccess<T> outputCursor = output.randomAccess();
		
		final LocalizingZeroMinIntervalIterator cursorDim = new LocalizingZeroMinIntervalIterator( input );
		
		final int[] position = new int[ numDimensions ];
		final int[] position2 = new int[ numDimensions ];
		
		while ( cursorDim.hasNext() )
		{
			cursorDim.fwd();
			cursorDim.localize( position );
			
			for ( int d = 0; d < numDimensions; ++d )
			{
				// the kernel might not be zero-bounded
				position2[ d ] = position[ d ] + (int)input.min( d );
				
				position[ d ] = ( position[ d ] - inputDim[ d ]/2 + outputDim[ d ] ) % outputDim[ d ];
			}
			
			inputCursor.setPosition( position2 );				
			outputCursor.setPosition( position );
			outputCursor.get().set( inputCursor.get() );
		}
		
		return true;
	}
}
