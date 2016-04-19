package parker.matt.recordcompanion.sync;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.net.URL;
import java.util.List;

import parker.matt.recordcompanion.database.DatabaseHelper;
import parker.matt.recordcompanion.http.RestHandler;
import parker.matt.recordcompanion.models.Biometric;
import parker.matt.recordcompanion.models.ECG;
import parker.matt.recordcompanion.models.Patient;
import retrofit.RetrofitError;

/**
 * AysncTask for synchronising the local and remote databases.
 * This handler is generally used with the SyncService1 class to perform
 * background data syncs.
 */

public class SyncHandler extends AsyncTask<Void, Integer, Void> {

    private static final String LOG_TAG = "SyncHandler";

    private static final int PART_PATIENT   = 0;
    private static final int PART_BIOMETRIC = 1;
    private static final int PART_ECG       = 2;

    private Context context;
    private URL databaseURL;
    DatabaseHelper dbAdapter;

    private RestHandler restHandler;

    private int part;
    private String lastError;
    private TextView progressText;

    /**
     * @param context       The current context
     * @param databaseURL   URL of the database REST api
     * @param progressText  View to write progress to
     */
    public SyncHandler(Context context, URL databaseURL, TextView progressText)
    {
        this.context = context;
        this.databaseURL = databaseURL;
        this.progressText = progressText;
    }

    /**
     * Executes a database sync operation
     *
     * @return True if successful, false if errors occurred.
     */
    @Override
    protected Void doInBackground(Void... params) {
        restHandler = new RestHandler(databaseURL);
        dbAdapter = new DatabaseHelper(context);

        syncPatients();
        syncBiometrics();
        syncECG();
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressText.setText("Pushing new records");
    }

    /**
     *  Update progress string during sync
     *  @param values   Status indicators:
     *      [0] Status: 0 = PatientDBModel, 1 = Biometrics, 2 = ECG
     *      [1] Current item
     *      [2] Total number of items
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Integer percent = (values[1] / values[2]) * 100;
        switch (values[0]) {
            case PART_PATIENT:
                progressText.setText(String.format("Synchronising patient records: %d%%", percent));
                break;
            case PART_BIOMETRIC:
                progressText.setText(String.format("Synchronising biometric records: %d%%", percent));
                break;
            case PART_ECG:
                progressText.setText(String.format("Synchronising ECG records: %d%%", percent));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressText.setText("Sync complete");
    }

    /**
     *  Synchronises patient records
     */
    private void syncPatients() {
        Cursor newPatients = dbAdapter.getNewPatients();
        Integer total = newPatients.getCount();
        Patient patient;
        boolean error = false;

        part = PART_PATIENT;
        Log.d(LOG_TAG, "Synchronising patients");

        // Push new records
        while (newPatients.moveToNext()) {
            patient = new Patient(
                    newPatients.getInt(newPatients.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                    newPatients.getString(newPatients.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME)),
                    newPatients.getString(newPatients.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME)),
                    newPatients.getString(newPatients.getColumnIndexOrThrow(DatabaseHelper.COL_DOB)),
                    newPatients.getInt(newPatients.getColumnIndexOrThrow(DatabaseHelper.COL_GENDER)));

            // Push the patient
            Integer remote_id;
            Integer local_id = patient.id;

            try {
                remote_id = restHandler.pushPatient(patient);
                dbAdapter.updatePatientID(local_id, remote_id);
            } catch (RetrofitError e) {
                Log.e(LOG_TAG, "Exception when pushing patient: ", e);
                error = true;
                break;
            }

            publishProgress(part, newPatients.getPosition(), total);
        }


        if(!error) {
            try {
                List<Patient> patients = restHandler.getPatientList();
                dbAdapter.updatePatients(patients);
            } catch (RetrofitError e) {
                Log.e(LOG_TAG, "Exception when fetching patients: ", e);
            }
        }

        newPatients.close();
    }

    /**
     *  Synchronises biometric records
     */
    private void syncBiometrics() {
        Cursor pushedPatients = dbAdapter.getNonNewPatients();
        Cursor newBiometrics;
        Integer patient_id;
        Biometric biometric;

        part = PART_BIOMETRIC;
        Log.d(LOG_TAG, "Synchronising biometrics");

        // Iterate over synced patient records
        while (pushedPatients.moveToNext()) {
            patient_id = pushedPatients.getInt(
                    pushedPatients.getColumnIndexOrThrow(DatabaseHelper.COL_ID)
            );

            newBiometrics = dbAdapter.getPatientBiometrics(patient_id);

            // Push each biometric for this patient
            while(newBiometrics.moveToNext()) {
                biometric = new Biometric(
                        newBiometrics.getInt(newBiometrics.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                        newBiometrics.getString(newBiometrics.getColumnIndexOrThrow(DatabaseHelper.COL_VALUE)),
                        newBiometrics.getInt(newBiometrics.getColumnIndexOrThrow(DatabaseHelper.COL_BIOTYPE_ID)),
                        newBiometrics.getInt(newBiometrics.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_ID)),
                        newBiometrics.getString(newBiometrics.getColumnIndexOrThrow(DatabaseHelper.COL_TIMESTAMP))
                );

                // Push the biometric record
                try {
                    restHandler.pushBiometric(biometric);
                    dbAdapter.deleteBiometric(biometric.id);
                } catch (RetrofitError e) {
                    Log.e(LOG_TAG, "Exception when pushing biometric: ", e);
                    break;
                }
            }
        }
    }

    /**
     *  Synchronises ECG records
     */
    private void syncECG() {
        Cursor pushedPatients = dbAdapter.getNonNewPatients();
        Cursor newECGs;
        Integer patient_id, ecgdata_id;
        ECG ecg;

        part = PART_ECG;
        Log.d(LOG_TAG, "Synchronising ECGs");

        // Iterate over synced patient records
        while (pushedPatients.moveToNext()) {
            patient_id = pushedPatients.getInt(
                        pushedPatients.getColumnIndexOrThrow(DatabaseHelper.COL_ID)
            );

            newECGs = dbAdapter.getPatientECGs(patient_id);

            // Push each ECG for this patient
            while(newECGs.moveToNext()) {

                try {
                    // Upload the ECGData file
                    ecgdata_id = restHandler.pushECGData(
                            newECGs.getString(newECGs.getColumnIndexOrThrow(DatabaseHelper.COL_FILENAME)));

                    ecg = new ECG(
                            newECGs.getInt(newECGs.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                            newECGs.getInt(newECGs.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_ID)),
                            newECGs.getFloat(newECGs.getColumnIndexOrThrow(DatabaseHelper.COL_SAMPLING_FREQ)),
                            ecgdata_id,
                            newECGs.getString(newECGs.getColumnIndexOrThrow(DatabaseHelper.COL_TIMESTAMP))
                    );

                    restHandler.pushECG(ecg);
                    dbAdapter.deleteECG(ecg.id);
                } catch (RetrofitError e) {
                    Log.e(LOG_TAG, "Exception when pushing ECG: ", e);
                    break;
                }
            }
        }
    }

    /**
     * Returns the last know error by the handler
     */
    public String getLastError(){
        return this.lastError;
    }
}
