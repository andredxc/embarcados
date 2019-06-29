package com.example.togepy;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private Metronome _metronome = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_metronome:
                    Log.e("andre", "Metronome");
                    return true;
                case R.id.navigation_tuner:
                    Log.e("andre", "Tuner");
                    startActivity(new Intent(MainActivity.this, Tuner.class));
                    overridePendingTransition(R.anim.fadein,
                            R.anim.fadeout);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        mTextMessage = findViewById(R.id.message);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        _metronome = new Metronome();
    }

    //Handles "start metronome" XML button
    public void activateMetronome(View view){

        if(_metronome.getIsRunning()){
            //Metronome is running
            _metronome.stop();
            Button mButton = (Button)findViewById(R.id.activate_metronome);
            mButton.setText(R.string.start_metronome);
        }
        else{
            _metronome.setBpmTextView((TextView) findViewById(R.id.bpmTextView));
            _metronome.setTimeSignatureTopTextView((TextView) findViewById(R.id.timeSignatureTopTextView));
            _metronome.start(this);
            Button mButton = (Button)findViewById(R.id.activate_metronome);
            mButton.setText(R.string.stop_metronome);
        }
    }

    //Handles time changing XML buttons
    public void changeMetronomeTime(View view){

        switch(view.getId()){

            case R.id.timePlusOne:
                _metronome.increaseBpm(1);
                break;
            case R.id.timePlusFive:
                _metronome.increaseBpm(5);
                break;
            case R.id.timeMinusOne:
                _metronome.decreaseBpm(1);
                break;
            case R.id.timeMinusFive:
                _metronome.decreaseBpm(5);
                break;
            default:
                Log.e("Internal", "Unrecognized button id");
        }

    }
    public void changeTimeSignature(View view){

        switch(view.getId()){

            case R.id.timeSignatureTop:
                int ts = _metronome.getTimeSignature1();
                ts+=1;
                if (ts > 20){
                    ts = 1;
                }
                _metronome.setTimeSignature1(ts);
                break;
            case R.id.timeSignatureBottom:
                break;
            default:
                Log.e("Internal", "Unrecognized button id");
        }

    }
}
