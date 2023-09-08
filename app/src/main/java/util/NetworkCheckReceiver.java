package util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NetworkCheckReceiver extends BroadcastReceiver {
    public static boolean isNetwork = true;
    ConnectivityManager connectivityManager;
    public NetworkCheckReceiver(ConnectivityManager connectivityManager){
        this.connectivityManager=connectivityManager;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            if(connectivityManager.getActiveNetworkInfo()!=null){
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null){
                    if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){
                        isNetwork=true;
                    }
                }
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null){
                    if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()){
                        isNetwork=true;
                    }
                }
            }else{
                isNetwork=false;
            }
        }
    }
}
