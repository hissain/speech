package com.hissain.speechrecognizer.utils;

/**
 * Fast Fourier Transform (FFT).
 *
 * Optimized version of the in-place FFT algorithm.
 * This implementation assumes the input length is a power of 2.
 *
 * @author Md. Sazzad Hissain Khan
 */
public class FFT {
    double[] real;
    double[] imag;

    /**
     * Performs Fast Fourier Transform in place on the given signal.
     *
     * @param signal the input signal (assumed to be a power of 2 in length)
     */
    public void process(double[] signal) {
        final int numPoints = signal.length;

        // Initialize real and imag arrays
        real = signal;
        imag = new double[numPoints];

        // Bit-reversal sorting
        bitReversal(numPoints);

        // Perform FFT using decimation-in-time (DIT) radix-2 algorithm
        computeFFT(numPoints);
    }

    /**
     * Perform bit-reversal on the real and imaginary arrays.
     *
     * @param numPoints number of points (length of the arrays)
     */
    private void bitReversal(int numPoints) {
        final int halfNumPoints = numPoints >> 1;
        int j = halfNumPoints;

        for (int i = 1; i < numPoints - 1; i++) {
            if (i < j) {
                // Swap real[i] with real[j] and imag[i] with imag[j]
                swap(i, j);
            }

            int k = halfNumPoints;
            while (k <= j) {
                j -= k;
                k >>= 1;
            }
            j += k;
        }
    }

    /**
     * Swap values in the real and imaginary arrays.
     *
     * @param i first index
     * @param j second index
     */
    private void swap(int i, int j) {
        double tempReal = real[j];
        double tempImag = imag[j];
        real[j] = real[i];
        imag[j] = imag[i];
        real[i] = tempReal;
        imag[i] = tempImag;
    }

    /**
     * Compute the FFT through a decimation-in-time radix-2 algorithm.
     *
     * @param numPoints number of points (length of the arrays)
     */
    private void computeFFT(int numPoints) {
        final double pi = Math.PI;
        final int numStages = (int) (Math.log(numPoints) / Math.log(2));

        for (int stage = 1; stage <= numStages; stage++) {
            int LE = 1 << stage;  // LE = 2^stage
            final int LE2 = LE >> 1;
            final double theta = pi / LE2;
            final double SR = Math.cos(theta);  // Cosine value
            final double SI = -Math.sin(theta); // Sine value

            // Loop for each sub-DFT
            for (int subDFT = 0; subDFT < LE2; subDFT++) {
                double UR = 1.0;
                double UI = 0.0;

                // Butterfly computation
                for (int butterfly = subDFT; butterfly < numPoints; butterfly += LE) {
                    int ip = butterfly + LE2;

                    // Butterfly calculation
                    double tempReal = real[ip] * UR - imag[ip] * UI;
                    double tempImag = real[ip] * UI + imag[ip] * UR;

                    real[ip] = real[butterfly] - tempReal;
                    imag[ip] = imag[butterfly] - tempImag;
                    real[butterfly] += tempReal;
                    imag[butterfly] += tempImag;
                }

                // Update trigonometric values
                double tempUR = UR;
                UR = tempUR * SR - UI * SI;
                UI = tempUR * SI + UI * SR;
            }
        }
    }
}
