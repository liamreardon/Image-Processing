// COMP3301 Assignment #1
// Written by Eric Roy Elli & Liam Reardon
// Based on the skeletal program written by Minglun Gong

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class ImageHistogram extends Frame implements ActionListener {
	// Mapping of color channel to int
	int _RED = 0;
	int _GREEN = 1;
	int _BLUE = 2;

	BufferedImage input;
	int width, height;
	TextField texRad, texThres;
	ImageCanvas source, target;
	PlotCanvas plot;
	int i = 0, j = 0;
	// Constructor
	public ImageHistogram(String name) {
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
		plot = new PlotCanvas();
		target = new ImageCanvas(input);
		main.setLayout(new GridLayout(1, 3, 10, 10));
		main.add(source);
		main.add(plot);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Display Histogram");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Histogram Stretch");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Cutoff fraction:"));
		texThres = new TextField("10", 2);
		controls.add(texThres);
		button = new Button("Aggressive Stretch");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Histogram Equalization");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+400, height+100);
		setVisible(true);
	}


	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}


	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {

		target.resetImage(input);
		String label = ((Button)e.getSource()).getLabel();
		int[][] intensities = new int[256][3];

        int red = 0, green = 0, blue = 0;
        double min = 1, max = 0;
        double cmin, cmax;
               
		switch(label){
			case "Display Histogram":

				// Generating the image histogram
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						int pixel = input.getRGB(x,y);
						red = (pixel >> 16) & 0xff;
						green = (pixel >> 8) & 0xff;
						blue = (pixel) & 0xff;

						intensities[red][_RED]++;
						intensities[green][_GREEN]++;
						intensities[blue][_BLUE]++;

					}
				}

				plot.showHistogram(intensities);
				// String msg = Arrays.deepToString(intensities);
				// System.out.println(msg);
				// System.out.println(intensities.length);
				break;
			case "Histogram Stretch":
							
				System.out.println("Histogram Stretch");

                // convert each pixel from RGB to HSB and find min & max brightness values                                       
                for ( int y = 0; y < height; y++ ) {
                    for ( int x = 0; x < width; x++) {
                        Color clr = new Color(input.getRGB(x, y));
                        
                        float[] hsb = Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), null);
                        
                        if(hsb[2] > max) {
                            max = hsb[2];
                        }

                        if(hsb[2] < min) {
                            min = hsb[2];
                        }
                    }
                }

//              System.out.println(max);
//              System.out.println(min);
                               

                // convert each pixel from RGB to HSB, apply min and max values to HSB to stretch
                // brightness, convert back to RGB and plot
                for (int y = 0; y < height; y++ ) {
                    for (int x = 0; x < width; x++) {

                        Color clr = new Color(input.getRGB(x, y));
                        float[] hsb = Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), null);
                        
                        hsb[2] = (float) ((hsb[2] - min) / (max - min));
                        
                        int nClr = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                        
                        red = (nClr >> 16) & 0xff;
                        green = (nClr >> 8) & 0xff;
                        blue = (nClr) & 0xff;

                        target.image.setRGB(x, y, nClr);

                        intensities[red][_RED]++;
                        intensities[green][_GREEN]++;
                        intensities[blue][_BLUE]++;

                    }
                }

                plot.showHistogram(intensities);
                target.repaint();

				break;
			case "Aggressive Stretch":

				System.out.println("Aggressive Stretch");

				String cutoff = texThres.getText();
				
				// if cutoff fraction is empty, assign default cmin/cmax v
				if (cutoff.isEmpty()) {

					cmin = 0;
					cmax = 1;

				}

				// else convert cutoff string to double and divide to get values between 0 and 1
				else {

					cmin = Double.parseDouble(cutoff);

					if (cmin > 100) {
						cmin = 100;
					}

					if (cmin < 0) {
						cmin = 0;
					}
					
					System.out.println(cmin);
					cmin = cmin / 100;
					cmax = 1 - cmin;

				}

				
				// convert each pixel from RGB to HSB and find min & max brightness values, compare with
				// cmin/cmax to aggressively stretch histogram
				for (int y = 0; y < height; y++ ) {
					for (int x = 0; x < width; x++ ) {
						
						Color clr = new Color(input.getRGB(x, y));
						float[] hsb = Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), null);

						if (hsb[2] > max) {

							if(hsb[2] < cmax) {

								max = hsb[2];
							}
						}

						if (hsb[2] < min) {

							if (hsb[2] > cmin) {
								min = hsb[2];
							}
						}
					}
				}


				// loop to ensure all brightness pixels are between 0 and 1 to avoid over/underexposure
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						
						Color clr = new Color(input.getRGB(x, y));

						float[] hsb = Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), null);

						hsb[2] = (float) ((hsb[2] - min)/(max - min));

						if(hsb[2] > 1) {

							hsb[2] = 1;
						}

						if(hsb[2] < 0) {

							hsb[2] = 0;
						}

						int nClr = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);

						red = (nClr >> 16) & 0xff;
                        green = (nClr >> 8) & 0xff;
                        blue = (nClr) & 0xff;

                        target.image.setRGB(x, y, nClr);

                        intensities[red][_RED]++;
                        intensities[green][_GREEN]++;
                        intensities[blue][_BLUE]++;				
					}
				}
				
                plot.showHistogram(intensities);
                target.repaint();
						
				break;
			case "Histogram Equalization":
				System.out.println("Histogram Equalization");
				// HSL representation of each pixel in the image
				HSL[][] hsl_img = new HSL[height][width];

				// Get the RGB values of each pixel and convert to HSL
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						int pixel = input.getRGB(x,y);
						red = (pixel >> 16) & 0xff;
						green = (pixel >> 8) & 0xff;
						blue = (pixel) & 0xff;

						hsl_img[y][x] = new HSL(red, green, blue);
					}
				}

				// Array to count the number of pixel having each intensity value
				int[] lightnessCounts = new int[256];

				for(int y = 0; y < hsl_img.length; y++){
					for(int x = 0; x < hsl_img[0].length; x++){
						lightnessCounts[hsl_img[y][x].lightness]++;
					}
				}

				int numPixels = width * height;
				double[] cdf = new double[256];  //Cumulative Distribution array

				for(int x = 0; x < lightnessCounts.length; x++){
					cdf[x] = (double) lightnessCounts[x] / numPixels;
					if(x > 0){
						cdf[x] += cdf[x-1];
					}
				}


				// Applying the transform function 
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						hsl_img[y][x].lightness = (int) Math.round(cdf[hsl_img[y][x].lightness] * 255);
					}
				}

				// Coverting hsl_img back to rgb
				Color[][] rgb_img = new Color[height][width];
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){

						rgb_img[y][x] = hsl_img[y][x].hsl2rgb();
						intensities[rgb_img[y][x].getRed()][_RED]++;
						intensities[rgb_img[y][x].getGreen()][_GREEN]++;
						intensities[rgb_img[y][x].getBlue()][_BLUE]++;

						Color nClr = new Color(rgb_img[y][x].getRed(), rgb_img[y][x].getGreen(), rgb_img[y][x].getBlue());
						target.image.setRGB(x, y, nClr.getRGB());

					}


				}

				plot.showHistogram(intensities);
				target.repaint();
				break;
		}
	}

	public static void main(String[] args) {
		new ImageHistogram(args.length==1 ? args[0] : "baboon.png");
	}
}

// Canvas for plotting histogram
class PlotCanvas extends Canvas {
	// lines for plotting axes and mean color locations
	LineSegment x_axis, y_axis;
	LineSegment red, green, blue;
	LineSegment[][] segments = new LineSegment[255][3];
	Color[] colors = {Color.RED, Color.GREEN, Color.BLUE};
	boolean showMean = false;
	boolean showHist = false;

	public PlotCanvas() {
		x_axis = new LineSegment(Color.BLACK, -10, 0, 256+10, 0);
		y_axis = new LineSegment(Color.BLACK, 0, -10, 0, 200+10);
	}
	// set mean image color for plot
	public void setMeanColor(Color clr) {
		red = new LineSegment(Color.RED, clr.getRed(), 0, clr.getRed(), 100);
		green = new LineSegment(Color.GREEN, clr.getGreen(), 0, clr.getGreen(), 100);
		blue = new LineSegment(Color.BLUE, clr.getBlue(), 0, clr.getBlue(), 100);
		showMean = true;
		repaint();
	}

	// Function to show image histogram
	//
	// arr should be structured so the first argument is the color intensity and
	// the second argument is the integer that represents a color 
	// (red = 0, blue = 1, green = 2)
	public void showHistogram(int[][] arr){
		double scalingFactor = getScalingFactor(arr);
		arr = scaleIntensities(arr, scalingFactor);

		// Creating line segments
		for(int x = 0; x<arr.length-1; x++){
			for(int y = 0; y<arr[0].length; y++){
				segments[x][y] = new LineSegment(colors[y], x, arr[x][y], x+1, arr[x+1][y]);
			}
		}

		showHist = true;
		repaint();

	}
	// redraw the canvas
	public void paint(Graphics g) {
		// draw axis
		int xoffset = (getWidth() - 256) / 2;
		int yoffset = (getHeight() - 200) / 2;
		x_axis.draw(g, xoffset, yoffset, getHeight());
		y_axis.draw(g, xoffset, yoffset, getHeight());


		// Drawing the histogram
		if( showHist ){
			for(int x = 0; x < segments.length; x++){
				for(int y = 0; y < segments[0].length; y++){
					segments[x][y].draw(g, xoffset, yoffset, getHeight());
				}
			}
		}
	}

	// Scaling factor to fit histogram within bounds
	private double getScalingFactor(int[][] arr){
		double y_max = 200; // max value in y-axis is 200
		int max_count = -1;

		//Finding the maximum intensity count over all channels
		for(int channel = 0; channel < 3; channel++){
			for(int bin = 0; bin < 256; bin++){
				if(arr[bin][channel] > max_count){
					max_count = arr[bin][channel];
				}
			}
		}

		return y_max / max_count;
	}
 	
 	// Applies the scaling factor to each element in arr
	private int[][] scaleIntensities(int[][] arr, double scalingFactor){
		for(int bin = 0; bin < 256; bin++){
			for(int channel = 0; channel < 3; channel++){
				arr[bin][channel] = (int) Math.round(arr[bin][channel] * scalingFactor);
			}
		}
		return arr;
	}
}

// LineSegment class defines line segments to be plotted
class LineSegment {
	// location and color of the line segment
	int x0, y0, x1, y1;
	Color color;
	// Constructor
	public LineSegment(Color clr, int x0, int y0, int x1, int y1) {
		color = clr;
		this.x0 = x0; this.x1 = x1;
		this.y0 = y0; this.y1 = y1;
	}
	public void draw(Graphics g, int xoffset, int yoffset, int height) {
		g.setColor(color);
		g.drawLine(x0+xoffset, height-y0-yoffset, x1+xoffset, height-y1-yoffset);
	}
}

