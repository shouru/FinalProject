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
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.medtoolbox.jviewbox.BufferedImageUtilities;
import org.medtoolbox.jviewbox.LookUpTable;

/**
 * <code>GrayLookUpEngine</code> which is used to fix the known Java bug
 * #4554571. This engine can <b>only</b> be used together with
 * <code>BugFix4554571AffineTransformEngine</code> on single-band
 * interleaved short/ushort rasters.
 *
 * @see BugFix4554571AffineTransformEngine
 *
 * @version January 8, 2004
 */
public class BugFix4554571LookUpEngine extends GrayLookUpEngine
{
    // --------------
    // Public methods
    // --------------

    /**
     * Filters an image by a table look-up operation and returns a filtered
     * copy. The source raster must be a single band integer-type raster
     * returned by <code>BugFix4554571AffineTransformEngine</code>.
     * The result is a grayscale image of 8-bit precision (byte pixels).
     *
     * @param srcColorModel <code>ColorModel</code> for the source image.
     *			    This is ignored by this engine for the output
     *			    is always <code>TYPE_BYTE_GRAY</code>.
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
     *	       exactly one band, or if <code>srcRaster</code> is not
     *	       a compatible single band integer-type raster.
     * @throws RasterFormatException if <code>srcRaster</code> is incompatible
     *	       with <code>srcColorModel</code>.
     */
    public BufferedImage filter(ColorModel srcColorModel,
				WritableRaster srcRaster, LookUpTable lut,
				BufferedImage previousResult)
    {
	if (!BufferedImageUtilities.isSingleBandInterleavedOfShorts(srcRaster))
	{
	    throw new IllegalArgumentException("srcRaster must be a single-" +
					       "band interleaved short/" +
					       "ushort raster.");
	}
	if (lut == null) {
	    // Return the original as is
	    return new BufferedImage(srcColorModel, srcRaster, false, null);
	}
	if (lut.getNumComponents() != 1) {
	    throw new IllegalArgumentException("lut must have exactly one " +
					       "band.");
	}

	// *** WORK-AROUND TO JAVA BUG -- START
	// *** BUG ID #4554571 IN SUN'S BUG DATABASE

	// Create a new destination image if no previous result is available,
	// or the output size has changed
	BufferedImage dest = previousResult;
	if (dest == null || dest.getType() != getOutputType() ||
	    dest.getWidth() != srcRaster.getWidth() ||
	    dest.getHeight() != srcRaster.getHeight()) {

	    dest = _createDestination(srcRaster.getWidth(),
				      srcRaster.getHeight());
	}

	// Create a view to srcRaster with normal (1x) scanline stride
	// *** HALF THE SCANLINE STRIDE OF THE SOURCE RASTER
	WritableRaster srcRaster1x =
	    Raster.createInterleavedRaster(srcRaster.getDataBuffer(),
					   srcRaster.getWidth(),
					   srcRaster.getHeight(),
					   srcRaster.getWidth(), 1,
					   new int[] {0}, null);

	// Apply LUT
	ByteLookupTable blut = lut.getByteLookupTable();
	LookupOp lo = new LookupOp(blut, null);
	lo.filter(srcRaster1x, dest.getRaster());

	return dest;

	// *** WORK-AROUND TO JAVA BUG -- END
    }
}
