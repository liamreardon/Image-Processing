public class NonMax {

	static double[] input;
	static int[] output;
	int width;
	int height;
	int templateSize = 3;

	int[] valx, valy;
	int[] diffx, diffy;

	public NonMax(double[] inputIn, int widthIn, int heightIn) {
		width = widthIn;
		height = heightIn;
		input = inputIn;
		output = new int[width * height];
	}

	public void init(int[] valx, int[] valy, int[] diffx, int[] diffy) {
		this.valx = valx;
		this.valy = valy;
		this.diffx = diffx;
		this.diffy = diffy;
	}

	public int[] process() {

		double mag[] = new double[width * height];

		for (int x = templateSize / 2; x < width - (templateSize / 2); x++) {
			for (int y = templateSize / 2; y < height - (templateSize / 2); y++) {
				mag[x + (y * width)] = Math.sqrt(
						(valx[y * width + x] * valx[y * width + x]) + (valy[y * width + x] * valy[y * width + x]));
			}
		}

		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				int dx, dy;

				if (diffx[x + (y * width)] > 0)
					dx = 1;
				else
					dx = -1;

				if (diffy[x + (y * width)] > 0)
					dy = 1;
				else
					dy = -1;

				double a1, a2, b1, b2, A, B, point, val;
				if (Math.abs(diffx[x + (y * width)]) > Math.abs(diffy[x + (y * width)])) {
					a1 = mag[(x + dx) + ((y) * width)];
					a2 = mag[(x + dx) + ((y - dy) * width)];
					b1 = mag[(x - dx) + ((y) * width)];
                    b2 = mag[(x - dx) + ((y + dy) * width)];
                    
					A = (Math.abs(diffx[x + (y * width)]) - Math.abs(diffy[x + (y * width)])) * a1
							+ Math.abs(diffy[x + (y * width)]) * a2;
					B = (Math.abs(diffx[x + (y * width)]) - Math.abs(diffy[x + (y * width)])) * b1
                            + Math.abs(diffy[x + (y * width)]) * b2;
                            
                    point = mag[x + (y * width)] * Math.abs(diffx[x + (y * width)]);
                    
					if (point >= A && point > B) {
						val = Math.abs(diffx[x + (y * width)]);
						output[x + (y * width)] = (int) val;
					} else {
						val = 0;
						output[x + (y * width)] = (int) val;
					}
				} else {
					a1 = mag[(x) + ((y - dy) * width)];
					a2 = mag[(x + dx) + ((y - dy) * width)];
					b1 = mag[(x) + ((y + dy) * width)];
                    b2 = mag[(x - dx) + ((y + dy) * width)];
                    
					A = (Math.abs(diffy[x + (y * width)]) - Math.abs(diffx[x + (y * width)])) * a1
							+ Math.abs(diffx[x + (y * width)]) * a2;
					B = (Math.abs(diffy[x + (y * width)]) - Math.abs(diffx[x + (y * width)])) * b1
                            + Math.abs(diffx[x + (y * width)]) * b2;
                            
					point = mag[x + (y * width)] * Math.abs(diffy[x + (y * width)]);
					
					if (point >= A && point > B) {
						// System.out.println("Setting value: " + (diffy[x + (y * width)]));
						val = Math.abs(diffy[x + (y * width)]);
						// output[x + (y * width)] = (int)val;
					} 
					else{
						val = 0;
					}
					output[x + (y * width)] = (int) val;
				}
			}
		}

		return output;
	}
}