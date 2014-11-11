package FileIO;
/**
 * Model-based Level Set (MLS) Algorithm
 *
 * COPYRIGHT NOTICE
 * Copyright (c) 2003-2005 Haihong Zhuang and Daniel J. Valentino
 *
 * Please read LICENSE.TXT for the license covering this software
 *
 * For more information, please contact the authors at:
 * haihongz@seas.ucla.edu
 * dvalentino@mednet.ucla.edu
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileImageInputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * Read a file's metadata and pixel data.
 *
 * @author Haihong Zhuang and Daniel J. Valentino
 * @version 2 September 2005
 */
public class FileReader
{
    /** File queried. */
    private File _file;

    /** Image reader associated with the queried file. */
    private ImageReader _reader;

    /** Format names list that may be referred to.*/
    private String[] _formatNames = {"analyze", "minc"};

    /** 
     * Constructs FileReader. 
     */
    public FileReader(File file)
    {
	_file = file;
	try{
	    _reader = _getImageReader(file);
	}catch(Exception e){
	    e.printStackTrace();
	}
    }


    /**
     * Gets the image reader of the given file.
     */
    private ImageReader _getImageReader(File file)
	throws FileNotFoundException, IOException
    {
	Iterator iter = null;
	ImageReader reader = null;

	try{

	    // Get an Image Input Stream to the File
	    Object input = new FileImageInputStream(file);
	    Object inputStream = input;

	    // Get an Image Reader for the source data from the source
	    // reader bytes
	    iter = ImageIO.getImageReaders(input);

	    // No Image Reader is found
	    if ( !iter.hasNext() ) {

		// Get an Image Reader from the file
		input = file;
		iter = ImageIO.getImageReaders(input);

		// No Image Reader is found
		if( !iter.hasNext() ) {
		    input = inputStream;

		    // Get an Image Reader from the file name suffix
		    String temp = file.toString();
		    String[] strings = temp.split("\\.");
		    if (strings.length > 1){
			iter = ImageIO.getImageReadersBySuffix(
						   strings[strings.length-1]);
		    }

		    // No Image Reader found
		    if ( !iter.hasNext() ) {
			//return (null);
		   String msg = "Cannot find an Image Reader for the source. ";
			throw new IOException(msg);
		    }
		}
	    }


	    // Set the Input Stream
	    reader = (ImageReader) iter.next();
	    reader.setInput(input);
	    ImageReaderSpi spi = reader.getOriginatingProvider();
	    if(spi.canDecodeInput(input)) {
		//_format = reader.getFormatName();
		return reader;
	    }
	} catch (Exception e){
	    e.printStackTrace();
	}
	return null;
    }

    /**
     * Get the image reader for reading the queried file.
     *
     * @return an ImageReader.
     */
    public ImageReader getImageReader()
    {
	return _reader;
    }


    /**
     * Get the image's resolutions.
     *
     * @return an 1D array containing three values.
     */
    public float[] getPixdims()
    {
	float[] defaultPixdims = {1.0f, 1.0f, 1.0f};
	
	try{
	    // if stream metadata is not available, return the default pixdims
	    if(_reader == null || _reader.getStreamMetadata() == null)
		return defaultPixdims;

	    // Get the IIOMetadata
	    IIOMetadata metadata = _reader.getStreamMetadata();
	    String[] formatNames = metadata.getMetadataFormatNames();

	    // Get the IIO Metadata Node
	    int whichTree = 0;
	    Node node = metadata.getAsTree(formatNames[whichTree]);
	    node = node.getFirstChild();

	    // Parse Analyze file format and get the pixdims
	    if(_reader.getFormatName().equals(_formatNames[0]))
	       return _getAnalyzePixdims(node);
	    else return defaultPixdims;
	}catch(Exception e){
	    e.printStackTrace();
	}
	return defaultPixdims;
    }

    /**
     * Parse Analyze metadata for the image's resolusions.
     *
     * @param node associated with the queried file.
     *
     * @return a 1D array of image's resolutions.
     */
    private float[] _getAnalyzePixdims(Node node)
    {
	float[] pixdims = {1.0f, 1.0f, 1.0f};
	NamedNodeMap map;
	Node attribute;
	while(node != null ){
	    /*if( node.getNodeName() == "DATA_HISTORY"){
		System.out.println("Node Name:  "+node.getNodeName());
		map = node.getAttributes();
		attribute = map.getNamedItem("orient");
		orient = (new Integer(attribute.getNodeValue())).intValue();
	    } else*/ 
	    if(node.getNodeName() == "IMAGE_DIMENSION"){
		map = node.getAttributes();
		for(int k = 0; k < 3; k++){
		    attribute = map.getNamedItem("pixdim_"+(k+1));
		    pixdims[k] = (new Float(attribute.getNodeValue()))
			                                         .floatValue();
		}
	    }
	    node = node.getNextSibling();
	}
	return pixdims;
    }
}
