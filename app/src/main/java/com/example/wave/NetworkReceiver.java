package com.example.wave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {

    public interface NetworkChangeListener {
        void onNetworkRestored();
    }

    private final NetworkChangeListener listener;

    public NetworkReceiver(NetworkChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Log.d("NetworkReceiver", "Internet connection restored.");
            if (listener != null) {
                listener.onNetworkRestored();
            }
        } else {
            Log.d("NetworkReceiver", "No internet connection.");
        }
    }

}

