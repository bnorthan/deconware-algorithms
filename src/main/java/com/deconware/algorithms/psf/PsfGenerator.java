package com.deconware.algorithms.psf;

import com.deconware.algorithms.StaticFunctions;

import net.imglib2.img.Img;
import net.imglib2.IterableInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.Cursor;

import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.real.FloatType;

import net.imglib2.meta.ImgPlus;

import com.deconware.algorithms.psf.FlipPsfQuadrants;

import com.deconware.wrappers.CosmPsf_swig;

/**
 * 
 * Generates a psf by calling COSM through swig wrapper
 * 
 * @author bnorthan
 *
 */
public class PsfGenerator 
{
	public static enum PsfType{WIDEFIELD, TWO_PHOTON, CONFOCAL_CIRCULAR, CONFOCAL_LINE};
	public static enum PsfModel{GIBSON_LANI, HAEBERLE};
	
	public static int PsfTypeToInt(PsfType psfType)
	{
		if (psfType==PsfType.WIDEFIELD)
		{
			return 0;
		}
		else if (psfType==PsfType.TWO_PHOTON)
		{
			return 1;
		}
		else if (psfType==PsfType.CONFOCAL_CIRCULAR)
		{
			return 2;
		}
		else if (psfType==PsfType.CONFOCAL_LINE)
		{
			return 3;
		}
		else 
		{
			return -1;
		}
	}
	
	public static int PsfModelToInt(PsfModel psfModel)
	{
		if (psfModel==PsfModel.GIBSON_LANI)
		{
			return 0;
		}
		else if (psfModel==PsfModel.HAEBERLE)
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}
										
	int[] size;
	int[] symsize;
	
	long[] lsymdim;
	long[] start;
	
	float[] spacing;
	double emissionWavelength;
	double numericalAperture;
	double designImmersionOilRefractiveIndex;
	double designSpecimenLayerRefractiveIndex;
	double actualImmersionOilRefractiveIndex;
	double actualSpecimenLayerRefractiveIndex;
	double actualPointSourceDepthInSpecimenLayer;

	float[] psfBuffer;
	
	PsfType psfType=PsfType.WIDEFIELD;
	PsfModel psfModel=PsfModel.GIBSON_LANI;
	
	/**
	 * default constructor just loads the COSM wrapper library without setting member variables
	 */
	public PsfGenerator()
	{
		LoadLib();	
	}
	
	/**
	 * load the COSM wrapper library and set variables
	 * 
	 * @param size
	 * @param spacing
	 * @param emissionWavelength
	 * @param numericalAperture
	 * @param designImmersionOilRefractiveIndex
	 * @param designSpecimenLayerRefractiveIndex
	 * @param actualImmersionOilRefractiveIndex
	 * @param actualSpecimimport net.imglib2.img.basictypeaccess.array.ByteArray;enLayerRefractiveIndex
	 * @param actualPointSourceDepthInSpecimenLayer
	 * @param psfType
	 * @param psfModel
	 */
	public PsfGenerator(int[] size,
			float[] spacing,
			double emissionWavelength,
			double numericalAperture,
			double designImmersionOilRefractiveIndex,
			double designSpecimenLayerRefractiveIndex,
			double actualImmersionOilRefractiveIndex,
			double actualSpecimenLayerRefractiveIndex,
			double actualPointSourceDepthInSpecimenLayer,
			PsfType psfType,
			PsfModel psfModel)
	{
		calculateSymSize(size);
		
		this.spacing=spacing;
		this.emissionWavelength=emissionWavelength;
		this.numericalAperture=numericalAperture;
		this.designImmersionOilRefractiveIndex=designImmersionOilRefractiveIndex;
		this.designSpecimenLayerRefractiveIndex=designSpecimenLayerRefractiveIndex;
		this.actualImmersionOilRefractiveIndex=actualImmersionOilRefractiveIndex;
		this.actualSpecimenLayerRefractiveIndex=actualSpecimenLayerRefractiveIndex;
		this.actualPointSourceDepthInSpecimenLayer=actualPointSourceDepthInSpecimenLayer;

		this.psfType=psfType;
		this.psfModel=psfModel;
		
		LoadLib();
	}
	
	/**
	 * loads the cosm swig wrapper library
	 */
	private void LoadLib()
	{
		// load the cosm swig wrapper library
		// for linux64 library should be placed in <ImageJ root>/lib/linux64
		// TODO: intelligent search for library on multiple systems (ie windows)
		
		System.loadLibrary("CosmPsfJavaSwig");
	}
	
	/**
	 * 
	 * Generate the PSF
	 * 	
	 * @param size
	 * @param spacing
	 * @param emissionWavelength
	 * @param numericalAperture
	 * @param designImmersionOilRefractiveIndex
	 * @param designSpecimenLayerRefractiveIndex
	 * @param actualImmersionOilRefractiveIndex
	 * @param actualSpecimenLayerRefractiveIndex
	 * @param actualPointSourceDepthInSpecimenLayer
	 * @param psfType
	 * @param psfModel
	 * @returnimport net.imglib2.img.basictypeaccess.array.ByteArray;
	 */
	public static Img<FloatType> CallGeneratePsf(int[] size,
												float[] spacing,
												double emissionWavelength,
												double numericalAperture,
												double designImmersionOilRefractiveIndex,
												double designSpecimenLayerRefractiveIndex,
												double actualImmersionOilRefractiveIndex,
												double actualSpecimenLayerRefractiveIndex,
												double actualPointSourceDepthInSpecimenLayer,
												PsfType psfType ,
												PsfModel psfModel)
	{
		// instantiate the psfGenerator
		PsfGenerator psfGenerator = new PsfGenerator();
		
		// create a temporary buffer to place the psf in
		int bufferSize = (int)(size[0]*size[1]*size[2]);
		
		float[] psfBuffer=new float[bufferSize];
		
		long[] lsize={size[0],size[1],size[2]};
		
		// call cosm psf using the swig wrapper
		long status = CosmPsf_swig.CosmPsf(psfBuffer, 
				new int[]{(int)lsize[0],(int)lsize[1],(int)lsize[2]}, 
				spacing, 
				emissionWavelength, 
				numericalAperture,
				designImmersionOilRefractiveIndex, 
				designSpecimenLayerRefractiveIndex, 
				actualImmersionOilRefractiveIndex, 
				actualSpecimenLayerRefractiveIndex, 
				actualPointSourceDepthInSpecimenLayer,
				PsfTypeToInt(psfType),
				PsfModelToInt(psfModel));	
		
		// if successful
		if (true)
		{
			return convertBufferToImage(psfBuffer, size);
		}
		else
		{
			return null;
		}

	}	
	
	/**
	 * 
	 * Generate the PSF
	 * 	
	 * @param size
	 * @param spacing
	 * @param emissionWavelength
	 * @param numericalAperture
	 * @param designImmersionOilRefractiveIndex
	 * @param designSpecimenLayerRefractiveIndex
	 * @param actualImmersionOilRefractiveIndex
	 * @param actualSpecimenLayerRefractiveIndex
	 * @param actualPointSourceDepthInSpecimenLayer
	 * @param psfType
	 * @param psfModel
	 * @returnimport net.imglib2.img.basictypeaccess.array.ByteArray;
	 */
	public static void CallGeneratePsf(IterableInterval out,
												int[] size,
												float[] spacing,
												double emissionWavelength,
												double numericalAperture,
												double designImmersionOilRefractiveIndex,
												double designSpecimenLayerRefractiveIndex,
												double actualImmersionOilRefractiveIndex,
												double actualSpecimenLayerRefractiveIndex,
												double actualPointSourceDepthInSpecimenLayer,
												PsfType psfType ,
												PsfModel psfModel)
	{
		// instantiate the psfGenerator
		PsfGenerator psfGenerator = new PsfGenerator();
		
		// create a temporary buffer to place the psf in
		int bufferSize = (int)(size[0]*size[1]*size[2]);
		
		// TODO: it would be nice to just send the memory from an image
		// to avoid this extra buffer and the copy.  But we have no guarentee
		// what kind of memory would be coming in (could be planar) so 
		// some checks would be needed
		float[] psfBuffer=new float[bufferSize];
		
		long[] lsize={size[0],size[1],size[2]};
		
		// call cosm psf using the swig wrapper
		long status = CosmPsf_swig.CosmPsf(psfBuffer, 
				new int[]{(int)lsize[0],(int)lsize[1],(int)lsize[2]}, 
				spacing, 
				emissionWavelength, 
				numericalAperture,
				designImmersionOilRefractiveIndex, 
				designSpecimenLayerRefractiveIndex, 
				actualImmersionOilRefractiveIndex, 
				actualSpecimenLayerRefractiveIndex, 
				actualPointSourceDepthInSpecimenLayer,
				PsfTypeToInt(psfType),
				PsfModelToInt(psfModel));	
		
		int i=0;
		
		// get a cursor so we can iterate through the image
		final Cursor<FloatType> cursor = out.cursor();
				
		// iterate through the image and copy from the psf buffer
		while (cursor.hasNext())
		{
			cursor.fwd();
						
			cursor.get().set(psfBuffer[i]);
						
			i++;
		}
	}	

	/**
	 * 
	 * Converts a float buffer to an Img
	 * 
	 * TODO put into a utility
	 * 
	 * @param buffer
	 * @param size
	 * @return
	 */
	static Img<FloatType> convertBufferToImage(float[] buffer, int[] size)
	{
		// create a planer image factory
		ImgFactory<FloatType> imgFactory = new PlanarImgFactory<FloatType>();
					
		// use the image factory to create an img
		Img<FloatType> image = imgFactory.create(size, new FloatType());
					
		// get a cursor so we can iterate through the image
		final Cursor<FloatType> cursor = image.cursor();
					
		int i=0;
					
		// iterate through the image and copy from the psf buffer
		while (cursor.hasNext())
		{
			cursor.fwd();
						
			cursor.get().set(buffer[i]);
						
			i++;
		}
					
		return image;
	}
	
	void calculateSymSize(int[] size)
	{

		this.size=size;
		
		this.symsize=new int[3];
		this.start=new long[3];
		
		// if x is larger then y
		if (size[0]>size[1])
		{
			this.symsize[0]=this.size[0];
			this.symsize[1]=this.size[0];
			this.symsize[2]=this.size[2];
			
			start[0]=0;
        	start[1]=(this.size[0]-this.size[1])/2;
        	start[2]=0;
	
		}
		// else if y is larger then x
		if (size[1]>size[0])
		{
			this.symsize[0]=this.size[1];
			this.symsize[1]=this.size[1];
			this.symsize[2]=this.size[2];
			
			start[0]=(this.size[1]-this.size[0])/2;
        	start[1]=0;
        	start[2]=0;
	
		}
		// else if they are the same size
		else
		{
			this.symsize[0]=this.size[0];
			this.symsize[1]=this.size[1];
			this.symsize[2]=this.size[2];
			
			start[0]=0;
        	start[1]=0;
        	start[2]=0;
		}
	}
	

}
