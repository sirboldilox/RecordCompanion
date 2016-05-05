package parker.matt.recordcompanion.database;

/**
 * Created by matt on 11/11/15.
 */

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import parker.matt.recordcompanion.models.Biometric;
import parker.matt.recordcompanion.models.BiometricType;
import parker.matt.recordcompanion.models.ECG;
import parker.matt.recordcompanion.models.Patient;

/**
 * PatientDBModel SQL data table
 *
 * Store generic information that all patients should have.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = "DatabaseHelper";

    // Database settings
    private static final String DATABASE_NAME = "health_records";
    private static final int DATABASE_VERSION = 11;

    // Time format
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");

    // Table names
    private static final String TABLE_PATIENTS   = "Patients";
    private static final String TABLE_BIOMETRICS = "Biometrics";
    private static final String TABLE_BIOTYPES   = "Biometric_Types";
    private static final String TABLE_ECG        = "ECG";

    // Column names
    public  static final String COL_ID         = "_id";
    public  static final String COL_FIRST_NAME = "first_name";
    public  static final String COL_LAST_NAME  = "last_name";
    public  static final String COL_DOB        = "date_of_birth";
    public  static final String COL_GENDER     = "gender";
    public  static final String COL_NEW        = "new";

    public static final String COL_VALUE      = "value";
    public static final String COL_TIMESTAMP  = "timestamp";
    public static final String COL_BIOTYPE_ID = "biometrictype_id";
    public static final String COL_PATIENT_ID = "patient_id";

    public static final String COL_NAME   = "name";
    public static final String COL_UNITS  = "units";

    public static final String COL_SAMPLING_FREQ = "sampling_freq";
    public static final String COL_FILENAME      = "filename";

    // Static table data
    public static final int ID_HEIGHT = 1;
    public static final int ID_WEIGHT = 2;
    public static final int ID_BP = 3;
    public static final int ID_ECG = 10;

    private static final int[]    STATIC_IDS   = {ID_HEIGHT, ID_WEIGHT, ID_BP, ID_ECG};
    private static final String[] STATIC_NAMES = {"Height", "Weight", "Blood pressure", "ECG"};
    private static final String[] STATIC_UNITS = {"cm", "kg", "mm Hg", "mV"};

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /***
     * Generate the local SQLite database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create Patients table
        db.execSQL("CREATE TABLE " + TABLE_PATIENTS + " ( "
                        + COL_ID + " integer primary key, "
                        + COL_FIRST_NAME + " text not null, "
                        + COL_LAST_NAME + " text not null, "
                        + COL_DOB + " dateOfBirth text not null, "
                        + COL_GENDER + " gender integer not null, "
                        + COL_NEW + " new integer );"
        );

        // Create BiometricTypes table
        db.execSQL("CREATE TABLE " + TABLE_BIOTYPES + " ( "
                        + COL_ID + " integer primary key, "
                        + COL_NAME + " text not null, "
                        + COL_UNITS + " text not null );"
        );

        // Create Biometrics table
        db.execSQL("CREATE TABLE " + TABLE_BIOMETRICS + " ( "
                        + COL_ID + " integer primary key, "
                        + COL_VALUE + " text not null, "
                        + COL_TIMESTAMP + " text not null, "
                        + COL_BIOTYPE_ID + " integer not null, "
                        + COL_PATIENT_ID + " integer not null, "
                        + "FOREIGN KEY (" + COL_BIOTYPE_ID + ") REFERENCES "
                            + TABLE_BIOTYPES + "(" + COL_ID + ") ON UPDATE CASCADE, "
                        + "FOREIGN KEY (" + COL_PATIENT_ID + ") REFERENCES "
                            + TABLE_PATIENTS + "(" + COL_ID + ") ON UPDATE CASCADE );"
        );

        // Create ECG table
        db.execSQL("CREATE TABLE " + TABLE_ECG + " ( "
                        + COL_ID + " integer primary key, "
                        + COL_FILENAME + " text not null, "
                        + COL_SAMPLING_FREQ + " real not null, "
                        + COL_TIMESTAMP + " text not null, "
                        + COL_PATIENT_ID + " integer not null, "
                        + "FOREIGN KEY (" + COL_PATIENT_ID + ") REFERENCES "
                            + TABLE_PATIENTS + "(" + COL_ID + ") ON UPDATE CASCADE );"
        );

        // populate table with static data
        for (int i = 0; i < STATIC_NAMES.length; i++) {
            ContentValues cv;
            cv = new ContentValues();

            Log.d(LOG_TAG, "Adding biometric type: " + STATIC_NAMES[i]);
            cv.put(COL_ID, STATIC_IDS[i]);
            cv.put(COL_NAME, STATIC_NAMES[i]);
            cv.put(COL_UNITS, STATIC_UNITS[i]);
            db.insert(TABLE_BIOTYPES, COL_ID, cv);
        }
    }

    /***
     * Enable foreign key support in SQLite
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     *  Drop the database during upgrades
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ECG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BIOMETRICS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BIOTYPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        onCreate(db);
    }

    /**
     * Add a new patient to the database table
     */
    public boolean addPatient(Patient patient, Boolean isNew) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        Log.d(LOG_TAG, patient.toString());

        cv.put(COL_FIRST_NAME, patient.first_name);
        cv.put(COL_LAST_NAME, patient.last_name);
        cv.put(COL_DOB, patient.date_of_birth);
        cv.put(COL_GENDER, patient.gender);
        cv.put(COL_NEW, isNew);
        db.insert(TABLE_PATIENTS, COL_ID, cv);
        db.close();

        return true;
    }

    /**
     * Returns a Cursor with all the patient names in the database
     */
    public Cursor getAllPatients()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(LOG_TAG, "getAllPatients");

        return db.query(TABLE_PATIENTS, null, null, null, null, null, null);
    }

    /**
     * Returns a Cursor with all patient records that have been added
     * since the last database sync
     */
    public Cursor getNewPatients()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(LOG_TAG, "getNewPatients");

        return db.query(TABLE_PATIENTS, null, COL_NEW + "=1", null, null, null, COL_ID);
    }

    /***
     * Fetches all patient records that have been syncronised
     */
    public Cursor getNonNewPatients()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(LOG_TAG, "getNonNewPatients");

        return db.query(TABLE_PATIENTS, null, COL_NEW + "=0", null, null, null, COL_ID);
    }

    /**
     * Returns a PatientDBModel record by id or null if no patient was found
     */
    public Patient getPatient(int id) {
        Patient patient;
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(LOG_TAG, String.format("getPatient(%d)", id));

        Cursor pCursor = db.query(TABLE_PATIENTS,
                null,
                COL_ID + "=?",
                new String[]{Integer.toString(id)},
                null, null, null, null);

        // Check valid cursor
        if (pCursor.getCount() == 0) {
            return null;
        }

        // Build a patient object from the cursor data
        pCursor.moveToNext();
        patient = new Patient(id,
                pCursor.getString(pCursor.getColumnIndexOrThrow(COL_FIRST_NAME)),
                pCursor.getString(pCursor.getColumnIndexOrThrow(COL_LAST_NAME)),
                pCursor.getString(pCursor.getColumnIndexOrThrow(COL_DOB)),
                pCursor.getInt(pCursor.getColumnIndexOrThrow(COL_GENDER))
        );
        pCursor.close();
        return patient;
    }

    /**
     * Update a patient record ID in the database for concurrency
     * Also clears the "new" flag to indicat a record is in sync
     * @param currentID Current ID of the patient in the database
     * @param newID     The ID to change to
     */
    public void updatePatientID(Integer currentID, Integer newID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, String.format("Update patient ID: %d -> %d", currentID, newID));

        cv.put(COL_ID, newID);
        cv.put(COL_NEW, false);

        db.update(TABLE_PATIENTS, cv, COL_ID + "=?", new String[]{Integer.toString(currentID)});
        db.close();
    }

    /**
     * Updates a patient record in the database with new data
     * @param patient
     */
    public void updatePatient(Patient patient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, String.format("Update patient: %s", patient.toString()));

        cv.put(COL_FIRST_NAME, patient.first_name);
        cv.put(COL_LAST_NAME, patient.last_name);
        cv.put(COL_GENDER, patient.gender);
        cv.put(COL_DOB, patient.date_of_birth);
        cv.put(COL_NEW, false);

        db.update(TABLE_PATIENTS, cv, COL_ID + "=?", new String[]{Integer.toString(patient.id)});
        db.close();
    }

    /**
     * Update multiple patient records in the database.
     * If the record doesn't exist it is created
     * @param patients
     */
    public void updatePatients(List<Patient> patients) {
        for(Patient patient : patients) {
            if(getPatient(patient.id) == null) {
                addPatient(patient, false);
            } else {
                updatePatient(patient);
            }
        }
    }

    /**
     * Add a new patient to the database table
     *
     * New records will have the "new" column set to 1
     * for remote sync operations.
     */
    public boolean addBiometric(Biometric biometric) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        Log.d(LOG_TAG, biometric.toString());

        cv.put(COL_VALUE, biometric.value);
        cv.put(COL_TIMESTAMP, biometric.timestamp);
        cv.put(COL_BIOTYPE_ID, biometric.biometric_type_id);
        cv.put(COL_PATIENT_ID, biometric.patient_id);
        db.insert(TABLE_BIOMETRICS, COL_ID, cv);
        db.close();

        return true;
    }

    /**
     * Deletes a biometric record from the table
     * @return
     */
    public boolean deleteBiometric(int biometric_id) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        Log.d(LOG_TAG, String.format("Delete biometric record: %d", biometric_id));

        db.delete(TABLE_BIOMETRICS, COL_ID + "=?", new String[]{Integer.toString(biometric_id)});
        db.close();

        return true;
    }

    /**
     *  Returns a Cursor with all biometric records for this patient
     *  @param patient_id   Patient id to filter the list
     */
    public Cursor getPatientBiometrics(int patient_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("Database", String.format("getPatientBiometrics(%d)", patient_id));

        Cursor cursor = db.query(TABLE_BIOMETRICS + " LEFT OUTER JOIN " + TABLE_BIOTYPES
                        + " ON " + TABLE_BIOMETRICS + '.' + COL_BIOTYPE_ID + '=' + TABLE_BIOTYPES +'.' + COL_ID,
                        null,
                        COL_PATIENT_ID + "=?",
                        new String[]{Integer.toString(patient_id)},
                        null, null, TABLE_BIOMETRICS + '.' + COL_ID
        );

        logCursor(cursor);
        return cursor;
    }

    /**
     *  Returns a Cursor", "Columns: " + Arrays.toString(cursor with all biometric types in the database
     */
    public Cursor getAllBioTypes()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("Database", "getAllBioTypes");

        Cursor cursor = db.query(TABLE_BIOTYPES, null, null, null, null, null, null);
        logCursor(cursor);
        return cursor;
    }


    public BiometricType getBioType(int biotype_id) {
        BiometricType biometricType;
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(LOG_TAG, String.format("getBioType(%d)", biotype_id));

        Cursor pCursor = db.query(TABLE_BIOTYPES,
                null,
                COL_ID + "=?",
                new String[]{Integer.toString(biotype_id)},
                null, null, null, null
        );

        // Build a patient object from the cursor data
        pCursor.moveToNext();
        biometricType = new BiometricType(biotype_id,
                pCursor.getString(pCursor.getColumnIndexOrThrow(COL_NAME)),
                pCursor.getString(pCursor.getColumnIndexOrThrow(COL_UNITS))
        );
        pCursor.close();

        return biometricType;
    }

    /**
     * Add a new ecg to the database table
     */
    public boolean addECG(ECG ecg, String datafile_path) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        Log.d(LOG_TAG, String.format("%s Data File: %s",
                ecg.toString(), datafile_path));

        cv.put(COL_SAMPLING_FREQ, ecg.sampling_freq);
        cv.put(COL_PATIENT_ID, ecg.patient_id);
        cv.put(COL_FILENAME, datafile_path);
        cv.put(COL_TIMESTAMP, ecg.timestamp);
        db.insert(TABLE_ECG, COL_ID, cv);
        db.close();

        return true;
    }

    /**
     * Deletes an ECG record from the table
     * @return
     */
    public boolean deleteECG(int ecg_id) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        Log.d(LOG_TAG, String.format("Delete biometric record: %d", ecg_id));

        db.delete(TABLE_ECG, COL_ID + "=?", new String[]{Integer.toString(ecg_id)});
        db.close();

        return true;
    }

    /**
     *  Returns a Cursor with all ECG records for this patient
     *  @param patient_id   Patient id to filter the list
     */
    public Cursor getPatientECGs(int patient_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("Database", String.format("getPatientECGs(%d)", patient_id));

        Cursor cursor = db.query(TABLE_ECG,
                null,
                COL_PATIENT_ID + "=?",
                new String[]{Integer.toString(patient_id)},
                null, null, TABLE_ECG + '.' + COL_ID
        );

        logCursor(cursor);
        return cursor;
    }

    /**
     * Gets the current time as a string using the database format
     * @return String
     */

    public static String getTimestring() {
        return DATE_FORMAT.format(new Date());
    }

    /**
     * Log a cursor for debugging
     */
    private void logCursor(Cursor cursor) {
        Log.d(LOG_TAG, "######### CURSOR DEBUG ########");
        Log.d(LOG_TAG, "Columns: " + Arrays.toString(cursor.getColumnNames()));
        Log.d(LOG_TAG, "Total entries: " + Integer.toString(cursor.getCount()));

    }
}
