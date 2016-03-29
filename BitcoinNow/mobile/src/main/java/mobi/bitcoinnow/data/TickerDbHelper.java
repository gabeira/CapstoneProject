package mobi.bitcoinnow.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import mobi.bitcoinnow.data.TickerContract.TickerEntry;

/**
 * Manages a local database for ticker data.
 */
public class TickerDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "ticker.db";

    public TickerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TICKER_TABLE = "CREATE TABLE " + TickerEntry.TABLE_NAME + " (" +
                TickerEntry.COLUMN_ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE," +
                TickerEntry.COLUMN_HIGH + " REAL NOT NULL, " +
                TickerEntry.COLUMN_LOW + " REAL NOT NULL, " +
                TickerEntry.COLUMN_VOL + " REAL NOT NULL, " +
                TickerEntry.COLUMN_LAST + " REAL NOT NULL, " +
                TickerEntry.COLUMN_BUY + " REAL NOT NULL, " +
                TickerEntry.COLUMN_SELL + " REAL NOT NULL," +
                TickerEntry.COLUMN_DATE + " INTEGER NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_TICKER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next line
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TickerEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
