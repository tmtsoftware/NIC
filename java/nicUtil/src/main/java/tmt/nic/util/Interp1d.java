package tmt.nic.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

public class Interp1d {
    private Vect x_table;
    private Vect y_table;

    private double dx_table;
    private double x0_table;

    private final boolean yOnly;
    private final boolean regular;

    public Interp1d(Vect y) throws Interp1dException {
        // Check that y includes at least two values
        if (y.d.length < 2) {
            throw new Interp1dException("Supplied vector must have at least 2 elements");
        }

        this.y_table = y;
        yOnly = true;
        regular = true;
    }

    public Interp1d(Vect x, Vect y, boolean regular) throws Interp1dException {
        // Supply both x and y value.
        if (x.d.length < 2) {
            throw new Interp1dException("Supplied x vector must have at least 2 elements");
        }
        if (y.d.length < 2) {
            throw new Interp1dException("Supplied y vector must have at least 2 elements");
        }
        if (x.d.length != y.d.length) {
            throw new Interp1dException("Supplied x and y vectors must have the same lengths");
        }

        this.x_table = x;
        this.y_table = y;

        // Regular if x known to be evenly spaced. Otherwise x need only be sorted
        this.regular = regular;

        if (regular) {
            // used to convert supplied values into array indices
            this.x0_table = x.d[0];
            this.dx_table = x.d[1] - x.d[0];
        } else {
            // verify that x values are monotonic when irregularly spaced
            for (int i=0; i<(x_table.d.length-1); ++i) {
                double x0 = x_table.d[i];
                double x1 = x_table.d[i+1];

                if (x0 > x1) {
                    throw new Interp1dException("Detected x array elements out of order: " + x0 + ", " + x1);
                }
            }
        }
        yOnly = false;
    }

    public Interp1d(String filename, boolean twoColumns, boolean regular) throws IOException, Interp1dException {
        // Read in Y, or X,Y (if twoColumns set) using space-delimited columns of numbers in a supplied text file

        Path file = Paths.get(filename);
        List<String> lines = Files.readAllLines(file, Charset.defaultCharset());

        ArrayList<Double> xList = new ArrayList<Double>();
        ArrayList<Double> yList = new ArrayList<Double>();

        if (twoColumns) {
            for (String line : lines) {
                String[] elements = line.trim().split("\\s+");
                if (elements.length == 2) {
                    xList.add(Double.parseDouble(elements[0]));
                    yList.add(Double.parseDouble(elements[1]));
                }
            }

            Vect x = new Vect(new double[xList.size()]);
            Vect y = new Vect(new double[yList.size()]);
            for (int i=0; i<x.d.length; ++i) {
                x.d[i] = xList.get(i).doubleValue();
                y.d[i] = yList.get(i).doubleValue();
            }

            if (x.d.length < 2) {
                throw new Interp1dException("Supplied x vector must have at least 2 elements");
            }

            this.x_table = x;
            this.y_table = y;
            this.regular = regular;

            if (regular) {
                // used to convert supplied values into array indices
                this.x0_table = x.d[0];
                this.dx_table = x.d[1] - x.d[0];
            } else {
                // verify that x values are monotonic when irregularly spaced
                for (int i=0; i<(x_table.d.length-1); ++i) {
                    double x0 = x_table.d[i];
                    double x1 = x_table.d[i+1];

                    if (x0 > x1) {
                        throw new Interp1dException("Detected x array elements out of order: " + x0 + ", " + x1);
                    }
                }
            }
            yOnly = false;

        } else {
            for (String line : lines) {
                String[] elements = line.trim().split("\\s+");
                if (elements.length == 1) {
                    yList.add(Double.parseDouble(elements[0]));
                }
            }

            Vect y = new Vect(new double[yList.size()]);
            for (int i=0; i<y.d.length; ++i) {
                y.d[i] = yList.get(i).doubleValue();;
            }

            if (y.d.length < 2) {
                throw new Interp1dException("Supplied vector must have at least 2 elements");
            }

            this.y_table = y;
            yOnly = true;
            this.regular = true;
        }
    }

    public double mod(double x, double y) {
        double retval = x % y;
        if (retval<0) {
            retval += y;
        }
        return retval;
    }

    public double valWithWrap(double x) throws Interp1dException {
        // Wrap x to within the range of the interpolation function
        double x_wrapped;
        if (yOnly) {
            x_wrapped = mod(x, getTableLen());
        } else {
            double xRange = x_table.d[getTableLen()-1] - x_table.d[0];
            x_wrapped = mod((x - x_table.d[0]),xRange) + x_table.d[0];
        }
        return val(x_wrapped);
    }

    public double val(double x) throws Interp1dException {
        // Evaluate interpolation function at x

        double retval;
        int i0, i1;     // indices of bounding samples
        double y0, y1;  // y values at bounding samples
        double i;       // fraction array index

        if (regular) {
            if (yOnly) {
                i = x;
            } else {
                i = (x - x0_table) / dx_table;
            }

            if (i < 1) {
                i0 = 0;
                i1 = 1;
            } else if (i >= (y_table.d.length - 2)) {
                i0 = y_table.d.length - 2;
                i1 = y_table.d.length - 1;
            } else {
                i0 = (int) floor(i);
                i1 = (int) ceil(i);
            }

            y0 = y_table.d[i0];
            y1 = y_table.d[i1];

            double m = (y1-y0)/1.;
            double b = y0 - m*i0;

            retval = m*i + b;
        } else {
            // For irregularly-spaced x do a binary search to find the bounding indices to fit line segment
            i0 = searchXIndex(x, 0, x_table.d.length-1);
            i1 = i0+1;

            y0 = y_table.d[i0];
            y1 = y_table.d[i1];

            double x0 = x_table.d[i0];
            double x1 = x_table.d[i1];

            if (x0 == x1) {
                // average if both values are equal
                retval = (y0+y1)/2.;
            } else {
                double m = (y1 - y0) / (x1 - x0);
                double b = y0 - m * x_table.d[i0];
                retval = m * x + b;
            }

        }

        return retval;
    }

    public int searchXIndex(double x, int i0, int i1) {
        // Recursive binary search
        int retval;
        double x0 = x_table.d[i0];
        double x1 = x_table.d[i1];

        if ((i1-i0) <= 1) {
            retval = i0;
        } else if (x <= x0) {
            retval = i0;
        } else if (x >= x1) {
            retval = i1-1;
        } else {
            int iMid = (i0 + i1)/2;
            double xMid = x_table.d[iMid];
            if (x > xMid) {
                retval = searchXIndex(x,iMid,i1);
            } else {
                retval = searchXIndex(x, i0, iMid);
            }
        }


        return retval;
    }

    public double getXTableVal(int index) {
        return x_table.d[index];
    }

    public double getYTableVal(int index) {
        return y_table.d[index];
    }

    public int getTableLen() {
        return y_table.d.length;
    }

    public void offsetXTable(double dx) throws Interp1dException {
        if (yOnly) {
            throw new Interp1dException("Interp1d object instantiated with y only.");
        }
        x_table = x_table.plus(dx);
        if (regular) {
            // Need to re-calculate
            x0_table = x_table.d[0];
        }
    }
}
