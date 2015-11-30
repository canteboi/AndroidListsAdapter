package com.example.ibanezf.androidweatherapp.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibanezf.androidweatherapp.R;
import com.example.ibanezf.androidweatherapp.model.CurrentLocation;
import com.example.ibanezf.androidweatherapp.model.CurrentWeather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private final OkHttpClient client = new OkHttpClient();

    private CurrentWeather mCurrentWeather;
    private CurrentLocation mCurrentLocation;

    private double mLatitude = 14.6042000;
    private double mLonghitude = 120.9822000;

    //region Properties
    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public double getLonghitude() {
        return mLonghitude;
    }

    public void setLonghitude(double longhitude) {
        this.mLonghitude = longhitude;
    }


    //region Bindings
    @Bind(R.id.timeLabel) TextView mTimeLabel;
    @Bind(R.id.temperatureLabel) TextView mTemperatureLabel;
    @Bind(R.id.humidityValue) TextView mHumidityValue;
    @Bind(R.id.precipValue) TextView mPrecipValue;
    @Bind(R.id.summaryLabel) TextView mSummaryLabel;
    @Bind(R.id.iconImageView) ImageView mIconImageView;
    @Bind(R.id.refreshImageView) ImageView mRefreshImageView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.locationLabel) TextView mLocationLabel;
    //endregion

    //region Overrides
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        getLocation();
        getForeCast(mLatitude, mLonghitude);
        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = mCurrentLocation.getLocation();

                if (location != null) {
                    setLatitude(location.getLatitude());
                    setLonghitude(location.getLongitude());
                }

                getForeCast(getLatitude(), getLonghitude());
            }
        });

    }






    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mCurrentLocation.Connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCurrentLocation.checkPlayServices())
        {
            mCurrentLocation.startLocationUpdates();
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        mCurrentLocation.Disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCurrentLocation.stopLocationUpdates();
    }
    //endregion

    private void getForeCast(double latitude, double longhitude) {

        if (isNetWorkAvailable()) {
            toggleRefresh();
            String url = getString(R.string.forecast_url);
            String apikey = "134806da256b7476535cc3ebfc240aad";


            String forecastUrl = String.format(url
                    , apikey
                    , latitude + ""
                    , longhitude + "");
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertuserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    try {
                        //Response response = call.execute();
                        if (!response.isSuccessful()) {
                            alertuserAboutError();
                        }
                        String jsonData = response.body().string();

                        mCurrentWeather = getCurrentDetails(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                            }
                        });

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else {
            AlertDialogFragment dialog = new AlertDialogFragment();
            String title = getString(R.string.network_unavailable_title);
            String message = getString(R.string.network_unavailable_message);
            dialog.setIsCustomAlert(true);
            dialog.setAlertTitle(title);
            dialog.setAlertMessage(message);

            dialog.show(getFragmentManager(), "error_dialog");

        }
    }

    private void toggleRefresh() {


        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }

    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemp() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "");
        mSummaryLabel.setText(mCurrentWeather.getSummary());
        mLocationLabel.setText(mCurrentWeather.getTimeZone());

        Drawable drawable = ContextCompat.getDrawable(this, mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);


    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setMtime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemp(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        Log.d(TAG, currentWeather.getFormattedTime());

        return currentWeather;
    }

    private boolean isNetWorkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertuserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(),"error_dialog");
    }

    private void getLocation() {
        mCurrentLocation = new CurrentLocation(this, getApplicationContext(), this);
        //mCurrentLocation.setListener(this);

        if (mCurrentLocation.checkPlayServices()) {

            // Building the GoogleApi client
            mCurrentLocation.buildGoogleApiClient();

            mCurrentLocation.createLocationRequest();

            Location location = mCurrentLocation.getLocation();

            if (location == null){
                AlertDialogFragment dialog = new AlertDialogFragment();
                String title = "Location Not found.";
                String message = "Couldn't get the location. " +
                        "Make sure location is enabled on the device. " +
                        "Using MANILA as default."
                        ;
                dialog.setIsCustomAlert(true);
                dialog.setAlertTitle(title);
                dialog.setAlertMessage(message);

                dialog.show(getFragmentManager(), "error_dialog");
            }
            else {

                setLatitude(location.getLatitude());
                setLonghitude(location.getLongitude());
            }
        }
//        else {
//            AlertDialogFragment dialog = new AlertDialogFragment();
//            String title = "Google Play Services Not found.";
//            String message = "Couldn't get the location. " +
//                    "Make sure Google Play Services is allowed.";
//            dialog.setIsCustomAlert(true);
//            dialog.setAlertTitle(title);
//            dialog.setAlertMessage(message);
//
//            dialog.show(getFragmentManager(), "error_dialog");
//        }
    }


}
