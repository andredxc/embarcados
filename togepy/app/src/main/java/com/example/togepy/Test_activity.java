package com.example.togepy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Test_activity extends AppCompatActivity {

    // Copiar iss para a nova activity
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_metronome:
                    Log.e("andre", "Metronome");
                    startActivity(new Intent(Test_activity.this, MainActivity.class));
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
    // Fim do copiar isso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_activity);

        // Copiar isso para a nova activity
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.setSelectedItemId(R.id.navigation_tuner);
        // Fim do copiar isso
    }
}
