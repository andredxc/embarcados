package andre.metronome;

import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Sets volume buttons to adjust metronome volume instead of ringtone volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //Initializes internal classes
        Singleton.getInstance();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //There are currently no items on the action bar
        //int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.drawer_metronome){
            // Handle the metronome action

        } else if (id == R.id.drawer_information){
            // Handle the metronome action

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Handles "start metronome" XML button
    public void activateMetronome(View view){

        if(Singleton.getInstance().getMetronome().getIsRunning()){
            //Metronome is running
            Singleton.getInstance().getMetronome().stop();
            Button mButton = (Button)findViewById(R.id.activate_metronome);
            mButton.setText(R.string.start_metronome);
        }
        else{
            Singleton.getInstance().getMetronome().setBpmTextView((TextView) findViewById(R.id.bpmTextView));
            Singleton.getInstance().getMetronome().start(this);
            Button mButton = (Button)findViewById(R.id.activate_metronome);
            mButton.setText(R.string.stop_metronome);
        }
    }

    //Handles time changing XML buttons
    public void changeMetronomeTime(View view){

        switch(view.getId()){

            case R.id.timePlusOne:
                Singleton.getInstance().getMetronome().increaseBpm(1);
                break;
            case R.id.timePlusFive:
                Singleton.getInstance().getMetronome().increaseBpm(5);
                break;
            case R.id.timeMinusOne:
                Singleton.getInstance().getMetronome().decreaseBpm(1);
                break;
            case R.id.timeMinusFive:
                Singleton.getInstance().getMetronome().decreaseBpm(5);
                break;
            default:
                Log.e("Internal", "Unrecognized button id");
        }

    }
}
