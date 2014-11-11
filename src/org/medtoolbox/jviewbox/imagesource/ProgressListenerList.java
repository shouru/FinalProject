/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.imagesource;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * A convenience class for the management of <code>ProgressListener</code>s.
 * This class maintains a list of listeners, handles the addition and removal
 * of listeners and broadcasts events to all listeners in the list.
 *
 * @version January 8, 2004
 */
public class ProgressListenerList
{
    // --------------
    // Private fields
    // --------------

    /** List of ProgressListeners. */
    private final List _listeners = new Vector(1, 1);

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs an initially empty ProgressListenerList.
     */
    public ProgressListenerList()
    {
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Adds an <code>ProgressListener</code> to this list.
     *
     * @param listener ProgressListener to add to this list. This method does
     *		       nothing and throws no exception if
     *		       <code>listener</code> is <code>null</code>.
     *
     * @see #remove
     */
    public synchronized void add(ProgressListener listener)
    {
	if (listener == null) {
	    return;
	}

	// Disallow duplicate
	synchronized (_listeners) {
	    if (!_listeners.contains(listener)) {
		_listeners.add(listener);
	    }
	}
    }

    /**
     * Removes an <code>ProgressListener</code> from this list.
     *
     * @param listener ProgressListener to remove from this list. This method
     *		       does nothing and throws no exception if
     *		       <code>listener</code> is <code>null</code>.
     *
     * @see #add
     */
    public synchronized void remove(ProgressListener listener)
    {
	if (listener == null) {
	    return;
	}

	synchronized (_listeners) {
	    _listeners.remove(listener);
	}
    }

    /**
     * Returns an <b>unmodifiable</b> <code>List</code> of
     * <code>ProgressListener</code>s in this list.
     *
     * @return Unmodifiable <code>List</code> of <code>ProgressListener</code>s
     *	       in this list.
     */
    public List getListeners()
    {
	return Collections.unmodifiableList(_listeners);
    }

    /**
     * Fires an <code>imageStarted</code> event to all
     * <code>ProgressListener</code>s in this list.
     *
     * @param source <code>ImageSource</code> instance firing the event.
     *
     * @throws NullPointerException if <code>source</code> is
     *	       <code>null</code>.
     */
    public void fireImageStarted(ImageSource source)
    {
	if (source == null) {
	    throw new NullPointerException("source can not be null.");
	}

	synchronized (_listeners) {
	    for (Iterator it = _listeners.iterator(); it.hasNext(); ) {
		((ProgressListener)it.next()).imageStarted(source);
	    }
	}
    }

    /**
     * Fires an <code>imageComplete</code> event to all
     * <code>ProgressListener</code>s in this list.
     *
     * @param source <code>ImageSource</code> instance firing the event.
     *
     * @throws NullPointerException if <code>source</code> is
     *	       <code>null</code>.
     */
    public void fireImageComplete(ImageSource source)
    {
	if (source == null) {
	    throw new NullPointerException("source can not be null.");
	}

	synchronized (_listeners) {
	    for (Iterator it = _listeners.iterator(); it.hasNext(); ) {
		((ProgressListener)it.next()).imageComplete(source);
	    }
	}
    }

    /**
     * Fires an <code>imageProgress</code> event to all
     * <code>ProgressListener</code>s in this list.
     *
     * @param source <code>ImageSource</code> instance firing the event.
     * @param percentage Percentage (a number between 0 and 100 inclusive) of
     *			 loading that has been completed; a negative number
     *			 if it is not available.
     *
     * @throws NullPointerException if <code>source</code> is
     *	       <code>null</code>.
     */
    public void fireImageProgress(ImageSource source, float percentage)
    {
	if (source == null) {
	    throw new NullPointerException("source can not be null.");
	}

	synchronized (_listeners) {
	    for (Iterator it = _listeners.iterator(); it.hasNext(); ) {
		((ProgressListener)it.next()).imageProgress(source,
							    percentage);
	    }
	}
    }
}
