/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox;

/**
 * An abstract base class for the ease of implementing
 * <code>ImageRenderingHints</code>. Each method in this class has a trivial
 * default implementation that returns <code>null</code>.
 *
 * @version January 8, 2004
 */
public abstract class AbstractImageRenderingHints
    implements ImageRenderingHints
{
    // --------------
    // Public methods
    // --------------

    /**
     * Returns the type of color space used by an image. The returned value
     * is an <code>Integer</code> whose value is one of the <code>TYPE</code>
     * constants defined in <code>java.awt.color.ColorSpace</code>.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return <code>null</code>.
     */
    public Integer getColorSpaceType()
    {
	return null;
    }

    /**
     * Returns the number of channels (bands) in an image, including alpha.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return <code>null</code>.
     */
    public Integer getNumChannels()
    {
	return null;
    }

    /**
     * Returns whether smaller values represent darker shades in an image.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return <code>null</code>.
     */
    public Boolean getBlackIsZero()
    {
	return null;
    }

    /**
     * Returns the default <code>LookUpTable</code> to apply to an image.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return <code>null</code>.
     */
    public LookUpTable getDefaultLookUpTable()
    {
	return null;
    }

    /**
     * Returns the sizes of image samples in each channel as they are stored
     * in memory, in # of bits. <code>null</code> may be returned if this
     * information is not available.
     *
     * @return <code>null</code>.
     */
    public int[] getBitsPerSample()
    {
	return null;
    }

    /**
     * Returns the number of significant bits per sample for each channel.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return <code>null</code>.
     */
    public int[] getSignificantBitsPerSample()
    {
	return null;
    }

    /**
     * Returns the ratio of a pixel's width divided by it height.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return <code>null</code>.
     */
    public Double getPixelAspectRatio()
    {
	return null;
    }

    /**
     * Returns the desired orientation of an image in terms of flips and
     * counter-clockwise rotation of multiples of 90 degrees. <code>null</code>
     * may be returned if this information is not available.
     *
     * @return <code>null</code>.
     */
    public String getImageOrientation()
    {
	return null;
    }
}
