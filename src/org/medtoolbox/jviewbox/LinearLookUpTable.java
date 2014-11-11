/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox;

import java.awt.image.ByteLookupTable;

/**
 * LookUpTable which implements a linear look up table (LUT) to map a
 * range of integer values to a range of unsigned byte values.  The terms
 * <i>window</i> and <i>level</i> are quantities which define this mapping.
 * <p>
 * For example, the pixel values of a typical CT (<i>Computed Tomography</i>)
 * image can be represented using 12 bits (0-4095).  In order to show this
 * range of values on an 8-bit gray scale monitor (0-255), there must be a
 * mapping from the image pixel values to those supported by the monitor.
 * <p>
 * Here, the mapping is done (piecewise) linearly:
 *
 * <pre>
 *.                    <-----> Window
 *.          ^
 *.     255  |               ************
 *.          |              *
 *.          |             *
 *.          |            *
 *.          |           *
 *.          |          *
 *.          |         *
 *.       0  **********---|--------------->
 *.          0          Level           4095
 * </pre>
 * Therefore the linear mapping is defined as follows:<ul>
 * <li> All integer values less than <code>Level - Window/2</code>
 *      are mapped to zero.</li>
 * <li> All integer values between <code>Level - Window/2</code>
 *      and <code>Level + Window/2</code> are mapped linearly to the
 *      full range of output values.</li>
 * <li> All integer values greater than <code>Level + Window/2</code>
 *      are mapped to the maximum output value, 255.</li>
 * <li> When the mapping is <b>inverted</b>, the output values are
 *      given by subtracting the values calculated as above from 255.
 *	In other words, it gives 255 for inputs less than
 *      <code>Level - Window/2</code>, 0 for inputs greater than
 *      <code>Level + Window/2</code>, and linear in between.</li>
 * <li> The only restriction on <code>Window</code> and <code>Level</code>
 *	values is <code>Window >= 0</code>.
 * </ul>
 *
 * @version January 8, 2004
 */
public class LinearLookUpTable implements LookUpTable
{
    // ----------------
    // Protected fields
    // ----------------

    /** Array which maps integer values to byte values. */
    protected byte[] _table;

    // --------------
    // Private fields
    // --------------

    /** Size of the array which maps integer values to byte values. */
    private final int _tableSize;

    /** Window value. */
    private int _window;

    /** Level value. */
    private int _level;

    /** True if the inverse of the linear mapping is to be used. */
    private boolean _isInverted = false;

    /** ByteLookupTable to return in getByteLookupTable(). */
    private ByteLookupTable _blut;

    // ----------------------
    // Constructor and cloner
    // ----------------------

    /**
     * Constructs a LinearLookUpTable of the specified size.
     *
     * @param size Size of the LUT array table.
     *
     * @throws IllegalArgumentException If <code>size</code> is invalid.
     */
    public LinearLookUpTable(int tableSize)
    {
	// Check the size
	if (tableSize < 1) {
	    throw new IllegalArgumentException("LinearLookUpTable: A size of "+
					       tableSize + " is not allowed " +
					       "in the constructor.");
	}

	_tableSize = tableSize;

	// Set default window and level values
	_window = tableSize;
	_level = tableSize / 2;
    }

    /**
     * Returns a clone of this LinearLookUpTable.  The copy contains a
     * reference to a clone of the LUT array table, not a reference to the
     * original array table.
     *
     * @return Copy of this LinearLookUpTable, or null on error.
     */
    public Object clone()
    {
	try {
	    LinearLookUpTable lut = (LinearLookUpTable)super.clone();

	    // Let the clone create its own array on demand
	    lut._table = null;
	    lut._blut = null;

	    return lut;
	}
	catch (CloneNotSupportedException e) {
	    // Should never happen
	    throw new InternalError("Fail to clone a LinearLookUpTable.");
	}
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the number of components (bands) in this LookUpTable, which is
     * always one.
     *
     * @return Number of components (bands) in this LookUpTable, i.e., 1.
     */
    public int getNumComponents()
    {
	return 1;
    }

    /**
     * Returns the offset of this LookUpTable, which is always 0.
     *
     * @return Offset of this LookUpTable, i.e., 0.
     */
    public int getOffset()
    {
	return 0;
    }

    /**
     * Returns the size of this LookUpTable, which is the number of entries
     * available in the table. The same size should apply to all bands, though
     * <code>java.awt.image.ByteLookupTable</code> does not have this
     * restriction. The actual range of valid input values is from offset to
     * offset + size - 1.
     *
     * @return Size of this LookUpTable.
     */
    public int getSize()
    {
	return _tableSize;
    }

    /**
     * Returns the window value.
     *
     * @return Window value, which is the range of image pixel values that
     *         is linearly mapped.
     */
    public int getWindow()
    {
	return _window;
    }

    /**
     * Returns the level value.
     *
     * @return Level value, which is the midpoint of the range of image pixel
     *         values that is linearly mapped.
     */
    public int getLevel()
    {
	return _level;
    }

    /**
     * Sets the window and level values.
     *
     * @param window Window value, which is the range of image pixel values
     *		     that is linearly mapped. Any negative value will be
     *		     treated as 0.
     * @param level Level value, which is the midpoint of the range of image
     *              pixel values that is linearly mapped.
     */
    public void setWindowLevel(int window, int level)
    {
	// Allocate memory for the table on first call
	if (_table == null) {
	    _table = new byte[_tableSize];
	}

	// Check bounds on new window and level fractional values
	_window = (window < 0) ? 0 : window;
	_level = level;

	// Calculate the window/level slope and intercept
	double slope = 0.0;
	double intercept = 0.0;
	if (_window > 0) {
	    slope = 255.0 / _window;
	    // Mathematically, the intercept should be 127.5
	    // However, 128.0 produces a rounding effect because later the
	    // floating-point results are cast, i.e., floored to byte values,
	    // which is actually how we prefer
	    intercept = 128.0 - 255.0 * (double)_level / _window;
	}

	// Fill the LUT with the mapped values
	int start = _level - _window / 2;
	int end   = _level + _window / 2;
	for (int pixel = 0; pixel < _tableSize; pixel++) {

	    // Pixel is before the linear ramp
	    if (pixel <= start) {
		_table[pixel] = 0;
	    }

	    // Pixel is after the linear ramp
	    else if (pixel >= end) {
		_table[pixel] = (byte)255;
	    }

	    // Pixel is on the linear ramp
	    else {
		// Casting from double to byte is done by flooring
		// We already adjust the intercept by +0.5 for rounding effect
		_table[pixel] = (byte)(slope * pixel + intercept);
	    }
	}

	// Invert the pixel if required
	if (_isInverted) {
	    for (int i = 0; i < _tableSize; i++) {
		// Bitwise inversion is equivalent to subtracting from 255
		_table[i] = (byte)(~_table[i]);
	    }
	}
    }

    /**
     * Returns whether or not the inverse of the linear mapping is to be used.
     *
     * @return <code>true</code> if each byte value of this linear map is
     *	       inverted; <code>false</code> otherwise.
     */
    public boolean isInverted()
    {
	return _isInverted;
    }

    /**
     * Sets whether or not the inverse of the linear mapping is to be used.
     *
     * @param isInverted <code>true</code> if each byte value of this linear
     *			 map is to be inverted; <code>false</code> if not.
     */ 
    public void setInverted(boolean isInverted)
    {
	// Recalculate the byte array if needed
	if (_isInverted != isInverted) { 
	    _isInverted = isInverted;
	    setWindowLevel(_window, _level);
	}
    }

    /**
     * Returns the byte array defined by the LUT. The return value is a
     * reference to the actual look-up table array, i.e., writing to it will
     * affect this LUT.
     *
     * @return Array which maps integer values to byte values.
     */
    public byte[] getArray()
    {
	if (_table == null) {
	    setWindowLevel(_window, _level);
	}

	return _table;
    }

    /**
     * Returns this look-up table in the form of a
     * <code>java.awt.image.ByteLookupTable</code>.
     *
     * @return This look-up table in the form of a
     *	       <code>java.awt.image.ByteLookupTable</code>.
     */
    public ByteLookupTable getByteLookupTable()
    {
	if (_blut == null) {
	    _blut = new ByteLookupTable(0, getArray());
	}

	return _blut;
    }

    /**
     * Flushes all disposable resource used by this LookUpTable. By disposble,
     * it means everything not necessary in creating/computing the actual
     * <code>ByteLookupTable</code>. For example, if the look-up table is 
     * defined by a function of a few parameters, only the parameter values 
     * need to be kept in order to compute the table.
     */
    public void flush()
    {
	_table = null;
	_blut = null;
    }
}
