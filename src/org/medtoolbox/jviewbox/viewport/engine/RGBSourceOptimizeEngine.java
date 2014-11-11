/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.engine;

import java.awt.image.BufferedImage;
import java.util.List;

import org.medtoolbox.jviewbox.BufferedImageUtilities;

/**
 * <code>SourceOptimizeEngine</code> which optmizes RGB images.
 *
 * @version January 8, 2004
 */
public class RGBSourceOptimizeEngine implements SourceOptimizeEngine
{
    // --------------
    // Private fields
    // --------------

    /** Optimal BufferedImage types, in the order of preference. */
    private int[] _optimalTypes;

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs a RGBSourceOptimizeEngine which considers the
     * <code>BufferedImage</code> type<b>s</b> returned by
     * <code>BufferedImageUtilities.getPreferredRGBTypes</code> as optimal.
     *
     * @see org.medtoolbox.jviewbox.BufferedImageUtilities#getPreferredRGBTypes
     */
    public RGBSourceOptimizeEngine()
    {
	this(BufferedImageUtilities.getPreferredRGBTypes());
    }

    /**
     * Constructs a RGBSourceOptimizeEngine which considers <b>only</b> the
     * specified <code>BufferedImage</code> type as optimal. It converts any 
     * other types into this type.
     *
     * @param optimalType Optimal <code>BufferedImage</code> type to convert
     *			  images into.
     *
     * @throws IllegalArgumentException if <code>optimalType</code> is not an
     *	       RGB type of no less than 24 bit depth.
     */
    public RGBSourceOptimizeEngine(int optimalType)
    {
	this(new int[] { optimalType });
    }

    /**
     * Constructs a RGBSourceOptimizeEngine which considers the
     * <code>BufferedImage</code> type<b>s</b> specified in the array as
     * optimal. It converts any other types into the first type in the array.
     *
     * @param optimalTypes <code>BufferedImage</code> types considered optimal.
     *
     * @throws IllegalArgumentException if <code>optimalTypes</code> is empty
     *	       or any element in it is not an RGB type of no less than 24 
     *         bit depth.
     *
     * @since 2.0b
     */
    public RGBSourceOptimizeEngine(int[] optimalTypes)
    {
	setOptimalBufferedImageTypes(optimalTypes);
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the <code>BufferedImage</code> type into which this engine
     * converts original images (which is the first type in the optimal types
     * array since 2.0b).
     *
     * @return <code>BufferedImage</code> type into which this engine converts
     *	       original images.
     */
    public int getOptimalBufferedImageType()
    {
	return _optimalTypes[0];
    }

    /**
     * Returns the <code>BufferedImage</code> types that are considered 
     * optimal by this engine. Any other types will be converted into the
     * first type in the returned array.
     *
     * @return <code>BufferedImage</code> types that are considered 
     *	       optimal by this engine.
     *
     * @since 2.0b
     */
    public int[] getOptimalBufferedImageTypes()
    {
	// Defensive copy
	return (int[])_optimalTypes.clone();
    }

    /**
     * Sets the <code>BufferedImage</code> types that are considered optimal
     * by this engine. This setting will take effect the next time the
     * <code>Viewport</code> repaint.
     *
     * @param optimalTypes <code>BufferedImage</code> types considered optimal.
     *
     * @throws IllegalArgumentException if <code>optimalTypes</code> is empty
     *	       or any element in it is not an RGB type of no less than 24 
     *         bit depth.
     *
     * @see #getOptimalBufferedImageTypes()
     *
     * @since 2.0b
     */
    public void setOptimalBufferedImageTypes(int[] optimalTypes)
    {
	if (optimalTypes == null) {
	    throw new NullPointerException("optimalTypes can not be null.");
	}
	if (optimalTypes.length == 0) {
	    throw new IllegalArgumentException("optimalTypes can not be " +
					       "empty.");
	}

	// Accept only types of RGB >= 24 bits
	for (int i = 0; i < optimalTypes.length; i++) {
	    if (!BufferedImageUtilities.is24BitOrMoreRGBType(optimalTypes[i]))
	    {
		throw new IllegalArgumentException("optimalType must be one " +
						   "of the RGB types of at " +
						   "least 24 bit depth.");
	    }
	}

	// Defensive copy
	_optimalTypes = (int[])optimalTypes.clone();
    }

    /**
     * Converts an original <code>BufferedImage</code> into the optimal format
     * specified at construction time. This conversion should be lossless. The
     * original is never modified.
     * <p>
     * This implementation will reuse the <code>previousResult</code> 
     * <code>BufferedImage</code> for output if it is of the same dimension as
     * <code>original</code> and it is of the optimal type.
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
	// Already optimal?
	int type = original.getType();
	for (int i = 0; i < _optimalTypes.length; i++) {
	    if (type == _optimalTypes[i]) {
		return original;
	    }
	}

	// Create new destination if necessary
	BufferedImage optimized;
	if (previousResult != null &&
	    previousResult.getWidth() == original.getWidth() &&
	    previousResult.getHeight() == original.getHeight() &&
	    previousResult.getType() == _optimalTypes[0]) {

	    optimized = previousResult;
	}
	else {
	    optimized =
		new BufferedImage(original.getWidth(), original.getHeight(),
				  _optimalTypes[0]);
	}


	// Perform conversion
	BufferedImageUtilities.convertToRGB(original, optimized);

	return optimized;
    }
}
