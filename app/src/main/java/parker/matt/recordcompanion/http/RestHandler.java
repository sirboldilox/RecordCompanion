package parker.matt.recordcompanion.http;

import android.util.Log;

import java.io.File;
import java.net.URL;
import java.util.List;

import parker.matt.recordcompanion.models.Biometric;
import parker.matt.recordcompanion.models.ECG;
import parker.matt.recordcompanion.models.ID;
import parker.matt.recordcompanion.models.Patient;
import parker.matt.recordcompanion.models.Response;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.mime.TypedFile;

/**
 * Created by matt on 13/01/16.
 * Handles connection to the REST interface
 */
public class RestHandler {

    private static final String LOG_TAG = "RestHandler";

    private URL databaseURL;
    private String lastError;

    private RestAdapter restAdapter;
    private RestAPI restAPI;

    /**
     * Creates a new RestHandler object
     * @param databaseURL   Address of the database to connect to
     *                      e.g: http://database-server:8080
     */
    public RestHandler(URL databaseURL)
    {
        this.databaseURL = databaseURL;

        // Build interface
        restAdapter = new RestAdapter.Builder()
                .setServer(databaseURL.toString())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog("Retrofit API"))
                .build();

        restAPI = restAdapter.create(RestAPI.class);
    }

    /**
     * Get the full list of patients from the remote database
     * @return  List of patient records
     */
    public List<Patient> getPatientList()
    {
        Response<List<Patient>> response;
        response = restAPI.getPatientList();

        return response.response;
    }

    /**
     * Push a patient records using the REST API
     * @param patient   Patient record model to send
     * @return          ID of the patient in the remote database
     */
    public int pushPatient(Patient patient) {
        Log.d("RestHandler", String.format("Pushing patient: %s",
                patient.toString())
        );

        Response<ID> response;
        response = restAPI.putPatient(patient);

        return response.response.id;
    }

    /**
     * Push a biometric record using the REST API
     * @param biometric Biometric record model to send
     * @return          ID of the biometric in the remote database
     */
    public int pushBiometric(Biometric biometric) {
        Log.d(LOG_TAG, String.format("Pushing biometric: %s",
                biometric.toString()));

        Response<ID> response;
        response = restAPI.putBiometric(biometric);

        return response.response.id;
    }

    /**
     * Push an ECG info record using the REST API
     * @param ecg   ECG record model to send
     * @return      ID of the biometric in the remote database
     */
    public int pushECG(ECG ecg) {


        Response<ID> response;
        response = restAPI.putECG(ecg);

        return response.response.id;
    }

    /**
     * Push an ECGData file using the REST API
     * @param filepath  Path to the file to upload
     * @return          ID of the ECGData upload in the remote database
     */
    public int pushECGData(String filepath) {
        TypedFile ecgdata = new TypedFile("multipart/form-data", new File(filepath));

        Response<ID> response;
        response = restAPI.putECGData(ecgdata);

        return response.response.id;
    }

    public String getLastError() {
        return this.lastError;
    }
}
