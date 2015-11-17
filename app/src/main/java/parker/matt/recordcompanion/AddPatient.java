package parker.matt.recordcompanion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

// Local imports
import parker.matt.recordcompanion.database.Patient;
import parker.matt.recordcompanion.database.PatientsTable;

public class AddPatient extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
    }

    // Handles create patient button
    public void onClickAdd(View button) {
        final EditText patientFirstName = (EditText) findViewById(R.id.patientFirstName);
        final EditText patientLastName = (EditText) findViewById(R.id.patientLastName);
        final EditText patientDOB  = (EditText) findViewById(R.id.patientBirthday);
        final RadioGroup genderGroup = (RadioGroup) findViewById(R.id.radioGender);
        final RadioButton selectedGender = (RadioButton) findViewById(genderGroup.getCheckedRadioButtonId());

        Patient patientRecord;
        String firstName, lastName, dateOfBirth;
        int gender;

        // Build Patient object
        firstName = patientFirstName.getText().toString();
        lastName = patientLastName.getText().toString();
        dateOfBirth = patientDOB.getText().toString();

        if (selectedGender.getText() == "Male") {
            gender = Patient.GENDER_MALE;
        }
        else {
            gender = Patient.GENDER_FEMALE;
        }

        patientRecord = new Patient(firstName, lastName, dateOfBirth, gender);

        // Add object to the database
        PatientsTable dbCon = new PatientsTable(this);
        dbCon.addPatient(patientRecord);

        finish();
    }
}
