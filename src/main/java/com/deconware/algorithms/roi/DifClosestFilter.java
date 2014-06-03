package com.deconware.algorithms.roi;

import java.util.Arrays;

import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;

/**
 * Dif Closest Filter / morphological operation.
 * 
 * @param <T> {@link Image} type.
 * @author Brian Northan (following median filter example by Larry Lindsey)
 */
public class DifClosestFilter<T extends RealType<T>> extends OrderStatistics<T> {

    public DifClosestFilter(final Img<T> imageIn,
            long[] size) {
        this(imageIn, StructuringElementCursor.sizeToPath(size));       
    }
    
    public DifClosestFilter(final Img<T> imageIn,
            long[][] path)
    {
        super(imageIn, path);
        setName(imageIn + " Median Filter");
    }
    
	@Override
	protected void statsOp(final T outputType) 
	{		
	    double[] array = super.getArray();
	    double currentVal = super.getCurrentVal();
	    
	    int n = array.length;
	    
	    double[] closestArray=new double[n];
	    
	    int i=0;
	    
	//    System.out.println("");
		
	    for (double d:array)
	    {
	    	closestArray[i]=java.lang.Math.abs(array[i]-currentVal);
	    	
	    	i++;    	
	    }
		
	    Arrays.sort(closestArray);
	 
	 //  	System.out.println(closestArray[0]+" "+closestArray[1]+" "+closestArray[2]);
		 
	    outputType.setReal((closestArray[1]+closestArray[2])/2);
	}
}

