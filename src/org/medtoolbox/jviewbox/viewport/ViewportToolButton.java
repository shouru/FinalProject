/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolTip;

/**
 * Toggle button used to represent Viewport tools in the toolbar.
 *
 * @version January 8, 2004
 */
public class ViewportToolButton extends JToggleButton
{
    // --------------
    // Private fields
    // --------------

    /** ViewportTool associated with the tool button. */
    private final ViewportTool _tool;

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs a ViewportToolButton for the specified tool with the
     * specified text on the button face.
     *
     * @param tool ViewportTool associated with the button.
     * @param text Text to display on the button face.
     */
    public ViewportToolButton(ViewportTool tool, String text)
    {
	super(text);

	if (tool == null) {
	    throw new NullPointerException("tool can not be null.");
	}
	_tool = tool;

	_setToolTipText();
    }

    /**
     * Constructs a ViewportToolButton for the specified tool with the
     * specified icon on the button face.
     *
     * @param tool ViewportTool associated with the button.
     * @param selectedIcon Icon to display on the button face when the tool is
     *			   selected; <code>null</code> to let Swing create one.
     * @param notSelectedIcon Icon to display on the button face when the tool
     *			      is not selected.
     */
    public ViewportToolButton(ViewportTool tool, ImageIcon selectedIcon,
			      ImageIcon notSelectedIcon)
    {
	super(notSelectedIcon);

	if (tool == null) {
	    throw new NullPointerException("tool can not be null.");
	}
	_tool = tool;

	if (selectedIcon != null) {
	    setSelectedIcon(selectedIcon);
	}

	_setToolTipText();
    }

    /**
     * Constructs a ViewportToolButton for the specified tool with the
     * specified text and icon on the button face.
     *
     * @param tool ViewportTool associated with the button.
     * @param selectedIcon Icon to display on the button face when the tool is
     *			   selected; <code>null</code> to let Swing create one.
     * @param notSelectedIcon Icon to display on the button face when the tool
     *			      is not selected.
     * @param text Text to display on the button face.
     */
    public ViewportToolButton(ViewportTool tool, ImageIcon selectedIcon,
			      ImageIcon notSelectedIcon, String text)
    {
	super(text, notSelectedIcon);

	if (tool == null) {
	    throw new NullPointerException("tool can not be null.");
	}
	_tool = tool;

	if (selectedIcon != null) {
	    setSelectedIcon(selectedIcon);
	}

	_setToolTipText();
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Gets the tool associated with this tool button.
     *
     * @return Tool associated with this tool button.
     */
    public ViewportTool getTool()
    {
	return _tool;
    }

    /**
     * Overriden to create a ToolTip capable of displaying more than one line.
     *
     * @return ToolTip formatted to multiple lines.
     */
    public JToolTip createToolTip()
    {
	MultiLineToolTip tip = new MultiLineToolTip();
	tip.setComponent(this);
	return tip;
    }

    // -----------------
    // Protected methods
    // -----------------

    /** Sets up tooltip text for this button. */
    protected void _setToolTipText()
    {
	StringBuffer buffer = new StringBuffer(_tool.getToolName());
	String[] functionDesc = _tool.getFunctionDescriptions();
	for (int i = 0; i < functionDesc.length; i++) {
	    buffer.append('\n');
	    buffer.append(functionDesc[i]);
	}

	setToolTipText(buffer.toString());
    }
}
