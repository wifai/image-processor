package ga.wiwit.imageprocessor;

/**
 * Created by asus on 16/09/2016.
 */
public class HistogramSpecification {
    private final int N = 256;
    private int[] hx;
    private int[] Hx;
    private int[] hz;
    private int[] Hz;
    private int[] lookUp;

    public HistogramSpecification() {
        this.hx = new int[N];
        this.Hx = new int[N];
        this.hz = new int[N];
        this.Hz = new int[N];
        this.lookUp = new int[N];
    }
    public HistogramSpecification(int[] tx, int[] tz) {
        this.hx = new int[N];
        this.Hx = new int[N];
        this.hz = new int[N];
        this.Hz = new int[N];
        this.lookUp = new int[N];
        int sum = 0;
        for (int i = 0; i < N; i++) {
            this.hx[i] = tx[i];
            sum += tx[i];
            this.Hx[i] = sum;
        }
        sum = 0;
        for (int i = 0; i < N; i++) {
            this.hz[i] = tz[i];
            sum += tz[i];
            this.Hz[i] = sum;
        }
    }
    int[] getLookUp() {
        int j = 0;
        long totX = Hx[N-1], totZ = Hz[N-1];
        for (int i = 0; i < N; i++) {
            if (totZ * Hx[i] <= totX * Hz[j]) lookUp[i] = j;
            else {
                while (totZ * Hx[i] > totX * Hz[j]) j++;
                if (totX * Hz[j] - totZ * Hx[i] > totZ * Hx[i] - totX * Hz[j-1]) lookUp[i] = j-1;
                else lookUp[i] = j;
            }
        }
        return this.lookUp;
    }
}
