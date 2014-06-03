package com.deconware.algorithms.parallel.arithmetic;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import com.deconware.algorithms.parallel.IterationChunk;
import com.deconware.algorithms.parallel.IterationChunker;

public class ParallelDivide <T extends RealType<T>> implements IterationChunk<T>
{
	/**
	 * Creates the chunker and runs a parallel division
	 * 
	 * @param denominatorOutput
	 * @param numerator
	 */
	public static<T extends RealType<T>> void InPlaceDivide(final Img<T> denominatorOutput, final RandomAccessibleInterval<T> numerator) 
	{
		IterableInterval<T> numeratorIterator=Views.iterable(numerator);
		
		ParallelDivide<T> divide=new ParallelDivide<T>(denominatorOutput, numeratorIterator, denominatorOutput);
		
		IterationChunker<T> chunker=new IterationChunker<T>(denominatorOutput.size(), divide);
		
		chunker.run();
	}
	
	public static<T extends RealType<T>> void Divide(final IterableInterval<T> denominator, final IterableInterval<T> numerator, final IterableInterval<T> output) 
	{
		ParallelDivide<T> divide=new ParallelDivide<T>(denominator, numerator, output);
		
		IterationChunker<T> chunker=new IterationChunker<T>(denominator.size(), divide);
		
		chunker.run();
	}
	
	IterableInterval<T> denominator;
	IterableInterval<T> numerator;
	IterableInterval<T> output;
	
	public ParallelDivide(IterableInterval<T>  denominator, IterableInterval<T> numerator, IterableInterval<T> output) 
	{
		this. denominator= denominator;
		this.numerator=numerator;
		this.output=output;
	}
	
	public void execute(long startIndex, int stepsize, long numSteps)
	{
		final Cursor<T> denominatorCursor =  denominator.cursor();
		final Cursor<T> numeratorCursor=numerator.cursor();
		final Cursor<T> outputCursor=output.cursor();
		
		denominatorCursor.jumpFwd( startIndex );
		numeratorCursor.jumpFwd( startIndex );
		outputCursor.jumpFwd( startIndex );
		
		// do as many pixels as wanted by this thread
		for ( long j = 0; j < numSteps; ++j )
		{
			denominatorCursor.fwd();
			numeratorCursor.fwd();
			outputCursor.fwd();
			
			// TODO: it seems the templated version is slower.  Test and confirm.
			
			/*T num_=numeratorCursor.get().copy();
			num_.div(denominatorCursor.get());
			outputCursor.get().set(num_);*/
					
			// TODO: division in floating point right now.  Add option for double precision?
			
			float num = numeratorCursor.get().getRealFloat();
			float div = denominatorCursor.get().getRealFloat();
			float res =0;
			
			if (div!=0)
			{
				res = num/div;
			}
			else
			{ 
				res = 0;
			}
			
	//		System.out.println(num+":"+div+":"+res);
				
			outputCursor.get().setReal(res);
		}
	}

}
