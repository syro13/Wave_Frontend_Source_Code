package com.example.wave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {

    public interface NetworkChangeListener {
        void onNetworkConnected();
        void onNetworkDisconnected();
    }

    private final NetworkChangeListener listener;
    private boolean wasConnected = true; // To prevent duplicate callbacks

    public NetworkReceiver(NetworkChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (listener != null) {
            if (isConnected && !wasConnected) {
                Log.d("NetworkReceiver", "Internet reconnected.");
                listener.onNetworkConnected();
            } else if (!isConnected && wasConnected) {
                Log.d("NetworkReceiver", "Internet disconnected.");
                listener.onNetworkDisconnected();
            }
        }

        wasConnected = isConnected;
    }
}

