package Texture;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.medtoolbox.jviewbox.viewport.Viewport;
import org.medtoolbox.jviewbox.viewport.ViewportCluster;
import org.medtoolbox.jviewbox.viewport.ViewportTool;

import LevelSet.SkullStripper;

public class TamuraTextureFeature extends ViewportTool{
	public static File img;
	private static double[][] GrayValue = {{0,1,2,3},{1,1,2,3},{2,2,2,3},{3,3,3,3}};
	private static BufferedImage origimage;
	private static int ybegin=0,xbegin=0,xend=3,yend=3; // modify
	private static int ROItotal = 16;
	private static int height = 4, width = 4; //need modify
	/**
	 * Vector matrix
	 */
	private Vector _Matrix;
	
	/**
	 * @mask = phi
	 */
	private static double[][] mask = {{1,1,1,1},{1,1,1,1},{1,1,1,1},{1,1,1,1}};
	
	public TamuraTextureFeature(double[][] phi, BufferedImage orig, int _xbegin,int _ybegin,int xen, int yen,int roi){
		super("TamuraTextureFeature", "TamuraTextureFeature of Image", "", "");
		/*origimage = orig;
		xbegin = _xbegin;
		xend = xen;
		ybegin = _ybegin;
		yend = yen;
		mask = phi;
		ROItotal = roi;*/
		//transform level to 256
		//getGrayScaleAvg(origimage,256);
		for(int i = 1;i<2;i++){
			if(i==0)
				System.out.println("===================Outside==================");
			else
        		System.out.println("===================Inside===================");
			//System.out.println("Cor is "+Cor(i));
			//System.out.println("Contrast is "+Contrast(i)); //fixing
			System.out.println("Dir is "+Dir(16,12,i));
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
						System.out.print(A[k][m][n]+" ");
						total = 0;
					}
					System.out.println();
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
					System.out.println("MaxV is "+maxV);
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
    	System.out.println("==========deltaV===========");
    	for(int i = 0; i<deltaV.length;i++){
    		for(int j = 0; j < deltaV[0].length;j++)
    			System.out.print(deltaV[i][j]+" ");
    		System.out.println();
    	}
    	System.out.println("==========deltaH===========");
    	for(int i = 0; i<deltaV.length;i++){
    		for(int j = 0; j < deltaV[0].length;j++)
    			System.out.print(deltaH[i][j]+" ");
    		System.out.println();
    	}
    	// second step ; construct the edge probability histogram EdgeProbaHisto and from now on , we need to separate roi into, inside and outside, two group. 
    	for(int k = 0; k<d;k++){
    		int accmulator = 0;
    		if(inside == 1){
    			for(int i = ybegin; i <= yend;i++)
    				for(int j = xbegin ; j <= xend ; j++){
    					if(mask[i][j]==inside)
    						if(theta[i][j]<(2*k+1)*	Math.PI/(2*d) && theta[i][j] >= (2*k-1)*Math.PI/(2*d))
    							if(deltaG[i][j] >= t)
    								accmulator++;
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
    		Ntheta[k] = accmulator;
    		sum_N_theta += accmulator;
    	}
    	// compute number of peak and construct EdgeProbaHisto
    	// find the position of peak;
    	int[] position = new int[d/2+1];
    	int[] v_position = new int[d/2+1];
    	int maxposi = 0;
    	int posi = 0,v_posi=0;
    	for(int k=0;k<d;k++){
    		EdgeProbaHisto[k] = Ntheta[k]/sum_N_theta;
    		if(k==0){
    			if(EdgeProbaHisto[k] > Ntheta[k+1]/sum_N_theta){
    				num_peak++;
    				position[++posi] = k;
    			}
    			else if(EdgeProbaHisto[k] < Ntheta[k+1]/sum_N_theta){
    				v_position[++v_posi] = k;
    			}
    		}
    		else if(k==d-1){
    			if(EdgeProbaHisto[k] < Ntheta[k-1]/sum_N_theta){
    				num_peak++;
    				position[++posi] = k;
    			}
    			else if(EdgeProbaHisto[k] > Ntheta[k-1]/sum_N_theta){
    				v_position[++v_posi] = k;
    			}
    		}
    		else{
    			if(EdgeProbaHisto[k] > Ntheta[k+1]/sum_N_theta && EdgeProbaHisto[k] > EdgeProbaHisto[k-1]){
    				num_peak++;
    				position[++posi] = k;
    			}
    			else if(EdgeProbaHisto[k] < Ntheta[k+1]/sum_N_theta && EdgeProbaHisto[k] < EdgeProbaHisto[k-1]){
    				v_position[++v_posi] = k;
    			}
    		}
    	}
    	
    	//make a recursive class for compute the lol minus
    	Fdir = thominus(EdgeProbaHisto,position,d,v_position,maxposi);
		return Fdir;
    }
    
    private static double thominus(double[] EdgeProbaHisto,int[] position,int d,int[] v_position,int maxposi){
    	double result = 0;
    	int posi = 1,v_posi=1;
    	if(position[posi] > v_position[v_posi]){
    		v_posi = 2;
    	}        
    	// to test the EdgeProbaHisto[[nowposi+dir] is valley or not
    	for(int i=0;i<d;i++){
    		if(i >= v_position[v_posi]){
    			v_posi++;
    			posi++;
    		}
    		result = Math.pow(i-position[posi], 2)*EdgeProbaHisto[i];
    	}
    	return result;
    }
}
