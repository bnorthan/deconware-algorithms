package com.deconware.algorithms.fft.filters;

import com.deconware.algorithms.StaticFunctions;
import com.deconware.algorithms.fft.SimpleFFT;
import com.deconware.algorithms.fft.SimpleFFTFactory;
import com.deconware.algorithms.phantom.Phantoms;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import net.imglib2.Point;

import com.deconware.algorithms.acceleration.Accelerator;

import com.deconware.algorithms.acceleration.VectorAccelerator;
import com.deconware.algorithms.acceleration.MultiplicativeAccelerator;

import com.deconware.algorithms.fft.SimpleFFTFactory.FFTTarget;

/**
 * Base class for an iterative deconvolution filter
 * 
 * @author bnorthan
 *
 * @param <T>
 * @param <S>
 */
public abstract class AbstractIterativeFilter<T extends RealType<T>, S extends RealType<S>> extends AbstractFrequencyFilter<T,S> 
	implements IterativeFilter<T, S>
{
	
	public AbstractIterativeFilter( final RandomAccessibleInterval<T> image, final RandomAccessibleInterval<S> kernel,
			   final ImgFactory<T> imgFactory, final ImgFactory<S> kernelImgFactory,
			   final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, imgFactory, kernelImgFactory, fftImgFactory );
	}
	
	/**
	 * @param image
	 * @param kernel
	 * @param imgFactory
	 * @param kernelImgFactory
	 * @throws IncompatibleTypeException
	 */
	public AbstractIterativeFilter(final RandomAccessibleInterval<T> image, 
			final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory,
			final ImgFactory<S> kernelImgFactory) throws IncompatibleTypeException
	{
		super(image, kernel, imgFactory, kernelImgFactory);
	}
	
	/**
	 * @param image
	 * @param kernel
	 * @param fftImgFactory
	 */
	public AbstractIterativeFilter( final Img<T> image, final Img<S> kernel, final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		super( image, kernel, fftImgFactory );
	}
	
	/**
	 * @param image
	 * @param kernel
	 * @throws IncompatibleTypeException
	 */
	public AbstractIterativeFilter( final Img< T > image, final Img< S > kernel ) throws IncompatibleTypeException
	{
		super( image, kernel );
	}	
	
	Img<T> estimate;
	Img<T> reblurred;
		
	Img<ComplexFloatType> estimateFFT;
	
	SimpleFFT<T, ComplexFloatType> fftEstimate;
	
	int iteration=0;
	
	int maxIterations = 10;
	
	int computeStatsInterval = 1;
			
	IterativeFilterCallback<T> callback=null;
	
	boolean keepOldEstimate=false;
	
	FirstGuessType firstGuessType=FirstGuessType.MEASURED;
	
	Img<T> normalization=null;
	
	Accelerator accelerator=null;
	
	/**
	 * perform the initial ffts, set first guess
	 * @return
	 */
	public boolean initialize()
	{
		boolean result;
		
		// perform fft of input
		
		result = performInputFFT();
		
		if (!result)
		{
			return result;
		}
		
		// perform fft of psf	
		result = performPsfFFT();
		
		if (!result)
		{
			return result;
		}
		
		// set first guess of the estimate if it has not been set explicitly
		if (estimate==null)
		{
			final T type = Util.getTypeFromInterval(image);
			estimate = imgFactory.create(image, type);
			
			if (firstGuessType==FirstGuessType.MEASURED)
			{
				setEstimate(image);
			}
			else if (firstGuessType==FirstGuessType.BLURRED_MEASURED)
			{
				setEstimate(image);
				
				setEstimate(reblurred);
			}
			else if (firstGuessType==FirstGuessType.CONSTANT)
			{
				
				Iterable<T> iterableImage=Views.iterable(image);
				Iterable<T> iterableEstimate=Views.iterable(estimate);
				
				final double sum=StaticFunctions.sum(iterableImage);
				
				final long numPixels=image.dimension(0)*image.dimension(1)*image.dimension(2);
				
				final double constant=sum/(numPixels);
				
				StaticFunctions.set(iterableEstimate, constant);
						
				createReblurred();
			}
			else if (firstGuessType==FirstGuessType.INVERSE_FILTER)
			{
				try
				{
					WienerFilter<T, S> wiener=new WienerFilter<T,S>(image,
							kernel, 
							imgFactory,
							kernelImgFactory);
				
					wiener.setRegularizationFactor(0.01);
					
					System.out.println("Wiener");
					wiener.process();
					
					setEstimate(wiener.getResult());
				}
				catch(Exception ex)
				{
					// TODO handle exception
				}
			}
		}
		
		// create normalization if needed
		if(this.convolutionStrategy==ConvolutionStrategy.NON_CIRCULANT)
		{
			this.CreateNormalizationImageNonCirculant();
		}
		else if (this.convolutionStrategy==ConvolutionStrategy.SEMI_NONCIRCULANT)
		{
			this.CreateNormalizationImageSemiNonCirculant();
		}
					
		if (!result)
		{
			return result;
		}
					
		return true;
	}
	
	/**
	 * overrride process to perform deconvolution iterations.  Return true if successful.
	 */
	@Override
	public boolean process() 
	{		
		boolean result;
		
		initialize();
			
		result=performIterations(maxIterations);
		    
		if (result==true)
		{
			output = estimate;
		}
        
		return result;
	}
	
	/**
	 * Perform iterations
	 * 
	 * TODO: check a stopping criteria
	 * @param n
	 * @return
	 */
	public boolean performIterations(int n)  
	{
		boolean result=true;
			
		while (iteration<n)
		{
			Img<T> oldEstimate=null;
			
			long startTime=System.currentTimeMillis();
			
			// if tracking stats keep track of current estimate in order to compute relative change
			if (keepOldEstimate)
			{
				oldEstimate = estimate.copy();
			}
			
			// perform the iteration
			result=performIteration(imgFFT, kernelFFT);
			
			if (result!=true)
			{
				return result;
			}
			
			long iterationTime=System.currentTimeMillis()-startTime;
			
		//	System.out.println("estimate stats before acceleration");
		//	StaticFunctions.showStats(estimate);
		//	System.out.println();
			
			// if an accelerator has been set accelerate the solution
			if ( (iteration>0)&&(iteration<n-1)&&(accelerator!=null))
			{
				long accelerationStartTime=System.currentTimeMillis();
				
				estimate=accelerator.Accelerate(estimate);
				
				long accelerationTime=System.currentTimeMillis()-accelerationStartTime;
				
				long totalTime=System.currentTimeMillis()-startTime;
				
				System.out.println("iteration time: "+iterationTime);
				System.out.println("acceleration time: "+accelerationTime);
				System.out.println("total time: "+totalTime);
				
			}
			
			createReblurred();
			
		//	System.out.println("estimate stats after acceleration");
		//	StaticFunctions.showStats(estimate);
		//	System.out.println();
			
			// if a callback has been set
			if (callback!=null)
			{ 
				
				double remainder = java.lang.Math.IEEEremainder(iteration, computeStatsInterval);
			
				// call the callback if the callbackInteral is a divisor of the current iteration
				if (remainder == 0)
				{
					callback.DoCallback(iteration, image, estimate, reblurred, iterationTime);
				}
			}
			
			iterationTime=System.currentTimeMillis()-startTime;
			
			System.out.println("Iteration:"+iteration);
		//	System.out.println();
			
			output=estimate;
			
			iteration++;
			
		}
	
		return result;
	}
	
	/**
	 * Perform estimate FFT
	 * @return
	 */
	protected boolean performEstimateFFT()
	{
		if (estimateFFT == null)
		{
			// create a new fft
			fftEstimate = SimpleFFTFactory.GetSimpleFFt(image, imgFactory, fftImgFactory, new ComplexFloatType());
		}
						
		// perform the fft
		estimateFFT = fftEstimate.forward(estimate);
		
		return true;
	}
	
	/** 
	 * Create reblurred by convolving estimate and PSF
	 * @return
	 */
	protected boolean createReblurred()
	{
		// estimate FFT
		boolean result = performEstimateFFT();
		
		if (!result)
		{
			return result;
		}
				
		// complex multiply transformed current estimate with transformed psf
		StaticFunctions.InPlaceComplexMultiply(estimateFFT, kernelFFT);
		
		// compute inverse to get reblurred
		reblurred = fftEstimate.inverse(estimateFFT);
		
		return true;
	}
	
	
	
	/**
	 *  create the normalization image needed for full noncirculant model described here 
	 *	http://bigwww.epfl.ch/deconvolution/challenge/index.html?p=documentation/overview
	 *	Richardson Lucy with the noncirculant model is described in the RLdeblur3D.m script
	 */
	protected void CreateNormalizationImageNonCirculant() 
	{
		int length=k.length;
	
		long[] n=new long[length];
		long[] fft_n;
		
		// calculate n - size of object space
		// n = k (measurement space) + l (psf space) -1
		for (int i=0;i<length;i++)
		{
			n[i]=k[i]+l[i]-1;	
		}
		
		// get size of fft space- may not be same size as object space due to fft padding
		fft_n=SimpleFFTFactory.GetPaddedInputSizeLong(n, FFTTarget.MINES_SPEED);
		
		// create the normalization image
		final T type = Util.getTypeFromInterval(image);
		normalization = imgFactory.create(image, type);
		
		
		Img<T> mask = imgFactory.create(image, type);
			
		// size of the measurement window
		Point size=new Point(3);
		size.setPosition(k[0], 0); //192
		size.setPosition(k[1], 1); //192
		size.setPosition(k[2], 2); //64
	
		// starting point of the measurement window when it is centered in fft space
		Point start=new Point(3);
		start.setPosition((fft_n[0]-k[0])/2, 0); //72
		start.setPosition((fft_n[1]-k[1])/2, 1); //84
		start.setPosition((fft_n[2]-k[2])/2, 2); //73
	
		// size of the object space
		Point maskSize=new Point(3);
		maskSize.setPosition(n[0], 0); //319
		maskSize.setPosition(n[1], 1); //319
		maskSize.setPosition(n[2], 2); //190
		
		// starting point of the object space within the fft space
		Point maskStart=new Point(3);
		maskStart.setPosition((fft_n[0]-n[0])/2+1, 0); //9
		maskStart.setPosition((fft_n[1]-n[1])/2+1, 1); //21
		maskStart.setPosition((fft_n[2]-n[2])/2+1, 2); //11
	
		// draw a cube the size of the measurement space
		Phantoms.drawCube(normalization, start, size, 1.0);
		
		// draw a cube the size of the object space
		Phantoms.drawCube(mask, maskStart, maskSize, 1.0);
	
		// forward FFT
		SimpleFFT<T, ComplexFloatType> fftTemp = 
				SimpleFFTFactory.GetSimpleFFt(normalization, imgFactory, fftImgFactory, new ComplexFloatType() );
		Img<ComplexFloatType> temp1FFT= fftTemp.forward(normalization);
		
		//StaticFunctions.SaveImg(normalization, "/home/bnorthan/Brian2014/Images/General/Deconvolution/Grand_Challenge/EvaluationData/Extended/testFeb10/normalcube_.tif");
		//StaticFunctions.SaveImg(mask, "/home/bnorthan/Brian2014/Images/General/Deconvolution/Grand_Challenge/EvaluationData/Extended/testFeb10/mask_.tif");
		
		// complex conjugate multiply fft of output of step 2 and fft of psf to get normalization factor
		// as done in RLdeblur3D.m script from EPFL
		StaticFunctions.InPlaceComplexConjugateMultiply(temp1FFT, kernelFFT);
		
		// inverse fft
		normalization = fftTemp.inverse(temp1FFT);
		//StaticFunctions.SaveImg(normalization, "/home/bnorthan/Brian2014/Images/General/Deconvolution/Grand_Challenge/EvaluationData/Extended/testFeb10/normalconv_.tif");
		
		// fft space can be slightly larger then the object space so so use a mask to get
		// rid of any values outside the object space.   
		StaticFunctions.InPlaceMultiply(normalization, mask);
		
		//StaticFunctions.SaveImg(normalization, "/home/bnorthan/Brian2014/Images/General/Deconvolution/Grand_Challenge/EvaluationData/Extended/testFeb10/normalfirst_.tif");	
	}
	
	
	/**
	 *  create the normalization image needed for semi noncirculant model
	 */
	protected void CreateNormalizationImageSemiNonCirculant() 
	{
		// k is the window size (valid image region)
		int length=k.length;
	
		long[] n=new long[length];
		long[] fft_n;
		
		// n is the valid image size plus the extended region
		// also referred to as object space size
		for (int d=0;d<length;d++)
		{
			n[d]=image.dimension(d);
		}
		
		// get size of fft space- may not be same size as object space due to fft padding
		fft_n=SimpleFFTFactory.GetPaddedInputSizeLong(n, FFTTarget.MINES_SPEED);
		
		// create the normalization image
		final T type = Util.getTypeFromInterval(image);
		normalization = imgFactory.create(image, type);
		
	//	Img<T> mask = imgFactory.create(image, type);
			
		// size of the measurement window
		Point size=new Point(3);
		size.setPosition(k[0], 0); 
		size.setPosition(k[1], 1); 
		size.setPosition(k[2], 2); 
		
		// starting point of the measurement window when it is centered in fft space
		Point start=new Point(3);
		start.setPosition((fft_n[0]-k[0])/2, 0); 
		start.setPosition((fft_n[1]-k[1])/2, 1); 
		start.setPosition((fft_n[2]-k[2])/2, 2); 
	
		// size of the object space
		Point maskSize=new Point(3);
		maskSize.setPosition(n[0], 0); 
		maskSize.setPosition(n[1], 1); 
		maskSize.setPosition(n[2], 2); 
		
		// starting point of the object space within the fft space
		Point maskStart=new Point(3);
		maskStart.setPosition((fft_n[0]-n[0])/2+1, 0);
		maskStart.setPosition((fft_n[1]-n[1])/2+1, 1); 
		maskStart.setPosition((fft_n[2]-n[2])/2+1, 2); 
		
		for (int i=0;i<3;i++)
		{
			System.out.println(n[i]+""+fft_n[i]);
			System.out.println(maskStart.getIntPosition(i)+" "+maskSize.getIntPosition(i));
			System.out.println("---------------------------------------------------------");
		}
	
		// draw a cube the size of the measurement space
		Phantoms.drawCube(normalization, start, size, 1.0);
		
		// draw a cube the size of the object space
	//	Phantoms.drawCube(mask, maskStart, maskSize, 1.0);
	
		// forward FFT
		SimpleFFT<T, ComplexFloatType> fftTemp = 
				SimpleFFTFactory.GetSimpleFFt(normalization, imgFactory, fftImgFactory, new ComplexFloatType() );
		Img<ComplexFloatType> temp1FFT= fftTemp.forward(normalization);
		
		// complex conjugate multiply fft of output of step 2 and fft of psf to get normalization factor
		// as done in RLdeblur3D.m script from EPFL
		StaticFunctions.InPlaceComplexConjugateMultiply(temp1FFT, kernelFFT);
		
		// inverse fft
		normalization = fftTemp.inverse(temp1FFT);
		
		// fft space can be slightly larger then the object space so so use a mask to get
		// rid of any values outside the object space.   
	//	StaticFunctions.InPlaceMultiply(normalization, mask);
		
		//StaticFunctions.SaveImg(normalization, "/home/bnorthan/Brian2014/Images/General/Deconvolution/Grand_Challenge/EvaluationData/Extended/testFeb10/normalfirst_.tif");	
	}
	
	/**
	 *  sets max iterations
	 */
	public void setMaxIterations(int maxIterations)
	{
		this.maxIterations = maxIterations;
	}
	
	/**
	 * returns max iterations
	 * @return
	 */
	public int getMaxIterations()
	{
		return maxIterations;
	}
	
	/**
	 * set image estimate (without creating reblurred)
	 * @param estimate
	 */
	public void setEstimateImg(Img<T> estimate)
	{
		this.estimate=estimate;
	}
	
	/**
	 * set a callback for updating status
	 */
	public void setCallback(IterativeFilterCallback<T> callback)
	{
		this.callback = callback;
	}
	
	/**
	 * set the estimate and use it to create the reblurred signal 
	 */
	public void setEstimate(RandomAccessibleInterval<T> estimate)
	{
		StaticFunctions.copy2(estimate, this.estimate);
	
		// create reblurred (so it is ready for the first iteration)
	
		boolean result = createReblurred();
	
		if (!result)
		{
			// handle error
		}
	}
	
	/**
	 * get the current estimate
	 */
	public Img<T> getEstimate()
	{
		return estimate;
	}
	
	/**
	 * return the reblurred
	 */
	public Img<T> getReblurred()
	{
		return reblurred;
	}
	
	/**
	 * set the first guess type
	 */
	public void setFirstGuessType(FirstGuessType firstGuessType)
	{
		this.firstGuessType=firstGuessType;
	}
	
	/**
	 * set the acceleration type
	 */
	public void setAccelerationType(AccelerationStrategy accelerationStrategy)
	{
		if (accelerationStrategy==AccelerationStrategy.VECTOR)
		{
			accelerator=new VectorAccelerator();
		}
		else if (accelerationStrategy==AccelerationStrategy.MULTIPLICATIVE_VECTOR)
		{
			accelerator=new MultiplicativeAccelerator();
		}
		else
		{
			accelerator=null;
		}
	}
	
	/**
	 * set up for non-circulant convolution model.  the "measurement window size" and the
	 * "psf window size" are needed to calculate the normalization factor
	 *  
	 * @param k - measurement window size
	 * @param l - psf window size
	 */
	public boolean setNonCirculantConvolutionStrategy(long[] k, long[] l)
	{
		this.k=k;
		this.l=l;
		
		this.convolutionStrategy=ConvolutionStrategy.NON_CIRCULANT;
		
		System.out.println("set noncirculant convolution window");
		
		return true;
	}
	
	/**
	 * Abstract function that needs to be implemented as to perform an iteration of 
	 * a type of deconvolution.  
	 * @param a
	 * @param b
	 * @return
	 */
	protected abstract boolean performIteration( final Img< ComplexFloatType > a, final Img< ComplexFloatType > b );

}
