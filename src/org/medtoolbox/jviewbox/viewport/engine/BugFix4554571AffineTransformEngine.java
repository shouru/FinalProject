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
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.medtoolbox.jviewbox.BufferedImageUtilities;
import org.medtoolbox.jviewbox.viewport.Viewport;

/**
 * <code>AffineTransformEngine</code> which is used to fix the known Java bug
 * #4554571. This engine can <b>only</b> be used together with
 * <code>BugFix4554571LookUpEngine</code> on single-band interleaved
 * short/ushort rasters.
 *
 * @see BugFix4554571LookUpEngine
 *
 * @version January 8, 2004
 */
public class BugFix4554571AffineTransformEngine
    extends DefaultAffineTransformEngine
{
    // --------------
    // Public methods
    // --------------

    /**
     * Applies an <code>AffineTransform</code> to a <code>Raster</code> and
     * returns a transformed copy clipped to the specified size. This method
     * works <b>only</b> on single-band interleaved short/ushort raster. The
     * transformed copy is in the same format as the source, only with a
     * doubled scanline stride for bug fix. The source <code>Raster</code> is
     * never modified.
     * <p>
     * Currently, either nearest-neighbor or bilinear interpolation can be
     * used for transformation. Bilinear interpolation will also be applied if
     * {@link Viewport#INTERPOLATION_BICUBIC} is requested. This is limited
     * by the capability of Java 2D's <code>AffineTransformOp</code>.
     *
     * @param source Source <code>Raster</code> to transform.
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
	// *** BUG ID #4554571 IN SUN'S BUG DATABASE

	// Create destination raster if no previous result is available,
	// output size has changed, or previous result is not in the required
	// format (single-band-interleaved, pixel stride 1, 2x scanline stride)
	WritableRaster dest = previousResult;
	if (dest == null ||
	    dest.getWidth() != outputSize.width ||
	    dest.getHeight() != outputSize.height ||
	    !BufferedImageUtilities.isSingleBandInterleavedOfShorts(dest) ||
	    source.getSampleModel().getDataType() != dest.getSampleModel().getDataType() ||
	    ((PixelInterleavedSampleModel)dest.getSampleModel()).getPixelStride() != 1 &&
	    ((PixelInterleavedSampleModel)dest.getSampleModel()).getScanlineStride() != dest.getWidth() * 2) {


	    // Create a new dest WritableRaster
	    // *** DOUBLE THE SCANLINE STRIDE OF THE DESTINATION RASTER
	    int dataType = source.getSampleModel().getDataType();
	    dest = Raster.createInterleavedRaster(dataType,
						  outputSize.width,
						  outputSize.height,
						  outputSize.width * 2, 1,
						  new int[] {0}, null);
	}

	// Otherwise, reuse the destination raster
	else {
	    // Zero out dest if requested
	    if (toClearBuffer) {
		BufferedImageUtilities.clearRaster(dest);
	    }
	}

	// Perform affine transform
	AffineTransformOp op =
	    new AffineTransformOp(transform,
				  (mode == Viewport.INTERPOLATION_BILINEAR ||
				   mode == Viewport.INTERPOLATION_BICUBIC)
				  ? AffineTransformOp.TYPE_BILINEAR
				  : AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	op.filter(source, dest);

	return dest;

	// *** WORK-AROUND TO JAVA BUG -- END
    }
}
