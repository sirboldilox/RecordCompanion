package parker.matt.recordcompanion;

import android.app.DatePickerDialog;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

// Local imports
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import parker.matt.recordcompanion.database.DatabaseHelper;
import parker.matt.recordcompanion.models.Patient;

public class AddPatient extends AppCompatActivity {

    private DatePickerDialog datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        final Calendar calendar = Calendar.getInstance();
        datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int month, int day) {
                EditText patientDOB  = (EditText) findViewById(R.id.patientBirthday);
                patientDOB.setText(String.format("%d/%d/%d", year, month, day));
            }

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        final EditText patientDOB  = (EditText) findViewById(R.id.patientBirthday);
        patientDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show();
            }
        });
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

        // Build PatientDBModel object
        firstName = patientFirstName.getText().toString();
        lastName = patientLastName.getText().toString();

        // Validate entries
        if (firstName.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Missing first name.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (lastName.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Missing last name.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }


        // Convert date to full timestamp format to date
        dateOfBirth = String.format("%s 00:00:00", patientDOB.getText().toString());

        // Check date of birth
        SimpleDateFormat dateFormat = DatabaseHelper.DATE_FORMAT;
        if (dateFormat.parse(dateOfBirth, new ParsePosition(0)) == null)
        {
            Toast.makeText(getApplicationContext(),
                    "Invaid date format.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Convert gender
        if (selectedGender.getId() == R.id.radioMale) {
            gender = Patient.GENDER_MALE;
        }
        else {
            gender = Patient.GENDER_FEMALE;
        }

        patientRecord = new Patient(firstName, lastName, dateOfBirth, gender);

        // Add object to the database
        DatabaseHelper dbCon = new DatabaseHelper(this);
        dbCon.addPatient(patientRecord, true);

        finish();
    }
}
