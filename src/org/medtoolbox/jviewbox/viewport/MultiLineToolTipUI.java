/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino


See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;

/**
 * Class for adding multi-line capability to the UI.  This class was downloaded
 * to supplement the JFC classes.
 *
 * @see <a href="http://www2.gol.com/users/tame/swing/examples/JToolTipExamples1.html">JToolTipExamples1.html</a>
 */
class MultiLineToolTipUI extends MetalToolTipUI
{
    private String[] strs;
    private int maxWidth = 0;

    public void paint(Graphics g, JComponent c) {
	FontMetrics metrics = g.getFontMetrics(g.getFont());
	Dimension size = c.getSize();
	g.setColor(c.getBackground());
	g.fillRect(0, 0, size.width, size.height);
	g.setColor(c.getForeground());
	if (strs != null) {
	    for (int i=0;i<strs.length;i++) {
		g.drawString(strs[i], 3, (metrics.getHeight()) * (i+1));
	    }
	}
    }

    public Dimension getPreferredSize(JComponent c) {
	FontMetrics metrics = c.getFontMetrics(c.getFont());
	String tipText = ((JToolTip)c).getTipText();
	if (tipText == null) {
	    tipText = "";
	}
	BufferedReader br = new BufferedReader(new StringReader(tipText));
	String line;
	int maxWidth = 0;
	Vector v = new Vector();
	try {
	    while ((line = br.readLine()) != null) {
		int width = SwingUtilities.computeStringWidth(metrics,line);
		maxWidth = (maxWidth < width) ? width : maxWidth;
		v.addElement(line);
	    }
	} catch (IOException ex) {
	    ex.printStackTrace();
	}   
	int lines = v.size();
	if (lines < 1) {
	    strs = null;
	    lines = 1;
	} else {
	    strs = new String[lines];
	    int i=0;
	    for (Enumeration e = v.elements(); e.hasMoreElements() ;i++) {
		strs[i] = (String)e.nextElement();
	    }
	}
	int height = metrics.getHeight() * lines;
	this.maxWidth = maxWidth;
	return new Dimension(maxWidth + 6, height + 4);
    }
}
