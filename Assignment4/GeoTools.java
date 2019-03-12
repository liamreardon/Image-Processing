public class GeoTools {
    static boolean inField(int x, int y, int w, int h) {
        return 0 <= x && x < w && 0 <= y && y < h;
    }
}