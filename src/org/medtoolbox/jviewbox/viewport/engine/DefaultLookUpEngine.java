/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.engine;

import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorModel;
import java.awt.image.LookupOp;
import java.awt.image.WritableRaster;

import org.medtoolbox.jviewbox.LookUpTable;

/**
 * A default implementation of <code>LookUpEngine</code>.
 *
 * @version January 8, 2004
 */
public class DefaultLookUpEngine implements LookUpEngine
{
    // --------------
    // Public methods
    // --------------

    /**
     * Filters an image by a table look-up operation and returns a filtered
     * copy. The secified <code>LookUpTable</code> must have as many bands
     * as the source image raster or exactly one, which is applied to all
     * bands in that case.
     * <p>
     * A <code>WritableRaster</code> of the same size and format as the source
     * is created as the destination of filtering. The filtered raster is then
     * paired with the specified source image color model to create a
     * <code>BufferedImage</code> as return value.
     *
     * @param srcColorModel <code>ColorModel</code> for the source image.
     * @param srcRaster <code>WritableRaster</code> which contains the pixels
     *			of the source image.
     * @param lut <code>LookUpTable</code> to apply to the source image.
     * @param previousResult Previous result from this method cached by a
     *			     <code>ViewBoxViewport</code> for buffer reuse;
     *			     <code>null</code> if not available.
     *
     * @return Filtered copy of the source image as a
     *	       <code>BufferedImage</code>.
     *
     * @throws IllegalArgumentException if <code>lut</code> does not have
     *	       as many bands as required, or if <code>srcRaster</code> is
     *	       incompatible with <code>srcColorModel</code>.
     * @throws RasterFormatException if <code>srcRaster</code> is incompatible
     *	       with <code>srcColorModel</code>.
     */
    public BufferedImage filter(ColorModel srcColorModel,
				WritableRaster srcRaster, LookUpTable lut,
				BufferedImage previousResult)
    {
	if (!srcColorModel.isCompatibleRaster(srcRaster)) {
	    throw new IllegalArgumentException("srcRaster must be compatible "+
					       "with srcColorModel.");
	}
	if (lut == null) {
	    // Return the original as is
	    return new BufferedImage(srcColorModel, srcRaster,
				     srcColorModel.isAlphaPremultiplied(),
				     null);
	}

	// Reuse previous raster unless it's not available, not of the same
	// size as the source, or not compatible with source ColorModel
	WritableRaster dstRaster =
	    (previousResult == null) ? null : previousResult.getRaster();
	if (dstRaster == null ||
	    dstRaster.getWidth() != srcRaster.getWidth() ||
	    dstRaster.getHeight() != srcRaster.getHeight() ||
	    !srcColorModel.isCompatibleRaster(dstRaster)) {

	    // Create a new destination raster
	    dstRaster = srcRaster.createCompatibleWritableRaster();
	}

	// Apply LUT
	ByteLookupTable blut = lut.getByteLookupTable();
	LookupOp lo = new LookupOp(blut, null);
	lo.filter(srcRaster, dstRaster);

	return new BufferedImage(srcColorModel, dstRaster,
				 srcColorModel.isAlphaPremultiplied(), null);
    }
}
