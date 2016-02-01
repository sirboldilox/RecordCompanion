package parker.matt.recordcompanion;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import parker.matt.recordcompanion.database.SyncHandler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickPatientList(View view) {
        // Handle launching the Patient list activity
        Intent patientListIntent = new Intent(getApplicationContext(), PatientList.class);
        startActivity(patientListIntent);
    }

    public void onClickDatabaseSync(View view) {
        // Run the database sync and update the field
        final TextView syncStatus = (TextView) findViewById(R.id.databaseSyncStatusText);
        //final String URLString = "http://boldilox.co.uk:8081";
        final String URLString = "http://hrsdb0:8080";

        // Setup database URL
        URL databaseURL;
        try {
            databaseURL = new URL(URLString);
        } catch(MalformedURLException url_exc) {
            Log.d("MainActivity", "Bad URL: " + URLString);
            syncStatus.setText("Bad database URL");
            return;
        }

        // Start the sync
        SyncHandler syncHandler = new SyncHandler(this, databaseURL, syncStatus);
        syncHandler.execute();
    }
}
