/*
 * @(#)FastFourierTransform.java		1.10 07/01/20
 *
 * ChargedFluid package
 *
 * COPYRIGHT NOTICE
 * Copyright (c) 2007 Herbert H.H. Chang, Daniel J. Valentino, Gary R. Duckwiler, and Arthur W. Toga
 * Laboratory of Neuro Imaging, Department of Neurology, UCLA.
 */

package tools;

/** 
 * FastFourierTransform class is mainly dedicated to the calculation of 2D Fast Fourier Transform.
 * There are fastFT1D method for one dimensional FFT and fastFT2D method for two dimensional FFT.
 */
public final class FastFourierTransform
{

   /** 
    * fastFT2D function accepts two double type arrays of length of integer power of 2 and a boolean
    * value representing forward (true) or backward (false) 2D FFT. This method returns the real part 
    * results restoring in the original realArray and the imaginary part in the original imagArray.
    * Note that both given arrays must have the same dimension or it will throw an exception.
    * If they are not an integer power of 2, it will throw an exception too.
    *
    * @param realArray Input double array representing real parts of complex variables.
    * @param imagArray Input double array representing imaginary parts of complex variables.
    * @param direction Boolean value, true indicates forward 2D FFT; false inverse.
    */
  public static void fastFT2D(double[][] realArray, double[][] imagArray, boolean direction)
  {
    int numOfRow, numOfCol;
    int numOfRowImag, numOfColImag;
    int numOfPowerInRow, numOfPowerInCol;
    double[] realCol;
    double[] imagCol;

    numOfRow = realArray.length;
    numOfCol = realArray[0].length;
    numOfRowImag = imagArray.length;
    numOfColImag = imagArray[0].length;

    /* Allocate and initialize memories to double variables */
    realCol = new double[numOfRow];
    imagCol = new double[numOfRow];

    try
    {
      //check if the dimensions of both arrays are the same or not!
      checkSizeOfArray(numOfRow, numOfCol, numOfRowImag, numOfColImag);
      numOfPowerInRow = checkPowerOf2(numOfRow);	//check if numOfRow is an integer power of 2 or not!
      numOfPowerInCol = checkPowerOf2(numOfCol);	//check if numOfCol is an integer power of 2 or not!
      
      /* Transform array in rows */
      for (int row = 0; row < numOfRow; row++)
      {
        /* Compute 1D FFT via function fastFT1D() */
        fastFT1D(realArray[row], imagArray[row], numOfPowerInCol, direction);
      }

      /* Transform array in columns */
      for (int col = 0; col < numOfCol; col++)
      {
        // Extract one vector from array for real and imaginary parts
        for (int row = 0; row < numOfRow; row++)
        {
          realCol[row] = realArray[row][col];
          imagCol[row] = imagArray[row][col];
        }
        
        /* Compute 1D FFT via function fastFT1D() */
        fastFT1D(realCol, imagCol, numOfPowerInRow, direction);

        // Store one vector into array after FFT for real and imaginary parts
        for (int row = 0; row < numOfRow; row++)
        {
          realArray[row][col] = realCol[row];
          imagArray[row][col] = imagCol[row];
        }
      }
    }
    catch(NotSameArraySizeException e)
    {
      System.out.println("\n" + e);
      System.out.println("Warning: Please assign real and imaginary arrays with the same dimension");
    }
    catch(NotPowerOf2Exception e)
    {
      System.out.println("\n" + e);
      System.out.println("Warning: Please assign a array length of integer power of 2");
    }
  }	//End of fastFT2D


  /** 
   * checkSizeOfArray function accepts four integers representing the sizes of two different arrays. 
   * If they don't have the same dimension, it throws a NotSameArraySizeException.
   * @param rowReal Input integer representing the number of row of a real part array.
   * @param colReal Input integer representing the number of column of a real part array.
   * @param rowImag Input integer representing the number of row of a imaginary part array.
   * @param colImag Input integer representing the number of column of a imaginary part array.
   */
  static void checkSizeOfArray(int rowReal, int colReal, int rowImag, int colImag) throws NotSameArraySizeException
  {
    if ((rowReal != rowImag) || (colReal != colImag))
      throw new NotSameArraySizeException();
  }     //End of checkSizeOfArray


  /** 
   * checkPowerOf2 function accepts a integer and check if this value is an integer power of 2 or
   * not. If it is an integer power of 2, this function returns the number of power of 2.
   * If it is not an integer power of 2, this function throws a NotPowerOf2Exception.
   *
   * @param index Input integer value for examination.
   * @return int Output integer value representing the number of power of 2.
   */
  static int checkPowerOf2(int index) throws NotPowerOf2Exception
  {
    // check if index is an integer power of 2
    if ( index < 2 )
      throw new NotPowerOf2Exception(index);
    if ( (index & (index-1)) != 0 )
      throw new NotPowerOf2Exception(index);
    // calculate the number of bits needed
    for (int i = 0; ; i++)
      if ( (index & (1<<i)) != 0 )
        return i;
  }     //End of checkPowerOf2


   /** 
    * fastFT1D function accepts two double type vectors of length of integer power of 2, an integer indicating the
    * number of power of 2, two boolean values indicating row computation and forward FFT or not.
    * 
    * @param real Input double vector indicating the real part of the array.
    * @param imag Input double vector indicating the imaginary part of the array.
    * @param numOfPower An integer indicating the number of power of 2 by the given vector.
    * @param direct Boolean value, true indicates forward 2D FFT; false inverse.
    */
  static void fastFT1D(double[] real, double[] imag, int numOfPower, boolean direct)
  {
    int numOfPoint, i1, i2, j2, k2, l1, l2;
    double tr, ti, c1, c2, u1, u2, t1, t2, z;

    numOfPoint = real.length;

    /* Do the bit reversal */
    i2 = numOfPoint >> 1;
    j2 = 0;
    for (int i = 0; i < numOfPoint - 1; i++)
    {
      if (i < j2)
      {
        tr = real[i];
        ti = imag[i];
        real[i] = real[j2];
        imag[i] = imag[j2];
        real[j2] = tr;
        imag[j2] = ti;
      }
      k2 = i2;
      while (k2 <= j2)
      {
        j2 -= k2;
        k2 >>= 1;
      } 
      j2 += k2;
    }
  
    /* Compute 1D FFT */
    c1 = -1.0;
    c2 = 0.0;
    l2 = 1;
    for (int l = 0; l < numOfPower; l++)		//Loops through stages
    {
      l1 = l2;
      l2 <<= 1;
      u1 = 1.0;
      u2 = 0.0;
  
      for (int j = 0; j < l1; j++)
      {
        for (int i = j; i < numOfPoint; i += l2)
        {
          i1 = i + l1;
          t1 = u1 * real[i1] - u2 * imag[i1];
          t2 = u1 * imag[i1] + u2 * real[i1];
          real[i1] = real[i] - t1;
          imag[i1] = imag[i] - t2;
          real[i] += t1;
          imag[i] += t2;
        }
        z = u1 * c1 - u2 * c2;
        u2 = u1 * c2 + u2 * c1;
        u1 = z;
      }
      c2 = Math.sqrt((1.0 - c1) / 2.0);
      if (direct)
        c2 = -c2;
      c1 = Math.sqrt((1.0 + c1) / 2.0);
    }

    /* Scaling for forward FFT */
    if (direct)
      for (int i = 0; i < numOfPoint; i++)
      {
        real[i] /= (double)numOfPoint;
        imag[i] /= (double)numOfPoint;
      }
  }	//End of fastFT1D
}
