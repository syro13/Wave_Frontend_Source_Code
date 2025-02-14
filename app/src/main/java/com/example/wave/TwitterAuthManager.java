package com.example.wave;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

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

    public void signIn() {
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
                        Toast.makeText(activity, "Sign In with X Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }
    interface Callback{
        void updateUI(FirebaseUser user);
    }
}
