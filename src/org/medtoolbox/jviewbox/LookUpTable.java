/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox;

import java.awt.image.ByteLookupTable;

/**
 * Interface for a look-up table (LUT) which maps integral values (byte, short
 * and int) to byte values. A mathematical definition of the mapping follows:
 * <p>
 * Let <tt>I,n(x,y)</tt> be the <tt>n</tt>th band of the input pixel value,
 * <tt>O,n(x,y)</tt> the <tt>n</tt>th band of the output pixel value,
 * <tt>T,n</tt> the <tt>n</tt>th band of the LUT, and <tt>off</tt> the LUT
 * offset. Then
 * <br><pre>
 * O,n(x,y) = T,n[I,n(x,y) - off]</pre>
 *
 * An LUT should have either as many bands as the input images or just one.
 * When an LUT has just one band, the same table applies to all bands of the
 * input images.
 *
 * @version January 8, 2004
 */
public interface LookUpTable extends Cloneable
{
    /**
     * Returns the number of components (bands) in this LookUpTable.
     *
     * @return Number of components (bands) in this LookUpTable.
     */
    public int getNumComponents();

    /**
     * Returns the offset of this LookUpTable, which is subtracted from input
     * values before table lookup. The same offset applies to all bands.
     * <p>
     * <b>Note:</b> a non-zero offset is expected to have considerable effect
     * on the performance of table look-up.
     *
     * @return Offset of this LookUpTable.
     */
    public int getOffset();

    /**
     * Returns the size of this LookUpTable, which is the number of entries
     * available in the table. The same size should apply to all bands, though
     * <code>java.awt.image.ByteLookupTable</code> does not have this
     * restriction. The range of valid input values is from <code>offset</code>
     * to <code>offset + size - 1</code>.
     *
     * @return Size of this LookUpTable.
     */
    public int getSize();

    /**
     * Returns this look-up table in the form of a
     * <code>java.awt.image.ByteLookupTable</code>.
     *
     * @return This look-up table in the form of a
     *	       <code>java.awt.image.ByteLookupTable</code>.
     */
    public ByteLookupTable getByteLookupTable();

    /**
     * Flushes all disposable resource used by this LookUpTable. By disposble,
     * it means everything not necessary in creating/computing the actual
     * <code>ByteLookupTable</code>. For example, if the look-up table is 
     * defined by a function of a few parameters, only the parameter values 
     * need to be kept in order to compute the table.
     */
    public void flush();

    /**
     * Creates and returns a copy of this LookUpTable object.
     * <p>
     * This method is explicitly declared in this interface to enforce that
     * every implementing subclass implements a public <code>clone</code>
     * method. Any implementation is also forbidden by this declaration from
     * throwing <code>CloneNotSupportedException</code> or any other checked
     * exception.
     */
    public Object clone();
}
