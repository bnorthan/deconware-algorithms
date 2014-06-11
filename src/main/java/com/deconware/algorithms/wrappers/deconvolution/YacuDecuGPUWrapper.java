package com.deconware.algorithms.wrappers.deconvolution;

import com.deconware.algorithms.wrappers.NativeWrapper;

public class YacuDecuGPUWrapper implements NativeWrapper
{
	public boolean loadDependencies()
	{
		try
		{
			String javaLibPath = System.getProperty("java.library.path");
			
			System.out.println("path is: "+javaLibPath);
		      
			System.loadLibrary("YacuDecu");
		}
		catch (Exception ex)
		{
			return false;
		}
		
		try
		{
			System.loadLibrary("YacuDecuJavaSwig");
		}
		catch (Exception ex)
		{
			return false;
		}
		
		return true;
	}
}
