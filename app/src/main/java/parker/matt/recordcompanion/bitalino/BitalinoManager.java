package parker.matt.recordcompanion.bitalino;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;

import java.io.IOException;
import java.util.UUID;

/**
 * Class for interfacing with the bitalino using threads
 */
public class BitalinoManager {

    private static final String LOG_TAG = "BitalinoManager";

    // Bluetooth UUID for serial devices
    private static final UUID BT_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Sampling frequency
    public static final int SAMPLING_FREQ = 1000;

    // Bluetooth and bitalino objects
    private BluetoothDevice btDev;
    private BluetoothSocket btSock;

    private BITalinoDevice bitalino;
    private BitalinoCallback btCallbacks;

    private boolean connected = false;
    private boolean reading = false;

    // Threads
    private Thread conThread = null;
    private Thread readThread = null;

    /**
     * Construct a manager class
     * @param btDev     The bluetooth device representing the bitalino
     * @param callbacks Callback class for thread handles
     */
    public BitalinoManager(final BluetoothDevice btDev, final BitalinoCallback callbacks) {
        this.btDev = btDev;
        this.btCallbacks = callbacks;
    }

    /**
     * Runnable class for opening bluetooth connections to the bitalino via
     * the bluetooth device in a separate thread.
     */
    private class ConnectRunnable implements Runnable {

        @Override
        public void run() {
            try {
                btSock = btDev.createInsecureRfcommSocketToServiceRecord(BT_UUID);
                btSock.connect();

                Log.d(LOG_TAG, "Opening socket to bluetooth device");
                bitalino = new BITalinoDevice(SAMPLING_FREQ, new int[]{0});
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
            connected = true;
        }
    }

    private class ReadRunnable implements Runnable {

        private static final int MAX_FRAMES = 2048;
        private int frameCount;

        ReadRunnable(int frameCount) {
            this.frameCount = frameCount;
        }

        @Override
        public void run() {

            // Start reading
            if (bitalino != null) {
                try {
                    BITalinoFrame[] biTalinoFrames;

                    bitalino.start();
                    Log.d(LOG_TAG, "BITalino started");

                    reading = true;
                    if (frameCount > 0) {
                        biTalinoFrames = bitalino.read(frameCount);
                        btCallbacks.handleRead(biTalinoFrames);
                    } else {
                        while (true) {
                            biTalinoFrames = bitalino.read(MAX_FRAMES);
                            btCallbacks.handleRead(biTalinoFrames);
                        }
                    }

                    // Stop the device
                    bitalino.stop();
                    Log.d(LOG_TAG, "BITalino stopped");
                    reading = false;

                } catch (BITalinoException e) {
                    Log.e(LOG_TAG, "BitalinoException: ", e);
                }
            }
            else {
                Log.w(LOG_TAG, "Attempted to start reading from a non connected device");
            }
        }
    }

    /**
     * Connect to the bitalino and start
     */
    public void start() {
        if (conThread == null) {
            conThread = new Thread(new ConnectRunnable());
            conThread.start();
        }
    }

    /**
     * Interrupt the threads if running to stop the manager
     */
    public void stop() {
        if (conThread != null) {
            conThread.interrupt();
        }
        if (readThread != null) {
            readThread.interrupt();
        }
    }

    /**
     * Block until the connection thread exits or is interrupted
     */
    public void waitForConnection() {
        if (conThread != null) {
            try {
                conThread.join();
            } catch (InterruptedException int_e) {

            }
            conThread = null;
            connected = false;
        }
    }

    /**
     * Read frames from the bitalino
     * @param frameCount
     */
    public void read(int frameCount) {
        if (readThread == null) {
            readThread = new Thread(new ReadRunnable(frameCount));
            readThread.start();
        }
    }

    /**
     * Checks if the device is connected
     * @return Connected state
     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * Checks if the device is reading
     * @return reading state
     */
    public boolean isReading() {
        return this.reading;
    }



}
