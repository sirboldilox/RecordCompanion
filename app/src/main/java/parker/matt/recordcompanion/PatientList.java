package parker.matt.recordcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import parker.matt.recordcompanion.database.PatientsTable;

public class PatientList extends AppCompatActivity {

    // Database connection handler
    protected PatientsTable dbAdapter = new PatientsTable(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        populatePatientList();
    }

    public void populatePatientList(){
        ListView patientListView = (ListView) findViewById(R.id.patientList);
 //       String[] patientNames = new String[]{"John Smith", "Matt Parker", "David Mulvaney", "Foo Bar"};

        SimpleCursorAdapter pLVAdapter = new SimpleCursorAdapter(this,
                R.layout.listview_patient_entry,
                dbAdapter.getAllNames(),
                new String[] { "name" },
                new int[] { R.id.patientEntryName });

        patientListView.setAdapter(pLVAdapter);
    }

    public void onClickAddPatient(View view) {
        Intent addPatientIntent = new Intent(getApplicationContext(), AddPatient.class);
        startActivity(addPatientIntent);
    }
}
