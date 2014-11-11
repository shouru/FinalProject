/*
ChargedFluid package

COPYRIGHT NOTICE
Copyright (c) 2003 Herbert H.H. Chang
 */
 
package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.IOException;


public class WriteArrayData
{
	private double[] _vectorDouble;
	private double[][] _arrayDouble;
	private double[][][] _arrayDouble3D;
	private String _string;
	private String[] _dataString;
	private File _file;
	
	
	/* 3D array 
	 * This function stores the 3-D data into continuous 2-D arrays based up the direction.
	 * Direction 1 represents x-axis, 2 y-axis, and 3 z-axis.
	 */
	public WriteArrayData(double[][][] arrayDouble3D, File file, int direction)
	{
		int numOfFrame = arrayDouble3D.length;			// z-axis
		int numOfRow = arrayDouble3D[0].length;			// y-axis
		int numOfCol = arrayDouble3D[0][0].length;		// x-axis
		// Get the original filename.
	    String filePath = file.getAbsolutePath();
	    int indexOfDot = filePath.indexOf(".");
	    // Extract the filename without the extension.
	    String fileName = filePath.substring(0, indexOfDot);
	    // Obtain the extension.
	    String extension = filePath.substring(indexOfDot);
		// Number of 2-D arrays to be stored.
		int numOfSlice = numOfCol;		// Default number in the x-axis.
		if (direction == 1) {
			fileName += "_x_";				// Slicing in the x-axis.
		}
		else if (direction == 2) {
			numOfSlice = numOfRow;		// in the y-axis.
			fileName += "_y_";
		}
		else if (direction == 3) {
			numOfSlice = numOfFrame;	// in the z-axis.
			fileName += "_z_";
		}
		
	    // Subroutine for generating the 2-D arrays.
	    for (int slice = 1; slice <= numOfSlice; slice++) {
	    	// Compute the 2-D arrays based up the slicing direction.
	    	double[][] arrayDouble2D = new double[numOfFrame][numOfRow];
	    	// Slicing in the x-axis.
	    	if (direction == 1) {
	    		for (int coord1 = 0; coord1 < numOfFrame; coord1++) {		// z-axis
					for (int coord2 = 0; coord2 < numOfRow; coord2++) {		// y-axis
						// The number of slice is minus 1 because of the starting value of slcie.
						arrayDouble2D[coord1][coord2] = arrayDouble3D[coord1][coord2][slice-1];
					}
				}
			}
	    	// Slicing in the y-axis.
	    	else if (direction == 2) {
	    		arrayDouble2D = new double[numOfFrame][numOfCol];
	    		for (int coord1 = 0; coord1 < numOfFrame; coord1++) {		// z-axis
					for (int coord2 = 0; coord2 < numOfCol; coord2++) {		// x-axis
						arrayDouble2D[coord1][coord2] = arrayDouble3D[coord1][slice-1][coord2];
					}
				}
	    	}
	    	// Slicing in the z-axis.
	    	else if (direction == 3) {
	    		arrayDouble2D = new double[numOfRow][numOfCol];
	    		for (int coord1 = 0; coord1 < numOfRow; coord1++) {			// y-axis
					for (int coord2 = 0; coord2 < numOfCol; coord2++) {		// x-axis
						arrayDouble2D[coord1][coord2] = arrayDouble3D[slice-1][coord1][coord2];
					}
				}
	    	}
	    	// Set the filename for each file.
	    	_file = new File(fileName + getInteger2String(slice) + extension);
	    	// Set the 2-D array.
	    	_arrayDouble = arrayDouble2D;
	    	// Write array action.
			try
			{
				WriteArrayAction();
			}
			catch(IOException e)
			{
				System.out.println("\n" + e);
			}
	    }
	}
	
	
	private String getInteger2String(int number)
	{
		Integer numberInt = new Integer(number);
		if (number < 10) {
			return "000" + numberInt.toString();
		}
		else if (number < 100) {
			return "00" + numberInt.toString();
		}
		else if (number < 1000) {
			return "0" + numberInt.toString();
		}
		else {
			return numberInt.toString();
		}
	}
	
	
	/* 3D array */
	public WriteArrayData(double[][][] arrayDouble3D, File file)
	{
		_arrayDouble3D = arrayDouble3D;
		_file = file;
		try
		{
			WriteArray3DAction();
		}
		catch(IOException e)
		{
			System.out.println("\n" + e);
		}
	}
	
	
	/* 2D array */
	public WriteArrayData(double[][] arrayDouble, File file)
	{
		_arrayDouble = arrayDouble;
		_file = file;
		try
		{
			WriteArrayAction();
		}
		catch(IOException e)
		{
			System.out.println("\n" + e);
		}
	}
	
	
	/* 1D array */
	public WriteArrayData(double[] vectorDouble, File file)
	{
		_vectorDouble = vectorDouble;
		_file = file;
		try
		{
			WriteVectorAction();
		}
		catch(IOException e)
		{
			System.out.println("\n" + e);
		}
	}
	
	
	/* Data string */
	public WriteArrayData(String[] dataString, File file)
	{
		_dataString = dataString;
		_file = file;
		try
		{
			WriteStringArrayAction();
		}
		catch(IOException e)
		{
			System.out.println("\n" + e);
		}
	}
	
	
	/* Data string */
	public WriteArrayData(String string, File file)
	{
		_string = string;
		_file = file;
		try
		{
			WriteStringAction();
		}
		catch(IOException e)
		{
			System.out.println("\n" + e);
		}
	}
	
	
	public void WriteArray3DAction() throws IOException
	{
		int depth = _arrayDouble3D.length;
		int height = _arrayDouble3D[0].length;
		int width = _arrayDouble3D[0][0].length;
		
		FileOutputStream fos;
		
		fos = new FileOutputStream(_file);
		
		Writer wt = new BufferedWriter(new OutputStreamWriter(fos));
		
		for (int d = 0; d < depth; d++) {
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					Double d3 = new Double(_arrayDouble3D[d][h][w]);
					wt.write(d3.toString());
					wt.write(' ');
				}
				wt.write(' ');
			}
			wt.write(' ');
		}
		
		wt.flush();
		fos.close();
	}
	
	
	public void WriteArrayAction() throws IOException
	{
		int height = _arrayDouble.length;
		int width = _arrayDouble[0].length;
		
		FileOutputStream fos;
		
		fos = new FileOutputStream(_file);
		
		Writer wt = new BufferedWriter(new OutputStreamWriter(fos));
		
		for (int h = 0; h < height; h++)
		{
			for (int w = 0; w < width; w++)
			{
				Double d2 = new Double(_arrayDouble[h][w]);
				wt.write(d2.toString());
				wt.write(' ');
			}
			wt.write('\n');
		}
		
		wt.flush();
		fos.close();
	}
	
	
	public void WriteVectorAction() throws IOException
	{
		int length = _vectorDouble.length;
		
		FileOutputStream fos;
		
		fos = new FileOutputStream(_file);
		
		Writer wt = new BufferedWriter(new OutputStreamWriter(fos));
		
		for (int h = 0; h < length; h++)
		{
			Double d2 = new Double(_vectorDouble[h]);
			wt.write(d2.toString());
			wt.write(' ');
		}
		
		wt.flush();
		fos.close();
	}
	
	
	public void WriteStringArrayAction() throws IOException
	{
		int length = _dataString.length;
		FileOutputStream fos;
		fos = new FileOutputStream(_file);
		Writer wt = new BufferedWriter(new OutputStreamWriter(fos));
		
		for (int h = 0; h < length; h++)
		{
			// Write the string to the file.
			wt.write(_dataString[h]);
			wt.write('\n');
		}
		
		wt.flush();
		fos.close();
	}
	
	
	public void WriteStringAction() throws IOException
	{
		FileOutputStream fos;
		fos = new FileOutputStream(_file);
		Writer wt = new BufferedWriter(new OutputStreamWriter(fos));
		
		wt.write(_string);
		wt.flush();
		fos.close();
	}
}