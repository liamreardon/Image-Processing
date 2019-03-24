import java.awt.Color;

public class DerivativeCalculus {

	
	private int[] valx, valy;
	private int width, height;
	private ImageCanvas source;

	
	public DerivativeCalculus(int width, int height, ImageCanvas source) {
		this.width = width;
		this.height = height;
		this.source = source;
		valx = new int[width * height];
		valy = new int[width * height];
	}
	
	public ImageCanvas process() {
		ImageCanvas target = new ImageCanvas(width, height);
		int l, t, r, b, dx, dy;
		Color clr1, clr2;
		int gray1, gray2;

		for (int q = 0; q < height; q++) {
			t = q == 0 ? q : q - 1;
			b = q == height - 1 ? q : q + 1;
			for (int p = 0; p < width; p++) {
				l = p == 0 ? p : p - 1;
				r = p == width - 1 ? p : p + 1;
				
				clr1 = new Color(source.image.getRGB(l, q));
				clr2 = new Color(source.image.getRGB(r, q));
				gray1 = clr1.getRed() + clr1.getGreen() + clr1.getBlue();
				gray2 = clr2.getRed() + clr2.getGreen() + clr2.getBlue();
				dx = (gray2 - gray1) / 3;
				
				clr1 = new Color(source.image.getRGB(p, t));
				clr2 = new Color(source.image.getRGB(p, b));
				gray1 = clr1.getRed() + clr1.getGreen() + clr1.getBlue();
				gray2 = clr2.getRed() + clr2.getGreen() + clr2.getBlue();
				dy = (gray2 - gray1) / 3;
				
				dx = (Math.max(-128, Math.min(dx, 127)));
				dy = (Math.max(-128, Math.min(dy, 127)));

				valx[p * width + q] = dx;
				valy[p * width + q] = dy;
						
				target.image.setRGB(p, q, new Color(dx + 128, dy + 128, 128).getRGB());
			}
		}
		
		return target;

	}
	
	public int[] getValx() {
		return valx;
	}
	
	public int[] getValy() {
		return valy;
	}

	
	
	
}
