package com.deconware.algorithms.parallel.math;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import com.deconware.algorithms.parallel.ReductionChunk;
import com.deconware.algorithms.parallel.ReductionChunker;

public class ParallelDot<T extends RealType<T>, S extends RealType<S>> implements ReductionChunk<T, S>
{
	public static<T extends RealType<T>, S extends RealType<S>> S RunParallelDot(IterableInterval<T> input1, IterableInterval<T> input2, S outType)
	{
		ParallelDot<T, S> dot=new ParallelDot<T, S>(input1, input2);
		
		ReductionChunker<T, S> chunker=
				new ReductionChunker<T, S>(input1.size(), dot, outType);
		
		return chunker.run();
	}
	
	IterableInterval<T> input1;
	IterableInterval<T> input2;
	
	public ParallelDot(IterableInterval<T> input1, IterableInterval<T> input2)
	{
		this.input1=input1;
		this.input2=input2;
	}
	
	public void evaluate(long startIndex, long stepSize, long numSteps, final S out)
	{
		final Cursor<T> cursor1 = input1.cursor();
		final Cursor<T> cursor2 = input2.cursor();
		
		cursor1.jumpFwd( startIndex );
		cursor2.jumpFwd( startIndex );
		
		// TODO: dot is calculated using double.  Make Generic??  Test speed.  
		
		double dot=0.0f;
		out.setZero();
		
		// do as many pixels as wanted by this thread
		for ( long j = 0; j < numSteps; ++j )
		{
			cursor1.fwd();
			cursor2.fwd();
			
			dot+=(cursor1.get().getRealDouble()*cursor2.get().getRealDouble());
		}
		
		out.setReal(dot);
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


