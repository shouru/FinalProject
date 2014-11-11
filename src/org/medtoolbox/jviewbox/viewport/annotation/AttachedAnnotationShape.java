/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.annotation;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * AnnotationShape whose shape is defined in Annotation Space while an
 * additional translation makes it appear attached to a point in Image Space,
 * called the <i>attach point</i>. More precisely, the attach point and
 * another point in Annotation Space, called <i>hot spot</i>, are transformed
 * into Viewport Space using Image and Annotation Transform respectively. An
 * additional translation which brings the hot spot to the attach point in
 * Viewport Space is then applied to this AttachedAnnotationShape after it is
 * first transformed in the same way as a <code>StaticAnnotationShape</code>.
 * The net effect is an AttachedAnnotationShape is sized and oriented
 * relative to the Viewport's bounds and positioned relative to the image.
 *
 * @see org.medtoolbox.jviewbox.viewport.Viewport for more details about
 *      coordinate spaces and transforms.
 *
 * @version January 8, 2004
 */
public class AttachedAnnotationShape extends AnnotationShape
{
    // --------------
    // Private fields
    // --------------

    /** Point in Image Space to attach this Annotation to. */
    private Point2D _attachPoint;

    /** Point in Annotation Space which is attached to the Attach Point. */
    private Point2D _hotSpot;
    
    /** AffineTransform last used in <code>_getTransformedShape</code>. */
    private AffineTransform _lastUsedTransform;

    /** Shape last returned by <code>_getTransformedShape</code>. */
    private Shape _lastTransformedShape;

    /** Shape last passed to <code>_getTransformedShape</code>. */
    private Shape _lastShapeToTransform;

    // ------------
    // Constructors
    // ------------

    /**
     * Constructs an AttachedAnnotationShape for the specified
     * <code>Shape</code>, attached to the specified point in Image Space. The
     * hot spot is set to <code>(0, 0)</code> in Annotation Space by default. A
     * copy will be made if the specified <code>Shape</code> is
     * <code>Cloneable</code>. Modifying the <code>Shape</code> instance after
     * calling this constructor is <b>STRONGLY DISCOURAGED</b> for it will
     * produce unpredictable effect on the constructed AnnotationShape.
     *
     * @param shape Shape managed by this Annotation.
     * @param attachPoint Point to attach this Annotation to in Image Space.
     *
     * @throws NullPointerException if either <code>shape</code> or
     *	       <code>attachPoint</code> is <code>null</code>.
     */
    public AttachedAnnotationShape(Shape shape, Point2D attachPoint)
    {
	this(shape, attachPoint, new Point());
    }

    /**
     * Constructs an AttachedAnnotationShape for the specified
     * <code>Shape</code> with the specified hot spot attached to the
     * specified attach point. A copy will be made if the specified
     * <code>Shape</code> is <code>Cloneable</code>. Modifying the
     * <code>Shape</code> instance after calling this constructor is
     * <b>STRONGLY DISCOURAGED</b> for it will produce unpredictable effect on
     * the constructed AnnotationShape.
     *
     * @param shape Shape managed by this Annotation.
     * @param attachPoint Point in Image Space to attach this Annotation to.
     * @param hotSpot Point in Annotation Space to align to attach point.
     *
     * @throws NullPointerException if any one of <code>shape</code>,
     *	       <code>attachPoint</code>, or <code>hotSpot</code> is
     *	       <code>null</code>.
     */
    public AttachedAnnotationShape(Shape shape, Point2D attachPoint,
				   Point2D hotSpot)
    {
	super(shape);
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
    // Protected methods
    // -----------------

    /**
     * Returns the Shape of this Annotation transformed into Viewport Space.
     * All the <code>AffineTransform</code>s passed are preserved. This method
     * caches the result for performace reason.
     *
     * @param imageTransform Image Transform, from Image Space to Viewport 
     *			     Space.
     * @param annotationTransform Annotation Transform, from Annotation Space 
     *			          to Viewport Space.
     *
     * @return Shape of this Annotation transformed into Viewport Space.
     */
    protected Shape _getTransformedShape(AffineTransform imageTransform,
					 AffineTransform annotationTransform)
    {
	// Make a copy of AT for it will be modified
	AffineTransform at = (AffineTransform)annotationTransform.clone();
	AffineTransform it = imageTransform;

	// Transform the attach point and hot spot to Viewport Space
	Point2D attachPoint = it.transform(getAttachPoint(), null);
	Point2D hotSpot = at.transform(_getHotSpot(), null);

	// Construct the translation to move the hot spot to the attach point
	double dx = attachPoint.getX() - hotSpot.getX();
	double dy = attachPoint.getY() - hotSpot.getY();
	AffineTransform translate =
	    AffineTransform.getTranslateInstance(dx, dy);

	// Pre-concatenate translate to at to form the final transform
	at.preConcatenate(translate);

	// Check if the cache is out of date
	if (_lastTransformedShape == null || _lastShapeToTransform != _shape ||
	    _lastUsedTransform == null || !_lastUsedTransform.equals(at)) {

	    // Create a new transformed shape
	    _lastTransformedShape = at.createTransformedShape(_shape);
	    // at is already a local copy so it's safe to just cache it
	    _lastUsedTransform = at;
	    _lastShapeToTransform = _shape;
	}

	return _lastTransformedShape;
    }

    /**
     * Clears the cache used by <code>_getTransformedShape</code>. This method
     * is called when <code>setShape</code> or other methods make any change
     * to the internal <code>Shape</code> instance <code>_shape</code> to
     * explicily flush the cache used by <code>_getTransformedShape</code>.
     */
    protected void _clearTransformedShapeCache()
    {
	_lastUsedTransform = null;
	_lastTransformedShape = null;
	_lastShapeToTransform = null;
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
