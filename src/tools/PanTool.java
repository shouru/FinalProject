/*
 * @(#)PanTool.java		1.10 07/01/20
 *
 * ChargedFluid package
 *
 * COPYRIGHT NOTICE
 * Copyright (c) 2007 Herbert H.H. Chang, Daniel J. Valentino, Gary R. Duckwiler, and Arthur W. Toga
 * Laboratory of Neuro Imaging, Department of Neurology, UCLA.
 */

package tools;

import java.awt.event.MouseEvent;
import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;

/**
 * Tool for panning an image inside a Viewport.  When the mouse cursor is 
 * pressed and dragged on an image in a Viewport, the image moves along with
 * the cursor until the mouse button is released.
 */
public class PanTool extends ViewportTool
{
    /** 
     * X coordinate of the mouse cursor when a mouse button is pressed inside
     * a ViewportCluster.
     */
    private int _anchorX;

    /** 
     * Y coordinate of the mouse cursor when a mouse button is pressed inside
     * a ViewportCluster.
     */
    private int _anchorY;

    /**
     * Viewport the mouse cursor is in when a mouse button is pressed.
     */
    private Viewport _vp = null;

    /** Constructs a PanTool. */
    public PanTool()
    {
	super("Pan Tool", "Pan an Image", "", "");
    }


    /**
     * Invoked when a mouse button is pressed on a ViewportCluster.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event.
     * @param e Event created by the button press.
     * @param button The button that was pressed.
     */
    public void mousePressed(ViewportCluster vpc, Viewport vp, MouseEvent e, 
			     int button)
    {
	_vp = vp;

	// Set the anchor
	if (_vp != null) {
	    _anchorX = e.getX() - _vp.getPanX();
	    _anchorY = e.getY() - _vp.getPanY();

	    // Save the current state
	    _vp.setCurrentState( _vp.getCurrentState() );

	}
    }

    /**
     * Invoked when a mouse button is pressed and then the mouse is dragged
     * on a ViewportCluster.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event.
     * @param e Event created by the button drag.
     * @param button The button that was pressed and dragged.
     */
    public void mouseDragged(ViewportCluster vpc, Viewport vp, MouseEvent e, 
			     int button)
    {
	if (_vp != null && vpc != null) {

	    // Set the new pan
	    int panX = e.getX() - _anchorX;
	    int panY = e.getY() - _anchorY;
	    _vp.setPan(panX, panY);

	    vpc.repaint(_vp);

	}
    }


}
