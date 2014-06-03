package com.deconware.algorithms.roi;

import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;

import net.imglib2.RandomAccessibleInterval;

/**
 * Median filter / morphological operation.
 * 
 * @param <T> {@link Image} type.
 * @author Larry Lindsey
 */
public class MedianFilter<T extends RealType<T>> extends OrderStatistics<T> {

    public MedianFilter(final ImgFactory factory, final T type, final RandomAccessibleInterval<T> imageIn,
            long[] size) {
        this(factory, type, imageIn, StructuringElementCursor.sizeToPath(size));       
    }
    
    public MedianFilter(final ImgFactory factory, final T type, final RandomAccessibleInterval<T> imageIn,
            long[][] path)
    {
        super(factory, type, imageIn, path);
        setName(imageIn + " Median Filter");
    }
    
	@Override
	protected void statsOp(final T outputType) 
	{		
	    int n = super.getArray().length;		
		outputType.setReal(super.getArray()[n / 2]);
	}
}
