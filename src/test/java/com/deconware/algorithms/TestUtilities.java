package com.deconware.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.imglib2.img.Img;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.Cursor;

import com.deconware.algorithms.StaticFunctions ;
import com.deconware.algorithms.parallel.ReductionChunker;
import com.deconware.algorithms.parallel.IterationChunker;
import com.deconware.algorithms.parallel.math.ParallelDivide;
import com.deconware.algorithms.parallel.math.ParallelSum;
import com.deconware.algorithms.parallel.math.ParallelSubtract;

public class TestUtilities 
{
	
	public static<T extends RealType<T>> void AreIntervalsTheSameFloat(IterableInterval<T> interval1, IterableInterval<T> interval2, float eps)
	{
		Cursor<T> c1=interval1.cursor();
		Cursor<T> c2=interval2.cursor();
		
		// confirm the static version and the threaded version produce the same answer
		while (c1.hasNext())
		{
			c1.fwd();
			c2.fwd();
		
			assertEquals(c1.get().getRealFloat(), c2.get().getRealFloat(), eps);
		}
		
	}
}
