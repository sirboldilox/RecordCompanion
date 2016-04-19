package parker.matt.recordcompanion.sync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import parker.matt.recordcompanion.R;
import parker.matt.recordcompanion.database.DatabaseHelper;

/**
 * Background service for syncronising the database.
 */
public class SyncService1 extends Service {

    private static final String LOG_TAG = "SyncService1";

    private URL databaseURL;

    private DatabaseHelper dbAdapter;
    private String lastError;

    @Override
    public void onCreate() {

        // Read local settings
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            databaseURL = new URL(preferences.getString("database_url", "NULL"));
        } catch (MalformedURLException url_e) {
            Log.e(LOG_TAG, "Malformed URL: ", url_e);
        }
        Log.d(LOG_TAG, "Service created");
        return;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        sendNotification("SyncService1", "onStartCommand");
        Log.d(LOG_TAG, "sent notification");
        // Terminate self
        stopSelf();
        return 0;
    }

    private void sendNotification(String title, String subject) {
        Intent intent = new Intent(this, NotificationManager.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Build notifiaction
        Notification notification = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(subject)
                .setSmallIcon(R.mipmap.ic_loop_black_18dp)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }
}
