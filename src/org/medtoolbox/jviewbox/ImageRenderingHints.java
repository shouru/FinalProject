/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Interface for providing additional information to jViewBox on how better
 * to render an image. Attributes defined in this interface are considered
 * supplementary to those provided by <code>ImageSource.getSampleModel</code>
 * and <code>ImageSource.getColorModel</code>.
 * <p>
 * Most of the attributes returned by the <code>get*</code> methods in this
 * interface originate from the <code>javax_imageio_1.0</code> standard
 * (plug-in neutral) metadata format specifiction defined in the Java Image
 * I/O API in Java 1.4. Currently, the only exception is the default look-up
 * table. See {@link ImageRenderingHintsFactory} for more information on how
 * <code>IIOMetadata</code> trees can be parsed to create a set of hints.
 * <p>
 * This interface is designed to be general while tailors to the specific
 * needs of the implementation of jViewBox's rendering pipeline. Not all
 * information available in the <code>javax_imageio_1.0</code> metadata format
 * is included, and not all combinations of the attributes returned by this
 * interface affect the rendering process.
 * <p>
 * Specifically, jViewBox relies on information provided by this interface
 * for the following purpose:
 * <ul>
 * <li>To better recognize grayscale images.<br>
 *     See {@link org.medtoolbox.jviewbox.viewport.ViewportFactory} for more
 *     information on how grayscale images may be detected and how specialized
 *     <code>ViewBoxViewport</code>s are created for them.</li>
 * <li>To support an initial look-up table.<br>
 *     <code>ViewBoxViewport</code>s will apply the default look-up table
 *     returned by {@link #getDefaultLookUpTable}, if available, when look-up
 *     operation is enabled. This initial look-up table is used to
 *     optimize the initial display of grayscale images, especially those
 *     of bit depths greater than 8.</li>
 * <li>To support atypical pixel aspect ratio and image orientation.<br>
 *     If an image has a pixel aspect ratio other than 1:1 or a special
 *     orientation (e.g., rotated 90 degrees, flipped), 
 *     <code>ImageSourceViewport</code> will adjust its initial state (scale
 *     factors for the pixel aspect ratio and rotation/flipping for the
 *     orientation) to reflect those preferences at the construction time.</li>
 * </ul>
 *
 * @see ImageRenderingHintsFactory
 * @see org.medtoolbox.jviewbox.imagesource.ImageSource
 * @see org.medtoolbox.jviewbox.viewport.ViewportFactory
 * @see org.medtoolbox.jviewbox.viewport.ImageSourceViewport
 * @see org.medtoolbox.jviewbox.viewport.ViewBoxViewport
 *
 * @version January 8, 2004
 */
public interface ImageRenderingHints
{
    // ---------
    // Constants
    // ---------



    //
    // Image orientation
    //

    /**
     * Image orientation: normal.
     * The string value is the same as <code>javax_imageio_1.0</code> metadata
     * format defines for its <code>ImageOrientation</code> element.
     */
    public static final String IMAGE_ORIENTATION_NORMAL = "Normal";

    /**
     * Image orientation: rotate 90 degrees (couter-clockwise).
     * The string value is the same as <code>javax_imageio_1.0</code> metadata
     * format defines for its <code>ImageOrientation</code> element.
     */
    public static final String IMAGE_ORIENTATION_ROTATE_90 = "Rotate90";

    /**
     * Image orientation: rotate 180 degrees (couter-clockwise).
     * The string value is the same as <code>javax_imageio_1.0</code> metadata
     * format defines for its <code>ImageOrientation</code> element.
     */
    public static final String IMAGE_ORIENTATION_ROTATE_180 = "Rotate180";

    /**
     * Image orientation: rotate 270 degrees (couter-clockwise).
     * The string value is the same as <code>javax_imageio_1.0</code> metadata
     * format defines for its <code>ImageOrientation</code> element.
     */
    public static final String IMAGE_ORIENTATION_ROTATE_270 = "Rotate270";

    /**
     * Image orientation: flip horizontally.
     * The string value is the same as <code>javax_imageio_1.0</code> metadata
     * format defines for its <code>ImageOrientation</code> element.
     */
    public static final String IMAGE_ORIENTATION_FLIP_H = "FlipH";

    /**
     * Image orientation: flip vertically.
     * The string value is the same as <code>javax_imageio_1.0</code> metadata
     * format defines for its <code>ImageOrientation</code> element.
     */
    public static final String IMAGE_ORIENTATION_FLIP_V = "FlipV";

    /**
     * Image orientation: flip horizontally then rotate 90 degrees CCW.
     * The string value is the same as <code>javax_imageio_1.0</code> metadata
     * format defines for its <code>ImageOrientation</code> element.
     */
    public static final String IMAGE_ORIENTATION_FLIP_H_ROTATE_90 =
	"FlipHRotate90";

    /**
     * Image orientation: flip vertically then rotate 90 degrees CCW.
     * The string value is the same as <code>javax_imageio_1.0</code> metadata
     * format defines for its <code>ImageOrientation</code> element.
     */
    public static final String IMAGE_ORIENTATION_FLIP_V_ROTATE_90 =
	"FlipVRotate90";

    /** Unmodifiable Set of all <code>IMAGE_ORIENTATION_*</code> constants. */
    public static final Set IMAGE_ORIENTATIONS =
	Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]
	    { IMAGE_ORIENTATION_NORMAL, IMAGE_ORIENTATION_ROTATE_90,
	      IMAGE_ORIENTATION_ROTATE_180, IMAGE_ORIENTATION_ROTATE_270,
	      IMAGE_ORIENTATION_FLIP_H, IMAGE_ORIENTATION_FLIP_V,
	      IMAGE_ORIENTATION_FLIP_H_ROTATE_90,
	      IMAGE_ORIENTATION_FLIP_H_ROTATE_90 })));

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the type of color space used by an image. The returned value
     * is an <code>Integer</code> whose value is one of the <code>TYPE_*</code>
     * constants defined in <code>java.awt.color.ColorSpace</code>.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return Type of color space used by an image, using integer constants
     *	       defined in <code>java.awt.color.ColorSpace</code>;
     *	       <code>null</code> if this information is not available.
     */
    public Integer getColorSpaceType();

    /**
     * Returns the number of channels (bands) in an image, including alpha.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return Number of channels (bands) in an image, including alpha;
     *	       <code>null</code> if this information is not available.
     */
    public Integer getNumChannels();

    /**
     * Returns whether smaller values represent darker shades in an image.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return <code>true</code> if smaller values represent darker shades;
     *	       <code>false</code> if larger values represent darker shades;
     *	       <code>null</code> if this information is not available.
     */
    public Boolean getBlackIsZero();

    /**
     * Returns the default <code>LookUpTable</code> to apply to an image.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return Default <code>LookUpTable</code> to apply to an image.
     *	       <code>null</code> if this information is not available.
     */
    public LookUpTable getDefaultLookUpTable();



    /**
     * Returns the sizes of image samples in each channel as they are stored
     * in memory, in # of bits. <code>null</code> may be returned if this
     * information is not available.
     *
     * @return Sizes of image samples in each channel as they are stored in
     *	       memory, in # of bits; <code>null</code> if this information is
     *	       not available.
     */
    public int[] getBitsPerSample();

    /**
     * Returns the number of significant bits per sample for each channel.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return Number of significant bits per sample for each channel;
     *	       <code>null</code> if this information is not available.
     */
    public int[] getSignificantBitsPerSample();


    /**
     * Returns the ratio of a pixel's width divided by it height.
     * <code>null</code> may be returned if this information is not available.
     *
     * @return Ratio of a pixel's width divided by it height;
     *	       <code>null</code> if this information is not available.
     */
    public Double getPixelAspectRatio();

    /**
     * Returns the desired orientation of an image in terms of flips and
     * counter-clockwise rotation of multiples of 90 degrees. <code>null</code>
     * may be returned if this information is not available.
     *
     * @return One of the predefined <code>IMAGE_ORIENTATION_*</code> constants
     *	       representing the desired orientation of an image in terms of
     *	       flips and counter-clockwise rotation of multiples of 90 degrees;
     *	       <code>null</code> if this information is not available.
     */
    public String getImageOrientation();
}
