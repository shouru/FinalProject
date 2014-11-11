/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.medtoolbox.jviewbox.LookUpTable;

/**
 * Class which stores the viewing state of a Viewport, including the location
 * and size of the Viewport and the scale factors, panning offsets, rotation,
 * flip, and look-up table that are applied to the image.
 * <p>
 * Some parameters are normalized by a Viewport dimension (width or height)
 * in order to make them independent of the Viewport size. This enables the
 * same parameters to be used between Viewports of different sizes.
 * <p>
 * <a name="autoscale"><b>Automatic scale factor adjustment</b></a>
 * <p>
 * Options are provided to have a ViewportState automatically adjust the scale
 * factors when the Viewport is resized. These options are:
 * <ul>
 * <li>{@link #AUTO_SCALE_OFF} leaves the scale factors as is.
 * </li>
 * <li>{@link #AUTO_SCALE_PROPORTIONAL_TO_WIDTH} proportionates both the X
 *     and Y scale factors by the ratio of the new Viewport width to the
 *     original width.
 * </li>
 * <li>{@link #AUTO_SCALE_PROPORTIONAL_TO_HEIGHT} proportionates both the X
 *     and Y scale factors by the ratio of the new Viewport height to the
 *     original height.
 * </li>
 * <li>{@link #AUTO_SCALE_PROPORTIONAL_TO_BOTH} proportionates both the X
 *     and Y scale factors by the smaller of the two ratios of the new
 *     Viewport width to the original width and the new Viewport height to
 *     the original height.
 * </li>
 * <li>{@link #AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT} maintains a lower bound
 *     on the scale factors so that the scaled image can never be smaller than
 *     the Viewport. This option is independent of the three
 *     <code>PROPORTIONAL</code> options and may be bitwise <code>OR</code>ed
 *     with them.
 * </li></ul>
 * These options also affect how scale factors are automatically adjusted when
 * a ViewportState is matched to another using {@link #match}:
 * <ul>
 * <li>{@link #AUTO_SCALE_OFF} copies the scale factors as is, even if the
 *     source and target ViewportStates have different pixel aspect ratios
 *     (i.e., different ratios of the X scale factor divided by Y), in which
 *     case the target ViewportState's original pixel aspect ratio will be
 *     overridden.
 * </li>
 * <li>{@link #AUTO_SCALE_PROPORTIONAL_TO_WIDTH} replicates the
 *     <b>normalized</b> X scale factor (as returned by {@link #getScaleX}).
 *     In other words, it sets the target ViewportState's X scale factor to
 *     the value of the source ViewportState's X scale factor multiplied by
 *     the ratio of the target Viewport's width to the source Viewport's
 *     width. The target Viewport's Y factor is proportionated accordingly to
 *     maintain its original pixel aspect ratio.
 * </li>
 * <li>{@link #AUTO_SCALE_PROPORTIONAL_TO_HEIGHT} replicates the
 *     <b>normalized</b> Y scale factor (as returned by {@link #getScaleX}).
 *     In other words, it sets the target ViewportState's Y scale factor to
 *     the value of the source ViewportState's Y scale factor multiplied by
 *     the ratio of the target Viewport's height to the source Viewport's
 *     height. The target Viewport's X factor is proportionated accordingly to
 *     maintain its original pixel aspect ratio.
 * </li>
 * <li>{@link #AUTO_SCALE_PROPORTIONAL_TO_BOTH} computes the results of 
 *     <code>AUTO_SCALE_PROPORTIONAL_TO_WIDTH</code> and
 *     <code>AUTO_SCALE_PROPORTIONAL_TO_HEIGHT</code> and then applies the
 *     one that gives smaller scale factors.
 * </li>
 * <li>{@link #AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT} has no effect here.
 * </li></ul>
 *
 * @version January 8, 2004
 */
public class ViewportState implements Cloneable
{
    // ---------
    // Constants
    // ---------

    /**
     * Automatic scale factor adjustment: off.
     *
     * @see #setAutoScaleOptions
     */
    public static final int AUTO_SCALE_OFF = 0x0;

    /**
     * Automatic scale factor adjustment: proportional to width.
     *
     * @see #setAutoScaleOptions
     */
    public static final int AUTO_SCALE_PROPORTIONAL_TO_WIDTH = 0x1;

    /**
     * Automatic scale factor adjustment: proportional to height.
     *
     * @see #setAutoScaleOptions
     */
    public static final int AUTO_SCALE_PROPORTIONAL_TO_HEIGHT = 0x2;

    /**
     * Automatic scale factor adjustment: proportional to width and height.
     * This is simply <code>AUTO_SCALE_PROPORTIONAL_TO_WIDTH |
     * AUTO_SCALE_PROPORTIONAL_TO_HEIGHT</code>.
     *
     * @see #setAutoScaleOptions
     */
    public static final int AUTO_SCALE_PROPORTIONAL_TO_WIDTH_AND_HEIGHT =
	AUTO_SCALE_PROPORTIONAL_TO_WIDTH |
	AUTO_SCALE_PROPORTIONAL_TO_HEIGHT;

    /**
     * Automatic scale factor adjustment: proportional to width and height.
     * This is synonym of {@link #AUTO_SCALE_PROPORTIONAL_TO_WIDTH_AND_HEIGHT}.
     *
     * @see #setAutoScaleOptions
     */
    public static final int AUTO_SCALE_PROPORTIONAL_TO_BOTH =
	AUTO_SCALE_PROPORTIONAL_TO_WIDTH_AND_HEIGHT;

    /**
     * Automatic scale factor adjustment: no smaller than Viewport.
     *
     * @see #setAutoScaleOptions
     */
    public static final int AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT = 0x4;

    // --------------
    // Private fields
    // --------------

    /** Options of automatic scale factor adjustment. */
    private static int _autoScaleOptions =
	AUTO_SCALE_PROPORTIONAL_TO_BOTH | AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT;

    /**
     * Original dimension of the image being displayed.
     *
     * @since 2.0b
     */
    private Dimension _imageSize;

    /**
     * Rectangle which defines the Viewport boundary in Component Space (i.e.,
     * the ViewportCluster's coordinate system).
     *
     * @since 2.0b
     */
    private Rectangle _bounds;

    /**
     * Scale ratio along the X direction.
     *
     * @since 2.0b, this is no longer normalized (divided by the Viewport
     *	      width).
     */
    private double _scaleX = 1.0;

    /**
     * Scale ratio along the Y direction.
     *
     * @since 2.0b, this is no longer normalized (divided by the Viewport
     *	      height).
     */
    private double _scaleY = 1.0;

    /** Translation along the X direction divided by the Viewport width. */
    private double _panX = 0.0;

    /** Translation along the Y direction divided by the Viewport height. */
    private double _panY = 0.0;

    /**
     * Number of times (0,1,2,3) a 90 degree clockwise rotation has occurred
     * amount the pivot point.
     * <p>
     * Note that vertical flips occur before rotations.
     */
    private int _numberOfRotations = 0;

    /**
     * True if a flip has occurred about the line which runs vertically through
     * the pivot point.
     * <p>
     * Note that vertical flip occurs before rotations.
     */
    private boolean _isVerticallyFlipped = false;

    /** Point about which rotations and flips are made. */
    private Point2D _pivotPoint;

    /**
     * Look-up table (LUT) which maps an Image pixel value to an 8-bit display
     * range.
     */
    private LookUpTable _lut;

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs a ViewportState for the specified Viewport. A ViewportState
     * instance must be validated by {@link #validate} before it can be used.
     *
     * @param vp Viewport to initialize this ViewportState with.
     * @param pivotPoint Point about which rotations and flips are made.
     *
     * @deprecated Replaced by {@link #ViewportState()} and
     *		   {@link #ViewportState(Point2D)} as of jViewBox 2.0b.
     */
    public ViewportState(Viewport vp, Point2D pivotPoint)
    {
	this(pivotPoint);
    }

    /**
     * Constructs a ViewportState. A ViewportState instance must be validated
     * by {@link #validate} before it can be used.
     *
     * @param pivotPoint Point about which rotations and flips are made.
     *
     * @since 2.0b
     */
    public ViewportState(Point2D pivotPoint)
    {
	this((Rectangle)null, pivotPoint);
    }

    /**
     * Constructs a ViewportState. A ViewportState instance must be validated
     * by {@link #validate} before it can be used.
     *
     * @param bounds Initial Viewport bounds.
     *
     * @since 2.0b
     */
    public ViewportState(Rectangle bounds)
    {
	this(bounds, null);
    }

    /**
     * Constructs a ViewportState. A ViewportState instance must be validated
     * by {@link #validate} before it can be used.
     *
     * @since 2.0b
     */
    public ViewportState()
    {
	this((Rectangle)null, null);
    }

    /**
     * Constructs a ViewportState. A ViewportState instance must be validated
     * by {@link #validate} before it can be used.
     *
     * @param bounds Initial Viewport bounds.
     * @param pivotPoint Point about which rotations and flips are made.
     *
     * @since 2.0b
     */
    public ViewportState(Rectangle bounds, Point2D pivotPoint)
    {
	// Defensive copy
	if (bounds != null) {
	    _bounds = (Rectangle)bounds.clone();
	}
	if (pivotPoint != null) {
	    _pivotPoint = (Point2D)pivotPoint.clone();
	}
    }

    /**
     * Returns a clone of this ViewportState.
     *
     * @return Copy of this ViewportState.
     */
    public Object clone()
    {
	ViewportState vs;
	try {
	    vs = (ViewportState)super.clone();
	}
	catch (CloneNotSupportedException e) {
	    // Should never happen
	    throw new InternalError("Failed to clone a ViewportState.");
	}

	// Clone imageSize, bounds, pivot point, and LUT
	if (_imageSize != null) {
	    vs._imageSize = (Dimension)_imageSize.clone();
	}
	if (_bounds != null) {
	    vs._bounds = (Rectangle)_bounds.clone();
	}
	if (_pivotPoint != null) {
	    vs._pivotPoint = (Point2D)_pivotPoint.clone();
	}
	if (_lut != null) {
	    vs._lut = (LookUpTable)_lut.clone();
	}

	return vs;
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Sets the options of
     * <a href="#autoscale">automatic scale factor adjustment</a>. The same
     * setting applies to <b>ALL</b> Viewports. The default is
     * {@link #AUTO_SCALE_PROPORTIONAL_TO_BOTH} <code>|</code>
     * {@link #AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT}.
     *
     * @param options Options of automatic scale factor adjustment, formed by
     *		      bitwise <code>OR</code> operations on the corresponding
     *		      <code>AUTO_SCALE_*</code> bit masks.
     *
     * @see #getAutoScaleOptions
     */
    public static void setAutoScaleOptions(int options)
    {
	_autoScaleOptions = options;
    }

    /**
     * Returns the current options of
     * <a href="#autoscale">automatic scale factor adjustment</a>. The same
     * setting applies to <b>ALL</b> Viewports.
     *
     * @return Current options of automatic scale factor adjustment.
     *
     * @see #setAutoScaleOptions
     */
    public static int getAutoScaleOptions()
    {
	return _autoScaleOptions;
    }

    /**
     * Turns off <a href="#autoscale">automatic scale factor adjustment</a>
     * completely. Shortcut of
     * <code>setAutoScaleOptions(AUTO_SCALE_OFF)</code>.
     *
     * @see #setAutoScaleOptions
     */
    public static void setAutoScaleOff()
    {
	setAutoScaleOptions(AUTO_SCALE_OFF);
    }

    /**
     * Returns whether the option of
     * {@link #AUTO_SCALE_PROPORTIONAL_TO_WIDTH} is on.
     *
     * @return <code>true</code> if {@link #AUTO_SCALE_PROPORTIONAL_TO_WIDTH}
     *	       is on;
     *	       <code>false</code> otherwise.
     *
     * @see #getAutoScaleOptions
     */
    public static boolean isAutoScaleProportionalToWidth()
    {
	return (_autoScaleOptions & AUTO_SCALE_PROPORTIONAL_TO_WIDTH) != 0;
    }

    /**
     * Turns on/off the option of {@link #AUTO_SCALE_PROPORTIONAL_TO_WIDTH}.
     *
     * @param enabled <code>true</code> to turn on
     *		      {@link #AUTO_SCALE_PROPORTIONAL_TO_WIDTH};
     *		      <code>false</code> to turn it off.
     *
     * @see #setAutoScaleOptions
     */
    public static void setAutoScaleProportionalToWidth(boolean enabled)
    {
	if (enabled) {
	    _autoScaleOptions |= AUTO_SCALE_PROPORTIONAL_TO_WIDTH;
	}
	else {
	    _autoScaleOptions &= ~AUTO_SCALE_PROPORTIONAL_TO_WIDTH;
	}
    }

    /**
     * Returns whether the option of
     * {@link #AUTO_SCALE_PROPORTIONAL_TO_HEIGHT} is on.
     *
     * @return <code>true</code> if {@link #AUTO_SCALE_PROPORTIONAL_TO_HEIGHT}
     *	       is on;
     *	       <code>false</code> otherwise.
     *
     * @see #getAutoScaleOptions
     */
    public static boolean isAutoScaleProportionalToHeight()
    {
	return (_autoScaleOptions & AUTO_SCALE_PROPORTIONAL_TO_HEIGHT) != 0;
    }

    /**
     * Turns on/off the option of {@link #AUTO_SCALE_PROPORTIONAL_TO_HEIGHT}.
     *
     * @param enabled <code>true</code> to turn on
     *		      {@link #AUTO_SCALE_PROPORTIONAL_TO_HEIGHT};
     *		      <code>false</code> to turn it off.
     *
     * @see #setAutoScaleOptions
     */
    public static void setAutoScaleProportionalToHeight(boolean enabled)
    {
	if (enabled) {
	    _autoScaleOptions |= AUTO_SCALE_PROPORTIONAL_TO_HEIGHT;
	}
	else {
	    _autoScaleOptions &= ~AUTO_SCALE_PROPORTIONAL_TO_HEIGHT;
	}
    }

    /**
     * Returns whether the option of
     * {@link #AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT} is on.
     *
     * @return <code>true</code> if
     *	       {@link #AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT} is on;
     *	       <code>false</code> otherwise.
     *
     * @see #getAutoScaleOptions
     */
    public static boolean isAutoScaleNoSmallerThanViewport()
    {
	return (_autoScaleOptions & AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT) != 0;
    }

    /**
     * Turns on/off the option of {@link #AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT}.
     *
     * @param enabled <code>true</code> to turn on
     *		      {@link #AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT};
     *		      <code>false</code> to turn it off.
     *
     * @see #setAutoScaleOptions
     */
    public static void setAutoScaleNoSmallerThanViewport(boolean enabled)
    {
	if (enabled) {
	    _autoScaleOptions |= AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT;
	}
	else {
	    _autoScaleOptions &= ~AUTO_SCALE_NO_SMALLER_THAN_VIEWPORT;
	}
    }

    /**
     * Returns the rectangle which defines the Viewport boundary in
     * Component Space.
     *
     * @return Rectangle which defines the Viewport boundary. The returned
     *	       value is a copy.
     */
    public Rectangle getBounds()
    {
	return (Rectangle)_bounds.clone();
    }

    /**
     * Returns the normalized scale factor along the X direction.
     *
     * @return Scale factor along the X direction divided by the Viewport's
     *	       width.
     */
    public double getScaleX()
    {
	return (_bounds.width != 0) ? _scaleX / _bounds.width : 0.0;
    }

    /**
     * Returns the normalized scale factor along the Y direction.
     *
     * @return Scale factor along the Y direction divided by the Viewport's
     *	       height.
     */
    public double getScaleY()
    {
	return (_bounds.height != 0) ? _scaleY / _bounds.height : 0.0;
    }

    /**
     * Returns the actual (not normalized) scale factor along the X direction.
     *
     * @return Scale factor along the X direction.
     *
     * @since 2.0b
     */
    public double getActualScaleX()
    {
	return _scaleX;
    }

    /**
     * Returns the actual (not normalized) scale factor along the Y direction.
     *
     * @return Scale factor along the Y direction.
     *
     * @since 2.0b
     */
    public double getActualScaleY()
    {
	return _scaleY;
    }

    /**
     * Sets the scale factors along the X and Y directions.
     *
     * @param scaleX Scale factor along the X direction divided by the 
     *               Viewport's width.
     * @param scaleY Scale factor along the Y direction divided by the
     *               Viewport's height.
     *
     * @throws IllegalArgumentException if either scale factor is not a
     *	       positive number.
     */
    public void setScale(double scaleX, double scaleY)
    {
	setActualScale(scaleX * _bounds.width, scaleY * _bounds.height);
    }

    /**
     * Sets the actual (not normalized) scale factors along the X and Y
     * directions.
     *
     * @param scaleX Scale factor along the X direction.
     * @param scaleY Scale factor along the Y direction.
     *
     * @throws IllegalArgumentException If either scale factor is not a
     *	       positive number.
     *
     * @since 2.0b
     */
    public void setActualScale(double scaleX, double scaleY)
    {
	// Disallow negative or zero scale ratios
	if (scaleX <= 0.0 || scaleY <= 0.0) {
	    throw new IllegalArgumentException("ViewportState: Scale values " +
					       "of " + scaleX + " and " +
					       scaleY + " are not allowed in "+
					       "setScale.");
	}

	_scaleX = scaleX;
	_scaleY = scaleY;
    }

    /**
     * Returns the normalized translation distance along X.
     *
     * @return Translation along the X direction divided by the Viewport's
     *	       width.
     */
    public double getPanX()
    {
	return _panX;
    }

    /**
     * Returns the normalized translation distance along Y.
     *
     * @return Translation along the Y direction divided by the Viewport's
     *	       height.
     */
    public double getPanY()
    {
	return _panY;
    }

    /**
     * Returns the actual (not normalized) translation distance along X.
     *
     * @return Translation along the X direction.
     */
    public double getActualPanX()
    {
	return _panX * _bounds.width;
    }

    /**
     * Returns the actual (not normalized) translation distance along Y.
     *
     * @return Translation along the Y direction.
     */
    public double getActualPanY()
    {
	return _panY * _bounds.height;
    }

    /**
     * Sets the translation distance along X and Y.
     *
     * @param panX Translation along the X direction divided by the
     *             Viewport's width.
     * @param panY Translation along the Y direction divided by the
     *             Viewport's height.
     */
    public void setPan(double panX, double panY)
    {
	_panX = panX;
	_panY = panY;
    }

    /**
     * Sets the translation distance along X and Y.
     *
     * @param panX Actual (not normalized) translation along the X direction.
     * @param panY Actual (not normalized) translation along the Y direction.
     */
    public void setActualPan(double panX, double panY)
    {
	setPan(panX / _bounds.width, panY / _bounds.height);
    }

    /**
     * Returns the number of times a 90 degree clockwise rotation has occurred.
     *
     * @return Number of times (0,1,2,3) a 90 degree clockwise rotation has
     *         occurred amount the pivot point.
     */
    public int getRotation()
    {
	return _numberOfRotations;
    }

    /**
     * Sets the number of times a 90 degree clockwise rotation has occurred.
     *
     * @param numberOfRotations Number of times to rotate 90 degrees clockwise
     *                          about the pivot point.
     */
    public void setRotation(int numberOfRotations)
    {
	// Limit to 0,1,2,3
	_numberOfRotations = numberOfRotations % 4;
	if (_numberOfRotations < 0) {
	    _numberOfRotations += 4;
	}
    }

    /**
     * Determines whether or not a vertical flip (flip about the vertical, or
     * say Y axis) is in effect.
     *
     * @return True if a flip has occurred about the line which runs vertically
     *         through the pivot point; false otherwise.
     */
    public boolean isVerticallyFlipped()
    {
	return _isVerticallyFlipped;
    }

    /**
     * Sets the vertical flip (flip about the vertical, or say Y axis).
     *
     * @param isVerticallyFlipped Whether (true) or not (false) to flip about
     *                            the line which runs vertically through the
     *                            pivot point. 
     */
    public void setVerticallyFlipped(boolean isVerticallyFlipped)
    {
	_isVerticallyFlipped = isVerticallyFlipped;
    }

    /**
     * Returns the look-up table (LUT) applied to an Image. The returned value
     * is a reference to the same <code>LookUpTable</code> instance in this
     * ViewportState and hence modifying the returned object will affect this
     * ViewportState.
     *
     * @return Look-up table used to map each Image pixel value to an
     *         8-bit display value;
     *	       <code>null</code> if not exists.
     */
    public LookUpTable getLut()
    {
	return _lut;
    }

    /**
     * Sets the look-up table (LUT) applied to an Image. No clone is made
     * and modifying the <code>LookUpTable</code> passed in afterward may
     * still affect this ViewportState.
     *
     * @param lut Look-up table used to map each Image pixel value to an
     *            8-bit display value.
     */
    public void setLut(LookUpTable lut)
    {
	_lut = lut;
    }

    /**
     * Validates this ViewportState against the specified <code>Viewport</code>
     * so it can be used with the <code>Viewport</code>.
     *
     * @since 2.0b
     */
    public void validate(Viewport vp)
    {
	// Always update image size?
	int imageWidth = vp._getImageWidth();
	int imageHeight = vp._getImageHeight();
	if (_imageSize == null) {
	    _imageSize = new Dimension(imageWidth, imageHeight);
	}
	else {
	    // Has the image size changed?
	    if (_imageSize.width != imageWidth ||
		_imageSize.height != imageHeight) {

		// Any other adjustments?
		if ((int)_pivotPoint.getX() == _imageSize.width / 2 &&
		    (int)_pivotPoint.getY() == _imageSize.height / 2) {
		    _pivotPoint.setLocation(imageWidth / 2, imageHeight / 2);
		}

		_imageSize.setSize(imageWidth, imageHeight);
	    }
	}

	// Apply the default pivot point if it has not been set
	if (_pivotPoint == null) {
	    _pivotPoint = new Point(imageWidth / 2, imageHeight / 2);
	}

	// Apply default bounds if bounds have not been set
	if (_bounds == null) {
	    _bounds = new Rectangle(_imageSize);
	}
    }

    /**
     * Creates a transform using this <code>ViewportState</code>'s properties
     * for the given <code>Viewport</code>.
     *
     * @param vp Viewport to create a transform for.
     *
     * @return AffineTransform containing the ViewportState properties, or
     *         null if such a transform cannot be created.
     *
     * @deprecated Replaced by {@link #createTransform()} as of jViewBox 2.0b.
     */
    public AffineTransform createTransform(Viewport vp)
    {
	return createTransform();
    }

    /**
     * Creates a transform using this <code>ViewportState</code>'s properties.
     * <p>
     * The transformation is formed as follows:
     * <ol>
     * <li>Translate the image so that the pivot point falls on the origin.
     * </li>
     * <li>Flip the image about the Y axis if a vertical flip is selected.
     * </li>
     * <li>Rotate the image about the origin according to the rotation
     * property.
     * </li>
     * <li>Translate the image in the opposite direction and the same distance
     *     as step 1 did so that the pivot point is returned to where it was.
     * </li>
     * <li>Apply scaling according to the scale factor properties.
     * </li>
     * <li>Translate again according to the pan properties.
     * </li><ol>
     *
     * @return AffineTransform containing the ViewportState properties, or
     *         null if such a transform cannot be created.
     *
     * @since 2.0b
     */
    public AffineTransform createTransform()
    {
	// Construct the new transform
	AffineTransform newTransform = new AffineTransform();

	// Translate
	newTransform.translate(Math.rint(getActualPanX()),
			       Math.rint(getActualPanY()));

	// Scale
	newTransform.scale(getActualScaleX(), getActualScaleY());

	// Translate the pivot point back to its original location
	newTransform.translate(_pivotPoint.getX(), _pivotPoint.getY());

	// Rotate clockwise about the origin by multiples of 90 degrees
	if (_numberOfRotations != 0) {
	    newTransform.rotate(Math.toRadians(90.0 * _numberOfRotations));
	}

	// Flip about the Y axis
	if (_isVerticallyFlipped) {
	    newTransform.scale(-1.0, 1.0);
	}

	// Translate the pivot point to the origin
	newTransform.translate(-_pivotPoint.getX(), -_pivotPoint.getY());

	return newTransform;
    }

    /**
     * Matches the viewing state settings of this ViewportState to the
     * specified ViewportState.
     * <p>
     * This method matches this ViewportState's scale factors to those of the
     * specified ViewportState according to the options set by
     * {@link #setAutoScaleOptions}. Also, it replicates the normalized
     * panning offsets (as returned by {@link #getPanX} and {@link #getPanY})
     * instead of the actual panning offsets (as returned by
     * {@link #getActualPanX} and {@link #getActualPanY}) to account for the
     * size difference between the two Viewports. All other viewing state
     * settings are copied as is, except for the look-up table which will
     * retain its original setting if the specified ViewportState's look-up
     * table is <code>null</code> or incompatible with this ViewportState
     * (i.e., with a different number of bands.)
     *
     * @param vs <code>ViewportState</code> to match this ViewportState to.
     *		 If same as <code>this</code>, do nothing.
     *
     * @see <a href="#autoscale">Automatic scale factor adjustment</a>
     */
    public void match(ViewportState vs)
    {
	// Defend against self-copying
	if (vs == this) {
	    return;
	}


	// Match the scale factors according to auto scale options
	boolean toWidth = isAutoScaleProportionalToWidth();
	boolean toHeight = isAutoScaleProportionalToHeight();
	if (toWidth && toHeight) {
	    // Copy the one of the normalized X and Y factors that leads to
	    // smaller scale factors 
	    if (vs.getScaleX() / getScaleX() < vs.getScaleY() / getScaleY()) {
		_setScaleX(vs.getScaleX());
	    }
	    else {
		_setScaleY(vs.getScaleY());
	    }
	}
	else if (toWidth) {
	    // Copy the normalized X factor while maintaining the original
	    // pixel aspect ratio
	    _setScaleX(vs.getScaleX());
	}
	else if (toHeight) {
	    // Copy the normalized Y factor while maintaining the original
	    // pixel aspect ratio
	    _setScaleY(vs.getScaleY());
	}
	else {
	    // Copy *actual* scale factors as is
	    setActualScale(vs.getActualScaleX(), vs.getActualScaleY());
	}

	// Defend against null LUT and LUT w/ different # of bands
	if (vs._lut != null &&
	    (_lut == null ||
	     _lut.getNumComponents() == vs._lut.getNumComponents())) {
	    // Copy the LUT
	    _lut = (LookUpTable)vs._lut.clone();
	}

	// Copy the rest of the states
	setPan(vs._panX, vs._panY);
	_numberOfRotations = vs._numberOfRotations;
	_isVerticallyFlipped = vs._isVerticallyFlipped;
    }

    // ---------------
    // Private methods
    // ---------------

    /**
     * Returns the rectangle which defines the Viewport boundary in
     * Component Space.
     *
     * @return Rectangle which defines the Viewport boundary. The returned
     *	       value is a reference to the internal instance.
     */
    Rectangle _getBounds()
    {
	return _bounds;
    }

    /**
     * Sets the rectangle which defines the Viewport boundary in Component
     * Space, adjusting the scale factors according to the options set by
     * {@link #setAutoScaleOptions}.
     *
     * @param x New X coordinate of the upper-left corner of the Viewport.
     * @param y New Y coordinate of the upper-left corner of the Viewport.
     * @param width New width of the Viewport.
     * @param height New height of the Viewport.
     */
    void _setBounds(int x, int y, int width, int height)
    {
	// Check the bounding rectangle dimensions
	if (width <= 0 || height <= 0) {
	    throw new IllegalArgumentException("width and height cannot be " +
					       "zero or negative");
	}

	// Adjust scale factors only if size changes
	if (_bounds.width != width || _bounds.height != height) {

	    // Determine the ratio to adjust the scale factors
	    boolean toWidth = isAutoScaleProportionalToWidth();
	    boolean toHeight = isAutoScaleProportionalToHeight();
	    double ratio;
	    if (toWidth && toHeight) {
		// Proportionate scale factors to the minimum of the width and
		// height ratios
		ratio = Math.min((double)width / _bounds.width,
				 (double)height / _bounds.height);
	    }
	    else if (toWidth) {
		// Proportionate scale factors to width
		ratio = (double)width / _bounds.width;
	    }
	    else if (toHeight) {
		// Proportionate scale factors to height
		ratio = (double)height / _bounds.height;
	    }
	    else {
		// Do not adjust scale factors automatcially
		ratio = 1.0;
	    }


	    // Apply the same ratio to both scale factors to maintain pixel
	    // aspect ratio
	    double newScaleX = _scaleX * ratio;
	    double newScaleY = _scaleY * ratio;

	    if (isAutoScaleNoSmallerThanViewport()) {
		// Check if the image display would be smaller than Viewport
		int displayWidth, displayHeight;
		if ((getRotation() % 2) == 0) {
		    displayWidth = (int)(_imageSize.width * newScaleX);
		    displayHeight = (int)(_imageSize.height * newScaleY);
		}
		else {
		    displayWidth = (int)(_imageSize.width * newScaleX);
		    displayHeight = (int)(_imageSize.height * newScaleY);
		}

		// Cannot let displayWidth and displayHeight be 0
		displayWidth = Math.max(1, displayWidth);
		displayHeight = Math.max(1, displayHeight);

		if (displayWidth < width && displayHeight < height) {

		    // Scale factors that would fit the image to the Viewport
		    // and maintain the pixel aspect ratio
		    ratio = Math.min((double)width / displayWidth,
				     (double)height / displayHeight);
		    newScaleX *= ratio;
		    newScaleY *= ratio;

		}
	    }

	    // Put the new scale factors into effect
	    setActualScale(newScaleX, newScaleY);
	}

	// Put the new bounds into effect
	_bounds.setBounds(x, y, width, height);
    }

    /**
     * Sets the normalized X scale factor and proportionates the normalized
     * Y scale factor so that the original pixel aspect ratio is maintained.
     *
     * @param scaleX New normalized X scale factor.
     */
    void _setScaleX(double scaleX)
    {
	setScale(scaleX, getScaleY() * scaleX / getScaleX());
    }

    /**
     * Sets the normalized Y scale factor and proportionates the normalized
     * X scale factor so that the original pixel aspect ratio is maintained.
     *
     * @param scaleY New normalized Y scale factor.
     */
    void _setScaleY(double scaleY)
    {
	setScale(getScaleX() * scaleY / getScaleY(), scaleY);
    }
}
