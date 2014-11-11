/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.engine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import org.medtoolbox.jviewbox.BufferedImageUtilities;

/**
 * <code>SourceOptimizeEngine</code> which copies pixels of the original
 * <code>BufferedImage</code> into a raster of twice as large data buffer,
 * for the fix to Java Bug #4192198 to work. This engine can <b>only</b> be
 * used together with <code>BugFix4192198AffineTransformEngine</code>
 * on grayscale images of short/ushort pixels.
 *
 * @version January 8, 2004
 */
public class BugFix4192198SourceOptimizeEngine implements SourceOptimizeEngine
{
    // ---------
    // Constants
    // ---------

    /** Margin (upper) of the DataBuffer size to reallocate. */
    private static final double REALLOCATE_MARGIN = 1.1;

    // --------------
    // Public methods
    // --------------

    /**
     * Converts an original grayscale short/ushort <code>BufferedImage</code>
     * into a slightly different format such that the fix to Java Bug #4192198
     * can work on. This conversion should be lossless. The original is never
     * modified.
     * <p>
     * Specifically, this conversion simply copies the pixel data into a
     * data buffer twice as large as needed in order for the bug fix to apply
     * a scanline stride doubling trick.
     *
     * @param original Original <code>BufferedImage</code> to convert.
     * @param previousResult Previous result from this method cached by a
     *			     <code>ViewBoxViewport</code> for buffer reuse;
     *			     <code>null</code> if not available.
     *
     * @return <code>BufferedImage</code> of a format which is optimal for
     *	       rendering.
     */
    public BufferedImage convert(BufferedImage original,
				 BufferedImage previousResult)
    {
	if (!BufferedImageUtilities.isSingleBandInterleavedOfShorts(original))
	{
	    throw new IllegalArgumentException("original must be a single " +
					       "band image of short/ushort " +
					       "pixels.");
	}

	// *** WORK-AROUND TO JAVA BUG -- START
	// *** BUG ID #4192198 IN SUN'S BUG DATABASE

	// Calculate required minimum DataBuffer size
	// The following formula is from Raster#createInterleavedRaster
	// size = maxBandOff + scanlineStride*(h-1) + pixelStride*(w-1) + 1
	// Here, maxBandOff = 0, scanlineStride = 2w, pixelStride = 1
	int width = original.getWidth();
	int height = original.getHeight();
	int dstDBMinSize = width * (2 * height - 1);

	// Reuse DataBuffer if possible
	DataBuffer srcDB = original.getRaster().getDataBuffer();
	DataBuffer dstDB = (previousResult == null) ? null
	    : previousResult.getRaster().getDataBuffer();
	// dstDB must be of the same type, large enough yet smaller than
	// REALLOCATE_MARGIN times the required minimum
	if (dstDB == null ||
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
	WritableRaster dstRaster =
	    Raster.createInterleavedRaster(dstDB, width, height, width, 1,
					   new int[] {0}, null);

	// Copy the pixels
	// The best case: pixels are stored continuously
	SampleModel origSM = original.getSampleModel();
	if (origSM instanceof PixelInterleavedSampleModel &&
	    ((PixelInterleavedSampleModel)origSM).getPixelStride() == 1 &&
	    ((PixelInterleavedSampleModel)origSM).getScanlineStride() ==
	    width) {


	    // Do direct array copy
	    short[] srcArray, dstArray;
	    if (srcDB instanceof DataBufferShort) {
		srcArray = ((DataBufferShort)srcDB).getData();
		dstArray = ((DataBufferShort)dstDB).getData();
	    }
	    else if (srcDB instanceof DataBufferUShort) {
		srcArray = ((DataBufferUShort)srcDB).getData();
		dstArray = ((DataBufferUShort)dstDB).getData();
	    }
	    else {
		throw new IllegalArgumentException("original must be a " +
						   "single band image of " +
						   "short/ushort pixel.");
	    }
	    System.arraycopy(srcArray, srcDB.getOffset(),
			     dstArray, dstDB.getOffset(),
			     width * height);
	}

	// Other cases
	else {

	    // Copy through get/setSamples
	    // This is going to use quite a bit of memory
	    int[] samples = new int[width * height];
	    original.getRaster().getSamples(0, 0, width, height, 0,
					    samples);
	    dstRaster.setSamples(0, 0, width, height, 0, samples);
	    samples = null;
	}

	// Pair the new raster with the original color model
	return new BufferedImage(original.getColorModel(), dstRaster,
				 original.isAlphaPremultiplied(), null);
	// *** WORK-AROUND TO JAVA BUG -- END
    }
}
