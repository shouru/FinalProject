package Texture;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;
import org.medtoolbox.jviewbox.viewport.annotation.DynamicAnnotationShape;

import tools.GeometryContour;
//import tools.Matrix;
import LevelSet.SkullStripper;

public class RunLengthMat extends ViewportTool{
	
	private static double[][] GrayValue;
	private static int[][] GLRmatrix,GLRPNmatrix,OGLRmatrix,OGLRPNmatrix;
	private static int[] GLRNVector,RLRNVector,OGLRNVector,ORLRNVector;
	private static BufferedImage origimage;
	private static double Nr=0,ONr=0;
	private static int w1,h1;
	private static int[][] isCheck;
	private static int _xbegin, _ybegin, xend,yend;
	private static int ROItotal = 0;
	
	//test用 記得刪掉
	/*private static int[][] test = {{0,1,2,3},{1,1,2,3},{2,2,2,3},{3,3,3,3}};
	private static int[][] isCheckT = new int[4][4];
	private static int[][] testPNmatrix;
	private static int[] testNVector,testRNVector;*/
	/**
	 * Cursor point.
	 */
	private Point _cursorPoint = new Point();
	
	/**
	 * Vector matrix
	 */
	private Vector _Matrix;
	
	/**
	 * Annotation dynamic shape.
	 */
	private DynamicAnnotationShape _dyShape;
	
	/**
	 * Computation function.
	 */
	//private Matrix _matrix;
	
	/**
	 * Geometry contour for the inital placement.
	 */
	private GeometryContour _geometryContour;
	/**
	 * @mask = phi
	 */
	private double[][] mask;
	
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

	public RunLengthMat(double[][] phi, BufferedImage orig, int xbegin,int ybegin,int xen, int yen,int roitotal){
		super("RunLengh", "RunLengthMat of Image", "", "");
		mask = phi;
		ROItotal = roitotal;
		int slicenum;  
		origimage = orig;
		_xbegin = xbegin;
		xend = xen;
		_ybegin = ybegin;
		yend = yen;
		w1 = origimage.getWidth();  //1200
	    h1 = origimage.getHeight(); //1242;
		isCheck = new int[h1][w1];
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
		// 浪費了 [max+1][0]的空間
		/*int[][] testmatrix = new int[4][5];
		testPNmatrix = new int[4][5];
		testNVector = new int[4];
		testRNVector = new int[5];*/
		GLRmatrix = new int[max + 1][Math.max(yend-_ybegin,xend-_xbegin) + 2];
		GLRPNmatrix = new int[max + 1][Math.max(yend-_ybegin,xend-_xbegin) + 2];
		GLRNVector = new int[max + 1];
		RLRNVector = new int[Math.max(yend-_ybegin,xend-_xbegin) + 2];
		OGLRmatrix = new int[max + 1][Math.max(h1,w1) + 1];
		OGLRPNmatrix = new int[max + 1][Math.max(h1,w1) + 1];
		OGLRNVector = new int[max + 1];
		ORLRNVector = new int[Math.max(h1,w1) + 1];
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
			int length;
			
			/**
			 * 此處_xBegin _yBegin xend yend mask
			 * 皆由skullstripper中取得，目前skullstripper並無這些函數 可以的話試看看自己修改
			 */
			if(dir == 0){
				for (int i = _ybegin; i <= yend; i++)
					for (int j = _xbegin; j <= xend; j++) {
						if (isCheck[i][j] == 0 && mask[i][j] == 1) {
							length = Length(i, j, xdir, ydir, mask,1);
							GLRmatrix[(int) GrayValue[i][j]][length]++;
						}
					}
				for (int i = 0; i < h1; i++)
					for (int j = 0; j < w1; j++) {
						if (isCheck[i][j] == 0 && mask[i][j] == 0) {
							length = Length(i, j, xdir, ydir, mask,0);
							OGLRmatrix[(int) GrayValue[i][j]][length]++;
						}
					}
				}
			else if(dir == 45){
				for(int i = yend;i >= _ybegin; i--)
					for(int j = _xbegin; j <= xend; j++) {
						if (isCheck[i][j] == 0 && mask[i][j] == 1) {
							length = Length(i, j, xdir, ydir, mask,1);
							GLRmatrix[(int) GrayValue[i][j]][length]++;
						}
					}
				// this is used to confirm my guess , need to be delete
				for(int i=0;i<GLRmatrix.length;i++){
				for(int e : GLRmatrix[i]){
					System.out.print(e+" ");
				}
				System.out.println();
				}
				///////////
				for (int i = h1-1; i >=0 ; i--)
					for (int j = 0; j < w1; j++) {
						if (isCheck[i][j] == 0 && mask[i][j] == 0) {
							length = Length(i, j, xdir, ydir, mask,0);
							OGLRmatrix[(int) GrayValue[i][j]][length]++;
						}
					}
			}
			else{
				for(int i = yend;i >= _ybegin; i--)
					for(int j = xend; j >= _xbegin; j--) {
						if (isCheck[i][j] == 0 && mask[i][j] == 1) {
							length = Length(i, j, xdir, ydir, mask,1);
							GLRmatrix[(int) GrayValue[i][j]][length]++;
						}
					}
				for (int i = h1-1; i >=0 ; i--)
					for (int j = w1-1; j >= 0; j--) {
						if (isCheck[i][j] == 0 && mask[i][j] == 0) {
							length = Length(i, j, xdir, ydir, mask,0);
							OGLRmatrix[(int) GrayValue[i][j]][length]++;
						}
					}
			}
			for (int i = 0; i <= max; i++) {
				for (int j = 1; j <= Math.max(yend - _ybegin, xend - _xbegin)+1; j++) {
					GLRPNmatrix[i][j] = GLRmatrix[i][j] * j;
					total += GLRmatrix[i][j];
				}
				GLRNVector[i] = total;
				total = 0;
			}
			for (int i = 0; i <= max; i++) {
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
			}
			for (int i = 1; i < 2; i++) {	//modify i = 0
				if (i == 0)
					System.out.println("===================angle is "+dir+" Outside==================");
				else
					System.out.println("===================angle is "+dir+" Inside==================");
				SRE(i);
				LRE(i);
				GLN(max, i);
				RLN(i);
				RP(i,ROItotal);
				LGRE(max, i);
				HGRE(max, i);
				SRLGE(i);
				SRHGE(i);
				LRLGE(i);
				LRHGE(i);
			}
			for(int m = isCheck.length-1; m >= 0; m--)
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
			Nr = 0; ONr=0;
			System.out.println();
			System.out.println();
		}
	}
	private static void LRHGE(int inside) {
		// TODO Auto-generated method stub
		double lrhge = 0;
		if(inside == 1){
		for(int i = 1;i < GLRmatrix.length + 1;i++)
			for(int j =1; j < GLRmatrix[0].length;j++)
				lrhge += Math.pow(i,2) * GLRmatrix[i - 1][j] * Math.pow(j,2);
		lrhge = lrhge / Nr;
		}
		else{
			for(int i = 1;i < OGLRmatrix.length + 1;i++)
				for(int j =1; j < OGLRmatrix[0].length;j++)
					lrhge += Math.pow(i,2) * OGLRmatrix[i - 1][j] * Math.pow(j,2);
			lrhge = lrhge / ONr;
		}
			
			System.out.println("LRHGE is "+lrhge);
	}
	private static void LRLGE(int inside) {
		// TODO Auto-generated method stub
		double lrlge = 0;
		if(inside==1){
			for (int i = 1; i < GLRmatrix.length + 1; i++)
				for (int j = 1; j < GLRmatrix[0].length; j++)
					lrlge += (Math.pow(j, 2) * GLRmatrix[i - 1][j]) / Math.pow(i, 2);
			lrlge = lrlge / Nr;
		}
		else{
			for (int i = 1; i < OGLRmatrix.length + 1; i++)
				for (int j = 1; j < OGLRmatrix[0].length; j++)
					lrlge += (Math.pow(j, 2) * OGLRPNmatrix[i - 1][j]) / Math.pow(i, 2);
			lrlge = lrlge / ONr;
		}
			System.out.println("LRLGE is "+lrlge);
	}
	private static void SRHGE(int inside) {
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
		System.out.println("SRHGE is "+srhge);
	}
	private static void SRLGE(int inside) {
		// TODO Auto-generated method stub
		double srlge = 0;
		if (inside == 1) {
			for (int i = 1; i < GLRmatrix.length + 1; i++)
				for (int j = 1; j < GLRmatrix[0].length; j++)
					srlge += GLRPNmatrix[i - 1][j] / (Math.pow(i, 2) * Math.pow(j, 2));
			srlge = srlge / Nr;
		} else {
			for (int i = 1; i < OGLRmatrix.length + 1; i++)
				for (int j = 1; j < OGLRmatrix[0].length; j++)
					srlge += OGLRPNmatrix[i - 1][j] / (Math.pow(i, 2) * Math.pow(j, 2));
			srlge = srlge / ONr;
		}
		System.out.println("SRLGE is "+srlge);
	}
	private static void HGRE(int max,int inside) {
		// TODO Auto-generated method stub
		double hgre = 0;
		if (inside == 1) {
			for (int i = 1; i <= max + 1; i++) {
				hgre += GLRNVector[i - 1] * Math.pow(i, 2);
			}
			hgre = hgre / Nr;
		} else {
			for (int i = 1; i <= max + 1; i++) {
				hgre += OGLRNVector[i - 1] * Math.pow(i, 2);
			}
			hgre = hgre / ONr;
		}
		System.out.println("HGRE is "+hgre);
	}
	private static void LGRE(int max,int inside) {
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
		System.out.println("LGRE is "+lgre);
	}
	private static void RP(int inside, int roitotal) {
		// TODO Auto-generated method stub
		double nr = 0;
		if(inside==1)
			nr = Nr / roitotal;
		else{
			roitotal = h1*w1-roitotal;
			nr = ONr / roitotal;
		}
		System.out.println("RP is "+ nr);
	}
	private static void RLN(int inside) {
		// TODO Auto-generated method stub
		double rln = 0;
		if (inside == 1) {
			for (int i = 1; i < RLRNVector.length; i++) {
				rln += Math.pow(RLRNVector[i], 2);
			}
			rln = rln / Nr;
		} else {
			for (int i = 1; i < ORLRNVector.length; i++) {
				rln += Math.pow(ORLRNVector[i], 2);
			}
			rln = rln / ONr;
		}
		System.out.println("RLN of is "+rln);
	}
	private static void GLN(int max,int inside) {
		// TODO Auto-generated method stub
		double gln = 0;
		if (inside == 1) {
			for (int i = 0; i <= max; i++)
				gln += Math.pow(GLRNVector[i], 2);
			gln = gln / Nr;
		} else {
			for (int i = 0; i <= max; i++)
				gln += Math.pow(OGLRNVector[i], 2);
			gln = gln / ONr;
		}
		System.out.println("GLN is "+gln);
	}
	private static void LRE(int inside) {
		// TODO Auto-generated method stub
		double lre=0;
		if (inside == 1) {
			for (int i = 1; i < RLRNVector.length; i++) {
				lre += RLRNVector[i] * Math.pow(i, 2);
			}
			lre = lre / Nr;
		} else {
			for (int i = 1; i < ORLRNVector.length; i++) {
				lre += ORLRNVector[i] * Math.pow(i, 2);
			}
			lre = lre / ONr;
		}
		System.out.println("LRE is "+lre);
	}
	private static void SRE(int inside) {
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
		System.out.println("SRE is "+sre);
	}
	private static int Length(int i, int j, int xdir, int ydir,double[][] mask,int inside) {
		// TODO Auto-generated method stub
		//recursive to count the length , not sure whether this costs much time or not 
		int leng = 1;
		/*isCheckT[i][j]=1;
		if (i + ydir >= 0 && j + xdir >= 0 && i + ydir < test.length && j + xdir < test[0].length && isCheckT[i+ydir][j+xdir]==0)
			if(test[i][j]==test[i+ydir][j+xdir])
				leng+=Length(i + ydir, j + xdir, xdir, ydir, mask, inside);*/
		isCheck[i][j] = 1;
		if(inside == 1){
			if (i + ydir >= _ybegin && j + xdir >= _xbegin && i + ydir <= yend && j + xdir <= xend)
				if (GrayValue[i][j] == GrayValue[i + ydir][j + xdir] && mask[i + ydir][j + xdir] == 1) {
					leng += Length(i + ydir, j + xdir, xdir, ydir, mask, inside);
				}
		}
		else{
			if (i + ydir >= 0 && j + xdir >= 0 && i + ydir < h1 && j + xdir < w1)
				if (GrayValue[i][j] == GrayValue[i + ydir][j + xdir] && mask[i + ydir][j + xdir] == 0) {
					leng += Length(i + ydir, j + xdir, xdir, ydir, mask, inside);
				}
		}
		return leng;
	}
}
