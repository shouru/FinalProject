/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.annotation;

import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * AnnotationImage which is defined entirely in Annotation Space and subject
 * to Annotation Transform. The net effect is a StaticAnnotationImage is
 * always positioned and sized relative to the Viewport's bounds.
 *
 * @see org.medtoolbox.jviewbox.viewport.Viewport for more details about
 *      coordinate spaces and transforms.
 *
 * @version January 8, 2004
 */
public class StaticAnnotationImage extends AnnotationImage
{
    // ------------
    // Constructors
    // ------------

    /**
     * Constructs a StaticAnnotationImage of the specified <code>Image</code>
     * at the specified location (i.e., where the upper-left corner of the
     * image goes) which is defined in the Annotation Space.
     *
     * @param image <code>Image</code> of this AnnotationImage.
     * @param location Location of this AnnotationImage (i.e., where the
     *		       upper-left corner of the image goes).
     */
    public StaticAnnotationImage(Image image, Point2D location)
    {
	super(image, location);
    }

    /**
     * Constructs a StaticAnnotationImage of the specified <code>Image</code>
     * at an initial location of (0,0).
     *
     * @param image <code>Image</code> of this AnnotationImage.
     */
    public StaticAnnotationImage(Image image)
    {
	this(image, new Point());
    }

    // -----------------
    // Protected Methods
    // -----------------

    /**
     * Returns the actual affine transform that should be applied to this
     * annotation's image before drawing it in the Component Space.
     *
     * @return <code>AffineTransform</code> to apply to this annotation's
     *	       image before drawing it in the Display Space.
     */
    protected AffineTransform
	_getTransform(AffineTransform imageTransform,
		      AffineTransform annotationTransform,
		      AffineTransform viewportTransform)
    {
	// Pre-concatenate VT to AT to form the transform from AS to DS
	AffineTransform xform = (AffineTransform)annotationTransform.clone();
	xform.preConcatenate(viewportTransform);

	// Translate the image to its location
	Point2D location = getLocation();
	xform.translate(location.getX(), location.getY());

	return xform;
    }
}
