package com.example.togepy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Locale;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.PitchConverter;


public class Tuner extends AppCompatActivity {

    final float PITCH_BAR_MAX = 200;
    final int PITCH_HISTORY_LENGTH = 5;
    final int PITCH_LOWER_LIMIT = 10;
    final double PITCH_OPTIMAL_PCTG = 5;
    final double PITCH_GOOD_PCTG = 20;

    private float pitchFreq = -1;
    private float[] pitchHistory;
    private int historyIndex = 0;
    private double avgPitch = -1;
    private String noteName;
    private int currentStringNum = 6;
    private float tunerFreqOffset;  //Difference between the frequency captured by the mic and the tuner goal
    private ProgressBar pitchBar = null;
    AudioDispatcher audioDispatcher;
    public static final String[] notes = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
//    ProgressBar pitchBar;

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

        // Initialize pitch variables
        pitchHistory = new float[PITCH_HISTORY_LENGTH];

        // Set pitch bar
        pitchBar = findViewById(R.id.pitchBar);
        pitchBar.setScaleY(3f);
        pitchBar.setMax((int)PITCH_BAR_MAX);
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
                        Tuner.this.pitchHistory[historyIndex] = pitchInHz;
                        Tuner.this.updateDisplay();
                        historyIndex++;
                        if(historyIndex == PITCH_HISTORY_LENGTH){
                            historyIndex = 0;
                        }
                        Log.e("run", "pitch[" + historyIndex + "] = " + pitchInHz);
                    }
                });
            }
        };

        this.audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 2048, 0);
        audioDispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, 44100, 2048, pitchDetectionHandler));
        new Thread(audioDispatcher, "AudioDispatcher").start();
    }

    protected void updateDisplay(){

        // Calculate average pitch
        float sum = 0;
        for(int i = 0; i < PITCH_HISTORY_LENGTH; i++){
            if(pitchHistory[i] > PITCH_LOWER_LIMIT){
                sum += pitchHistory[i];
            }
        }
        avgPitch = sum/(float)PITCH_HISTORY_LENGTH;

        // Calculate standard deviation
        double sum2 = 0;
        for(int i = 0; i < PITCH_HISTORY_LENGTH; i++){
            if(pitchHistory[i] > PITCH_LOWER_LIMIT){
                sum2 += Math.sqrt(Math.pow(pitchHistory[i] - avgPitch, 2));
            }
        }
        sum2 = Math.sqrt(sum2/(float)PITCH_HISTORY_LENGTH);
        Log.v("debug", "" + sum2 + ", " + avgPitch + ", " + pitchFreq);



        // Calculate note, frequencies and offset
        int pitchMidi = PitchConverter.hertzToMidiKey(avgPitch);
        if(pitchMidi < 0){
            // Sometimes this functions returns a negative number unexpectedly
            pitchMidi = 0;
        }
        noteName = notes[pitchMidi%12];
        Log.v("debug", noteName);
        double noteFreq = PitchConverter.midiKeyToHertz(pitchMidi);
        double noteCent = PitchConverter.hertzToAbsoluteCent(noteFreq);
        double pitchCent = 0;
        if(avgPitch > 0){
            pitchCent = PitchConverter.hertzToAbsoluteCent(avgPitch);
        }
        double offset = noteCent - pitchCent;

        // Create strings
        String freqStr;
        if (avgPitch < PITCH_LOWER_LIMIT){
            // Too quiet
            freqStr = "";
            noteName = "Too quiet";
        }
        else{
            freqStr = String.format(Locale.getDefault(), "%.2f Hz", avgPitch);
        }

        // Update frequency text
        TextView freqText = findViewById(R.id.pitchFreq);
        freqText.setText(freqStr);
        // Update note text
        TextView noteText = findViewById(R.id.pitchNote);
        noteText.setText(this.noteName);

        // Update pitch progress bar
        // -50 < offset < 50
        float progress;
        if(avgPitch < PITCH_LOWER_LIMIT){
            progress = 0;
        }
        else{
            progress = (int)(((50.0+offset)/100.0)*PITCH_BAR_MAX);
            // Reflect the progress bar around the middle point
            progress = (progress < PITCH_BAR_MAX/2) ?
                    (progress + (PITCH_BAR_MAX/2-progress)*2) : (progress - (progress-PITCH_BAR_MAX/2)*2);
            Log.e("progress", "" + progress);

        }

        // Set bar color
        if(offset >= -PITCH_OPTIMAL_PCTG && offset < PITCH_OPTIMAL_PCTG){
            pitchBar.getProgressDrawable().setColorFilter(Color.GREEN,
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else if(offset >= -PITCH_GOOD_PCTG && offset < PITCH_GOOD_PCTG){
            pitchBar.getProgressDrawable().setColorFilter(Color.rgb(230, 230, 0),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else{
            pitchBar.getProgressDrawable().setColorFilter(Color.RED,
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }

        pitchBar.setProgress((int)progress);

        //Update tuner bar
        ProgressBar tunerBar = findViewById(R.id.tunerBar);
        tunerBar.setProgress((int)tunerFreqOffset + 100);
    }

    public void startTuner(View view){
        Button startTuningButton = findViewById(R.id.tunerStartButton);
        startTuningButton.setVisibility(View.INVISIBLE);
        TextView currentStringText = findViewById(R.id.currentString);
        currentStringText.setVisibility(View.VISIBLE);
        ProgressBar tunerBar = findViewById(R.id.tunerBar);
        tunerBar.setVisibility(View.VISIBLE);
        TextView tunerLowText = findViewById(R.id.tunerLowText);
        tunerLowText.setVisibility(View.VISIBLE);
        TextView tunerHighText = findViewById(R.id.tunerHighText);
        tunerHighText.setVisibility(View.VISIBLE);

        new Thread(new Runnable(){
            @Override
            public void run(){
                Log.v("Tuner", "STARTING TUNER");

                String[] tunerNoteGoals = {"E", "B", "G", "D", "A", "E"};   //From string 1 to string 6
                float[] tunerFreqGoals = {660, 494 ,392, 293, 219 ,164};
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
                    while(Tuner.this.noteName != tunerNoteGoals[Tuner.this.currentStringNum-1]){
                        Tuner.this.tunerFreqOffset = Tuner.this.pitchFreq - tunerFreqGoals[Tuner.this.currentStringNum-1];
                    }
                }

                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        TextView currentStringText = findViewById(R.id.currentString);
                        currentStringText.setText("Done tuning!");
                        Button startTuningButton = findViewById(R.id.tunerStartButton);
                        startTuningButton.setVisibility(View.VISIBLE);
                        ProgressBar tunerBar = findViewById(R.id.tunerBar);
                        tunerBar.setVisibility(View.INVISIBLE);
                        TextView tunerLowText = findViewById(R.id.tunerLowText);
                        tunerLowText.setVisibility(View.INVISIBLE);
                        TextView tunerHighText = findViewById(R.id.tunerHighText);
                        tunerHighText.setVisibility(View.INVISIBLE);
                    }
                });

                Log.v("Tuner", "DONE TUNING");
            }
        }, "Tuner").start();
    }
}
