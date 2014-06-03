package com.deconware.algorithms.roi;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
/*
public class MedianAndClosestFilter<T extends RealType<T>> extends OrderAndClosest<T> 
{

    public MedianAndClosestFilter(final Img<T> imageIn,
	           long[] size) {
	       this(imageIn, StructuringElementCursor.sizeToPath(size));       
   }
	    
    public MedianAndClosestFilter(final Img<T> imageIn,
	         long[][] path)
	{
	    super(imageIn, path);
	    setName(imageIn + " Median Filter");
	}
	    
    @Override
	protected void statsOp(final T outputType) 
	{		
		int n = super.getArray().length;		
		outputType.setReal(super.getArray()[n / 2]);
	}
}*/
