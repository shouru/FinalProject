/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.engine;

import java.awt.image.BufferedImage;

/**
 * Interface for the source optimizing step in <code>ViewBoxViewport</code>'s
 * image rendering pipeline. An instance of <code>SourceOptimizeEngine</code>
 * may be shared by more than one <code>ViewBoxViewport</code>s.
 *
 * @see org.medtoolbox.jviewbox.viewport.ViewBoxViewport
 *
 * @version January 8, 2004
 */
public interface SourceOptimizeEngine
{
    /**
     * Converts an original <code>BufferedImage</code> into a possibly
     * different format which is optimal for rendering in a
     * <code>Viewport</code>. This conversion should be lossless. The original
     * <b>MUST NOT</b> be modified.
     * <p>
     * A previous result from this method cached by a
     * <code>ViewBoxViewport</code> is also passed as a parameter if available.
     * The implementation is encouraged to reuse this buffer for output when
     * possible to improve memory performance.
     *
     * @param original Original <code>BufferedImage</code> to convert.
     * @param previousResult Previous result from this method cached by a
     *			     <code>ViewBoxViewport</code> for buffer reuse;
     *			     <code>null</code> if not available.
     *
     * @return <code>BufferedImage</code> in a format optimal for rendering.
     *	       May be <code>original</code> if it is already optimal.
     */
    public BufferedImage convert(BufferedImage original,
				 BufferedImage previousResult);
}
