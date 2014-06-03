package com.deconware.algorithms.noise;

public class FuzzyMembershipFunction 
{
	double[][] membershipFunction;
	
	int membershipSize1;
	int membershipSize2;
	
	double minParameter1;
	double maxParameter1;
	
	double minParameter2;
	double maxParameter2;
	
	public FuzzyMembershipFunction(int membershipSize1, int membershipSize2)
	{
		this.membershipSize1=membershipSize1;
		this.membershipSize2=membershipSize2;
		
		membershipFunction = new double[membershipSize1][membershipSize2];
		
		for (int i=0;i<membershipSize1;i++)
		{
			for (int j=0;j<membershipSize2;j++)
			{
				membershipFunction[i][j]=1.0;
			}
		}
	}
	public double[][] getMembershipFunction() 
	{ 
		return membershipFunction; 
	}
	
	public int getMembershipSize1()
	{
		return membershipSize1;
	}
	
	public int getMembershipSize2()
	{
		return membershipSize2;
	}
	
	public double getMinParameter1()
	{
		return minParameter1;
	}
	
	public void setMinParameter1(double minParameter1)
	{
		this.minParameter1=minParameter1;
	}
	
	public double getMaxParameter1()
	{
		return maxParameter1;
	}
	
	public void setMaxParameter1(double maxParameter1)
	{
		this.maxParameter1=maxParameter1;
	}
	
	public double getMinParameter2()
	{
		return minParameter2;
	}
	
	public void setMinParameter2(double maxParameter2)
	{
		this.minParameter2=maxParameter2;
	}
	
	public double getMaxParameter2()
	{
		return maxParameter2;
	}
	
	public void setMaxParameter2(double maxParameter2)
	{
		this.maxParameter2=maxParameter2;
	}
	
	public int getBucket1(double val)
	{
		
		if (val<minParameter1)
		{
			return 0;
		}
		else if (val>maxParameter1)
		{
			return membershipSize1-1;
		}
		else
		{
			double bucketSize1=(maxParameter1-minParameter1)/membershipSize1;
			return (int)((val-minParameter1)/(bucketSize1));
		}
	}
	
	public int getBucket2(double val)
	{
		if (val<minParameter2)
		{
			return 0;
		}
		else if (val>maxParameter2)
		{
			return membershipSize2-1;
		}
		else
		{
			double bucketSize2=(maxParameter2-minParameter2)/membershipSize2;
			return (int)((val-minParameter2)/(bucketSize2));
		}
	}
	
	public double getFuzzyMembership(double parameter1, double parameter2)
	{
		double fuzzy=0.0;
		
		int bucket1 = getBucket1(parameter1);
		int bucket2 = getBucket2(parameter2);
		
		return membershipFunction[bucket1][bucket2];
	}
}