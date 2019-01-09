package nic.util;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by chapine on 14/03/17.
 */
public class HistogramTest {
    double epsilon = 0.0001;

    @Test
    public void updateScalar() throws Exception {
        Histogram h = new Histogram(0,10,1, Optional.of("test"));

        // 2 values added inside histogram range
        h.update(1.5);
        h.update(0.5);
        assertEquals(1.0,h.getMean(),epsilon);
        assertEquals(0.5,h.getMin(),epsilon);
        assertEquals(1.5,h.getMax(),epsilon);
        assertEquals(2,h.getnHist());
        assertEquals(2,h.getnTotal());
        assertEquals(0.5,h.getVariance(),epsilon);
        assertEquals(0.707107,h.getStdev(),epsilon);


        // 2 values added outside histogram range still contribute to min, max, mean, variance, sigma
        h.update(-1.5);
        h.update(-0.5);
        assertEquals(0.0,h.getMean(),epsilon);
        assertEquals(-1.5,h.getMin(),epsilon);
        assertEquals(1.5,h.getMax(),epsilon);
        assertEquals(2,h.getnHist());
        assertEquals(4,h.getnTotal());
        assertEquals(1.66666,h.getVariance(),epsilon);
        assertEquals(1.29099,h.getStdev(),epsilon);

        // String output only contains bins with values
        assertTrue(h.toString().matches(
                "[\\[\\]]+\n"+
                        "\ntest\n\n"+
                        "Min: -1.5 Mean: -2.7755575615628914E-17 SD: 1.2909944487358054\n"+
                        "Max: 1.5 @ [\\d\\w-:.]+ \\(sample 1\\)\n"+
                        "Histogram \\(bin label represents lower bound\\)\n"+
                        "0.0: 1\n"+
                        "1.0: 1\n"));
        System.out.println(h.toString());
    }

    @Test
    public void updateArray() throws Exception {
        Histogram h = new Histogram(0,10,1,Optional.of("test"));
        h.update(new double[]{1,2,3,4,5,6,7,8,9} );
        h.update(new double[]{1,2,3,4,5,6,7,8,9} );

        assertEquals(5.0,h.getMean(),epsilon);
        assertEquals(1.0,h.getMin(),epsilon);
        assertEquals(9,h.getMax(),epsilon);
        assertEquals(18,h.getnHist());
        assertEquals(18,h.getnTotal());
        assertEquals(7.05882,h.getVariance(),epsilon);
        assertEquals(2.65684,h.getStdev(),epsilon);

        System.out.println(h.toString());
    }

    @Test(expected = Exception.class)
    public void noSamplesNoWarmup() throws Exception {
        // Should throw exception because no samples added
        Histogram h = new Histogram(0,10,1,Optional.of("test"));
        double m = h.getMin();
    }

    @Test(expected = Exception.class)
    public void noSamplesWarmup() throws Exception {
        // Should throw exception because no samples added
        Histogram h = new Histogram(0,10,1, 2, Optional.of("test"));
        h.update(new double[]{1,2});
        double m = h.getMin();
    }

    @Test
    public void warmup() throws Exception {
        // first two samples are ignored due to warmup
        Histogram h = new Histogram(0,10,1, 2, Optional.of("test"));
        h.update(new double[]{1,2,3});
        assertEquals(3,h.getMin(),epsilon);

    }
}