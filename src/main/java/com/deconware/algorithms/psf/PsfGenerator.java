package com.deconware.algorithms.psf;

import com.deconware.algorithms.StaticFunctions;

import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.Cursor;

import net.imglib2.type.numeric.real.FloatType;

import net.imglib2.meta.ImgPlus;

import com.deconware.algorithms.psf.FlipPsfQuadrants;

import com.deconware.wrappers.CosmPsf_swig;

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
	public native boolean GeneratePsf(float[] psfBuffer);
	
	public native boolean GeneratePsf(float[] psfBuffer,
										long[] size,
										float[] spacing,
										double emissionWavelength,
										double numericalAperture,
										double designImmersionOilRefractiveIndex,
										double designSpecimenLayerRefractiveIndex,
										double actualImmersionOilRefractiveIndex,
										double actualSpecimenLayerRefractiveIndex,
										double actualPointSourceDespthInSpecimenLayer,
										int type,
										int model);
										
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
	
	// default constructor just loads the COSM wrapper library without setting member variables
	public PsfGenerator()
	{
		LoadLib();	
	}
	
	// load the COSM wrapper library and set variables 
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
	
	private void LoadLib()
	{
		// load the cosm wrapper library
		// (library should be placed in <ImageJ root>/lib/linux64
		
		System.loadLibrary("CosmPsfWrapper");
		System.loadLibrary("CosmPsfJavaSwig");
	}
	
		
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
		
		// generate the psf
		/*boolean success = psfGenerator.GeneratePsf(psfBuffer, 
													lsize, 
													spacing, 
													emissionWavelength, 
													numericalAperture,
													designImmersionOilRefractiveIndex, 
													designSpecimenLayerRefractiveIndex, 
													actualImmersionOilRefractiveIndex, 
													actualSpecimenLayerRefractiveIndex, 
													actualPointSourceDespthInSpecimenLayer,
													PsfTypeToInt(psfType),
													PsfModelToInt(psfModel));*/
		
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
	
	public Img<FloatType> CallGeneratePsf(double ri)
	{
		
		// create a temporary buffer to place the psf in
		int bufferSize = (int)(symsize[0]*symsize[1]*symsize[2]);

		if (psfBuffer==null)
		{
			psfBuffer=new float[bufferSize];
		}

		System.out.println("generating psf...");

		long[] lsize={size[0],size[1],size[2]};
		long[] lsymsize={symsize[0], symsize[1], symsize[2]};
		
		System.out.println("size: "+size[0]+" "+size[1]+" "+size[2]);
		System.out.println("symsize: "+symsize[0]+" "+symsize[1]+" "+symsize[2]);
		System.out.println("space: "+spacing[0]+" "+spacing[1]+" "+spacing[2]);
		System.out.println("emissionWavelength: "+emissionWavelength);
		System.out.println("numericalAperture: "+numericalAperture);
		System.out.println("designImmersionOilRefractiveIndex: "+designImmersionOilRefractiveIndex);
		System.out.println("designSpecimenLayerRefractiveIndex: "+designSpecimenLayerRefractiveIndex);
		System.out.println("actualImmersionOilRefractiveIndex: "+actualImmersionOilRefractiveIndex);
		System.out.println("ri: "+ri);
		System.out.println("actualPointSourceDepthInSpecimenLayer: "+actualPointSourceDepthInSpecimenLayer);
		
		// generate the psf
	/*	boolean success = GeneratePsf(psfBuffer, 
				lsymsize, 
				spacing, 
				emissionWavelength, 
				numericalAperture,
				designImmersionOilRefractiveIndex, 
				designSpecimenLayerRefractiveIndex, 
				actualImmersionOilRefractiveIndex, 
				ri, 
				actualPointSourceDepthInSpecimenLayer,
				0,
				0);*/
		
		long status = CosmPsf_swig.CosmPsf(psfBuffer, 
				symsize, 
				spacing, 
				emissionWavelength, 
				numericalAperture,
				designImmersionOilRefractiveIndex, 
				designSpecimenLayerRefractiveIndex, 
				actualImmersionOilRefractiveIndex, 
				ri, 
				actualPointSourceDepthInSpecimenLayer,
				PsfTypeToInt(psfType),
				PsfModelToInt(psfModel));

		// if successful
		if (true)
		{		
			Img<FloatType> psf = convertBufferToImage(psfBuffer, symsize);
			
			psf = cropSymmetricPsf(psf);
			
	     	return psf;
		}
		else
		{
			System.out.println("Native psf generation error.");
			return null;
		}
	}	
	
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
	
	Img<FloatType> cropSymmetricPsf(Img<FloatType> psfSym)
	{
		Img<FloatType> flipped=FlipPsfQuadrants.flip(psfSym, psfSym.factory(), symsize);
		
		 // crop the psf
        Img<FloatType> cropped=StaticFunctions.crop(flipped, start, new long[]{size[0], size[1], size[2]});
        
		ImgPlus<FloatType> psfPlus=StaticFunctions.Wrap3DImg(cropped, "psf");
		
		flipped =FlipPsfQuadrants.flip(cropped, cropped.factory(), size);
		
		return flipped;
	}
}
