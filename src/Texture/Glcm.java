package Texture;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportGrid;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;
import org.medtoolbox.jviewbox.viewport.annotation.DynamicAnnotationShape;

import LevelSet.SkullStripper;
import tools.GeometryContour;
//import tools.Matrix;

public class Glcm extends ViewportTool{
	private static double[][] GrayValue;
	private static BufferedImage origimage;
	private static int xwindowsize = 0;
	private static int ywindowsize = 0;
	private static int ybegin,xbegin,xend,yend;
	private static int max;
	/**
	 * Computation function.
	 */
	//private Matrix _matrix;
	
	/**
	 * image length and height
	 */
	private static int h1,w1;
	/**
	 * Vector matrix
	 */
	private Vector _Matrix;

	/**
	 * ViewportCluster for displaying the viewports.
	 */
	private ViewportCluster _vpc;

	private Viewport _origViewport;

	/**
	 * Annotation dynamic shape.
	 */
	private DynamicAnnotationShape _dyShape;

	/**
	 * Cursor point.
	 */
	private Point _cursorPoint = new Point();

	/**
	 * Geometry contour for the inital placement.
	 */
	private GeometryContour _geometryContour;

	/**
	 * The start postion of selected image.
	 */
	private int _xStart, _yStart;
	
	/**
	 * @mask = phi
	 */
	double[][] mask;


//	public void mousePressed(ViewportCluster vpc, Viewport vp, MouseEvent e,
//			int button) {
//		
//		//to determine which slice it is.
//		int slicenum=0;
//		int angle = 0;
//		double[][][] cpGray = new double[4][][];
//		for(slicenum=0;slicenum<vpc.getViewports().size();slicenum++){
//			if(vp==(Viewport)vpc.getViewports().get(slicenum)){
//					break;
//				}
//		}   
//		Matrix temp = (Matrix) _Matrix.get(slicenum);
//		SkullStripper skultemp = (SkullStripper) _skullstripper.get(slicenum);
//		origimage = skultemp.getInputImage();
//		xbegin = skultemp.getxbegin();
//		xend = skultemp.getxend();
//		ybegin = skultemp.getybegin();
//		yend = skultemp.getyend();
//		mask = skultemp.getmask();
//		// BufferedImage buffimg = ImageIO.read(new File("D:/CG/R4.jpg"));
//		Scanner scan = new Scanner(System.in);
//		System.out.println("請輸入轉換level");
//		int level = scan.nextInt();
//		BufferedImage greyImage = getGrayScaleAvg(origimage, level);
//		for(int i =0;i<4;i++){
//			System.out.println("===========================================================================");
//			cpGray[i] = ComputeGlcm(level, greyImage,angle);
//			angle +=45;
//		}
//		// compute GLCM method
//	}
//	
	public Glcm(double[][] phi, BufferedImage orig, int _xbegin,int _ybegin,int xen, int yen) throws IOException {		
		super("GLCM", "Gray-level construct methods of Image", "", "");
		//to determine which slice it is.
		int angle = 0;
		double[][][] cpGray = new double[4][][];
		origimage = orig;
		xbegin = _xbegin;
		xend = xen;
		ybegin = _ybegin;
		yend = yen;
		mask = phi;
		BufferedImage greyImage = getGrayScaleAvg(origimage, 256);
		for(int i =0;i<4;i++){
			System.out.println("=========================================================================");
			cpGray[i] = ComputeGlcm(256, greyImage,angle,mask);
			angle +=45;
			System.out.println();
		}
		// compute GLCM method
	}
	
	public static BufferedImage getGrayScaleAvg(BufferedImage img, int level) {
		w1 = img.getWidth(); // 1200
		h1 = img.getHeight(); // 1242;
		GrayValue = new double[h1][w1];
		BufferedImage gray = new BufferedImage(w1, h1, 1);// new image
		int value,localmax=0;
		Raster origRaster = img.getData();
		for (int i = 0; i < w1; i++) {
			for (int j = 0; j < h1; j++) {
				value = (int) origRaster.getSampleDouble(i, j, 0); // read pixel gray value
				GrayValue[j][i] = value;
				if (value > max) {
					max = value;
				}
				gray.setRGB(i, j, value);
			}
		}
		for(int g=0;g<20;g++){
			localmax = (int) Math.pow(2,g);
			if(localmax >= max)
				break;
		}
		if (level != 0) {
			for (int i = 0; i < w1; i++) {
				for (int j = 0; j < h1; j++) {
					GrayValue[j][i] = Math.round((GrayValue[j][i] / localmax)
							* (level - 1) * 10) / 10;
					// gray.setRGB(i, j, (int) GrayValue[j][i]);
					// System.out.println("Value is " + GrayValue[j][i]);
				}
			}
		}
		return gray;
	}

	/*public void mouseMoved(ViewportCluster vpc, Viewport vp, MouseEvent e) {
		if (!_matrix.is_start) {
			Iterator it = vpc.getViewports().iterator();
			_origViewport = (Viewport) it.next();
			xwindowsize = _matrix.getInitialBoxWidth();
			ywindowsize = _matrix.getInitialBoxHeight();

			try {
				if (vp == _origViewport) {
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
					 *
					_dyShape.setForegroundColor(Color.yellow);
					_dyShape.setBackgroundColor(null);
					vp.addAnnotation(_dyShape);
					// Repaint the Viewport
					vpc.repaint(vp);
				}
			} catch (Exception ex) {
				System.out.println("Error:  " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}*/

	// GLCM算法!
	//new
	public static double[][] ComputeGlcm(int Co_size, BufferedImage Greyimg, int angle, double[][] mask) {
		double[][] comatrix = new double[Co_size][Co_size];
		double[][] cpGray = GrayValue;
		int xoffset=0,yoffset=0;
		int total = 0;
		switch(angle){
		case 0:
			xoffset = 1;
			yoffset = 0;
        	break;
        case 45:
        	xoffset = 1;
        	yoffset = 1;
        	break;
        case 90:
        	xoffset = 0;
        	yoffset = 1;
        	break;
        case 135:
        	xoffset = -1;
        	yoffset = 1;
        	break;
		}
		/**
		 * 此處_xBegin _yBegin xend yend 皆由skullstripper中取得，目前skullstripper並無這些函數 可以的話試看看自己修改
		 */
		for(int inside =0;inside<2;inside++){
			if(inside == 0){
				for (int u = 0; u < h1; u++) {
					for (int v = 0; v < w1; v++) {
						int u2 = u + yoffset;
						int v2 = v + xoffset;
						if (mask[u][v] == inside && u2 < h1 && v2 < w1 && u2 > 0 && v2 > 0) {
							comatrix[(int) GrayValue[u][v]][(int) GrayValue[u2][v2]]++;
							total++;
						}
					}
				}
			System.out.println("=============================== "+angle +" Outside===============================");
			}
			else{
				for (int u = ybegin; u < yend; u++) {
					for (int v = xbegin; v < xend; v++) {
						int u2 = u + yoffset;
						int v2 = v + xoffset;
						if (u2 < yend && u2 > ybegin && v2 < xend
								&& v2 > xbegin) {
							if (mask[u][v] == inside) {
								comatrix[(int) GrayValue[u][v]][(int) GrayValue[u2][v2]]++;
								total++;
							}
						}
					}
				}				
				System.out.println("=============================== "+angle +" Inside===============================");
			}
			System.out.println(angle+" degree："+"Asm value is " + ASM(comatrix, total));
			System.out.println(angle+" degree："+"CON value is " + CON(comatrix, total));
			System.out.println(angle+" degree："+"ENT value is " + ENT(comatrix, total));
			System.out.println(angle+" degree："+"HOM value is " + HOM(comatrix, total));
			System.out.println(angle+" degree："+"DIS value is " + DIS(comatrix, total));
			System.out.println(angle+" degree："+"COR value is " + COR(comatrix, total));
			total = 0;
			for (int m = 0; m < comatrix.length; m++) {
				Arrays.fill(comatrix[m], 0);
			}
		}
		return cpGray;
	}
	//old
//	public static double[][] ComputeGlcm(int Co_size, BufferedImage Greyimg, int angle) {
//		double[][] comatrix = new double[Co_size][Co_size];
//		double[][] cpGray = GrayValue;
//		int xoffset=0,yoffset=0;
//		int total = 0;
//		switch(angle){
//		case 0:
//			xoffset = 1;
//			yoffset = 0;
//        	break;
//        case 45:
//        	xoffset = 1;
//        	yoffset = 1;
//        	break;
//        case 90:
//        	xoffset = 0;
//        	yoffset = 1;
//        	break;
//        case 135:
//        	xoffset = -1;
//        	yoffset = 1;
//        	break;
//		}
//		for (int u = ybegin; u <  yend; u++) {
//			for (int v = xbegin; v < xend; v++) {
//				int u2 = u + yoffset;
//				int v2 = v + xoffset;
//				if(u2 < yend && u2 > ybegin && v2 < xend && v2 > xbegin){
//					comatrix[(int) GrayValue[u][v]][(int) GrayValue[u2][v2]]++;
//					total++;
//				}
//			}
//		}
//		for(int i =0;i<2;i++){
//			System.out.println(angle+" degree："+"Asm value is " + ASM(comatrix, total,i));
//			System.out.println(angle+" degree："+"CON value is " + CON(comatrix, total,i));
//			System.out.println(angle+" degree："+"ENT value is " + ENT(comatrix, total,i));
//			System.out.println(angle+" degree："+"HOM value is " + HOM(comatrix, total,i));
//			System.out.println(angle+" degree："+"DIS value is " + DIS(comatrix, total,i));
//			System.out.println(angle+" degree："+"COR value is " + COR(comatrix, total,i));
//		}
//		// Greyimg.setRGB(j+windowsize/2, i+windowsize/2, Tvalue);
//		for (int m = 0; m < comatrix.length; m++) {
//			Arrays.fill(comatrix[m], 0);
//		}
//		total = 0;
//		return cpGray;
//	}

	private static double ASM(double[][] comatrix, int total) {
		// TODO Auto-generated method stub
		double asm = 0,temp = 0;
		for (int i = 0; i < comatrix.length; i++) {
			for (int j = 0; j < comatrix[0].length; j++) {
				temp = comatrix[i][j] / total;
				asm += Math.pow(temp, 2);
			}
		}
		return asm;
	}

	private static double CON(double[][] comatrix, int total) {
		double con = 0,temp = 0;
		for (int i = 0; i < comatrix.length; i++) {
			for (int j = 0; j < comatrix[0].length; j++) {
				temp = comatrix[i][j] / total;
				con += temp * Math.pow((i - j), 2);
			}
		}
		return con;
	}

	private static double ENT(double[][] comatrix, int total) {
		double ent = 0,temp = 0;
		for (int i = 0; i < comatrix.length; i++) {
			for (int j = 0; j < comatrix[0].length; j++) {
				temp = comatrix[i][j] / total;
				if (temp != 0)
					ent += temp * Math.log(temp);
			}
		}
		return (-ent);
	}

	private static double HOM(double[][] comatrix, int total) {
		double hom = 0,temp = 0;
		for (int i = 0; i < comatrix.length; i++) {
			for (int j = 0; j < comatrix[0].length; j++) {
				temp = comatrix[i][j] / total;
				hom += temp / (1 + Math.pow((i - j), 2));
			}
		}
		return hom;
	}

	private static double DIS(double[][] comatrix, int total) {
		double dis = 0,temp = 0;
		for (int i = 0; i < comatrix.length; i++) {
			for (int j = 0; j < comatrix[0].length; j++) {
				temp = comatrix[i][j] / total;
				dis += temp * Math.abs(i - j);
			}
		}
		return dis;
	}
	
	private static double COR(double[][] comatrix, int total) {
		double cor = 0,temp = 0;
		double mu_x=0,mu_y=0,sd_x=0,sd_y=0;
		for (int i = 0; i < comatrix.length; i++) {
			for (int j = 0; j < comatrix[0].length; j++) {
				temp = comatrix[i][j] / total;
				mu_x += i*temp;
			}
		}
		for (int j = 0; j < comatrix[0].length; j++)
			for (int i = 0; i < comatrix.length; i++){
				temp = comatrix[i][j] / total;
				mu_y += j*temp;
			}
		for (int i = 0; i < comatrix.length; i++) {
			for (int j = 0; j < comatrix[0].length; j++) {
				temp = comatrix[i][j] / total;
				sd_x += temp*Math.pow(i-mu_x, 2);
			}
		}
		for (int j = 0; j < comatrix[0].length; j++)
			for (int i = 0; i < comatrix.length; i++){
				temp = comatrix[i][j] / total;
				sd_y += Math.pow(j-mu_y, 2)*temp;
			}
		sd_x = Math.sqrt(sd_x);
		sd_y = Math.sqrt(sd_y);
		for (int i = 0; i < comatrix.length; i++) {
			for (int j = 0; j < comatrix[0].length; j++) {
				temp = comatrix[i][j] / total;
				cor += i*j*temp-(mu_x*mu_y);
			}
		}
		return cor/(sd_x*sd_y);
	}
}
