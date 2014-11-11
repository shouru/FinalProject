/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.engine;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.medtoolbox.jviewbox.LookUpTable;

/**
 * Null implementation of <code>LookUpEngine</code>.
 *
 * @version January 8, 2004
 */
public class NullLookUpEngine implements LookUpEngine
{
    // --------------
    // Public methods
    // --------------

    /**
     * Ignores the specified <code>LookUpTable</code> and simply combines
     * the specified <code>WritableRaster</code> and <code>ColorModel</code>
     * into a <code>BufferedImage</code>.
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
     * @throws RasterFormatException if <code>srcRaster</code> is incompatible
     *	       with <code>srcColorModel</code>.
     * @throws IllegalArgumentException if <code>srcRaster</code> is
     *	       incompatible with <code>srcColorModel</code>.
     */
    public BufferedImage filter(ColorModel srcColorModel,
				WritableRaster srcRaster, LookUpTable lut,
				BufferedImage previousResult)
    {
	// Check if the previous result is applicable
	if (previousResult != null &&
	    previousResult.getColorModel() == srcColorModel &&
	    previousResult.getRaster() == srcRaster) {
	    return previousResult;
	}

	// Return a new BufferedImage
	else {
	    return new BufferedImage(srcColorModel, srcRaster,
				     srcColorModel.isAlphaPremultiplied(),
				     null);
	}
    }
}
