package mobi.bitcoinnow;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Date;

import mobi.bitcoinnow.sync.BitcoinNowSyncAdapter;
import mobi.bitcoinnow.sync.CoinMapIntentService;

public class MainActivity
        extends AppCompatActivity
        implements NewsFragment.OnNewsFragmentInteractionListener {

    Fragment mainScreenFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0f);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getString(getString(R.string.pref_key_main_screen), "").equals(getString(R.string.title_maps))) {
            mainScreenFragment = CoinMapFragment.newInstance();
        } else {
            mainScreenFragment = new NewsFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mainScreenFragment)
                .commit();

        try {
            BitcoinNowSyncAdapter.initializeSyncAdapter(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getString(getString(R.string.pref_key_update_map), "").isEmpty()) {
            CoinMapIntentService.startActionUpdateVenues(getApplicationContext());
            Toast.makeText(this, (getString(R.string.pref_update_map_request) + " " + DateFormat.format("d MMM HH:mm", new Date())), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_map) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new CoinMapFragment())
                    .commit();
            return true;
        } else if (id == R.id.action_news) {
            BitcoinNowSyncAdapter.syncImmediately(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new NewsFragment())
                    .commit();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
