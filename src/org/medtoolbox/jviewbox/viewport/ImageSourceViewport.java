/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.medtoolbox.jviewbox.ImageRenderingHints;
import org.medtoolbox.jviewbox.imagesource.ImageSource;
import org.medtoolbox.jviewbox.imagesource.ProgressListener;

/**
 * Viewport which gets its image from an <code>ImageSource</code>.
 *
 * @version January 8, 2004
 */
public class ImageSourceViewport extends Viewport
{
    // ---------
    // Constants
    // ---------

    /** Default width to use if image width is unavailable. */
    private static final int DEFAULT_WIDTH = 512;

    /** Default height to use if image height is unavailable. */
    private static final int DEFAULT_HEIGHT = 512;

    // --------------
    // Private fields
    // --------------

    /** ImageSource of this Viewport. */
    private final ImageSource _imageSource;

    /** Cached BufferedImage. */
    private BufferedImage _bufferedImage;

    /** Text message to show when image is not available. */
    private volatile String _viewportMessage;

    /** IOException occurred when loading the image. */
    private volatile IOException _ioException;

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs a Viewport for the specified <code>ImageSource</code>.
     * <p>
     * If <code>ImageRenderingHints</code> are available from the specified
     * source, the pixel aspect ratio hint will be used to adjust the initial
     * scale factors accordingly. The image orientation hint will also be
     * applied to the initial rotation and flipping settings.
     *
     * @param imageSource <code>ImageSource</code> of the image to display.
     *
     * @throws NullPointerException if <code>imageSource</code> is
     *	       <code>null</code>.
     *
     * @see org.medtoolbox.jviewbox.ImageRenderingHints
     */
    public ImageSourceViewport(ImageSource imageSource)
    {
	_imageSource = imageSource;

	// Read rendering hints
	ImageRenderingHints hints = null;
	try {
	    hints = imageSource.getImageRenderingHints();
	}
	catch (IOException e) {}

	// Handle rendering hints
	if (hints != null) {

	    // Pixel aspect ratio
	    Double d = hints.getPixelAspectRatio();
	    double ratio = d != null ? d.doubleValue() : 0.0;
	    if (ratio > 0.0 && ratio != 1.0) {
		// Adjust initial scaling factor to compensate 
		if (ratio > 1.0) {
		    // Pixel is wider than it is tall
		    setScale(getScaleX(), getScaleY() / ratio);
		}
		else {
		    // Pixel is taller than it is wide
		    setScale(getScaleX() * ratio, getScaleY());
		}
	    }

	    // Image orientation
	    String orientation = hints.getImageOrientation();
	    if (orientation != null) {
		// Note that Viewport's rotation is clockwise
		if (orientation.equalsIgnoreCase(
		        ImageRenderingHints.IMAGE_ORIENTATION_ROTATE_90)) {
		    setRotation(3);
		}
		else if (orientation.equalsIgnoreCase(
			 ImageRenderingHints.IMAGE_ORIENTATION_ROTATE_180)) {
		    setRotation(2);
		}
		else if (orientation.equalsIgnoreCase(
			 ImageRenderingHints.IMAGE_ORIENTATION_ROTATE_270)) {
		    setRotation(1);
		}

		else if (orientation.equalsIgnoreCase(
			 ImageRenderingHints.IMAGE_ORIENTATION_FLIP_H)) {
		    setVerticallyFlipped(true);
		    setRotation(2);
		}
		else if (orientation.equalsIgnoreCase(
			 ImageRenderingHints.IMAGE_ORIENTATION_FLIP_V)) {
		    setVerticallyFlipped(true);
		}
		else if (orientation.equalsIgnoreCase(
		     ImageRenderingHints.IMAGE_ORIENTATION_FLIP_H_ROTATE_90)) {
		    setVerticallyFlipped(true);
		    setRotation(1);
		}
		else if (orientation.equalsIgnoreCase(
		     ImageRenderingHints.IMAGE_ORIENTATION_FLIP_V_ROTATE_90)) {
		    setVerticallyFlipped(true);
		    setRotation(3);
		}
	    }
	}
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the image in this Viewport. Synonym of
     * {@link #getBufferedImage}.
     *
     * @return Image displayed in this Viewport;
     *	       <code>null</code> if failed to load the image from source.
     *
     * @see #getBufferedImage
     */
    public Image getImage()
    {
	return getBufferedImage();
    }

    /**
     * Returns the <code>BufferedImage</code> in this Viewport.
     *
     * @return <code>BufferedImage</code> displayed in this Viewport;
     *	       <code>null</code> if failed to load the image from source.
     *
     * @see #getIOException
     */
    public synchronized BufferedImage getBufferedImage()
    {
	// If cache is not available, get it from source
	if (_bufferedImage == null && _ioException == null) {
	    try {
		_bufferedImage = _imageSource.getBufferedImage();
		_ioException = null;
	    }
	    catch (IOException e) {
		_ioException = e;
		_viewportMessage = "I/O Error, " + e;
	    }
	}

	return _bufferedImage;
    }

    /**
     * Returns the <code>IOException</code>, if any, that occurred the last
     * time loading the image from <code>ImageSource</code>. This Viewport
     * will not try again to load the image until the <code>IOException</code>
     * is explicitly cleared by calling {@link #clearIOException}.
     *
     * @return <code>IOException</code> that occurred the last time loading
     *	       the image from <code>ImageSource</code>; <code>null</code> if
     *	       none.
     *
     * @see #clearIOException
     */
    public IOException getIOException()
    {
	return _ioException;
    }

    /**
     * Clears the record of the <code>IOException</code> that occurred the last
     * time loading the image from <code>ImageSource</code>. Only after the
     * <code>IOException</code> is cleared will this Viewport try again to
     * load the image.
     *
     * @see #getIOException
     */
    public synchronized void clearIOException()
    {
	_ioException = null;
    }

    /**
     * Returns the <code>ImageSource</code> of this Viewport.
     *
     * @return <code>ImageSource</code> of this Viewport.
     */
    public ImageSource getImageSource()
    {
	return _imageSource;
    }

    /**
     * Flushes all the resources being used by the Viewport. This includes
     * any cached data for rendering to the screen and any system resources
     * that are being used to store the image pixel data.
     * <p>
     * The record of <code>IOException</code> that occurred the last time
     * loading the image from <code>ImageSource</code> will be cleared, too,
     * just like when {@link #clearIOException} is called.
     *
     * @see #clearIOException
     */
    public synchronized void flush()
    {
	super.flush();

	// As ImageSource.flush() is deprecated in 2.0b, this call may be
	// removed once ImageSource.flush() is completely retired
	_imageSource.flush();

	if (_bufferedImage != null) {
	    _bufferedImage.flush();
	    _bufferedImage = null;
	}
	_ioException = null;
    }

    /** 
     * Paints this Viewport. The internal states of the <code>Graphics2D</code>
     * object passed in <b>MUST</b> be preserved, i.e., saved and restored if
     * changes are to be made. The states includes (but are not limited to)
     * the current color, font, clip, and transform (translation, scale, etc.)
     * <p>
     * As of the current implementation, this method will load the pixel data
     * from the <code>ImageSource</code> if the data has not been loaded or
     * the cache has been flushed. Pixel data loading is synchronous, which
     * means this method stalls as long as it takes to finish loading.
     * <p>
     * If pixel loading fails, this method shows an error message in the
     * Viewport.
     *
     * @param g2d Graphics context used for painting.
     * @param toEraseBackground Whether this method should erase the background
     *				while painting.
     */
    public synchronized void paint(Graphics2D g2d, boolean toEraseBackground)
    {
	// Make sure the image is loaded
	BufferedImage bi = getBufferedImage();

	// Is image available?
	if (bi != null) {
	    super.paint(g2d, toEraseBackground);
	}

	// Image is not available
	else {
	    // Erase background if requested
	    // Since there's no image, always do pre erase.
	    if (toEraseBackground) {
		_preEraseBackground(g2d);
	    }

	    // Is message available?
	    if (_viewportMessage != null) {
		// Center the text message in this Viewport
		int vpCenterX = getX() + getWidth() / 2;
		int vpCenterY = getY() + getHeight() / 2;
		FontMetrics metric = g2d.getFontMetrics();
		Rectangle2D bounds = metric.getStringBounds(_viewportMessage,
							    g2d);
		Color c = g2d.getColor();
		g2d.setColor(Color.white);
		g2d.drawString(_viewportMessage,
			       vpCenterX - (int)bounds.getCenterX(),
			       vpCenterY - (int)bounds.getCenterY());
		g2d.setColor(c);
	    }

	    // Still paint annotations
	    if (isAnnotationEnabled()) {
		_paintAnnotations(g2d);
	    }
	}
    }

    // -----------------
    // Protected methods
    // -----------------

    // Image dimension

    /**
     * Returns the (original) width of the image to be displayed.
     */
    protected int _getImageWidth()
    {
	try {
	    return getImageSource().getWidth();
	}
	catch (IOException e) {
	    // Ignore the IOException
	    return DEFAULT_WIDTH;
	}
    }

    /**
     * Returns the (original) height of the image to be displayed.
     */
    protected int _getImageHeight()
    {
	try {
	    return getImageSource().getHeight();
	}
	catch (IOException e) {
	    // Ignore the IOException
	    return DEFAULT_HEIGHT;
	}
    }
}
