/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.*;
import javax.swing.*;

/**
 * Class for adding multi-line capability to the UI.  This class was downloaded
 * to supplement the JFC classes.
 *
 * @see <a href="http://www2.gol.com/users/tame/swing/examples/JToolTipExamples1.html">JToolTipExamples1.html</a>
 */
class MultiLineToolTip extends JToolTip
{
    public MultiLineToolTip() {  
	setUI( new MultiLineToolTipUI() );
    }
}
