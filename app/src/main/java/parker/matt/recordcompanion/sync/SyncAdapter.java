package parker.matt.recordcompanion.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by matt on 13/04/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public SyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
    }

    public SyncAdapter(
            Context context,
            boolean autoInialize,
            boolean allowParallelSyncs ) {

        super(context, autoInialize, allowParallelSyncs);

    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {

    }
}
