package mobi.bitcoinnow.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for Bitcoin Ticker database.
 */
public class TickerContract {

    public static final String CONTENT_AUTHORITY = "mobi.android.bitcoinnow";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TICKER = "ticker";

    /* Inner class that defines the table contents of the ticker table */
    public static final class TickerEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TICKER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TICKER;

        public static final String TABLE_NAME = "ticker";

        public static final String COLUMN_ID = "id";
        public static final int COLUMN_ID_VALUE = 1;
        public static final String COLUMN_HIGH = "high";
        public static final String COLUMN_LOW = "low";
        public static final String COLUMN_VOL = "vol";
        public static final String COLUMN_LAST = "last";
        public static final String COLUMN_BUY = "buy";
        public static final String COLUMN_SELL = "sell";
        public static final String COLUMN_DATE = "date";

        public static Uri getTickerUri() {
            return CONTENT_URI;
        }

    }
}
