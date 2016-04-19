package parker.matt.recordcompanion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import parker.matt.recordcompanion.database.BioTypeCursorAdapter;
import parker.matt.recordcompanion.database.DatabaseHelper;
import parker.matt.recordcompanion.database.RecentBioCursorAdapter;
import parker.matt.recordcompanion.models.Patient;

public class PatientRecord extends AppCompatActivity {

    private static final String LOG_TAG = "PatientRecord";

    public static final String EXTRA_PATIENT_ID = "patient_id";

    private long patient_id;

    // Database connection handlers
    private DatabaseHelper dbAdapter;

    // Listview
    private ListView recordBioTypeLV;
    private ListView recentBioLV;
    private BioTypeCursorAdapter btLVAdapter;
    private RecentBioCursorAdapter recentBioLVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_record);

        // Setup database handlers
        dbAdapter = new DatabaseHelper(this);

        // Build basic patient record
        Intent intent = this.getIntent();
        patient_id = intent.getLongExtra(EXTRA_PATIENT_ID, 0);

        createRecord(dbAdapter.getPatient((int) patient_id));

        // Populate recent recordings
        recentBioLV = (ListView) findViewById(R.id.recordBioRecentList);
        recentBioLVAdapter = new RecentBioCursorAdapter(this, dbAdapter.getPatientBiometrics((int) patient_id));
        recentBioLV.setEmptyView(findViewById(R.id.recentBioEntryEmpty));
        recentBioLV.setAdapter(recentBioLVAdapter);


        // Setup ListView and adapter for recording
        recordBioTypeLV = (ListView) findViewById(R.id.recordBioTypeList);
        btLVAdapter = new BioTypeCursorAdapter(this, dbAdapter.getAllBioTypes());

        recordBioTypeLV.setAdapter(btLVAdapter);
        recordBioTypeLV.setClickable(true);
        recordBioTypeLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Long bioType_id = btLVAdapter.getItemId(position);
                Intent pRecordIntent;

                Log.d(LOG_TAG, String.format("Item %d was clicked: _id: %d", position, bioType_id));

                // Start the activity
                if (bioType_id == DatabaseHelper.ID_ECG) {
                    // Set ECG activity
                    pRecordIntent = new Intent(getApplicationContext(), RecordECG.class);
                    pRecordIntent.putExtra(RecordECG.EXTRA_PATIENT_ID, patient_id);
                } else {
                    // Set generic biometric activity
                    pRecordIntent = new Intent(getApplicationContext(), RecordBiometric.class);
                    pRecordIntent.putExtra(RecordBiometric.EXTRA_PATIENT_ID, patient_id);
                    pRecordIntent.putExtra(RecordBiometric.EXTRA_BIOTYPE_ID, bioType_id);
                }
                startActivity(pRecordIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");

        // Reload the database
        recentBioLVAdapter.changeCursor(dbAdapter.getPatientBiometrics((int) patient_id));
        recentBioLVAdapter.notifyDataSetChanged();
    }

    /**
     * Populate the record with data from the patient class
     */
    private void createRecord(Patient patient)
    {
        TextView patientName   = (TextView) findViewById(R.id.patientRecordName);
        TextView patientID     = (TextView) findViewById(R.id.patientRecordID);
        TextView patientGender = (TextView) findViewById(R.id.patientRecordGender);
        TextView patientAge    = (TextView) findViewById(R.id.patientRecordAge);

        patientName.setText(String.format("%s", patient.getNameString()));
        patientID.setText(String.format("ID: %d", patient.id));
        patientGender.setText(patient.getGenderString());
        patientAge.setText(String.format("Age %s", patient.getAgeString()));
    }

    /**
     * Expand the record biometric options
     * @param view
     */
    public void onClickRecordBiometric(View view) {
//        final TextView biometricText = (TextView) findViewById(R.id.patientRecordBiometric);

    }
}
