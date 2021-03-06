import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

import javafx.geometry.Point2D;

import java.util.*;

// Main class
public class HoughTransform extends Frame implements ActionListener {
	BufferedImage input;
	int width, height;
	ImageCanvas source, target;
	TextField texRad, texThres;

    double DEG_TO_RAD = Math.PI / 180.0;
    int maxTheta = 360;
    int minTheta = 0;
    int thetaRange = maxTheta - minTheta;
	int xOffset, yOffset, rhoMax, doubledRhoMax;
	Raster rast;

	double[] sinCache = new double[thetaRange];
	double[] cosCache = new double[thetaRange];

	// Constructor
	public HoughTransform(String name) {
		super("Hough Transform");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		rhoMax = (int) Math.sqrt(width * width + height * height);
		// prepare the panel for two images.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		target = new ImageCanvas(input);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Line Transform");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Radius:"));
		texRad = new TextField("10", 3);
		controls.add(texRad);
		button = new Button("Circle Transform");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Threshold:"));
		texThres = new TextField("25", 3);
		controls.add(texThres);
		button = new Button("Search");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(rhoMax*2+100, Math.max(height, 360)+100);
		setVisible(true);

		xOffset = width / 2;
		yOffset = height / 2;
		rast = source.image.getData();

        doubledRhoMax = 2 * rhoMax;

		// Caching sin and cos values
		setTrigCacheValues(sinCache, cosCache, minTheta, maxTheta);
	}


	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}


	// Action listener
	public void actionPerformed(ActionEvent e) {
		// perform one of the Hough transforms if the button is clicked.
		if ( ((Button)e.getSource()).getLabel().equals("Line Transform") ) {
            int maxCount = Integer.MIN_VALUE;
            int[][] g = new int[thetaRange][doubledRhoMax];

            // Find edge pixels
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){ 
                    // If the pixel is black, it is an edge pixel
					if(rast.getSample(x, y, 0) == 0){
                        // Update the accumulator
                        for(int theta = minTheta; theta < maxTheta; theta++){
                            int rho = (int) Math.round((x * cosCache[theta]) + (y * sinCache[theta]));
                            // shifting rho values to fit in the accumulator array
                            rho += rhoMax;
                            if(rho < 0 || rho > doubledRhoMax) continue;
                            g[theta][rho]++;

                            if(g[theta][rho] > maxCount){
                                maxCount = g[theta][rho];
                            }
                        }
					}
				}
			}

			double threshold = Double.parseDouble(texThres.getText()) / 100;
            int minBound = (int) (maxCount - (maxCount * threshold));
            source.resetBuffer(width, height);
            source.copyImage(input);
            for(int theta = 0; theta < maxTheta; theta++){
                for(int rho = 0; rho < doubledRhoMax; rho++){
                    if(g[theta][rho] > minBound){
                        HoughLine line = new HoughLine(rho-rhoMax, theta);
                        line.draw(source.image, Color.RED.getRGB());
                    }
                }
            }
            source.repaint();

            enforce(g, 360, doubledRhoMax, 10);
            DisplayTransform(doubledRhoMax, 360, g);
		}
		else if ( ((Button)e.getSource()).getLabel().equals("Circle Transform") ) {
			int[][] g = new int[height][width];
			int radius = Integer.parseInt(texRad.getText());
			for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (input.getRGB(x, y) == -1)
                        continue;
                    Point2D center = new Point2D(x, y);
                    selectSector(center, radius, 1, 1, g);
                    selectSector(center, radius, 1, -1, g);
                    selectSector(center, radius, -1, 1, g);
                    selectSector(center, radius, -1, -1, g);
                }
            }

            double threshold = Double.parseDouble(texThres.getText()) / 100;
            int minBound = (int)Math.round(4 * radius * threshold);
            source.resetBuffer(width, height);
            source.copyImage(input);
            Graphics2D modified = source.image.createGraphics();
            modified.setColor(Color.red);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (g[y][x] >= minBound)
                        modified.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
                }
            }
            source.repaint();

            enforce(g, height, width, Math.max(0, 255 / minBound));
			DisplayTransform(width, height, g);
		}
	}

	// display the spectrum of the transform.
	public void DisplayTransform(int wid, int hgt, int[][] g) {
		target.resetBuffer(wid, hgt);
		for ( int y=0, i=0 ; y<hgt ; y++ )
			for ( int x=0 ; x<wid ; x++, i++ )
			{
				int value = g[y][x] > 255 ? 255 : g[y][x];
				target.image.setRGB(x, y, new Color(value, value, value).getRGB());
			}
		target.repaint();
	}

	public static void main(String[] args) {
		new HoughTransform(args.length==1 ? args[0] : "rectangle.png");
	}


	/*
	* Additional Functions
	*/

    private void selectSector(Point2D center, int radius, int dx, int dy, int[][] g) {
        //int curX = (int)center.getX() + dx * radius;
        for (int i = 0; i <= radius; i++) {
            int curY = (int)center.getY() + dy * i;
            int deltaX = (int)Math.round(Math.sqrt(radius * radius - i * i));
            int curX = (int)center.getX() + dx * deltaX;
            /*
            while (center.distance(curX, curY) > radius)
                curX -= dx;
            if (Math.abs(center.distance(curX, curY) - radius) >
                    Math.abs(center.distance(curX + dx, curY) - radius))
                curX += dx;
               */
            if (GeoTools.inField(curX, curY, height, width))
                g[curY][curX]++;
        }
    }

    private void enforce(int[][] g, int h, int w, int multiplier) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++)
                g[y][x] *= multiplier;
        }
    }

    // Populate the sin and cos cache arrays
    private void setTrigCacheValues(double[] sinArray, double[] cosArray, int minTheta, int maxTheta){
        for(int theta = minTheta; theta < maxTheta; theta++){
            double rad = theta * DEG_TO_RAD;
            sinArray[theta] = Math.sin(rad);
            cosArray[theta] = Math.cos(rad);
        }
    }
}
