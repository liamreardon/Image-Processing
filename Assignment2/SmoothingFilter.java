import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class SmoothingFilter extends Frame implements ActionListener {
	BufferedImage input;
	ImageCanvas source, target;
	TextField texSigma;
	int width, height;
	// Constructor
	public SmoothingFilter(String name) {
		super("Smoothing Filters");
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
		target = new ImageCanvas(input);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Add noise");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 mean");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Sigma:"));
		texSigma = new TextField("1", 1);
		controls.add(texSigma);
		button = new Button("5x5 Gaussian");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 median");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 Kuwahara");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+100, height+100);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		// example -- add random noise

		String label = ((Button)e.getSource()).getLabel();

		switch (label) {

			case "Add noise":

				Random rand = new Random();
				int dev = 64;
				for ( int y=0, i=0; y<height; y++ )
					for ( int x=0 ; x<width ; x++, i++ ) {
						Color clr = new Color(source.image.getRGB(x, y));
						int red = clr.getRed() + (int)(rand.nextGaussian() * dev);
						int green = clr.getGreen() + (int)(rand.nextGaussian() * dev);
						int blue = clr.getBlue() + (int)(rand.nextGaussian() * dev);
						red = red < 0 ? 0 : red > 255 ? 255 : red;
						green = green < 0 ? 0 : green > 255 ? 255 : green;
						blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
						source.image.setRGB(x, y, (new Color(red, green, blue)).getRGB());
					}

				source.repaint();
				break;

			case "5x5 mean":
			
				int inputImage[][] = new int[height][width];
				int red = 0;
				int blue = 0;
				int green = 0;

				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						inputImage[i][j] = input.getRGB(j, i);
					}
				}

				for (int u = 0; u < height; u++) {
					for (int v = 0; v < width; v++) {
			             
						for (int j = 0; j <= 2; j++) {
                            for (int i = -2; i <= 2; i++) {
                                if((u + (j) >= -2 && v + (i) >= 0 && u + (j) < width && v + (i) < height)) {
                                    int pixel = inputImage[u + j][v + i];
                                    //System.out.println(pixel);

                                    int rr = (pixel >> 16) & 0xff;
                                    int rg = (pixel >> 8) & 0xff;
                                    int rb = pixel & 0xff;

                                    red += rr;
                                    green += rg;
                                    blue += rb;
        

                                }
                            }
                        }
                        
                        red /= 25; green /= 25; blue /= 25;
                        int newPixel = (red<<16)|(green<<8)|blue;
                        //System.out.println(newPixel);

                        target.image.setRGB(v, u, newPixel);


					}
				}

				target.repaint();


				break;
        
        case "5x5 Gaussian":
          String inputSigma = texSigma.getText();
          double sigma = 0;

          if(inputSigma.isEmpty()){
            sigma = 1f;
          } else{
            sigma = Double.parseDouble(inputSigma);
          }
        
          int kernelSize = 5;

          double[][] kernel = getGaussianKernel(sigma, kernelSize);
        
				  break;
		}
		
	}


	/*
		Function to calculate a normalized gaussian kernel of a given size

		@param	sigma  			the standard deviation for gaussian
		@param	kernelSize		the side length of the kernel

		@return 				the normalized gaussian kernel
	*/
	public double[][] getGaussianKernel(double sigma, int kernelSize){
		double[][] kernel = new double[kernelSize][kernelSize];

		double euler = 1f / (2 * Math.PI * sigma * sigma);
		int kernelRadius = kernelSize / 2;
		double exponent = 0;
		double total = 0;

		// Calculating kernel values and the normalizing factor
		for(int y = -kernelRadius; y <= kernelRadius; y++){
			for(int x = -kernelRadius; x <= kernelRadius; x++){
				exponent = ((y * y) + (x * x)) / (2 * sigma * sigma);

				kernel[y + kernelRadius][x + kernelRadius] = 
					euler * Math.exp(exponent);

				total += kernel[y + kernelRadius][x + kernelRadius];
			}
		}

		// Normalizing the kernel
		for(int y = 0; y < kernel.length; y++){
			for(int x = 0; x < kernel[0].length; x++){
				kernel[y][x] /= total;
			}
		}
		return kernel;
	}


	public static void main(String[] args) {
		new SmoothingFilter(args.length==1 ? args[0] : "baboon.png");
	}
}
