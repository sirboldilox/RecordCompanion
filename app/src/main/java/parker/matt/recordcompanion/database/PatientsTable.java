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


public class PatientsTable extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "health_records";
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_NAME ="Patients";
    private static final String COL_ID ="_id";
    private static final String COL_FIRST_NAME ="first_name";
    private static final String COL_LAST_NAME = "last_name";
    private static final String COL_DOB ="date_of_birth";
    private static final String COL_GENDER ="gender";

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
                        + COL_GENDER + " gender integer not null) "
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addPatient(Patient patient) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();

        Log.d("AddPR", "Patient: "
                + patient.firstName + " "
                + patient.lastName + " "
                + patient.dateOfBirth);

        cv.put(COL_FIRST_NAME, patient.firstName);
        cv.put(COL_LAST_NAME, patient.lastName);
        cv.put(COL_DOB, patient.dateOfBirth);
        cv.put(COL_GENDER, patient.gender);
        db.insert(TABLE_NAME, COL_ID, cv);
        db.close();

        return true;
    }

    // Returns a Cursor with all the patient names in the database
    public Cursor getAllNames()
    {
        Cursor rCursor;
        SQLiteDatabase db = this.getReadableDatabase();

        Log.d("Database", "getAllNames");
        rCursor = db.query(TABLE_NAME, new String[]{COL_ID, COL_LAST_NAME, COL_FIRST_NAME},
                null, null, null, null, null);

        return rCursor;
    }

    // Returns a Patient record
    public Patient getPatient(int id) {
        Patient patient;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor pCursor = db.query(TABLE_NAME,
                null,
                "_id=?",
                new String[]{Integer.toString(id)},
                null, null, null, null);

        patient = new Patient(id,
                pCursor.getString(pCursor.getColumnIndexOrThrow("first_name")),
                pCursor.getString(pCursor.getColumnIndexOrThrow("last_name")),
                pCursor.getString(pCursor.getColumnIndexOrThrow("date_of_birth")),
                pCursor.getInt(pCursor.getColumnIndexOrThrow("gender")));

        pCursor.close();
        return patient;
    }

}
