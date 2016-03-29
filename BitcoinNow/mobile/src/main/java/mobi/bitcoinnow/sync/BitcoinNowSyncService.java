package mobi.bitcoinnow.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BitcoinNowSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static BitcoinNowSyncAdapter sBitcoinNowSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.w("BitcoinNowSyncService", "onCreate - BitcoinNowSyncService");
        synchronized (sSyncAdapterLock) {
            if (sBitcoinNowSyncAdapter == null) {
                sBitcoinNowSyncAdapter = new BitcoinNowSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sBitcoinNowSyncAdapter.getSyncAdapterBinder();
    }
}