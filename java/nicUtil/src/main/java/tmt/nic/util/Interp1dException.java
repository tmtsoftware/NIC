package tmt.nic.util;

/**
 * Helper class for interpolating vectors (specifying either just Y, or X and Y)
 */

public class Interp1dException extends Exception {
    public Interp1dException(String message){
        super(message);
    }
}
