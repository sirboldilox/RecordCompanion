package parker.matt.recordcompanion;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import parker.matt.recordcompanion.database.PatientsTable;
import parker.matt.recordcompanion.database.PatientCursorAdapter;

public class PatientList extends AppCompatActivity {

    // Database connection handler
    protected PatientsTable dbAdapter;
    protected Cursor dbCursor;

    // List view
    protected ListView patientListView;
    protected PatientCursorAdapter pLVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        // Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup database
        dbAdapter = new PatientsTable(this);
        dbCursor = dbAdapter.getAll();

        // Setup ListView and PatientCursorAdapter
        patientListView = (ListView) findViewById(R.id.patientList);
        pLVAdapter = new PatientCursorAdapter(this, dbCursor);

        patientListView.setAdapter(pLVAdapter);
        patientListView.setClickable(true);
        patientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Long patient_id = pLVAdapter.getItemId(position);
                Log.d("PatientList", String.format("Item %d was clicked: _id: %d.", position, patient_id));

                // Start the activity
                Intent pRecordIntent = new Intent(getApplicationContext(), PatientRecord.class);
                pRecordIntent.putExtra(PatientRecord.EXTRA_PATIENT_ID, patient_id);
                startActivity(pRecordIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("PatientList", "onResume");

        // Reload the database
        dbCursor = dbAdapter.getAll();
        pLVAdapter.changeCursor(dbCursor);
        pLVAdapter.notifyDataSetChanged();
    }

    public void debug_test() {
        TextView patietnListDebug = (TextView) findViewById(R.id.patientListDebug);
        Cursor debugCursor;
        String debugString = "Debug";

        // Test database
        debugCursor = dbAdapter.getAll();
        for (String element: debugCursor.getColumnNames())
            debugString += " " + element;

        patietnListDebug.setText(debugString);
    }

    /**
     * Handle clicking on add patient button
     */
    public void onClickAddPatient(View view) {
        Intent addPatientIntent = new Intent(getApplicationContext(), AddPatient.class);
        startActivity(addPatientIntent);
    }
}


