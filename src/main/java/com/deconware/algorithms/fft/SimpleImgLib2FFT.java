package com.deconware.algorithms.fft;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft.FourierTransform;
import net.imglib2.algorithm.fft.InverseFourierTransform;
import net.imglib2.algorithm.fft.FourierTransform.FFTOptimization;
import net.imglib2.algorithm.fft.FourierTransform.PreProcessing;
import net.imglib2.algorithm.fft.FourierTransform.Rearrangement;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.ComplexType;

/**
 * wraps imglib2 fft using simplefft interface
 * 
 * @author bnorthan
 *
 * @param <T>
 * @param <S>
 */
public class SimpleImgLib2FFT<T extends RealType<T>, S extends ComplexType<S>> implements SimpleFFT<T,S>
{
	FourierTransform<T, S> forward;
	InverseFourierTransform<T, S> inverse; 
	
	final ImgFactory<T> imgImgFactory;
	final ImgFactory<S> fftImgFactory;
	
	Img<S> imgForward;
	
	public SimpleImgLib2FFT(RandomAccessibleInterval<T> image, ImgFactory<T> imgImgFactory, ImgFactory<S> fftImgFactory, final S complexType)
	{
		this.imgImgFactory=imgImgFactory;
		this.fftImgFactory=fftImgFactory;
		forward = new FourierTransform<T, S>(image, fftImgFactory, complexType );
		
		forward.setMinExtension(0);  
		forward.setRelativeImageExtension(0);
		
		// set optimization and threading parameters
		
		forward.setFFTOptimization(FFTOptimization.SPEED);
		//forward.setNumThreads(this.getNumThreads());
		
		forward.setPreProcessing(PreProcessing.EXTEND_MIRROR);
		forward.setRearrangement(Rearrangement.UNCHANGED);		
	}
	
	@Override 
	public Img<S> forward(final RandomAccessibleInterval<T> input)
	{
		forward.process();
		
		imgForward=forward.getResult();
		
		return imgForward;
	}
	
	@Override
	public Img<T> inverse(final Img<S> fft)
	{
		inverse =
	        	new InverseFourierTransform<T, S>(imgForward, imgImgFactory, forward);
		
		inverse.process();
		return inverse.getResult();
	}
	
	@Override
	public int[] GetPaddedInputSize(final Img<T> input)
	{
		int numDimensions = input.numDimensions();
		
		int[] dimensions = new int[numDimensions];
		
		for (int i=0;i<numDimensions;i++)
		{
			dimensions[i]=(int)(input.dimension(i));
		}
		
		return null;
	}

}
