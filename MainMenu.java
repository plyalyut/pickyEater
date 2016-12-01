package pickyeater.pickyeaterandroid;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.os.StrictMode;

import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//Google Play functionality
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.common.ConnectionResult;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.ChangeImageTransform;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;


import android.util.Log;


import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    //getting the dimensions of the screen
    int width;
    int height;

    //sets up the array of nearby places
    ArrayList<Place> nearbyRest;

    //jsonArray for the restaurant results
    JSONArray restArray;

    public final int searchRadius = 10000; //how far do you want to search
    public double latP;//coordinate for the person's latitude
    public double longP;//coordinate for the person's longitude
    public final String type = "restaurant";
    private final String APIkey = "AIzaSyD0wuYcL5-fir9uciCfaZXLSb2IIM8_RB0"; //our API key ### GET AN ACTUAL KEY UPON RELEASE
    //private String APIkey = "AIzaSyC8sTME_6ujyMOiA690lldcUr7FrcAKtLY"; //backup key for teting purposes
    public String location; //latitude & longitude displayed for ##testing purposes

    //Console Tag for Error Identification
    private final String TAG = "PlayServices"; //error log identifier

    private final int largeImageRes = 300; //resolution for downloaded images (default)
    private final int smallImageRes = 200; //resolution for downloaded images (if cannot load large)
    private final int imageResInDP = 120;
    private int imageResInPx;


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

        //sets the text to the location value #testing purposes
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

        jsonResults = createJSON(jsonUrl);

        JSONObject restObj;
        Boolean hasNext = true;

        nearbyRest = new ArrayList<>();
        do {
            try {
                restObj = new JSONObject(jsonResults.toString());
                restArray = restObj.getJSONArray("results");
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

        loadRes(smallImageRes,largeImageRes, randomRes1, randomRes2, APIkey);
        resImage1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                Drawable[] layers = new Drawable[2];
                layers[0] = resImage1.getDrawable();
                layers[1] = getResources().getDrawable(R.drawable.check);
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
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.die, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;

        BitmapMem betImage = new BitmapMem();
        imageResInPx=(int)betImage.convertDpToPixel(imageResInDP, getApplicationContext());
        betImage.decodeSampledBitmapFromResource(getResources(), R.drawable.die, imageResInPx, imageResInPx);



        reroll.setImageDrawable(getResources().getDrawable(R.drawable.die));
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

                loadRes(smallImageRes,largeImageRes, randomRes1, randomRes2, APIkey);

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

        Button win = (Button) findViewById(R.id.win);
    }



    public void loadRes(int smallImageRes,int largeImageRes, int randomRes1, int randomRes2, String APIkey){
        Drawable phot1;
        Drawable phot2;

        resTitle1.setText(nearbyRest.get(randomRes1).name);
        resTitle2.setText(nearbyRest.get(randomRes2).name);

        if (LoadImageFromWeb(largeImageRes, nearbyRest.get(randomRes1).photoImage, APIkey)!=null
            && LoadImageFromWeb(largeImageRes, nearbyRest.get(randomRes2).photoImage, APIkey)!=null
            ) {
                phot1 = LoadImageFromWeb(largeImageRes, nearbyRest.get(randomRes1).photoImage, APIkey);
                        //((BitmapDrawable) LoadImageFromWeb(largeImageRes, nearbyRest.get(randomRes1).photoImage, APIkey)).getBitmap();
                //resImage1.setImageBitmap(getRoundedShape(phot1));
                resImage1.setImageDrawable(phot1);
                phot2 = LoadImageFromWeb(largeImageRes, nearbyRest.get(randomRes2).photoImage, APIkey);
                        //((BitmapDrawable) LoadImageFromWeb(largeImageRes, nearbyRest.get(randomRes2).photoImage, APIkey)).getBitmap();
                //resImage2.setImageBitmap(getRoundedShape(phot2));
                resImage2.setImageDrawable(phot2);
        }
        else if(LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes1).photoImage, APIkey)!=null
                && LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes2).photoImage, APIkey)!=null
        ) {
            phot1 = LoadImageFromWeb(largeImageRes, nearbyRest.get(randomRes1).photoImage, APIkey);
                    //((BitmapDrawable) LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes1).photoImage, APIkey)).getBitmap();
            //resImage1.setImageBitmap(getRoundedShape(phot1));
            resImage1.setImageDrawable(phot1);
            phot2 = LoadImageFromWeb(largeImageRes, nearbyRest.get(randomRes2).photoImage, APIkey);
                    //((BitmapDrawable) LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes2).photoImage, APIkey)).getBitmap();
            //resImage2.setImageBitmap(getRoundedShape(phot2));
            resImage2.setImageDrawable(phot2);
        }
        else{
            if(LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes1).photoImage, APIkey)==null){
                phot1 = getResources().getDrawable(R.drawable.lost);
                        //((BitmapDrawable) getResources().getDrawable(R.drawable.lost)).getBitmap();
                //resImage1.setImageBitmap(getRoundedShape(phot1));
                resImage1.setImageDrawable(phot1);
            }
            if(LoadImageFromWeb(smallImageRes, nearbyRest.get(randomRes2).photoImage, APIkey)==null){
                phot2 =  getResources().getDrawable(R.drawable.lost);
                        //((BitmapDrawable) getResources().getDrawable(R.drawable.lost)).getBitmap();
                //resImage2.setImageBitmap(getRoundedShape(phot2));
                resImage2.setImageDrawable(phot2);
            }
        }}



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
    /*
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
    }*/

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
        Log.i(TAG, "Connecting to Google Play."); //Console log for connecting
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

    public void showResults(int winner, ImageView res1, ImageView res2, ImageView random) {
        Drawable[] layers = new Drawable[2];

        if (winner == 0) {
            layers[0] = res1.getDrawable();
            layers[1] = getResources().getDrawable(R.drawable.check);
            layers[1].setAlpha(200);
            LayerDrawable intermediateSelected = new LayerDrawable(layers);
            Drawable selected = getSingleDrawable(intermediateSelected);
            resImage1.setImageDrawable(selected);
            layers[0].mutate();
            layers[1].mutate();

            layers[0] = res2.getDrawable();

            layers[1] = getResources().getDrawable(R.drawable.x);
            layers[1].setAlpha(200);
            intermediateSelected = new LayerDrawable(layers);
            selected = getSingleDrawable(intermediateSelected);
            resImage2.setImageDrawable(selected);

            layers[0] = random.getDrawable();


            layers[1] = getResources().getDrawable(R.drawable.x);
            layers[1].setAlpha(200);
            intermediateSelected = new LayerDrawable(layers);
            selected = getSingleDrawable(intermediateSelected);
            random.setImageDrawable(selected);

        } else if (winner == 1) {
            layers[0] = res2.getDrawable();
            layers[1] = getResources().getDrawable(R.drawable.check);
            layers[1].setAlpha(200);
            LayerDrawable intermediateSelected = new LayerDrawable(layers);
            Drawable selected = getSingleDrawable(intermediateSelected);
            resImage1.setImageDrawable(selected);
            layers[0].mutate();
            layers[1].mutate();

            layers[0] = res1.getDrawable();

            layers[1] = getResources().getDrawable(R.drawable.x);
            layers[1].setAlpha(200);
            intermediateSelected = new LayerDrawable(layers);
            selected = getSingleDrawable(intermediateSelected);
            resImage2.setImageDrawable(selected);

            layers[0] = random.getDrawable();

            layers[1] = getResources().getDrawable(R.drawable.x);
            layers[1].setAlpha(200);
            intermediateSelected = new LayerDrawable(layers);
            selected = getSingleDrawable(intermediateSelected);
            random.setImageDrawable(selected);
        } else {
            layers[0] = random.getDrawable();
            layers[1] = getResources().getDrawable(R.drawable.check);
            layers[1].setAlpha(200);
            LayerDrawable intermediateSelected = new LayerDrawable(layers);
            Drawable selected = getSingleDrawable(intermediateSelected);
            resImage1.setImageDrawable(selected);
            layers[0].mutate();
            layers[1].mutate();

            layers[0] = res1.getDrawable();

            layers[1] = getResources().getDrawable(R.drawable.x);
            layers[1].setAlpha(200);
            intermediateSelected = new LayerDrawable(layers);
            selected = getSingleDrawable(intermediateSelected);
            resImage2.setImageDrawable(selected);

            layers[0] = res2.getDrawable();

            layers[1] = getResources().getDrawable(R.drawable.x);
            layers[1].setAlpha(200);
            intermediateSelected = new LayerDrawable(layers);
            selected = getSingleDrawable(intermediateSelected);
            random.setImageDrawable(selected);
        }
    }

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

    //
    public Location getLocation() {
        try {
            Location findUser = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            return findUser;
        } catch (SecurityException s) {
            return null;
        }
    }
}
