package parker.matt.recordcompanion;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import parker.matt.recordcompanion.database.Patient;
import parker.matt.recordcompanion.database.PatientsTable;

public class PatientRecord extends AppCompatActivity {

    private static final String EXTRA_PATIENT_ID = "patient_id";

    // Database connection handler
    protected PatientsTable dbAdapter = new PatientsTable(this);

    public static void start(Context context, int patient_id) {
        Intent pRecordIntent = new Intent(context, PatientRecord.class);
        pRecordIntent.putExtra(EXTRA_PATIENT_ID, patient_id);
        context.startActivity(pRecordIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        int patient_id = intent.getIntExtra(EXTRA_PATIENT_ID, 0);

        createRecord(dbAdapter.getPatient(patient_id));
        setContentView(R.layout.activity_patient_record);
    }

    // Populate the record with data from the patient class
    private void createRecord(Patient patient)
    {
        TextView patientName   = (TextView) findViewById(R.id.patientRecordName);
        TextView patientAge    = (TextView) findViewById(R.id.patientRecordAge);
        TextView patientGender = (TextView) findViewById(R.id.patientRecordGender);

        patientName.setText(patient.firstName + " " + patient.lastName);
        patientAge.setText("Unknown");
    }
}
