package pickyeater.pickyeaterandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.Arrays;


public class Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
                                                         {
    GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth; //intializing firebase
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "Sign In"; //tag for signing in
    private static final int RC_SIGN_IN = 9001; //what google likes to use for their sign-in's

    //create the sign-in screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //facebook login

        //AppEventsLogger.activateApp(this); //logs the amount of times people log in
        //CallbackManager callbackManager = CallbackManager.Factory.create();
        //LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        //google login
       // SignInButton googleButton = (SignInButton) findViewById(R.id.google_button);
        //googleButton.setSize(SignInButton.SIZE_STANDARD);
        //findViewById(R.id.google_button).setOnClickListener(this);

        // Configure Google Sign In
        //GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
         //       .requestIdToken(getString(R.string.default_web_client_id))
          //      .requestEmail()
          //      .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.

        //mGoogleApiClient = new GoogleApiClient.Builder(this)
          //      .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            //    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        //        .build();


        //authentication listener
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null)
        {
            Intent intent = new Intent(this, MainMenu.class);
            startActivity(intent);
            Log.i("photo", mAuth.getCurrentUser().getPhotoUrl().toString());
        }
        else {
            startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
                .setProviders((Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build()))
                )
                .setIsSmartLockEnabled(true)
                .build(), RC_SIGN_IN
        );}
        //mAuthListener = new FirebaseAuth.AuthStateListener() {
        //    @Override
        //    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        //        FirebaseUser user = firebaseAuth.getCurrentUser();
         //       if (user != null) {
         //           // User is signed in
         //           Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
         //       } else {
         //           // User is signed out
           //         Log.d(TAG, "onAuthStateChanged:signed_out");
         //     }
          //  }};

        //gets the google sign in button

    }

    /*
    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.google_button:
                signIn();
                break;

        }
    }*/

    /*
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }*/

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }*/

    /*
    //methods to set up authentication
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    */
    /*
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                Intent intent = new Intent(this, MainMenu.class);
                startActivity(intent);
                finish();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    //showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    //showSnackbar(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    //showSnackbar(R.string.unknown_error);
                    return;
                }
            }

            //showSnackbar(R.string.unknown_sign_in_response);
        }
    }


    //if connection fails to connect to google
    @Override
    public void onConnectionFailed(ConnectionResult result){
        Log.d("GOOGLE", "Connection Failed");
    }

    /*
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }*/

}
