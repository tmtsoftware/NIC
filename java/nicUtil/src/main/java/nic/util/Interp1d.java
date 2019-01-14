/******************************************************************************
 ****         D A O   I N S T R U M E N T A T I O N   G R O U P           *****
 *
 * (c) 2018                               (c) 2018
 * National Research Council              Conseil national de recherches
 * Ottawa, Canada, K1A 0R6                Ottawa, Canada, K1A 0R6
 * All rights reserved                    Tous droits reserves
 *
 * NRC disclaims any warranties,          Le CNRC denie toute garantie
 * expressed, implied, or statutory, of   enoncee, implicite ou legale, de
 * any kind with respect to the soft-     quelque nature que se soit, concer-
 * ware, including without limitation     nant le logiciel, y compris sans
 * any warranty of merchantability or     restriction toute garantie de valeur
 * fitness for a particular purpose.      marchande u de pertinence pour un
 * NRC shall not be liable in any event   usage particulier. Le CNRC ne pourra
 * for any damages, whether direct or     en aucun cas etre tenu responsable
 * indirect, special or general, conse-   de tout dommage, direct ou indirect,
 * quential or incidental, arising from   particulier ou general, accessoire
 * the use of the software.               ou fortuit, resultant de l'utili-
 *                                        sation du logiciel.
 *
 *****************************************************************************/

/*!
 ******************************************************************************
 * \file Interp1d.java
 * \brief This file implements a class for performing 1-dimensional interpolation
 * <hr>
 ******************************************************************************
 */

package nic.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

/*
 ******************************************************************************
 * Interp1d
 ******************************************************************************
 *//*!
 * \brief
 * This class is used to perform interpolation (and extrapolation) of
 * 1-dimensional data.
 *
 * <b> Implementation Details: </b>
 * This class uses either linearly- or irregularly-spaced data to perform
 * interpolation and extrapolation. The original intended purposes are
 * to assist in: (i) creation of lookup tables for things like pointing
 * models, or waveforms in motion simulations; and (ii) to interpolate/
 * extrapolate precise positions from demand streams (e.g., provided
 * by the TCS).
 *
 * If the constructor is given a single vector, Y, the interpolation
 * method val() takes a fractional array index as the argument (i.e., val(0.5) would
 * return the interpolated value between the first and second elements of Y).
 *
 * If the constructor is provided with two vectors X and Y, then val() returns
 * the interpolated value at the requested x-coordinate. For example, if X
 * were an array of time stamps (seconds), and Y positions at those time stamps,
 * val(32.3) would return the interpolated Y-value at 32.3 seconds.
 *
 * Data may be provided using double[] arrays or a named file containing columns of
 * values that will be read.
 *
 * For data values beyond the extent of X and Y, extrapolation is achieved
 * using the val() methods, or with wrapping using the valWithWrap() method.
 *
 * Presently there is only support for linear interpolation/extrapolation.
 *
 * <hr>
 * \callgraph
 ******************************************************************************
 */
public class Interp1d {

    /*-------------------------------------------------------------------------
     * Private Attributes
     *-----------------------------------------------------------------------*/
    private double[] _x_table;/*!< optional x values corresponding to y values */
    private double[] _y_table;/*!< values to be interpolated */

    private double _x0_table; /*!< reference x value for converting into array indices */
    private double _dx_table; /*!< x step size for converting into array indices */

    private boolean _yOnly;    /*!< false if constructed only with y-values */
    private boolean _regular;  /*!< flag indicating regularly-spaced data */

    /*
     ******************************************************************************
     * Interp1d::Interp1d()
     ******************************************************************************
     *//*!
     * \brief
     * Interp1d Constructor provided a single array of values.
     *
     * <b> Implementation Details: </b>\n\n
     * The caller provides a vector of regularly-spaced values to be interpolated.
     * Subsequent calls to val() interpret the argument as fractional array indices.
     *
     * \param[in] y (double[]) regularly-spaced values
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public Interp1d(double[] y) throws IllegalArgumentException {
        yOnlyConstructor(y);
    }

    private void yOnlyConstructor(double[] y) throws IllegalArgumentException {
        if (y.length < 2) {
            throw new IllegalArgumentException("Supplied vector must have at least 2 elements");
        }

        _y_table = y;
        _yOnly = true;
        _regular = true;
    }

    /*
     ******************************************************************************
     * Interp1d::Interp1d()
     ******************************************************************************
     *//*!
     * \brief
     * Interp1d Constructor using provided x and y values.
     *
     * <b> Implementation Details: </b>\n\n
     * The caller provides a list of y-values to be interpolated, with corresponding
     * x-values.
     * Subsequent calls to val() interpret the argument as x values at which to
     * evaluate the interpolation.
     * The caller can assert that the x-values are regularly sampled (equally spaced),
     * which will result in a slight performance improvement (no checking is performed).
     * Otherwise, irregularly-spaced data may also be provided (setting regular to
     * false), though it must be sorted in monotonically increasing order of x (checked).
     *
     * \param[in] x (double[]) x-coordinates of values to be interpolated
     * \param[in] y (double[]) y-values to be interpolated
     * \param[in] regular (boolean) indicate whether data are regularly or irregularly-spaced along x-coordinate
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public Interp1d(double[] x, double[] y, boolean regular) throws IllegalArgumentException, ArithmeticException {
        xAndYConstructor(x, y, regular);
    }


    private void xAndYConstructor(double[] x, double[] y, boolean regular) throws IllegalArgumentException, ArithmeticException {
        if (x.length < 2) {
            throw new IllegalArgumentException("Supplied x vector must have at least 2 elements");
        }
        if (y.length < 2) {
            throw new IllegalArgumentException("Supplied y vector must have at least 2 elements");
        }
        if (x.length != y.length) {
            throw new IllegalArgumentException("Supplied x and y vectors must have the same lengths");
        }

        _x_table = x;
        _y_table = y;
        _regular = regular;

        if (_regular) {
            // used to convert supplied values into array indices
            _x0_table = x[0];
            _dx_table = x[1] - x[0];
        } else {
            // verify that x values are monotonic when irregularly spaced
            for (int i = 0; i<(_x_table.length-1); ++i) {
                double x0 = _x_table[i];
                double x1 = _x_table[i+1];

                if (x0 > x1) {
                    throw new ArithmeticException("Detected x array elements out of order at indices " + x0 + ", " + x1);
                }
            }
        }
        _yOnly = false;
    }

    /*
     ******************************************************************************
     * Interp1d::Interp1d()
     ******************************************************************************
     *//*!
     * \brief
     * Interp1d Constructor using values stored in a file.
     *
     * <b> Implementation Details: </b>\n\n
     * The caller provides the name of a text file containing one (Y only), or two columns
     * of numerical data (X and Y separated by whitespace), indicating how many
     * columns to expect with the twoColumns argument.
     * The interpretation of the loaded X and Y values and the regular argument are
     * otherwise identical to Interp1d::Interp1d(double[] x, double[] y, boolean regular).
     *
     * \param[in] filename (String) name of the file containing interpolation data
     * \param[in] twoColumns (boolean) indicate whether file contains just Y (false), or X and Y (true)
     * \param[in] regular (boolean) indicate whether data are regularly or irregularly-spaced along x-coordinate
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public Interp1d(String filename, boolean twoColumns, boolean regular) throws IOException, IllegalArgumentException {
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

            double[] x = new double[xList.size()];
            double[] y = new double[yList.size()];
            for (int i=0; i<x.length; ++i) {
                x[i] = xList.get(i).doubleValue();
                y[i] = yList.get(i).doubleValue();
            }

            xAndYConstructor(x, y, regular);
        } else {
            for (String line : lines) {
                String[] elements = line.trim().split("\\s+");
                if (elements.length == 1) {
                    yList.add(Double.parseDouble(elements[0]));
                }
            }

            double[] y = new double[yList.size()];
            for (int i=0; i<y.length; ++i) {
                y[i] = yList.get(i).doubleValue();;
            }

            yOnlyConstructor(y);
        }
    }

    /*
     ******************************************************************************
     * Interp1d::val()
     ******************************************************************************
     *//*!
     * \brief
     * Obtain interpolated/extrapolated value.
     *
     * <b> Implementation Details: </b>\n\n
     * If the object was constructed only with Y values, the argument is interpreted
     * as a fractional array index into Y (i.e., val(0.5) returns the value interpolated
     * between the first and second elements of Y).
     *
     * If the object was constructed with X and Y values, the argument is interpreted
     * as the x value at which to obtain an interpolated value. For example, if X
     * were an array of time stamps (seconds), and Y positions at those time stamps,
     * val(32.3) would return the interpolated position at 32.3 seconds.
     *
     * If the argument lies beyond the range of the input data (array length in the
     * case of Y only, or the range of X if specified), then the returned value
     * is the linear extrapolation of the nearest two data points. For example, if
     * constructed using Y=[0,2], then val(2) is an index past the end of the Y array,
     * and it would return the extrapolated value 4.
     *
     * \param[in] x (double) fractional index or x-value at which to evaluate interpolation
     *
     * \return (double) the interpolated/extrapolated value
     *
     * \callgraph
     ******************************************************************************
     */
    public double val(double x) {
        // Evaluate interpolation function at x

        double retval;
        int i0, i1;     // indices of bounding samples
        double y0, y1;  // y values at bounding samples
        double i;       // fractional array index

        if (_regular) {
            // If regularly spaced we are just converting x into
            // a fractional array index, identify the values of
            // y on either side, and then performing linear interpolation.
            if (_yOnly) {
                i = x;
            } else {
                i = (x - _x0_table) / _dx_table;
            }

            if (i < 1) {
                i0 = 0;
                i1 = 1;
            } else if (i >= (_y_table.length - 2)) {
                i0 = _y_table.length - 2;
                i1 = _y_table.length - 1;
            } else {
                i0 = (int) floor(i);
                i1 = (int) ceil(i);
            }

            y0 = _y_table[i0];
            y1 = _y_table[i1];

            double m = (y1-y0)/1.;
            double b = y0 - m*i0;

            retval = m*i + b;
        } else {
            // For irregularly-spaced x do a binary search to find the
            // bounding indices to fit line segment.
            i0 = searchXIndex(x, 0, _x_table.length-1);
            i1 = i0+1;

            y0 = _y_table[i0];
            y1 = _y_table[i1];

            double x0 = _x_table[i0];
            double x1 = _x_table[i1];

            if (x0 == x1) {
                // average if both values are equal
                retval = (y0+y1)/2.;
            } else {
                double m = (y1 - y0) / (x1 - x0);
                double b = y0 - m * _x_table[i0];
                retval = m * x + b;
            }

        }

        return retval;
    }

    /*
     ******************************************************************************
     * Interp1d::valWithWrap()
     ******************************************************************************
     *//*!
     * \brief
     * Obtain interpolated value with wrapping.
     *
     * <b> Implementation Details: </b>\n\n
     * This method behaves the same as val() when the argument lies within
     * the range of the input data (array length in the case of Y only, or the range of
     * X if specified).
     *
     * If the argument is outside the range, instead of extrapolating, this method
     * will return an interpolated value with wrapping. For example, if constructed
     * using Y=[0,1,2], then valWithWrap(2.5) is an index past the end of the Y array,
     * and it would return the interpolated value 1.0 which lies between 2 at index 2 and
     * the wrapped value 0 index 0. Similarly, valWithWrap(3.5) would return 0.5 as
     * an interpolation between 0 and wrapped index 0, and 1 at wrapped index 1.
     *
     * \param[in] x (double) fractional index or x-value at which to evaluate interpolation
     *
     * \return (double) the interpolated value
     *
     * \callgraph
     ******************************************************************************
     */
    public double valWithWrap(double x) {
        // Wrap x to within the range of the interpolation function
        double x_wrapped;
        if (_yOnly) {
            x_wrapped = mod(x, _y_table.length);
        } else {
            double xRange = _x_table[_x_table.length-1] - _x_table[0];
            x_wrapped = mod((x - _x_table[0]),xRange) + _x_table[0];
        }
        return val(x_wrapped);
    }


    /*
     ******************************************************************************
     * Interp1d::mod()
     ******************************************************************************
     *//*!
     * \brief
     * Private unsigned implementation of modulo
     *
     * <b> Implementation Details: </b>\n\n
     * Returns x % y, but adds y to the result if it is < 0. This is the operation
     * we need when identifying wrapped array indices.
     *
     * \param[in] x (double) dividend
     * \param[in] y (double) divisor
     *
     * \return (double) the unsigned remainder
     *
     * \callgraph
     ******************************************************************************
     */
    private double mod(double x, double y) {
        double retval = x % y;
        if (retval<0) {
            retval += y;
        }
        return retval;
    }

    /*
     ******************************************************************************
     * Interp1d::searchXIndex()
     ******************************************************************************
     *//*!
     * \brief
     * Recursive private method that performs a binary search of the X array used in the
     * class constructor to find the lower of two bounding indices for an arbitrary x value.
     *
     * <b> Implementation Details: </b>\n\n
     * Given x and two starting indices i0 and i1, compares x to the X array evaluated at
     * the midpoint xMid = X[iMid], where iMid=(i0+i1)/2.
     * It continues to search recursively in the top half (searchIndex(x,iMid,i1) if x>xMid,
     * and the bottom half (searchIndex(x,i0,iMid) otherwise. It stops under the following
     * conditions:
     *   - i1-i0 <= 1: found the bounding indices somewhere in the middle of the X table
     *   - x <= X[i0]: x is beyond the lower bound of X
     *   - x >= X[i1]: x is beyond the upper bound of X
     *
     * For example, if X=[0,1,2,3], then getTableLen(1.2) would return 1 (since the bounding
     * indices are 1 and 2).
     *
     * \return (int) the lower bounding index
     *
     * \callgraph
     ******************************************************************************
     */
    private int searchXIndex(double x, int i0, int i1) {
        int retval;
        double x0 = _x_table[i0];
        double x1 = _x_table[i1];

        if ((i1-i0) <= 1) {
            retval = i0;
        } else if (x <= x0) {
            retval = i0;
        } else if (x >= x1) {
            retval = i1-1;
        } else {
            int iMid = (i0 + i1)/2;
            double xMid = _x_table[iMid];
            if (x > xMid) {
                retval = searchXIndex(x,iMid,i1);
            } else {
                retval = searchXIndex(x, i0, iMid);
            }
        }
        return retval;
    }

}
