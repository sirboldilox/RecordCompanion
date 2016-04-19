package parker.matt.recordcompanion.http;

import java.util.List;

import parker.matt.recordcompanion.models.Biometric;
import parker.matt.recordcompanion.models.ECG;
import parker.matt.recordcompanion.models.ID;
import parker.matt.recordcompanion.models.Patient;
import parker.matt.recordcompanion.models.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Rest API definition for retrofit
 */
public interface RestAPI {

    // Patient interface
    @GET("/patient")
    Patient getPatient();

    @GET("/patients")
    Response<List<Patient>> getPatientList();

    @PUT("/patient/0")
    Response<ID> putPatient(@Body Patient patient);

    // Biometric interface
    @PUT("/biometric/0")
    Response<ID> putBiometric(@Body Biometric biometric);

    // ECG interface
    @PUT("/ecg")
    Response<ID> putECG(@Body ECG ecg);

    // ECG data interface
    @Multipart
    @PUT("/ecgdata")
    Response<ID> putECGData(@Part("data") TypedFile ecgdata);

}
