package mobi.bitcoinnow;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import mobi.bitcoinnow.data.TickerContract;
import mobi.bitcoinnow.sync.BitcoinNowSyncAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewsFragment.OnNewsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int FORECAST_LOADER = 0;

    TextView lastRate, dateRate;

    private OnNewsFragmentInteractionListener mListener;

    public NewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsFragment newInstance(String param1, String param2) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        lastRate = (TextView) view.findViewById(R.id.last_rate);
        dateRate = (TextView) view.findViewById(R.id.date_rate);
        lastRate.setText("");
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);

        if (context instanceof OnNewsFragmentInteractionListener) {
            mListener = (OnNewsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri weatherForLocationUri = TickerContract.TickerEntry.getTickerUri();
        return new CursorLoader(this.getActivity(),
                weatherForLocationUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            DecimalFormat df = new DecimalFormat("#.00");
            data.moveToFirst();
            lastRate.setText(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_key_currency), "-")+" $ " + df.format(data.getDouble(BitcoinNowSyncAdapter.INDEX_TICKER_LAST)));
            dateRate.setText(SimpleDateFormat.getDateTimeInstance().format(data.getInt(BitcoinNowSyncAdapter.INDEX_TICKER_DATE) * 1000L));
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNewsFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
