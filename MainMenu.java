package pickyeater.pickyeaterandroid;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.StrictMode;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.LocationSource;

import android.Manifest;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

//Main Menu class, implements failures to connect to Google Play Services
public  class MainMenu extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
{
    //sets up a test textbox
    private TextView test;
    public int searchRadius = 1000;
    public double latP;
    public double longP;
    public String type = "restaurant";
    private String APIkey = "AIzaSyD0wuYcL5-fir9uciCfaZXLSb2IIM8_RB0";
    public String location;

    //Console Tag for Error Identification
    private final String TAG = "PlayServices";

    //When Google Play Connects, logs to console
    @Override
    public void onConnected(Bundle connectionHint) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ArrayList<Place> nearbyRest = null;


        Log.d(TAG, "Connection Established");
        Location pPlace = getLocation();
        if (pPlace != null){
            latP = getLat(pPlace);
            longP = getLong(pPlace);
            location = String.valueOf(latP)+","+String.valueOf(longP);
        }

        TextView test = (TextView) findViewById(R.id.testbox);
        test.setText(location);

        StringBuilder jsonResults = new StringBuilder();

        StringBuilder jsonUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
        jsonUrl.append(location);
        jsonUrl.append("&radius=");
        jsonUrl.append(searchRadius);
        jsonUrl.append("&type=");
        jsonUrl.append(type);
        jsonUrl.append("&key=");
        jsonUrl.append(APIkey);

        HttpURLConnection conn = null;
        try {
            URL address = new URL(jsonUrl.toString());
            conn = (HttpsURLConnection) address.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while((read = in.read(buff)) != -1)
            {
                jsonResults.append(buff, 0, read);
            }
        }
        catch (MalformedURLException e)
        {
            Log.i("URL", "Error, bad Url");
        }
        catch (IOException e)
        {
            Log.i("IO", "IO exception caught");
        }
        finally
        {
            if(conn!=null)
            conn.disconnect();
        }

        try {
            JSONObject restObj = new JSONObject(jsonResults.toString());
            JSONArray restArray = restObj.getJSONArray("results");
            nearbyRest =  new ArrayList<Place>(restArray.length());
            //for(int i = 0; i<restArray.length(); i++)
            test.setText(restArray.getJSONObject(randomizer(restArray.length())).getString("name"));
        }
        catch(JSONException e)
        {
            Log.i("JSON", "JSON error");
        }
    }

    public int randomizer(int length){
        return (int)(length*Math.random());
    }

    //gets an image from a place search
    public static Drawable LoadImageFromWeb(int maxwidht, String reference, String APIkey){
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?maxwidth=");
        url.append(maxwidht);
        url.append("&photoreference=");
        url.append(reference);
        url.append("&key=");
        url.append(APIkey);
        try {
            InputStream is = (InputStream) new URL(url.toString()).getContent();
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        }
        catch(MalformedURLException e){
            Log.i("URL","Bad Url");
            return null;
        }
        catch(IOException e){
            Log.i("IO", "IO Exception");
            return null;
        }
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

    //requests permissions to get your coordinates
    @Override
    public void onRequestPermissionsResult(int reqCode, String[] perms, int[] results) {
        if (reqCode == 1) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                Location pPlace = getLocation();
                if (pPlace != null){
                    latP = getLat(pPlace);
                    longP = getLong(pPlace);
                }
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

   // public JSONObject nearbyPlaces(){

   // }


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
    }

    public Location getLocation() {
        try {
            Location findUser = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            return findUser;
        } catch (SecurityException s) {
            return null;
        }
    }

    public double getLat(Location loc){
            return loc.getLatitude();
        }

    public double getLong(Location loc){
            return loc.getLongitude();
    }

}
