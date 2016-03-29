/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mobi.bitcoinnow.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import mobi.bitcoinnow.data.TickerContract.TickerEntry;


/*
    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.
    Students: Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.
       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                TickerEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                TickerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Ticker table during delete", 0, cursor.getCount());
        cursor.close();

    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // TickerProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                TickerProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: TickerProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + TickerContract.CONTENT_AUTHORITY,
                    providerInfo.authority, TickerContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: TickerProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
         */
    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(TickerEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the TickerEntry CONTENT_URI should return TickerEntry.CONTENT_TYPE",
                TickerEntry.CONTENT_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic weather query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicTickerQuery() {
        // insert our test records into the database
        TickerDbHelper dbHelper = new TickerDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues weatherValues = TestUtilities.createTickerValues();

        long weatherRowId = db.insert(TickerEntry.TABLE_NAME, null, weatherValues);
        assertTrue("Unable to Insert TickerEntry into the Database", weatherRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor weatherCursor = mContext.getContentResolver().query(
                TickerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicTickerQuery", weatherCursor, weatherValues);
    }

    public void testInsertReadProvider() {
        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = TestUtilities.createTickerValues();
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(TickerEntry.CONTENT_URI, true, tco);

        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(TickerEntry.CONTENT_URI, weatherValues);
        assertTrue(weatherInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                TickerEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating TickerEntry insert.",
                weatherCursor, weatherValues);

    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TickerEntry.CONTENT_URI, true, weatherObserver);

        deleteAllRecordsFromProvider();

        weatherObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
    }

}
