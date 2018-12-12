package nic.util;

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

        // Subsequent call should used stored value
        Vect v_again = v3.abs();                // should be same object
        Vect v_other = (new Vect(v3.d)).abs();  // different object with same values
        assertEquals(v_again, v);
        assertNotEquals( v_again, v_other);     // not same object
        assertTrue(v_again.equals(v_other));    // but have the same values
    }

    @Test
    public void dot() {
        assertEquals(20, v1.dot(v2), epsilon);
        assertEquals(20, v2.dot(v1), epsilon);

        // Should return same value twice in a row
        assertEquals(20, v2.dot(v1), epsilon);
    }

    @Test
    public void min() {
        assertEquals(-5,v3.min(),epsilon);

        // Should return same value twice in a row
        assertEquals(-5,v3.min(),epsilon);
    }

    @Test
    public void max() {
        assertEquals(6,v3.max(),epsilon);

        // Should return same value twice in a row
        assertEquals(6,v3.max(),epsilon);
    }

    @Test
    public void norm() {
        assertEquals(Math.sqrt(5),v1.norm(), epsilon);

        // Should return same value twice in a row
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