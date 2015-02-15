package Texture;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;
import org.medtoolbox.jviewbox.viewport.annotation.DynamicAnnotationShape;

import tools.GeometryContour;
//import tools.Matrix;
import LevelSet.SkullStripper;

public class RunLengthMat extends ViewportTool{
	
	/*private static double[][] GrayValue;
	private static int[][] GLRmatrix,GLRPNmatrix,OGLRmatrix,OGLRPNmatrix;
	private static int[] GLRNVector,RLRNVector,OGLRNVector,ORLRNVector;
	private static BufferedImage origimage;
	private static double Nr=0,ONr=0;
	private static int w1,h1;
	private static int _xbegin, _ybegin, xend,yend;
	private static int ROItotal = 0;
	private static int imagenumber = 0;
	static double[][][] Values = new double[2][4][11];
	*/
	private static int[][] isCheck;
	//test用 記得刪掉
	/*private static int[][] test = {{0,1,2,3},{1,1,2,3},{2,2,2,3},{3,3,3,3}};
	private static int[][] isCheckT = new int[4][4];
	private static int[][] testPNmatrix;
	private static int[] testNVector,testRNVector;*/
	/**
	 * Cursor point.
	 */
	//private Point _cursorPoint = new Point();
	
	/**
	 * Vector matrix
	 */
	//private Vector _Matrix;
	
	/**
	 * Annotation dynamic shape.
	 */
	//private DynamicAnnotationShape _dyShape;
	
	/**
	 * Computation function.
	 */
	//private Matrix _matrix;
	
	/**
	 * Geometry contour for the inital placement.
	 */
	//private GeometryContour _geometryContour;
	/**
	 * @mask = phi
	 */
	private double[][] mask;
	
	private static int ArraySize;
	
	/*public RunLengthMat(Matrix matrix,Vector VMatrix){
		super("RunLengthMat", "RunLengthMat of Image", "", "");
		_matrix = matrix;
		_Matrix = VMatrix;
        isCheck = new int[h1][w1];
	}*/
	
	/*public void mouseMoved(ViewportCluster vpc, Viewport vp, MouseEvent e) {
		int slicenum;
		for(slicenum=0;slicenum<vpc.getViewports().size();slicenum++){
			if(vp==(Viewport)vpc.getViewports().get(slicenum)){
					break;
				}
		}   
			//Iterator it = vpc.getViewports().iterator();
			//_origViewport = (Viewport) it.next();
			xwindowsize = _matrix.getInitialBoxWidth();
			ywindowsize = _matrix.getInitialBoxHeight();

			try {
				//if (vp == _origViewport) {
					vp.removeAnnotation(_dyShape);
					// Transform the mouse point to annotation coordinates.
					AffineTransform annotationTransform = vp
							.getViewportTransform();
					annotationTransform.concatenate(vp.getImageTransform());
					annotationTransform.inverseTransform(e.getPoint(),
							_cursorPoint);
					// If the initial contour is square
					// if (_isSquare) {
					_xStart = _cursorPoint.x - xwindowsize / 2;
					_yStart = _cursorPoint.y - ywindowsize / 2;
					// Create a sqaure shaped contour.
					_geometryContour = new GeometryContour(_xStart, _yStart,
							xwindowsize, ywindowsize);
					_dyShape = new DynamicAnnotationShape(
							_geometryContour.rectangle);
					// }
					// If the initial contour is not square but circle.
					/*
					 * else { // Set the radius be the minimum of width and
					 * height. int radius = Math.min(_width, _height); radius /=
					 * 2; // Create a circle-like contour. _geometryContour =
					 * new GeometryContour(_cursorPoint, radius); _dyShape = new
					 * DynamicAnnotationShape(_geometryContour.circlePolygon); }
					 
					_dyShape.setForegroundColor(Color.yellow);
					_dyShape.setBackgroundColor(null);
					vp.addAnnotation(_dyShape);
					// Repaint the Viewport
					vpc.repaint(vp);
				//}
			} catch (Exception ex) {
				System.out.println("Error:  " + ex.getMessage());
				ex.printStackTrace();
			}
	}*/
	
//	public void  mousePressed(ViewportCluster vpc, Viewport vp, MouseEvent e, int button){
//		//construct gray-level
//		_xbegin = _xStart;
//		_ybegin = _yStart;
//		isCheck = new int[h1][w1];
//		//to determine which slice it is.
//		int slicenum;
//		System.out.println("size is "+vpc.getViewports().size());
//		for(slicenum=0;slicenum<vpc.getViewports().size();slicenum++){
//			if(vp==(Viewport)vpc.getViewports().get(slicenum)){
//				System.out.println("In and slicenum is "+slicenum);
//					break;
//				}
//		}   
//		SkullStripper skultemp = (SkullStripper) _skullstripper.get(slicenum);
//		Matrix temp = (Matrix) _Matrix.get(slicenum);
//		origimage = skultemp.getInputImage();
//		_xbegin = skultemp.getxbegin();
//		xend = skultemp.getxend();
//		_ybegin = skultemp.getybegin();
//		yend = skultemp.getyend();
//		mask = skultemp.getmask();
//		w1 = origimage.getWidth();  //1200
//	    h1 = origimage.getHeight(); //1242;
//		Scanner scan=new Scanner(System.in);
//		System.out.println("請以45為最小加值輸入角度(0，45，90...)");
//		int dir=scan.nextInt();
//        int xdir = 0, ydir = 0;
//        int total = 0;
//        GrayValue = new double[h1][w1];
//        BufferedImage gray = new BufferedImage(w1, h1, 1);// new image
//        int value,max = 0;
//        Raster origRaster = origimage.getData();
//        for (int i = 0; i < w1; i++) {
//            for (int j = 0; j < h1; j++) {
//            	value = (int) origRaster.getSampleDouble(i, j, 0); // read and store pixel value
//                if(max < value)
//                	max = value;
//                GrayValue[j][i] = value;
//                gray.setRGB(i, j, value);
//            }
//        }
//        //浪費了 [max+1][0]的空間
//        GLRmatrix = new int[2][max+1][Math.max(xwindowsize,ywindowsize)+1];
//        GLRPNmatrix = new int[2][max+1][Math.max(xwindowsize,ywindowsize)+1];
//        GLRNVector = new int[2][max+1];
//        RLRNVector = new int[2][Math.max(xwindowsize,ywindowsize)+1];
//        switch(dir){
//        case 0:
//        	xdir = 1;
//        	ydir = 0;
//        	break;
//        case 45:
//        	xdir = 1;
//        	ydir = 1;
//        	break;
//        case 90:
//        	xdir = 0;
//        	ydir = 1;
//        	break;
//        case 135:
//        	xdir = -1;
//        	ydir = 1;
//        	break;
//        }
//        int length;
//        /**
//		 * 此處_xBegin _yBegin xend yend mask 皆由skullstripper中取得，目前skullstripper並無這些函數 可以的話試看看自己修改
//         */
//        for(int inside = 0;inside<2;inside++){
//        	for(nt i = _ybegin ; i < yend;i++)
//        		for(nt j = _xbegin ; j < xend; j++){
//        			if(isCheck[i][j] == 0 && mask[i][j] == inside){
//        				//length需要在修改
//        				length = Length(i,j,xdir,ydir,ybegin+ ywindowsize,xbegin + xwindowsize);
//        				GLRmatrix[inside][(int)GrayValue[i][j]][length]++;
//        			}
//        		}
//        }
//        for(int inside = 0;inside<2;inside++){
//        	for(int i = 0; i<= max ;i++){
//        		for(int j =1; j <= Math.max(xwindowsize,ywindowsize);j++){
//        			GLRPNmatrix[inside][i][j] = GLRmatrix[inside][i][j] * j;
//        			total += GLRmatrix[inside][i][j];
//        		}
//        		GLRNVector[inside][i] = total;
//        		total = 0;
//        	}
//        }
//        for(int inside = 0;inside<2;inside++){
//        for(int j =1;j<= Math.max(xwindowsize,ywindowsize);j++){
//        	for(int i = 0 ; i <= max;i++)
//        		total += GLRmatrix[inside][i][j];
//        	RLRNVector[inside][j] = total;
//        	Nr += total;
//        	total = 0;
//        }
//        }
//        for(int i=0;i<2;i++){
//        	if(i == 0)
//        		System.out.print("===================Outside==================");
//        	else
//        		System.out.print("===================Inside==================");
//        	SRE(i);
//        	LRE(i);
//        	GLN(max,i);
//        	RLN(i);
//        	RP(i);
//        	LGRE(max,i);
//        	HGRE(max,i);
//        	SRLGE(i);
//        	SRHGE(i);
//        	LRLGE(i);
//        	LRHGE(i);
//        }
//	}

	public RunLengthMat(double[][] phi, BufferedImage orig, int xbegin,int ybegin,int xen, int yen,int roitotal,int imagenum,int arraysize){
		super("RunLengh", "RunLengthMat of Image", "", "");
		double[][] GrayValue;
		int[][] GLRmatrix,GLRPNmatrix,OGLRmatrix,OGLRPNmatrix;
		int[] GLRNVector,RLRNVector,OGLRNVector,ORLRNVector;
		BufferedImage origimage;
		double Nr=0,ONr=0;
		int w1,h1;
		int _xbegin, _ybegin;
		int xend;
		int yend;
		int ROItotal = 0;
		int imagenumber = 0;
		double[][][] Values = new double[2][4][11];
		long StartTime = System.currentTimeMillis();
		mask = phi;
		ROItotal = roitotal;
		imagenumber = imagenum;
		origimage = orig;
		_xbegin = xbegin;
		xend = xen;
		_ybegin = ybegin;
		yend = yen;
		w1 = origimage.getWidth();  //1200
	    h1 = origimage.getHeight(); //1242;
	    ArraySize = arraysize;
		isCheck = new int[ArraySize][ArraySize];
		int xdir = 0, ydir = 0;
		int total = 0;
		GrayValue = new double[h1][w1];
		int value, max = 3, maxgray = 0,level = 256; //origin max =0, modify max = 3
		Raster origRaster = origimage.getData();
		for (int i = 0; i < w1; i++) {
			for (int j = 0; j < h1; j++) {
				value = (int) origRaster.getSampleDouble(i, j, 0); // read and store pixel value
				if (max < value)
					max = value;
				GrayValue[j][i] = value;
			}
		}
		for(int i=1;i<20;i++)
			if(Math.pow(2, i) >= max)
				maxgray = (int) Math.pow(2, i);
		for (int i = 0; i < w1; i++) {
			for (int j = 0; j < h1; j++) {
				GrayValue[j][i] = Math.round((GrayValue[j][i] / maxgray)
						* (level - 1) * 10) / 10;
			}
		}
		//System.out.println("max is "+max);
		// 浪費了 [max+1][0]的空間
		/*int[][] testmatrix = new int[4][5];
		testPNmatrix = new int[4][5];
		testNVector = new int[4];
		testRNVector = new int[5];*/
		//GLRmatrix = new int[max + 1][Math.max(yend-_ybegin,xend-_xbegin) + 2];
		//GLRPNmatrix = new int[max + 1][Math.max(yend-_ybegin,xend-_xbegin) + 2];
		//GLRNVector = new int[max + 1];
		//RLRNVector = new int[Math.max(yend-_ybegin,xend-_xbegin) + 2];
		
		OGLRmatrix = new int[level + 1][ArraySize + 1];  //OGLRmatrix = new int[max + 1][Math.max(h1,w1) + 1];
		OGLRPNmatrix = new int[level + 1][ArraySize + 1]; //OGLRPNmatrix = new int[max + 1][Math.max(h1,w1) + 1];
		OGLRNVector = new int[level + 1];
		ORLRNVector = new int[ArraySize + 1]; //ORLRNVector = new int[Math.max(h1,w1) + 1];
		StringBuffer Lre = new StringBuffer();
		StringBuffer Gln = new StringBuffer(); 
		StringBuffer Rln = new StringBuffer();
		StringBuffer Lrlge = new StringBuffer();
		StringBuffer Lrhge = new StringBuffer();
    	FileWriter fw =null;
    	BufferedWriter bw =null;
    	FileWriter fw1 =null;
    	BufferedWriter bw1 =null;
    	FileWriter fw2=null;
    	BufferedWriter bw2 =null;
    	FileWriter fw3 =null;
    	BufferedWriter bw3 =null;
    	FileWriter fw4 =null;
    	BufferedWriter bw4 =null;
		for (int dir = 0; dir <= 135; dir += 45) {
			switch (dir) {
			case 0:
				xdir = 1;
				ydir = 0;
				break;
			case 45:
				xdir = 1;
				ydir = -1;
				break;
			case 90:
				xdir = 0;
				ydir = -1;
				break;
			case 135:
				xdir = -1;
				ydir = -1;
				break;
			}
			//outside的GLRmatrix那些的要額外定義了 outside的function也要重做
			int length = 0;
			
			/**
			 * 此處_xBegin _yBegin xend yend mask
			 * 皆由skullstripper中取得，目前skullstripper並無這些函數 可以的話試看看自己修改
			 */
			//if(dir == 0){
				/*for (int i = _ybegin; i <= yend; i++)
					for (int j = _xbegin; j <= xend; j++) {
						if (isCheck[i][j] == 0 && mask[i][j] == 1) {
							length = Length(i, j, xdir, ydir, mask,1);
							GLRmatrix[(int) GrayValue[i][j]][length]++;
						}
					}*/
				for (int i = 0; i < h1; i++){
					_ybegin = i - (ArraySize / 2);
					yend = i+(ArraySize / 2);
					for (int j = 0; j < w1; j++) {
						_xbegin = j - (ArraySize / 2);
						xend = j + (ArraySize / 2);
						double lre,gln,rln,lrlge,lrhge;
						for (int im = i - (ArraySize / 2); im <= i+(ArraySize / 2);im++)
							for(int in = j - (ArraySize / 2); in <= j + (ArraySize / 2); in++){
								if (im >= 0 && im < h1 && in >= 0 && in < w1) {
									if(isCheck[yend - im][xend - in]==0){
										length = Length(im, in, xdir, ydir, mask, 0,_xbegin,_ybegin,yend,xend,GrayValue);
										length += Length(im, in, -xdir, -ydir, mask, 0,_xbegin,_ybegin,yend,xend,GrayValue);
										length--;
										OGLRmatrix[(int) GrayValue[im][in]][length]++;
									}
								}
							}
							for (int k = 0; k <= level; k++) {
								for (int l = 1; l <= ArraySize; l++) {
									OGLRPNmatrix[k][l] = OGLRmatrix[k][l] * l;
									total += OGLRmatrix[k][l];
								}
								OGLRNVector[k] = total;
								total = 0;
							}
							for (int l = 1; l <= ArraySize; l++) {
								for (int k = 0; k <= level; k++){
									total += OGLRmatrix[k][l];
								}
								ORLRNVector[l] = total;
								ONr += total;
								total = 0;
							}
							lre = LRE(0,ORLRNVector,ONr);
							gln = GLN(level, 0,OGLRNVector,ONr);
							rln = RLN(0,ORLRNVector,ONr);
							lrlge = LRLGE(0,OGLRPNmatrix,ONr);
							lrhge = LRHGE(0,OGLRmatrix,ONr);
							Lre.append(lre);
							Gln.append(gln);
							Rln.append(rln);
							Lrlge.append(lrlge);
							Lrhge.append(lrhge);
							if(j!=w1-1){
								Lre.append("\t");
								Gln.append("\t");
								Rln.append("\t");
								Lrlge.append("\t");
								Lrhge.append("\t");
							}
							for(int m = isCheck.length-1; m >= 0; m--)
								Arrays.fill(isCheck[m], 0);
							/*for(int m=GLRmatrix.length-1;m>=0;m--){
								Arrays.fill(GLRmatrix[m], 0);
								Arrays.fill(GLRPNmatrix[m], 0);
							}*/
							for(int m=OGLRmatrix.length-1;m>=0;m--){
								Arrays.fill(OGLRmatrix[m], 0);
								Arrays.fill(OGLRPNmatrix[m], 0);
							}
							//Arrays.fill(GLRNVector,0);
							//Arrays.fill(RLRNVector,0);
							Arrays.fill(OGLRNVector,0);
							Arrays.fill(ORLRNVector,0);
							ONr=0;
						}
						try {
							//fw = new FileWriter("C:/Users/cebleclipse/Desktop/RunLength_point/Image"+imagenumber+"/Lre_RunL_"+dir+".txt ", true);
							fw = new FileWriter("../../RunLength_point/Image"+imagenumber+"/Lre_RunL_"+dir+".txt ", true);
							bw = new BufferedWriter(fw);
							bw.write(Lre.toString());
							bw.newLine();
							fw1 = new FileWriter("../../RunLength_point/Image"+imagenumber+"/Gln_RunL_"+dir+".txt ", true);
							bw1 = new BufferedWriter(fw1);
							bw1.write(Gln.toString());
							bw1.newLine();		
							fw2 = new FileWriter("../../RunLength_point/Image"+imagenumber+"/Rln_RunL_"+dir+".txt ", true);
							bw2 = new BufferedWriter(fw2);
							bw2.write(Rln.toString());
							bw2.newLine();	
							fw3 = new FileWriter("../../RunLength_point/Image"+imagenumber+"/Lrlge_RunL_"+dir+".txt ", true);
							bw3 = new BufferedWriter(fw3);
							bw3.write(Lrlge.toString());
							bw3.newLine();	
							fw4 = new FileWriter("../../RunLength_point/Image"+imagenumber+"/Lrhge_RunL_"+dir+".txt ", true);
							bw4 = new BufferedWriter(fw4);
							bw4.write(Lrhge.toString());
							bw4.newLine();	
							//System.out.println("In");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							bw.close();
							bw1.close();
							bw2.close();
							bw3.close();
							bw4.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Lre.delete(0, Lre.length());
						Gln.delete(0, Gln.length());
						Rln.delete(0, Rln.length());
						Lrlge.delete(0, Lrlge.length());
						Lrhge.delete(0, Lrhge.length());
					}
			//}
			/*else if(dir == 45){
				/*for(int i = yend;i >= _ybegin; i--)
					for(int j = _xbegin; j <= xend; j++) {
						if (isCheck[i][j] == 0 && mask[i][j] == 1) {
							length = Length(i, j, xdir, ydir, mask,1);
							GLRmatrix[(int) GrayValue[i][j]][length]++;
						}
					}
				for (int i = 0; i < h1 ; i++)
					for (int j = 0; j < w1; j++) {
						for (int im = i - (ArraySize / 2); im <= i+(ArraySize / 2);im++)
							for(int in = i - (ArraySize / 2); in <= i + (ArraySize / 2); in++)
						if (isCheck[im][in] == 0 && im >=0 && im < h1 && in >=0 && in < w1) {
							length = Length(im, in, xdir, ydir, mask,0);
							OGLRmatrix[(int) GrayValue[im][in]][length]++;
						}
					}
			}
			else{
				/*for(int i = yend;i >= _ybegin; i--)
					for(int j = xend; j >= _xbegin; j--) {
						if (isCheck[i][j] == 0 && mask[i][j] == 1) {
							length = Length(i, j, xdir, ydir, mask,1);
							GLRmatrix[(int) GrayValue[i][j]][length]++;
						}
					}
				for (int i = 0; i < h1 ; i++)
					for (int j = 0; j < w1; j++) {
						for (int im = i - (ArraySize / 2); im <= i+(ArraySize / 2);im++)
							for(int in = i - (ArraySize / 2); in <= i + (ArraySize / 2); in++)
						if (isCheck[im][in] == 0 && im >=0 && im < h1 && in >=0 && in < w1) {
							length = Length(im, in, xdir, ydir, mask,0);
							OGLRmatrix[(int) GrayValue[im][in]][length]++;
						}
					}
			}
			/*for (int i = 0; i <= max; i++) {
				for (int j = 1; j <= Math.max(yend - _ybegin, xend - _xbegin)+1; j++) {
					GLRPNmatrix[i][j] = GLRmatrix[i][j] * j;
					total += GLRmatrix[i][j];
				}
				GLRNVector[i] = total;
				total = 0;
			}*/
			/*for (int i = 0; i <= max; i++) {
				for (int j = 1; j <= Math.max(h1,w1); j++) {
					OGLRPNmatrix[i][j] = OGLRmatrix[i][j] * j;
					total += OGLRmatrix[i][j];
				}
				OGLRNVector[i] = total;
				total = 0;
			}
			for (int j = 1; j <= Math.max(yend - _ybegin, xend - _xbegin)+1; j++) {
				for (int i = 0; i <= max; i++){
					total += GLRmatrix[i][j];
				}
				RLRNVector[j] = total;
				Nr += total;
				total = 0;
			}
			for (int j = 1; j <= Math.max(h1, w1); j++) {
				for (int i = 0; i <= max; i++)
					total += OGLRmatrix[i][j];
				ORLRNVector[j] = total;
				ONr += total;
				total = 0;
			}*/
			/*for (int i = 0; i < 1; i++) {	//modify i = 0 about out or in
				
				/*if (i == 0)
					System.out.println("===================angle is "+dir+" Outside==================");
				else
					System.out.println("===================angle is "+dir+" Inside==================");
				//Values[i][dir/45][0] = SRE(i);
				//Values[i][dir/45][1] = LRE(i);
				//Values[i][dir/45][2] = GLN(max, i);
				//Values[i][dir/45][3] = RLN(i);
				//Values[i][dir/45][4] = RP(i,ROItotal);
				//Values[i][dir/45][5] = LGRE(max, i);
				//Values[i][dir/45][6] = HGRE(max, i);
				//Values[i][dir/45][7] = SRLGE(i);
				//Values[i][dir/45][8] = SRHGE(i);
				//Values[i][dir/45][9] = LRLGE(i);
				//Values[i][dir/45][10] = LRHGE(i);
			}*/
			/*for(int m = isCheck.length-1; m >= 0; m--)
				Arrays.fill(isCheck[m], 0);
			for(int m=GLRmatrix.length-1;m>=0;m--){
				Arrays.fill(GLRmatrix[m], 0);
				Arrays.fill(GLRPNmatrix[m], 0);
			}
			for(int m=OGLRmatrix.length-1;m>=0;m--){
				Arrays.fill(OGLRmatrix[m], 0);
				Arrays.fill(OGLRPNmatrix[m], 0);
			}
			Arrays.fill(GLRNVector,0);
			Arrays.fill(RLRNVector,0);
			Arrays.fill(OGLRNVector,0);
			Arrays.fill(ORLRNVector,0);
			Nr = 0; ONr=0;*/
			/*System.out.println();
			System.out.println();*/
		}
		/*for(int i =1;i<2;i++){
			try{
				WritableWorkbook workbook = null;
				if(i==1)
					workbook = Workbook.createWorkbook(new File("C:/Users/cebleclipse/Desktop/RunLength/pn9_rf40/Inside/Image"+imagenum+".xls"));
				else
					workbook = Workbook.createWorkbook(new File("C:/Users/cebleclipse/Desktop/RunLength/pn9_rf40/Background/Image"+imagenum+".xls"));
				//將工作表一取名成 First Sheet
		    	WritableSheet sheet = workbook.createSheet("Image"+imagenum, 0);
		    	// first(0) is column second(2) para is row, and (0,2) in excel is (A,3)  
		    	WritableFont arial14font = new WritableFont(WritableFont.ARIAL, 14); 
		    	WritableCellFormat arial14format = new WritableCellFormat (arial14font);
		    	WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12); 
		    	WritableCellFormat arial12format = new WritableCellFormat (arial12font);
		    	Label label = new Label(2,0, "SRE",arial14format);
		    	sheet.addCell(label); 
		    	Label label1 = new Label(3,0, "LRE",arial14format);
		    	sheet.addCell(label1); 
		    	Label label5 = new Label(4,0, "GLN",arial14format);
		    	sheet.addCell(label5); 
		    	Label label2 = new Label(5,0, "RLN",arial14format);
		    	sheet.addCell(label2); 
		    	Label label3 = new Label(6,0, "RP",arial14format);
		    	sheet.addCell(label3); 
		    	Label label4 = new Label(7,0, "LGRE",arial14format);
		    	sheet.addCell(label4); 
		    	Label label6 = new Label(0,0, "angle",arial14format);
		    	sheet.addCell(label6); 
		    	Label label7 = new Label(0,1, "0",arial14format);
		    	sheet.addCell(label7); 
		    	Label label8 = new Label(0,2, "45",arial14format);
		    	sheet.addCell(label8); 
		    	Label label9 = new Label(0,3, "90",arial14format);
		    	sheet.addCell(label9); 
		    	Label label10 = new Label(0,4, "135",arial14format);
		    	sheet.addCell(label10); 
		    	Label label11 = new Label(8,0,"HGRE",arial14format);
		    	sheet.addCell(label11);
		    	Label label12 = new Label(9,0,"SRLGE",arial14format);
		    	sheet.addCell(label12);
		    	Label label13 = new Label(10,0,"SRHGE",arial14format);
		    	sheet.addCell(label13);
		    	Label label14 = new Label(11,0,"LRLGE",arial14format);
		    	sheet.addCell(label14);
		    	Label label15 = new Label(12,0,"LRHGE",arial14format);
		    	sheet.addCell(label15);
		    	
		    	Number number = new Number(100, 100, 0,arial12format); 
		    	for(int m = 0; m<4; m++)
		    	for(int j =0;j<11;j++){
		    		number.setValue(Values[i][m][j]);
		    		sheet.addCell(number.copyTo(j+2,m+1));
		    	}
		    	workbook.write(); 
		    	workbook.close();
		    }
		    catch(Exception ex){
		    	ex.printStackTrace();
		    }
		}*/
		System.out.println("Using Time:" + (System.currentTimeMillis() - StartTime) + " ms");
	}
	private static double LRHGE(int inside,int[][] OGLRmatrix,double ONr) {
		// TODO Auto-generated method stub
		double lrhge = 0;
		if(inside == 1){
		/*for(int i = 1;i < GLRmatrix.length + 1;i++)
			for(int j =1; j < GLRmatrix[0].length;j++)
				lrhge += Math.pow(i,2) * GLRmatrix[i - 1][j] * Math.pow(j,2);
		lrhge = lrhge / Nr;*/
		}
		else{
			for(int i = 1;i < OGLRmatrix.length + 1;i++)
				for(int j =1; j < OGLRmatrix[0].length;j++)
					lrhge += Math.pow(i,2) * OGLRmatrix[i - 1][j] * Math.pow(j,2);
			lrhge = lrhge / ONr;
		}
			
			//System.out.println("LRHGE is "+lrhge);
			return lrhge;
	}
	private static double LRLGE(int inside,int[][] OGLRPNmatrix, double ONr) {
		// TODO Auto-generated method stub
		double lrlge = 0;
		if(inside==1){
			/*for (int i = 1; i < GLRmatrix.length + 1; i++)
				for (int j = 1; j < GLRmatrix[0].length; j++)
					lrlge += (Math.pow(j, 2) * GLRmatrix[i - 1][j]) / Math.pow(i, 2);
			lrlge = lrlge / Nr;*/
		}
		else{
			for (int i = 1; i < OGLRPNmatrix.length + 1; i++)
				for (int j = 1; j < OGLRPNmatrix[0].length; j++)
					lrlge += (Math.pow(j, 2) * OGLRPNmatrix[i - 1][j]) / Math.pow(i, 2);
			lrlge = lrlge / ONr;
		}
			//System.out.println("LRLGE is "+lrlge);
			return lrlge;
	}
	/*private static double SRHGE(int inside,double[][] OGLRmatrix,double ONr) {
		// TODO Auto-generated method stub
		double srhge=0;
		if (inside == 1) {
			for (int i = 1; i < GLRmatrix.length + 1; i++)
				for (int j = 1; j < GLRmatrix[0].length; j++)
					srhge += (Math.pow(i, 2) * GLRmatrix[i - 1][j]) / Math.pow(j, 2);
			srhge = srhge / Nr;
		} else {
			for (int i = 1; i < OGLRmatrix.length + 1; i++)
				for (int j = 1; j < OGLRmatrix[0].length; j++)
					srhge += (Math.pow(i, 2) * OGLRmatrix[i - 1][j]) / Math.pow(j, 2);
			srhge = srhge / ONr;
		}
		//System.out.println("SRHGE is "+srhge);
		return srhge;
	}*/
	/*private static double SRLGE(int inside,double[][] OGLRPNmatrix, double ONr) {
		// TODO Auto-generated method stub
		double srlge = 0;
		if (inside == 1) {
			for (int i = 1; i < GLRmatrix.length + 1; i++)
				for (int j = 1; j < GLRmatrix[0].length; j++)
					srlge += GLRPNmatrix[i - 1][j] / (Math.pow(i, 2) * Math.pow(j, 2));
			srlge = srlge / Nr;
		} else {
			for (int i = 1; i < OGLRPNmatrix.length + 1; i++)
				for (int j = 1; j < OGLRPNmatrix[0].length; j++)
					srlge += OGLRPNmatrix[i - 1][j] / (Math.pow(i, 2) * Math.pow(j, 2));
			srlge = srlge / ONr;
		}
		//System.out.println("SRLGE is "+srlge);
		return srlge;
	}*/
	/*private static double HGRE(int level,int inside, double[] OGLRNVector,double ONr) {
		// TODO Auto-generated method stub
		double hgre = 0;
		if (inside == 1) {
			for (int i = 1; i <= level + 1; i++) {
				hgre += GLRNVector[i - 1] * Math.pow(i, 2);
			}
			hgre = hgre / Nr;
		} else {
			for (int i = 1; i <= level + 1; i++) {
				hgre += OGLRNVector[i - 1] * Math.pow(i, 2);
			}
			hgre = hgre / ONr;
		}
		return hgre;
	}*/
/*	private static double LGRE(int max,int inside) {
		// TODO Auto-generated method stub
		double lgre = 0;
		if (inside == 1) {
			for(int i = 1; i <= max+1; i++){
				lgre += GLRNVector[i - 1] / Math.pow(i, 2);
			}
			lgre = lgre / Nr;
		} else {
			for (int i = 1; i <= max + 1; i++) {
				lgre += OGLRNVector[i - 1] / Math.pow(i, 2);
			}
			lgre = lgre / ONr;
		}
		//System.out.println("LGRE is "+lgre);
		return lgre;
	}*/
	/*private static double RP(int inside, int roitotal) {
		// TODO Auto-generated method stub
		double nr = 0;
		if(inside==1)
			nr = Nr / roitotal;
		else{
			roitotal = h1*w1-roitotal;
			nr = ONr / roitotal;
		}
		//System.out.println("RP is "+ nr);
		return nr;
	}*/
	private static double RLN(int inside,int[] ORLRNVector,double ONr) {
		// TODO Auto-generated method stub
		double rln = 0;
		if (inside == 1) {
			/*for (int i = 1; i < RLRNVector.length; i++) {
				rln += Math.pow(RLRNVector[i], 2);
			}
			rln = rln / Nr;*/
		} else {
			for (int i = 1; i < ORLRNVector.length; i++) {
				rln += Math.pow(ORLRNVector[i], 2);
			}
			rln = rln / ONr;
		}
		//System.out.println("RLN of is "+rln);
		return rln;
	}
	private static double GLN(int max,int inside, int[] OGLRNVector,double ONr) {
		// TODO Auto-generated method stub
		double gln = 0;
		if (inside == 1) {
			/*for (int i = 0; i <= max; i++)
				gln += Math.pow(GLRNVector[i], 2);
			gln = gln / Nr;*/
		} else {
			for (int i = 0; i <= max; i++)
				gln += Math.pow(OGLRNVector[i], 2);
			gln = gln / ONr;
		}
		//System.out.println("GLN is "+gln);
		return gln;
	}
	private static double LRE(int inside, int[] ORLRNVector,double ONr) {
		// TODO Auto-generated method stub
		double lre=0;
		if (inside == 1) {
			/*for (int i = 1; i < RLRNVector.length; i++) {
				lre += RLRNVector[i] * Math.pow(i, 2);
			}
			lre = lre / Nr;*/
		} else {
			for (int i = 1; i < ORLRNVector.length; i++) {
				lre += ORLRNVector[i] * Math.pow(i, 2);
			}
			lre = lre / ONr;
		}
		//System.out.println("LRE is "+lre);
		return lre;
	}
	/*private static double SRE(int inside, double[] ORLRNVector, double ONr) {
		// TODO Auto-generated method stub
		double sre=0,temp = 0;
		if (inside == 1) {
			for (int i = 1; i < RLRNVector.length; i++) {
				sre += RLRNVector[i] / Math.pow(i, 2);
			}
			sre = sre / Nr;
		} else {
			for (int i = 1; i < ORLRNVector.length; i++) {
				sre += ORLRNVector[i] / Math.pow(i, 2);
			}
			sre = sre / ONr;
		}
		//System.out.println("SRE is "+sre);
		return sre;
	}*/
	private static int Length(int i, int j, int xdir, int ydir,double[][] mask,int inside,int _xbegin,int _ybegin,int yend, int xend,double[][] GrayValue) {
		// TODO Auto-generated method stub
		//recursive to count the length , 
		int leng = 1;
		/*isCheckT[i][j]=1;
		if (i + ydir >= 0 && j + xdir >= 0 && i + ydir < test.length && j + xdir < test[0].length && isCheckT[i+ydir][j+xdir]==0)
			if(test[i][j]==test[i+ydir][j+xdir])
				leng+=Length(i + ydir, j + xdir, xdir, ydir, mask, inside);*/
		isCheck[yend - i][xend - j] = 1;
		if(inside == 1){
			/*if (i + ydir >= _ybegin && j + xdir >= _xbegin && i + ydir <= yend && j + xdir <= xend )
				if (GrayValue[i][j] == GrayValue[i + ydir][j + xdir] && mask[i + ydir][j + xdir] == 1) {
					leng += Length(i + ydir, j + xdir, xdir, ydir, mask, inside);
				}*/
		}
		else{
			if (i + ydir >= 0 && j + xdir >= 0 && i + ydir < GrayValue.length && j + xdir < GrayValue[0].length && i + ydir>= _ybegin && j + xdir >= _xbegin && i + ydir <= yend && j + xdir <= xend)
				if (GrayValue[i][j] == GrayValue[i + ydir][j + xdir] && isCheck[yend - (i + ydir)][xend - (j + xdir)] == 0) {  //delete掉 mask[i + ydir][j + xdir] == 0
					leng += Length(i + ydir, j + xdir, xdir, ydir, mask, inside,_xbegin,_ybegin,yend,xend,GrayValue);
				}
		}
		return leng;
	}
}
