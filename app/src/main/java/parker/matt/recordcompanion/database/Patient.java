package parker.matt.recordcompanion.database;

import java.util.Date;

/**
 * Created by matt on 11/11/15.
 */
public class Patient {

    public final static int GENDER_MALE = 0;
    public final static int GENDER_FEMALE = 1;

    private int _id;
    private String _firstName;
    private String _lastName;
    private String _dateOfBirth;
    private int _gender;

    public Patient(int id, String firstName, String lastName, String dateOfBirth, int gender) {
        _id = id;
        _firstName = firstName;
        _lastName = lastName;
        _dateOfBirth = dateOfBirth;
        _gender = gender;
    }

    public Patient(String firstName, String lastName, String dateOfBirth, int gender) {
        _firstName = firstName;
        _lastName = lastName;
        _dateOfBirth = dateOfBirth;
        _gender = gender;
    }

}
