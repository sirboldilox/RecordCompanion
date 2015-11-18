package parker.matt.recordcompanion;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import parker.matt.recordcompanion.database.PatientsTable;
import parker.matt.recordcompanion.database.PatientCursorAdapter;

public class PatientList extends AppCompatActivity {

    // Database connection handler
    protected PatientsTable dbAdapter = new PatientsTable(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populatePatientList();
    }

    public void debug_test() {
        TextView patietnListDebug = (TextView) findViewById(R.id.patientListDebug);
        Cursor debugCursor;
        String debugString = "Debug";

        // Test database
        debugCursor = dbAdapter.getAllNames();
        for (String element: debugCursor.getColumnNames())
            debugString += " " + element;

        patietnListDebug.setText(debugString);

    }

    public void populatePatientList() {
        ListView patientListView = (ListView) findViewById(R.id.patientList);
        PatientCursorAdapter pLVAdapter = new PatientCursorAdapter(this, dbAdapter.getAllNames());
        patientListView.setAdapter(pLVAdapter);
    }

    public void onClickAddPatient(View view) {
        Intent addPatientIntent = new Intent(getApplicationContext(), AddPatient.class);
        startActivity(addPatientIntent);

        // Reload Patient list on exit
        populatePatientList();
    }
}


