package com.deconware.algorithms.fft.filters;

import com.deconware.algorithms.StaticFunctions;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;

import net.imglib2.algorithm.fft.FourierTransform;
import net.imglib2.algorithm.fft.InverseFourierTransform;
import net.imglib2.algorithm.fft.FourierTransform.FFTOptimization;
import net.imglib2.algorithm.fft.FourierTransform.PreProcessing;
import net.imglib2.algorithm.fft.FourierTransform.Rearrangement;

/*import com.deconware.algorithms.fft.FourierTransformBN.FFTOptimization;
import com.deconware.algorithms.fft.FourierTransformBN.PreProcessing;
import com.deconware.algorithms.fft.FourierTransformBN.Rearrangement;*/
import com.deconware.algorithms.roi.MedianFilter;

/**
 * 
 * INCOMPLETE implementation of Residual Denoised version of Richardson Lucy
 
 * Not only incomplete but quite possibly (likely) full of errors.  If anybody stumbles upon
 * this feel free to try and fix it.  
 * 
 * @author bnorthan
 *
 * @param <T>
 * @param <S>
 */
public class ResidualDenoisedRL <T extends RealType<T>, S extends RealType<S>> extends AbstractIterativeFilter<T,S>
{
	public static <T extends RealType<T>, S extends RealType<S>> Img<T> deconvolve(final Img<T> img, final Img<S> kernel, int maxIterations, final Img<T> truth) throws IncompatibleTypeException
	{
		final ResidualDenoisedRL<T,S> rl = new ResidualDenoisedRL<T,S>(img, kernel);
		rl.setMaxIterations(maxIterations);
		rl.setTruth(truth);
		rl.process();
		return rl.getResult();
	}
	
	public static <T extends RealType<T>, S extends RealType<S>> Img<T> deconvolve(final Img<T> img, final Img<S> kernel, int maxIterations) throws IncompatibleTypeException
	{
		return deconvolve(img, kernel, maxIterations, null);
	}
	
	public ResidualDenoisedRL( final RandomAccessibleInterval<T> image, final RandomAccessibleInterval<S> kernel,
			   final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory,
			   final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, imgFactory, kernelImgFactory, fftImgFactory );
	}
	
	public ResidualDenoisedRL( final Img<T> image, final Img<S> kernel, final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, fftImgFactory );
	}
	
	public ResidualDenoisedRL( final Img< T > image, final Img< S > kernel ) throws IncompatibleTypeException
	{	
		super( image, kernel );
	}
	
	Img<T> trueObserved;
	
	public boolean initialize()
	{
		// create a new fft
		return super.initialize();
	}
	
	protected boolean performIteration( final Img< ComplexFloatType > a, final Img< ComplexFloatType > b )
	{
		boolean result;
		
		// if there is a true observed train the filter...
		
		// 0. Residual is observed minus reblurred
		Img<T> residual = StaticFunctions.Subtract((Img<T>)image, reblurred);
		
		residual=DeNoise(residual);
		
		Img<T> denoisedImage = StaticFunctions.Add(reblurred, residual);
			
		System.out.println("image stats:");
		StaticFunctions.showStats((Img<T>)image);
		
		System.out.println("denoised stats:");
		StaticFunctions.showStats(denoisedImage);
		
		// 1. Reblurred should have allready been created previous...
		
		// 2.  divide observed by reblurred
		
		StaticFunctions.InPlaceDivide(reblurred, denoisedImage);
		
		//System.out.println("showing observed/reblurred");
		//ImageJFunctions.show(temp1, "o over r");
		
		// 3. correlate psf and output of step 2.
		
		// take FFT of output of step 2. 
		FourierTransform<T, ComplexFloatType> fftTemp = new FourierTransform<T, ComplexFloatType>(reblurred, fftImgFactory,new ComplexFloatType() );
		
		// assume it's been extended allready (so we don't want any extra extension)
		
		fftTemp.setMinExtension(0);  
		fftTemp.setRelativeImageExtension(0);
		
		// set optimization and threading parameters
		
		fftTemp.setFFTOptimization(FFTOptimization.SPEED);
		fftTemp.setNumThreads(this.getNumThreads());
		
		fftTemp.setPreProcessing(PreProcessing.NONE);
		fftTemp.setRearrangement(Rearrangement.UNCHANGED);
		
		if (!fftTemp.checkInput() || !fftTemp.process())
		{
			errorMessage = "FFT of temp failed: "+fftTemp.getErrorMessage();
			return false;
		}
		
		Img<ComplexFloatType> temp1FFT=fftTemp.getResult();
		
		// complex conjugate multiply transformed psf and output of step 2.  
		
		StaticFunctions.InPlaceComplexConjugateMultiply(temp1FFT, kernelFFT);
		
		//System.out.println("second FFT!");
		
		// inverse fft
		final InverseFourierTransform<T, ComplexFloatType> ifft2 =
	        	new InverseFourierTransform<T, ComplexFloatType>(temp1FFT, imgFactory, fftTemp);
	        
	    if (!ifft2.checkInput() || !ifft2.process())
	    {
	        errorMessage = "Inverse FFT of kernel failed: " +ifft2.getErrorMessage();
			return false;
	    }
	        
		Img<T> temp2 = ifft2.getResult();
	    
		// multiply output of step 3 and current estimate
		StaticFunctions.InPlaceMultiply(estimate, temp2);
		
		// create reblurred so we can use it to calculate likelihood and so it is ready for next time
		createReblurred();
			
		return true;
	}
	
	protected Img<T> DeNoise(Img<T> r)
	{
		long size = 3;
		
		long filterSizeArray[] = new long[r.numDimensions()];
		
		for (int i=0;i<r.numDimensions();i++)
		{
			filterSizeArray[i]=size;
		}
		
		double sumR=StaticFunctions.sum(r);
		
		System.out.println("denoise by median...");
		
		/*MedianFilter<T> medianFilter = new MedianFilter<T>(r, filterSizeArray);
		Img<T> denoised=medianFilter.getResult();
		
		long numVoxels=1;
		
		for (int i=0;i<denoised.numDimensions();i++)
		{
			numVoxels*=denoised.dimension(i);
		}
		
		double sumD=StaticFunctions.sum(denoised);
		
		StaticFunctions.addOffset(denoised, (sumR-sumD)/numVoxels);*/
		
		return null;
		
	}
	
	public void setTrueObserved(Img<T> trueObserved)
	{
		this.trueObserved=trueObserved;
	}

}

