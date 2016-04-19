package parker.matt.recordcompanion;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.UUID;

import parker.matt.recordcompanion.bitalino.BitalinoCallback;
import parker.matt.recordcompanion.bitalino.BitalinoManager;


public class BluetoothTest extends AppCompatActivity {

    private static final String LOG_TAG = "BluetoothTest";

    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

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
            final TextView infoText = (TextView)  findViewById(R.id.btTestInfo);
            final ImageView btIcon  = (ImageView) findViewById(R.id.btTestIcon);
            final Button btButton   = (Button)    findViewById(R.id.btTestButton);

            String action = intent.getAction();

            switch(action) {

                case(BluetoothAdapter.ACTION_DISCOVERY_STARTED): {
                    Log.d("BluetoothTest", "DISCOVERY_STARTED detected");
                    infoText.setText("Starting scan");
                    btIcon.setImageResource(R.mipmap.ic_bluetooth_searching_black_36dp);
                    btButton.setEnabled(false);
                }
                case(BluetoothAdapter.ACTION_DISCOVERY_FINISHED): {
                    Log.d("BluetoothTest", "DISCOVERY_FINISHED detected");
                    infoText.setText("Scan complete");
                    btIcon.setImageResource(R.mipmap.ic_bluetooth_black_36dp);
                    btButton.setEnabled(true);
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
        setContentView(R.layout.activity_bluetooth_test);

        final ImageView btIcon = (ImageView) findViewById(R.id.btTestIcon);
        final TextView addressText = (TextView) findViewById(R.id.btTestDevAddress);
        final TextView channelText = (TextView) findViewById(R.id.btTestDevChannel);
        final TextView infoText = (TextView) findViewById(R.id.btTestInfo);
        final Button btButton = (Button) findViewById(R.id.btTestButton);

        graphView = (GraphView) findViewById(R.id.btTestGraph);

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
            infoText.setText("Bluetooth not enabled");
            btIcon.setImageResource(R.mipmap.ic_bluetooth_disabled_black_36dp);
            return;
        }

        // Register the broadcast receiver
        btBroadcastReceiver = new BtBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();

        // Actions for the receiver to respond to
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        registerReceiver(btBroadcastReceiver, intentFilter);
    }


    /**
     * Run the Bluetooth test
     * @param view
     */
    public void onClickRunTest(View view) {

        final TextView infoText = (TextView)  findViewById(R.id.btTestInfo);
        final ImageView btIcon  = (ImageView) findViewById(R.id.btTestIcon);

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

        // Start and read forever
        bitalinoManager.start();
        bitalinoManager.waitForConnection();

        bitalinoManager.read(0);
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
                series.appendData(new DataPoint(sampleCount + i, frames[i].getAnalog(0)), true, GRAPH_MAX_POINTS) ;
            }
            sampleCount += frames.length;
        }
    }
}


