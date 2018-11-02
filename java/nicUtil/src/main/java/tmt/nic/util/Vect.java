package tmt.nic.util;

/**
 * Helper class for doing typical vector operations
 */
public class Vect {
    public double[] d;

    public Vect(double[] d) {
        this.d = d;
    }

    public double dot(Vect v) {
        double result=0;
        if (v.d.length != d.length) {
            throw new IllegalArgumentException("Vectors must have the same length.");
        }
        for (int i=0; i<d.length; ++i) {
            result += d[i]*v.d[i];
        }
        return result;
    }

    public boolean equals(Vect v) {
        // Test element-wise vector equality
        boolean result=true;
        if (v.d.length != d.length) {
            throw new IllegalArgumentException("Vectors must have the same length.");
        }
        for (int i=0; i<d.length; ++i) {
            if (d[i] != v.d[i]) {
                result = false;
                break;
            }
        }
        return result;
    }

    public Vect abs() {
        Vect result = new Vect(new double[d.length]);
        for (int i=0; i<d.length; ++i) {
            result.d[i] = Math.abs(d[i]);
        }
        return result;
    }

    public double norm() {
        double sum=0;
        for (int i=0; i<d.length; ++i) {
            sum += d[i]*d[i];
        }
        return Math.sqrt(sum);
    }

    public double min() {
        double result;
        result = d[0];
        for (int i=1; i<d.length; ++i) {
            result = Math.min(result,d[i]);
        }
        return result;
    }

    public Vect times(double a) {
        // Scalar multiplication
        Vect result = new Vect(new double[d.length]);
        for (int i=0; i<d.length; ++i) {
            result.d[i] = a*d[i];
        }
        return result;
    }

    public Vect plus(double a) {
        // Scalar addition
        Vect result = new Vect(new double[d.length]);
        for (int i=0; i<d.length; ++i) {
            result.d[i] = d[i]+a;
        }
        return result;
    }

}
