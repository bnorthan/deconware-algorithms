package com.deconware.algorithms.roi;

import java.util.Arrays;

import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;

import net.imglib2.type.numeric.RealType;

import net.imglib2.RandomAccessibleInterval;

/**
 * OrderStatistics provides the framework to create Order Statistic operations.  It operates
 * by cursing over the input {@link Image}, and collecting a sorted list of the pixels "covered" by
 * a {@link StructuringElement}.  This list is made available to children classes, which are
 * responsible for setting the pixel value at the current position in the output Image.
 * 
 * @param <T> The input- and output-{@link Image} type.
 * @author Larry Lindsey
 */
public abstract class OrderStatistics<T extends RealType<T>> extends ROIAlgorithm<T, T> {
	//Member variables
	
	private final double[] statArray;
	private double currentVal;
	
	public long statsTime;
	
	//Member functions
	
	public OrderStatistics(final Img<T> imageIn, long[][] path) 
	{
		super(imageIn.factory(), 
				imageIn.firstElement().createVariable(),
				imageIn,
		        new StructuringElementCursor<T>(
		                imageIn, path));
		statsTime = 0;
		statArray = new double[path.length];
    }
	
	public OrderStatistics(final ImgFactory<T> factory, final T type, final RandomAccessibleInterval<T> imageIn, long[][] path) 
	{
		super(factory, 
				type,//imageIn.firstElement().createVariable(),
				imageIn,
		        new StructuringElementCursor<T>(
		                imageIn, path));
		statsTime = 0;
		statArray = new double[path.length];
    }
	
	public void collectStats(final StructuringElementCursor <T> cursor)
    {
        int i = 0;
        
        while(cursor.hasNext())
        {
            cursor.fwd();
            statArray[i++] = cursor.get().getRealDouble();
        }

        Arrays.sort(statArray);
    }
	
	protected double[] getArray()
	{
	    return statArray;
	}
	
	protected double getCurrentVal()
	{
		return currentVal;
	}
			
	@Override
	protected boolean patchOperation(
            final StructuringElementCursor<T> cursor,
            final T inputType,
            final T outputType) 
	{
	    long p = System.currentTimeMillis();
	    
	    currentVal=inputType.getRealDouble();
	    
		collectStats(cursor);
		statsTime += System.currentTimeMillis() - p;
		statsOp(outputType);
		
		return true;
	}
		
	/**
	 * Perform the order statistic operation, then set the value of the given type.
	 * @param outputType the type whose value is to be set.  Belongs to the output Image.
	 */
	protected abstract void statsOp(final T outputType);
	
}

