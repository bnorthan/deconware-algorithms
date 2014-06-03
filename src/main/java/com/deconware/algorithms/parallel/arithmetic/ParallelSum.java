package com.deconware.algorithms.parallel.arithmetic;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import com.deconware.algorithms.parallel.ReductionChunk;

public class ParallelSum<T extends RealType<T>, S extends RealType<S>> implements ReductionChunk<T, S>
{
	IterableInterval<T> input;
	
	public ParallelSum(IterableInterval<T> input)
	{
		this.input=input;
	}
	
	public void evaluate(long startIndex, long stepSize, long numSteps, final S out)
	{
		final Cursor<T> cursor = input.cursor();
		
		cursor.jumpFwd( startIndex );
		
		double sum=0.0f;
		out.setZero();
		
		// do as many pixels as wanted by this thread
		for ( long j = 0; j < numSteps; ++j )
		{
			cursor.fwd();
			sum+=(cursor.get().getRealDouble());
			
		}
		
		out.setReal(sum);
	}
	
	public void evaluate(S in, S out)
	{
		out.add(in);
	}
	
	public void initialize(S out)
	{
		out.setZero();
	}
}

