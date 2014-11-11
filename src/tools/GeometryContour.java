/*
 * @(#)GeometryContour.java		1.10 07/01/20
 *
 * ChargedFluid package
 *
 * COPYRIGHT NOTICE
 * Copyright (c) 2007 Herbert H.H. Chang, Daniel J. Valentino, Gary R. Duckwiler, and Arthur W. Toga
 * Laboratory of Neuro Imaging, Department of Neurology, UCLA.
 */
 
package tools;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Point;
import java.util.LinkedList;

/**
 * The <code>GeometryContour</code> class is dedicated to the initial contour generation for
 * the charged fluid algorithm. There are two geometric shapes, square and circle-like 
 * contours, for users to choose. After the initial contour is chosen, it automatically 
 * creates a <code>Polygon</code> for display.
 */
public class GeometryContour
{
	/** 
	 * The center of a specified circle in x-axis. 
	 */
	private int _cx;
	
	/** 
	 * The center of a specified circle in y-axis. 
	 */
	private int _cy;
	
	/** 
	 * The radius of a specified circle. 
	 */
	private int _radius;
	
	/** 
	 * The starting point of a specified circle for contour tracing. 
	 */
	private Point _startPt;
	
	/** 
	 * The new point of a specified circle during the contour tracing procedure. 
	 */
	private Point _newPt;
	
	/** 
	 * The original point in x-axis for a specified rectangle. 
	 */
	private int _ox;
	
	/** 
	 * The original point in y-axis for a specified rectangle. 
	 */
	private int _oy;
	
	/** 
	 * The width of a specified rectangle. 
	 */
	private int _width;
	
	/** 
	 * The height of a specified rectangle. 
	 */
	private int _height;
	
	/** 
	 * Storing the contour points, <code>Point</code>. 
	 */
  	public LinkedList pointLinkedList = new LinkedList();
  	
  	/** 
  	 * Showing the contour of a specified circle.
  	 */
  	public Polygon circlePolygon;
	
	/** 
	 * Showing the contour of a specified rectangle. 
	 */
	public Rectangle rectangle;
	
	
	/** 
	 * Constructs a circle-like polygon for the initial geometry contour with the specified
	 * center point and radius.
	 * @param	centerPt	the center of the specified circle.
	 * @param	radius		the radius of the specified circle.
	 */
  	public GeometryContour(Point centerPt, int radius)
  	{
    	_cx = centerPt.x;
    	_cy = centerPt.y;
    	_radius = radius;
    	// Search the contour points.
    	_searchContour();
    	// Create a polygon based up the searching points.
    	_createPolygon();
  	}
  	
  	
  	/** 
	 * Constructs a rectangle polygon for the initial geometry contour with the specified
	 * bounds.
	 * @param	ox		the origin of the specified rectangle in x-axis.
	 * @param	oy		the origin of the specified rectangle in y-axis.
	 * @param	width	the width of the specified rectangle.
	 * @param	height	the height of the specified rectangle.
	 */
  	public GeometryContour(int ox, int oy, int width, int height)
  	{
    	_ox = ox;
    	_oy = oy;
    	_width = width;
    	_height = height;
    	// Create a new rectangle.
		rectangle = new Rectangle(_ox, _oy, _width, _height);
		// Create the linkedlist that stores the points of the contour.
		_createRectangleList();
  	}
  	
  	
  	/** 
  	 * Searches the circle-like contour points and store them to the linkedlist,
  	 * <tt>pointLinkedList</tt>. 
  	 */
  	private void _searchContour()
  	{
  		// Add the first point, the right most point, to the linkedlist.
  		_startPt = new Point(_cx+_radius, _cy);
  		pointLinkedList.add(_startPt);
  		
  		_newPt = _startPt;
  		int direction = 1;
  		boolean isClosed = false;
  		// Search for the following points.
  		while (!isClosed) {
  			direction = _contourTracing(_newPt, direction);
  			// Add the new point to the linkedlist.
  			pointLinkedList.add(_newPt);
  			// Check if the contour is closed.
			if (pointLinkedList.size() >= 4) {
				Point lastPoint = (Point)pointLinkedList.getLast();
				if (lastPoint.equals(_startPt)) {
					pointLinkedList.removeLast();
					isClosed = true;
				}
			}
  		}
  	}
	
	
	/** 
	 * Returns the next tracing direction after tracing the circle-like contour points with
	 * the specified start point and direction.
	 * @param	startPt		the start point for the contour tracing.
	 * @param	direction	the searching direction according to the start point.
	 *						0: positive x-axis (->)
	 *						1: positive y-axis (/|\)
	 *						2: negative x-axis (<-)
	 *						3: negative y-axis (\|/)
	 * @return	the next tracing direction.
	 */
	private int _contourTracing(Point startPt, int direction)
	{
		Point zeroPt = new Point();
		Point firstPt = new Point();
		Point secondPt = new Point();
		Point thirdPt = new Point();
		// Decide the searching positions based on the direction.
		switch (direction) {
			// Direction type 0: Positive x-axis (->) and (/>).
		  	case 0:
				zeroPt.setLocation(startPt.x+1,startPt.y-1);
				firstPt.setLocation(startPt.x+1,startPt.y);
				secondPt.setLocation(startPt.x+1,startPt.y+1);
				thirdPt.setLocation(startPt.x,startPt.y+1);
				break;
		  	// Direction type 1: Positive y-axis (/|\) and (<\).
		  	case 1:
		  		zeroPt.setLocation(startPt.x+1,startPt.y+1);
				firstPt.setLocation(startPt.x,startPt.y+1);
				secondPt.setLocation(startPt.x-1,startPt.y+1);
				thirdPt.setLocation(startPt.x-1,startPt.y);
				break;
		  	// Direction type 2: Negative x-axis (<-) and (</).
		  	case 2:
		  		zeroPt.setLocation(startPt.x-1,startPt.y+1);
				firstPt.setLocation(startPt.x-1,startPt.y);
				secondPt.setLocation(startPt.x-1,startPt.y-1);
				thirdPt.setLocation(startPt.x,startPt.y-1);
				break;
		  	// Direction type 3: Negative y-axis (\|/) and (\>).
		  	case 3:
		  		zeroPt.setLocation(startPt.x-1,startPt.y-1);
				firstPt.setLocation(startPt.x,startPt.y-1);
				secondPt.setLocation(startPt.x+1,startPt.y-1);
				thirdPt.setLocation(startPt.x+1,startPt.y);
				break;
		    default:
				System.out.println("Initial contour tracing error");
				return -99;
		}
		double[] distanceGap = new double[4];
		double radius2P = _radius * _radius;
		// Compute the distances of the three candidates to the center of the circle.
		double zeroDistance = (zeroPt.x - _cx) * (zeroPt.x - _cx) + (zeroPt.y - _cy) * (zeroPt.y - _cy);
		distanceGap[0] = Math.abs(radius2P - zeroDistance);
		double firstDistance = (firstPt.x - _cx) * (firstPt.x - _cx) + (firstPt.y - _cy) * (firstPt.y - _cy);
		distanceGap[1] = Math.abs(radius2P - firstDistance);
		double secDistance = (secondPt.x - _cx) * (secondPt.x - _cx) + (secondPt.y - _cy) * (secondPt.y - _cy);
		distanceGap[2] = Math.abs(radius2P - secDistance);
		double thirdDistance = (thirdPt.x - _cx) * (thirdPt.x - _cx) + (thirdPt.y - _cy) * (thirdPt.y - _cy);
		distanceGap[3] = Math.abs(radius2P - thirdDistance);
		// Search for the minimum gap among the three candidates.
		// Set the default minimum gap be at the zero point.
		double minGap = distanceGap[0];
		int minGapIndex = 0;
		_newPt = zeroPt;
		for (int i = 1; i < 4; i++) {
			if (distanceGap[i] < minGap) {
				minGap = distanceGap[i];
				minGapIndex = i;
			}
		}
		// New point change if the minimum gap is not at the zero point.
		if (minGapIndex == 1) {
			_newPt = firstPt;
		}
		else if (minGapIndex == 2) {
			_newPt = secondPt;
		}
		else if (minGapIndex == 3) {
			_newPt = thirdPt;
		}
		int newDirection;
		// Choose the new searching direction based up the chosen point and previous directoin.
		if ((minGapIndex == 1) || (minGapIndex == 2)) {
			newDirection = direction;
		}
		else if (minGapIndex == 0) {
			newDirection = direction - 1;
		}
		// If the new point is the third point.
		else {
			newDirection = direction + 1;
		}
		// Index changes.
		if (newDirection == -1) {
			newDirection = 3;
		}
		if (newDirection == 4) {
			newDirection = 0;
		}
		return newDirection;
	}
	
	
	/** 
	 * Creates a polygon based upon the contour points in the linkedlist,
	 * <tt>pointLinkedList</tt>.
	 */
  	private void _createPolygon()
  	{
  		// Obtain the number of elements (points) in the contour.
  		int npoints = pointLinkedList.size();
  		int[] xpoints = new int[npoints];
  		int[] ypoints = new int[npoints];
  		// Set the coordinates to the corresponding vectors.
  		for (int i = 0; i < npoints; i++) {
		  	Point contourPt = (Point)pointLinkedList.get(i);
		  	xpoints[i] = contourPt.x;
		  	ypoints[i] = contourPt.y;
  		}
  		// Create a new polygon.
  		circlePolygon = new Polygon(xpoints, ypoints, npoints);
  	}
	
	
	/** 
	 * Creates a rectangle for the initial contour and stores the contour points to the
	 * linkedlist, <tt>pointLinkedList</tt>. 
	 */
	private void _createRectangleList()
	{
		// Set the end coordinates of the rectangle.
		int xEnd = _ox + _width - 1;
		int yEnd = _oy + _height - 1;
		// For the bottom row.
		for (int x = _ox; x < xEnd; x++) {
			// Add the new point to the linkedlist.
  			pointLinkedList.add(new Point(x,_oy));
		}
		// For the right column.
		for (int y = _oy; y < yEnd; y++) {
			// Add the new point to the linkedlist.
  			pointLinkedList.add(new Point(xEnd,y));
		}
		// For the upper row.
		for (int x = xEnd; x > _ox; x--) {
			// Add the new point to the linkedlist.
  			pointLinkedList.add(new Point(x,yEnd));
		}
		// For the left column.
		for (int y = yEnd; y > _oy; y--) {
			// Add the new point to the linkedlist.
  			pointLinkedList.add(new Point(_ox,y));
		}
	}
}