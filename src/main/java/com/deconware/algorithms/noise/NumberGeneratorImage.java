package com.deconware.algorithms.noise;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

//import org.uncommons.maths.number.NumberGenerator;

// TODO: This code was cut and pasted from https://github.com/StephanPreibisch/multiview-simulation/tree/master/src/main/java/simulation
// doesn't look like it is in a maven repository yet but need to link through maven when that option is available. 


/**
* Helper class for the poisson process
*
* @author Stephan Preibisch (stephan.preibisch@gmx.de)
*
* @param <T>
*/

/*
public class NumberGeneratorImage< T extends RealType< T > > implements NumberGenerator< Double >
{
	final Cursor< T > cursor;
	final double multiplicativeFactor;

	public NumberGeneratorImage( final RandomAccessibleInterval<T> image, final double multiplicativeFactor )
	{
		this.cursor = Views.iterable( image ).cursor();
		this.multiplicativeFactor = multiplicativeFactor;
	}

	
	public void fwd()
	{
		cursor.fwd();
	}

	public void reset()
	{
		cursor.reset();
	}

	@Override
	public Double nextValue()
	{
		return cursor.get().getRealDouble() * multiplicativeFactor;
	}
}*/


