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

	int KERNEL_SIZE = 5;
	double SIGMA = 1;
	private boolean isColorImage;
	private static final int BLACK = (0 << 16) | (0 << 8) | 0;
	private static final int WHITE = (255 << 16) | (255 << 8) | 255;
	private static final int DEFAULT_MANUAL_THRESHOLD = 128;

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
		isColorImage = Misc.isColorImage(source.image);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {

		// define types of output images
		ImageCanvas derivatedImage, cornerResponsedImage, thresholdedImage, nonMaxedImage;

		int[] thresholdArr = new int[3];
		thresholdArr[0] = thresholdArr[1] = thresholdArr[2] = DEFAULT_MANUAL_THRESHOLD;

		// derivative class
		DerivativeCalculus derivative = new DerivativeCalculus(width, height, source);
		derivatedImage = derivative.process();

		// corner response class
		CornerResponse	cr = new CornerResponse(width, height, source, sensitivity);
		cr.init(derivative.getValx(), derivative.getValy());
		cornerResponsedImage = cr.process();

		// Threshold class
		ThresholdCalculus tc = new ThresholdCalculus(width, height, threshold);
		tc.init(cr.getHcr());
		thresholdedImage = tc.process();

		// generate Moravec corner detection result
		if ( ((Button)e.getSource()).getLabel().equals("Derivatives") )
			DoG();
    
		if (((Button) e.getSource()).getLabel().equals("Corner Response")) {
			target.image.setData(cornerResponsedImage.image.getData());
			target.repaint();
		}
		if (((Button) e.getSource()).getLabel().equals("Thresholding")) {
			if (isColorImage) {
				for (int i = 0; i < 3; i++) {
					thresholdArr[i] = automaticThreshold(source.image, i);
				}
			}
			else {
				thresholdArr[0] = thresholdArr[1] = thresholdArr[2] = automaticThreshold(source.image, 0);
			}

			showFilter(thresholdArr);

		}
	}
	public static void main(String[] args) {
		new CornerDetection(args.length==1 ? args[0] : "signal_hill.png");
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

	public void DoG() {
		int red = 0;
		int green = 0;
		int blue = 0;

		int[][] convolution = new int[height][width];
		double[] kernel = get1dKernel(KERNEL_SIZE, 1);
		int kernelRadius = KERNEL_SIZE / 2;
		System.out.println(Arrays.toString(kernel));

		// Applying the derivative of gaussian horizontally
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				for(int offset = -kernelRadius; offset < kernelRadius; offset++){
					int curX = x + offset;
					curX = (curX < 0 ? 0 : curX >= width ? (width - 1) : curX);
					Color color = new Color(source.image.getRGB(curX, y));
					red = (int) Math.round(red + color.getRed() * kernel[offset + kernelRadius]);
					green = (int) Math.round(green + color.getGreen() * kernel[offset + kernelRadius]);
					blue = (int) Math.round(blue + color.getBlue() * kernel[offset + kernelRadius]);
				}

				int pixel = (red << 16) | (green << 8) | (blue);
				target.image.setRGB(x, y, pixel);
			}
		}

		// Applying the derivative of gaussiain vertically
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				for(int offset = -kernelRadius; offset < kernelRadius; offset++){
					int curY = y + offset;
					curY = (curY < 0 ? 0 : curY >= width ? (width - 1) : curY);
					Color color = new Color(source.image.getRGB(x, curY));
					red = (int) Math.round(red + color.getRed() * kernel[offset + kernelRadius]);
					green = (int) Math.round(green + color.getGreen() * kernel[offset + kernelRadius]);
					blue = (int) Math.round(blue + color.getBlue() * kernel[offset + kernelRadius]);
				}

				int pixel = (red << 16) | (green << 8) | (blue);
				target.image.setRGB(x, y, pixel);
			}
		}

		target.repaint();

	}

	public double[] get1dKernel(int kernelSize, double sigma) {
		double[] kernel = new double[kernelSize];
		int kernelRadius = kernelSize / 2;
		double constant = 1d / (Math.sqrt(2 * Math.PI) * sigma * sigma * sigma);
		double total = 0;

		// Calculating kernel values and the normalizing factor
		for(int x = -kernelRadius; x <= kernelRadius; x++){
			constant = -((double) x * constant);
			double exponent = ((double) -(x * x)) / (2 * sigma * sigma);
			System.out.println(exponent);
			kernel[x + kernelRadius] =  constant * Math.exp(exponent);

			// This value will be used later to normalize the kernel
			total += kernel[x + kernelRadius];
		}

		// Normalizing the kernel
		for(int x = 0; x < kernel.length; x++){
			kernel[x] /= total;
		}

		return kernel;
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
                target.setRGB(i, j, BLACK);
            } else {
                target.setRGB(i, j, WHITE);
            }
        }
	}


}
