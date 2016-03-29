package mobi.bitcoinnow.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;

import mobi.bitcoinnow.R;
import mobi.bitcoinnow.MainActivity;
import mobi.bitcoinnow.data.TickerContract;

public class BitcoinNowWidgetIntentService extends IntentService {

    private static final String[] MATCH_COLUMNS = {
            TickerContract.TickerEntry.COLUMN_DATE,
            TickerContract.TickerEntry.COLUMN_LAST
    };

    // these indices must match the projection
    public static final int INDEX_TICKER_HIGH = 1;
    public static final int INDEX_TICKER_LOW = 2;
    public static final int INDEX_TICKER_VOL = 3;
    public static final int INDEX_TICKER_LAST = 4;
    public static final int INDEX_TICKER_BUY = 5;
    public static final int INDEX_TICKER_SELL = 6;
    public static final int INDEX_TICKER_DATE = 7;

    public BitcoinNowWidgetIntentService() {
        super("BitcoinNowWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                BitcoinNowWidgetProvider.class));

        // Get tomorrow's match from the ContentProvider
        Uri matchesWithDateUri = TickerContract.TickerEntry.getTickerUri();

        Cursor data = getContentResolver().query(matchesWithDateUri, null, null, null, null);
        String last = "";

        String selectedProvider = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_key_bitcoin_provider), "");
        String date = "";

        if (data == null) {
//            empty = true;
        } else if (!data.moveToFirst()) {
            data.close();
//            empty = true;
        } else {

            // Extract the Match data from the Cursor
            last = data.getString(INDEX_TICKER_LAST);
            date = SimpleDateFormat.getDateTimeInstance().format(data.getLong(INDEX_TICKER_DATE) * 1000L);
            Log.d("Widget", "$ " + last + " from " + selectedProvider + " at " + date);

            data.close();
        }
        // Perform this loop procedure for each widget
        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_default_width);
            int layoutId;
//            if (widgetWidth >= defaultWidth) {
//                layoutId = R.layout.widget_bitcoin;
//            } else {
            layoutId = R.layout.widget_bitcoin;
//            }
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, last);
            }
//            if (widgetWidth >= defaultWidth) {
//                views.setTextViewText(R.id.widget_provider, last);
//            } else {
            views.setTextViewText(R.id.widget_last, last);
            views.setTextViewText(R.id.widget_provider, selectedProvider);
            views.setTextViewText(R.id.widget_date, date);
//            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.layout, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp, displayMetrics);
        }
        return getResources().getDimensionPixelSize(R.dimen.widget_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.layout, description);
    }
}
