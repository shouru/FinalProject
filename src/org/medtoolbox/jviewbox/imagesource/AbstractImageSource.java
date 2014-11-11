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
import java.util.List;

import org.medtoolbox.jviewbox.ImageRenderingHints;

/**
 * This abstract class provides default implementation for many methods in
 * the <code>ImageSource</code> interface. It takes care of the management
 * of listeners and provides some convenience methods for firing progress
 * events. To create a concrete <code>ImageSource</code> class by subclassing
 * <code>AbstractImageSource</code>, one needs only to implement the following
 * methods:
 * <br><pre>
 * public BufferedImage getBufferedImage()
 * public ColorModel getColorModel()
 * public SampleModel getSampleModel()</pre>
 *
 * Note that the implementation of <code>getColorModel</code> and
 * <code>getSampleModel</code> should do as little work as necessary to
 * retrieve the information and generally should <b>NOT</b> invoke
 * <code>getBufferedImage</code>. Otherwise it would defeat the original
 * purpose of <code>ImageSource</code>: on-demand loading of image data. In
 * addition, the default implementation for the following methods
 * <br><pre>
 * public int getWidth()
 * public int getHeight()</pre>
 *
 * gets the corresponding information from the <code>SampleModel</code>
 * returned by <code>getSampleModel</code>. This is usually a reasonably
 * efficient choice. Nevertheless, subclass may choose to override them
 * with more efficient implementation.
 * <p>
 * Also, the default implementation of <code>getImageRenderingHints</code>
 * simply returns <code>null</code>.
 *
 * @version January 8, 2004
 */
public abstract class AbstractImageSource implements ImageSource
{
    // --------------
    // Private fields
    // --------------

    /** List of ProgressListener. */
    private final ProgressListenerList _listenerList =
	new ProgressListenerList();

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs an AbstractImageSource.
     */
    public AbstractImageSource()
    {
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the width of the image in this source, in pixels. This default
     * implementation gets the width from the <code>SampleModel</code>
     * returned by <code>getSampleModel</code>. Subclasses are encouraged
     * to override this method with a more efficient implementation, i.e.,
     * one that does not require the loading/generating of the entire image.
     *
     * @return Width of the image in this source in pixels.
     *
     * @throws IOException if I/O error occurs reading the width information.
     */
    public int getWidth() throws IOException
    {
	return getSampleModel().getWidth();
    }

    /**
     * Returns the height of the image in this source, in pixels. This default
     * implementation gets the height from the <code>SampleModel</code>
     * returned by <code>getSampleModel</code>. Subclasses are encouraged
     * to override this method with a more efficient implementation, i.e.,
     * one that does not require the loading/generating of the entire image.
     *
     * @return Height of the image in this source in pixels.
     *
     * @throws IOException if I/O error occurs reading the height information.
     */
    public int getHeight() throws IOException
    {
	return getSampleModel().getHeight();
    }



    /**
     * Returns hints for rendering the image in this source, e.g., aspect ratio
     * of non-squre pixels, default look-up table, etc. This default
     * implementation simply returns <code>null</code>.
     *
     * @return <code>null</code>.
     *
     * @throws IOException if I/O error occurs reading this information.
     */
    public ImageRenderingHints getImageRenderingHints() throws IOException
    {
	return null;
    }

    /**
     * Flush any disposable resource used by this ImageSource. By disposable,
     * it means anything that can be re-loaded or re-generated and hence does
     * not need to be possessed by the program at all time. For example, pixel
     * data loaded into memory from an image file.
     * <p>
     * This default implementation does nothing and hence does not need to be
     * called in subclasses' <code>flush</code>.
     *
     * @deprecated As of jViewBox 2.0b, caching of <code>BufferedImage</code>
     *		   is discontinued and discouraged.
     */
    public void flush()
    {
    }

    /**
     * Adds an <code>ProgressListener</code> to this ImageSource. All
     * registered listeners get notified of progess made in
     * <code>getBufferedImage</code>.
     *
     * @param listener ProgressListener to add to this source. This method does
     *		       nothing and throws no exception if
     *		       <code>listener</code> is <code>null</code>.
     *
     * @see #removeProgressListener
     */
    public void addProgressListener(ProgressListener listener)
    {
	_listenerList.add(listener);
    }

    /**
     * Unregister an <code>ProgressListener</code> from this ImageSource.
     *
     * @param listener ProgressListener to remove from this source. This method
     *		       does nothing and throws no exception if
     *		       <code>listener</code> is <code>null</code>.
     *
     * @see #addProgressListener
     */
    public void removeProgressListener(ProgressListener listener)
    {
	_listenerList.remove(listener);
    }

    /**
     * Returns an <b>unmodifiable</b> <code>List</code> of
     * <code>ProgressListener</code>s registered with this ImageSource.
     *
     * @return Unmodifiable <code>List</code> of <code>ProgressListener</code>s
     *	       registered with this ImageSource.
     *
     * @see #addProgressListener
     * @see #removeProgressListener
     */
    public List getProgressListeners()
    {
	return _listenerList.getListeners();
    }

    // -----------------
    // Protected methods
    // -----------------

    /**
     * Fires an <code>imageStarted</code> event to all
     * <code>ProgressListener</code>s registered with this ImageSource.
     */
    protected void fireImageStarted()
    {
	_listenerList.fireImageStarted(this);
    }

    /**
     * Fires an <code>imageComplete</code> event to all
     * <code>ProgressListener</code>s registered with this ImageSource.
     */
    protected void fireImageComplete()
    {
	_listenerList.fireImageComplete(this);
    }

    /**
     * Fires an <code>imageProgress</code> event to all
     * <code>ProgressListener</code>s registered with this ImageSource.
     *
     * @param percentage Percentage (a number between 0 and 100 inclusive) of
     *			 loading that has been completed; a negative number
     *			 if it is not available.
     */
    protected void fireImageProgress(float percentage)
    {
	_listenerList.fireImageProgress(this, percentage);
    }
}
