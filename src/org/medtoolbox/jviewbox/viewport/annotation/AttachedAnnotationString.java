/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.annotation;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

/**
 * AnnotationString whose text layout is defined in Annotation Space with
 * respect to a reference point, the <i>location</i>, while an additional
 * translation makes it appear attached to a point in Image Space, called the
 * <i>attach point</i>. More precisely, the attach point and yet another point
 * in Annotation Space, called <i>hot spot</i>, are transformed into Viewport
 * Space using Image and Annotation Transform respectively. An additional
 * translation which brings the hot spot to the attach point in Viewport Space
 * is then applied to this AttachedAnnotationString after it is first
 * transformed in the same way as a <code>StaticAnnotationString</code>. The
 * net effect is an AttachedAnnotationString is sized and oriented relative to
 * the Viewport's bounds and positioned relative to the image.
 *
 * @see org.medtoolbox.jviewbox.viewport.Viewport for more details about
 *      coordinate spaces and transforms.
 *
 * @version January 8, 2004
 */
public class AttachedAnnotationString extends AnnotationString
{
    // --------------
    // Private fields
    // --------------

    /** Point in Image Space to attach this Annotation to. */
    private Point2D _attachPoint;

    /** Point in Annotation Space which is attached to the Attach Point. */
    private Point2D _hotSpot;

    // ------------
    // Constructors
    // ------------

    /**
     * Constructs an AttachedAnnotationString given a text string, a location
     * in the Annotation Space, and an attach point in Image Space. The hot
     * spot is the same as the location. By default the constructed
     * AttachedAnnotationString has an initial font of
     * <code>new Font(null, Font.PLAIN, 10)</code> and an initial alignment
     * of <code>ALIGN_TOP_LEFT</code>. 
     *
     * @param string Test string of this AnnotationString.
     * @param location Reference point in Annotation Space for positioning
     *	      this AnnotationString.
     * @param attachPoint Point in Image Space to attach this Annotation to.
     *
     * @throws NullPointerException if any one of <code>string</code>,
     *	       <code>location</code>, or <code>attachPoint</code> is
     *	       <code>null</code>.
     */
    public AttachedAnnotationString(String string, Point2D location,
				    Point2D attachPoint)
    {
	this(string, location, attachPoint, location);
    }

    /**
     * Constructs an AttachedAnnotationString given a text string, a location
     * in the Annotation Space, an attach point in Image Space, and a hot spot
     * in the Annotation Space. By default the constructed
     * AttachedAnnotationString has an initial font of
     * <code>new Font(null, Font.PLAIN, 10)</code> and an initial alignment
     * of <code>ALIGN_TOP_LEFT</code>. 
     *
     * @param string Test string of this AnnotationString.
     * @param location Reference point in Annotation Space for positioning
     *	      this AnnotationString.
     * @param attachPoint Point in Image Space to attach this Annotation to.
     * @param hotSpot Point in Annotation Space to be aligned to the attach
     *		      point.
     *
     * @throws NullPointerException if any one of <code>string</code>,
     *	       <code>location</code>, <code>attachPoint</code>, or
     *	       <code>hotSpot</code> is <code>null</code>.
     */
    public AttachedAnnotationString(String string, Point2D location,
				    Point2D attachPoint, Point2D hotSpot)
    {
	super(string, location);
	setAttachPoint(attachPoint);
	_setHotSpot(hotSpot);
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Sets the attach point in Image Space.
     *
     * @param attachPoint Point in Image Space to attach to.
     *
     * @throws NullPointerException if <code>attachPoint</code>
     *	       is <code>null</code>.
     */
    public void setAttachPoint(Point2D attachPoint)
    {
	if (attachPoint == null) {
	    throw new NullPointerException("attachPoint can not be null.");
	}
	// Make a defensive copy
	_attachPoint = (Point2D)attachPoint.clone();
    }

    /**
     * Returns the attach point in Image Space.
     *
     * @return Point in Image Space to attach to.
     */
    public Point2D getAttachPoint()
    {
	// Make a defensive copy
	return (Point2D)_attachPoint.clone();
    }

    /**
     * Paints this Annotation. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.) All the <code>AffineTransform</code> passed
     * in <b>MUST</b> also be preserved, i.e., cloned copies should be used
     * if changes are to be made.
     *
     * @param g2d Graphics context to paint in.
     * @param imageTransform Image Transform, from Image Space to Viewport 
     *			     Space.
     * @param annotationTransform Annotation Transform, from Annotation Space 
     *			          to Viewport Space.
     * @param viewportTransform Viewport Transform, from Viewport Space to
     *				Component (Display) Space.
     */
    public void paint(Graphics2D g2d, AffineTransform imageTransform,
		      AffineTransform annotationTransform,
		      AffineTransform viewportTransform)
    {
	// Pre-concatenate VT to AT to form the transform from AS to DS
	AffineTransform at = (AffineTransform)annotationTransform.clone();
	at.preConcatenate(viewportTransform);

	// Pre-concatenate VT to IT to form the transform from IS to DS
	AffineTransform it = (AffineTransform)imageTransform.clone();
	it.preConcatenate(viewportTransform);

	// Transform the attach point and hot spot to Display Space
	Point2D attachPoint = it.transform(getAttachPoint(), null);
	Point2D hotSpot = at.transform(_getHotSpot(), null);

	// Construct the translation to move the hot spot to the attach point
	double dx = attachPoint.getX() - hotSpot.getX();
	double dy = attachPoint.getY() - hotSpot.getY();
	AffineTransform translate =
	    AffineTransform.getTranslateInstance(dx, dy);

	// Pre-concatenate translate to at to form the final transform
	at.preConcatenate(translate);

	// Find the anchor point in Annotation Space
	Rectangle2D bounds = _getOriginalBounds(g2d);
	Point2D anchor = _getAnchorPoint(bounds);

	// Find the background shift, i.e., the inverse mapping of (+1, +1)
	// from Display Space back to Annotation Space
	Point2D shift = new Point2D.Double(1.0 / at.getScaleX(),
					   1.0 / at.getScaleY());

	// Prepare the graphics context
	Map origSettings = _prepareGraphics2D(g2d);
	AffineTransform origTransform = g2d.getTransform();
	g2d.transform(at);

	// Draw the string's background
	String s = getString();
	Color c = getBackgroundColor();
	if (c != null) {
	    g2d.setColor(c);
	    g2d.drawString(s, (float)(anchor.getX() + shift.getX()), 
			   (float)(anchor.getY() + shift.getY()));

	    // Double outlining
	    if (isDoubleOutliningEnabled()) {
		g2d.drawString(s, (float)(anchor.getX() - shift.getX()), 
			       (float)(anchor.getY() - shift.getY()));
	    }
	}

	// Draw the string's foreground
	c = getForegroundColor();
	if (c != null) {
	    g2d.setColor(c);
	    g2d.drawString(s, (float)anchor.getX(), (float)anchor.getY());
	}

	// Restore the graphics context
	g2d.setTransform(origTransform);
	_restoreGraphics2D(g2d, origSettings);
    }

    /**
     * Calculates the bounding Rectangle that encloses this Annotation in 
     * Display (Component) Space. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.) All the <code>AffineTransform</code> passed
     * in <b>MUST</b> also be preserved, i.e., cloned copies should be used
     * if changes are to be made.
     *
     * @param g2d Graphics context used to paint this Annotation.
     * @param imageTransform Image Transform, from Image Space to Viewport 
     *			     Space.
     * @param annotationTransform Annotation Transform, from Annotation Space 
     *			          to Viewport Space.
     * @param viewportTransform Viewport Transform, from Viewport Space to
     *				Component (Display) Space.
     *
     * @return Smallest Rectangle in Display Space which bounds this
     *	       Annotation; <code>null</code> if such a Rectangle does not
     *	       exist.
     */
    public Rectangle getBounds(Graphics2D g2d,
			       AffineTransform imageTransform,
			       AffineTransform annotationTransform,
			       AffineTransform viewportTransform)
    {
	// Pre-concatenate VT to AT to form the transform from AS to DS
	AffineTransform at = (AffineTransform)annotationTransform.clone();
	at.preConcatenate(viewportTransform);

	// Pre-concatenate VT to IT to form the transform from IS to DS
	AffineTransform it = (AffineTransform)imageTransform.clone();
	it.preConcatenate(viewportTransform);

	// Transform the attach point and hot spot to Display Space
	Point2D attachPoint = it.transform(getAttachPoint(), null);
	Point2D hotSpot = at.transform(_getHotSpot(), null);

	// Construct the translation to move the hot spot to the attach point
	double dx = attachPoint.getX() - hotSpot.getX();
	double dy = attachPoint.getY() - hotSpot.getY();
	AffineTransform translate =
	    AffineTransform.getTranslateInstance(dx, dy);

	// Pre-concatenate translate to at to form the final transform
	at.preConcatenate(translate);

	// Find the anchor point for drawing the string in Annotation Space
	Rectangle2D bounds = _getOriginalBounds(g2d);
	Point2D anchor = _getAnchorPoint(bounds);

	// Append a translation to the anchor point to the transform
	at.translate(anchor.getX(), anchor.getY());

	// Transform the original bounds and return the new bounds
	return at.createTransformedShape(bounds).getBounds();
    }

    // ---------------
    // Private methods
    // ---------------

    /**
     * Sets the hot spot in Annotation Space.
     *
     * @param Hot spot in Annotation Space.
     *
     * @throws NullPointerException if <code>hotSpoit</code> is
     *	       <code>null</code>.
     */
    private void _setHotSpot(Point2D hotSpot)
    {
	if (hotSpot == null) {
	    throw new NullPointerException("hotSpot can not be null.");
	}
	_hotSpot = hotSpot;
    }

    /**
     * Returns the hot spot in Annotation Space. The returned value is a
     * reference to the internal instance.
     *
     * @return Hot spot in Annotation Space.
     */
    private Point2D _getHotSpot()
    {
	return _hotSpot;
    }
}
