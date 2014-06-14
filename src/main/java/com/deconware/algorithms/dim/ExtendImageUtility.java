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
	
	int[] axisIndices;
	int[] extension;
	long[] oldDimensions;
	long[] newDimensions;
	
	long[] offset;
	
	BoundaryType boundaryType;
	FFTTarget fftTarget;
	
	RandomAccessibleInterval<T> interval;
	
	OutOfBoundsFactory<T,RandomAccessibleInterval<T>> outOfBoundsFactory;
	
	public ExtendImageUtility(int[] axisIndices, int[] extension, RandomAccessibleInterval<T> interval, BoundaryType boundaryType, FFTTarget fftTarget)
	{
		this.axisIndices=axisIndices;
		this.extension=extension;
		this.interval=interval;
		
		this.boundaryType=boundaryType;
		this.fftTarget=fftTarget;
		
		oldDimensions=new long[interval.numDimensions()];
		newDimensions=new long[interval.numDimensions()];
		offset=new long[interval.numDimensions()];
		
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
					offset[d]=-extension[a];
				}
			}
			
			// if this dimension is not extended the new dimension is the same as the old
			if (extendAxis==false)
			{
				newDimensions[d]=interval.dimension(d);
				offset[d]=0;
			}
		}
		
		// if the image should be extended further to the nearest FFT size that is optimized for speed
		if (fftTarget==FFTTarget.MINES_SPEED)
		{
			System.out.println("fft speed");
						
			newDimensions=SimpleFFTFactory.GetPaddedInputSizeLong(newDimensions, fftTarget);
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

