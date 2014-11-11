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
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.imageio.ImageReader;

import FileIO.MetadataParser;

/**
 * Application to initialize a zero level set circle in a 2D slice resampled 
 * from a given data volume.
 *
 * @author Haihong Zhuang and Daniel J. Valentino
 * @version 2 September 2005
 */
public class ZeroLSInitializer
{

  /** An 4 element double array: the first element is x-coord of the center,
   * the second element is y-coord of the center, the third element is 
   * index of the 2D slice, and the fourth element is the radius of the 
   * circle. */
  double[] _params = new double[2];

  /** It is the intensity minimum below which lies 2% of the cumulative
   * histogram.  */ 
  private double _intens2 = 0;

  /** It is the intensity maxmum below which lies 98% of the cumulative
   * histogram.  */
  private double _intens98 = 0;

  /** Format names supported by volume constructor.*/
  private static String[] FORMAT_TYPES = { "analyze", "minc", "dicom", "ge", 
					   "ucf"};

  /** 
   * Construct ZeroLSInitializer. 
   *
   * @param imageReader An ImageReader object associated with the brain data
   *                    to skull-strip.
   * @param skullStripper A vector of ActiveContour objects.
   * @param intens2  The intensity minimum below which lies 2% of the 
   *                 cumulative histogram.
   * @param intens98 The intensity maxmum below which lies 98% of the 
   *                 cumulative histogram.
   * @param resamplingOrient The orientation of 2D slices resampled from the 
   *                         data volume and stored in the skullStrippers.
   */
  public ZeroLSInitializer(ImageReader imageReader, SkullStripper skullStripper, 
			   double intens2, double intens98, 
			   int resamplingOrient)
  {
      _intens2 = intens2;
      _intens98 = intens98;
      _params = initializeZeroLS(imageReader, skullStripper,resamplingOrient);
  }
  /**
   *  Get x
   * 
   * 
   */
  public double x()
  {
	  
	  return _params[0];
  }
  /**
   *  Get x
   * 
   * 
   */
  public double y()
  {
	  
	  return _params[1];
  }
  
  /**
   * Get the parameters to initialize zero level set.
   *
   * @return An 4 element double array: the first element is x-coord of the 
   *         center, the second element is y-coord of the center, the third 
   *         element is index of the 2D slice, and the fourth element is the 
   *         radius of the circle.
   */
    public double[] getInitialParameters()
    {
	return _params;
    }

  /**
   * Initialize level set by computing the brain center and the radius.
   *
   * @param imageReader An ImageReader object associated with the brain data
   *                    to skull-strip.
   * @param skullStrippers A vector of ActiveContour objects.
   * @param resamplingOrient The orientation of 2D slices resampled from the 
   *                         data volume and stored in the skullStrippers.
   * @return An 4 element double array: the first element is x-coord of the 
   *         center, the second element is y-coord of the center, the third 
   *         element is index of the 2D slice, and the fourth element is the 
   *         radius of the circle.
   */
  public double[] initializeZeroLS(ImageReader imageReader, 
				   SkullStripper skullStripper, int resamplingOrient)
  {
    int initialIndex = 0;
    int centerX;
    int centerY;
    double r;

    // Select the slice where the zero level set will be initialized. Usually 
    // select the middle slice of all slice.
    BufferedImage image = null;
    try{
     
    
      image = skullStripper.getInputImage();
    }catch(Exception e){
      e.printStackTrace();
    }

    // Calculate the mask by removing the background from the original image
    // slice. The 80% histogram is used as the threshold to distinguish 
    // image pixels from the background pixels and human skin.   
    int width = image.getWidth();
    int height = image.getHeight();
    byte[][] mask = new byte[width][height];
    byte maskValue = 1;
    int temp;
    double intens80  = (_intens98 - _intens2) * 0.8 + _intens2;//*0.8
    for (int y = 0; y < height; y++){
      for(int x = 0; x < width; x++){
	temp = image.getRaster().getSample(x, y, 0);
	if(temp > intens80) {
	    mask[x][y] = maskValue;
	}
      }
    }

    // Search the boundaries of the head. In order to calculate North point
    // (the point on the north edge of brain ), a group of pixels will be 
    // identified first, then the center of the group will be computed and used
    // as the North point.
    double thickness = 5;
    MetadataParser metadataParser = new MetadataParser(imageReader);
    float xyDim = _getXYDim(metadataParser);
    int MIN_GROUP_NUM = (int)Math.floor( thickness / xyDim + 0.5);
    Point north = _getNorth(mask,MIN_GROUP_NUM );
    Point south = _getSouth(mask,MIN_GROUP_NUM );
    Point west = _getWest(mask, MIN_GROUP_NUM);
    Point east = _getEast(mask, MIN_GROUP_NUM);  

//     Point newCenter = null;
    int headDiameter;

    // Get heading direction
    float[] signedPixDims = metadataParser.getSignedPixDims();
    int heading = _getHeadingDirection(metadataParser.getFormatName(), 
				       resamplingOrient, signedPixDims);

    // Initialize according to orientation and head position
    if(resamplingOrient == 2){
      if(heading == 0 || heading == 2){
	
	//CenterX is determined by westX and eastX (the middle between the two)
	centerX = (int)Math.floor(( east.getX() + west.getX() ) / 2 + 0.5);
	headDiameter = (int)Math.floor(( east.getX() - west.getX()) + 0.5); 

	if(heading == 0){
	    // CenterY is determined by northY and disWE/3
	    centerY = (int)Math.floor((north.getY() + headDiameter / 3) + 0.5);
	}else {
	    centerY = (int)Math.floor((south.getY() - headDiameter / 3) + 0.5);
	}
      }
      else {
	//CenterX is determined by northX and southX 
	centerX = (int)Math.floor((north.getX() + south.getX()) / 2 + 0.5);
	headDiameter = (int)Math.floor(( south.getX() - north.getX()) + 0.5); 

	if(heading == 1) {
	    centerY = (int)Math.floor((east.getY() - headDiameter / 3) + 0.5);
	}else{
	    centerY = (int)Math.floor((west.getY() + headDiameter / 3) + 0.5);
	}
      }

      r = headDiameter / 6;
    }
    else {
      if(heading == 0 || heading == 2){
       
	//CenterX is determined by westX and eastX (the middle between the two)
	centerX = (int)Math.floor(( east.getX() + west.getX() ) / 2 + 0.5);
	headDiameter = (int)Math.floor(( east.getX() - west.getX()) + 0.5); 

	if(heading == 0){
	    centerY = (int)Math.floor((north.getY() + headDiameter / 2) + 0.5);
	}else {
	    centerY = (int)Math.floor((south.getY() - headDiameter / 2) + 0.5);
	}
      }
      else {

	//CenterX is determined by northX and southX (the middle between them)
	centerX = (int)Math.floor(( north.getX() + south.getX())/ 2 + 0.5);
	headDiameter = (int)Math.floor(( south.getX() - north.getX()) + 0.5); 

	if(heading == 1){
	    centerY = (int)Math.floor((east.getY() - headDiameter / 2) + 0.5);
	}else {
	    centerY = (int)Math.floor((west.getY() + headDiameter / 2) + 0.5);
	}
     }

      r = headDiameter / 4;
    }

    double[] ans = new double[4];
    ans[0] = centerX;
    ans[1] = centerY;
   
    return ans;
  }

  /**
   * Get heading direction in the given orientation. 
   *
   * @param format The image file's format (i.e., Analyze, Minc). Minc has 
   *               defined the coordinate system, which is the left-heeled 
   *               coordinate system (x positive points from left to the right,
   *               y positive points from posterior to anterior, z positive 
   *               points from superior to inferiro. Note that if the step 
   *               size is negative, then the corresponding axis become 
   *               negative values and increasing direction is reversed!) 
   *               Analyze format doesn't define coordinate system, but both
   *               SPM and Debabeler set the coordinate system as left-heeled
   *               system while saving data in Analyze format.
   * @param orient The orient to sample the data volume.
   * @param metadataParser The parser of the image file's metadata. 
   * @return An int value. 0 indicates the superior or posterior is at north 
   *         in the slice resampled in the given orientation, 1 indicates the
   *         superior or posterior is at east, 2 indicates the superior or 
   *         posterior is at south, 3 indicates the superior or posterior is 
   *         at west. 
   */
    private int _getHeadingDirection(String format, int resamplingOrient, 
				     float[] signedPixDims)
    {
      int heading = 0;

      // Axial orientation
      if(resamplingOrient == 0){

	// Check heading direction based on file format
	if(format.equals(FORMAT_TYPES[0])||format.equals(FORMAT_TYPES[1])){

	    // Get yStep and check it is positive or negative
	    if(signedPixDims[1] >= 0) heading = 0;
	    else heading = 2;
	}
      }

      // Coronal orientation
      else if(resamplingOrient == 1){

	// Check heading direction based on file format
	if(format.equals(FORMAT_TYPES[0])||format.equals(FORMAT_TYPES[1])) {

	    // Get zStep and check it is positive or negative
	    if(signedPixDims[2] >= 0) heading = 2;
	    else heading = 0;
	}
      }

      // Sagittal orientation
      else if(resamplingOrient == 2){

	// Check heading direction based on file format
	if(format.equals(FORMAT_TYPES[0])||format.equals(FORMAT_TYPES[1])) {

	    // Get yStep and check it is positive or negative
	    if(signedPixDims[2] >= 0) heading = 2;
	    else heading = 0; 
	}
      }
      else {
	  if(format.equals(FORMAT_TYPES[0])) heading = 2;
	  else if(format.equals(FORMAT_TYPES[1])) heading = 0;
	  else heading = 2;
      }
      return heading;
    }

  /**
   * Get the northmost point in the given mask. To avoid the influence of noise,
   * a group of northmost points, including more than MIN_GROUP_NUM pionts, 
   * will be found and the center of the group will be considered the northmost 
   * point.
   */
  private Point _getNorth(byte[][] mask, int MIN_LINE_NUM)
  {
    Vector group = new Vector(5, 5);
    int counter = 0;
    Point outputP = null;
    if(true){
      boolean isMaskPoint = false;
      for(int y = 0; y < mask[0].length; y++){
	isMaskPoint = false;

	for(int x = 0; x < mask.length; x++){

	  // Search for mask point
          if(mask[x][y] > 0){
	    isMaskPoint = true;
	    counter++;
	    x = mask.length;
	  }
	}
	// if isMaskPoint = false, reset counter
	if( !isMaskPoint) counter = 0;

	// If counter >= MIN_LINE_NUM return 
	if(counter >= MIN_LINE_NUM) {
	    int temp = (int) Math.floor(MIN_LINE_NUM / 2 + 0.5);
	    int tempY = y - temp;
	    int tempX = (int) mask.length / 2;
	    outputP = new Point(tempX, tempY);
	    return outputP;
	}
      }
    }
    return outputP;
  }

  /**
   * Get the southmost point in the given mask. To avoid the influence of noise,
   * a number of southmost points, including more than MIN_GROUP_NUM, will be 
   * found and the center of the group will be considered the southmost point.
   */
  private Point _getSouth(byte[][] mask, int MIN_LINE_NUM)
  {
    Vector group = new Vector(5, 5);
    int counter = 0;
    int height = mask[0].length;
    int width = mask.length;
    Point outputP = null;

    if(true){
      boolean isMaskPoint = false;
      for(int y = (height-1); y >= 0 ; y--){
	isMaskPoint = false;
	for(int x = (width-1); x >= 0; x--){
	  // Search for mask point
          if(mask[x][y] > 0){
	    isMaskPoint = true;
	    counter++;
	    x = 0;
	  }
	}
	// if isMaskPoint = false, reset counter
	if( !isMaskPoint) counter = 0;

	// If counter >= MIN_LINE_NUM return 
	if(counter >= MIN_LINE_NUM) {
	    int temp = (int)Math.floor(MIN_LINE_NUM / 2 + 0.5);
	    int tempY = y + temp;
	    int tempX = (int) mask.length / 2;
	    outputP = new Point(tempX, tempY);
	    return outputP;
	}
      }
    }
    return outputP;
  }

  /**
   * Get the westmost point in the given mask. To avoid the influence of noise,
   * a number of westmost points, including more than MIN_GROUP_NUM, will be 
   * found and the center of the group will be considered the westest point.
   */
  private Point _getWest(byte[][] mask, int MIN_LINE_NUM)
  {
    Vector group = new Vector(5, 5);
    int counter = 0;
    int height = mask[0].length;
    int width = mask.length;
    Point outputP = null;

    if(true){
      boolean isMaskPoint = false;
      for(int x = 0; x < width; x++){
	isMaskPoint = false;

	for(int y = 0; y < height; y++){

	  // Search for mask point
          if(mask[x][y] > 0){
	    isMaskPoint = true;
	    counter++;
	    y = height;
	  }
	}
	// if isMaskPoint = false, reset counter
	if( !isMaskPoint) counter = 0;

	// If counter >= MIN_LINE_NUM return 
	if(counter >= MIN_LINE_NUM) {
	    int temp = (int) Math.floor(MIN_LINE_NUM / 2 + 0.5);
	    int tempY = (int) height / 2;
	    int tempX = x - temp;
	    outputP = new Point(tempX, tempY);
	    return outputP;
	}
      }
    }
    return outputP;
  }

  /**
   * Get the eastmost point in the given mask. To avoid the influence of noise,
   * a number of eastmost points, including more than MIN_GROUP_NUM, will be 
   * found and the center of the group will be considered the eastest point.
   */
  private Point _getEast(byte[][] mask, int MIN_LINE_NUM)
  {
    Vector group = new Vector(5, 5);
    int counter = 0;
    int height = mask[0].length;
    int width = mask.length;
    Point outputP = null;

    if(true){
      boolean isMaskPoint = false;
      for(int x = (width-1); x >= 0; x--){
	isMaskPoint = false;

	for(int y = (height-1); y >= 0; y--){

	  // Search for mask point
          if(mask[x][y] > 0){
	    isMaskPoint = true;
	    counter++;
	    y = 0;
	  }
	}
	// if isMaskPoint = false, reset counter
	if( !isMaskPoint) counter = 0;

	// If counter >= MIN_LINE_NUM return 
	if(counter >= MIN_LINE_NUM) {
	    int temp = (int) Math.floor(MIN_LINE_NUM / 2 + 0.5);
	    int tempY = (int) height / 2;
	    int tempX = x + temp;
	    outputP = new Point(tempX, tempY);
	    return outputP;
	}
      }
    }
    return outputP;
  }

  /**
   * Get the 2D resolusions. The pixDims contains 3D resolusions. The 2D
   * will be obtained by comparing the three elements of pixDims array,
   * and by assuming that the pixels are square, that is, the 
   * resolusions are the same in x and y directions. So if two elements 
   * in pixDims are the same, it is considered the 2D resolusions,
   */
    private float _getXYDim(MetadataParser metadataParser)
    {
	float xyDim = 1.0f;
	float[] pixDims = metadataParser.getPixDims();

	// Get the 2D dimensions. The pixDims contains 3D dimensions. The 2D
	// will be obtained by comparing the three elements of pixDims array,
	// and by assuming that the pixels are square, that is, the 
	// resolusions are the same in x and y directions. So if two elements 
	// in pixDims are the same, it is considered the 2D resolusions,
	if( Math.abs(pixDims[0]-pixDims[1]) < (pixDims[0] * 0.1))
	    xyDim = pixDims[0];
	else if (Math.abs(pixDims[0]-pixDims[2]) < (pixDims[0] * 0.1))
	    xyDim = pixDims[0];
	else if (Math.abs(pixDims[1]-pixDims[2]) < (pixDims[1] * 0.1))
	    xyDim = pixDims[1];
	else xyDim = 1.0f; 
	return xyDim;
    }

}
