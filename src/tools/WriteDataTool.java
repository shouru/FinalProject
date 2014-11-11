/*
jViewBox 2.0 alpha

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */
 
/*
ChargedFluid package

COPYRIGHT NOTICE
Copyright (c) 2003 Herbert H.H. Chang
 */
 
package tools;

import LevelSet.SkullStripper;

import java.io.File;
import java.io.IOException;
import java.awt.Point;
import java.util.Vector;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;

/**
 * Tool for exporting an image inside a Viewport to a file on disk.  When the
 * mouse cursor is pressed on an image in a Viewport, a dialog box appears
 * asking the user for a file name.  Upon approving a file name, the displayed
 * image is written to the file in JPEG format as it appears in the Viewport.
 *
 * @version 1 October 2003
 */
public class WriteDataTool extends ViewportTool
{
  /** File directory the files were written to. */
  private File _parentFile;
  /** String filename of the same part. */
  private String _fileName;
  /** Skull-stripping volume */
  private Vector _skullstripper;
  
  /** Constructs an ExportTool. */
  public WriteDataTool(File file,Vector skull)
  {
    super("Write Data Tool", "Save segmentation results", "", "");
    
 
    String parentName = file.getParent();
    _fileName = file.getName();
    _skullstripper = skull;
    
  
    // Extract the file name without the extension.
    int indexName = _fileName.lastIndexOf(".");
    _fileName = _fileName.substring(0, indexName);
    
    // Extract the last name of the parent directories.
    // For Windows machine.
    int indexParent = parentName.lastIndexOf("\\");
    // For Unix machine.
    if (indexParent == -1) {
    	indexParent = parentName.lastIndexOf("/");
    }
    parentName = parentName.substring(indexParent+1);
    
  }	

	
  /**
   * Invoked when a mouse button is pressed on a ViewportCluster.
   *
   * @param vpc ViewportCluster which received the event.
   * @param vp Viewport which the mouse cursor was on during the event.
   * @param e Event created by the button press.
   * @param button The button that was pressed.
 * @throws IOException 
   */
  public void mousePressed(ViewportCluster vpc, Viewport vp, MouseEvent e, int button) throws IOException
  {
  
		
	//to determine which slice it is.
	int slicenum=0;
	for(slicenum=0;slicenum<vpc.getViewports().size();slicenum++){
		if(vp==(Viewport)vpc.getViewports().get(slicenum)){
				break;
			}
	}   
	  
	  
	  
  	// Generate the files.
  	String fileName = _fileName + "_" + (slicenum+1);
    File headerFile = new File(fileName + ".png");  
    SkullStripper save = (SkullStripper)_skullstripper.get(slicenum);
    BufferedImage outImage ; 
    // detecting if contour is null or not
    if(save.phi!= null){
       outImage = save.OutputImageWithContour();}
    else{
    	outImage = save.getInputImage();
    }
    ImageIO.write(outImage, "png", headerFile);	     
	System.out.println("save " + fileName);  
	  
  }
}