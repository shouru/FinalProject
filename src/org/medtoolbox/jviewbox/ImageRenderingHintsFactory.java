/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox;

import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.medtoolbox.jviewbox.LookUpTable;

/**
 * Factory class for <code>ImageRenderingHints</code>.
 * <p>
 * When used with Java Image I/O, this class provides a method for creating
 * an instance of <code>ImageRenderingHints</code> from an instance of
 * <code>IIOMetadata</code>. The method looks for image format information
 * in the <code>javax_imageio_1.0</code> standard (plug-in neutral) metadata
 * tree and the jViewBox specific <code>org_medtoolbox_jviewbox_2.0</code>
 * metadata tree which is defined by the following DTD:
 * <p>
 * <a name="jviewbox_DTD"><code>org_medtoolbox_jviewbox_2.0</code> metadata
 * format specification</a>
 * <pre>
&lt;!DOCTYPE "org_medtoolbox_jviewbox_2.0" [

  &lt;!ELEMENT "org_medtoolbox_jviewbox_2.0" (Lookup_table?, Image_data?)&gt;

  &lt;!ELEMENT "Lookup_table" EMPTY&gt;
  &lt;!-- Default look-up table --&gt;

    &lt;!ATTLIST "Lookup_table" "Type" CDATA #FIXED "Linear"&gt;
    &lt;!-- Only linear look-up table is supported now --&gt;

    &lt;!ATTLIST "Lookup_table" "Window" CDATA #REQUIRED&gt;
    &lt;!-- An interger value as the window of the linear LUT --&gt;

    &lt;!ATTLIST "Lookup_table" "Level" CDATA #REQUIRED&gt;
    &lt;!-- An interger value as the level of the linear LUT --&gt;

  &lt;!ELEMENT "Image_data" EMPTY&gt;
  &lt;!-- Statistics of pixel values --&gt;

    &lt;!ATTLIST "Image_data" "maximum" CDATA #IMPLIED&gt;
    &lt;!-- An interger value as the maximum of pixel values --&gt;

    &lt;!ATTLIST "Image_data" "minimum" CDATA #IMPLIED&gt;
    &lt;!-- An interger value as the minimum of pixel values --&gt;

]&gt;
 * </pre>
 *
 * @version January 8, 2004
 */
public class ImageRenderingHintsFactory
{
    // ---------
    // Constants
    // ---------

    //
    // Names of Java Image I/O metadata formats that are parsed by this
    // factory to generate rendering hints
    //

    /** Name of the javax_imageio_1.0 metadata format. */
    public static final String FORMAT_JAVAX_IMAGEIO_1_0 = "javax_imageio_1.0";

    /** Name of the org_medtoolbox_jviewbox_2.0 metadata format. */
    public static final String FORMAT_ORG_MEDTOOLBOX_JVIEWBOX_2_0 =
	"org_medtoolbox_jviewbox_2.0";

    //
    // Tags of individual elements in the Image I/O metadata tree that are
    // parsed by this factory to generate rendering hints
    //

    /**
     * Tag of the <b>color space type</b> element, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String TAG_COLOR_SPACE_TYPE = "ColorSpaceType";

    /**
     * Tag of the <b>number of channels</b> element, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String TAG_NUM_CHANNELS = "NumChannels";

    /**
     * Tag of the <b>black is zero</b> element, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String TAG_BLACK_IS_ZERO = "BlackIsZero";



    /**
     * Tag of the <b>bits per sample</b> element, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String TAG_BITS_PER_SAMPLE = "BitsPerSample";

    /**
     * Tag of the <b>significant bits per sample</b> element, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String TAG_SIGNIFICANT_BITS_PER_SAMPLE =
	"SignificantBitsPerSample";


    /**
     * Tag of the <b>pixel aspect ratio</b> element, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String TAG_PIXEL_ASPECT_RATIO = "PixelAspectRatio";

    /**
     * Tag of the <b>image orientation</b> element, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String TAG_IMAGE_ORIENTATION = "ImageOrientation";

    /**
     * Tag of the <b>look-up table</b> element, as specified by
     * <code>org_medtoolbox_jviewbox_2.0</code> DTD.
     */
    public static final String TAG_LOOK_UP_TABLE = "Lookup_table";

    /**
     * Tag of the <b>image data</b> element, as specified by
     * <code>org_medtoolbox_jviewbox_2.0</code> DTD.
     */
    public static final String TAG_IMAGE_DATA = "Image_data";

    //
    // Color space types
    //

    /**
     * Name of the color space type XYZ, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_XYZ = "XYZ";

    /**
     * Name of the color space type Lab, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_Lab = "Lab";

    /**
     * Name of the color space type Luv, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_Luv = "Luv";

    /**
     * Name of the color space type YCbCr, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_YCbCr = "YCbCr";

    /**
     * Name of the color space type Yxy, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_Yxy = "Yxy";

    /**
     * Name of the color space type YCCK, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_YCCK = "YCCK";

    /**
     * Name of the color space type PhotoYCC, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_PhotoYCC = "PhotoYCC";

    /**
     * Name of the color space type RGB, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_RGB = "RGB";

    /**
     * Name of the color space type GRAY, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_GRAY = "GRAY";

    /**
     * Name of the color space type HSV, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_HSV = "HSV";

    /**
     * Name of the color space type HLS, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_HLS = "HLS";

    /**
     * Name of the color space type CMYK, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_CMYK = "CMYK";

    /**
     * Name of the color space type CMY, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_CMY = "CMY";

    /**
     * Name of the color space type 2CLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_2CLR = "2CLR";

    /**
     * Name of the color space type 3CLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_3CLR = "3CLR";

    /**
     * Name of the color space type 4CLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_4CLR = "4CLR";

    /**
     * Name of the color space type 5CLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_5CLR = "5CLR";

    /**
     * Name of the color space type 6CLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_6CLR = "6CLR";

    /**
     * Name of the color space type 7CLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_7CLR = "7CLR";

    /**
     * Name of the color space type 8CLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_8CLR = "8CLR";

    /**
     * Name of the color space type 9CLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_9CLR = "9CLR";

    /**
     * Name of the color space type ACLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_ACLR = "ACLR";

    /**
     * Name of the color space type BCLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_BCLR = "BCLR";

    /**
     * Name of the color space type CCLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_CCLR = "CCLR";

    /**
     * Name of the color space type DCLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_DCLR = "DCLR";

    /**
     * Name of the color space type ECLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_ECLR = "ECLR";

    /**
     * Name of the color space type FCLR, as specified by
     * <code>javax_imageio_1.0</code> DTD.
     */
    public static final String COLOR_SPACE_NAME_FCLR = "FCLR";

    /**
     * Map of <code>java.awt.color.ColorSpace.TYPE_* int</code> constants
     * (which are used by <code>ImageRenderingHints</code> to
     * <code>COLOR_SPACE_NAME_* String</code> constants defined in this class
     * (which are used in <code>javax_imageio_1.0</code> metadata trees).
     */
    public static final Map COLOR_SPACE_TYPE_INT_TO_NAME;

    /**
     * Map of <code>COLOR_SPACE_NAME_* String</code> constants defined in this
     * class (which are used in <code>javax_imageio_1.0</code> metadata tree)
     * to <code>java.awt.color.ColorSpace.TYPE_* int</code> constants
     * (which are used by <code>ImageRenderingHints</code>).
     */
    public static final Map COLOR_SPACE_NAME_TO_TYPE_INT;

    /**
     * Map of <code>COLOR_SPACE_NAME_* String</code> constants defined in this
     * class, converted to all uppercase letters, to
     * <code>java.awt.color.ColorSpace.TYPE_* int</code> constants.
     *
     * @see #COLOR_SPACE_NAME_TO_TYPE_INT
     */
    public static final Map COLOR_SPACE_NAME_ALL_CAPS_TO_TYPE_INT;

    // Static initializer for the above three maps
    static {
	Map m = new HashMap();
	m.put(new Integer(ColorSpace.TYPE_2CLR), COLOR_SPACE_NAME_2CLR);
	m.put(new Integer(ColorSpace.TYPE_3CLR), COLOR_SPACE_NAME_3CLR);
	m.put(new Integer(ColorSpace.TYPE_4CLR), COLOR_SPACE_NAME_4CLR);
	m.put(new Integer(ColorSpace.TYPE_5CLR), COLOR_SPACE_NAME_5CLR);
	m.put(new Integer(ColorSpace.TYPE_6CLR), COLOR_SPACE_NAME_6CLR);
	m.put(new Integer(ColorSpace.TYPE_7CLR), COLOR_SPACE_NAME_7CLR);
	m.put(new Integer(ColorSpace.TYPE_8CLR), COLOR_SPACE_NAME_8CLR);
	m.put(new Integer(ColorSpace.TYPE_9CLR), COLOR_SPACE_NAME_9CLR);
	m.put(new Integer(ColorSpace.TYPE_ACLR), COLOR_SPACE_NAME_ACLR);
	m.put(new Integer(ColorSpace.TYPE_BCLR), COLOR_SPACE_NAME_BCLR);
	m.put(new Integer(ColorSpace.TYPE_CCLR), COLOR_SPACE_NAME_CCLR);
	m.put(new Integer(ColorSpace.TYPE_CMY), COLOR_SPACE_NAME_CMY);
	m.put(new Integer(ColorSpace.TYPE_CMYK), COLOR_SPACE_NAME_CMYK);
	m.put(new Integer(ColorSpace.TYPE_DCLR), COLOR_SPACE_NAME_DCLR);
	m.put(new Integer(ColorSpace.TYPE_ECLR), COLOR_SPACE_NAME_ECLR);
	m.put(new Integer(ColorSpace.TYPE_FCLR), COLOR_SPACE_NAME_FCLR);
	m.put(new Integer(ColorSpace.TYPE_GRAY), COLOR_SPACE_NAME_GRAY);
	m.put(new Integer(ColorSpace.TYPE_HLS), COLOR_SPACE_NAME_HLS);
	m.put(new Integer(ColorSpace.TYPE_HSV), COLOR_SPACE_NAME_HSV);
	m.put(new Integer(ColorSpace.TYPE_Lab), COLOR_SPACE_NAME_Lab);
	m.put(new Integer(ColorSpace.TYPE_Luv), COLOR_SPACE_NAME_Luv);
	m.put(new Integer(ColorSpace.TYPE_RGB), COLOR_SPACE_NAME_RGB);
	m.put(new Integer(ColorSpace.TYPE_XYZ), COLOR_SPACE_NAME_XYZ);
	m.put(new Integer(ColorSpace.TYPE_YCbCr), COLOR_SPACE_NAME_YCbCr);
	m.put(new Integer(ColorSpace.TYPE_Yxy), COLOR_SPACE_NAME_Yxy);
	COLOR_SPACE_TYPE_INT_TO_NAME = Collections.unmodifiableMap(m);

	Map n = new HashMap();
	// Invert the map
	for (Iterator it = m.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry)it.next();
	    n.put(e.getValue(), e.getKey());
	}
	n.put(COLOR_SPACE_NAME_PhotoYCC, new Integer(ColorSpace.TYPE_YCbCr));
	n.put(COLOR_SPACE_NAME_YCCK, new Integer(ColorSpace.TYPE_YCbCr));
	COLOR_SPACE_NAME_TO_TYPE_INT = Collections.unmodifiableMap(n);

	Map o = new HashMap();
	// Turn all names to uppercase
	for (Iterator it = n.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry)it.next();
	    o.put(((String)e.getKey()).toUpperCase(), e.getValue());
	}
	COLOR_SPACE_NAME_ALL_CAPS_TO_TYPE_INT = Collections.unmodifiableMap(o);
    }

    // -----------
    // Constructor
    // -----------

    /** Factory class: no instance may be created. */
    private ImageRenderingHintsFactory()
    {
	throw new UnsupportedOperationException("Non-instantiable class.");
    }

    // --------------
    // Public methods
    // --------------


    /**
     * Creates an <code>ImageRenderingHints</code> using information from
     * the supplied <code>IIOMetadata</code>.
     * <p>
     * This method analyzes the metadatas trees of the
     * <code>javax_imageio_1.0</code> standard metadata format and
     * <a href="#jviewbox_DTD"><code>org_medtoolbox_jviewbox_2.0</code></a>
     * supplement metadata format for rendering hints.
     * <p>
     * Specifically, this method gets the following attributes from the
     * corresponding elements in the <code>javax_imageio_1.0</code> tree:
     * <ul>
     * <li>Color space type</li>
     * <li>Number of channels</li>
     * <li>Black is zero</li>
     * <li>Bits per sample</li>
     * <li>Significant bits per sample</li>
     * <li>Pixel aspect ratio</li>
     * <li>Image orientation</li>
     * </ul>
     * and the following from the <a href="#jviewbox_DTD">
     * <code>org_medtoolbox_jviewbox_2.0</code></a> tree:
     * <ul>
     * <li>Default look-up table</li>
     * </ul>
     *
     * @param metadata <code>IIOMetadata</code> to be analyzed for creating
     *		       <code>ImageRenderingHints</code>.
     *
     * @return <code>ImageRenderingHints</code> created using information from
     *	       the supplied <code>IIOMetadata</code>.
     */
    public static ImageRenderingHints createHints(IIOMetadata metadata)
    {
	// Hints to be found from the tree
	Integer colorSpaceType = null;
	Integer numChannels = null;
	Boolean blackIsZero = null;
	LookUpTable defaultLookUpTable = null;
	int[] bitsPerSample = null;
	int[] significantBitsPerSample = null;
	Double pixelAspectRatio = null;
	String imageOrientation = null;

	// Get javax_imageio_1.0 tree
	Node root = metadata.isStandardMetadataFormatSupported()
	    ? metadata.getAsTree(FORMAT_JAVAX_IMAGEIO_1_0)
	    : new IIOMetadataNode();

	// Chroma element
	Node chroma = _findChild(root, "Chroma");
	if (chroma != null) {
	    Node node;
	    String value;

	    // Color space type
	    node = _findChild(chroma, TAG_COLOR_SPACE_TYPE);
	    if (node != null &&
		(value = _getAttribute(node, "name")) != null) {

		value = value.trim().toUpperCase();
		colorSpaceType = (Integer)
		    COLOR_SPACE_NAME_ALL_CAPS_TO_TYPE_INT.get(value);
	    }

	    // Number of channels
	    node = _findChild(chroma, TAG_NUM_CHANNELS);
	    if (node != null) {
		numChannels = _parseInt(_getAttribute(node, "value"));
	    }

	    // Black is zero
	    node = _findChild(chroma, TAG_BLACK_IS_ZERO);
	    if (node != null) {
		// Since the default is TRUE per DTD, only check FALSE
		value = _getAttribute(node, "value");
		blackIsZero = (value != null &&
			       value.trim().equalsIgnoreCase("FALSE"))
		    ? Boolean.FALSE : Boolean.TRUE;
	    }
	}

	// Data element
	Node data = _findChild(root, "Data");
	if (data != null) {
	    Node node;
	    String value;

	    // Bits per sample
	    node = _findChild(data, TAG_BITS_PER_SAMPLE);
	    if (node != null &&
		(value = _getAttribute(node, "value")) != null) {

		bitsPerSample = _parseIntList(value.trim());

		// Check if we have the right # of ints
		if (bitsPerSample.length == 0 ||
		    (numChannels != null &&
		     numChannels.intValue() != bitsPerSample.length)) {
		    bitsPerSample = null;
		}
	    }

	    // Significant bits per sample
	    node = _findChild(data, TAG_SIGNIFICANT_BITS_PER_SAMPLE);
	    if (node != null &&
		(value = _getAttribute(node, "value")) != null) {

		significantBitsPerSample = _parseIntList(value.trim());

		// Check if we have the right # of ints
		if (significantBitsPerSample.length == 0 ||
		    (numChannels != null &&
		     numChannels.intValue() !=
		     significantBitsPerSample.length)) {
		    significantBitsPerSample = null;
		}
	    }
	}

	// Dimension element
	Node dimension = _findChild(root, "Dimension");
	if (dimension != null) {
	    Node node;
	    String value;

	    // Pixel aspect ratio
	    node = _findChild(dimension, TAG_PIXEL_ASPECT_RATIO);
	    if (node != null) {
		pixelAspectRatio = _parseDouble(_getAttribute(node, "value"));
	    }

	    // Image orientation
	    node = _findChild(dimension, TAG_IMAGE_ORIENTATION);
	    if (node != null &&
		(value = _getAttribute(node, "value")) != null) {

		value = value.trim();
		// Case sensitive match
		Set all = ImageRenderingHints.IMAGE_ORIENTATIONS;
		if (all.contains(value)) {
		    imageOrientation = value;
		}
		// Case insensitive match
		else {
		    for (Iterator it = all.iterator(); it.hasNext(); ) {
			String orient = (String)it.next();
			if (value.equalsIgnoreCase(orient)) {
			    imageOrientation = orient;
			    break;
			}
		    }
		}
	    }
	}

	// Get org_medtoolbox_jviewbox_2.0 tree
	List formatNames = metadata.getMetadataFormatNames() != null
	    ? Arrays.asList(metadata.getMetadataFormatNames())
	    : Collections.EMPTY_LIST;
	root = (formatNames != null &&
		formatNames.contains(FORMAT_ORG_MEDTOOLBOX_JVIEWBOX_2_0))
	    ? metadata.getAsTree(FORMAT_ORG_MEDTOOLBOX_JVIEWBOX_2_0)
	    : new IIOMetadataNode();

	// Lookup_table element
	Node lookupTable = _findChild(root, TAG_LOOK_UP_TABLE);
	if (lookupTable != null) {
	    // Find LUT type; only LINEAR is supported
	    String value = _getAttribute(lookupTable, "Type");
	    if (value != null && "LINEAR".equalsIgnoreCase(value.trim())) {

		// Find window and level
		Integer window = _parseInt(_getAttribute(lookupTable,
							 "Window"));
		Integer level = _parseInt(_getAttribute(lookupTable, "Level"));

		// Find LUT size
		// First see if Image_data->maximum exists
		Integer size = null;
		Node node = _findChild(root, TAG_IMAGE_DATA);
		if (node != null) {
		    size = _parseInt(_getAttribute(node, "maximum"));
		    if (size != null) {
			size = new Integer(size.intValue() + 1);
		    }
		}
		// Then try the largest of significantBitsPerSample,
		// followed by the largest of bitsPerSample
		if (size == null) {
		    int[] bits = significantBitsPerSample != null
			         ? significantBitsPerSample : bitsPerSample;
		    if (bits != null) {
			int max = -1;
			for (int i = 0; i < bits.length; i++) {
			    max = Math.max(max, bits[i]);
			}
			if (max > 0) {
			    size = new Integer(1 << max);
			}
		    }
		}

		// Create a default LUT
		if (window != null && level != null && size != null) {
		    LinearLookUpTable llut =
			new LinearLookUpTable(size.intValue());
		    llut.setWindowLevel(window.intValue(), level.intValue());
		    defaultLookUpTable = llut;
		}
	    }
	}

	return new DefaultImageRenderingHints(colorSpaceType,
					      numChannels,
					      blackIsZero,
					      defaultLookUpTable,
					      bitsPerSample,
					      significantBitsPerSample,
					      pixelAspectRatio,
					      imageOrientation);
    }


    /**
     * Creates an <code>ImageRenderingHints</code> using information from
     * the supplied <code>Map</code> which maps <code>TAG_*</code> to their
     * corresponding values when available. If an exact match of
     * <code>TAG_*</code> is not found, this method will try case-insensitive
     * suffix match.
     * <p>
     * The values may be in their corresponding types, e.g.,
     * <code>Integer</code> for <code>TAG_COLOR_SPACE_TYPE</code>, or as
     * <code>String</code>s which will be parsed for the actual values.
     * <p>
     * <b>Exceptions:</b>
     * <ul>
     * <li><code>TAG_LOOK_UP_TABLE</code><br>
     *     Per <a href="#jviewbox_DTD">
     *     <code>org_medtoolbox_jviewbox_2.0 DTD</code></a>, the look-up
     *	   table element has three properties: type, window, and level. The
     *	   supplied <code>Map</code> should provide separate mappings for the
     *     three properties with keys formed by appending "->Type",
     *     "->Window", and "->Level" to <code>TAG_LOOK_UP_TABLE</code>
     *     respectively. Alternatively, the <code>Map</code> can provide a
     *	   single mapping to <code>TAG_LOOK_UP_TABLE</code> that returns an
     *	   instance of <code>LookUpTable</code>.</li>
     * <li><code>TAG_IMAGE_DATA</code><br>
     *     Per <a href="#jviewbox_DTD">
     *     <code>org_medtoolbox_jviewbox_2.0 DTD</code></a>, the image data
     *	   element has two properties: maximum and minimum. The supplied
     *	   <code>Map</code> should provide separate mappings for the two
     *	   properties with keys formed by appending "->maximum" and
     *     "->minimum" to <code>TAG_IMAGE_DATA</code> respectively.</li>
     * </ul>
     *
     * @param map <code>Map</code> which contains rendering hints.
     *
     * @return <code>ImageRenderingHints</code> created using information from
     *	       the supplied <code>Map</code>.
     */
    public static ImageRenderingHints createHints(Map map)
    {
	// Hints to be found from the map
	Integer colorSpaceType = null;
	Integer numChannels = null;
	Boolean blackIsZero = null;
	LookUpTable defaultLookUpTable = null;
	int[] bitsPerSample = null;
	int[] significantBitsPerSample = null;
	Double pixelAspectRatio = null;
	String imageOrientation = null;

	Object o;
	// Color space type
	o = _getEndingWith(map, TAG_COLOR_SPACE_TYPE);
	if (o != null) {
	    if (o instanceof Integer) {
		colorSpaceType = (Integer)o;
	    }
	    else if (o instanceof String) {
		String value = ((String)o).trim().toUpperCase();
		colorSpaceType = (Integer)
		    COLOR_SPACE_NAME_ALL_CAPS_TO_TYPE_INT.get(value);
	    }
	}

	// Number of channels
	numChannels = _parseInt(_getEndingWith(map, TAG_NUM_CHANNELS));

	// Black is zero
	o = _getEndingWith(map, TAG_BLACK_IS_ZERO);
	if (o != null) {
	    if (o instanceof Boolean) {
		blackIsZero = (Boolean)o;
	    }
	    else if (o instanceof String) {
		String value = ((String)o).trim();
		// Since the default is TRUE per DTD, only check FALSE
		blackIsZero = (value != null &&
			       value.trim().equalsIgnoreCase("FALSE"))
		    ? Boolean.FALSE : Boolean.TRUE;
	    }
	}

	// Bits per sample
	o = _getEndingWith(map, TAG_BITS_PER_SAMPLE);
	if (o instanceof int[]) {
	    bitsPerSample = (int[])o;
	}
	else if (o instanceof String) {
	    bitsPerSample = _parseIntList(((String)o).trim());
	}
	// Check if we have numChannel of ints
	if (bitsPerSample != null &&
	    (bitsPerSample.length == 0 ||
	     (numChannels != null &&
	      numChannels.intValue() != bitsPerSample.length))) {
	    bitsPerSample = null;
	}

	// Significant bits per sample
	o = _getEndingWith(map, TAG_SIGNIFICANT_BITS_PER_SAMPLE);
	if (o instanceof int[]) {
	    significantBitsPerSample = (int[])o;
	}
	else if (o instanceof String) {
	    significantBitsPerSample = _parseIntList(((String)o).trim());
	}
	// Check if we have numChannel of ints
	if (significantBitsPerSample != null &&
	    (significantBitsPerSample.length == 0 ||
	     (numChannels != null &&
	      numChannels.intValue() != significantBitsPerSample.length))){
	    significantBitsPerSample = null;
	}

	// Pixel aspect ratio
	o = _getEndingWith(map, TAG_PIXEL_ASPECT_RATIO);
	pixelAspectRatio = _parseDouble(o);

	// Image orientation
	o = _getEndingWith(map, TAG_IMAGE_ORIENTATION);
	if (o != null && o instanceof String) {
	    String value = ((String)o).trim();
	    // Case sensitive match
	    Set all = ImageRenderingHints.IMAGE_ORIENTATIONS;
	    if (all.contains(value)) {
		imageOrientation = value;
	    }
	    else {
		// Case insensitive match
		for (Iterator it = all.iterator(); it.hasNext(); ) {
		    String orient = (String)it.next();
		    if (value.equalsIgnoreCase(orient)) {
			imageOrientation = orient;
			break;
		    }
		}
	    }
	}

	// Default look-up table
	// First see if TAG_LOOK_UP_TABLE maps to a LookUpTable
	o = _getEndingWith(map, TAG_LOOK_UP_TABLE);
	if (o instanceof LookUpTable) {
	    defaultLookUpTable = (LookUpTable)o;
	}
	// Otherwise, look for Lookup_table->Type, Window, and Level
	else {
	    // Find LUT type; only LINEAR is supported
	    o = _getEndingWith(map, TAG_LOOK_UP_TABLE + "->Type");
	    if (o instanceof String &&
		"LINEAR".equalsIgnoreCase(((String)o).trim())) {

		// Find window and level
		o = _getEndingWith(map, TAG_LOOK_UP_TABLE + "->Window");
		Integer window = _parseInt(o);
		o = _getEndingWith(map, TAG_LOOK_UP_TABLE + "->Level");
		Integer level = _parseInt(o);

		// Find LUT size
		// First see if Image_data->maximum exists
		o = _getEndingWith(map, TAG_IMAGE_DATA + "->maximum");
		Integer size = _parseInt(o);
		if (size != null) {
		    size = new Integer(size.intValue() + 1);
		}
		// Then try the largest of significantBitsPerSample,
		// followed by the largest of bitsPerSample
		if (size == null) {
		    int[] bits = significantBitsPerSample != null
			         ? significantBitsPerSample : bitsPerSample;
		    if (bits != null) {
			int max = -1;
			for (int i = 0; i < bits.length; i++) {
			    max = Math.max(max, bits[i]);
			}
			if (max > 0) {
			    size = new Integer(1 << max);
			}
		    }
		}

		// Create a default LUT
		if (window != null && level != null && size != null) {
		    LinearLookUpTable llut =
			new LinearLookUpTable(size.intValue());
		    llut.setWindowLevel(window.intValue(), level.intValue());
		    defaultLookUpTable = llut;
		}
	    }
	}

	return new DefaultImageRenderingHints(colorSpaceType,
					      numChannels,
					      blackIsZero,
					      defaultLookUpTable,
					      bitsPerSample,
					      significantBitsPerSample,
					      pixelAspectRatio,
					      imageOrientation);
    }

    /**
     * Creates an <code>ImageRenderingHints</code> using information from
     * the specified <code>BufferedImage</code>'s properties. This method
     * looks for the same set of key-value pairs as {@link #createHints(Map)}
     * does.
     *
     * @param map <code>Map</code> which contains rendering hints.
     *
     * @return <code>ImageRenderingHints</code> created using information from
     *	       the specified <code>BufferedImage</code>'s properties.
     *
     * @see #createHints(Map)
     */
    public static ImageRenderingHints createHints(BufferedImage image)
    {
	return createHints(_createPropertyMap(image));
    }

    // ---------------
    // Private methods
    // ---------------


    /** Finds the (first) child node with the specified name, ignoring case. */
    private static Node _findChild(Node node, String name)
    {
	NodeList children = node.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node child = children.item(i);
	    if (name.equalsIgnoreCase(child.getNodeName())) {
		return child;
	    }
	}

	// Not found
	return null;
    }

    /** Returns the value of a named attribute if exists. */
    private static String _getAttribute(Node node, String name)
    {
	NamedNodeMap attributes = node.getAttributes();
	if (attributes != null) {
	    Node attribute = attributes.getNamedItem(name);
	    return (attribute == null) ? null : attribute.getNodeValue();
	}

	// No attributes available
	return null;
    }


    /**
     * Parses the specified <code>Object</code> for an <code>Integer</code>
     * value, in one of the following ways applied in the respective order:
     * treat <code>o</code> as an <code>Integer</code> if applicable, treat
     * <code>o</code> as a <code>Number</code> and get its int value if
     * applicable, or treat <code>o</code> as a <code>String</code> and parse
     * it for the integer value if applicable. Returns <code>null</code> if
     * none of the above applies.
     */
    static Integer _parseInt(Object o)
    {
	if (o instanceof Integer) {
	    return (Integer)o;
	}
	else if (o instanceof Number) {
	    return new Integer(((Number)o).intValue());
	}
	else if (o instanceof String) {
	    try {
		return Integer.valueOf(((String)o).trim());
	    }
	    catch (NumberFormatException e) {}
	}

	return null;
    }

    /**
     * Parses the specified <code>Object</code> for an <code>Double</code>
     * value, in one of the following ways applied in the respective order:
     * treat <code>o</code> as an <code>Double</code> if applicable, treat
     * <code>o</code> as a <code>Number</code> and get its double value if
     * applicable, or treat <code>o</code> as a <code>String</code> and parse
     * it for the double value if applicable. Returns <code>null</code> if
     * none of the above applies.
     */
    static Double _parseDouble(Object o)
    {
	if (o instanceof Double) {
	    return (Double)o;
	}
	else if (o instanceof Number) {
	    return new Double(((Number)o).doubleValue());
	}
	else if (o instanceof String) {
	    try {
		return Double.valueOf(((String)o).trim());
	    }
	    catch (NumberFormatException e) {}
	}

	return null;
    }

    /** Parses a list of integer in String representation. */
    static int[] _parseIntList(String s)
    {
	// Parse the list into a Vector
	Vector v = new Vector();
	for (int i = 0; i < s.length(); i++) {
	    // Find the first non-decimal-digit character
	    int j = i;
	    for (; j < s.length(); j++) {
		char c = s.charAt(j);
		if (c < '0' || c > '9') {
		    break;
		}
	    }

	    // Parse what's in between as one integer
	    try {
		v.add(Integer.valueOf(s.substring(i, j)));
	    }
	    catch (NumberFormatException e) {}

	    // Advance to the next decimal-digit character
	    for (j++; j < s.length(); j++) {
		char c = s.charAt(j);
		if (c >= '0' && c <= '9') {
		    break;
		}
	    }
	    i = j;
	}

	// Convert the Vector into int[]
	int[] retval = new int[v.size()];
	for (int i = 0; i < retval.length; i++) {
	    retval[i] = ((Integer)v.get(i)).intValue();
	}

	return retval;
    }

    /**
     * Searches a <code>Map</code> for a mapping whose key either matches
     * the specified String, or if no exact match, ends with the specified
     * String (has the specified String as suffix) case-insensitively.
     * <p>
     * There can be more than one keys that satisfy the suffix matching in
     * the <code>Map</code>. The first such mapping returned by the
     * <code>Iterator</code> of the <code>Map</code>'s entry set will be
     * returned by this method.
     */
    static Object _getEndingWith(Map map, String key)
    {
	// Try exact match first
	Object o = map.get(key);
	if (o != null) {
	    return o;
	}

	// Brute force linear search
	for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry)it.next();
	    Object k = e.getKey();
	    if (k instanceof String &&
		((String)k).trim().toLowerCase().endsWith(key.toLowerCase())) {
		return e.getValue();
	    }
	}

	// Not found
	return null;
    }

    /**
     * Collects all properties of a <code>BufferedImage</code> in a
     * <code>Map</code>.
     */
    static Map _createPropertyMap(BufferedImage image)
    {
	String[] names = image.getPropertyNames();
	if (names == null || names.length == 0) {
	    return Collections.EMPTY_MAP;
	}

	Map map = new HashMap(names.length * 4 / 3);
	for (int i = 0; i < names.length; i++) {
	    Object o = image.getProperty(names[i]);
	    if (o != null && !Image.UndefinedProperty.equals(o)) {
		map.put(names[i], o);
	    }
	}

	return map;
    }
}
