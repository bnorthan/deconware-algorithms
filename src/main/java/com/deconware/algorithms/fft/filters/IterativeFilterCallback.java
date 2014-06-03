package com.deconware.algorithms.fft.filters;

import net.imglib2.type.numeric.RealType;
import net.imglib2.img.Img;
import net.imglib2.RandomAccessibleInterval;

/**
 * A callback to be used by an iterative filter to update it's status
 * 
 * @author bnorthan
 *
 * @param <T>
 */
public interface IterativeFilterCallback<T extends RealType<T>> 
{
    public void DoCallback(int iteration, RandomAccessibleInterval<T> image, Img<T> estimate, Img<T> reblurred, long executionTime);
}