/**
 * Model-based Level Set (MLS) Algorithm
 *
 * COPYRIGHT NOTICE
 * Copyright (c) 2003-2005 Haihong Zhuang and Daniel J. Valentino
 *
 * Please read LICENSE.TXT for the license covering this software
 *
 * For more information, please contact the authors at:
 * haihongz@seas.ucla.edu
 * dvalentino@mednet.ucla.edu
 */
package LevelSet;

import java.awt.Point;
import java.util.Vector;

/**
 * Fills the holes in a brain mask.
 *
 * @author Haihong Zhuang and Daniel J. Valentino
 * @version 2 September 2004
 */
public class Filler
{

  /** The mask's non-zero value. */
  private byte _maskValue = 1;

  /** The width of the given array. */
  private int X_DIM = 0;

  /** The height of the given array. */
  private int Y_DIM = 0;

  /** The filled array. */
  private byte[][] _filledArray = null;

  /** 
   * Constructor. 
   *
   * @param array A byte array to fill holes.
   */
  public Filler(byte[][] array)
  {
    if(array != null){     
      X_DIM = array.length;
      Y_DIM = array[0].length;
    }
    if(X_DIM * Y_DIM != 0){
	_filledArray = _fillHoles(array);
    }
    else _filledArray = array;
  }

  /**
   * Returns the filled array.
   * 
   * @return The hole-filled array.
   */
  public byte[][] getFilledArray()
  {
    return _filledArray;
  }

  /**
   * Fill the holes in the mask.
   *
   * @param array An byte array to fill holes.
   */ 
  private byte[][] _fillHoles(byte[][] array)
  {
    int depth;
    double alpha;
    double holesBeginAlpha = 0;
    boolean foundHolesBeginAlpha = false;
    double holesEndAlpha = 0;
    boolean foundMaskEdge = false;
    boolean foundHoles = false;
    
    // Calculate the center of the mask
    Point center;
    if( _getCenter(array) != null) center = _getCenter(array);
    else return array;
    double centerX = center.getX();
    double centerY = center.getY();

    // Find the longest distance and consider it as the radius of the mask
    double r = _getMaxRadius(array, center);

    // If the center of mask is a zero point, find the nearest non zero point
    byte centerValue = array[(int)center.getX()][(int)center.getY()];
    if(centerValue <= 0){
	if(_fillCenterHole(array, center, r, _maskValue) == null) return array;
	else array = _fillCenterHole(array, center, r, _maskValue);
    }

    // Find the longest distance and consider it as the radius of the mask
    r = _getMaxRadius(array, center);
    
    // Find a direction that doesn's have holes, and use this direction as the
    // starting alpha
    double startingAlpha  = _getSolidPass(array, center, r) ;
    if( startingAlpha < 0 ) return array;

    // Rotate the probing direction across 2*PI to search holes
    // Calculate the delta_alpha
    double c = 2 * Math.PI * r;
    double deltaAlpha = 2 * Math.PI / c;

    // Searching holes. Holes are defined as closed zero areas that are
    // surrounded by non-zero mask points
    int i = 0;
    foundHolesBeginAlpha = false;		       
    boolean isDisplayEnabled = false;
    while( i * deltaAlpha + startingAlpha  
	   <= (2 * Math.PI + startingAlpha + deltaAlpha) ){
      alpha = i * deltaAlpha + startingAlpha;
      foundMaskEdge = false;
      foundHoles = false;

      // Probe the points up to distance of r
      depth = (int) Math.floor( r + 5 + 0.5);
      Point holePoint = _getHoleEdgePoint(array, center, alpha, depth, 
					  isDisplayEnabled);
      if(holePoint != null) foundHoles = true;
      if( foundHoles && (!foundHolesBeginAlpha) ) {
 	  holesBeginAlpha = alpha;
 	  foundHolesBeginAlpha = true;
      }  
  
      if( foundHolesBeginAlpha && ( !foundHoles) ) {
	holesEndAlpha = (i-1) * deltaAlpha + startingAlpha;	
	boolean isHoles = _isHoles(array, center, r, holesBeginAlpha, 
				   holesEndAlpha, deltaAlpha);
	if(isHoles)
	    array = _fillHoles(array, center, r, holesBeginAlpha, 
			       holesEndAlpha, deltaAlpha, _maskValue);
	foundHolesBeginAlpha = false;
      }
      i++;
    }
    return array;
  }

  /**
   * Fill the hole in the given mask around the given point, if it is a hole. 
   *
   * @param array An array containing the mask.
   * @param center The center of the mask.
   * @param r The largest distance between the edge of the mask and the center.
   * @param maskValue The pixel value of mask.
   * 
   * @return A byte array with the filled holes.
   */
  private byte[][] _fillCenterHole(byte[][] array, Point center, double r, 
				   byte maskValue)
  {
    boolean isHoles = true;
    int width = array.length;
    int height = array[0].length;
    byte[][] filledArray = new byte[width][height];
    for(int y = 0; y < height; y++){
	for(int x = 0; x < width; x++){
	    filledArray[x][y] = array[x][y];
	}
    }

    // Calculate the delta_alpha
    double c = 2 * Math.PI * r;
    double deltaAlpha = 2 * Math.PI / c;
    int i = 0;
    Point tempMaskPoint = null;
    double alpha;
    double holeStartingAlpha = 0;
    double holeEndAlpha = 2 * Math.PI;
    double centerX = center.getX();
    double centerY = center.getY();

    // If the "hole" is not complete, search for its starting edge
    boolean foundBreach = false; 
    boolean foundStartingAlpha = false;
    double alphaTest;
    while( (i * deltaAlpha) <= (2 * Math.PI) && (!foundStartingAlpha)){
      alpha = i * deltaAlpha;
      tempMaskPoint = _getInsideMaskPoint(array, center, alpha, (int)(r+5) );
      if(tempMaskPoint == null) {
	  foundBreach = true;
      }
      if(foundBreach && (tempMaskPoint != null)){
	holeStartingAlpha = alpha;
	foundStartingAlpha = true;
      }
      i++;
    }

    if(foundStartingAlpha){

      // Search endAlpha
      i = 0;
      boolean foundEndAlpha = false;
      while( (i*deltaAlpha+holeStartingAlpha) 
	     < (2*Math.PI+holeStartingAlpha) && (!foundEndAlpha)){
	alpha = i* deltaAlpha + holeStartingAlpha;
	tempMaskPoint = _getInsideMaskPoint(array, center, alpha, (int)(r+5) );
	if( tempMaskPoint == null ) {
	    foundEndAlpha = true;
	    holeEndAlpha = alpha;
	}
	i++;
      }
      if( !foundEndAlpha ) {
	  holeEndAlpha = holeStartingAlpha + 2*Math.PI - deltaAlpha;
	  isHoles = true;
      }
      else if( (holeEndAlpha - holeStartingAlpha) > Math.PI ) isHoles = true;
      else isHoles = false;
    }
    
    if(isHoles){

      // Fill holes
      i = 0;
      int maxDepth = (int)(r+5);
      int d = 0;
      int tempX;
      int tempY;
      double cosAlpha;
      double sinAlpha;
      boolean foundMaskPoint = false;
      while( (i * deltaAlpha + holeStartingAlpha) <= holeEndAlpha ){
	alpha = i * deltaAlpha + holeStartingAlpha;
	cosAlpha = Math.cos( alpha);
	sinAlpha = Math.sin( alpha );
	d = 0;
	foundMaskPoint = false;
	while( d <= maxDepth && (!foundMaskPoint) ){
	  tempX = (int) Math.floor( centerX + d * cosAlpha + 0.5 );
	  tempY = (int) Math.floor( centerY + d * sinAlpha + 0.5 );

	  // Check boundary and check if mask points is available along this
	  // direction
	  if(tempX >= 0 && tempY >= 0 && tempX < X_DIM && tempY < Y_DIM
	     && _getInsideMaskPoint(array, center, alpha, maxDepth) != null){
	      if(array[tempX][tempY] > 0) foundMaskPoint = true;
	      if(array[tempX][tempY] <= 0 ) 
		  filledArray[tempX][tempY] = maskValue;
	  }
	  d++;
	}
	i++;
      }
    }
    if(isHoles) return filledArray;
    else return null;
  }

  /**
   * Whether there is a hole in the given section of the mask.
   *
   * @param array An array containing the mask.
   * @param center The center of the mask.
   * @param r The largest distance between the edge of the mask and the center.
   * @param holesBeginAlpha The begin alpha of the section.
   * @param holesEndAlpha The end alpha of the section.
   */
  private boolean _isHoles(byte[][] array, Point center, double r,
			   double holesBeginAlpha, double holesEndAlpha,
			   double deltaAlpha)
  {
    boolean isHoles = true;

    // Check the edge of hole on holesBegin and holeEnd and see if it is
    // surrounded by mask points beyond holeBegin and holeEnd
    int depth = (int) Math.floor( r + 5 + 0.5 );
    Point point0 = _getMaskEdgePoint(array, center, 
				     (holesBeginAlpha - deltaAlpha), depth);
    Point point1 = _getMaskEdgePoint(array, center, holesBeginAlpha, depth);
    if(point0 == null || point1 == null) return false;

    double dis1 = _getDistance(point0, point1);
    Point point2 = _getMaskEdgePoint(array, center, holesEndAlpha, depth);
    Point point3 = _getMaskEdgePoint(array, center,
				     (holesEndAlpha + deltaAlpha), depth );
    if(point2 == null || point3 == null) return false;
    double dis2 = _getDistance(point2, point3);

    // If both dis1 and dis2 are larger than GAP, it is not considered a hole
    double GAP = 5;
    if( dis1 >= GAP && dis2 >= GAP ) isHoles = false;

    // If both dis1 and dis2 are smaller than GAP, it is considered a hole
    else if (dis1 < GAP && dis2 < GAP ) isHoles = true;

    // If only one side is larger than GAP and the section covers over PI/2,
    // it is considered a hole
    else {
	if( (holesEndAlpha - holesBeginAlpha) > (Math.PI/2) ) isHoles = true;
	else isHoles = false;
    }
    return isHoles;
  }

  /**
   * Get the nearest mask point from the given center. The center is not a 
   * mask point.
   *
   * @param array The mask array.
   * @param center The center of the mask.
   * @param alpha The searching direction.
   * @param maxDepth The maximum searching distance.
   */
  private Point _getInsideMaskPoint(byte[][] array, Point center, 
				    double alpha, int maxDepth)
  {
    double centerX = center.getX();
    double centerY = center.getY();
    double cosAlpha;
    double sinAlpha;
    int tempX;
    int tempY;

    cosAlpha = Math.cos( alpha );
    sinAlpha = Math.sin( alpha );
    int d = 0;
    while( d <= maxDepth ) {
	tempX = (int) Math.floor( centerX + d * cosAlpha + 0.5 );
	tempY = (int) Math.floor( centerY + d * sinAlpha + 0.5 );
	if( tempX >= 0 && tempY >= 0 && tempX < X_DIM && tempY < Y_DIM ){
	    if( array[tempX][tempY] > 0 ) {

		// Found the mask point and return
		Point point = new Point(tempX, tempY);
		return point;
	    }
	}
	d++;
    }

    // If not found the edge mask point, return null
    return null;

  }

  /**
   * Get the mask edge point.
   *
   * @param array The mask array.
   * @param center The center of the mask.
   * @param alpha The searching direction.
   * @param depth The searching distance.
   */
  private Point _getMaskEdgePoint(byte[][] array, Point center, double alpha, 
				  int depth)
  {
    double centerX = center.getX();
    double centerY = center.getY();
    double cosAlpha;
    double sinAlpha;
    int tempX;
    int tempY;

    cosAlpha = Math.cos( alpha );
    sinAlpha = Math.sin( alpha );
    while( depth >= 0 ) {
	tempX = (int) Math.floor( centerX + depth * cosAlpha + 0.5 );
	tempY = (int) Math.floor( centerY + depth * sinAlpha + 0.5 );
	if( tempX >= 0 && tempY >= 0 && tempX < X_DIM && tempY < Y_DIM ){
	    if( array[tempX][tempY] > 0 ) {

		// Found the edge mask point and return
		Point point = new Point(tempX, tempY);
		return point;
	    }
	}
	depth--;
    }

    // If not found the edge mask point, return null
    return null;
  }

  private Point _getHoleEdgePoint(byte[][] array, Point center, double alpha,
				   int depth, boolean isDisplayEnabled)
  {
    double centerX = center.getX();
    double centerY = center.getY();
    double cosAlpha;
    double sinAlpha;
    int tempX;
    int tempY;
    boolean foundMaskEdge = false;

    cosAlpha = Math.cos( alpha );
    sinAlpha = Math.sin( alpha );
    while( depth >= 0 ) {
	tempX = (int) Math.floor( centerX + depth * cosAlpha + 0.5 );
	tempY = (int) Math.floor( centerY + depth * sinAlpha + 0.5 );
	if( tempX >= 0 && tempY >= 0 && tempX < X_DIM && tempY < Y_DIM ){
	    if( array[tempX][tempY] > 0 && ( !foundMaskEdge ) ) {
		foundMaskEdge = true;
	    }
	    if( array[tempX][tempY] <= 0 && foundMaskEdge) {

		// Found the edge mask point and return
		Point point = new Point(tempX, tempY);
		return point;
	    }
	}
	depth--;
    }

    // If not found the edge mask point, return null
    return null;
  }
      

  /**
   * Get the distance between the two points.
   *
   * @param point1 The first point.
   * @param point2 The second point.
   *
   * @return The distance between point1 and point2.
   */
  private double _getDistance(Point point1, Point point2)
  {
      double x1 = point1.getX();
      double y1 = point1.getY();
      double x2 = point2.getX();
      double y2 = point2.getY();

      double dis = Math.sqrt( (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) );
      return dis;
  }


  /**
   * Fill the holes in a particular section in the mask.
   *
   * @param array An array containing the mask.
   * @param center The center of the mask.
   * @param r The largest distance between the edge of the mask and the center.
   * @param holesBeginAlpha The begin alpha of the section.
   * @param holesEndAlpha The end alpha of the section.
   * @param maskValue The pixel value of mask.
   * 
   * @return A byte array with the filled holes.
   */
  private byte[][] _fillHoles(byte[][] array, Point center, double r,
			      double holesBeginAlpha, double holesEndAlpha,
			      double deltaAlpha, byte maskValue)
  {
    int width = array.length;
    int height = array[0].length;
    byte[][] filledArray = new byte[width][height];
    for(int y = 0; y < height; y++){
	for(int x = 0; x < width; x++){
	    filledArray[x][y] = array[x][y];
	}
    }
    int i = 0;
    boolean isHoles = true;
    boolean foundMaskEdge = false;
    double centerX = center.getX();
    double centerY = center.getY();
    double cosAlpha;
    double sinAlpha;
    int tempX;
    int tempY;
    int depth;

    // Fill the holes
    while ( (i * deltaAlpha + holesBeginAlpha ) <= holesEndAlpha ){
      cosAlpha = Math.cos( i * deltaAlpha + holesBeginAlpha);
      sinAlpha = Math.sin( i * deltaAlpha + holesBeginAlpha );
      depth = (int)Math.floor(r+0.5);
      foundMaskEdge = false;
      while( depth >= 0 ){
	tempX = (int) Math.floor( centerX + depth * cosAlpha + 0.5 );
	tempY = (int) Math.floor( centerY + depth * sinAlpha + 0.5 );
	if(tempX >= 0 && tempY >= 0 && tempX < X_DIM && tempY < Y_DIM ){
	  if(array[tempX][tempY] > 0) foundMaskEdge = true;
	  if(array[tempX][tempY] <= 0 && foundMaskEdge){

	    // fill the holes
	    filledArray[tempX][tempY] = maskValue;
	  }
	}
	depth--;
      }
      i++;
    }
    return filledArray;
  }

  /**
   * The given point has the value of zero. Search around the point for 
   * the nearest point which has non-zero value.
   *
   * @param 
   */
  private Point _findNearestNonZeroPoint(byte[][] array, Point center)
  {
      // Find the radius of the mask 
      int r  = (int) Math.floor(_getMaxRadius(array, center));
      double deltaAlpha = Math.PI * 2 / 30;
      double cosAlpha;
      double sinAlpha;
      boolean foundMaskPoint = false;
      int d;
      int tempI = 0;
      int tempX;
      int tempY;
      double centerX = center.getX();
      double centerY = center.getY();
      Vector maskPoints = new Vector(5, 5);
      int i = 0;
      while(i * deltaAlpha < (2 * Math.PI) ){
	cosAlpha = Math.cos( i * deltaAlpha );
	sinAlpha = Math.sin( i * deltaAlpha );
	  
	d = 0;
	foundMaskPoint = false;
	while(d <= r && (!foundMaskPoint)){

	  // Calculate the coords of the point
	  tempX = (int) Math.floor( centerX + d * cosAlpha + 0.5 );
	  tempY = (int) Math.floor( centerY + d * sinAlpha + 0.5 );

	  if(tempX >= 0 && tempY >= 0 && tempX < X_DIM && tempY < Y_DIM){
	    if( array[tempX][tempY] > 0){
	      maskPoints.add(new Integer(d));
	      foundMaskPoint = true;
	    }
	    if(d == r && (!foundMaskPoint)) 
	      maskPoints.add(new Integer((int)r));
	  }
	  d++;
	}
	i++;
      }
      // Find the minimum distance
      int minD = (int)r;
      int minIndex = 0;
      int temp;
      for(int k = 0; k < maskPoints.size(); k++){
	  temp = ( (Integer)maskPoints.elementAt(k) ).intValue();
	  if(temp < minD) {
	      minD = temp;
	      minIndex = k;
	  }
      }
      if(minD < r){
	 
        // Compute the new mask center
	cosAlpha = Math.cos(minIndex * deltaAlpha);
	sinAlpha = Math.sin(minIndex * deltaAlpha);
	tempX = (int) Math.floor(minD * cosAlpha + centerX + 0.05 );
	tempY = (int) Math.floor(minD * sinAlpha + centerY + 0.05 );
	Point newCenter = new Point(tempX, tempY);
	return newCenter;
      }
      else return null;
  }

  /**
   * Get the center of the mask.
   *
   * @param array A byte array containing the mask.
   */
  private Point _getCenter(byte[][] array)
  {
    // Collect the Mask points
    Vector maskPoints = new Vector(5, 5);
    for(int y = 0; y < Y_DIM; y++ ){
      for(int x = 0; x < X_DIM; x++ ){
	if(array[x][y] > 0) maskPoints.add(new Point(x, y));
      }
    }
    if(maskPoints.size() == 0) return null;

    Point tempPoint = (Point)maskPoints.elementAt(0);
    _maskValue = array[(int)tempPoint.getX()][(int)tempPoint.getY()];

    // Convert mask point vector to array
    Point[] pointArray = new Point[maskPoints.size()];
    for(int i = 0; i < maskPoints.size(); i++){
	pointArray[i] = (Point)maskPoints.elementAt(i);
    }

    // Calculate the center of the mask
    Point center = _getAverage( pointArray );
    return center;
  }

  /**
   * Find a direction that doesn't pass any holes.
   *
   * @param array The mask array.
   * @param center The center of the mask.
   * @param r The radius of the mask (normally the radius for the point that
   *          is farthest away from the center.
   */
  private double _getSolidPass(byte[][] array, Point center, double r)
  {
    int i = 0;
    double centerX = center.getX();
    double centerY = center.getY();
    double cosAlpha;
    double sinAlpha;
    int tempX;
    int tempY;
    int depth;
    boolean foundMaskEdge = false;
    double startingAlpha  = 0;
    boolean foundHoles = false;

    // Calculate the delta_alpha
    double c = 2 * Math.PI * r;
    double deltaAlpha = 2 * Math.PI / c;

    i = 0;
    boolean foundDirection = false;
    while( i * deltaAlpha <= (2 * Math.PI) && (!foundDirection) ){
      cosAlpha = Math.cos( i * deltaAlpha );
      sinAlpha = Math.sin( i * deltaAlpha );

      // Probe the points up to distance of r
      depth = (int)Math.floor( r + 0.5 );
      foundMaskEdge = false;
      foundHoles = false;
      while( depth >= 0 && (!foundHoles) ){

	// Calculate the coords of the point
	tempX = (int) Math.floor( centerX + depth * cosAlpha + 0.5 );
	tempY = (int) Math.floor( centerY + depth * sinAlpha + 0.5 );
	if(tempX >= 0 && tempX < X_DIM && tempY >= 0 && tempY < Y_DIM){
	  if( array[tempX][tempY] > 0 ) foundMaskEdge = true;
	  if( array[tempX][tempY] <= 0 && foundMaskEdge ) {
	      foundHoles = true;
	  }
	}
	depth--;
      }
      if(!foundHoles) {
	  startingAlpha = i * deltaAlpha;
	  foundDirection = true;
      }
      i++;
    }
    if( foundDirection ) return startingAlpha;
    else return -1;
  }

    /**
     * Find the farthest distance between the given point and the edge of the
     * the mask.
     *
     * @param center The center of the mask.
     * 
     * @return An int value of the farthest distance between the center and a 
     *         point on the edge of the mask.
     */
    private double _getMaxRadius(byte[][] array, Point center)
    {
      // Calculate the boundary points. In order to calculate North point (the 
      // point on the north edge of brain ), a group of pixels will be 
      // identified first, then the center of the group will be computed and 
      // used as the North point.
      int MIN_GROUP_NUM = 1;
      Point north = _getNorth(array,MIN_GROUP_NUM );
      Point south = _getSouth(array,MIN_GROUP_NUM );
      Point west = _getWest(array, MIN_GROUP_NUM);
      Point east = _getEast(array, MIN_GROUP_NUM);  
      double dist_N = center.getY() - north.getY();
      double dist_S = south.getY() - center.getY();
      double dist_W = center.getX() - west.getX();
      double dist_E = east.getX() - center.getX();
      double r = 0;

      if(dist_N>dist_S && dist_N>dist_W && dist_N>dist_E) r = dist_N;
      else if (dist_S>dist_N && dist_S>dist_W && dist_S>dist_E) r = dist_S;
      else if (dist_W>dist_N && dist_W>dist_S && dist_W>dist_E) r = dist_W;
      else r = dist_E;
      return r;
    }


 /**
  * Get the center of the 
  */
  private Point _getAverage(Point[] points)
  {
    if(points == null || points.length == 0) return null;
    if(points.length == 1) return points[0];
    double sum_x = 0;
    double sum_y = 0;
    int size = points.length;
    for(int i = 0; i < size; i++){
	sum_x += points[i].getX();
	sum_y += points[i].getY();
    }
    int avg_x = (int)Math.floor( sum_x / size + 0.5);
    int avg_y = (int)Math.floor( sum_y / size + 0.5);
    return new Point(avg_x, avg_y);
  }

  /**
   * Get the northest point in the given mask. To avoid the influence of noise,
   * a group of northest points, including more than MIN_GROUP_NUM pionts, 
   * will be found and the center of the group will be considered the northest 
   * point.
   */
  private Point _getNorth(byte[][] mask, int MIN_GROUP_NUM)
  {
    Vector group = new Vector(5, 5);
    int counter = 0;
    for(int y = 0; y < mask[0].length; y++){
      for(int x = 0; x < mask.length; x++){
	if(mask[x][y] > 0 ){
	  group.add(new Point(x, y));
	  counter++;
	}
      }
      if(counter >= MIN_GROUP_NUM) break;
    }

    Point[] pointArray = new Point[group.size()];
    for(int i = 0; i < group.size(); i++){
	pointArray[i] = (Point) group.elementAt(i);
    }
    return _getAverage(pointArray);
  }

  /**
   * Get the southest point in the given mask. To avoid the influence of noise,
   * a number of southest points, including more than MIN_GROUP_NUM, will be 
   * found and the center of the group will be considered the southest point.
   */
  private Point _getSouth(byte[][] mask, int MIN_GROUP_NUM)
  {
    Vector group = new Vector(5, 5);
    int counter = 0;
    int height = mask[0].length;
    int width = mask.length;
    for(int y = (height-1); y >= 0; y--){
      for(int x = (width-1); x >= 0; x--){
	if(mask[x][y] > 0 ){
	  group.add(new Point(x, y));
	  counter++;
	}
      }
      if(counter >= MIN_GROUP_NUM) break;
    }

    Point[] pointArray = new Point[group.size()];
    for(int i = 0; i < group.size(); i++){
	pointArray[i] = (Point) group.elementAt(i);
    }
    return _getAverage(pointArray);
  }

  /**
   * Get the westest point in the given mask. To avoid the influence of noise,
   * a number of westest points, including more than MIN_GROUP_NUM, will be 
   * found and the center of the group will be considered the westest point.
   */
  private Point _getWest(byte[][] mask, int MIN_GROUP_NUM)
  {
    Vector group = new Vector(5, 5);
    int counter = 0;
    int height = mask[0].length;
    int width = mask.length;
    for(int x = 0; x < width; x++){
      for(int y = 0; y < height; y++){
	if(mask[x][y] > 0 ){
	  group.add(new Point(x, y));
	  counter++;
	}
      }
      if(counter >= MIN_GROUP_NUM) break;
    }

    Point[] pointArray = new Point[group.size()];
    for(int i = 0; i < group.size(); i++){
	pointArray[i] = (Point) group.elementAt(i);
    }
    return _getAverage(pointArray);
  }

  /**
   * Get the eastest point in the given mask. To avoid the influence of noise,
   * a number of eastest points, including more than MIN_GROUP_NUM, will be 
   * found and the center of the group will be considered the eastest point.
   */
  private Point _getEast(byte[][] mask, int MIN_GROUP_NUM)
  {
    Vector group = new Vector(5, 5);
    int counter = 0;
    int height = mask[0].length;
    int width = mask.length;
    for(int x = (width-1); x >= 0; x--){
      for(int y = (height-1); y >= 0; y--){
	if(mask[x][y] > 0 ){
	  group.add(new Point(x, y));
	  counter++;
	}
      }
      if(counter >= MIN_GROUP_NUM) break;
    }

    Point[] pointArray = new Point[group.size()];
    for(int i = 0; i < group.size(); i++){
	pointArray[i] = (Point) group.elementAt(i);
    }
    return _getAverage(pointArray);
  }
}
