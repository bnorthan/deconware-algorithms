package com.deconware.algorithms.fft;

import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.ComplexType;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;

/**
 *  defines a simple interface for a forward and inverse fft that can be used to wrap
 *  other fft implementations
 * 
 * @author bnorthan
 *
 * @param <T>
 * @param <S>
 */

public interface SimpleFFT<T extends RealType<T>, S extends ComplexType<S>> 
{
	public static enum FFTType{SPEED, SIZE, NONE};

	// returns forward fft of "input"
	public Img<S> forward(final RandomAccessibleInterval<T> input);
	
	// returns inverse fft of "fft"
	public Img<T> inverse(final Img<S> fft);
	
	// returns the size that the input needs to be padded to
	public int[] GetPaddedInputSize(final Img<T> input);
}
