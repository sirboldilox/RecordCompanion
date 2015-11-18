package parker.matt.recordcompanion.database;

import java.text.SimpleDateFormat;
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
}
