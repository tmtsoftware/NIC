package tmt.nic.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class VectTest {
    double epsilon = 0.0001;

    Vect v1 = new Vect(new double[]{ 0,1,2});
    Vect v2 = new Vect(new double[]{ 5,6,7});
    Vect v3 = new Vect(new double[]{-5,6,0});

    @Test
    public void abs() {
        Vect v = v3.abs();
        Vect expected = new Vect(new double[]{5,6,0});
        assertTrue(v.equals(expected));
        assertFalse(v.equals(v3));
    }

    @Test
    public void dot() {
        assertEquals(20, v1.dot(v2), epsilon);
        assertEquals(20, v2.dot(v1), epsilon);
    }

    @Test
    public void min() {
        assertEquals(-5,v3.min(),epsilon);
    }

    @Test
    public void max() {
        assertEquals(6,v3.max(),epsilon);
    }

    @Test
    public void norm() {
        assertEquals(Math.sqrt(5),v1.norm(), epsilon);
    }

    @Test
    public void plus() {
        Vect v = v1.plus(-2);
        Vect expected = new Vect(new double[]{-2,-1,0});
        assertTrue(v.equals(expected));
    }
    @Test
    public void times() {
        Vect v = v1.times(3);
        Vect expected = new Vect(new double[]{0,3,6});
        assertTrue(v.equals(expected));
    }

}