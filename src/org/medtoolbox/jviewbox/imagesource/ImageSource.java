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

import org.medtoolbox.jviewbox.ImageRenderingHints;

/**
 * Interface which represents a reloadable source of a single image. This
 * interface is used by jViewBox to facilitate on-demand image loading and
 * flushing.
 * <p>
 * Certain information about the image is necessary for jViewBox's operation,
 * namely, the width, height, and format of the image. The format is specified
 * by the combination of a <code>ColorModel</code> and a
 * <code>SampleModel</code>. The implementation of <code>get</code> methods
 * for the above information is expected to perform as little amount of work
 * as possible just enough to retrieve the specific information. <b>Note</b>
 * that the width, height, and format of an image must be invariant, i.e., the
 * corresponding <code>get</code> methods must return the same values (more
 * precisely, same or equivalent values) throughout the lifetime of an instance
 * of ImageSource. Failure to do so may result in unpredictable behavior of
 * jViewBox.
 * <p>
 * Supplementary information about the image format may be provided through
 * <code>ImageRenderingHints</code>, e.g., the default look-up table to apply
 * and pixel's aspect ratio. This is optional and hence an implementation may
 * simply return <code>null</code>. The same invariant constraint as above
 * applies, too.
 * <p>
 * Through this interface and an associated listener interface, jViewBox is
 * also able to support progress monitoring of pixel data loading/generating.
 * Within the <code>getBufferedImage</code> method, an implementation of
 * this interface may fire events to notify a <code>Viewport</code> or other
 * interested parties of pixel data loading progress. Specifically, an
 * implementation must act in one of the following ways per each call to
 * <code>getBufferedImage</code>:
 * <br><ul>
 * <li>Fires no event at all. This usually implies the pixel data is currently
 *     cached and therefore may be returned immediately.
 * </li>
 * <li>Fires exactly one <code>imageStarted</code> event at the beginning, one
 *     <code>imageCompleted</code> event at the end, and zero or more
 *     <code>imageProgress</code> events in between.
 * </li></ul>
 * An implementation is <b>NOT</b> allowed to fire any events outside
 * <code>getBufferedImage</code>, i.e., events may only be fired after
 * <code>getBufferedImage</code> is called and before it returns.
 *
 * @see ProgressListener
 *
 * @version January 8, 2004
 */
public interface ImageSource
{
    /**
     * Returns the width of the image in this source, in pixels. An
     * implementing class should only perform the minimum amount of work
     * required to retrieve the width information. The returned value must
     * remain the same throughout the lifetime of an instance.
     *
     * @return Width of the image in this source in pixels.
     *
     * @throws IOException if I/O error occurs reading the width information.
     */
    public int getWidth() throws IOException;

    /**
     * Returns the height of the image in this source, in pixels. An
     * implementing class should only perform the minimum amount of work
     * required to retrieve the height information. The returned value must
     * remain the same throughout the lifetime of an instance.
     *
     * @return Height of the image in this source in pixels.
     *
     * @throws IOException if I/O error occurs reading the height information.
     */
    public int getHeight() throws IOException;

    /**
     * Returns the <code>ColorModel</code> of the image in this source. An
     * implementing class should only perform the minimum amount of work
     * required to retrieve the color information. The returned value must
     * remain consistent throughout the lifetime of an instance.
     *
     * @return <code>ColorModel</code> of the image in this source.
     *
     * @throws IOException if I/O error occurs reading the color information.
     */
    public ColorModel getColorModel() throws IOException; 

    /**
     * Returns the <code>SampleModel</code> of the image in this source. An
     * implementing class should only perform the minimum amount of work
     * required to retrieve this information. The returned value must remain
     * the consistent throughout the lifetime of an instance.
     *
     * @return <code>SampleModel</code> of the image in this source.
     *
     * @throws IOException if I/O error occurs reading this information.
     */
    public SampleModel getSampleModel() throws IOException;

    /**
     * Returns the image pixels in this source as a <code>BufferedImage</code>.
     * The returned <code>BufferedImage</code> must be consistent with the
     * dimension and format as specified by the other <code>get</code>
     * methods.
     * <p>
     * As of jViewBox 2.0b, caching of <code>BufferedImage</code> in the
     * implementation of <code>ImageSource</code> is discontinued and
     * discouraged, for <code>ImageSourceViewport</code> caches the image, too.
     *
     * @return Image in this source as a <code>BufferedImage</code>.
     *
     * @throws IOException if I/O error occurs reading the pixel data.
     *
     * @see #flush
     */
    public BufferedImage getBufferedImage() throws IOException;

    /**
     * Returns hints for rendering the image in this source, e.g., aspect ratio
     * of non-squre pixels, default look-up table, etc. An implementing class
     * should only perform the minimum amount of work required to retrieve the
     * relevent information. The returned value must remain consistent
     * throughout the lifetime of an instance. <code>null</code> may be
     * returned if no hint is available.
     *
     * @return <code>ImageRenderingHints</code> which contains the hints; or
     *	       <code>null</code> if no hint is available.
     *
     * @throws IOException if I/O error occurs reading this information.
     */
    public ImageRenderingHints getImageRenderingHints() throws IOException;

    /**
     * Flush all disposable resource used by this ImageSource. By disposable,
     * it means anything that can be dynamically loaded or generated and hence
     * does not need to be possessed by the program at all time. For example,
     * pixel data loaded into memory from an image file.
     *
     * @deprecated As of jViewBox 2.0b, caching of <code>BufferedImage</code>
     *		   is discontinued and discouraged.
     */
    public void flush();

    /**
     * Adds an <code>ProgressListener</code> to this ImageSource. All
     * registered listeners get notified of progess made in
     * <code>getBufferedImage</code>. An implementing class may ignore this
     * operation (i.e., do nothing) if it never fires events. It is not
     * allowed to throw exceptions.
     *
     * @param listener ProgressListener to add to this source. This method does
     *		       nothing and throws no exception if
     *		       <code>listener</code> is <code>null</code>.
     *
     * @see #removeProgressListener
     */
    public void addProgressListener(ProgressListener listener);

    /**
     * Unregister an <code>ProgressListener</code> from this ImageSource. An
     * implementing class may ignore this operation (i.e., do nothing) if it
     * is never fires events. It is not allowed to throw exceptions.
     *
     * @param listener ProgressListener to remove from this source. This method
     *		       does nothing and throws no exception if
     *		       <code>listener</code> is <code>null</code>.
     *
     * @see #addProgressListener
     */
    public void removeProgressListener(ProgressListener listener);
}
