package com.deconware.algorithms.parallel;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import net.imglib2.multithreading.Chunk;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.numeric.RealType;

public class ReductionChunker<T extends RealType<T>, S extends RealType<S>> 
{
	long size;
	ReductionChunk<T, S> chunk;
	S type;
	
	public ReductionChunker(long size, ReductionChunk<T, S> chunk, S type)
	{
		this.size=size;
		this.chunk=chunk;
		this.type=type; 
	}
	
	public S run()
	{
		final AtomicInteger ai = new AtomicInteger( 0 );
		int numThreads=Runtime.getRuntime().availableProcessors();
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads);
		
		final Vector< Chunk > threadChunks = SimpleMultiThreading.divideIntoChunks( size, numThreads );
		final Vector< S > values = new Vector< S >();
		
		for ( int ithread = 0; ithread < threads.length; ++ithread )
		{
			S value=type.createVariable();
			chunk.initialize(value);
			
			values.add( type.createVariable() ); 
			
			threads[ ithread ] = new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					// Thread ID
					final int myNumber = ai.getAndIncrement();

					// get chunk of pixels to process
					final Chunk myChunk = threadChunks.get( myNumber );

					// compute 
					chunk.evaluate(myChunk.getStartPosition(), 1, myChunk.getLoopSize(), values.get( myNumber ) );

				}
			} );
		}
		
		SimpleMultiThreading.startAndJoin( threads );
		
		S value=type.createVariable();
		chunk.initialize(value);
	
		// final 
		for ( int i = 0; i < threads.length; ++i )
		{
			chunk.evaluate(values.get(i), value);
		}
		
		return value;
	}

}
