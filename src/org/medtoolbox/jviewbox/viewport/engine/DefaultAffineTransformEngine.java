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
import java.awt.image.WritableRaster;

import org.medtoolbox.jviewbox.BufferedImageUtilities;
import org.medtoolbox.jviewbox.viewport.Viewport;

/**
 * A default implementation of <code>AffineTransformEngine</code>.
 *
 * @version January 8, 2004
 */
public class DefaultAffineTransformEngine implements AffineTransformEngine
{
    // --------------
    // Public methods
    // --------------

    /**
     * Applies an <code>AffineTransform</code> to a <code>WritableRaster</code>
     * and returns a transformed copy clipped to the specified size. The
     * transformed copy is in the same format as the source. The source
     * <code>WritableRaster</code> is never modified.
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
	// Create a new dest WritableRaster if no previous result is available
	// or output size has changed
	WritableRaster dest;
	if (previousResult == null ||
	    previousResult.getWidth() != outputSize.width ||
	    previousResult.getHeight() != outputSize.height) {

	    dest = source.createCompatibleWritableRaster(outputSize.width,
							 outputSize.height);
	}

	// Reuse the previous WritableRaster after clearing it
	else {
	    dest = previousResult;
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
    }
}
