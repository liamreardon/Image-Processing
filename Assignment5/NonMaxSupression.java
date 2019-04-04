import java.awt.image.BufferedImage;

public class NonMaxSupression {

     private int width;
     private int height;
     private double[] hcrt;
     private int[] valx, valy;
     private int[] diffx, diffy;

     public NonMaxSupression(int width, int height) {
         this.width = width;
         this.height = height;
     }

     public void init(double[] hcrt, int[] valx, int valy[], int[] diffx, int[] diffy) {
         this.hcrt = hcrt;
         this.valx = valx;
         this.valy = valy;
         this.diffx = diffx;
         this.diffy = diffy;
     }

     public ImageCanvas process() {
         ImageTools imageTools = new ImageTools(width, height);
         ImageCanvas target = new ImageCanvas(width, height);
         NonMax nmOp = new NonMax(hcrt, width, height);

         nmOp.init(valx, valy, diffx, diffy);
         int nm[] = nmOp.process();
         
         int[] output = new int[width*height];
         int val;

         for (int x = 0; x < width; x++) {
             for (int y = 0; y < height; y++) {
                if ((nm[y * width + x]) == 0) {
                    val = (((int)hcrt[y * width + x] & 0xff) + 255) / 2;
                }
                else {
                    val = 0;
                }

                output[y * width + x] = 0xff000000 | ((val) << 16 | (val << 8) | (val));
             }
         }

         BufferedImage image = imageTools.creatingImage(output);
         target.image.setData(image.getRaster());
         return target;

     }
}