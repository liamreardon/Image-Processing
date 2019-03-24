import java.awt.image.BufferedImage;

public class CornerResponse {
	
	private int width, height;
	private double k;
	private ImageCanvas source;

	private int[] diffx;
	private int[] diffy;
	private int[] diffxy;
	private double[] hcr;
	
	private int[] valx, valy;
	
	public CornerResponse(int width, int height, ImageCanvas source, double k) {
		this.width = width;
		this.height = height;
		this.k = k;
		this.source = source;
		hcr = new double[width * height];
		diffx = new int[width*height];
		diffy = new int[width*height];
		diffxy = new int[width*height];
	}	
	
	public void init(int[] valx, int[] valy) {
		this.valx = valx;
		this.valy = valy;
	}
	
	public ImageCanvas process() {
		ImageTools imageTools = new ImageTools(width, height);
		ImageCanvas target = new ImageCanvas(width, height);

		double A, B, C;
		int[] output = new int[width*height];
		double cornerVal = 0;
		
		
		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				diffx[x + (y * width)] = valx[x + (y * width)] * valx[x + (y * width)];
				diffy[x + (y * width)] = valy[x + (y * width)] * valy[x + (y * width)];
				diffxy[x + (y * width)] = valx[x + (y * width)] * valy[x + (y * width)];
			}
		}
		
		gaussianCalculate(); //Make Gaussian Filter

		for(int x = 0; x < width; x++) {
			for(int y = 0 ; y < height; y++) {
				A = (diffx[y * width + x]);
				B = (diffy[y * width + x]);
				C = (diffxy[y * width + x]);
				cornerVal = ((A * B - (C * C)) - (k*((A + B)*(A + B))));
				hcr[y * width + x] = cornerVal;
				
				output[y * width + x] = 0xff000000 | ((int)(hcr[y * width + x]) << 16 | (int)(hcr[y * width + x]) << 8 | (int)(hcr[y * width + x]));
			}
		}
		BufferedImage image = imageTools.creatingImage(output);
		target.image.setData(image.getRaster());
		return target;
				
		
	}
	
	public void gaussianCalculate() {
		// initialize diff values

		
		GaussianFilter gf = new GaussianFilter();
		gf.init(diffx, 2, 12, width, height);
		gf.generateTemplate();
		diffx = gf.process();
		
		gf.init(diffy, 2, 12, width, height);
		gf.generateTemplate();
		diffy = gf.process();
		
		gf.init(diffxy, 2, 12, width, height);
		gf.generateTemplate();
		diffxy = gf.process();
	}

	public double[] getHcr() {
		return hcr;
	}
	
	public int[] getDiffx() {
		return diffx;
	}
	
	public int[] getDiffy() {
		return diffy;
	}
	
	public void grayscaleSource(int width, int height, ImageCanvas source) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int p = source.image.getRGB(x, y);

				int a = (p >> 24) & 0xff;
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;

				// calculate average
				int avg = (r + g + b) / 3;

				// replace RGB value with avg
				p = (a << 24) | (avg << 16) | (avg << 8) | avg;

				source.image.setRGB(x, y, p);
			}
		}
	}
}
