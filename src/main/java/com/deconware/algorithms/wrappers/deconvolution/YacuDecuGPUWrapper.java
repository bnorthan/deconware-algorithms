package com.deconware.algorithms.wrappers.deconvolution;

import com.deconware.algorithms.wrappers.NativeWrapper;

public class YacuDecuGPUWrapper implements NativeWrapper
{
	public boolean loadDependencies()
	{
		try
		{
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
