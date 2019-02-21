import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Misc utils
 */
public class Misc {

    public static double groupSum(int[] histogram, int from, int to){
        return Arrays.stream(histogram).limit(to).skip(from).sum();
    }

    public static double mean(int[] histogram, int from, int to){
        return IntStream.range(from, to).map(i -> i*histogram[i]).sum() /
                groupSum(histogram, from, to);
    }

    public static double variance(int[] histogram, int from, int to){
        double mean = mean(histogram, from, to);
        return IntStream.range(from, to)
                .mapToDouble(i -> Math.pow((mean - i), 2) * histogram[i]).sum() /
                groupSum(histogram, from, to);
    }

    public static int[] buildHistogram(int[][] matrix){
        int[] histogram = new int[256];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                histogram[matrix[i][j]]++;
            }
        }
        return histogram;
    }

    public static double weight(int[] histogram, int from, int to){
        return groupSum(histogram, from, to) /
               groupSum(histogram, 0, histogram.length);
    }

    public static boolean isColorImage(BufferedImage image){
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (new Color(image.getRGB(i, j)).getRed() !=
                        new Color(image.getRGB(i, j)).getBlue() ||
                        new Color(image.getRGB(i, j)).getBlue() !=
                        new Color(image.getRGB(i, j)).getGreen()){
                    return true;
                }
            }
        }
        return false;
    }

    public static int[][] getMatrixOfImage(BufferedImage image, int color){
        int[][] matrix = new int[image.getWidth()][image.getHeight()];

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                matrix[i][j] = image.getRaster().getPixel(i, j, new int[3])[color];
            }
        }
        return matrix;
    }

    public static int getChannelFromRGB(int rgb, int channel){
        Color color = new Color(rgb);
        switch (channel){
            case 0:
               return color.getRed();
            case 1:
                return color.getGreen();
            case 2:
                return color.getBlue();
            default:
                return 0;
        }
    }
}
