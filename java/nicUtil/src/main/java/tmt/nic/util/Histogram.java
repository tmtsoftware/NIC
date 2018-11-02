package tmt.nic.util;

import static java.lang.Math.*;

/**
 * Created by chapine on 14/03/17.
 *
 * Collect basic running statistics.
 * The histogram size need not match the full extent of the data; the min, mean, and max are
 * calculated correctly even when data fall outside the bounds.
 */
public class Histogram {

    public final double d[];
    public final int len;      // number of bins
    public long n;             // number of values in histogram
    public long nTotal;        // number of values, including those outside of histogram
    private long nUpdate;      // number of times update() called (compared to warmup)
    public final double binMin;
    public final double binMax;
    public final double binStep;
    public final long warmup;  // ignore the first warmup samples in calls to update()

    private double mu;
    private double m2;
    private double min;
    private double max;

    public Histogram(double binMin, double binMax, double binStep, long warmup) {
        // Allocate histogram with bins of size binStep between binMin and binMax
        this.binMin = binMin;
        this.binMax = binMax;
        this.binStep = binStep;
        this.warmup = warmup;
        len = (int) ceil((binMax-binMin)/binStep);
        d = new double[len];
        n = 0;
        nTotal = 0;
        mu = 0;
        m2 = 0;
        nUpdate=0;
    }

    public Histogram(double binMin, double binMax, double binStep) {
        this(binMin, binMax, binStep, 0);
    }

    public void update(double x[]) {
        // Add an array of values to the histogram and update running stats
        for (int i=0; i<x.length; ++i) {
            update(x[i]);
        }
    }

    public void update(double x) {
        // If we haven't gotten enough warmup samples yet, skip
        if (++nUpdate <= warmup) {
            return;
        }

        // Add a value to the histogram and update running stats
        if (nTotal == 0) {
            min = x;
            max = x;
        } else {
            if (x > max) {
                max = x;
            }
            if (x < min) {
                min = x;
            }
        }

        int bin = (int) floor(((x-binMin)/binStep));
        if ( (bin >=0) && (bin<len) ) {
            d[bin]++;
            n++;
        }

        nTotal++;
        double delta = x - mu;
        mu += delta / nTotal;
        m2 += delta * (x - mu);
    }

    public double getMin() throws Exception {
        if (nTotal == 0) {
            throw new Exception("No values added to histogram.");
        }
        return min;
    }

    public double getMax() throws Exception {
        if (nTotal == 0) {
            throw new Exception("No values added to histogram.");
        }
        return max;
    }

    public double getMean() throws Exception {
        if (nTotal == 0) {
            throw new Exception("No values added to histogram.");
        }
        return mu;
    }

    public double getVariance() throws Exception {
        if (nTotal < 2) {
            throw new Exception("Unable to calculate variance with < 2 samples.");
        }
        // Return unbiased estimate of variance (not the maximum likelihood estimator!)
        // Based on all added values (not just those that landed in histogram bounds)
        return m2 / (nTotal-1);
    }

    public double getStdev() throws Exception {
        // Return unbiased estimate of standard deviation (not the maximum likelihood estimator!)
        // Based on all added values (not just those that landed in histogram bounds)
        return sqrt(getVariance());
    }

    public String printstr() {
        String retVal;
        try {
            StringBuffer sb = new StringBuffer();

            sb.append("Min:" + getMin() + " Mean:" + getMean() + " Std:" + getStdev() + " Max:" + getMax() + " Samples:" + nTotal + "\n");
            for (int i = 0; i < len; ++i) {
                if (d[i] != 0) {
                    sb.append((binMin + i * binStep) + ": " + d[i] + "\n");
                }
            }

            retVal = sb.toString();
        } catch (Exception E) {
            retVal = "No stats available.";
        }
        return retVal;
    }
}
