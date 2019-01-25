/******************************************************************************
 ****         D A O   I N S T R U M E N T A T I O N   G R O U P           *****
 *
 * (c) 2018-2019                          (c) 2018-2019
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
 * \file Vect.java
 * \brief This file implements the nic.util.Vect vector utility class.
 * <hr>
 ******************************************************************************
 */

package nic.util;

import java.util.Arrays;
import java.util.Optional;

/*
 ******************************************************************************
 * Class Interp1d
 ******************************************************************************
 *//*!
 * \brief
 * This class is used to implement vectors and some common operations.
 *
 * <b> Implementation Details: </b>
 * Vector data are provided as a double[] to the constructor, and are public
 * to allow the user to update and read values as they see fit.
 *
 * The following basic vector operations are currently supported:
 *   - abs() to return the element-wise absolute value of a Vector
 *   - dot() to perform the dot product between two Vectors
 *   - equals() to check for element-wise equality of two Vectors
 *   - max() to calculate the maximum value of the vector
 *   - min() to calculate the minimum value of the vector
 *   - norm() to calculate the vector norm
 *   - plus() to perform scalar addition
 *   - times() to perform scalar multiplication
 *
 * Examples:
 *
 * \code
 * // Create two Vect objects with the same number of elements
 * Vect v1 = new Vect(new double[]{5,6,0});
 * Vect v2 = new Vect(new double[]{1,2,3});
 *
 * // Check for equality of v1 and v2
 * boolean isEqual = v1.equals(v2);
 *
 * // Calculate the dot product of v1 and v2
 * double d = v1.dot(v2);
 *
 * // Calculate a new Vect that results from adding a scalar to v1
 * Vect v3 = v1.plus(42.0);
 *
 * // Calculate a new Vect that results from multiplying v1 by a scalar
 * Vect v4 = v1.times(3.14);
 *
 * \endcode
 *
 * For more examples see tests in VectTest.java .
 *
 * <hr>
 * \callgraph
 ******************************************************************************
 */
public class Vect {

    /*-------------------------------------------------------------------------
     * Private Attributes
     *-----------------------------------------------------------------------*/

    private final double[] _d; /*!< fixed-size double array holds vector data */

    // Since __d is final it is safe to store the result of operations that
    // depend only on _d on the first invocation, and then simply return these
    // stored values on subsequent invocations.
    private Optional<Vect> _abs = Optional.empty();
    private Optional<Double> _max = Optional.empty();
    private Optional<Double> _min = Optional.empty();
    private Optional<Double> _norm = Optional.empty();

    /*
     ******************************************************************************
     * Method Vect::Vect()
     ******************************************************************************
     *//*!
     * \brief
     * Default constructor provided an array of values.
     *
     * \param[in] d (double[]) array of data for the Vector.
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public Vect(double[] d) {
        _d = d;
    }

    /*
     ******************************************************************************
     * Method Vect::abs()
     ******************************************************************************
     *//*!
     * \brief
     * Returns a new Vector of element-wise absolute values.
     *
     * <b> Implementation Details: </b>\n\n
     * Creates a new vector in which each element is the absolute value.
     *
     * \return (Vect) new vector of absolute values
     *
     * \callgraph
     ******************************************************************************
     */
    public Vect abs() {
        if (_abs.isPresent()) {
            return _abs.get();
        } else {
            Vect result = new Vect(new double[_d.length]);
            for (int i = 0; i < _d.length; ++i) {
                result._d[i] = Math.abs(_d[i]);
            }
            _abs = Optional.of(result);
            return result;
        }
    }

    /*
     ******************************************************************************
     * Method Vect::dot()
     ******************************************************************************
     *//*!
     * \brief
     * Perform the dot product.
     *
     * <b> Implementation Details: </b>\n\n
     * Performs the dot product with the supplied vector.
     *
     * \param[in] v (Vect) second vector with which to perform the dot product.
     *
     * \return (double) the dot product.
     *
     * \callgraph
     ******************************************************************************
     */
    public double dot(Vect v) throws IllegalArgumentException {
        double result=0;
        if (v._d.length != _d.length) {
            throw new IllegalArgumentException("Vectors must have the same length.");
        }
        for (int i = 0; i< _d.length; ++i) {
            result += _d[i]*v._d[i];
        }
        return result;
    }

    /*
     ******************************************************************************
     * Method Vect::equals()
     ******************************************************************************
     *//*!
     * \brief
     * Checks for element-wise equality between two vectors.
     *
     * <b> Implementation Details: </b>\n\n
     * Checks for element-wise equality. It is optimized to return true
     * without checking element-wise equality if the comparison vector is
     * in fact the same object.
     *
     * \param[in] v (Vect) second vector with which to to compare.
     *
     * \return (boolean) the result of the equality check.
     *
     * \callgraph
     ******************************************************************************
     */
    public boolean equals(Vect v) throws IllegalArgumentException {
        boolean result=true;
        if (this == v) {
            // same object so we can skip the element-wise test
            result = true;
        } else {
            if (v._d.length != _d.length) {
                throw new IllegalArgumentException("Vectors must have the same length.");
            }
            // Check element-wise equality
            for (int i = 0; i < _d.length; ++i) {
                if (_d[i] != v._d[i]) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /*
     ******************************************************************************
     * Method Vect::max()
     ******************************************************************************
     *//*!
     * \brief
     * Vector maximum.
     *
     * <b> Implementation Details: </b>\n\n
     * Calculates the maximum value of the vector.
     *
     * \return (double) the vector maximum.
     *
     * \callgraph
     ******************************************************************************
     */
    public double max() {
        if (_max.isPresent()) {
            return _max.get();
        } else {
            double result = _d[0];
            for (int i = 1; i < _d.length; ++i) {
                result = Math.max(result, _d[i]);
            }
            _max = Optional.of(result);
            return result;
        }
    }

    /*
     ******************************************************************************
     * Method Vect::min()
     ******************************************************************************
     *//*!
     * \brief
     * Vector minimum.
     *
     * <b> Implementation Details: </b>\n\n
     * Calculates the minimum value of the vector.
     *
     * \return (double) the vector minimum.
     *
     * \callgraph
     ******************************************************************************
     */
    public double min() {
        if (_min.isPresent()) {
            return _min.get();
        } else {
            double result = _d[0];
            for (int i = 1; i < _d.length; ++i) {
                result = Math.min(result, _d[i]);
            }
            _min = Optional.of(result);
            return result;
        }
    }

    /*
     ******************************************************************************
     * Method Vect::norm()
     ******************************************************************************
     *//*!
     * \brief
     * Vector norm.
     *
     * <b> Implementation Details: </b>\n\n
     * Calculates the sqrt of the sum of the squared values.
     *
     * \return (double) the vector norm.
     *
     * \callgraph
     ******************************************************************************
     */
    public double norm() {
        if (_norm.isPresent()) {
            return _norm.get();
        } else {
            double sum = 0;
            for (int i = 0; i < _d.length; ++i) {
                sum += _d[i] * _d[i];
            }
            double result = Math.sqrt(sum);
            _norm = Optional.of(result);
            return result;
        }
    }

    /*
     ******************************************************************************
     * Method Vect::plus()
     ******************************************************************************
     *//*!
     * \brief
     * Scalar addition.
     *
     * <b> Implementation Details: </b>\n\n
     * Creates a new vector in which each element has a added to it.
     *
     * \param[in] a (double) scalar value to add
     *
     * \return (Vect) new vector with a added to each element.
     *
     * \callgraph
     ******************************************************************************
     */
    public Vect plus(double a) {
        Vect result = new Vect(new double[_d.length]);
        for (int i = 0; i< _d.length; ++i) {
            result._d[i] = _d[i]+a;
        }
        return result;
    }

    /*
     ******************************************************************************
     * Method Vect::times()
     ******************************************************************************
     *//*!
     * \brief
     * Scalar multiplication.
     *
     * <b> Implementation Details: </b>\n\n
     * Creates a new vector in which each element is multiplied by a.
     *
     * \param[in] a (double) scalar multiplier
     *
     * \return (Vect) new vector with each element multiplied by a.
     *
     * \callgraph
     ******************************************************************************
     */
    public Vect times(double a) {
        Vect result = new Vect(new double[_d.length]);
        for (int i = 0; i< _d.length; ++i) {
            result._d[i] = a*_d[i];
        }
        return result;
    }

    // --- Simple Getters  ----------------------------------------------------------------------------------------

    /*! @copydoc _d */
    public double[] getd() { return Arrays.copyOf(_d,_d.length); }

}
