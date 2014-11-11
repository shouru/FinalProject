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
 
import java.io.IOException;
import java.util.Vector;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to parse the metadata of an image file.
 *
 * @author Haihong Zhuang and Daniel J. Valentino
 * @version 2 September 2005
 */
public class MetadataParser 
{
  /** Format names supported by volume constructor.*/
  private static String[] FORMAT_TYPES = {"analyze", "minc", "dicom", "ge", 
					  "ucf"};

  /** Format type of the given file. */
  private String _format = "analyze";

  /** Bits per pixel. */
  private int _bitsPP = 12;

  /** value of orientation. 0-axial, 1-coronal, 2-sagittal. */
  private int _orient = 0;

  /** Original pixDims. */
  private float[] _pixDims = {1f, 1f, 1f};

  /** An array of factors to re-normalize each image slide.  */
  private double[] _reNormValues = null;

  /** Whether the readers refer to the same set of image data. */
  private boolean _isSame = true;

  /** Age of the subject to whom the data belongs. */
  private double _age = 40;

  /**
   * Constructs MetadataParser.
   * 
   * @param reader ImageReader for parsing the queried file.
   */
  public MetadataParser(ImageReader reader)
  {
    Node node = null;
    try{
      node = _getImageNode(reader);
      _format = reader.getFormatName();
    }catch(Exception e){
      e.printStackTrace();
    }

    // Get orientation
    _orient = _getOrient(node, _format);
    _bitsPP = _getBitsPP(node, _format);

    // Only the first three elements in the array are necessary.
    float[] dims = _getPixDims(node, _format);
    _pixDims[0] = dims[0];
    _pixDims[1] = dims[1];
    _pixDims[2] = dims[2];

    // Get subject's age
    _age = _getAge(node, _format);

    // Get the old max pixel values
    if(_format.equals(FORMAT_TYPES[1])){
      try{
	double[] oldMax = _getMaxPixelValues(node, reader.getNumImages(true));
   
	// Get renormalization array
	_reNormValues = _createReNorm(oldMax);
      }catch(Exception ex){
	ex.printStackTrace();
      }
    }
  }

  /**
   * Constructor.
   *
   * @param imgReaders A vector imageReaders.
   */
  public MetadataParser(Vector imgReaders)
  {
    Node[] nodes = new Node[imgReaders.size()];
    try{
      for(int i = 0; i < imgReaders.size(); i++ ){
	nodes[i] = _getImageNode( (ImageReader) imgReaders.elementAt(i) );
      }
    }catch(Exception e){
      e.printStackTrace();
    }

    String[] formatArray = new String[nodes.length];
    int[] orientArray = new int[nodes.length];
    int[] bitsPPArray = new int[nodes.length];
    Object[] pixDimsArray = new Object[nodes.length];

    try{
      for(int i = 0; i < nodes.length; i ++ ){
        Node node = nodes[i];
	String format = ((ImageReader)imgReaders.elementAt(i)).getFormatName();
	formatArray[i] = format;
	orientArray[i] = _getOrient(node, format);
	bitsPPArray[i] = _getBitsPP(node, format);
	pixDimsArray[i] = _getPixDims(node, format);
      }
    }catch(Exception e){
      e.printStackTrace();
    }

    // Whether the readers refer to the same set of image data
    _isSame = _isSame(formatArray, orientArray, bitsPPArray, pixDimsArray);

    if(_isSame){
	_format = formatArray[0];
	_orient = orientArray[0];
	_bitsPP = bitsPPArray[0];
	_pixDims = _getPixDims(pixDimsArray, _format, _orient);
    }
    _age = _getAge(nodes[0], _format);
  }
   
  //---------------
  // Public methods
  //---------------

  /**
   * Gets the age of the subject to whom the data belongs.
   * 
   * @return The year of age.
   */
  public double getAge()
  {
      return _age;
  }

  /**
   * Gets bits per pixel.
   *
   * @return The number of bits per pixel.
   */
  public int getBitsPP()
  {
    return _bitsPP;
  }

  /**
   * Gets the name of the file format.
   *
   * @return The name of the file format.
   */
  public String getFormatName()
  {
    return _format;
  }

  /**
   * Gets the orientation of the image in the ImageReader object.
   *
   * @return A number indicating the orientation of the image. 0-axial, 
   *         1-coronal, 2-sagittal.
   */
  public int getOrient()
  {
    return _orient;
  }                 

  /**
   * Gets a voxel's dimention or resolusion.
   *
   * @return An array of three positive float number in the order of 
   *         x_dimension, y_dimension and z_dimension.
   */
  public float[] getPixDims()
  {
    float[] newDims = new float[3];
    newDims[0] = Math.abs(_pixDims[0]);
    newDims[1] = Math.abs(_pixDims[1]);
    newDims[2] = Math.abs(_pixDims[2]);
    return newDims;
  }

  /**
   * Gets a voxel's dimention or resolusion.
   *
   * @return An array of three signed float number in the order of 
   *         x_dimension, y_dimension and z_dimension.
   */
   public float[] getSignedPixDims()
   {
       return _pixDims;
   }

  /**
   * Gets an array of renormalization values.
   */ 
  public double[] getReNormValues()
  {
    return _reNormValues;
  }

  /**
   * Whether the given readers refer the same set of image data.
   */
  public boolean isSame()
  {
      return _isSame;
  }

  //----------------
  // Private Methods  
  //----------------

  /**
   * Creates a renormalization array.
   *
   * @param maxArray An array containing the maximum intensity values
   *                 of all the imaging slices.
   * 
   * @return A renormalization array, or null if the data does not need to 
   *         be renormalized..
   */
  private double[] _createReNorm(double[] maxArray)
  {
    if(maxArray == null) return null;

    // If the maxArray contains the same value, return null
    boolean isSameValue = true;
    for(int k = 0; k < maxArray.length-1; k++){
      if(maxArray[k] != maxArray[k+1]){
	  isSameValue = false;
	  break;
      }
    }
    if(isSameValue) return null;

    double maxPixel = _getMax(maxArray);
    double[] renorm = new double[maxArray.length];
    for(int i = 0; i < maxArray.length; i++){
      renorm[i] = maxArray[i] / maxPixel;
    }
    return renorm;
  }

  /**
   * Get the age of the subject to whom the data belongs.
   * 
   * @param node Node of the header of the queried file.
   * @param format Format of the queried file.
   * @return A double value indicating the value of the age. 
   */
  private double _getAge(Node node, String format)
  {
      double age = 40;
      if(format.equals(FORMAT_TYPES[1]))age = _getMincAge(node);
      return age; 
  }
    
  /**
   * Get value of bits per pixel.
   *
   * @param node Node of the file header.
   * @return an Interger indicating number of bits per pixel.
   */
  private int _getAnalyzeBitsPP(Node node)
  {
    int bitsPP = 16;
    NamedNodeMap map;
    Node attribute;
    while(node != null ){
      if(node.getNodeName().equals("IMAGE_DIMENSION")){
	map = node.getAttributes();
		
	// get the bits per pixel
	attribute = map.getNamedItem("bitpix");
	bitsPP = (new Integer(attribute.getNodeValue())).intValue();
      }
      node = node.getNextSibling();
    }
    if(bitsPP < 0) return 16;
    return bitsPP;
  }

  /**
   * Gets the image's orientation.
   * 
   * @return orient value.
   */
  private int _getAnalyzeOrient(Node node)
  {
    return 0;
  }

  /**
   * Parse Analyze metadata for the image's resolusions.
   *
   * @param node associated with the queried file.
   *
   * @return a 1D array of image's resolutions.
   */
  private float[] _getAnalyzePixDims(Node node)
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
      if(node.getNodeName().equals("IMAGE_DIMENSION")){
	map = node.getAttributes();
	for(int k = 0; k < 3; k++){
          attribute = map.getNamedItem("pixdim_"+(k+1));
	  float f = (new Float(attribute.getNodeValue())).floatValue();
	  pixdims[k] = f;
	}
      }
      node = node.getNextSibling();
    }
    return pixdims;
  }

  /**
   * Gets image's bits per pixel.
   *
   * @param node Node of the header of the queried file.
   * @param format Format of the queried file.
   * @return An Integer indicating the value of bits per pixel.
   */
  private int _getBitsPP(Node node, String format)
  {
    int bitsPP = 16;
    if(format.equals(FORMAT_TYPES[0])) bitsPP = _getAnalyzeBitsPP(node);
    else if (format.equals(FORMAT_TYPES[1])) bitsPP = _getMincBitsPP(node);
    return bitsPP;
  }

    /**
     * Parses Dicom metadata for the image's orientation.
     * 
     * @param node A Node object of the file's metadata.
     *
     * @return Orientation of the image in the ImageReader.
     */
    private int _getDicomOrient(Node node)
    {
	int orient = 0;
	NamedNodeMap map;
	Node attribute;
	NodeList datasetList;
	Node datasetNode;
	NodeList orientList;
	Node orientNode;

	while(node != null){

	    // Search for "DATA_SET"
	    if(node.getNodeName().equals("DATA_SET")){
		   
		datasetList = node.getChildNodes();
		for(int i = 0; i < datasetList.getLength(); i++){
		    datasetNode = datasetList.item(i);

		    // Search "DATA_SET" for "00200037" (orientation)
		    if(datasetNode.getNodeName().equals("00200037")){

			// Search "00200037" for "VALUE"
			orientList = datasetNode.getChildNodes();
			for(int k = 0; k < orientList.getLength(); k++){
			    orientNode = orientList.item(k);
			    if(orientNode.getNodeName() == "VALUE"){

				// Get orient value
				map = orientNode.getAttributes();
				attribute = map.getNamedItem("value");
				float f = (new Float(attribute.getNodeValue()))
				    .floatValue();
				f = Math.abs(f);
				if(f == 1f) {
				    if(k == 0){
					// axial
					orient = 0;
				    }
				    else if(k == 1){
					// sagittal
					orient = 2;
				    }
				    else if(k == 2){
					// coronal
					orient = 1;
				    }
				    else {
					// axial
					orient = 0;
				    }
				    k = orientList.getLength();
				}
			    }
			}
			break;
		    }
		}	    
	    }
	    node = node.getNextSibling();
	}
	return orient;
    }

    /**
     * Parses Dicom metadata for the images' resolusions, orientation and 
     * the number of bits per pixel.
     *
     * @param node Associated with the queried file.
     * 
     * @return A array of float values containing images' resolusions, 
     *         orientation and the number of bits per pixel.
     */
    private float[] _getDicomPixDims(Node node)
    {
      float[] pixdims = {1.0f, 1.0f, 1.0f, 1f, 1f};
      NamedNodeMap map;
      Node attribute;
      float[] pos = new float[3];

      while(node != null){

	// Search for "DATA_SET"
	if(node.getNodeName().equals("DATA_SET")){

	  NodeList datasetList = node.getChildNodes();
	  for(int i = 0; i < datasetList.getLength(); i++){
	    Node datasetNode = datasetList.item(i);

	    // Search "DATA_SET" for "00280030"
	    if(datasetNode.getNodeName().equals("00280030")){

	      // Search "00280030" for "VALUE"
	      NodeList pixelList = datasetNode.getChildNodes();

	      // Y dim
	      Node pNode = pixelList.item(0);			    
	      map = pNode.getAttributes();
	      attribute = map.getNamedItem("value");
	      float f = (new Float(attribute.getNodeValue())).floatValue();
	      pixdims[1] = f;

	      // X dim
	      pNode = pixelList.item(1);			    
	      map = pNode.getAttributes();
	      attribute = map.getNamedItem("value");
	      f = (new Float(attribute.getNodeValue())).floatValue();
	      pixdims[0] = f;
	    }

	    // Search "DATA_SET" for "00200032"
	    if(datasetNode.getNodeName().equals("00200032")){
	      for(int k = 0; k < 3; k++){
		NodeList pixelList = datasetNode.getChildNodes();
		Node pNode = pixelList.item(k);
		map = pNode.getAttributes();
		attribute = map.getNamedItem("value");
		pos[k] = (new Float(attribute.getNodeValue())).floatValue();
	      }
	    }
	  }
	}
	node = node.getNextSibling();
      }

      // Concatenate elements in array pos at the end of pixdims
      for(int k = 0; k < pos.length; k++ ){
	  pixdims[k+2] = pos[k];
      }
      return pixdims;
    }

    /**
     * Parses Dicom metadata for the image's orientation.
     * 
     * @return The value of the image's orientation. 0-axial, 1-coronal,
     *         2-sagittal.
     */
    private int _getGEOrient(Node node)
    {
	int orient;
	NamedNodeMap map;
	Node attribute;
	float[] topLeft = new float[3];
	float[] topRight = new float[3];
	float[] botRight = new float[3];

	// Vector1 = topLeft - topRight;
	float[] v1 = new float[3];

	// Vector2 = topRight - botRight;
	float[] v2 = new float[3];

	float[] crossProduct = new float[3];

	while(node != null){
	  if(node.getNodeName().equals("GE_MR_IMAGE_INFO")){
	    map = node.getAttributes();

	    // TopLeft
	    attribute = map.getNamedItem("TopLeftR");
	    topLeft[0] = (new Float(attribute.getNodeValue())).floatValue();
	    attribute = map.getNamedItem("TopLeftA");
	    topLeft[1] = (new Float(attribute.getNodeValue())).floatValue();
	    attribute = map.getNamedItem("TopLeftS");
	    topLeft[2] = (new Float(attribute.getNodeValue())).floatValue();

	    // TopRight
	    attribute = map.getNamedItem("TopRightR");
	    topRight[0] = (new Float(attribute.getNodeValue())).floatValue();
	    attribute = map.getNamedItem("TopRightA");
	    topRight[1] = (new Float(attribute.getNodeValue())).floatValue();
	    attribute = map.getNamedItem("TopRightS");
	    topRight[2] = (new Float(attribute.getNodeValue())).floatValue();

	    // BotRight
	    attribute = map.getNamedItem("BotRightR");
	    botRight[0] = (new Float(attribute.getNodeValue())).floatValue();
	    attribute = map.getNamedItem("BotRightA");
	    botRight[1] = (new Float(attribute.getNodeValue())).floatValue();
	    attribute = map.getNamedItem("BotRightS");
	    botRight[2] = (new Float(attribute.getNodeValue())).floatValue();
	  }
	  node = node.getNextSibling();
	}

	// Compute vectors
	for( int k = 0; k < 3; k++){

	    // Vector1 = topLeft - topRight;
	    v1[k] = topLeft[k] - topRight[k]; 

	    // Vector2 = topRight - botRight;
	    v2[k] = topRight[k] - botRight[k]; 
	}

	// Compute the cross product of vector 1 and vector 2
	for( int k = 0; k < 3; k++){
	    crossProduct[0] = v1[1]*v2[2] - v1[2]*v2[1];
	    crossProduct[1] = v1[2]*v2[0] - v1[0]*v2[2];
	    crossProduct[2] = v1[0]*v2[1] - v1[1]*v2[0];
	}

	// Determine the orientation of the patient by recognizing the 
	// axis with biggest values
	float x = Math.abs(crossProduct[0]);
	float y = Math.abs(crossProduct[1]);
	float z = Math.abs(crossProduct[2]);
	float m = Math.max(Math.max(x, y), Math.max(y, z));
	if(m == x) orient = 2;
	else if(m == y)orient = 1;
	else if(m == z)orient = 0;
	else orient = 0;
	return orient;
    }

    /**
     * Parses GE metadata for the image's resolusions.
     *
     * @param node Node object associated with the queried file.
     *
     * @return An 1D array of the image's resolutions.
     */
    private float[] _getGEPixDims(Node node)
    {
	float[] pixdims = {1.0f, 1.0f, 1.0f};
	NamedNodeMap map;
	Node attribute;
	float f;
	while(node != null){
	    if(node.getNodeName().equals("GE_MR_IMAGE_INFO")){
		map = node.getAttributes();

		// Get X dim
		attribute = map.getNamedItem("pixelSizeX");
		f = (new Float(attribute.getNodeValue())).floatValue();
		pixdims[0] = f;

		// Get Y dim
		attribute = map.getNamedItem("pixelSizeY");
		f = (new Float(attribute.getNodeValue())).floatValue();
		pixdims[1] = f;

		// Get Z position of node1
		// Axial
		if(_orient == 0)  attribute = map.getNamedItem("TopLeftS");
		
		// Coronal
		else if(_orient == 1) attribute=map.getNamedItem("TopLeftA");
		
		// Sagittal
		else attribute = map.getNamedItem("TopLeftR");
		f = (new Float(attribute.getNodeValue())).floatValue();
		pixdims[2] = f;
	    }
	    node = node.getNextSibling();
	}
	return pixdims;
    }

  /**
   * Gets image's node from image reader.
   *
   * @param reader ImageReader of the queried file.
   * @return Node of the header file.
   */
  private Node _getImageNode(ImageReader reader) throws IOException
  {
    Node node = null;
    try{
      // if stream metadata is not available, return the default pixdims
      if(reader == null || reader.getStreamMetadata() == null){
	String msg = "Cannot find header information. ";
	throw new IOException(msg);
      }

      // Get the IIOMetadata
      IIOMetadata metadata = reader.getStreamMetadata();
      String[] formatNames = metadata.getMetadataFormatNames();

      // Get the IIO Metadata Node
      int whichTree = 0;
      node = metadata.getAsTree(formatNames[whichTree]);
      node = node.getFirstChild();
    }catch(Exception e){
	e.printStackTrace();
    }
    return node;
  }

  /**
   * Gets the max value of the array.
   *
   * @param array to search max within.
   * @return max value of the array.
   */
  private double _getMax(double[] array)
  {
    double max = array[0];
    for(int i = 1; i < array.length; i++){
      if(max < array[i]) max = array[i];
    }
    return max;
  }

  /**
   * Gets the max pixel values, each corresponding to the maximum pixel 
   * value of one slice.
   *
   * @param node Node of the file header.
   * @param numOfImages Number of images.
   * 
   * @return An array of max pixel values, or null if header does not carry 
   *         the information. 
   */
  private double[] _getMaxPixelValues(Node node, int numOfImages)
  {
    return _getMaxOrMinPixelValues(node, numOfImages, "image-max");
  }

  /**
   * Get the age of the subject to whom the data belongs.
   * 
   * @param node Node of the header of the queried file.
   * @return A double value indicating the value of the age. 
   */
  private double _getMincAge(Node node)
  {
    double age = 40;
    NamedNodeMap map;
    Node attribute;
    while(node != null ){
     if(node.getNodeName() == "VARIABLES"){
	NodeList list = node.getChildNodes();
	for(int i = 0; i < list.getLength(); i++){
	  Node nodeForPatient = list.item(i);

	  // Search for "patient"
	  if(nodeForPatient.getNodeName().equals("patient")){
	    NodeList listUnderPatient = nodeForPatient.getChildNodes();
	    for(int n = 0; n < listUnderPatient.getLength(); n++){
	      Node nodeForATTR = listUnderPatient.item(n);

	      // Search for "ATTRIBUTES"
	      if(nodeForATTR.getNodeName().equals("ATTRIBUTES")){
		NodeList listUnderATTR = nodeForATTR.getChildNodes();
		for(int m = 0; m < listUnderATTR.getLength(); m++){
		  Node nodeForAge = listUnderATTR.item(m);
	
		  // Search for "age"
		  if(nodeForAge.getNodeName().equals("age")){
		    NodeList ageList = nodeForAge.getChildNodes();
		    Node ageNode = ageList.item(0);		    
		    map = ageNode.getAttributes();
		    attribute = map.getNamedItem("value");		  
		    age = (new Double(attribute.getNodeValue())).doubleValue();
		  }
		}
	      }
	    }
	  }
	}
      }
     node = node.getNextSibling();
    }
    return age;
  }

  /**
   * Gets the minimum pixel values, each corresponding to the minimum pixel
   * value of one slice.
   *
   * @param node Node of the file header.
   * @param numOfImages number of images.
   * @return array of min pixel values, or null if header does not carry the
   *         information. 
   */
  private double[] _getMinPixelValues(Node node, int numOfImages)
  {
    return _getMaxOrMinPixelValues(node, numOfImages, "image-min");
  }

  /**
   * Gets Maximum or Minimum pixel values, each corresponding to the maximum
   * or minimum pixel value of one slice.
   *
   * @param node Node of the file header.
   * @param numOfImages Number of images.
   * @param nodeName Name of the node containing the max or min pixel 
   *                 values.
   * 
   * @return An array of max or min pixel values, or null if header does not 
   *         carry the information.
   */
  private double[] _getMaxOrMinPixelValues(Node node, int numOfImages,
					   String nodeName)
  {
    double[] max = null;
    NamedNodeMap map;
    Node attribute;
    while(node != null ){
      if(node.getNodeName() == "VARIABLES"){
	NodeList list = node.getChildNodes();
	for(int i = 0; i < list.getLength(); i++){
	  Node nodeForImageMax = list.item(i);

	  // Search for "image-max"
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
		    max = new double[numOfImages];
		    Node elementNode = null;
		    for(int k = 0; k < max.length; k++){
		      elementNode = arrayList.item(k);
		      map = elementNode.getAttributes();
		      attribute = map.getNamedItem("value");
		      max[k] = (new Double(attribute.getNodeValue())) 
			  .doubleValue();
		    }
		  }
		}
	      }
	    }
	  }
	}
      }
      node = node.getNextSibling();
    }
    return max;
  }

  /**
   * Gets value of bits per pixel.
   *
   * @param node Node of the file header.
   * @return an Interger indicating number of bits per pixel.
   */
  private int _getMincBitsPP(Node node)
  {
    int bitsPP = 16;
    NamedNodeMap map;
    Node attribute;
    while(node != null){
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
		String s = attribute.getNodeValue();
		if(s.equals("byte")) bitsPP = 8;
		else if(s.equals("short")) bitsPP = 16;
	      }
	    }
	  }
	}
      }
      node = node.getNextSibling();
    }
    return bitsPP;
  }

  /**
   * Get the range of data.
   *
   * @param node Node of the file header.
   * @return A two-element double array: the first element is the minimum 
   *         intensity value of the data and the second element is the maximum
   *         intensity value of the data. 
   */
  private double[] _getMincRange(Node node)
  {
    double[] max = new double[2];
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
	      Node nodeForATTR = listUnderImage.item(n);

	      // Search for "ATTRIBUTES"
	      if(nodeForATTR.getNodeName().equals("ATTRIBUTES")){
		NodeList listUnderATTR = nodeForATTR.getChildNodes();
		for(int m = 0; m < listUnderATTR.getLength(); m++){
		  Node nodeForValRan = listUnderATTR.item(m);

		  // search for "valid_range"
		  if(nodeForValRan.getNodeName().equals("valid_range")){
		    NodeList valRanList = nodeForValRan.getChildNodes();
		    for(int k = 0; k < max.length; k++){
		      Node valRanNode = valRanList.item(k);
		      map = valRanNode.getAttributes();
		      attribute = map.getNamedItem("value");
 		      max[k] = (new Double(attribute.getNodeValue()))  
 			  .doubleValue();
		    }
		  }
		}
	      }
	    }
	  }
	}
      }
      node = node.getNextSibling();
    }
    return max;
  }

  /**
   * Gets image's orientation.
   * 
   * @return Orientation of the image. 0-aixal, 1-coronal, 2-sagittal.
   */
  private int _getMincOrient(Node node)
  {
    int orient = 2;
    NamedNodeMap map;
    Node attribute;
    String spaceName = null;
    float[] dims = null;

    while(node != null) {
      if(node.getNodeName() == "VARIABLES"){

	NodeList list = node.getChildNodes();
	for(int i = 0; i < list.getLength(); i++){
	  Node nodeForImage = list.item(i);

	  // Search "VARIABLES" for "image"
	  if(nodeForImage.getNodeName().equals("image")){
	    // Search "image" for "DATA"
	    NodeList listUnderImage = nodeForImage.getChildNodes();
	    for(int k = 0; k < listUnderImage.getLength(); k++){
	      Node nodeForDATA = listUnderImage.item(k);
	      if(nodeForDATA.getNodeName().equals("DATA")){

		// Get the first child of DATA
	        NodeList listUnderDATA = nodeForDATA.getChildNodes();
		Node nodeForArray = listUnderDATA.item(0);
		map = nodeForArray.getAttributes(); 
		attribute = map.getNamedItem("dimension");
		spaceName = attribute.getNodeValue();
	      }
	    }
	    i = list.getLength();
	  }
	}
	if(spaceName == null) return orient;
	for(int k = 0; k < list.getLength(); k++){
	  Node nodeForSpace = list.item(k);

	  // Search "VARIABLES" for spaceName.
	  if(nodeForSpace.getNodeName().equals(spaceName)){
	    NodeList listUnderSpace = nodeForSpace.getChildNodes();
	    for(int i = 0; i < listUnderSpace.getLength(); i++){
	      Node nodeForATT = listUnderSpace.item(i);

	      // Search spaceName for "ATTRIBUTES"
	      if(nodeForATT.getNodeName().equals("ATTRIBUTES")){
		NodeList listUnderATT = nodeForATT.getChildNodes();
		for(int n = 0; n < listUnderATT.getLength(); n++){
		  Node nodeForDirCos = listUnderATT.item(n);
		  
		  // Search spaceName for "direction_cosines"
		  if(nodeForDirCos.getNodeName().equals("direction_cosines")){
		    dims = new float[3];
		    NodeList dimList = nodeForDirCos.getChildNodes();
		    for(int m = 0; m < dims.length; m++){
		      Node dimNode = dimList.item(m);
		      map = dimNode.getAttributes();
		      attribute = map.getNamedItem("value");
		      dims[m] = (new Float(attribute.getNodeValue()))
			.floatValue();
		    }
		  }
		}
	      }
	    }
	  }
	}
      }
      node = node.getNextSibling();
    }

    if(dims != null){
      if( (Math.abs(dims[0]) > Math.abs(dims[1])) 
	  && (Math.abs(dims[0]) > Math.abs(dims[2]))){
	orient = 2;
      }
      else if( (Math.abs(dims[1]) > Math.abs(dims[0]))
	       &&  (Math.abs(dims[1]) > Math.abs(dims[0]))){
	orient = 1;}
      else orient = 0;
    }
    else {
      if(spaceName.equals("xspace")) orient = 2;
      else if (spaceName.equals("yspace")) orient = 1;
      else orient = 0;
    }
    return orient;
  }

  /**
   * Parsees Analyze metadata for the image's resolusions.
   *
   * @param node Associated with the queried file.
   *
   * @return An 1D array of image's resolutions.
   */
  private float[] _getMincPixDims(Node node)
  {
    float[] pixdims = {1f, 1f, 1f};
    NamedNodeMap map;
    Node attribute;
    while(node != null) {
      if(node.getNodeName().equals("VARIABLES")){

	// Search "VARIABLES" for "xspace"
	NodeList childNodesVar = node.getChildNodes();
	for(int i = 0; i < childNodesVar.getLength(); i++){
	  Node childNodeVar = childNodesVar.item(i);
	  if(childNodeVar.getNodeName().equals("xspace")){

	    // Search "xspace" for "ATTRIBUTES"
	    NodeList cNodesXSpace = childNodeVar.getChildNodes();
	    for(int k = 0; k < cNodesXSpace.getLength(); k++){
	      Node cNodeXSpace = cNodesXSpace.item(k);
	      if(cNodeXSpace.getNodeName().equals("ATTRIBUTES")){

		// Search "ATTRIBUTES" for "step"
		NodeList cNodesAtt = cNodeXSpace.getChildNodes();
		for(int m = 0; m < cNodesAtt.getLength(); m++){
		  Node cNodeAtt = cNodesAtt.item(m);
		  if(cNodeAtt.getNodeName().equals("step")){

		    //Search "step" for "VALUE"
		    NodeList cNodesStep = cNodeAtt.getChildNodes();
		    for(int n = 0; n < cNodesStep.getLength(); n++){
		      Node cNodeStep = cNodesStep.item(n);
		      if(cNodeStep.getNodeName().equals("VALUE")){

			// Get x step
			map = cNodeStep.getAttributes();
			attribute = map.getNamedItem("value");
			float f = (new Float(attribute.getNodeValue()))
			    .floatValue();
			pixdims[0] = f;
		      }
		    }
		  }
		}	
	      }	    
	    }
	  }

	  // Search "VARIABLES" for "yspace"
	  if(childNodeVar.getNodeName().equals("yspace")){

	    // Search "yspace" for "ATTRIBUTES"
	    NodeList cNsYSpace = childNodeVar.getChildNodes();
	    for(int k = 0; k < cNsYSpace.getLength(); k++){
	      Node cNYSpace = cNsYSpace.item(k);
	      if(cNYSpace.getNodeName().equals("ATTRIBUTES")){

		// Search "ATTRIBUTES" for "step"
		NodeList cNsAtt = cNYSpace.getChildNodes();
		for(int m = 0; m < cNsAtt.getLength(); m++){
		  Node cNAtt = cNsAtt.item(m);
		  if(cNAtt.getNodeName().equals("step")){

		    // Search "step" for "VALUE"
		    NodeList cNsStep = cNAtt.getChildNodes();  
		    for(int n = 0; n < cNsStep.getLength(); n++){
		      Node cNStep = cNsStep.item(n);
		      if(cNStep.getNodeName().equals("VALUE")){

			// Get y step
			map = cNStep.getAttributes();
			attribute = map.getNamedItem("value");
			float f = (new Float(attribute.getNodeValue()))
			    .floatValue();
			pixdims[1] = f;
		      }
		    }
		  }
		}
	      }
	    }
	  }

	  // Search "VARIABLES" for "yspace"
	  if(childNodeVar.getNodeName().equals("zspace")){

	    // Search "yspace" for "ATTRIBUTES"
	    NodeList cNsZSpace = childNodeVar.getChildNodes();
	    for(int k = 0; k < cNsZSpace.getLength(); k++){
	      Node cNZSpace = cNsZSpace.item(k);
	      if(cNZSpace.getNodeName().equals("ATTRIBUTES")){

		// Search "ATTRIBUTES" for "step"
		NodeList cNsAtt = cNZSpace.getChildNodes();
		for(int m = 0; m < cNsAtt.getLength(); m++){
		  Node cNAtt = cNsAtt.item(m);
		  if(cNAtt.getNodeName().equals("step")){

		    // Search "step" for "VALUE"
		    NodeList cNsStep = cNAtt.getChildNodes();  
		    for(int n = 0; n < cNsStep.getLength(); n++){
		      Node cNStep = cNsStep.item(n);
		      if(cNStep.getNodeName().equals("VALUE")){

			// Get y step
			map = cNStep.getAttributes();
			attribute = map.getNamedItem("value");
			float f = (new Float(attribute.getNodeValue()))
			    .floatValue();
			pixdims[2] = f;
		      }
		    }
		  }
		}
	      }
	    }
	  }
	}
      }
      node = node.getNextSibling();
    }
    return pixdims;
  }

  /**
   * Gets orientation of the image.
   *
   * @return The value of the image's orientation. 0-axial, 1-coronal, 
   *         2-sagittal.
   */
  private int _getOrient(Node node, String format)
  {
    int orient = 0;

    // Check Analyze file format and get the pixdims
    if(format.equals(FORMAT_TYPES[0])) orient = _getAnalyzeOrient(node);

    // Check Minc file format and get the pixdims
    else if(format.equals(FORMAT_TYPES[1])) orient = _getMincOrient(node);

    // Check Dicom file format and get the pixdims
    else if(format.equals(FORMAT_TYPES[2]))
	return _getDicomOrient(node);

    // Check GE file format and get the pixdims
    else if(format.equals(FORMAT_TYPES[3]))
	return _getGEOrient(node);

    // Assume ucf file always in coronal orient
    else if(format.equals(FORMAT_TYPES[4])) orient = 1;
    else orient = 0;
    return orient;
  }                 

  /**
   * Gets the image's resolutions.
   *
   * @return an 1D array containing three values.
   */
  private float[] _getPixDims(Node node, String format)
  {
    float[] defaultPixdims = {1.0f, 1.0f, 1.0f};
	
    // Check Analyze file format and get the pixdims
    if(format.equals(FORMAT_TYPES[0])){
      float[] dims = _getAnalyzePixDims(node);

      // Reset the dims in the order of dim_x, dim_y and dim_z.
      float[] newDims = new float[3];
      if(_orient == -1 || _orient == 0 || _orient == 3){
	newDims = dims;
      }
      else if (_orient == 1 || _orient == 4 ){
        newDims[0] = dims[0];
	newDims[1] = dims[2];
	newDims[2] = dims[1];
      } 
      else {
        newDims[0] = dims[2];
	newDims[1] = dims[0];
	newDims[2] = dims[1];
       }
      return newDims;
    }

    // Check Minc file format and get the pixdims. The dims in Minc format is 
    // already in the order of dim_x, dim_y and dim_z
    else if(format.equals(FORMAT_TYPES[1])) return _getMincPixDims(node);

    else if(format.equals(FORMAT_TYPES[2])) return _getDicomPixDims(node);

    else if(format.equals(FORMAT_TYPES[3])) return _getGEPixDims(node);

    else return defaultPixdims;
  }    
  
  /**
   * Gets pixel dimensions. 
   */  
  private float[] _getPixDims(Object[] pixDimsArray, String format, int orient)
  {
    float[] pixDims = new float[3];
    float[] pd0 = (float[]) pixDimsArray[0];
    float[] pd1 = (float[]) pixDimsArray[1];

    // Same dimension in x and y direction
    pixDims[0] = pd0[0];
    pixDims[1] = pd0[1];
    
    if( format.equals(FORMAT_TYPES[2]) ){

      // The coords of the pixel at up-left corner in image0
      float[] pos0 = new float[3];

      // The coords of the pixel at up-left corner in image1
      float[] pos1 = new float[3];
      for(int k = 0; k < 3; k++){
	pos0[k] = pd0[k+2];
	pos1[k] = pd1[k+2];
      }
	  
      if(orient == 0) pixDims[2] = pos1[2] - pos0[2];
      else if(orient == 1) pixDims[2] = pos1[1] - pos0[1];
      else pixDims[2] = pos1[0] - pos0[0];
	  
      // If pixdim[2] is zero, try to find a non-zero value for it 
      if(pixDims[2] == 0) {
	for(int i = 0; i < 3; i++){
	  float dis = pos1[i] - pos0[i];
	  if (dis != 0) {
	    pixDims[2] = dis;
	    break;
	  }
	}
      }
    }
    else if (format.equals(FORMAT_TYPES[3]) ){
      pixDims[2] = pd1[2] - pd0[2];
    }
    return pixDims;
  }

  /**
   * Check if all the files have the same file format, orient, bitsPP and
   * pixDims.
   *
   * @param formatArray An array of String values. Each element is 
   *                    the format of an image file.
   * @param orientArray An array of int value. Each element is the 
   *                    orientation of an image file.
   * @param bitsPPArray An array of int values. Each element is the
   *                    number of bits per pixel of an image file.
   * @param pixDimsArray An array ob Ojbects. Each element is a two element
   *                     array indicating the pixel's dimensions in x and y 
   *                     axises. 
   */
  private boolean _isSame(String[] formatArray, int[] orientArray, 
			  int[] bitsPPArray, Object[] pixDimsArray)
  {
    boolean isSame = true;
    float[] pixDims1;
    float[] pixDims2;
    for(int i = 0; i < ( formatArray.length - 1); i++ ){
      pixDims1 = (float[]) pixDimsArray[i];
      pixDims2 = (float[]) pixDimsArray[i+1];
      if( formatArray[i].equals(formatArray[i+1]) 
	  && orientArray[i] ==orientArray[i+1]
	  && bitsPPArray[i] == bitsPPArray[i+1] 
	  && pixDims1[0] == pixDims2[0] && pixDims1[1] == pixDims2[1] ){ 
      }
      else {
	  isSame = false;
	  break;
      }
    }
    return isSame;
  }
}
