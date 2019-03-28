import java.awt.image.BufferedImage;

public class ThresholdCalculus {

	private int width, height;
	private double[] hcr, hcrt;
	private long threshold;
	
	public ThresholdCalculus(int width, int height, int threshold) {
		this.width = width;
		this.height = height;
		this.threshold = threshold;
		hcrt = new double[width*height];
	}
	
	public void init(double[] hcr) {
		this.hcr = hcr;

	}
	
	public ImageCanvas process() {
		ImageTools imageTools = new ImageTools(width, height);
		ImageCanvas target = new ImageCanvas(width, height);
		
		int[] output = new int[width*height];
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (hcr[y * width + x] > threshold*100000000) {
					hcrt[y * width + x] = hcr[y * width + x];
				} else {
					hcrt[y * width + x] = 0;
				}
				output[y * width + x] = 0xff000000 | ((int) (hcrt[y * width + x]) << 16	| (int) (hcrt[y * width + x]) << 8 | (int) (hcrt[y * width + x]));
			}
		}
		BufferedImage image = imageTools.creatingImage(output);
		target.image.setData(image.getRaster());		
		return target;		
	}
	
	public double[] getHcrt() {
		return hcrt;
	}
}
