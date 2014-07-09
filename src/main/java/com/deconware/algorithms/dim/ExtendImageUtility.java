package com.deconware.algorithms.dim;

import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;

import com.deconware.algorithms.fft.SimpleFFTFactory;

import com.deconware.algorithms.fft.SimpleFFTFactory.FFTTarget;

public class ExtendImageUtility <T extends RealType<T>>
{
	static public enum BoundaryType
	{
		ZERO, CONSTANT, REFLECTION, MIRROR, MIRROR_EXP, NEUMANN
	}
	
	int[] extension;
	long[] oldDimensions;
	long[] newDimensions;
	
	long[] offset;
	
	BoundaryType boundaryType;
	FFTTarget fftTarget;
	
	RandomAccessibleInterval<T> interval;
	
	OutOfBoundsFactory<T,RandomAccessibleInterval<T>> outOfBoundsFactory;
	
	public ExtendImageUtility(int[] extension, RandomAccessibleInterval<T> interval, BoundaryType boundaryType, FFTTarget fftTarget)
	{
		this(null, extension, interval, boundaryType, fftTarget);
	}
	
	public ExtendImageUtility(int[] axisIndices, int[] extension, RandomAccessibleInterval<T> interval, BoundaryType boundaryType, FFTTarget fftTarget)
	{
		this.extension=extension;
		this.interval=interval;
		
		oldDimensions=new long[interval.numDimensions()];
		newDimensions=new long[interval.numDimensions()];
		
		if (axisIndices!=null)
		{
			for (int d=0;d<interval.numDimensions();d++)
			{
				oldDimensions[d]=interval.dimension(d);
				
				boolean extendAxis=false;
				for (int a=0;a<axisIndices.length;a++)
				{
					if (axisIndices[a]==d)
					{
						extendAxis=true;
						newDimensions[d]=oldDimensions[d]+2*extension[a];
					}
				}
				
				// if this dimension is not extended the new dimension is the same as the old
				if (extendAxis==false)
				{
					newDimensions[d]=interval.dimension(d);
				}
			}
		}
		else
		{
			for (int d=0;d<interval.numDimensions();d++)
			{
				oldDimensions[d]=interval.dimension(d);
				
				if (d<extension.length)
				{
					newDimensions[d]=oldDimensions[d]+2*extension[d];
				}
				else
				{
					newDimensions[d]=oldDimensions[d];
				}
			}
		}
	
		offset=new long[interval.numDimensions()];
		this.boundaryType=boundaryType;
		this.fftTarget=fftTarget;
	
		// if the image should be extended further to the nearest FFT size that is optimized for speed
		if (fftTarget!=null)
		{
			if (fftTarget!=FFTTarget.NONE)
			{
				System.out.println("fft speed");
						
				newDimensions=SimpleFFTFactory.GetPaddedInputSizeLong(newDimensions, fftTarget);
			}
		}
		
		for (int d=0;d<interval.numDimensions();d++)
		{
			offset[d]=-(newDimensions[d]-oldDimensions[d])/2;
		}
		
		// create the OutOfBoundsFactory according to the boundary type
		if (boundaryType==BoundaryType.MIRROR)
		{
			outOfBoundsFactory 
			= new OutOfBoundsMirrorFactory< T, RandomAccessibleInterval<T> >( Boundary.SINGLE );
		}
		// default to zero extension TODO support more extension types
		else
		{
			T type=Util.getTypeFromInterval(interval);
			type.setReal(0.0);
			
			outOfBoundsFactory 
				= new OutOfBoundsConstantValueFactory<T, RandomAccessibleInterval<T>>(type);
		}
	}
	
	public long[] getNewDimensions()
	{
		return newDimensions;
	}
	
	public long[] getOffset()
	{
		return offset;
	}
	
	public OutOfBoundsFactory<T,RandomAccessibleInterval<T>> getOutOfBoundsFactory()
	{
		return this.outOfBoundsFactory;
	}
}

