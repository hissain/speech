package com.hissain.speechrecognizer;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hissain.speechrecognizer.utils.MfccExtractor;

import java.io.IOException;

import androidx.activity.ComponentActivity;

public class MainActivity extends ComponentActivity {

    private MediaPlayer mediaPlayer;
    private Button buttonFFT;
    private Button buttonMFFC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        buttonFFT = findViewById(R.id.button_fft);
        buttonMFFC = findViewById(R.id.button_mfcc);

        buttonMFFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MfccExtractor extractor = new MfccExtractor(MainActivity.this);
                try {
                    // Use the resource ID of the file in /res/raw/
                    float[] mfccs = extractor.extractMfccFromWav(R.raw.sample);
                    Log.i("MainActivity", mfccs.toString());
                    Toast.makeText(MainActivity.this, "mfcc length: " + mfccs.length, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        // Load and play the .wav file
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.sample); // `sample` is the name of your .wav file without extension
        mediaPlayer.start(); // Start playing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Release the MediaPlayer when done
            mediaPlayer = null;
        }
    }
}
