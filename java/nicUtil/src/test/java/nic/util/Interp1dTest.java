package nic.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Interp1dTest {
    double epsilon = 0.0001;

    double[] v0 = new double[]{0.0};
    double[] v1 = new double[]{0.0, 1.0};
    double[] v2 = new double[]{-1.0, 2.0, 3.0, 5.0, 7.0};
    double[] v3 = new double[]{-2.0, 0.0, 2.0, 4.0, 6.0};
    double[] v4 = new double[]{-0.5, 0.0, 2.0, 4.0, 10.0};
    double[] v5 = new double[]{0.0, 2.0, 2.0, 1.0, 5.0};
    double[] v6 = new double[]{0.0, 1.0, 1.0, 2.0, 3.0};
    double[] v7 = new double[]{-1.0, 0.0, 0.5};
    double[] v8 = new double[]{0.0, 1.0, 2.0};

    @Test(expected = IllegalArgumentException.class)
    public void constructorTooSmall() throws IllegalArgumentException {
        // Should throw exception because v0 doesn't have 2 elements
        Interp1d t0 = new Interp1d(v0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorVectMismatch() throws IllegalArgumentException {
        // Should throw exception because number of elements don't match
        Interp1d t0 = new Interp1d(v0, v2, true);
    }

    @Test(expected = ArithmeticException.class)
    public void invalidXOrder() throws ArithmeticException {
        // Should throw exception because X values out of order
        Interp1d t0 = new Interp1d(v5, v2, false);
        System.out.println("Shouldn't see this: " + t0.eval(3.0));
    }

    @Test(expected = ArithmeticException.class)
    public void invalidRegularOrder() throws ArithmeticException {
        // Should throw exception because X values irregularly spaced
        Interp1d t0 = new Interp1d(v7, v8, true);
        System.out.println("Shouldn't see this: " + t0.eval(3.0));
    }

    @Test
    public void val() {
        // y only, 2-element array
        Interp1d t1 = new Interp1d(v1);
        assertEquals(0, t1.eval(0), epsilon);
        assertEquals(0.67, t1.eval(0.67), epsilon);
        assertEquals(1, t1.eval(1), epsilon);
        assertEquals(-0.5, t1.eval(-0.5), epsilon);
        assertEquals(2, t1.eval(2), epsilon);

        // y only, larger array
        Interp1d t2 = new Interp1d(v2);
        assertEquals(3.5, t2.eval(2.25), epsilon);
        assertEquals(9, t2.eval(5), epsilon);

        // x and y supplied, regular x spacing
        Interp1d t3 = new Interp1d(v3, v2, true);
        assertEquals(-1, t3.eval(-2), epsilon);
        assertEquals(2.5, t3.eval(1), epsilon);

        // x and y, irregular x spacing
        Interp1d t4 = new Interp1d(v4, v3, false);
        assertEquals(-2, t4.eval(-0.5), epsilon);
        assertEquals(-1.0, t4.eval(-0.25), epsilon);
        assertEquals(0.5, t4.eval(0.5), epsilon);
        assertEquals(7, t4.eval(13.0), epsilon);

        // repeated x value
        Interp1d t5 = new Interp1d(v6, v2, false);
        assertEquals(2.5, t5.eval(1.0), epsilon);

        // used to find problem with recursive search
        Interp1d t6 = new Interp1d(v7, v8, false);
        assertEquals(1.5, t6.eval(0.25), epsilon);
    }

    @Test
    public void file() throws IOException {
        // Note that the files holding the test data are stored
        // in test/resources
        ClassLoader classLoader = getClass().getClassLoader();

        // irregular
        String xyFile = classLoader.getResource("xydata.txt").getFile();
        Interp1d t1 = new Interp1d(xyFile, true, false);
        assertEquals(1.5, t1.eval(0.25), epsilon);

        // regular
        String xyFile2 = classLoader.getResource("xydata2.txt").getFile();
        Interp1d t2 = new Interp1d(xyFile2, true, false);
        assertEquals(1.0, t2.eval(3.5), epsilon);

        // y only
        String yFile = classLoader.getResource("ydata.txt").getFile();
        Interp1d t3 = new Interp1d(yFile, false, true);
        assertEquals(3.0, t3.eval(2.5), epsilon);
    }

    @Test
    public void valWithWrap() {
        // y only
        Interp1d t2 = new Interp1d(v2);
        assertEquals(-1, t2.evalWithWrap(0), epsilon);
        assertEquals(8, t2.evalWithWrap(-0.5), epsilon);

        // x and y supplied, regular x spacing
        Interp1d t3 = new Interp1d(v3, v2, true);
        assertEquals(-1, t3.evalWithWrap(-2), epsilon);
        assertEquals(2, t3.evalWithWrap(8), epsilon);

        // x and y, irregular x spacing
        Interp1d t4 = new Interp1d(v4, v3, false);
        assertEquals(-2, t4.evalWithWrap(-0.5), epsilon);
        assertEquals(0, t4.evalWithWrap(10.5), epsilon);

    }
}