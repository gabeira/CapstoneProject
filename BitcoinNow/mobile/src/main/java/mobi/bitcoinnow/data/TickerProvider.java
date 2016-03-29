package mobi.bitcoinnow.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import mobi.bitcoinnow.data.TickerContract.TickerEntry;

public class TickerProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TickerDbHelper mOpenHelper;

    static final int TICKER = 100;

    private static final SQLiteQueryBuilder sTickerByCurrencySettingQueryBuilder;

    static {
        sTickerByCurrencySettingQueryBuilder = new SQLiteQueryBuilder();

        sTickerByCurrencySettingQueryBuilder.setTables(TickerEntry.TABLE_NAME);
    }

    private Cursor getTicker(Uri uri, String[] projection) {
        return sTickerByCurrencySettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                null
        );
    }

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TickerContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // TickerContract to help define the types to the UriMatcher.
        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, TickerContract.PATH_TICKER, TICKER);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new TickerDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TICKER:
                return TickerEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case TICKER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TickerEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TICKER: {
                long _id = db.insert(TickerEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = TickerEntry.getTickerUri();
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case TICKER:
                rowsDeleted = db.delete(
                        TickerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case TICKER:
                rowsUpdated = db.update(TickerEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}