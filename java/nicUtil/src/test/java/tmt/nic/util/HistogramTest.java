package tmt.nic.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by chapine on 14/03/17.
 */
public class HistogramTest {
    double epsilon = 0.0001;

    @Test
    public void updateScalar() throws Exception {
        Histogram h = new Histogram(0,10,1);

        // 2 values added inside histogram range
        h.update(1.5);
        h.update(0.5);
        assertEquals(1.0,h.getMean(),epsilon);
        assertEquals(0.5,h.getMin(),epsilon);
        assertEquals(1.5,h.getMax(),epsilon);
        assertEquals(2,h.n);
        assertEquals(2,h.nTotal);
        assertEquals(0.5,h.getVariance(),epsilon);
        assertEquals(0.707107,h.getStdev(),epsilon);


        // 2 values added outside histogram range still contribute to min, max, mean, variance, sigma
        h.update(-1.5);
        h.update(-0.5);
        assertEquals(0.0,h.getMean(),epsilon);
        assertEquals(-1.5,h.getMin(),epsilon);
        assertEquals(1.5,h.getMax(),epsilon);
        assertEquals(2,h.n);
        assertEquals(4,h.nTotal);
        assertEquals(1.66666,h.getVariance(),epsilon);
        assertEquals(1.29099,h.getStdev(),epsilon);

        // String output only contains bins with values
        assertEquals("Min:-1.5 Mean:-2.7755575615628914E-17 Std:1.2909944487358054 Max:1.5 Samples:4\n"+
                "0.0: 1.0\n1.0: 1.0\n",h.printstr());
        System.out.println(h.printstr());
    }

    @Test
    public void updateArray() throws Exception {
        Histogram h = new Histogram(0,10,1);
        h.update(new double[]{1,2,3,4,5,6,7,8,9} );
        h.update(new double[]{1,2,3,4,5,6,7,8,9} );

        assertEquals(5.0,h.getMean(),epsilon);
        assertEquals(1.0,h.getMin(),epsilon);
        assertEquals(9,h.getMax(),epsilon);
        assertEquals(18,h.n);
        assertEquals(18,h.nTotal);
        assertEquals(7.05882,h.getVariance(),epsilon);
        assertEquals(2.65684,h.getStdev(),epsilon);

        System.out.println(h.printstr());
    }

    @Test(expected = Exception.class)
    public void noSamplesNoWarmup() throws Exception {
        // Should throw exception because no samples added
        Histogram h = new Histogram(0,10,1);
        double m = h.getMin();
    }

    @Test(expected = Exception.class)
    public void noSamplesWarmup() throws Exception {
        // Should throw exception because no samples added
        Histogram h = new Histogram(0,10,1, 2);
        h.update(new double[]{1,2});
        double m = h.getMin();
    }

    @Test
    public void warmup() throws Exception {
        // first two samples are ignored due to warmup
        Histogram h = new Histogram(0,10,1, 2);
        h.update(new double[]{1,2,3});
        assertEquals(3,h.getMin(),epsilon);

    }
}