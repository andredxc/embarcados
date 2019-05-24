package andre.metronome;

import android.util.Log;

/**
 * Created by Andre on 08-Sep-17.
 */

public class Singleton {

    private static Singleton _instance = null;
    private Metronome _metronome = null;

    private Singleton(){

        _metronome = new Metronome();
    }

    public static Singleton getInstance(){

        if(_instance == null){
            _instance = new Singleton();
            Log.v("Singleton", "Initializing new singleton instance");
        }

        return _instance;
    }

    public Metronome getMetronome(){
        return _metronome;
    }
}
