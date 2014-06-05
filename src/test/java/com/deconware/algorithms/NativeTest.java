package com.deconware.algorithms;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.deconware.algorithms.wrappers.deconvolution.YacuDecuGPUWrapper;

public class NativeTest 
{
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
