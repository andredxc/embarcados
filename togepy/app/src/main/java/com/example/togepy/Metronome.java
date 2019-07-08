package com.example.togepy;

import com.example.togepy.R;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.TextView;
import android.net.Uri;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class Metronome {

    private final int MAX_BPM = 200;
    private final int MAX_TS = 20;
    private int _timeSignature1, _timeSignature2, _currentTimeSignature;
    private int _bpm, _accentNote, _currentNote;
    private boolean _isRunning;
    private long _lastTickTime, _tickPeriodMs;
    private Timer _timer;
    private Context _currentContext;
    private MediaPlayer _mediaPlayer = null, _accentMediaPlayer = null;
    private TextView _bpmTextView = null;
    private TextView _timeSignatureTopTextView = null;
    private TextView _timeSignatureBottomTextView = null;

    public Metronome(){

        setBpm(80);
        _isRunning = false;
        _currentNote = 1;
        _timeSignature1 = 4;
        _timeSignature2 = 0;
        _currentTimeSignature = 1;
        _accentNote = 1;
    }

    public void start(Context context){

        _currentContext = context;

        if(!_isRunning){

            _isRunning = true;
            _currentNote = 1;
            if(_mediaPlayer!=null)
                _mediaPlayer.release();
            if(_accentMediaPlayer!=null)
                _accentMediaPlayer.release();
            //_mediaPlayer = MediaPlayer.create(context, R.raw.low_seiko_sq50);
            try{
                _mediaPlayer = new MediaPlayer();
                _mediaPlayer.setDataSource(context, Uri.parse("android.resource://com.example.togepy/raw/low_seiko_sq50"));
                _mediaPlayer.prepare();
                _accentMediaPlayer = new MediaPlayer();
                _accentMediaPlayer.setDataSource(context, Uri.parse("android.resource://com.example.togepy/raw/high_seiko_sq50"));
                _accentMediaPlayer.prepare();

            } catch(Exception e) {
                Log.e("Metronome", e.toString());
            }
            //_accentMediaPlayer = MediaPlayer.create(context, R.raw.high_seiko_sq50);
            _tickPeriodMs =  (long) ((60.0/(float) getBpm())*1000.0);
            _timer = new Timer();
            Log.v("Metronome", "Starting metronome at " + getBpm() + " BPM ( " +
                    _tickPeriodMs + " milliseconds per beat)");

            _timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    tick();
                }
            }, 0, _tickPeriodMs);
        }
    }

    public void stop(){

        _mediaPlayer.release();
        _timer.cancel();
        _isRunning = false;
    }

    private void tick(){

        long currentPeriodMs;

        currentPeriodMs = System.currentTimeMillis() - _lastTickTime;
        _lastTickTime = System.currentTimeMillis();

        if(_currentNote == _accentNote){
            //Plays accent sound
            try{
                _accentMediaPlayer.start();
                Log.v("Metronome", "Time since last tick is " + currentPeriodMs + "ms ("
                        + getBpm() + " BPM)");
            }catch(Exception e){
                Log.e("Metronome", e.toString());
            }
        }
        else{
            //Plays regular sound
            try{
                _mediaPlayer.start();
                Log.v("Metronome", "Time since last tick is " + currentPeriodMs + "ms ("
                        + getBpm() + " BPM)");
            }catch(Exception e) {
                Log.e("Metronome", e.toString());
            }
        }

        Log.v("Metronome", "Played note " + _currentNote + " of " + _timeSignature1);
        _currentNote++;
        switch (_currentTimeSignature)
        {
            case 1:
                if(_currentNote > _timeSignature1){
                    _currentNote = 1;
                    if (_timeSignature2 > 0 )
                    _currentTimeSignature++;
                }
                break;

            case 2:
                if(_currentNote > _timeSignature2){
                    _currentNote = 1;
                    _currentTimeSignature = 1;
                }
                break;
        }

    }

    private void refreshBpmTextView(){

        if(_bpmTextView != null){
            _bpmTextView.setText(Integer.toString(getBpm()) + " BPM");
        }
    }
    private void refreshTimeSignatureTopTextView(){

        if(_timeSignatureTopTextView != null){
            _timeSignatureTopTextView.setText(Integer.toString(getTimeSignature1()) + "/4");
            Log.v("Time Signature set", "TS = " + _timeSignature1 + "/4");
        }
        Log.v("Time Signature set", "TS = " + _timeSignature1);
    }

    private void refreshTimeSignatureBottomTextView(){

        if(_timeSignatureBottomTextView != null){
            if (getTimeSignature2() == 0)
            {
                _timeSignatureBottomTextView.setText(" --");
            }
            else
            {
                _timeSignatureBottomTextView.setText(Integer.toString(getTimeSignature2()) + "/4");

            }
            Log.v("Time Signature set", "TS = " + _timeSignature2 + "/4");
        }
        Log.v("Time Signature set", "TS = " + _timeSignature2);
    }

    private float calculatePrecision(long currentPeriodMs){

        return (float) (((float) (currentPeriodMs - _tickPeriodMs)) / ((float)_tickPeriodMs/100.0));
    }

    //--------------------------------------------------SETTERS
    public void setBpmTextView(TextView textView){
        _bpmTextView = textView;
        refreshBpmTextView();
    }

    public void setTimeSignatureTopTextView(TextView textView){

        _timeSignatureTopTextView = textView;
        refreshTimeSignatureTopTextView();
    }

    public void setTimeSignatureBottomTextView(TextView textView){

        _timeSignatureBottomTextView = textView;
        refreshTimeSignatureBottomTextView();
    }

    public void setBpm(int bpm){

        if(bpm <= MAX_BPM){
            _bpm = bpm;
            refreshBpmTextView();
        }
        else{
            Log.v("Metronome", "BPM = " + bpm + " not allowed.");
        }
    }

    public void setTimeSignatureTop(int ts){

        if(ts < MAX_TS){
            _timeSignature1 = ts;

        }
        else{
            Log.v("Time Signature", "TS = " + _timeSignature1 + " not allowed.");
        }
        refreshTimeSignatureTopTextView();
    }

    public void setTimeSignatureBottom(int ts){

        if(ts < MAX_TS){
            _timeSignature2 = ts;

        }
        else{
            Log.v("Time Signature", "TS = " + _timeSignature2 + " not allowed.");
        }
        refreshTimeSignatureBottomTextView();
    }

    public void increaseBpm(int value){

        if(_bpm + value <= MAX_BPM){
            _bpm = _bpm + value;
            Log.v("Metronome", "BPM set to " + getBpm());
            if(_isRunning){
                stop();
                start(_currentContext);
            }
        }
        refreshBpmTextView();
    }

    public void decreaseBpm(int value){

        if(_bpm - value >= 1){
            _bpm = _bpm - value;
            Log.v("Metronome", "BPM set to " + getBpm());
            if(_isRunning){
                stop();
                start(_currentContext);
            }
            refreshBpmTextView();
        }
    }

    public void setTimeSignature1(int value){
        _timeSignature1 = value;
        Log.v("Time Signature", "TS = " + _timeSignature1);
        if(_isRunning){
            stop();
            start(_currentContext);
        }
        refreshTimeSignatureTopTextView();
    }

    public void setTimeSignature2(int value){
        _timeSignature2 = value;
        Log.v("Time Signature", "TS = " + _timeSignature2);
        if(_isRunning){
            stop();
            start(_currentContext);
        }
        refreshTimeSignatureBottomTextView();
    }

    public void setAccentNote(int value){
        switch (_currentTimeSignature)
        {
            case 1:
                if(value <= _timeSignature1)
                    _accentNote = value;
                break;

            case 2:
                if(value <= _timeSignature2)
                    _accentNote = value;
                break;
        }

    }

    //--------------------------------------------------GETTERS

    public int getBpm(){ return _bpm; }

    boolean getIsRunning(){ return _isRunning; }

    public int getTimeSignature1(){ return _timeSignature1; }

    public int getTimeSignature2(){ return _timeSignature2; }

    public int getAccentNote(){ return _accentNote; }
}
