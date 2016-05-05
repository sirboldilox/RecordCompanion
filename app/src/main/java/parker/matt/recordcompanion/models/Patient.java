package parker.matt.recordcompanion.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import parker.matt.recordcompanion.database.DatabaseHelper;

/**
 * Data model for basic patient records
 *
 *
 */
public class Patient {

    private static final String LOG_TAG = "Patient";

    public final static int GENDER_MALE = 1;
    public final static int GENDER_FEMALE = 2;

    public int id;
    public String first_name;
    public String last_name;
    public String date_of_birth;
    public int gender;

    public Patient(String firstName, String lastName, String dateOfBirth, int gender) {
            this.first_name = firstName;
            this.last_name = lastName;
            this.date_of_birth = dateOfBirth;
            this.gender = gender;
    }

    public Patient(int id, String firstName, String lastName, String dateOfBirth, int gender) {
            this.id = id;
            this.first_name = firstName;
            this.last_name = lastName;
            this.date_of_birth = dateOfBirth;
            this.gender = gender;
    }

    /**
     * Gets the full patient name with first letter capitalised
     */
    public String getNameString() {
        return first_name + ' ' + last_name;
    }

    /**
     * Calculate age from date of birth
     */
    public String getAgeString() {
            int age;
            SimpleDateFormat dateFormat = DatabaseHelper.DATE_FORMAT;
            Calendar date_dob = Calendar.getInstance();
            Calendar date_now = Calendar.getInstance();

            try {
                date_dob.setTime(dateFormat.parse(this.date_of_birth));
                age = (date_now.get(Calendar.YEAR) - date_dob.get(Calendar.YEAR));
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Exception when calculating age: ", e);
                return "Invalid";
            }

            return Integer.toString(age);
    }

    /**
     * Returns the gender feild as a capitalised string
     */
    public String getGenderString() {
        switch (gender) {
            case GENDER_MALE:
                return "Male";
            case GENDER_FEMALE:
                return "Female";
            default:
                return "Invalid gender";
        }
    }

    /**
     * Returns the object as a JSON encoded object
     */
    public JSONObject toJSON()
    {
        JSONObject rJson;
        try {
            rJson = new JSONObject('{'
                    + "\"first_name\": " + this.first_name + ','
                    + "\"last_name\": " + this.last_name + ','
                    + "\"gender\": " + Integer.toString(this.gender) + ','
                    + "\"date_of_birth\": " + this.date_of_birth + '}');
            return rJson;
        } catch(JSONException json_exc) {
            Log.d(LOG_TAG, "IOException when converting record to JSON");
            return null;
        }
    }

    /**
     * Return this record as a string
     */
    public String toString() {
        return String.format("Patient(%d, %s, %s, %s %s)",
            id, first_name, last_name, getGenderString(), getAgeString()
        );
    }
}
