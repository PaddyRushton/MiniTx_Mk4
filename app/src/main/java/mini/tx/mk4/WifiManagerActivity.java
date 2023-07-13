package mini.tx.mk4;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiNetworkSpecifier;

public class WifiManagerActivity {
    private Context context;
    String ConnectionStatus = "Connect";

    public WifiManagerActivity(Context context) {
        this.context = context;
    }



public String connectToWifi(String ssid, String password) {
        WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build();
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                // Connection successful
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                // Connection failed
            }
        });
        ConnectionStatus = "Connected";
    ConnectionStatus = "Connect";

    return ConnectionStatus;
    }
}