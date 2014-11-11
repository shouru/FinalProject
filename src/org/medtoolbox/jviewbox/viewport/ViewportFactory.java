/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.medtoolbox.jviewbox.BufferedImageUtilities;
import org.medtoolbox.jviewbox.BugDetector;
import org.medtoolbox.jviewbox.ImageRenderingHints;
import org.medtoolbox.jviewbox.LinearLookUpTable;
import org.medtoolbox.jviewbox.LookUpTable;
import org.medtoolbox.jviewbox.imagesource.ImageSource;
import org.medtoolbox.jviewbox.viewport.engine.AffineTransformEngine;
import org.medtoolbox.jviewbox.viewport.engine.BugFix4192198AffineTransformEngine;
import org.medtoolbox.jviewbox.viewport.engine.BugFix4192198SourceOptimizeEngine;
import org.medtoolbox.jviewbox.viewport.engine.BugFix4554571AffineTransformEngine;
import org.medtoolbox.jviewbox.viewport.engine.BugFix4554571LookUpEngine;
import org.medtoolbox.jviewbox.viewport.engine.DefaultAffineTransformEngine;
import org.medtoolbox.jviewbox.viewport.engine.GrayLookUpEngine;
import org.medtoolbox.jviewbox.viewport.engine.LookUpEngine;
import org.medtoolbox.jviewbox.viewport.engine.NullLookUpEngine;
import org.medtoolbox.jviewbox.viewport.engine.NullSourceOptimizeEngine;
import org.medtoolbox.jviewbox.viewport.engine.RGBSourceOptimizeEngine;
import org.medtoolbox.jviewbox.viewport.engine.SourceOptimizeEngine;

/**
 * Class which contains factory methods for creating {@link Viewport}s.
 *
 * @version January 8, 2004
 */
public class ViewportFactory
{
    // --------------
    // Private fields
    // --------------

    /** Optimal RGB types for RGBSourceOptimizeEngines. */
    private static int[] _optimalRGBTypes;

    // -----------
    // Constructor
    // -----------

    /** Non-instantiable class. */
    private ViewportFactory()
    {
	throw new UnsupportedOperationException("Non-instantiable class.");
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Creates an appropriate Viewport for the specified image object.
     * <p>
     * Currently, this method returns the generic <code>Viewport</code> for
     * any <code>java.awt.Image</code> instance, and invokes
     * {@link #createViewport(ImageSource)} for <code>ImageSource</code>s.
     *
     * @param image A <code>java.awt.Image</code> or <code>ImageSource</code>
     *		    instance to create Viewport for.
     *
     * @return Viewport created for <code>image</code>; <code>null</code>
     *	       if <code>image</code> is not recognized as an acceptable image
     *	       object (<code>java.awt.Image</code> or
     *	       <code>ImageSource</code>.)
     */
    public static Viewport createViewport(Object image)
    {
	Viewport vp = null;

	// An ImageSource?
	if (image instanceof ImageSource) {
	    vp = createViewport((ImageSource)image);
	}

	// A Java image?
	else if (image instanceof Image) {
	    vp = new Viewport((Image)image);
	}

	return vp;
    }

    /**
     * Creates Viewports for a <code>List</code> of image objects. This method
     * simply calls <code>createViewport</code> for each object in the list.
     * <p>
     * If <code>createViewport</code> fails to create a <code>Viewport</code>
     * for an object in the image list and returns <code>null</code>, that
     * <code>null</code> will be <b>excluded</b> from the Viewport list.
     * Hence it is possible to get a <code>Viewport[]</code> with fewer
     * elements than the list of images passed in.
     *
     * @param images <code>List</code> of image objects.
     *
     * @return Viewports created for the image objects in the list.
     */
    public static Viewport[] createViewports(List images)
    {
	Vector viewports = new Vector(images.size());
	for (Iterator it = images.iterator(); it.hasNext(); ) {
	    Viewport vp = createViewport(it.next());
	    if (vp != null) {
		viewports.add(vp);
	    }
	}

	return (Viewport[])viewports.toArray(new Viewport[viewports.size()]);
    }

    /**
     * Creates an appropriate <code>ImageSourceViewport</code> for the
     * specified <code>ImageSource</code>. This method tries to create
     * <code>ViewBoxViewport</code> with accelerated rendering and table
     * look-up capability when the image is in a format among those that are
     * supported by <code>ViewBoxViewport</code>.
     * <p>
     * Currently, the following rules are used to identify supported image
     * formats and to create corresponding ViewBoxViewports:
     * <ul>
     * <li>Source is an RGB <code>BufferedImage</code> that passes the test
     *	   of <code>BufferedImageUtilities.isRGB</code>:<br>
     *     a ViewBoxViewport that optimizes the RGB source but does not
     *	   support look-up operation.</li>
     * <li>Source is of <code>BYTE_GRAY</code> or <code>USHORT_GRAY
     *     BufferedImage</code> types:<br>
     *	   a ViewBoxViewport that supports look-up operation and resolves
     *	   some known grayscale rendering bugs in Java 2D.</li>
     * <li>Source is of <code>CUSTOM BufferedImage</code> type but passes
     *	   the test of <code>BufferedImageUtilities.isGrayscale</code>:<br>
     *	   a ViewBoxViewport that supports look-up operation and resolves
     *	   some known grayscale rendering bugs in Java 2D.</li>
     * <li>Source is of <code>BYTE_INDEXED</code> or <code>CUSTOM
     *	   BufferedImage</code> type but its <code>ImageRenderingHints</code>
     *     suggests a color space type of grayscale and it has a single-band
     *     PixelInterleavedSampleModel:<br>
     *	   a ViewBoxViewport that supports look-up operation and resolves
     *	   some known grayscale rendering bugs in Java 2D.</li>
     * </ul><p>
     * For any other image formats, a generic <code>ImageSourceViewport</code>
     * will be created with no optimization and no look-up capability.
     *
     * @param source <code>ImageSource</code> to create a Viewport for.
     *
     * @return <code>ImageSourceViewport</code> created for
     *	       <code>source</code>.
     *
     * @see #createEnginesForViewBoxViewport
     */
    public static ImageSourceViewport createViewport(ImageSource source)
    {
	// Try to create a ViewBoxViewport
	Object[] engines = createEnginesForViewBoxViewport(source);
	if (engines != null) {
	    return new ViewBoxViewport(source,
				       (SourceOptimizeEngine)engines[0],
				       (AffineTransformEngine)engines[1],
				       (LookUpEngine)engines[2],
				       (LookUpTable)engines[3]);
	}

	// Otherwise, return an ImageSourceViewport
	else {
	    return new ImageSourceViewport(source);
	}
    }

    /**
     * Identifies and creates a set of <code>SourceOptimizeEngine</code>,
     * <code>AffineTransformEngine</code>, <code>LookUpEngine</code>, and
     * initial <code>LookUpTable</code> that may be used to create a
     * <code>ViewBoxViewport</code> for the specified <code>ImageSource</code>.
     * <p>
     * This method is used by <code>createViewport(ImageSource)</code> to
     * create engines and initial look-up tables. See the description of
     * {@link #createViewport(ImageSource)} for supported image formats.
     *
     * @return Array of 4 objects: <code>SourceOptimizeEngine</code>,
     *	       <code>AffineTransformEngine</code>, <code>LookUpEngine</code>,
     *	       and <code>LookUpTable</code> instances in the respective order
     *	       (the fourth element, i.e., <code>LookUpTable</code>, may be
     *	       <code>null</code> if unavailable);
     *	       <code>null</code> if the image format is not supported by the
     *	       current implementation.
     *
     * @see ViewBoxViewport
     * @see org.medtoolbox.jviewbox.viewport.engine.SourceOptimizeEngine
     * @see org.medtoolbox.jviewbox.viewport.engine.AffineTransformEngine
     * @see org.medtoolbox.jviewbox.viewport.engine.LookUpEngine
     * @see org.medtoolbox.jviewbox.LookUpTable
     *
     * @since 2.0b
     */
    public static Object[] createEnginesForViewBoxViewport(ImageSource source)
    {
	// Retrieve all relevant format and rendering information
	ColorModel cm;
	SampleModel sm;
	ImageRenderingHints hints = null;
	try {
	    cm = source.getColorModel();
	    sm = source.getSampleModel();
	}
	catch (IOException e) {
	    // Unable to get format information
	    return null;
	}
	try {
	    hints = source.getImageRenderingHints();
	}
	catch (IOException e) {
	    // Ignore this exception
	}

	// Create ViewBoxViewport for recognized image type
	int type = BufferedImageUtilities.getBufferedImageType(cm, sm);
	Integer csType = null;
	if (hints != null) {
	    csType = hints.getColorSpaceType();
	}

	// Any RGB type
	//if (BufferedImageUtilities.isRGBType(type)) {
	if (BufferedImageUtilities.isRGB(cm, sm)) {
	    Object[] engines = _createEnginesForRGBViewport();
	    return new Object[] { engines[0], engines[1], engines[2], null };
	}

	// BYTE_GRAY
	else if (type == BufferedImage.TYPE_BYTE_GRAY) {
	    Object[] engines = _createEnginesForGrayViewport();
	    return new Object[] { engines[0], engines[1], engines[2],
				  _makeGrayscaleLUT(cm, hints) };
	}

	// USHORT_GRAY
	else if (type == BufferedImage.TYPE_USHORT_GRAY) {
	    Object[] engines = _createEnginesForShortGrayViewport();
	    return new Object[] { engines[0], engines[1], engines[2],
				  _makeGrayscaleLUT(cm, hints) };
	}

	// General component grayscale
	else if (BufferedImageUtilities.isGrayscale(cm, sm)) {
	    Object[] engines =
	        BufferedImageUtilities.isGrayscaleOfShorts(cm, sm) ?
		    _createEnginesForShortGrayViewport() :
		    _createEnginesForGrayViewport();
	    return new Object[] { engines[0], engines[1], engines[2],
				  _makeGrayscaleLUT(cm, hints) };
	}

	// Grayscale as indicated by rendering hints (Color Space Type)
	// cm and sm need to be verified as compatible
	else if (csType != null &&
		 csType.intValue() == ColorSpace.TYPE_GRAY &&
		 cm.isCompatibleSampleModel(sm) &&
		 BufferedImageUtilities.isSingleBandInterleaved(sm)) {
	    Object[] engines =
	        BufferedImageUtilities.isSingleBandInterleavedOfShorts(sm) ?
	            _createEnginesForShortGrayViewport() :
		    _createEnginesForGrayViewport();
	    return new Object[] { engines[0], engines[1], engines[2],
				  _makeGrayscaleLUT(cm, hints) };
	}

	// Anything else
	return null;
    }

    /**
     * Synonym of <code>setOptimalRGBTypes(new int[] {optimalRGBType})</code>.
     *
     * @deprecated Replaced by {@link #setOptimalRGBTypes(int[])} as of
     *		   jViewBox 2.0b.
     */
    public static void setOptimalRGBType(int optimalRGBType)
    {
	setOptimalRGBTypes(new int[] { optimalRGBType });
    }

    /**
     * Returns the <code>BufferedImage</code> type used as the optimal type
     * when creating <code>RGBSourceOptimizeEngine</code>s.
     *
     * @return The first element in the optimal RGB types array if previously
     *	       set by <code>setOptimalRGBTypes</code>;
     *	       <code>-1</code> if the optimal types array has not be set or
     *	       is set to <code>null</code>.
     *
     * @deprecated Replaced by {@link #getOptimalRGBTypes()} as of jViewBox
     *		   2.0b.
     */
    public static int getOptimalRGBType()
    {
	return _optimalRGBTypes != null ? _optimalRGBTypes[0] : -1;
    }

    /**
     * Sets the <code>BufferedImage</code> type<b>s</b> to use as the optimal
     * type when creating <code>RGBSourceOptimizeEngine</code>s used in
     * <code>ViewBoxViewport</code>s of RGB images. If none has been set or
     * a <code>null</code> is set, none will be passed to the constructor of
     * <code>RGBSourceOptimizeEngine</code> (i.e., the default constructor),
     * which in turns uses the types given by
     * <code>BufferedImageUtilities.getPreferredRGBTypes</code> as the optimal
     * types.
     *
     * @param optimalRGBTypes Array of optimal RGB <code>BufferedImage</code>
     *			      type<b>s</b> to use when creating
     *			      <code>RGBSourceOptimizeEngine</code>s;
     *			      <code>null</code> to invoke the default (no
     *			      parameter) constructor to create
     *			      <code>RGBSourceOptimizeEngine</code>s.
     *
     * @throws IllegalArgumentException if <code>optimalRGBTypes</code> is
     *	       empty or any element in it is not an RGB type of no less than
     *	       24 bit depth.
     *
     * @see #getOptimalRGBTypes
     * @see org.medtoolbox.jviewbox.BufferedImageUtilities#getPreferredRGBTypes
     * @see org.medtoolbox.jviewbox.viewport.engine.RGBSourceOptimizeEngine
     *
     * @since 2.0b
     */
    public static void setOptimalRGBTypes(int[] optimalRGBTypes)
    {
	if (optimalRGBTypes == null) {
	    _optimalRGBTypes = null;
	    return;
	}
	if (optimalRGBTypes.length == 0) {
	    throw new IllegalArgumentException("optimalRGBTypes can not be " +
					       "empty.");
	}

	// Accept only types of RGB >= 24 bits
	for (int i = 0; i < optimalRGBTypes.length; i++) {
	    if (!BufferedImageUtilities.is24BitOrMoreRGBType(optimalRGBTypes[i]))
	    {
		throw new IllegalArgumentException("optimalRGBType must be " +
						   "one of the RGB types of " +
						   "at least 24 bit depth.");
	    }
	}

	// Defensive copy
	_optimalRGBTypes = (int[])optimalRGBTypes.clone();
    }

    /**
     * Returns the <code>BufferedImage</code> type<b>s</b> used as the optimal
     * types when creating <code>RGBSourceOptimizeEngine</code>s.
     *
     * @return <code>BufferedImage</code> type<b>s</b> used as the optimal
     *	       types when creating <code>RGBSourceOptimizeEngine</code>s;
     *	       <code>null</code> if the default is used, i.e., the types given
     *	       by <code>BufferedImageUtilities.getPreferredRGBTypes</code>.
     *
     * @see #setOptimalRGBTypes
     * @see org.medtoolbox.jviewbox.BufferedImageUtilities#getPreferredRGBTypes
     *
     * @since 2.0b
     */
    public static int[] getOptimalRGBTypes()
    {
	// Defensive copy
	return (int[])_optimalRGBTypes.clone();
    }

    // ---------------
    // Private methods
    // ---------------

    /** Creates the Engines for a RGB ViewBoxViewport. */
    private static Object[] _createEnginesForRGBViewport()
    {
	SourceOptimizeEngine rgbOptimizeEngine;
	if (_optimalRGBTypes != null) {
	    rgbOptimizeEngine = new RGBSourceOptimizeEngine(_optimalRGBTypes);
	}
	else {
	    rgbOptimizeEngine = new RGBSourceOptimizeEngine();
	}

	return new Object[] { rgbOptimizeEngine,
			      new DefaultAffineTransformEngine(),
			      new NullLookUpEngine() };
    }

    /** Creates the Engines for a non-short grayscale ViewBoxViewport. */
    private static Object[] _createEnginesForGrayViewport()
    {
	return new Object[] { new NullSourceOptimizeEngine(),
			      new DefaultAffineTransformEngine(),
			      new GrayLookUpEngine() };
    }

    /** Creates the Engines for a short grayscale ViewBoxViewport. */
    private static Object[] _createEnginesForShortGrayViewport()
    {
	// Bug #4192198?
	if (BugDetector.hasSunBug4192198()) {
	    return new Object[] { new BugFix4192198SourceOptimizeEngine(),
				  new BugFix4192198AffineTransformEngine(),
				  new GrayLookUpEngine() };
	}

	// Bug #4554571?
	else if (BugDetector.hasSunBug4554571()) {
	    return new Object[] { new NullSourceOptimizeEngine(),
				  new BugFix4554571AffineTransformEngine(),
				  new BugFix4554571LookUpEngine() };
	}

	// Bug free
	else {
	    return new Object[] { new NullSourceOptimizeEngine(),
				  new DefaultAffineTransformEngine(),
				  new GrayLookUpEngine() };
	}
    }

    /** Makes a grayscale (i.e., single-band) LookUpTable. */
    private static LookUpTable _makeGrayscaleLUT(ColorModel cm,
						 ImageRenderingHints hints)
    {
	// Apply ImageRenderingHints' default LUT first
	LookUpTable lut = null;
	if (hints != null) {
	    lut = hints.getDefaultLookUpTable();
	}

	// Otherwise, create a default LUT
	if (lut == null) {
	    // Find out the # of significant bits
	    // Try ImageRenderingHints' significant bits per sample first
	    Integer nBits = null;
	    if (hints != null) {
		int[] array = hints.getSignificantBitsPerSample();
		if (array != null && array.length > 0) {
		    nBits = new Integer(array[0]);
		}
	    }
	    // Then grayscale ColorModel's component size
	    if (nBits == null && (cm instanceof ComponentColorModel) &&
		cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
		nBits = new Integer(cm.getComponentSize(0));
	    }
	    // Then ImageRenderingHints' bits per sample
	    if (nBits == null && hints != null) {
		int[] array = hints.getBitsPerSample();
		if (array != null && array.length > 0) {
		    nBits = new Integer(array[0]);
		}
	    }
	    // Finally, ColorModel's pixel size
	    if (nBits == null) {
		nBits = new Integer(cm.getPixelSize());
	    }

	    lut = new LinearLookUpTable(1 << nBits.intValue());
	}

	// Invert the LUT if black is zero is not true
	Boolean blackIsZero = hints != null ? hints.getBlackIsZero() : null;
	if (blackIsZero != null &&
	    (lut instanceof LinearLookUpTable)) {
	    ((LinearLookUpTable)lut).setInverted(!blackIsZero.booleanValue());
	}

	return lut;
    }

}
