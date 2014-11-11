/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import org.medtoolbox.jviewbox.BufferedImageUtilities;
import org.medtoolbox.jviewbox.LookUpTable;
import org.medtoolbox.jviewbox.imagesource.ImageSource;
import org.medtoolbox.jviewbox.viewport.engine.AffineTransformEngine;
import org.medtoolbox.jviewbox.viewport.engine.BugFix4192198AffineTransformEngine;
import org.medtoolbox.jviewbox.viewport.engine.LookUpEngine;
import org.medtoolbox.jviewbox.viewport.engine.SourceOptimizeEngine;

/**
 * ImageSourceViewport with accelarated image rendering and table look-up
 * capability.
 * <p>
 * Image rendering in this class is divided into steps, each of which is
 * implemented by classes implementing the corresponding <i>Engine</i>
 * interface. See the description of {@link #_paintImage} for the breakdown
 * of the rendering pipeline.
 * <p>
 * Instances of Engines are assigned to a ViewBoxViewport at construction
 * time. An Engine class may choose to support only images of certain format(s)
 * for performance optimization or bug fixes. It is necessary to choose an
 * proper combination of Engines for a ViewBoxViewport to function properly.
 * <p>
 * <a name="residual"><i>Residuals in Multi-buffered Rendering Pipeline</i>
 * </a><p>
 * Image rendering in ViewBoxViewport is multi-buffered. If the affine
 * transform buffer is not cleared before rendering to it, residuals from the
 * previous transform may show up as a result, e.g., traces of image being
 * panned around or zoomed out. To handle the residuals, ViewBoxViewport
 * provides the following two options:
 * <ul>
 * <li><i>Clearing the buffer beforehand,</i> which can be enabled and
 *     disabled by {@link #setPreBufferClearingEnabled}. This eliminates
 *     any residual from the previous affine transformation, regardless of the
 *     exact nature of the transform (as opposed to the other option.) However,
 *     since table look-up is performed after affine transformation, this
 *     option does not prevent background pixels (i.e., regions in a Viewport
 *     not covered by the image) from picking up unwanted color values if the
 *     look-up table maps zero, the value of which the background pixels
 *     have before look-up operation, into non-zero. Also, there may be some
 *     performance hit if the buffer is to be cleared.
 * </li><br>
 * <li><i>Clipping the output from buffers to screen.</i> Instead of clearing
 *     up the buffer, we may clip the output from the internal buffer to
 *     screen to the exact image bounds after affine transformation. This not
 *     only prevents residuals from showing up but also keeps the backgrounds
 *     clear from the effect of look-up table. However, if the exact image
 *     bounds after affine transformation are not a rectangle parallel to both
 *     the X and Y axes, e.g., in the rare case of a rotation not of mutiples
 *     of 90 degrees, clipping to the exact bounds can be a very expensive
 *     operation. For performance reason, ViewBoxViewport by default clips
 *     only to the bounding rectangle of the actual image bounds. Clipping to
 *     the exact image bounds may be enabled and disabled by
 *     {@link #setExactClippingEnabled}.
 * </li></ul>
 * For performance reason, ViewBoxViewport by default does not clear the buffer
 * and clips only to the bounding rectangle of the actual image bounds. It is
 * recommended to leave it like that unless you do encounter visual residual
 * problem.
 *
 * @see #_paintImage
 * @see org.medtoolbox.jviewbox.viewport.engine.SourceOptimizeEngine
 * @see org.medtoolbox.jviewbox.viewport.engine.AffineTransformEngine
 * @see org.medtoolbox.jviewbox.viewport.engine.LookUpEngine
 * @see #setPreBufferClearingEnabled
 * @see #setExactClippingEnabled
 *
 * @version January 8, 2004
 */
public class ViewBoxViewport extends ImageSourceViewport
{
    // ----------------
    // Protected fields
    // ----------------

    /** Source optimize engine. */
    protected final SourceOptimizeEngine _sourceOptimizeEngine;

    /** Affine transform engine. */
    protected final AffineTransformEngine _affineTransformEngine;

    /** Look-up engine. */
    protected final LookUpEngine _lookUpEngine;

    // --------------
    // Private fields
    // --------------

    /** Whether to clear affine transform buffer before image rendering. */
    private static boolean _preBufferClearing = false;

    /** Whether to clip image drawing exactly. */
    private static boolean _exactClipping = false;

    /**
     * Whether to automatically flush ImageSource after source optimization.
     */
    private static boolean _autoSourceFlushing = false;

    /** Cache of optimized source image. */
    private BufferedImage _cachedOptimizedSource;

    /** Cache of affine transformed raster. */
    private WritableRaster _cachedTransformedRaster;


    /** Previous Viewport size. */
    private Dimension _previousSize;

    /** Previously used AffineTransform for image. */
    private AffineTransform _previousImageTransform;

    /** Initial LUT. */
    private LookUpTable _initialLUT;

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs a Viewport for the specified <code>ImageSource</code>.
     *
     * @param imageSource <code>ImageSource</code> of the image to display.
     * @param sourceOptimizeEngine <code>SourceOptimizeEngine</code> to use.
     * @param affineTransformEngine <code>AffineTransformEngine</code> to use.
     * @param lookUpEngine <code>LookUpEngine</code> to use.
     *
     * @throws NullPointerException if <code>imageSource</code> is
     *	       <code>null</code>.
     *
     * @see org.medtoolbox.jviewbox.viewport.engine.SourceOptimizeEngine
     * @see org.medtoolbox.jviewbox.viewport.engine.AffineTransformEngine
     * @see org.medtoolbox.jviewbox.viewport.engine.LookUpEngine
     */
    public ViewBoxViewport(ImageSource imageSource,
			   SourceOptimizeEngine sourceOptimizeEngine,
			   AffineTransformEngine affineTransformEngine,
			   LookUpEngine lookUpEngine)
    {
	this(imageSource, sourceOptimizeEngine, affineTransformEngine,
	     lookUpEngine, null);
    }

    /**
     * Constructs a Viewport for the specified <code>ImageSource</code>.
     *
     * @param imageSource <code>ImageSource</code> of the image to display.
     * @param sourceOptimizeEngine <code>SourceOptimizeEngine</code> to use.
     * @param affineTransformEngine <code>AffineTransformEngine</code> to use.
     * @param lookUpEngine <code>LookUpEngine</code> to use.
     * @param initialLUT Default look-up table to use initially.
     *
     * @throws NullPointerException if <code>imageSource</code> is
     *	       <code>null</code>.
     *
     * @see org.medtoolbox.jviewbox.viewport.engine.SourceOptimizeEngine
     * @see org.medtoolbox.jviewbox.viewport.engine.AffineTransformEngine
     * @see org.medtoolbox.jviewbox.viewport.engine.LookUpEngine
     * @see org.medtoolbox.jviewbox.LookUpTable
     */
    public ViewBoxViewport(ImageSource imageSource,
			   SourceOptimizeEngine sourceOptimizeEngine,
			   AffineTransformEngine affineTransformEngine,
			   LookUpEngine lookUpEngine,
			   LookUpTable initialLUT)
    {
	super(imageSource);

	if (sourceOptimizeEngine == null) {
	    throw new NullPointerException("sourceOptimizeEngine can not be " +
					   "null.");
	}
	_sourceOptimizeEngine = sourceOptimizeEngine;

	if (affineTransformEngine == null) {
	    throw new NullPointerException("affineTransformEngine can not be "+
					   "null.");
	}
	_affineTransformEngine = affineTransformEngine;

	if (lookUpEngine == null) {
	    throw new NullPointerException("lookUpEngine can not be null.");
	}
	_lookUpEngine = lookUpEngine;

	_initialLUT = initialLUT;

    }

    // --------------
    // Public methods
    // --------------

    /**
     * Moves and resizes this Viewport. The current aspect ratio of the
     * displayed image will be maintained by adjusting scaling factors
     * correspondingly.
     * <p>
     * For the fix to bug #4192198 to work, it is necessary to round the
     * size down to the closest even numbers. Otherwise the bug fix may crash
     * the JVM. This method is overriden to do exactly so.
     *
     * @param x New X coordinate of the upper-left corner of this Viewport.
     * @param y New Y coordinate of the upper-left corner of this Viewport.
     * @param width New width of this Viewport.
     * @param height New height of this Viewport.
     *
     * @throws IllegalArgumentException If the boundary width and height are
     *                                  invalid.
     */
    public void setBounds(int x, int y, int width, int height)
    {
	// With Bug Fix 4192198, round width and height to even
	if (_affineTransformEngine instanceof
	    BugFix4192198AffineTransformEngine) {

	    width &= 0xFFFFFFFE;
	    height &= 0xFFFFFFFE;
	}

	super.setBounds(x, y, width, height);
    }

    /**
     * Flushes all the resources being used by the Viewport. This includes
     * any cached data for rendering to the screen and any system resources
     * that are being used to store the image pixel data.
     */
    public synchronized void flush()
    {
	super.flush();
	if (_cachedOptimizedSource != null) {
	    _cachedOptimizedSource.flush();
	    _cachedOptimizedSource = null;
	}
	_cachedTransformedRaster = null;
    }

    /**
     * Flushes only the resources being used by the Viewport to display the
     * image. This includes any cached data for rendering to the screen but
     * does not include any system resources that are being used to store the
     * image pixel data.
     */
    public synchronized void flushDisplayOnly()
    {
	super.flushDisplayOnly();
	if (_cachedOptimizedSource != null) {
	    _cachedOptimizedSource.flush();
	    _cachedOptimizedSource = null;
	}
	_cachedTransformedRaster = null;
    }

    /**
     * Sets whether to clear the affine transform buffer before rendering.
     * This setting applies to <b>ALL</b> <code>ViewBoxViewport</code>s. The
     * default is not. See <a href="#residual">Residuals in the Multi-buffered
     * Rendering Pipeline</a> for a detailed description of this option.
     *
     * @param preBufferClearing <code>true</code> to clear the affine
     *			        transform buffer before rendering;
     *				<code>false</code> to not to.
     *
     * @see <a href="#residual">Residuals in the Multi-buffered Rendering
     *	    Pipeline</a>
     * @see #setExactClippingEnabled
     */
    public static void setPreBufferClearingEnabled(boolean preBufferClearing)
    {
	_preBufferClearing = preBufferClearing;
    }

    /**
     * Returns whether to clear the affine transform buffer before rendering.
     * This setting applies to <b>ALL</b> <code>ViewBoxViewport</code>s.
     *
     * @return <code>true</code> if affine transform buffers are to be cleared
     *	       before rendering; <code>false</code> if not.
     *
     * @see #setPreBufferClearingEnabled
     */
    public static boolean isPreBufferClearingEnabled()
    {
	return _preBufferClearing;
    }

    /**
     * Sets whether to clip the output of the internal buffer to the exact
     * image bounds after affine transformation (versus clip to the bounding
     * rectangle of the exact bounds). This setting applies to <b>ALL</b>
     * <code>ViewBoxViewport</code>s. The default is <code>false</code>, i.e.,
     * to clip to the bounding rectangle of the exact bounds. See
     * <a href="#residual">Residuals in the Multi-buffered Rendering
     * Pipeline</a> for a detailed description of this option.
     *
     * @param exactClipping <code>true</code> to clip to the exact image bounds
     *			    after affine transformation; <code>false</code> to
     *			    clip to the exact bounds' bounding rectangle.
     *
     * @see <a href="#residual">Residuals in the Multi-buffered Rendering
     *	    Pipeline</a>
     * @see #setPreBufferClearingEnabled
     */
    public static void setExactClippingEnabled(boolean exactClipping)
    {
	_exactClipping = exactClipping;
    }

    /**
     * Returns whether to clip the output of internal image buffer to the exact
     * image bounds after affine transformation.
     *
     * @return <code>true</code> if the output of internal image buffer is
     *	       clipped to the exact image bounds after affine transformation;
     *	       <code>false</code> if clipped to the exact bounds' bounding
     *	       rectangle.
     *
     * @see #setExactClippingEnabled
     */
    public static boolean isExactClippingEnabled()
    {
	return _exactClipping;
    }

    /**
     * Sets whether to automatically flush the source pixel data cached by
     * <code>ImageSourceViewport</code> after source optimization is done.
     * The default is <code>false</code>. This setting applies to <b>ALL</b>
     * <code>ViewBoxViewport</code>s.
     * <p>
     * The result of source optimization is cached and the process do not need
     * to repeat until the cache is explicitly flushed by one of the two
     * <code>flush*</code> methods. Therefore, turning on this option may
     * improve memory performance at the penalty of longer reloading time
     * after <code>flush*</code>.
     *
     * @param autoSourceFlushing <code>true</code> to automatically flush the
     *				 source pixel data cached by
     *				 <code>ImageSourceViewport</code> after source
     *				 optimization is done; <code>false</code> to
     *				 not to.
     *
     * @see #flush
     * @see #flushDisplayOnly
     * @see #isAutoSourceFlushingEnabled
     *
     * @since 2.0b
     */
    public static void setAutoSourceFlushingEnabled(boolean autoSourceFlushing)
    {
	_autoSourceFlushing = autoSourceFlushing;
    }

    /**
     * Returns whether the source pixel data cached by
     * <code>ImageSourceViewport</code> is automatically flushed
     * after source optimization is done. This setting applies to <b>ALL</b>
     * <code>ViewBoxViewport</code>s.
     *
     * @return <code>true</code> if the source pixel data cached by
     *	       <code>ImageSourceViewport</code> is automatically flushed
     *	       after source optimization is done;
     *	       <code>false</code> if not.
     *
     * @see #setAutoSourceFlushingEnabled
     *
     * @since 2.0b
     */
    public static boolean isAutoSourceFlushingEnabled()
    {
	return _autoSourceFlushing;
    }

    // -----------------
    // Protected methods
    // -----------------

    // Paint methods

    /**
     * Paints the image in this Viewport. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.)
     * <p>
     * The image rendering process is divided into three steps in this class:
     * <ol>
     * <li>The original <code>BufferedImage</code> is passed through the
     *     <code>SourceOptimizeEngine</code> to be converted into a possibly
     *	   different format optimal for rendering if necessary.</li>
     * <li>The optimized <code>BufferedImage</code> is then passed through the
     *     <code>AffineTransformEngine</code> for panning, zoomming, rotation,
     *	   and other affine transformation effect.</li>
     * <li>The transformed image is then passed through the
     *	   <code>LookUpEngine</code> for table look-up operation if supported.
     *	   The final result is displayed.</li>
     * </ol><p>
     * If look-up operation is supported but no look-up table has been
     * assigned, the default look-up table given at the construction time will
     * appply. If no default LUT is given at the construction time either, 
     * {@link BufferedImageUtilities#createLUTFromProperties} is called to
     * create one.
     * <p>
     * Current implementation of <code>AffineTransformEngine</code> supports
     * only nearest-neighbor and bilinear interpolation, and bilinear is used
     * instead when bicubic is requested. However, unlike in {@link Viewport}
     * where interpolation can only be suggested by the use of
     * <code>RenderingHints</code>, current implementation of
     * <code>AffineTransformEngine</code> can guarantee the application of
     * bilinear interpolation.
     * 
     * @param g2d Graphics context to paint in.
     *
     * @see org.medtoolbox.jviewbox.viewport.engine.SourceOptimizeEngine
     * @see org.medtoolbox.jviewbox.viewport.engine.AffineTransformEngine
     * @see org.medtoolbox.jviewbox.viewport.engine.LookUpEngine
     */
    protected synchronized void _paintImage(Graphics2D g2d)
    {
	boolean pipelineIsDirty = false;

	// 1st stage of the pipeline: source optimization
	// Executed only when cache is not available
	if (_cachedOptimizedSource == null) {

	    // Mark pipeline dirty
	    pipelineIsDirty = true;

	    // Apply source optimization
	    BufferedImage source = getBufferedImage();
	    _cachedOptimizedSource =
		_sourceOptimizeEngine.convert(source, _cachedOptimizedSource);

	    // Initialize LUT if not yet done so
	    if (getLut() == null) {
		// Check the source BufferedImage for LUT, which takes
		// precendence over _initialLUT
		///*
		LookUpTable lut =
		    BufferedImageUtilities.createLUTFromProperties(source);
		if (lut == null) {
		    lut = _initialLUT;
		}
		//*/

		if (lut != null) {
		    setLut(lut);
		}
	    }

	    // Auto-flush source only if requested and optimization does occur
	    if (isAutoSourceFlushingEnabled() &&
		(source != _cachedOptimizedSource)) {
		if (source != null) {
		    source.flush();
		    source = null;
		}
		super.flush();
	    }
	}

	// 2nd stage of the pipeline: affine transformation
	// Executed only if it's the first time, if pipeline is dirty,
	// if the transform has changed, or if the Viewport's size has changed

	// Get Image Transform and Viewport size
	AffineTransform it = getImageTransform();
	Dimension vpSize = getSize();

	if (pipelineIsDirty || _cachedTransformedRaster == null ||
	    !vpSize.equals(_previousSize) ||
	    !it.equals(_previousImageTransform)) {
	    
	    // Mark pipeline dirty
	    pipelineIsDirty = true;

	    // Apply affine transform
	    WritableRaster wr = _cachedOptimizedSource.getRaster();
	    ColorModel cm = _cachedOptimizedSource.getColorModel();
	    _cachedTransformedRaster =
		_affineTransformEngine.transform(wr, cm, it, vpSize,
						 _cachedTransformedRaster,
						 isPreBufferClearingEnabled(),
						 getInterpolationMode());

	    // Keep copies of previous inputs
	    _previousSize = (Dimension)vpSize.clone();
	    _previousImageTransform = (AffineTransform)it.clone();
	}

	// 3rd stage of the pipeline: table look-up
	// Always executed
	BufferedImage lookupResult =
	    _lookUpEngine.filter(_cachedOptimizedSource.getColorModel(),
				 _cachedTransformedRaster, getLut(), null);

	// Set up g2d's clip before drawing the image
	Shape origClip = g2d.getClip();
	it.preConcatenate(getViewportTransform());
	Rectangle imageRect = new Rectangle(_cachedOptimizedSource.getWidth(),
					   _cachedOptimizedSource.getHeight());
	Shape imageClip = it.createTransformedShape(imageRect);
	if (isExactClippingEnabled()) {
	    g2d.clip(imageClip);
	}
	else {
	    Rectangle imageBounds = imageClip.getBounds();
	    g2d.clipRect(imageBounds.x, imageBounds.y,
			 imageBounds.width, imageBounds.height);
	}

	// Paint the image
	// *** WORK-AROUND TO JAVA BUG -- START
	// *** BUG ID #4364215 IN SUN'S BUG DATABASE
	// *** DRAW A LINE OUTSIDE THE CLIP BOUNDS
	Rectangle viewportBounds = getBounds();
	g2d.drawLine(viewportBounds.x + viewportBounds.width,
		     viewportBounds.y + viewportBounds.height,
		     viewportBounds.x + viewportBounds.width,
		     viewportBounds.y + viewportBounds.height);
	// *** WORK-AROUND TO JAVA BUG -- END
	g2d.drawImage(lookupResult, viewportBounds.x, viewportBounds.y, null);

	// Restore g2d's clip
	g2d.setClip(origClip);
    }
}
