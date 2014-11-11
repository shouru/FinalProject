/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.engine;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.LookupOp;
import java.awt.image.WritableRaster;

import org.medtoolbox.jviewbox.BufferedImageUtilities;
import org.medtoolbox.jviewbox.LookUpTable;

/**
 * <code>LookUpEngine</code> which filters a grayscale image into a grayscale
 * image of byte pixels with a <code>LookUpTable</code> for display.
 *
 * @version January 8, 2004
 */
public class GrayLookUpEngine implements LookUpEngine
{
    // --------------
    // Private fields
    // --------------

    /**
     * Whether the filtering result, 256-grayscale BufferedImage, should be of
     * TYPE_BYTE_INDEXED (vs. TYPE_BYTE_GRAY).
     */
    private static boolean _usingTypeByteIndexed = true;

    /**
     * <code>IndexColorModel</code> with a uniformly linear 256-grayscale color
     * map.
     */
    private static final IndexColorModel _LINEAR_GRAYSCALE;

    // Static initializer for the linear grayscale index color model
    static {
	// Fill the color table with uniform linear values
	byte[] table = new byte[256];
	for (int i = 0; i < table.length; i++) {
	    table[i] = (byte)i;
	}

	// Create the grayscale color model
	_LINEAR_GRAYSCALE = new IndexColorModel(8, 256, table, table, table);
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Sets whether the output of look-up table filtering, i.e., 256-grayscale
     * <code>BufferedImage</code>s, should be of <code>TYPE_BYTE_INDEXED</code>
     * (vs. <code>TYPE_BYTE_GRAY</code>.) This setting applies to <b>ALL</b>
     * GrayLookUpEngines. The default is <code>true</code>, i.e., to output
     * <code>TYPE_BYTE_INDEXED</code> <code>BufferedImage</code>s.
     *
     * @param usingTypeByteIndexed <code>true</code> to make the output
     *				   <code>TYPE_BYTE_INDEXED</code> images;
     *				   <code>false</code> to make the output
     *				   <code>TYPE_BYTE_GRAY</code> images.
     *
     * @see #isUsingTypeByteIndexed
     */
    public static void setUsingTypeByteIndexed(boolean usingTypeByteIndexed)
    {
	_usingTypeByteIndexed = usingTypeByteIndexed;
    }

    /**
     * Returns whether the output of LUT filtering, i.e., 256-grayscale
     * <code>BufferedImage</code>s, are of <code>TYPE_BYTE_INDEXED</code>
     * instead of the default <code>TYPE_BYTE_GRAY</code>.
     *
     * @return <code>true</code> if the output of <code>filter</code> is of
     *	       <code>TYPE_BYTE_INDEXED</code>; <code>false</code> if of
     *	       <code>TYPE_BYTE_GRAY</code>.
     *
     * @see #setUsingTypeByteIndexed
     */
    public static boolean isUsingTypeByteIndexed()
    {
	return _usingTypeByteIndexed;
    }

    /**
     * Filters an image by a table look-up operation and returns a filtered
     * copy. The source raster must be a single band integer-type raster.
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
	if (!BufferedImageUtilities.isSingleBandInterleaved(srcRaster)) {
	    throw new IllegalArgumentException("srcRaster must be a single-" +
					       "band interleaved raster.");
	}
	if (lut == null) {
	    // Return the original as is
	    return new BufferedImage(srcColorModel, srcRaster, false, null);
	}
	if (lut.getNumComponents() != 1) {
	    throw new IllegalArgumentException("lut must have exactly one " +
					       "band.");
	}

	// Create a new destination image if no previous result is available,
	// or the output size has changed
	BufferedImage dest = previousResult;
	if (dest == null || dest.getType() != getOutputType() ||
	    dest.getWidth() != srcRaster.getWidth() ||
	    dest.getHeight() != srcRaster.getHeight()) {

	    dest = _createDestination(srcRaster.getWidth(),
				      srcRaster.getHeight());
	}

	// Apply LUT
	ByteLookupTable blut = lut.getByteLookupTable();
	LookupOp lo = new LookupOp(blut, null);
	lo.filter(srcRaster, dest.getRaster());

	return dest;
    }

    // -----------------------
    // Package private methods
    // -----------------------

    /**
     * Returns the type of the output <code>BufferedImage</code>s according
     * to the settings given by <code>setUsingTypeByteIndexed</code>.
     *
     * @return <code>BufferedImage.TYPE_BYTE_GRAY</code> or
     *	       <code>BufferedImage.TYPE_BYTE_INDEXED</code>.
     */
    static int getOutputType()
    {
	return isUsingTypeByteIndexed() ? BufferedImage.TYPE_BYTE_INDEXED
					: BufferedImage.TYPE_BYTE_GRAY;
    }

    /**
     * Creates a destination BufferedImage of appropriate type and the
     * specified size.
     */
    static BufferedImage _createDestination(int width, int height)
    {
	// Byte indexed
	if (isUsingTypeByteIndexed()) {
	    return new BufferedImage(width, height,
				     BufferedImage.TYPE_BYTE_INDEXED,
				     _LINEAR_GRAYSCALE);
	}

	// Byte gray
	else {
	    return new BufferedImage(width, height,
				     BufferedImage.TYPE_BYTE_GRAY);
	}
    }
}
