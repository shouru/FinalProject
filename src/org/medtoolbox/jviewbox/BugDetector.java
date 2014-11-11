/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.LookupOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Class for detecting 2D graphics bugs in the Java Runtime Environment.
 *
 * @version January 8, 2004
 */
public class BugDetector
{
    // --------------
    // Private fields
    // --------------

    /** Whether the Java Runtime Environment has bug #4192198. */
    private static Boolean _hasSunBug4192198;

    /** Whether the Java Runtime Environment has bug #4554571. */
    private static Boolean _hasSunBug4554571;

    // -----------
    // Constructor
    // -----------

    /** This class is non-instantiable. */
    private BugDetector()
    {
	throw new UnsupportedOperationException("Non-instantiable class.");
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Determines whether or not the Java Runtime Environment has the bug with
     * the ID #4192198 in Sun's Bug Database. This bug affects the operation
     * of <code>java.awt.image.AffineTransformOp</code> on short (signed or
     * unsigned) data.
     *
     * @return <code>true</code> if the Java Runtime Environment has the bug
     *	       #4192198; <code>false</code> otherwise.
     */
    public static final synchronized boolean hasSunBug4192198()
    {
	// If already tested for the bug, return the result
	if (_hasSunBug4192198 != null) {
	    return _hasSunBug4192198.booleanValue();
	}

	// Create a small 2x2 unsigned short raster
	short[] data = {1001, 1002, 2001, 2002};
	DataBuffer db = new DataBufferUShort(data, data.length);
	WritableRaster raster =
	    Raster.createInterleavedRaster(db, 2, 2, 2, 1, new int[1], null);

	// Construct an AffineTransformOp that scales by a factor of two
	AffineTransform at = new AffineTransform();
	at.setToScale(2.0, 2.0);
	AffineTransformOp atOp =
	    new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

	// Create a scaled raster using the affine transform
	WritableRaster scaledRaster =
	    Raster.createInterleavedRaster(DataBuffer.TYPE_USHORT, 4, 4,
					   4, 1, new int[1], null);
	atOp.filter(raster, scaledRaster);

	// Verify each element of the scaled raster
	for (int x = 0; x < 4; x++) {
	    for (int y = 0; y < 4; y++) {
		if (raster.getSample(x / 2, y / 2, 0) !=
		    scaledRaster.getSample(x, y, 0) ) {

		    // Found data that has been incorrectly scaled
		    _hasSunBug4192198 = Boolean.TRUE;
		    return true;
		}
	    }
	}

	// Affine transform Op is working correctly
	_hasSunBug4192198 = Boolean.FALSE;
	return false;
    }

    /**
     * Determines whether or not the Java Runtime Environment has the bug with
     * the ID #4554571 in Sun's Bug Database. This bug affects the operation
     * of <code>java.awt.image.LookupOp</code> on short (signed or unsigned)
     * data.
     *
     * @return <code>true</code> if the Java Runtime Environment has the bug
     *	       #4554571; <code>false</code> otherwise.
     */
    public static final synchronized boolean hasSunBug4554571()
    {
	// If already tested for the bug, return the result
	if (_hasSunBug4554571 != null) {
	    return _hasSunBug4554571.booleanValue();
	}

	// Create a small 2x2 unsigned short raster
	WritableRaster raster =
	    Raster.createInterleavedRaster(DataBuffer.TYPE_USHORT, 2, 2, 1,
					   null);

	// Give the raster some values
	for (int x = 0; x < 2; x++) {
	    for (int y = 0; y < 2; y++) {
		raster.setSample(x, y, 0, 5*x + 3*y);
	    }
	}

	// Create a LookUpOp for the raster which maps to reversed values
	byte[] lookUpTable = new byte[5*1 + 3*1 + 1];
	for (int i = 0; i < lookUpTable.length; i++) {
	    lookUpTable[i] = (byte)(lookUpTable.length - i - 1);
	}
	ByteLookupTable blut = new ByteLookupTable(0, lookUpTable);
	LookupOp op = new LookupOp(blut, null);

	// Create a transformed raster using the look up operation
	WritableRaster transformedRaster =
	    Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 2, 2, 1,
					   null);

	op.filter(raster, transformedRaster);

	// Verify each element of the transformed raster
	for (int x = 0; x < 2; x++) {
	    for (int y = 0; y < 2; y++) {
		if ( lookUpTable[raster.getSample(x, y, 0)] !=
		     transformedRaster.getSample(x, y, 0) ) {

		    // Found data that has been incorrectly mapped
		    _hasSunBug4554571 = Boolean.TRUE;
		    return true;
		}
	    }
	}

	// Look up Op is working correctly
	_hasSunBug4554571 = Boolean.FALSE;
	return false;
    }

    // ----
    // Main
    // ----

    /** Command-line tool for bug detection. */
    public static void main(String[] args)
    {
	if (hasSunBug4192198()) {
	    System.out.println("The Java Runtime you are using is determined "+
			       "to have bug #4192198 as listed in " +
			       "Sun's bug database.\n");
	    System.out.println("This bug affects affine transform operation " +
			       "on images with short or unsigned short " +
			       "pixels, e.g., 10, 12, and 16 bit grayscale " +
			       "images.\n");
	}

	if (hasSunBug4554571()) {
	    System.out.println("The Java Runtime you are using is determined "+
			       "to have bug #4554571 as listed in " +
			       "Sun's bug database.\n");
	    System.out.println("This bug affects table look-up operation on " +
			       "images with short or unsigned short pixels, " +
			       "e.g., 10, 12, and 16 bit grayscale images.\n");
	}

	if (hasSunBug4192198() ^ hasSunBug4554571()) {
	    System.out.println("This version of jViewBox contains code to " +
			       "work around this bug, which has been tested " +
			       "to work for many different platforms/OS.");
	}
	else if (hasSunBug4192198() && hasSunBug4554571()) {
	    System.out.println("Both bugs are found. In this case, jViewBox " +
			       "is not likely to function properly.");
	}
	else {
	    System.out.println("No bug is found.");
	}
    }
}
