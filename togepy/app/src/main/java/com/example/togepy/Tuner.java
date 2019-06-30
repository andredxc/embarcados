package com.example.togepy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.PitchConverter;


public class Tuner extends AppCompatActivity {
    private float pitchFreq = -1;
    AudioDispatcher audioDispatcher;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_metronome:
                    Log.e("andre", "Metronome");
                    startActivity(new Intent(Tuner.this, MainActivity.class));
                    overridePendingTransition(R.anim.fadein,
                            R.anim.fadeout);
                    return true;
                case R.id.navigation_tuner:
                    Log.e("andre", "Tuner");
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuner);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.setSelectedItemId(R.id.navigation_tuner);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.v("PitchDetector", "ACTIVITY STARTED");

        Log.v("PitchDetector", "ASKING FOR PERMISSION");
        ActivityCompat.requestPermissions(Tuner.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.v("PitchDetector", "ACTIVITY PAUSED");
        if(this.audioDispatcher != null) {
            Log.v("PitchDetector", "STOPPING DISPATCHER");
            this.audioDispatcher.stop();
            this.audioDispatcher = null;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.v("PitchDetector", "PERMISSION GRANTED");
                    this.detectPitch();
                }
                else{
                    Log.v("PitchDetector", "PERMISSION DENIED");
                }
                return;
            default:
                Log.v("PitchDetector", "Invalid permission request code!");
        }
    }

    protected void detectPitch() {
        Log.v("PitchDetector", "RUNNING");

        PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        Tuner.this.pitchFreq = pitchInHz;
                        Tuner.this.updateDisplay();
                    }
                });
            }
        };

        this.audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 2048, 0);
        audioDispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, 44100, 2048, pitchDetectionHandler));
        new Thread(audioDispatcher, "AudioDispatcher").start();
    }

    protected void updateDisplay(){
        int pitchMidi = PitchConverter.hertzToMidiKey(Double.valueOf(this.pitchFreq));
        String[] notes = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
        String noteName = notes[pitchMidi%12];

        if(this.pitchFreq == -1){
            noteName = "Too quiet";
        }

        TextView freqText = findViewById(R.id.pitchFreq);
        freqText.setText("" + this.pitchFreq);
        TextView noteText = findViewById(R.id.pitchNote);
        noteText.setText("" + noteName);
    }
}
