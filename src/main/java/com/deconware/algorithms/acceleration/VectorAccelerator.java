package com.deconware.algorithms.acceleration;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import com.deconware.algorithms.StaticFunctions;

public class VectorAccelerator <T extends RealType<T>> implements Accelerator<T>
{
	Img<T> xkm1_previous=null;
	Img<T> yk_prediction=null;
	Img<T> hk_vector=null;
	
	Img<T> gk;
	Img<T> gkm1;
	
	double accelerationFactor=0.0f;
	
	@Override
	public Img<T> Accelerate(Img<T> yk_iterated)
	{
		// use the iterated prediction and the previous value of the predcition
		// to calculate the acceleration factor
		if (yk_prediction!=null)
		{
			//StaticFunctions.showStats(yk_iterated);
			//StaticFunctions.showStats(yk_prediction);
			
			accelerationFactor=computeAccelerationFactor(yk_iterated, yk_prediction);
			
			System.out.println(accelerationFactor);
			
			
			if ( (accelerationFactor<0) )
			{
				xkm1_previous=null;
				gkm1=null;
				accelerationFactor=0.0;
			}
			
			if ( (accelerationFactor>1.0f))
			{
				accelerationFactor=1.0f;
			}
		}
		
		// current estimate for x is yk_iterated
		Img<T> xk_estimate=yk_iterated;
		
		// calculate the change vector between x and x previous
		if (xkm1_previous!=null)
		{
			hk_vector=StaticFunctions.Subtract(xk_estimate, xkm1_previous);
			
			// make the next prediction
			yk_prediction=AddAndScale(xk_estimate, hk_vector, (float)accelerationFactor);
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
	
	double computeAccelerationFactor(Img<T> yk_iterated, Img<T> yk)
	{
		gk=StaticFunctions.Subtract(yk_iterated, yk_prediction);
		
		if (gkm1!=null)
		{
			double numerator=DotProduct(gk, gkm1);
			double denominator=DotProduct(gkm1, gkm1);
			
			gkm1=gk.copy();
			
			return numerator/denominator;
			
		}
		
		gkm1=gk.copy();
		
		return 0.0;
		
		
	}
	
	/*
	 * multiply inputOutput by input and place the result in input
	 */
	public double DotProduct(final Img<T> image1, final Img<T> image2)
	{
		final Cursor<T> cursorImage1 = image1.cursor();
		final Cursor<T> cursorImage2 = image2.cursor();
		
		double dotProduct=0.0d;
		
		while (cursorImage1.hasNext())
		{
			cursorImage1.fwd();
			cursorImage2.fwd();
			
			float val1=cursorImage1.get().getRealFloat();
			float val2=cursorImage2.get().getRealFloat();
			
			dotProduct+=val1*val2;
		}
			
		return dotProduct;
	}
	
	public Img<T> AddAndScale(final Img<T> img1, final Img<T> img2, final float a)
	{
		Img<T> out = img1.factory().create(img1, img1.firstElement());
		
		final Cursor<T> cursor1 = img1.cursor();
		final Cursor<T> cursor2 = img2.cursor();
		final Cursor<T> cursorOut = out.cursor();
		
		while (cursor1.hasNext())
		{
			cursor1.fwd();
			cursor2.fwd();
			cursorOut.fwd();
			
			float val1=cursor1.get().getRealFloat();
			float val2=cursor2.get().getRealFloat();
			
			float val3=Math.max(val1+a*val2, 0.0001f);
			
			cursorOut.get().setReal(val3);
		}
		
		return out;
	}
}
