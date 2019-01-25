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
 * \file Histogram.java
 * \brief This file implements the nic.util.Histogram class for accumulating data into a histogram.
 * <hr>
 ******************************************************************************
 */

package nic.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
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
 * the bounds (compare getnTotal(), getMin(), and getMax() to getnHist(),
 * gethistLowBound(), and gethistUpBound(), respectively, to get a sense of
 * how the range of the histogram compares to the input data).
 *
 * Examples:
 *
 * \code
 * // Create histogram with 10 bins from 0 to 10 labeled "test"
 * Histogram h = new Histogram(0,10,1, Optional.of("test"));
 *
 * // Add a single value to the histogram
 * h.update(5.);
 *
 * // Add several values to the histogram
 * h.update(new double[]{7.5, 0.1, 1.});
 *
 * // Get the min, mean, max, and standard deviation of the accumulated values
 * double min = h.getMin();
 * double mean = h.getMean();
 * double max = h.getMax();
 * double stdev = h.getStdev();
 *
 * // Add a value beyond the bounds of the histogram
 * h.update(-100);
 *
 * // The minimum value will correctly report -100 even though it is
 * // beyond the lower bound of the histogram:
 * min = h.getMin();
 *
 * // You can find out the total number of items added in this way:
 * long nTotal = h.getnTotal();
 *
 * // You can also find out how many landed only within the histogram bounds this way:
 * long nHist = h.getnHist();
 *
 * // Nicely-formatted text output that can be plotted with daoPlotHist.py (from daoToolkit):
 * string histStr = h.toString()
 *
 * \endcode
 *
 * For more examples see tests in HistogramTest.java .
 *
 * <hr>
 * \callgraph
 ******************************************************************************
 */
public class Histogram {

    /*-----------------------------------------------------------------------------
     * Public Attributes
     *---------------------------------------------------------------------------*/

    /** \brief Delimeter for printed histograms. Ensure consistency with DAO_STATS_HIST_DELIM in daoLib */
    public static final String HIST_DELIM = "[][][][][][][][][][][][][][][][][][][][][][][][][][][][]";

    /*-------------------------------------------------------------------------
     * Private Attributes
     *-----------------------------------------------------------------------*/

    // Determined at construction time
    private final long _d[];   /*!< fixed-size long array to hold the histogram */
    private final double _histLowBound;/*!< lower bound of Histogram */
    private final double _histUpBound; /*!< upper bound of Histogram */
    private final double _binSize;/*!< Histogram bin step sizes */
    private final int _nBins;  /*!< number of bins in the Histogram */
    private final Optional<String> _label;/*!< Label for this histogram */

    // Continually updated
    private final long _warmup;/*!< ignore the first warmup samples in calls to update() */
    private long _nHist;       /*!< number of values in histogram */
    private long _nTotal;      /*!< total number of values, including those outside of the histogram */
    private long _nUpdate;     /*!< number of times update() called (compared to warmup) */
    private double _mean;      /*!< current calculation of the mean */
    private double _sumSqDev;  /*!< running sum of squared deviation, used for variance calc */
    private double _min;       /*!< current minimum data value */
    private double _max;       /*!< current maximum data value */
    private long _iMax;        /*!< call # to update() at which maximum occurred */
    private Instant _tMax;     /*!< time at which the maximum occurred */

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
     * \param[in] histLowBound (double) lower bound of the Histogram
     * \param[in] histUpBound (double) upper bound of the Histogram
     * \param[in] binSize (double) Histogram bin size
     * \param[in] warmup (long) how many samples to ignore until accumulation begins
     * \param[in] label (Optional<String>) Optional label for the Histogram.
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public Histogram(double histLowBound, double histUpBound, double binSize, long warmup, Optional<String> label) throws IllegalArgumentException {
        if (warmup < 0) {
            throw new IllegalArgumentException("warmup ("+warmup+") must be >= 0");
        }

        // Allocate histogram with bins of size binSize between histLowBound and histUpBound
        _histLowBound = histLowBound;
        _histUpBound = histUpBound;
        _binSize = binSize;
        _warmup = warmup;
        _label = label;
        _nBins = (int) ceil((_histUpBound - _histLowBound)/_binSize);
        _d = new long[_nBins];
        _nHist = 0;
        _nTotal = 0;
        _mean = 0;
        _sumSqDev = 0;
        _nUpdate =0;
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
     * \param[in] histLowBound (double) lower bound of the Histogram
     * \param[in] histUpBound (double) upper bound of the Histogram
     * \param[in] binStep (double) Histogram bin size
     * \param[in] label (Optional<String>) Optional label for the Histogram.
     *
     * \return N/A
     *
     * \callgraph
     ******************************************************************************
     */
    public Histogram(double histLowBound, double histUpBound, double binStep, Optional<String> label) {
        this(histLowBound, histUpBound, binStep, 0, label);
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
        if (++_nUpdate <= _warmup) {
            return;
        }

        // Add a value to the histogram and update running stats
        if (_nTotal == 0) {
            _min = x;
            _max = x;
            _iMax = _nUpdate;
            _tMax = Instant.now();
        } else {
            if (x > _max) {
                _max = x;
                _iMax = _nUpdate;
                _tMax = Instant.now();
            }
            if (x < _min) {
                _min = x;
            }
        }

        int bin = (int) floor(((x- _histLowBound)/ _binSize));
        if ( (bin >=0) && (bin< _nBins) ) {
            _d[bin]++;
            _nHist++;
        }

        // Update running sums required for statistics
        _nTotal++;
        double delta = x - _mean;
        _mean += delta / _nTotal;
        _sumSqDev += delta * (x - _mean);
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
        if (_nTotal == 0) {
            throw new ArithmeticException("No values have been added.");
        }
        return _min;
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
        if (_nTotal == 0) {
            throw new ArithmeticException("No values have been added.");
        }
        return _max;
    }

    /*
     ******************************************************************************
     * Histogram::getiMax()
     ******************************************************************************
     *//*!
     * \brief
     * Return the index at which the maximum occurred.
     *
     * <b> Implementation Details: </b>\n\n
     * The return value is the number of times update() was called including warmup
     * at which the maximum occured.
     *
     * \return (long) the index
     *
     * \callgraph
     ******************************************************************************
     */
    public long getiMax() throws ArithmeticException {
        if (_nTotal == 0) {
            throw new ArithmeticException("No values have been added.");
        }
        return _iMax;
    }

    /*
     ******************************************************************************
     * Histogram::gettMax()
     ******************************************************************************
     *//*!
     * \brief
     * Return the time at which the maximum occurred.
     *
     * <b> Implementation Details: </b>\n\n
     *
     * \return (Instant) the time
     *
     * \callgraph
     ******************************************************************************
     */
    public Instant gettMax() throws ArithmeticException {
        if (_nTotal == 0) {
            throw new ArithmeticException("No values have been added.");
        }
        return _tMax;
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
        if (_nTotal == 0) {
            throw new ArithmeticException("No values have been added.");
        }
        return _mean;
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
        if (_nTotal < 2) {
            throw new ArithmeticException("Unable to calculate variance with < 2 samples.");
        }
        return _sumSqDev / (_nTotal -1);
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
     * The format of the string should match daoStats_printHistogram().
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

            sb.append(Histogram.HIST_DELIM);

            _label.ifPresent(l->{
               sb.append("\n\n"+l+"\n\n");
            });

            sb.append("Min: " + getMin() + " Mean: " + getMean() + " SD: " + getStdev() + "\n" +
                    "Max: " + getMax() + " @ " + _tMax + " (sample " + _iMax + ")\n");

            sb.append("Histogram (bin label represents lower bound)\n");

            for (int i = 0; i < _nBins; ++i) {
                if (_d[i] != 0) {
                    sb.append((_histLowBound + i * _binSize) + ": " + _d[i] + "\n");
                }
            }

            retVal = sb.toString();
        } catch (ArithmeticException e) {
            retVal = "Stats unavailable due to: "+e;
        }
        return retVal;
    }

    // --- Simpler Getters  ----------------------------------------------------------------------------------------

    /*! @copydoc _d */
    public long[] getd() { return Arrays.copyOf(_d,_d.length); }

    /*! @copydoc _histLowBound */
    public double gethistLowBound() { return _histLowBound; }

    /*! @copydoc _histUpBound */
    public double gethistUpBound() { return _histUpBound; }

    /*! @copydoc _binSize */
    public double getbinSize() { return _binSize; }

    /*! @copydoc _nHist */
    public long getnHist() {
        return _nHist;
    }

    /*! @copydoc _nTotal */
    public long getnTotal() {
        return _nTotal;
    }
}
