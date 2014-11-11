/*
 * @(#)NotPowerOf2Exception.java		1.10 07/01/20
 *
 * ChargedFluid package
 *
 * COPYRIGHT NOTICE
 * Copyright (c) 2007 Herbert H.H. Chang, Daniel J. Valentino, Gary R. Duckwiler, and Arthur W. Toga
 * Laboratory of Neuro Imaging, Department of Neurology, UCLA.
 */

package tools;

/** 
 * NotPowerOf2Exception is dedicated to the definition of NotPowerOf2Exception usage. 
 */
public class NotPowerOf2Exception extends Exception
{
  private int _numOfPoint;

  NotPowerOf2Exception(int index)
  {
    _numOfPoint = index;
  }

  public String toString()
  {
    return "NotPowerOf2Exception[ " + _numOfPoint + " is not an integer power of 2 ]";
  }
}
    

