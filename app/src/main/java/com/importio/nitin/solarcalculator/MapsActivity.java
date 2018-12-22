package com.importio.nitin.solarcalculator;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.importio.nitin.solarcalculator.models.MyPlaceInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    private static final int LOCATION_PERMISSION_REQ_CODE = 234;
    private static final String TAG = "Nitin";
    private static final float DEFAULT_ZOOM = 15f;
    private int DATE;

    private GoogleMap mMap;
    private GeoDataClient mGeoDataClient;
    private GoogleApiClient mGoogleApiClient;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private MyPlaceInfo mPlace;

    private AutoCompleteTextView mSearchBar;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (places.getStatus().isSuccess()) {
                Place place = places.get(0);
                moveCamera(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude),
                        place.getName().toString());

                places.release();
            } else {
                Log.d(TAG, "onResult: place not found");
                places.release();
            }
        }
    };

    private void locatePlace() {
        Log.d(TAG, "locatePlace: locating place");
        String str = mSearchBar.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(str, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "found location" + address.toString());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), address.getAddressLine(0));
        }
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        Toast.makeText(this, "map is ready", Toast.LENGTH_SHORT).show();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d("Nitin", "found current location");
                            Location currLocation = (Location) task.getResult();

                            if (currLocation != null) {
                                moveCamera(new LatLng(currLocation.getLatitude(), currLocation.getLongitude()), "My location");
                            }
                        } else {
                            Toast.makeText(MapsActivity.this, "location not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException ex) {
            Log.e("Nitin", "security exception:" + ex.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, String title) {
        mPlace = new MyPlaceInfo(latLng.latitude, latLng.longitude, title);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MapsActivity.DEFAULT_ZOOM));

        if (!title.equals("My location")) { //don't add marker for user current location
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);

            mMap.addMarker(options);
            hideSoftKeyboard();
        }

        new GetPhaseTimeAsync().execute("https://www.timeanddate.com/sun/@" + latLng.latitude + "," + latLng.longitude, "" + DATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mSearchBar = findViewById(R.id.search_bar);
        mSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    locatePlace();
                    return true;
                }
                return false;
            }
        });
        mSearchBar.setOnItemClickListener(mAutocompleteClickListener);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mGeoDataClient = Places.getGeoDataClient(this);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(
                this,
                mGeoDataClient,
                new LatLngBounds(new LatLng(10.362045, 77.426159), new LatLng(31.816808, 76.668343)),
                null);

        mSearchBar.setAdapter(mPlaceAutocompleteAdapter);

        String date = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
        DATE = Integer.parseInt(date);

        if (isServicesOK()) {
            getLocationPermission();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }

    private boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //error but we can resolve it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, 123);
            dialog.show();
        } else {
            Toast.makeText(this, "google services unavailable", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            initMap();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQ_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = false;
                    return;
                }
                mLocationPermissionGranted = true;
                initMap();
            }
        }
    }

    public void gpsIconClicked(View view) {
        Log.d(TAG, "gpsIconClicked: clicked gps icon");
        getDeviceLocation();
    }

    public void prevDayClicked(View view) {
        if (DATE > 1) {
            DATE--;
        }
        new GetPhaseTimeAsync().execute("https://www.timeanddate.com/sun/@" + mPlace.getLat() + "," + mPlace.getLng(), "" + DATE);
    }

    public void todayClicked(View view) {
        String date = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
        DATE = Integer.parseInt(date);

        getDeviceLocation();
    }

    public void nextDayClicked(View view) {
        if (DATE < 30) {
            DATE++;
        }
        new GetPhaseTimeAsync().execute("https://www.timeanddate.com/sun/@" + mPlace.getLat() + "," + mPlace.getLng(), "" + DATE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();

            AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            String placeId = Objects.requireNonNull(item).getPlaceId();
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    class GetPhaseTimeAsync extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPostExecute(String[] result) {
            ProgressBar progress = findViewById(R.id.progress);
            progress.setVisibility(View.GONE);

            TextView sunrise = findViewById(R.id.sunrise);
            TextView sunset = findViewById(R.id.sunset);
            TextView moonrise = findViewById(R.id.moonrise);
            TextView moonset = findViewById(R.id.moonset);

            sunrise.setText(result[0]);
            sunset.setText(result[1]);
            moonrise.setText(result[2]);
            moonset.setText(result[3]);
        }

        @Override
        protected void onPreExecute() {
            ProgressBar progress = findViewById(R.id.progress);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {
            String result[] = new String[4];
            Document document;
            try {
                int day = Integer.parseInt(params[1]);

                //sunrise and sunset
                document = Jsoup.connect(params[0]).get();

                Elements tableBody = document.getElementsByTag("tbody");


                //sunrise
                Element data = tableBody.get(0).child(day - 1);
                String sunrise = data.child(1).text().substring(0, 5);

                int openBrace = data.child(1).text().indexOf("(");
                int closeBrace = data.child(1).text().indexOf(")");
                String degree = data.child(1).text().substring(openBrace + 1, closeBrace - 1);

                String direction = data.child(1).child(0).attr("title");
                Log.d(TAG, "sunrise=" + sunrise + " " + degree + " degrees " + direction);
                result[0] = sunrise;


                //sunset
                String sunset = data.child(2).text().substring(0, 5);

                openBrace = data.child(2).text().indexOf("(");
                closeBrace = data.child(2).text().indexOf(")");
                degree = data.child(2).text().substring(openBrace + 1, closeBrace - 1);

                direction = data.child(2).child(0).attr("title");
                Log.d(TAG, "sunset=" + sunset + " " + degree + " degrees " + direction);
                result[1] = sunset;

                //moonrise and moonset

                document = Jsoup.connect(params[0].replace("sun", "moon")).get();
                tableBody = document.getElementsByTag("tbody");

                data = tableBody.get(0).child(day - 1);
                String moonrise = data.child(1).text();
                String moonset = data.child(3).text();
                if (moonrise.equals("-")) {
                    moonrise = data.child(4).text();
                    moonset = data.child(2).text();
                }

                result[2] = moonrise;
                result[3] = moonset;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }
    }
}
