/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox;

/**
 * A default implementation of <code>ImageRenderingHints</code> which takes
 * hint information supplied by the user at construction time.
 *
 * @version January 8, 2004
 */
public class DefaultImageRenderingHints extends AbstractImageRenderingHints
{
    // --------------
    // Private fields
    // --------------

    /** Type of color space. */
    private final Integer _colorSpaceType;

    /** Number of channels (bands). */
    private final Integer _numChannels;

    /** Whether smaller values represent darker shades. */
    private final Boolean _blackIsZero;

    /** Default <code>LookUpTable</code> to apply. */
    private final LookUpTable _defaultLookUpTable;

    /** Sizes of image samples in each channel as they are stored in memory,
	in # of bits. */
    private final int[] _bitsPerSample;

    /** Number of significant bits per sample for each channel. */
    private final int[] _significantBitsPerSample;

    /** Ratio of a pixel's width divided by it height. */
    private final Double _pixelAspectRatio;

    /** Desired orientation of an image in terms of flips and
	counter-clockwise rotation of multiples of 90 degrees. */
    private final String _imageOrientation;

    // ------------
    // Constructors
    // ------------

    /**
     * Constructs an <code>ImageRenderingHints</code> based on the supplied
     * information. Any of the parameters may be <code>null</code> if the
     * corresponding information is not available.
     *
     * @param colorSpaceType Type of color space.
     * @param numChannels Number of channels (bands).
     * @param blackIsZero Whether smaller values represent darker shades.
     * @param defaultLookUpTable Default <code>LookUpTable</code> to apply.
     * @param bitsPerSample Sizes of image samples in each channel as they are
     *			    stored in memory, in # of bits.
     * @param significantBitsPerSample Number of significant bits per sample
     *				       for each channel.
     * @param pixelAspectRatio Ratio of a pixel's width divided by it height.
     * @param imageOrientation Desired orientation of an image in terms of
     *			       flips and counter-clockwise rotation of
     *			       multiples of 90 degrees.
     */
    public DefaultImageRenderingHints(Integer colorSpaceType,
				      Integer numChannels,
				      Boolean blackIsZero,
				      LookUpTable defaultLookUpTable,
				      int[] bitsPerSample,
				      int[] significantBitsPerSample,
				      Double pixelAspectRatio,
				      String imageOrientation)
    {
	// Make defensive copy when necessary
	_colorSpaceType = colorSpaceType;
	_numChannels = numChannels;
	_blackIsZero = blackIsZero;
	_defaultLookUpTable = defaultLookUpTable == null ? null :
	    (LookUpTable)defaultLookUpTable.clone();
	_bitsPerSample = bitsPerSample == null ? null :
	    (int[])bitsPerSample.clone();
	_significantBitsPerSample = significantBitsPerSample == null ? null :
	    (int[])significantBitsPerSample.clone();
	_pixelAspectRatio = pixelAspectRatio;
	_imageOrientation = imageOrientation;
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the type of color space used by an image. The returned value
     * is an <code>Integer</code> whose value is one of the <code>TYPE</code>
     * constants defined in <code>java.awt.color.ColorSpace</code>.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return Type of color space used by an image, using integer constants
     *	       defined in <code>java.awt.color.ColorSpace</code>;
     *	       <code>null</code> if this information is not available.
     */
    public Integer getColorSpaceType()
    {
	return _colorSpaceType;
    }

    /**
     * Returns the number of channels (bands) in an image, including alpha.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return Number of channels (bands) in an image, including alpha;
     *	       <code>null</code> if this information is not available.
     */
    public Integer getNumChannels()
    {
	return _numChannels;
    }

    /**
     * Returns whether smaller values represent darker shades in an image.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return <code>true</code> if smaller values represent darker shades;
     *	       <code>false</code> if larger values represent darker shades;
     *	       <code>null</code> if this information is not available.
     */
    public Boolean getBlackIsZero()
    {
	return _blackIsZero;
    }

    /**
     * Returns the default <code>LookUpTable</code> to apply to an image.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return Default <code>LookUpTable</code> to apply to an image.
     *	       <code>null</code> if this information is not available.
     */
    public LookUpTable getDefaultLookUpTable()
    {
	// Defensive copy
	return (_defaultLookUpTable == null ? null
		: (LookUpTable)_defaultLookUpTable.clone());
    }

    /**
     * Returns the sizes of image samples in each channel as they are stored
     * in memory, in # of bits. <code>null</code> may be returned if this
     * information is not available.
     *
     * @return Sizes of image samples in each channel as they are stored in
     *	       memory, in # of bits; <code>null</code> if this information is
     *	       not available.
     */
    public int[] getBitsPerSample()
    {
	// Defensive copy
	return _bitsPerSample == null ? null : (int[])_bitsPerSample.clone();
    }

    /**
     * Returns the number of significant bits per sample for each channel.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return Number of significant bits per sample for each channel;
     *	       <code>null</code> if this information is not available.
     */
    public int[] getSignificantBitsPerSample()
    {
	// Defensive copy
	return (_significantBitsPerSample == null ? null
		: (int[])_significantBitsPerSample.clone());
    }

    /**
     * Returns the ratio of a pixel's width divided by it height.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return Ratio of a pixel's width divided by it height;
     *	       <code>null</code> if this information is not available.
     */
    public Double getPixelAspectRatio()
    {
	return _pixelAspectRatio;
    }

    /**
     * Returns the desired orientation of an image in terms of flips and
     * counter-clockwise rotation of multiples of 90 degrees. <code>null</code>
     * may be returned if this information is not available.
     *
     * @return One of the predefined <code>IMAGE_ORIENTATION</code> constants
     *	       representing the desired orientation of an image in terms of
     *	       flips and counter-clockwise rotation of multiples of 90 degrees;
     *	       <code>null</code> if this information is not available.
     */
    public String getImageOrientation()
    {
	return _imageOrientation;
    }
}
