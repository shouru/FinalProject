/*
 * @(#)Viewer.java		1.10 07/01/20
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
 
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Font;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import Texture.*;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;

import org.medtoolbox.jviewbox.imagesource.ImageReaderAdapter;
import org.medtoolbox.jviewbox.viewport.ViewportGrid;
import org.medtoolbox.jviewbox.viewport.ViewportToolBar;
import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.annotation.StaticAnnotationString;

import FileIO.FileReader;
import LevelSet.LevelSetTool;
import LevelSet.SkullStripper;
import tools.PanTool;
import tools.WindowLevelTool;
import tools.ZoomTool2;
import tools.LayoutTool;
import tools.ScrollTool;
import tools.WriteDataTool;


/**
 * The <code>Viewer</code> class is dedicated to display a set of medical images. The viewer
 * reads images from the file names provided on the command line as arguments
 * and displays them.
 */
public class Viewer extends JFrame
{
	/**
	 * Storing the input images from the original data.
	 */
	private Vector _allImages = new Vector(2);
	
	/**
	 * Arranging the display layout of the input images.
	 */
	private ViewportGrid _grid;
	
	/**
	 * A typical <code>JToolBar</code> for managing the function tools to the images.
	 */
	public ViewportToolBar _toolBar;
	
	/** 
	 * A typical <code>JPanel</code> for controlling the parameters during the evolution
	 * process and managing the image and contour display. 
	 */
	private JControlSet _jControlSet;
	
	/** 
	 * The default color for the contour. 
	 */
	private Color _contourColor = Color.yellow;
	
	/** 
	 * Input image width. 
	 */
	private int _width;
	
	/** 
	 * Input image height. 
	 */
    private int _height;
    
    /** 
     * Integer parameters for the evolution process: index of power and index of filter.
     */
    private int[] _intParameter = {1, 1};
    
    /** 
     * Double parameters for the evolution process: beta. 
     */
    private double[] _parameter = {1.8};
    
    /**
	 * Boolean for indicating if the result is superimposed by the input image for display,
	 * if displaying the intensity image, if the gamma value is normal or not, 
	 * and if the image gradient is used as the effective field.
	 */
    private boolean[] _boolean = {true, true, true, false};
    

  	/**
  	* A special class for skullstripping 
  	*/
    private Vector _skullstripper = new Vector(1,1); 
 
    /**
     * Constructs a viewer to display the input images with the specified file.
     * @param	file		a file representing the input image.
     * @throws IOException 
     */
    public Viewer(File file) throws IOException
    {
		super("LevelSet for brain image segmentation");
		
		// Create the GUI showing the images and toolbars.
		_setGUI(file);	
	 }


    /**
     * Application main method to start the whole process.
     * @param	args	command-line arguments as a image file name to view.
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException 
	{
		File file;
		// If there is no command-line arguments, pop up a JFileChooser and
		// let the user choose image file to load
		if (args.length == 0) {
	    	JFileChooser chooser = new JFileChooser("C:/Users/matlab/Desktop/R00525047/materials/sbd/3mm/t1/pn1_rf0");
	    	chooser.setFileHidingEnabled(true);
	    	chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    	if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
	    	}
	    	else {
	    		file = null;
	 		}
		}
		// Use command-line arguments
		else {
			file = new File(args[0]);
		}
		
		// Create a Viewer and make it visible
		
		Viewer viewer = new Viewer(file);
		viewer.setDefaultCloseOperation(EXIT_ON_CLOSE);
		viewer.setSize(800, 600);
		viewer.setVisible(true);
    }
    /**
    *Reads the image file using FileReader.java.
    *@param file an image file that are selected
    */
    /*public void _imageRead(File file)
    {
    	 // System.out.println(file);
    	  FileReader fileReader = new FileReader(file);
    	  ImageReader reader = fileReader.getImageReader();
    	  System.out.println(file);
    	  try{
    			// Resample data volume into 2D slices
    			BufferedImage[] imgs = null;		 
    			  for(int i = 0; i < reader.getNumImages(true); i++){
    			      _allImages.add(reader.read(i));
    			  }
    			
    	  }	
    	  catch(Exception e){		  
    		e.printStackTrace();
    	}
    	
    }*/

	/**
	 * Reads the image file using the image I/O plugins.
	 * @param	file	an image file that are selected.
	 * @throws IOException 
	 */
	public void _imageIO(File file) throws IOException
	{
		try {
			// 1st try: use FileImageInputStream as input
			Object input = new FileImageInputStream(file);
			Iterator it = ImageIO.getImageReaders(input);	    
			// Found no reader at the 1st try?
			if (!it.hasNext()) {
			    // 2nd try: use the file itself as input
			    input = file;
			    it = ImageIO.getImageReaders(file);
			    // Found no reader at the 2nd try?
			    if (!it.hasNext()) {
					// 3rd try: get a reader from the file name suffix
					String fileName = file.getName();
					int lastDot = fileName.lastIndexOf('.');
					if (lastDot >= 0) {
				    	String suffix = fileName.substring(lastDot + 1);
				    	it = ImageIO.getImageReadersBySuffix(suffix);
					}
				}
			}
			// Found any reader at all?
			if (it.hasNext()) {
			    ImageReader reader = (ImageReader)it.next();
			    reader.setInput(input, false);
				// Get the width and height of the first image.
				_width = reader.getWidth(0);
				_height = reader.getHeight(0);
			    // Wrap each image in the reader in an adapter for jViewBox
			    int numImages = reader.getNumImages(true);
			    for (int j = 0; j < numImages; j++) {
			    //test
					_allImages.add(new ImageReaderAdapter(reader, j));
					SkullStripper slice = new SkullStripper(j);
					slice.sliceInVolume=numImages;
					//for skullstripper ImageIO using 
					FileReader fileReader = new FileReader(file);
					slice._imageReader=fileReader.getImageReader();
                    _skullstripper.add(slice);
			    }
			}
			
			
			
			
			// No reader available for the file
			else {
			    System.out.println("Unable to find an image decoder for " + file);
			    System.out.println("Skipping the file");
			}
		}
		catch (IOException e) {
			System.out.println("Unable to read " + file + " due to " + e);
			System.out.println("Skipping the file");
		}
		// Check for at least one image to show
		if (_allImages.isEmpty()) {
		    System.out.println("No image to show");
		    System.exit(1);
		} 	
	     
		
	
	}
  
  
	/**
	 * Adds tools to the toolbar.
	 * @param	file	the input file to be referred for some tools.
	 */
	private void _addToolBar(File file)
	{
		_toolBar = new ViewportToolBar();
		// Add tools.
		_toolBar.addTool(new PanTool(), "Pan");
		_toolBar.addTool(new ZoomTool2(), "Zoom");
		_toolBar.addTool(new WindowLevelTool(), "Window/Level");
		_toolBar.addTool(new ScrollTool(),"Scroll");
	
		_toolBar.addTool(new WriteDataTool(file,_skullstripper), "WriteData");
	    _toolBar.addTool(new LevelSetTool(_skullstripper), "LevelSet");
	
	
	}


	/**
	 * Creates a blank image for the duplicate image in RGB mode and generates a GUI
	 * for the manipulation of the program.
	 * @param	file	the input file to be referred.
	 * @throws IOException 
	 */
	private void _setGUI(File file) throws IOException
	{
		/*if (file == null) {
			return;
		}
		
		// Read image via function _imageIO.
		_imageIO(file);
		
		//Set skullstripper width and height
		for(int i=0;i<_skullstripper.capacity();i++){   
			SkullStripper slice=(SkullStripper)_skullstripper.elementAt(i);
			slice.setDim(_width,_height);
		}*/
		
		// Create a ToolBar and add Tools to it.
		//_addToolBar(file);
		
		// Create a ViewportGrid to display the images.
		_grid = new ViewportGrid(_allImages);
		//_grid.setGrid(1,1);
		
		
		// Get the original viewport and buffered image.
		//Iterator it = _grid.getViewports().iterator();
		//Viewport origViewport = (Viewport)it.next();
		//BufferedImage origImage = (BufferedImage)origViewport.getImage();
		BufferedImage origImage = ImageIO.read(new File("C:/Users/matlab/Desktop/R00525047/materials/sbd/3mm/t1/pn1_rf0/t1_icbm_normal_3mm_pn1_rf0_025.dcm"));
		BufferedImage maskimage = ImageIO.read(new File("C:/Users/matlab/Desktop/R00525047/materials/sbd/truth/3mm/standard mask/I25staMask.png"));
		// Fill in the double array with the original data
		_height = origImage.getHeight();
		_width = origImage.getWidth();
      	double[][] data = new double[_height][_width];
	    Raster origRaster = maskimage.getData();
	    int xbegin = _width, ybegin = _height,xend = 0, yend = 0,roitotal=0;
 	    for (int j = 0; j < _height; j++) {
			for (int i = 0; i < _width; i++) {
				//data[j][i] = origRaster.getSampleDouble(i, j, 0);
				if(origRaster.getSampleDouble(i, j, 0)>0){
					data[j][i] = 1;
					if(j<ybegin)
						ybegin = j;
					if(j > yend)
						yend = j;
					if(i < xbegin)
						xbegin=i;
					if(i > xend)
						xend = i;
					roitotal++;
				}
				else
					data[j][i] = 0;
			}
	    }
 	    System.out.println("I am ready in");
 	    System.out.println("xbegin is "+ xbegin+" xend is "+ xend + " ybegin is "+ ybegin + " yend is "+yend+" roitotal is "+roitotal);
 	    RunLengthMat run = new RunLengthMat(data,origImage,xbegin,ybegin,xend,yend,roitotal);
 	    //TamuraTextureFeature run3 = new TamuraTextureFeature(data,origImage,xbegin,ybegin,xend,yend,roitotal);
 	    //Glcm run2 = new Glcm(data,origImage,xbegin,ybegin,xend,yend);
        // Ininitalization 
        for(int i=0;i<_allImages.size();i++){ 
        	ImageReaderAdapter image = (ImageReaderAdapter)_allImages.get(i);
        	
           //Pass image data and bufferedimage to skullstripper     	
           SkullStripper slice = (SkullStripper)_skullstripper.elementAt(i);
           //System.out.print("height="+_height +"in veiwer slice = "+slice.slice);
           slice.initialization(image.getBufferedImage());
        }
        
		// Link the ToolBar to the ViewportGrid
		_grid.registerViewportToolBar(_toolBar);
		// Create a JControlSet for adjusting controlled parameters.
		_jControlSet = new JControlSet(_skullstripper,_grid, file, this);
		// Remove all container elements.
		getContentPane().removeAll();
		// Arrange the ToolBar and ViewportGrid in this frame
		getContentPane().add(_toolBar, BorderLayout.NORTH);
		getContentPane().add(_grid, BorderLayout.CENTER);
		getContentPane().add(_jControlSet, BorderLayout.WEST);
		getContentPane().add(new JLabel(file.toString()), BorderLayout.SOUTH);
	    
	}
}
