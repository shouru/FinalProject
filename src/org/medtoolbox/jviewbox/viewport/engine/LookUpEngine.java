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
 * Interface for the table look-up step in <code>ViewBoxViewport</code>'s
 * image rendering pipeline. An instance of <code>LookUpEngine</code> may be 
 * shared by more than one <code>ViewBoxViewport</code>s.
 *
 * @see org.medtoolbox.jviewbox.viewport.ViewBoxViewport
 *
 * @version January 8, 2004
 */
public interface LookUpEngine
{
    /**
     * Filters an image by a table look-up operation and returns a filtered
     * copy. The source <b>MUST NOT</b> be modified. The filtered copy is
     * wrapped into a <code>BufferedImage</code> which may have a different
     * <code>ColorModel</code> than the source's as determined by the engine.
     * <p>
     * A previous result from this method cached by a
     * <code>ViewBoxViewport</code> is also passed as a parameter if available.
     * The implementation is encouraged to reuse this buffer for output when
     * possible to improve memory performance.
     *
     * @param srcColorModel <code>ColorModel</code> of the source image.
     * @param srcRaster <code>WritableRaster</code> which contains the pixels
     *			of the source image.
     * @param lut <code>LookUpTable</code> to apply to the source image.
     * @param previousResult Previous result from this method cached by a
     *			     <code>ViewBoxViewport</code> for buffer reuse;
     *			     <code>null</code> if not available.
     *
     * @return Filtered copy of the source image as a
     *	       <code>BufferedImage</code>.
     */
    public BufferedImage filter(ColorModel srcColorModel,
				WritableRaster srcRaster, LookUpTable lut,
				BufferedImage previousResult);
}
