import java.awt.image.BufferedImage;

public class ImageTools {

	int width, height;
	
	public ImageTools(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
    public BufferedImage creatingImage(int[] pixels){
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.SCALE_DEFAULT);
        int ctr=0;
      
        for(int i=0; i<height; i++){
           for(int j=0; j<width; j++){               
               newImage.setRGB(i, j, pixels[ctr]);
               ctr++;
            } 
       }                
              
       return newImage;
    }
}
