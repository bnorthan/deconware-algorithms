package com.deconware.algorithms.wrappers;

public class DotProductGPUWrapper implements NativeWrapper
{
	public boolean loadDependencies()
	{
		try
		{
			System.loadLibrary("DeconwareSwig");
		}
		catch (Exception ex)
		{
			return false;
		}
		
		return true;
	}
}
