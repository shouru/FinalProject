package LevelSet;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;
import org.medtoolbox.jviewbox.viewport.annotation.DynamicAnnotationShape;

import tools.GeometryContour;


public class LevelSetTool extends ViewportTool{

	/** 
	 * Computation function. 
	 */
	private Vector _SkullStripper;
	
	/** 
	 * ViewportCluster for displaying the viewports. 
	 */
	private ViewportCluster _vpc;
	
	/** 
	 * Viewport for storing the image. 
	 */
	private Viewport _origViewport;
	
	/** 
	 * The width of the image. 
	 */
	private int _imageWidth;
	
	/**
	 * The height of the image. 
	 */
	private int _imageHeight;
	
	/**
	 * The start postion of selected image. 
	 */
	private int _xStart, _yStart;
	
	
	/**
	 * Annotation dynamic shape. 
	 */
	private DynamicAnnotationShape _dyShape;
	
	
	/**
	*
	**/
	public LevelSetTool(Vector stripper)
	{          
		super("LevelSet Tool","Set alpha and beta");	
		_SkullStripper=stripper;
        SkullStripper slice = (SkullStripper) _SkullStripper.elementAt(0);
		_imageWidth=slice.getXdim();
	    _imageHeight=slice.getYdim();	    
	}
	
	
	/**
	   * Invoked when a mouse button is pressed on a ViewportCluster.
	   *
	   * @param vpc ViewportCluster which received the event.
	   * @param vp Viewport which the mouse cursor was on during the event.
	   * @param e Event created by the button press.
	   * @param button The button that was pressed.
	   */
	  public void mousePressed(ViewportCluster vpc, Viewport vp, MouseEvent e, int button)
	  {
		  
		   // Transform the mouse point to image coordinates.
			Point cursorPoint = new Point();
			AffineTransform imageTransform = vp.getViewportTransform();
			imageTransform.concatenate(vp.getImageTransform());
		    try {
				imageTransform.inverseTransform(e.getPoint(), cursorPoint);
				//System.out.println("x="+cursorPoint.x + "y="+cursorPoint.y);
				
				//to determine which slice it is.
				int slicenum=0;
				for(slicenum=0;slicenum<vpc.getViewports().size();slicenum++){
					if(vp==(Viewport)vpc.getViewports().get(slicenum)){
						break;
					}
				}
								
				
				
				SkullStripper slice = (SkullStripper)_SkullStripper.elementAt(slicenum);
				
				
				
				//print image force in that point 
				double f=slice.getImageForce(cursorPoint.x, cursorPoint.y);
				double phi=slice.getPhi(cursorPoint.x,cursorPoint.y);
				System.out.println("f="+f+"phi="+phi+" slice="+slicenum+"real slice="+(slice.slice+1));
			
				
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
		  
	  }
	  /**
		* Invoked when a mouse button is moved on a ViewportCluster.
		*
		* @param vpc ViewportCluster which received the event.
		* @param vp Viewport which the mouse cursor was on during the event.
		* @param e Event created by the button press.
		*/
		public void mouseMoved(ViewportCluster vpc, Viewport vp, MouseEvent e)
		{
		    try {
		    	// Transform the mouse point to image coordinates.
				Point cursorPoint = new Point();
				AffineTransform imageTransform = vp.getViewportTransform();
				imageTransform.concatenate(vp.getImageTransform());
				imageTransform.inverseTransform(e.getPoint(), cursorPoint);
				//System.out.println("x="+cursorPoint.x + "y="+cursorPoint.y);
				vp.removeAnnotation(_dyShape);
				GeometryContour _geometryContour = new GeometryContour(cursorPoint.x,cursorPoint.y,1,1);
				_dyShape = new DynamicAnnotationShape(_geometryContour.rectangle);
				_dyShape.setBackgroundColor(Color.yellow);
				vp.addAnnotation(_dyShape);
				vpc.repaint();
		
				
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			
			
			
		}
	
	
	
}
