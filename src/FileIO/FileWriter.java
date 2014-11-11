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
package FileIO;

import edu.ucla.loni.analyze.plugin.AnalyzeImageWriterSpi;
import edu.ucla.loni.analyze.plugin.AnalyzeMetadata;
import edu.ucla.loni.analyze.plugin.AnalyzeOutputStream;
import edu.ucla.loni.minc.plugin.MincImageWriterSpi;
import edu.ucla.loni.minc.plugin.MincMetadata;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageOutputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Write metadata and pixel data into a file.
 *
 * @author Haihong Zhuang and Daniel J. Valentino
 * @version 2 September 2005
 */
public class FileWriter
{
    /**
     * Constructor.
     */
    public FileWriter()
    {
    }

    // Future modification: Only metadata is needed for this class. 
    // metadata = reader.getStreamMetadata()
    public void saveFile(String baseName, BufferedImage[] imgs,
			 ImageReader inputReader)
    {
      try{
	if(inputReader.getFormatName().equals("analyze")) {
	    saveAnalyzeFile(baseName, imgs, inputReader);
	}
	else if (inputReader.getFormatName().equals("minc")){
	    saveMincFile(baseName, imgs, inputReader);
	}
	else {
	    System.out.println("FileWriter: Sorry! Cannot save the file!");
	}
      }catch(Exception e){
	  e.printStackTrace();
      }
    }
    public void saveMaskFile(String baseName, BufferedImage[] imgs,
			     int intensityOfMask, ImageReader inputReader)
    {
      try{
	if(inputReader.getFormatName().equals("analyze")) {
	    saveAnalyzeFile(baseName, imgs, inputReader);
	}
	else if (inputReader.getFormatName().equals("minc")){
	    saveMincMaskFile(baseName, imgs, intensityOfMask, inputReader);
	}
	else {
	    System.out.println("FileWriter: Sorry! Cannot save the file!");
	}
      }catch(Exception e){
	  e.printStackTrace();
      }
    }

  public void saveAnalyzeFile(String baseName, BufferedImage[] imgs, 
			      ImageReader inputReader)
  {
    try {

      // Get the file's baseName
      if(baseName.endsWith(".img") ){
	  int temp = baseName.lastIndexOf(".img");
	  baseName = baseName.substring(0, temp);
      } 
      else if(baseName.endsWith(".hdr")){
	  int temp = baseName.lastIndexOf(".hdr");
	  baseName = baseName.substring(0, temp);
      }

      // Create the output stream
      File hdrFile = new File(baseName+".hdr");
      FileImageOutputStream hdrStream = new FileImageOutputStream(hdrFile);
      File imgFile = new File(baseName+".img");
      FileImageOutputStream imgStream = new FileImageOutputStream(imgFile);
      AnalyzeOutputStream stream =new AnalyzeOutputStream(hdrStream,imgStream);

      // Create the metadata
      AnalyzeMetadata metadata = 
	  (AnalyzeMetadata)inputReader.getStreamMetadata();

      // Prepare the writer
      AnalyzeImageWriterSpi spi = new AnalyzeImageWriterSpi();
      ImageWriter writer = spi.createWriterInstance(null);
      writer.setOutput(stream);
      writer.prepareWriteSequence(metadata);

      // Write images
      for(int i = 0; i < imgs.length; i++ ){
	  IIOImage iioImage = new IIOImage(imgs[i], null, null);
	  writer.writeToSequence(iioImage, null);
      }

      // Write the hdr file
      writer.endWriteSequence();
      System.out.println();
      System.out.println("File "+baseName
			 +".hdr(img) is successfully saved.");

    }catch (Exception e){
      e.printStackTrace();
    }
  }

  public void saveMincFile(String fileName, BufferedImage[] imgs,
			    ImageReader inputReader) 
  {
    try {

      // Create the output stream
      if( ! fileName.endsWith(".mnc") ) fileName += ".mnc";
      File file = new File(fileName);
      FileImageOutputStream stream = new FileImageOutputStream(file);

      // Create the metadata
      MincMetadata metadata = (MincMetadata)inputReader.getStreamMetadata();

      // Prepare the writer
      MincImageWriterSpi spi = new MincImageWriterSpi();
      ImageWriter writer = spi.createWriterInstance(null);
      writer.setOutput(stream);
      writer.prepareWriteSequence(metadata);

      // Write images
      for(int i = 0; i < imgs.length; i++ ){
	  IIOImage iioImage = new IIOImage(imgs[i], null, null);
	  writer.writeToSequence(iioImage, null);
      }

      // Write the hdr file
      writer.endWriteSequence();

    }catch (Exception e){
      e.printStackTrace();
    }
  }

  public void saveMincMaskFile(String fileName, BufferedImage[] imgs,
			       int intensityOfMask, ImageReader inputReader)
  {
    try {

      // Create the output stream
      if( ! fileName.endsWith(".mnc") ) fileName += ".mnc";
      File file = new File(fileName);
      FileImageOutputStream stream = new FileImageOutputStream(file);

      // Check if stream metadata is not available
      if(inputReader == null || inputReader.getStreamMetadata() == null){
	  String msg = "Cannot find input images' header information. ";
	  throw new IOException(msg);
      }

      // Get inputReader's metadata
      IIOMetadata metadata = inputReader.getStreamMetadata();
      String[] formatNames = metadata.getMetadataFormatNames();
      int whichTree = 0;
      Node node = metadata.getAsTree(formatNames[whichTree]);
      node = node.getFirstChild();

      // Modify the metadata with new maximum and minimum values of all slides
      _setMaxOrMinIntensityValues(node, imgs.length, "image-max", 
				  intensityOfMask );
      _setMaxOrMinIntensityValues(node, imgs.length, "image-min", 0 );

      // Set the node to metadata
      node = node.getParentNode();
      metadata.setFromTree(formatNames[whichTree], node);

      // Prepare the writer
      MincImageWriterSpi spi = new MincImageWriterSpi();
      ImageWriter writer = spi.createWriterInstance(null);
      writer.setOutput(stream);
      writer.prepareWriteSequence(metadata);

      // Write images
      for(int i = 0; i < imgs.length; i++ ){
	  IIOImage iioImage = new IIOImage(imgs[i], null, null);
	  writer.writeToSequence(iioImage, null);
      }

      // Write the hdr file
      writer.endWriteSequence();

    }catch (Exception e){
      e.printStackTrace();
    }
  }


  /**
   * Setss Maximum or Minimum pixel values, each corresponding to the maximum
   * or minimum pixel value of one slice.
   *
   * @param node Node of the file header.
   * @param numOfImages Number of images.
   * @param nodeName Name of the node storing the max or min pixel values.
   * @param intensity The intensity value to set.
   */
  private void _setMaxOrMinIntensityValues(Node node, int numOfImages,
					   String nodeName, int intensity)
  {
    NamedNodeMap map;
    Node attribute;
    double intensDouble = intensity;
    while(node != null ){
      if(node.getNodeName() == "VARIABLES"){
	NodeList list = node.getChildNodes();
	for(int i = 0; i < list.getLength(); i++){
	  Node nodeForImageMax = list.item(i);

	  // Search for the given nodeName which is either "image-max" or 
	  // "image-min
	  if(nodeForImageMax.getNodeName().equals(nodeName)){
	    NodeList listUnderImageMax = nodeForImageMax.getChildNodes();
	    for(int n = 0; n < listUnderImageMax.getLength(); n++){
	      Node nodeForDATA = listUnderImageMax.item(n);

	      // Search for "DATA"
	      if(nodeForDATA.getNodeName().equals("DATA")){
		NodeList listUnderDATA = nodeForDATA.getChildNodes();
		for(int m = 0; m < listUnderDATA.getLength(); m++){
		  Node nodeForARRAY = listUnderDATA.item(m);

		  // search for "ARRAY"
		  if(nodeForARRAY.getNodeName().equals("ARRAY")){
		    NodeList arrayList = nodeForARRAY.getChildNodes();
		    Node elementNode = null;
		    for(int k = 0; k < numOfImages; k++){
		      elementNode = arrayList.item(k);
		      map = elementNode.getAttributes();
		      attribute = map.getNamedItem("value");
		      attribute.setNodeValue(String.valueOf(intensDouble));
		    }
		    break;
		  }
		}
	      }
	    }
	  }
	}
      }
      node = node.getNextSibling();
    }
  }

  private void _setMincBitsPP(Node node, int bitsPP)
  {
    String type = "byte";
    if(bitsPP <= 8) type = "byte";
    else type = "short";

    NamedNodeMap map;
    Node attribute;
    while(node != null ){
      if(node.getNodeName() == "VARIABLES"){
	  
	NodeList list = node.getChildNodes();
	for(int i = 0; i < list.getLength(); i++){
	  Node nodeForImage = list.item(i);

	  // Search for "image"
	  if(nodeForImage.getNodeName().equals("image")){		    
	    NodeList listUnderImage = nodeForImage.getChildNodes();
	    for(int n = 0; n < listUnderImage.getLength(); n++){
	      Node nodeForDATA = listUnderImage.item(n);

	      // Search for "DATA"
	      if(nodeForDATA.getNodeName().equals("DATA")){
		map = nodeForDATA.getAttributes();
		attribute = map.getNamedItem("type");
		attribute.setNodeValue(type);
		break;
 	      }
	    }
	  }
	}
      }
      node = node.getNextSibling();
    }
  }
}
