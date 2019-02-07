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
		int kernelSize = 5;
		int kernelRadius = kernelSize / 2;

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
			
				//int inputImage[][] = new int[height][width];

				int red = 0;
				int blue = 0;
				int green = 0;

				// for (int i = 0; i < height; i++) {
				// 	for (int j = 0; j < width; j++) {
				// 		inputImage[i][j] = input.getRGB(j, i);
				// 	}
				// }

				for (int u = 0; u < height; u++) {
					for (int v = 0; v < width; v++) {
			             
						for (int j = -2; j <= 2; j++) {
                            for (int i = -2; i <= 2; i++) {
                                

                                int cHeight = u + j;
                                int cWidth = v + i;

                                // System.out.println(cHeight);
                                // System.out.println(cWidth);

                                if(cHeight < 0) {
									cHeight = 0;
								}

								if (cWidth < 0) {
									cWidth = 0;
								}

								if (cHeight >= height) {
									cHeight = height - 1;
								}

								if(cWidth >= width) {
									cWidth = width - 1;
								}

                            
								Color nClr = new Color(source.image.getRGB(cWidth, cHeight));
                                red += nClr.getRed();
                                green += nClr.getGreen();
                                blue += nClr.getBlue();

                                
                            }
                        }
                        
                        red = (int) Math.round(red / 25); 
                        green = (int) Math.round(green / 25); 
                        blue = (int) Math.round(blue / 25);
                        int newPixel = (red << 16) | (green << 8) | blue;

                        target.image.setRGB(v, u, newPixel);

					}
				}

				target.repaint();


				break;
        
	        case "5x5 Gaussian":
				double[][] kernel = getGaussianKernel(kernelSize);

				// iy - y-coordinate of pixel in the image
				// oy - offset in the x direction
				// cy - y-coordinate of the current pixel the kernel is referring to
				for(int iy = 0; iy < height; iy++){
					for(int ix = 0; ix < width; ix++){
						red = 0;
						green = 0;
						blue = 0;

						for(int oy = -kernelRadius; oy <= kernelRadius; oy++){
							for(int ox = -kernelRadius; ox <= kernelRadius; ox++){
								int cy = oy + iy;
								int cx = ox + ix;
								if(cx < 0){ cx = 0; }
								if(cy < 0){ cy = 0; }
								if(cx >= width) { cx = width - 1; }
								if(cy >= height) { cy = height -1; }

								Color color = new Color(source.image.getRGB(cx, cy));

								red = (int) Math.round(red + color.getRed() * kernel[oy + kernelRadius][ox + kernelRadius]);
								green = (int) Math.round(green + color.getGreen() * kernel[oy + kernelRadius][ox + kernelRadius]);
								blue = (int) Math.round(blue + color.getBlue() * kernel[oy + kernelRadius][ox + kernelRadius]);
							}
						}

						int pixel = (red << 16) | (green << 8) | blue;
						target.image.setRGB(ix, iy, pixel);
					}
				}

				target.repaint();
				break;
        
			case "5x5 median":
				// Image Loop
				for(int iy = 0; iy < height; iy++)	{
					for(int ix = 0; ix < width; ix++){
						java.util.List<Integer> pixels_r= new ArrayList<Integer>();
						java.util.List<Integer> pixels_g= new ArrayList<Integer>();
						java.util.List<Integer> pixels_b= new ArrayList<Integer>();

						// Kernel Loop
						for(int oy = -kernelRadius; oy <= kernelRadius; oy++){
							for(int ox = - kernelRadius; ox <= kernelRadius; ox++){
								int cy = oy + iy;
								int cx = ox + ix;

								cx = cx < 0 ? 0 : cx >= width ? (width - 1) : cx;
								cy = cy < 0 ? 0 : cy >= width ? (width - 1) : cy;

								Color color = new Color(source.image.getRGB(cx, cy));

								pixels_r.add(color.getRed());
								pixels_g.add(color.getGreen());
								pixels_b.add(color.getBlue());
							}

						}

						Collections.sort(pixels_r);
						Collections.sort(pixels_g);
						Collections.sort(pixels_b);

						red = pixels_r.get(pixels_r.size() / 2);
						green = pixels_g.get(pixels_g.size() / 2);
						blue = pixels_b.get(pixels_b.size() / 2);

						int pixel = (red << 16) | (green << 8) | blue;

						target.image.setRGB(ix, iy, pixel);
					}
				}

				target.repaint();
				break;

			case "5x5 Kuwahara":

				int windowSize = 5;
                int size2 = (windowSize + 1) / 2;
                int offset = (windowSize - 1)/ 2;
                int width2 = width + offset;
                int height2 = height + offset;
                float[][][] mean = new float[width2][height2][3];
                float[][][] variance = new float[width2][height2][3];
                double sumR, sum2R;
                double sumG, sum2G;
                double sumB, sum2B;
                int n, r, g, b, xbase, ybase;
                for (int y1 = 0 - offset; y1 < 0 + height; y1++) {
                    for (int x1 = 0 - offset; x1 < 0 + width; x1++) {
                        sumR = sumG = sumB = 0;
                        sum2R = sum2G = sum2B = 0;
                        n = 0;
                        for (int x2 = x1; x2 < x1 + size2; x2++) {
                            for (int y2 = y1; y2 < y1+ size2; y2++) {
                                if(x2 > 0 && x2 < width && y2 > 0 && y2 < height){
                                    Color clr = new Color(source.image.getRGB(y2, x2));
                                    r = clr.getRed();
                                    g = clr.getGreen();
                                    b = clr.getBlue();

                                    sumR += r;
                                    sum2R += r*r;

                                    sumG += g;
                                    sum2G += g*g;

                                    sumB += b;
                                    sum2B += b*b;

                                    n++;
                                }
                                else{
                                    n++;
                                }
                            }
                        }
                        mean[x1+offset][y1+offset][0] = (float)(sumR/n);
                        mean[x1+offset][y1+offset][1] = (float)(sumG/n);
                        mean[x1+offset][y1+offset][2] = (float)(sumB/n);

                        variance[x1+offset][y1+offset][0] = (float)(sum2R-sumR*sumR/n);
                        variance[x1+offset][y1+offset][1] = (float)(sum2G-sumG*sumG/n);
                        variance[x1+offset][y1+offset][2] = (float)(sum2B-sumB*sumB/n);
                    }
                }

                int xbase2 = 0, ybase2 = 0;
                float var, min;
                for (int y1 = 0; y1 < 0 + height; y1++) {
                    for (int x1 = 0; x1 < 0 + width; x1++) {

                        //Red channel
                        
                        min = Float.MAX_VALUE;
                        xbase = x1; ybase = y1;
                        var = variance[xbase][ybase][0];
                        if (var < min) {
                            min = var; 
                            xbase2 = xbase; 
                            ybase2 = ybase;
                        }
                        xbase = x1 + offset;

                        var = variance[xbase][ybase][0];
                        if (var < min) {
                            min = var; 
                            xbase2 = xbase; 
                            ybase2 = ybase;
                        }
                        ybase = y1 + offset;

                        var = variance[xbase][ybase][0];
                        if (var < min) {
                            min = var; 
                            xbase2 = xbase; 
                            ybase2 = ybase;
                        }
                        xbase = x1; 

                        var = variance[xbase][ybase][0];
                        if (var < min) {
                            min = var; 
                            xbase2 = xbase; 
                            ybase2 = ybase;
                        }

                        r = (int)(mean[xbase2][ybase2][0]+0.5);

                        //Green channel

                        min = Float.MAX_VALUE;
                        xbase = x1; ybase=y1;
                        var = variance[xbase][ybase][1];
                        if (var < min) {
                            min = var; 
                            xbase2 = xbase; 
                            ybase2 = ybase;
                        }
                        xbase = x1+offset;
                        var = variance[xbase][ybase][1];
                        if (var < min) {
                            min= var; 
                            xbase2=xbase; 
                            ybase2=ybase;
                        }
                        ybase = y1 + offset;

                        var = variance[xbase][ybase][1];
                        if (var < min) {
                            min = var; 
                            xbase2 = xbase; 
                            ybase2 = ybase;
                        }
                        xbase = x1; 

                        var = variance[xbase][ybase][1];
                        if (var < min) {
                            min = var;
                            xbase2 = xbase; 
                            ybase2 = ybase;}

                        g = (int)(mean[xbase2][ybase2][1]+0.5);

                        //Blue channel

                        min = Float.MAX_VALUE;
                        xbase = x1; ybase = y1;
                        var = variance[xbase][ybase][2];
                        if (var < min) {
                            min = var;
                            xbase2 = xbase; 
                            ybase2 = ybase;
                        }

                        xbase = x1 + offset;
                        var = variance[xbase][ybase][2];
                        if (var < min) {
                            min = var;
                            xbase2 = xbase;
                            ybase2 = ybase;
                        }
                        ybase = y1 + offset;

                        var = variance[xbase][ybase][2];
                        if (var < min) {
                            min = var; 
                            xbase2 = xbase; 
                            ybase2 = ybase;
                        }
                        xbase = x1; 

                        var = variance[xbase][ybase][2];
                        if (var < min) {
                            min = var; 
                            xbase2 = xbase; 
                            ybase2 = ybase;
                        }

                        b = (int)(mean[xbase2][ybase2][2]+0.5);
                        
                        int newPixel = 0xff000000 | (r << 16) | (g << 8)| b;

                        target.image.setRGB(y1, x1, newPixel);



                    }
                }

                target.repaint();
                break;

		}



	}


	/*
		Function to calculate a normalized gaussian kernel of a given size

		@param	kernelSize		the side length of the kernel

		@return 				the normalized gaussian kernel
	*/
	public double[][] getGaussianKernel(int kernelSize){
		String inputSigma = texSigma.getText();
		double sigma = 0;

		if(texSigma.getText().isEmpty()){
			sigma = 1;
		}else{
			sigma = Double.parseDouble(texSigma.getText());
		}

		double[][] kernel = new double[kernelSize][kernelSize];

		double euler = 1d / (2 * Math.PI * sigma * sigma);
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
