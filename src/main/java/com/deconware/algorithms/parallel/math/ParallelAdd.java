package com.deconware.algorithms.parallel.math;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import com.deconware.algorithms.parallel.IterationChunk;
import com.deconware.algorithms.parallel.IterationChunker;

public class ParallelAdd <T extends RealType<T>> implements IterationChunk<T>
{
	public static<T extends RealType<T>> void Add(final IterableInterval<T> interval1, final IterableInterval<T> interval2, final IterableInterval<T> output) 
	{
		ParallelAdd<T> add=new ParallelAdd<T>(interval1, interval2, output);
		
		IterationChunker<T> chunker=new IterationChunker<T>(interval1.size(), add);
		
		chunker.run();
	}
	
	IterableInterval<T> interval1;
	IterableInterval<T> interval2;
	IterableInterval<T> output;
	
	public ParallelAdd(IterableInterval<T>  interval1, IterableInterval<T> interval2, IterableInterval<T> output) 
	{
		this.interval1= interval1;
		this.interval2=interval2;
		this.output=output;
	}
	
	public void execute(long startIndex, int stepsize, long numSteps)
	{
		final Cursor<T> interval1Cursor =  interval1.cursor();
		final Cursor<T> interval2Cursor=interval2.cursor();
		final Cursor<T> outputCursor=output.cursor();
		
		interval1Cursor.jumpFwd( startIndex );
		interval2Cursor.jumpFwd( startIndex );
		outputCursor.jumpFwd( startIndex );
		
		// do as many pixels as wanted by this thread
		for ( long j = 0; j < numSteps; ++j )
		{
			interval1Cursor.fwd();
			interval2Cursor.fwd();
			outputCursor.fwd();
			
			// TODO: it seems the templated version is slower.  Test and confirm.
			outputCursor.get().set(interval1Cursor.get());
			outputCursor.get().add(interval2Cursor.get());
				
		}
	}

}

