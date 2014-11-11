/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport.annotation;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

/**
 * Annotation which displays a text string in a <code>Viewport</code>.
 * <p>
 * An AnnotationString is positioned with respect to a reference point called
 * <i>location</i> in this class. The choice of a coordinate space for the
 * reference point, out of the four candidates: Image, Annotation, Viewport,
 * and Display (Component) Spaces, is up to the implementing subclass.
 * <p>
 * The <i>alignment mode</i> of an AnnotationString determines which specific
 * point on/inside the text's bounding box should be aligned to the reference
 * point.
 *
 * @see Annotation
 *
 * @version January 8, 2004
 */
//
// Implementation Notes
//
// The strategy for all the text string annotation classes is to use
// Graphics2D.drawString() for rendering for performance reason. Therefore,
// any affine transform is applied to the Graphics2D instance.
//
// Since the stroke attribute of a Graphics2D does not apply to the text
// rendered by drawString(), outlining is done by shadowing, i.e., drawing
// the text in background color with a tiny shift (one pixel right and one
// pixel down, to be exact). This shift is in Display Space, so an inverse
// transform is used to find out the shift in user space.
//
// Because of the fixed (1,1) shadowing shift, the result may be fuzzy and
// inlegible when the actual size of the text (its size in device space)
// is small. However, the same can be said of the original way of outlining
// by using a stroke, which proves to be very slow.
//
public abstract class AnnotationString extends Annotation
{
    // ---------
    // Constants
    // ---------

    /** Align by the upper-left corner of the bounding box. */
    public static final AlignMode ALIGN_TOP_LEFT
	= new AlignMode("ALIGN_TOP_LEFT",
			AlignMode.LEFT, AlignMode.TOP);

    /** Align by the center of the top side of the bounding box. */
    public static final AlignMode ALIGN_TOP_CENTER      
	= new AlignMode("ALIGN_TOP_CENTER",
			AlignMode.CENTER, AlignMode.TOP);

    /** Align by the upper-right corner of the bounding box. */
    public static final AlignMode ALIGN_TOP_RIGHT
	= new AlignMode("ALIGN_TOP_RIGHT",
			AlignMode.RIGHT, AlignMode.TOP);

    /** Align by the center of the left side of the bounding box. */
    public static final AlignMode ALIGN_CENTER_LEFT
	= new AlignMode("ALIGN_CENTER_LEFT",
			AlignMode.LEFT, AlignMode.CENTER);

    /** Align by the center of the bounding box. */
    public static final AlignMode ALIGN_CENTER_CENTER
	= new AlignMode("ALIGN_CENTER_CENTER",
			AlignMode.CENTER, AlignMode.CENTER);

    /** Align by the center of the right side of the bounding box. */
    public static final AlignMode ALIGN_CENTER_RIGHT
	= new AlignMode("ALIGN_CENTER_RIGHT",
			AlignMode.RIGHT, AlignMode.CENTER);

    /** Align by the lower-left corner of the bounding box. */
    public static final AlignMode ALIGN_BOTTOM_LEFT
	= new AlignMode("ALIGN_BOTTOM_LEFT",
			AlignMode.LEFT, AlignMode.BOTTOM);

    /** Align by the center of the bottom side of the bounding box. */
    public static final AlignMode ALIGN_BOTTOM_CENTER
	= new AlignMode("ALIGN_BOTTOM_CENTER",
			AlignMode.CENTER, AlignMode.BOTTOM);

    /** Align by the lower-right corner of the bounding box. */
    public static final AlignMode ALIGN_BOTTOM_RIGHT
	= new AlignMode("ALIGN_BOTTOM_RIGHT",
			AlignMode.RIGHT, AlignMode.BOTTOM);

    /** Align by the left endpoint of the baseline. */
    public static final AlignMode ALIGN_BASELINE_LEFT
	= new AlignMode("ALIGN_BASELINE_LEFT",
			AlignMode.LEFT, AlignMode.BASELINE);

    /** Align by the center of the baseline. */
    public static final AlignMode ALIGN_BASELINE_CENTER
	= new AlignMode("ALIGN_BASELINE_CENTER",
			AlignMode.CENTER, AlignMode.BASELINE);

    /** Align by the right endpoint of the baseline. */
    public static final AlignMode ALIGN_BASELINE_RIGHT
	= new AlignMode("ALIGN_BASELINE_RIGHT",
			AlignMode.RIGHT, AlignMode.BASELINE);

    /**
     * Enumerated constant class representing modes of text alignment.
     */
    public static final class AlignMode
    {
	private static final int CENTER = 0;
	private static final int LEFT = 1;
	private static final int RIGHT = 2;
	private static final int TOP = 1;
	private static final int BOTTOM = 2;
	private static final int BASELINE = 3;

	/** Private constructor as in typesafe enum pattern. */
	private AlignMode(String name, int x, int y)
	{
	    if (name == null) {
		throw new NullPointerException("name can not be null.");
	    }
	    _name = name;
	    if (x < CENTER || x > RIGHT) {
		throw new IllegalArgumentException(x +" is not a valid value "+
						   "for x.");
	    }
	    X = x;
	    if (y < CENTER || y > BASELINE) {
		throw new IllegalArgumentException(y +" is not a valid value "+
						   "for y.");
	    }
	    Y = y;
	}

	public String toString()
	{
	    return "AlignMode: " + _name;
	}

	private final int X;
	private final int Y;
	private final String _name;
    }

    // ----------------
    // Protected fields
    // ----------------

    /** Key for saving the Font setting of a Graphics2D in a Map. */
    protected static final Object _KEY_FONT = Font.class;

    /**
     * Key for saving the antialiasing rendering hint setting of a Graphics2D
     * in a Map.
     */
    protected static final Object _KEY_ANTIALIASING =
	RenderingHints.KEY_TEXT_ANTIALIASING;

    // --------------
    // Private fields
    // --------------

    /** Whether antialiasing rendering is enabled. */
    private static boolean _antialiasing = false;

    /** Whether double outlining is enabled. */
    private static boolean _doubleOutlining = false;

    /** Text string of this AnnotationString. */
    private String _string;

    /** Font used to display the text string. */
    private Font _font;

    /** Alignment of this AnnotationString. */
    private AlignMode _alignment;

    /** Reference point for positioning this AnnotationString. */
    private Point2D _location;

    // ------------
    // Constructors
    // ------------

    /**
     * Constructs an AnnotationString given a text string and a location. By
     * default the constructed AnnotationString has an initial font of
     * <code>new Font(null, Font.PLAIN, 10)</code> and an initial alignment
     * of <code>ALIGN_TOP_LEFT</code>.
     *
     * @param string Text string of the AnnotationString.
     * @param location Reference point for positioning this AnnotationString.
     *
     * @throws NullPointerException if <code>string</code> or
     *	       <code>location</code> is <code>null</code>.
     */
    protected AnnotationString(String string, Point2D location)
    {
	this(string, location, new Font(null, Font.PLAIN, 10), ALIGN_TOP_LEFT);
    }

    /**
     * Constructs an AnnotationString given a text string, a location, and a
     * font. By default the constructed AnnotationString has an initial
     * alignment of <code>ALIGN_TOP_LEFT</code>.
     *
     * @param string Text string of the AnnotationString.
     * @param location Reference point for positioning this AnnotationString.
     * @param font Font used to display the text string.
     *
     * @throws NullPointerException if any of <code>string</code>,
     *	       <code>location</code>, or <code>font</code> is
     *	       <code>null</code>.
     */
    protected AnnotationString(String string, Point2D location, Font font)
    {
	this(string, location, font, ALIGN_TOP_LEFT);
    }

    /**
     * Constructs an AnnotationString given a text string, a location, and an
     * alignment mode. By default the constructed AnnotationString has an
     * initial font of <code>new Font(null, Font.PLAIN, 10)</code>.
     *
     * @param string Text string of the AnnotationString.
     * @param location Reference point for positioning this AnnotationString.
     * @param alignment Alignment mode of this AnnotationString.
     *
     * @throws NullPointerException if any of <code>string</code>,
     *	       <code>location</code>, or <code>alignment</code> is
     *	       <code>null</code>.
     */
    protected AnnotationString(String string, Point2D location,
			       AlignMode alignment)
    {
	this(string, location, new Font(null, Font.PLAIN, 10), alignment);
    }

    /**
     * Constructs an AnnotationString given a text string, a location, a font,
     * and an alignment mode.
     *
     * @param string Text string of the AnnotationString.
     * @param location Reference point for positioning this AnnotationString.
     * @param font Font used to display the text string.
     * @param alignment Alignment mode of this AnnotationString.
     *
     * @throws NullPointerException if any of <code>string</code>,
     *	       <code>location</code>, <code>font</code>, or
     *	       <code>alignment</code> is <code>null</code>.
     */
    protected AnnotationString(String string, Point2D location,
			       Font font, AlignMode alignment)
    {
	setString(string);
	setLocation(location);
	setFont(font);
	setAlignment(alignment);
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Enables/disables antialiasing rendering. This setting applies to
     * <b>ALL</b> AnnotationStrings. By default antialiasing is disabled.
     *
     * @param enabled <code>true</code> to enable and <code>false</code> to
     *		      disable antialiasing rendering of all AnnotationStrings.
     */
    public static void setAntialiasingEnabled(boolean enabled)
    {
	_antialiasing = enabled;
    }

    /**
     * Returns whether antialiasing rendering is enabled. This setting applies
     * to <b>ALL</b> AnnotationStrings
     *
     * @return <code>true</code> if antialiasing rendering is enabled;
     *	       <code>false</code> if disabled.
     */
    public static boolean isAntialiasingEnabled()
    {
	return _antialiasing;
    }

    /**
     * Enables/disables the double outlining effect. This setting applies to
     * <b>ALL</b> AnnotationStrings. The default is no double outlining, in
     * which case AnnotationStrings are rendered with single outlining, which
     * is more like shadows to the text.
     * <p>
     * Enabling double outlining may slow down the rendering by up to 50%.
     *
     * @param enabled <code>true</code> to enable and <code>false</code> to
     *		      disable double outlining of all AnnotationStrings.
     */
    public static void setDoubleOutliningEnabled(boolean enabled)
    {
	_doubleOutlining = enabled;
    }

    /**
     * Returns whether double outlining is enabled. This setting applies to
     * <b>ALL</b> AnnotationStrings
     *
     * @return <code>true</code> if double outlining is enabled;
     *	       <code>false</code> if disabled.
     */
    public static boolean isDoubleOutliningEnabled()
    {
	return _doubleOutlining;
    }

    /**
     * Sets the text string of this AnnotationString.
     *
     * @param string New text string of this AnnotationString.
     *
     * @throws NullPointerException if <code>string</code> is
     *	       <code>null</code>.
     */
    public void setString(String string)
    {
	if (string == null) {
	    throw new NullPointerException("string can not be null.");
	}
	_string = string;
    }

    /**
     * Returns the text string of this AnnotationString.
     *
     * @return Text string of this AnnotationString.
     */
    public String getString()
    {
	return _string;
    }

    /**
     * Sets the reference point for positioning this AnnotationString. The
     * choice of the coordinate space in which this reference point is defined
     * is up to the implementing subclass.
     *
     * @param location Reference point for positioning this AnnotationString.
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
     * Returns the reference point for positioning this AnnotationString. The
     * choice of the coordinate space in which this reference point is defined
     * is up to the implementing subclass.
     *
     * @return Reference point for positioning this AnnotationString.
     */
    public Point2D getLocation()
    {
	// Make a defensive copy
	return (Point2D)_location.clone();
    }

    /**
     * Sets the font used to display the String.
     *
     * @param font Font used to display the String.
     *
     * @throws NullPointerException if <code>font</code> is <code>null</code>.
     */
    public void setFont(Font font)
    {
	if (font == null) {
	    throw new NullPointerException("font can not be null.");
	}
	_font = font;
    }

    /**
     * Returns the font used to display the String.
     *
     * @return Font used to display the String.
     */
    public Font getFont()
    {
	return _font;
    }

    /**
     * Sets the alignment mode of this AnnotationString.
     *
     * @param alignment Alignment mode of this AnnotationString.
     *
     * @throws NullPointerException if <code>alignment</code> is
     *	       <code>null</code>.
     */
    public void setAlignment(AlignMode alignment)
    {
	if (alignment == null) {
	    throw new NullPointerException("alignment can not be null.");
	}
	_alignment = alignment;
    }

    /**
     * Returns the alignment mode of this AnnotationString.
     *
     * @return Alignment of this AnnotationString.
     */
    public AlignMode getAlignment()
    {
	return _alignment;
    }

    /**
     * Returns the square of the distance from a point to this Annotation in
     * Display (Component) Space.
     * <p>
     * This default implementation calculates the square distance as the
     * minimum square distance from the given point to all four sides of the
     * AnnotationString's bounding box if the point is outside the box. If
     * the point falls inside the bounding box, the square distance is zero by
     * definition.
     *
     * @param point Point2D in Display Space from which to determine the
     *              square distance to the Annotation in Display Space.
     * @param g2d Graphics context used to paint this Annotation. This is
     *		  needed for font metrics calculation.
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
	// Get the bounding Rectangle in Display Space
	Rectangle bounds = getBounds(g2d, imageTransform, annotationTransform,
				     viewportTransform);

	// Check if the point is inside the bounding Rectangle
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

    /**
     * Returns the bounding box of a text string as rendered by the specified
     * graphics context before any kind of affine transformation. The bounding
     * box is defined as if the anchor point is the origin. The anchor point is
     * the point that would be passed to <code>drawString</code>, which is also
     * the left endpoint of the baseline.
     * <p>
     * This method uses the <code>FontMetrics</code> of the specified graphics
     * context to calculate the bounding box. FontMetrics seems not affected
     * by the affine transform of the graphics context.
     *
     * @param g2d Graphics context in which the text string is to be rendered.
     * @param string Text string to be rendered.
     * @param font Font used to render the text string.
     *
     * @return Bounding box of the text string as rendered by the specified
     *	       graphics context before any kind of affine transformation.
     *
     * @throws NullPointerException if any of <code>g2d</code>,
     *	       <code>string</code>, or <code>font</code> is null.
     */
    public static Rectangle2D getOriginalBounds(Graphics2D g2d,
						String string, Font font)
    {
	FontMetrics metrics = g2d.getFontMetrics(font);
	return metrics.getStringBounds(string, g2d);
    }

    /**
     * Returns the displacement from the anchor point to the alignment point
     * on/inside the specified bounding box. The anchor point is assumed at
     * the origin, which is consistent with the bounding box returned by
     * <code>getOriginalBounds</code>.
     *
     * @param bounds Bounding box of a text string.
     * @param alignment Alignment mode for positioning the text string.
     *
     * @return Displacement from the anchor point to the alignment point
     *	       on/inside the specified bounding box.
     *
     * @throws NullPointerException if <code>bounds</code> or
     *	       <code>alignment</code> is <code>null</code>.
     *
     * @see #getOriginalBounds(Graphics2D, String, Font)
     */
    public static Point2D getAlignmentDisplacement(Rectangle2D bounds,
						   AlignMode alignment)
    {
	Point2D.Double p = new Point2D.Double();

	// X displacement
	switch (alignment.X) {
	case AlignMode.CENTER:
	    p.x = bounds.getX() + bounds.getWidth() / 2.;
	    break;
	case AlignMode.LEFT:
	    p.x = bounds.getX();
	    break;
	case AlignMode.RIGHT:
	    p.x = bounds.getX() + bounds.getWidth();
	    break;
	default:
	    // Should never happen; we could have an assertion here
	}

	// Y displacement
	switch (alignment.Y) {
	case AlignMode.CENTER:
	    p.y = bounds.getY() + bounds.getHeight() / 2.;
	    break;
	case AlignMode.TOP:
	    p.y = bounds.getY();
	    break;
	case AlignMode.BOTTOM:
	    p.y = bounds.getY() + bounds.getHeight();
	    break;
	case AlignMode.BASELINE:
	    p.y = 0.0;
	    break;
	default:
	    // Should never happen; we could have an assertion here
	}

	return p;
    }

    // -----------------
    // Protected methods
    // -----------------

    /**
     * Convenient synonym of
     * <code>getOriginalBounds(g2d, getString(), getFont())</code>
     *
     * @see #getOriginalBounds(Graphics2D, String, Font)
     */
    protected Rectangle2D _getOriginalBounds(Graphics2D g2d)
    {
	return getOriginalBounds(g2d, getString(), getFont());
    }

    /**
     * Convenient synonym of
     * <code>getAlignmentDisplacement(bounds, getAlignment())</code>
     *
     * @see #getAlignmentDisplacement
     */
    protected Point2D _getAlignmentDisplacement(Rectangle2D bounds)
    {
	return getAlignmentDisplacement(bounds, getAlignment());
    }

    /**
     * Convenience method for calculating the actual anchor point to use in
     * <code>drawString</code> after accounting alignment, given the specified
     * bounding box, the current alignment mode and location.
     *
     * @param bounds Bounding box of the text with the anchor point as origin.
     *
     * @return Actual anchor point to use in <code>drawString</code>.
     */
    protected Point2D _getAnchorPoint(Rectangle2D bounds)
    {
	Point2D disp = _getAlignmentDisplacement(bounds);
	Point2D location = getLocation();
	return new Point2D.Double(location.getX() - disp.getX(),
				  location.getY() - disp.getY());
    }

    /**
     * Prepares a <code>Graphics2D</code> graphics context for drawing this
     * AnnotationString according to the settings of this Annotation. Namely,
     * the <code>Font</code> and antialiasing <code>RenderingHint</code> will
     * be set to that of this AnnotationString, in addition to settings
     * that are saved and changed by the super class, Annotation.
     * <p>
     * The original settings are saved in a <code>Map</code> and returned.
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

	// Save and set font
	origSettings.put(_KEY_FONT, g2d.getFont());
	g2d.setFont(getFont());

	// Save and set antialiasing rendering hint
	Object origAntialiasing =
	    g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
	if (isAntialiasingEnabled() ^ 
	    (origAntialiasing == RenderingHints.VALUE_TEXT_ANTIALIAS_ON)) {
	    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				 isAntialiasingEnabled() ?
				 RenderingHints.VALUE_TEXT_ANTIALIAS_ON :
				 RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
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

	// Restore font
	g2d.setFont((Font)origSettings.get(_KEY_FONT));

	// Restore antialiasing rendering hint
	Object origAntialiasing = origSettings.get(_KEY_ANTIALIASING);
	if (origAntialiasing != null) {
	    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				 origAntialiasing); 
	}
    }
}
