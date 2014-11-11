/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JToolBar;
import javax.swing.ImageIcon;
import javax.swing.ButtonGroup;

/**
 * JToolBar which manages a group of <code>ViewportTool</code>s.
 * <p>
 * <code>ViewportTool</code>s managed by a ViewportToolBar are represented as
 * toggle buttons in the toolbar and grouped in a <code>ButtonGroup</code> so
 * that one and only one of them is selected at all time. Once registered with
 * a <code>ViewportCluster</code>, ViewportToolBar listens to input events from
 * the cluster and forwards them to the selected tool so that the tool may
 * perform its functions. Note that user input to the toolbar itself (e.g., 
 * click on one of the tool buttons) only affects the selection of
 * ViewportTools. For the selected tool to function, user input needs to occur
 * inside some <code>ViewportCluster</code> this toolbar has registered with.
 * <p>
 * Remember that a ViewportToolBar is still a <code>JToolBar</code>. It is
 * permitted to add buttons directly to a ViewportToolBar. Buttons added this
 * way are <b>NOT</b> managed by the ViewportToolBar. This may be useful in
 * the case when non-Viewport related tools are needed in the same toolbar.
 *
 * @version January 8, 2004
 */
public class ViewportToolBar extends JToolBar
{
    // --------------
    // Private fields
    // --------------

    /** ButtonGroup of all ViewportToolButtons. */
    private final ButtonGroup _toolButtonGroup = new ButtonGroup();

    /** CompositeInputListener. */
    private final CompositeInputListener _listener =
	new CompositeInputListener();

    /** MouseWheelListener. */
    private final java.awt.event.MouseWheelListener _mouseWheelListener =
	new java.awt.event.MouseWheelListener()
	    {
		public void mouseWheelMoved(java.awt.event.MouseWheelEvent e)
		{
		    ViewportToolButton tb =
			ViewportToolBar.this.getSelectedToolButton();
		    if (tb != null) {
			tb.getTool().mouseWheelMoved(e);
		    }
		}
	    };

    // -----------
    // Constructor
    // -----------

    /** Constructs a ViewportToolBar. */
    public ViewportToolBar()
    {
    }

    /**
     * Constructs a named ViewportToolBar. The name is used as the title of
     * the toolbar when it is undocked.
     *
     * @param name Name of the ViewportToolBar.
     */
    public ViewportToolBar(String name)
    {
	super(name);
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Adds a {@link ViewportTool} to this toolbar. The tool is represented
     * as a {@link ViewportToolButton} with image icon and text on the button
     * face.
     *
     * @param tool {@link ViewportTool} to add to this toolbar.
     * @param selectedIcon Icon shown on the tool's button when it is selected;
     *			   <code>null</code> to let Swing create one for you.
     * @param notSelectedIcon Icon shown on the tool's button when it is
     *			      not selected.
     * @param text Text displayed on the tool's button.
     *
     * @return The tool's button in this toolbar.
     */
    public ViewportToolButton addTool(ViewportTool tool,
				      ImageIcon selectedIcon,
				      ImageIcon notSelectedIcon, String text)
    {
	ViewportToolButton button = new ViewportToolButton(tool, selectedIcon,
							   notSelectedIcon,
							   text);
	_addToolButton(button);
	return button;
    }

    /**
     * Adds a {@link ViewportTool} to this toolbar. The tool is represented
     * as a {@link ViewportToolButton} with image icon but no text on the
     * button face.
     *
     * @param tool {@link ViewportTool} to add to this toolbar.
     * @param selectedIcon Icon shown on the tool's button when it is selected.
     *			   <code>null</code> to let Swing create one for you.
     * @param notSelectedIcon Icon shown on the tool's button when it is
     *			      not selected.
     * @param text Text displayed on the tool's button.
     *
     * @return The tool's button in this toolbar.
     */
    public ViewportToolButton addTool(ViewportTool tool,
				      ImageIcon selectedIcon,
				      ImageIcon notSelectedIcon)
    {
	ViewportToolButton button = new ViewportToolButton(tool, selectedIcon,
							   notSelectedIcon);
	_addToolButton(button);
	return button;
    }

    /**
     * Adds a {@link ViewportTool} to this toolbar. The tool is 
     * represented as a {@link ViewportToolButton} with text but no image icon
     * on the button face.
     *
     * @param tool {@link ViewportTool} to add to this toolbar.
     * @param text Text displayed on the tool's button.
     *
     * @return The tool's button in this toolbar.
     */
    public ViewportToolButton addTool(ViewportTool tool, String text)
    {
	ViewportToolButton button = new ViewportToolButton(tool, text);
	_addToolButton(button);
	return button;
    }

    /**
     * Sets the selected tool button in this toolbar. All other tool buttons
     * in this toolbar then become unselected.
     *
     * @param selectedToolButton The tool button to be selected.
     */
    public void setSelectedToolButton(ViewportToolButton selectedToolButton)
    {
	// Search to make sure the button is in the group
	for (Enumeration enumn = _toolButtonGroup.getElements();
	     enumn.hasMoreElements(); ) {

	    if (selectedToolButton == enumn.nextElement()) {
		selectedToolButton.setSelected(true);
		return;
	    }
	}

	// Not found
	throw new IllegalArgumentException("The specified tool button is not "+
					   "found in this toolbar.");
    }

    /**
     * Gets the selected tool button in this toolbar.
     *
     * @return Tool button which is currently selected; <code>null</code> if
     *         none is selected.
     */
    public ViewportToolButton getSelectedToolButton()
    {
	// Find a tool button in the group with a selected state
	for (Enumeration enumn = _toolButtonGroup.getElements();
	     enumn.hasMoreElements(); ) {

	    ViewportToolButton tb = (ViewportToolButton)enumn.nextElement();
	    if (tb.isSelected()) {
		return tb;
	    }
	}

	// Not found
	return null;
    }

    /**
     * Returns the <code>MouseListener</code> to be added to a
     * <code>ViewportCluster</code> for this toolbar to function on the
     * ViewportCluster.
     *
     * @return <code>MouseListener</code> to be added to a
     *	       <code>ViewportCluster</code> for this toolbar to function on the
     *	       ViewportCluster.
     */
    public MouseListener getMouseListener()
    {
	return _listener;
    }

    /**
     * Returns the <code>MouseMotionListener</code> to be added to a
     * <code>ViewportCluster</code> for this toolbar to function on the
     * ViewportCluster.
     *
     * @return <code>MouseMotionListener</code> to be added to a
     *	       <code>ViewportCluster</code> for this toolbar to function on the
     *	       ViewportCluster.
     */
    public MouseMotionListener getMouseMotionListener()
    {
	return _listener;
    }

    /**
     * Returns the <code>KeyListener</code> to be added to a
     * <code>ViewportCluster</code> for this toolbar to function on the
     * ViewportCluster.
     *
     * @return <code>KeyListener</code> to be added to a
     *	       <code>ViewportCluster</code> for this toolbar to function on the
     *	       ViewportCluster.
     */
    public KeyListener getKeyListener()
    {
	return _listener;
    }

    /**
     * Returns the <code>MouseWheelListener</code> to be added to a
     * <code>ViewportCluster</code> for this toolbar to function on the
     * ViewportCluster.
     *
     * @return <code>MouseWheelListener</code> to be added to a
     *	       <code>ViewportCluster</code> for this toolbar to function on the
     *	       ViewportCluster.
     */
    public java.awt.event.MouseWheelListener getMouseWheelListener()
    {
	return _mouseWheelListener;
    }

    // --------------------
    // Private member class
    // --------------------

    /**
     * Inner class used to hide the implementation of Key, Mouse, and
     * MouseMotion Listeners in this class for forwarding events from
     * ViewportCluster to the selected tool.
     */
    private class CompositeInputListener
	implements MouseListener, MouseMotionListener, KeyListener
    {
	CompositeInputListener() {}

	// ------------------
	// Listener callbacks
	// ------------------

	/**
	 * Invoked when a mouse button has been clicked (pressed and released)
	 * on a component.
	 *
	 * @param e <code>MouseEvent</code> created by the button click.
	 */
	public void mouseClicked(MouseEvent e)
	{
	    ViewportToolButton tb =
		ViewportToolBar.this.getSelectedToolButton();
	    if (tb != null) {
		tb.getTool().mouseClicked(e);
	    }
	}

	/**
	 * Invoked when the mouse enters a component.
	 *
	 * @param e <code>MouseEvent</code> created by the mouse motion.
	 */
	public void mouseEntered(MouseEvent e)
	{
	    ViewportToolButton tb =
		ViewportToolBar.this.getSelectedToolButton();
	    if (tb != null) {
		tb.getTool().mouseEntered(e);
	    }
	}

	/**
	 * Invoked when the mouse exits a component.
	 *
	 * @param e <code>MouseEvent</code> created by the mouse motion.
	 */
	public void mouseExited(MouseEvent e)
	{
	    ViewportToolButton tb =
		ViewportToolBar.this.getSelectedToolButton();
	    if (tb != null) {
		tb.getTool().mouseExited(e);
	    }
	}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 *
	 * @param e <code>MouseEvent</code> created by the button press.
	 */
	public void mousePressed(MouseEvent e)
	{
	    ViewportToolButton tb =
		ViewportToolBar.this.getSelectedToolButton();
	    if (tb != null) {
		try {
			tb.getTool().mousePressed(e);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    }
	}

	/**
	 * Invoked when a mouse button is released on a component.
	 *
	 * @param e <code>MouseEvent</code> created by the button release.
	 */
	public void mouseReleased(MouseEvent e)
	{
	    ViewportToolButton tb =
		ViewportToolBar.this.getSelectedToolButton();
	    if (tb != null) {
		tb.getTool().mouseReleased(e);
	    }
	}

	/**
	 * Invoked when a mouse button is pressed on a component and then
	 * dragged.
	 *
	 * @param e <code>MouseEvent</code> created by the mouse drag.
	 */
	public void mouseDragged(MouseEvent e)
	{
	    ViewportToolButton tb =
		ViewportToolBar.this.getSelectedToolButton();
	    if (tb != null) {
		tb.getTool().mouseDragged(e);
	    }
	}

	/**
	 * Invoked when the mouse has been moved on a component without no
	 * buttons down.
	 *
	 * @param e <code>MouseEvent</code> created by the mouse motion.
	 */
	public void mouseMoved(MouseEvent e)
	{
	    ViewportToolButton tb =
		ViewportToolBar.this.getSelectedToolButton();
	    if (tb != null) {
		tb.getTool().mouseMoved(e);
	    }
	}

	/**
	 * Invoked when a key is pressed while the focus is on a component.
	 *
	 * @param e <code>KeyEvent</code> created by the key press.
	 */
	public void keyPressed(KeyEvent e)
	{
	    ViewportToolButton tb =
		ViewportToolBar.this.getSelectedToolButton();
	    if (tb != null) {
		tb.getTool().keyPressed(e);
	    }
	}

	/**
	 * Invoked when a key is released while the focus is on a component.
	 *
	 * @param e <code>KeyEvent</code> created by the key release.
	 */
	public void keyReleased(KeyEvent e)
	{
	    ViewportToolButton tb =
		ViewportToolBar.this.getSelectedToolButton();
	    if (tb != null) {
		tb.getTool().keyReleased(e);
	    }
	}

	/**
	 * Invoked when a keyboard key is typed (pressed and released) while
	 * the focus is on a component.
	 *
	 * @param e <code>KeyEvent</code> created by the key being typed.
	 */
	public void keyTyped(KeyEvent e)
	{
	    ViewportToolButton tb =
		ViewportToolBar.this.getSelectedToolButton();
	    if (tb != null) {
		tb.getTool().keyTyped(e);
	    }
	}
    }

    // ---------------
    // Private methods
    // ---------------

    /** Adds the specified tool button to this toolbar. */
    private void _addToolButton(ViewportToolButton toolButton)
    {
	add(toolButton);
	_toolButtonGroup.add(toolButton);

	// The first tool?
	if (_toolButtonGroup.getButtonCount() == 1) {
	    // Select it
	    _toolButtonGroup.setSelected(toolButton.getModel(), true);
	}
    }
}
