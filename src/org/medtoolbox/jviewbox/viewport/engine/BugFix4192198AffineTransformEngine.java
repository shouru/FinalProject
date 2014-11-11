/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.engine;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.medtoolbox.jviewbox.BufferedImageUtilities;
import org.medtoolbox.jviewbox.viewport.Viewport;

/**
 * <code>AffineTransformEngine</code> which incorporates the fix to Java bug
 * #4192198 affecting AffineTransformOp. This engine can <b>only</b> be used
 * together with <code>BugFix4192198SourceOptimizeEngine</code> on
 * grayscale images of short/ushort pixels.
 *
 * @version January 8, 2004
 */
public class BugFix4192198AffineTransformEngine
    extends DefaultAffineTransformEngine
{
    // ---------
    // Constants
    // ---------

    /** Margin (upper) of the DataBuffer size to reallocate. */
    private static final double REALLOCATE_MARGIN = 1.25;

    // --------------
    // Public methods
    // --------------

    /**
     * Applies an <code>AffineTransform</code> to a <code>WritableRaster</code>
     * and returns a transformed copy clipped to the specified size. This
     * method works <b>only</b> on single-band interleaved short/ushort
     * raster. The transformed copy is in the same format as the source.
     * The source <code>WritableRaster</code> is never modified.
     * <p>
     * Currently, either nearest-neighbor or bilinear interpolation can be
     * used for transformation. Bilinear interpolation will also be applied if
     * {@link Viewport#INTERPOLATION_BICUBIC} is requested. This is limited
     * by the capability of Java 2D's <code>AffineTransformOp</code>.
     *
     * @param source Source <code>WritableRaster</code> to transform.
     * @param sourceColorModel <code>ColorModel</code> of the source image.
     *			       This is ignored by this engine.
     * @param transform <code>AffineTransform</code> to apply to the source.
     * @param outputSize Size to which the output should be clipped.
     * @param previousResult Previous result from this method cached by a
     *			     <code>ViewBoxViewport</code> for buffer reuse;
     *			     <code>null</code> if not available.
     * @param toClearBuffer Whether to clear <code>previousResult</code> before
     *			    outputing to it.
     * @param mode Mode of interpolation suggested for the transformation
     *		   (only nearest-neighbor and bilinear are implemented);
     *	           <code>null</code> to use nearest-neighbor interpolation.
     *
     * @return Transformed copy of the source as a <code>WritableRaster</code>.
     *
     * @throws IllegalArgumentException if applied to a raster of type other
     *	       than single-band interleaved short/ushort raster.
     * @throws ImagingOpException if the transformation failed because of a
     *	       because of a data-processing error that might be caused by an
     *	       invalid image format, tile format, or image-processing
     *	       operation, or any other unsupported operation.
     */
    public WritableRaster transform(WritableRaster source,
				    ColorModel sourceColorModel,
				    AffineTransform transform,
				    Dimension outputSize,
				    WritableRaster previousResult,
				    boolean toClearBuffer,
				    Viewport.InterpolationMode mode)
    {
	// Check source raster format
	if (!BufferedImageUtilities.isSingleBandInterleavedOfShorts(source)) {
	    throw new IllegalArgumentException("source must be a single-band "+
					       "interleaved short/ushort " +
					       "raster.");
	}

	// *** WORK-AROUND TO JAVA BUG -- START
	// *** BUG ID #4192198 IN SUN'S BUG DATABASE

	// Calculate required minimum output DataBuffer size
	// The following formula is from Raster#createInterleavedRaster
	// size = maxBandOff + scanlineStride*(h-1) + pixelStride*(w-1) + 1
	// Here, maxBandOff = 0, scanlineStride = 2w, pixelStride = 1
	int dstDBMinSize = outputSize.width * (2 * outputSize.height - 1);

	// Reuse DataBuffer if possible
	DataBuffer srcDB = source.getDataBuffer();
	DataBuffer dstDB =
	    (previousResult == null) ? null : previousResult.getDataBuffer();
	// dstDB must be of the same type, large enough yet smaller than
	// REALLOCATE_MARGIN times the required minimum
	if (dstDB == null || toClearBuffer ||
	    dstDB.getDataType() != srcDB.getDataType() ||
	    dstDB.getSize() < dstDBMinSize ||
	    dstDB.getSize() > (int)(dstDBMinSize * REALLOCATE_MARGIN)) {


	    // Allocate new DataBuffer
	    switch (srcDB.getDataType()) {
	    case DataBuffer.TYPE_SHORT:
		dstDB = new DataBufferShort(dstDBMinSize);
		break;
	    case DataBuffer.TYPE_USHORT:
		dstDB = new DataBufferUShort(dstDBMinSize);
		break;
	    default:
		// Should never happens
		throw new IllegalArgumentException("original must be a " +
						   "single band image of " +
						   "short/ushort pixels.");
	    }
	}

	// Destination raster
	WritableRaster dest =
	    Raster.createInterleavedRaster(dstDB,
					   outputSize.width, outputSize.height,
					   outputSize.width, 1,
					   new int[] {0}, null);

	// Create a view to the source raster with 2x scanline stride
	// *** DOUBLE THE SCANLINE STRIDE OF THE SOURCE RASTER
	WritableRaster source2x =
	    Raster.createInterleavedRaster(source.getDataBuffer(),
					   source.getWidth(),
					   source.getHeight(),
					   source.getWidth() * 2, 1,
					   new int[] {0}, null);

	// Create a view to the destination raster with 2x scanline stride
	// *** DOUBLE THE SCANLINE STRIDE OF THE DESTINATION RASTER
	WritableRaster dest2x =
	    Raster.createInterleavedRaster(dest.getDataBuffer(),
					   dest.getWidth(),
					   dest.getHeight(),
					   dest.getWidth() * 2, 1,
					   new int[] {0}, null);

	// Perform affine transform on 2x rasters, which tricks Java 2D into
	// doing the right thing in 1x over the scanline stride bug
	AffineTransformOp op =
	    new AffineTransformOp(transform,
				  (mode == Viewport.INTERPOLATION_BILINEAR ||
				   mode == Viewport.INTERPOLATION_BICUBIC)
				  ? AffineTransformOp.TYPE_BILINEAR
				  : AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	op.filter(source2x, dest2x);

	return dest;

	// *** WORK-AROUND TO JAVA BUG -- END
    }
}
