package com.deconware.algorithms.roi;

import java.util.Arrays;

import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;

import net.imglib2.Cursor;

/**
 * OrderStatistics provides the framework to create Order Statistic operations.  It operates
 * by cursing over the input {@link Image}, and collecting a sorted list of the pixels "covered" by
 * a {@link StructuringElement}.  This list is made available to children classes, which are
 * responsible for setting the pixel value at the current position in the output Image.
 * 
 * @param <T> The input- and output-{@link Image} type.
 * @author Larry Lindsey
 */
public abstract class OrderAndClosest<T extends RealType<T>> extends ROIAlgorithm<T, T> {
	//Member variables
	
	private final double[] statArray;
	private final double[] closeArray;
	
	public long statsTime;
	
	//Member functions
	
	public OrderAndClosest(final Img<T> imageIn, long[][] path) 
	{
		super(imageIn.factory(), 
				imageIn.firstElement().createVariable(),
				imageIn,
		        new StructuringElementCursor<T>(
		                imageIn, path));
		statsTime = 0;
		statArray = new double[path.length];
		closeArray = new double[path.length];
    }
	
	public void collectStats(final StructuringElementCursor <T> cursor)
    {
        int i = 0;
        
        
        while(cursor.hasNext())
        {
            cursor.fwd();
            statArray[i++] = cursor.get().getRealDouble();
            //System.out.println("Current is: "+cursor.get().getRealDouble());
    	    
        }

        Arrays.sort(statArray);
    }
	
	protected double[] getArray()
	{
	    return statArray;
	}
	
	protected double[] getClosestArray()
	{
		return closeArray;
	}
			
	@Override
	protected boolean patchOperation(
            final StructuringElementCursor<T> cursor,
            final T inputType,
            final T outputType) 
	{
	    long p = System.currentTimeMillis();
	    
	    double currentVal=inputType.getRealDouble();
	    
	   collectStats(cursor);
	
		int i=0;
		
		for (double d : statArray)
		{
			closeArray[i]=java.lang.Math.abs(statArray[i]-currentVal);
		}
		
		Arrays.sort(closeArray);
		
		statsTime += System.currentTimeMillis() - p;
		statsOp(outputType);
		
		return true;
	}
		
	/**
	 * Perform the order statistic operation, then set the value of the given type.
	 * @param outputType the type whose value is to be set.  Belongs to the output Image.
	 */
	protected abstract void statsOp(final T outputTypeMedian);
	
}


