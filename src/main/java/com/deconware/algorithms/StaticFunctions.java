package com.deconware.algorithms;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.iterator.LocalizingZeroMinIntervalIterator;
import io.scif.SCIFIO;
import io.scif.img.ImgSaver;

import java.text.NumberFormat;
import java.io.*;  

/*
 * class to be used for quick implementation and prototyping of math and utility functions
 * 
 * @author bnorthan
 *
 */
public class StaticFunctions 
{
	      
	/*
	 * complex multiply two images
	 */
	public static<T extends ComplexType<T>> void InPlaceComplexMultiply(final Img<T> inputOutput, final Img<T> input)
	{
		final Cursor<T> cursorInputOutput = inputOutput.cursor();
		final Cursor<T> cursorInput = input.cursor();
		
		while (cursorInputOutput.hasNext())
		{
			cursorInputOutput.fwd();
			cursorInput.fwd();
			cursorInputOutput.get().mul(cursorInput.get());
		}
	}
	
	/*
	 * Complex multiply inputOutput by the conjugate of input and place the result in inputOutput
	 */
	public static void InPlaceComplexConjugateMultiply(final Img<ComplexFloatType> inputOutput, final Img<ComplexFloatType> input)
	{
		final Cursor<ComplexFloatType> cursorInputOutput = inputOutput.cursor();
		final Cursor<ComplexFloatType> cursorInput = input.cursor();
		
		while (cursorInputOutput.hasNext())
		{
			cursorInputOutput.fwd();
			cursorInput.fwd();
			
			ComplexFloatType temp = new ComplexFloatType();
			temp.set(cursorInput.get());
			temp.complexConjugate();
			
			cursorInputOutput.get().mul(temp);
		}
	}
	
	/*
	 * multiply inputOutput by input and place the result in input
	 */
	public static<T extends RealType<T>> void InPlaceMultiply(final Img<T> inputOutput, final Img<T> input)
	{
		final Cursor<T> cursorInputOutput = inputOutput.cursor();
		final Cursor<T> cursorInput = input.cursor();
		
		while (cursorInputOutput.hasNext())
		{
			cursorInputOutput.fwd();
			cursorInput.fwd();
			cursorInputOutput.get().mul(cursorInput.get());
		}
		
	}
	
	/*
	 * multiply inputOutput by input and place the result in input
	 */
	public static<T extends RealType<T>> double DotProduct(final Img<T> image1, final Img<T> image2)
	{
		final Cursor<T> cursorImage1 = image1.cursor();
		final Cursor<T> cursorImage2 = image2.cursor();
		
		double dotProduct=0.0d;
		
		while (cursorImage1.hasNext())
		{
			cursorImage1.fwd();
			cursorImage2.fwd();
			dotProduct+=cursorImage1.get().getRealDouble()*cursorImage2.get().getRealDouble();
		}
			
		return dotProduct;
	}
	
	/*
	 * multiply inputOutput by input and place the result in input
	 */
	public static<T extends RealType<T>> double DotProductLog(final Img<T> image1, final Img<T> image2)
	{
		final Cursor<T> cursorImage1 = image1.cursor();
		final Cursor<T> cursorImage2 = image2.cursor();
		
		double dotProduct=0.0d;
		
		while (cursorImage1.hasNext())
		{
			cursorImage1.fwd();
			cursorImage2.fwd();
			
			
			dotProduct+=Math.log(cursorImage1.get().getRealDouble())*Math.log(cursorImage2.get().getRealDouble());
		}
		
		
		return dotProduct;
	}
		
	/*
	 * divide numerator by denominatorOutput and place result in denominatorOutput
	 */
	public static<T extends RealType<T>> void InPlaceDivide(final Img<T> denominatorOutput, final RandomAccessible<T> numerator) 
	{
		final Cursor<T> cursorDenominatorOutput = denominatorOutput.cursor();
		
		RandomAccess< T > numeratorAccess = numerator.randomAccess();
		 
		while (cursorDenominatorOutput.hasNext())
		{
			cursorDenominatorOutput.fwd();
			numeratorAccess.setPosition(cursorDenominatorOutput);
			
			float num = numeratorAccess.get().getRealFloat();
			float div = cursorDenominatorOutput.get().getRealFloat();
			float res =0;
			
			if (div>0)
			{
				res = num/div;
			}
			else
			{ 
				res = 0;
			}
				
			cursorDenominatorOutput.get().setReal(res);
		}
	}
	
	public static<T extends RealType<T>> void InPlaceDivide3(final Img<T> denominatorOutput, final RandomAccessibleInterval<T> numerator) 
	{
		final Cursor<T> cursorDenominatorOutput = denominatorOutput.cursor();
				
		IterableInterval<T> numeratorIterator=Views.iterable(numerator);
		
		Cursor<T> numeratorCursor=numeratorIterator.cursor();
		 
		while (cursorDenominatorOutput.hasNext())
		{
			cursorDenominatorOutput.fwd();
			numeratorCursor.fwd();
			
			float num = numeratorCursor.get().getRealFloat();
			float div = cursorDenominatorOutput.get().getRealFloat();
			float res =0;
			
			if (div>0)
			{
				res = num/div;
			}
			else
			{ 
				res = 0;
			}
				
			cursorDenominatorOutput.get().setReal(res);
		}
	}
	
	/*
	 * divide numeratorOutput by denominator and place result in numeratorOutput
	 */
	public static<T extends RealType<T>> void InPlaceDivide2(final Img<T> denominator, final Img<T> numeratorOutput) 
	{
		final Cursor<T> cursorDenominator = denominator.cursor();
		Cursor< T > cursorNumeratorOutput = numeratorOutput.cursor();
		 
		while (cursorDenominator.hasNext())
		{
			cursorDenominator.fwd();
			cursorNumeratorOutput.fwd();
			
			float num = cursorNumeratorOutput.get().getRealFloat();
			float div = cursorDenominator.get().getRealFloat();
			float res =0;
			
			if (div>0e-7&&num>0e-7)
			{
				res = num/div;
				
				if (num>10)
				{
					int stop=5;
				}
			}
			else
			{ 
				res = 0;
			}
				
			cursorNumeratorOutput.get().setReal(res);
		}
	}
	
	public static <T extends RealType<T>> Img<T> crop(final Img<T> source, long[] start, long[] size)
	{
		return crop(source, source.factory(), source.firstElement(), start, size);
	}
	
	public static <T extends RealType<T>> Img<T> crop(final RandomAccessible<T> source, ImgFactory<T> outputFactory, T type, long[] start, long[] size)
	{
		Img<T> cropped = outputFactory.create(size, type);
		
		return crop(source, cropped, start, size);
	}
	
	public static <T extends RealType<T>> Img<T> crop(final RandomAccessible<T> source, Img<T> cropped, long[] start, long[] size)
	{
		final RandomAccess<T> random = source.randomAccess();
		
		final Cursor<T> cursor = cropped.cursor();
		
		final long[] tmpPosition = new long[start.length];
		
		while (cursor.hasNext())
		{
			cursor.next();
			
			cursor.localize(tmpPosition);
				
			for (int i=0;i<start.length;i++)
			{
				tmpPosition[i] += start[i];
			}
			
			random.setPosition(tmpPosition);
			
			double value = random.get().getRealDouble();
				
			cursor.get().setReal(value);
		}
		return cropped;
	}
	
	/*
	 * subtract img2 from img1 and return the result
	 */
	public static <T extends RealType<T>> Img<T> Subtract(final Img<T> img1, final Img<T> img2)
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
			
			cursorOut.get().set(cursor1.get());
			
			cursorOut.get().sub(cursor2.get());
		}
		
		return out;
	}
	
	/*
	 * divide img2 from img1 and return the result
	 */
	public static <T extends RealType<T>> Img<T> Divide(final Img<T> img1, final Img<T> img2)
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
			
			float num = cursor1.get().getRealFloat();
			float div = cursor2.get().getRealFloat();
			float res =0;
			
			if (div!=0)
			{
				res = num/div;
			}
			else
			{ 
				res = 0;
			}
			
			cursorOut.get().setReal(res);
			
		}
		
		return out;
	}
	
	/*
	 * add img1 plus a*img2 and return the result
	 */
	public static <T extends RealType<T>> Img<T> AddAndScale(final Img<T> img1, final Img<T> img2, final float a)
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
			
			cursorOut.get().set(cursor1.get());
			cursor2.get().mul(a);
			cursorOut.get().add(cursor2.get());
		}
		
		return out;
	}
	
	/*
	 * 
	 */
	public static <T extends RealType<T>> Img<T> MulAndExponent(final Img<T> img1, final Img<T> img2, final float a)
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
			
			double val1=cursor1.get().getRealDouble();
			double val2=cursor2.get().getRealDouble();
			
			
			
		//	val2=Math.pow(val2, a);
			val2=net.jafama.StrictFastMath.pow(val2, a);
		//	net.jafama.StrictFastMath.powQuick(val2,a);
		//	net.jafama.FastMath.pow(val2, a);
			
			cursorOut.get().setReal(val1*val2);
		}
		
		return out;
	}
		
	public static <T extends RealType<T>> Img<T> Mul(final Img<T> img1, final Img<T> img2)
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
			
			double val1=cursor1.get().getRealDouble();
			double val2=cursor2.get().getRealDouble();
			
			cursorOut.get().setReal(val1*val2);
		}
		
		return out;
	}

	
	/*
	 * add img1 and img2 and return the result
	 */
	public static <T extends RealType<T>> Img<T> Add(final Img<T> img1, final Img<T> img2)
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
			
			cursorOut.get().set(cursor1.get());
			
			cursorOut.get().add(cursor2.get());
		}
		
		return out;
	}
	
	/*
	 * take the absolute value of an image
	 */
	public static <T extends RealType<T>> void Abs(final Img<T> img)
	{
		final Cursor<T> cursor = img.cursor();
		
		while (cursor.hasNext())
		{
			cursor.fwd();
			
			double val = cursor.get().getRealDouble();
			cursor.get().setReal(java.lang.Math.abs(val));
		}
	}
	
	/*
	 * copy iterableInterval source into iterableInterval target
	 */
	public static <T extends RealType<T>> void copy(final IterableInterval<T> source, final IterableInterval<T> target)
	{
		// create a cursor that automatically localizes itself on every move
        Cursor< T > targetCursor = target.localizingCursor();
        Cursor< T > sourceCursor = source.localizingCursor();
 
        // iterate over the input cursor
        while ( targetCursor.hasNext())
        {
            // move input cursor forward
            targetCursor.fwd();
            sourceCursor.fwd();
           
            // set the value of this pixel of the output image, every Type supports T.set( T type )
   
            targetCursor.get().set((T)sourceCursor.get() );
        }
	}
	
	/*
	 * copy RandomAccessible source into IterableInterval target
	 */
	public static <T extends RealType<T>> void copy2(final RandomAccessible<T> source, final IterableInterval<T> target)
	{
		// create a cursor that automatically localizes itself on every move
        Cursor< T > targetCursor = target.localizingCursor();
        RandomAccess< T > sourceRandomAccess = source.randomAccess();
 
        // iterate over the input cursor
        while ( targetCursor.hasNext())
        {
            // move input cursor forward
            targetCursor.fwd();
            
            // set the output cursor to the yposition of the input cursor
            sourceRandomAccess.setPosition( targetCursor );
              
            // set the value of this pixel of the output image, every Type supports T.set( T type )
   
            targetCursor.get().set((T)sourceRandomAccess.get() );
        }
	}
	
	/*
	 * copy Img source into RandomAccessible target
	 */
	public static <T extends RealType<T>> void copy3(final Img<T> source, final RandomAccessible<T> target)
	{
		// create a cursor that automatically localizes itself on every move
        RandomAccess< T > targetRandomAccess = target.randomAccess();
        Cursor< T > sourceCursor = source.cursor();
 
        // iterate over the input cursor
        while ( sourceCursor.hasNext())
        {
            // move input cursor forward
            sourceCursor.fwd();
            
            // set the output cursor to the yposition of the input cursor
            targetRandomAccess.setPosition( sourceCursor );
              
            // set the value of this pixel of the output image, every Type supports T.set( T type )
   
            targetRandomAccess.get().set((T)sourceCursor.get() );
        }
	}
	
	/*
	 * return the sum of a RandomAccessibleInterval
	 */
	public static <T extends RealType<T>> double sum2(final RandomAccessibleInterval<T> input)
	{
		Double sum = 0.0;
		
		LocalizingZeroMinIntervalIterator i = new LocalizingZeroMinIntervalIterator(input);
		
		RandomAccess<T> s = input.randomAccess();
		
		
		while (i.hasNext()) 
		{
		   i.fwd();
		   s.setPosition(i);
		   
		   sum+=s.get().getRealDouble();
		}
		
		return sum;
	}
		
	/*
	 * return the sum of an Iterable
	 */
	public static <T extends RealType<T>> double sum(final Iterable<T> input)
	{
		Double sum = 0.0;
		
		for (final T type:input)
		{
			sum += type.getRealDouble();		
		}
		
		return (sum);
	}
	
	/*
	 * return the squared sum of an Iterable
	 */
	public static <T extends RealType<T>> double squaredSum(final Iterable<T> input)
	{
		Double sum = 0.0;
		Double temp;
		
		for (final T type:input)
		{
			temp = type.getRealDouble();
			
			sum+=temp*temp;
		}
	
		return sum;
	}
	
	/*
	 * return the standard deviation of an image
	 */
	public static <T extends RealType<T>> double standardDeviation(final Img<T> image)
	{
		long numPixels = image.dimension(0);
		
		for (int i=1;i<image.numDimensions();i++)
		{
			numPixels*=image.dimension(i);
		}
		
		double mean = sum(image)/numPixels;
		
		double variance = 0.0;
		
		Cursor<T> cimage = image.cursor();
		
		while (cimage.hasNext())
		{
			cimage.fwd();
			
			double dif = cimage.get().getRealDouble()-mean;
			
			variance += dif*dif;
		}
		
		variance /= numPixels;
		
		double std = java.lang.Math.sqrt(variance);
		
		return std;
	}	
	/*
	 * compute the sum of likelihood of a process with observation i and mean r
	 */
	public static <T extends RealType<T>> double likelihood(final RandomAccessibleInterval<T> i, final Img<T> r)
	{
		Double likelihood = 0.0;
		
		RandomAccess<T> imageRandomAccess = i.randomAccess();
		Cursor<T> rcursor=r.cursor();
		
		double ival, rval;
		
		while (rcursor.hasNext())
		{
			rcursor.fwd();
			imageRandomAccess.setPosition(rcursor);
			
			ival=imageRandomAccess.get().getRealDouble();
			rval=rcursor.get().getRealDouble();
			
			likelihood += ival * java.lang.Math.log(rval)-rval;	
		}
		
		return likelihood;
	}
	
	/*
	 * compute sum of likelihood of process with observation i and mean r
	 */
	public static <T extends RealType<T>> double likelihood(final Img<T> i, final Img<T> r)
	{
		Double likelihood = 0.0;
		
		Cursor<T> icursor=i.cursor();
		Cursor<T> rcursor=r.cursor();
		
		double ival, rval;
		
		while (icursor.hasNext())
		{
			icursor.fwd();
			rcursor.fwd();
			
			ival=icursor.get().getRealDouble();
			rval=rcursor.get().getRealDouble();
			
			likelihood += ival * java.lang.Math.log(rval)-rval; 
		}
		
		return likelihood;
	}
	
	
	public static <T extends RealType<T>> double squaredErrorWithOffset(final RandomAccessibleInterval<T> rnd1, final RandomAccessibleInterval<T> rnd2, long[] offset)
	{
		Double squaredError = 0.0;
		Double temp;
		
		LocalizingZeroMinIntervalIterator i1 = new LocalizingZeroMinIntervalIterator(rnd1);

		RandomAccess<T> access1 = rnd1.randomAccess();
		RandomAccess<T> access2 = rnd2.randomAccess();
		
		double check1=0.0;
		double check2=0.0;
		
		int currentSlice=0;
		
		long[] r2Pos=new long[i1.numDimensions()];
		
		while (i1.hasNext()) 
		{
		   i1.fwd();
		   
		   for (int j=0;j<i1.numDimensions();j++)
		   {
			   r2Pos[j]=(long)(i1.getFloatPosition(j))+offset[j];
		   }
		   
		   access1.setPosition(i1);
		   access2.setPosition(r2Pos);
		   	   
		   check1=check1+access1.get().getRealDouble();
		   check2=check2+access2.get().getRealDouble();
		   
		   temp=access1.get().getRealDouble()-access2.get().getRealDouble();
			
		   squaredError+=temp*temp;
		}
		
		return squaredError;
	}
	
	/*
	 * return squared error between img1 and img2
	 */
	public static <T extends RealType<T>> double squaredError(final Img<T> img1, final Img<T> img2)
	{
		Double squaredError = 0.0;
		
		Cursor<T> cursor1 = img1.cursor();
		Cursor<T> cursor2 = img2.cursor();
		
		double error;
		
		while (cursor1.hasNext())
		{
			cursor1.fwd();
			cursor2.fwd();
			
			error=cursor1.get().getRealDouble()-cursor2.get().getRealDouble();
			
			squaredError += error*error;
		}
		
		return squaredError;
	}
	
	public static <T extends RealType<T>> double squaredErrorCenter(final Img<T> img1, final Img<T> img2)
	{
		Double squaredError = 0.0;
		
		System.out.println("squared error center in progress... "+img1.dimension(0)+" "+img2.dimension(0));
		
		return squaredError;
	}
	
	/*
	 * return sum squared error between img1 and img2 and also place squared error in destination image
	 */
	public static <T extends RealType<T>> double createSquaredErrorImage(final Img<T> img1, final Img<T> img2, final Img<T> destination)
	{
		Double squaredError = 0.0;
		
		Cursor<T> cursor1 = img1.cursor();
		Cursor<T> cursor2 = img2.cursor();
		Cursor<T> destinationCursor = destination.cursor();
		
		double error;
		
		while (cursor1.hasNext())
		{
			cursor1.fwd();
			cursor2.fwd();
			destinationCursor.fwd();
			
			error=cursor1.get().getRealDouble()-cursor2.get().getRealDouble();
			
			squaredError += error*error;
			
			destinationCursor.get().setReal(squaredError);
		}
		
		return squaredError;
	}
	
	/*
	 * compute the sum of the absolute change between img1 and img2
	 */
	public static <T extends RealType<T>> double absoluteChange(final Img<T> img1, final Img<T> img2)
	{
		Double absoluteChange = 0.0;
		
		Cursor<T> cursor1 = img1.cursor();
		Cursor<T> cursor2 = img2.cursor();
		
		while (cursor1.hasNext())
		{
			cursor1.fwd();
			cursor2.fwd();
		
			absoluteChange += java.lang.Math.abs(cursor1.get().getRealDouble()-cursor2.get().getRealDouble());
		}
		
		return absoluteChange;
	}
	
	/*
	 * return the absolute change normalized by the total sum of img1
	 * (Todo: should be squared sum??)
	 */
	public static<T extends RealType<T>> double relativeChange(final Img<T> img1, final Img<T> img2)
	{
		
		Double absoluteChange = absoluteChange(img1, img2);
		
		Double relativeChange = absoluteChange/sum(img1);
		
		return relativeChange;
	}
	
	/*
	 * return the total variation of the source image
	 */
	public static <T extends RealType<T>> double totalVariation(final Img<T> source)
	{
		double variation = 0.0;
		
		long[] dims=new long[]{source.dimension(0), source.dimension(1), source.dimension(2)};
		
		// extend the source zero pad
		RandomAccessible<T> sourceExtended = Views.extendValue(source, source.firstElement());
		
		Img<T> xgradient = source.factory().create(dims, source.firstElement());
		PartialDerivative.gradientCentralDifference(sourceExtended, xgradient, 0);
		
		Img<T> ygradient = source.factory().create(dims, source.firstElement());
		PartialDerivative.gradientCentralDifference(sourceExtended, ygradient, 0);
		
		Img<T> zgradient = source.factory().create(dims, source.firstElement());
		PartialDerivative.gradientCentralDifference(sourceExtended, ygradient, 0);
		
		Cursor<T> xcursor = xgradient.cursor();
		Cursor<T> ycursor = ygradient.cursor();
		Cursor<T> zcursor = zgradient.cursor();
		
		
		while (xcursor.hasNext())
		{
			xcursor.fwd();ycursor.fwd();zcursor.fwd();
		
			double temp=xcursor.get().getRealDouble()*xcursor.get().getRealDouble()+
						ycursor.get().getRealDouble()*ycursor.get().getRealDouble()+
						zcursor.get().getRealDouble()*zcursor.get().getRealDouble();
			
			variation += java.lang.Math.sqrt(temp);
		}
		
		return variation;
	}
	
	/*
	 * normalize the Iterable
	 */
	public static <T extends RealType<T>> void norm(final Iterable<T> iterable)
	{
		final double sum=sum(iterable);
		
		for (final T type:iterable)
		{
			type.setReal(type.getRealDouble()/sum);
		}
	}
	
	/*
	 * set all values in the iterable to a constant value
	 */
	public static <T extends RealType<T>> void set(final Iterable<T> iterable, final double value)
	{
		for (final T type:iterable)
		{
			type.setReal(value);
		}
	}
	
	/*
	 * rescale the Iterable
	 */
	public static <T extends RealType<T>> void rescale(final Iterable<T> iterable, final double scale)
	{
		final double sum=sum(iterable);
		 
		for (final T type:iterable)
		{
			type.setReal(type.getRealDouble()*scale);
		}
	}
	
	/*
	 * add an offset to each value in the iterable
	 */
	public static <T extends RealType<T>> void addOffset(final Iterable<T> iterable, final double offset)
	{
		for (final T type:iterable)
		{
			type.setReal(type.getRealDouble()+offset);
		}
	}
	
	/*
	 * rescale the image with a new max value
	 */
	public static <T extends RealType<T>> void rescaleWithNewMax(final Img<T> img, final double newMax)
	{	
		
		T min=img.firstElement().copy();
		min.setReal(min.getMaxValue());
		
		T max=img.firstElement().copy();

		max.setReal(max.getMinValue());
		ComputeMinMax<T> minmax=new ComputeMinMax<T>(img, min, max);
		
		minmax.process();
		
		double scale = newMax/minmax.getMax().getRealDouble();
		
		rescale(img, scale);
	}
	
	/*
	 * show the min, max and sum of an image
	 */
	public static <T extends RealType<T>> void showStats(RandomAccessibleInterval<T> image)
	{	
		IterableInterval<T> it=Views.iterable(image);
		
		T min=it.firstElement().copy();
		min.setReal(min.getMaxValue());
		
		T max=it.firstElement().copy();
		max.setReal(max.getMinValue());
		
		ComputeMinMax<T> minmax=new ComputeMinMax<T>(it, min, max);
		
		minmax.process();
		
		double sum=sum2(image);
		
		System.out.println("min: "+minmax.getMin()+" max: "+ minmax.getMax()+" sum: "+sum);
		
	}
	
	/*
	 * compute the max value of an image
	 */
	public static <T extends RealType<T>> double getMax(Img<T> image)
	{
		
		T min=image.firstElement().copy();
		min.setReal(min.getMaxValue());
		
		T max=image.firstElement().copy();
		max.setReal(max.getMinValue());
		
		ComputeMinMax<T> minmax=new ComputeMinMax<T>(image, min, max);
		minmax.process();
		
		return minmax.getMax().getRealDouble();
	}
	
	public static <T extends RealType<T>> void SaveImg(Img<T> image, String name)
	{
		final SCIFIO scifio = new SCIFIO();
		final ImgSaver saver = new ImgSaver(scifio.getContext());
		
		ImgPlus<T> imgPlus=Wrap3DImg(image, "temp");
		
		try
		{
			saver.saveImg(name, imgPlus);
		}
		catch (Exception ex)
		{
			
		}
	}
	
	public static <T extends RealType<T>> Img<T> CreateImage(Img<T> template, ImgFactory<T> factory)
	{
		final int numDims=template.numDimensions();
		
		final long[] dims = new long[numDims];
		
		for (int i=0;i<numDims;i++)
		{
			dims[i]=template.dimension(i);
		}
		
		return factory.create(dims, template.firstElement());
	}
	
	public static <T extends RealType<T>> Img<T> CreateNdImage(Img<T> template, ImgFactory<T> factory)
	{
		final long[] dims = new long[template.numDimensions()];
		
		for (int i=0;i<template.numDimensions();i++)
		{
			dims[i]=template.dimension(i);
		}
		
		return factory.create(dims, template.firstElement());
	}
	
	public static <T extends RealType<T>> Img<T> CreateNdImage(Img<T> template)
	{
		return CreateNdImage(template, template.factory());
	}
	
	public static <T extends RealType<T>> Img<T> Create3dImage(RandomAccessibleInterval<T> template, ImgFactory<T> factory, T t)
	{
		final long[] dims = new long[3];
		
		for (int i=0;i<3;i++)
		{
			dims[i]=template.dimension(i);
		}
		https://github.com/bnorthan/RogueImageJPlugins/blob/master/SimpleSimpleITKDecon/src/main/java/com/truenorth/itk/commands/RichardsonLucyITKCommandSimple.java
		return factory.create(dims, t);
	}
	/*
	 * wrap an Img as an ImgPlus
	 */
	public static <T extends RealType<T>> ImgPlus<T> Wrap3DImg(Img<T> image, String name)
	{
		AxisType[] ax = new AxisType[3];
        ax[0]=Axes.X;
        ax[1]=Axes.Y;
        ax[2]=Axes.Z;
       // ax[3]=Axes.TIME;
        
        return new ImgPlus<T>(image, name, ax);
	}

	/*
	 * print out info about current state of system memory
	 */
	public static void PrintMemoryStatuses()
	{
		Runtime runtime = Runtime.getRuntime();
		
		NumberFormat format = NumberFormat.getInstance();
			
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		
		System.out.println("free memory: "+format.format(freeMemory/1024));
		System.out.println("allocated memory: " + format.format(allocatedMemory / 1024));https://github.com/bnorthan/RogueImageJPlugins/blob/master/SimpleSimpleITKDecon/src/main/java/com/truenorth/itk/commands/RichardsonLucyITKCommandSimple.java
		System.out.println("max memory: " + format.format(maxMemory / 1024));
		System.out.println("total free memory: "+format.format((freeMemory+(maxMemory-allocatedMemory))/1024));	
	}
	
	public static void Pause()
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
    	try
    	{
    		System.out.println("Paused.  Press any key to continue...");
    		String dumb = br.readLine();
    	}
    	catch (IOException ex)
    	{
    		// ...
    	}
	}
	
	public static<T extends RealType<T>> Img<T> convertFloatBufferToImage(float[] buffer, int[] size, ImgFactory<T> imgFactory, T type)
	{
		// use the image factory to create an img
		Img<T> image = imgFactory.create(size, type);
					
		// get a cursor so we can iterate through the image
		final Cursor<T> cursor = image.cursor();
					
		int i=0;
					
		// iterate through the image and copy from the psf buffer
		while (cursor.hasNext())
		{
			cursor.fwd();
						
			cursor.get().setReal(buffer[i]);
						
			i++;
		}
					
		return image;
	}
	
	public static<T extends RealType<T>> float[] convertImageToFloatBuffer(Img<T> image)
	{
		int size=1;
		
		for (int d=0;d<image.numDimensions();d++)
		{
			size*=image.dimension(d);
		}
		
		float[] buffer=new float[size];
					
		// get a cursor so we can iterate through the image
		final Cursor<T> cursor = image.cursor();
					
		int i=0;
					
		// iterate through the image and copy from the psf buffer
		while (cursor.hasNext())
		{
			cursor.fwd();
						
			buffer[i]=cursor.get().getRealFloat();
						
			i++;
		}
					
		return buffer;
	}
	
	private static int pseudoRandom(int seed) {
		return 3170425 * seed + 132102;
	}
	
	public static<T extends RealType<T>> Img<UnsignedByteType> generateUnsignedByteTestImg(int seed, final boolean fill, final long... dims)
	{
		final byte[] array =
				new byte[(int) Intervals.numElements(new FinalInterval(dims))];

		if (fill) 
		{
			
			for (int i = 0; i < array.length; i++) 
			{
				seed=pseudoRandom(seed);
				array[i] = (byte)seed; 
			}
		}

		return ArrayImgs.unsignedBytes(array, dims);
	}
	
	public static Img<ByteType> generateByteTestImg(int seed, final boolean fill,
			final long... dims)
		{
			final byte[] array =
				new byte[(int) Intervals.numElements(new FinalInterval(dims))];

			if (fill) {
				for (int i = 0; i < array.length; i++) {
					seed=pseudoRandom(seed);
					array[i] = (byte) seed;
				}
			}

			return ArrayImgs.bytes(array, dims);
		}
	
	public static Img<DoubleType> generateDoubleTestImg(int seed, final boolean fill,
			final long... dims)
		{
			final double[] array =
				new double[(int) Intervals.numElements(new FinalInterval(dims))];

			if (fill) {
				for (int i = 0; i < array.length; i++) {
					seed=pseudoRandom(seed);
					array[i] = (double) seed;
				}
			}

			return ArrayImgs.doubles(array, dims);
		}
	
	public static Img<FloatType> generateFloatTestImg(int seed, final boolean fill,
			final long... dims)
		{
			final float[] array =
				new float[(int) Intervals.numElements(new FinalInterval(dims))];

			if (fill) {
				for (int i = 0; i < array.length; i++) {
					seed=pseudoRandom(seed);
					array[i] = (float) seed;
				}
			}

			return ArrayImgs.floats(array, dims);
		}
	
}

