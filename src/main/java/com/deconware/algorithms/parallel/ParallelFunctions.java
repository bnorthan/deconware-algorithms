package com.deconware.algorithms.parallel;

/*import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imagej.ops.chunker.Chunk;
import net.imagej.ops.chunker.Chunker;

import net.imagej.ops.OpService;

public class ParallelFunctions 
{
	public static<T extends RealType<T>> long getLength(Img<T> img)
	{
		long length=1;
		for (int d=0;d< img.numDimensions();d++)
		{
			length*=img.dimension(d);
		}
		return length;
	}
	
	public static <T extends RealType<T>> Img<T> MulAndExponent(final Img<T> img1, final Img<T> img2, final float a)
	{
		final Img<T> out = img1.factory().create(img1, img1.firstElement());
		
		Chunk chunk=new Chunk() {

			@Override
			public void execute(final int startIndex, final int stepSize, final int numSteps)
			{
				final Cursor<T> cursor1 = img1.cursor();
				final Cursor<T> cursor2 = img2.cursor();
				final Cursor<T> cursorOut = out.cursor();
				
				cursor1.jumpFwd(startIndex);
				cursor2.jumpFwd(startIndex);
				cursorOut.jumpFwd(startIndex);
			
				for (int i = startIndex; i < startIndex + numSteps; i++) 
				{
					cursor1.fwd();
					cursor2.fwd();
					cursorOut.fwd();
					
					double val1=cursor1.get().getRealDouble();
					double val2=cursor2.get().getRealDouble();
					
					val2=net.jafama.StrictFastMath.pow(val2, a);
					
					cursorOut.get().setReal(val1*val2);
				;
				}
			}
		};
		
		opService.run(Chunker.class, chunk, getLength(img1));
		
		return out;
	}
	
	public Chunk MulAndExponentChunk()
	{
		return new Chunk() {

			@Override
			public void execute(final int startIndex, final int stepSize, final int numSteps)
			{
	
				for (int i = startIndex; i < startIndex + numSteps; i++) 
				{
					data[i] += value;
				}
			}
		};
	}
}*/
