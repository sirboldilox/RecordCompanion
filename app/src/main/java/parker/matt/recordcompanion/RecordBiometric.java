package parker.matt.recordcompanion;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import parker.matt.recordcompanion.database.DatabaseHelper;
import parker.matt.recordcompanion.models.Biometric;
import parker.matt.recordcompanion.models.BiometricType;

public class RecordBiometric extends AppCompatActivity {

    private static final String LOG_TAG = "RecordBiometric";

    public static final String EXTRA_PATIENT_ID = "patient_id";
    public static final String EXTRA_BIOTYPE_ID = "biotype_id";

    private long patient_id;
    private long biotype_id;

    private DatabaseHelper dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_biometric);

        final TextView tvTitle = (TextView) findViewById(R.id.recordBiometricTitle);
        final TextView tvUnits = (TextView) findViewById(R.id.recordBiometricUnits);
        final EditText tvInput = (EditText) findViewById(R.id.recordBiometricValue);

        // Get extra data
        Intent intent = this.getIntent();
        patient_id = intent.getLongExtra(EXTRA_PATIENT_ID, 0);
        biotype_id = intent.getLongExtra(EXTRA_BIOTYPE_ID, 0);

        // Get data from database
        BiometricType biometricType;
        dbAdapter = new DatabaseHelper(this);
        biometricType = dbAdapter.getBioType((int) biotype_id);

        tvTitle.setText(biometricType.name);
        tvUnits.setText(biometricType.units);

        Log.d(LOG_TAG, String.format("Biometric type: %d", biometricType.id));

        // Allow / digit for inputting Blood pressure
        if (biometricType.id == DatabaseHelper.ID_BP) {
            Log.d(LOG_TAG, "Got BP");
            tvInput.setKeyListener(DigitsKeyListener.getInstance("1234567890./"));
        }
    }

    public void onClickAddButton(View view) {
        final EditText tvValue = (EditText) findViewById(R.id.recordBiometricValue);
        final String timestamp = DatabaseHelper.getTimestring();

        Biometric biometric = new Biometric(
                tvValue.getText().toString(),
                (int) biotype_id,
                (int) patient_id,
                timestamp
        );

        dbAdapter.addBiometric(biometric);
        finish();
    }

}
