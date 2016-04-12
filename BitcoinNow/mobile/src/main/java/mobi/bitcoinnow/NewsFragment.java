package mobi.bitcoinnow;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import mobi.bitcoinnow.data.RedditDAO;
import mobi.bitcoinnow.data.TickerContract;
import mobi.bitcoinnow.model.Reddit;
import mobi.bitcoinnow.sync.BitcoinNowSyncAdapter;
import mobi.bitcoinnow.sync.RedditDataSyncTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, RedditDataSyncTask.OnRedditDataSyncFinishListener {

    private static final int FORECAST_LOADER = 0;
    private static SharedPreferences.Editor editor;
    private static SharedPreferences sharedPreferences;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsRowAdapter adapter;
    private RedditDAO redditDAO;
    private TextView lastRate, dateRate;
    private ProgressDialog progress;

    public NewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewsFragment.
     */
    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        progress = new ProgressDialog(getActivity(), DialogFragment.STYLE_NO_TITLE);

        lastRate = (TextView) view.findViewById(R.id.last_rate);
        dateRate = (TextView) view.findViewById(R.id.date_rate);
        lastRate.setText("");

        this.sharedPreferences = getContext().getSharedPreferences("redlist", 0);
        this.editor = this.sharedPreferences.edit();

        redditDAO = new RedditDAO(getContext());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestRedditDataSync();
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        adapter = new NewsRowAdapter(redditDAO.getRedditList(), getContext());
        recyclerView.setAdapter(adapter);
//            recyclerView.setSelection(sharedPreferences.getInt("position", 0));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.setOnRecycleViewItemClickListener(new NewsRowAdapter.OnRecycleViewItemClickListener() {
            @Override
            public void onRecycleViewItemClicked(View view, int position) {
                Reddit reddit = adapter.getItem(position);
                Intent intent = new Intent(getContext(), NewsDetailActivity.class);
                intent.putExtra("reddit", reddit);
                startActivity(intent);

                editor.putInt("last_position", position);
                editor.commit();
            }
        });
        recyclerView.scrollToPosition(sharedPreferences.getInt("last_position", 0));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        redditDAO.open();
        if (null == adapter || adapter.getItemCount() < 1) {
            requestRedditDataSync();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        redditDAO.close();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = TickerContract.TickerEntry.getTickerUri();
        return new CursorLoader(this.getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            DecimalFormat df = new DecimalFormat("#.00");
            data.moveToFirst();
            lastRate.setText(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_key_currency), "") + " $ " + df.format(data.getDouble(BitcoinNowSyncAdapter.INDEX_TICKER_LAST)));
            dateRate.setText(SimpleDateFormat.getDateTimeInstance().format(data.getInt(BitcoinNowSyncAdapter.INDEX_TICKER_DATE) * 1000L));
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onRedditDataSyncFinish(List<Reddit> redditList) {
        adapter.setEntries(redditList);
        redditDAO.setRedditList(redditList);
        swipeRefreshLayout.setRefreshing(false);
        progress.dismiss();
    }

    public void requestRedditDataSync() {
        progress.show();
        redditDAO.deleteAllReddits();
        RedditDataSyncTask redditDataSyncTask = new RedditDataSyncTask();
        redditDataSyncTask.setListener(this);
        redditDataSyncTask.execute();
    }
}
