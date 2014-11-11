/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import org.medtoolbox.jviewbox.LinearLookUpTable;
import org.medtoolbox.jviewbox.LookUpTable;

/**
 * Class which contains utility methods for <code>BufferedImage</code> and
 * <code>WritableRaster</code>.
 *
 * @version January 8, 2004
 */
public class BufferedImageUtilities
{
    // ---------
    // Constants
    // ---------

    /**
     * Name of BufferedImage property: Lookup Table Type.
     * <p>
     * The value of this property should be a <code>String</code>.
     *
     * @see #createLUTFromProperties
     */
    public static final String PROPERTY_NAME_LOOKUP_TABLE_TYPE =
	"org_medtoolbox_jviewbox_2.0->Lookup_table->Type";

    /**
     * Name of BufferedImage property: Lookup Table Window.
     * <p>
     * The value of this property should be either an <code>Integer</code> or
     * a <code>String</code> representation of the integer.
     *
     * @see #createLUTFromProperties
     */
    public static final String PROPERTY_NAME_LOOKUP_TABLE_WINDOW =
	"org_medtoolbox_jviewbox_2.0->Lookup_table->Window";

    /**
     * Name of BufferedImage property: Lookup Table Level.
     * <p>
     * The value of this property should be either an <code>Integer</code> or
     * a <code>String</code> representation of the integer.
     *
     * @see #createLUTFromProperties
     */
    public static final String PROPERTY_NAME_LOOKUP_TABLE_LEVEL =
	"org_medtoolbox_jviewbox_2.0->Lookup_table->Level";

    /**
     * Name of BufferedImage property: Image Data Maximum.
     * <p>
     * The value of this property should be either an <code>Integer</code> or
     * a <code>String</code> representation of the integer.
     *
     * @see #createLUTFromProperties
     */
    public static final String PROPERTY_NAME_IMAGE_DATA_MAXIMUM =
	"org_medtoolbox_jviewbox_2.0->Image_data->maximum";

    /**
     * Name of BufferedImage property: Image Data Minimum.
     * <p>
     * The value of this property should be either an <code>Integer</code> or
     * a <code>String</code> representation of the integer.
     *
     * @see #createLUTFromProperties
     */
    public static final String PROPERTY_NAME_IMAGE_DATA_MINIMUM =
	"org_medtoolbox_jviewbox_2.0->Image_data->minimum";

    /**
     * The maximum difference between any two of the R, G, and B components
     * (ranged 0-255) of a color to be considered a "grayscale" color by
     * {@link #mayBeGrayscale(ColorModel, SampleModel)} and
     * {@link #mayBeGrayscale(BufferedImage)}.
     */
    public static final int GRAYSCALE_THRESHOLD = 3;

    // --------------
    // Private fields
    // --------------

    /** Preferred RGB BufferedImage type. */
    private static int _preferredRGBType = -1;

    // -----------
    // Constructor
    // -----------

    /** Utility class. Non-instantiable. */
    private BufferedImageUtilities()
    {
	throw new UnsupportedOperationException("Non-instantiable class.");
    }

    // --------------
    // Public methods
    // --------------

    //
    // Enumerated RGB BufferedImage Types
    //

    /**
     * Returns the <code>BufferedImage</code> type corresponding to the
     * specified <code>ColorModel</code> and <code>SampleModel</code>.
     *
     * @param cm <code>ColorModel</code> of an image.
     * @param sm <code>SampleModel</code> of an image.
     *
     * @return <code>BufferedImage</code> type corresponding to the specified
     *	       <code>ColorModel</code> and <code>SampleModel</code>;
     *	       <code>-1</code> if the exact type could not be found or
     *	       <code>cm</code> and <code>sm</code> are incompatible.
     */
    public static int getBufferedImageType(ColorModel cm, SampleModel sm)
    {
	// Create a test BufferedImage and get its type
	try {
	    SampleModel sm8x8 = sm.createCompatibleSampleModel(8, 8);
	    WritableRaster wr = Raster.createWritableRaster(sm8x8, null);
	    BufferedImage bi = new BufferedImage(cm, wr,
						 cm.isAlphaPremultiplied(),
						 null);
	    int type = bi.getType();

	    // Explicitly flush bi
	    bi.flush();
	    bi = null;

	    return type;
	}
	catch (RasterFormatException e) {
	    // ?
	    return -1;
	}
	catch (IllegalArgumentException e) {
	    // cm and sm are incompatible
	    return -1;
	}
    }

    /**
     * Determines if the specified <code>BufferedImage</code> type is an RGB
     * type.
     *
     * @param type <code>BufferedImage</code> type to check.
     *
     * @return <code>true</code> if <code>type</code> is an RGB
     *	       <code>BufferedImage</code> type; <code>false</code> if not.
     */
    public static boolean isRGBType(int type)
    {
	return (type == BufferedImage.TYPE_3BYTE_BGR ||
		type == BufferedImage.TYPE_4BYTE_ABGR ||
		type == BufferedImage.TYPE_4BYTE_ABGR_PRE ||
		type == BufferedImage.TYPE_INT_ARGB ||
		type == BufferedImage.TYPE_INT_ARGB_PRE ||
		type == BufferedImage.TYPE_INT_BGR ||
		type == BufferedImage.TYPE_INT_RGB ||
		type == BufferedImage.TYPE_USHORT_555_RGB ||
		type == BufferedImage.TYPE_USHORT_565_RGB);
    }

    /**
     * Determines if the specified <code>BufferedImage</code> type is an RGB
     * type of at least 24 bit depth.
     *
     * @param type <code>BufferedImage</code> type to check.
     *
     * @return <code>true</code> if <code>type</code> is an RGB
     *	       <code>BufferedImage</code> type of at least 24 bit depth;
     *	       <code>false</code> if not.
     */
    public static boolean is24BitOrMoreRGBType(int type)
    {
	return (type == BufferedImage.TYPE_3BYTE_BGR ||
		type == BufferedImage.TYPE_4BYTE_ABGR ||
		type == BufferedImage.TYPE_4BYTE_ABGR_PRE ||
		type == BufferedImage.TYPE_INT_ARGB ||
		type == BufferedImage.TYPE_INT_ARGB_PRE ||
		type == BufferedImage.TYPE_INT_BGR ||
		type == BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Determines if the specified <code>BufferedImage</code> is of an RGB
     * type.
     * <p>
     * Currently, this method simply accepts images that are accepted by
     * either {@link #isRGBType} or {@link #isRGB(ColorModel, SampleModel)}.
     *
     * @param image <code>BufferedImage</code> to check.
     *
     * @return <code>true</code> if <code>image</code> is of an RGB type;
     *	       <code>false</code> if not.
     *
     * @see #isRGBType
     * @see #isRGB(ColorModel, SampleModel)
     */
    public static boolean isRGB(BufferedImage image)
    {
	// Is it of an RGB type?
	if (isRGBType(image.getType())) {
	    return true;
	}

	// Is it of a CUSTOM type?
	else if (image.getType() == BufferedImage.TYPE_CUSTOM) {
	    return isRGB(image.getColorModel(), image.getSampleModel());
	}

	return false;
    }

    /**
     * Determines if the specified <code>ColorModel</code> and
     * <code>SampleModel</code> define an RGB image format.
     * <p>
     * Currently, this method accepts image formats that are of any RGB
     * <code>BufferedImage</code> type (as accepted by {@link #isRGBType})
     * and image formats with <code>ComponentColorModel</code>s or
     * <code>DirectColorModel</code>s in RGB color space with 3 (no alpha) or
     * 4 (with alpha) bands. It deliberately <b>excludes</b> images with any
     * channel (band) of more than 8 bits. The reason is jViewBox may choose to
     * convert them into 24-bit RGB, which can result in loss of precision in
     * the aforementioned case.
     *
     * @param cm <code>ColorModel</code> to check.
     * @param sm <code>SampleModel</code> to check.
     *
     * @return <code>true</code> if <code>cm</code> and <code>sm</code>
     *	       define an RGB image format;
     *	       <code>false</code> otherwise.
     *
     * @see #isRGBType
     * @see #isRGB(BufferedImage)
     *
     * @since 2.0b
     */
    public static boolean isRGB(ColorModel cm, SampleModel sm)
    {
	// Is it of an RGB BufferedImage type?
	int type = getBufferedImageType(cm, sm);
	if (isRGBType(type)) {
	    return true;
	}

	// Is it of a CUSTOM type?
	// This automatically excludes incompatible cm and sm
	else if (type == BufferedImage.TYPE_CUSTOM) {

	    // Is it of a component or direct RGB color model with 3 or 4 bands
	    int nBands = sm.getNumBands();
	    if ((cm instanceof ComponentColorModel ||
		 cm instanceof DirectColorModel) &&
		cm.getColorSpace().getType() == ColorSpace.TYPE_RGB &&
		(nBands == 3 || nBands == 4)) {

		// Exclude images with more than 8 bits/channel since
		// "optimizing" them to 24-bit RGB would degrade them
		int[] nBits = cm.getComponentSize();
		for (int i = 0; i < nBits.length; i++) {
		    if (nBits[i] > 8) {
			return false;
		    }
		}

		// Accept images of a component or direct color model in RGB
		// with 3 (no alpha) or 4 (with alpha) bands and no more than
		// 8 bits/channel
		return true;
	    }
	}

	return false;
    }

    /**
     * Determines if the specified <code>BufferedImage</code> is of an RGB
     * type of at least 24 bit depth.
     *
     * @param image <code>BufferedImage</code> to check.
     *
     * @return <code>true</code> if <code>image</code> is of an RGB of at least
     *	       24 bit depth; <code>false</code> if not.
     */
    public static boolean is24BitOrMoreRGB(BufferedImage image)
    {
	return is24BitOrMoreRGBType(image.getType());
    }

    /**
     * Returns the type of RGB <code>BufferedImage</code> of at least 24 bit
     * depth that is closest to the native format of the local
     * <code>GraphicsEnvironment</code>'s default <code>GraphicsDevice</code>
     * and default <code>GraphicsConfiguration</code>.
     * <p>
     * It is assumed that this type remains the same for the lifetime of a
     * Java VM. Hence it is evaluated at most once and cached afterward.
     *
     * @return <code>BufferedImage</code> type of RGB image of at least 24 bit
     *	       depth that is closest to the native format used in the system's
     *	       default <code>GraphicsDevice</code>.
     *
     * @see java.awt.GraphicsEnvironment
     * @see java.awt.GraphicsDevice
     * @see java.awt.GraphicsConfiguration
     */
    public static int getPreferredRGBType()
    {
	// Lazy initialization
	if (_preferredRGBType < 0) {

	    // Get default graphics configuration
	    GraphicsEnvironment ge =
		GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice gd = ge.getDefaultScreenDevice();
	    GraphicsConfiguration gc = gd.getDefaultConfiguration();

	    _preferredRGBType = getPreferredRGBType(gc);
	}

	return _preferredRGBType;
    }

    /**
     * Returns the type of RGB <code>BufferedImage</code> of at least 24 bit
     * depth that is closest to the native format used in the specified
     * <code>GraphicsConfiguration</code>.
     *
     * @param gc <code>GraphicsConfiguration</code> for which to find the
     *		 native format.
     *
     * @return <code>BufferedImage</code> type of RGB image of at least 24 bit
     *	       depth that is closest to the native format used in
     *	       <code>gc</code>.
     */
    public static int getPreferredRGBType(GraphicsConfiguration gc)
    {
	// Create a compatible image which is supposed to be optimal
	BufferedImage bi = gc.createCompatibleImage(8, 8);

	switch (bi.getType()) {

	// 3 byte BGR?
	case BufferedImage.TYPE_3BYTE_BGR:
	    return BufferedImage.TYPE_3BYTE_BGR;

	// 4 byte ABGR?
	case BufferedImage.TYPE_4BYTE_ABGR:
	case BufferedImage.TYPE_4BYTE_ABGR_PRE:
	    return BufferedImage.TYPE_4BYTE_ABGR;

	// int ARGB?
	case BufferedImage.TYPE_INT_ARGB:
	case BufferedImage.TYPE_INT_ARGB_PRE:
	    return BufferedImage.TYPE_INT_ARGB;

	// int BGR?
	case BufferedImage.TYPE_INT_BGR:
	    return BufferedImage.TYPE_INT_BGR;

	// int RGB?
	case BufferedImage.TYPE_INT_RGB:
	    return BufferedImage.TYPE_INT_RGB;

	// Anything else
	default:
	    return BufferedImage.TYPE_INT_RGB;
	}
    }

    /**
     * Returns the type<b>s</b> of RGB <code>BufferedImage</code> of at least 
     * 24 bit depth that are considered closest to the native format of the 
     * local <code>GraphicsEnvironment</code>'s default
     * <code>GraphicsDevice</code> and default
     * <code>GraphicsConfiguration</code>.
     * <p>
     * In addition to the type identified by
     * {@link #getPreferredRGBType()}, this method considers the types
     * listed in the following table as preferred types depending on the
     * underlying platform when running Java 1.4 or later:
     * <pre>
     * Platform    Additional Preferred Types
     * -----------------------------------------------------------------------
     * Windows     TYPE_INT_RGB, TYPE_3BYTE_BGR
     * Linux       TYPE_INT_RGB, TYPE_3BYTE_BGR
     * Mac OS X    TYPE_INT_RGB, TYPE_3BYTE_BGR, TYPE_INT_BGR, TYPE_4BYTE_ABGR
     * Sun OS      TYPE_INT_RGB, TYPE_3BYTE_BGR, TYPE_INT_BGR
     * </pre>
     * There is, unfortunately, no hard science behind this table. The table
     * was compiled using performance data gathered from limited tests. The
     * listed types are expected to have rendering performance comparable to
     * the native format.
     *
     * @return <code>BufferedImage</code> type<b>s</b> of RGB image of at
     *	       least 24 bit depth that is closest to the native format used
     *	       in the system's default <code>GraphicsDevice</code>.
     *
     * @see java.awt.GraphicsEnvironment
     * @see java.awt.GraphicsDevice
     * @see java.awt.GraphicsConfiguration
     * @see #getPreferredRGBType()
     *
     * @since 2.0b
     */
    public static int[] getPreferredRGBTypes()
    {
	int preferredType = getPreferredRGBType();

	// Reading system properties may fail
	try {
	    // Find out Java major/minor version
	    String javaVersionStr = System.getProperty("java.version");
	    int index = javaVersionStr.indexOf('.');
	    if (index >= 0) {
		index = javaVersionStr.indexOf('.', index + 1);
		if (index >= 0) {
		    javaVersionStr = javaVersionStr.substring(0, index);
		}
	    }
	    double javaVersion;
	    try {
		javaVersion = Double.parseDouble(javaVersionStr);
	    }
	    catch (NumberFormatException e) {
		javaVersion = 0.0;
	    }

	    // Platform dependent additional preferred types
	    // Only for Java 1.4 or later
	    if (javaVersion >= 1.4) {
		// Windows
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.startsWith("windows")) {
		    // INT_RGB and 3BYTE_BGR
		    if (preferredType == BufferedImage.TYPE_INT_RGB) {
			return new int[] { BufferedImage.TYPE_INT_RGB,
					   BufferedImage.TYPE_3BYTE_BGR };
		    }
		    else if (preferredType == BufferedImage.TYPE_3BYTE_BGR) {
			return new int[] { BufferedImage.TYPE_3BYTE_BGR,
					   BufferedImage.TYPE_INT_RGB };
		    }
		}

		// Linux
		else if (osName.startsWith("linux")) {
		    // INT_RGB and 3BYTE_BGR
		    if (preferredType == BufferedImage.TYPE_INT_RGB) {
			return new int[] { BufferedImage.TYPE_INT_RGB,
					   BufferedImage.TYPE_3BYTE_BGR };
		    }
		    else if (preferredType == BufferedImage.TYPE_3BYTE_BGR) {
			return new int[] { BufferedImage.TYPE_3BYTE_BGR,
					   BufferedImage.TYPE_INT_RGB };
		    }
		}

		// Mac OS X
		else if (osName.startsWith("mac os x")) {
		    // INT_RGB, 3BYTE_BGR, INT_BGR, and 4BYTE_ABGR
		    if (preferredType == BufferedImage.TYPE_INT_RGB) {
			return new int[] { BufferedImage.TYPE_INT_RGB,
					   BufferedImage.TYPE_3BYTE_BGR,
					   BufferedImage.TYPE_INT_BGR,
					   BufferedImage.TYPE_4BYTE_ABGR };
		    }
		    else if (preferredType == BufferedImage.TYPE_3BYTE_BGR) {
			return new int[] { BufferedImage.TYPE_3BYTE_BGR,
					   BufferedImage.TYPE_INT_RGB,
					   BufferedImage.TYPE_INT_BGR,
					   BufferedImage.TYPE_4BYTE_ABGR };
		    }
		    else if (preferredType == BufferedImage.TYPE_INT_BGR) {
			return new int[] { BufferedImage.TYPE_INT_BGR,
					   BufferedImage.TYPE_3BYTE_BGR,
					   BufferedImage.TYPE_INT_RGB,
					   BufferedImage.TYPE_4BYTE_ABGR };
		    }
		    else if (preferredType == BufferedImage.TYPE_4BYTE_ABGR) {
			return new int[] { BufferedImage.TYPE_4BYTE_ABGR,
					   BufferedImage.TYPE_3BYTE_BGR,
					   BufferedImage.TYPE_INT_BGR,
					   BufferedImage.TYPE_INT_RGB };
		    }
		}

		// Sun OS
		else if (osName.startsWith("sun")) {
		    // INT_RGB, 3BYTE_BGR, and INT_BGR
		    if (preferredType == BufferedImage.TYPE_INT_RGB) {
			return new int[] { BufferedImage.TYPE_INT_RGB,
					   BufferedImage.TYPE_3BYTE_BGR,
					   BufferedImage.TYPE_INT_BGR };
		    }
		    else if (preferredType == BufferedImage.TYPE_3BYTE_BGR) {
			return new int[] { BufferedImage.TYPE_3BYTE_BGR,
					   BufferedImage.TYPE_INT_RGB,
					   BufferedImage.TYPE_INT_BGR };
		    }
		    else if (preferredType == BufferedImage.TYPE_INT_BGR) {
			return new int[] { BufferedImage.TYPE_INT_BGR,
					   BufferedImage.TYPE_3BYTE_BGR,
					   BufferedImage.TYPE_INT_RGB };
		    }
		}
	    }
	}
	catch (SecurityException e) {}

	// No additional preferred types was found
	return new int[] { preferredType };
    }

    //
    // Conversion to RGB
    //

    /**
     * Converts a <code>BufferedImage</code> to the specified RGB type.
     *
     * @param original <code>BufferedImage</code> to convert.
     * @param destType RGB <code>BufferedImage</code> type to convert to.
     *
     * @return <code>original</code> converted to the specified type; or
     *	       <code>original</code> itself if it is already of the specified
     *	       type.
     *
     * @throws IllegalArgumentException if <code>destType</code> is not an
     *	       RGB type.
     */
    public static BufferedImage convertToRGB(BufferedImage original,
					     int destType)
    {
	// Is destType RGB?
	if (!isRGBType(destType)) {
	    throw new IllegalArgumentException("destType is not an RGB type.");
	}

	// Is already in destType?
	if (original.getType() == destType) {
	    return original;
	}

	// Create a destination image
	BufferedImage dest = new BufferedImage(original.getWidth(),
					       original.getHeight(), destType);

	Graphics2D g2d = dest.createGraphics();
	g2d.drawImage(original, 0, 0, null);
	g2d.dispose();

	return dest;
    }

    /**
     * Converts a <code>BufferedImage</code> to the specified RGB type.
     *
     * @param src <code>BufferedImage</code> to convert.
     * @param dest RGB <code>BufferedImage</code> as the destination for the
     *		   conversion. It must be of the same dimension as
     *		   <code>src</code>.
     *
     * @throws IllegalArgumentException if <code>dest</code> is not of an RGB
     *	       type or not of the same dimension as <code>src</code>.
     */
    public static void convertToRGB(BufferedImage src, BufferedImage dest)
    {
	// Is destination RGB?
	if (!isRGBType(dest.getType())) {
	    throw new IllegalArgumentException("dest is not of an RGB type.");
	}

	// Is destination of the same dimension as source?
	if (src.getWidth() != dest.getWidth() ||
	    src.getHeight() != dest.getHeight()) {
	    throw new IllegalArgumentException("dest is not of the same " +
					       "dimension as src.");
	}

	Graphics2D g2d = dest.createGraphics();
	g2d.drawImage(src, 0, 0, null);
	g2d.dispose();
    }

    //
    // Clear (Zero-Out) a WritableRaster
    //

    /**
     * Clears a <code>WritableRaster</code>, i.e., fills its
     * <code>DataBuffer</code> with the default initial value of its element
     * type, usually 0.
     *
     * @param raster <code>WritableRaster</code> to clear.
     */
    public static void clearRaster(WritableRaster raster)
    {
	// Scale a 1x1 newly created raster into the one to clear
	// Note that this may be subject to the affine transform bug #4192198
	WritableRaster zero = raster.createCompatibleWritableRaster(1, 1);
	AffineTransform xform =
	    AffineTransform.getScaleInstance(raster.getWidth(),
					     raster.getHeight());
	AffineTransformOp op =
	    new AffineTransformOp(xform,
				  AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	op.filter(zero, raster);
    }

    //
    // Grayscale Tests
    //

    /**
     * Determines if the specified <code>ColorModel</code> and
     * <code>SampleModel</code> define an opaque grayscale image format which
     * is preferred and accelarated by jViewBox.
     * <p>
     * Specifically, this method checks if the <code>ColorModel</code> is a
     * <code>ComponentColorModel</code> in the <code>TYPE_GRAY</code>
     * <code>ColorSpace</code> without alpha, if the <code>ColorModel</code>
     * and <code>SampleModel</code> are compatible with each other, and if
     * the <code>SampleModel</code> is a
     * <code>PixelInterleavedSampleModel</code>.
     *
     * @param cm <code>ColorModel</code> to check.
     * @param sm <code>SampleModel</code> to check.
     *
     * @return <code>true</code> if <code>cm</code> and <code>sm</code> define
     *	       a grayscale image format preferred and accelarated by jViewBox;
     *	       <code>false</code> otherwise.
     */
    public static boolean isGrayscale(ColorModel cm, SampleModel sm)
    {
	return (cm.isCompatibleSampleModel(sm) &&
		(cm instanceof ComponentColorModel) && !cm.hasAlpha() &&
		cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY &&
		(sm instanceof PixelInterleavedSampleModel));
    }

    /**
     * Determines if the specified <code>BufferedImage</code> is an opaque
     * grayscale image in a format preferred and accelarated by jViewBox.
     * <p>
     * This method applies the same rules as
     * {@link #isGrayscale(ColorModel, SampleModel)}.
     *
     * @param image <code>BufferedImage</code> to check.
     *
     * @return <code>true</code> if <code>image</code> is an opaque grayscale
     *	       image in a format preferred and accelarated by jViewBox;
     *	       <code>false</code> otherwise.
     *
     * @see #isGrayscale(ColorModel, SampleModel)
     */
    public static boolean isGrayscale(BufferedImage image)
    {
	return isGrayscale(image.getColorModel(), image.getSampleModel());
    }

    /**
     * Determines if the specified <code>ColorModel</code> and
     * <code>SampleModel</code> define an opaque grayscale image format which
     * is preferred and accelarated by jViewBox and consists of pixels that
     * are stored as <code>short</code>s (signed or unsigned). Pixels of
     * <code>short</code>s are known to cause bugs in
     * <code>AffineTransformOp</code> (ID# 4192198) and <code>LookupOp</code>
     * (ID# 4554571).
     * <p>
     * This method applies the same rules as
     * {@link #isGrayscale(ColorModel, SampleModel)} plus checking the size
     * of the pixels.
     *
     * @param cm <code>ColorModel</code> to check.
     * @param sm <code>SampleModel</code> to check.
     *
     * @return <code>true</code> if <code>cm</code> and <code>sm</code> define
     *	       a grayscale image format preferred and accelarated by jViewBox
     *	       and consisting of pixels that are stored as <code>short</code>s;
     *	       <code>false</code> otherwise.
     *
     * @see #isGrayscale(ColorModel, SampleModel)
     */
    public static boolean isGrayscaleOfShorts(ColorModel cm, SampleModel sm)
    {
	// Check only data type and ignore transfer type, which, according
	// to the source code, should be the same as data type for
	// ComponentSampleModel and PixelInterleavedSampleModel
	int dataType = sm.getDataType();
	return (isGrayscale(cm, sm) &&
		(dataType == DataBuffer.TYPE_SHORT ||
		 dataType == DataBuffer.TYPE_USHORT));
    }

    /**
     * Determines if the specified <code>BufferedImage</code> is an opaque
     * grayscale image in a format preferred and accelarated by jViewBox
     * and consisting of pixels that are stored as <code>short</code>s. Pixels
     * of <code>short</code>s are known to cause bugs in
     * <code>AffineTransformOp</code> (ID# 4192198) and <code>LookupOp</code>
     * (ID# 4554571).
     * <p>
     * This method applies the same rules as
     * {@link #isGrayscaleOfShorts(ColorModel, SampleModel)}.
     *
     * @param image <code>BufferedImage</code> to check.
     *
     * @return <code>true</code> if <code>image</code> is an opaque grayscale
     *	       image in a format preferred and accelarated by jViewBox
     *	       and consisting of pixels that are stored as <code>short</code>s.
     *	       <code>false</code> otherwise.
     *
     * @see #isGrayscaleOfShorts(ColorModel, SampleModel)
     */
    public static boolean isGrayscaleOfShorts(BufferedImage image)
    {
	return isGrayscaleOfShorts(image.getColorModel(),
				   image.getSampleModel());
    }

    /**
     * A relaxed test of opaque grayscale images.
     * <p>
     * Specifically, this method accepts images, in addition to those already
     * accepted by <code>isGrayscale</code>, that consist of "grayscale"
     * <code>IndexColorModel</code>s and single-band 
     * <code>PixelInterleavedSampleModel</code>. An
     * <code>IndexColorModel</code> is considered "grayscale" if the
     * difference between any two of the R, G, and B components of any color
     * in the color model is no greater than {@link #GRAYSCALE_THRESHOLD}
     * and if each of the R, G, and B components in the color map increases
     * monotonically.
     *
     * @param cm <code>ColorModel</code> to check.
     * @param sm <code>SampleModel</code> to check.
     *
     * @return <code>true</code> if <code>cm</code> and <code>sm</code>
     *	       <b>may</b> define an opaque grayscale image format;
     *	       <code>false</code> otherwise.
     *
     * @see #isGrayscale(ColorModel, SampleModel)
     * @see #GRAYSCALE_THRESHOLD
     *
     * @since 2.0b
     */
    public static boolean mayBeGrayscale(ColorModel cm, SampleModel sm)
    {
	// Original grayscale test
	if (isGrayscale(cm, sm)) {
	    return true;
	}

	// Relaxed grayscale test: "grayscale" IndexColorModel and
	// single-band pixel interleaved raster
	if ((cm instanceof IndexColorModel) &&
	    cm.getTransparency() == Transparency.OPAQUE &&
	    isSingleBandInterleaved(sm)) {

	    // Check the color table to see if the RGB components are
	    // "close enough" and monotonically increasing
	    IndexColorModel icm = (IndexColorModel)cm;
	    int size = icm.getMapSize();
	    for (int i = 0, r = 0, g = 0, b = 0, c; i < size; i++) {
		if ((c = icm.getRed(i)) < r) {
		    return false;
		}
		else {
		    r = c;
		}
		if ((c = icm.getGreen(i)) < g) {
		    return false;
		}
		else {
		    g = c;
		}
		if ((c = icm.getBlue(i)) < b) {
		    return false;
		}
		else {
		    b = c;
		}
		if (Math.abs(r - g) > GRAYSCALE_THRESHOLD ||
		    Math.abs(g - b) > GRAYSCALE_THRESHOLD ||
		    Math.abs(b - r) > GRAYSCALE_THRESHOLD) {
		    return false;
		}
	    }

	    return true;
	}

	return false;
    }

    /**
     * A relaxed test of opaque grayscale images.
     * <p>
     * Specifically, this method accepts images, in addition to those already
     * accepted by <code>isGrayscale</code>, that consists of "grayscale"
     * <code>IndexColorModel</code>s and single-band 
     * <code>PixelInterleavedSampleModel</code>. An
     * <code>IndexColorModel</code> is considered "grayscale" if the
     * difference between any two of the R, G, and B components of any color
     * in the color model is no greater than <code>GRAYSCALE_THRESHOLD</code>
     * and if each of the R, G, and B components in the color map increases
     * monotonically.
     *
     * @param image <code>BufferedImage</code> to check.
     *
     * @return <code>true</code> if <code>image</code> <b>may</b> br an opaque
     *	       grayscale image; <code>false</code> otherwise.
     *
     * @see #isGrayscale(ColorModel, SampleModel)
     * @see #GRAYSCALE_THRESHOLD
     *
     * @since 2.0b
     */
    public static boolean mayBeGrayscale(BufferedImage image)
    {
	return mayBeGrayscale(image.getColorModel(), image.getSampleModel());
    }

    //
    // Single-Band Interleaved Tests
    //

    /**
     * Determines if the specified <code>SampleModel</code> defines an image
     * format of one sample per pixel and one sample per data array element.
     * <p>
     * Specifically, this method checks if the <code>SampleModel</code> is
     * a <code>PixelInterleavedSampleModel</code> of exactly one band.
     *
     * @param sm <code>SampleModel</code> to check.
     *
     * @return <code>true</code> if <code>sm</code> defines an image format of
     *	       one sample per pixel and one sample per data array element;
     *	       <code>false</code> otherwise.
     */
    public static boolean isSingleBandInterleaved(SampleModel sm)
    {
	return ((sm instanceof PixelInterleavedSampleModel) &&
		sm.getNumBands() == 1);
    }

    /**
     * Determines if the specified <code>Raster</code> is in a format of one
     * sample per pixel and one sample per data array element.
     * <p>
     * This method applies the same rules as
     * {@link #isSingleBandInterleaved(SampleModel)}.
     *
     * @param raster <code>Raster</code> to check.
     *
     * @return <code>true</code> if <code>raster</code> is in a format of
     *	       one sample per pixel and one sample per data array element;
     *	       <code>false</code> otherwise.
     *
     * @see #isSingleBandInterleaved(SampleModel)
     */
    public static boolean isSingleBandInterleaved(Raster raster)
    {
	return isSingleBandInterleaved(raster.getSampleModel());
    }

    /**
     * Determines if the specified <code>BufferedImage</code> is in a format
     * of one sample per pixel and one sample per data array element.
     * <p>
     * This method applies the same rules as
     * {@link #isSingleBandInterleaved(SampleModel)}.
     *
     * @param image <code>BufferedImage</code> to check.
     *
     * @return <code>true</code> if <code>image</code> is in a format of
     *	       one sample per pixel and one sample per data array element;
     *	       <code>false</code> otherwise.
     *
     * @see #isSingleBandInterleaved(SampleModel)
     */
    public static boolean isSingleBandInterleaved(BufferedImage image)
    {
	return isSingleBandInterleaved(image.getSampleModel());
    }

    /**
     * Determines if the specified <code>SampleModel</code> defines an image
     * format of one sample per pixel and one sample per data array element
     * and with samples stored as <code>short</code>s (signed or unsigned).
     * <p>
     * This method applies the same rules as
     * <code>isSingleBandInterleaved(SampleModel)</code> plus checking the size
     * of the pixels.
     *
     * @param sm <code>SampleModel</code> to check.
     *
     * @return <code>true</code> if <code>sm</code> defines an image format of
     *	       one sample per pixel and one sample per data array element and
     *	       with samples stored as <code>short</code>s;
     *	       <code>false</code> otherwise.
     *
     * @see #isSingleBandInterleaved(SampleModel)
     */
    public static boolean isSingleBandInterleavedOfShorts(SampleModel sm)
    {
	// Check only data type and ignore transfer type, which, according
	// to the source code, should be the same as data type for
	// ComponentSampleModel and PixelInterleavedSampleModel
	int dataType = sm.getDataType();
	return (isSingleBandInterleaved(sm) && 
		(dataType == DataBuffer.TYPE_SHORT ||
		 dataType == DataBuffer.TYPE_USHORT));
    }

    /**
     * Determines if the specified <code>Raster</code> is in a format of one
     * sample per pixel and one sample per data array element and with samples
     * stored as <code>short</code>s (signed or unsigned).
     * <p>
     * This method applies the same rules as
     * {@link #isSingleBandInterleavedOfShorts(SampleModel)}.
     *
     * @param raster <code>Raster</code> to check.
     *
     * @return <code>true</code> if <code>raster</code> is in a format of
     *	       one sample per pixel and one sample per data array element and
     *	       with samples stored as <code>short</code>s;
     *	       <code>false</code> otherwise.
     *
     * @see #isSingleBandInterleavedOfShorts(SampleModel)
     */
    public static boolean isSingleBandInterleavedOfShorts(Raster raster)
    {
	return isSingleBandInterleavedOfShorts(raster.getSampleModel());
    }

    /**
     * Determines if the specified <code>BufferedImage</code> is in a format
     * of one sample per pixel and one sample per data array element and with
     * samples stored as <code>short</code>s (signed or unsigned).
     * <p>
     * This method applies the same rules as
     * {@link #isSingleBandInterleavedOfShorts(SampleModel)}.
     *
     * @param image <code>BufferedImage</code> to check.
     *
     * @return <code>true</code> if <code>image</code> is in a format of
     *	       one sample per pixel and one sample per data array element and
     *	       with samples precision stored as <code>short</code>s;
     *	       <code>false</code> otherwise.
     *
     * @see #isSingleBandInterleavedOfShorts(SampleModel)
     */
    public static boolean isSingleBandInterleavedOfShorts(BufferedImage image)
    {
	return isSingleBandInterleavedOfShorts(image.getSampleModel());
    }

    //
    // Look-up Tables
    //

    /**
     * Creates a <code>LookUpTable</code> from the specified
     * <code>BufferedImage</code>'s properties.
     * <p>
     * This method recognizes the properties of the names given by
     * <code>PROPERTY_NAME_LOOKUP_TABLE_*</code> and
     * <code>PROPERTY_NAME_IMAGE_DATA_*</code> and tries to create an LUT
     * as specified. Currently, the only supported
     * <code>LOOKUP_TABLE_TYPE</code> is &quote;LINEAR&quote;, with which
     * a <code>LOOKUP_TABLE_WINDOW</code> and <code>LOOKUP_TABLE_LEVEL</code>
     * properties define a <code>LinearLookUpTable</code>.
     * <code>IMAGE_DATA_MAXIMUM</code>, if exists, is used to determine the
     * the most efficient size of the look-up table.
     *
     * @param image <code>BufferedImage</code> to create
     *		    <code>LookUpTable</code> for.
     *
     * @return <code>LookUpTable</code> created from the properties of
     *	       <code>image</code>; <code>null</code> if no look up table
     *	       information may be found in <code>image</code>.
     *
     * @see LookUpTable
     * @see #PROPERTY_NAME_LOOKUP_TABLE_TYPE
     * @see #PROPERTY_NAME_LOOKUP_TABLE_WINDOW
     * @see #PROPERTY_NAME_LOOKUP_TABLE_LEVEL
     * @see #PROPERTY_NAME_IMAGE_DATA_MAXIMUM
     * @see #PROPERTY_NAME_IMAGE_DATA_MINIMUM
     * @see ImageRenderingHintsFactory#createHints(BufferedImage)
     */
    public static LookUpTable createLUTFromProperties(BufferedImage image)
    {
	// Find lookup table type
	Object type = image.getProperty(PROPERTY_NAME_LOOKUP_TABLE_TYPE);
	if (type != null && type != Image.UndefinedProperty &&
	    type instanceof String) {

	    // Only LINEAR type is supported now
	    if ("LINEAR".equalsIgnoreCase(((String)type).trim())) {
		// Find WINDOW and LEVEL
		Integer window = ImageRenderingHintsFactory._parseInt(
		    image.getProperty(PROPERTY_NAME_LOOKUP_TABLE_WINDOW));
		Integer level = ImageRenderingHintsFactory._parseInt(
		    image.getProperty(PROPERTY_NAME_LOOKUP_TABLE_LEVEL));

		// Create the linear LUT
		if (window != null && level != null) {
		    // Find the LUT size from image's color model
		    ColorModel cm = image.getColorModel();
		    int size;

		    // IndexColorModel?
		    if (cm instanceof IndexColorModel) {
			// Use map size
			size = ((IndexColorModel)cm).getMapSize();
		    }

		    // ComponentColorModel?
		    else if (cm instanceof ComponentColorModel) {
			// Take the largest of component sizes as LUT size
			int[] sizes = cm.getComponentSize();
			size = 0;
			for (int i = 0, max = 0; i < sizes.length; i++) {
			    size = Math.max(size, sizes[i]);
			}
			size = 1 << size;
		    }

		    // PackedColorModel and else
		    else {
			// Use pixel size for lack of information
			size = 1 << cm.getPixelSize();
		    }

		    // If Image Data Maximum property exists, use as size
		    Integer imageDataMaximum =
			ImageRenderingHintsFactory._parseInt(
			  image.getProperty(PROPERTY_NAME_IMAGE_DATA_MAXIMUM));
		    if (imageDataMaximum != null) {
			size = imageDataMaximum.intValue() + 1;
		    }

		    LinearLookUpTable llut = new LinearLookUpTable(size);
		    llut.setWindowLevel(window.intValue(), level.intValue());
		    return llut;
		}
	    }
	}

	return null;
    }

    /**
     * Creates a <code>LookUpTable</code> for the specified
     * <code>BufferedImage</code> by scanning for the minimum and maximum
     * pixel values and mapping them to the full output scale.
     * <p>
     * Currently, this method only applies to images that pass the test of
     * {@link #isSingleBandInterleaved(BufferedImage)} and returns
     * {@link LinearLookUpTable}s that map the minimum and maximum pixel
     * values to 0 and 255 respectively. It returns <code>null</code>
     * otherwise.
     *
     * @param image <code>BufferedImage</code> to scan and create a
     *		    <code>LookUpTable</code> for.
     *
     * @return <code>LookUpTable</code> created by scanning for the minimum
     *	       and maximum pixel values and mapping them to the full output
     *	       scale;
     *	       <code>null</code> if unable to perform the task.
     *
     * @since 2.0b
     */
    public static LookUpTable createLUTByScanning(BufferedImage image)
    {
	Raster raster = image.getRaster();

	// Single-band interleaved?
	if (isSingleBandInterleaved(raster)) {
	    // Find out the min and max of pixel values
	    int min, max;

	    // Pixels are continuously stored in the array?
	    PixelInterleavedSampleModel pism =
		(PixelInterleavedSampleModel)raster.getSampleModel();
	    if (pism.getPixelStride() == 1 &&
		pism.getScanlineStride() == raster.getWidth()) {

		DataBuffer db = raster.getDataBuffer();
		int offset = pism.getBandOffsets()[0];
		int limit = raster.getWidth() * raster.getHeight() + offset;
		min = max = db.getElem(offset);
		if (db instanceof DataBufferByte) {
		    byte[] array = ((DataBufferByte)db).getData();
		    for (int i = offset + 1; i < limit; i++) {
			int pixel = array[i] & 0xFF;
			min = Math.min(min, pixel);
			max = Math.max(max, pixel);
		    }
		}
		else if (db instanceof DataBufferShort) {
		    short[] array = ((DataBufferShort)db).getData();
		    for (int i = offset + 1; i < limit; i++) {
			int pixel = array[i];
			min = Math.min(min, pixel);
			max = Math.max(max, pixel);
		    }
		}
		else if (db instanceof DataBufferUShort) {
		    short[] array = ((DataBufferUShort)db).getData();
		    for (int i = offset + 1; i < limit; i++) {
			int pixel = array[i] & 0xFFFF;
			min = Math.min(min, pixel);
			max = Math.max(max, pixel);
		    }
		}
		else if (db instanceof DataBufferInt) {
		    int[] array = ((DataBufferInt)db).getData();
		    for (int i = offset + 1; i < limit; i++) {
			int pixel = array[i];
			min = Math.min(min, pixel);
			max = Math.max(max, pixel);
		    }
		}
		else {
		    for (int i = offset + 1; i < limit; i++) {
			int pixel = db.getElem(i);
			min = Math.min(min, pixel);
			max = Math.max(max, pixel);
		    }
		}
	    }

	    // General PixelInterleavedSampleModel
	    else {
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		DataBuffer db = raster.getDataBuffer();
		for (int j = 0, h = raster.getHeight(); j < h; j++) {
		    for (int i = 0, w = raster.getWidth(); i < w; i++) {
			int pixel = pism.getSample(i, j, 0, db);
			min = Math.min(min, pixel);
			max = Math.max(max, pixel);
		    }
		}
	    }

	    LinearLookUpTable llut = new LinearLookUpTable(max + 1);
	    llut.setWindowLevel(max - min, (max + min) / 2);
	    return llut;
	}

	return null;
    }
}
