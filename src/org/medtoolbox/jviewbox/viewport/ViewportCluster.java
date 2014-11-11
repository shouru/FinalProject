/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import javax.swing.JComponent;

/**
 * Class which manages the Viewports of a group of displayed images. It
 * extends <code>JComponent</code> and is meant to be the basic component
 * for Swing-based GUI imaging applications.
 * <p>
 * Viewports are arranged and displayed inside the ViewportCluster using
 * "Viewport windows." These are <code>Rectangle</code>s that define the
 * display bounds of Viewports in the ViewportCluster. The number of Viewport
 * windows is not dependent upon the number of Viewports; there can be
 * any non-zero number of each.
 * <p>
 * The Viewports and the Viewport windows are ordered in linear arrays.
 * The Viewport array is conceptually allowed to shift with respect to the
 * Viewport window array.  An "offset" describes the amount of this shift;
 * for example, if there are 4 Viewport windows and 3 Viewports in a
 * ViewportCluster, and if the offset is equal to 1:
 * <pre>
 *                        Window 1     Window 2     Window 3     Window 4
 *
 * <--(+)   Viewport 1   Viewport 2   Viewport 3  (-)--></pre>
 *
 * This implies that Viewport 2 will be displayed in Window 1, Viewport 3
 * will be displayed in Window 2, Windows 3 and 4 will have no Viewport
 * displayed in them, and Viewport 1 will not be displayed in the
 * ViewportCluster. Changing the offset is said to "scroll" the Viewports
 * in the ViewportCluster.
 * <p>
 * To implement a concrete ViewportCluster subclass, the programmer has to
 * provide an implementation for {@link #_getViewportWindows} which defines
 * the geometry of Viewport windows. {@link ViewportGrid} is such a class
 * in which Viewports are displayed in a rectangular grid of square Viewport
 * windows.
 *
 * @see Viewport
 * @see ViewportGrid
 *
 * @version January 8, 2004
 */
public abstract class ViewportCluster extends JComponent
{
    // ----------------
    // Protected fields
    // ----------------

    /** Viewports in the ViewportCluster. */
    protected final Vector _viewports;

    /** Offset of the first Viewport from the first Viewport window. */
    protected int _offset = 0;

    // --------------
    // Private fields
    // --------------

    /** Whether to bypass RepaintManager in repaint(Viewport). */
    private static boolean _bypassingRepaintManager = false;

    /** Background color of this ViewportCluster. */
    private Color _background = Color.black;


    /**
     * A MouseListener which acquires input focus when the Component is
     * clicked on.
     */
    private static final MouseListener _FOCUS_GRABBER = new FocusGrabber();

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs a ViewportCluster out of the specified <code>List</code> of
     * images.
     *
     * @param images <code>List</code> of images, usually instances of
     *		     <code>java.awt.Image</code> or <code>ImageSource</code>.
     */
    protected ViewportCluster(List images)
    {
	this(ViewportFactory.createViewports(images));
    }

    /**
     * Constructs a ViewportCluster out of the specified array of
     * <code>Viewport</code>s.
     *
     * @param viewports Array of <code>Viewport</code>s to construct a
     *			ViewportCluster out of; <code>null</code> would result
     *			in an empty ViewportCluster.
     */
    protected ViewportCluster(Viewport[] viewports)
    {
	if (viewports != null) {
	    _viewports = new Vector(Arrays.asList(viewports));
	}
	else {
	    _viewports = new Vector(1, 1);
	}

	// Make sure this ViewportCluster is focusable
	setFocusable(true);

	// Make sure this ViewportCluster gain focus when clicked on
	addMouseListener(_FOCUS_GRABBER);
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the Viewports of the ViewportCluster in a <code>List</code>.
     * The returned list is itself unmodifiable but the Viewports in it may
     * be operated on.
     *
     * @return Viewports of the ViewportCluster in a <code>List</code>.
     */
    public List getViewports()
    {
	return Collections.unmodifiableList(_viewports);
    }

    /**
     * Gets the Viewport which contains the Point.
     *
     * @param point Point in the ViewportCluster.
     *
     * @return Viewport on which the Point lies, or null if there isn't such
     *         a Viewport.
     */
    public Viewport getViewport(Point point)
    {
	// Search the Viewport windows to see if one contains the point
	for (Iterator it = _getViewportWindows().iterator(); it.hasNext(); ) {
	    Rectangle window = (Rectangle)it.next();
	    if (window.contains(point)) {
		// Return the viewport in the window
		return _getViewportInWindow(window);
	    }
	}

	return null;
    }

    /**
     * Scrolls the Viewports through the ViewportCluster. Viewport locations
     * inside the ViewportCluster are exchanged so that some Viewports are
     * hidden in order to display some previously non-visible Viewports.
     * Scrolling is restricted such that there is always at least one Viewport
     * visible inside the ViewportCluster.
     *
     * @param scrollNumber Number of Viewports to scroll by.
     */
    public void scroll(int scrollNumber)
    {
	scroll(scrollNumber, 1);
    }

    /**
     * Scrolls the Viewports through the ViewportCluster by a fixed number of
     * Viewports. Scrolling is restricted such that no scroll will occur if
     * such an action would result in no Viewport being visible in the
     * ViewportCluster.
     *
     * @param scrollNumber Number of times to change the scroll.
     * @param scrollSize Number of Viewports to change the scroll by each time.
     */
    public void scroll(int scrollNumber, int scrollSize)
    {
	List viewportWindows = _getViewportWindows();
	while (scrollNumber != 0) {
	    int newOffset = _offset + scrollNumber * scrollSize;

	    // Scroll too large, reduce scroll number and try again
	    if (newOffset >= _viewports.size()) { scrollNumber--; }

	    // Scroll too small, increase scroll number and try again
	    else if (newOffset <= -viewportWindows.size()) { scrollNumber++; }

	    // Scroll not restricted
	    else {
		_offset = newOffset;
		return;
	    }
	}
    }

    /**
     * Returns the current offset by which the Viewport sequence is shifted
     * with respect to the Viewport Window sequence.
     *
     * @return Current offset by which the Viewport sequence is shifted with
     *	       respect to the Viewport Window sequence.
     */
    public int getOffset()
    {
	return _offset;
    }

    /**
     * Scrolls the Viewports through the ViewportCluster by pages.  A page is
     * defined as the maximum number of Viewports that may be displayed in a
     * ViewportCluster at once. Scrolling is restricted such that no scroll
     * will occur if such an action would result in no Viewport being visible
     * in the ViewportCluster.
     *
     * @param pageNumber Number of pages (maximum number of Viewports that
     *                   may be displayed in a ViewportCluster at once) to
     *                   scroll by.
     */
    public void pageScroll(int pageNumber)
    {
	scroll(pageNumber, _getViewportWindows().size());
    }

    /**
     * Reverses the order of the Viewports in the ViewportCluster around the
     * specified Viewport.  Viewports will then be displayed by reversing
     * their locations inside the ViewportCluster about the location of the
     * specified Viewport.
     *
     * @param pivotViewport Viewport used as the pivot point about which the
     *                      Viewports are reversed.
     */
    public void reverseViewportOrder(Viewport pivotViewport)
    {
	// Viewport must be part of the ViewportCluster
	int index = _viewports.indexOf(pivotViewport);
	if (index < 0) {
	    return;
	}

	// Reverse the Viewports Vector
	Collections.reverse(_viewports);

	// Adjust the offset by the difference between the pivot Viewport's
	// new position in the reversed Vector and its old position
	_offset += (_viewports.size() - 1 - index) - index;
    }

    /**
     * Convenience method for enabling/disabling annotations in all Viewports
     * in this ViewportCluster. This method simply calls all Viewports'
     * <code>setAnnotationEnabled</code> one by one to set their states and
     * does not keep track of the states afterward.
     *
     * @param enabled <code>true</code> to enable annotations in all Viewports
     *		      in this ViewportCluster; <code>false</code> to disable.
     */
    public void setAnnotationEnabled(boolean enabled)
    {
	for (Iterator it = _viewports.iterator(); it.hasNext(); ) {
	    ((Viewport)it.next()).setAnnotationEnabled(enabled);
	}
    }

    /**
     * Flushes all the resources being used by the ViewportCluster. This
     * includes any cached data for rendering to the screen and any system
     * resources that are being used to store image pixel data.
     */
    public void flush()
    {
	// Flush all of the Viewports in the ViewportCluster
	for (Iterator it = getViewports().iterator(); it.hasNext(); ) {
	    Viewport vp = (Viewport)it.next();
	    vp.flush();
	}
    }

    /**
     * Flushes only the resources being used by the ViewportCluster to display
     * the images.  This includes any cached data for rendering to the screen
     * but does not include any system resources that are being used to store
     * image pixel data.
     */
    public void flushDisplayOnly()
    {
	// Flush all of the Viewports in the ViewportCluster
	for (Iterator it = getViewports().iterator(); it.hasNext(); ) {
	    Viewport vp = (Viewport)it.next();
	    vp.flushDisplayOnly();
	}
    }



    /**
     * Registers the specified {@link ViewportToolBar} in this
     * ViewportCluster and hooks up its event listeners for the toolbar to
     * function on this ViewportCluster.
     *
     * @param toolbar <code>ViewportToolBar</code> to register.
     */
    public void registerViewportToolBar(ViewportToolBar toolbar)
    {
	addMouseListener(toolbar.getMouseListener());
	addMouseMotionListener(toolbar.getMouseMotionListener());
	addKeyListener(toolbar.getKeyListener());
	addMouseWheelListener(toolbar.getMouseWheelListener());
    }

    /**
     * Unregisters the specified {@link ViewportToolBar} in this
     * ViewportCluster and removes its event listeners from this
     * ViewportCluster.
     *
     * @param toolbar <code>ViewportToolBar</code> to unregister.
     */
    public void unregisterViewportToolBar(ViewportToolBar toolbar)
    {
	removeMouseListener(toolbar.getMouseListener());
	removeMouseMotionListener(toolbar.getMouseMotionListener());
	removeKeyListener(toolbar.getKeyListener());
	removeMouseWheelListener(toolbar.getMouseWheelListener());
    }

    /**
     * Sets whether to bypass <code>RepaintManager</code> in
     * <code>repaint(Viewport)</code>. This setting applies to <b>ALL</b>
     * <code>ViewportCluster</code>s. The default is not.
     * <p>
     * For slow machines, especially if running Java 1.3, enabling this
     * option may improve the painting performance, at the cost of heavier
     * flickering since Swing's double buffering is also bypassed. On fast
     * machines, especially if running Java 1.4, enabling this option has
     * either negligible benefits or sometimes adverse effects on performance.
     * Therefore, it is recommended to turning on this option only if you do
     * encounter performance problem.
     * <p>
     * Flickering caused by enabling this option may be partially reduced by
     * setting {@link Viewport#setPostErasingBackground} to <code>true</code>.
     * <p>
     * Specifically, enabling this option instructs
     * {@link #repaint(Viewport)} to get a <code>Graphics</code> instance
     * using <code>JComponent.getGraphics</code> and use it for the painting
     * of the Viewport, instead of invoking <code>repaint(Rectangle)</code>
     * and letting Swing handle the painting through
     * <code>RepaintManager</code>.
     * <p>
     * As one of the side effects, {@link #repaint(Viewport)} becomes
     * <b>thread-unsafe</b> if this option is enabled.
     *
     * @param bypassingRepaintManager <code>true</code> to bypass
     *				      <code>RepaintManager</code> in
     *				      <code>repaint(Viewport)</code>;
     *				      <code>false</code> to not to.
     *
     * @see #isBypassingRepaintManager
     * @see Viewport#setPostErasingBackground
     */
    public static void
	setBypassingRepaintManager(boolean bypassingRepaintManager)
    {
	_bypassingRepaintManager = bypassingRepaintManager;
    }

    /**
     * Returns whether <code>RepaintManager</code> is bypassed in
     * <code>repaint(Viewport)</code>. This setting applies to <b>ALL</b>
     * <code>ViewportCluster</code>s.
     *
     * @return <code>true</code> if <code>RepaintManager</code> is bypassed in
     *	       <code>repaint(Viewport)</code>; <code>false</code> if not.
     *
     * @see #setBypassingRepaintManager
     */
    public static boolean isBypassingRepaintManager()
    {
	return _bypassingRepaintManager;
    }

    // -------------
    // Paint methods
    // -------------

    /**
     * Paints the Viewports inside the ViewportCluster which need updating.
     * The region needing updating is determined by the clip bounds of the
     * Graphics. This method is called by the system when events requiring
     * updates occur.
     *
     * @param g Graphics window to display in.
     */
    protected void paintComponent(Graphics g)
    {
	Graphics2D g2d = (Graphics2D)g;

	// Save the original clip
	Shape origClip = g2d.getClip();

	// Limit painting to area inside insets
	Rectangle clipBounds = _getInnerBounds();
	g2d.clipRect(clipBounds.x, clipBounds.y,
		     clipBounds.width, clipBounds.height);
	clipBounds = g2d.getClipBounds();

	// Always erase background
	Color c = g2d.getBackground();
	g2d.setBackground(_background);
	g2d.clearRect(clipBounds.x, clipBounds.y,
		      clipBounds.width, clipBounds.height);
	g2d.setBackground(c);

	// Determine which windows need updating
	for (ListIterator it = _getViewportWindows().listIterator();
	     it.hasNext(); ) {

	    Rectangle window = (Rectangle)it.next();

	    // Window needs updating if it overlaps the clip bounds
	    if (clipBounds.intersects(window)) {
		Viewport vp = _getViewportInWindow(window);

		// Paint the Viewport in the window if it exists
		if (vp != null) { 
		    // Temporarily restrict the output to Viewport's bounds
		    Shape clip = g2d.getClip();
		    g2d.clipRect(window.x, window.y,
				 window.width, window.height);

		    vp.setBounds(window);
		    vp.paint(g2d, false);

		    // Restore the clip bounds
		    g2d.setClip(clip);
		}
	    }
	}

	// Restore the original clip
	g2d.setClip(origClip);
    }

    /**
     * Repaints only the specified Viewport inside the ViewportCluster.
     *
     * @param viewport Viewport to repaint inside the ViewportCluster.
     */
    public void repaint(Viewport viewport)
    {
	// Determine the window of the Viewport
	Rectangle window = _getWindowForViewport(viewport);

	// Is viewport visible?
	if (window != null) {
	    // Bypass RepaintManager?
	    if (isBypassingRepaintManager()) {
		Graphics2D g2d = (Graphics2D)getGraphics();
		g2d.clipRect(window.x, window.y, window.width, window.height);
		viewport.paint(g2d, true);
		g2d.dispose();
	    }

	    // No, call normal repaint(Rectangle)
	    else {
		repaint(window);
	    }
	}
    }


    // -----------------
    // Protected methods
    // -----------------

    /**
     * Returns the rectangles which define the Viewport Windows in a
     * <code>List</code>. The returned list and all elements in the list
     * should be deemed as unmodifiable constants even though overwriting is
     * not explicitly prohibited.
     * <p>
     * Any implementing subclass must override this method to define its own
     * Viewport Window geometry. The implementation must take the insets into
     * consideration, i.e., arrange Viewport Windows within the insets.
     * Otherwise, part of the image display may be blocked by the borders of
     * this ViewportCluster. {@link #_getInnerBounds} is a convenient method
     * for figuring out the drawable region inside the insets.
     *
     * @return <code>List</code> of <code>java.awt.Rectangle</code>s which
     *	       define the Viewport Windows.
     */
    protected abstract List _getViewportWindows();

    /**
     * Gets the Viewport currently in the Viewport window.
     *
     * @return Viewport currently displayed in the Viewport window, or null if
     *         there isn't a Viewport in the window.
     */
    protected Viewport _getViewportInWindow(Rectangle viewportWindow)
    {
	List viewportWindows = _getViewportWindows();
	int windowIndex = viewportWindows.indexOf(viewportWindow);
	if (windowIndex >= 0) {
	    // Viewport index is the window index plus the offset
	    int viewportIndex = windowIndex + _offset;

	    if (viewportIndex >= 0 && viewportIndex < _viewports.size()) {
		return (Viewport)_viewports.get(viewportIndex);
	    }
	}

	return null;
    }

    /**
     * Gets the current Viewport window for the Viewport.
     *
     * @return Viewport window inside of which the Viewport is currently
     *         displayed, or null if there isn't a Viewport window for the
     *         Viewport.
     */
    protected Rectangle _getWindowForViewport(Viewport viewport)
    {
	int viewportIndex = _viewports.indexOf(viewport);
	if (viewportIndex >= 0) {
	    List viewportWindows = _getViewportWindows();
	    // Window index is the viewport index minus the offset
	    int windowIndex = viewportIndex - _offset;

	    if (windowIndex >= 0 && windowIndex < viewportWindows.size()) {
		return (Rectangle)viewportWindows.get(windowIndex);
	    }
	}

	return null;
    }

    /**
     * Returns the bounds for the inner client area in this ViewportCluster,
     * i.e., this ViewportCluster minus the insets.
     *
     * @return Bounds for the inner client area in this ViewportCluster,
     *	       i.e., this ViewportCluster minus the insets.
     */
    protected Rectangle _getInnerBounds()
    {
	Rectangle r = new Rectangle(getSize());
	Insets insets = getInsets();
	r.x += insets.left;
	r.y += insets.top;
	r.width -= insets.left + insets.right;
	r.height -= insets.top + insets.bottom;

	// What if insets take more than what the cluster has?
	if (r.width < 0) {
	    r.width = 0;
	}
	if (r.height < 0) {
	    r.height = 0;
	}

	return r;
    }

    // --------------------
    // Private member class
    // --------------------

    /**
     * A MouseListener which acquires input focus when the Component is
     * clicked on.
     */
    private static class FocusGrabber extends MouseAdapter
    {
	/** Tries to grab the focus when clicked on. */
	public void mouseClicked(MouseEvent e)
	{
	    Component c = e.getComponent();
	    if (c != null && !c.isFocusOwner()) {
		c.requestFocusInWindow();
	    }
	}
    }
}
