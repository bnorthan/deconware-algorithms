package com.deconware.algorithms.psf;

import com.deconware.algorithms.InputOutputAlgorithm;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
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
public class FlipPsfQuadrants<T extends RealType<T>> extends InputOutputAlgorithm<T>
{
	final int outputDim[];
	final ImgFactory<T> imageFactory;
	
	public static <T extends RealType<T>> Img<T> flip(RandomAccessibleInterval<T> input, ImgFactory<T> outputFactory, int outputDim[])
	{
		final FlipPsfQuadrants<T> flipPsfQuadrants=new FlipPsfQuadrants<T>(input, outputFactory, outputDim);
		flipPsfQuadrants.process();
		return flipPsfQuadrants.output;
	}
	
	public FlipPsfQuadrants(RandomAccessibleInterval<T> input, ImgFactory<T> outputFactory, int outputDim[])
	{
		this.input=input;
		this.imageFactory=outputFactory;
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
		
		T kernelType = Util.getTypeFromInterval( input );
		output = imageFactory.create(outputDim, kernelType.createVariable());
		
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
