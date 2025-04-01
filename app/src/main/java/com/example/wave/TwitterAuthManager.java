package com.example.wave;

import android.app.Activity;
import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import java.util.List;

public class TwitterAuthManager{

    private static TwitterAuthManager instance = null;
    private Context context;
    private FirebaseAuth firebaseAuth;
    private OAuthProvider.Builder provider;
    private FirebaseUser firebaseUser;
    private Activity activity;
    private Callback callback;

    private TwitterAuthManager() {
    }

    public static TwitterAuthManager getInstance(Activity activity, Callback callback) {
        if (instance == null) {
            instance = new TwitterAuthManager();
        }
        instance.init(activity,callback);
        return instance;
    }

    private void init(Activity activity, Callback callback){
        this.activity = activity;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.provider = OAuthProvider.newBuilder("twitter.com");
        this.callback = callback;
    }
    private void clearSessionData() {
        // Clear WebView cookies to prevent OAuth session issues
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();

        // Clear WebView storage
        WebStorage.getInstance().deleteAllData();

        // Sign out from Firebase to clear cached credentials
        firebaseAuth.signOut();
    }
    public void signIn() {
        clearSessionData();
        firebaseAuth
                .startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        callback.updateUI(user);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            // Firebase detects an existing account with the same email
                            Toast.makeText(activity, "An account with this email already exists.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(activity, "Sign In with X Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void showProfile(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        callback.updateUI(user);
    }

    boolean isUserAlreadySignedIn(){
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null){
            return true;
        } else{
            return false;
        }
    }
    void signOut(){
        firebaseAuth.signOut();
        clearSessionData();
    }
    interface Callback{
        void updateUI(FirebaseUser user);
    }
}
