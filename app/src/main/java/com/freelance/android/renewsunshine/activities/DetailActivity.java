package com.freelance.android.renewsunshine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.freelance.android.renewsunshine.R;
import com.freelance.android.renewsunshine.fragments.DetailFragment;


public class DetailActivity extends ActionBarActivity {

 /*   public static final String DATE_KEY = "date";
    public static final String LOCATION_KEY = "location";*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, fragment)
                    .commit();
            /*getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, new DetailFragment())
                    .commit();*/
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }
}

    /*
     @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String dateString = getActivity().getIntent().getStringExtra(DATE_KEY);

            String[] columns = {
                    WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                    WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                    WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                    WeatherContract.WeatherEntry.COLUMN_DEGREES,
                    WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                    WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING

            };
            mLocation = Utility.getPreferredLocation(getActivity());
            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, dateString);

            return new CursorLoader(
                    getActivity(),
                    weatherUri,
                    columns,
                    null,
                    null,
                    null);}

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if(data.moveToFirst()){
                String description = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
                String dateText = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));

                double high = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
                double low = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));

                boolean isMetric = Utility.isMetric(getActivity());

                TextView dateView = (TextView)getView().findViewById(R.id.list_item_date_textview);
                TextView forecastView = (TextView)getView().findViewById(R.id.list_item_forecast_textview);
                TextView highView = (TextView)getView().findViewById(R.id.list_item_high_textview);
                TextView lowView = (TextView)getView().findViewById(R.id.list_item_low_textview);

                dateView.setText(Utility.formatDate(dateText));
                forecastView.setText(description);
                highView.setText(Utility.formatTemperature(high, isMetric)+"\u00B0");
                lowView.setText(Utility.formatTemperature(low, isMetric)+"\u00B0");

                mForecast = String.format("%s - %s - %s/%s",
                        dateView.getText(),
                        forecastView.getText(),
                        highView.getText(),
                        lowView.getText());
            }*/
