package com.hissain.speechrecognizer.utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;


import com.hissain.speechrecognizer.utils.MFCC;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MfccExtractor {

    private Context context;
    private MFCC mfcc;

    public MfccExtractor(Context context) {
        this.context = context;
        this.mfcc = new MFCC();
    }

    public float[] extractMfccFromWav(int resId) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(resId);

        // Read WAV file header to skip it
        byte[] header = new byte[44]; // WAV header is 44 bytes
        inputStream.read(header);

        // Read WAV data as raw PCM
        byte[] audioBytes = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            audioBytes = inputStream.readAllBytes();
        }

        // Convert byte data to PCM double data
        double[] audioBuffer = bytesToDoubleArray(audioBytes);

        // Process the audio buffer through MFCC
        return mfcc.process(audioBuffer);
    }

    private double[] bytesToDoubleArray(byte[] audioBytes) {
        int length = audioBytes.length / 2; // 16-bit audio
        double[] audioBuffer = new double[length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < length; i++) {
            audioBuffer[i] = byteBuffer.getShort() / 32768.0; // Convert to [-1.0, 1.0] range
        }

        return audioBuffer;
    }
}
