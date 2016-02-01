package parker.matt.recordcompanion;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import parker.matt.recordcompanion.database.Patient;
import parker.matt.recordcompanion.database.PatientsTable;

public class PatientRecord extends AppCompatActivity {

    public static final String EXTRA_PATIENT_ID = "patient_id";

    // Database connection handler
    protected PatientsTable dbAdapter = new PatientsTable(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_record);

        Intent intent = this.getIntent();
        long patient_id = intent.getLongExtra(EXTRA_PATIENT_ID, 0);

        TextView patientName = (TextView) findViewById(R.id.patientRecordName);
        createRecord(dbAdapter.getPatient((int) patient_id));
    }

    /**
     * Populate the record with data from the patient class
     */
    private void createRecord(Patient patient)
    {
        TextView patientName   = (TextView) findViewById(R.id.patientRecordName);
        TextView patientAge    = (TextView) findViewById(R.id.patientRecordAge);
        TextView patientGender = (TextView) findViewById(R.id.patientRecordGender);

        patientName.setText(patient.firstName + " " + patient.lastName);
        patientAge.setText("Age function not implemented");
        patientGender.setText(patient.getGenderString());
    }
}
