package parker.matt.recordcompanion;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import parker.matt.recordcompanion.sync.SyncHandler;
import parker.matt.recordcompanion.sync.SyncService1;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Open the settings activity
        if (id == R.id.action_settings) {
            Log.d(LOG_TAG, "Settings option clicked");

            Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onClickPatientList(View view) {
        // Handle launching the PatientDBModel list activity
        Intent patientListIntent = new Intent(getApplicationContext(), PatientList.class);
        startActivity(patientListIntent);
    }

    /**
     * Manually run a database sync
     * @param view
     */
    public void onClickDatabaseSync(View view) {
        // Run the database sync and update the field
        final TextView syncStatus = (TextView) findViewById(R.id.databaseSyncStatusText);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = new Intent(this, SyncService1.class);
        startService(intent);
        Log.d(LOG_TAG, "Starting Sync service");

        // Setup database URL
        String URLString = preferences.getString("sync_url", "NULL");
        URL databaseURL;

        try {
            databaseURL = new URL(URLString);
        } catch(MalformedURLException url_exc) {
            Log.e(LOG_TAG, "Bad URL: " + URLString);
            syncStatus.setText("Bad database URL");
            return;
        }

        // Start the sync
        SyncHandler syncHandler = new SyncHandler(this, databaseURL, syncStatus);
        syncHandler.execute();
    }

    public void onClickBluetoothTest(View view) {
        // Test the bluetooth connection
        Intent bluetoothTestIntent = new Intent(getApplicationContext(), BluetoothTest.class);
        startActivity(bluetoothTestIntent);
    }
}
