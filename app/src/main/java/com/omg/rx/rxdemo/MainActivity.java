package com.omg.rx.rxdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.jakewharton.rxbinding.view.RxView;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

    private static final String DEALS_URL = "https://rawgit.com/mgujare/try_git/master/deals.json";// dummy url.
    private static final String TAG = "MainActivity";
    private CompositeSubscription _compositeSubscription = new CompositeSubscription();
    private TextView textView;
    private String consumerKey = "FsxTpwjRk15suwXZfeG7Og";;
    private String consumerSecret = "DZ6JHCcW3GldLFzYWKfV5O44GcI";
    private String token = "O9J2OU-3_fsTN6LHLZdAJKzztwhzQT7B";
    private String tokenSecret = "y6lYOZu6udKDJn3r5mMgZIeDD0U";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView3);
        RxView.clicks(b)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        try {
                            Toast.makeText(MainActivity.this, "Fetching data from server.", Toast.LENGTH_SHORT).show();
                            startOkhttpRequest();
                        } catch (Exception e) {
                            Log.e(TAG, "Observer error");
                        }
                    }
                });
        Log.d(TAG, "onCreate");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        _compositeSubscription.clear();
    }

    public Observable<JSONObject> newGetDealData() {
        return Observable.defer(new Func0<Observable<JSONObject>>() {
            @Override
            public Observable<JSONObject> call() {
                try {
                    return Observable.just(getYelpData());
                } catch (JSONException e) { //InterruptedException | ExecutionException | IOException |
                            Log.e("deals", e.getMessage());
                    return Observable.error(e);
                }
            }
        });
    }

    private void startOkhttpRequest() {
        Log.d(TAG, "startOkhttpRequest");
        _compositeSubscription.add(newGetDealData().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<JSONObject>() {
                @Override
                public void onCompleted() {
                    Log.d(TAG, "onCompleted");
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "onError");
                    textView.setText("Error " + e.toString());
                }

                @Override
                public void onNext(JSONObject jsonObject) {
                    textView.setText(jsonObject.toString());
                    Log.d(TAG, "success " + jsonObject.toString());
                }
            }));
    }

    private JSONObject getDealData() throws ExecutionException, InterruptedException,
            IOException, JSONException {
        Log.d(TAG, "getDealData");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(DEALS_URL)
                .build();
        //Make call.
        Response response = client.newCall(request).execute();
        JSONObject jsonObject = new JSONObject(response.body().string());
        return jsonObject;
    }

    private JSONObject getYelpData() throws JSONException {
        String result = null;
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("term", "restaurant");
        queryParams.put("limit", "3");
        CoordinateOptions coordinate = CoordinateOptions.builder()
                .latitude(37.7749)
                .longitude(-122.4194).build();
        YelpAPIFactory apiFactory = new YelpAPIFactory(consumerKey, consumerSecret, token, tokenSecret);
        YelpAPI yelpAPI = apiFactory.createAPI();
        Call<SearchResponse> call = yelpAPI.search(coordinate, queryParams);
        try {
            retrofit2.Response<SearchResponse> response = call.execute();
            Log.d(TAG + "error", " " + response.isSuccessful());
            Log.d(TAG + "res", response.raw().toString());
            result = response.raw().toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return new JSONObject(result);
    }
}
