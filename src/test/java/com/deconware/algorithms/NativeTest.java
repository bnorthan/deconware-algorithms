package com.deconware.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.deconware.algorithms.wrappers.deconvolution.YacuDecuGPUWrapper;
import com.deconware.algorithms.wrappers.DotProductGPUWrapper;
import com.deconware.wrappers.DeconwareSwig;
import com.deconware.algorithms.parallel.math.ParallelDot;

import net.imglib2.img.Img;
import net.imglib2.Point;
import net.imglib2.img.ImgFactory;
import net.imglib2.Cursor;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.real.FloatType;

public class NativeTest 
{
	@Test
	public void testJavaWrappers()
	{
		long xSize=50;
		long ySize=50;
		long zSize=1;
		
		long[] sizeArray=new long[]{xSize, ySize, zSize};
		
		// create a planer image factory
		ImgFactory<FloatType> factory = new PlanarImgFactory<FloatType>();
		
		Img<FloatType> img1=factory.create(sizeArray, new FloatType());
		Img<FloatType> img2=factory.create(sizeArray, new FloatType());
		Cursor<FloatType> c1=img1.cursor();
		Cursor<FloatType> c2=img2.cursor();
		
		DotProductGPUWrapper wrapper=new DotProductGPUWrapper();
		
		boolean dependenciesMet=wrapper.loadDependencies();
		
		int size=(int)(xSize*ySize);
		float[] in1=new float[size];
		float[] in2=new float[size];
		
		for (int i=0;i<size;i++)
		{
			in1[i]=2.0f;
			in2[i]=2.0f;
			
			c1.fwd();
			c2.fwd();
			
			c1.get().setReal(2.0f);
			c2.get().setReal(2.0f);
		}
		
		float dot_device=DeconwareSwig.dot_device((long)size, in1, in2);
		FloatType dot_parallel=ParallelDot.RunParallelDot(img1, img2, new FloatType());
		
		System.out.println("dot parallel is: "+dot_parallel);
		System.out.println("dot device is: "+dot_device);
		System.out.println("!!!!!!!!!!!!!!!!!!!!dot products!!!!!!!!!!!!");
	}
	
	@Test  
	public void testYacuDecu() 
	{
		// TODO:
		// how to handle case where it can't find library????
		
		/*YacuDecuGPUWrapper wrapper=new YacuDecuGPUWrapper();
		
		// check dependencies
		boolean dependenciesMet=wrapper.checkDependencies();
		
		// if dependencies are not met it isn't an error... just means
		// cuda is not available so can't run test. 
		if (!dependenciesMet)
		{
			System.out.println();
			System.out.println("Could not find YacuDecu native libraries.");
			System.out.println("Skipping YacuDecu test...");
			System.out.println();
			
			return;
		}*/
		
	}
}
