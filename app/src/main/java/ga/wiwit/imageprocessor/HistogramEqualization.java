package ga.wiwit.imageprocessor;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Wiwit Rifa'i on 07/09/2016.
 */
public class HistogramEqualization {
    private final int PIXEL_SIZE = 256;
    private Bitmap bitmap;
    private int[] greyScaleHistogram;
    private int[] lookUpTable;
    public HistogramEqualization(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap apply() {
        int h = this.bitmap.getHeight();
        int w = this.bitmap.getWidth();
        greyScaleHistogram = new int[PIXEL_SIZE];
        lookUpTable = new int[PIXEL_SIZE];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                int mean = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) + 1)/3;
                greyScaleHistogram[mean]++;
            }
        }
        int cumulativeCount = 0;
        int bmsize = w * h;
        for (int i = 0; i < PIXEL_SIZE; i++) {
            cumulativeCount += greyScaleHistogram[i];
            int pixel = (cumulativeCount * (PIXEL_SIZE - 1)) / bmsize;
            lookUpTable[i] = 0x010101 * pixel;
        }
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                int mean = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) + 1)/3;
                this.bitmap.setPixel(i, j, lookUpTable[mean]);
            }
        }
        return this.bitmap;
    }
}
