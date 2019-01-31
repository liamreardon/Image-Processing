// HSL pixel representation
// Written by: Eric Roy Elli & Liam Reardon

import java.awt.Color;

public class HSL {
    public double hue;
    public double saturation;
    public int lightness;

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

        double lightness = (max + min) / 2.0;
        double delta = max - min;

        // Saturation calculations
        if(min == max){ this.saturation = 0; }
        else if(lightness <= 0.5){ this.saturation = delta / (2 * lightness); }
        else { this.saturation = delta / (2 - 2 * lightness); }

        // Hue calculations
        if(min == max){ this.hue = 0; }
        else if(max == r){ this.hue = (60 * ((g - b) / delta) + 360) % 360; }
        else if(max == g){ this.hue = (60 * ((b - r) / delta) + 120) % 360; }
        else if(max == b){ this.hue = (60 * ((r - g) / delta) + 240) % 360; }

        this.lightness = (int) Math.round(lightness * 255);
    }


    // Function to convert from hsl to rgb
    public Color hsl2rgb(){
        float h = (float) this.hue % 360;
        float s = (float) this.saturation;
        float l = (float) this.lightness / 255;
        h /= 360d;

        float q = 0;

        if(l < 0.5){
            q = l * (l + s);
        }else{
            q = (l + s) - (l * s);
        }

        float p = 2 * l - q;

        float r = Math.max(0, HueToRGB(p, q, h + (1.0f / 3.0f)));
        float g = Math.max(0, HueToRGB(p, q, h));
        float b = Math.max(0, HueToRGB(p, q, h - (1.0f / 3.0f)));

        return new Color(r, g, b);
    }

    private static float HueToRGB(float p, float q, float h) {
        if (h < 0){
            h += 1;
        }

        if (h > 1){
            h -= 1;
        }

        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }

        if (2 * h < 1) {
            return q;
        }

        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f/ 3.0f) - h));
        }

        return p;
    }
}