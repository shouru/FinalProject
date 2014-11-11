/*
 * @(#)JControlSet.java		1.10 07/01/20
 *
 * ChargedFluid package
 *
 * COPYRIGHT NOTICE
 * Copyright (c) 2007 Herbert H. Chang, Daniel J. Valentino, Gary R. Duckwiler, and Arthur W. Toga
 * Laboratory of Neuro Imaging, Department of Neurology, UCLA.
 *
 * Copyright (c) 2012 Herbert H. Chang, Ph.D.
 * Computational Biomedical Engineering Laboratory (CBEL)
 * Department of Engineering Science and Ocean Engineering
 * National Taiwan University, Taipei, Taiwan
 */
 
import java.io.File;

import javax.imageio.ImageIO;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.GregorianCalendar;
import java.util.Date;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;

import org.medtoolbox.jviewbox.viewport.ViewportGrid;
import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportToolButton;
import org.medtoolbox.jviewbox.viewport.annotation.Annotation;
import org.medtoolbox.jviewbox.viewport.annotation.DynamicAnnotationImage;
import org.medtoolbox.jviewbox.viewport.annotation.DynamicAnnotationShape;

import LevelSet.SkullStripper;


/**
 * The <code>JControlSet</code> class is dedicated to the GUI setting for controlling 
 * parameters used in the evolution process and the charged fluid algorithm.
 */
public class JControlSet extends JPanel
{
	

  	
  	/**
  	* The LevelSetComputation class is an inner class for handling
  	* the levelset evolution
  	*/
  	private class LevelSetComputation extends Thread
  	{
  		/** 
  		 * The hour of the computation time. 
  		 */
		private int _hour;
		
		/** 
		 * The minute of the computation time. 
		 */
		private int _min;
		
		/** 
		 * The second of the computation time. 
		 */
		private int _sec;
		
		/** 
		 * The mini second of the computation time. 
		 */
		private int _msec;
		
		/**
		 * To determine computation is frozend or not.
		 */
  		private boolean isFrozen = false;
		/**
		* Computing Levelset function
		*/
		public void run(){
			
			try{
				// Set the starting time.
				GregorianCalendar gc = new GregorianCalendar();
				Date startDate = gc.getTime();
				long startMS = startDate.getTime();
				
				//calculate histogram
				_histogram(_skullstripper);
				
				// The slices, that are around mid-sagittal slices, are estimated to be 
			    // within SLICE_1 and SLICE_2
			    double temp = _skullstripper.size();
			    int SLICE_1 = (int) (temp * 0.45);
			    int SLICE_2 = (int) (temp * 0.55);
				
				
				int iter=0;
				
				

                    //start at center slice
					int num = _skullstripper.size()/2-1;
				    //threshold function mode
				    int thresmode = 0;
				    
				    SkullStripper slice = (SkullStripper) _skullstripper.get(num);	    
					slice.initialParameter();
					//slice.SetZeroLevel();
				//	while(true){
						
				      while(iter<1500&&slice.isStationary()==false&&slice.getisFrozen()==false){
				        _origViewport[num].removeAnnotation(slice.annImage);	
				        //do skull stripping
					    slice.Skullstrip();
				        slice.annImage = new DynamicAnnotationImage(slice.getAnnotationImage());
				        _origViewport[num].addAnnotation(slice.annImage);
				        _grid.repaint(_origViewport[num]);
					    // Let the system wait for 5 ms for updating the display.
					    Thread.sleep(10);
					    iter++;
					  }
				      System.out.println("center slice area = "+slice.calculateMaskArea(slice.phi));
				      //7500
				      /*
				      if(slice.calculateMaskArea(slice.phi)<6500){
				    	  _origViewport[num].removeAnnotation(slice.annImage);			
				    	 // slice.AdjustTh(-0.05);
				 	 
				    	  slice.initialParameter();     
				    	  thresmode = slice.switchTh(1);
				    	  iter=0;
				      }
				      
				     
				      else
				    	  break;
					  */
					//}
			      
					slice.saveMaskImage();
					int adjusttime=0;
					
					
					
					//compute lower slice 
					
					for(int i=num-1;i>=0;i--){
						System.out.println("current slice = "+(i+1));
						iter=0;
						SkullStripper lowerslice = (SkullStripper) _skullstripper.elementAt(i);
						SkullStripper formerslice = (SkullStripper) _skullstripper.elementAt(i+1);
						lowerslice.initialphiwithFormer(formerslice.phi);
						lowerslice.initialParameter();
					//	lowerslice.switchTh(thresmode);
						
					
						lowerslice.phi=lowerslice.shrinkPhiZero(lowerslice.phi,(int)Math.floor(Math.abs(lowerslice.INTER_SLICE_DIST)+0.5));
					
						while(iter<600 && lowerslice.isStationary()==false){
						
							 _origViewport[i].removeAnnotation(lowerslice.annImage);	
							
						       if(lowerslice.calculateMaskArea(lowerslice.phi)<100){
							       lowerslice.setPhiZero();
							       _grid.repaint(_origViewport[i]);
							       break;
							   }   
						       
						       
						       //do skull stripping
							   lowerslice.Skullstrip();
						       lowerslice.annImage = new DynamicAnnotationImage(lowerslice.getAnnotationImage());
						       _origViewport[i].addAnnotation(lowerslice.annImage);
						       _grid.repaint(_origViewport[i]);
							   // Let the system wait for 2 ms for updating the display.
							   Thread.sleep(10);
							   iter++;
							   
							}
					    if(!(i>SLICE_1&&i<SLICE_2)) { 	
					      if(_isPhiAcceptable(lowerslice.phi,lowerslice,formerslice,formerslice.phi)==false&&adjusttime<=2)	{
					       		// adjust parameter of curvature and stanCounter and Th				    	     
					    	    adjusttime++;
					    	    System.out.println("lower adjust slice="+(i+1));
					    	    i=i+1;
					    	   
					    	    //if adjust time equal to 2 replace current phi with former phi
					    	    if(adjusttime==3){					  
					    	       i--;
					    	       System.out.println("replace slice "+(lowerslice.slice+1)+" with former"+"i="+i);
					    	       _origViewport[i].removeAnnotation(lowerslice.annImage); 
					    	       lowerslice.initialphiwithFormer(formerslice.phi);
					    	       lowerslice.annImage = new DynamicAnnotationImage(lowerslice.getAnnotationImage());
					    	       _origViewport[i].addAnnotation(lowerslice.annImage);
					    	       _grid.repaint(_origViewport[i]);
					    	       Thread.sleep(10);
					    	       lowerslice.saveMaskImage();
					    	       adjusttime=0;
					    	   }
					        }  
					      else{
					           lowerslice.saveMaskImage();
					            adjusttime=0;   
					       }
					    }
					   else{
						     lowerslice.saveMaskImage();
					     }
					}
				
					adjusttime=0;
				
					
					
					// compute upper slices
					
					for(int i=num+1;i<_skullstripper.capacity();i++){
						iter=0;
						SkullStripper upperslice = (SkullStripper) _skullstripper.elementAt(i);
						SkullStripper formerslice = (SkullStripper) _skullstripper.elementAt(i-1);
						upperslice.initialphiwithFormer(formerslice.phi);
						upperslice.initialParameter();
						
						//upperslice.switchTh(thresmode);
						upperslice.phi=upperslice.shrinkPhiZero(upperslice.phi,(int)Math.floor(Math.abs(upperslice.INTER_SLICE_DIST)-0.5));
						while(iter<700 && upperslice.isStationary()==false){
						       _origViewport[i].removeAnnotation(upperslice.annImage);	
						       
						       if(upperslice.calculateMaskArea(upperslice.phi)<10){
								    upperslice.setPhiZero();
								    _grid.repaint(_origViewport[i]);
									break;
								}    
						       
						       //do skull stripping
							   upperslice.Skullstrip();
						       upperslice.annImage = new DynamicAnnotationImage(upperslice.getAnnotationImage());
						       _origViewport[i].addAnnotation(upperslice.annImage);
						       _grid.repaint(_origViewport[i]);
							   // Let the system wait for 2 ms for updating the display.
							   Thread.sleep(7);
							   iter++;
							}
					  if(!(i>SLICE_1&&i<SLICE_2)) { 	
					     if(_isPhiAcceptable(upperslice.phi,upperslice,formerslice,formerslice.phi)==false&&adjusttime<=2)	{
					    	i-=1;
					    	adjusttime++;
					    	
					    	 //if adjust time equal to 2 replace current phi with former phi
				    	    if(adjusttime==3){					  
				    	       i++;
				    	       System.out.println("replace slice "+(upperslice.slice+1)+" with former"+"i="+i);
				    	       _origViewport[i].removeAnnotation(upperslice.annImage); 
				    	       upperslice.initialphiwithFormer(formerslice.phi);
				    	       upperslice.annImage = new DynamicAnnotationImage(upperslice.getAnnotationImage());
				    	       _origViewport[i].addAnnotation(upperslice.annImage);
				    	       _grid.repaint(_origViewport[i]);
				    	       Thread.sleep(10);
				    	       upperslice.saveMaskImage();
				    	       adjusttime=0;
				    	   }  	
					    	
					     }
					    else{
					        upperslice.saveMaskImage();
					        adjusttime=0;   
					    }
					  }
					  else{
						  upperslice.saveMaskImage();
					  }
						 
					}
					
				
					
					
					
					
					
				
				
				
				
				
				
				
				
			}
			catch (Exception e){
				System.out.println("Error:  " + e.getMessage());	
			}	
			
			
			
			
			
		}
		
		
		
		
		public int returnToRGB(int R, int G, int B) {
			int RGB = 0x00000000;
			int alpha = (0xff << 24);
			int Red = (R & 0xff) << 16;
			int Green = (G & 0xff) << 8;
			int Blue = (B & 0xff);
			RGB = alpha | Red | Green | Blue;
			return RGB;
			}
		
		/**
		 * Calculates the computation time in terms of hour, min, sec and minisec with the
		 * given time in minisecs.
		 * @param	ms	computation time in minisecond.
		 */
		private void _computeHourMinSecond(long ms)
		{
			int timeInSec = (int)(ms/1000);
			_msec = (int)(ms - timeInSec * 1000);
			int timeInMin = (int)(timeInSec/60);
			_sec = timeInSec - timeInMin * 60;
			_hour = (int)(timeInMin/60);
			_min = timeInMin - _hour * 60;
			
		}
		 /**
		   * If the segmentation results, represented by phi, is acceptable.
		   *
		   * @param phi to check on.
		   * @param current slide
		   * @param previous slide
		   * @param lastPhi Phi in last (or previous) slide.
		   * @return True is phi is acceptable; or false if it is unacceptable.
		   */
		  private boolean _isPhiAcceptable(double[][] phi,SkullStripper current, SkullStripper ac, 
						   double[][] lastPhi)
		  { 
		    boolean isAcceptable = true;
		    double HLIMIT_JACCARD = 0.90;
		    double LIMIT_JACCARD = 0.75;
		    if(true){
		      double[] temp = _evaluate(lastPhi, phi);
		      double jaccard = temp[0];
		      int maskArea = ac.calculateMaskArea(phi);
		      if( maskArea > 10000){
		    	 if(jaccard < HLIMIT_JACCARD) {System.out.print("Adjust th, ");}//current.AdjustTh(0.05); isAcceptable = false;} 
			     if(jaccard < LIMIT_JACCARD){System.out.print("Adjust curvature, ");current.AdjustCurva(); isAcceptable = false;}
		      }
		      else {
		    	if(true){
			       int lastMaskArea = 0;
			       int contourLength = 0;
			       lastMaskArea = ac.calculateMaskArea(lastPhi);
			       double difference = maskArea - lastMaskArea;
		      
			       double MAX_AREA_DIF = 0;
			       contourLength = ac.calculateContourLength(lastPhi);
			       if(contourLength > 0) 
			           MAX_AREA_DIF = contourLength *(Math.abs(3)+1) * 2;
		       	  else MAX_AREA_DIF = difference; 
			  
			  // If the difference between lastMaskArea and maskArea is within 
			  // 15% of lastMaskArea, then phi is acceptable
			   if( difference > MAX_AREA_DIF ) {current.AdjustCurva(); isAcceptable = false;}
			}
		      }
		    }
		    return isAcceptable;
		  }
		
		  /**
		   * Evaluate the skull-stripping algorithm.
		   *
		   * @param phi an array containing the family of the level sets.
		   * @param labelBImg a buffered image which serves as the golden standard. 
		   * @return An one element array of double values. The element is the 
		   *         Jaccard coefficient of the given phi2 compared to the given phi1. 
		   */
		  private double[] _evaluate(double[][] phi1, double[][] phi2)
		  {

		    // True positive is successfully segmented pixels
		    double TP = 0;

		    // Pixels in the segmentation result that do not belong to the golden 
		    // standard: false positive
		    double FP = 0;

		    // pixels of the golden standard but do not belong to the segmentation
		    // results: false negative
		    double FN = 0;
		    
		    double var1 = 0;
		    double var2 = 0;
		    double BOUNDARY_VALUE = -0.5;
		    for(int x = 0; x < phi1.length; x++){
		      for(int y = 0; y < phi1[0].length; y++){
			var1 = phi1[x][y];
			var2 = phi2[x][y];

			// True positive
			if(var1 >= BOUNDARY_VALUE && var2 >= BOUNDARY_VALUE) TP += 1;
			
			// False positive
			if(var1 >= BOUNDARY_VALUE && var2 < BOUNDARY_VALUE) FP += 1;
			
			// False negative
			if(var1 < BOUNDARY_VALUE && var2 >= BOUNDARY_VALUE) FN += 1;
		      }
		    }    
		    double jaccard = TP / (TP + FP + FN);
		    double[] ans = new double[2];
		    ans[0] = jaccard;
		    return ans;
		  }
		  
		  double _intens2;
		  double _intens98;
		  double _intens10;
		  double _intens40;
		  /**
		   * Calculate the histogram of an image and initialize the class members of
		   * _intens2 and _intens98.
		   */
		  private void _histogram(Vector skullStrippers)
		  {
		    // Find the maximum intensity
		    int maxI = 0;
		    short[][] array = null;
		    BufferedImage image = null;
		    SkullStripper ac = null;
		    for(int z = 0; z < skullStrippers.size(); z++ ){
		      ac = (SkullStripper)skullStrippers.elementAt(z);
		      image = ac.getInputImage();
		      array = ac.create2DArray(image);
		      for(int x = 0; x < array.length; x++ ){
			for( int y = 0; y < array[0].length; y++ ){
			  if(maxI < array[x][y]) maxI = array[x][y];
			}
		      }
		    }

		    // Initialize the histogram array
		    int[] hist = new int[maxI + 1];

		    // Construct histogram array
		    int intens;
		    for(int z = 0; z < skullStrippers.size(); z++ ){
		      ac = (SkullStripper)skullStrippers.elementAt(z);
		      image = ac.getInputImage();
		      array = ac.create2DArray(image);
		      for(int x = 0; x < array.length; x++ ){
			for( int y = 0; y < array[0].length; y++ ){
			  intens = array[x][y];
			  hist[intens]++;
			}
		      }
		    }

		    // Calculate _intens2
		    double sum = 0;
		    ac = (SkullStripper)skullStrippers.elementAt(0);
		    image = ac.getInputImage();
		    int width = image.getWidth();
		    int height = image.getHeight();
		    double totalSum = width * height * skullStrippers.size();
		    for(int k = 0; k < hist.length; k++){
			sum += hist[k];
			if(sum / totalSum > 0.02) {
			    _intens2 = k;
			    k = hist.length;
			}
		    }

		    // Calculate _intens98
		    sum = 0;
		    for(int k = 0; k < hist.length; k++){
			sum += hist[k];
			if(sum / totalSum > 0.98) {
			    _intens98 = k;
			    k = hist.length;
			}
		    }

		   // Calculate _intense10
		    _intens10 = Math.floor(((_intens98 - _intens2)*0.1) + _intens2 + 0.5);

		    // Calculate _intens40
		    _intens40 = Math.floor(((_intens98 - _intens2)*0.4) + _intens2 + 0.5);
		    
		    for(int i=0;i<skullStrippers.size();i++){
		     SkullStripper slice = (SkullStripper)skullStrippers.get(i);
		     slice._calculator.setIntensity(maxI, 0);
		     slice.setThresholdIntensities(_intens2,_intens10,_intens40,_intens98);
		    }
		  
  	     }
  	}
  	
	/**
	 * Arranging the display layout of the input images.
	 */
	private ViewportGrid _grid;
	
	/**
	 * The original view port for the image display.
	 */
	private Viewport[] _origViewport;
	
	/** 
	 * The original buffered image corresponding to the <tt>_origViewport</tt>. 
	 */
  	private BufferedImage _origImage;
  	

    
    /**
  	* A special class for skullstripping 
  	*/
    private Vector _skullstripper; 
    
    
    
    
	/** 
  	 * Weighting factor for the electric potential to balance the image potential, which 
  	 * is the modulus of the image gradient. 
  	 */
	private double _beta;
	
	/**
	 * 
	 *  Weighting factor for the Image force 
	 * 
	 * 
	 */
	private double _alpha;
	
	/**
	 *  Radius
	 * 
	 */
	
	private double _radius;
	
	
	/**
	 * The specified electrostatic equilibrium condition for the system. 
	 */
	private double _gamma;
	
	/** 
	 * A text field Swing component for the value of beta.
	 */
	private JTextField _betaText = new JTextField(3);
	
	
	/** 
	 * A text field Swing component for the value of alpha.
	 */
	private JTextField _alphaText = new JTextField(3);
	/** 
	 * A text field Swing component for the value of radius.
	 */
	private JTextField _radiusText = new JTextField(3);
	
	
	
	
	
	/** 
	 * A slider Swing component for the integer value of beta.
	 */
	private JSlider _betaSlider;
	
	/** 
	 * A slider Swing component for the integer value of alpha.
	 */
	private JSlider _alphaSlider;
	
	/** 
	 * A slider Swing component for the integer value of radius.
	 */
	private JSlider _radiusSlider;
	/** 
	 * A slider Swing component for the decimal value of beta.
	 */
	private JSlider _betaSlider2;
	
	/** 
	 * A radio button Swing component of using circle-like shapes for the initial contours.
	 */
	private JRadioButton _circleButton;
	
	/** 
	 * A radio button Swing component of using square shapes for the initial contours.
	 */
	private JRadioButton _squareButton;
	
	/** 
	 * A radio button Swing component for using filter #1.
	 */
	private JRadioButton _filter1Button;
	
	/** 
	 * A radio button Swing component for using filter #2.
	 */
	private JRadioButton _filter2Button;
	
	/** 
	 * A button Swing component for submitting the parameter changes.
	 */
	private JButton _actionSubmitButton = new JButton("Submit");
	
	/** 
	 * A button Swing component for pausing the program.
	 */
	private JButton _pauseButton = new JButton("Pause");
	
	/** 
	 * A button Swing component for starting the program.
	 */
	private JButton _startButton = new JButton("Start");
	
	/** 
	 * A button Swing component for terminating the program.
	 */
	private JButton _terminateButton = new JButton("Terminate");
	
	private File _file;
	private Viewer _viewer;
	
	
	/**
	 * Constructs a new control set Swing component with the specified <code>Matrix</code>,
	 * <code>ViewportGrid</code>, and <code>JFilterSet</code>.
	
	 * @param	grid				a view port grid containing the input images.
	 */
	public JControlSet(Vector stripper, ViewportGrid grid, File file, Viewer viewer)
	{
		_file = file;
		_viewer = viewer;
		
		
		_skullstripper=stripper;
		//get alpha and radius
		
		
		
		
		
		
		SkullStripper slice = (SkullStripper)_skullstripper.elementAt(0);
		_alpha=slice.getAlpha();
		_radius=slice.getRadius();
		
		
		
		
		_grid = grid;
		
		_origViewport = new Viewport[_grid.getViewports().size()];
		for(int i=0;i<_grid.getViewports().size();i++){
		   _origViewport[i]=(Viewport)_grid.getViewports().get(i);
		}
	    //get center slice
		_origImage = (BufferedImage)_origViewport[_origViewport.length/2].getImage();
		
		// Parameter initialization.
		this.initialParameter();
		// Setup of GUI.
		this.setupOfGUI();
		// Add the listeners to the control elements.
		this.addListener();
	}
	
	
	/**
	 * Initializes and allocates the parameters for display.
	 */
	public void initialParameter()
	{
		_alphaSlider = new JSlider(0, 100, (int)_alpha);
		_radiusSlider = new JSlider(0, 80, (int)_radius);
		
		// JTextField initialization.
		Float value = new Float((_alphaSlider.getValue()/100));
		_alphaText.setText(value.toString());
		
		Integer value1 = new Integer(_radiusSlider.getValue());
		_radiusText.setText(value1.toString());
		
	}
	
	
	/**
	 * Adds listeners to the Swing components subject to the changes of status.
	 */
	public void addListener()
	{
		/* Alpha */
		// Add ChangeListener to the JSlider subjected to stateChanged.
		_alphaSlider.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int temp=_alphaSlider.getValue();
					Float alpha = new Float((float)temp/100);
				//	System.out.println(alpha);
					_alphaText.setText(alpha.toString());	
					_alphaText.setForeground(Color.blue);
				}
			}
		);
		/*Radius*/
		// Add ChangeListener to the JSlider subjected to stateChanged.
		_radiusSlider.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					Integer radius = new Integer(_radiusSlider.getValue());
					_radiusText.setText(radius.toString());	
					_radiusText.setForeground(Color.blue);
				}
			}
		);
		// Add ActionListener to the JTextField responding to the JSlider
		_alphaText.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					_alphaText.setForeground(Color.red);
					Float alpha = new Float(_alphaText.getText());
					_alphaSlider.setValue((int)(alpha.floatValue()*100));
				}
			}
		);
		// Add ActionListener to the JTextField responding to the JSlider
		_radiusText.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					 _radiusText.setForeground(Color.red);
					  Integer radius = new Integer(_radiusText.getText());
					 _radiusSlider.setValue(radius.intValue());
						}
					}
		);
		
		// Add ActionListener to the action JButtons.
		_actionSubmitButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				
				    Float alpha = new Float(_alphaText.getText());
				    Integer radius = new Integer(_radiusText.getText());
				    for(int i=0;i<_skullstripper.capacity();i++){
				    	SkullStripper slice =(SkullStripper) _skullstripper.elementAt(i);
				        slice.setParam(alpha.floatValue(), radius.intValue());
				    }
				    _alphaText.setForeground(Color.gray);
				    _radiusText.setForeground(Color.gray);
				}
			}
		);
		_pauseButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
                     System.out.println("Level set pause");					
					 for(int i=0;i<_skullstripper.capacity();i++){
					    	SkullStripper slice =(SkullStripper) _skullstripper.elementAt(i);
					        slice.setPause(true);
					    }				
				}
			}
		);
		_startButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
			
	                    System.out.println("levelset compute start");
						_startLevelSet();
						
					
				}
			}
		);
		_terminateButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
				}
			}
		);
	}
	
	
	/**
	 * Creates a new GUI for showing the Swing components.
	 */
	public void setupOfGUI()
	{
		// Setup of JSlider parameters.
		_alphaSlider.setPaintTicks(true);
		_alphaSlider.setMajorTickSpacing(10);
		_alphaSlider.setMinorTickSpacing(2);
		_alphaSlider.setPaintLabels(true);
		_radiusSlider.setPaintTicks(true);
		_radiusSlider.setMajorTickSpacing(1);
		_radiusSlider.setPaintLabels(true);
    	
    	
    	// Set TextField color blue.
    	_alphaText.setForeground(Color.blue);
    	// Set TextField color blue.
    	_radiusText.setForeground(Color.red);
    	
		// Setup of Panel arrangement (alpha)
		JPanel alphaSliderPanel = new JPanel(new BorderLayout());
		alphaSliderPanel.add(_alphaSlider, BorderLayout.CENTER);
		
		JPanel alphaPanel = new JPanel(new BorderLayout());
		JLabel alphaLabel = new JLabel("Alpha: ");
		alphaPanel.add(alphaLabel, BorderLayout.WEST);
		alphaPanel.add(_alphaText, BorderLayout.EAST);
		alphaPanel.add(alphaSliderPanel, BorderLayout.SOUTH);
		
		// Setup of Panel arrangement (radius)
		JPanel radiusSliderPanel= new JPanel(new BorderLayout());
		radiusSliderPanel.add(_radiusSlider, BorderLayout.CENTER);
		
		JPanel radiusPanel = new JPanel(new BorderLayout());
		JLabel radiusLabel = new JLabel("Radius: ");
		radiusPanel.add(radiusLabel, BorderLayout.WEST);
		radiusPanel.add(_radiusText, BorderLayout.EAST);
		radiusPanel.add(radiusSliderPanel, BorderLayout.SOUTH);
		
		
		
		JPanel actionButtonPanel = new JPanel(new GridLayout(2, 2, 5, 5));	
		actionButtonPanel.add(_actionSubmitButton);
		actionButtonPanel.add(_pauseButton);
		actionButtonPanel.add(_startButton);
		actionButtonPanel.add(_terminateButton);
		
		// Main panels : _paraPanel, _displayPanel, and _actionPanel
		JPanel paraPanel = new JPanel(new BorderLayout(0, 5));
		
		paraPanel.add(alphaPanel, BorderLayout.NORTH); 
		paraPanel.add(radiusPanel, BorderLayout.CENTER); 
		paraPanel.setBorder(new TitledBorder(new EtchedBorder(), "PARAMETER"));

		JPanel actionPanel = new JPanel(new BorderLayout(0, 5));
		actionPanel.add(actionButtonPanel, BorderLayout.CENTER);
		actionPanel.setBorder(new TitledBorder(new EtchedBorder(), "ACTION"));
		
		// Add main panels to be shown.
		int width = 100;
		this.setSize(width, 2*width);
		this.setLayout(new BorderLayout(0,10));
		this.add(paraPanel, BorderLayout.NORTH);
		this.add(actionPanel, BorderLayout.SOUTH);	
	}
	
	

	/**
	* Start level set algorithm by threading the class
	*<code>LevelSetComputation<code>
	*/
	private void _startLevelSet()
	{
	  	LevelSetComputation level = new LevelSetComputation();
		level.start();
		
		
	}

}