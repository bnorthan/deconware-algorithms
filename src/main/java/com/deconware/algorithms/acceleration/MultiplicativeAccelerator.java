package com.deconware.algorithms.acceleration;

import com.deconware.algorithms.StaticFunctions;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/*import net.imagej.ops.OpService;
import net.imagej.ops.chunker.Chunk;
import net.imagej.ops.chunker.Chunker;*/

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.multithreading.Chunk;

/**
 * Implemenation of Multiplicative Vector Acceleration
 * 
 * Biggs, Andrews "Acceleration of Iterative image restoration algorithms"
 * 
 * https://researchspace.auckland.ac.nz/handle/2292/1760
 * 
 * @author bnorthan
 *
 * @param <T>
 */
public class MultiplicativeAccelerator <T extends RealType<T>> implements Accelerator<T>
{
	Img<T> xkm1_previous=null;
	Img<T> yk_prediction=null;
	Img<T> hk_vector=null;
	
	Img<T> gk;
	Img<T> gkm1;
	
	double accelerationFactor=0.0f;
	
	public Img<T> Accelerate(Img<T> yk_iterated)
	{
		// use the iterated prediction and the previous value of the prediction
		// to calculate the acceleration factor
		if (yk_prediction!=null)
		{
			long startTime=System.currentTimeMillis();
			
			accelerationFactor=computeAccelerationFactorMultiplicative(yk_iterated, yk_prediction, yk_iterated, xkm1_previous);
			
			long endTime=System.currentTimeMillis()-startTime;
			
			System.out.println("acceleration factor is: "+accelerationFactor);
			System.out.println("acceleration factor time is: "+endTime);
			
			// if acceleration factor is negative restart the acceleration process
			if ( (accelerationFactor<0) )
			{
				xkm1_previous=null;
				gkm1=null;
				accelerationFactor=0.0;
			}
			
			if (accelerationFactor>1.0f)
			{
				accelerationFactor=1.0f;
			}
			
		}
		
		// current estimate for x is yk_iterated
		Img<T> xk_estimate=yk_iterated;
		
		// calculate the change vector between x and x previous
		if (xkm1_previous!=null)
		{
			// --------------------divide
			hk_vector=StaticFunctions.Divide(xk_estimate, xkm1_previous);
			
			long startTime=System.currentTimeMillis();
			
			yk_prediction=StaticFunctions.MulAndExponent(xk_estimate, hk_vector, (float)accelerationFactor);
				
			long endTime=System.currentTimeMillis()-startTime;
			
			System.out.println("prediction time: "+endTime);
		}
		else
		{
			// can't make a prediction yet
			yk_prediction=xk_estimate.copy();
		}
		
		// make a copy of the estimate to use as previous next time
		xkm1_previous=xk_estimate.copy();
		
		// return the prediction
		return yk_prediction.copy();
				
	}
	
	double computeAccelerationFactorMultiplicative(Img<T> yk_iterated, Img<T> yk, Img<T> xk, Img<T> xkm1)
	{
		// divide
		gk=StaticFunctions.Divide(yk_iterated, yk_prediction);
		
		if (gkm1!=null)
		{
			// log dot
			double numerator=DotProductLogThreaded(xk, gk, xkm1, gkm1);
			double denominator=DotProductLogThreaded(xkm1, gkm1, xkm1, gkm1);
			
			System.out.println("num: "+numerator+" denom "+denominator);			
			gkm1=gk.copy();
			
			//double accelerationFactor = Math.max(numerator/denominator, 0.0);
			double accelerationFactor=numerator/denominator;
			
			return accelerationFactor;
		}
		
		gkm1=gk.copy();
		
		return 0.0f;
	}
	
	/*
	 * 
	 */
	public double DotProductLog(final Img<T> x1, final Img<T> u1, final Img<T> x2, final Img<T> u2)
	{
		final Cursor<T> cursorU1 = u1.cursor();
		final Cursor<T> cursorU2 = u2.cursor();
		
		final Cursor<T> cursorX1 = x1.cursor();
		final Cursor<T> cursorX2 = x2.cursor();
		
		double dotProduct=0.0d;
		
		while (cursorU1.hasNext())
		{
			cursorU1.fwd();
			cursorU2.fwd();
			cursorX1.fwd();
			cursorX2.fwd();
			
			double u1val=cursorU1.get().getRealDouble();
			double u2val=cursorU2.get().getRealDouble();
			
			double thresh=0.00001;
			
			if ( (u1val>thresh) && (u2val>thresh) )
			{
				//double val1=cursorX1.get().getRealDouble()*Math.log(u1val);
				//double val2=cursorX2.get().getRealDouble()*Math.log(u2val);
				
				double val1=cursorX1.get().getRealDouble()*net.jafama.FastMath.logQuick(u1val);
				double val2=cursorX2.get().getRealDouble()*net.jafama.FastMath.logQuick(u2val);
				
				dotProduct+=val1*val2;
			}			
		}
		
		return dotProduct;
	}
	
	/*
	 * 
	 */
	public double DotProductLogThreaded(final Img<T> x1, final Img<T> u1, final Img<T> x2, final Img<T> u2)
	{
		double dotProduct=0.0d;
		
		final AtomicInteger ai = new AtomicInteger( 0 );
		int numThreads=Runtime.getRuntime().availableProcessors();
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads);
		
		long imageSize=1;
		for (int i=0;i<x1.numDimensions();i++)
		{
			imageSize*=x1.dimension(i);
		}

		final Vector< Chunk > threadChunks = SimpleMultiThreading.divideIntoChunks( imageSize, numThreads );
		final Vector< FloatType > dotValues = new Vector< FloatType >();
		
		for ( int ithread = 0; ithread < threads.length; ++ithread )
		{
			dotValues.add( new FloatType() );
			
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
					computeDotProductLog( x1, u1, x2, u2, myChunk.getStartPosition(), myChunk.getLoopSize(), dotValues.get( myNumber ) );

				}
			} );
		}
		
		SimpleMultiThreading.startAndJoin( threads );
	
		// final dot product is some of the result from each chunk
		for ( int i = 0; i < threads.length; ++i )
		{
			dotProduct+=dotValues.get(i).getRealDouble();
		}
		
		return dotProduct;
	}
	
	protected void computeDotProductLog( final Img<T> x1, final Img<T> u1, final Img<T> x2, final Img<T> u2, 
			final long startPos, final long loopSize, final FloatType dot )
	{
		final Cursor<T> cursorU1 = u1.cursor();
		final Cursor<T> cursorU2 = u2.cursor();
	
		final Cursor<T> cursorX1 = x1.cursor();
		final Cursor<T> cursorX2 = x2.cursor();
	
		cursorU1.jumpFwd( startPos );
		cursorU2.jumpFwd( startPos );
		cursorX1.jumpFwd( startPos );
		cursorX2.jumpFwd( startPos );

		float dotProduct=0.0f;
		
		// do as many pixels as wanted by this thread
		for ( long j = 0; j < loopSize; ++j )
		{
			cursorU1.fwd();
			cursorU2.fwd();
			cursorX1.fwd();
			cursorX2.fwd();

			double u1val=cursorU1.get().getRealDouble();
			double u2val=cursorU2.get().getRealDouble();

			double thresh=0.00001;
			
			if ( (u1val>thresh) && (u2val>thresh) )
			{
				double val1=cursorX1.get().getRealDouble()*net.jafama.FastMath.logQuick(u1val);
				double val2=cursorX2.get().getRealDouble()*net.jafama.FastMath.logQuick(u2val);
	
				dotProduct+=val1*val2;
			}
		}
		
		dot.setReal(dotProduct);
	}

}
