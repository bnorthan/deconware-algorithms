package com.deconware.algorithms.roi;


import java.util.Arrays;

import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.RandomAccess;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Util;

import net.imglib2.RandomAccessibleInterval;

/**
 * ROIAlgorithm implements a framework against which to build operations of one image against
 * another, like convolution, cross-correlation, or morphological operations.  It operates by
 * creating a {@link RegionOfInterestCursor} from the input image, and an output image, which is
 * assumed to be of the same size as the input.  The process() method curses over the output image,
 * and at each step calls the abstract method patchOperation(), giving it the current location in 
 * the output and input images, as well as the RegionOfInterestCursor, which will curse over a 
 * patch of a given size in the input image.  patchOperation() is responsible for setting the
 * value of the pixel at the given position in the output image.
 * 
 * @param <T>
 * @param <S>
 *
 * @author Larry Lindsey
 */
public abstract class ROIAlgorithm <T extends NumericType<T>, S extends NumericType<S>>
	implements OutputAlgorithm<Img<S>>, Benchmark
{

	private final RandomAccessibleInterval<T> inputImage;
	private Img<S> outputImage;
	private S type;
	private StructuringElementCursor<T> strelCursor;
	private final ImgFactory<S> imageFactory;		
	private String errorMsg;
	private String name;
	private long pTime;

	protected ROIAlgorithm(final ImgFactory<S> imFactory,
			final S type,
            final RandomAccessibleInterval<T> inputImage,
            final long[] patchSize)
	{
		this(imFactory, 
				type, 
				inputImage,
				new StructuringElementCursor<T>(
	            inputImage,
	            patchSize));
	}
	
	protected ROIAlgorithm(final ImgFactory<S> imFactory,
			final S type,
			RandomAccessibleInterval<T> inputImage,
	        final StructuringElementCursor<T> strelCursor)
	{		
		int nd = inputImage.numDimensions();
		
		final int[] initPos = new int[nd];
		
		pTime = 0;
		this.inputImage = inputImage;
		
		outputImage = null;
		imageFactory = imFactory;
		errorMsg = "";
		name = null;
		Arrays.fill(initPos, 0);
		
		this.strelCursor = strelCursor;
		
		this.type=type;
		
	}

	
	protected abstract boolean patchOperation(
			final StructuringElementCursor<T> cursor,
			final T inputType,
			final S outputType);


    protected StructuringElementCursor<T> getStrelCursor()
    {
        return strelCursor;
    }

	/**
	 * Set the name given to the output image.
	 * @param inName the name to give to the output image.
	 */
	public void setName(final String inName)
	{
		name = inName;
	}
		
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns the {@link Image} that will eventually become the result of this
	 * {@link OutputAlgorithm}, and creates it if it has not yet been created.
	 * @return the {@link Image} that will eventually become the result of this
	 * {@link OutputAlgorithm}.
	 */
	protected Img<S> getOutputImage()
	{		
		if (outputImage == null)
		{
			long[] imDim = new long[inputImage.numDimensions()];
			for (int i=0;i<inputImage.numDimensions();i++) {
				imDim[i]=inputImage.dimension(i);
			}
			outputImage = imageFactory.create(imDim, type.createVariable());
		}
		return outputImage;
	}
	
	@Override
	public Img<S> getResult()
	{		
		return outputImage;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMsg;
	}
	
	protected void setErrorMessage(String message)
	{
		errorMsg = message;
	}
	
	@Override
	public boolean process()    
	{
		final RandomAccess<T> inputCursor = 
				inputImage.randomAccess();
		
		final RandomAccess<S> outputCursor =
		    getOutputImage().randomAccess();
		
		final long sTime = System.currentTimeMillis();
		strelCursor.patchReset();
		
		try
		{
		
		while (strelCursor.patchHasNext())
		{
		
		    strelCursor.patchFwd();
		    outputCursor.setPosition(strelCursor.getPatchCenterCursor());
		    inputCursor.setPosition(strelCursor.getPatchCenterCursor());
		    
		    if (!patchOperation(strelCursor, inputCursor.get(), outputCursor.get()))
			{
				strelCursor.patchReset();
				return false;
			}
		}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
		    System.out.println("Found error, strel at : " + Util.printCoordinates(strelCursor));
		    System.out.println("Whereas outputcursor at : " + Util.printCoordinates(outputCursor));
		    throw e;
		}
		
		pTime = System.currentTimeMillis() - sTime;
		return true;		
	}
	
	@Override
	public boolean checkInput()
	{
		return true;
	}

	@Override
	public long getProcessingTime()
	{
		return pTime;
	}

}
