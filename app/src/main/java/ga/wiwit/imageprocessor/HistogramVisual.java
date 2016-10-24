package ga.wiwit.imageprocessor;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Wiwit Rifa'i on 16/09/2016.
 */
public class HistogramVisual {
    private final int N = 256;
    private Boolean changed;
    private int width, height;
    private int left, right, up;
    private int[] hx, hz, horigin;
    public HistogramVisual() {
        this.hx = new int[N];
        this.hz = new int[N];
        this.horigin = new int[N];
        this.width = this.N;
        this.height = this.N;
        left = 0;
        up = 0;
        right = 0;
        changed = false;
    }
    public HistogramVisual(int[] hx) {
        this.hx = new int[N];
        this.hz = new int[N];
        this.horigin = new int[N];
        this.width = this.N;
        this.height = this.N;
        for (int i = 0; i < N; i++) {
            this.hx[i] = hx[i];
            this.horigin[i] = hx[i];
        }
        left = 0;
        up = 0;
        right = 0;
        changed = false;
    }
    public void generate() {
        int y = height - left - 1;
        for (int i = 0; i < up; i++) {
            hz[i] = left + y * i / up;
        }
        y = height - right - 1;
        int x = width - up;
        for (int i = up; i < width; i++) {
            hz[i] = right + y * (width-i-1) / x;
        }
    }
    public Bitmap getBitmap() {
        generate();
        int mul = 4;
        Bitmap bitmap = Bitmap.createBitmap(this.width * mul, this.height, Bitmap.Config.ARGB_8888);
        int maxhx = 1;
        for (int i = 0; i < N; i++)
            if (maxhx < hx[i])
                maxhx = hx[i];
        for (int i = 0; i < N; i++) {
            int hh = (int)1L * hx[i] * height / maxhx;
            for (int j = 0; j < hh; j++) {
                for (int k = 0; k < mul; k++)
                    bitmap.setPixel(i * mul + k, height-j-1, Color.BLACK);
            }
            if (changed) {
                for (int k = 0; k < mul; k++)
                    bitmap.setPixel(i * mul + k, height - hz[i] - 1, Color.RED);
            }
        }
        return bitmap;
    }
    public void calculateActualHistogram(int[] lookUp) {
        for (int i = 0; i < N; i++)
            this.hx[i] = 0;
        for (int i = 0; i < N; i++) {
            this.hx[lookUp[i]] += this.horigin[i];
        }
        changed = true;
    }
    public void setUp(int up) {
        this.up = up;
        if (this.up < 0) this.up = 0;
        if (this.up >= this.width) this.up = this.width - 1;
    }
    public void setLeft(int left) {
        this.left = left;
        if (this.left < 0) this.left = 0;
        if (this.left >= this.height) this.left = this.height - 1;
    }
    public void setRight(int right) {
        this.right = right;
        if (this.right < 0) this.right = 0;
        if (this.right >= this.height) this.right = this.height - 1;
    }
    public void setOriginHistogram(int[] origin) {
        for (int i = 0; i < this.N; i++) {
            this.horigin[i] = origin[i];
            this.hx[i] = origin[i];
        }
        left = 0;
        up = 0;
        right = 0;
        changed = false;
    }
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
    public int[] getExpectedHistogram() {
        return this.hz;
    }
    public int[] getActualHistogram() { return this.hx; }
    public int[] getOriginHistogram() {
        return this.horigin;
    }
}
