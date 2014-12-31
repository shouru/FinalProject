package Texture;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
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

import LevelSet.SkullStripper;

public class TamuraTextureFeature extends ViewportTool{
	public static File img;
	private static double[][] GrayValue;
	private static BufferedImage origimage;
	private static int ybegin,xbegin,xend,yend;
	private static int ROItotal;
	private static int height, width;
	/**
	 * Vector matrix
	 */
	private Vector _Matrix;
	
	/**
	 * record coarseness contrast direction
	 */
	private static double[] Values = new double[3];
	
	/**
	 * @mask = phi
	 */
	private static double[][] mask;
	
	public TamuraTextureFeature(double[][] phi, BufferedImage orig, int _xbegin,int _ybegin,int xen, int yen,int roi,int imagenum){
		super("TamuraTextureFeature", "TamuraTextureFeature of Image", "", "");
		origimage = orig;
		xbegin = _xbegin;
		xend = xen;
		ybegin = _ybegin;
		yend = yen;
		mask = phi;
		ROItotal = roi;
		double cor,cont,dir=0;
		//transform level to 256
		getGrayScaleAvg(origimage,256);
		for(int i = 1;i<2;i++){ //about out or in
			/*if(i==0)
				System.out.println("===================Outside==================");
			else
        		System.out.println("===================Inside===================");
			System.out.println("Cor is "+Cor(i));
			System.out.println("Contrast is "+Contrast(i)); */
			//System.out.println("Dir is "+Dir(16,12,i));
			cor = Cor(i);
			cont = Contrast(i);
			dir = Dir(16,12,i);
			try{
		    	WritableWorkbook workbook = null;
		    	if(i==1)
		    		workbook = Workbook.createWorkbook(new File("C:/Users/cebleclipse/Desktop/backup/Tamura/pn5_rf20/Inside/Image"+imagenum+".xls"));
		    	else
		    		workbook = Workbook.createWorkbook(new File("C:/Users/cebleclipse/Desktop/Tamura/pn3_rf20/Background/Image"+imagenum+".xls"));
		    	//將工作表一取名成 First Sheet
		    	WritableSheet sheet = workbook.createSheet("Image"+imagenum, 0);
		    	// first(0) is column second(2) para is row, and (0,2) in excel is (A,3)  
		    	WritableFont arial14font = new WritableFont(WritableFont.ARIAL, 14); 
		    	WritableCellFormat arial14format = new WritableCellFormat (arial14font);
		    	WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12); 
		    	WritableCellFormat arial12format = new WritableCellFormat (arial12font);
		    	Label label = new Label(2,0, "Coarseness",arial14format);
		    	sheet.addCell(label); 
		    	Label label1 = new Label(3,0, "Contrast",arial14format);
		    	sheet.addCell(label1); 
		    	Label label5 = new Label(4,0, "Direction",arial14format);
		    	sheet.addCell(label5); 
		    	Label label7 = new Label(0,1, "Values",arial14format);
		    	sheet.addCell(label7); 
		    	Number number = new Number(100, 100, 0,arial12format); 
		    	for(int j =0;j<3;j++){
		    		number.setValue(Values[j]);
		    		sheet.addCell(number.copyTo(j+2,1));
		    	}
		    	workbook.write(); 
		    	workbook.close();
			}
		    catch(Exception ex){
		    	ex.printStackTrace();
		    }
		}
	}
	
	/*public void mousePressed(ViewportCluster vpc, Viewport vp, MouseEvent e, int button){
		double[][] cpGray = GrayValue;
		//to determine which slice it is.
		int slicenum=0;
		System.out.println("size is "+vpc.getViewports().size());
		for(slicenum=0;slicenum<vpc.getViewports().size();slicenum++){
			if(vp==(Viewport)vpc.getViewports().get(slicenum))
					break;
		}
		SkullStripper skultemp = (SkullStripper) _skullstripper.get(slicenum);
		Matrix temp = (Matrix) _Matrix.get(slicenum);
		origimage = skultemp.getInputImage();
		xbegin = skultemp.getxbegin();
		xend = skultemp.getxend();
		ybegin = skultemp.getybegin();
		yend = skultemp.getyend();
		mask = skultemp.getmask();
		Scanner scan=new Scanner(System.in);
		System.out.println("請輸入轉換level");
		int level=scan.nextInt();
		getGrayScaleAvg(origimage,level);
		for(int i = 0;i<2;i++){
			System.out.println("Cor is "+Cor(i));
			System.out.println("Contrast is "+Contrast(i));
			System.out.println("Dir is "+Dir(16,12,i));
		}
	}*/
	/*public static void main(String[] args){
		double[][] cpGray = GrayValue;
		try {
			origimage = ImageIO.read(new File("D:/PJtest/Brain_abscess_simple_brain_CT.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scanner scan=new Scanner(System.in);
		System.out.println("請輸入轉換level");
		int level=scan.nextInt();
		getGrayScaleAvg(origimage,level);
		System.out.println("Cor is "+Cor());
		System.out.println("Contrast is "+Contrast());
		System.out.println("Dir is "+Dir(16,12,0.2));
	}*/
	public static void getGrayScaleAvg(BufferedImage img,int level) {
        width = img.getWidth();  //1200
        height = img.getHeight(); //1242;
        GrayValue = new double[height][width];
        int max =0, localmax = -1;
        BufferedImage gray = new BufferedImage(width, height, 1);// new image
        int value;
        Raster origRaster = img.getData();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                value = (int) origRaster.getSampleDouble(i, j, 0); // read and store pixel value
                GrayValue[j][i] = value;
                if(value>max){
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
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				GrayValue[j][i] = Math.round((GrayValue[j][i] / localmax) * (level - 1) * 10) / 10;
			}
		}
    }
	
    /**
     * 有需要增加的參數~~~
     */
    public static double Cor(int inside){
    	double cor = 0;
    	double maxV = 0;
    	double [][][] A = new double[6][height][width];
    	int BestS=0;
    	//denominator is region of interest, if inside == 1, it means the total pixel of roi of image,else the outside, roi inside+roi outside == image size
    	int total = 0,denominator=1;
    	//step 1
    	for(int m=0;m<height;m++){
    		for(int n =0; n < width;n++){
    			if(mask[m][n]==inside)
    				A[0][m][n] = GrayValue[m][n];
    		}
    	}
		if (inside == 1) {
			denominator = ROItotal;
			for (int k = 1; k < 6; k++) {
				for (int m = ybegin; m < yend; m++) {
					for (int n = xbegin; n < xend; n++) {
						for (int i = (int) (m - Math.pow(2, k - 1)); i < (int) (m + Math.pow(2, k - 1)); i++) {
							for (int j = (int) (n - Math.pow(2, k - 1)); j < (int) (n + Math.pow(2, k - 1)); j++) {
								// test the border to prevent out of array
								if (i >= 0 && i < height && j >= 0 && j < width && mask[m][n] == inside && mask[i][j] == inside) {
									A[k][m][n] += GrayValue[i][j];
									total++;
								}
							}
						}
						A[k][m][n] /= total;
						//System.out.print(A[k][m][n]+" ");
						total = 0;
					}
					//System.out.println();
				}
			}
			// step 2
			double Eh = 0, Ev = 0;
			for (int i = ybegin; i <= yend; i++) { 
				for (int j = xbegin; j <= xend; j++) { 
					for (int k = 0; k < 6; k++) { 
						if (k > 0) {
							if (i + Math.pow(2, k - 1) < height && i - Math.pow(2, k - 1) >= 0) {
								Ev = Math.abs(A[k][(int) (i + Math.pow(2, k - 1))][j] - A[k][(int) (i - Math.pow(2, k - 1))][j]);
							}// 這裡超過邊界的話 就直接用0替代前面/後面
							else if (i + Math.pow(2, k - 1) >= height) {
								Ev = Math.abs(A[k][(int) (i - Math.pow(2, k - 1))][j]);
							} else {
								Ev = Math.abs(A[k][(int) (i + Math.pow(2, k - 1))][j]);
							}
							if (j + Math.pow(2, k - 1) < width && j - Math.pow(2, k - 1) >= 0) {
								Eh = Math.abs(A[k][i][(int) (j + Math.pow(2, k - 1))] - A[k][i][(int) (j - Math.pow( 2, k - 1))]);
							} else if (j + Math.pow(2, k - 1) >= width) {
								Eh = Math.abs(A[k][i][(int) (j - Math.pow(2, k - 1))]);
							} else {
								Eh = Math.abs(A[k][i][(int) (j + Math.pow(2, k - 1))]);
							}
						} else {
							Ev = Eh = A[k][i][j];
						}
						if (maxV < Math.max(Ev, Eh)) {
							BestS = (int) Math.pow(2, k);
							maxV = Math.max(Ev, Eh);
						}
					}
					//System.out.println("MaxV is "+maxV);
					cor += BestS;
					maxV = 0;
				}
			}
		}
		// computation of outside 
		else{
			denominator = width*height - ROItotal;
			for (int k = 1; k < 6; k++) {
				for (int m = 0; m < height; m++) {
					for (int n = 0; n < width; n++) {
						for (int i = (int) (m - Math.pow(2, k - 1)); i < (int) (m + Math.pow(2, k - 1)); i++) {
							for (int j = (int) (n - Math.pow(2, k - 1)); j < (int) (n + Math.pow(2, k - 1)); j++) {
								// test the border to prevent out of array
								if (i >= 0 && i < height && j >= 0 && j < width && mask[m][n] == inside && mask[i][j] == inside) {
									A[k][m][n] += GrayValue[i][j];
									total++;
								}
							}
						}
						A[k][m][n] /= Math.pow(2, total);
						total = 0;
					}
				}
			}
			// step 2
			double Eh = 0, Ev = 0;
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					for (int k = 0; k < 6; k++) {
						if (k > 0) {
							if (i + Math.pow(2, k - 1) < height && i - Math.pow(2, k - 1) >= 0) {
								Ev = Math.abs(A[k][(int) (i + Math.pow(2, k - 1))][j] - A[k][(int) (i - Math.pow(2, k - 1))][j]);
							}// 這裡超過邊界的話 就直接用0替代前面/後面
							else if (i + Math.pow(2, k - 1) >= height) {
								Ev = Math.abs(A[k][(int) (i - Math.pow(2, k - 1))][j]);
							} else {
								Ev = Math.abs(A[k][(int) (i + Math.pow(2, k - 1))][j]);
							}
							if (j + Math.pow(2, k - 1) < width && j - Math.pow(2, k - 1) >= 0) {
								Eh = Math.abs(A[k][i][(int) (j + Math.pow(2, k - 1))] - A[k][i][(int) (j - Math.pow( 2, k - 1))]);
							} else if (j + Math.pow(2, k - 1) >= width) {
								Eh = Math.abs(A[k][i][(int) (j - Math.pow(2, k - 1))]);
							} else {
								Eh = Math.abs(A[k][i][(int) (j + Math.pow(2, k - 1))]);
							}
						} else {
							Ev = Eh = A[k][i][j];
						}
						if (maxV < Math.max(Ev, Eh)) {
							BestS = (int) Math.pow(k, 2);
							maxV = Math.max(Ev, Eh);
						}
					}
					cor += BestS;
					maxV = 0;
				}
			}
		}
		Values[0] = cor/denominator;
    	return cor/denominator;
    }
    
    public static double Contrast(int inside){
    	double Fcos=0;
    	double mean = 0;
    	double variance = 0; 
    	double Alpha4 = 0;
    	int total = 0;
    	if(inside == 1){
    		for(int i = ybegin; i <= yend;i++){
    			for(int j = xbegin ; j <= xend ; j++){
    				if(mask[i][j]==inside){
    					mean += GrayValue[i][j];
    					total++;
    				}
    			}
    		}
    		mean /= total;
    		total = 0;
    		for(int i = ybegin; i <= yend;i++){
    			for(int j = xbegin ; j <= xend ; j++){
    				if(mask[i][j]==inside){
    					variance += Math.pow(GrayValue[i][j]-mean, 2);
    					total++;
    				}
    			}
    		}
    		variance = variance/total;
    		variance = Math.pow(variance, 0.5);
    		total = 0;
    	//compute alpha4
    		for(int i = ybegin; i <= yend;i++){
    			for(int j = xbegin ; j <= xend ; j++){
    				if(mask[i][j]==inside){
    					Alpha4 += Math.pow((GrayValue[i][j]-mean), 4);
    					total++;
    				}
    			}
    		}
    	}
    	else{
    		for(int i = 0; i < height;i++){
        		for(int j = 0 ; j < width ; j++){
        			if(mask[i][j]==inside){
        				mean += GrayValue[i][j];
        				total++;
        			}
        		}
        	}
        	mean /= total;
        	total = 0;
        	for(int i = 0; i < height;i++){
        		for(int j = 0 ; j < width ; j++){
        			if(mask[i][j]==inside){
        				variance += Math.pow(GrayValue[i][j]-mean, 2);
        				total++;
        			}
        		}
        	}
        	variance = variance/total;
        	variance = Math.pow(variance, 0.5);
        	total = 0;
        	//compute alpha4
        	for(int i = 0; i < height;i++){
        		for(int j = 0 ; j < width ; j++){
        			if(mask[i][j]==inside){
        				Alpha4 += Math.pow((GrayValue[i][j]-mean), 4);
        				total++;
        			}
        		}
        	}
    	}
    	Alpha4 = Alpha4 / (total*Math.pow(variance, 4));
    	//compute contrast n = 1/4
    	if(Alpha4 == 0)
    		Fcos = 0;
    	else
    		Fcos = variance / Math.pow(Alpha4, 0.25);
    	Values[1] = Fcos;
		return Fcos;
    }
    
    //a lot of code need to be fixed
    private static double Dir(int d, int t, int inside){
    	double Fdir=0;
    	double[][] Hmatrix = new double[][]{{-1,0,1},{-1,0,1},{-1,0,1}};
    	double[][] Vmatrix = new double[][]{{1,1,1},{0,0,0},{-1,-1,-1}};
    	int sum_N_theta = 0;
    	int num_peak = 0;
    	double[][] Filterimg = new double[height+2][width+2];
    	double[][] deltaH = new double[height][width];
    	double[][] deltaV = new double[height][width];
    	double[][] deltaG = new double[height][width];
    	double[][] theta = new double[height][width];
    	double[] Ntheta = new double[d];
    	double[] EdgeProbaHisto = new double[d];
    	// first step; in order to filter the deltaH and deltaV kernel, add additional 0 to img;
    	for(int i = 0; i < height;i++){
    		for(int j = 0 ; j < width; j++){
    			Filterimg[i+1][j+1] = GrayValue[i][j];
    		}
    	}
    	for(int i = 1;i<=height;i++){
    		for(int j = 1;j<=width;j++){ //no normalization
    			deltaH[i-1][j-1] = Hmatrix[0][0]*Filterimg[i-1][j-1] + Hmatrix[0][1]*Filterimg[i-1][j] + Hmatrix[0][2]*Filterimg[i-1][j+1] + 
    							   Hmatrix[1][0]*Filterimg[i][j-1] + Hmatrix[1][1]*Filterimg[i][j] + Hmatrix[1][2]*Filterimg[i][j+1] + 
    							   Hmatrix[2][0]*Filterimg[i+1][j-1] + Hmatrix[2][1]*Filterimg[i+1][j] + Hmatrix[2][2]*Filterimg[i+1][j+1];
    			deltaV[i-1][j-1] = Vmatrix[0][0]*Filterimg[i-1][j-1] + Vmatrix[0][1]*Filterimg[i-1][j] + Vmatrix[0][2]*Filterimg[i-1][j+1] + 
						   		   Vmatrix[1][0]*Filterimg[i][j-1] + Vmatrix[1][1]*Filterimg[i][j] + Vmatrix[1][2]*Filterimg[i][j+1] + 
						   		   Vmatrix[2][0]*Filterimg[i+1][j-1] + Vmatrix[2][1]*Filterimg[i+1][j] + Vmatrix[2][2]*Filterimg[i+1][j+1];
    			theta[i-1][j-1] = Math.atan(deltaV[i-1][j-1] / deltaH[i-1][j-1]) + Math.PI/2;
    			deltaG[i-1][j-1] = (Math.abs(deltaH[i-1][j-1]) + Math.abs(deltaV[i-1][j-1])) / 2;
    			//System.out.print("deltaG["+(i-1)+"]["+(j-1)+"] is "+deltaG[i-1][j-1]+" ");
    		}
    		//System.out.println();
    	}
    	// second step ; construct the edge probability histogram EdgeProbaHisto and from now on , we need to separate roi into, inside and outside, two group. 
    	for(int k = 0; k<d;k++){
    		int accmulator = 0;
    		if(inside == 1){
    			for(int i = ybegin; i <= yend;i++)
    				for(int j = xbegin ; j <= xend ; j++){
    					if(mask[i][j]==inside){
    						if(theta[i][j]<(2*k+1)*	Math.PI/(2*d) && theta[i][j] >= (2*k-1)*Math.PI/(2*d)){
    							if(deltaG[i][j] >= t){
    								accmulator++;
    							}
    						}
    					}
    			}
    		}
    		else{
    			for(int i = 0; i < height;i++)
    				for(int j = 0; j < width ; j++){
    					if(mask[i][j]==inside)
    						if(theta[i][j]<(2*k+1)*	Math.PI/(2*d) && theta[i][j] >= (2*k-1)*Math.PI/(2*d))
    							if(deltaG[i][j] >= t)
    								accmulator++;
    				}
    		}
    		//System.out.println("accmulator is "+ accmulator);
    		Ntheta[k] = accmulator;
    		sum_N_theta += accmulator;
    	}
    	// compute number of peak and construct EdgeProbaHisto
    	// find the position of peak;
    	int maxposi = 0;
    	double maxvalue = 0;
    	//System.out.println("sum_N_theta is "+sum_N_theta);
    	for(int k=0;k<d;k++){
    		EdgeProbaHisto[k] = Ntheta[k] / sum_N_theta;
    		//System.out.println("EdgeProbaHisto["+k+"] is : "+EdgeProbaHisto[k]);
    		if(k==0){
    			maxvalue = Ntheta[k] / sum_N_theta;
    			maxposi = k;
    		}
    		else if((Ntheta[k] / sum_N_theta) > maxvalue){
    			maxposi = k;	
    			maxvalue = EdgeProbaHisto[k];
    		}
    	}
    	//System.out.println("maxposi is " + maxposi);
    	//System.out.println("maxvalue is "+ maxvalue);
    	//make a recursive class for compute the lol minus
    	Fdir = thominus(EdgeProbaHisto,d,maxposi);
    	Values[2] = Fdir;
		return Fdir;
    }
    
    private static double thominus(double[] EdgeProbaHisto,int d,int maxposi){
    	double result = 0;
    	for(int i=0;i<d;i++){
    		result = Math.pow(i-maxposi, 2)*EdgeProbaHisto[i];
    	}
    	return result;
    }
}
