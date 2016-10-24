package ga.wiwit.imageprocessor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Wiwit Rifa'i on 16/09/2016.
 */
public class ImageContainer {
    public final int N = 256;
    private Bitmap origin;
    private Bitmap bitmap;
    private int width;
    private int height;
    private boolean visit[][];

    public ImageContainer() {
        width = 256;
        height = 128;
        this.origin = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }
    public ImageContainer(Bitmap bitmap) {
        this.origin = bitmap;
        this.bitmap = origin.copy(Bitmap.Config.ARGB_8888, true);
        this.width = this.bitmap.getWidth();
        this.height = this.bitmap.getHeight();
    }
    private int grayscale(int pixel) {
        return (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) + 1)/3;
    }
    public Bitmap applyConvolutionMatrix(int n, int[][] conv, int norm) {
        int[][][] mat = new int [8][3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mat[0][i][j] = conv[i][j];
            }
        }
        for (int k = 1; k < 8; k++) {
            mat[k][0][0] = mat[k-1][0][1];
            mat[k][0][1] = mat[k-1][0][2];
            mat[k][0][2] = mat[k-1][1][2];
            mat[k][1][0] = mat[k-1][0][0];
            mat[k][1][1] = mat[k-1][1][1];
            mat[k][1][2] = mat[k-1][2][2];
            mat[k][2][0] = mat[k-1][1][0];
            mat[k][2][1] = mat[k-1][2][0];
            mat[k][2][2] = mat[k-1][2][1];
        }
        int step = (n == 8) ? 1 : (n == 2) ? 2 : 4;
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sum += conv[i][j];
            }
        }
        if (sum != 0)
            norm = sum;
        for (int i = 1; i + 1 < width; i++) {
            for (int j = 1; j + 1 < height; j++) {
                int r = 0, g = 0, b = 0;
                for (int k = 0; k < n; k++) {
                    int kk = k * step;
                    int sumr = 0, sumg = 0, sumb = 0;
                    for (int di = 0; di < 3; di++) {
                        for (int dj = 0; dj < 3; dj++) {
                            int pixel = this.bitmap.getPixel(i+di-1, j+dj-1);
                            sumr += mat[kk][di][dj] * Color.red(pixel);
                            sumg += mat[kk][di][dj] * Color.green(pixel);
                            sumb += mat[kk][di][dj] * Color.blue(pixel);
                        }
                    }
                    if (sumr > r) r = sumr;
                    if (sumg > g) g = sumg;
                    if (sumb > b) b = sumb;
                }
                if (norm > 1) {
                    r /= norm;
                    g /= norm;
                    b /= norm;
                }
                if (r < 0) r = 0;
                if (r >= N) r = N-1;
                if (g < 0) g = 0;
                if (g >= N) g = N-1;
                if (b < 0) b = 0;
                if (b >= N) b = N-1;
                result.setPixel(i, j, Color.rgb(r, g, b));
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.bitmap.setPixel(i, j, result.getPixel(i, j));
            }
        }
        return result;
    }

    public Bitmap reset() {
        this.bitmap = origin.copy(Bitmap.Config.ARGB_8888, true);
        return this.bitmap;
    }
    public Bitmap applyLookUpGrayScale(int[] lookUp) {
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < N; i++)
            lookUp[i] *= 0x010101;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                int mean = grayscale(pixel);
                pixel &= ~0xFFFFFF;
                pixel |= lookUp[mean];
                result.setPixel(i, j, pixel);
            }
        }
        return result;
    }
    public Bitmap applyLookUpRed(int[] lookUp) {
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < N; i++)
            lookUp[i] *= 0x010000;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                int mean = grayscale(pixel);
                pixel &= ~0xFF0000;
                pixel |= lookUp[mean];
                result.setPixel(i, j, pixel);
            }
        }
        return result;
    }
    public Bitmap applyLookUpGreen(int[] lookUp) {
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < N; i++)
            lookUp[i] *= 0x000100;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                int mean = grayscale(pixel);
                pixel &= ~0x00FF00;
                pixel |= lookUp[mean];
                result.setPixel(i, j, pixel);
            }
        }
        return result;
    }
    public Bitmap applyLookUpBlue(int[] lookUp) {
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < N; i++)
            lookUp[i] *= 0x000001;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                int mean = grayscale(pixel);
                pixel &= ~0x0000FF;
                pixel |= lookUp[mean];
                result.setPixel(i, j, pixel);
            }
        }
        return result;
    }
    public Bitmap applyLookUpAll(int[] lookUp) {
        for (int i = 0; i < N; i++)
            lookUp[i] -= i;
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                int r = Color.red(pixel), g = Color.green(pixel), b = Color.blue(pixel);
                int add = lookUp[(r + g + b + 1)/3];
                r += add;
                if (r >= N) r = N-1;
                if (r < 0) r = 0;
                g += add;
                if (g >= N) g = N-1;
                if (g < 0) g = 0;
                b += add;
                if (b >= N) b = N-1;
                if (b < 0) b = 0;
                result.setPixel(i, j, Color.rgb(r, g, b));
            }
        }
        return result;
    }
    public Bitmap getBitmap() {
        return this.bitmap;
    }
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
    public int[] getHistogramGrayScale() {
        int[] histogram = new int[N];
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                int mean = grayscale(pixel);
                histogram[mean]++;
            }
        }
        return histogram;
    }
    public int[] getHistogramRed() {
        int[] histogram = new int[N];
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                histogram[Color.red(pixel)]++;
            }
        }
        return histogram;
    }
    public int[] getHistogramGreen() {
        int[] histogram = new int[N];
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                histogram[Color.green(pixel)]++;
            }
        }
        return histogram;
    }
    public int[] getHistogramBlue() {
        int[] histogram = new int[N];
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                histogram[Color.blue(pixel)]++;
            }
        }
        return histogram;
    }
    public void toGreyScale() {
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                int gray = grayscale(pixel);
                this.bitmap.setPixel(i, j, Color.rgb(gray, gray, gray));
            }
        }
    }

    public Bitmap toBinary() {
        int histogram[] = this.getHistogramGrayScale();
        int sum = 0, wB = 0, wF = 0, threshold = this.N/2;
        int sumB = 0;
        double mB = 0, mF = 0, between = 0, maxv = 0;
        for (int i = 0; i < this.N; i++)
            sum += i * histogram[i];
        for (int i = 0; i < this.N; ++i) {
            wB += histogram[i];
            if (wB == 0)
                continue;
            wF = this.height * this.width - wB;
            if (wF == 0)
                break;
            sumB += i * histogram[i];
            mB = (double)sumB / wB;
            mF = (double)(sum - sumB) / wF;
            between = wB * wF * Math.pow(mB - mF, 2);
            if (between > maxv) {
                maxv = between;
                threshold = i;
            }
        }
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                int pixel = this.bitmap.getPixel(i, j);
                if (grayscale(pixel) < threshold)
                    this.bitmap.setPixel(i, j, Color.BLACK);
                else
                    this.bitmap.setPixel(i, j, Color.WHITE);
            }
        }
        return this.bitmap;
    }
    private final static int dy[] = {-1, -1, 0, 1, 1, 1, 0, -1}, dx[] = {0, 1, 1, 1, 0, -1, -1, -1};
    public void fill(int x, int y, int before, int after) {
        Queue<Point> que = new LinkedList<>();
        que.add(new Point(x, y));
        this.bitmap.setPixel(x, y, after);
        while (!que.isEmpty()) {
            Point front = que.remove();
            x = front.x;
            y = front.y;
            for (int i = 0; i < 8; i++) {
                int nx = x + dx[i], ny = y + dy[i];
                if (nx >= 0 && nx < this.width && ny >= 0 && ny < this.height) {
                    int pixel = this.bitmap.getPixel(nx, ny);
                    if (pixel == before) {
                        que.add(new Point(nx, ny));
                        this.bitmap.setPixel(nx, ny, after);
                    }
                }
            }
        }
    }
    public int count(int x, int y, int pixel) {
        Queue<Point> que = new LinkedList<>();
        que.add(new Point(x, y));
        visit[x][y] = true;
        int result = 0;
        while (!que.isEmpty()) {
            result++;
            Point front = que.remove();
            x = front.x;
            y = front.y;
            for (int i = 0; i < 8; i++) {
                int nx = x + dx[i], ny = y + dy[i];
                if (nx >= 0 && nx < this.width && ny >= 0 && ny < this.height) {
                    if (visit[nx][ny])
                        continue;
                    int npixel = this.bitmap.getPixel(nx, ny);
                    if (npixel == pixel) {
                        que.add(new Point(nx, ny));
                        visit[nx][ny] = true;
                    }
                }
            }
        }
        return result;
    }
    public void reduceNoise() {
        int threshold = this.width * this.height / 5000;
        visit = new boolean[this.width][this.height];
        for (int  i = 0; i < this.width; i++)
            for (int j = 0; j < this.height; j++)
                visit[i][j] = false;
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) if (!visit[i][j]) {
                int pixel = this.bitmap.getPixel(i, j);
                if (pixel == Color.BLACK)
                    continue;
                int cnt = count(i, j, pixel);
                if (cnt < threshold)
                    fill(i, j, pixel, pixel == Color.BLACK ? Color.WHITE : Color.BLACK);
            }
        }
    }


    public void zhangSuen() {
        List<Point> toWhite = new ArrayList<>();

        boolean firstStep = false;
        boolean hasChanged;

        do {
            hasChanged = false;
            firstStep = !firstStep;

            for (int x = 1; x < this.width - 1; x++) {
                for (int y = 1; y < this.height - 1; y++) {

                    if (this.bitmap.getPixel(x, y) != Color.WHITE)
                        continue;

                    int nn = numNeighbors(x, y);
                    if (nn < 2 || nn > 6)
                        continue;

                    if (numTransitions(x, y) != 1)
                        continue;

                    if (!atLeastOneIsWhite(x, y, firstStep ? 0 : 1))
                        continue;

                    toWhite.add(new Point(x, y));
                    hasChanged = true;
                }
            }

            for (Point p : toWhite)
                this.bitmap.setPixel(p.x, p.y, Color.BLACK);
            toWhite.clear();
        } while (firstStep || hasChanged);
    }
    private int numNeighbors(int x, int y) {
        int count = 0;
        for (int i = 0; i < 8; i++)
            if (this.bitmap.getPixel(x+dx[i], y+dy[i]) == Color.WHITE)
                count++;
        return count;
    }

    private int numTransitions(int x, int y) {
        int count = 0;
        for (int i = 0; i < 8; i++)
            if (this.bitmap.getPixel(x+dx[i], y+dy[i]) == Color.BLACK) {
                if (this.bitmap.getPixel(x+dx[(i+1) % 8], y+dy[(i+1) % 8]) == Color.WHITE)
                    count++;
            }
        return count;
    }

    private boolean atLeastOneIsWhite(int x, int y, int step) {
        boolean ok1, ok2;
        ok1 = ok2 = false;
        int id1 = step * 2, id2 = id1 + 2;
        for (int i = 0; i < 3; i++) {
            ok1 |= (this.bitmap.getPixel(x+dx[id1], y+dy[id1]) == Color.WHITE);
            ok2 |= (this.bitmap.getPixel(x+dx[id2], y+dy[id2]) == Color.WHITE);
            id1 = (id1 + 2) % 8;
            id2 = (id2 + 2) % 8;
        }
        return ok1 && ok2;
    }
}