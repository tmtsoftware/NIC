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
 * \file Histogram.java
 * \brief This file implements a class that inserts data into a histogram.
 * <hr>
 ******************************************************************************
 */

package nic.util;

import static java.lang.Math.*;

/*
 ******************************************************************************
 * Histogram
 ******************************************************************************
 *//*!
 * \brief
 * This class accumulates data into a histogram and calculates running statistics.
 *
 * <b> Implementation Details: </b>
 * When constructed the Histogram is given a finite size and bin
 * spacing. The Histogram subsequently accumulates data through the update()
 * method.
 * Methods are provided to measure statistical properties of
 * the data.
 * The histogram size need not match the full extent of the data;
 * the min, mean, max etc. are calculated correctly even when data fall outside
 * the bounds (they are simply not added to the histogram).
 *
 * <hr>
 * \callgraph
 ******************************************************************************
 */

public class Histogram {

    /*-----------------------------------------------------------------------------
     * Public Attributes
     *---------------------------------------------------------------------------*/

    public final double d[];    /*!< fixed-size double array to hold the histogram */
    public final double binMin; /*!< lower bound of Histogram */
    public final double binMax; /*!< upper bound of Histogram */
    public final double binStep;/*!< Histogram bin sizes */
    public final int len;       /*!< number of bins in the Histogram */

    /*-------------------------------------------------------------------------
     * Private Attributes
     *-----------------------------------------------------------------------*/

    private final long warmup; /*!< ignore the first warmup samples in calls to update() */
    private long nHist;        /*!< number of values in histogram */
    private long nTotal;       /*!< total number of values, including those outside of the histogram */
    private long nUpdate;      /*!< number of times update() called (compared to warmup) */
    private double mu;         /*!< current calculation of the mean */
    private double m2;         /*!< running sum of squared deviation, used for variance calc */
    private double min;        /*!< current minimum data value */
    private double max;        /*!< current maximum data value */

    /*
     ******************************************************************************
     * Histogram::Histogram()
     ******************************************************************************
     *//*!
     * \brief
     * Base Histogram Constructor.
     *
     * <b> Implementation Details: </b>\n\n
     * The caller defines the extent and bin size of the Histogram. They also
     * provide a "warmup": if non-zero, the specified number of samples will be
     * ignored in call to update() before it begins to accumulate data and
     * calculate statistics. This feature was added to assist with the generation
     * of timing statistics: one normally wants to start collecting data once
     * the system has been running for a while and has reached a steady-state.
     *
     * \param[in] binMin (double) lower bound of the Histogram
     * \param[in] binMax (double) upper bound of the Histogram
     * \param[in] binStep (double) Histogram bin size
     * \param[in] warmup (long) how many samples to ignore until accumulation begins
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public Histogram(double binMin, double binMax, double binStep, long warmup) {
        // Allocate histogram with bins of size binStep between binMin and binMax
        this.binMin = binMin;
        this.binMax = binMax;
        this.binStep = binStep;
        this.warmup = warmup;
        len = (int) ceil((binMax-binMin)/binStep);
        d = new double[len];
        nHist = 0;
        nTotal = 0;
        mu = 0;
        m2 = 0;
        nUpdate=0;
    }

    /*
     ******************************************************************************
     * Histogram::Histogram()
     ******************************************************************************
     *//*!
     * \brief
     * Histogram constructor without warmup
     *
     * <b> Implementation Details: </b>\n\n
     * Identical to the base constructor, though with no warmup.
     *
     * \param[in] binMin (double) lower bound of the Histogram
     * \param[in] binMax (double) upper bound of the Histogram
     * \param[in] binStep (double) Histogram bin size
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public Histogram(double binMin, double binMax, double binStep) {
        this(binMin, binMax, binStep, 0);
    }

    /*
     ******************************************************************************
     * Histogram::Update()
     ******************************************************************************
     *//*!
     * \brief
     * Update the histogram with an array of data.
     *
     * <b> Implementation Details: </b>\n\n
     * Any of the samples from x that lie within the bounds of the histogram are
     * added. Regardless of whether samples lie within these bounds, basic statistics
     * like the min, max, mean, and variance will be calculated using all values.
     * Any data that are accumulated prior to exceeding the warmup threshold are
     * completely ignored (neither added to the histogram, not accounted for in
     * statistics).
     *
     * \param[in] x (double[]) array of data.
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public void update(double x[]) {
        // Add an array of values to the histogram and update running stats
        for (int i=0; i<x.length; ++i) {
            update(x[i]);
        }
    }

    /*
     ******************************************************************************
     * Histogram::Update()
     ******************************************************************************
     *//*!
     * \brief
     * Update the histogram with a single value.
     *
     * <b> Implementation Details: </b>\n\n
     * If x lies within the bounds of the histogram it is added to the appropriate
     * bin. Regardless of whether it lies within these bounds, basic statistics
     * like the min, max, mean, and variance will be calculated using this value.
     * Any data that are accumulated prior to exceeding the warmup threshold are
     * completely ignored (neither added to the histogram, not accounted for in
     * statistics).
     *
     * \param[in] x (double) array of data.
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public void update(double x) {
        // If we haven't gotten enough warmup samples yet, skip
        if (++nUpdate <= warmup) {
            return;
        }

        // Add a value to the histogram and update running stats
        if (nTotal == 0) {
            min = x;
            max = x;
        } else {
            if (x > max) {
                max = x;
            }
            if (x < min) {
                min = x;
            }
        }

        int bin = (int) floor(((x-binMin)/binStep));
        if ( (bin >=0) && (bin<len) ) {
            d[bin]++;
            nHist++;
        }

        // Update running sums required for statistics
        nTotal++;
        double delta = x - mu;
        mu += delta / nTotal;
        m2 += delta * (x - mu);
    }

    /*
     ******************************************************************************
     * Histogram::getMin()
     ******************************************************************************
     *//*!
     * \brief
     * Return the minimum value of previously accumulated data.
     *
     * <b> Implementation Details: </b>\n\n
     * The minimum is calculated as the minimum of all data entered using the
     * update() method (after the warmup period), regardless of whether it lies
     * within the bounds of the Histogram
     *
     * \return (double) the minimum value
     *
     * \callgraph
     ******************************************************************************
     */
    public double getMin() throws ArithmeticException {
        if (nTotal == 0) {
            throw new ArithmeticException("No values have been added.");
        }
        return min;
    }

    /*
     ******************************************************************************
     * Histogram::getMax()
     ******************************************************************************
     *//*!
     * \brief
     * Return the maximum value of previously accumulated data.
     *
     * <b> Implementation Details: </b>\n\n
     * The maximum is calculated as the maximum of all data entered using the
     * update() method (after the warmup period), regardless of whether it lies
     * within the bounds of the Histogram
     *
     * \return (double) the maximum value
     *
     * \callgraph
     ******************************************************************************
     */
    public double getMax() throws ArithmeticException {
        if (nTotal == 0) {
            throw new ArithmeticException("No values have been added.");
        }
        return max;
    }

    /*
     ******************************************************************************
     * Histogram::getMean()
     ******************************************************************************
     *//*!
     * \brief
     * Return the mean value, \f$\mu\f$, of previously accumulated data.
     *
     * <b> Implementation Details: </b>\n\n
     * The mean is calculated as the mean of all data entered using the
     * update() method (after the warmup period), regardless of whether it lies
     * within the bounds of the Histogram
     *
     * \return (double) the mean value
     *
     * \callgraph
     ******************************************************************************
     */
    public double getMean() throws ArithmeticException {
        if (nTotal == 0) {
            throw new ArithmeticException("No values have been added.");
        }
        return mu;
    }

    /*
     ******************************************************************************
     * Histogram::getVariance()
     ******************************************************************************
     *//*!
     * \brief
     * Return the unbiased sample variance
     *
     * <b> Implementation Details: </b>\n\n
     * This method calculates the unbiased sample variance:
     * \f[
     *     \sigma^2 = \frac{1}{n-1} \sum_i (x_i - \mu)^2
     * \f]
     * where \f$n\f$ is the number of data points, \f$x_i\f$ are the data accumulated using
     * update(), and \f$\mu\f$ is the mean of the data.
     *
     * The variance is calculated using all data entered with the
     * update() method (after the warmup period), regardless of whether it lies
     * within the bounds of the Histogram.
     *
     * \return (double) the unbiased sample variance
     *
     * \callgraph
     * ******************************************************************************
     */
    public double getVariance() throws ArithmeticException {
        if (nTotal < 2) {
            throw new ArithmeticException("Unable to calculate variance with < 2 samples.");
        }
        return m2 / (nTotal-1);
    }

    /*
     ******************************************************************************
     * Histogram::getStdev()
     ******************************************************************************
     *//*!
     * \brief
     * Return the corrected sample standard deviation
     *
     * <b> Implementation Details: </b>\n\n
     * This method calculates the corrected sample standard deviation:
     * \f[
     *     \sigma = \sqrt{ \frac{1}{n-1} \sum_i (x_i - \mu)^2 }
     * \f]
     * where \f$n\f$ is the number of data points, \f$x_i\f$ are the data accumulated using
     * update(), and \f$\mu\f$ is the mean of the data.
     *
     * The standard deviation is calculated using all data entered with the
     * update() method (after the warmup period), regardless of whether it lies
     * within the bounds of the Histogram.
     *
     * \return (double) the corrected sample standard deviation
     *
     * \callgraph
     * ******************************************************************************
     */
    public double getStdev() {
        return sqrt(getVariance());
    }

    /*
     ******************************************************************************
     * Histogram::toString()
     ******************************************************************************
     *//*!
     * \brief
     * String representation of the Histogram, including basic statistics.
     *
     * <b> Implementation Details: </b>\n\n
     *
     * \return (String) string representation
     *
     * \callgraph
     ******************************************************************************
     */
    @Override
    public String toString() {
        String retVal;
        try {
            StringBuffer sb = new StringBuffer();

            sb.append("Min:" + getMin() + " Mean:" + getMean() + " Std:" + getStdev() + " Max:" + getMax() + " Samples:" + nTotal + "\n");
            for (int i = 0; i < len; ++i) {
                if (d[i] != 0) {
                    sb.append((binMin + i * binStep) + ": " + d[i] + "\n");
                }
            }

            retVal = sb.toString();
        } catch (ArithmeticException e) {
            retVal = "Stats unavailable due to: "+e;
        }
        return retVal;
    }

    // --- Simpler Getters  ----------------------------------------------------------------------------------------

    /*! @copydoc nHist */
    public long getnHist() {
        return nHist;
    }

    /*! @copydoc nTotal */
    public long getnTotal() {
        return nTotal;
    }
}
