package com.deconware.algorithms.parallel;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import net.imglib2.multithreading.Chunk;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.numeric.RealType;

public class IterationChunker<T extends RealType<T>> 
{
	long size;
	IterationChunk<T> chunk;
	
	public IterationChunker(long size, IterationChunk<T> chunk)
	{
		this.size=size;
		this.chunk=chunk;
	}
	
	public void run()
	{
		final AtomicInteger ai = new AtomicInteger( 0 );
		int numThreads=Runtime.getRuntime().availableProcessors();
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads);
		
		final Vector< Chunk > threadChunks = SimpleMultiThreading.divideIntoChunks( size, numThreads );
		
		for ( int ithread = 0; ithread < threads.length; ++ithread )
		{
				
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
					chunk.execute(myChunk.getStartPosition(), 1, myChunk.getLoopSize() );

				}
			} );
		}
		
		SimpleMultiThreading.startAndJoin( threads );	
	}

}

