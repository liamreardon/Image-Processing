// HSL pixel representation
// Written by: Eric Roy Elli & Liam Reardon
public class HSL {
    public double hue;
    public double saturation;
    public double lightness;

    // Constructor
    // RGB values expected in the range [0, 255]
    public HSL(int r, int g, int b){
        rgb2hsl(r, g, b);
    }

    // Private function to convert from rgb to hsl
    private void rgb2hsl(int red, int green, int blue){

        // Normalizing RGB values
        double r = (double) red / 255;
        double g = (double) green / 255;
        double b = (double) blue / 255;

        double max = Math.max(Math.max(r, g), b);
        double min = Math.min(Math.min(r, g), b);

        this.lightness = (max + min) / 2.0;
        double delta = max - min;

        // Saturation calculations
        if(min == max){ this.saturation = 0; }
        else if(this.lightness <= 0.5){ this.saturation = delta / (2 * this.lightness); }
        else { this.saturation = delta / (2 - 2 * this.lightness); }

        // Hue calculations
        if(min == max){ this.hue = 0; }
        else if(max == r){ this.hue = (60 * ((g - b) / delta) + 360) % 360; }
        else if(max == g){ this.hue = (60 * ((b - r) / delta) + 120) % 360; }
        else if(max == b){ this.hue = (60 * ((r - g) / delta) + 240) % 360; }

        this.lightness = lightness;
    }
}