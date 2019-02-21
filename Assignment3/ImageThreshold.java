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
	int[][][] baseHist;
	BufferedImage input;
	int width, height;
	int numChannels = 3;
	TextField texThres, texOffset;
	ImageCanvas source, target;
	PlotCanvas2 plot;
	Boolean isGrayScale = true;

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

		baseHist = getHistogram();
	}


	public int[][][] getHistogram(){
		int[][][] hist = new int[height][width][numChannels];
		int red, green, blue;

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				int pixel = input.getRGB(x,y);
				red = (pixel >> 16) & 0xff;
				green = (pixel >> 8) & 0xff;
				blue = (pixel) & 0xff;

				if((red != green) || (green != blue) || (blue != red)){
					isGrayScale = false;
				}

				hist[y][x][0] = red;
				hist[y][x][1] = green;
				hist[y][x][2] = blue;
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

			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					if(baseHist[y][x][0] > threshold){
						if(isGrayScale){
							int colour = (255 << 16) | (255 << 8) | 255;
							target.image.setRGB(x,y, colour);
						}
					}
				}
			}

			target.repaint();
		}
	}
	public static void main(String[] args) {
		new ImageThreshold(args.length==1 ? args[0] : "fingerprint.png");
	}
}
