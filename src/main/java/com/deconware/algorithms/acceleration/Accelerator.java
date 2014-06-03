package com.deconware.algorithms.acceleration;

import net.imglib2.type.numeric.RealType;
import net.imglib2.img.Img;

public interface Accelerator <T extends RealType<T>>
{
	public Img<T> Accelerate(Img<T> estimate);

}
