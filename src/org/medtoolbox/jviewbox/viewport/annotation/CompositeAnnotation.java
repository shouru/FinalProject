/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.annotation;

import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Annotation which is a composite of zero or more annotations.
 *
 * @version January 8, 2004
 */
public class CompositeAnnotation extends Annotation
{
    // ----------------
    // Protected fields
    // ----------------

    /** Annotations which are part of this Annotation. */
    protected final Vector _childAnnotations = new Vector(1, 1);

    // -----------
    // Constructor
    // -----------

    /** Constructs a CompositeAnnotation which is initially empty. */
    public CompositeAnnotation()
    {
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Adds a child Annotation to this CompositeAnnotation.
     *
     * @param annotation Annotation to be made a part of this Annotation.
     *
     * @throws NullPointerException if <code>annotation</code> is
     *	       <code>null</code>.
     */
    public void addAnnotation(Annotation annotation)
    {
	if (annotation == null) {
	    throw new NullPointerException("annotation can not be null.");
	}
	_childAnnotations.add(annotation);
    }

    /**
     * Removes a child Annotation from this CompositeAnnotation.
     */
    public void removeAnnotation(Annotation annotation)
    {
	_childAnnotations.remove(annotation);
    }

    /**
     * Returns an unmodifiable <code>List</code> of the child Annotations of
     * this CompositeAnnotation.
     *
     * @return All the annotations which are part of this CompositeAnnotation
     *	       in an unmodifiable <code>List</code>.
     */
    public List getAnnotations()
    {
	return Collections.unmodifiableList(_childAnnotations);
    }

    /**
     * Sets the foreground color of this Annotation. This applies to all child
     * annotations recursively.
     *
     * @param color Foreground color that is used by this Annotation.
     */
    public void setForegroundColor(Color color)
    {
	super.setForegroundColor(color);

	// Set the foreground color of all the child Annotations
	for (Iterator it = getAnnotations().iterator(); it.hasNext(); ) {
	    ((Annotation)it.next()).setForegroundColor(color);
	}
    }

    /**
     * Sets the background color of this Annotation. This applies to all child
     * annotations recursively.
     *
     * @param color Background color that is used by this Annotation.
     */
    public void setBackgroundColor(Color color)
    {
	super.setBackgroundColor(color);

	// Set the background color of all the child Annotations
	for (Iterator it = getAnnotations().iterator(); it.hasNext(); ) {
	    ((Annotation)it.next()).setBackgroundColor(color);
	}
    }

    /**
     * Paints this Annotation. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.) All the <code>AffineTransform</code> passed
     * in <b>MUST</b> also be preserved, i.e., cloned copies should be used
     * if changes are to be made.
     * <p>
     * Child Annotations of a CompositeAnnotation are painted in the order
     * they are added, or equivalently, in the order they appear in the list
     * returned by <code>getAnnotations</code>.
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
	for (Iterator it = getAnnotations().iterator(); it.hasNext(); ) {
	    Annotation ann = (Annotation)it.next();
	    if (ann.isEnabled()) {
		ann.paint(g2d, imageTransform, annotationTransform,
			  viewportTransform);
	    }
	}
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
     * <p>
     * The bounding rectangle of a CompositeAnnotation is defined as the 
     * bounding rectangle of the union of all child Annotation's bounding
     * rectangles.
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
    public Rectangle getBounds(Graphics2D g2d, AffineTransform imageTransform,
			       AffineTransform annotationTransform,
			       AffineTransform viewportTransform)
    {
	Rectangle bounds = null;

	// Get initial bounds from the first child
	Iterator it = getAnnotations().iterator();
	if (it.hasNext()) {
	    bounds = ((Annotation)it.next()).getBounds(g2d, imageTransform,
						       annotationTransform,
						       viewportTransform);
	}

	// Bounds of first child not available?
	if (bounds == null) {
	    return null;
	}

	// Build the bounding Rectangle by combining Annotation Rectangles
	while (it.hasNext()) {
	    Annotation a = (Annotation)it.next();

	    Rectangle r = a.getBounds(g2d, imageTransform,
				      annotationTransform, viewportTransform);

	    // Bounds of some child not available
	    if (r == null) {
		return null;
	    }

	    bounds.add(r);
	}

	return bounds;
    }

    /**
     * Returns the square of the distance from a point to this Annotation in
     * Display (Component) Space. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.) All the <code>AffineTransform</code> passed
     * in <b>MUST</b> also be preserved, i.e., cloned copies should be used
     * if changes are to be made.
     * <p>
     * The square distance from a point to a CompositeAnnotation is defined as
     * the minimum of the square distances from the point to all child
     * Annotations.
     *
     * @param point Point2D in Display Space from which to determine the
     *              square distance to the Annotation in Display Space.
     * @param g2d Graphics context used to paint this Annotation.
     * @param imageTransform Image Transform, from Image Space to Viewport 
     *			     Space.
     * @param annotationTransform Annotation Transform, from Annotation Space 
     *			          to Viewport Space.
     * @param viewportTransform Viewport Transform, from Viewport Space to
     *				Component (Display) Space.
     *
     * @return Distance squared from the point to this Annotation in Display
     *         Space.
     */
    public double getDistanceSquared(Point2D point, Graphics2D g2d,
				     AffineTransform imageTransform,
				     AffineTransform annotationTransform,
				     AffineTransform viewportTransform)
    {
	double distSq = Double.POSITIVE_INFINITY;

	// Test all the Annotations for a closer distance squared
	for (Iterator it = getAnnotations().iterator(); it.hasNext(); ) {
	    Annotation a = (Annotation)it.next();
	    double sq = a.getDistanceSquared(point, g2d, imageTransform,
					     annotationTransform,
					     viewportTransform);
	    distSq = Math.min(distSq, sq);
	}

	return distSq;
    }
}
