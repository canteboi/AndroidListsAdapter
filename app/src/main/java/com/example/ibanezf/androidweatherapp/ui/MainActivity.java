package com.example.ibanezf.androidweatherapp.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.ibanezf.androidweatherapp.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final String apikey = "134806da256b7476535cc3ebfc240aad";
    //private double latitude = 37.8267 ;
    private double latitude = 9999 ;
    private double longhitude = -122.423 ;
    private final OkHttpClient client = new OkHttpClient();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupApi();
    }

    private void setupApi() {

        if (isNetWorkAvailable()) {
            String url = getString(R.string.forecast_url);
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
                    alertuserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        //Response response = call.execute();

                        if (!response.isSuccessful()) {
                            alertuserAboutError();
                        }

                        Log.v(TAG, response.body().string());

                    } catch (IOException e) {
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

            dialog.show(getFragmentManager(),"error_dialog");
        }
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


}
