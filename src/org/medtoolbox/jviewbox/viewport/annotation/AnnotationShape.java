/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.annotation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Annotation which displays a <code>Shape</code> in a <code>Viewport</code>.
 * <p>
 * This abstract base class provides default implementation for all abstract
 * methods in <code>Annotation</code>. An implementing subclass only needs to
 * provide implementation for <code>_getTransformedShape</code> for the class
 * to work. The behavior of a concrete AnnotationShape subclass with respect
 * to the various coordinate spaces and transformations is also defined by
 * <code>_getTransformedShape</code>.
 *
 * @see Annotation
 *
 * @version January 8, 2004
 */
//
// Implementation Notes
//
// The strategy for all the shape annotation classes is to apply any affine
// transform on the shape directly. Hence drawing is carried out in the natural
// Graphics2D space, or say Display (Component) Space. 
//
// Stroke is used to create outline effect. Since drawing is in Display Space,
// a fixed width stroke produces the same visual effect across all Viewports.
// Stroke is subject to Graphics2D transformation and hence is difficult to
// control if carried out in arbitrary user space.
//
public abstract class AnnotationShape extends Annotation
{
    // ----------------
    // Protected fields
    // ----------------

    /**
     * Key for saving the antialiasing rendering hint setting of a Graphics2D
     * in a Map.
     */
    protected static final Object _KEY_ANTIALIASING =
	RenderingHints.KEY_ANTIALIASING;

    /** Shape managed by this Annotation. */
    protected Shape _shape;

    // --------------
    // Private fields
    // --------------

    /** Whether antialiasing rendering is enabled. */
    private static boolean _antialiasing = false;

    /** Stroke used to create background outline. */
    private static Stroke _stroke = new BasicStroke(3.0f);

    /** Whether the interior of the shape is filled. */
    private boolean _isFilled = false;

    /** Paint used to fill the interior of the shape. */
    private Paint _interiorPaint;

    /** Stroke last used in <code>_getStrokedShape</code>. */
    private Stroke _lastUsedStroke;

    /** Shape last passed to <code>_getStrokedShape</code>. */
    private Shape _lastShapeToStroke;

    /** Shape last returned by <code>_getStrokedShape</code>. */
    private Shape _lastStrokedShape;

    // ------------
    // Constructors
    // ------------

    /**
     * Constructs an AnnotationShape for the specified <code>Shape</code>.
     * A copy will be made if the specified <code>Shape</code> is
     * <code>Cloneable</code>. Modifying the <code>Shape</code> instance after
     * calling this constructor is <b>STRONGLY DISCOURAGED</b> for it will
     * produce unpredictable effect on the constructed AnnotationShape.
     *
     * @param shape Shape managed by this Annotation.
     *
     * @throws NullPointerException if <code>shape</code> is <code>null</code>.
     */
    protected AnnotationShape(Shape shape)
    {
	if (shape == null) {
	    throw new NullPointerException("shape can not be null.");
	}

	// Make a defensive copy if possible
	_shape = (Shape)_clone(shape);
	if (_shape == null) {
	    _shape = shape;
	}
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Enables/disables antialiasing rendering. This setting applies to
     * <b>ALL</b> AnnotationShapes. By default antialiasing is disabled.
     *
     * @param enabled <code>true</code> to enable and <code>false</code> to
     *		      disable antialiasing rendering of all AnnotationShapes.
     */
    public static void setAntialiasingEnabled(boolean enabled)
    {
	_antialiasing = enabled;
    }

    /**
     * Returns whether antialiasing rendering is enabled. This setting applies
     * to <b>ALL</b> AnnotationShapes.
     *
     * @return <code>true</code> if antialiasing rendering is enabled;
     *	       <code>false</code> if disabled.
     */
    public static boolean isAntialiasingEnabled()
    {
	return _antialiasing;
    }

    /**
     * Sets the stroke used to create background outlines. This setting
     * applies to <b>ALL</b> AnnotationShapes. By default it is
     * <code>BasicStroke(3.0f)</code>.
     *
     * @param stroke Stroke used to create background outline for <b>ALL</b>
     *		     AnnotationShapes.
     *
     * @throws NullPointerException if <code>stroke</code> is
     *	       <code>null</code>.
     */
    public static void setOutliningStroke(Stroke stroke)
    {
	if (stroke == null) {
	    throw new NullPointerException("stroke can not be null.");
	}
	_stroke = stroke;
    }

    /**
     * Returns the stroke used to create background outlines. This setting
     * applies to <b>ALL</b> AnnotationShapes.
     *
     * @return Stroke used to create background outline for <b>ALL</b>
     *	       AnnotationShapes.
     */
    public static Stroke getOutliningStroke()
    {
	return _stroke;
    }

    /**
     * Sets the Shape that is managed by the Annotation. A copy will be made
     * if the specified <code>Shape</code> is <code>Cloneable</code> and has
     * an accessible <code>clone</code> method, which is not always true.
     * Modifying the <code>Shape</code> instance after calling this method is
     * <b>STRONGLY DISCOURAGED</b> for it will have unpredictable effect on
     * this AnnotationShape.
     *
     * @param shape Shape managed by the Annotation.
     *
     * @throws NullPointerException if <code>shape</code> is <code>null</code>.
     */
    public void setShape(Shape shape)
    {
	if (shape == null) {
	    throw new NullPointerException("shape can not be null.");
	}

	// Make a defensive copy if possible
	_shape = (Shape)_clone(shape);
	if (_shape == null) {
	    _shape = shape;
	}

	// Clear cache
	_clearStrokedShapeCache();
	_clearTransformedShapeCache();
    }

    /**
     * Returns the Shape that is managed by the Annotation. The returned value
     * may be a reference to either the internal instance or a copy. The user
     * should <b>NOT</b> try to modify the returned <code>Shape</code> for
     * it will produce unpredictable results.
     *
     * @return Shape managed by the Annotation.
     */
    public Shape getShape()
    {
	// Make a defensive copy if possible
	Shape shape = (Shape)_clone(_shape);
	return (shape != null ? shape : _shape);
    }

    /**
     * Sets whether the interior of the Shape is filled. If set, the interior
     * is filled with the <code>Paint</code> specified by
     * {@link #setInteriorPaint} in this class, or the foreground color if a
     * <code>Paint</code> is not available, i.e., set to <code>null</code>.
     * By default an AnnotationShape is not filled.
     *
     * @param isFilled <code>true</code> to fill the interior of the Shape;
     *		       <code>false</code> to not fill.
     *
     * @see #isFilled
     * @see #setInteriorPaint
     */
    public void setFilled(boolean isFilled)
    {
	_isFilled = isFilled;
    }

    /**
     * Returns whether the interior of the Shape is to be filled.
     *
     * @return <code>true</code> if the interior of the Shape is to be filled;
     *	       <code>false</code> if not.
     *
     * @see #setFilled
     */
    public boolean isFilled()
    {
	return _isFilled;
    }

    /**
     * Sets the <code>Paint</code> used to fill the interior of the Shape. The
     * default is <code>null</code>, in which case the foreground color is used
     * to fill. Note that any <code>Color</code> may be used as a
     * <code>Paint</code>.
     *
     * @param paint <code>Paint</code> used to fill the interior of the Shape;
     *		    <code>null</code> to use the Annotation's foreground color.
     *
     * @see #getInteriorPaint
     */
    public void setInteriorPaint(Paint paint)
    {
	_interiorPaint = paint;
    }

    /**
     * Returns the <code>Paint</code> used to fill the interior of the Shape.
     *
     * @return <code>Paint</code> used to fill the interior of the Shape;
     *	       <code>null</code> if the foreground color is used.
     *
     * @see #setInteriorPaint
     */
    public Paint getInteriorPaint()
    {
	return _interiorPaint;
    }

    /**
     * Paints this Annotation.
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
	// Get the Shape in Viewport Space
	Shape shape = _getTransformedShape(imageTransform,annotationTransform);

	// Prepare the graphics context
	Map origSettings = _prepareGraphics2D(g2d);

	// Apply viewport transform to g2d
	AffineTransform origXform = g2d.getTransform();
	g2d.transform(viewportTransform);

	// Draw the Shape's background
	Color c = getBackgroundColor();
	if (c != null) {
	    g2d.setColor(c);
	    g2d.draw(_getStrokedShape(shape));
	}

	// Draw the Shape's foreground
	c = getForegroundColor();
	if (c != null) {
	    g2d.setColor(c);
	    g2d.draw(shape);
	}

	// Fill the Shape's interior
	if (isFilled()) {
	    // Apply the interior paint if given
	    Paint paint = getInteriorPaint();
	    if (paint != null) {
		g2d.setPaint(paint);
	    }
	    // Otherwise, use foreground color, which is already in g2d

	    g2d.fill(shape);
	}

	// Restore g2d's transform
	g2d.setTransform(origXform);

	// Restore the graphics context
	_restoreGraphics2D(g2d, origSettings);
    }

    /**
     * Calculates the bounding Rectangle that encloses this Annotation in 
     * Display (Component) Space.
     *
     * @param g2d Graphics context used to paint this Annotation. This is not
     *		  used at all and may simply be <code>null</code>.
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
	// Get the Shape in Display Space
	Shape shape = _getTransformedShape(imageTransform,annotationTransform);
	shape = viewportTransform.createTransformedShape(shape);

	return shape.getBounds();
    }

    /**
     * Returns the square of the distance from a point to this Annotation in
     * Display (Component) Space.
     * <p>
     * This default implementation first tests whether the point falls inside
     * the shape using <code>Shape.contains</code> if {@link #setFilled} is
     * on and returns 0.0 if the point is inside the shape. Otherwise, it
     * approximates the Shape as a collection of line segments, and then
     * calculates the square distance from the point to each of the line
     * segments and returns the minimum.
     *
     * @param point Point2D in Display Space from which to determine the
     *              square distance to the Annotation in Display Space.
     * @param g2d Graphics context used to paint this Annotation. This is not
     *		  used at all and may simply be <code>null</code>.
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
	// Get the Shape in Display Space
	Shape shape = _getTransformedShape(imageTransform,annotationTransform);
	shape = viewportTransform.createTransformedShape(shape);

	return _getDistanceSquared(point, shape, isFilled());
    }

    /**
     * Returns the square of the distance from a point to this Annotation in
     * Display (Component) Space.
     * <p>
     * The only difference between this method and {@link #getDistanceSquared}
     * is this method never checks if the point is inside the shape and always
     * returns the minimum square distance to the edges.
     *
     * @param point Point2D in Display Space from which to determine the
     *              square distance to the Annotation in Display Space.
     * @param imageTransform Image Transform, from Image Space to Viewport 
     *			     Space.
     * @param annotationTransform Annotation Transform, from Annotation Space 
     *			          to Viewport Space.
     * @param viewportTransform Viewport Transform, from Viewport Space to
     *				Component (Display) Space.
     *
     * @return Distance squared from the point to the edges of this Annotation
     *	       in Display Space.
     */
    public double
	getDistanceToEdgesSquared(Point2D point,
				  AffineTransform imageTransform,
				  AffineTransform annotationTransform,
				  AffineTransform viewportTransform)
    {
	// Get the Shape in Display Space
	Shape shape = _getTransformedShape(imageTransform,annotationTransform);
	shape = viewportTransform.createTransformedShape(shape);

	return _getDistanceSquared(point, shape, false);
    }

    // -----------------
    // Protected methods
    // -----------------

    /**
     * Returns the Shape of this Annotation transformed into Viewport Space.
     * All the <code>AffineTransform</code>s passed in <b>MUST</b> be
     * preserved, i.e., cloned copies should be used if changes are to be made.
     * <p>
     * Implementation of this method is encouraged to cache the result for
     * performace reason.
     * <p>
     * <b>Note</b> that many of the AWT classes implementing the
     * <code>Shape</code> interface are <i>mutable</i>, i.e., their content
     * may be modified after instance creation. Furthermore, some of them do
     * not override <code>equal</code> for testing equivalency. In this case,
     * it is impossible to reliably determine if two <code>Shape</code>
     * references refers to the same geometric shape. Even if they refers to
     * the same instance, the instance may have mutated in the time between
     * when the first and the second reference is acquired. This creates a
     * very difficult situation for caching because there is no reliable way
     * to determine if the Shape of this Annotation has been updated.
     * <p>
     * The default implementation of constructor, <code>getShape</code> and
     * <code>setShape</code> does its best to make defensive copies to shield
     * other parts of this class from mutable Shape instance. However, this
     * is not always possible for a class implementing the Shape interface
     * does not have to be <code>Cloneable</code>. Therefore, the user of
     * this class is required <b>NEVER</b> to modify Shape instances after
     * they are passed to this class.
     * <p>
     * Also, for proper caching, the implementation of this method
     * <b>MUST</b> call {@link #_clearStrokedShapeCache} if it is going to
     * return a mutated instance rather than a brand new instance of
     * <code>Shape</code>. The reason is the internal cache mechanism assumes
     * immutable <code>Shape</code> instance and may fail to update its cache
     * if a mutated instance is seen the second time.
     *
     * @param imageTransform Image Transform, from Image Space to Viewport 
     *			     Space.
     * @param annotationTransform Annotation Transform, from Annotation Space 
     *			          to Viewport Space.
     *
     * @return Shape of this Annotation transformed into Viewport Space.
     *
     * @see #_clearTransformedShapeCache
     * @see #setShape
     * @see #getShape
     * @see #_getStrokedShape
     * @see #_clearStrokedShapeCache
     */
    protected abstract Shape
	_getTransformedShape(AffineTransform imageTransform,
			     AffineTransform annotationTransform);

    /**
     * Clears the cache used by <code>_getTransformedShape</code>. This method
     * is called when <code>setShape</code> or other methods make any change
     * to the internal <code>Shape</code> instance <code>_shape</code> to
     * explicily flush the cache used by <code>_getTransformedShape</code>.
     */
    protected abstract void _clearTransformedShapeCache();

    /**
     * Prepares a <code>Graphics2D</code> graphics context for drawing this
     * AnnotationShape according to the settings of this Annotation.
     * <p>
     * Namely, the antialiasing <code>RenderingHint</code> will be saved and
     * set to that of this AnnotationShape, in addition to settings that are
     * saved and set by the super class' version
     * {@link Annotation#_prepareGraphics2D}.
     *
     * @param g2d <code>Graphics2D</code> to prepare.
     *
     * @return Original <code>Graphics2D</code> settings stored in a
     *	       <code>Map</code>.
     */
    protected Map _prepareGraphics2D(Graphics2D g2d)
    {
	// Call super
	Map origSettings = super._prepareGraphics2D(g2d);

	// Save and set antialiasing rendering hint
	Object origAntialiasing =
	    g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
	if (isAntialiasingEnabled() ^ 
	    (origAntialiasing == RenderingHints.VALUE_ANTIALIAS_ON)) {
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				 isAntialiasingEnabled() ?
				 RenderingHints.VALUE_ANTIALIAS_ON :
				 RenderingHints.VALUE_ANTIALIAS_OFF);
	    origSettings.put(_KEY_ANTIALIASING, origAntialiasing);
	}

	return origSettings;
    }

    /**
     * Restores a <code>Graphics2D</code> graphics context after drawing is
     * completed according to the original settings saved in the
     * <code>Map</code>.
     *
     * @param g2d <code>Graphics2D</code> to restore.
     * @param origSettings Original <code>Graphics2D</code> settings stored in
     *			   a <code>Map</code>.
     */
    protected void _restoreGraphics2D(Graphics2D g2d, Map origSettings)
    {
	// Call super
	super._restoreGraphics2D(g2d, origSettings);

	// Restore antialiasing rendering hint
	Object origAntialiasing = origSettings.get(_KEY_ANTIALIASING);
	if (origAntialiasing != null) {
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				 origAntialiasing); 
	}
    }

    /**
     * Returns a outline <code>Shape</code> of the specified <code>Shape</code>
     * using the current <code>Stroke</code> setting.
     * <p>
     * This default implementation caches the result. However, it assumes the
     * <code>shape</code> passed in does not change if it refers to the same
     * instance as the last time this method was called. This is unfortunately
     * untrue because many of the subclasses implementing <code>Shape</code>
     * are not immutable.
     * <p>
     * Therefore, the implementation of <code>_getTransformedShape</code>
     * <b>MUST</b> explicitly call <code>_clearStrokedShapeCache</code> if it
     * is going to return a mutated instance rather than a brand new one
     * of <code>Shape</code> for the caching to work properly.
     *
     * @param shape <code>Shape</code> to create an outline <code>Shape</code>.
     *
     * @return Outline <code>Shape</code> of <code>shape</code>.
     *
     * @throws NullPointerException if <code>shape</code> is <code>null</code>.
     *
     * @see #_getTransformedShape
     * @see #_clearStrokedShapeCache
     */
    protected Shape _getStrokedShape(Shape shape)
    {
	if (shape == null) {
	    throw new NullPointerException("shape can not be null.");
	}

	// Check if cache is out of date
	Stroke stroke = getOutliningStroke();
	if (stroke != _lastUsedStroke || shape != _lastShapeToStroke) {
	    // Create a new stroked shape
	    _lastStrokedShape = stroke.createStrokedShape(shape);
	    _lastShapeToStroke = shape;
	    _lastUsedStroke = stroke;
	}

	return _lastStrokedShape;
    }

    /**
     * Clears the cache used by <code>_getStrokedShape</code>. This method
     * <b>must</b> be called if <code>_getTransformedShape</code> is going to
     * return a mutated instance rather than a brand new one of
     * <code>Shape</code>
     *
     * @see #_getStrokedShape
     */
    protected void _clearStrokedShapeCache()
    {
	_lastShapeToStroke = null;
    }

    // ---------------
    // Private methods
    // ---------------

    /** A reflection "cloner." Returns <code>null</code> if cloning failed. */
    private static Object _clone(Object o)
    {
	if (o == null) {
	    throw new NullPointerException("o can not be null.");
	}

	// Must be cloneable
	if (!(o instanceof Cloneable)) {
	    return null;
	}

	try {
	    // Get o.clone()
	    Method clone = o.getClass().getMethod("clone", new Class[0]);

	    // Invoke o.clone()
	    return clone.invoke(o, new Object[0]);
	}
	catch (NoSuchMethodException e) {
	    // clone() is not found?
	}
	catch (SecurityException e) {
	    // No reflection digging is allowed
	}
	catch (IllegalAccessException e) {
	    // o's class or o.clone() is not accessible
	}
	catch (InvocationTargetException e) {
	    // o.clone() throws an exception
	    // throw e;
	}
	catch (IllegalArgumentException e) {
	    // Arguments do not match? Should never happen
	    //throw e;
	    e.printStackTrace();
	}

	return null;
    }

    /**
     * Calculates the squared distance from a point to a shape, optionally
     * checking if the point falls inside the shape.
     */
    private static double _getDistanceSquared(Point2D point, Shape shape,
					      boolean checkInsideness)
    {
	// Check whether the point is inside the shape if requested to do so
	if (checkInsideness && shape.contains(point)) {
	    return 0.0;
	}

	// Approximate the Shape with a series of segments
	double distSq = Double.POSITIVE_INFINITY;
	Point2D lastPoint = null;
	Point2D startPoint = null;
	Line2D line = null;
	for (PathIterator path = shape.getPathIterator(null, 3.0);
	     !path.isDone(); path.next()) {

	    // Get the next vertex type and coordinates
	    double[] coords = new double[6];
	    int type = path.currentSegment(coords);

	    // Find the next line segment as defined by the PathIterator
	    switch (type) {

	    case PathIterator.SEG_MOVETO:
		// Starting location for a new subpath
		startPoint = new Point2D.Double(coords[0], coords[1]);
		lastPoint = startPoint;
		line = null;
		break;

	    case PathIterator.SEG_LINETO:
		// Draw line between last location and this one
		Point2D thisPoint = new Point2D.Double(coords[0], coords[1]);
		line = new Line2D.Float(lastPoint, thisPoint);
		lastPoint = thisPoint;
		break;

	    case PathIterator.SEG_CLOSE:
		// Draw line between last location and the starting location
		line = new Line2D.Double(lastPoint, startPoint);
		break;
	    }

	    // Update the minimum square distance
	    if (line != null) {
		distSq = Math.min(distSq, line.ptSegDistSq(point));
	    }
	}

	return distSq;
    }
}
