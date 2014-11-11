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
import java.util.Vector;

/**
 * Class to support data calculations.
 *
 * @author Haihong Zhuang and Daniel J. Valentino
 * @version 2 September 2005
 */
public class Calculator
{

  /** Parameter h. */
  private double H = 1.0d;

  /** Parameter delta_t. */
  private double DELTA_T = 0.1d;

  /** Parameter mu. If mu is assigned a small value, small objects can be 
   * detected; otherwise, if mu is assigned a large value, small objects 
   * will be skipped.   
   */
  private double MU = 0.05 * 255d * 255d;
  /**
   *  AutoAdjust Mu
   * 
   */
  private double MUA=1;
  
  /** The weight of the image force. */
  private double _fFactor = 0.2 * 255d * 255d;
 // private double _fFactor = 0.*255*255;  
  /** Parameter epsilon. */
  private double EPSILON = H;

  /** The width of the image. */
  int X_DIM;

  /** The height of the image. */
  int Y_DIM;

  /** The distance for searching minimum intensity value which is used to 
   * calculate the image-based force. It is 16 by default. */
  private int _minD = 16;

  /** The distance for searching maximum intensity value which is used to 
   * calculate the image-based force. It is always the half of _minD */
  private int _maxD = (int) Math.floor(_minD/2 + 0.5);

  /** The resolusions of the pixels in either x and y directions, assuming
   * the resolution in both directions are the same as xyDim. */
  private float _xyDim = 1.0f;

  /** Threshold selector. */
  private double _bT = 0.5;

  /** Cut off boundary value of phi. */
  private double BOUNDARY_VALUE = -0.5;
  /** average intensity inside curve*/
  private double c1;
  /** average intensity ouside curve*/
  private double c2;
  /**his of this slice*/
  private int[] his ;
  /**The biggest intensity in whole volume*/
  private double _MaxI;
  /**The smallest intensity in wholde voluem*/
  private double _MinI;
  public int slice;
  
  
  
  /**
   * Constructor.
   * 
   * @param width Width of the 2D image grid.
   * @param height Height of the 2D image grid.
  
   * @param velocity Velocity of the level set model.
   * @param thresholdSelector A float number working as the threshold selector.
   */
  public Calculator(int width, int height, double velocity,
		     double thresholdSelector)
  {
    X_DIM = width;
    Y_DIM = height;
    MU = velocity*255d*255d;
    _bT = thresholdSelector;
  }
  /**
   *  set bt
   * 
   */
  public void setThresholdSelector(double thresholdSelector)
  {
	  _bT = thresholdSelector;
	  
  }
  
  
  /**
   * Calculates the array F. Each element of the array is a double number
   * whose value is the image force.
   */
  public double[][] calculateF(double[][] phi, 
			       double[][] delta_eps, short[][] inputArray,
			       double intens2, double intens10, 
			       double intensM, double intens98)
  {
    double[][] fArray1 = new double[X_DIM][Y_DIM];

    // Find out zero level set and calculate force for zero level set
    long tempLong;
    for(int y = 1; y < Y_DIM-1; y++){
      for(int x =1; x < X_DIM-1; x++){
	     if(delta_eps[x][y] > 0){
	       // Calculate array of force for zero level set
	     fArray1[x][y] = _calculateF(x, y, phi, inputArray, intens2,
		        		      intens10, intensM, intens98);
	    } 		
      }
    }
    return fArray1;
  }

  /**
   * Calculate delta_epsilon.
   * 
   * @param phi A double array. 
   * @return A double array of delta_eps.
   */
  public double[][] calculateHEpsilonAndDeltaEpsilon3(double[][] phi)
  {
    double[][] delta_eps = new double[X_DIM][Y_DIM];

    // epsilon = h = delta_x = delta_y
    double eps = 1.5 * H;
    double temp;
    double var;
    for(int x = 0; x < X_DIM; x++ ){
      for(int y = 0; y < Y_DIM; y++){
	var = phi[x][y];
	if( var < eps && var > (-eps)){
	  delta_eps[x][y] = (1+ Math.cos(Math.PI * var / eps)) / (2*eps);  
	}
      }
    }
    return delta_eps;
  }
    

  /**
   * Calculate median intensity from the inputArray.
   * 
   * @param phi A double array.
   * @param inputArray InputArray to be queried.
   * @return A double value.
   */
  public double calculateMedianIntensity(double[][] phi, short[][] inputArray)
  {
    double median = 0;

    // Find the intensity of the pixel at the middle of the intensity queue
    int count = 0;

    // Find maxI
    int max = 0;
    for(int y = 0; y < Y_DIM; y++){
      for(int x = 0; x < X_DIM; x++){
	if(phi[x][y] > 0){
	  if(max < inputArray[x][y]) max = inputArray[x][y];
	}
      }
    }
    int[] hist = new int[max + 1];
    int intens;
    for(int y = 0; y < Y_DIM; y++){
      for(int x = 0; x < X_DIM; x++){
	if(phi[x][y] > 0){
	    count++;
	    intens = inputArray[x][y];
	    hist[intens]++;
	}
      }
    }
    int temp = 0;
    int half = (int) (count / 2);
    for(int i = 0; i < hist.length; i++){
	temp = temp + hist[i];
	if(temp > half){
	    median = i;
	    i = hist.length;
	}
    }
    return median;
  }

  /**
   * Calculates the new phi.
   * 
   * @param phi A double array.
   * @param delta_eps A double array.
   * @param fArray A double array of image-based force.
   * @return A double array representing the phi.
   */
  public double[][] calculatePhiNew2(double[][] phi, double[][] delta_eps, 
				     double[][] fArray,short[][] inputArray)
  {
    double[][] phiNew = new double[X_DIM][Y_DIM];
    for(int i = 0; i < X_DIM; i++){
      for(int j = 0; j < Y_DIM; j++){
	  phiNew[i][j] = phi[i][j];
      }
    }
    
    //calculate c1 and c2
	caculateAverageIntensity(phi,inputArray);
    
    
    for(int x = 2; x < X_DIM-2; x++){
      for(int y = 2; y < Y_DIM -2; y++){
	if(Math.abs(fArray[x][y]) > 0 ){
	  phiNew[x][y] = calculatePhiNew(x, y, phi, delta_eps, fArray[x][y],inputArray);
	} 
      }
    }
    return phiNew;
  }


  /**
   * Calculates the new phi.
   * 
   * @param x X-coord of the pixel.
   * @param y Y-coord of the pixel.
   * @param phi A double array.
   * @param delta_eps A double array.
   * @param f Image-based force. 
   * @return A double value representing the phi value at the queried pixel.
   */
  public double calculatePhiNew(int x, int y, double[][] phi,
				  double[][] delta_eps, double f,short[][] inputArray)
  {
    double d1, d2, d3, d4, d;
    double u;
    double m;
    double phiNew;

    // Calculate d1, d2, d3, d4
    d1 = _calculateD1(phi, x, y);
    d2 = _calculateD1(phi, x-1, y);
    d3 = _calculateD3(phi, x, y);
    d4 = _calculateD3(phi, x, y-1);
   
	//Calculate MU
    autosetVelocity(x,y,phi,inputArray);
    // Calculate m = delta_t * delta_eps[x][y] * mu / h^2
    m = DELTA_T * delta_eps[x][y] * MU / (H*H);
    // 	m = DELTA_T * MU / (H*H);
	
    // Calculate d
    d = 1 + m * (d1 + d2 + d3 + d4);

    // 	double fFactor = 255*255;	
    double fFactor = _fFactor;
    u = DELTA_T * delta_eps[x][y] * fFactor * f;
    phiNew = (phi[x][y] + m * ( d1 * phi[x+1][y]
				+ d2 * phi[x-1][y] 
				+ d3 * phi[x][y+1]
				+ d4 * phi[x][y-1]) + u) / d;
    return phiNew;
  }

  /**
   * Calculate the probing distance which is used for calculating the image-
   * based force. The distance for searching minimum intensity is 20mm, and 
   * the distance for searching maximum intensity is 10mm. 
   *
   * @param xyDim The equal dimension in x or y direction.
   * @param age The age of the subject to whom the data belongs.
   */
  public void calculateProbingDistance(double xyDim, double age)
  {
      // A pixel's length is approximated as xyDim * (sqrt(2) + 1)/2
      double pixelLength1 = xyDim * Math.sqrt(2);
      double pixelLength2 = xyDim;
      double pixelLength = (pixelLength1 + pixelLength2) / 2;

      // Distance (with the unit of mm) searched from maxI and minI 
      double maxDisDouble = 10;
      double minDisDouble = (100 - age) / 100 * 20;
      if(age > 100) minDisDouble = 0;

      _maxD = (int) Math.floor(maxDisDouble / pixelLength + 0.5);
      _minD = (int) Math.floor(minDisDouble / pixelLength + 0.5);
  }

 /**
  * Converts a level set array to a binary mask. All positive elements in 
  * the level set array will be set as a unique positive value, and all
  * negative elements will be set as 0.
  * 
  * @param d A double array.
  * @return A byte, binary array.
  */ 
  public byte[][] convertToBinaryArray(double[][] d)
  {
    byte[][] outputArray = new byte[X_DIM][Y_DIM];
    for(int x = 0; x < X_DIM; x++ ){
      for(int y = 0; y < Y_DIM; y++ ){
	  
	// Set all positive level sets with a unique positive number
	if(d[x][y] >= BOUNDARY_VALUE) outputArray[x][y] = 40;
      }
    }
    return outputArray;
  }

  /**
   * Get velocity.
   *
   * @return A double value.
   */
  public double getVelocity()
  {
      return MU;
  }

  public void setVelocity(int time)
  {
	  MU*=time;
      MUA=time;
  }
  /** 
   *  Set Max and Min intenstity
   */
  public void setIntensity(double max,double min)
  {
	  _MaxI = max;
	  _MinI = min;
	
  }
  
  
  
  
  /**
   * Reinitialize phi to a new signed distance function. Each element of the 
   * new phi has its value equal to the distance to the zero level set curve,
   * and has positive sign if it is within the zero level set curve, and has
   * negative sign if it is out side of the zero level set curve.
   * 
   * @param array A double array to be queried.
   * @return A double array. 
   */
  public double[][] reinitialize(double[][] array)
  {
    double[][] newArray = null;

    double old;
    double dis;
    int MAX_ITER = 200;


    double[][] tempArray = array;
    double s;
    boolean isStationary = false;
    int reini = 0;
    while( !isStationary && reini < MAX_ITER ){

      // Allocate newArray
      newArray = new double[X_DIM][Y_DIM];
      for(int y = 1; y < (Y_DIM-1); y++){
	for(int x = 1; x < (X_DIM-1); x++){

	  old = tempArray[x][y];

	  // Revision of Sussman's phi0 construction done by Peng
	  dis = _calculateDistance(x, y, tempArray);
	  s = old / Math.sqrt( old * old + 1);  
	  newArray[x][y] = old - DELTA_T * s * (dis -1);
	
	}
      }
      for(int x = 0; x < X_DIM; x++){
	newArray[x][0] = newArray[x][1];
	newArray[x][Y_DIM-1] = newArray[x][Y_DIM-2];
      }
      for(int y = 0; y < Y_DIM; y++){
	newArray[0][y] = newArray[1][y];
	newArray[X_DIM-1][y] = newArray[X_DIM-2][y];
      }

      // Check stationary
      double sum = 0;
      int M = 0;
      for(int y = 1; y < (Y_DIM-1); y++){
	for(int x = 1; x < (X_DIM-1); x++){
	  if( Math.abs(tempArray[x][y]) > 1.5 ){
	    sum += Math.abs(newArray[x][y] - tempArray[x][y]) ;
	    M++;
	  }
	}
      }
      double m = sum / M;
      if(m < DELTA_T) isStationary = true;

      // Update tempArray
      tempArray = newArray;
      reini++;
    }
    return newArray;
  }

  /**
   * Set velocity.
   *
   * @param velocity Velocity of the level set model.
   */
 /* public void setVelocity(double velocity)
  {
    MU = velocity;
  }
*/
  /**
  *
  * Auto Set velocity 
  *
  *
  */
  public void autosetVelocity(int x,int y,double [][]phi,short [][]inputArray)
  {
	 
	  // caculateVelocity
	  MU=(inputArray[x][y]-c1+inputArray[x][y]-c2)/(_MaxI-_MinI)*X_DIM*Y_DIM*MUA;
	    if(MU<0){  	  
	        MU*=-1;}
	  
	//System.out.println("c1="+c1+"c2="+c2+"MU="+MU/(X_DIM*Y_DIM*MUA));  
	
	// MU = 0.05*255*255;
	  
  }
  /**
  *
  * Caculate average intensity inside and ouside curve 
  *
  *
  */
  public void caculateAverageIntensity(double [][]phi,short [][]inputArray)
  {
	  double sumc1=0,numc1=0;
	  double sumc2=0,numc2=0;
	  int x1=0,y1=0;
	  for(x1=0;x1<X_DIM;x1++)
	   for(y1=0;y1<Y_DIM;y1++)
	   {
		   //System.out.println("x="+x1+"y="+y1+"phi="+phi[x1][y1]);
		   //outside curve
		   if(phi[x1][y1]>0){
			   sumc2+=inputArray[x1][y1];
			   numc2++;
		   }
		   //inside curve
		   else if(phi[x1][y1]<0){
			   sumc1+=inputArray[x1][y1];
		       numc1++;
		   }
			   		   
	   }
	  c1=sumc1/numc1;
	  c2=sumc2/numc2;  
  }
  
  
  
  /**
   * d1 = (phi[x+1][y] - phi[x][y])/ sqrt(((phi[x+1][y]-phi[x][y])/h)^2 
   *                                 + ((phi[x][y+1]-phi[x][y-1])/2h)^2)
   */
  private double _calculateD1(double[][] phi, int x, int y)
  {
    double d1 = 0;
    double temp1 = (phi[x+1][y] - phi[x][y]) / H;
    double temp2 = (phi[x][y+1] - phi[x][y-1]) / (2*H);
    double temp3 = Math.sqrt(temp1*temp1 + temp2*temp2);
    if(Math.abs(temp3) > 0)	d1 = 1 / temp3;
    return d1;
  }

  /**
   * d2  = (phi[x][y+1] - phi[x][y]) / sqrt(((phi[x+1][y]-phi[x-1][y])/2h)^2 
   *                                       + ((phi[x][y+1]-phi[x][y])/h)^2)
   */
  private double _calculateD3(double[][] phi, int x, int y)
  {
    double d2 = 0;
    double temp1 = (phi[x+1][y] - phi[x-1][y]) / (2*H);
    double temp2 = (phi[x][y+1] - phi[x][y]) / H;
    double temp3 = Math.sqrt(temp1*temp1 + temp2*temp2);
    if(Math.abs(temp3) > 0) d2 = 1 / temp3;
    return d2;
  }

  /**
   * The distance used in reinitialization.
   */
  private double _calculateDistance(int x, int y, double[][] phi)
  {
    // The exact equation should be  a = (phi[x][y] - phi[x-1][y] ) / H
    double a = phi[x][y] - phi[x-1][y];
    double b = phi[x+1][y] - phi[x][y];
    double c = phi[x][y] - phi[x][y-1];
    double d = phi[x][y+1] - phi[x][y];

    double ans = 0;
    if(phi[x][y] != 0){
      if(phi[x][y] > 0){
	if( a < 0) a = 0;
	if( b > 0) b = 0;
	if( c < 0) c = 0;
	if( d > 0) d = 0;
      }
      else {
	if( a > 0) a = 0;
	if( b < 0) b = 0;
	if( c > 0) c = 0;
	if( d < 0) d = 0;
      } 
      double max1 = Math.max(a*a, b*b);
      double max2 = Math.max(c*c, d*d);
      ans = Math.sqrt(max1 + max2);
    }
    return ans;
  }

  /**
   * Calculate the MR force at (x, y).
   * 
   */
  public double _calculateF(int x, int y, double[][] phi,  
			    short[][] inputArray, double intens2, 
			    double intens10, double intensM, double intens98)
  {
    double f = 0;
     double[] normal;
     int minD = _minD;
     int maxD = _maxD;

    // Calculate the direction of the normal of phi(x,y)
     normal = _calculateNorDir(x, y, phi);

     Vector intensities = new Vector(1, 1);

     int tempX;
     int tempY;
     int samplingDis = 0;
     if(maxD > minD) samplingDis = maxD;
     else samplingDis = minD;
    // System.out.println("maxD = "+maxD+"minD = "+minD+"samplinDis = " + samplingDis);
     samplingDis = slice+5;
     for(int i = 0; i < samplingDis; i++){

       // calculate the coords of pixels
       tempX = (int)Math.floor(x + i * normal[0] + 0.5);
       tempY = (int)Math.floor(y + i * normal[1] + 0.5);

       if(tempX < X_DIM && tempY < Y_DIM && tempX > 0 && tempY > 0)
 	  intensities.add(new Integer(inputArray[tempX][tempY]));
       
       // calculate the counter-clockwise coords 
       
     
    
       int c_clockwiseX = (int) (x + ((tempX-x)*Math.cos(Math.PI/40) - (tempY-y)*Math.sin(Math.PI/40)));  
       int c_clockwiseY = (int) (y + ((tempX-x)*Math.sin(Math.PI/40) + (tempY-y)*Math.cos(Math.PI/40))); 
       
      if(c_clockwiseX < X_DIM && c_clockwiseY < Y_DIM && c_clockwiseX > 0 && c_clockwiseY > 0)
        intensities.add(new Integer(inputArray[c_clockwiseX][c_clockwiseY]));
      
       // calculate the clockwise coords
       
      int clockwiseX = (int) (x + ((tempX-x)*Math.cos(-Math.PI/40) - (tempY-y)*Math.sin(-Math.PI/40)));  
       int clockwiseY = (int) (y + ((tempX-x)*Math.sin(-Math.PI/40) + (tempY-y)*Math.cos(-Math.PI/40))); 
       
       if(clockwiseX < X_DIM && clockwiseY < Y_DIM && clockwiseX > 0 && clockwiseY > 0)
        intensities.add(new Integer(inputArray[clockwiseX][clockwiseY]));
       
     
       
     }
    

     double tempMin = _getMin(intensities, minD);
     double minI = Math.max( intens2, Math.min(tempMin, intensM));
     double tempMax = _getMax(intensities, maxD);
      double maxI = Math.max(intensM, tempMax);
     double tL = (maxI - intens2) * _bT + intens2;
    // double  ntL = (maxI - intens2) * 0.4 + intens2;
     f = 2 * (1*minI- tL ) / (maxI - intens2);

    return f;
  }

  /**
   * Calculate the direction of the normal of phi(x, y), which is 
   * phi_y / phi_x.
   *
   * @return A two element array in the order of normal_x and normal_y.
   */
  private double[] _calculateNorDir(int x, int y, double[][] phi)
  {
    double phi_y, phi_x;

    phi_y = (phi[x][y+1] - phi[x][y-1]) / 2;
    phi_x = (phi[x+1][y] - phi[x-1][y]) / 2;
    double d = Math.sqrt(phi_y * phi_y + phi_x * phi_x);
    double[] normal = new double[2];
    if(d != 0){
	normal[0] = phi_x / d;
	normal[1] = phi_y / d;
    }
    else {
	normal[0] = 0;
	normal[1] = 0;
    }
    return normal;
  }


  /**
   * Gets the max value of the array.
   *
   * @param array to search max within.
   * @return max value of the array.
   */
  private int _getMax(Vector vector, int depth)
  {
    int max = ((Integer)vector.elementAt(0)).intValue();
    if(vector.size() <= 1) return max;
    if(depth > vector.size() ) depth = vector.size();
    int temp;
    for(int i = 1; i < depth; i++){
	temp = ((Integer)vector.elementAt(i)).intValue();
	if(max < temp) max = temp;
    }
    return max;
  }

  /**
   * Gets the max value of the array.
   *
   * @param array to search max within.
   * @return max value of the array.
   */
  private int _getMin(Vector vector, int depth)
  {
    int min = ((Integer)vector.elementAt(0)).intValue();
    if(vector.size() <= 1) return min;
    if(depth > vector.size()) depth = vector.size();
    int temp;
    for(int i = 1; i < depth; i++){
	temp = ((Integer)vector.elementAt(i)).intValue();
	if(min > temp) min = temp;
    }
    return min;
  }
  
  
  /**
   * Calculate histogram
   * @param inputimage
   *
   **/
   public void calculateHis(short[][] inputimage)
   {
 	  // Find the maximum intensity
 	    double maxI = 0;
 	   
 	    BufferedImage image = null;
 	    for(int x = 0; x < inputimage.length; x++ ){
 	    	for( int y = 0; y <inputimage[0].length; y++ ){
 		      if(maxI < inputimage[x][y]) maxI = inputimage[x][y];
 		     }
 	      
 	    }
 	  // Initialize the histogram array  
       his = new int[(int)maxI+1]; 
       // Construct histogram array
       double intens;
         for(int x = 0; x < inputimage.length; x++ ){
   	     for( int y = 0; y < inputimage[0].length; y++ ){
   	       intens = inputimage[x][y];
   	       his[(int)intens]++;
   	      }
         }
   
   
   }
   
   
   /**
   * Calculate intens2
   *
   * @param inputimage 
   * return intens2
   */
   public int calculateIntens2(short[][] inputimage)
   {
 	// Calculate _intens2
 	// adjust to _intens1  
 	    double sum = 0;
 	    int _intens2 = 0;
 	    double totalSum = X_DIM * Y_DIM ;
 	    for(int k = 0; k < his.length; k++){
 		     sum += his[k];
 		    if(sum / totalSum > 0.02) {
 		    	_intens2 = k;
 		        k = his.length;
 		  }
 	    }
 	   return _intens2;
   }
   /**
   * Calculate intens98
   * @param inputimage
   */ 
   public int calculateIntens98(short[][] inputimage)
   {
 	// Calculate _intens98
 	    double sum = 0;
 	    int _intens98 = 0;
 	    double totalSum = X_DIM * Y_DIM ;
 	    for(int k = 0; k < his.length; k++){
 		     sum += his[k];
 		    if(sum / totalSum > 0.98) {
 		       _intens98 = k;
 		        k = his.length;
 		  }
 	    }
 	   return _intens98;
   }
   /**
   * Calculate intens10
   * @param int intens2 @param int intens98
   */
   public int calculateIntens10(int intens2,int intens98)
   {
 	  int _intens10;
 	// Calculate _intense10
 	  _intens10 = (int)Math.floor(((intens98 - intens2)*0.1) + intens2 + 0.5);
 	  return _intens10;
   }
   
   /**
    * Calculate intens40
    * @param int intens2 @param int intens98
    */
    public int calculateIntens40(int intens2,int intens98)
    {
  	  int _intens40;
  	 // Calculate _intens40
  	  _intens40 = (int)Math.floor(((intens98 - intens2)*0.4) + intens2 + 0.5);
  	  return _intens40;
    }
   
   
  
  
  
  
  
  
  
  
  
  
  
}
