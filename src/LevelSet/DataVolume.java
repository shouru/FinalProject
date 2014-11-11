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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Vector;

import javax.imageio.ImageReader;

import FileIO.MetadataParser;


/**
 * Class to reconstruct a data volume from the queried file.
 *
 * @author Haihong Zhuang and Daniel J. Valentino
 * @version 2 September 2005
 */
public class DataVolume
{

    /** Width (X) of the data volume. */
    protected int _width;

    /** Height (Y) of the data volume. */
    protected int _height;

    /** Depth (Z) of the data volume. */
    protected int _depth;

    /** The intensity minimum below which lies 2% of the cumulative histogram. 
     */
    private int _intens2Percent = 0;

    /** The object of metadataParser. */
    private MetadataParser _metadataParser;

    /** Data volume. */
    private short[][][] _volShort = null;

    /** Data volume. */
    private byte[][][] _volByte = null;

    /** Histogram of the volume. */
    private int[] _histogram = null;

    /** File format. */
    private String _format = "analyze";

   /**
    * Constructs DataVolume.
    *
    * @param reader ImageReader to parse the queried file.
    */
    public DataVolume(ImageReader reader)
    {
	this(reader, 0, false, false);
    }

   /**
    * Constructs DataVolume.
    *
    * @param reader ImageReader for parsing the queried file.
    * @param rotate The number of rotation of the images provided by the given
    *               imageReader. The images would be rotated (number * 90) 
    *               degrees clockwisely.
    * @param isXFlipEnabled Whether to flip the images around x axis.
    * @param isYFlipEnabled Whether to flip the images around y axis.
    */
    public DataVolume(ImageReader reader, int rotate, boolean isXFlipEnabled,
		      boolean isYFlipEnabled)
    {

      // Create a MetadataParser object
      _metadataParser = new MetadataParser(reader);
      BufferedImage[] images = _getBufferedImages(reader, rotate, 
						  isXFlipEnabled, 
						  isYFlipEnabled);
      _format = _metadataParser.getFormatName();
	
      // Create data volume
      if(_metadataParser.getBitsPP() <= 8 ) {
	_volByte= _createByteDataVolume(images, _metadataParser.getOrient(), 
					_metadataParser.getReNormValues() );
	_histogram = _calculateHistogram(_volByte);
      }
      else {
	_volShort =_createShortDataVolume(images, _metadataParser.getOrient(), 
					  _metadataParser.getReNormValues() );
	_histogram = _calculateHistogram(_volShort);
      }
//       System.out.println("(X, Y, Z) = "+_width+ ", "+ _height+", "+_depth); 
    }

   /**
    * Constructs DataVolume.
    *
    * @param imgReaders A vector of ImageReaders for parsing the queried files.
    */
    public DataVolume(Vector imgReaders)
    {
	this(imgReaders, 0, false, false);
    }

   /**
    * Constructs DataVolume.
    *
    * @param imgReaders A vector of ImageReaders for parsing the queried files.
    * @param rotate The number of rotation of the images provided by the given
    *               imageReader. The images would be rotated (number * 90) 
    *               degrees clockwisely.
    * @param isXFlipEnabled Whether to flip the images around x axis.
    * @param isYFlipEnabled Whether to flip the images around y axis.
    */
    public DataVolume(Vector imgReaders, int rotate, boolean isXFlipEnabled,
		      boolean isYFlipEnabled)
    {
      _metadataParser = new MetadataParser(imgReaders);

      _format = _metadataParser.getFormatName();

      // Check if all the files have the same file format, orient bitsPP and
      //  pixDims
      if(_metadataParser.isSame() ){
	BufferedImage[] bImgs = _getBufferedImages(imgReaders, rotate, 
						   isXFlipEnabled, 
						   isYFlipEnabled);

	if(_metadataParser.getBitsPP() <= 8 ) {
	  _volByte = _createByteDataVolume(bImgs, _metadataParser.getOrient());
	}
	else {
	  _volShort =_createShortDataVolume(bImgs,_metadataParser.getOrient());
	}
      }
    }

    /**
     * Constructs DataVolume.
     *
     * @param images BufferedImage array that contains image data.
     * @param orient Orientation of the image data.
     * @param bitsPP Bits per pixel.
     */
    public DataVolume(BufferedImage[] images, int orient, int bitsPP, 
		      String format)
    {
      // Create data volume
      if(bitsPP <= 8){
	_volByte= _createByteDataVolume(images, orient );
	_histogram = _calculateHistogram(_volByte);
      }
      else {
	_volShort =_createShortDataVolume(images, orient );
	_histogram = _calculateHistogram(_volShort);
      }
      _format = format;
    }

    /**
     * Gets bits per pixel.
     *
     * @return The number of bits per pixel.
     */
    public int getBitsPP()
    {
	return _metadataParser.getBitsPP();
    }

    /**
     * Gets the data volume.
     *
     * @return The data volume.
     */
    public Object getDataVolume()
    {
	if(_volByte != null) return _volByte;
	else return _volShort;
    }

    /**
     * Gets the name of the file format.
     *
     * @return The name of the file format.
     */
    public String getFileFormat()
    {
	return _metadataParser.getFormatName();
    }

    /**
     * Gets the histogram of the data volume.
     *
     * @return An int array of histogram.
     */
    public int[] getHistogram()
    {
	return _histogram;
    }

    /**
     * Gets the intensity value below which lies 2% of the cumulative 
     * histagram. 
     * 
     * @return The value of the instensity.
     */ 
    public int getIntensity2Percent()
    {
	//_histogram();
	_intens2Percent = 8;
// 	System.out.println("DataVolume: intens2Perc = "+_intens2Percent);
	return _intens2Percent;
    }

    /**
     * Gets the intensity value below which lies 2% of the cumulative 
     * histagram. 
     * 
     * @param array Data volume to be queried.
     * 
     * @return The value of the instensity.
     */ 
//     public int getIntensity2Percent(short[][][] array)
//     {
// 	_histogram(array);
// 	return _intens2Percent;
//     }

    /**
     * Gets the orientation of the image in the ImageReader object.
     *
     * @return A number indicating the orientation of the image. 0-axial, 
     *         1-coronal, 2-sagittal.
     */
    public int getOrient()
    {
	return _metadataParser.getOrient();
    }                 

    /**
     * Gets voxels' dims.
     *
     * @return A float array contains three elements with each representing
     *         the dimension on X, Y and Z directions.
     */
    public float[] getPixDims()
    {
	return _metadataParser.getPixDims();
    }

    /**
     * Gets scale ratios of the image from header file.
     *
     * @return An array of scale ratios in the order of the ratio in Y 
     *         direction, the ratio in X direction and the ratio in Z 
     *         direction.
     */
    public float[] getScaleRatios()
    {
	// get pix dims
	float[] pixdims = _metadataParser.getPixDims();

	float[] scaleRatios = new float[3];

	if(pixdims[0] == pixdims[1] && pixdims[1] == pixdims[2]) {
	    float[] tmp =  {1.0f, 1.0f, 1.0f};
	    return tmp;
	}

	// Check if any item of pixdims array is negative
	for(int i = 0; i < pixdims.length; i++){
	    if(pixdims[i] <= 0 ) {
		float[] tmp =  {1.0f, 1.0f, 1.0f};
		return tmp;
	    }
	}  

	// find a smallest resolution as the base resolution
	float base = 1.0f;
	if( (pixdims[1] <= pixdims[0]) && (pixdims[1] <= pixdims[2])){ 
	    base = pixdims[1];
	}
	else if ( (pixdims[2] <= pixdims[0]) && (pixdims[2] <= pixdims[1])){ 
	    base = pixdims[2];
	} 
	else { base = pixdims[0]; }	
	scaleRatios[1] = pixdims[0] / base;
	scaleRatios[0] = pixdims[1] / base;
	scaleRatios[2] = pixdims[2] / base;
	return scaleRatios;
    }

    /**
     * Gets the scale ratios of a section in a given orientation.
     *
     * @param orient Orientation of the section to be queried. If orient is 0, 
     *               it is axial; 1 is coronal; and 2 is sagittal.
     *
     * @throws IllegalArgumentException If the orientation value is invalid.
     *
     * @return An array of the scale ratios of the section in the given 
     *         orientation. The order of the array elements is scale ratio in
     *         X direction and scale ratio in Y direction.
     */
    public float[] getScaleRatios(int orient)
    {
	if(orient >= 3) {
	    throw new IllegalArgumentException("DataVolume: "+ orient + 
					       " is not a valid orientation. "+
					       "A valid orientation is 0, 1, "+
					       "or 2 which indicates axial, "+
					       "coronal and sagittal " +
					       "respectively.");
	}
	float[] ratios = getScaleRatios();
	float[] newRatios = new float[2];
	if(orient == 0) {
	    newRatios[0] = ratios[1];
	    newRatios[1] = ratios[0];
	}
	else if(orient == 1) {
	    newRatios[0] = ratios[1];
	    newRatios[1] = ratios[2];
	}
	else {
	    newRatios[0] = ratios[0];
	    newRatios[1] = ratios[2];
	}
	return newRatios;
    }

    /**
     * Gets the width of the data volume.
     *
     * @return The width of the data volume.
     */
    public int getWidth()
    {
	return _width;
    }

    /**
     * Gets the width of sections in a given orientation.
     *
     * @param orient Orientation of the sections to be queried. If orient is 
     *               0, it is axial; 1 is coronal; and 2 is sagittal.
     *
     * @throws IllegalArgumentException If the orientation value is invalid.
     *
     * @return The width of the sections in the given orientation.
     */
    public int getWidth(int orient)
    {
	if(orient >= 3) {
	    throw new IllegalArgumentException("DataVolume: "+ orient + 
					       " is not a valid orientation. "+
					       "A valid orientation is 0, 1, "+
					       "or 2 which indicates axial, "+
					       "coronal and sagittal " +
					       "respectively.");
	}
	if(orient == 0) return _width;
	else if(orient == 1) return _width;
	else return _height;
    }

    /**
     * Gets the height of the data volume.
     *
     * @return The height of the data volume.
     */
    public int getHeight()
    {
	return _height;
    }

    /**
     * Gets the height of sections in a given orientation.
     *
     * @param orient Orientation of the sections to be queried. If orient is 
     *               0, it is axial; 1 is coronal; and 2 is sagittal.
     *
     * @throws IllegalArgumentException If the orientation value is invalid.
     *
     * @return The height of the sections in the given orientation.
     */
    public int getHeight(int orient)
    {
	if(orient >= 3) {
	    throw new IllegalArgumentException("DataVolume: "+ orient + 
					       " is not a valid orientation. "+
					       "A valid orientation is 0, 1, "+
					       "or 2 which indicates axial, "+
					       "coronal and sagittal " +
					       "respectively.");
	}
	if(orient == 0) return _height;
	else if(orient == 1) return _depth;
	else return _depth;
    }  

    /**
     * Gets the depth of the data volume.
     *
     * @return The depth of the data volume.
     */
    public int getDepth()
    {
	return _depth;
    }

    /**
     * Gets the depth of sections in a given orientation.
     *
     * @param orient Orientation of the sections to be queried. If orient is 
     *               0, it is axial; 1 is coronal; and 2 is sagittal.
     *
     * @throws IllegalArgumentException If the orientation value is invalid.
     *
     * @return The depth of the sections in the given orientation.
     */
    public int getDepth(int orient)
    {
	if(orient >= 3) {
	    throw new IllegalArgumentException("DataVolume: "+ orient + 
					       " is not a valid orientation. "+
					       "A valid orientation is 0, 1, "+
					       "or 2 which indicates axial, "+
					       "coronal and sagittal " +
					       "respectively.");
	}
	if(orient == 0) return _depth;
	else if(orient == 1) return _height;
	else return _width;
    }

    public int getValue(int x, int y, int z)
    {
	int temp;
	if(_volByte != null) temp = _volByte[z][x][y] & 0xff;
	else temp = _volShort[z][x][y] & 0xffff;
	return temp;
    }

    /**
     * Calculate histogram of the volume.
     *
     * @param volume A byte array.
     * @return An int array.
     */
    private int[] _calculateHistogram(byte[][][] volume)
    {
      // Find the maximum intensity
      int maxI = 0;
      int tempInt;
      for(int z = 0; z < this.getDepth(); z++ ){
	for(int y = 0; y < this.getHeight(); y++ ){
	  for( int x = 0; x < this.getWidth(); x++ ){
	      tempInt = volume[z][x][y] & 0xff;
	    if(maxI < tempInt) maxI = tempInt;
	  }
	}
      }

      // Initialize the histogram array
      int[] hist = new int[maxI + 1];

      // Construct histogram array
      int intens;
      for(int z = 0; z < this.getDepth(); z++ ){
	for(int y = 0; y < this.getHeight(); y++ ){
	  for( int x = 0; x < this.getWidth(); x++ ){
	    intens = (int)volume[z][x][y];
	    intens = intens & 0xff;
	    hist[intens]++;
	  }
	}
      }
      return hist;
    }

    /**
     * Calculate histogram of the volume.
     *
     * @param volume A short array.
     * @return An int array.
     */
    private int[] _calculateHistogram(short[][][] volume)
    {
      // Find the maximum intensity
      int maxI = 0;
      for(int z = 0; z < this.getDepth(); z++ ){
	for(int y = 0; y < this.getHeight(); y++ ){
	  for( int x = 0; x < this.getWidth(); x++ ){
	    if(maxI < volume[z][x][y]) maxI = volume[z][x][y];
	  }
	}
      }

      // Initialize the histogram array
      int[] hist = new int[maxI + 1];

      // Construct histogram array
      int intens;
      for(int z = 0; z < this.getDepth(); z++ ){
	for(int y = 0; y < this.getHeight(); y++ ){
	  for( int x = 0; x < this.getWidth(); x++ ){
	    intens = (int)volume[z][x][y];
	    hist[intens]++;
	  }
	}
      }
      return hist;
    }

    /**
     * Creates a data volume from the given image reader.
     *
     * @param reader ImageReader object of the queried file.
     * @param orient Orientation of the image in the imageReader.
     * 
     * @return A 3D array of BYTE numbers.
     */
    private byte[][][] _createByteDataVolume(BufferedImage[] images,int orient)
    {
	byte[][][] vol = null;
	int width = 0;
	int height = 0;
	int depth = 0;
	try{
// 	    System.out.println("Loading " + images.length +" "
// 			       + _format +" images. Please wait...");

	    Raster raster;
	    int tempInt;
	    /** Get the images orientation:
	     *  0  transverse unflipped
	     *  1  coronal unflipped
	     *  2  sagittal unflipped
	     *  3  transverse flipped
	     *  4  coronal flipped
	     *  5  sagittal flipped
	     */
	    BufferedImage bImg;

	    // Transverse or axial orientation
	    if(orient == -1 || orient == 0 || orient == 3) {
	    
		// initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = bImg.getWidth();
		height = bImg.getHeight();
		depth = images.length;
		vol = new byte[depth][width][height];
		for(int j = 0; j < depth; j++){
		    bImg = images[j];
		    raster = bImg.getRaster();

		    // load into vol
		    for(int xIndex = 0; xIndex < width; xIndex++){
			for(int yIndex = 0; yIndex < height; yIndex++){
			    tempInt = raster.getSample(xIndex, yIndex, 0);
			    tempInt &= 0xff;
			    vol[j][xIndex][yIndex] = (byte)tempInt;
// 			    vol[depth -1-j][xIndex][height-yIndex-1] = 
// 				(byte)tempInt;
			}
		    }
		}
	    } 

	    // Coronal orientation 
	    else if(orient == 1 || orient == 4){
	    
		// initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = bImg.getWidth();
		height = images.length;
		depth = bImg.getHeight();
		vol = new byte[depth][width][height];
		for(int yIndex = 0; yIndex < height; yIndex++){
		    bImg = images[yIndex];
		    raster = bImg.getRaster();

		    // load into vol
		    for(int xIndex = 0; xIndex < width; xIndex++){
			for(int j = 0; j < depth; j++){
			    tempInt = raster.getSample(xIndex, j, 0);
			    tempInt &= 0xff;
			    vol[j][xIndex][yIndex] = (byte)tempInt;
			}
		    }
		}
	    }

	    // Sagittal orientation
	    else {

		// Initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = images.length;
		depth = bImg.getHeight();
		height = bImg.getWidth();
		vol = new byte[depth][width][height];

		for(int x = 0; x < width; x++){
		    bImg = images[x];
		    raster = bImg.getRaster();

		    // Load into vol
		    for(int z = 0; z < depth; z++){
			for(int y = 0; y < height; y++){
			    tempInt = raster.getSample(y, z, 0);
			    tempInt &= 0xff;
			    vol[z][x][y] = (byte)tempInt;
			}
		    }
		}
	    }
	} catch (Exception e){
	    e.printStackTrace();
	}
	_width = width;
	_height = height;
	_depth = depth;
	return vol;
    }

    /**
     * Creates a data volume from the given image reader.
     *
     * @param images  An ImageReader object for parsing the queried file
     * @param orient Orientation of the image in the ImageReader.
     * @param renorm An array of values for renormalization.
     * 
     * @return A 3D array of BYTE numbers.
     */
    private byte[][][] _createByteDataVolume(BufferedImage[] images, 
					     int orient, double[] renorm)
    {
	if(renorm == null) return _createByteDataVolume(images, orient);
	byte[][][] vol = null;
	int width = 0;
	int height = 0;
	int depth = 0;
	try{
// 	    System.out.println("Loading " + images.length +" "
// 			       + _format +" images. Please wait...");

	    Raster raster;
	    int tempInt;
	    BufferedImage bImg;

	    // Transverse or axial orientation
	    if(orient == -1 || orient == 0 || orient == 3) {
	    
		// Initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = bImg.getWidth();
		height = bImg.getHeight();
		depth = images.length;
		vol = new byte[depth][width][height];
		for(int j =0; j < depth; j++){
		    bImg = images[j];
		    raster = bImg.getRaster();

		    // load into vol
		    for(int xIndex = 0; xIndex < width; xIndex++){
			for(int yIndex = 0; yIndex < height; yIndex++){
			    tempInt = raster.getSample(xIndex, yIndex, 0);
			    tempInt *= renorm[j];
			    tempInt &= 0xff;
			    vol[j][xIndex][yIndex] = (byte)tempInt;
// 			    vol[depth -1-j][xIndex][height-yIndex-1] = 
// 				(byte)tempInt;
			}
		    }
		}
	    } 

	    // Coronal orientation 
	    else if(orient == 1 || orient == 4){
	    
		// Initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = bImg.getWidth();
		height = images.length;
		depth = bImg.getHeight();
		vol = new byte[depth][width][height];
		for(int yIndex = 0; yIndex < height; yIndex++){
		    bImg = images[yIndex];
		    raster = bImg.getRaster();

		    // Load into vol
		    for(int xIndex = 0; xIndex < width; xIndex++){
			for(int j = 0; j < depth; j++){
			    tempInt = raster.getSample(xIndex, j, 0);
			    tempInt *= renorm[yIndex];
			    tempInt &= 0xff;
			    vol[j][xIndex][yIndex] = (byte)tempInt;
			}
		    }
		}
	    }

	    // Sagittal orientation
	    else {

		// Initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = images.length;
		depth = bImg.getHeight();
		height = bImg.getWidth();
		vol = new byte[depth][width][height];

		for(int x =0; x < width; x++){
		    bImg = images[x];
		    raster = bImg.getRaster();

		    // Load into vol
		    for(int z = 0; z < depth; z++){
			for(int y = 0; y < height; y++){
			    tempInt = raster.getSample(y, z, 0);
			    tempInt *= renorm[x];
			    tempInt &= 0xff;
			    vol[z][x][y] = (byte)tempInt;
			}
		    }
		}
	    }
	} catch (Exception e){
	    e.printStackTrace();
	}
	_width = width;
	_height = height;
	_depth = depth;
	return vol;
    }

    /**
     * Creates a data volume from the given image reader.
     *
     * @param reader ImageReader object of the queried file.
     * @param orient Orientation of the image in the imageReader.
     * 
     * @return A 3D array of SHORT numbers.
     */
    private short[][][] _createShortDataVolume(BufferedImage[] images,
					       int orient)
    {
	short[][][] vol = null;
	int width = 0;
	int height = 0;
	int depth = 0;
	try{
// 	    System.out.println("Loading " + images.length +" "
// 			       + _format +" images. Please wait...");

	    Raster raster;
	    int tempInt;
	    /** Get the images orientation:
	     *  0  transverse unflipped
	     *  1  coronal unflipped
	     *  2  sagittal unflipped
	     *  3  transverse flipped
	     *  4  coronal flipped
	     *  5  sagittal flipped
	     */
	    BufferedImage bImg;

	    // Transverse or axial orientation
	    if(orient == -1 || orient == 0 || orient == 3) {
	    
		// initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = bImg.getWidth();
		height = bImg.getHeight();
		depth = images.length;
		vol = new short[depth][width][height];
		for(int j = 0; j < depth; j++){
		    bImg = images[j];
		    raster = bImg.getRaster();

		    // load into vol
		    for(int xIndex = 0; xIndex < width; xIndex++){
			for(int yIndex = 0; yIndex < height; yIndex++){
			    tempInt = raster.getSample(xIndex, yIndex, 0);
			    tempInt &= 0xffff;
			    vol[j][xIndex][yIndex] = (short)tempInt;
// 			    vol[depth -1-j][xIndex][height-yIndex-1] = 
// 				(short)tempInt;
			}
		    }
		}
	    } 

	    // Coronal orientation 
	    else if(orient == 1 || orient == 4){
	    
		// initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = bImg.getWidth();
		height = images.length;
		depth = bImg.getHeight();
		vol = new short[depth][width][height];
		for(int yIndex = 0; yIndex < height; yIndex++){
		    bImg = images[yIndex];
		    raster = bImg.getRaster();

		    // load into vol
		    for(int xIndex = 0; xIndex < width; xIndex++){
			for(int j = 0; j < depth; j++){
			    tempInt = raster.getSample(xIndex, j, 0);
			    tempInt &= 0xffff;
			    vol[j][xIndex][yIndex] = (short)tempInt;
			}
		    }
		}
	    }

	    // Sagittal orientation
	    else {

		// Initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = images.length;
		depth = bImg.getHeight();
		height = bImg.getWidth();
		vol = new short[depth][width][height];

		for(int x = 0; x < width; x++){
		    bImg = images[x];
		    raster = bImg.getRaster();

		    // Load into vol
		    for(int z = 0; z < depth; z++){
			for(int y = 0; y < height; y++){
			    tempInt = raster.getSample(y, z, 0);
			    tempInt &= 0xffff;
			    vol[z][x][y] = (short)tempInt;
			}
		    }
		}
	    }
	} catch (Exception e){
	    e.printStackTrace();
	}
	_width = width;
	_height = height;
	_depth = depth;
	return vol;
    }

    /**
     * Creates a data volume from the given image reader.
     *
     * @param reader An ImageReader object for parsing the queried file
     * @param orient Orientation of the image in the ImageReader.
     * @param renorm An array of values for renormalization.
     *
     * @return A 3D array of SHORT numbers.
     */
    private short[][][] _createShortDataVolume(BufferedImage[] images, 
					       int orient, double[] renorm)
    {
	if(renorm == null) return _createShortDataVolume(images, orient);
	short[][][] vol = null;
	int width = 0;
	int height = 0;
	int depth = 0;
	try{
// 	    System.out.println("Loading " + images.length +" "
// 			       + _format +" images. Please wait...");

	    Raster raster;
	    int tempInt;
	    BufferedImage bImg;

	    // Transverse or axial orientation
	    if(orient == -1 || orient == 0 || orient == 3) {
	    
		// Initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = bImg.getWidth();
		height = bImg.getHeight();
		depth = images.length;
		vol = new short[depth][width][height];
		for(int j = 0; j < depth; j++){
		    bImg = images[j];
		    raster = bImg.getRaster();

		    // load into vol
		    for(int xIndex = 0; xIndex < width; xIndex++){
			for(int yIndex = 0; yIndex < height; yIndex++){
			    tempInt = raster.getSample(xIndex, yIndex, 0);
			    tempInt *= renorm[j];
			    tempInt &= 0xffff;
			    vol[j][xIndex][yIndex] = (short)tempInt;
// 			    vol[depth -1-j][xIndex][height-yIndex-1] = 
// 				(short)tempInt;
			}
		    }
		}
	    } 

	    // Coronal orientation 
	    else if(orient == 1 || orient == 4){
	    
		// Initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = bImg.getWidth();
		height = images.length;
		depth = bImg.getHeight();
		vol = new short[depth][width][height];
		for(int yIndex = 0; yIndex < height; yIndex++){
		    bImg = images[yIndex];
		    raster = bImg.getRaster();

		    // Load into vol
		    for(int xIndex = 0; xIndex < width; xIndex++){
			for(int j = 0; j < depth; j++){
			    tempInt = raster.getSample(xIndex, j, 0);
			    tempInt *= renorm[yIndex];
			    tempInt &= 0xffff;
			    vol[j][xIndex][yIndex] = (short)tempInt;
			}
		    }
		}
	    }

	    // Sagittal orientation
	    else {

		// Initialize pixelArray size for pixel volume array
		bImg = images[0];
		width = images.length;
		depth = bImg.getHeight();
		height = bImg.getWidth();
		vol = new short[depth][width][height];

		for(int x = 0; x < width; x++){
		    bImg = images[x];
		    raster = bImg.getRaster();

		    // Load into vol
		    for(int z = 0; z < depth; z++){
			for(int y = 0; y < height; y++){
			    tempInt = raster.getSample(y, z, 0);
			    tempInt *= renorm[x];
			    tempInt &= 0xffff;
			    vol[z][x][y] = (short)tempInt;
			}
		    }
		}
	    }
	} catch (Exception e){
	    e.printStackTrace();
	}
	_width = width;
	_height = height;
	_depth = depth;
	return vol;
    }

    /**
     * Flips the BufferedImage horizontally or vertically.
     *
     * @param BufferedImage the image to rotate.
     * @param xFlip true for a flip about the vertical (X) axis.
     * @param yFlip true for a flip about the horizontal (Y) axis.
     * 
     * @return The flipped image.
     */
    private BufferedImage _flipImage(BufferedImage image, boolean xFlip, 
				     boolean yFlip)
    {
	if( (!xFlip) && (!yFlip)) return image;
	if( image == null ) return null;
	int width = image.getWidth();
	int height = image.getHeight();
	Raster imageRaster = image.getRaster();

	int imageType = image.getType();
	if( imageType <= 0 || imageType > 13) {
	    if(_metadataParser.getBitsPP() <= 8) 
		imageType = BufferedImage.TYPE_BYTE_GRAY;
	    else imageType = BufferedImage.TYPE_USHORT_GRAY;
	}
	BufferedImage flippedImage = new BufferedImage(width,height,imageType);
	WritableRaster flippedRaster = flippedImage.getRaster();

	float pixelArray[] = null;
	int iFlip, jFlip;
	for(int j = 0; j <height; j++){
	    for(int i = 0; i < width; i++){
		
		pixelArray = imageRaster.getPixel(i,j, pixelArray);
		if (xFlip) {
		    jFlip = height -j - 1;
		} else {
		    jFlip = j;
		}
		if (yFlip) {
		    iFlip = width - i - 1;
		} else {
		    iFlip = i;
		}

		flippedRaster.setPixel(iFlip, jFlip, pixelArray); 
	    }
	} 
	return flippedImage;
    }

    /**
     * Gets a set of BufferedImages from an ImageReader.
     * 
     * @param reader The imageReader to be queried.
     * @param rotate The number of rotation of the images provided by the given
     *               imageReader. The images would be rotated (number * 90) 
     *               degrees clockwisely.
     * @param isXFlipEnabled Whether to flip the images around x axis.
     * @param isYFlipEnabled Whether to flip the images around y axis.
     *
     * @return An array of bufferedImages.
     */
    private BufferedImage[] _getBufferedImages(ImageReader reader, int rotate,
					       boolean isXFlipEnabled,
					       boolean isYFlipEnabled)
    {
	BufferedImage[] bImgs = null;
	BufferedImage tempImage = null;
	try{
	    bImgs = new BufferedImage[reader.getNumImages(true)];
	    for(int i = 0; i < bImgs.length; i++ ){
		tempImage = reader.read(i);
		tempImage = _rotateImage(tempImage, rotate);
		tempImage = _flipImage(tempImage, isXFlipEnabled, 
				       isYFlipEnabled);
		bImgs[i] = tempImage;
	    }
	}catch (Exception e){
	    e.printStackTrace();
	}
	return bImgs;
    }

    /**
     * Gets a set of BufferedImages from a set of ImageReaders.
     * 
     * @param readers A vector of imageReaders to be queried.
     * @param rotate The number of rotation of the images provided by the given
     *               imageReader. The images would be rotated (number * 90) 
     *               degrees clockwisely.
     * @param isXFlipEnabled Whether to flip the images around x axis.
     * @param isYFlipEnabled Whether to flip the images around y axis.
     * 
     * @return An array of bufferedImages.
     */
    private BufferedImage[] _getBufferedImages(Vector readers, int rotate,
					       boolean isXFlipEnabled,
					       boolean isYFlipEnabled)
    {
	BufferedImage[] bImgs = null;
	try{
	    bImgs = new BufferedImage[readers.size()];
	    for(int i = 0; i < bImgs.length; i++ ){
		bImgs[i] = ( (ImageReader)readers.elementAt(i) ).read(0);
		bImgs[i] = _rotateImage(bImgs[i], rotate);
		bImgs[i] = _flipImage(bImgs[i], isXFlipEnabled,
				      isYFlipEnabled);
	    }
	}catch (Exception e){
	    e.printStackTrace();
	}
	return bImgs;
    }

    /**
     * Gets the maximum of the given numbers.
     * 
     * @param array An array of int numbers to query.
     *
     * @return The maximum number of the given numbers.
     */
    private int _getMax(int[] array)
    {
	if(array == null || array.length == 0) return 0;
	if(array.length == 1) return array[0];
	int max = array[0];
	for(int i = 1; i < array.length; i++){
	    if( array[i] > max) max = array[i];
	}
	return max;
    }

    /**
     * Gets the minimum of the given numbers.
     * 
     * @param array An array of int numbers to query.
     *
     * @return The minimum of the given numbers.
     */
    private int _getMin(int[] array)
    {
	if(array == null || array.length == 0) return 0;
	if(array.length == 1) return array[0];
	int min = array[0];
	for(int i = 1; i < array.length; i++){
	    if( array[i] < min) min = array[i];
	}
	return min;
    } 

    /**
     * Calculates the histogram of the data volume and compute the value of 
     * _intens2, under which there are 2% pixel intensities.
     *
     * @param array An 3D array of short numbers.
     */
    private void _histogram()
    {
	// Find the maximum intensity value
	int maxI = 0;
	int temp;
	for(int z = 0; z < _depth; z++){
	    for(int x = 0; x < _width; x++){
		for(int y = 0; y < _height; y++){
		    temp = getValue(x, y, z);
		    if(maxI < temp) maxI = temp;
		}
	    }
	}

	// Create the histogram array
	int[] hist = new int[(int)maxI +1];
	int intens;
	for(int z = 0; z < _depth; z++){
	    for(int x = 0; x < _width; x++){
		for(int y = 0; y < _height; y++){
		    intens = getValue(x, y, z);
		    hist[(int)intens]++;
		}
	    }
	}

	// Calculate _intens2
	int num2 = (int) (_depth * _width * _height * 0.05);
	int tempInt = 0;
	for(int i = 0; i < hist.length; i++){
	    tempInt += hist[i];
	    if(tempInt >= num2){
		_intens2Percent = i;
		i = hist.length;
	    }
	}
    }



    /**
     * Rotates the given image clockwisely.
     * 
     * @param image The image to rotate.
     * @param rotateNumber The number of the rotation. The degrees of rotation
     *                     will be (the number of rotation * 90).
     *
     * @return The rotated bufferedImage.
     */
    private BufferedImage _rotateImage(BufferedImage image, int rotateNumber)
    {
	if (rotateNumber%4 == 0 ) return image;

	// Construct rotation matrix
	double alpha = rotateNumber * Math.PI / 2;
	double r11 = Math.cos(alpha);
	double r12 = -Math.sin(alpha);
	double r21 = -r12;
	double r22 = r11;

	// Calculate boudary
	int width = image.getWidth();
	int height = image.getHeight();
	int[] xArray = new int[4];
	int[] yArray = new int[4];

	// rotate (0, 0)
	xArray[0] = 0;
	yArray[0] = 0;

	// rotate (width, 0)
	xArray[1] = (int) Math.floor(r11 * (width-1) + r12 * 0 + 0.5);
	yArray[1] = (int) Math.floor(r21 * (width-1) + r22 * 0 + 0.5);

	// rotate (0, height)
	xArray[2] = (int) Math.floor(r11 * 0 + r12 * (height-1) + 0.5);
	yArray[2] = (int) Math.floor(r21 * 0 + r22 * (height-1) + 0.5);

	// rotate (width, height)
	xArray[3] = (int) Math.floor(r11 * (width-1) + r12 * (height-1) + 0.5);
	yArray[3] = (int) Math.floor(r21 * (width-1) + r22 * (height-1) + 0.5);

	// Calculate the maximum/minimum coordinate along the x and y axises
	int xMin = _getMin(xArray);
	int xMax = _getMax(xArray);
	int yMin = _getMin(yArray);
	int yMax = _getMax(yArray);

	// Create a new bufferedImage
	int newWidth = xMax - xMin + 1;
	int newHeight = yMax - yMin + 1;
	int imageType = image.getType();
	if( imageType <= 0 || imageType > 13) {
	    if(_metadataParser.getBitsPP() <= 8) 
		imageType = BufferedImage.TYPE_BYTE_GRAY;
	    else imageType = BufferedImage.TYPE_USHORT_GRAY;
	}
	BufferedImage newImage=new BufferedImage(newWidth,newHeight,imageType);
	WritableRaster newRaster = newImage.getRaster();
	Raster raster = image.getRaster();
	int tempPixel;
	int _x, _y;
	for(int y = 0; y < height; y++){
	    for(int x = 0; x < width; x++){
		tempPixel = raster.getSample(x, y, 0);
		_x = (int)Math.floor((r11 * x + r12 * y) - xMin + 0.5);
		_y = (int)Math.floor((r21 * x + r22 * y) - yMin + 0.5);
		newRaster.setSample(_x, _y, 0, tempPixel);
	    }
	}
	return newImage;
    }
}
