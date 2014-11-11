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
import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.metadata.IIOMetadata;

import org.w3c.dom.Node;

import org.medtoolbox.jviewbox.ImageRenderingHints;
import org.medtoolbox.jviewbox.ImageRenderingHintsFactory;

/**
 * This class provides a convenient implementation of <code>ImageSource</code>
 * by adapting an existing <code>ImageReader</code>. The ImageReader instance
 * used by an adapter instance must be configured with an input source which
 * allows the reading of the same image repetitively.
 *
 * @version January 8, 2004
 */
public class ImageReaderAdapter extends AbstractImageSource
{
    // --------------
    // Private fields
    // --------------

    /** ImageReader this adapter is adapting. */
    private final ImageReader _imageReader;

    /** Index of the image for the ImageReader to read. */
    private final int _imageIndex;

    /** Parameters used to control the read process. */
    private final ImageReadParam _imageReadParam;

    /** Cached copy of the image's width. */
    private int _imageWidth = -1;

    /** Cached copy of the image's height. */
    private int _imageHeight = -1;



    /** Cached copy of the image's rendering hints. */
    private ImageRenderingHints _renderingHints;

    // ------------
    // Constructors
    // ------------

    /**
     * Constructs an <code>ImageSource</code> where an <code>ImageReader</code>
     * is used to load the image. The input source of the ImageReader must be
     * set correctly before calling this constrcutor. By default the
     * constructed adapter monitors the read progress and fires event
     * accordingly.
     *
     * @param reader <code>ImageReader</code> used to load the image.
     * @param index Index of the image to retrieve by <code>reader</code>.
     *
     * @throws NullPointerException if <code>reader</code> is
     *	       <code>null</code>.
     */
    public ImageReaderAdapter(ImageReader reader, int index)
    {
	this(reader, index, null, true);
    }

    /**
     * Constructs an <code>ImageSource</code> where an <code>ImageReader</code>
     * is used to load the image. The input source of the ImageReader must be
     * set correctly before calling this constrcutor. By default the
     * constructed adapter monitors the read progress and fires event
     * accordingly.
     *
     * @param reader <code>ImageReader</code> used to load the image.
     * @param index Index of the image to retrieve by <code>reader</code>.
     * @param param <code>ImageReadParam</code> used to control the reading
     *		    process, or <code>null</code> to use the default.
     *
     * @throws NullPointerException if <code>reader</code> is
     *	       <code>null</code>.
     */
    public ImageReaderAdapter(ImageReader reader, int index,
			      ImageReadParam param)
    {
	this(reader, index, param, true);
    }

    /**
     * Constructs an <code>ImageSource</code> where an <code>ImageReader</code>
     * is used to load the image. The input source of the ImageReader must be
     * set correctly before calling this constrcutor.
     *
     * @param reader <code>ImageReader</code> used to load the image.
     * @param index Index of the image to retrieve by <code>reader</code>.
     * @param param <code>ImageReadParam</code> used to control the reading
     *		    process, or <code>null</code> to use the default.
     * @param monitorsProgress <code>true</code> to have the constructed
     *			       adapter monitor the read progress of
     *			       <code>reader</code> and fire events accordingly;
     *			       <code>false</code> to disable monitoring and
     *			       event firing.
     *
     * @throws NullPointerException if <code>reader</code> is
     *	       <code>null</code>.
     */
    public ImageReaderAdapter(ImageReader reader, int index,
			      ImageReadParam param, boolean monitorsProgress)
    {
	if (reader == null) {
	    throw new NullPointerException("reader can not be null.");
	}

	_imageReader = reader;
	_imageIndex = index;
	_imageReadParam = param;

	// Register an IIOReadProgressListener if in asynchronous mode
	if (monitorsProgress) {
	    IIOReadProgressListener listener = new ReadProgressListener();
	    _imageReader.addIIOReadProgressListener(listener);
	}
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the <code>ImageReader</code> in this adapter.
     *
     * @return <code>ImageReader</code> in this adapter.
     */
    public ImageReader getImageReader()
    {
	return _imageReader;
    }

    /**
     * Returns the index of the image to be read by the
     * <code>ImageReader</code> in this adapter.
     *
     * @return Index of the image to be read.
     */
    public int getImageIndex()
    {
	return _imageIndex;
    }

    /**
     * Returns the <code>ImageReadParam</code> used to read the image.
     *
     * @return <code>ImageReadParam</code> used to read the image;
     *	       <code>null</code> if the reader's default is to be used.
     */
    public ImageReadParam getImageReadParam()
    {
	return _imageReadParam;
    }

    /**
     * Returns the width of the image in this source, in pixels.
     *
     * @return Width of the image in this source in pixels.
     *
     * @throws IOException if I/O error occurs reading the width information.
     * @throws IndexOutOfBoundsException if the image index supplied at 
     *	       construction time is out of bound.
     * @throws IllegalStateException if the input source of the ImageReader
     *	       supplied at construction time has not been set.
     */
    public synchronized int getWidth() throws IOException
    {
	// Lazy initialization
	if (_imageWidth < 0) {
	    _imageWidth = _imageReader.getWidth(_imageIndex);
	}

	return _imageWidth;
    }

    /**
     * Returns the height of the image in this source, in pixels.
     *
     * @return Height of the image in this source in pixels.
     *
     * @throws IOException if I/O error occurs reading the height information.
     * @throws IndexOutOfBoundsException if the image index supplied at 
     *	       construction time is out of bound.
     * @throws IllegalStateException if the input source of the ImageReader
     *	       supplied at construction time has not been set.
     */
    public synchronized int getHeight() throws IOException
    {
	// Lazy initialization
	if (_imageHeight < 0) {
	    _imageHeight = _imageReader.getHeight(_imageIndex);
	}

	return _imageHeight;
    }

    /**
     * Returns the <code>ImageTypeSpecifier</code> which specifies the format
     * of the image in this source.
     * <p>
     * Specifically, this method gets the type specifier from, if available,
     * the <code>ImageReadParam</code> specified at the construction time.
     * Otherwise, the first among <code>ImageReader.getImageTypes</code> is
     * returned.
     *
     * @return <code>ImageTypeSpecifier</code> which specifies the format of
     *	       the image in this source.
     *
     * @throws IOException if I/O error occurs reading the format information.
     * @throws IndexOutOfBoundsException if the image index supplied at 
     *	       construction time is out of bound.
     * @throws IllegalStateException if the input source of the ImageReader
     *	       supplied at construction time has not been set.
     *
     * @see javax.imageio.ImageReadParam#getDestinationType
     * @see javax.imageio.ImageReader#getImageTypes
     */
    public synchronized ImageTypeSpecifier getImageType() throws IOException
    {

	// Try to get type from read param
	if (_imageReadParam != null) {
	    ImageTypeSpecifier type = _imageReadParam.getDestinationType();
	    if (type != null) {
		return type;
	    }
	}
	// Otherwise, use the first from ImageReader.getImageTypes(),
	// which is supposed to be the one used by the reader in this case.
	return (ImageTypeSpecifier)
	    _imageReader.getImageTypes(_imageIndex).next();
    }

    /**
     * Returns the <code>ColorModel</code> of the image in this source.
     * <p>
     * This method depends on <code>getImageType</code> for the color model
     * information.
     *
     * @return <code>ColorModel</code> of the image in this source.
     *
     * @throws IOException if I/O error occurs reading the color information.
     * @throws IndexOutOfBoundsException if the image index supplied at 
     *	       construction time is out of bound.
     * @throws IllegalStateException if the input source of the ImageReader
     *	       supplied at construction time has not been set.
     *
     * @see #getImageType
     * @see javax.imageio.ImageTypeSpecifier#getColorModel
     */
    public ColorModel getColorModel() throws IOException
    {
	return getImageType().getColorModel();
    }

    /**
     * Returns the <code>SampleModel</code> of the image in this source.
     * <p>
     * This method depends on <code>getImageType</code> for the sample model
     * information.
     *
     * @return <code>SampleModel</code> of the image in this source.
     *
     * @throws IOException if I/O error occurs reading this information.
     * @throws IndexOutOfBoundsException if the image index supplied at 
     *	       construction time is out of bound.
     * @throws IllegalStateException if the input source of the ImageReader
     *	       supplied at construction time has not been set.
     *
     * @see #getImageType
     * @see javax.imageio.ImageTypeSpecifier#getSampleModel
     */
    public SampleModel getSampleModel() throws IOException
    {
	return getImageType().getSampleModel();
    }

    /**
     * Returns the image pixels in this source as a <code>BufferedImage</code>.
     *
     * @return Image in this source as a <code>BufferedImage</code>.
     *
     * @throws IOException if I/O error occurs reading the pixel data.
     * @throws IndexOutOfBoundsException if the image index supplied at 
     *	       construction time is out of bound.
     * @throws IllegalStateException if the input source of the ImageReader
     *	       supplied at construction time has not been set.
     */
    public synchronized BufferedImage getBufferedImage() throws IOException
    {

	return _imageReader.read(_imageIndex, _imageReadParam);
    }

    /**
     * Sets the <code>ImageRenderingHints</code> to be returned by
     * <code>getImageRenderingHints</code>.
     * <p>
     * Normally, <code>getImageRenderingHints</code> will try to send the
     * image's metadata to <code>ImageRenderingHintsFactory</code> for
     * rendering hints. It caches the result and will not repeat the process.
     * This method allows you to override the setting. Setting this to
     * <code>null</code> will cause <code>getImageRenderingHints</code> to
     * send the image's metadata to <code>ImageRenderingHintsFactory</code>
     * for hints again.
     *
     * @param hints <code>ImageRenderingHints</code> to be returned by
     *		    <code>getImageRenderingHints</code>;
     *		    <code>null</code> to have
     *		    <code>getImageRenderingHints</code> send the image's
     *		    metadata to <code>ImageRenderingHintsFactory</code> for
     *		    rendering hints.
     *
     * @see #getImageRenderingHints
     * @see org.medtoolbox.jviewbox.ImageRenderingHintsFactory
     *
     * @since 2.0b
     */
    public synchronized void setImageRenderingHints(ImageRenderingHints hints)
    {
	_renderingHints = hints;
    }

    /**
     * Returns hints for rendering the image in this source.
     * <p>
     * If hints have not been previously set or have been set to
     * <code>null</code> by <code>setImageRenderingHints</code>, this method
     * will try to create hints by sending the image's metadata to
     * <code>ImageRenderingHintsFactory</code>.
     *
     * @return Hints for rendering the image in this source, either previously
     *	       given by <code>setImageRenderingHints</code> or created by
     *	       sending the image's metadata to
     *	       <code>ImageRenderingHintsFactory</code>.
     *
     * @throws IOException if I/O error occurs when reading the image's
     *	       metadata.
     */
    public synchronized ImageRenderingHints getImageRenderingHints()
	throws IOException
    {
	// Lazy initialization
	if (_renderingHints == null) {
	    // Try image metadata first, followed by stream metadata
	    // for one that is not null and supports standard metadata format
	    IIOMetadata metadata;
	    IIOMetadata imageMeta;
	    IIOMetadata streamMeta;
	    if ((imageMeta = _imageReader.getImageMetadata(_imageIndex)) !=
		null && imageMeta.isStandardMetadataFormatSupported()) {
		metadata = imageMeta;
	    }
	    else if ((streamMeta = _imageReader.getStreamMetadata()) != null &&
		     streamMeta.isStandardMetadataFormatSupported()) {
		metadata = streamMeta;
	    }
	    else {
		metadata = imageMeta;
	    }

	    if (metadata != null) {
		_renderingHints =
		    ImageRenderingHintsFactory.createHints(metadata);
	    }
	}

	return _renderingHints;
    }

    /**
     * As of the deprecation, pixel data is no longer cached and this method
     * does nothing.
     *
     * @deprecated As of jViewBox 2.0b, caching of <code>BufferedImage</code>
     *		   is discontinued and discouraged.
     */
    public void flush()
    {
    }

    // --------------------
    // Private member class
    // --------------------

    /**
     * This private inner class is used to implement IIOReadProgressListener.
     * Implementing a listener interface this way prevents the exposing of
     * callback methods as "public" methods to the outside world which they
     * are never meant to be.
     */
    private class ReadProgressListener implements IIOReadProgressListener
    {
	/** Constructor. */
	ReadProgressListener() {}

	// ----------------
	// Callback methods
	// ----------------

	/**
	 * <code>IIOReadProgressListener</code> callback from
	 * <code>ImageReader</code>.
	 */
	public void imageStarted(ImageReader source, int imageIndex)
	{
	    ImageReaderAdapter.this.fireImageStarted();
	}

	/**
	 * <code>IIOReadProgressListener</code> callback from
	 * <code>ImageReader</code>.
	 */
	public void imageComplete(ImageReader source)
	{
	    ImageReaderAdapter.this.fireImageComplete();
	}

	/**
	 * <code>IIOReadProgressListener</code> callback from
	 * <code>ImageReader</code>.
	 */
	public void imageProgress(ImageReader source, float percentageDone)
	{
	    ImageReaderAdapter.this.fireImageProgress(percentageDone);
	}

	/**
	 * <code>IIOReadProgressListener</code> callback from
	 * <code>ImageReader</code>.
	 */
	public void readAborted(ImageReader source)
	{
	    // Should never happen because we never call ImageReader.abort()
	}

	/**
	 * <code>IIOReadProgressListener</code> callback from
	 * <code>ImageReader</code>.
	 */
	public void sequenceStarted(ImageReader source, int minIndex)
	{
	    // Should never happen because we never call ImageReader.readAll()
	}

	/**
	 * <code>IIOReadProgressListener</code> callback from
	 * <code>ImageReader</code>.
	 */
	public void sequenceComplete(ImageReader source)
	{
	    // Should never happen because we never call ImageReader.readAll()
	}

	/**
	 * <code>IIOReadProgressListener</code> callback from
	 * <code>ImageReader</code>.
	 */
	public void thumbnailStarted(ImageReader source, int imageIndex,
				     int thumbnailIndex)
	{
	    // Don't care because we don't use thumbnails at all
	}

	/**
	 * <code>IIOReadProgressListener</code> callback from
	 * <code>ImageReader</code>.
	 */
	public void thumbnailComplete(ImageReader source)
	{
	    // Don't care because we don't use thumbnails at all
	}

	/**
	 * <code>IIOReadProgressListener</code> callback from
	 * <code>ImageReader</code>.
	 */
	public void thumbnailProgress(ImageReader source, float percentageDone)
	{
	    // Don't care because we don't use thumbnails at all
	}
    }
}
