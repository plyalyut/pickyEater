package pickyeater.pickyeaterandroid;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.StrictMode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//Google Play functionality
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.common.ConnectionResult;

import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;

import android.util.StringBuilderPrinter;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

//Main Menu class, implements failures to connect to Google Play Services
public  class MainMenu extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    //sets up a textbox & image for the first restaurant
    private TextView resTitle1;
    private ImageView resImage1;
    //sets up a textbox & image for the second restaurant
    private TextView resTitle2;
    private ImageView resImage2;

    //random restaurant numbers
    private int randomRes1;
    private int randomRes2;

    int width;
    int height;

    //sets up the array of nearby places
    ArrayList<Place> nearbyRest;

    //jsonArray for the restaurant results
    JSONArray restArray;

    public int searchRadius = 1000; //how far do you want to search
    public double latP;//coordinate for the person's latitude
    public double longP;//coordinate for the person's longitude
    public String type = "restaurant";
    private String APIkey = "AIzaSyD0wuYcL5-fir9uciCfaZXLSb2IIM8_RB0"; //our API key ### GET AN ACTUAL KEY UPON RELEASE
    //private String APIkey = "AIzaSyC8sTME_6ujyMOiA690lldcUr7FrcAKtLY"; //backup key for teting purposes
    public String location; //latitude & longitude displayed for ##testing purposes

    //Console Tag for Error Identification
    private final String TAG = "PlayServices"; //error log identifier

    private final int smallImageRes = 500; //resolution for downloaded images

    //When Google Play Connects, logs to console
    @Override
    public void onConnected(Bundle connectionHint) {
        //allows the ability to obtain HTTP access
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //A code to indicate that the connection was a success
        Log.d(TAG, "Connection Established");
        Location pPlace = getLocation(); //gets the user's current location
        if (pPlace != null) {
            latP = pPlace.getLatitude();
            longP = pPlace.getLongitude();
            location = String.valueOf(latP) + "," + String.valueOf(longP); //testString #remove in actual release
        }

        //initialize the views from the xml file
        resTitle1 = (TextView) findViewById(R.id.firstRes);
        resImage1 = (ImageView) findViewById(R.id.resIm);
        resTitle2 = (TextView) findViewById(R.id.secondRes);
        resImage2 = (ImageView) findViewById(R.id.resIm2);

        //sets the text to the location value
        resTitle1.setText(location);

        //creates a new string holding the json results
        StringBuilder jsonResults;

        //creates the url to connect to
        StringBuilder jsonUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
        jsonUrl.append(location);
        jsonUrl.append("&radius=");
        jsonUrl.append(searchRadius);

        jsonUrl.append("&opennow");

        jsonUrl.append("&type=");
        jsonUrl.append(type);
        jsonUrl.append("&key=");
        jsonUrl.append(APIkey);

        jsonResults=createJSON(jsonUrl);

        JSONObject restObj;
        Boolean hasNext=true;

        nearbyRest = new ArrayList<>();
            do{
                try {
                    restObj = new JSONObject(jsonResults.toString());
                    restArray = restObj.getJSONArray("results");
                    hasNext=restObj.has("next_page_token");

                    for (int i = 0; i < restArray.length(); i++) {
                        Place place = new Place();
                        place.name = restArray.getJSONObject(i).getString("name");
                        try {
                            JSONArray photos = restArray.getJSONObject(i).getJSONArray("photos");
                            place.photoImage = photos.getJSONObject(0).getString("photo_reference");
                        } catch (JSONException e) {
                            Log.i("JSON", "JSON Photo Error");
                        }
                        nearbyRest.add(place);
                    }
                    if(hasNext){
                        String nextPage = restObj.getString("next_page_token");
                        jsonUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=");
                        jsonUrl.append(nextPage);
                        jsonUrl.append("&key=");
                        jsonUrl.append(APIkey);
                        jsonResults = createJSON(jsonUrl);
                    }
                }
                catch (JSONException e){
                    Log.i("JSON", "JSON main error");
                }
            }while(hasNext);

            randomRes1 = randomizer(nearbyRest.size());
            randomRes2 = randomizer(nearbyRest.size());
            while (randomRes1 == randomRes2) {
                randomRes2 = randomizer(nearbyRest.size());
            }

            resTitle1.setText(nearbyRest.get(randomRes1).name);
            resTitle2.setText(nearbyRest.get(randomRes2).name);
            Bitmap phot1 = ((BitmapDrawable) LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes1).photoImage, APIkey)).getBitmap();
            resImage1.setImageBitmap(getRoundedShape(phot1));
            Bitmap phot2 = ((BitmapDrawable) LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes2).photoImage, APIkey)).getBitmap();
            resImage2.setImageBitmap(getRoundedShape(phot2));
/*
            resImage1.setMaxHeight(width/3);
            resImage1.setMaxWidth(width/3);

            resImage2.setMaxHeight(width/3);
            resImage2.setMaxWidth(width/3);*/
/*
            resImage1.setX(0-100);
            resImage1.setY(height/4);
            resImage2.setX(0);
            resImage2.setY(height/2);

            resTitle1.setX(width/4);
            resTitle1.setY(height/2);*/

            Button reroll = (Button) findViewById(R.id.reroll);
            //reroll.setX(width/2);
            reroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    
                    if(randomRes1>randomRes2) {
                        nearbyRest.remove(randomRes1);
                        nearbyRest.remove(randomRes2);
                    }
                    else
                    {
                        nearbyRest.remove(randomRes2);
                        nearbyRest.remove(randomRes1);
                    }
                    randomRes1 = randomizer(nearbyRest.size());
                    randomRes2 = randomizer(nearbyRest.size());
                    while (randomRes1 == randomRes2) {
                        randomRes2 = randomizer(nearbyRest.size());
                    }
                    resTitle1.setText(nearbyRest.get(randomRes1).name);
                    resTitle2.setText(nearbyRest.get(randomRes2).name);
                    Bitmap phot1 = ((BitmapDrawable) LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes1).photoImage, APIkey)).getBitmap();
                    resImage1.setImageBitmap(getRoundedShape(phot1));
                    Bitmap phot2 = ((BitmapDrawable) LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes2).photoImage, APIkey)).getBitmap();
                    resImage2.setImageBitmap(getRoundedShape(phot2));
                }
            });
    }


    public StringBuilder createJSON(StringBuilder URL){
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try
        {
            URL address = new URL(URL.toString());
            conn = (HttpsURLConnection) address.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
            return jsonResults;
        }
        catch(MalformedURLException e)
        {
            Log.i("URL", "Error, bad Url");
            return null;
        }
        catch(IOException e)
        {
            Log.i("IO", "IO exception caught");
            return null;
        }
        finally
        {
            if (conn != null)
                conn.disconnect();
        }

    }
    public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 1200;
        int targetHeight = 1200;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
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
            return Drawable.createFromStream(is, "src");
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
                    latP = pPlace.getLatitude();
                    longP = pPlace.getLongitude();
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
        getSupportActionBar().hide();
        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
        resTitle1 = (TextView) findViewById(R.id.firstRes);
        resImage1 = (ImageView) findViewById(R.id.resIm);
    }

    public Location getLocation() {
        try {
            Location findUser = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            return findUser;
        } catch (SecurityException s) {
            return null;
        }
    }
/*
    public double getLat(Location loc){
            return loc.getLatitude();
        }

    public double getLong(Location loc){
            return loc.getLongitude();
    }
*/
}
