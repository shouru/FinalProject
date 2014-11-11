/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.annotation;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Map;

import org.medtoolbox.jviewbox.viewport.Viewport;

/**
 * Annotation which displays an <code>Image</code> in a <code>Viewport</code>.
 * Paired with appropriate <code>Composite</code>s, AnnotationImages can be
 * used to easily create semi-transparent image overlay effects.
 * <p>
 * This abstract base class provides default implementation for all abstract
 * methods in {@link Annotation}. An implementing subclass only needs to
 * provide an implementation for {@link #_getTransform} for the class
 * to work. The behavior of a concrete AnnotationImage subclass with respect
 * to the various coordinate spaces and transformations is also defined by
 * {@link #_getTransform}.
 * <p>
 * This class applies the same interpolation setting in {@link Viewport}
 * (as returned by {@link Viewport#getInterpolationMode}) when the annotation
 * image is transformed.
 *
 * @see Annotation
 *
 * @since 2.0b
 *
 * @version January 8, 2004
 */
public abstract class AnnotationImage extends Annotation
{
    // ---------
    // Constants
    // ---------

    /** 
     * Key for saving the interpolation rendering hint setting of a Graphics2D
     * in a Map.
     */
    protected static final Object _KEY_INTERPOLATION =
	RenderingHints.KEY_INTERPOLATION;

    // --------------
    // Private fields
    // --------------

    /** Image of this annotation. */
    private final Image _image;

    /** Location of this AnnotationImage. */
    private Point2D _location;

    // ------------
    // Constructors
    // ------------

    /**
     * Constructs an AnnotationImage of the specified <code>Image</code> at
     * the specified location (i.e., where the upper-left corner of the image
     * goes).
     *
     * @param image <code>Image</code> of this AnnotationImage.
     * @param location Location of this AnnotationImage (i.e., where the
     *		       upper-left corner of the image goes).
     */
    protected AnnotationImage(Image image, Point2D location)
    {
	if (image == null) {
	    throw new NullPointerException("image cannot be null.");
	}

	_image = image;
	_location = (Point2D)location.clone();
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Sets the location of this AnnotationImage (i.e., where the upper-left
     * corner of the image goes). The choice of the coordinate space in which
     * this reference point is defined is up to the implementing subclass.
     *
     * @param location Location of this AnnotationImage.
     *
     * @throws NullPointerException if <code>location</code>
     *	       is <code>null</code>.
     */
    public void setLocation(Point2D location)
    {
	if (location == null) {
	    throw new NullPointerException("location can not be null.");
	}
	// Make a defensive copy
	_location = (Point2D)location.clone();
    }

    /**
     * Returns the location of this AnnotationImage (i.e., where the
     * upper-left corner of the image goes). The choice of the coordinate
     * space in which this reference point is defined is up to the
     * implementing subclass.
     *
     * @return Location of this AnnotationString.
     */
    public Point2D getLocation()
    {
	// Make a defensive copy
	return (Point2D)_location.clone();
    }

    /**
     * Returns the image of this AnnotationImage.
     *
     * @return Image of this AnnotationImage.
     */
    public Image getImage()
    {
	return _image;
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
    public void paint(Graphics2D g2d,
		      AffineTransform imageTransform,
		      AffineTransform annotationTransform,
		      AffineTransform viewportTransform)
    {
	// Get the AffineTransform to apply to the image
	AffineTransform xform = _getTransform(imageTransform,
					      annotationTransform,
					      viewportTransform);

	// Prepare the graphics context
	Map origSettings = _prepareGraphics2D(g2d);

	// Draw the image
	g2d.drawImage(getImage(), xform, null);

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
	// Get the AffineTransform to apply to the image
	AffineTransform xform = _getTransform(imageTransform,
					      annotationTransform,
					      viewportTransform);

	// Compute the transformed image bounds
	Image image = getImage();
	Rectangle bounds = new Rectangle(image.getWidth(null),
					 image.getHeight(null));
	return xform.createTransformedShape(bounds).getBounds();
    }

    /**
     * Returns the square of the distance from a point to this Annotation in
     * Display (Component) Space.
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
	// Find out the transformed image bounds
	Rectangle bounds = getBounds(g2d, imageTransform, annotationTransform,
				     viewportTransform);

	// Point falls inside the bounds?
	if (bounds.contains(point)) {
	    return 0.0;
	}

	// Point falls outside the bounds
	else {
	    // Find out the shortest distance to any one side of the bounds
	    double x = point.getX();
	    double y = point.getY();
	    double x1 = bounds.getMinX();
	    double x2 = bounds.getMaxX();
	    double y1 = bounds.getMinY();
	    double y2 = bounds.getMaxY();
	    return
		Math.min(Math.min(Line2D.ptSegDistSq(x1, y1, x2, y1, x, y),
				  Line2D.ptSegDistSq(x1, y2, x2, y2, x, y)),
			 Math.min(Line2D.ptSegDistSq(x1, y1, x1, y2, x, y),
				  Line2D.ptSegDistSq(x2, y1, x2, y2, x, y)));
	}
    }

    // -----------------
    // Protected Methods
    // -----------------

    /**
     * Prepares a <code>Graphics2D</code> graphics context for drawing this
     * AnnotationImage according to the settings of this AnnotationImage.
     * <p>
     * Namely, in this method the interpolation <code>RenderingHints</code>
     * will be saved and set to that of {@link Viewport#getInterpolationMode},
     * in addition to all that are saved and set by the super class' version
     * {@link Annotation#_prepareGraphics2D}.
     *
     * @param g2d <code>Graphics2D</code> to prepare.
     *
     * @return Original <code>Graphics2D</code> settings stored in a
     *	       <code>Map</code>.
     *
     * @see #_restoreGraphics2D
     */
    protected Map _prepareGraphics2D(Graphics2D g2d)
    {
	// Call super
	Map origSettings = super._prepareGraphics2D(g2d);

	// Save and set interpolation rendering hint
	Object origInterpolation =
	    g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
	origSettings.put(_KEY_INTERPOLATION, origInterpolation);
	Viewport.InterpolationMode mode = Viewport.getInterpolationMode();
	if (mode == Viewport.INTERPOLATION_BILINEAR) {
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				 RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}
	else if (mode == Viewport.INTERPOLATION_BICUBIC) {
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				 RenderingHints.VALUE_INTERPOLATION_BICUBIC);
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

	// Restore interpolation rendering hint
	Object origInterpolation = origSettings.get(_KEY_INTERPOLATION);
	if (origInterpolation != null) {
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				 origInterpolation); 
	}
    }

    /**
     * Returns the actual affine transform that should be applied to this
     * annotation's image before drawing it in the Display Space. All the
     * <code>AffineTransform</code>s passed
     * in <b>MUST</b> also be preserved, i.e., cloned copies should be used
     * if changes are to be made.
     *
     * @return <code>AffineTransform</code> to apply to this annotation's
     *	       image before drawing it in the Component Space.
     */
    protected abstract AffineTransform
	_getTransform(AffineTransform imageTransform,
		      AffineTransform annotationTransform,
		      AffineTransform viewportTransform);
}
