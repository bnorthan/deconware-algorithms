package com.deconware.algorithms.parallel.math;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import com.deconware.algorithms.parallel.ReductionChunk;
import com.deconware.algorithms.parallel.ReductionChunker;

public class ParallelSum<T extends RealType<T>, S extends RealType<S>> implements ReductionChunk<T, S>
{
	public static<T extends RealType<T>, S extends RealType<S>> S RunParallelSum(IterableInterval<T> in, S outType)
	{
		ParallelSum<T, S> sum=new ParallelSum<T, S>(in);
		
		ReductionChunker<T, S> chunker=
				new ReductionChunker<T, S>(in.size(), sum, outType);
		
		return chunker.run();
	}
	
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

