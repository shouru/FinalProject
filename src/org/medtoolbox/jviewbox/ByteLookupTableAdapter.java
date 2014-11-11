/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox;

import java.awt.image.ByteLookupTable;

/**
 * Adapter for encapsulating a <code>java.awt.image.ByteLookupTable</code> in
 * a jViewBox <code>LookUpTable</code>.
 *
 * @version January 8, 2004
 */
public class ByteLookupTableAdapter implements LookUpTable
{
    // --------------
    // Private fields
    // --------------

    /** Encapsulated ByteLookupTable. */
    private ByteLookupTable _table;

    /** Whether to copy the ByteLookupTable when cloning. */
    private final boolean _copiesTable;

    // ----------------------
    // Constructor and cloner
    // ----------------------

    /**
     * Constructs a <code>LookUpTable</code> by adapting an existing
     * <code>ByteLookupTable</code>. By default the LookUpTable will make
     * copies of the embedded <code>ByteLookupTable</code> when
     * <code>clone</code>d.
     *
     * @param table ByteLookupTable out of which to construct a LookUpTable.
     *
     * @throws NullPointerException if <code>table</code> is <code>null</code>.
     */
    public ByteLookupTableAdapter(ByteLookupTable table)
    {
	this(table, true);
    }

    /**
     * Constructs a <code>LookUpTable</code> by adapting an existing
     * <code>ByteLookupTable</code>.
     *
     * @param table ByteLookupTable out of which to construct a LookUpTable.
     * @param copiesTable Whether to copy the embedded ByteLookupTable when
     *			  <code>clone</code>d.
     *
     * @throws NullPointerException if <code>table</code> is <code>null</code>.
     */
    public ByteLookupTableAdapter(ByteLookupTable table, boolean copiesTable)
    {
	if (table == null) {
	    throw new NullPointerException("table can not be null.");
	}
	_table = table;
	_copiesTable = copiesTable;
    }

    /**
     * Returns a clone of this LookUpTable. The clone contains a reference
     * to either the original embedded ByteLookupTable or a completely
     * indepedent copy, depending on the option chosen at construction time.
     * The clone inherits the same copy option from the original instance.
     *
     * @return Copy of this LookUpTable.
     *
     * @see #ByteLookupTableAdapter(ByteLookupTable, boolean)
     */
    public Object clone()
    {
	try {
	    ByteLookupTableAdapter copy =
		(ByteLookupTableAdapter)super.clone();

	    // Clone the ByteLookupTable if applicable
	    if (_copiesTable) {
		// Clone the byte array and create a new ByteLookupTable
		byte[][] array = _table.getTable();
		// Single-band look up table?
		if (array.length == 1) {
		    byte[] arraycopy = (byte[])array[0].clone();
		    copy._table = new ByteLookupTable(_table.getOffset(),
						      arraycopy);
		}

		// Multi-band look up table
		else {
		    byte[][] arraycopy = new byte[array.length][];
		    for (int i = 0; i < array.length; i++) {
			arraycopy[i] = (byte[])array[i].clone();
		    }
		    copy._table = new ByteLookupTable(_table.getOffset(),
						      arraycopy);
		}
	    }

	    return copy;
	}
	catch (CloneNotSupportedException e) {
	    // Should never happen; we could have an assertion here
	    throw new InternalError("Failed to clone ByteLookupTableAdapter.");
	}
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Returns the number of components (bands) in this LookUpTable.
     *
     * @return Number of components (bands) in this LookUpTable.
     */
    public int getNumComponents()
    {
	return _table.getNumComponents();
    }

    /**
     * Returns the offset of this LookUpTable, which is subtracted from input
     * values before table lookup. The same offset applies to all bands.
     * <p>
     * <b>Note:</b> an non-zero offset is likely to seriously hurt 
     * the performance of table look-up.
     *
     * @return Offset of this LookUpTable.
     */
    public int getOffset()
    {
	return _table.getOffset();
    }

    /**
     * Returns the size of this LookUpTable, which is the number of entries
     * available in the table. The same size should apply to all bands, though
     * <code>java.awt.image.ByteLookupTable</code> does not have this
     * restriction. The actual range of valid input values is from offset to
     * offset + size - 1.
     * <p>
     * This adapter returns the minimum of the sizes of all bands in the
     * embedded <code>ByteLookupTable</code>.
     *
     * @return Size of this LookUpTable, the minimum of the sizes of all bands
     *	       in the embedded <code>ByteLookupTable</code>.
     */
    public int getSize()
    {
	int size = -1;
	byte[][] array = _table.getTable();
	for (int i = 0; i < array.length; i++) {
	    if (size < 0) {
		size = array[i].length;
	    }
	    else {
		size = Math.min(size, array[i].length);
	    }
	}

	return size;
    }

    /**
     * Returns this look-up table in the form of a
     * <code>java.awt.image.ByteLookupTable</code>.
     * <p>
     * This adapter simply returns the embedded <code>ByteLookupTable</code>.
     *
     * @return This look-up table in the form of a
     *	       <code>java.awt.image.ByteLookupTable</code>.
     */
    public ByteLookupTable getByteLookupTable()
    {
	return _table;
    }

    /**
     * Flushes all disposable resource used by this LookUpTable. By disposble,
     * it means everything not necessary in creating/computing the actual
     * <code>ByteLookupTable</code>. For example, if the look-up table is 
     * defined by a function of a few parameters, only the parameter values 
     * need to be kept in order to compute the table.
     * <p>
     * This adapter does nothing on <code>flush</code>.
     */
    public void flush()
    {
    }
}
