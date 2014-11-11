/*
 * @(#)ZoomTool2.java		1.10 07/01/20
 *
 * ChargedFluid package
 *
 * COPYRIGHT NOTICE
 * Copyright (c) 2007 Herbert H.H. Chang, Daniel J. Valentino, Gary R. Duckwiler, and Arthur W. Toga
 * Laboratory of Neuro Imaging, Department of Neurology, UCLA.
 */

package tools;

import java.awt.event.MouseEvent;
import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;
// import tools.ViewportGridAppendable;

/**
 * Tool for scaling an image inside a Viewport.  When the mouse cursor is
 * pressed and dragged upwards on an image in a Viewport, the image is
 * magnified until the mouse button is released.  If the mouse is instead
 * dragged downwards, the image is minified.
 */
public class ZoomTool2 extends ViewportTool
{
  /**
   * Number of pixels the mouse cursor must move to change the current
   * scale factor by 1.
   */
  private double _scaleSensitivity;

  /** Minimum allowed value for the scale factor of an image. */
  private double _minimumScale;

  /** Maximum allowed value for the scale factor of an image. */
  private double _maximumScale;

  /** Current scale factor along X. */
  private double _currentScaleX;

  /** Current scale factor along Y. */
  private double _currentScaleY;

  /** 
   * Y coordinate of the mouse cursor when a mouse button is pressed inside
   * a ViewportCluster.
   */
  private int _anchorY;

  private int _anchorX;

  private int _rX;

  private int _rY;

  private int _pX;

  private int _pY;

  /** Viewport the mouse cursor is in when a mouse button is pressed. */
  private Viewport _vp = null;

  /** Constructs a ZoomTool2 with default settings. */
  public ZoomTool2()
    {
      this(200, 0.01, 5.0);
    }

  /**
   * Constructs a ZoomTool2 with the specified mouse scale sensitivity and
   * bounds on the allowed scale factor.
   *
   * @param scaleSensitivity Number of pixels the mouse cursor must move to
   *                         change the current scale factor by 1.
   * @param minimumScale Minimum below which the scale factor is not allowed
   *                     to go under.
   * @param maximumScale Maximum above which the scale factor is not allowed
   *                     to go above.
   *
   * @throws IllegalArgumentException If the scale sensitivity,
   *                                  minimum scale, or maximum scale
   *                                  are invalid.
   */
  public ZoomTool2(int scaleSensitivity, double minimumScale,
		  double maximumScale)
    {
      super("Zoom Tool", "Zoom an Image", "", "");

      // Check bounds
      if (scaleSensitivity <= 0 || minimumScale < 0.0001 ||
	  maximumScale < 0.0001)
	{
	  throw new IllegalArgumentException("ZoomTool: A scale sensitivity " +
					     "of " + scaleSensitivity +
					     " and a minimum scale of " +
					     minimumScale + " and a maximum " +
					     "scale of " + maximumScale +
					     " are not allowed in the " +
					     "constructor.");
	}

      _scaleSensitivity = scaleSensitivity;
      _minimumScale = minimumScale;
      _maximumScale = maximumScale;
    }

  /**
   * Invoked when a mouse button is pressed on a ViewportCluster.
   *
   * @param vpc ViewportCluster which received the event.
   * @param vp Viewport which the mouse cursor was on during the event.
   * @param e Event created by the button press.
   * @param button The button that was pressed.
   */
  public void mousePressed(ViewportCluster vpc, Viewport vp, MouseEvent e, 
			   int button)
    {
      _vp = vp;
//       ((ViewportGridAppendable)vpc).updateNumberAnnotation(false);

	  // Set the anchor and current scale
      if (_vp != null) {
	_anchorX = e.getX();
	_anchorY = e.getY();
	_pX = _vp.getPanX();
	_pY = _vp.getPanY();
	_rX = _anchorX - _vp.getX() - _pX;
	_rY = _anchorY - _vp.getY() - _pY;
	_currentScaleX = _vp.getScaleX();
	_currentScaleY = _vp.getScaleY();

	// Save the current state
	_vp.setCurrentState( _vp.getCurrentState() );
	// _vp.setAnnotationEnabled(false);
      }
    }

  /**
   * Invoked when a mouse button is pressed and then the mouse is dragged
   * on a ViewportCluster.
   *
   * @param vpc ViewportCluster which received the event.
   * @param vp Viewport which the mouse cursor was on during the event.
   * @param e Event created by the button drag.
   * @param button The button that was pressed and dragged.
   */
  public void mouseDragged(ViewportCluster vpc, Viewport vp, MouseEvent e, 
			   int button)
    {
      if (_vp != null && vpc != null) {

	// Compute the change in the X scale factors
	// ***JACK*** Use Y mouse coord to determine X scale factor???
	double deltaScaleX = (double)(e.getY() - _anchorY)/_scaleSensitivity;

	// Calculate the new scale factors
	double scaleX = _currentScaleX + deltaScaleX;
	if (scaleX < _minimumScale) { scaleX = _minimumScale; }
	else if (scaleX > _maximumScale) { scaleX = _maximumScale; }

	// ***JACK***
	// deltaScaleY should be deltaScaleX * _currentScaleY / _currentScaleX
	// to maintain aspect ratio
	double aspectRatio = _currentScaleY / _currentScaleX;
	double scaleY = _currentScaleY + deltaScaleX * aspectRatio;

	// ***JACK***
	// Similarily, range of scaleY w.r.t aspectRatio
	if (scaleY < _minimumScale * aspectRatio)
	    { scaleY = _minimumScale * aspectRatio; }
	else if (scaleY > _maximumScale * aspectRatio)
	    { scaleY = _maximumScale * aspectRatio; }

	// Calculate new pan
	double rX = _rX * scaleX / _currentScaleX;
	double rY = _rY * scaleY / _currentScaleY;
	_vp.setPan((int)(_pX + _rX - rX), (int)(_pY + _rY - rY));
	
	// Pan the anchor point to origin
	// _vp.setPan(_vp.getPanX() - dispX, _vp.getPanY() - dispY);

	// Then scale
	_vp.setScale(scaleX, scaleY);

	// Pan the anchor point back to its original position
	// _vp.setPan(_vp.getPanX() + dispX, _vp.getPanY() + dispY);

	vpc.repaint(_vp);
      }
    }

  /**
   * Invoked when a mouse button is pressed and then the mouse is dragged
   * on a ViewportCluster.
   *
   * @param vpc ViewportCluster which received the event.
   * @param vp Viewport which the mouse cursor was on during the event.
   * @param e Event created by the button drag.
   * @param button The button that was pressed and dragged.
   */
  public void mouseReleased(ViewportCluster vpc, Viewport vp, MouseEvent e, 
			   int button)
    {
      if (_vp != null) { 
// 	((ViewportGridAppendable)vpc).updateNumberAnnotation(true);
	vpc.repaint(_vp);
      }
    }
}
