/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

/**
 * ViewportCluster which manages its Viewports in a rectangular grid. It keeps
 * the Viewports in square sizes.
 *
 * @version January 8, 2004
 */
public class ViewportGrid extends ViewportCluster
{
    // ---------
    // Constants
    // ---------

    /** Arrange Viewports along the rows of the grid. */
    public static final int ROW_LAYOUT = 0;

    /** Arrange Viewports along the columns of the grid. */
    public static final int COLUMN_LAYOUT = 1;

    // ----------------
    // Protected fields
    // ----------------

    /** Rectangles inside of which to display Viewports. */
    protected final Vector _viewportWindows = new Vector(1, 1);

    // --------------
    // Private fields
    // --------------

    /** Dimension of the grid used to display Viewports in. */
    private final Dimension _grid = new Dimension();

    /** Layout option for the Viewports. */
    private int _layout;

    // ------------
    // Constructors
    // ------------

    /**
     * Constructs a ViewportGrid for the specified Images. By default, the
     * Viewports are arranged along the rows of the grid (from left to right
     * along the 1st row, then from left to right along the 2nd row, etc.) If
     * there is only one Image, the grid dimension defaults to 1 x 1; otherwise
     * the grid dimension defaults to 2 x 2.
     *
     * @param images Images to be displayed in the ViewportGrid.
     */
    public ViewportGrid(List images)
    {
	this(images, ROW_LAYOUT);
    }

    /**
     * Constructs a ViewportGrid for the specified Images that is arranged by
     * the specified layout. If there is only one Image, the grid dimension 
     * defaults to 1 x 1; otherwise the grid dimension defaults to 2 x 2.
     *
     * @param images Images to be displayed in the ViewportGrid.
     * @param layout {@link #ROW_LAYOUT} to arrange Viewports along the
     *		     rows; {@link #COLUMN_LAYOUT} to arrange Viewports
     *		     along the columns.
     *
     * @throws IllegalArgumentException if <code>layout</code> is not one of
     *	       {@link #ROW_LAYOUT} or {@link #COLUMN_LAYOUT}.
     */
    public ViewportGrid(List images, int layout)
    {
	super(images);
	_initLayout(layout);
    }

    /**
     * Constructs a ViewportGrid out of the specified Viewports, which are
     * by default arranged along the rows of the grid. If there is only one
     * Viewport, the grid dimension defaults to 1 x 1; otherwise the grid
     * dimension defaults to 2 x 2.
     *
     * @param viewports <code>Viewport</code>s to construct a ViewportGrid
     *			out of.
     */
    public ViewportGrid(Viewport[] viewports)
    {
	this(viewports, ROW_LAYOUT);
    }

    /**
     * Constructs a ViewportGrid out of the specified Viewports that is
     * arranged by the specified layout. If there is only one Image, the grid
     * dimension defaults to 1 x 1; otherwise the grid dimension defaults to
     * 2 x 2.
     *
     * @param viewports <code>Viewport</code>s to construct a ViewportGrid
     *			out of.
     * @param layout {@link #ROW_LAYOUT} to arrange Viewports along the
     *		     rows; {@link #COLUMN_LAYOUT} to arrange Viewports
     *		     along the columns.
     *
     * @throws IllegalArgumentException if <code>layout</code> is not one of
     *	       {@link #ROW_LAYOUT} or {@link #COLUMN_LAYOUT}.
     */
    public ViewportGrid(Viewport[] viewports, int layout)
    {
	super(viewports);
	_initLayout(layout);
    }

    /** Called only by constructos to initialize this grid's layout. */
    private void _initLayout(int layout)
    {
	if (layout != ROW_LAYOUT && layout != COLUMN_LAYOUT) {
	    throw new IllegalArgumentException("layout must be either " +
					       "ROW_LAYOUT (=" + ROW_LAYOUT +
					       ") or COLUMN_LAYOUT (=" +
					       COLUMN_LAYOUT + "), not " +
					       layout);
	}
	_layout = layout;

	// Set the grid dimensions
	if (_viewports.size() <= 1) {
	    _grid.setSize(1, 1);
	}
	else {
	    _grid.setSize(2, 2);
	}
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Scrolls the Viewports by grid dimension.  If the Viewports are
     * arranged along the rows of the grid, the grid dimension is
     * to the number of Viewports in a row.  If the Viewports are arranged
     * along the columns of the grid, the grid dimension is the number of
     * Viewports in a column.  Scrolling is restricted such that no scroll
     * will occur if such an action would result in no Viewport being visible
     * in the ViewportGrid.
     *
     * @param number Number of grid dimensions to scroll by.
     */
    public void gridScroll(int number)
    {
	// Determine the grid dimension
	int gridDimension = getNumberOfRows();
	if (_layout == COLUMN_LAYOUT) {
	    gridDimension = getNumberOfColumns();
	}

	// Scroll
	scroll(number, gridDimension);
    }

    /**
     * Returns the number of rows in the grid.
     *
     * @return Number of rows in the grid.
     */
    public int getNumberOfRows()
    {
	return _grid.height;
    }

    /**
     * Returns the number of columns in the grid.
     *
     * @return Number of columns in the grid.
     */
    public int getNumberOfColumns()
    {
	return _grid.width;
    }

    /**
     * Sets the number of rows and columns in the grid.
     *
     * @param numberOfRows Number of rows in the grid.
     * @param numberOfColumns Number of columns in the grid.
     *
     * @throws IllegalArgumentException if the number of rows or columns is
     *	       not a positive integer.
     */
    public void setGrid(int numberOfRows, int numberOfColumns)
    {
	if (numberOfRows <= 0 || numberOfColumns <= 0) {
	    throw new IllegalArgumentException("The number of rows or " +
					       "columns can not be zero or " +
					       "negative.");
	}

	_grid.setSize(numberOfColumns, numberOfRows);

	// Update the Viewport windows with the new grid
	_updateViewportWindows();

	// Scroll so there is a Viewport in the first Viewport window
	if (_offset < 0) { _offset = 0; }
    }

    /**
     * Moves and resizes this component.
     * <p>
     * We override this method to update Viewport Window geometry when this
     * ViewportGrid's bounds change. This is more or less of a hack because
     * we assume this method is called in all cases of bound changes, for
     * example, by <code>setSize</code>.
     * <p>
     * Ideally, {@link #_getViewportWindows} should always calculate the
     * Viewport Windows given the current bounds, instead of caching the
     * result. The current implementation of <code>ViewportCluster</code> makes
     * many calls to this method. Without caching there can be a performance
     * problem.
     *
     * @param x New x coordinate of this component.
     * @param y New y coordinate of this component.
     * @param width New width of this component.
     * @param height New height of this component.
     */
    public void setBounds(int x, int y, int width, int height)
    {
	super.setBounds(x, y, width, height);

	// Update the Viewport windows with the new grid
	_updateViewportWindows();
    }

    /**
     * Returns the rectangles which define the Viewport Windows in a
     * <code>List</code>. The returned list and all elements in the list
     * should be deemed as unmodifiable constants even though overwriting is
     * not actually prohibited.
     * <p>
     * Any implementing subclass must override this method to define itw own
     * Viewport Window geometry. The implementation must take the insets into
     * consideration, i.e., arrange Viewport Windows within the insets.
     * Otherwise, part of the image display may be blocked by the borders of
     * this ViewportCluster.
     *
     * @return <code>List</code> of <code>java.awt.Rectangle</code>s which
     *	       define the Viewport Windows.
     */
    protected List _getViewportWindows()
    {
	if (_viewportWindows.isEmpty()) {
	    _updateViewportWindows();
	}
	return _viewportWindows;
    }

    // ---------------
    // Private methods
    // ---------------

    /**
     * Updates the Viewport windows.  Creates a new set of windows which
     * make up the grid.
     */
    private void _updateViewportWindows()
    {
	// Delete all of the current windows
	_viewportWindows.clear();

	// Calculate the length of each side of each box in the grid
	int rows = getNumberOfRows();
	int cols = getNumberOfColumns();
	Rectangle gridBounds = _getInnerBounds();
	int gridBoxWidth = gridBounds.width / cols;
	int gridBoxHeight = gridBounds.height / rows;
	int gridBoxLength = Math.min(gridBoxWidth, gridBoxHeight);

	// Do not let Viewport window have a width or height of 0
	// for Viewport.setBounds would fail with a zero width or height
	// even though this may cause clipping of viewports
	if (gridBoxLength <= 0) {
	    gridBoxLength = 1;
	}

	// Calculate the top left grid corner so that the grid is centered
	int gridX = gridBounds.x +
		    (gridBounds.width - gridBoxLength * cols) / 2;
	int gridY = gridBounds.y +
		    (gridBounds.height - gridBoxLength * rows) / 2;

	// Generate the windows of the grid for the row layout
	if (_layout == ROW_LAYOUT) {
	    for (int j = 0; j < rows; j++) {
		for (int i = 0; i < cols; i++) {
		    Rectangle r = new Rectangle(gridX + i * gridBoxLength,
						gridY + j * gridBoxLength,
						gridBoxLength, gridBoxLength);
		    _viewportWindows.add(r);
		}
	    }
	}

	// Generate the windows of the grid for the column layout
	else /*if (_layout == COLUMN_LAYOUT)*/ {
	    for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
		    Rectangle r = new Rectangle(gridX + i * gridBoxLength,
						gridY + j * gridBoxLength,
						gridBoxLength, gridBoxLength);
		    _viewportWindows.add(r);
		}
	    }
	}

	// Propogate the size change to all Viewports so that they have a
	// chance to react accordingly. This helps solving the bug of
	// incorrect scale factors. This should not have any visible side
	// effect because ViewportCluster.paintComponent() always reset the
	// correct Viewport bounds before calling Viewport.paint()
	for (ListIterator it = getViewports().listIterator(); it.hasNext(); ) {
	    int viewportIndex = it.nextIndex();
	    Viewport viewport = (Viewport)it.next();
	    // Window index is the viewport index minus the offset
	    int windowIndex = viewportIndex - _offset;
	    if (windowIndex >= 0 && windowIndex < _viewportWindows.size()) {
		// A Viewport currently in view; assign its true bounds to it
		Rectangle r = (Rectangle)_viewportWindows.get(windowIndex);
		viewport.setBounds(r);
	    }
	    else {
		// A Viewport currently out of view; assign the new size to it
		viewport.setBounds(0,0, gridBoxLength, gridBoxLength);
	    }
	}
    }
}
