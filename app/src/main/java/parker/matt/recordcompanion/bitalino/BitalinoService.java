package parker.matt.recordcompanion.bitalino;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by matt on 09/03/16.
 */
public class BitalinoService extends Service {

    private static final String LOG_TAG = "BitalinoService";

    // Bluetooth UUID for serial devices
    private final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Preferences
    private String bitalinoAddress;
    private Integer bitalinoChannel;

    private BluetoothSocket btSock;
    private BITalinoDevice  bitalino;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Read local settings
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        bitalinoAddress = preferences.getString("bitalino_address", "NULL");
        bitalinoChannel = Integer.parseInt(preferences.getString("bitalino_channel", "0"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(LOG_TAG, "Service started");

        return Service.START_NOT_STICKY;
    }

    private void bluetoothConnect() {
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice btDev;

        if (!btAdapter.isEnabled()) {
            Log.i(LOG_TAG, "Failed to start bluetooth connection: Bluetooth disabled.");
            // Send error broadcast and terminate service
            return;
        }

        // Attempt device connection
        try {
            btDev = btAdapter.getRemoteDevice(bitalinoAddress);
            btAdapter.cancelDiscovery();
        } catch (IllegalArgumentException e) {
            String msg = String.format("Invalid device address: %s", bitalinoAddress);
            Log.w(LOG_TAG, msg);
            // Send error broadcast and terminate service
            return;
        }

        try {
            btSock = btDev.createInsecureRfcommSocketToServiceRecord(BT_UUID);
            btSock.connect();

            Log.d(LOG_TAG, "Opening socket to bluetooth device");
            bitalino = new BITalinoDevice(1000, new int[]{bitalinoChannel});
            bitalino.open(btSock.getInputStream(), btSock.getOutputStream());

            Log.d(LOG_TAG, "Connected to BITalino: Version: " + bitalino.version());

        } catch (IOException ioe) {
            Log.e(LOG_TAG, "IOException when opening connection to bitalino: ", ioe);
            // Send error broadcast and terminate service
            return;
        } catch (BITalinoException bte) {
            Log.e(LOG_TAG, "BitalinoException: ", bte);
            try {
                btSock.close();
            } catch (IOException ioe) {
                Log.e(LOG_TAG, "IOException when closing bluetooth socket", ioe);
            }
            // Send error broadcast and terminate service
            return;
        }
    }

    private void getFrames(Integer frameCount) {
        BITalinoFrame[] fames;

        // Start reading
        try {
            bitalino.start();
            Log.d(LOG_TAG, "BITalino started");
            fames = bitalino.read(frameCount);
            bitalino.stop();
            Log.d(LOG_TAG, "BITalino stopped");
        } catch (BITalinoException bte) {
            Log.e(LOG_TAG, "BitalinoException when reading frames: ", bte);
            // Send error broadcast
            return;
        }
    }

    /**
     * Handle calls to end the service and cleanup the bluetooth state.
     * If the device is still communicating handle closing sockets and devices
     */
    @Override
    public void onDestroy() {

    }
}
