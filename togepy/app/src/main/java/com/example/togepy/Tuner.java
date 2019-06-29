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


public class Tuner extends AppCompatActivity {

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

//        Log.v("PitchDetector", "CHECKING FOR PERMISSION");
//        if(ContextCompat.checkSelfPermission(Tuner.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            Log.v("PitchDetector", "ASKING FOR PERMISSION");
            ActivityCompat.requestPermissions(Tuner.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
//        }
//        else{
//            Log.v("PitchDetector", "ALREADY HAD PERMISSION");
//        }
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
        TextView text = findViewById(R.id.bpmTextView);
        text.setText("agora vai");

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        TextView text = findViewById(R.id.bpmTextView);
                        text.setText("" + pitchInHz);
                    }
                });
            }
        };

        AudioDispatcher adp = AudioDispatcherFactory.fromDefaultMicrophone(22050, 2048, 0);
        adp.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, 44100, 2040, pdh));
        Thread pitchThread = new Thread(adp, "PitchDetector");
        pitchThread.start();
    }
}
