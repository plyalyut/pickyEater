package pickyeater.pickyeaterandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//Google Play functionality
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import android.support.v4.app.FragmentActivity;
import android.widget.TextView;


public  class MainMenu extends AppCompatActivity //implements GoogleApiClient.OnConnectionFailedListener
    {
        TextView test = (TextView) findViewById(R.id.testbox);

        private GoogleApiClient googleApiClient;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main_menu);

            googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                //.addOnConnectionFailedListener(this)
                .build();
            //sets up a placepicker
            int PLACE_PICKER_REQUEST = 1;
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (com.google.android.gms.common.GooglePlayServicesRepairableException e) {
                test.setText("Error with Google Play Services");
            } catch (com.google.android.gms.common.GooglePlayServicesNotAvailableException e) {
                test.setText("Error with Google Play Services");
            }
        }
    }

