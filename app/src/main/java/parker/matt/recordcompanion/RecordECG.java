package parker.matt.recordcompanion;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitalino.comm.BITalinoFrame;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import parker.matt.recordcompanion.bitalino.BitalinoCallback;
import parker.matt.recordcompanion.bitalino.BitalinoManager;
import parker.matt.recordcompanion.database.DatabaseHelper;
import parker.matt.recordcompanion.models.ECG;

public class RecordECG extends AppCompatActivity {

    private static final String LOG_TAG = "RecordECG";

    public static final String EXTRA_PATIENT_ID = "patient_id";

    // Patient settings
    private long patient_id;
    private String filename_format = "ecg_%s_%s.dat";

    // Database
    private DatabaseHelper dbAdapter;

    // Bitalino settings
    private String bitalinoAddress;
    private Integer bitalinoChannel;

    // Bluetooth
    private BluetoothAdapter btAdapter;
    private BtBroadcastReceiver btBroadcastReceiver;

    // Bitalino manager
    private BitalinoManager bitalinoManager = null;
    private BitalinoCallback bitalinoCallbacks = null;

    // Data graph
    private GraphView graphView;
    private LineGraphSeries<DataPoint> series = null;
    private int sampleCount = 0;
    private static final int GRAPH_MAX_POINTS = 8192;   // 8 seconds of data
    /**
     *  Broadcast receiver for bluetooth events
     */
    private class BtBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final TextView  infoText = (TextView)  findViewById(R.id.recordECGInfo);
            final ImageView btIcon   = (ImageView) findViewById(R.id.recordECGBTIcon);
            final Button    btButton = (Button)    findViewById(R.id.recordECGButton);

            final String action = intent.getAction();

            Log.d(LOG_TAG, String.format("Broadcast receiver: %s", action));
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                        BluetoothAdapter.ERROR);
                Log.d(LOG_TAG, String.format("State: %d", state));
                switch(state) {

                    case(BluetoothAdapter.STATE_OFF): {
                        infoText.setText("Bluetooth disabled");
                        btIcon.setImageResource(R.mipmap.ic_bluetooth_disabled_black_36dp);
                        btButton.setEnabled(false);
                        break;
                    }
                    case(BluetoothAdapter.STATE_TURNING_OFF): {
                        infoText.setText("Bluetooth disabled");
                        btIcon.setImageResource(R.mipmap.ic_bluetooth_disabled_black_36dp);
                        btButton.setEnabled(false);
                        break;
                    }
                    case(BluetoothAdapter.STATE_TURNING_ON): {
                        infoText.setText("Bluetooth starting");
                        btIcon.setImageResource(R.mipmap.ic_bluetooth_black_36dp);
                        btButton.setEnabled(false);
                        break;
                    }
                    case(BluetoothAdapter.STATE_ON): {
                        infoText.setText("Bluetooth enabled");
                        btIcon.setImageResource(R.mipmap.ic_bluetooth_black_36dp);
                        btButton.setEnabled(true);
                        break;
                    }
                }
            }
        }
    }

    /**
     *  Callback interface for updating the graph
     */
    private class GraphCallback implements BitalinoCallback {

        public void handleConnected(boolean connected) {

        }

        /**
         * Update the graph on a read callback
         * @param frames   Array of frames read from device
         */
        public void handleRead(BITalinoFrame[] frames) {
            final BITalinoFrame[] fFrames = frames;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    writeFrames(fFrames);
                    updateGraph(fFrames);
                }
            });
        }
    }

    /**
     * Initalise the UI and other base components
     * Setup status messages and bluetooth icon depending on current bluetooth configuration.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ecg);

        final ImageView btIcon = (ImageView) findViewById(R.id.recordECGBTIcon);
        final TextView addressText = (TextView) findViewById(R.id.recordECGDevAddress);
        final TextView channelText = (TextView) findViewById(R.id.recordECGDevChannel);
        final TextView infoText = (TextView) findViewById(R.id.recordECGInfo);
        final Button btButton = (Button) findViewById(R.id.recordECGButton);

        graphView = (GraphView) findViewById(R.id.recordECGGraph);

        // Get extra data
        Intent intent = this.getIntent();
        patient_id = intent.getLongExtra(EXTRA_PATIENT_ID, 0);
        dbAdapter = new DatabaseHelper(this);

        // Address of the bitalino
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        bitalinoAddress = preferences.getString("bitalino_address", "NULL");
        bitalinoChannel = Integer.parseInt(preferences.getString("bitalino_channel", "0"));

        addressText.setText(String.format("Device Address: %s", bitalinoAddress));
        channelText.setText(String.format("Device Channel: %d", bitalinoChannel));

        // Setup graph
        graphView.setTitle("Readings");
        GridLabelRenderer graphLabels = graphView.getGridLabelRenderer();
        graphLabels.setHorizontalAxisTitle("Time (ms)");
        graphLabels.setVerticalAxisTitle("mV");

        // Setup callbacks
        bitalinoCallbacks = new GraphCallback();

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        // Check if Bluetooth is supported on the device
        if (btAdapter == null) {
            infoText.setText("Bluetooth not supported on this device");
            btIcon.setImageResource(R.mipmap.ic_bluetooth_disabled_black_36dp);
            btButton.setEnabled(false);
            btButton.setText("Disabled");
            return;
        }

        // Check bluetooth service state
        if (!btAdapter.isEnabled()) {
            infoText.setText("Bluetooth disabled");
            btIcon.setImageResource(R.mipmap.ic_bluetooth_disabled_black_36dp);
            return;
        }

        // Register the broadcast receiver
        btBroadcastReceiver = new BtBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();

        // Actions for the receiver to respond to
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btBroadcastReceiver);
    }

    /**
     * Run the Bluetooth test
     * @param view
     */
    public void onClickRunToggle(View view) {

        final TextView infoText = (TextView)  findViewById(R.id.recordECGInfo);
        final ImageView btIcon  = (ImageView) findViewById(R.id.recordECGBTIcon);
        final Button runButton  = (Button)    findViewById(R.id.recordECGButton);

        runButton.setText("Run");

        // Setup BitalinoManager if null
        if (bitalinoManager == null) {

            BluetoothDevice btDev;

            if (!btAdapter.isEnabled()) {
                infoText.setText("Bluetooth not enabled");
                btIcon.setImageResource(R.mipmap.ic_bluetooth_disabled_black_36dp);
                Toast.makeText(getApplicationContext(), "Please enable bluetooth.", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            // Attempt device connection
            try {
                btDev = btAdapter.getRemoteDevice(bitalinoAddress);
                btAdapter.cancelDiscovery();
            } catch (IllegalArgumentException e) {
                String msg = String.format("Invalid device address: %s", bitalinoAddress);
                Log.d(LOG_TAG, msg);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                return;
            }

            // Build manager
            bitalinoManager = new BitalinoManager(btDev, bitalinoCallbacks);
        }

        // Update button state
        runButton.setText("Stop");

        // Start and read forever
        bitalinoManager.start();
        bitalinoManager.waitForConnection();

        bitalinoManager.read(GRAPH_MAX_POINTS);
    }

    /**
     * Writes the frames to the disk
     * @param frames
     */
    private void writeFrames(BITalinoFrame frames[])
    {
        final String filename = String.format("ecg_%s", UUID.randomUUID().toString());
        final String fullpath = String.format("%s/%s", getFilesDir(), filename);

        final String timestamp = DatabaseHelper.getTimestring();
        String frameString = "";

        for (int i = 0; i < frames.length; i++) {
            frameString += String.format("%s,",
                    Float.toString(frames[i].getAnalog(0)));
        }
        try {
            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(frameString.getBytes());
            fos.close();

        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Exeption when writing frames: ", e);
            return;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exeption when writing frames: ", e);
            return;
        }

        ECG ecg = new ECG((int) patient_id, (float) BitalinoManager.SAMPLING_FREQ, 0, timestamp);
        dbAdapter.addECG(ecg, fullpath);
    }

    /**
     * Update the graph with new frames
     * @param frames    Bitalino frames array to display
     */
    private void updateGraph(BITalinoFrame frames[]) {

        // First run
        if (series == null) {
            DataPoint[] dataPoints = new DataPoint[frames.length];

            for (int i = 0; i < frames.length; i++) {
                dataPoints[i] = new DataPoint(i, frames[i].getAnalog(0));
            }

            sampleCount = frames.length;

            // Add new data to graph
            series = new LineGraphSeries<>(dataPoints);
            graphView.addSeries(series);

        } else {
            // Append data
            for (int i = 0; i < frames.length; i++) {
                series.appendData(new DataPoint(sampleCount + i, frames[i].getAnalog(0)), false, GRAPH_MAX_POINTS) ;
            }
            sampleCount += frames.length;
        }
    }
}
