/*
jViewBox 2.0 beta

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino

See README.license for license notices.
 */

package org.medtoolbox.jviewbox.viewport;

import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility class for accessing <code>MouseWheelEvent</code> through reflection
 * for backward compatibility with Java versions prior to 1.4.
 * <p>
 * By backward compatible, it means the methods in this class can be safely
 * referenced without running into any problem, at either compile or runtime,
 * whether or not using Java 1.4. Therefore a same source code may be used
 * for both Java 1.4 and prior versions. There is still <b>NO</b> mouse wheel
 * support in Java prior to 1.4. The methods in this class simply return
 * quietly with neither results nor side effects.
 * <p>
 * For example, when implementing <code>ViewportTool.mouseWheelMoved</code>,
 * explicit <code>MouseWheelEvent</code> reference and type casting such as
 * <pre>
 * public void mouseWheelMoved(ViewportCluster vpc, Viewport vp,
 *			       MouseEvent e)
 * {
 *     int rotation = ((MouseWheelEvent)e).getWheelRotation();
 *     ...</pre>
 *
 * is not backward compatible (e.g., it will not compile in 1.3.) Instead,
 * <pre>
 * public void mouseWheelMoved(ViewportCluster vpc, Viewport vp,
 *			       MouseEvent e)
 * {
 *     Integer rotation = MouseWheelEventHelper.getWheelRotation(e);
 *     ...</pre>
 *
 * is backward compatible with Java versions prior to 1.4. To be on the safe
 * side, <code>rotation</code> should be checked against <code>null</code> for
 * this helper may fail to reflectively access <code>e</code>, however
 * unlikely.
 *
 * @version January 8, 2004
 */
public class MouseWheelEventHelper
{
    // -----------
    // Constructor
    // -----------

    /** Non-instantiable class. */
    private MouseWheelEventHelper()
    {
	throw new UnsupportedOperationException("Non-instantiable class.");
    }

    // ---------
    // Constants
    // ---------

    /**
     * <code>MouseWheelEvent</code> class; <code>null</code> if not exists.
     */
    public static final Class CLASS_MOUSE_WHEEL_EVENT;

    /**
     * <code>MouseWheelEvent.getScrollType()</code> method;
     * <code>null</code> if not exists.
     */
    public static final Method METHOD_GET_SCROLL_TYPE;

    /** 
     * <code>MouseWheelEvent.getScrollAmount()</code> method;
     * <code>null</code> if not exists.
     */
    public static final Method METHOD_GET_SCROLL_AMOUNT;

    /**
     * <code>MouseWheelEvent.getWheelRotation()</code> method;
     * <code>null</code> if not exists.
     */
    public static final Method METHOD_GET_WHEEL_ROTATION;

    /**
     * <code>MouseWheelEvent.getUnitsToScroll()</code> method;
     * <code>null</code> if not exists.
     */
    public static final Method METHOD_GET_UNITS_TO_SCROLL;

    // Static initializer for the above reflection variables
    static {
	// Is there a MouseWheelEvent class?
	Class cMouseWheelEvent = null;
	try {
	    cMouseWheelEvent =
		Class.forName("java.awt.event.MouseWheelEvent");
	}
	catch (ClassNotFoundException e) {}

	// Identify MouseWheelEvent methods
	Method mGetScrollType = null;
	Method mGetScrollAmount = null;
	Method mGetWheelRotation = null;
	Method mGetUnitsToScroll = null;
	if (cMouseWheelEvent != null) {
	    try {
		Class[] noArg = new Class[0];
		mGetScrollType =
		    cMouseWheelEvent.getMethod("getScrollType", noArg);
		// Check return type
		if (!mGetScrollType.getReturnType().equals(int.class)) {
		    mGetScrollType = null;
		}

		mGetScrollAmount =
		    cMouseWheelEvent.getMethod("getScrollAmount", noArg);
		// Check return type
		if (!mGetScrollAmount.getReturnType().equals(int.class)) {
		    mGetScrollAmount = null;
		}

		mGetWheelRotation =
		    cMouseWheelEvent.getMethod("getWheelRotation", noArg);
		// Check return type
		if (!mGetWheelRotation.getReturnType().equals(int.class)) {
		    mGetWheelRotation = null;
		}

		mGetUnitsToScroll =
		    cMouseWheelEvent.getMethod("getUnitsToScroll", noArg);
		// Check return type
		if (!mGetUnitsToScroll.getReturnType().equals(int.class)) {
		    mGetUnitsToScroll = null;
		}
	    }
	    catch (NoSuchMethodException e) {}
	    catch (SecurityException e) {}
	}

	// All or nothing
	if (mGetScrollType != null && mGetScrollAmount != null &&
	    mGetWheelRotation != null && mGetUnitsToScroll != null) {

	    CLASS_MOUSE_WHEEL_EVENT = cMouseWheelEvent;
	    METHOD_GET_SCROLL_TYPE = mGetScrollType;
	    METHOD_GET_SCROLL_AMOUNT = mGetScrollAmount;
	    METHOD_GET_WHEEL_ROTATION = mGetWheelRotation;
	    METHOD_GET_UNITS_TO_SCROLL = mGetUnitsToScroll;
	}
	else {
	    CLASS_MOUSE_WHEEL_EVENT = null;
	    METHOD_GET_SCROLL_TYPE = null;
	    METHOD_GET_SCROLL_AMOUNT = null;
	    METHOD_GET_WHEEL_ROTATION = null;
	    METHOD_GET_UNITS_TO_SCROLL = null;
	}
    }

    // --------------
    // Public methods
    // --------------

    /**
     * Whether MouseWheelEvent is supported by the current runtime.
     */
    public static boolean isMouseWheelEventSupported()
    {
	return CLASS_MOUSE_WHEEL_EVENT != null;
    }

    /**
     * Calls <code>MouseWheelEvent.getScrollType</code> through reflection.
     *
     * @param e <code>MouseWheelEvent</code> whose method is to be invoked
     *	        through reflection.
     *
     * @return <code>((MouseWheelEvent)e).getScrollType()</code> wrapped in
     *	       an <code>Integer</code>;
     *	       <code>null</code> if the runtime type of <code>e</code> is not
     *	       <code>MouseWheelEvent</code> or the reflection access failed 
     *	       for some unknown reason.
     *
     * @throws NullPointerException if <code>e</code> is <code>null</code>.
     */
    public static Integer getScrollType(MouseEvent e)
    {
	// Is reflection possible?
	if (CLASS_MOUSE_WHEEL_EVENT != null &&
	    CLASS_MOUSE_WHEEL_EVENT.isInstance(e)) {

	    try {
		Object o = METHOD_GET_SCROLL_TYPE.invoke(e, new Object[0]);
		return (Integer)o;
	    }
	    catch (IllegalAccessException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	    catch (IllegalArgumentException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	    catch (InvocationTargetException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	}

	return null;
    }

    /**
     * Calls <code>MouseWheelEvent.getScrollAmount</code> through reflection.
     *
     * @param e <code>MouseWheelEvent</code> whose method is to be invoked
     *	        through reflection.
     *
     * @return <code>((MouseWheelEvent)e).getScrollAmount()</code> wrapped in
     *	       an <code>Integer</code>;
     *	       <code>null</code> if the runtime type of <code>e</code> is not
     *	       <code>MouseWheelEvent</code> or the reflection access failed 
     *	       for some unknown reason.
     *
     * @throws NullPointerException if <code>e</code> is <code>null</code>.
     */
    public static Integer getScrollAmount(MouseEvent e)
    {
	// Is reflection possible?
	if (CLASS_MOUSE_WHEEL_EVENT != null &&
	    CLASS_MOUSE_WHEEL_EVENT.isInstance(e)) {

	    try {
		Object o = METHOD_GET_SCROLL_AMOUNT.invoke(e, new Object[0]);
		return (Integer)o;
	    }
	    catch (IllegalAccessException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	    catch (IllegalArgumentException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	    catch (InvocationTargetException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	}

	return null;
    }

    /**
     * Calls <code>MouseWheelEvent.getUnitsToScroll</code> through reflection.
     *
     * @param e <code>MouseWheelEvent</code> whose method is to be invoked
     *	        through reflection.
     *
     * @return <code>((MouseWheelEvent)e).getUnitsToScroll()</code> wrapped in
     *	       an <code>Integer</code>;
     *	       <code>null</code> if the runtime type of <code>e</code> is not
     *	       <code>MouseWheelEvent</code> or the reflection access failed 
     *	       for some unknown reason.
     *
     * @throws NullPointerException if <code>e</code> is <code>null</code>.
     */
    public static Integer getUnitsToScroll(MouseEvent e)
    {
	// Is reflection possible?
	if (CLASS_MOUSE_WHEEL_EVENT != null &&
	    CLASS_MOUSE_WHEEL_EVENT.isInstance(e)) {

	    try {
		Object o = METHOD_GET_UNITS_TO_SCROLL.invoke(e, new Object[0]);
		return (Integer)o;
	    }
	    catch (IllegalAccessException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	    catch (IllegalArgumentException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	    catch (InvocationTargetException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	}

	return null;
    }

    /**
     * Calls <code>MouseWheelEvent.getWheelRotation</code> through reflection.
     *
     * @param e <code>MouseWheelEvent</code> whose method is to be invoked
     *	        through reflection.
     *
     * @return <code>((MouseWheelEvent)e).getWheelRotation()</code> wrapped in
     *	       an <code>Integer</code>;
     *	       <code>null</code> if the runtime type of <code>e</code> is not
     *	       <code>MouseWheelEvent</code> or the reflection access failed 
     *	       for some unknown reason.
     *
     * @throws NullPointerException if <code>e</code> is <code>null</code>.
     */
    public static Integer getWheelRotation(MouseEvent e)
    {
	// Is reflection possible?
	if (CLASS_MOUSE_WHEEL_EVENT != null &&
	    CLASS_MOUSE_WHEEL_EVENT.isInstance(e)) {

	    try {
		Object o = METHOD_GET_WHEEL_ROTATION.invoke(e, new Object[0]);
		return (Integer)o;
	    }
	    catch (IllegalAccessException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	    catch (IllegalArgumentException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	    catch (InvocationTargetException ex) {
		// Should not happen
		// Ignore this exception
		ex.printStackTrace();
	    }
	}

	return null;
    }
}
