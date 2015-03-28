package com.deconware.algorithms.roi;

import net.imglib2.Cursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.ByteAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import  net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Fraction;

/**
 * TODO: Get rid of this or rework/retest!  It was written 2 years ago and is out of date
 * with other projects. 
 *
 */
public class StructuringElement extends ArrayImg<ByteType,ByteArray> {
	
	private final long[] offset;
	private String name;
	
	static private final int sizeOf(final long[] dim) {
		long a = dim[0];
		for (int i=1; i<dim.length; i++) a *= dim[i];
		return (int) a;
	}
	
	public StructuringElement(final long[] dimensions, final String name)
	{
		super(new ByteArray(sizeOf(dimensions)), dimensions,new Fraction());
		this.name = name;
		offset = new long[dimensions.length];
		
		for (int i = 0; i < dimensions.length; ++i)
		{
			offset[i] = dimensions[i] / 2;
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public long[] getOffset()
	{
		return offset;
	}
	
	public static StructuringElement createBall(final int nd, final double radius)
	{
		StructuringElement strel;
		Cursor<ByteType> cursor;
		final long[] dims = new long[nd];
		final long[] pos = new long[nd];
		double dist;
		
		for (int i = 0; i < dims.length; ++i)
		{
			dims[i] = (int)(radius * 2 + 1);
		}
		strel = new StructuringElement(dims, "Ball Structure " + nd + "D, " + radius);
		
		cursor = strel.cursor();
		
		while (cursor.hasNext())
		{
			dist = 0;
			cursor.fwd();
			cursor.localize(pos);
			for (int i = 0; i < dims.length; ++i)
			{
				dist += Math.pow(pos[i] - strel.offset[i], 2);
			}
			dist = Math.sqrt(dist);
			
			if (dist <= radius)
			{
				cursor.get().setOne();
			}
			else
			{
				cursor.get().setZero();
			}
		}
		
		return strel;
	}
	
	public static StructuringElement createCube(final int nd, final int length)
	{
		StructuringElement strel;
		Cursor<ByteType> cursor;
		final long[] dims = new long[nd];
		for (int i = 0; i < nd; ++i)
		{
			dims[i] = length;
		}
		
		strel = new StructuringElement(dims, "Cube Structure " + length);
		cursor = strel.cursor(); 
		
		while (cursor.hasNext())
		{
			cursor.fwd();
			cursor.get().setOne();
		}
		
		return strel;
	}
	
	public static StructuringElement createBar(int nd, int length, int lengthDim)
	{		
		if (lengthDim >= nd)
		{
			throw new RuntimeException("Invalid bar dimension " + lengthDim + ". Only have " + nd +
					" dimensions.");
		}
		final long[] dims = new long[nd];
		Cursor<ByteType> cursor;
		StructuringElement strel;
		
		for (int i = 0; i < nd; ++i)
		{
			if (i == lengthDim)
			{
				dims[i] = length;
			}
			else
			{
				dims[i] = 1;
			}
		}
		
		strel = new StructuringElement(dims, "Bar " + lengthDim + " of " + nd + ", " + length);
		cursor = strel.cursor();
		
		while(cursor.hasNext())
		{
			cursor.fwd();
			cursor.get().setOne();
		}
		
		return strel;	
	}

}


