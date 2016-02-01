package parker.matt.recordcompanion.database;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by matt on 11/11/15.
 */
public class Patient {

    public final static int GENDER_MALE = 0;
    public final static int GENDER_FEMALE = 1;

    public int _id;
    public String firstName;
    public String lastName;
    public String dateOfBirth;
    public int gender;

    public Patient(int id, String firstName, String lastName, String dateOfBirth, int gender) {
        this._id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public Patient(String firstName, String lastName, String dateOfBirth, int gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    // Calculate age from date of birth
    public String getAge() {
        int age = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar date_dob = Calendar.getInstance();
        Calendar date_now = Calendar.getInstance();

        //date_dob.setTime(dateFormat.parse(this.dateOfBirth));
        //age = (date_now.get(Calendar.YEAR) - date_dob.get(Calendar.YEAR));

        return Integer.toString(age);
    }

    public String getGenderString() {
        if (gender == GENDER_MALE) {
            return "Male";
        } else {
            return "Female";
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
                    + "\"first_name\": " + this.firstName + ','
                    + "\"last_name\": " + this.lastName + ','
                    + "\"gender\": " + Integer.toString(this.gender) + ','
                    + "\"date_of_birth\": " + this.dateOfBirth + '}');
            return rJson;
        } catch(JSONException json_exc) {
            Log.d("Patient", "IOException when converting record to JSON");
            return null;
        }
    }
}
