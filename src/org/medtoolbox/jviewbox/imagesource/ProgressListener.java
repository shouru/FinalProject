/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.imagesource;

import java.util.EventListener;

/**
 * The listener interface for receiving progress events from an instance of
 * <code>ImageSource</code>.
 *
 * @version January 8, 2004
 */
public interface ProgressListener extends EventListener
{
    /**
     * Reports that the image loading is starting.
     * <p>
     * An <code>ImageSource</code> implementation must fire exactly one of
     * this event and one corresponding <code>imageComplete</code> per one
     * call to <code>getBufferedImage</code> if it elects to fire any events
     * for that call. It is permitted to fire no events at all.
     *
     * @param source <code>ImageSource</code> instance calling this method.
     *
     * @see #imageComplete
     */
    public void imageStarted(ImageSource source);

    /**
     * Reports that the image loading has complete.
     * <p>
     * An <code>ImageSource</code> implementation must fire exactly one 
     * corresponding <code>imageStarted</code> and one of this event per one
     * call to <code>getBufferedImage</code> if it elects to fire any events
     * for that call. It is permitted to fire no events at all.
     *
     * @param source <code>ImageSource</code> instance calling this method.
     *
     * @see #imageStarted
     */
    public void imageComplete(ImageSource source);

    /**
     * Reports that the image loading is in progress and (optionally) the
     * percentage of completion.
     * <p>
     * An <code>ImageSource</code> implementation may only fire this event
     * between <code>imageStarted</code> and <code>imageComplete</code>. It
     * may fire this event zero or more times.
     *
     * @param source <code>ImageSource</code> instance calling this method.
     * @param percentage Percentage (a number between 0 and 100 inclusive) of
     *			 loading that has been completed; a negative number
     *			 if it is not available.
     */
    public void imageProgress(ImageSource source, float percentage);
}
