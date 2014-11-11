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
 * AnnotationImage whose bounds is defined in Annotation Space while an
 * additional translation makes it appear attached to a point in Image Space,
 * called the <i>attach point</i>. More precisely, the attach point and
 * another point in Annotation Space, called <i>hot spot</i>, are transformed
 * into Viewport Space using Image and Annotation Transform respectively. An
 * additional translation which brings the hot spot to the attach point in
 * Viewport Space is then applied to this AttachedAnnotationImage after it is
 * first transformed in the same way as a {@link StaticAnnotationImage}.
 * The net effect is an AttachedAnnotationImage is sized and oriented
 * relative to the Viewport's bounds and positioned relative to the image.
 *
 * @see org.medtoolbox.jviewbox.viewport.Viewport for more details about
 *      coordinate spaces and transforms.
 *
 * @version January 8, 2004
 */
public class AttachedAnnotationImage extends AnnotationImage
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
     * Constructs an AttachedAnnotationImage of the specified
     * <code>Image</code>, attached to the specified point in Image Space. The
     * hot spot is set to <code>(0, 0)</code> in Annotation Space by default.
     *
     * @param image <code>Image</code> of this AnnotationImage.
     * @param location Location of this AnnotationImage (i.e., where the
     *		       upper-left corner of the image goes).
     * @param attachPoint Point to attach this Annotation to in Image Space.
     */
    public AttachedAnnotationImage(Image image, Point2D location,
				   Point2D attachPoint)
    {
	this(image, location, attachPoint, new Point());
    }

    /**
     * Constructs an AttachedAnnotationImage of the specified
     * <code>Image</code> with the specified hot spot attached to the
     * specified attach point.
     *
     * @param image <code>Image</code> of this AnnotationImage.
     * @param location Location of this AnnotationImage (i.e., where the
     *		       upper-left corner of the image goes).
     * @param attachPoint Point in Image Space to attach this Annotation to.
     * @param hotSpot Point in Annotation Space to align to attach point.
     */
    public AttachedAnnotationImage(Image image, Point2D location,
				   Point2D attachPoint, Point2D hotSpot)
    {
	super(image, location);
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
	// Make a copy of AT for it will be modified
	AffineTransform at = (AffineTransform)annotationTransform.clone();
	AffineTransform it = imageTransform;

	// Transform the attach point and hot spot to Viewport Space
	Point2D attachPoint = it.transform(getAttachPoint(), null);
	Point2D hotSpot = at.transform(_getHotSpot(), null);

	// Find the translation that moves the hot spot to the attach point
	double dx = attachPoint.getX() - hotSpot.getX();
	double dy = attachPoint.getY() - hotSpot.getY();

	// Translate the image to its location
	Point2D location = getLocation();
	at.translate(location.getX(), location.getY());

	// Pre-concatenate the attaching translation to AT
	at.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));

	// Pre-concatenate VT to AT to form the final transform
	at.preConcatenate(viewportTransform);

	return at;
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
