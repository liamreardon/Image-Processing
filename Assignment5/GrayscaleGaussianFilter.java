/*
    This class performs gaussian smoothing on a matrix that represents a
    grayscale image.
*/
import java.awt.image.BufferedImage;

public class GrayscaleGaussianFilter{
    private int[][] image;
    private int[][] smoothed;
    private double sigma;
    private double[] kernel;
    private int kernelSize;
    private int kernelRadius;


    public GrayscaleGaussianFilter(int[][] grayscale, int kernelSize, double sigma){
        this.image = grayscale;
        this.kernelSize = kernelSize;
        this.sigma = sigma;
        this.kernelRadius = kernelSize / 2;
        this.kernel = get1dKernel(kernelSize, sigma);
        this.smoothed = applyGaussianFilter(this.image, this.kernel);
    }


    /*
        Returns a 1D gaussian filter kernel

        @param kernelSize The length of the kernel
        @param sigma the standard deviation of the normal curve
        @return double[] the 1D gaussian filter kernel
    */
    private double[] get1dKernel(int kernelSize, double sigma){
        double[] kernel = new double[kernelSize];
        double constant = 1d / (Math.sqrt(2 * Math.PI) * sigma);
        int kernelRadius = kernelSize / 2;
        double total = 0;

        // Calculating kernel values and the normalizing factor
        for(int x = -kernelRadius; x <= kernelRadius; x++){
            double exponent = -(x * x) / (2 * sigma * sigma);
            kernel[x + kernelRadius] = 
                constant * Math.exp(exponent);

            //This value will be used later the normalize the kernel
            total += kernel[x + kernelRadius];
        }

        // Normalizing the kernel
        for(int x = 0; x < kernel.length; x++){
            kernel[x] /= total;
        }

        return kernel;
    }


    /*
        Function to apply the gaussian filter to a 3 channel grayscale image
        @param imageMatrix the matrix representation of a 3 channel grayscale image
        @param kernel the gaussian kernel
        @return the matrix representation of the imageMatrix with the
                gaussian filter applied
    */
    private int[][] applyGaussianFilter(int[][] imageMatrix, double[] kernel){
        // System.out.println(Arrays.deepToString(imageMatrix));
        int height = imageMatrix.length;
        int width = imageMatrix[0].length;
        int kernelRadius = kernel.length / 2;
        int[][] smoothed = new int[height][width];

        // Using the separable property of the gaussian filter to apply the 
        // filter horizontally
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int color = 0;
                for(int offset = -kernelRadius; offset <= kernelRadius; offset++){
                    int curX = x + offset;
                    curX = curX < 0 ? 0 : curX >= width ? (width - 1) : curX;
                    color += (int) Math.round(((imageMatrix[y][curX] >> 16) & 0xFF) * kernel[offset + kernelRadius]);
                }
                smoothed[y][x] = (color << 16) | (color << 8) | color;
            }
        }

        // Applying the gaussian filter vertically
        for(int x = 0; x < width; x++){
            for(int y = 0; y< height; y++){
                int color = 0;
                for(int offset = -kernelRadius; offset <= kernelRadius; offset++){
                    int curY = y + offset;
                    curY = curY < 0 ? 0 : curY >= height ? (height - 1) : curY;
                    color += (int) Math.round(((imageMatrix[curY][x] >> 16) & 0xFF) * kernel[offset + kernelRadius]);
                }
                smoothed[y][x] = (color << 16) | (color << 8) | color;
            }
        }

        return smoothed;
    }

    public int[][] getSmoothedImage(){
        return smoothed;
    }
}