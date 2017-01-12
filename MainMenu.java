package pickyeater.pickyeaterandroid; //change this

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.location.Location;
import android.os.StrictMode;

import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//Google Play functionality
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.ConnectionResult;
import com.squareup.picasso.Picasso;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import android.util.Log;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import static pickyeater.pickyeaterandroid.Constants.EARTHRADIUS;
import static pickyeater.pickyeaterandroid.Constants.APIkey;
import static pickyeater.pickyeaterandroid.Constants.TYPE;

//Main Menu class, implements failures to connect to Google Play Services
public  class MainMenu extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
                                                        GoogleApiClient.ConnectionCallbacks,
                                                        View.OnClickListener {
    //sets up a textbox & image for the first restaurant
    private TextView resTitle1;
    private ImageView resImage1;
    //sets up a textbox & image for the second restaurant
    private TextView resTitle2;
    private ImageView resImage2;

    //random restaurant numbers
    private int randomRes1;
    private int randomRes2;

    private GoogleApiClient googleApiClient; //Google API Client initialization

    ArrayList<Place> nearbyRest;//sets up the array of nearby places
    JSONArray restArray;//jsonArray for the restaurant results

    public ViewGroup transitions;

    public int searchRadius = 10000; //how far do you want to search
    public double latP;//coordinate for the person's latitude
    public double longP;//coordinate for the person's longitude

    public String location; //latitude & longitude for the location

    private final String GOOGLE = "PlayServices"; //error log identifier

    private int largeImageRes = 200; //resolution for downloaded images (default)
    private int imageRes;  //optimal image resolution in digital pixels

    //When Google Play Connects, logs to console
    @Override
    public void onConnected(Bundle connectionHint) {
        //allows the ability to obtain HTTP access
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //connection success
        Log.d(GOOGLE, "Connection Established");

        //gets the user's current location
        Location pPlace = getLocation();
        if (pPlace != null) {
            latP = pPlace.getLatitude();
            longP = pPlace.getLongitude();
            location = String.valueOf(latP) + "," + String.valueOf(longP); //testString #remove in actual release
        }

        //creates a new string holding the json results
        StringBuilder jsonResults;

        //creates the url to connect to
        location = subselect();
        Log.i("Location", location);

        StringBuilder jsonUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
        jsonUrl.append(location);
        jsonUrl.append("&radius=");
        jsonUrl.append(searchRadius);
        jsonUrl.append("&opennow");
        jsonUrl.append("&type=");
        jsonUrl.append(TYPE);
        jsonUrl.append("&key=");
        jsonUrl.append(APIkey);

        jsonResults = createJSON(jsonUrl); //creates the JSON url and gets the queries
        JSONObject restObj; //creates the JSOn object from the search above
        int numResNearby;
        nearbyRest = new ArrayList<>();

        //checks the size of the JSON file
        try {
            restObj = new JSONObject(jsonResults.toString());
            numResNearby = restObj.getJSONArray("results").length();
            Log.i("Size of list", Integer.toString(numResNearby));
        }
        catch (JSONException e){
            Log.i("No Nearby Restaurants", "");
            numResNearby = 0;
            }
        Log.i("address", jsonUrl.toString());

        Boolean hasNext = true; //sees if there are extra pages to sort from

        nearbyRest = new ArrayList<>();

        do {
            try {
                restObj = new JSONObject(jsonResults.toString());

                restArray = restObj.getJSONArray("results");

                Log.i("Size of list", Integer.toString(restArray.length()));
                hasNext = restObj.has("next_page_token");

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
                if (hasNext) {
                    String nextPage = restObj.getString("next_page_token");
                    jsonUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=");
                    jsonUrl.append(nextPage);
                    jsonUrl.append("&key=");
                    jsonUrl.append(APIkey);
                    jsonResults = createJSON(jsonUrl);
                }
            } catch (JSONException e) {
                Log.i("JSON", "JSON main error");
            }
        } while (hasNext);


        randomRes1 = randomizer(nearbyRest.size());
        randomRes2 = randomizer(nearbyRest.size());
        while (randomRes1 == randomRes2) {
            randomRes2 = randomizer(nearbyRest.size());
        }

        loadRes(largeImageRes, randomRes1, randomRes2, APIkey);
        resImage1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                Drawable[] layers = new Drawable[2];
                layers[0] = resImage1.getDrawable();
                BitmapMem betImage = new BitmapMem();
                Drawable checkdraw = new BitmapDrawable(betImage.decodeSampledBitmapFromResource(getResources(), R.drawable.check, imageRes, imageRes));
                layers[1] = checkdraw;
                layers[1].setAlpha(200);
                LayerDrawable intermedaiteSelected = new LayerDrawable(layers);
                Drawable selected = getSingleDrawable(intermedaiteSelected);
                //Bitmap phot1 = ((BitmapDrawable) selected).getBitmap();
                //resImage1.setImageBitmap(getRoundedShape(phot1));
                resImage1.setImageDrawable(selected);

            }
        }
        );

        /*
        resTitle1.setText(nearbyRest.get(randomRes1).name);
        resTitle2.setText(nearbyRest.get(randomRes2).name);
        Bitmap phot1 = ((BitmapDrawable) LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes1).photoImage, APIkey)).getBitmap();
        resImage1.setImageBitmap(getRoundedShape(phot1));
        Bitmap phot2 = ((BitmapDrawable) LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes2).photoImage, APIkey)).getBitmap();
        resImage2.setImageBitmap(getRoundedShape(phot2));*/

        ImageView reroll = (ImageView) findViewById(R.id.reroll);

        BitmapMem betImage = new BitmapMem();
        //imageResInPx=(int)convertDpToPixel(imageResInDP, getApplicationContext());
        Drawable die = new BitmapDrawable(betImage.decodeSampledBitmapFromResource(getResources(), R.drawable.die, imageRes, imageRes));
        reroll.setImageDrawable(die);

        //reroll.setX(width/2);
        reroll.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View v){
            if (nearbyRest.size() > 4) {
                if (randomRes1 > randomRes2) {
                    nearbyRest.remove(randomRes1);
                    nearbyRest.remove(randomRes2);
                } else {
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

                loadRes(largeImageRes, randomRes1, randomRes2, APIkey);



                /*Drawable[] layers = new Drawable[2];
                layers[0] = LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes1).photoImage, APIkey);
                layers[1] = getResources().getDrawable(R.drawable.check);
                layers[1].setAlpha(200);
                LayerDrawable intermedaiteSelected = new LayerDrawable(layers);
                Drawable selected = getSingleDrawable(intermedaiteSelected);*/
                //Bitmap phot1 = ((BitmapDrawable) selected).getBitmap();
                //resImage1.setImageBitmap(getRoundedShape(phot1));

                //resImage1.setImageDrawable(selected);
                //resImage1.setImageDrawable(layers[0]);
                //Bitmap phot2 = ((BitmapDrawable) LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes2).photoImage, APIkey)).getBitmap();
                //resImage2.setImageBitmap(getRoundedShape(phot2));
                //Drawable phot2 = LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes2).photoImage, APIkey);
                //resImage2.setImageDrawable(phot2);

            } else {
                resTitle1.setText("Error, too small data set!");
            }
        }});

    }

    public void loadRes(int largeImageRes, int randomRes1, int randomRes2, String APIkey) {

        resTitle1.setText(nearbyRest.get(randomRes1).name);
        resTitle2.setText(nearbyRest.get(randomRes2).name);

        Glide.with(getApplicationContext()).load(LoadImageFromWeb1(largeImageRes, nearbyRest.get(randomRes1).photoImage, APIkey)).into(resImage1);
        Glide.with(getApplicationContext()).load(LoadImageFromWeb1(largeImageRes, nearbyRest.get(randomRes2).photoImage, APIkey)).into(resImage2);
        /*Picasso.with(getApplicationContext())
                .load("https://drive.google.com/uc?id=0B_s3LqFjDfvGZjdTaVJEejJOdDg")
                .resize(300,300)
                .into(resImage1);*/
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

    public String subselect(){
        int distFromCent = (int)(searchRadius*Math.random());
        double theta = Math.toRadians(360*Math.random());
        double lati = latP + ((180/Math.PI)*distFromCent/EARTHRADIUS)*Math.sin(theta);
        double longi = longP + ((180/Math.PI)*distFromCent/EARTHRADIUS)*Math.cos(theta);

        return String.valueOf(lati) + "," + String.valueOf(longi);
    }

    public int getMaxRadius(int numRes){
        if(numRes != 0) {
            int smallRadius = (int) Math.sqrt(searchRadius * searchRadius / numRes);
            return (int) (2 * smallRadius * Math.random());
        }
        else
            return 0;
    }

    public Place genSearchCircle(int length) {
        int rad = getMaxRadius(length);
        String loc = subselect();
        Place place = new Place();

        StringBuilder jsonUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
        jsonUrl.append(loc);
        jsonUrl.append("&radius=");
        jsonUrl.append(rad);
        jsonUrl.append("&opennow");
        jsonUrl.append("&type=");
        jsonUrl.append(TYPE);
        jsonUrl.append("&key=");
        jsonUrl.append(APIkey);

        StringBuilder jsonResults = createJSON(jsonUrl); //creates the JSON url and gets the queries
        JSONObject restObj; //creates the JSOn object from the search above

            try {
                restObj = new JSONObject(jsonResults.toString());

                restArray = restObj.getJSONArray("results");
                short randomNearbyRes = (short)(restArray.length()*Math.random());

                place.name = restArray.getJSONObject(randomNearbyRes).getString("name");
                place.placeID = restArray.getJSONObject(randomNearbyRes).getString("place_id");
                try {
                    JSONArray photos = restArray.getJSONObject(randomNearbyRes).getJSONArray("photos");
                    place.photoImage = photos.getJSONObject(0).getString("photo_reference");
                } catch (JSONException e) {
                    Log.i("JSON", "JSON Photo Error");
                }
                return place;

            } catch (JSONException e) {
                Log.i("JSON ERROR", "Error retrieving values");
                return null;
            }
        }

    public Drawable getSingleDrawable(LayerDrawable layerDrawable){

        int resourceBitmapHeight = 1200, resourceBitmapWidth = 1200;

        float widthInInches = 1.0f;

        int widthInPixels = (int)(widthInInches * getResources().getDisplayMetrics().densityDpi);
        int heightInPixels = (int)(widthInPixels * resourceBitmapHeight / resourceBitmapWidth);

        int insetLeft = 0, insetTop = 0, insetRight = 0, insetBottom = 0;

        layerDrawable.setLayerInset(1, insetLeft, insetTop, insetRight, insetBottom);

        Bitmap bitmap = Bitmap.createBitmap(widthInPixels, heightInPixels, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        layerDrawable.setBounds(0, 0, widthInPixels, heightInPixels);
        layerDrawable.draw(canvas);

        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        bitmapDrawable.setBounds(0, 0, widthInPixels, heightInPixels);

        return bitmapDrawable;
    }

    public int randomizer(int length){
        return (int)(length*Math.random());
    }

    public static String LoadImageFromWeb1(int maxwidth, String reference, String APIkey)
    {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?maxwidth=");
        url.append(maxwidth);
        url.append("&photoreference=");
        url.append(reference);
        url.append("&key=");
        url.append(APIkey);

        return url.toString();
    }

    //When connection is lost it tries to reconnect
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(GOOGLE, "Connection lost. Trying to get it back.");
        googleApiClient.connect();
    }

    //Logs a failed connection
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(GOOGLE, "Connection Failed");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.die)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
    }

    //Starts google play services.
    @Override
    public void onStart() {
        super.onStart();
        Log.i(GOOGLE, "Connecting to Google Play."); //Console log for connecting
        GoogleApiAvailability gAv = GoogleApiAvailability.getInstance(); //checks google play is available on this device
        int result = gAv.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            gAv.getErrorDialog(this, result, 1).show();//error if google play is not available & brings up screen to get google play
        } else {
            googleApiClient.connect(); //connects to google play
        }

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

    //to disconnect from google play services
    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            Log.i(GOOGLE, "Disconnecting from Google Play");
            googleApiClient.disconnect();
        }
    }

    //when the app is paused
    @Override
    public void onPause() {
        super.onPause();
        Log.i(GOOGLE, "Pausing Google Play");
    }

    //whent he app is resumed
    @Override
    public void onResume() {
        super.onResume();
        Log.i(GOOGLE, "Resuming Google Play");
    }

    //when the app first is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        getSupportActionBar().hide(); //should not result in errors, hides the action bar
        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

        //convert imageResolution from DP to pixels
        imageRes = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,180, getResources().getDisplayMetrics());

        //instantiate the images to the screen
        resTitle1 = (TextView) findViewById(R.id.firstRes);
        resImage1 = (ImageView) findViewById(R.id.resIm);
        resTitle2 = (TextView) findViewById(R.id.secondRes);
        resImage2 = (ImageView) findViewById(R.id.resIm2);

        Location pPlace = getLocation();
        if (pPlace != null) {
            latP = pPlace.getLatitude();
            longP = pPlace.getLongitude();
            location = String.valueOf(latP) + "," + String.valueOf(longP); //testString #remove in actual release
        }

        transitions = (ViewGroup)findViewById(R.id.activity_main_menu);
        ImageView search = (ImageView)findViewById(R.id.searchPreference);

        //todo set up the settings menu, optimize without lag
        search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Create the transition set animating the click color change of button
                TransitionSet set = new TransitionSet()
                        .addTransition(new TintTransition());
                set.setDuration(200);

                ColorDrawable[] color = {new ColorDrawable(getResources().getColor(R.color.layoutColor)),
                        new ColorDrawable(getResources().getColor(R.color.activatedBackground))};
                TransitionDrawable trans = new TransitionDrawable(color);

                TransitionManager.beginDelayedTransition(transitions, set);
                //((ImageView)v).setImageTintList((ColorStateList.valueOf(0xFF298178)));
                v.setBackgroundDrawable(trans);
                trans.startTransition(200);
            }
        });

        //causes the linear layouts to start listening
        LinearLayout rectRes = (LinearLayout) findViewById(R.id.rectRes1);
        rectRes.setOnClickListener(this);
        LinearLayout rectRes2 = (LinearLayout) findViewById(R.id.rectRes2);
        rectRes2.setOnClickListener(this);
    }

    boolean firstClicked = false;
    boolean secondClicked = false;
    @Override
    public void onClick(View v) {
        TransitionDrawable transition = (TransitionDrawable) v.getBackground();
        switch (v.getId()){
            case R.id.rectRes1:

                if(!firstClicked) {
                    transition.startTransition(200);
                    firstClicked = true;
                    if(secondClicked) {
                        TransitionDrawable transition1 = (TransitionDrawable)
                                findViewById(R.id.rectRes2).getBackground();
                        transition1.reverseTransition(200);
                        secondClicked = false;
                    }
                }
                break;
            case R.id.rectRes2:
                if(!secondClicked) {
                    transition.startTransition(200);
                    secondClicked = true;
                    if(firstClicked) {
                        TransitionDrawable transition2 = (TransitionDrawable)
                                findViewById(R.id.rectRes1).getBackground();
                        transition2.reverseTransition(200);
                        firstClicked = false;
                    }
                }
                break;

        }
    }

    //gets the location of the user at a given time
    //todo add security exceptions and user access
    public Location getLocation() {
        try {
            Location findUser = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            return findUser;
        } catch (SecurityException s) {
            Log.i("Permissions", "Security Exception");
            return null;
        }
    }
}
