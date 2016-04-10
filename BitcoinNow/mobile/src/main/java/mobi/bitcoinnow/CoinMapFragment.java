package mobi.bitcoinnow;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import mobi.bitcoinnow.data.TickerContract;
import mobi.bitcoinnow.data.VenuesDbHelper;
import mobi.bitcoinnow.model.Venue;
import mobi.bitcoinnow.sync.BitcoinNowSyncAdapter;
import mobi.bitcoinnow.sync.CoinMapIntentService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link CoinMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoinMapFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private GoogleMap map;
    private VenuesDbHelper dbHelper;
    private SQLiteDatabase db;
    private int numberVenues;
    private Cursor cursor;
    private SupportMapFragment mMapFragment;

    private static final int FORECAST_LOADER = 0;

    TextView lastRate, dateRate;

    public CoinMapFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CoinMapFragment.
     */
    public static CoinMapFragment newInstance() {
        return new CoinMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        setUpMapIfNeeded();
        lastRate = (TextView) view.findViewById(R.id.last_rate);
        dateRate = (TextView) view.findViewById(R.id.date_rate);
        lastRate.setText("");
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setUpMapIfNeeded() {
        if (mMapFragment == null) {
            mMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
            if (mMapFragment != null) {
                setUpMap();
            } else {
                Log.e("Read", "mMapFragment null");
            }
        } else {
            getVenuesFromDB();
        }
    }

    private void setUpMap() {
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setIndoorEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-12.0, -47.9), 4));

                getVenuesFromDB();

                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                    }
                } else {
                    map.setMyLocationEnabled(true);
                    map.getUiSettings().setMyLocationButtonEnabled(true);
                }
            }
        });
    }

    public void getVenuesFromDB() {
        try {
            Log.d("Read", "getVenuesFromDB");
            dbHelper = new VenuesDbHelper(getContext());
            db = dbHelper.getReadableDatabase();
            cursor = db.query(Venue.TABLE_NAME, null, null, null, null, null, null);
            numberVenues = cursor.getCount();
        } catch (SQLiteException e) {
            dbHelper.onCreate(db);
            Log.e("Read", "Error Will Create Table - " + e.getLocalizedMessage());
        }
        if (numberVenues > 0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cursor.moveToFirst();
                        numberVenues = 0;
                        if (map != null) {
                            map.clear();
                            do {
                                String createdOn = DateFormat.getDateFormat(getContext()).format(
                                        cursor.getInt(cursor.getColumnIndex(Venue.COLUMN_CREATED)) * 1000L);
                                String category = cursor.getString(cursor.getColumnIndex(Venue.COLUMN_CATEGORY));
                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(
                                                Double.valueOf(cursor.getString(cursor.getColumnIndex(Venue.COLUMN_LATITUDE))),
                                                Double.valueOf(cursor.getString(cursor.getColumnIndex(Venue.COLUMN_LONGITUDE)))))
                                        .title(cursor.getString(cursor.getColumnIndex(Venue.COLUMN_NAME)))
                                        .snippet(category.substring(0, 1).toUpperCase() + category.substring(1, category.length()) + " - Registered " + createdOn)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bit)));

                                numberVenues++;
                                cursor.moveToNext();
                            } while (!cursor.isLast());
                        }
                    } catch (Exception e) {
                        Log.e("Error", e.getLocalizedMessage());
                        e.printStackTrace();
                    } finally {
                        if (!cursor.isClosed())
                            cursor.close();
                        if (db.isOpen())
                            db.close();
                    }
                }
            });
        } else {
            CoinMapIntentService.startActionUpdateVenues(getContext());
            Toast.makeText(getContext(), (getString(R.string.pref_update_map_request) + " " +
                    DateFormat.format("d MMM HH:mm", new Date())), Toast.LENGTH_LONG).show();
            if (!cursor.isClosed())
                cursor.close();
            if (db.isOpen())
                db.close();
        }
    }


    //Ticker
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri weatherForLocationUri = TickerContract.TickerEntry.getTickerUri();
        return new CursorLoader(this.getActivity(), weatherForLocationUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            DecimalFormat df = new DecimalFormat("#.00");
            data.moveToFirst();
            lastRate.setText(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_key_currency), "-") + " $ " + df.format(data.getDouble(BitcoinNowSyncAdapter.INDEX_TICKER_LAST)));
            dateRate.setText(SimpleDateFormat.getDateTimeInstance().format(data.getInt(BitcoinNowSyncAdapter.INDEX_TICKER_DATE) * 1000L));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
