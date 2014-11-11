/*
 * @(#)WindowLevelTool.java		1.10 07/01/20
 *
 * ChargedFluid package
 *
 * COPYRIGHT NOTICE
 * Copyright (c) 2007 Herbert H.H. Chang, Daniel J. Valentino, Gary R. Duckwiler, and Arthur W. Toga
 * Laboratory of Neuro Imaging, Department of Neurology, UCLA.
 */

package tools;

import java.awt.event.MouseEvent;
import org.medtoolbox.jviewbox.LinearLookUpTable;
import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;

/**
 * Tool for changing the window and level values of an image inside a
 * Viewport.  When the mouse cursor is pressed and dragged to the left on
 * an image, the level value of the image is decreased.  If instead the
 * mouse is moved to the right, the level value is increased.  If the
 * mouse cursor is pressed and dragged upwards on an image, the window
 * value of the image is decreased.  If instead the mouse is moved downwards,
 * the window value is increased.
 */
public class WindowLevelTool extends ViewportTool
{
    /**
     * Number of pixels the mouse cursor must move to change the window or
     * level by 1.
     */
    private double _windowLevelSensitivity;

    /** Current window value. */
    private int _currentWindow;

    /** Current level value. */
    private int _currentLevel;

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

    /** 
     * LinearLookUpTable of the Viewport the mouse cursor is in when a
     * mouse button is pressed.
     */
    private LinearLookUpTable _vll = null;

    /** Constructs a WindowLevelTool with default settings. */
    public WindowLevelTool()
    {
	this(1);
    }

    /**
     * Constructs a WindowLevelTool with the specified mouse window/level
     * sensitivity.
     *
     * @param windowLevelSensitivity Number of pixels the mouse cursor must
     *        move to change the window or level by 1.
     *
     * throws IllegalArgumentException If the window/level sensitivity
     *                                 is invalid.
     */
    public WindowLevelTool(double windowLevelSensitivity)
    {
	super("Window/Level Tool", "Window/Level an Image", "", "");

	if (windowLevelSensitivity <= 0) {
	    throw new IllegalArgumentException("WindowLevelTool: A window/" +
					       "level sensitivity of " +
					       windowLevelSensitivity +
					       " is not allowed in the " +
					       "constructor.");
	}

	_windowLevelSensitivity = windowLevelSensitivity;
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
	// Determine if there is a Linear LUT to window/level
	if (vp != null && vp.getLut() instanceof LinearLookUpTable) {
	    _vp = vp;

	    // Save the current state
	    vp.setCurrentState( vp.getCurrentState() );

	    // Set the anchor and current window/level values
	    _anchorX = e.getX();
	    _anchorY = e.getY();

	    _vll = (LinearLookUpTable)vp.getLut();
	    _currentWindow = _vll.getWindow();
	    _currentLevel = _vll.getLevel();

	}
	else {
	    _vll = null;
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
	if (_vll != null && _vp != null && vpc != null) {

	    // Set the new window and level values
	    double window = _currentWindow + 
		(double)(e.getY() - _anchorY)/_windowLevelSensitivity;

	    double level = _currentLevel + 
		(double)(e.getX() - _anchorX)/_windowLevelSensitivity;

	    _vll.setWindowLevel( (int)window, (int)level );

	    vpc.repaint(_vp);

	}
    }

}
