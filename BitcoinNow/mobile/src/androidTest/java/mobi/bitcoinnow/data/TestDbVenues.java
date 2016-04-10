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

import java.util.Date;
import java.util.HashSet;

import mobi.bitcoinnow.model.Venue;

public class TestDbVenues extends AndroidTestCase {

    public static final String LOG_TAG = TestDbVenues.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(VenuesDbHelper.DATABASE_NAME);
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
        tableNameHashSet.add(Venue.TABLE_NAME);

        mContext.deleteDatabase(VenuesDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new VenuesDbHelper(this.mContext).getWritableDatabase();
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
        assertTrue("Error: Your database was created without Venue entry table",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + Venue.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> venuesColumnHashSet = new HashSet<String>();
        venuesColumnHashSet.add(Venue.COLUMN_ID);
        venuesColumnHashSet.add(Venue.COLUMN_NAME);
        venuesColumnHashSet.add(Venue.COLUMN_CREATED);
        venuesColumnHashSet.add(Venue.COLUMN_LATITUDE);
        venuesColumnHashSet.add(Venue.COLUMN_LONGITUDE);
        venuesColumnHashSet.add(Venue.COLUMN_CATEGORY);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            venuesColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required entry columns",
                venuesColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createVenuesValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testVenuesTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.
        VenuesDbHelper dbHelper = new VenuesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // First step: Get reference to writable database

        // Create ContentValues of what you want to insert
        // (you can use the createVenuesValues TestUtilities function if you wish)
        ContentValues testValues = createVenuesValues();

        // Insert ContentValues into database and get a row ID back
        long venuesRowId;
        venuesRowId = db.insert(Venue.TABLE_NAME, null, testValues);

        // Query the database and receive a Cursor back
        assertTrue(venuesRowId != -1);

        // Move the cursor to a valid database row
        Cursor cursor = db.query(
                Venue.TABLE_NAME,
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
        TestUtilities.validateCurrentRecord("testInsertReadDb Venue failed to validate",
                cursor, testValues);

        assertFalse("Error: More than one record returned from weather query",
                cursor.moveToNext());

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }

    public ContentValues createVenuesValues() {
        ContentValues venueX = new ContentValues();
        venueX.put(Venue.COLUMN_ID, 1);
        venueX.put(Venue.COLUMN_NAME, "Bitcoin Venue Test");
        venueX.put(Venue.COLUMN_CREATED, new Date().getTime());
        venueX.put(Venue.COLUMN_LATITUDE, "35.000");
        venueX.put(Venue.COLUMN_LONGITUDE, "150.000");
        venueX.put(Venue.COLUMN_CATEGORY, "category x");

        return venueX;
    }
}
