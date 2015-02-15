package Texture;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportGrid;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;
import org.medtoolbox.jviewbox.viewport.annotation.DynamicAnnotationShape;

import LevelSet.SkullStripper;
import tools.GeometryContour;
//import tools.Matrix;

public class Glcm extends ViewportTool{
	/*private static double[][] GrayValue;
	private static BufferedImage origimage;
	private static int xwindowsize = 0;
	private static int ywindowsize = 0;
	private static int ybegin,xbegin,xend,yend;
	private static int max;
	private static int imagenum;
	static double[][][] Values = new double[2][4][6];
	
	/**
	 * image length and height
	 */
	/*private static int h1,w1;*/
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
	
	private static int ArraySize;


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
	public Glcm(double[][] phi, BufferedImage orig, int _xbegin,int _ybegin,int xen, int yen, int imagenumber,int arraysize) throws IOException {		
		super("GLCM", "Gray-level construct methods of Image", "", "");
		BufferedImage origimage;
		int xwindowsize = 0;
		int ywindowsize = 0;
		int ybegin,xbegin,xend,yend;
		int imagenum;
		long StartTime = System.currentTimeMillis();
		//to determine which slice it is.
		int angle = 0;
		int w1 = orig.getWidth();
		int h1 = orig.getHeight();
		double[][][] cpGray = new double[4][][];
		double[][] GrayValue;
		origimage = orig;
		xbegin = _xbegin;
		xend = xen;
		ybegin = _ybegin;
		yend = yen;
		mask = phi;
		//imagenum = imagenumber;
		ArraySize = arraysize;
		GrayValue = getGrayScaleAvg(origimage, 128,w1,h1);
		for(int i =0;i<4;i++){
			System.out.println("=========================================================================");
			angle = i*45;
			cpGray[i] = ComputeGlcm(128,GrayValue,angle,mask,h1,w1,imagenumber);
			System.out.println();
		}
		// compute GLCM method
		System.out.println("Using Time:" + (System.currentTimeMillis() - StartTime) + " ms");
	}
	
	public static double[][] getGrayScaleAvg(BufferedImage img, int level,int w1,int h1) {
		double[][] GrayValue = new double[h1][w1];
		int value,localmax=0,max=0;
		Raster origRaster = img.getData();
		for (int i = 0; i < w1; i++) {
			for (int j = 0; j < h1; j++) {
				value = (int) origRaster.getSampleDouble(i, j, 0); // read pixel gray value
				GrayValue[j][i] = value;
				if (value > max) {
					max = value;
				}
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
				}
			}
		}
		return GrayValue;
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
	public static double[][] ComputeGlcm(int Co_size, double[][] GrayValue, int angle, double[][] mask, int h1, int w1,int imagenum) {
		double[][] comatrix = new double[Co_size][Co_size];
		double[][] cpGray = GrayValue;
		int xoffset=0,yoffset=0;
		double[][][] Values = new double[2][4][6];
		int total = ArraySize*ArraySize;
		StringBuffer OutsASM = new StringBuffer();
		StringBuffer OutsCon = new StringBuffer(); 
		StringBuffer OutsCOR = new StringBuffer();
		StringBuffer OutsDis = new StringBuffer();
    	FileWriter fw =null;
    	BufferedWriter bw =null;
    	FileWriter fw1 =null;
    	BufferedWriter bw1 =null;
    	FileWriter fw2=null;
    	BufferedWriter bw2 =null;
    	FileWriter fw3 =null;
    	BufferedWriter bw3 =null;
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
		//for(int inside = 1; inside<2; inside++){ //about out or in
			//if(inside == 0){
				for (int u = 0; u < h1; u++) {
					for (int v = 0; v < w1; v++) {
						for (int iu = u - (ArraySize / 2); iu <= u	+ (ArraySize / 2); iu++) {
							for (int iv = v - (ArraySize / 2); iv <= v	+ (ArraySize / 2); iv++) {
								int u2 = iu + yoffset;
								int v2 = iv + xoffset;
								if (u2 < h1 && v2 < w1	&& u2 >= 0 && v2 >= 0 && iu >= 0 && iu< h1 && iv >=0 && iv < w1) {
									comatrix[(int) GrayValue[iu][iv]][(int) GrayValue[u2][v2]]++;
									//total++;
								}
							}
						}
						Values[1][(int) angle/45][0] = ASM(comatrix, total);
						Values[1][(int) angle/45][1] = CON(comatrix, total);
						//Values[inside][(int) angle/45][2] = ENT(comatrix, total);
						//Values[inside][(int) angle/45][3] = HOM(comatrix, total);
						Values[1][(int) angle/45][4] = DIS(comatrix, total);
						Values[1][(int) angle/45][5] = COR(comatrix, total);
						for (int m = 0; m < comatrix.length; m++) {
							Arrays.fill(comatrix[m], 0);
						}
						OutsASM.append(Values[1][(int) angle/45][0]);
						OutsCon.append(Values[1][(int) angle/45][1]);
						OutsCOR.append(Values[1][(int) angle/45][5]);
						OutsDis.append(Values[1][(int) angle/45][4]);
						if(v != w1-1){
							OutsASM.append("\t");
							OutsCon.append("\t");
							OutsCOR.append("\t");
							OutsDis.append("\t");
						}
					}
		    		try {
						fw = new FileWriter("../../GLCM_point/Image"+imagenum+"/ASM_GLCM_"+angle+".txt ", true);
						bw = new BufferedWriter(fw);
						bw.write(OutsASM.toString());
						bw.newLine();
						fw1 = new FileWriter("../../GLCM_point/Image"+imagenum+"/Con_GLCM_"+angle+".txt ", true);
						bw1 = new BufferedWriter(fw1);
						bw1.write(OutsCon.toString());
						bw1.newLine();		
						fw2 = new FileWriter("../../GLCM_point/Image"+imagenum+"/Dis_GLCM_"+angle+".txt ", true);
						bw2 = new BufferedWriter(fw2);
						bw2.write(OutsDis.toString());
						bw2.newLine();	
						fw3 = new FileWriter("../../GLCM_point/Image"+imagenum+"/Cor_GLCM_"+angle+".txt ", true);
						bw3 = new BufferedWriter(fw3);
						bw3.write(OutsCOR.toString());
						bw3.newLine();	
						//System.out.println("In");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					OutsASM.delete(0, OutsASM.length());
					OutsCon.delete(0, OutsCon.length());
					OutsCOR.delete(0, OutsCOR.length());
					OutsDis.delete(0, OutsDis.length());
					try {
						bw.close();
						bw1.close();
						bw2.close();
						bw3.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//System.out.println("=============================== "+angle +" Outside===============================");
			//}
			/*else{
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
				//System.out.println("=============================== "+angle +" Inside===============================");
			}*/
			/*System.out.println(angle+" degree："+"Asm value is " + ASM(comatrix, total));
			System.out.println(angle+" degree："+"CON value is " + CON(comatrix, total));
			System.out.println(angle+" degree："+"ENT value is " + ENT(comatrix, total));
			System.out.println(angle+" degree："+"HOM value is " + HOM(comatrix, total));
			System.out.println(angle+" degree："+"DIS value is " + DIS(comatrix, total));
			System.out.println(angle+" degree："+"COR value is " + COR(comatrix, total));*/
			/*Values[inside][(int) angle/45][0] = ASM(comatrix, total);
			Values[inside][(int) angle/45][1] = CON(comatrix, total);
			Values[inside][(int) angle/45][2] = ENT(comatrix, total);
			Values[inside][(int) angle/45][3] = HOM(comatrix, total);
			Values[inside][(int) angle/45][4] = DIS(comatrix, total);
			Values[inside][(int) angle/45][5] = COR(comatrix, total);*/
			/*System.out.println(angle+" degree："+"Asm value is " + Values[inside][(int) angle/45][0]);
			System.out.println(angle+" degree："+"CON value is " + Values[inside][(int) angle/45][1]);
			System.out.println(angle+" degree："+"ENT value is " + Values[inside][(int) angle/45][2]);
			System.out.println(angle+" degree："+"HOM value is " + Values[inside][(int) angle/45][3]);
			System.out.println(angle+" degree："+"DIS value is " + Values[inside][(int) angle/45][4]);
			System.out.println(angle+" degree："+"COR value is " + Values[inside][(int) angle/45][5]);*/
			//total = 0;
			/*for (int m = 0; m < comatrix.length; m++) {
				Arrays.fill(comatrix[m], 0);
			}*/
		//}
	    /*try{
	    	WritableWorkbook workbook = null;
	    	for(int Insd = 1;Insd<2;Insd++){
	    	if(Insd==1)
	    		workbook = Workbook.createWorkbook(new File("C:/Users/cebleclipse/Desktop/GLCM/pn9_rf40/Inside/Image"+imagenum+".xls"));
	    	else
	    		workbook = Workbook.createWorkbook(new File("C:/Users/cebleclipse/Desktop/GLCM/pn9_rf40/Background/Image"+imagenum+".xls"));
	    	//將工作表一取名成 First Sheet
	    	WritableSheet sheet = workbook.createSheet("Image"+imagenum, 0);
	    	// first(0) is column second(2) para is row, and (0,2) in excel is (A,3)  
	    	WritableFont arial14font = new WritableFont(WritableFont.ARIAL, 14); 
	    	WritableCellFormat arial14format = new WritableCellFormat (arial14font);
	    	WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12); 
	    	WritableCellFormat arial12format = new WritableCellFormat (arial12font);
	    	Label label = new Label(2,0, "ASM",arial14format);
	    	sheet.addCell(label); 
	    	Label label1 = new Label(3,0, "CON",arial14format);
	    	sheet.addCell(label1); 
	    	Label label5 = new Label(4,0, "ENT",arial14format);
	    	sheet.addCell(label5); 
	    	Label label2 = new Label(5,0, "HOM",arial14format);
	    	sheet.addCell(label2); 
	    	Label label3 = new Label(6,0, "DIS",arial14format);
	    	sheet.addCell(label3); 
	    	Label label4 = new Label(7,0, "COR",arial14format);
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
	    	Number number = new Number(100, 100, 0,arial12format); 
	    	for(int i=0;i<4;i++){
	    		for(int j =0;j<6;j++){
	    			number.setValue(Values[Insd][i][j]);
	    			sheet.addCell(number.copyTo(j+2,i+1));
	    		}
	    	}
	    	workbook.write(); 
	    	workbook.close();
	    	}
	    }
	    catch(Exception ex){
	    	ex.printStackTrace();
	    }*/
		return cpGray;
	}

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

	/*private static double ENT(double[][] comatrix, int total) {
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
	}*/

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
