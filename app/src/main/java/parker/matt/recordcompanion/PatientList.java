package parker.matt.recordcompanion;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import parker.matt.recordcompanion.database.PatientsTable;
import parker.matt.recordcompanion.database.PatientCursorAdapter;

public class PatientList extends AppCompatActivity {

    // Database connection handler
    protected ListView patientListView;
    protected PatientsTable dbAdapter;
    protected Cursor dbCursor;
    protected PatientCursorAdapter pLVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        // Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup listview
        patientListView = (ListView) findViewById(R.id.patientList);
        dbAdapter = new PatientsTable(this);
        dbCursor = dbAdapter.getAll();
        pLVAdapter = new PatientCursorAdapter(this, dbCursor);

        patientListView.setAdapter(pLVAdapter);
        patientListView.setClickable(true);
        patientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                View item = patientListView.getChildAt(position);


                System.out.println("" + position);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume");

        // Reload the database
        dbCursor = dbAdapter.getAll();
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

    // Handle click on add patient button
    public void onClickAddPatient(View view) {
        Intent addPatientIntent = new Intent(getApplicationContext(), AddPatient.class);
        startActivity(addPatientIntent);

        // Reload Patient list on exit
        //populatePatientList();
    }
}


