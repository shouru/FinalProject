package LevelSet;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import FileIO.FileWriter;
import FileIO.MetadataParser;
import LevelSet.DataVolume;
import LevelSet.Filler;

import org.medtoolbox.jviewbox.viewport.annotation.DynamicAnnotationImage;

import LevelSet.Calculator;

public class SkullStripper {
	/**
	 * Annotation image for showing the evolving contour.
	 */
	public DynamicAnnotationImage annImage;

	/** iteration is pause **/
	boolean isfrozen = false;
	boolean isfirstTime = true;
	double BOUNDARY_VALUE = -0.5;

	/** number of this slice**/
	public int slice;
	
	/** The width of the image. */
	int X_DIM = 160;

	/** The height of the image. */
	int Y_DIM = 160;

	/** The given inputImage */
	private BufferedImage _inputImage ;
	/** The given inputImage raw data. */
	private short[][] _inputImageArray = null;

	/** The thresholdSelector */
	private double thresholdSelector = 0.5;
	/** parameter of image force */
	private double alpha = 0.1;
	/**
	 * The intensity minimum below which lies 2% of the cumulative histogram.
	 */
	private double _intens2;

	/**
	 * The intensity maxmum below which lies 98% of the cumulative histogram.
	 */
	private double _intens98;

	/**
	 * The intensity maxmum below which lies 10% of the cumulative histogram.
	 */
	private double _intens10;

	/**
	 * The intensity maxmum below which lies 40% of the cumulative histogram.
	 */
	private double _intens40;

	/** The Last Mask Area is used to compare with current mask area. */
	private int _lastMaskArea = -1;
	private int _lastLastMaskArea = -1;

	/**
	 * The Stationary Counter indicates if this activeContour is stable: IF
	 * _statnCounter < STATN_MAX ==> unstable
	 */
	private int _statnCounter = -1;

	/** Maximum oscillations to reach stability */
	private int STATN_MAX = 4;

	/** Calculates level set functions */
	public Calculator _calculator;

	/** Intensity of brain mask. */
	private int _intensityOfMask = 1;

	/** X-coord of the center of the initial zero level set circle. */
	private double _centerX;

	/** Y-coord of the center of the initial zero level set circle. */
	private double _centerY;

	/** Radius of the initial zero level set circle. */
	private double _radius = 70;

	/** LevelSet function */
	public double[][] phi = null;

	/** var for count level set function */
	double[][] delta_eps;
	double[][] f;
	double _intensM;

	/**How many slices in the volume */
	public int sliceInVolume;
	
	
	/** For File I/O */
	public ImageReader _imageReader;
	
	 /** Interslice distance (/pixel size). */
	public float INTER_SLICE_DIST = 3;
	
	public SkullStripper(int num) {
      slice=num;
	}

	/**
	 * Class to set dimention
	 * 
	 * @param X
	 *            dimention of x direction
	 * @param Y
	 *            dimention of y direction
	 */
	public void setDim(int X, int Y) {
		X_DIM = X;
		Y_DIM = Y;
	}
   /**
    *  Set parameter of level set method
    *  @param alpha @param radius 
    * 
    */
	public void setParam(double Inputalpha,double radius)
	{
		alpha=Inputalpha;
		_radius=radius;
	}
	/***
	 * 
	 *  Switch thesholdselector function
	 *  input 0 or input 1
	 * 
	 */
	public int switchTh(int num)
	{
        System.out.println("switch to num = " +num);		
		//Using s-shape 
        int adpara1 = 0;
		float adpara2 = 0;
		int adpara3 = 0;
		
		
		//threshold for more expansion
		if(num==1){
		  adpara1 = 4;
		  adpara2 = (float) 0.05;
		  adpara3 = (int)sliceInVolume/5;
		
		
		  int a = 0;
		  float b = (float) (sliceInVolume * adpara1);
		  int c = sliceInVolume*2*adpara1;
		
	      float b1 = (float) (sliceInVolume *adpara1);
		  int c1 = sliceInVolume*2*adpara1;
		
		  //Calculate current slice distance to center slice
		  float d = slice - sliceInVolume/2;
		  float x1 = Math.abs(slice - sliceInVolume/2 ) + b - adpara3;
		  //-(int)sliceInVolume/10;
		  float x2 = Math.abs(slice - sliceInVolume/2 ) + b1 + adpara3;
		  // if x ==a
		  //if(x==a)
		
		  if(d<=0){
			   if( x1 <=b)
				 	thresholdSelector = 2 * Math.pow((x1-a)/(c-a), 2);
				 
			   if(x1>b && x1<=c) 
					thresholdSelector = 1 - 2 * Math.pow((x1-c)/(c-a), 2);  	
		   }
		  else{
			//+0.1
			   if( x2 <=b1)
			        thresholdSelector = 2 * Math.pow((x2-a)/(c1-a), 2) ;
						 
		       if(x2>b1 && x2<=c1) 
				  thresholdSelector = 1 - 2 * Math.pow((x2-c1)/(c1-a), 2) ;  
		}		   
		System.out.println("slice ="+slice+"threshold ="+thresholdSelector);		
	    _calculator.setThresholdSelector(thresholdSelector);
	    _statnCounter = -1;
	}
		
	 
	  
	  return num;
	
	}
	
	
	
	/**
	 * Get alpha;
	 * 
	 */
	public double getAlpha()
	{
		
		return alpha;
	}
	/**
	 * 
	 * Get radius
	 * 
	 */
	public double getRadius()
	{
		return _radius;
		
	}
	/**
	 *  Get image force in a point
	 * 
	 * 
	 */
	
	public double getImageForce(int x,int y)
	{
	  if(f!=null){	
		double force = f[x][y];
		return force;}
	  else
		  return -79852;
	}
	
	/**
	 *  Get image phi in a point
	 * 
	 * 
	 */
	
	public double getPhi(int x,int y)
	{
	  if(phi!=null){	
		double phixy = phi[x][y];
		return phixy;}
	  else
		  return -79852;
	}
	
	public void DisplayInputImage()
	{
		 JLabel jLabel = new JLabel(new ImageIcon(_inputImage));
         JPanel jPanel = new JPanel();
         jPanel.add(jLabel);
		 JFrame test = new JFrame();
		 test.setSize(217,181);
		 test.setTitle("slice = "+slice);
		 test.add(jPanel);
		 test.setVisible(true);
	
	}
	
	/**
	 * Class to initilization
	 * 
	 * @param bufferedImage
	 *            the buffered image from the original image data.
	 */
	public void initialization(BufferedImage bufferedImage) {
		_inputImage = new BufferedImage(X_DIM, Y_DIM,BufferedImage.TYPE_BYTE_GRAY);
		_inputImage = bufferedImage;
		_inputImageArray = create2DArray(_inputImage);
	
		
		if(slice<=sliceInVolume/2){
			 STATN_MAX=15;}
		else{
			STATN_MAX=2;
		}
		
		
		//Using s-shape 
		//4,8
		int a = 0;
		float b = (float) (sliceInVolume * 8);
		int c = sliceInVolume*16;
		//8,16
		float b1 = (float) (sliceInVolume * 16);
		int c1 = sliceInVolume*32;
		
		//Calculate current slice distance to center slice
		float d = slice - sliceInVolume/2;
		float x1 = Math.abs(slice - sliceInVolume/2 ) + b;
				
		float x2 = Math.abs(slice - sliceInVolume/2 )+b1+sliceInVolume/5;
	
		if(d<0){
			if( x1 <=b)
				thresholdSelector = 2 * Math.pow((x1-a)/(c-a), 2) ;
		 
			if(x1>b && x1<=c) 
				thresholdSelector = 1 - 2 * Math.pow((x1-c)/(c-a), 2);  	
		}
		else{
			//0.1
			if( x2 <=b1)
				thresholdSelector = 2 * Math.pow((x2-a)/(c1-a), 2)-0.15;
				 
		    if(x2>b1 && x2<=c1) 
		    	thresholdSelector = 1 - 2 * Math.pow((x2-c1)/(c1-a), 2)-0.15;  
		   
		}
		
		//STATN_MAX=4;
		//thresholdSelector = 0.5;
		
		
		
        System.out.println("slice ="+slice+"threshold ="+thresholdSelector);
		
		_calculator = new Calculator(X_DIM, Y_DIM, alpha, thresholdSelector);
		_calculator.slice=slice;	
	}
    /**
     * 
     * Get inputImage
     * 
     */
	public BufferedImage getInputImage()
	{
		return _inputImage;
		
	}
	
	/**
	 * Class to get X_DIM
	 * 
	 * @return int dim of x-direction
	 */

	public int getXdim() {
		return X_DIM;
	}

	/**
	 * Class to get Y_DIM
	 * 
	 * @return int dim of y-direction
	 */

	public int getYdim() {
		return Y_DIM;
	}
	
	/**
	   * Set intensity values at 2%, 10%, 40% and 98% histogram.
	   * 
	   * @param intens2 The intensity minimum below which lies 2% of the 
	   *                cumulative histogram.
	   * @param intens10 The intensity minimum below which lies 10% of the 
	   *                 cumulative histogram.
	   * @param intens40 The intensity minimum below which lies 40% of the 
	   *                 cumulative histogram.
	   * @param intens98 The intensity minimum below which lies 98% of the 
	   *                 cumulative histogram.
	   */
	  public void setThresholdIntensities(double intens2, double intens10, 
					      double intens40, double intens98)
	  {
	    _intens2 = intens2;
	    _intens10 = intens10;
	    _intens40 = intens40;
	    _intens98 = intens98;
	  }

	/**
	 * Class to get smallest x, y and biggest x, y of phi
	 */
	
	public int getxbegin(){
		int xstart = 5000;
		for(int i =0;i<phi.length;i++){
			for(int j = 0; j < phi[0].length;j++)
				if(phi[i][j]==1 && xstart > j)
					xstart = j;
		}
		return xstart;
	}
	
	public int getybegin(){
		int ystart = 5000;
		for(int i =0;i<phi.length;i++){
			for(int j = 0; j < phi[0].length;j++)
				if(phi[i][j]==1 && ystart > i)
					ystart = i;
		}
		return ystart;
	}
	
	public int getxend(){
		int xend = 0;
		for(int i =0;i<phi.length;i++){
			for(int j = 0; j < phi[0].length;j++)
				if(phi[i][j]==1 && xend < j)
					xend = i;
		}
		return xend+1;
	}
	
	public int getyend(){
		int yend = 0;
		for(int i =0;i<phi.length;i++){
			for(int j = 0; j < phi[0].length;j++)
				if(phi[i][j]==1 && yend < i)
					yend = j;
		}
		return yend+1;
	}
	

	/**
	 * Class initial parameter all parameter
	 * 
	 */
	public void initialParameter() {
		
		MetadataParser metadataParser = new MetadataParser(_imageReader); 
		
		DataVolume dv = new DataVolume(_imageReader);
		
		int _resamplingOrient = _getResamplingOrient(metadataParser, dv);
		isfrozen = false;
	
		if(slice==(int)sliceInVolume/2-1)
	         SetZeroLevel();
	
			
	  
		
		INTER_SLICE_DIST = _getInterSliceDist(metadataParser,_resamplingOrient);
		_calculator.calculateProbingDistance(_getXYDim(metadataParser),metadataParser.getAge());		
		
	
		
		short[][] inputarray = create2DArray(_inputImage);
        //initial maskArea
		_lastMaskArea=calculateMaskArea(phi);
		//DisplayInputImage();
		
		// If initial mask area is too small, use _intens10 as intensM
		if (calculateMaskArea(phi) <= 200) _intensM = _intens10;
	    else _intensM = _calculator.calculateMedianIntensity(phi, inputarray);
	  
	  
	}
    /**
     * Set original zero-level set in center slice
     * 
     * 
     */
	public void SetZeroLevel()
	{

		MetadataParser metadataParser = new MetadataParser(_imageReader); 
		DataVolume dv = new DataVolume(_imageReader);
		int _resamplingOrient = _getResamplingOrient(metadataParser, dv);
		    
		ZeroLSInitializer phimetadata = new ZeroLSInitializer(_imageReader,this,_intens2,_intens98,_resamplingOrient);
	     _centerX = phimetadata.x();
		 _centerY = phimetadata.y()-30;//-10,30
		System.out.println("centerx= "+_centerX+"centery="+_centerY);
		phi = initialphi();
			
		
		
	}
	
	
	
	
	
	
	/**
	 * Class for initial phi
	 * 
	 */
	public double[][] initialphi() {
		double[][] initialphi = new double[X_DIM][Y_DIM];
		double temp1, temp2, temp3;
      
		
		
		
		System.out.println("centerx= "+_centerX+"centery="+_centerY);
		// phi1_0 = -sqrt((x-centerX)^2 + (y-centerY)^2) + radius
		for (int y = 0; y < Y_DIM; y++) {
			for (int x = 0; x < X_DIM; x++) {
				temp1 = (x - _centerX) * (x - _centerX);
				temp2 = (y - _centerY) * (y - _centerY);
				temp3 = -Math.sqrt(temp1 + temp2) + _radius;
				initialphi[x][y] = temp3;

			}
		}

		return initialphi;
	}
	/***
	 *  Set phi = 0
	 * 
	 * 
	 * 
	 */
	public void setPhiZero()
	{
		for(int i=0;i<X_DIM;i++)
		  for(int s=0;s<Y_DIM;s++)
			  phi[i][s]=-1;
			  
	}
	
	
	
	
	
	/**
	 * initial phi with former slice
	 * 
	 * 
	 */
	public void initialphiwithFormer(double[][] formerphi)
	{
		phi=formerphi;
		// If initial mask area is too small, use _intens10 as intensM
		if (calculateMaskArea(phi) <= 200) _intensM = _intens10;
	    else _intensM = _calculator.calculateMedianIntensity(phi,_inputImageArray);
	}
	
	public double[][] getmask(){
		return phi;
	}
	
	

	/**
	 * Class for skullstripper
	 * 
	 */
	public void Skullstrip() {
		   
		   short[][] inputarray = create2DArray(_inputImage);
			
		 
			
			// when the zero level set is stationary, stop iterating
			// When the interation exceeds the max iteration numbers, stop
			// iterating
			delta_eps = _calculator.calculateHEpsilonAndDeltaEpsilon3(phi);

			// Calculate array of force
			f = _calculator.calculateF(phi, delta_eps, inputarray, _intens2,
					_intens10, _intensM, _intens98);
	
			// Calculate phi1new
			phi = _calculator.calculatePhiNew2(phi, delta_eps, f,inputarray);
			
			// Reinitialization
			phi = _calculator.reinitialize(phi);
		
	}
	
	/**
	 * When result is not acceptable auto adjust curvature
	 *  
	 * 
	 */
	
	public void AdjustCurva()
	{
		_calculator.setVelocity(5);
		_statnCounter = -1;
		
	}
	/**
	 *  When result is not acceptable auto adjust thresholdselector
	 */
	public void AdjustTh(double adjust)
	{
		
		thresholdSelector+=adjust;
		_calculator.setThresholdSelector(thresholdSelector);
		_statnCounter = -1;
		System.out.println("thresholdSelector = "+thresholdSelector+"_statnCounter = " + _statnCounter);
		
	}
	
	
	
	/**
	   * Shrink phi.
	   *
	   * @param phi The array of phi.
	   * @param DIST Distance to shrink zero level set. 
	   */
	  public double[][] shrinkPhiZero(double[][] phi, int DIST)
	  {
	      for(int k = 0; k < DIST; k++){

		  // Move zero contour inward
		  for(int y = 0; y < phi[0].length; y++){
		      for(int x = 0; x < phi.length; x++){
			  phi[x][y] = phi[x][y] - 1;
		      }
		  }

		  // reinialize phi
		  phi = _calculator.reinitialize(phi);
	      }
	      return phi;
	  }
	

	

	public int calculateMaskArea(double[][] array) {
		int maskArea = 0;

		for (int y = 0; y < Y_DIM; y++) {
			for (int x = 0; x < X_DIM; x++) {
				if (array[x][y] >= BOUNDARY_VALUE)
					maskArea++;
			}
		}

		// System.out.println("area="+maskArea);
		return maskArea;
	}
	
	/**
	   * Calculate the length of the zero level set.
	   *
	   * @param array An array of binary values, whose elements with positive 
	   *              values correspond to the brain tissue. 
	   * @return The number of positive pixels of the given array.
	   */
	  public int calculateContourLength(double[][] array)
	  {
	    int length = 0;
	    byte[][] byteArray = _calculator.convertToBinaryArray(array);
	    long tempLong;
	    for(int y = 1; y < Y_DIM-1; y++){
	      for(int x =1; x < X_DIM-1; x++){
		tempLong = 4* byteArray[x][y] - byteArray[x][y-1] - byteArray[x][y+1]
		             - byteArray[x-1][y] - byteArray[x+1][y];
		if(tempLong > 0 ) length++;		
	      }
	    }
	    return length;
	  }
	
	
	

	/**
	 * Set pause
	 */
	public void setPause(boolean set) {
		isfrozen = set;
	}

	/**
	 * Return iteration state
	 */
	public boolean getisFrozen() {
		return isfrozen;
	}

	/**
	 * Create a 2D array to store the image data.
	 */
	public short[][] create2DArray(BufferedImage bImg) {
		// Get the raster of the bImg
		Raster raster = bImg.getRaster();
		int xOffset = 0;
		int yOffset = 0;
		short[][] array = new short[bImg.getWidth()][bImg.getHeight()];

		// Offset to get the interesting region, set manually
		int tempInt;
		for (int y = 0; y < Y_DIM; y++) {
			for (int x = 0; x < X_DIM; x++) {
				tempInt = raster.getSample(x + xOffset, y + yOffset, 0);
				tempInt &= 0xffff;
				array[x][y] = (short) tempInt;

			}
		}
		return array;
	}

	public BufferedImage getAnnotationImage() {
		BufferedImage AnnotationImage = new BufferedImage(X_DIM, Y_DIM,
				BufferedImage.TYPE_INT_ARGB);
		int rgb = (0xff << 24) | (0xff << 16) | (0xf << 8); // red

		byte[][] outputArray = _calculator.convertToBinaryArray(phi);
		
		long tempLong;
		for (int y = 1; y < Y_DIM - 1; y++) {
			for (int x = 1; x < X_DIM - 1; x++) {
				tempLong = 4 * outputArray[x][y] - outputArray[x][y - 1]
						- outputArray[x][y + 1] - outputArray[x - 1][y]
						- outputArray[x + 1][y];
				// System.out.println("templong="+tempLong);
				if (tempLong != 0) {
					// System.out.println("tttt");
					AnnotationImage.setRGB(x, y, rgb);
				}
			}
		}

		return AnnotationImage;
	}

	/**
	 * print phi in consle for testing
	 * 
	 */
	public void printPhi() {
		for (int y = 0; y < Y_DIM; y++) {
			for (int x = 0; x < X_DIM; x++) {
				if (phi[x][y] > 0)
					System.out.print(phi[x][y] + " ");
			}
			// System.out.println("");
		}

	}
	/**
    * determind it is steady state or not 
    *
    *
    */
	public boolean isStationary(){
		 if(phi==null){
	       return true;   	 
		 }
		 int maskArea=calculateMaskArea(phi);
		
		   
		    
		    if(_lastLastMaskArea < 0) {
		      _lastLastMaskArea = maskArea;
		      return false;
		    }
		    if(_lastMaskArea < 0){
		      _lastMaskArea = maskArea;
		      return false;
		    }

		    int GROWTH_RANGE = (int)((double)_lastLastMaskArea * 0.0000003);
             //int GROWTH_RANGE = 1;
		    // Check if the mask are stops growing
		    // if maskArea is 14000 pixels, then 2 pixel growth is insignificant, which
		    // is often occur at spinal cord
		    
		    if( (_lastMaskArea - _lastLastMaskArea) == GROWTH_RANGE
			&& (maskArea - _lastMaskArea) == GROWTH_RANGE ){
		 //   System.out.println("stop rule 1");	
			_statnCounter++;
		    }

		    // Check if the mask area is oscillating
		    else if ( (_lastMaskArea - _lastLastMaskArea) < 0 
			&& (maskArea - _lastMaskArea) > 0 ){
		    	 System.out.println("stop rule 2");	
		    	_statnCounter++;
		    }
		    else if( (_lastMaskArea - _lastLastMaskArea) > 0 
			     && (maskArea - _lastMaskArea) < 0 ){
		    	 System.out.println("stop rule 3");	
			_statnCounter++;
		    }
			     
		    // update lastmaskArea and _lastLastMaskArea
		    _lastLastMaskArea = _lastMaskArea;
		    _lastMaskArea = maskArea;
		    
		    if(_statnCounter > STATN_MAX) return true;
		    else return false;
		  }    	
	
	/**
	 * Save the mask image
	 * @throws IOException 
	 * 
	 * 
	 * 
	 */
	public void saveMaskImage() throws IOException
	{
		// Save the brain masks as Byte images
		System.out.println("slice "+(slice+1)+" save.....");
		byte[][] outputArray = _calculator.convertToBinaryArray(phi);
		//filling the hole
		Filler filler = new Filler(outputArray);
		byte[][] outputArray2 = filler.getFilledArray();
		//create mask   	
	    BufferedImage[] images=new BufferedImage[1];
	    images[0]=_createMaskImage(outputArray2,8);
	   // filename began to 1  
	    int filename=slice+1;
	    File outputfile = new File(filename+".png");
        ImageIO.write(images[0], "png", outputfile);
		
	}
	/**
	 *  Output Image with Contour 
	 * 
	 * 
	 */
	public BufferedImage OutputImageWithContour()
	{
		BufferedImage contour = _createContourImage();
		
		BufferedImage OutputImage = new BufferedImage(X_DIM, Y_DIM,
				  BufferedImage.TYPE_INT_ARGB);
      
		// paint both images, preserving the alpha channels
		Graphics g = OutputImage.getGraphics();
		g.drawImage(_inputImage, 0, 0, null);
		g.drawImage(contour, 0, 0, null);  
       
		
        return OutputImage;
	}
	/**
	 *  Output Skulls-stripping image
	 * 
	 */
	
	 public BufferedImage OutputSkullsImage()
	  {
		 // get output aray
		 byte[][] outputArray = _calculator.convertToBinaryArray(phi);  
	    // Create an empty bufferedImage
	    int imageType = 0;
	    imageType = BufferedImage.TYPE_BYTE_GRAY;
	
	    BufferedImage outputImage = new BufferedImage(_inputImage.getWidth(),
							  _inputImage.getHeight(), 
							  imageType);
	      
	    Raster inputRaster = _inputImage.getRaster();
	    WritableRaster outputRaster = outputImage.getRaster();
	    int width = _inputImage.getWidth();
	    int height = _inputImage.getHeight();
	    int tempInt;
	    for(int y = 0; y < height;  y++){
		for(int x = 0; x < width; x++){
		  if(outputArray[x][y] > 0 ) {
		      
		    tempInt = inputRaster.getSample(x, y, 0);
		    outputRaster.setSample(x, y, 0, tempInt);
		  } 
		}
	    }
	    
	    return outputImage;
	  }
	
	
	
	
	
	/**
	 *  Create ContourImage
	 *  
	 */
	 private BufferedImage _createContourImage()
	  {
		byte[][] outputArray = _calculator.convertToBinaryArray(phi); 
		//filling the hole
	    Filler filler = new Filler(outputArray);
	    byte[][] array = filler.getFilledArray();
		
		BufferedImage contourImage = new BufferedImage(X_DIM, Y_DIM,
							  BufferedImage.TYPE_INT_ARGB);
	    int rgb = (0xff << 24) | (0xff << 16) | (0xf << 8); //red
	    // int rgb = (0xff << 24) |(0xff << 16) | (0xff << 8) | 0xff;//white
	    long tempLong;
	    for(int y = 1; y < Y_DIM-1; y++){
	      for(int x =1; x < X_DIM-1; x++){
		tempLong = 4* array[x][y] - array[x][y-1] - array[x][y+1]
		             - array[x-1][y] - array[x+1][y];
		if(tempLong !=  0 ){
		    contourImage.setRGB(x, y, rgb);
		} 		
	      }
	    }
	    return contourImage;
	  }
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	   * Create a brain mask image.
	   * 
	   * @param outputArray A byte, binary array.
	   * @param bitsPP Bits per pixel.
	   * @return A BufferedImage object.
	   */
	  private BufferedImage _createMaskImage(byte[][] outputArray, int bitsPP)
	  {
	    BufferedImage outputImage = null;
	    int imageType;
	    if(bitsPP <= 8){
	      imageType = BufferedImage.TYPE_BYTE_GRAY;
	      byte tempByte = (byte)_intensityOfMask;
	      outputImage = new BufferedImage(X_DIM, Y_DIM, imageType);
	      WritableRaster outputRaster = outputImage.getRaster();
	      for(int y = 0; y < Y_DIM; y++){
		  for(int x = 0; x < X_DIM; x++){
		      if(outputArray[x][y] > 0) {
			  outputRaster.setSample(x, y, 0, tempByte);
		      }
		  }
	      }
	    }
	    else {
	      imageType = BufferedImage.TYPE_USHORT_GRAY;
	      short tempShort = (short)_intensityOfMask;
	      outputImage = new BufferedImage(X_DIM, Y_DIM, imageType);
	      WritableRaster outputRaster = outputImage.getRaster();
	      for(int y = 0; y < Y_DIM; y++){
		  for(int x = 0; x < X_DIM; x++){
		      if(outputArray[x][y] > 0) {
			  outputRaster.setSample(x, y, 0, tempShort);
		      }
		  }
	      }
	    }
	    return outputImage;
	  }
		
	  
	  /**
	   * Get the 2D resolusions. The pixDims contains 3D resolusions. The 2D
	   * will be obtained by comparing the three elements of pixDims array,
	   * and by assuming that the pixels are square, that is, the 
	   * resolusions are the same in x and y directions. So if two elements 
	   * in pixDims are the same, it is considered the 2D resolusions,
	   * 
	   * @param metadataParser An parser for parsing image file's metadata.
	   * @return A float number indicating the resolution of the pixels (unit: mm).
	   */
	    private float _getXYDim(MetadataParser metadataParser)
	    {
		float xyDim = 1.0f;
		float[] pixDims = metadataParser.getPixDims();

		// Get the 2D resolusions. The pixDims contains 3D resolusions. The 2D
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
	  
	    /**
	     * Get the interslice distance. 
	     *
	     * @param metadataParser An parser for parsing image file's metadata.
	     * @param resamplingOrient The resampling orientation.
	     * @return A float number indicating the inter-slice distance (unit: mm).
	     */
	    private float _getInterSliceDist(MetadataParser metadataParser, 
	  				   int resamplingOrient)
	    {
	      float interSliceDist = 1.0f;
	      float[] pixDims = metadataParser.getPixDims();

	      if(resamplingOrient == 0) interSliceDist = pixDims[2];
	      else if(resamplingOrient == 1) interSliceDist = pixDims[1];
	      else interSliceDist = pixDims[0];
	      return interSliceDist;
	    }

	    /**
	     * Get resampling orient that is determined by the orientation giving the 
	     * shortest depth. For example, if resampling in x direction gives the 
	     * shortest depth, then the resampling orientation is assigned as axial.  
	     * 
	     * @param metadataParser An parser for parsing image file's metadata.
	     * @param dv An dataVolume object storing the data.
	     * @return An int value indicating the orientation: 0-axial, 1-coronal, 
	     *         2-sagittal.
	     */
	    private int _getResamplingOrient(MetadataParser metadataParser,DataVolume dv)
	    {
	      int resamplingOrient = 0;


	      if(dv.getDepth(0)<dv.getDepth(1) && dv.getDepth(0)<dv.getDepth(2)){
	  	resamplingOrient = 0;
	      }
	      else if(dv.getDepth(1)<dv.getDepth(2)){
	  	resamplingOrient = 1;
	      }
	      else resamplingOrient = 2;

	      return resamplingOrient;
	    }


}
