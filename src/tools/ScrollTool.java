/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package tools;

import java.awt.event.MouseEvent;
import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportGrid;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;

/**
 * Tool for scrolling through the Viewports of a ViewportCluster.
 * <p>
 * When the mouse cursor is pressed and dragged downward on a ViewportCluster,
 * the ViewportCluster's offset is increased to bring succeeding Viewports in
 * the sequence into display. If the mouse is dragged upward instead, the
 * offset is decreased to bring preceding Viewports in the sequence into
 * display. If the mouse is simply clicked (pressed and released without being
 * dragged), the ViewportCluster is scrolled forward by one unit on a left
 * button click and backward by one unit otherwise.
 *
 * @version January 8, 2004
 */
public class ScrollTool extends ViewportTool
{
    /** Vertical mouse movement in pixels that translates to 1 scroll unit. */
    private int _scrollSensitivity;

    /** Baseline Y coordinate used to calculate the amount to scroll. */
    private int _anchorY;

    /** ViewportCluster being scrolled. */
    private ViewportCluster _vpc;

    /** Constructs a ScrollTool with default settings. */
    public ScrollTool()
    {
	this(20);
    }

    /**
     * Constructs a ScrollTool with the specified mouse scroll sensitivity.
     *
     * @param scrollSensitivity Vertical mouse movement in pixels that
     *				translates to 1 scroll unit.
     *
     * @throws IllegalArgumentException If the scroll sensitivity is invalid.
     */
    public ScrollTool(int scrollSensitivity)
    {
	super("Scroll Tool", "Scroll Through Images", "", "");

	// Check bounds
	if (scrollSensitivity <= 0) {
	    throw new IllegalArgumentException("ScrollTool: A scroll " +
					       "sensitivity of " +
					       scrollSensitivity + " is not " +
					       "allowed in the constructor.");
	}

	_scrollSensitivity = scrollSensitivity;
    }

    /**
     * Invoked when a mouse button is pressed on a ViewportCluster.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event;
     *		 <code>null</code> if the mouse was outside all visible
     *		 Viewports in the ViewportCluster.
     * @param e Event created by the button press.
     * @param button The button that was pressed.
     */
    public void mousePressed(ViewportCluster vpc, Viewport vp, MouseEvent e, 
			     int button)
    {
	_vpc = null;

	// Set the anchor
	if (vpc != null) {
	    _vpc = vpc;
	    _anchorY = e.getY();
	}
    }

    /**
     * Invoked when a mouse button is pressed and then the mouse is dragged
     * on a ViewportCluster.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event;
     *		 <code>null</code> if the mouse was outside all visible
     *		 Viewports in the ViewportCluster.
     * @param e Event created by the button drag.
     * @param button The button that was pressed and dragged.
     */
    public void mouseDragged(ViewportCluster vpc, Viewport vp, MouseEvent e, 
			     int button)
    {
	if (_vpc != null) {

	    // Determine how much to scroll
	    int scroll = (e.getY() - _anchorY) / _scrollSensitivity;

	    if (scroll != 0) {
		// Reset the anchor
		_anchorY = e.getY();

		// Scroll
		if (_vpc instanceof ViewportGrid) {
		    ((ViewportGrid)_vpc).gridScroll(scroll);
		}
		else {
		    _vpc.scroll(scroll);
		}

		// Repaint the ViewportCluster
		vpc.repaint();
	    }
	}
    }

    /**
     * Invoked when a mouse button is clicked (pressed and released without
     * movement in between) on a ViewportCluster.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event;
     *		 <code>null</code> if the mouse was outside all visible
     *		 Viewports in the ViewportCluster.
     * @param e Event created by the button click.
     * @param button The button that was clicked.
     */
    public void mouseClicked(ViewportCluster vpc, Viewport vp, MouseEvent e, 
			     int button)
    {
	if (vpc != null) {
	    // Scroll forward for the left button, backward otherwise
	    int scroll = (button == LEFT_MOUSE_BUTTON) ? +1 : -1;

	    // Scroll
	    if (vpc instanceof ViewportGrid) {
		((ViewportGrid)vpc).gridScroll(scroll);
	    }
	    else {
		vpc.scroll(scroll);
	    }

	    // Repaint the ViewportCluster
	    vpc.repaint();
	}
    }
}
