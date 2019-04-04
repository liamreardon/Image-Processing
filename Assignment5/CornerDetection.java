import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;

// Main class
public class CornerDetection extends Frame implements ActionListener {
	BufferedImage input;
	int width, height;
	double sensitivity=.1;
	int threshold=20;
	ImageCanvas source, target;
	CheckboxGroup metrics = new CheckboxGroup();
	int numClicks = 0;

	// Convolutions in the x and y directions
	int[][] Ix, Iy;

	// Image smoothed by gaussian filter
	int[][] Smoothed;

	// The values necessary for the structure tensor
	int[][] Ix2, Iy2, Ixy;

	int KERNEL_SIZE = 3;
	double SIGMA = 1;

	// Constructor
	public CornerDetection(String name) {
		super("Corner Detection");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		target = new ImageCanvas(width, height);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Derivatives");
		button.addActionListener(this);
		controls.add(button);
		// Use a slider to change sensitivity
		JLabel label1 = new JLabel("sensitivity=" + sensitivity);
		controls.add(label1);
		JSlider slider1 = new JSlider(1, 25, (int)(sensitivity*100));
		slider1.setPreferredSize(new Dimension(50, 20));
		controls.add(slider1);
		slider1.addChangeListener(changeEvent -> {
			sensitivity = slider1.getValue() / 100.0;
			label1.setText("sensitivity=" + (int)(sensitivity*100)/100.0);
		});
		button = new Button("Corner Response");
		button.addActionListener(this);
		controls.add(button);
		JLabel label2 = new JLabel("threshold=" + threshold);
		controls.add(label2);
		JSlider slider2 = new JSlider(0, 100, threshold);
		slider2.setPreferredSize(new Dimension(50, 20));
		controls.add(slider2);
		slider2.addChangeListener(changeEvent -> {
			threshold = slider2.getValue();
			label2.setText("threshold=" + threshold);
		});
		button = new Button("Thresholding");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Non-max Suppression");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Display Corners");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(Math.max(width*2+100,850), height+110);
		setVisible(true);

		Ix = new int[height][width];
		Iy = new int[height][width];
		Ix2 = new int[height][width];
		Iy2 = new int[height][width];
		Ixy = new int[height][width];
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {

		// define types of output images
		ImageCanvas derivatedImage, cornerResponsedImage, thressholdedImage, nonMaxedImage;

		// derivative class
		DerivativeCalculus derivative = new DerivativeCalculus(width, height, source);
		derivatedImage = derivative.process();

		// corner response class
		CornerResponse	cr = new CornerResponse(width, height, source, sensitivity);
		cr.init(derivative.getValx(), derivative.getValy());
		cornerResponsedImage = cr.process();

		// generate Moravec corner detection result
		if ( ((Button)e.getSource()).getLabel().equals("Derivatives") ){
			if(numClicks == 0){
				displayPartialDerivative("Ix");
				System.out.println("Now Displaying Ix");
				numClicks++;
			}
			else if(numClicks == 1){
				displayPartialDerivative("Iy");
				System.out.println("Now Displaying Iy");
				numClicks = 0;
			}
		}
    
		if (((Button) e.getSource()).getLabel().equals("Corner Response")) {
			target.image.setData(cornerResponsedImage.image.getData());
			target.repaint();
		}
	}
	public static void main(String[] args) {
		CornerDetection cd = new CornerDetection(args.length==1 ? args[0] : "signal_hill.png");
		cd.derivativeOfGaussian();
		cd.structureTensorComponents();
	}

	// moravec implementation
	void derivatives() {
		int l, t, r, b, dx, dy;
		Color clr1, clr2;
		int gray1, gray2;

		for ( int q=0 ; q<height ; q++ ) {
			t = q==0 ? q : q-1;
			b = q==height-1 ? q : q+1;
			for ( int p=0 ; p<width ; p++ ) {
				l = p==0 ? p : p-1;
				r = p==width-1 ? p : p+1;
				clr1 = new Color(source.image.getRGB(l,q));
				clr2 = new Color(source.image.getRGB(r,q));
				gray1 = clr1.getRed() + clr1.getGreen() + clr1.getBlue();
				gray2 = clr2.getRed() + clr2.getGreen() + clr2.getBlue();
				dx = (gray2 - gray1) / 3;
				clr1 = new Color(source.image.getRGB(p,t));
				clr2 = new Color(source.image.getRGB(p,b));
				gray1 = clr1.getRed() + clr1.getGreen() + clr1.getBlue();
				gray2 = clr2.getRed() + clr2.getGreen() + clr2.getBlue();
				dy = (gray2 - gray1) / 3;
				dx = Math.max(-128, Math.min(dx, 127));
				dy = Math.max(-128, Math.min(dy, 127));
				target.image.setRGB(p, q, new Color(dx+128, dy+128, 128).getRGB());
			}
		}
		target.repaint();
	}


	public void derivativeOfGaussian() {
		double[] kernel = get1dKernel(KERNEL_SIZE, 1.0);


		// Convert the image to grayscale
		int[][] gray = rgb2gray(source.image);

		// Apply the gaussian smoothing filter to the grayscale image
		GrayscaleGaussianFilter gaussian = new GrayscaleGaussianFilter(gray, KERNEL_SIZE, SIGMA);
		Smoothed = gaussian.getSmoothedImage();


		double[] dKernel = {-0.5, 0, 0.5}; // The kernel use to calculate the derivative
		int derivRadius = dKernel.length / 2;

		// Applying the derivative kernel horizontally
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				int color = 0;
				for(int offset = -derivRadius; offset <= derivRadius; offset++){
					int curX = x + offset;
					curX = curX < 0 ? 0 : curX >= width ? (width - 1) : curX;
					color += (int) (((Smoothed[y][curX] >> 16) & 0xFF) * dKernel[offset + derivRadius]);
				}
				// color = Math.max(-128, Math.min(color, 127)) + 128;
				Ix[y][x] = color;
			}
		}

		// Apply the derivitive kernel vertically
		for(int x = 0; x < height; x++){
			for(int y = 0; y < width; y++){
				int color = 0;
				for(int offset = -derivRadius; offset <= derivRadius; offset++){
					int curY = y + offset;
					curY = curY < 0 ? 0 : curY >= width ? (width - 1) : curY;
					color += (int) (((Smoothed[curY][x] >> 16) & 0xFF) * dKernel[offset + derivRadius]);
				}
				// color = Math.max(-128, Math.min(color, 127)) + 128;
				Iy[y][x] = color;
			}
		}
	}


	public void structureTensorComponents(){
		// Calculating the components of the structure tensor
		int[][] Ix2 = new int[height][width];
		int[][] Iy2 = new int[height][width];
		int[][] Ixy = new int[height][width];

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				Ix2[y][x] = Ix[y][x] * Ix[y][x];
				Iy2[y][x] = Iy[y][x] * Iy[y][x];
				Ixy[y][x] = Ix[y][x] * Iy[y][x];
			}
		}

		System.out.println(Ix[200][150]);
	}

	/*
		Draws the specified partial derivative to the target image
		Negative values are set to black
		Positive values are set to white
		Zero values are set to gray
		@param derivative the desired partial derivative to show (Ix or Iy);
	*/
	private void displayPartialDerivative(String derivative){
		int[][] matrix = Ix;
		if(derivative.equals("Ix")){ matrix = Ix; } 
		else if(derivative.equals("Iy")){ matrix = Iy; }

		for(int y = 0; y < matrix.length; y++){
			for(int x = 0; x < matrix[0].length; x++){
				int color = Math.max(-128, Math.min(matrix[y][x], 127)) + 128;
				int pixel = (color << 16) | (color << 8) | color;
				target.image.setRGB(x, y, pixel);
			}
		}

		target.repaint();
	}

	/*
		Returns the 1D kernel of the first derivative of the gaussian

		@param kernelSize the length of the kernel
		@param sigma the standard deviation of the normal curve
		@return the 1D gaussian kernel of the first derivative of the gaussian
	*/
	public double[] get1dKernel(int kernelSize, double sigma) {
		double[] kernel = new double[kernelSize];
		int kernelRadius = kernelSize / 2;
		double constant = 1d / (Math.sqrt(2 * Math.PI) * sigma * sigma * sigma);
		double total = 0;

		// Calculating kernel values and the normalizing factor
		for(int x = -kernelRadius; x <= kernelRadius; x++){
			double temp = -((double) x * constant);
			double exponent = ((double) -(x * x)) / (2 * sigma * sigma);

			kernel[x + kernelRadius] =  temp * Math.exp(exponent);
			if(kernel[x + kernelRadius] > 0){
				total -= kernel[x + kernelRadius];
			}
		}

		for(int x = 0; x < kernelSize; x++){
			kernel[x] /= total;
		}

		return kernel;
	}


	/*
	* Takes a colored BufferedImage as an argument and returns a matrix that 
	* represents a grayscale image. The first argument of the matrix is the row 
	* of the image and the second is the column
	*
	* @param image The colored BufferedImage to be converted to grayscale
	* @return int[][] the grayscale image in matrix form
	*/
	public int[][] rgb2gray(BufferedImage image){
		int[][] grayImg = new int[image.getHeight()][image.getWidth()];
		int[] pixel;
		for(int y = 0; y < image.getHeight(); y++){
			for(int x = 0; x < image.getWidth(); x++){
				Color color = new Color(image.getRGB(x, y));
				int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
				grayImg[y][x] = (gray << 16 | gray << 8 | gray);
			}
		}

		return grayImg;
	}
}
