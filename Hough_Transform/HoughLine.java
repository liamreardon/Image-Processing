import java.awt.image.BufferedImage;
import java.awt.Color;

public class HoughLine{
    public double m;  // the slope of the line
    public double b;  // the y-intercept of the line
    public int rho; // the perpendicular distance of the line from the origin
    public double theta;   // the angle rho makes with the positive x-axist

    // Constructor
    public HoughLine(int rho, int theta){
        this.rho = rho;
        this.theta = Math.toRadians((double) theta);
    }


    // Function to draw hough lines in the given image
    public void draw(BufferedImage image, int color){
        int height = image.getHeight();
        int width = image.getWidth();

        int rhoMax = (int) Math.sqrt(width * width + height * height);

        // Vertical lines [11pi / 4, pi / 4] U [3pi / 4, 5pi / 4]
        if((this.theta >=  0 && this.theta <= Math.PI * 0.25) || (this.theta >= Math.PI*0.75 && this.theta <= Math.PI * 1.25) || (this.theta >= Math.PI * 1.75)){
            for(int y = 0; y < height; y++){
                // x = (rho - y*sin(theta)) / cos(theta)
                int x = (int) ((((double) this.rho) - (((double) y) * Math.sin(this.theta))) / Math.cos(this.theta));
                if(x >= 0 && x < width){
                    image.setRGB(x, y, color);
                }
            }
        }
        else{
            for(int x = 0; x < width; x++){
                // y = (rho - x*cos(theta)) / sin(theta)
                int y = (int) ((((double) this.rho) - (((double) x) * Math.cos(this.theta))) / Math.sin(this.theta));
                if(y >= 0 && y < height){
                    image.setRGB(x, y, color);
                }
            }
        }
    }
}