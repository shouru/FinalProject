/*
jViewBox 2.0 alpha

COPYRIGHT NOTICE
Copyright (c) 2003  Jack C. Wei, Scott C. Neu, and Daniel J. Valentino
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Generates a list of all the files in the specified directory (and its
 * subdirectories) that end with "java" and writes the list to the
 * specified file.
 *
 * @version February 12, 2003
 */
public class JavaFileListGenerator
{
    /**
     * Main start method. The first command line argument is assumed to be
     * the directory to search for files ending with "java". The second
     * command line argument is the name of the file to write the list to.
     * If the second is omitted, use standard output.
     *
     * @param Command line arguments, the first of which is the directory to
     *        search for files ending with "java"; the second is the file to
     *        write the list of files to.
     */
    public static void main(String[] args)
    {
	// Check for at least one argument
	if (args.length < 1) {
	    System.out.println("Usage:  JavaFileListGenerator " +
			       "<directory to search> <output file name>");
	    System.out.println("If the output file name is omitted, the " +
			       "result is sent to the standard output.");
	}

	// Search the given directory and write to the given output file
	else {
	    try {
		PrintStream out;
		if (args.length >= 2) {
		    out = new PrintStream(new FileOutputStream(args[1]));
		}
		else {
		    out = System.out;
		}

		listJavaFiles(new File(args[0]), out);
	    }
	    catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Recursively lists all .java files in the directory tree.
     */
    private static void listJavaFiles(File f, PrintStream out)
    {
	// Is f indeed a file and with extension .java?
	if (f.isFile() && f.getName().endsWith(".java")) {
	    // Take care of occurrences of backslash \ and double quote " in
	    // the path by preceding them with another backslash so we
	    // can safely enclose the path in quotes, exactly like what we
	    // do when writing string literal in Java source code
	    // We want the path quoted so that spaces and other special
	    // characters in the path can be interpreted correctly
	    String path = f.getAbsolutePath();
	    StringBuffer quotedPath = new StringBuffer("\"");
	    for (int i = 0; i < path.length(); i++) {
		char c = path.charAt(i);
		if (c == '\\' || c == '\"') {
		    quotedPath.append('\\');
		}
		quotedPath.append(c);
	    }
	    quotedPath.append('\"');

	    // Print the quoted path
	    out.println(quotedPath);
	}

	// Or is it a directory?
	else if (f.isDirectory()) {
	    // Recursively handle each item in the directory
	    File[] contents = f.listFiles();
	    for (int i = 0; i < contents.length; i++) {
		listJavaFiles(contents[i], out);
	    }
	}
    }
}
