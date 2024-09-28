/**
 * Mel-Frequency Cepstral Coefficients.
 *
 * @author Md. Sazzad Hissain Khan
 *
 */

package com.hissain.speechrecognizer.utils;

public class MFCC {
    private static final int N_MFCC = 20;
    private static final double F_MIN = 0.0;
    private static final int N_FFT = 2048;
    private static final int HOP_LENGTH = 512;
    private static final int N_MELS = 128;
    private static final double SAMPLE_RATE = 16000.0;
    private static final double F_MAX = SAMPLE_RATE / 2.0;

    private final FFT fft = new FFT();

    public float[] process(double[] doubleInputBuffer) {
        double[][] mfccResult = dctMfcc(doubleInputBuffer);
        return flatten(mfccResult);
    }

    private float[] flatten(double[][] mfccSpecTro) {
        int totalLength = mfccSpecTro.length * mfccSpecTro[0].length;
        float[] flattened = new float[totalLength];
        for (int i = 0, k = 0; i < mfccSpecTro.length; i++) {
            for (int j = 0; j < mfccSpecTro[0].length; j++, k++) {
                flattened[k] = (float) mfccSpecTro[i][j];
            }
        }
        return flattened;
    }

    private double[][] dctMfcc(double[] y) {
        double[][] spectrogram = powerToDb(melSpectrogram(y));
        double[][] dctBasis = dctFilter(N_MFCC, N_MELS);
        double[][] mfccResult = new double[N_MFCC][spectrogram[0].length];

        for (int i = 0; i < N_MFCC; i++) {
            for (int j = 0; j < spectrogram[0].length; j++) {
                for (int k = 0; k < N_MELS; k++) {
                    mfccResult[i][j] += dctBasis[i][k] * spectrogram[k][j];
                }
            }
        }
        return mfccResult;
    }

    private double[][] melSpectrogram(double[] y) {
        double[][] melFilter = melFilter();
        double[][] spectrogram = stftMagSpec(y);
        double[][] melSpectrogram = new double[N_MELS][spectrogram[0].length];

        for (int i = 0; i < N_MELS; i++) {
            for (int j = 0; j < spectrogram[0].length; j++) {
                for (int k = 0; k < spectrogram.length; k++) {
                    melSpectrogram[i][j] += melFilter[i][k] * spectrogram[k][j];
                }
            }
        }
        return melSpectrogram;
    }

    private double[][] stftMagSpec(double[] y) {
        double[] fftwin = getWindow();
        double[] ypad = padSignal(y);

        double[][] frames = frameSignal(ypad);
        double[][] fftmagSpec = new double[1 + N_FFT / 2][frames[0].length];

        for (int i = 0; i < frames[0].length; i++) {
            double[] fftFrame = new double[N_FFT];
            for (int j = 0; j < N_FFT; j++) {
                fftFrame[j] = fftwin[j] * frames[j][i];
            }
            double[] magSpec = magSpectrogram(fftFrame);
            System.arraycopy(magSpec, 0, fftmagSpec, 0, magSpec.length);
        }
        return fftmagSpec;
    }

    private double[] magSpectrogram(double[] frame) {
        fft.process(frame);
        double[] magSpec = new double[frame.length / 2 + 1];
        for (int i = 0; i < magSpec.length; i++) {
            magSpec[i] = fft.real[i] * fft.real[i] + fft.imag[i] * fft.imag[i];
        }
        return magSpec;
    }

    private double[] padSignal(double[] y) {
        double[] ypad = new double[N_FFT + y.length];
        System.arraycopy(y, 0, ypad, N_FFT / 2, y.length);
        return ypad;
    }

    private double[][] frameSignal(double[] ypad) {
        int nFrames = 1 + (ypad.length - N_FFT) / HOP_LENGTH;
        double[][] frames = new double[N_FFT][nFrames];
        for (int i = 0; i < nFrames; i++) {
            System.arraycopy(ypad, i * HOP_LENGTH, frames[i], 0, N_FFT);
        }
        return frames;
    }

    private double[][] melFilter() {
        double[] fftFreqs = fftFreq();
        double[] melF = melFreq(N_MELS + 2);
        double[][] filterbank = new double[N_MELS][fftFreqs.length];

        for (int i = 1; i < melF.length - 1; i++) {
            for (int j = 0; j < fftFreqs.length; j++) {
                if (fftFreqs[j] >= melF[i - 1] && fftFreqs[j] <= melF[i + 1]) {
                    double melDiff = melF[i + 1] - melF[i - 1];
                    filterbank[i - 1][j] = Math.max(0, (melF[i] - fftFreqs[j]) / melDiff);
                }
            }
        }
        return filterbank;
    }

    private double[] fftFreq() {
        double[] freqs = new double[1 + N_FFT / 2];
        for (int i = 0; i < freqs.length; i++) {
            freqs[i] = i * SAMPLE_RATE / N_FFT;
        }
        return freqs;
    }

    private double[] melFreq(int numMels) {
        double[] melPoints = new double[numMels];
        double melMin = hzToMel(F_MIN);
        double melMax = hzToMel(F_MAX);
        for (int i = 0; i < numMels; i++) {
            melPoints[i] = melToHz(melMin + i * (melMax - melMin) / (numMels - 1));
        }
        return melPoints;
    }

    private double hzToMel(double hz) {
        return 2595.0 * Math.log10(1.0 + hz / 700.0);
    }

    private double melToHz(double mel) {
        return 700.0 * (Math.pow(10.0, mel / 2595.0) - 1.0);
    }

    private double[][] dctFilter(int nMfcc, int nMel) {
        double[][] basis = new double[nMfcc][nMel];
        double scale = Math.sqrt(2.0 / nMel);

        for (int i = 0; i < nMfcc; i++) {
            for (int j = 0; j < nMel; j++) {
                basis[i][j] = scale * Math.cos(i * (j + 0.5) * Math.PI / nMel);
            }
        }
        return basis;
    }

    private double[][] powerToDb(double[][] melSpectrogram) {
        double[][] logMelSpectrogram = new double[melSpectrogram.length][melSpectrogram[0].length];
        for (int i = 0; i < melSpectrogram.length; i++) {
            for (int j = 0; j < melSpectrogram[i].length; j++) {
                logMelSpectrogram[i][j] = 10.0 * Math.log10(Math.max(1e-10, melSpectrogram[i][j]));
            }
        }
        return logMelSpectrogram;
    }

    private double[] getWindow() {
        double[] fftwin = new double[N_FFT];
        for (int i = 0; i < N_FFT; i++) {
            fftwin[i] = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * i / N_FFT);
        }
        return fftwin;
    }
}

