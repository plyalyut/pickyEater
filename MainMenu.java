package pickyeater.pickyeaterandroid;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//Google Play functionality
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.LocationSource;

import android.Manifest;
import android.util.Log;
import android.widget.TextView;

//Main Menu class, implements failures to connect to Google Play Services
public  class MainMenu extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
{
    //sets up a test textbox
    private TextView test;

    //Console Tag for Error Identification
    private final String TAG = "PlayServices";

    //When Google Play Connects, logs to console
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Connection Established");
        Location pPlace = getLocation();
        locationString(pPlace);
    }

    //When connection is lost it tries to reconnect
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Connection lost. Trying to get it back.");
        googleApiClient.connect();
    }

    //Logs a failed connection
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "Connection Failed");
    }

    //Starts google play services.
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "Connecting to Google Play."); //Console log for connecting
        GoogleApiAvailability gAv = GoogleApiAvailability.getInstance(); //checks google play is available on this device
        int result = gAv.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            gAv.getErrorDialog(this, result, 1).show();//error if google play is not available & brings up screen to get google play
        } else {
            googleApiClient.connect(); //connects to google play
        }
        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Location pPlace = getLocation();
            locationString(pPlace);
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] perms, int[] results) {
        if (reqCode == 1) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                Location pPlace = getLocation();
                locationString(pPlace);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            Log.i(TAG, "Disconnecting from Google Play");
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "Pausing Google Play");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "Resuming Google Play");
    }


    private GoogleApiClient googleApiClient;
    //private LocationRequest requestloc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();


        test = (TextView) findViewById(R.id.testbox);
        //sets up a placepicker
        //nearby();
        //Location loc = getLocation();
        //test.setText("Lat: " +loc.getLatitude()+" Long: " +loc.getLongitude());
    }

    public void nearby() {
        int PLACE_PICKER_REQUEST = 1;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (com.google.android.gms.common.GooglePlayServicesRepairableException e) {
            //test.setText("Error with Google Play Services");
        } catch (com.google.android.gms.common.GooglePlayServicesNotAvailableException e) {
            //test.setText("Error with Google Play Services");
        }
    }

    public Location getLocation() {
        try {
            Location findUser = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            return findUser;
        } catch (SecurityException s) {
            return null;
        }
    }


    public void locationString(Location loc){
        if(loc != null)
        {
            test.setText("Lat: " +String.valueOf(loc.getLatitude())+" Long: " +String.valueOf(loc.getLongitude()));
        }
            //test.setText("hello from the other side");
    }
}
