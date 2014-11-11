/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.engine;

import java.awt.image.BufferedImage;

/**
 * Null implementation of <code>SourceOptimizeEngine</code>.
 *
 * @version January 8, 2004
 */
public class NullSourceOptimizeEngine implements SourceOptimizeEngine
{
    // --------------
    // Public methods
    // --------------

    /**
     * Does nothing and simply returns the original 
     * <code>BufferedImage</code>.
     *
     * @param original Original <code>BufferedImage</code> to convert.
     * @param previousResult Previous result from this method cached by a
     *			     <code>ViewBoxViewport</code> for buffer reuse;
     *			     <code>null</code> if not available.
     *
     * @return <code>original</code>.
     */
    public BufferedImage convert(BufferedImage original,
				 BufferedImage previousResult)
    {
	return original;
    }
}
