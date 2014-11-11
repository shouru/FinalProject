/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * Tool that performs image operations on Viewports within ViewportClusters
 * upon user input events.
 * <p>
 * To implement a tool, override the listener methods for the specific input
 * events that trigger the tool's operation. It is preferable to override
 * the <code>public</code> version of listener methods with a
 * <code>ViewportCluster</code> among the parameters. The
 * <code>protected</code> version of listener methods with no
 * <code>ViewportCluster</code> parameter has a default implementation
 * that identifies the originating <code>ViewportCluster</code> and
 * <code>Viewport</code> of an event and forwards the event to their
 * <code>public</code> couterparts.
 * <p>
 * Since individual Viewports do not receive key focus, <code>KeyEvent</code>s
 * can only be associated with an entire <code>ViewportCluster</code>.
 *
 * @version January 8, 2004
 */
public abstract class ViewportTool
{
    // ---------
    // Constants
    // ---------

    /**
     * No mouse button involved. Synonym of
     * <code>java.awt.event.MouseEvent.NOBUTTON</code> in Java 1.4. 
     */
    public static final int NO_MOUSE_BUTTON = MouseEvent.NOBUTTON;

    /**
     * The left-most mouse button. Synonym of
     * <code>java.awt.event.MouseEvent.BUTTON1</code> in Java 1.4.
     */
    public static final int LEFT_MOUSE_BUTTON = MouseEvent.BUTTON1;

    /**
     * The middle mouse button. Synonym of
     * <code>java.awt.event.MouseEvent.BUTTON2</code> in Java 1.4.
     */
    public static final int MIDDLE_MOUSE_BUTTON = MouseEvent.BUTTON2;

    /**
     * The right-most mouse button. Synonym of
     * <code>java.awt.event.MouseEvent.BUTTON3</code> in Java 1.4.
     */
    public static final int RIGHT_MOUSE_BUTTON = MouseEvent.BUTTON3;

    // --------------
    // Private fields
    // --------------

    /** Name of this tool. */
    private final String _toolName;

    /** Descriptions of this tool's functions. */
    private final String[] _functionDescs;

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs a ViewportTool with the specified name and functions. Creates
     * a formatted description of the Tool.
     *
     * @param toolName Name of the ViewportTool.
     * @param function Name of the function that is provided by the Tool
     *                 through any mouse button.
     */
    protected ViewportTool(String toolName, String function)
    {
	this(toolName, new String[] { "Any button: " + function });
    }

    /**
     * Constructs a ViewportTool with the specified name and functions. Creates
     * a formatted description of this Tool.
     *
     * @param toolName Name of this ViewportTool.
     * @param leftFunction Name of the function provided by this tool through
     *			   the left mouse button; <code>null</code> or "" if
     *			   this function does not exist.
     * @param middleFunction Name of the function provided by this tool through
     *			     the middle mouse button; <code>null</code> or ""
     *			     if this function does not exist.
     * @param rightFunction Name of the function provided by this tool through
     *			    the right mouse button; <code>null</code> or "" if
     *			    this function does not exist.
     */
    protected ViewportTool(String toolName, String leftFunction, 
			   String middleFunction, String rightFunction)
    {
	this(toolName,
	     _buildFunctionDesc(leftFunction, middleFunction, rightFunction));
    }

    /**
     * Constructs a ViewportTool with the specified name and functions.
     *
     * @param toolName Name of this ViewportTool.
     * @param functionDescs Descriptions of this ViewportTool's functions.
     */
    protected ViewportTool(String toolName, String[] functionDescs)
    {
	if (toolName == null) {
	    throw new NullPointerException("toolName can not be null.");
	}
	_toolName = toolName;

	if (functionDescs == null) {
	    throw new NullPointerException("functionDescs can not be null.");
	}
	for (int i = 0; i < functionDescs.length; i++) {
	    if (functionDescs[i] == null) {
		throw new NullPointerException("Elements of functionDescs " +
					       "can not be null.");
	    }
	}
	_functionDescs = functionDescs;
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the name of this ViewportTool.
     *
     * @return The name of this ViewportTool.
     */
    public String getToolName()
    {
	return _toolName;
    }

    /**
     * Returns the descriptions of this tool's functions.
     *
     * @return Descriptions of the functions of this tool.
     */
    public String[] getFunctionDescriptions()
    {
	return _functionDescs;
    }

    // -----------------------------------------------------------
    // Event callbacks to override for implementing tool functions
    // -----------------------------------------------------------

    /**
     * Invoked when a mouse button is clicked (pressed and released without
     * movement in between) on a ViewportCluster. Override this method to
     * implement tool functions triggered by the specific event.
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
    }

    /**
     * Invoked when the mouse cursor is moved onto a ViewportCluster without
     * any mouse buttons pressed. Override this method to implement tool
     * functions triggered by the specific event.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event;
     *		 <code>null</code> if the mouse was outside all visible
     *		 Viewports in the ViewportCluster.
     * @param e Event created by the cursor motion.
     */
    public void mouseEntered(ViewportCluster vpc, Viewport vp, MouseEvent e)
    {
    }

    /**
     * Invoked when the mouse cursor is moved off a ViewportCluster without
     * any mouse buttons pressed. Override this method to implement tool
     * functions triggered by the specific event.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event;
     *		 <code>null</code> if the mouse was outside all visible
     *		 Viewports in the ViewportCluster.
     * @param e Event created by the cursor motion.
     */
    public void mouseExited(ViewportCluster vpc, Viewport vp, MouseEvent e)
    {
    }

    /**
     * Invoked when a mouse button is pressed on a ViewportCluster. Override
     * this method to implement tool functions triggered by the specific event.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event;
     *		 <code>null</code> if the mouse was outside all visible
     *		 Viewports in the ViewportCluster.
     * @param e Event created by the button press.
     * @param button The button that was pressed.
     * @throws IOException 
     */
    public void mousePressed(ViewportCluster vpc, Viewport vp, MouseEvent e, 
			     int button) throws IOException
    {
    }

    /**
     * Invoked when a mouse button is released on a ViewportCluster. Override
     * this method to implement tool functions triggered by the specific event.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event;
     *		 <code>null</code> if the mouse was outside all visible
     *		 Viewports in the ViewportCluster.
     * @param e Event created by the button release.
     * @param button The button that was released.
     */
    public void mouseReleased(ViewportCluster vpc, Viewport vp, MouseEvent e, 
			      int button)
    {
    }

    /**
     * Invoked when a mouse button is pressed and then the mouse is dragged
     * on a ViewportCluster. Override this method to implement tool functions
     * triggered by the specific event.
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
    }

    /**
     * Invoked when the mouse cursor is moved on a ViewportCluster without
     * any mouse buttons pressed. Override this method to implement tool
     * functions triggered by the specific event.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event;
     *		 <code>null</code> if the mouse was outside all visible
     *		 Viewports in the ViewportCluster.
     * @param e Event created by the cursor motion.
     */
    public void mouseMoved(ViewportCluster vpc, Viewport vp, MouseEvent e)
    {
    }

    /**
     * Invoked when a keyboard key is pressed while the focus is on a
     * ViewportCluster. Override this method to implement tool functions
     * triggered by the specific event.
     *
     * @param vpc ViewportCluster which received the event.
     * @param e Event created by the key press.
     */
    public void keyPressed(ViewportCluster vpc, KeyEvent e)
    {
    }

    /**
     * Invoked when a keyboard key is released while the focus is on a
     * ViewportCluster. Override this method to implement tool functions
     * triggered by the specific event.
     *
     * @param vpc ViewportCluster which received the event.
     * @param e Event created by the key release.
     */
    public void keyReleased(ViewportCluster vpc, KeyEvent e)
    {
    }

    /**
     * Invoked when a keyboard key is typed (pressed and released) while the
     * focus is on a ViewportCluster. Override this method to implement tool
     * functions triggered by the specific event.
     *
     * @param vpc ViewportCluster which received the event.
     * @param e Event created by the key being typed.
     */
    public void keyTyped(ViewportCluster vpc, KeyEvent e)
    {
    }

    /**
     * Invoked when the mouse wheel is rotated when the mouse is on a
     * ViewportCluster. Override this method to implement tool functions
     * triggered by the specific event.
     * <p>
     * There is <b>no</b> support for mouse wheel in Java versions prior to
     * 1.4, in which case this method never gets called.
     *
     * @param vpc ViewportCluster which received the event.
     * @param vp Viewport which the mouse cursor was on during the event;
     *		 <code>null</code> if the mouse was outside all visible
     *		 Viewports in the ViewportCluster.
     * @param e <code>MouseWheelEvent</code> created by the mouse wheel
     *	        rotation. However, for backward compatibility with Java
     *	        versions prior to 1.4, <code>e</code> is declared as a
     *	        <code>MouseEvent</code>. If this method is ever invoked by
     *		jViewBox, mouse wheel support is implied and <code>e</code>
     *		will be indeed a <code>MouseWheelEvent</code>. For maximum
     *	        compatibility, <code>MouseWheelEventHelper</code> may be used
     *		to access <code>e</code> instead of explicitly casting e to
     *		<code>MouseWheelEvent</code> which may cause problems in Java
     *		versions prior to 1.4.
     *
     * @see MouseWheelEventHelper
     */
    public void mouseWheelMoved(ViewportCluster vpc, Viewport vp,
				MouseEvent e)
    {
    }

    // ------------------
    // Listener callbacks 
    // ------------------

    /**
     * Invoked when a mouse button has been clicked (pressed and released) on a
     * component.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>Viewport</code> and <code>ViewportCluster</code> of the event is
     * identified.
     *
     * @param e <code>MouseEvent</code> created by the button click.
     */
    protected void mouseClicked(MouseEvent e)
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	mouseClicked(vpc, vpc.getViewport(e.getPoint()), e,
		     _getMouseButtonNumber(e));
    }

    /**
     * Invoked when the mouse enters a component.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>Viewport</code> and <code>ViewportCluster</code> of the event is
     * identified.
     *
     * @param e <code>MouseEvent</code> created by the mouse motion.
     */
    protected void mouseEntered(MouseEvent e)
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	mouseEntered(vpc, vpc.getViewport(e.getPoint()), e);
    }

    /**
     * Invoked when the mouse exits a component.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>Viewport</code> and <code>ViewportCluster</code> of the event is
     * identified.
     *
     * @param e <code>MouseEvent</code> created by the mouse motion.
     */
    protected void mouseExited(MouseEvent e)
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	mouseExited(vpc, vpc.getViewport(e.getPoint()), e);
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>Viewport</code> and <code>ViewportCluster</code> of the event is
     * identified.
     *
     * @param e <code>MouseEvent</code> created by the button press.
     * @throws IOException 
     */
    protected void mousePressed(MouseEvent e) throws IOException
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	mousePressed(vpc, vpc.getViewport(e.getPoint()), e,
		     _getMouseButtonNumber(e));
    }

    /**
     * Invoked when a mouse button is released on a component.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>Viewport</code> and <code>ViewportCluster</code> of the event is
     * identified.
     *
     * @param e <code>MouseEvent</code> created by the button release.
     */
    protected void mouseReleased(MouseEvent e)
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	mouseReleased(vpc, vpc.getViewport(e.getPoint()), e,
		      _getMouseButtonNumber(e));
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>Viewport</code> and <code>ViewportCluster</code> of the event is
     * identified.
     *
     * @param e <code>MouseEvent</code> created by the mouse drag.
     */
    protected void mouseDragged(MouseEvent e)
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	mouseDragged(vpc, vpc.getViewport(e.getPoint()), e,
		     _getMouseButtonNumber(e));
    }

    /**
     * Invoked when the mouse has been moved on a component without no buttons
     * down.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>Viewport</code> and <code>ViewportCluster</code> of the event is
     * identified.
     *
     * @param e <code>MouseEvent</code> created by the mouse motion.
     */
    protected void mouseMoved(MouseEvent e)
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	mouseMoved(vpc, vpc.getViewport(e.getPoint()), e);
    }

    /**
     * Invoked when a key is pressed while the focus is on a component.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>ViewportCluster</code> of the event is identified.
     *
     * @param e <code>KeyEvent</code> created by the key press.
     */
    protected void keyPressed(KeyEvent e)
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	keyPressed(vpc, e);
    }

    /**
     * Invoked when a key is released while the focus is on a component.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>ViewportCluster</code> of the event is identified.
     *
     * @param e <code>KeyEvent</code> created by the key release.
     */
    protected void keyReleased(KeyEvent e)
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	keyReleased(vpc, e);
    }

    /**
     * Invoked when a keyboard key is typed (pressed and released) while the
     * focus is on a component.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>ViewportCluster</code> of the event is identified.
     *
     * @param e <code>KeyEvent</code> created by the key being typed.
     */
    protected void keyTyped(KeyEvent e)
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	keyTyped(vpc, e);
    }

    /**
     * Invoked when the mouse wheel is rotated.
     * <p>
     * This forwards the event to the corresponding <code>public</code> method
     * designed to be overriden by subclasses after the source
     * <code>Viewport</code> and <code>ViewportCluster</code> of the event is
     * identified.
     *
     * @param e <code>MouseWheelEvent</code> created by the mouse wheel
     *	        rotation, declared as a <code>MouseEvent</code> for backward
     *		compatibility.
     */
    protected void mouseWheelMoved(MouseEvent e)
    {
	// Only deal with events from a ViewportCluster
	if (!(e.getComponent() instanceof ViewportCluster)) {
	    return;
	}
	ViewportCluster vpc = (ViewportCluster)e.getComponent();

	// Forward the event
	mouseWheelMoved(vpc, vpc.getViewport(e.getPoint()), e);
    }

    // ---------------
    // Private methods
    // ---------------

    /** Creates function descriptions from functions for each button. */
    private static String[] _buildFunctionDesc(String leftFunction,
					       String middleFunction,
					       String rightFunction)
    {
	String[] functions = new String[3];
	int n = 0;
	if (leftFunction != null && leftFunction.length() > 0) {
	    functions[n++] = "Left button: " + leftFunction;
	}
	if (middleFunction != null && middleFunction.length() > 0) {
	    functions[n++] = "Middle button: " + middleFunction;
	}
	if (rightFunction != null && rightFunction.length() > 0) {
	    functions[n++] = "Right button: " + rightFunction;
	}

	String[] retval = new String[n];
	System.arraycopy(functions, 0, retval, 0, n);
	return retval;
    }

    /**
     * Returns the # representing the mouse button pressed.
     * <p>
     * This method uses the <code>MouseEvent.BUTTON?_MASK</code>s and
     * <code>MouseEvent.getModifiers</code> to find out which mouse button
     * was pressed/released/clicked. By the Java API convention, the value of
     * <code>BUTTON2_MASK</code> is the same as <code>ALT_MASK</code> and
     * <code>BUTTON3_MASK</code> the same as <code>META_MASK</code>. As of
     * jViewBox 2.0b, this method is modified to test for the
     * <code>BUTTON?_MASK</code> in the order of 3, 2, and 1, instead of 1,
     * 2, and 3 in previous version. In this case, a mouse event with the meta
     * modifier key down is recognized as button 3, and a mouse event with the
     * alt modifier key down as button 2. This improves the compatibility with
     * the single button mouses found on most Mac machines, where option key
     * is alt and command key is meta.
     */
    private static int _getMouseButtonNumber(MouseEvent e)
    {
	// Test for button 3 first, followed by 2 and 1, to take advantage of
	// the overlap in the values of ALT_MASK/BUTTON2_MASK and
	// META_MASK/BUTTON3_MASK and improve the compatibility with single
	// button mouses typically found on Mac
	int modifiers = e.getModifiers();
	if ((modifiers & MouseEvent.BUTTON3_MASK) != 0) {
	    return RIGHT_MOUSE_BUTTON;
	}
	else if ((modifiers & MouseEvent.BUTTON2_MASK) != 0) {
	    return MIDDLE_MOUSE_BUTTON;
	}
	else if ((modifiers & MouseEvent.BUTTON1_MASK) != 0) {
	    return LEFT_MOUSE_BUTTON;
	}
	else {
	    return NO_MOUSE_BUTTON;
	}
    }
}
