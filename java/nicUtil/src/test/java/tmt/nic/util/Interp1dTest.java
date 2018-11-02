package tmt.nic.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Interp1dTest {
    double epsilon = 0.0001;

    Vect v0 = new Vect(new double[]{0.0});
    Vect v1 = new Vect(new double[]{0.0, 1.0});
    Vect v2 = new Vect(new double[]{-1.0, 2.0, 3.0, 5.0, 7.0});
    Vect v3 = new Vect(new double[]{-2.0, 0.0, 2.0, 4.0, 6.0});
    Vect v4 = new Vect(new double[]{-0.5, 0.0, 2.0, 4.0, 10.0});
    Vect v5 = new Vect(new double[]{0.0, 2.0, 2.0, 1.0, 5.0});
    Vect v6 = new Vect(new double[]{0.0, 1.0, 1.0, 2.0, 3.0});
    Vect v7 = new Vect(new double[]{-1.0, 0.0, 0.5});
    Vect v8 = new Vect(new double[]{0.0, 1.0, 2.0});

    @Test(expected = Interp1dException.class)
    public void constructorTooSmall() throws Interp1dException {
        // Should throw exception because v0 doesn't have 2 elements
        Interp1d t0 = new Interp1d(v0);
    }

    @Test(expected = Interp1dException.class)
    public void constructorVectMismatch() throws Interp1dException {
        // Should throw exception because number of elements don't match
        Interp1d t0 = new Interp1d(v0, v2, true);
    }

    @Test(expected = Interp1dException.class)
    public void invalidXOrder() throws Interp1dException {
        // Should throw exception because X values out of order
        Interp1d t0 = new Interp1d(v5, v2, false);
        System.out.println("Shouldn't see this: " + t0.val(3.0));
    }

    @Test
    public void val() throws Interp1dException {
        // y only, 2-element array
        Interp1d t1 = new Interp1d(v1);
        assertEquals(0, t1.val(0), epsilon);
        assertEquals(0.67, t1.val(0.67), epsilon);
        assertEquals(1, t1.val(1), epsilon);
        assertEquals(-0.5, t1.val(-0.5), epsilon);
        assertEquals(2, t1.val(2), epsilon);

        // y only, larger array
        Interp1d t2 = new Interp1d(v2);
        assertEquals(3.5, t2.val(2.25), epsilon);
        assertEquals(9, t2.val(5), epsilon);

        // x and y supplied, regular x spacing
        Interp1d t3 = new Interp1d(v3, v2, true);
        assertEquals(-1, t3.val(-2), epsilon);
        assertEquals(2.5, t3.val(1), epsilon);

        // x and y, irregular x spacing
        Interp1d t4 = new Interp1d(v4, v3, false);
        assertEquals(-2, t4.val(-0.5), epsilon);
        assertEquals(-1.0, t4.val(-0.25), epsilon);
        assertEquals(0.5, t4.val(0.5), epsilon);
        assertEquals(7, t4.val(13.0), epsilon);

        // repeated x value
        Interp1d t5 = new Interp1d(v6, v2, false);
        assertEquals(2.5, t5.val(1.0), epsilon);

        // used to find problem with recursive search
        Interp1d t6 = new Interp1d(v7, v8, false);
        assertEquals(1.5, t6.val(0.25), epsilon);
    }

    @Test
    public void file() throws IOException, Interp1dException {
        ClassLoader classLoader = getClass().getClassLoader();

        // irregular
        String xyFile = classLoader.getResource("xydata.txt").getFile();
        Interp1d t1 = new Interp1d(xyFile, true, false);
        assertEquals(1.5, t1.val(0.25), epsilon);

        // regular
        String xyFile2 = classLoader.getResource("xydata2.txt").getFile();
        Interp1d t2 = new Interp1d(xyFile2, true, false);
        assertEquals(1.0, t2.val(3.5), epsilon);

        // y only
        String yFile = classLoader.getResource("ydata.txt").getFile();
        Interp1d t3 = new Interp1d(yFile, false, true);
        assertEquals(3.0, t3.val(2.5), epsilon);
    }

    @Test
    public void valWithWrap() throws Interp1dException {
        // y only
        Interp1d t2 = new Interp1d(v2);
        assertEquals(-1, t2.valWithWrap(0), epsilon);
        assertEquals(8, t2.valWithWrap(-0.5), epsilon);

        // x and y supplied, regular x spacing
        Interp1d t3 = new Interp1d(v3, v2, true);
        assertEquals(-1, t3.valWithWrap(-2), epsilon);
        assertEquals(2, t3.valWithWrap(8), epsilon);

        // x and y, irregular x spacing
        Interp1d t4 = new Interp1d(v4, v3, false);
        assertEquals(-2, t4.valWithWrap(-0.5), epsilon);
        assertEquals(0, t4.valWithWrap(10.5), epsilon);

    }
}