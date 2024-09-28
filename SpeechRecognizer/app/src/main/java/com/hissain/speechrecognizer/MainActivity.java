package com.hissain.speechrecognizer;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
                // do MFCC processing
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
