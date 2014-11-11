/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.medtoolbox.jviewbox.LookUpTable;
import org.medtoolbox.jviewbox.viewport.annotation.Annotation;

/** 
 * Class which displays a single Image. Note that this class itself is
 * <b>not</b> a <code>JComponent</code> and may not be used directly in
 * Swing-based GUI applications. {@link ViewportCluster} and its
 * subclasses are for that purpose. States of the displayed image may be
 * saved and restored, and Annotations can be drawn over the displayed image.
 * <p>
 * This class supports any images in the form of <code>java.awt.Image</code>.
 * Because of the generality of <code>java.awt.Image</code>, rendering may not
 * be optimal and look-up operation is not supported (look-up tables settings
 * are saved and ignored.) Subclasses are expected to add functionalities and
 * may choose to support only images in some more specific formats.
 * <p>
 * Methods are provided to manipulate the display state which includes settings
 * of scale, pan, rotation (in multiple of 90 degrees), and flipping about the
 * vertical axis. These methods should be used to avoid direct manipulation of
 * the {@link ViewportState} object returned by {@link #getCurrentState}.
 * <p>
 * <a name="spaces">Four different <b>coordinate spaces</b> are involved in the
 * operation of a Viewport:</a>
 * <ul>
 * <li><i>Viewport Space</i> where the upper-left corner of this Viewport
 *     is the origin and the lower-right corner of this Viewport falls on
 *     <tt>(viewport_width - 1, viewport_height - 1)</tt>.
 * </li>
 * <li><i>Image Space</i> where the upper-left corner of the original image
 *     is the origin and the lower-right corner of the image falls on
 *     <tt>(image_width - 1, image_height - 1)</tt>. The
 *     <i>Image Transform</i>, returned by {@link #getImageTransform} maps
 *     this space into Viewport Space.
 * </li>
 * <li><i>Annotation Space</i> where the upper-left corner of this Viewport
 *     is the origin and the lower-right corner of this Viewport falls on
 *     <tt>(ANNOTATION_SPACE_SIZE - 1, ANNOTATION_SPACE_SIZE - 1)</tt>. This
 *     space is used to position annotations relative to the Viewport's bounds
 *     without knowing the actual Viewport dimension, which is subject to
 *     change at any time. The <i>Annotation Transform</i>, which is just a
 *     scaling, maps this space into Viewport Space. This transform may be
 *     retrieved by calling {@link #getAnnotationTransform}.
 * </li>
 * <li><i>Component Space</i> is the coordinate space of the actual AWT or
 *     Swing component that contains a Viewport, usually a
 *     {@link ViewportCluster}, where the upper-left corner of the
 *     AWT/Swing component is the origin. This space is where all drawing
 *     operations take place. The <i>Viewport Transform</i>, which is just a
 *     translation, maps Viewport Space into this space. This transform may be
 *     retrieved by calling {@link #getViewportTransform}.
 * </li>
 * </ul>
 *
 * @see ViewportCluster
 *
 * @version January 8, 2004
 */
public class Viewport
{
    // ---------
    // Constants
    // ---------

    /** Size of this Viewport in the Annotation Space. */
    public static final int ANNOTATION_SPACE_SIZE = 1024;

    /** Maximum number of Viewport states allowed on the history stack. */
    public static final int MAXIMUM_VIEWPORT_STATES_ALLOWED = 20;

    /**
     * Enumerated constant class for modes of interpolation.
     * A <code>null</code> always means no interpolation (or equivalently,
     * nearest-neighbor interpolation.)
     *
     * @since 2.0b
     */
    public static final class InterpolationMode
    {
	/** Name of the interpolation mode, for informational purpose only. */
	private final String _name;

	/**
	 * Constructor is declared <code>protected</code> to allow subclasses
	 * to add their own modes.
	 *
	 * @param Name of the interpolation mode. This is for informational
	 *	  purpose only.
	 */
	protected InterpolationMode(String name)
	{
	    _name = name;
	}

	/** Returns the name of this InterpolationMode. */
	public String toString()
	{
	    return _name;
	}
    }

    /**
     * Interpolation mode: bilinear.
     *
     * @since 2.0b
     */
    public static final InterpolationMode INTERPOLATION_BILINEAR =
	new InterpolationMode("BILINEAR");

    /**
     * Interpolation mode: bicubic.
     *
     * @since 2.0b
     */
    public static final InterpolationMode INTERPOLATION_BICUBIC =
	new InterpolationMode("BICUBIC");

    // --------------
    // Private fields
    // --------------

    /** Whether to erase background after image is painted (versus before). */
    private static boolean _postErasingBackground = false;

    /**
     * Interpolation mode. <code>null</code> means no interpolation, which
     * is the default.
     *
     * @since 2.0b
     */
    private static InterpolationMode _interpolationMode;

    /** Image that is displayed. */
    private Image _image;



    /** Previous and current viewing states of the image. */
    private final Vector _imageStates = new Vector(1, 1);

    /** Current viewing state of the Annotations on the image. */
    private final ViewportState _annotationState;

    /** Annotations on this Viewport. */
    private Vector _annotations = new Vector(1, 1);

    /** Whether annotations are enabled. */
    private boolean _annotationEnabled = true;


    /** Initial (first valid) state of this Viewport. */
    private ViewportState _initialState;

    /** Background color of this Viewport. */
    private Color _background = Color.black;

    // -----------
    // Constructor
    // -----------

    /**
     * Constructs a Viewport for the specified <code>Image</code>.
     *
     * @param image <code>Image</code> to be displayed.
     *
     * @throws NullPointerException if <code>image</code> is <code>null</code>.
     */
    public Viewport(Image image)
    {
	this();

	if (image == null) {
	    throw new NullPointerException("image cannot be null.");
	}
	_image = image;
    }

    /**
     * Constructs a Viewport for an image of the named width and height but
     * the image itself will have to be specified later.
     * <p>
     * This constructor is provided for derived classes only.
     *
     * @param imageWidth Width of the image in pixels that is displayed.
     * @param imageHeight Height of the image in pixels that is displayed.
     *
     * @deprecated As of jViewBox 2.0b, replaced by {@link #Viewport()}. The
     *		  width and height parameters have no meaning at all.
     */
    protected Viewport(int imageWidth, int imageHeight)
    {
	this();

    }

    /**
     * Constructs a Viewport whose image to display will have to be specified
     * later.
     * <p>
     * This constructor is provided for derived classes only.
     *
     * @since 2.0b
     */
    protected Viewport()
    {
	// Initialize the Annotation state. The Annotation Space is a logical
	// coordintate space where the rectangle (0,0)-(ANNOTATION_SPACE_SIZE,
	// ANNOTATION_SPACE_SIZE) always maps to the actual Viewport rectangle
	// (0,0)-(Viewport's width, Viewport's height) in Viewport
	// Space. The Annotation Space allows the positioning of annotations
	// relative to the Viewport bounds without knowing the actual dimension
	// of the Viewport, which is subject to change at any time.
	_annotationState =
	    new ViewportState(new Rectangle(ANNOTATION_SPACE_SIZE,
					    ANNOTATION_SPACE_SIZE),
			      new Point());
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the image in this Viewport.
     *
     * @return Image that is displayed in this Viewport.
     */
    public Image getImage()
    {
	return _image;
    }

    /**
     * Creates a What-You-See-Is-What-You-Get snapshot of this Viewport.
     * The output is scaled to fit the specified snapshot size, which may be
     * different from that of this Viewport.
     *
     * @param width Width of the snapshot.
     * @param height Height of the snapshot.
     * @param type Type of the <code>BufferedImage</code> used for the
     *		   snapshot, an <code>int</code> constant from the
     *		   <code>BufferedImage</code> class.
     *
     * @return <code>BufferedImage</code> that is a WYSIWYG snapshot of this
     *	       Viewport.
     *
     * @throws IllegalArgumentException if either <code>width</code> or
     *	       <code>height</code> is negative or zero, or <code>type</code>
     *	       is not supported by <code>BufferedImage</code>.
     */
    public BufferedImage createSnapshot(int width, int height, int type)
    {
	BufferedImage bufferedImage = new BufferedImage(width, height, type);
	getSnapshot(bufferedImage);

	return bufferedImage;
    }

    /**
     * Gets a What-You-See-Is-What-You-Get snapshot of this Viewport in the
     * supplied <code>BufferedImage</code>. The target BufferedImage may have
     * a size different from that of this Viewport, and the output is scaled to
     * fit the BufferedImage.
     * <p>
     * Implementation Note: This method is marked as <code>synchronized</code>
     * because it may interfere with the normal painting operation of this
     * Viewport to the screen invoked internally by Swing/AWT and hence must
     * not run simultaneously with <code>paint</code>.
     *
     * @param bufferedImage <code>BufferedImage</code> to which the snapshot is
     *			    output. The output will be scaled to fit the
     *			    BufferedImage.
     *
     * @throws NullPointerException if <code>bufferedImage</code> is
     *	       <code>null</code>.
     */
    public synchronized void getSnapshot(BufferedImage bufferedImage)
    {
	// Temporary make this Viewport to cover the entire BufferedImage
	Rectangle origBounds = getBounds();
	setBounds(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

	// Paint into the BufferedImage
	Graphics2D g2d = bufferedImage.createGraphics();
	g2d.setClip(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
	paint(g2d, true);
	g2d.dispose();

	// Restore this Viewport back to its original location and size
	setBounds(origBounds);
    }

    /**
     * Returns the current viewing state of the image in this Viewport.
     * Methods provided in this class, e.g., {@link #setScale}, should be used
     * to avoid direct manipultion of the returned <code>ViewportState</code>
     * for the regular settings of scale, pan, window/level, rotation, and
     * flipping. The returned value is a copy and modifying it has no direct
     * effect on this Viewport.
     * <p>
     * Note that all of the individual state manipulating methods, e.g.,
     * {@link #setScale} and {@link #setLut}, modify the current viewing state
     * <b>without</b> saving it.
     *
     * @return A copy of the current viewing state.
     *
     * @see #setCurrentState
     * @see #saveCurrentState
     */
    public ViewportState getCurrentState()
    {
	return (ViewportState)_getCurrentState().clone();
    }

    /**
     * Sets the current viewing state of the image in this Viewport. An
     * internal copy of the <code>state</code> parameter is made and used to
     * display the image. The original viewing state is automatically saved
     * on a stack and can be restored in the reverse order in which states
     * were saved. The size of the stack is fixed; saving a state on a full
     * stack will result in the bottom-most state (i.e., the oldest) being
     * removed from the stack.
     * <p>
     * Note that all of the individual state manipulating methods, e.g.,
     * {@link #setScale} and {@link #setLut}, modify the current viewing state
     * <b>without</b> saving it.
     * <p>
     * The {@link ViewportState} specified by the <code>state</code>
     * parameter is applied as is, which is why this method should be
     * generally <b>avoided</b>. It does not take into considertaion the
     * differences between the sizes and aspect ratios of the two Viewports and
     * their images and may cause the image display to be distorted. Moreover,
     * the <code>LookUpTable</code> setting is overwritten without checking if
     * it will become <code>null</code> or otherwise incompatible (e.g.,
     * having different number of bands in the tables) with this Viewport,
     * which may cause the look-up operation to fail.
     * <p>
     * To save the current state, {@link #saveCurrentState} should be used
     * instead of this method. To copy the state from another Viewport,
     * {@link #copyStateFrom(Viewport)} should be used in place of this method.
     *
     * @param state The viewing state to be made the current state.
     *
     * @see #getCurrentState
     * @see #saveCurrentState
     * @see #copyStateFrom(Viewport)
     * @see #copyStateFrom(ViewportState)
     */
    public synchronized void setCurrentState(ViewportState state)
    {
	// Defensive copy
	state = (ViewportState)state.clone();

	// Validate the state
	state.validate(this);

	// Apply the current Viewport bounds
	state._getBounds().setBounds(getBounds());

	// Apply the new state
	_setCurrentState(state);
    }

    /**
     * Saves the current viewing state by making a copy of it to be used
     * as the new viewing state and then saving it to the state stack. This
     * is a convenient method for making a "check point" in the state stack
     * before modifying the viewing state using methods like {@link #setScale}
     * and {@link #setLut} which do <b>not</b> save the state.
     *
     * @see #setCurrentState
     *
     * @since 2.0b
     */
    public void saveCurrentState()
    {
	_setCurrentState(getCurrentState());
    }

    /**
     * Restores the previous viewing state of the image in this Viewport.
     * The current state is deleted and replaced with the state that was
     * saved before it. If there is no previous state to restore, this
     * method does nothing.
     */
    public synchronized void restorePreviousState()
    {
	// Is there any previous state?
	if (_imageStates.size() <= 1) {
	    return;
	}

	// Save the original Viewport bounds
	Rectangle bounds = getBounds();

	// Remove the current (last) state if there is one previous state
	_imageStates.remove(_imageStates.size() - 1);

	// Restore the Viewport bounds
	setBounds(bounds);
    }

    /**
     * Resets the viewing state to the initial value of this Viewport.
     * This does not clear the state history but rather appends one more
     * state (the initial state) to it.
     * <p>
     * The initial state is defined as the state right after the very first
     * time <code>paint</code> is called. No initial state is available before
     * that, and calling this method does nothing.
     */
    public synchronized void resetToInitialState()
    {
	if (_initialState != null) {
	    // Clone the initial state and apply the current bounds
	    ViewportState vs = (ViewportState)_initialState.clone();
	    vs._setBounds(getX(), getY(), getWidth(), getHeight());
	    _setCurrentState(vs);
	}
    }

    /**
     * Copies the viewing state of the specified Viewport to this Viewport.
     * The original state is saved on the stack.
     * <p>
     * The copying of viewing state is performed by {@link ViewportState#match}
     * which tries to match the states to make the images in the two Viewports
     * "look similar." It also tries not to copy an incompatible look-up table
     * setting which may cause errors in this Viewport's rendering. See the
     * documentation of {@link ViewportState#match} for details.
     *
     * @param vp <code>Viewport</code> to copy the viewing state from.
     *		 If same as <code>this</code>, do nothing.
     *
     * @see #setCurrentState
     * @see #copyStateFrom(ViewportState)
     * @see ViewportState#match
     */
    public synchronized void copyStateFrom(Viewport vp)
    {
	// Defend against self-copying
	if (vp == this) {
	    return;
	}
	copyStateFrom(vp._getCurrentState());
    }

    /**
     * Copies the viewing state of the specified ViewportState to this
     * Viewport. The original state is saved on the stack.
     * <p>
     * The copying of viewing state is performed by {@link ViewportState#match}
     * which tries to match the states to make the images in the two Viewports
     * "look similar." It also tries not to copy an incompatible look-up table
     * setting which may cause errors in this Viewport's rendering. See the
     * documentation of {@link ViewportState#match} for details.
     *
     * @param state <code>ViewportState</code> to copy the viewing state from.
     *
     * @see #setCurrentState
     * @see #copyStateFrom(Viewport)
     * @see ViewportState#match
     */
    public synchronized void copyStateFrom(ViewportState state)
    {
	ViewportState vs = getCurrentState();
	vs.match(state);
	_setCurrentState(vs);
    }

    /**
     * Returns the X coordinate of the upper left Viewport corner in
     * Component Space.
     *
     * @return The number of pixels the upper left corner of this Viewport is
     *         from the origin along the X direction.
     *
     * @see #getBounds()
     */
    public int getX()
    {
	return _getCurrentState()._getBounds().x;
    }

    /**
     * Returns the Y coordinate of the upper left Viewport corner in
     * Component Space.
     *
     * @return The number of pixels the upper left corner of this Viewport is
     *         from the origin along the Y direction.
     *
     * @see #getBounds()
     */
    public int getY()
    {
	return _getCurrentState()._getBounds().y;
    }

    /**
     * Returns the width of this Viewport in Component Space.
     *
     * @return The width of this Viewport in pixels.
     *
     * @see #getBounds()
     */
    public int getWidth()
    {
	return _getCurrentState()._getBounds().width;
    }

    /**
     * Returns the height of this Viewport in Component Space.
     *
     * @return The height of this Viewport in pixels.
     *
     * @see #getBounds()
     */
    public int getHeight()
    {
	return _getCurrentState()._getBounds().height;
    }

    /**
     * Returns the size of this Viewport, in the Component Space.
     *
     * @return Size of this Viewport in pixels.
     *
     * @see #getBounds()
     */
    public Dimension getSize()
    {
	return getSize(null);
    }

    /**
     * Returns the size of this Viewport in Component Space.
     *
     * @param rv <code>Dimension</code> object where to store the size of this
     *		 Viewport.
     *
     * @return <code>rv</code> if <code>rv</code> is not <code>null</code>;
     *	       otherwise a new instance of <code>Dimension</code>.
     *
     * @see #getBounds()
     */
    public Dimension getSize(Dimension rv)
    {
	// Allocate a new object if rv is null
	if (rv == null) {
	    rv = new Dimension();
	}
	rv.setSize(_getCurrentState()._getBounds().getSize());

	return rv;
    }

    /**
     * Returns the bounding rectangle of this Viewport in Component Space.
     *
     * @return Bounding rectangle of this Viewport.
     *
     * @see #getBounds(Rectangle)
     * @see #setBounds(int, int, int, int)
     */
    public Rectangle getBounds()
    {
	return getBounds(null);
    }

    /**
     * Returns the bounding rectangle of this Viewport in Component Space.
     *
     * @param rv <code>Rectangle</code> object where to store the bounding
     *		 rectangle of this Viewport.
     *
     * @return <code>rv</code> if <code>rv</code> is not <code>null</code>;
     *	       otherwise a new instance of <code>Rectangle</code>.
     */
    public Rectangle getBounds(Rectangle rv)
    {
	// Allocate a new object if rv is null
	if (rv == null) {
	    rv = new Rectangle();
	}
	rv.setBounds(_getCurrentState()._getBounds());

	return rv;
    }

    /**
     * Moves and resizes this Viewport. The current aspect ratio of the
     * displayed image will be maintained by adjusting scaling factors
     * according to the options set by
     * {@link ViewportState#setAutoScaleOptions}.
     *
     * @param bounds Rectangle defining the new boundary for this Viewport.
     *
     * @throws IllegalArgumentException if the boundary width and height are
     *                                  invalid.
     *
     * @see #setBounds(int, int, int, int)
     */
    public void setBounds(Rectangle bounds)
    {
	setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Moves and resizes this Viewport. The current aspect ratio of the
     * displayed image will be maintained by adjusting scaling factors
     * according to the options set by
     * {@link ViewportState#setAutoScaleOptions}.
     *
     * @param x New X coordinate of the upper-left corner of this Viewport.
     * @param y New Y coordinate of the upper-left corner of this Viewport.
     * @param width New width of this Viewport.
     * @param height New height of this Viewport.
     *
     * @throws IllegalArgumentException if the boundary width and height are
     *                                  invalid.
     */
    public void setBounds(int x, int y, int width, int height)
    {
	_getCurrentState()._setBounds(x, y, width, height);
    }

    /**
     * Gets the scale factor along the X direction that is applied to the
     * image.
     * <p>
     * The scale factors may be automatically adjusted whenever the Viewport
     * dimension changes. Use {@link ViewportState#setAutoScaleOptions} to
     * control how scale factors are adjusted.
     *
     * @return Scale factor along the X direction that is applied to the
     *	       image.
     *
     * @see #setScale
     */
    public double getScaleX()
    {
	return _getCurrentState().getActualScaleX();
    }

    /**
     * Gets the scale factor along the Y direction that is applied to the
     * image.
     * <p>
     * The scale factors may be automatically adjusted whenever the Viewport
     * dimension changes. Use {@link ViewportState#setAutoScaleOptions} to
     * control how scale factors are adjusted.
     *
     * @return The scale ratio along the Y direction of the transformed
     *         image height to the original image height.
     *
     * @see #setScale
     */
    public double getScaleY()
    {
	return _getCurrentState().getActualScaleY();
    }

    /**
     * Sets the scale factors along the X and Y directions that are applied to
     * the image.
     * <p>
     * The scale factors may be automatically adjusted whenever the Viewport
     * dimension changes. Use {@link ViewportState#setAutoScaleOptions} to
     * control how scale factors are adjusted.
     *
     * @param scaleX Scale factor along the X direction that is applied to the
     *		     image.
     * @param scaleY Scale factor along the Y direction that is applied to the
     *		     image.
     *
     * @see #getScaleX
     * @see #getScaleY
     */
    public void setScale(double scaleX, double scaleY)
    {
	_getCurrentState().setActualScale(scaleX, scaleY);
    }

    /**
     * Gets the distance along X that the image is panned, measured in pixels
     * in Viewport Space. This value is subject to change whenever the
     * Viewport dimension changes.
     *
     * @return The number of pixels along the X direction the upper left corner
     *         of the transformed image is from the upper left corner of the
     *         original image.
     *
     * @see #setPan
     */
    public int getPanX()
    {
	return (int)Math.rint(_getCurrentState().getActualPanX());
    }

    /**
     * Gets the distance along Y that the image is panned, measured in pixels
     * in Viewport Space. This value is subject to change whenever the
     * Viewport dimension changes.
     *
     * @return The number of pixels along the Y direction the upper left corner
     *         of the transformed image is from the upper left corner of the
     *         original image.
     *
     * @see #setPan
     */
    public int getPanY()
    {
	return (int)Math.rint(_getCurrentState().getActualPanY());
    }

    /**
     * Sets the distance along X and Y that the image is panned, in pixels
     * in Viewport Space.
     *
     * @param panX The number of pixels along the X direction the upper left
     *             corner of the transformed image is from the upper left
     *             corner of the original image.
     * @param panY The number of pixels along the Y direction the upper left
     *             corner of the transformed image is from the upper left
     *             corner of the original image.
     *
     * @see #getPanX
     * @see #getPanY
     */
    public void setPan(int panX, int panY)
    {
	_getCurrentState().setActualPan(panX, panY);
    }

    /**
     * Gets the number of times the image is rotated 90 degrees clockwise.
     *
     * @return The number of times (0,1,2,3) the transformed image has been
     *         rotated 90 degrees clockwise about its center.
     *
     * @see #setRotation
     */
    public int getRotation()
    {
	return _getCurrentState().getRotation();
    }

    /**
     * Sets the number of times the image is rotated 90 degrees clockwise.
     *
     * @param numberOfRotations The number of times to rotate the transformed
     *                          image 90 degrees clockwise about its center.
     *
     * @see #getRotation
     */
    public void setRotation(int numberOfRotations)
    {
	// If the rotation switches X and Y axes an odd number of times
	if (Math.abs(getRotation() - numberOfRotations) % 2 == 1) {

	    // Switch the X and Y scale factors
	    setScale(getScaleY(), getScaleX());
	}

	_getCurrentState().setRotation(numberOfRotations);
    }

    /**
     * Determines whether or not the image has been vertically flipped, i.e.,
     * flipped about the vertical (Y) axis.
     *
     * @return <code>true</code> if the transformed image is flipped about the
     *	       line which runs vertically through its center;
     *	       <code>false</code> otherwise.
     *
     * @see #setVerticallyFlipped
     */
    public boolean isVerticallyFlipped()
    {
	return _getCurrentState().isVerticallyFlipped();
    }

    /**
     * Sets the vertical flip (i.e., flip about the vertical, or Y, axis)
     * applied to the image.
     *
     * @param verticalFlip <code>true</code> to flip the transformed
     *                     image about the line which runs vertically through
     *                     its center.
     *
     * @see #isVerticallyFlipped
     */
    public void setVerticallyFlipped(boolean isVerticallyFlipped)
    {
	_getCurrentState().setVerticallyFlipped(isVerticallyFlipped);
    }

    /**
     * Returns <code>Image Transform</code>, the transform currently applied
     * to the image to map it into Viewport Space.
     *
     * @return <code>AffineTransform</code> that is used to transform the
     *         image into Viewport Space.
     *
     * @see <a href='#spaces'>Definition of coordinate spaces</a>
     */
    public AffineTransform getImageTransform()
    {
	return _getCurrentState().createTransform();
    }

    /**
     * Returns <code>Annotation Transform</code> which maps Annotation Space
     * into Viewport Space.
     *
     * @return <code>AffineTransform</code> that is used to map Annotation
     *	       Space into Viewport Space.
     *
     * @see <a href='#spaces'>Definition of coordinate spaces</a>
     */
    public AffineTransform getAnnotationTransform()
    {
	// Scale the annotation space to match the size of this Viewport
	_annotationState.validate(this);
	_annotationState.setActualScale(
	    (double)getWidth() / ANNOTATION_SPACE_SIZE,
	    (double)getHeight() / ANNOTATION_SPACE_SIZE);
	return _annotationState.createTransform();
    }

    /**
     * Returns <code>Viewport Transform</code> which maps Viewport Space into
     * Component Space.
     *
     * @return <code>AffineTransform</code> that maps Viewport Space into
     *	       Component Space.
     *
     * @see <a href='#spaces'>Definition of coordinate spaces</a>
     */
    public AffineTransform getViewportTransform()
    {
	// Translate the origin of the transform to the Viewport origin
	return AffineTransform.getTranslateInstance(getX(), getY());
    }

    /**
     * Gets the look-up table (LUT) applied to the image.
     *
     * @return Look-up table used to map each image pixel value to an 8-bit
     *         display value, or <code>null</code> if it does not exist.
     *
     * @see #setLut
     */
    public LookUpTable getLut()
    {
	return _getCurrentState().getLut();
    }

    /**
     * Sets the look-up table (LUT) applied to the image.
     *
     * @param lut Look-up table used to map each image pixel value to an
     *            8-bit display value.
     *
     * @see #getLut
     */
    public void setLut(LookUpTable lut)
    {
	_getCurrentState().setLut(lut);
    }

    /**
     * Adds an Annotation to this Viewport.
     * <p>
     * Annotations are drawn over the image in the order they were added to
     * this Viewport, which is the same order they will be in the list
     * returned by {@link #getAnnotations}. An annotation drawn later will
     * appear to be above another drawn earlier.
     *
     * @param annotation Annotation to be displayed in this Viewport.
     *
     * @throws NullPointerException if <code>annotation</code>is
     *	       <code>null</code>.
     *
     * @see #removeAnnotation
     * @see #getAnnotations
     */
    public void addAnnotation(Annotation annotation)
    {
	if (annotation == null) {
	    throw new NullPointerException("annotation can not be null.");
	}

	_annotations.add(annotation);
    }

    /**
     * Removes an Annotation from this Viewport.
     *
     * @param annotation Annotation to be removed from this Viewport.
     *
     * @see #addAnnotation
     * @see #getAnnotations
     */
    public void removeAnnotation(Annotation annotation)
    {
	_annotations.remove(annotation);
    }

    /**
     * Returns an unmodifiable <code>List</code> of all the Annotations in this
     * Viewport. The order of the Annotations in the list is the same as the
     * order they were added, which is also the order they are drawn over the
     * image.
     *
     * @return All the Annotations in this Viewport in an unmodifiable
     *	       <code>List</code>.
     *
     * @see #addAnnotation
     * @see #removeAnnotation
     */
    public List getAnnotations()
    {
	return Collections.unmodifiableList(_annotations);
    }

    /**
     * Enables/disables annotations in this Viewport. An annotation is
     * displayed only if annotations are enabled in this Viewport and the
     * annotation itself is enabled.
     *
     * @param enabled <code>true</code> to enable annotations in this Viewport;
     *		      <code>false</code> to disable.
     *
     * @see org.medtoolbox.jviewbox.viewport.annotation.Annotation#setEnabled
     * @see #isAnnotationEnabled
     */
    public void setAnnotationEnabled(boolean enabled)
    {
	_annotationEnabled = enabled;
    }

    /**
     * Returns whether annotations are enabled in this Viewport. An annotation
     * is displayed only if annotations are enabled in this Viewport and the
     * annotation itself is enabled.
     *
     * @return <code>true</code> if annotations are enabled in this Viewport;
     *	       <code>false</code> if disabled.
     *
     * @see org.medtoolbox.jviewbox.viewport.annotation.Annotation#isEnabled
     * @see #setAnnotationEnabled
     */
    public boolean isAnnotationEnabled()
    {
	return _annotationEnabled;
    }

    /**
     * Flushes all the resources being used by this Viewport. This includes
     * any cached data for rendering to the screen and any system resources
     * that are being used to store the image pixel data.
     *
     * @see #flushDisplayOnly
     */
    public synchronized void flush()
    {
	if (_image != null) {
	    _image.flush();
	}
    }

    /**
     * Flushes only the resources being used by this Viewport to display the
     * image. This includes any cached data for rendering to the screen but
     * does not include any system resources that are being used to store the
     * image pixel data.
     *
     * @see #flush
     */
    public void flushDisplayOnly()
    {
    }

    /** 
     * Paints this Viewport. The internal states of the <code>Graphics2D</code>
     * object passed in <b>MUST</b> be preserved, i.e., saved and restored if
     * changes are to be made. The states includes (but are not limited to)
     * the current color, font, clip, and transform (translation, scale, etc.)
     * <p>
     * In this implementation, this method simply invokes, in the specified
     * order:
     * <ol>
     * <li><code>_preEraseBackground</code> if requested to erase background
     *     and the <code>PostErasingBackground</code> option is diabled;
     * </li>
     * <li><code>_paintImage</code> to paint the image;
     *  </li>
     * <li><code>_postEraseBackground</code> if requested to erase background
     *     and the <code>PostErasingBackground</code> option is enabled;
     * </li>
     * <li><code>_paintAnnotations</code> to paint the annotations if they are
     *     enabled.
     * </li></ol>
     * Subclass are encouraged to override just the relevant sub-paint methods
     * in order to provide performance optimization or additional
     * functionality. If this method is to be overriden, the subclass should
     * take care of background erasing if requested, image painting, and
     * annotation painting if enabled.
     *
     * @param g2d Graphics context used for painting.
     * @param toEraseBackground Whether this method should erase the background
     *				while painting.
     *
     * @see #_paintImage
     * @see #_paintAnnotations
     * @see #_preEraseBackground
     * @see #_postEraseBackground
     * @see #setPostErasingBackground
     */
    public synchronized void paint(Graphics2D g2d, boolean toEraseBackground)
    {
	// Erase background before painting image?
	if (toEraseBackground && !isPostErasingBackground()) {
	    _preEraseBackground(g2d);
	}

	// Paint the image
	try {
	    _paintImage(g2d);
	}
	catch (RuntimeException e) {
	    // Show an error message at the center of this Viewport
	    int vpCenterX = getX() + getWidth() / 2;
	    int vpCenterY = getY() + getHeight() / 2;
	    FontMetrics metric = g2d.getFontMetrics();
	    Rectangle2D bounds = 
		metric.getStringBounds("Internal Rendering Error", g2d);
	    if (toEraseBackground) {
		_preEraseBackground(g2d);
	    }
	    Color c = g2d.getColor();
	    g2d.setColor(Color.white);
	    g2d.drawString("Internal Rendering Error",
			   vpCenterX - (int)bounds.getCenterX(),
			   vpCenterY - (int)bounds.getCenterY());
	    g2d.setColor(c);

	    // Bail out
	    e.printStackTrace();
	    return;
	}

	// Erase background after painting image?
	if (toEraseBackground && isPostErasingBackground()) {
	    _postEraseBackground(g2d);
	}

	if (isAnnotationEnabled()) {
	    _paintAnnotations(g2d);
	}

	// Sets initial state if not yet done so
	if (_initialState == null) {
	    _initialState = (ViewportState)_getCurrentState().clone();
	}
    }

    /**
     * Sets in <code>paint</code> whether to erase background, when requested
     * to do so, after image is painted (versus before image is painted). This
     * setting applies to <b>ALL</b> <code>Viewport</code>s. The default is
     * <code>false</code>, i.e., to erase background before image is painted.
     * <p>
     * <code>ViewportCluster</code> normally does not ask Viewports to erase
     * the background for it takes care of that itself. The only exception is
     * when <code>repaint(Viewport)</code> is called and the
     * <code>setBypassingRepaintManager</code> option is enabled. In that case,
     * erasing the enitre background beforehand may cause serious flickering
     * because Swing's double buffering is bypassed by the same option. This
     * option may help alleviate the flickering, and is recommended only to
     * be used in conjuction with the <code>BypassingRepaintManager</code>
     * option.
     * <p>
     * <b>Note</b> that to erase the background after image is painted, it is
     * assumed that the image bounds after affine transformation are still a
     * rectangle parallel to both X and Y axes. Regions outside of that
     * rectangle are erased without affecting the image. If this assumption is
     * not true, e.g., as in the rare case of an affine transform of arbitrary
     * rotation (non-90 degree rotation is possible only by direct
     * manipulation of {@link ViewportState}), Viewport may fail to cover all
     * the background, resulting in residuals and artifacts.
     *
     * @param postErasingBackground <code>true</code> to erase background after
     *				    image is painted; <code>false</code> to
     *				    do so beforehand.
     *
     * @see #isPostErasingBackground
     * @see ViewportCluster#repaint(Viewport)
     * @see ViewportCluster#setBypassingRepaintManager
     */
    public static void setPostErasingBackground(boolean postErasingBackground)
    {
	_postErasingBackground = postErasingBackground;
    }

    /**
     * Returns in <code>paint</code> whether to erase background, when
     * requested to do so, after image is painted (versus before that). This
     * setting applies to <b>ALL</b> <code>Viewport</code>s.
     *
     * @return <code>true</code> if background is erased after image is
     *	       painted; <code>false</code> if background is erased before
     *	       that.
     *
     * @see #setPostErasingBackground
     */
    public static boolean isPostErasingBackground()
    {
	return _postErasingBackground;
    }

    /**
     * Sets the mode of interpolation for image rendering. The default is
     * <code>null</code>, i.e., to use nearest-neighbor interpolation. The same
     * setting applies to <b>ALL</b> <code>Viewport</code>s and supposedly
     * its subclasses.
     * <p>
     * <b>Notice</b> that interpolation is <b>NOT</b> guaranteed. This is
     * because there is no known way of forcing
     * <code>Graphics2D.drawImage</code> to perform certain type of
     * interpolation when an image undergoes affine transformation, only to
     * suggest it by using <code>RenderingHints</code>. Subclasses, e.g.,
     * {@link ViewBoxViewport}, may be able to ensure the application of
     * certain (but not necessarily all) modes of interpolation by performing
     * image transformation themselves.
     * <p>
     * Subclasses are also permitted to add their own interpolation modes and
     * corresponding {@link InterpolationMode} constants. None of these 
     * additional <code>InterpolationMode</code> constants will be recognized
     * by the base class and they may be taken as <code>null</code>.
     *
     * @param mode Mode of interpolation for image rendering;
     *		   <code>null</code> to disable interpolation (or say to
     *		   use nearest-neighbor interpolation.)
     *
     * @see #getInterpolationMode
     */
    public static void setInterpolationMode(InterpolationMode mode)
    {
	_interpolationMode = mode;
    }

    /**
     * Returns the mode of interpolation for image rendering. The same
     * setting applies to <b>ALL</b> <code>Viewport</code>s and supposedly
     * its subclasses.
     *
     * @return Mode of interpolation for image rendering;
     *	       <code>null</code> if no interpolation (or equivalently,
     *	       nearest-neighbor interpolation) is performed.
     *
     * @see #setInterpolationMode
     */
    public static InterpolationMode getInterpolationMode()
    {
	return _interpolationMode;
    }

    // -----------------
    // Protected methods
    // -----------------

    // Image dimension

    /**
     * Returns the (original) width of the image to be displayed. This is
     * simply synonym of <code>getImage().getWidth(null)</code>. Subclasses are
     * encouraged to override this method if they handle images differently.
     */
    protected int _getImageWidth()
    {
	return getImage().getWidth(null);
    }

    /**
     * Returns the (original) height of the image to be displayed. This is
     * simply synonym of <code>getImage().getHeight(null)</code>. Subclasses
     * are encouraged to override this method if they handle images
     * differently.
     */
    protected int _getImageHeight()
    {
	return getImage().getHeight(null);
    }

    // Paint methods

    /**
     * Paints the image in this Viewport. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.)
     *
     * @param g2d Graphics context used for painting.
     * @param toEraseBackground Whether this method should erase the background
     *				while painting.
     */
    protected void _paintImage(Graphics2D g2d)
    {
	// Get Image and Viewport Transforms
	AffineTransform it = getImageTransform();
	AffineTransform vt = getViewportTransform();

	// Pre-concatenate vt to it to form the final transform
	it.preConcatenate(vt);

	// Paint the image
	// *** WORK-AROUND TO JAVA BUG -- START
	// *** BUG ID #4364215 IN SUN'S BUG DATABASE
	// *** DRAW A LINE OUTSIDE THE CLIP BOUNDS
	Rectangle bounds = _getCurrentState()._getBounds();
	g2d.drawLine(bounds.x + bounds.width, bounds.y + bounds.height,
		     bounds.x + bounds.width, bounds.y + bounds.height);
	// *** WORK-AROUND TO JAVA BUG -- END

	// Apply interpolation RenderingHints if necessary
	Object origInterpolation =
	    g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
	InterpolationMode mode = getInterpolationMode();
	if (mode == INTERPOLATION_BILINEAR) {
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				 RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}
	else if (mode == INTERPOLATION_BICUBIC) {
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				 RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}

	g2d.drawImage(getImage(), it, null);

	// Restore interpolation RenderingHints
	if (origInterpolation != null) {
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				 origInterpolation);
	}
    }

    /**
     * Paints the annotations in this Viewport. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.)
     *
     * @param g2d Graphics context used for painting.
     */
    protected void _paintAnnotations(Graphics2D g2d)
    {
	// Get Image, Annotation, and Viewport Transforms
	AffineTransform it = getImageTransform();
	AffineTransform at = getAnnotationTransform();
	AffineTransform vt = getViewportTransform();

	// Let annotations paint themselves
	for (Iterator iter = getAnnotations().iterator(); iter.hasNext(); ) {
	    Annotation ann = (Annotation)iter.next();
	    if (ann.isEnabled()) {
		try {
		    ann.paint(g2d, it, at, vt);
		}
		catch (RuntimeException e) {
		    // Fail quietly
		}
	    }
	}
    }

    /**
     * Erases the background of this Viewport before the image is painted.
     * Since this takes place before all other painting activities, it is
     * safe and usually preferred to simply erase the entire Viewport.
     * The internal states of the <code>Graphics2D</code> object passed in
     * <b>MUST</b> be preserved, i.e., saved and restored if changes are to
     * be made. The states includes (but are not limited to) the current color,
     * font, clip, and transform (translation, scale, etc.)
     *
     * @param g2d Graphics context used for painting.
     */
    protected void _preEraseBackground(Graphics2D g2d)
    {
	Color c = g2d.getBackground();
	g2d.setBackground(_background);
	g2d.clearRect(getX(), getY(), getWidth(), getHeight());
	g2d.setBackground(c);
    }

    /**
     * Calculates the background regions in this Viewport (i.e., regions not
     * covered by the image) and erases them. The internal states of the
     * <code>Graphics2D</code> object passed in <b>MUST</b> be preserved, i.e.,
     * saved and restored if changes are to be made. The states includes (but
     * are not limited to) the current color, font, clip, and transform
     * (translation, scale, etc.)
     * <p>
     * Note that this implementation assumes the real image bounds, after
     * transformed into Component Space, are a rectangle parallel to both X
     * and Y axes. If not, the background erasing will only erase what is
     * outside the real image bounds' bounding rectangle.
     *
     * @param g2d Graphics context used for painting.
     */
    protected void _postEraseBackground(Graphics2D g2d)
    {

	// Calculate the bounding rectangle of the displayed image, cropped
	// by the Viewport's bounds
	Rectangle viewportBounds = getBounds();
	Rectangle imageRect = new Rectangle(_getImageWidth(),
					    _getImageHeight());
	Rectangle imageBounds =
	    getImageTransform().createTransformedShape(imageRect).getBounds();
	imageBounds.translate(viewportBounds.x, viewportBounds.y);
	imageBounds = viewportBounds.intersection(imageBounds);

	// Prepare Graphics2D
	Color c = g2d.getBackground();
	g2d.setBackground(_background);

	// Image is at least partially inside the viewport
	if (imageBounds.width != 0 && imageBounds.height != 0) {
	    // Erase regions outside the image
	    // Top
	    if (imageBounds.y > viewportBounds.y) {
		g2d.clearRect(viewportBounds.x,
			      viewportBounds.y,
			      viewportBounds.width,
			      imageBounds.y - viewportBounds.y);
	    }
	    // Bottom
	    if ((viewportBounds.y + viewportBounds.height) >
		(imageBounds.y + imageBounds.height)) {
		g2d.clearRect(viewportBounds.x,
			      imageBounds.y + imageBounds.height,
			      viewportBounds.width,
			      (viewportBounds.y + viewportBounds.height) -
			      (imageBounds.y + imageBounds.height));
	    }
	    // Left
	    if (imageBounds.x > viewportBounds.x) {
		g2d.clearRect(viewportBounds.x,
			      imageBounds.y,
			      imageBounds.x - viewportBounds.x,
			      imageBounds.height);
	    }
	    // Right
	    if ((viewportBounds.x + viewportBounds.width) >
		(imageBounds.x + imageBounds.width)) {
		g2d.clearRect(imageBounds.x + imageBounds.width,
			      imageBounds.y,
			      (viewportBounds.x + viewportBounds.width) -
			      (imageBounds.x + imageBounds.width),
			      imageBounds.height);
	    }
	}

	// Image is completely outside the viewport
	else {
	    // Erase the entire viewport
	    g2d.clearRect(viewportBounds.x, viewportBounds.y,
			  viewportBounds.width, viewportBounds.height);
	}

	// Restore Graphics2D
	g2d.setBackground(c);
    }

    // Other protected methods

    /**
     * Gets the current viewing state of the image in this Viewport. The
     * returned value is <b>not</b> a clone and modifying it will affect
     * this Viewport.
     *
     * @return The current viewing state.
     */
    protected ViewportState _getCurrentState()
    {
	// If the stack is empty, create a new state
	ViewportState vs;
	if ( _imageStates.isEmpty() ) {
	    vs = new ViewportState();
	    _imageStates.add(vs);
	}
	else {
	    vs = (ViewportState)_imageStates.lastElement();
	}

	// Validate the state against this Viewport
	vs.validate(this);

	return vs;
    }

    /**
     * Sets the current viewing state of the image in this Viewport. The
     * <code>state</code> parameter is not cloned and is applied as is. The
     * original state is saved on the stack. If the stack is full, the bottom
     * (oldest) state is deleted.
     *
     * @param state New viewing state to apply.
     */
    protected void _setCurrentState(ViewportState state)
    {
	// Make state the current state
	_imageStates.add(state);

	// Flush the previous state's LUT if applicable
	if (_imageStates.size() > 1) {
	    ViewportState prevState =
		(ViewportState)_imageStates.get(_imageStates.size() - 2);
	    LookUpTable lut = prevState.getLut();
	    if (lut != null) {
		lut.flush();
	    }
	}

	// If the stack is too large, remove the first element
	if (_imageStates.size() > MAXIMUM_VIEWPORT_STATES_ALLOWED) {
	    _imageStates.remove(0);
	}
    }
}
