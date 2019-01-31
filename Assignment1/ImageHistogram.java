// Skeletal program for the "Image Histogram" assignment
// Written by:  Minglun Gong

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

						plot.showHistogram(intensities);
					}
				}
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

                        intensities[red][_RED]++;
                        intensities[green][_GREEN]++;
                        intensities[blue][_BLUE]++;

                        plot.showHistogram(intensities);
                    }
                }

				break;
			case "Aggressive Stretch":

				System.out.println("Aggressive Stretch");

				String cutoff = texThres.getText();

				if (cutoff.isEmpty()) {

					cmin = 0;
					cmax = 1;

				}

				else {

					cmin = Double.parseDouble(cutoff);
					cmin = cmin / 100;
					cmax = 1 - cmin;

				}

				
				
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

                        intensities[red][_RED]++;
                        intensities[green][_GREEN]++;
                        intensities[blue][_BLUE]++;

                        plot.showHistogram(intensities);
				
					}
				}
						
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

				int[] lightnessCounts = new int[256];

				for(int y = 0; y < hsl_img.length; y++){
					for(int x = 0; x < hsl_img[0].length; x++){
						lightnessCounts[(int) Math.round(hsl_img[y][x].lightness* 255)]++;
					}
				}

				int numPixels = width * height;

				double[] equalizedCounts = new double[256];

				for(int x = 0; x < lightnessCounts.length; x++){
					equalizedCounts[x] = (double) lightnessCounts[x] / numPixels;
					if(x > 0){
						equalizedCounts[x] += equalizedCounts[x-1];
					}
				}

				// float[][] normalized = normalizeRGB(intensities);
				plot.showHistogram(intensities);
				break;
		}
	}


	// Function to normalize the rbg values for each pixel
	// Args:
	// 	intensities - the array of the original intensities of each pixel
	//
	// Returns:
	// 	the 2D array of normalized values
	public float[][] normalizeRGB(int[][] intensities){
		int bins = intensities.length;
		int channels = intensities[0].length;

		float[][] normalized = new float[bins][channels];

		float maxTotal = -1;

		// Finding the bin that has the highest total value over all channels
		for(int x = 0; x < bins; x++){
			float total = 0;
			for(int y = 0; y < channels; y++){
				total += intensities[x][y];
			}
			if(total > maxTotal){ maxTotal = total; }
		}

		// Normalizing all bins in each channel based on maxTotal
		for(int x = 0; x < bins; x++){
			for(int y = 0; y < channels; y++){
				normalized[x][y] = (float) intensities[x][y]/maxTotal;
			}
		}

		return normalized;
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
		for(int x = 0; x<arr.length-1; x++){
			for(int y = 0; y<arr[0].length; y++){
				segments[x][y] = new LineSegment(colors[y], x, arr[x][y]/3, x+1, arr[x+1][y]/3);
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

