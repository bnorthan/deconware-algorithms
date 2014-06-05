package com.deconware.algorithms.fft.filters;

import com.deconware.algorithms.StaticFunctions;
import com.deconware.algorithms.fft.SimpleFFT;
import com.deconware.algorithms.fft.SimpleFFTFactory;
import com.deconware.algorithms.fft.filters.IterativeFilter.ConvolutionStrategy;
import com.deconware.algorithms.psf.FlipPsfQuadrants;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.view.Views;

/**
 * Base class for Fourier based filters
 * based on Stephan Preibisch's imglib2 Convolution code.
 * 
 * @author bnorthan
 * 
**/
public abstract class AbstractFrequencyFilter<T extends RealType<T>, S extends RealType<S>> 
	implements  FrequencyFilter<T, S>
{
	final int numDimensions;
	
	protected RandomAccessibleInterval<T> image;
	
	protected RandomAccessibleInterval<S> kernel;
	
	boolean flipKernel=true;
	
	protected Img<T> output;
	
	Img<T> truth=null;
	
	Img<ComplexFloatType> kernelFFT, imgFFT;
	
	SimpleFFT<T, ComplexFloatType> fftInput;
	
	final ImgFactory<ComplexFloatType> fftImgFactory;
	final protected ImgFactory<T> imgFactory;
	final ImgFactory<S> kernelImgFactory;
	
	final int[] kernelDim;
	
	String errorMessage="";
	int numThreads;
	long processingTime;
	
	boolean preConditionPsf=true;
	
	int windowSize[];
	
	ConvolutionStrategy convolutionStrategy=ConvolutionStrategy.CIRCULANT;
	
	// size of PSF space
	long[] l;
		
	// size of measured image space
	long[] k;
	
	public AbstractFrequencyFilter(final RandomAccessibleInterval<T> image, 
			final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory,
			final ImgFactory<S> kernelImgFactory,
			final ImgFactory<ComplexFloatType> fftImgFactory)
	{
		this.numDimensions = image.numDimensions();
		
		this.image = image;
		
		this.kernel = kernel;
		
		this.fftImgFactory = fftImgFactory;
		this.imgFactory = imgFactory;
		this.kernelImgFactory = kernelImgFactory;
		
		this.kernelDim = new int[ numDimensions ];
		
		for (int d=0;d<numDimensions; ++d)
		{
			kernelDim[d]=(int)kernel.dimension(d);
		}
		
		this.kernelFFT=null;
		
		this.imgFFT=null;
		
		setNumThreads();
		
	}
	
	public AbstractFrequencyFilter( final Img<T> image, final Img<S> kernel, final ImgFactory<ComplexFloatType> fftImgFactory )
	{
		this( image, kernel, image.factory(), kernel.factory(), fftImgFactory );
	}
	
	public AbstractFrequencyFilter(final RandomAccessibleInterval<T> image, 
			final RandomAccessibleInterval<S> kernel,
			final ImgFactory<T> imgFactory,
			final ImgFactory<S> kernelImgFactory) throws IncompatibleTypeException
	{
		this(image, kernel, imgFactory, kernelImgFactory, kernelImgFactory.imgFactory(new ComplexFloatType()));
	}
	
	public AbstractFrequencyFilter( final Img<T> image, final Img<S> kernel ) throws IncompatibleTypeException
	{
		this( image, kernel, image.factory(), kernel.factory(), image.factory().imgFactory( new ComplexFloatType() ) );
	}
	
	protected boolean performInputFFT()
	{	
		if (imgFFT == null)
		{
			fftInput = SimpleFFTFactory.GetSimpleFFt(image, imgFactory, fftImgFactory, new ComplexFloatType());
			
			imgFFT=fftInput.forward(image);
		}
		
		return true;
	}
	
	/**
	 * Perform PSF fft.  Flip PSF Quadrants if needed.
	 * @return
	 */
	protected boolean performPsfFFT()
	{
		double sumbeforenorm=StaticFunctions.sum2(kernel);
		System.out.println("sumbeforenorm: "+sumbeforenorm);
		
		// TODO: remove normalization step.  This should be done at a higher level
		
		StaticFunctions.norm(Views.iterable(kernel));
		double sum=StaticFunctions.sum2(kernel);
		double sum2=StaticFunctions.sum2(image);
		System.out.println("sum: "+sum+" sum2 "+sum2);
		
		final int kernelTemplateDim[] = new int[ numDimensions ];
		
		// set the kernel fft dimensions to be the same size as the image fft dimensions
		for ( int d = 0; d < numDimensions; ++d )
			kernelTemplateDim[ d ] = (int)imgFFT.dimension( d );

		// calculate the size of the first dimension keeping in mind it will be a real to complex transform
		kernelTemplateDim[ 0 ] = ( (int)imgFFT.dimension( 0 ) - 1 ) * 2;
		
		SimpleFFT<S, ComplexFloatType> psfFFT;
			
		if (flipKernel)
		{
			// Flip PSF Quadrants to place the center at 0, 0, 0
			Img<S> flippedKernel = FlipPsfQuadrants.flip(kernel, kernelImgFactory, kernelTemplateDim);
			
			psfFFT = SimpleFFTFactory.GetSimpleFFt(flippedKernel, kernelImgFactory, fftImgFactory, new ComplexFloatType() );
			kernelFFT = psfFFT.forward(flippedKernel);
		}
		else
		{
			psfFFT = SimpleFFTFactory.GetSimpleFFt(kernel, kernelImgFactory, fftImgFactory, new ComplexFloatType() );
			kernelFFT = psfFFT.forward(kernel);
		}
		
		return true;
	}
		
	@Override
	public long getProcessingTime() 
	{ 
		return processingTime; 
	}
	
	@Override
	public void setNumThreads() 
	{ 
		this.numThreads = Runtime.getRuntime().availableProcessors(); 
	}
	
	@Override
	public void setNumThreads( final int numThreads ) 
	{ 
		this.numThreads = numThreads; 
	}

	@Override
	public int getNumThreads() 
	{ 
		return numThreads; 
	}	
	
	@Override
	public Img<T> getResult() 
	{ 
		return output; 
	}

	@Override
	public boolean checkInput() 
	{
		return false;
	}
	
	@Override
	public String getErrorMessage()  
	{ 
		return errorMessage; 
	}
	
	public void setPreconditionPsf(boolean preConditionPsf)
	{
		this.preConditionPsf = preConditionPsf; 
	}
	
	public boolean getPreconditionPsf()
	{
		return (this.preConditionPsf);
	}
	
	public void setTruth(Img<T> truth)
	{
		this.truth=truth;
	}
	
	public void setKernel(RandomAccessibleInterval<S> kernel)
	{
		this.kernel = kernel;
		
		// we have a new kernel so compute the kernel FFT
		this.performPsfFFT();
	}
	
	public void setFlipKernel(boolean flipKernel)
	{
		this.flipKernel=flipKernel;
	}
	
	/**
	 * @param  . 
	 */
	public void setSemiNonCirculantConvolutionStrategy(long[] k)
	{
		this.convolutionStrategy=ConvolutionStrategy.SEMI_NONCIRCULANT;
		this.k=k;
	}
}
