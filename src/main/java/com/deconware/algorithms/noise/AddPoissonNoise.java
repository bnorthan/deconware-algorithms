/**
 * Based on code by Ignazio Gallo.  Code has been modified for imglib2. 
 * Original copyright notice is below...
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * This plugin adds Poisson distributed noise to each pixel of an image. This
 * plugin uses a simple way to generate random Poisson-distributed numbers given
 * by Knuth (http://en.wikipedia.org/wiki/Donald_Knuth).
 * <p>
 * Poisson noise or (particularly in electronics) as shot noise is a type of
 * electronic noise that occurs when the finite number of particles that carry
 * energy, such as electrons in an electronic circuit or photons in an optical
 * device, is small enough to give rise to detectable statistical fluctuations
 * in a measurement. It is important in electronics, telecommunications, and
 * fundamental physics.
 * <p>
 * Changes: <br>
 * 30/nov/2008<br>
 * - subtracted the mean value before adding the noise to the signal. This in
 * order to distribute the noise around the signal.<br>
 * - Added the MEAN_FACTOR constant to obtain a significant noise working with
 * float images and with a small Lambda (mean) value.
 * 
 * @author Ignazio Gallo(ignazio.gallo@gmail.com,
 *         http://www.dicom.uninsubria.it/~ignazio.gallo/)
 * @since 18/nov/2008
 * 
 * @version 1.1
 */

package com.deconware.algorithms.noise;

import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

import java.util.Vector;

import net.imglib2.algorithm.Algorithm;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;

import net.imglib2.IterableInterval;
import net.imglib2.Cursor;
import net.imglib2.img.Img;

import net.imglib2.multithreading.Chunk;
import net.imglib2.multithreading.SimpleMultiThreading;

import java.util.concurrent.atomic.AtomicInteger;


public class AddPoissonNoise<T extends RealType<T> > implements Algorithm, MultiThreaded, Benchmark
{
	int numThreads;
	long processingTime;

	String errorMessage;
	
	final IterableInterval< T > image;

	public AddPoissonNoise( final Img< T > img )
	{
		this( (IterableInterval<T>) img );
	}

	public AddPoissonNoise( final IterableInterval< T > interval )
	{
		setNumThreads();
		
		this.image = interval;
	}

	@Override
	public boolean process()
	{
		final long startTime = System.currentTimeMillis();

		final long imageSize = image.size();
		
		final AtomicInteger ai = new AtomicInteger(0);	
		
		final Thread[] threads = SimpleMultiThreading.newThreads(getNumThreads());
     
		final Vector<Chunk> threadChunks = SimpleMultiThreading.divideIntoChunks(imageSize, numThreads);
		
		for (int ithread =0; ithread<threads.length; ++ithread)
		{
			threads[ithread]= new Thread(new Runnable()
			{
			
					@Override
					public void run()
			{
				// Thread ID
				final int myNumber=ai.getAndIncrement();
				
				// get chunk of pixels
				final Chunk myChunk=threadChunks.get(myNumber);
				
				compute(myChunk.getStartPosition(), myChunk.getLoopSize());
				
			}
				
			});
		}
		
		SimpleMultiThreading.startAndJoin(threads);
		
		
		processingTime = System.currentTimeMillis() - startTime;
        
		return true;
	}
	
	protected void compute(final long startPos, final long loopSize)
	{
		final Cursor<T> cursor=image.cursor();
		
		cursor.jumpFwd(startPos);
		
		for (long j=0;j<loopSize;j++)
		{
			System.out.println(startPos);
			cursor.fwd();
			
			double val = cursor.get().getRealDouble();
			
			int noisy = poissonValue(val);
			
			cursor.get().setReal(noisy);
		}
	}

	public static int poissonValue(double lambda)
	{
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k=0;
		
		do
		{
			k++;
			p*=java.lang.Math.random();
		}while (p>L);
		
		return k-1;		
	}

	
	@Override
	public boolean checkInput()
	{
		return true;
	}
	
	@Override
	public long getProcessingTime() { return processingTime; }

	@Override
	public void setNumThreads() { this.numThreads = Runtime.getRuntime().availableProcessors(); }

	@Override
	public void setNumThreads( final int numThreads ) { this.numThreads = numThreads; }

	@Override
	public int getNumThreads() { return numThreads; }
	
	@Override
	public String getErrorMessage() { return errorMessage; }
}
