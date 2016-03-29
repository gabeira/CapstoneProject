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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

import mobi.bitcoinnow.data.TickerContract.TickerEntry;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(TickerDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(TickerContract.TickerEntry.TABLE_NAME);

        mContext.deleteDatabase(TickerDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new TickerDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly", c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without ticker entry table",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + TickerContract.TickerEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> tickerColumnHashSet = new HashSet<String>();
//        tickerColumnHashSet.add(TickerEntry._ID);
        tickerColumnHashSet.add(TickerEntry.COLUMN_DATE);
        tickerColumnHashSet.add(TickerEntry.COLUMN_LOW);
        tickerColumnHashSet.add(TickerEntry.COLUMN_HIGH);
        tickerColumnHashSet.add(TickerEntry.COLUMN_VOL);
        tickerColumnHashSet.add(TickerEntry.COLUMN_LAST);
        tickerColumnHashSet.add(TickerEntry.COLUMN_BUY);
        tickerColumnHashSet.add(TickerEntry.COLUMN_SELL);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            tickerColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required entry columns",
                tickerColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createTickerValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testTickerTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.
        TickerDbHelper dbHelper = new TickerDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // First step: Get reference to writable database

        // Create ContentValues of what you want to insert
        // (you can use the createTickerValues TestUtilities function if you wish)
        ContentValues testValues = TestUtilities.createTickerValues();

        // Insert ContentValues into database and get a row ID back
        long tickerRowId;
//        tickerRowId =
        db.insert(TickerContract.TickerEntry.TABLE_NAME, null, testValues);
        tickerRowId = db.insert(TickerContract.TickerEntry.TABLE_NAME, null, testValues);
//        tickerRowId = db.update(TickerContract.TickerEntry.TABLE_NAME, testValues,"*", null);

        // Query the database and receive a Cursor back
        assertTrue(tickerRowId != -1);

        // Move the cursor to a valid database row
        Cursor cursor = db.query(
                TickerContract.TickerEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertTrue("Error: No Records returned from weather query", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("testInsertReadDb weatherEntry failed to validate",
                cursor, testValues);

        assertFalse("Error: More than one record returned from weather query",
                cursor.moveToNext());

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }

}
