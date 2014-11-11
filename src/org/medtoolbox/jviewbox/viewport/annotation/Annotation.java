/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.annotation;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for annotations. Annotations and their properties
 * under transformations are defined using 4 different coordinate spaces:
 * <pre>
 *
 *.   Image Space              Viewport Space           Annotation Space
 *.  =============             =============             =============
 *. |             |           |             |           |             |
 *. |             |           |             |           |             |
 *. |             |    -->    |             |   <--     |             |
 *. |             |   Image   |             | Annotation|             |
 *. |             | Transform |             | Transform |             |
 *. |             |           |             |           |             |
 *. |             |           |             |           |             |
 *.  =============             =============             =============
 *.				     |
 *.				     | Viewport Transform
 *.				     V
 *.			       =============
 *.			      |		    |
 *.			      |		    |
 *.			      |		    | Component
 *.			      |		    | (Display)
 *.			      |		    | Space
 *.			      |		    |
 *.			      |		    |
 *.			       =============
 * </pre>
 * Image Space is the coordinate space of an image. Annotations defined
 * in this space will be transformed along with the image when they are
 * displayed in Display Space. They are in a sense "stuck" to the image.
 * Annotation Space is a separate coordinate space only for Annotations.
 * Annotations defined in this space will be transformed under a
 * different AffineTransform than the transform from Image Space to
 * Display Space. They then behave differently in Display
 * Space than Annotations defined in Image Space.
 *
 * @see <a href='../Viewport.html#spaces'>more details about coordinate spaces
 *      and transforms.</a>
 *
 * @version January 8, 2004
 */
public abstract class Annotation
{
    // ----------------
    // Protected fields
    // ----------------

    /** Key for saving the Composite setting of a Graphics2D in a Map. */
    protected static final Object _KEY_COMPOSITE = Composite.class;

    /** Key for saving the Paint setting of a Graphics2D in a Map.*/
    protected static final Object _KEY_PAINT = Paint.class;

    // --------------
    // Private fields
    // --------------

    /** Foreground color of this Annotation. */
    private Color _foregroundColor = Color.white;

    /** Background color of this Annotation. */
    private Color _backgroundColor = Color.black;

    /** Composite to use when drawing this Annotation. */
    private Composite _composite;

    /** Whether this Annotation is enabled. */
    private boolean _enabled = true;

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs an Annotation. By default this Annotation will have a
     * foreground color of white and background color of black and is enabled.
     */
    public Annotation()
    {
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Sets the foreground color of this Annotation.
     *
     * @param color Foreground color that is used by this Annotation.
     *
     * @throws NullPointerException if <code>color</code> is <code>null</code>.
     */
    public void setForegroundColor(Color color)
    {
	if (color == null) {
	    throw new NullPointerException("color can not be null.");
	}
	_foregroundColor = color;
    }

    /**
     * Returns the foreground color of this Annotation.
     *
     * @return Foreground color that is used by this Annotation.
     */
    public Color getForegroundColor()
    {
	return _foregroundColor;
    }

    /**
     * Sets the background color of this Annotation. The background color may
     * be cleared by setting it to <code>null</code>, which is used to indicate
     * that the painting of the background should be disabled. The exact
     * behavior depends on the implementing subclass. An implemeting subclass
     * <b>MUST NOT</b> throw <code>NullPointerException</code> when the
     * background color is set to <code>null</code>.
     *
     * @param color Background color that is used by this Annotation.
     *
     * @see #getBackgroundColor
     */
    public void setBackgroundColor(Color color)
    {
	_backgroundColor = color;
    }

    /**
     * Returns the background color of this Annotation.
     *
     * @return Background color that is used by this Annotation; or
     *	       <code>null</code> if the background color setting is cleared.
     *
     * @see #setBackgroundColor
     */
    public Color getBackgroundColor()
    {
	return _backgroundColor;
    }

    /**
     * Sets the <code>Composite</code> to use in the <code>Graphics2D</code>
     * graphics context when drawing this Annotation. By default an Annotation
     * uses the default Composite in the graphics context.
     *
     * @param composite Composite to use when drawing this Annotation; or
     *			<code>null</code> to use the default Composite in the
     *			graphics context.
     */
    public void setComposite(Composite composite)
    {
	_composite = composite;
    }

    /**
     * Returns the <code>Composite</code> which would be used when drawing
     * this Annotation.
     *
     * @return Composite which would be used when drawing this Annotation; or
     *	       <code>null</code> if using the default Composite in the graphics
     *	       context.
     */
    public Composite getComposite()
    {
	return _composite;
    }

    /**
     * Sets whether this Annotation is enabled. Disabled Annotations are 
     * ignored by Viewport during its painting process. By default an
     * Annotation is initially enabled.
     *
     * @param enabled <code>true</code> to enable this Annotation;
     *		      <code>false</code> to disable.
     */
    public void setEnabled(boolean enabled)
    {
	_enabled = enabled;
    }

    /**
     * Returns whether this Annotation is enabled. Disabled Annotations are 
     * ignored by Viewport during its painting process. By default an
     * Annotation is initially enabled.
     *
     * @return <code>true</code> if this Annotation is enabled;
     *	       <code>false</code> if disabled.
     */
    public boolean isEnabled()
    {
	return _enabled;
    }

    /**
     * Paints this Annotation. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.) All the <code>AffineTransform</code>s passed
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
    public abstract void paint(Graphics2D g2d,
			       AffineTransform imageTransform,
			       AffineTransform annotationTransform,
			       AffineTransform viewportTransform);

    /**
     * Calculates the bounding Rectangle that encloses this Annotation in 
     * Display (Component) Space. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.) All the <code>AffineTransform</code>s passed
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
    public abstract Rectangle getBounds(Graphics2D g2d,
					AffineTransform imageTransform,
					AffineTransform annotationTransform,
					AffineTransform viewportTransform);

    /**
     * Returns the square of the distance from a point to this Annotation in
     * Display (Component) Space. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.) All the <code>AffineTransform</code>s passed
     * in <b>MUST</b> also be preserved, i.e., cloned copies should be used
     * if changes are to be made.
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
    public abstract double
	getDistanceSquared(Point2D point, Graphics2D g2d,
			   AffineTransform imageTransform,
			   AffineTransform annotationTransform,
			   AffineTransform viewportTransform);

    // -----------------
    // Protected methods
    // -----------------

    /**
     * Prepares a <code>Graphics2D</code> graphics context for drawing this
     * Annotation according to the settings of this Annotation. This method
     * should be invoked in {@link #paint} to set up the
     * <code>Graphics2D</code> before using it for drawing.
     * {@link #_restoreGraphics2D} should be invoked after drawing to restore
     * the <code>Graphics2D</code> to its original state.
     * <p>
     * The original settings are saved in a <code>Map</code> and returned.
     * Subclasses with additional <code>Graphics2D</code> settings should
     * override this method and invoke the super class' version in its own
     * implementation, saving the original values of whatever settings it
     * changes in the same map. This method and {@link #_restoreGraphics2D}
     * should always be overriden at the same time to make sure the
     * <code>Graphics2D</code> is fully preserved and restored.
     * <p>
     * Namely, in this method the <code>Composite</code> will be saved and
     * set to that of this Annotation. The <code>Paint</code> will be saved
     * but not set.
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
	// Storage for original settings
	Map origSettings = new HashMap();

	// Save and set composite
	Composite composite = getComposite();
	if (composite != null) {
	    origSettings.put(_KEY_COMPOSITE, g2d.getComposite());
	    g2d.setComposite(composite);
	}

	// Save paint
	origSettings.put(_KEY_PAINT, g2d.getPaint());

	return origSettings;
    }

    /**
     * Restores a <code>Graphics2D</code> graphics context after drawing is
     * completed according to the original settings saved in the
     * <code>Map</code>.
     * <p>
     * This method should be overriden correspondingly if
     * {@link #_prepareGraphics2D} is overriden for additional settings. The
     * super class' version should be invoked in a subclass' implementation.
     * <p>
     * Namely this method does the opposite of {@link #_prepareGraphics2D} and
     * restores the settings of <code>Composite</code> and <code>Paint</code>.
     *
     * @param g2d <code>Graphics2D</code> to restore.
     * @param origSettings Original <code>Graphics2D</code> settings stored in
     *			   a <code>Map</code>.
     */
    protected void _restoreGraphics2D(Graphics2D g2d, Map origSettings)
    {
	// Restore composite
	Composite composite = (Composite)origSettings.get(_KEY_COMPOSITE);
	if (composite != null) {
	    g2d.setComposite(composite);
	}

	// Restore paint
	Paint paint = (Paint)origSettings.get(_KEY_PAINT);
	if (paint != null) {
	    g2d.setPaint(paint);

	}
    }
}
