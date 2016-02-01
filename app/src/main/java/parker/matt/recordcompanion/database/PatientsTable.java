package parker.matt.recordcompanion.database;

/**
 * Created by matt on 11/11/15.
 */

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Patient SQL data table
 *
 * Store generic information that all patients should have.
 */
public class PatientsTable extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "health_records";
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_NAME ="Patients";
    private static final String COL_ID ="_id";
    private static final String COL_FIRST_NAME ="first_name";
    private static final String COL_LAST_NAME = "last_name";
    private static final String COL_DOB ="date_of_birth";
    private static final String COL_GENDER ="gender";
    private static final String COL_NEW = "new";

    public PatientsTable(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Generate database

        // Create Patients table
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ( "
                        + COL_ID + " integer primary key, "
                        + COL_FIRST_NAME + " text not null, "
                        + COL_LAST_NAME + " text not null, "
                        + COL_DOB + " dateOfBirth text not null, "
                        + COL_GENDER + " gender integer not null, "
                        + COL_NEW + " new integer)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Drops the table
     */
    public void drop()
    {
    }

    /**
     * Add a new patient to the database table
     *
     * New records will have the "new" column set to 1
     * for remote sync operations.
     */
    public boolean addPatient(Patient patient) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();

        // Debuggin log
        Log.d("Database", String.format("addPatient(%s,%s,%s)",
                patient.firstName,
                patient.lastName,
                patient.dateOfBirth)
        );

        cv.put(COL_FIRST_NAME, patient.firstName);
        cv.put(COL_LAST_NAME, patient.lastName);
        cv.put(COL_DOB, patient.dateOfBirth);
        cv.put(COL_GENDER, patient.gender);
        cv.put(COL_NEW, 1);
        db.insert(TABLE_NAME, COL_ID, cv);
        db.close();

        return true;
    }

    // Returns a Cursor with all the patient names in the database
    public Cursor getAll()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("Database", "getAll");

        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }

    /**
     * Returns a Cursor with all patient records that have been added
     * since the last database sync
     */
    public Cursor getNew()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("Database", "getNew");

        return db.query(TABLE_NAME, null, COL_NEW + "=1", null, null, null, COL_ID);
    }

    /**
     * Returns a Patient record by id
     */
    public Patient getPatient(int id) {
        Patient patient = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("Database", String.format("getPatient(%d)", id));

        Cursor pCursor = db.query(TABLE_NAME,
                null,
                COL_ID + "=?",
                new String[]{Integer.toString(id)},
                null, null, null, null);

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

    public void updatePatient(Patient patient, Boolean clearNew)
    {
        updatePatient(patient, clearNew, patient._id);
    }

    /**
     * Update a patient record in the database
     * @param patient   Patient record of new data
     * @param searchID  ID to query the database for.
     */
    public void updatePatient(Patient patient, Boolean clearNew, Integer searchID)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();

        // Debuggin log
        Log.d("Database", String.format("updatePatient(%s,%s,%s) ID:%d:%d",
                        patient.firstName,
                        patient.lastName,
                        patient.dateOfBirth,
                        searchID,
                        patient._id)
        );

        cv.put(COL_ID, patient._id);
        cv.put(COL_FIRST_NAME, patient.firstName);
        cv.put(COL_LAST_NAME, patient.lastName);
        cv.put(COL_DOB, patient.dateOfBirth);
        cv.put(COL_GENDER, patient.gender);
        if (clearNew){
            cv.put(COL_NEW, 0);
        }

        db.update(TABLE_NAME, cv, COL_ID + "=?", new String[]{Integer.toString(searchID)});
        db.close();
    }

    public void logCursor(Cursor cursor)
    {
        Log.d("Cursor", "######### CURSOR DEBUG ########");
        Log.d("Cursor", "Columns: " + Arrays.toString(cursor.getColumnNames()));
        Log.d("Cursor", "Total Count: " + Integer.toString(cursor.getCount()));
        Log.d("Cursor", "Entry: " + cursor.getString(cursor.getColumnIndexOrThrow(COL_FIRST_NAME)));
    }
}
