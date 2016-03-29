package mobi.bitcoinnow.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import mobi.bitcoinnow.R;
import mobi.bitcoinnow.data.TickerContract.TickerEntry;

public class BitcoinNowSyncAdapter extends AbstractThreadedSyncAdapter implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public final String LOG_TAG = BitcoinNowSyncAdapter.class.getSimpleName();

    public static final int INDEX_TICKER_HIGH = 1;
    public static final int INDEX_TICKER_LOW = 2;
    public static final int INDEX_TICKER_VOL = 3;
    public static final int INDEX_TICKER_LAST = 4;
    public static final int INDEX_TICKER_BUY = 5;
    public static final int INDEX_TICKER_SELL = 6;
    public static final int INDEX_TICKER_DATE = 7;

    private static final String WATCH_FACE_LAST_KEY = "mobi.bitcoinnow.key.last";
    private static final String WATCH_FACE_PROVIDER_KEY = "mobi.bitcoinnow.key.provider";

    private GoogleApiClient mGoogleApiClient;

    @Retention(RetentionPolicy.SOURCE)

    @IntDef({SERVER_STATUS_OK, SERVER_STATUS_SERVER_DOWN, SERVER_STATUS_SERVER_INVALID, SERVER_STATUS_UNKNOWN, SERVER_STATUS_INVALID})

    public @interface ServerStatus {
    }

    public static final int SERVER_STATUS_OK = 0;
    public static final int SERVER_STATUS_SERVER_DOWN = 1;
    public static final int SERVER_STATUS_SERVER_INVALID = 2;
    public static final int SERVER_STATUS_UNKNOWN = 3;
    public static final int SERVER_STATUS_INVALID = 4;

    static private void setServerStatus(Context c, @ServerStatus int ServerStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_server_status_key), ServerStatus);
        spe.commit();
    }

    public BitcoinNowSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";

        try {
            final String BASE_URL;

            String selectedProvider = PreferenceManager.getDefaultSharedPreferences(
                    getContext()).getString(getContext().getString(R.string.pref_key_bitcoin_provider),
                    getContext().getString(R.string.pref_title_provider_mercado));

            if (selectedProvider.equals(getContext().getString(R.string.pref_title_provider_mercado))) {
                BASE_URL = "https://www.mercadobitcoin.net/api/v1/ticker/";
            } else {
                BASE_URL = "https://blockchain.info/ticker";
            }

            final String QUERY_PARAM = "";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, "")
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                setServerStatus(getContext(), SERVER_STATUS_SERVER_DOWN);
                return;
            }
            forecastJsonStr = buffer.toString();
//            Log.w(LOG_TAG, "JSON Requested:" + forecastJsonStr);
            getTickerDataFromJson(forecastJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the ticker data, there's no point in attempting
            // to parse it.
            setServerStatus(getContext(), SERVER_STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            setServerStatus(getContext(), SERVER_STATUS_SERVER_INVALID);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getTickerDataFromJson(String forecastJsonStr)
            throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String TICKER_JSON_HIGH = "high";
        final String TICKER_JSON_LOW = "low";
        final String TICKER_JSON_VOL = "vol";
        final String TICKER_JSON_LAST = "last";
        final String TICKER_JSON_BUY = "buy";
        final String TICKER_JSON_SELL = "sell";
        final String TICKER_JSON_DATE = "date";
        String TICKER_JSON;

        final String MESSAGE_CODE = "cod";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            if (forecastJson.has(MESSAGE_CODE)) {
                int errorCode = forecastJson.getInt(MESSAGE_CODE);
                switch (errorCode) {
                    case HttpURLConnection.HTTP_OK:
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        setServerStatus(getContext(), SERVER_STATUS_INVALID);
                        return;
                    default:
                        setServerStatus(getContext(), SERVER_STATUS_SERVER_DOWN);
                        return;
                }
            }

            String selectedProvider = PreferenceManager.getDefaultSharedPreferences(
                    getContext()).getString(getContext().getString(R.string.pref_key_bitcoin_provider),
                    getContext().getString(R.string.pref_title_provider_mercado));

            if (selectedProvider.equals(getContext().getString(R.string.pref_title_provider_mercado))) {
                TICKER_JSON = "ticker";
            } else {
                TICKER_JSON = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getContext().getString(R.string.pref_key_currency), "BRL");
            }

            JSONObject tickerJson = forecastJson.getJSONObject(TICKER_JSON);

            double last = tickerJson.getDouble(TICKER_JSON_LAST);
            double buy = tickerJson.getDouble(TICKER_JSON_BUY);
            double sell = tickerJson.getDouble(TICKER_JSON_SELL);
            double high = 0;
            double low = 0;
            double vol = 0;
            long date;
            if (selectedProvider.equals(getContext().getString(R.string.pref_title_provider_mercado))) {
                high = tickerJson.getDouble(TICKER_JSON_HIGH);
                low = tickerJson.getDouble(TICKER_JSON_LOW);
                vol = tickerJson.getDouble(TICKER_JSON_VOL);
                date = tickerJson.getInt(TICKER_JSON_DATE);
            } else {
                date = (new Date()).getTime() / 1000L;
            }

            ContentValues tickerValues = new ContentValues();

            tickerValues.put(TickerEntry.COLUMN_ID, TickerEntry.COLUMN_ID_VALUE);
            tickerValues.put(TickerEntry.COLUMN_HIGH, high);
            tickerValues.put(TickerEntry.COLUMN_LOW, low);
            tickerValues.put(TickerEntry.COLUMN_VOL, vol);
            tickerValues.put(TickerEntry.COLUMN_LAST, last);
            tickerValues.put(TickerEntry.COLUMN_BUY, buy);
            tickerValues.put(TickerEntry.COLUMN_SELL, sell);
            tickerValues.put(TickerEntry.COLUMN_DATE, date);

            // add to database
            getContext().getContentResolver().insert(TickerEntry.CONTENT_URI, tickerValues);

            updateWearable();

            Log.d(LOG_TAG, "Sync Complete. Ticker Inserted with last=" + last + " and date=" + date);
            setServerStatus(getContext(), SERVER_STATUS_OK);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            setServerStatus(getContext(), SERVER_STATUS_SERVER_INVALID);
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        int selectedSyncInterval = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_sync), "60"));
//         Interval at which to sync with the ticker, in milliseconds.
//         60 seconds (1 minute) * 60 = 1 hours
        int SYNC_INTERVAL = 60 * selectedSyncInterval;
        int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
        BitcoinNowSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    public void updateWearable() {
        Context context = getContext();
        Uri tickerUri = TickerEntry.getTickerUri();

        Cursor cursor = context.getContentResolver().query(tickerUri, null, null, null, null);
        if (cursor.moveToFirst()) {
            String selectedProvider = PreferenceManager.getDefaultSharedPreferences(context).getString(getContext().getString(R.string.pref_key_bitcoin_provider), "");
            double last = cursor.getDouble(INDEX_TICKER_LAST);

            Log.d(LOG_TAG, "SENDING TICKER DATA ");
            String PATH_WITH_FEATURE = "/watch_face_config/Digital";
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH_WITH_FEATURE);
            putDataMapReq.getDataMap().putDouble(WATCH_FACE_LAST_KEY, last);
            putDataMapReq.getDataMap().putString(WATCH_FACE_PROVIDER_KEY, selectedProvider);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
    }
}