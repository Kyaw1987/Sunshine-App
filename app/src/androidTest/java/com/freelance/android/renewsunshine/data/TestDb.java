package com.freelance.android.renewsunshine.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.freelance.android.renewsunshine.data.WeatherContract.WeatherEntry;
import com.freelance.android.renewsunshine.data.WeatherContract.LocationEntry;

import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 07/24/2016.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    static public String TEST_CITY_NAME = "North Pole";

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

        String testLocationSetting = "99705";
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
        wCV.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
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
}


    /*public void testInsertReadDb() {
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