// Skeletal program for the "Image Threshold" assignment
// Written by:  Minglun Gong

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class ImageThreshold extends Frame implements ActionListener {
	int[][][] sourceMatrix;  	// Indices:  x, y, and channel
	int[][] sourceHistogram;	// Indices: intensity and channel
	BufferedImage input;
	int width, height;
	int numChannels = 3;
	TextField texThres, texOffset;
	ImageCanvas source, target;
	PlotCanvas2 plot;
	Boolean isGrayScale = true;

	private boolean isColorImage;
	private static final int DEFAULT_MANUAL_THRESHOLD = 128;

	private static final int[] BLACK = new int[]{0xff, 0xff, 0xff};
    private static final int[] WHITE = new int[]{0, 0, 0};

	// Constructor
	public ImageThreshold(String name) {
		super("Image Histogram");
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
		plot = new PlotCanvas2(256, 200);
		target = new ImageCanvas(width, height);
		//target.copyImage(input);
                target.resetImage(input);
		main.setLayout(new GridLayout(1, 3, 10, 10));
		main.add(source);
		main.add(plot);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		controls.add(new Label("Threshold:"));
		texThres = new TextField("128", 2);
		controls.add(texThres);
		Button button = new Button("Manual Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Automatic Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Otsu's Method");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Offset:"));
		texOffset = new TextField("10", 2);
		controls.add(texOffset);
		button = new Button("Adaptive Mean-C");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+400, height+100);
		setVisible(true);

		isColorImage = Misc.isColorImage(source.image);
		sourceMatrix = get3ChannelImageMatrix(source.image);
		sourceHistogram = get3ChannelHistogram(sourceMatrix);
	}


	public int[][][] get3ChannelImageMatrix(BufferedImage image){
		int[][][] matrix = new int[height][width][numChannels];
		int red, green, blue;

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				int pixel = input.getRGB(x,y);
				red = (pixel >> 16) & 0xff;
				green = (pixel >> 8) & 0xff;
				blue = (pixel) & 0xff;

				matrix[y][x][0] = red;
				matrix[y][x][1] = green;
				matrix[y][x][2] = blue;
			}
		}

		return matrix;
	}


	public int[][] get3ChannelHistogram(int [][][] matrix){
		int[][] hist = new int[256][numChannels];

		for(int channel = 0; channel < numChannels; channel++){
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					hist[matrix[y][x][channel]][channel]++;
				}
			}
		}

		return hist;
	}


	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		int[][][] transformHist = new int[height][width][numChannels];

		int[] thresholdArr = new int[3];
		thresholdArr[0] = thresholdArr[1] = thresholdArr[2] = DEFAULT_MANUAL_THRESHOLD;

		// example -- compute the average color for the image
		if ( ((Button)e.getSource()).getLabel().equals("Manual Selection") ) {
			int threshold;
			try {
				threshold = Integer.parseInt(texThres.getText());
			} catch (Exception ex) {
				texThres.setText("128");
				threshold = 128;
			}
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.BLACK, threshold, 100));

			int colour;

			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					if(isColorImage){
						for(int c = 0; c < numChannels; c++){
							if(sourceMatrix[y][x][c] > threshold){
								colour = (255 << 16) | (255 << 8) | 255;
								target.image.setRGB(x, y, colour);
							}
							else{
								colour = (0 << 16) | (0 << 8) | 0;
								target.image.setRGB(x, y, colour);
							}
						}
					}
					else{
						if(sourceMatrix[y][x][0] > threshold){
							colour = (255 << 16) | (255 << 8) | 255;
							target.image.setRGB(x, y, colour);
						}
						else{
							colour = (0 << 16) | (0 << 8) | 0;
							target.image.setRGB(x, y, colour);
						}
					}
				}
			}
			target.repaint();
		}

		if ( ((Button)e.getSource()).getLabel().equals("Automatic Selection") ) {
			if (isColorImage) {
				for (int i = 0; i < 3; i++) {
					thresholdArr[i] = automaticThreshold(source.image, 0);
				}
			}
			else {
				thresholdArr[0] = thresholdArr[1] = thresholdArr[2] = automaticThreshold(source.image, 0);
			}

			displayThreshold(thresholdArr);
			showFilter(thresholdArr);
		}

		if ( ((Button)e.getSource()).getLabel().equals("Otsu's Method") ) {
			if(isColorImage){
				for(int channel = 0; channel < numChannels; channel++){
					thresholdArr[channel] = OtsuThreshold(source.image, channel);
				}
			}
			else{
				thresholdArr[0] = thresholdArr[1] = thresholdArr[2] = OtsuThreshold(source.image,0);
			}

			displayThreshold(thresholdArr);
			showFilter(thresholdArr);
		}
	}

	
	public static void main(String[] args) {
		new ImageThreshold(args.length==1 ? args[0] : "fingerprint.png");
	}

	// Additional Functions
	private void displayThreshold(int[] thresholdArr){

		Color[] colors = {Color.RED, Color.GREEN, Color.BLUE};
		
		plot.clearObjects();
		
        if (isColorImage) {
            for (int i = 0; i < 3; i++) {
                plot.addObject(new VerticalBar(colors[i], thresholdArr[i], 100));
            }
		} 
		else {
            plot.addObject(new VerticalBar(Color.BLACK, thresholdArr[0], 100));
        }
    }

	public int automaticThreshold(BufferedImage image, int color) {
		int[][] matrix = Misc.getMatrixOfImage(image, color);
		int[] histogram = Misc.buildHistogram(matrix);

		double currThreshold = Misc.mean(histogram, 0, histogram.length), newThreshold;
		double group1, group2;
		int count1, count2, curr;

		while (true) {

			group1 = 0;
			group2 = 0;
			count1 = 0;
			count2 = 0;

			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					curr = Misc.getChannelFromRGB(image.getRGB(i, j), color);
					if (curr < currThreshold) {
						count1++;
						group1 += curr;
					}
					else {
						count2++;
						group2 += curr;
					}
				}
			}

			newThreshold = (group1 / count1 + group2 / count2) / 2;

			double comp = Math.abs(newThreshold - currThreshold);
			if (comp < 1.0) {
				break;
			}
			else {
				currThreshold = newThreshold;
			}
		}

		return Double.valueOf(newThreshold).intValue();
	}


	/*
	Function to calculate the threshold value of a specified color channel using
	Otsu's Method

	@param image 	The BufferedImage object of the source image
	@param color 	Integer value representing the color channel to consider
					red = 0, green = 1, blue = 2

	@return the threshold value
	*/
	private int OtsuThreshold(BufferedImage image, int color){
		int[][] imageMatrix = Misc.getMatrixOfImage(image, color);
		int[] histogram = Misc.buildHistogram(imageMatrix);

		double weightBackground = 0;
		double weightForeground = 0;
		double numerBackground = 0;		//Numerator of the meanBackground term
		double meanBackground = 0;
		double meanForeground = 0;
		double diffOfMeans = 0;
		double varianceInter = 0;
		double maxVariance = -Double.MAX_VALUE;
		int threshold = 0;
		int numPixels = image.getHeight() * image.getWidth();
		int sum = sumOfIntensities(histogram);		//Sum of all pixel intensities

		// Iterating through all candidate thresholds
		for(int t = 0; t < 256; t++){
			weightBackground += histogram[t];
			if(weightBackground == 0) continue;

			weightForeground  = numPixels - weightBackground;
			if(weightForeground == 0) break;

			numerBackground += (double) t * histogram[t];
			if(t == 0) continue;

			meanBackground = numerBackground / weightBackground;
			meanForeground = (sum - numerBackground) / weightForeground;

			diffOfMeans = meanForeground - meanBackground;

			varianceInter = weightBackground * weightForeground * diffOfMeans * diffOfMeans;

			if(varianceInter > maxVariance){
				maxVariance = varianceInter;
				threshold = t;
			}
		}

		return threshold;
	}


	/*
	Function to calculate the numerator of the mean term from intensity 0 to 255
	in Otsu's method

	@param histogram 	The histogram of an image
	@return the sum of the terms in the histogram
	*/
	private int sumOfIntensities(int[] histogram){
		int sum = 0;

		for(int i = 0; i < histogram.length; i++){
			sum += (i * histogram[i]);
		}

		return sum;
	}

	public void showFilter(int[] thr) {

        BufferedImage newImage = new BufferedImage(target.image.getWidth(), target.image.getHeight(), source.image.getType());

        for (int i = 0; i < target.image.getWidth(); i++) {
            for (int j = 0; j < target.image.getHeight(); j++) {
                setNewColor(source.image, newImage, i, j, thr);
            }
        }

        target.resetImage(newImage);
    }

	private void setNewColor(BufferedImage source, BufferedImage target, int i, int j, int[] thr){

        int[] col = source.getRaster().getPixel(i, j, new int[3]);
        int[] ncol = new int[3];
        if (isColorImage) {
            for (int k = 0; k < 3; k++) {
                ncol[k] = col[k] < thr[k] ? 0 : 255;
            }
            target.getRaster().setPixel(i, j, ncol);
		} 
		else {
            if (col[0] < thr[0]) {
                target.getRaster().setPixel(i, j, WHITE);
            } else {
                target.getRaster().setPixel(i, j, BLACK);
            }
        }
    }
}
