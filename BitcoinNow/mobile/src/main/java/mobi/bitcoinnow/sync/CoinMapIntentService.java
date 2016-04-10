package mobi.bitcoinnow.sync;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import mobi.bitcoinnow.R;
import mobi.bitcoinnow.data.VenuesDbHelper;
import mobi.bitcoinnow.model.Venue;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class CoinMapIntentService extends IntentService {

    private final String TAG = CoinMapIntentService.class.getSimpleName();
    private static final String ACTION_UPDATE_VENUES = "mobi.bitcoinnow.sync.action.UPDATE_VENUES";
    private VenuesDbHelper dbHelper;
    private SQLiteDatabase db;

    public CoinMapIntentService() {
        super("CoinMapIntentService");
    }

    /**
     * Starts this service to perform action ACTION_UPDATE_VENUES with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateVenues(Context context) {
        Intent intent = new Intent(context, CoinMapIntentService.class);
        intent.setAction(ACTION_UPDATE_VENUES);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_VENUES.equals(action)) {
                handleActionUpdateVenues();
            }
        }
    }

    /**
     * Handle action Update in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateVenues() {
        Log.d(TAG, "handleActionUpdateVenues");
        StringBuilder sb = new StringBuilder(
                "https://coinmap.org/api/v1/venues/");
        HttpURLConnection conn = null;
        BufferedReader reader;
        String jsonResults;
        try {
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            // Read the input stream into a String
            InputStream inputStream = conn.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                Log.e(TAG, "Server down");
                return;
            }
            jsonResults = buffer.toString();
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            parseJSON(jsonObj.getJSONArray("venues"));
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing BitcoinPlaces URL", e);
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to BitcoinPlaces API", e);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public void parseJSON(JSONArray data) {
        try {
            dbHelper = new VenuesDbHelper(this.getApplicationContext());
            db = dbHelper.getWritableDatabase();
            for (int i = 0; i < data.length(); i++) {
                Venue v = new Venue();
                v.setId(data.getJSONObject(i).getInt(Venue.COLUMN_ID));
                v.setName(data.getJSONObject(i).getString(Venue.COLUMN_NAME));
                v.setCreated_on(data.getJSONObject(i).getInt(Venue.COLUMN_CREATED));
                v.setLatitude(data.getJSONObject(i).getString(Venue.COLUMN_LATITUDE));
                v.setLongitude(data.getJSONObject(i).getString(Venue.COLUMN_LONGITUDE));
                v.setCategory(data.getJSONObject(i).getString(Venue.COLUMN_CATEGORY));
                if (db.isOpen()) {
                    ContentValues venueX = new ContentValues();
                    venueX.put(Venue.COLUMN_ID, v.getId());
                    venueX.put(Venue.COLUMN_NAME, v.getName());
                    venueX.put(Venue.COLUMN_CREATED, v.getCreated_on());
                    venueX.put(Venue.COLUMN_LATITUDE, v.getLatitude());
                    venueX.put(Venue.COLUMN_LONGITUDE, v.getLongitude());
                    venueX.put(Venue.COLUMN_CATEGORY, v.getCategory());
                    long insertedId = db.insert(Venue.TABLE_NAME, null, venueX);
                    if (insertedId == -1) {
                        Log.e(TAG, "Error inserting ");
                        return;
                    }
                } else {
                    Log.e(TAG, "Fail db is closed");
                }
            }
            String summary = String.format(getString(R.string.pref_update_map_last), DateFormat.format("d MMM HH:mm", new Date()), data.length());

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor spe = sp.edit();
            spe.putString(getString(R.string.pref_key_update_map), summary);
            spe.commit();

        } catch (JSONException e) {
            Log.e(TAG, "BitcoinPlaces Error:", e);
        } finally {
            db.close();
        }
    }
}