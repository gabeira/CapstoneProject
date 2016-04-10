package mobi.bitcoinnow.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import mobi.bitcoinnow.model.Venue;

/**
 * Manages a local database for Map Venues data.
 */
public class VenuesDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "coinmap.db";

    public VenuesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_VENUES_TABLE = "CREATE TABLE " + Venue.TABLE_NAME + " (" +
                Venue.COLUMN_ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE," +
                Venue.COLUMN_NAME + " TEXT NOT NULL, " +
                Venue.COLUMN_CREATED + " INTEGER NOT NULL, " +
                Venue.COLUMN_LATITUDE + " TEXT NOT NULL, " +
                Venue.COLUMN_LONGITUDE + " TEXT NOT NULL, " +
                Venue.COLUMN_CATEGORY + " TEXT);";

        sqLiteDatabase.execSQL(SQL_CREATE_VENUES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next line
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Venue.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}