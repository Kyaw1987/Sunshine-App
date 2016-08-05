package com.freelance.android.renewsunshine.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.freelance.android.renewsunshine.data.WeatherContract.LocationEntry;
import com.freelance.android.renewsunshine.data.WeatherContract.WeatherEntry;

import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 07/24/2016.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    static public String TEST_CITY_NAME = "North Pole";
    static public String TEST_LOCATION = "99705";
    static public long TEST_DATE = 20141205;

   /* public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }*/

    public void testDeleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Location table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(LocationEntry.CONTENT_URI, true, tco);
        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);

        // Did our content observer get called?  Students:  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating LocationEntry.",
                cursor, testValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, tco);

        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, weatherValues);
        assertTrue(weatherInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating WeatherEntry insert.",
                weatherCursor, weatherValues);

        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        weatherValues.putAll(testValues);

        // Get the joined Weather and Location data
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TestUtilities.TEST_LOCATION),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location Data.",
                weatherCursor, weatherValues);

        // Get the joined Weather and Location data with a start date
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(
                        TestUtilities.TEST_LOCATION,
                        TestUtilities.TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location Data with start date.",
                weatherCursor, weatherValues);

        // Get the joined Weather data for a specific date
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TestUtilities.TEST_LOCATION, TestUtilities.TEST_DATE),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location data for a specific date.",
                weatherCursor, weatherValues);
    }

    /*
     This test uses the provider to insert and then update the data. Uncomment this test to
     see if your update location is functioning correctly.
  */
    public void testUpdateLocation() {

        testDeleteAllRecords(); /*actually,this code is no need in new version and this method used to in old version of code.*/
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().
                insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor locationCursor = mContext.getContentResolver().query(LocationEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        locationCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[]{Long.toString(locationRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        locationCursor.unregisterContentObserver(tco);
        locationCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,   // projection
                LocationEntry._ID + " = " + locationRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateLocation.  Error validating location entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    public void testGetType() {

        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
                WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the WeatherEntry CONTENT_URI with location should return WeatherEntry.CONTENT_TYPE",
                WeatherEntry.CONTENT_TYPE, type);

        long testDate = 1419120000L; // December 21st, 2014
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather/1419120000
        assertEquals("Error: the WeatherEntry CONTENT_URI with location and date should return WeatherEntry.CONTENT_ITEM_TYPE",
                WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals("Error: the LocationEntry CONTENT_URI should return LocationEntry.CONTENT_TYPE",
                LocationEntry.CONTENT_TYPE, type);
    }

    static public void validateCursor(ContentValues expectedValues, Cursor valueCursor) {

//        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(-1 == idx);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
//        valueCursor.close();
    }

    static public ContentValues getLocationContentValues() {

        ContentValues lCV = new ContentValues();

        String testLocationSetting = TEST_LOCATION;
        double testLatitude = 64.772;
        double testLongitude = -147.355;

        lCV.put(LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        lCV.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        lCV.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        lCV.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        return lCV;
    }

    static public ContentValues getWeatherContentValues(long locationRowId) {

        ContentValues wCV = new ContentValues();

        wCV.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        wCV.put(WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
        wCV.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        wCV.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        wCV.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        wCV.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        wCV.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        wCV.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        wCV.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        wCV.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        return wCV;
    }


    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver locationObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(LocationEntry.CONTENT_URI, true, locationObserver);

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, weatherObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        locationObserver.waitForNotificationOrFail();
        weatherObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(locationObserver);
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
    }

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
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Location table during delete", 0, cursor.getCount());
        cursor.close();
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static ContentValues[] createBulkInsertWeatherValues(long locationRowId) {
//        long currentTestDate = TestUtilities.TEST_DATE;
        long currentTestDate = TEST_DATE;
        long millisecondsInADay = 1000 * 60 * 60 * 24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];


        /*        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) or below code indicates that data count for BULK_INSERT_RECORDS_TO_INSERT.*/
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate += millisecondsInADay) {

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, currentTestDate);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2 + 0.01 * (float) i);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3 - 0.01 * (float) i);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75 + i);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65 - i);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Yangon");
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5 + 0.2 * (float) i);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);

            returnContentValues[i] = weatherValues;
        }
        return returnContentValues;
    }

    // Student: Uncomment this test after you have completed writing the BulkInsert functionality
    // in your provider.  Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time, so really do implement the
    // BulkInsert ContentProvider function.
    public void testBulkInsert() {
        // first, let's create a location value
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating LocationEntry.",
                cursor, testValues);

        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertWeatherValues(locationRowId);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                WeatherEntry.COLUMN_DATETEXT + " ASC"  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }


}


/*

    public void testInsertReadProvider() {

        ContentValues locationValues = getLocationContentValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues);
        long locationRowId = ContentUris.parseId(locationUri);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        Cursor locationCursor = mContext.getContentResolver().query(LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (locationCursor.moveToFirst()) {

            validateCursor(locationValues, locationCursor);

            ContentValues weatherValues = getWeatherContentValues(locationRowId);

            Uri weatherUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
            long weatherRowId = ContentUris.parseId(weatherUri); *//*actually, no need this code*//*

            Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );

            if (weatherCursor.moveToFirst()) {
                validateCursor(weatherValues, weatherCursor);
            } else {
                fail("No weather data returned!");
            }

            weatherCursor.close();

            weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocation(TEST_LOCATION),
                    null,
                    null,
                    null,
                    null
            );

            if (weatherCursor.moveToFirst()) {
                validateCursor(weatherValues, weatherCursor);
            } else {
                fail("No weather data returned!");
            }

            weatherCursor.close();

            weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION, TEST_DATE),
                    null,
                    null,
                    null,
                    null
            );

            if (weatherCursor.moveToFirst()) {
                validateCursor(weatherValues, weatherCursor);
            } else {
                fail("No weather data returned!");
            }

            weatherCursor.close();

            weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE),
                    null,
                    null,
                    null,
                    null
            );

            if (weatherCursor.moveToFirst()) {
                validateCursor(weatherValues, weatherCursor);
            } else {
                fail("No weather data returned!");
            }
        } else {
            fail("No values returned :(");
        }
    }

++++============++++

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/weather/94074
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        String testLocation = "94074";
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocation(testLocation));
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/weather/94074/20140612
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather/1419120000
        String testDate = "20140612"; // December 21st, 2014
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

++++============++++

     public void testInsertReadDb() {

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues locationValues = getLocationContentValues();

        long locationRowId = db.insert(LocationEntry.TABLE_NAME, null, locationValues);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        Cursor locationCursor = db.query(
                LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (locationCursor.moveToFirst()) {

            validateCursor(locationValues, locationCursor);

            ContentValues weatherValues = getWeatherContentValues(locationRowId);

            long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
            assertTrue(weatherRowId != -1);

            Cursor weatherCursor = db.query(
                    WeatherEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (weatherCursor.moveToFirst()) {

                validateCursor(weatherValues, weatherCursor);

            } else {
                fail("No weather data returned!");
            }
        } else {
            fail("No values returned :(");
        }
    }

++++============++++

    public void testInsertReadDb() {
        String testName = "North Pole";
        String testLocationSettings = "99705";
        double testLatitude = 64.772;
        double testLongitude = -147.355;

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cV = new ContentValues();
        cV.put(LocationEntry.COLUMN_CITY_NAME, testName);
        cV.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSettings);
        cV.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        cV.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        long locationRowId = db.insert(LocationEntry.TABLE_NAME, null, cV);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        Cursor locationCursor = db.query(
                LocationEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        );

        if(locationCursor.moveToFirst()){

            int locationIndex = locationCursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = locationCursor.getString(locationIndex);

            int nameIndex = locationCursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME);
            String name = locationCursor.getString(nameIndex);

            int latIndex = locationCursor.getColumnIndex(LocationEntry.COLUMN_COORD_LAT);
            double latitude = locationCursor.getDouble(latIndex);

            int longIndex = locationCursor.getColumnIndex(LocationEntry.COLUMN_COORD_LONG);
            double longitude = locationCursor.getDouble(longIndex);

            assertEquals(testName, name);
            assertEquals(testLocationSettings, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

            long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
            assertTrue(weatherRowId != -1);

            Cursor weatherCursor = db.query(
                    WeatherEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if(weatherCursor.moveToFirst()) {

                int dateIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT);
                String date = weatherCursor.getString(dateIndex);

                int degreesIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES);
                double degrees = weatherCursor.getDouble(degreesIndex);

                int humidityIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY);
                double humidity = weatherCursor.getDouble(humidityIndex);

                int pressureIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE);
                double pressure = weatherCursor.getDouble(pressureIndex);

                int maxIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP);
                double max = weatherCursor.getDouble(maxIndex);

                int minIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP);
                double min = weatherCursor.getDouble(minIndex);

                int descIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC);
                double desc = weatherCursor.getDouble(descIndex);

                int windIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED);
                double windSpeed = weatherCursor.getDouble(windIndex);

                int weatherIdIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID);
                int weather_id = weatherCursor.getInt(weatherIdIndex);

            }
            else{
                fail("No weather data returned!");
            }
        }
        else{
            fail("No values returned :(");
        }
    }*/