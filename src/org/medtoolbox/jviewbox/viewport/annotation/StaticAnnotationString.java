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
 * AnnotationString which is defined entirely in Annotation Space. A
 * StaticAnnotationString is attached to a reference point, i.e.,
 * <i>location</i> in Annotation Space and subject to Annotation Transform.
 * The net effect is a StaticAnnotationString is always positioned and sized
 * relative to the Viewport's bounds.
 *
 * @see org.medtoolbox.jviewbox.viewport.Viewport for more details about
 *      coordinate spaces and transforms.
 *
 * @version January 8, 2004
 */
public class StaticAnnotationString extends AnnotationString
{
    // ------------
    // Constructors
    // ------------

    /**
     * Constructs a StaticAnnotationString given a text string and a location
     * in the Annotation Space. By default the constructed
     * StaticAnnotationString has an initial font of
     * <code>new Font(null, Font.PLAIN, 10)</code> and an initial alignment
     * of <code>ALIGN_TOP_LEFT</code>.
     *
     * @param string Text string of the AnnotationString.
     * @param location Reference point in Annotation Space for positioning
     *	      this AnnotationString.
     *
     * @throws NullPointerException if <code>string</code> or
     *	       <code>location</code> is <code>null</code>.
     */
    public StaticAnnotationString(String string, Point2D location)
    {
	super(string, location);
    }

    /**
     * Constructs a StaticAnnotationString given a text string, a location in
     * the Annotation Space, and a font. By default the constructed
     * StaticAnnotationString has an initial alignment of
     * <code>ALIGN_TOP_LEFT</code>.
     *
     * @param string Text string of the AnnotationString.
     * @param location Reference point in Annotation Space for positioning
     *	      this AnnotationString.
     * @param font Font used to display the text string.
     *
     * @throws NullPointerException if any of <code>string</code>,
     *	       <code>location</code>, or <code>font</code> is
     *	       <code>null</code>.
     */
    public StaticAnnotationString(String string, Point2D location, Font font)
    {
	super(string, location, font);
    }

    /**
     * Constructs a StaticAnnotationString given a text string, a location in
     * the Annotation Space, and an alignment mode. By default the constructed
     * StaticAnnotationString has an initial font of
     * <code>new Font(null, Font.PLAIN, 10)</code>.
     *
     * @param string Text string of the AnnotationString.
     * @param location Reference point in Annotation Space for positioning
     *	      this AnnotationString.
     * @param alignment Alignment mode of this AnnotationString.
     *
     * @throws NullPointerException if any of <code>string</code>,
     *	       <code>location</code>, or <code>alignment</code> is
     *	       <code>null</code>.
     */
    public StaticAnnotationString(String string, Point2D location,
				  AlignMode alignment)
    {
	super(string, location, alignment);
    }

    /**
     * Constructs an StaticAnnotationString given a text string, a location in
     * Annotation Space, a font, and an alignment mode.
     *
     * @param string Text string of the AnnotationString.
     * @param location Reference point in Annotation Space for positioning
     *	      this AnnotationString.
     * @param font Font used to display the text string.
     * @param alignment Alignment mode of this AnnotationString.
     *
     * @throws NullPointerException if any of <code>string</code>,
     *	       <code>location</code>, <code>font</code>, or
     *	       <code>alignment</code> is <code>null</code>.
     */
    public StaticAnnotationString(String string, Point2D location,
				  Font font, AlignMode alignment)
    {
	super(string, location, font, alignment);
    }

    // --------------
    // Public methods
    // --------------

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
	AffineTransform xform = (AffineTransform)annotationTransform.clone();
	xform.preConcatenate(viewportTransform);

	// Find the anchor point in Annotation Space
	Rectangle2D bounds = _getOriginalBounds(g2d);
	Point2D anchor = _getAnchorPoint(bounds);

	// Find the background shift, i.e., the inverse mapping of (+1, +1)
	// from Display Space back to Annotation Space
	Point2D shift = new Point2D.Double(1.0 / xform.getScaleX(),
					   1.0 / xform.getScaleY());

	// Prepare the graphics context
	Map origSettings = _prepareGraphics2D(g2d);
	AffineTransform origTransform = g2d.getTransform();
	g2d.transform(xform);

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
	AffineTransform xform = (AffineTransform)annotationTransform.clone();
	xform.preConcatenate(viewportTransform);

	// Find the anchor point for drawing the string in Annotation Space
	Rectangle2D bounds = _getOriginalBounds(g2d);
	Point2D anchor = _getAnchorPoint(bounds);

	// Append a translation to the anchor point to the transform
	xform.translate(anchor.getX(), anchor.getY());

	// Transform the original bounds and return the new bounds
	return xform.createTransformedShape(bounds).getBounds();
    }
}
