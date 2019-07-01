package com.example.togepy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.PitchConverter;


public class Tuner extends AppCompatActivity {

    private float pitchFreq = -1;
    private String noteName;
    private int currentStringNum = 6;
    private ProgressBar pitchBar = null;
    AudioDispatcher audioDispatcher;
    final int PITCH_BAR_MAX = 200;

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

        // Set bottom navigation view
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.setSelectedItemId(R.id.navigation_tuner);

        // Set pitch bar
        pitchBar = findViewById(R.id.pitchBar);
        pitchBar.setMax(PITCH_BAR_MAX);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.v("Tuner", "ACTIVITY STARTED");

        Log.v("Tuner", "ASKING FOR PERMISSION");
        ActivityCompat.requestPermissions(Tuner.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.v("Tuner", "ACTIVITY PAUSED");
        if(this.audioDispatcher != null) {
            Log.v("Tuner", "STOPPING DISPATCHER");
            this.audioDispatcher.stop();
            this.audioDispatcher = null;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.v("Tuner", "PERMISSION GRANTED");
                    this.detectPitch();
                }
                else{
                    Log.v("Tuner", "PERMISSION DENIED");
                }
                return;
            default:
                Log.v("Tuner", "Invalid permission request code!");
        }
    }

    protected void detectPitch() {
        Log.v("Tuner", "RUNNING");

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
        this.noteName = notes[pitchMidi%12];

        double noteFreq = PitchConverter.midiKeyToHertz(pitchMidi);
        double noteCent = PitchConverter.hertzToAbsoluteCent(noteFreq);
        double pitchCent = 0;
        if(this.pitchFreq > 0){
            pitchCent = PitchConverter.hertzToAbsoluteCent(this.pitchFreq);
        }
        double offset = noteCent - pitchCent;

        // Create strings
        String freqStr, offsetStr;
        if (this.pitchFreq == -1){
            // Too quiet
            freqStr = "";
            offsetStr = "";
            this.noteName = "Too quiet";
        }
        else{
            freqStr = String.format(Locale.getDefault(), "%.2f Hz", this.pitchFreq);
            offsetStr = String.format(Locale.getDefault(),"%.2f", offset);
        }

        // Update frequency text
        TextView freqText = findViewById(R.id.pitchFreq);
        freqText.setText(freqStr);
        // Update note text
        TextView noteText = findViewById(R.id.pitchNote);
        noteText.setText(this.noteName);
        // Update offset text
        TextView offsetText = findViewById(R.id.pitchOffset);
        offsetText.setText(offsetStr);

        // Update pitch progress bar
        // -50 < offset < 50
        int progress = (int)(((50.0+offset)/100.0)*(float) PITCH_BAR_MAX);
        pitchBar.setProgress(progress);
    }

    public void startTuner(View view){
        Button startTuningButton = findViewById(R.id.tunerStartButton);
        startTuningButton.setVisibility(View.INVISIBLE);
        TextView currentStringText = findViewById(R.id.currentString);
        currentStringText.setVisibility(View.VISIBLE);

        new Thread(new Runnable(){
            @Override
            public void run(){
                Log.v("Tuner", "STARTING TUNER");

                String[] tunerGoals = {"E", "B", "G", "D", "A", "E"};   //From string 1 to string 6
                //Goes through all 6 strings starting from string 6 (lowest string)
                for(Tuner.this.currentStringNum = 6; Tuner.this.currentStringNum >= 1; Tuner.this.currentStringNum--){
                    //Updates the text on screen
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            TextView currentStringText = findViewById(R.id.currentString);
                            currentStringText.setText("String " + Tuner.this.currentStringNum);
                        }
                    });
                    while(Tuner.this.noteName != tunerGoals[Tuner.this.currentStringNum-1]){
                        //Do nothing (waits for the right note to proceed)
                    }
                }

                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        TextView currentStringText = findViewById(R.id.currentString);
                        currentStringText.setText("Done tuning!");
                        Button startTuningButton = findViewById(R.id.tunerStartButton);
                        startTuningButton.setVisibility(View.VISIBLE);
                    }
                });

                Log.v("Tuner", "DONE TUNING");
            }
        }, "Tuner").start();
    }
}
