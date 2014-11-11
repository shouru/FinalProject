/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.annotation;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

/**
 * AnnotationShape which is defined entirely in Image Space and subject to
 * Image Transform. The net effect is a DynamicAnnotationShape is always
 * positioned, sized, and oriented relative to the image.
 *
 * @see org.medtoolbox.jviewbox.viewport.Viewport for more details about
 *      coordinate spaces and transforms.
 *
 * @version January 8, 2004
 */
public class DynamicAnnotationShape extends AnnotationShape
{
    // --------------
    // Private fields
    // --------------

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
     * Constructs a DynamicAnnotationShape for the specified
     * <code>Shape</code>. A copy will be made if the specified
     * <code>Shape</code> is <code>Cloneable</code>. Modifying the
     * <code>Shape</code> instance after calling this constructor is
     * <b>STRONGLY DISCOURAGED</b> for it will produce unpredictable effect
     * on the constructed AnnotationShape.
     *
     * @param shape Shape managed by this Annotation.
     *
     * @throws NullPointerException if <code>shape</code> is <code>null</code>.
     */
    public DynamicAnnotationShape(Shape shape)
    {
	super(shape);
    }

    // -----------------
    // Protected methods
    // -----------------

    /**
     * Returns the Shape of this Annotation transformed into Viewport Space.
     * All the <code>AffineTransform</code>>s passed are preserved. This method
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
	AffineTransform xform = imageTransform;

	// Check if the cache is out of date
	if (_lastTransformedShape == null || _lastShapeToTransform != _shape ||
	    _lastUsedTransform == null || !_lastUsedTransform.equals(xform)) {

	    // Create a new transformed shape
	    _lastTransformedShape = xform.createTransformedShape(_shape);
	    // xform needs to be cloned before caching it
	    _lastUsedTransform = (AffineTransform)xform.clone();
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
}
