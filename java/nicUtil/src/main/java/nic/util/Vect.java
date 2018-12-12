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
 * \file Vect.java
 * \brief This file implements a utility class for vectors.
 * <hr>
 ******************************************************************************
 */

package nic.util;

import java.util.Optional;

/*
 ******************************************************************************
 * Interp1d
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
 * <hr>
 * \callgraph
 ******************************************************************************
 */
public class Vect {
    /*-------------------------------------------------------------------------
     * Public Attributes
     *-----------------------------------------------------------------------*/
    public final double[] d;

    /*-------------------------------------------------------------------------
     * Private Attributes
     *-----------------------------------------------------------------------*/

    // Since d is final it is safe to store the result of operations that
    // depend only on d on the first invocation, and then simply return these
    // stored values on subsequent invocations.
    private Optional<Vect> abs = Optional.empty();
    private Optional<Double> max = Optional.empty();
    private Optional<Double> min = Optional.empty();
    private Optional<Double> norm = Optional.empty();

    /*
     ******************************************************************************
     * Vect::Vect()
     ******************************************************************************
     *//*!
     * \brief
     * Default constructor provided an array of values.
     *
     * \param[d] d (double[]) array of data for the Vector.
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public Vect(double[] d) {
        this.d = d;
    }

    /*
     ******************************************************************************
     * Vect::abs()
     ******************************************************************************
     *//*!
     * \brief
     * Returns a new Vector of element-wise absolute values.
     *
     * <b> Implementation Details: </b>\n\n
     * Creates a new vector in which each element is the absolute value.
     *
     * \return (Vect) vector of absolute values
     *
     * \callgraph
     ******************************************************************************
     */
    public Vect abs() {
        if (abs.isPresent()) {
            return abs.get();
        } else {
            Vect result = new Vect(new double[d.length]);
            for (int i = 0; i < d.length; ++i) {
                result.d[i] = Math.abs(d[i]);
            }
            abs = Optional.of(result);
            return result;
        }
    }

    /*
     ******************************************************************************
     * Vect::dot()
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
    public double dot(Vect v) {
        double result=0;
        if (v.d.length != d.length) {
            throw new IllegalArgumentException("Vectors must have the same length.");
        }
        for (int i=0; i<d.length; ++i) {
            result += d[i]*v.d[i];
        }
        return result;
    }

    /*
     ******************************************************************************
     * Vect::equals()
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
    public boolean equals(Vect v) {
        boolean result=true;
        if (this == v) {
            // same object so we can skip the element-wise test
            result = true;
        } else {
            if (v.d.length != d.length) {
                throw new IllegalArgumentException("Vectors must have the same length.");
            }
            // Check element-wise equality
            for (int i = 0; i < d.length; ++i) {
                if (d[i] != v.d[i]) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /*
     ******************************************************************************
     * Vect::max()
     ******************************************************************************
     *//*!
     * \brief
     * Vector maximum.
     *
     * <b> Implementation Details: </b>\n\n
     * Calculates the maximum value of the vector.
     *
     * \return (double) the vector minimum.
     *
     * \callgraph
     ******************************************************************************
     */
    public double max() {
        if (max.isPresent()) {
            return max.get();
        } else {
            double result = d[0];
            for (int i = 1; i < d.length; ++i) {
                result = Math.max(result, d[i]);
            }
            max = Optional.of(result);
            return result;
        }
    }

    /*
     ******************************************************************************
     * Vect::min()
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
        if (min.isPresent()) {
            return min.get();
        } else {
            double result = d[0];
            for (int i = 1; i < d.length; ++i) {
                result = Math.min(result, d[i]);
            }
            min = Optional.of(result);
            return result;
        }
    }

    /*
     ******************************************************************************
     * Vect::norm()
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
        if (norm.isPresent()) {
            return norm.get();
        } else {
            double sum = 0;
            for (int i = 0; i < d.length; ++i) {
                sum += d[i] * d[i];
            }
            double result = Math.sqrt(sum);
            norm = Optional.of(result);
            return result;
        }
    }

    /*
     ******************************************************************************
     * Vect::plus()
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
     * \return (Vect) vector of added values.
     *
     * \callgraph
     ******************************************************************************
     */
    public Vect plus(double a) {
        Vect result = new Vect(new double[d.length]);
        for (int i=0; i<d.length; ++i) {
            result.d[i] = d[i]+a;
        }
        return result;
    }

    /*
     ******************************************************************************
     * Vect::times()
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
     * \return (Vect) vector of multiplied values.
     *
     * \callgraph
     ******************************************************************************
     */
    public Vect times(double a) {
        Vect result = new Vect(new double[d.length]);
        for (int i=0; i<d.length; ++i) {
            result.d[i] = a*d[i];
        }
        return result;
    }


}
