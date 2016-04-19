package parker.matt.recordcompanion.bitalino;

import com.bitalino.comm.BITalinoFrame;

/**
 * Created by matt on 07/04/16.
 */
public interface BitalinoCallback {

    /**
     * Abstract function to handle connect attempts
     * @param connected If the connection was successful
     */
    void handleConnected(boolean connected);


    /**
     *  Abstract function to handle read frames using threads
     *  @param frames   Array of frames read from device
     */
    void handleRead(BITalinoFrame[] frames);
}
