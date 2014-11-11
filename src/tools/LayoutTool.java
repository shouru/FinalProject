/*
jViewBox 2.0 alpha

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
 * Tool for cycling through different Viewport layouts.  When the mouse
 * cursor is pressed on a ViewportCluster, the Viewports inside of it
 * will be arranged into a different layout.  Multiple mouse presses
 * will eventually return the ViewportCluster to its original layout.
 *
 * @version 29 September 2000
 *
 * @version February 28, 2003
 */
public class LayoutTool extends ViewportTool
{
    /** Constructs a LayoutTool. */
    public LayoutTool()
    {
	super("Layout Tool", "Cycle Through Image Layouts", "", "");
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
	if (vpc != null && vpc instanceof ViewportGrid) {
	    ViewportGrid vpg = (ViewportGrid)vpc;
	    int rowNumber = vpg.getNumberOfRows();
	    int colNumber = vpg.getNumberOfColumns();

	    // Grid is too fine; reduce the number of rows and columns
	    if (rowNumber % 4 == 0 && colNumber % 4 == 0) {
		vpg.setGrid(rowNumber/4, colNumber/4);
	    }
	
	    // Else double the number of rows and columns
	    else {
		vpg.setGrid(2*rowNumber, 2*colNumber);
	    }

	    // Repaint the ViewportCluster
	    vpc.repaint();
	}
    }
}
