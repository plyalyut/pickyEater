package pickyeater.pickyeaterandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//Google Play functionality
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.Places;
import android.support.v4.app.FragmentActivity;


public class MainMenu extends AppCompatActivity {

    private GoogleApiClient googleApiClient;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);


        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

    }
}
