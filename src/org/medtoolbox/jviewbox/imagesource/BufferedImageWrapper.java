/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.imagesource;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;

import org.medtoolbox.jviewbox.ImageRenderingHints;

/**
 * This class provides a convenient implementation of <code>ImageSource</code>
 * by wrapping around an existing <code>BufferedImage</code>. With an existing
 * <code>BufferedImage</code> instance, the image dimension and format never
 * change, automatically satisfying the requirement of
 * <code>ImageSource</code>.
 * <p>
 * This wrapper class never fires any event.
 *
 * @version January 8, 2004
 */
public class BufferedImageWrapper extends AbstractImageSource
{
    // --------------
    // Private fields
    // --------------

    /** BufferedImage to wrap around. */
    private final BufferedImage _bufferedImage;

    /** Rendering hints for this image. */
    private final ImageRenderingHints _imageRenderingHints;

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs an <code>ImageSource</code> based on an existing
     * <code>BufferedImage</code> with no additional rendering hints.
     *
     * @param bufferedImage <code>BufferedImage</code> on which this
     *			    ImageSource is based.
     *
     * @see #BufferedImageWrapper(BufferedImage, ImageRenderingHints).
     */
    public BufferedImageWrapper(BufferedImage bufferedImage)
    {
	this(bufferedImage, null);
    }

    /**
     * Constructs an <code>ImageSource</code> based on an existing
     * <code>BufferedImage</code> with the specified rendering hints.
     *
     * @param bufferedImage <code>BufferedImage</code> on which this
     *			    ImageSource is based.
     * @param imageRenderingHints <code>ImageRenderingHints</code> for the
     *				  <code>BufferedImage</code>; <code>null</code>
     *				  if not available.
     */
    public BufferedImageWrapper(BufferedImage bufferedImage,
				ImageRenderingHints imageRenderingHints)
    {
	if (bufferedImage == null) {
	    throw new NullPointerException("bufferedImage can not be null.");
	}

	_bufferedImage = bufferedImage;
	_imageRenderingHints = imageRenderingHints;
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the image pixels in this source as a <code>BufferedImage</code>.
     * This implementation simply returns the BufferedImage wrapped inside the
     * instance.
     *
     * @return Image in this source as a <code>BufferedImage</code>.
     */
    public BufferedImage getBufferedImage()
    {
	return _bufferedImage;
    }

    /**
     * Returns hints for rendering the image in this source, e.g., aspect ratio
     * of non-squre pixels, default look-up table, etc.
     *
     * @return Hints for rendering the image in this source specified at
     *	       construction time.
     */
    public ImageRenderingHints getImageRenderingHints()
    {
	return _imageRenderingHints;
    }

    /**
     * Returns the width of the image in this source, in pixels.
     *
     * @return Width of the image in this source in pixels.
     */
    public int getWidth()
    {
	return _bufferedImage.getWidth();
    }

    /**
     * Returns the height of the image in this source, in pixels.
     *
     * @return Height of the image in this source in pixels.
     */
    public int getHeight()
    {
	return _bufferedImage.getHeight();
    }

    /**
     * Returns the <code>ColorModel</code> of the image in this source.
     *
     * @return <code>ColorModel</code> of the image in this source.
     */
    public ColorModel getColorModel()
    {
	return _bufferedImage.getColorModel();
    }

    /**
     * Returns the <code>SampleModel</code> of the image in this source.
     *
     * @return <code>SampleModel</code> of the image in this source.
     */
    public SampleModel getSampleModel()
    {
	return _bufferedImage.getSampleModel();
    }
}
