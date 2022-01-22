package fr.isep.ii3510.movieman.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fr.isep.ii3510.movieman.utils.NetworkConn;

public class ConnBroadcastReceiver extends BroadcastReceiver {

    private ConnReceiverListener mConnReceiverListener;

    public ConnBroadcastReceiver(ConnReceiverListener mConnReceiverListener) {
        this.mConnReceiverListener = mConnReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mConnReceiverListener != null && NetworkConn.isConnected(context))
            mConnReceiverListener.onNetworkConnectionConnected();
    }


    public interface ConnReceiverListener {
        void onNetworkConnectionConnected();
    }

}
