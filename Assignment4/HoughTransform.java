import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

// Main class
public class HoughTransform extends Frame implements ActionListener {
	BufferedImage input;
	int width, height, diagonal;
	ImageCanvas source, target;
	TextField texRad, texThres;

	double deg2rad = Math.PI / 180.0;
	int xOffset, yOffset;
	Raster rast;
	double[] sinCache = new double[360];
	double[] cosCache = new double[360];

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
		diagonal = (int) Math.sqrt(width * width + height * height);
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
		setSize(diagonal*2+100, Math.max(height,360)+100);
		setVisible(true);

		xOffset = width / 2;
		yOffset = height / 2;
		rast = source.image.getData();

		// Precalculating sin and cos values
		setTrigCacheValues(sinCache, cosCache);
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
			int maxRho = -100000;
			int maxTheta = -100000;
			int maxCount = 0;
			// double threshold = Double.parseDouble(texThres) / 100d;
			int[][] g = new int[360][diagonal];
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					if(rast.getSample(x, y, 0) == 0){
						for(int theta = 0; theta < 360; theta++){
							int rho = (int) (((x - xOffset) * cosCache[theta]) + ((y - yOffset) * sinCache[theta]));
							rho += (int) diagonal / 2;
							g[theta][rho]++;
							if(g[theta][rho] > maxCount){
								maxRho = rho;
								maxTheta = theta;
							}
						}
					}
				}
			}

			// for(int theta = 0; theta < 360; theta++){
			// 	for(int rho = 0; rho < diagonal; rho++){
			// 		if(g[theta][rho] > (int) (maxCount * threshold){

			// 		}
			// 	}
			// }

			DisplayTransform(diagonal, 360, g);



		}
		else if ( ((Button)e.getSource()).getLabel().equals("Circle Transform") ) {
			int[][] g = new int[height][width];
			int radius = Integer.parseInt(texRad.getText());
			// insert your implementation for circle here.
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

	// Populate the sin and cos cache arrays
	private void setTrigCacheValues(double[] sinArray, double[] cosArray){
		for(int theta = 0; theta < 360; theta++){
			double rad = theta * deg2rad;

			sinArray[theta] = Math.sin(rad);
			cosArray[theta] = Math.cos(rad);
		}
	}


}
