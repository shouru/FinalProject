/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.engine;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.medtoolbox.jviewbox.viewport.Viewport;

/**
 * Interface for the affine transform step in <code>ViewBoxViewport</code>'s
 * image rendering pipeline. An instance of <code>AffineTransformEngine</code>
 * may be shared by more than one <code>ViewBoxViewport</code>s.
 *
 * @see org.medtoolbox.jviewbox.viewport.ViewBoxViewport
 *
 * @version January 8, 2004
 */
public interface AffineTransformEngine
{
    /**
     * Applies an <code>AffineTransform</code> to a <code>WritableRaster</code>
     * and returns a transformed copy clipped to the specified size. The
     * transformed copy <b>MUST</b> be in either the same format or a different
     * format compatible with the source's <code>ColorModel</code>. The source
     * <code>WritableRaster</code> <b>MUST NOT</b> be modified.
     * <p>
     * A previous result from this method cached by a
     * <code>ViewBoxViewport</code> is also passed as a parameter if available.
     * The implementation is encouraged to reuse this buffer for output when
     * possible to improve memory performance.
     * <p>
     * As of jViewBox 2.0b, a new parameter of
     * {@link org.medtoolbox.jviewbox.viewport.Viewport.InterpolationMode} is
     * added to this method. Currently, this parameter is only meant for
     * suggesting a preferred mode of interpolation. Whether or not certain
     * interpolation modes are supported is entirely up to the implementation.
     *
     * @param source Source <code>WritableRaster</code> to transform.
     * @param sourceColorModel <code>ColorModel</code> of the source image.
     *			       This is for the engine's reference only. When
     *			       sample interpolation is performed, how samples
     *			       are interpreted into colors should be taken
     *			       into consideration.  For non-interpolating
     *			       engines, this information may be ignored.
     * @param transform <code>AffineTransform</code> to apply to the source.
     * @param outputSize Size to which the output should be clipped.
     * @param previousResult Previous result from this method cached by a
     *			     <code>ViewBoxViewport</code> for buffer reuse;
     *			     <code>null</code> if not available.
     * @param toClearBuffer Whether to clear <code>previousResult</code> before
     *			    outputing to it.
     * @param mode Mode of interpolation suggested for the transformation;
     *	           <code>null</code> to use nearest-neighbor interpolation.
     *
     * @return Transformed copy of the source as a <code>WritableRaster</code>.
     *	       <code>source</code> may be returned if the transform is
     *	       identity.
     *
     * @throws ImagingOpException if the transformation failed due to a
     *	       data-processing error that might be caused by an invalid image
     *	       format, tile format, or image-processing operation, or any
     *	       other unsupported operation.
     */
    public WritableRaster transform(WritableRaster source,
				    ColorModel sourceColorModel,
				    AffineTransform transform,
				    Dimension outputSize,
				    WritableRaster previousResult,
				    boolean toClearBuffer,
				    Viewport.InterpolationMode mode);
}
